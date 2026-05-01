/*
 * ScopeModel.java
 *
 * Created on December 17, 2002, 1:49 PM
 */

package xal.app.scope;

import xal.extension.application.*;
import xal.tools.data.*;
import xal.tools.ArrayMath;
import xal.tools.messaging.MessageCenter;
import xal.tools.correlator.*;
import xal.ca.correlator.*;
import xal.ca.*;

import java.util.*;


/**
 * Main model for the scope.  This model manages the individual channel models 
 * as a group.  It listens to those models and forwards their data for display 
 * on the scope screen.  It creates the channel models and handles the plumbing 
 * to the stake holders.
 *
 * @author  tap
 */
public class ScopeModel implements CorrelationNotice<ChannelTimeRecord>, ChannelModelListener, TriggerListener, XalDocumentListener, DataListener  {
    // messaging variables
    final private MessageCenter MESSAGE_CENTER;
    final private TraceListener TRACE_EVENT_PROXY;
    final private SettingListener SETTING_EVENT_PROXY;

    // models
    protected ChannelModel[] channelModels;
    protected MathModel[] mathModels;
	protected TraceSource[] traceSources;
    protected TimeModel timeModel;
    protected Trigger trigger;
	protected RawHistoryKeeper rawHistoryKeeper;
    
    // variables
    protected ChannelCorrelator correlator;
    protected PeriodicPoster<ChannelTimeRecord> poster;
    
    
    /** Creates a new instance of ScopeModel */
    public ScopeModel() {
        timeModel = new TimeModel();
        
        trigger = new Trigger();
        trigger.addTriggerListener(this);
        
        createChannelModels();
        createMathModels();
		traceSources = new TraceSource[mathModels.length + channelModels.length];
		System.arraycopy(channelModels, 0, traceSources, 0, channelModels.length);
		System.arraycopy(mathModels, 0, traceSources, channelModels.length, mathModels.length);
		
		// make the timestamp broad enough for the clock synchronization precision
        correlator = new ChannelCorrelator(0.12);	// 0.12 second timespan tolerance
        correlator.setCorrelationFilter( CorrelationFilterFactory.<ChannelTimeRecord>minCountFilter(1) );
        poster = new PeriodicPoster<>( correlator, 0.2 );   // default to 5 Hz refresh
        poster.addCorrelationNoticeListener( this );
		
		rawHistoryKeeper = new RawHistoryKeeper( channelModels );
		poster.addCorrelationNoticeListener( rawHistoryKeeper );
        
        MESSAGE_CENTER = new MessageCenter("Scope Model");
        TRACE_EVENT_PROXY = MESSAGE_CENTER.registerSource(this, TraceListener.class);
        SETTING_EVENT_PROXY = MESSAGE_CENTER.registerSource(this, SettingListener.class);
    }
	
	
	/**
	 * Dispose of the ScopeModel's resources.
	 */
	void dispose() {
		stopMonitor();
		
		poster.removeCorrelationNoticeListener( rawHistoryKeeper );
		rawHistoryKeeper.dispose();
		rawHistoryKeeper = null;
				
        for ( int index = 0 ; index < channelModels.length ; index++ ) {
			channelModels[index].dispose();
			channelModels[index] = null;
        }
        
        timeModel.dispose();
		timeModel = null;
		
		correlator.dispose();
		correlator = null;
	}
    
    
    /** 
     * dataLabel() provides the name used to identify the class in an 
     * external data source.
     * @return The tag for this data node.
     */
    public String dataLabel() {
        return "ScopeModel";
    }
    
    
    /**
     * Instructs the receiver to update its data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update(DataAdaptor adaptor) {
        if ( adaptor.hasAttribute("refreshPeriod") ) {
            setSweepPeriod( adaptor.doubleValue("refreshPeriod") );
        }
		
		if ( adaptor.hasAttribute("correlationWindow") ) {
			setCorrelatorTimespan( adaptor.doubleValue("correlationWindow") );
		}
        
        DataAdaptor timeAdaptor = adaptor.childAdaptor(TimeModel.dataLabel);
        if ( timeAdaptor != null ) {
            timeModel.update(timeAdaptor);
        }
        
        final List<DataAdaptor> channelAdaptors = adaptor.childAdaptors( ChannelModel.DATA_LABEL );
        int channelCount = channelAdaptors.size();
        
        for ( int index = 0 ; index < channelCount ; index++ ) {
            final DataAdaptor channelAdaptor = channelAdaptors.get( index );
            try {
                channelModels[index].update(channelAdaptor);
            }
            catch(ChannelSetException exception) {
                System.err.println(exception);
            }
        }
        
        final List<DataAdaptor> mathAdaptors = adaptor.childAdaptors( MathModel.dataLabel );
        int mathCount = mathAdaptors.size();
        for ( int index = 0 ; index < mathCount ; index++ ) {
            final DataAdaptor mathAdaptor = mathAdaptors.get( index );
            mathModels[index].update( mathAdaptor );
        }
        
        final DataAdaptor triggerAdaptor = adaptor.childAdaptor( Trigger.dataLabel );
        if ( triggerAdaptor != null ) {
            trigger.update( triggerAdaptor );
        }
    }
    
    
    /**
     * Instructs the receiver to write its data to the adaptor for external
     * storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write(DataAdaptor adaptor) {
        adaptor.setValue("refreshPeriod", getSweepPeriod());
		adaptor.setValue("correlationWindow", getCorrelatorTimespan());
        
        adaptor.writeNode(timeModel);
        
        for ( int index = 0 ; index < channelModels.length ; index++ ) {
            adaptor.writeNode( channelModels[index] );
        }
        
        for ( int index = 0 ; index < mathModels.length ; index++ ) {
            adaptor.writeNode( mathModels[index] );
        }
        
        adaptor.writeNode(trigger);
    }
    
    
    /**
     * Add the listener to be notified when a setting has changed.  Register this
     * model and all of its submodels.
     * @param listener Object to receive setting change events.
     */
    void addSettingListener(SettingListener listener) {
        MESSAGE_CENTER.registerTarget(listener, this, SettingListener.class);
        timeModel.addSettingListener(listener);
        for ( int index = 0 ; index < channelModels.length ; index++ ) {
            channelModels[index].addSettingListener(listener);
        }
        for ( int index = 0 ; index < mathModels.length ; index++ ) {
            mathModels[index].addSettingListener(listener);
        }
        trigger.addSettingListener(listener);
    }
    
    
    /**
     * Remove the listener as a receiver of setting change events.
     * @param listener Object to remove from receiving setting change events.
     */
    void removeSettingListener(SettingListener listener) {
        MESSAGE_CENTER.removeTarget(listener, this, SettingListener.class);
        timeModel.removeSettingListener(listener);
        for ( int index = 0 ; index < channelModels.length ; index++ ) {
            channelModels[index].removeSettingListener(listener);
        }
        for ( int index = 0 ; index < mathModels.length ; index++ ) {
            mathModels[index].removeSettingListener(listener);
        }
        trigger.removeSettingListener(listener);
    }
    
    
    /** 
	 * Add a TraceListener to monitor TraceListener events from this instance.
	 * @param listener The new listener of Trace events.
	 */
    public void addTraceListener(TraceListener listener) {
        MESSAGE_CENTER.registerTarget(listener, this, TraceListener.class);
    }
    
    
    /** 
	 * Remove a TraceListener from monitoring TraceListener events from this instance.
	 * @param listener The listener of trace events to remove.
	 */
    public void removeTraceListener(TraceListener listener) {
        MESSAGE_CENTER.removeTarget(listener, this, TraceListener.class);
    }
    
    
    /** Create the channel models for the scope */
    protected void createChannelModels() {
        final int count = 4;
        channelModels = new ChannelModel[count];
        
        for ( int index = 0 ; index < count ; index++ ) {
            String channelId = "ch" + (index+1);
            ChannelModel channelModel = new ChannelModel(channelId, timeModel);
            channelModels[index] = channelModel;
            channelModel.addChannelModelListener(this);
        }
    }
    
    
    /** Create the math models for the scope */
    protected void createMathModels() {
        final int count = 3;
        mathModels = new MathModel[count];
        
        for ( int index = 0 ; index < count ; index++ ) {
            String mathId = "math" + (index+1);
            MathModel mathModel = new MathModel(mathId, channelModels, timeModel);
            mathModels[index] = mathModel;
        }
    }
	
	
	/**
	 * Get the raw history keeper for this model.
	 * @return This model's raw history keeper.
	 */
	public RawHistoryKeeper getRawHistoryKeeper() {
		return rawHistoryKeeper;
	}
    
    
    /** 
     * Get the number of channels available on the scope 
     * @return The number of channel models in this scope.
     */
    public int numChannels() {
        return channelModels.length;
    }
    
    
    /** 
     * Get the channel model specified by the index.
     * @param index The index of the channel model to fetch.
     * @return The channel model corresponding to the specified index.
     */
    public ChannelModel getChannelModel(int index) {
        return channelModels[index];
    }
    
    
    /** 
     * Get the channel model specified by the pv.
     * @param pvName The name of the process variable.
     * @return The channel model corresponding to the specified PV.
     */
    public ChannelModel getChannelModelWithPV(String pvName) {
		ChannelModel channelModel = null;
		for ( int index = 0 ; index < channelModels.length ; index++ ) {
			if ( channelModels[index].getChannel() != null ) {
				if ( channelModels[index].getChannel().channelName().equals(pvName) ) {
					return channelModels[index];
				}
			}
		}
		
		return channelModel;
    }
    
    
    /** 
     * Get the math model specified by the index.
     * @param index The index of the math model to fetch.
     * @return The math model corresponding to the specified index.
     */
    public MathModel getMathModel(int index) {
        return mathModels[index];
    }
    
    
    /** 
     * Get the trace source by index.
     * @param index The index of the trace source to fetch.
     * @return The trace source corresponding to the specified index.
     */
    public TraceSource getTraceSource(int index) {
        return traceSources[index];
    }
    
    
    /**
     * Get the time model.
     * @return the time model
     */
    public TimeModel getTimeModel() {
        return timeModel;
    }
    
    
    /** 
     * Get the trigger for this model.
     * @return The trigger for this model.
     */
    public Trigger getTrigger() {
        return trigger;
    }
	
	
	/**
	 * Get the timespan tolerance of the correlator.
	 * @return The timespan tolerance of the correlator in seconds.
	 */
	public double getCorrelatorTimespan() {
		return correlator.binTimespan();
	}
	
	
	/**
	 * Set the timespan tolerance of the correlator.
	 * @param newTimespan The new timespan tolerance of the correlator in seconds.
	 */
	public void setCorrelatorTimespan(double newTimespan) {
		if ( newTimespan != correlator.binTimespan() ) {
			correlator.setBinTimespan(newTimespan);
            SETTING_EVENT_PROXY.settingChanged(this);
		}
	}
    
    
    /** Start monitoring the channels */
    public void startMonitor() {
        correlator.startMonitoring();
        poster.start();
    }
    
    
    /** Stop monitoring the channels */
    public void stopMonitor() {
        poster.stop();
        correlator.stopMonitoring();
    }
    
    
    /** 
     * Set the sweep period in seconds.
     * @param sweepPeriod The period in seconds for each screen refresh.
     */
    public void setSweepPeriod(double sweepPeriod) {
        if ( sweepPeriod != getSweepPeriod() ) {
            poster.setPeriod(sweepPeriod);
            SETTING_EVENT_PROXY.settingChanged(this);
        }
    }
    
    
    /** 
     * Get the sweep period in seconds.  This is the delay between screen refreshes.
     * @return sweep period in seconds.
     */
    public double getSweepPeriod() {
        return poster.getPeriod();
    }


	/**
	 * Get the most recent snapshot of the waveforms.
	 * @return the most recent snapshot of waveform data.
	 */
	public WaveformSnapshot getRawWaveformSnapshot() {
		return rawHistoryKeeper.getWaveformSnapshot();
	}
	
    
    /** 
     * Handle a new correlation event from the correlator.
     * @param sender The correlator which fired the event.
     * @param correlation The correlation record.
     */
    public void newCorrelation( final Object sender, final Correlation<ChannelTimeRecord> correlation ) {
        TraceEvent[] traceEvents = new TraceEvent[traceSources.length];
        
        for ( int index = 0 ; index < traceSources.length ; index++ ) {
            final TraceSource traceSource = traceSources[index];
			traceEvents[index] = traceSource.getTraceEvent( correlation );
        }

        postTraces(traceEvents, correlation.meanDate());
    }
    
    
    /** 
     * Handle an empty correlation event.  This method gets called when a 
     * timed correlator timesout and no correlations were found within that 
     * timeout period.
     * @param sender The correlator that fired the event.
     */
    public void noCorrelationCaught(Object sender) {
    }
    
    
    /** 
     * Post the new traces to the registered TraceListener instances.
     * @param traceEvents The new array of trace events.
     * @param timestamp The average time stamp for the event that generated the traces.
     */
    public void postTraces(final TraceEvent[] traceEvents, final Date timestamp) {
        TRACE_EVENT_PROXY.updateTraces(this, traceEvents, timestamp);
    }
    
    
    /** 
     * Handle the ChannelModelListener event indicating that the specified channel is being enabled.
     * @param channelModel ChannelModel posting the event.
     * @param channel The channel being enabled.
     */
    synchronized public void enableChannel( final ChannelModel channelModel, final Channel channel ) {
		if ( channelModel.canMonitor() && !correlator.hasSource( channelModel.getID() ) ) {
			correlator.addChannel( channel, channelModel.getID() );
		}
    }
    
    
    /**
     * Handle the ChannelModelListener event indicating that the specified channel is being disabled.
     * @param channelModel ChannelModel posting the event.
     * @param channel The channel being disabled.
     */
    synchronized public void disableChannel( final ChannelModel channelModel, final Channel channel ) {
        if ( channel != null && correlator != null && correlator.hasSource( channelModel.getID() ) ) {
            try {
                correlator.removeChannel( channelModel.getID() );
            }
            catch(Exception exception) {
				System.err.println(exception);
				exception.printStackTrace();
            }
        }
    }
    
    
    /**
     * Event indicating that the channel model has a new channel.
     * @param source ChannelModel posting the event.
     * @param channel The new channel.
     */
    synchronized public void channelChanged(ChannelModel source, Channel channel) {
		if ( source.canMonitor() ) {
			enableChannel(source, channel);
		}
		else {
			disableChannel(source, channel);
		}
    }
    
    
    /**
     * Event indicating that the channel model has a new array of element times.
     * @param source ChannelModel posting the event.
     * @param elementTimes The new element times array measured in turns.
     */
    public void elementTimesChanged(ChannelModel source, final double[] elementTimes) {
    }
    
    
    /**
     * Handle the TriggerListener event indicating that the specified trigger has been enabled.
     * @param source Trigger posting the event.
     */
    public void triggerEnabled( final Trigger source ) {
        final Channel channel = source.getChannel();
        final String channelName = channel.channelName();
        correlator.addChannel( channel, source.getRecordFilter() );
        correlator.setCorrelationFilter( new CorrelationFilter<ChannelTimeRecord>() {
            public boolean accept( final Correlation<ChannelTimeRecord> correlation, final int fullCount ) {
                return correlation.isCorrelated( channelName ) && ( correlation.numRecords() > 1 );
            }
        });
    }
    
    
    /**
     * Handle the TriggerListener event indicating that the specified trigger has been disabled.
     * @param source Trigger posting the event.
     */
    public void triggerDisabled( final Trigger source ) {
        try {
            Channel channel = source.getChannel();
            if ( channel != null && correlator.hasSource( channel.channelName() ) ) {
                correlator.removeChannel(channel);
            }
        }
        catch(Exception exception) {
			System.err.println(exception);
			exception.printStackTrace();
        }
        correlator.setCorrelationFilter( CorrelationFilterFactory.<ChannelTimeRecord>minCountFilter(1) );
    }
    
    
    /**
     * Event indicating that the trigger channel state has changed.
     * @param source Trigger posting the event.
     */
    public void channelStateChanged(Trigger source) {}
    
    
    /**
     * Implement XalDocumentListener interface
     * Handle the title having changed for a document.
     * @param document The document whose title changed.
     * @param newTitle The new document title.
     */
    public void titleChanged(XalDocument document, String newTitle) {
    }
    
    
    /**
     * Implement XalDocumentListener interface
     * Handle a change in the whether a document has changes that need saving.
     * @param document The document whose change status changed
     * @param newHasChangesStatus The new "hasChanges" status of the document.
     */
    public void hasChangesChanged(XalDocument document, boolean newHasChangesStatus) {
    }
    
    
    /**
     * Implement XalDocumentListener interface
     * Handle a the event indicating that a document is about to close.
     * @param document The document that will close.
     */
    public void documentWillClose(XalDocument document) {
		dispose();
    }
    
    
    /**
     * Implement XalDocumentListener interface
     * Handle the event in which a document has closed.
     * @param document The document that has closed.
     */
    public void documentHasClosed(XalDocument document) {
    }
}



