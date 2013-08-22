/*
 * MathModel.java
 *
 * Created on July 29, 2003, 10:37 AM
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import xal.tools.formula.*;
import xal.tools.LinearInterpolator;
import xal.tools.correlator.*;
import xal.tools.dispatch.DispatchQueue;
import xal.tools.messaging.MessageCenter;
import xal.tools.data.*;
import xal.ca.*;

import java.util.concurrent.Callable;
import java.util.*;


/**
 * MathModel generates a waveform from a formula which references one or more channel
 * inputs.
 *
 * @author  tap
 */
public class MathModel implements TraceSource, DataListener, ChannelModelListener {
    // constants
    final static String dataLabel = "MathModel";
    
    // properties
    protected String id;

    // messaging variables
    final private MessageCenter MESSAGE_CENTER;
    final private SettingListener SETTING_PROXY;
    
    // formula infrastructure
    protected String _formula;
    protected FormulaInterpreter _interpreter;
    
    // state variables
    protected boolean hasCompileErrors;
    protected boolean canEnable;
    protected boolean enabled;
    
    // model variables
    protected TimeModel timeModel;
    protected ChannelModel[] allSources;
    protected ChannelModel[] sources;
    protected double startTurn;
    protected double turnStep;

    // data variables
    protected double[] elementTimes;

	/** queue to synchronize busy state for modifications and access */
	private final DispatchQueue BUSY_QUEUE;
    
    
    /** Creates a new instance of MathModel */
    public MathModel( final String newId, final ChannelModel[]  newSources, final TimeModel aTimeModel ) {
		BUSY_QUEUE = DispatchQueue.createConcurrentQueue( "math model busy" );

        id = newId;
        timeModel = aTimeModel;
        
        MESSAGE_CENTER = new MessageCenter( "Channel Model" );
        SETTING_PROXY = MESSAGE_CENTER.registerSource(this, SettingListener.class);

        _interpreter = new FormulaInterpreter();
        canEnable = false;
        enabled = false;
        hasCompileErrors = false;
        
        allSources = newSources;
        sources = new ChannelModel[0];
    }
    
    
    /**
     * Get the identifier label for this model.
     * @return The identifier label for this model.
     */
    public String getId() {
        return id;
    }
    	
	
	/**
	 * Get the label for this math model
	 * @return The model's formula.
	 */
	public String getLabel() {
		return _formula;
	}

    
    /** 
     * dataLabel() provides the name used to identify the class in an 
     * external data source.
     * @return The tag for this data node.
     */
    public String dataLabel() {
        return dataLabel;
    }
    
    
    /**
     * Instructs the receiver to update its data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update( final DataAdaptor adaptor ) throws ChannelSetException {
        try {
            if ( adaptor.hasAttribute("formula") ) {
                setFormula( adaptor.stringValue("formula") );
            }
        }
        catch( RuntimeException exception ) {
            System.err.println( exception );
        }
        
        setEnabled( adaptor.booleanValue("enabled") );
    }
    
    
    /**
     * Instructs the receiver to write its data to the adaptor for external
     * storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write(DataAdaptor adaptor) {
        if ( _formula != null ) {
            adaptor.setValue("formula", _formula);
        }
        adaptor.setValue("enabled", enabled);
    }
    
    
    /**
     * Add the listener to be notified when a setting has changed.
     * @param listener Object to receive setting change events.
     */
    void addSettingListener( final SettingListener listener ) {
        MESSAGE_CENTER.registerTarget( listener, this, SettingListener.class );
    }
    
    
    /**
     * Remove the listener as a receiver of setting change events.
     * @param listener Object to remove from receiving setting change events.
     */
    void removeSettingListener( final SettingListener listener ) {
        MESSAGE_CENTER.removeTarget( listener, this, SettingListener.class );
    }


	/** post the channel change event */
	private void postSettingChangeEvent() {
		DispatchQueue.getGlobalDefaultPriorityQueue().dispatchAsync( new Runnable() {
			public void run() {
				SETTING_PROXY.settingChanged( MathModel.this );
			}
		});
	}

    
    /**
     * Set the formula for this model to manage and compile it.
     * @param newFormula The formula to manage.
     */
    public void setFormula( final String newFormula ) throws RuntimeException {
		try {
			// create fresh interpreter with no variables assigned
			final FormulaInterpreter interpreter = new FormulaInterpreter();
			interpreter.compile( newFormula );

			dispatchUpdateOperation( new Runnable() {
				public void run() {
					_formula = newFormula;
					_interpreter = interpreter;
					hasCompileErrors = false;

					setupSources();

					setCanEnable( true );
					setEnabled( true );

					postSettingChangeEvent();
				}
			});
		}
		catch( RuntimeException exception ) {
			setCanEnable( false );
			hasCompileErrors = true;
			throw exception;
		}
    }
    
    
    /**
     * Identify which sources are referenced in the formula and setup this model
     * to listen to those sources.
     */
    protected void setupSources() {
		dispatchUpdateOperation( new Runnable() {
			public void run() {
				for ( int index = 0 ; index < sources.length ; index++ ) {
					sources[index].removeChannelModelListener( MathModel.this );
				}

				final ChannelModel[] tempSources = new ChannelModel[allSources.length];
				int numSources = 0;
				for ( int index = 0 ; index < allSources.length ; index++ ) {
					final String sourceID = allSources[index].getID();
					if ( _formula.contains( sourceID ) ) {
						tempSources[numSources++] = allSources[index];
					}
				}
				sources = new ChannelModel[numSources];
				System.arraycopy( tempSources, 0, sources, 0, numSources );

				for ( int index = 0 ; index < sources.length ; index++ ) {
					sources[index].addChannelModelListener( MathModel.this );
				}

				updateElementTimes();
			}
		});
    }


    /**
     * Get the formula managed by this model.
     * @return This model's formula.
     */
    public String getFormula() {
        return _formula;
    }
    
    
    /**
     * Determine if this model can be enabled.
     * @return true if this model can be enabled and false otherwise.
     */
    public boolean canEnable() {
        return canEnable;
    }
    
    
    /**
     * Used internally to set whether this model is capable of being enabled.
     * If the model can't be enabled it also disables the model if necessary.
     * @param state The requested enable state.  True if the model can be enabled and false if not.
     */
    protected void setCanEnable(boolean state) {
        canEnable = state;
        if ( !canEnable ) {
            setEnabled(false);
        }
    }
    
    
    /**
     * Determine whether this math model is enabled to produce a waveform.
     * @return True if this model is enabled and false if not.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    
    /**
     * Attempt to enable the math model so it produces a waveform.
     * @param state The requested enable state.  True to enable and false to disable.
     */
    public void setEnabled( final boolean state ) {
		dispatchUpdateOperation( new Runnable() {
			public void run() {
				if ( enabled != state && (canEnable || !state) ) {
					enabled = state;
					postSettingChangeEvent();
				}
			}
		});
    }
    
    
    /** Toggle the enable */
    public void toggleEnable() {
        setEnabled( !enabled );
    }
    
    
    /**
     * Determine whether the formula has any compile errors.
     * @return true if the formula has compile errors and false otherwise.
     */
    public boolean hasCompileErrors() {
        return hasCompileErrors;
    }
    
    
    /**
     * Get the array of time elements for the waveform.  Each time element represents
     * the time associated with the corresponding waveform element.  The time unit is a turn.
     * @return The element times in units of turns relative to cycle start.
     */
    final public double[] getElementTimes() {
        return elementTimes;
    }


	/** dispatch an operation that updates this model */
	private void dispatchUpdateOperation( final Runnable operation ) {
		// run the operation immediately if on the busy queue, otherwise dispatch asynchronously
		if ( DispatchQueue.getCurrentQueue() == BUSY_QUEUE ) {
			operation.run();
		}
		else {
			BUSY_QUEUE.dispatchBarrierAsync( operation );
		}
	}

    
    /**
     * Get the trace for the formula given the correlation as a data source.
     * @return the waveform trace
     */
    final public double[] getTrace( final Correlation<ChannelTimeRecord> correlation ) {
        if ( !enabled || correlation == null )  return null;
        
        final LinearInterpolator[] interpolators = new LinearInterpolator[sources.length];
        final String[] channelKeys = new String[sources.length];
        for ( int sourceIndex = 0 ; sourceIndex < sources.length ; sourceIndex++ ) {
            final ChannelModel sourceCopy = sources[sourceIndex].cheapCopy();
			if ( !sourceCopy.canMonitor() )  return null;
			
			final String channelKey = sourceCopy.getID();
			channelKeys[sourceIndex] = channelKey;

			if ( !correlation.isCorrelated( channelKey ) )  return null;      // one or more variables undefined
			
			double delay = sourceCopy.getWaveformDelay();
			double samplePeriod = sourceCopy.getSamplePeriod();
			double[] rawArray = correlation.getRecord( channelKey ).doubleArray();
			if ( rawArray.length != sourceCopy.getNumElements() )  return null;	// check for consistency
			interpolators[sourceIndex] = new LinearInterpolator( rawArray, delay, samplePeriod );
        }
        
        final double[] values = new double[elementTimes.length];
        for ( int sampleIndex = 0 ; sampleIndex < elementTimes.length ; sampleIndex++ ) {
            double time = startTurn + sampleIndex * turnStep;
            for ( int sourceIndex = 0 ; sourceIndex < sources.length ; sourceIndex++ ) {
                _interpreter.setVariable(channelKeys[sourceIndex], interpolators[sourceIndex].calcValueAt(time));
            }
            values[sampleIndex] = _interpreter.evaluate();
        }
        
        return values;
    }
	
	
    /**
     * Get the trace event for this trace source extracted from the correlation.
	 * @param correlation The correlation from which the trace is extracted.
	 * @return the trace event corresponding to this trace source and the correlation
     */
	final public TraceEvent getTraceEvent( final Correlation<ChannelTimeRecord> correlation ) {
		return BUSY_QUEUE.dispatchSync( new Callable<TraceEvent>() {
			public TraceEvent call() {
				return new MathTraceEvent( MathModel.this, getTrace( correlation ), elementTimes );
			}
		});
	}

    
    /**
     * Calculate the start turn, turn step, last turn and element times of the 
     * formula based waveform.  Since this waveform is calculated from multiple 
     * sources, we must generate a time range of elements that covers the 
     * intersection of times.  So, the start time of the waveform is the latest
     * start time among the sources.  The end time of the waveform is the earliest
     * end time among the sources.  The sample time step is the shortest of the
     * source time steps.
     */
    final private void updateElementTimes() {
		dispatchUpdateOperation( new Runnable() {
			public void run() {
				startTurn = Double.MIN_VALUE;
				turnStep = Double.MAX_VALUE;
				double lastTurn = Double.MAX_VALUE;

				for ( int index = 0 ; index < sources.length ; index++ ) {
					final ChannelModel sourceCopy = sources[index].cheapCopy();
					if ( sourceCopy.canMonitor() ) {
						int numElements = sourceCopy.getNumElements();

						if ( numElements == 0 )  continue;

						double samplePeriod = sourceCopy.getSamplePeriod();
						double waveformDelay = sourceCopy.getWaveformDelay();

						turnStep = Math.min( turnStep, samplePeriod);
						startTurn = Math.max( startTurn, waveformDelay);

						double lastSourceTurn = waveformDelay + numElements * samplePeriod;
						lastTurn = Math.min(lastTurn, lastSourceTurn);
					}
				}

				try {
					int count = (int) ( (lastTurn - startTurn) / turnStep );
					elementTimes = new double[count];
					double nextTime = startTurn;
					for ( int index = 0 ; index < count ; index++ ) {
						elementTimes[index] = nextTime;
						nextTime += turnStep;
					}
					timeModel.convertTurns(elementTimes);
				}
				catch(Exception exception) {
					elementTimes = new double[0];
				}
			}
		});
	}
    
    
    /**
     * Event indicating that the specified channel is being enabled.
     * @param source ChannelModel posting the event.
     * @param channel The channel being enabled.
     */
    public void enableChannel(ChannelModel source, Channel channel) {
    }
    
    
    /**
     * Event indicating that the specified channel is being disabled.
     * @param source ChannelModel posting the event.
     * @param channel The channel being disabled.
     */
    public void disableChannel(ChannelModel source, Channel channel) {
    }
    
    
    /**
     * Event indicating that the channel model has a new channel.
     * @param source ChannelModel posting the event.
     * @param channel The new channel.
     */
    public void channelChanged(ChannelModel source, Channel channel) {
        updateElementTimes();
    }
    
    
    /**
     * Event indicating that the channel model has a new array of element times.
     * @param source ChannelModel posting the event.
     * @param elementTimes The new element times array measured in turns.
     */
    public void elementTimesChanged(ChannelModel source, final double[] elementTimes) {
        updateElementTimes();
    }
}
