//
//  LiveParameter.java
//  xal
//
//  Created by Thomas Pelaia II on 2/21/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.ca.*;
import xal.tools.messaging.MessageCenter;
import xal.tools.data.*;
import xal.smf.AcceleratorNode;

import java.util.*;
import java.util.logging.*;


/** agent for managing a parameter's connections */
public class LiveParameter implements KeyedRecord, CoreParameterListener {
	/** indicates that the variable settings should come frm teh design values */
	static public final int DESIGN_SOURCE = CoreParameter.DESIGN_SOURCE;
	
	/** indicates that the variable settings should come from the current control values */
	static public final int CONTROL_SOURCE = CoreParameter.CONTROL_SOURCE;
	
	/** indicates that the variable settings should come from the custom settins */
	static public final int CUSTOM_SOURCE = CoreParameter.CUSTOM_SOURCE;
	
	/** constant variable state value indicating that a parameter is variable */
	static public final Integer IS_VARIABLE = CoreParameter.IS_VARIABLE;
	
	/** constant variable state value indicating that a parameter is not variable */
	static public final Integer IS_NOT_VARIABLE = CoreParameter.IS_NOT_VARIABLE;
	
	/** table of instances which can fetch a particular value from a parameter */
	static private final Map<String, ValueGetter> VALUE_GETTERS;
	
	/** key for getting a paramter's name */
	static public final String NAME_KEY = "PARAMETER_NAME";
	
	/** key for getting a parameter's type */
	static public final String TYPE_KEY = CoreParameter.TYPE_KEY;
	
	/** key for getting a parameter's position */
	static public final String POSITION_KEY = NodeAgent.POSITION_KEY;
	
	/** key for getting a paramter's property accessor field */
	static public final String ACCESSOR_FIELD_KEY = CoreParameter.ACCESSOR_FIELD_KEY;
	
	/** key for getting a parameter's variable state */
	static public final String VARIABLE_KEY = CoreParameter.VARIABLE_KEY;
	
	/** key for getting a parameter's design value */
	static public final String DESIGN_VALUE_KEY = "DESIGN_VALUE";
	
	/** key for getting a parameter's initial value */
	static public final String INITIAL_VALUE_KEY = CoreParameter.INITIAL_VALUE_KEY;
	
	/** key for getting a parameter's control value */
	static public final String CONTROL_VALUE_KEY = "CONTROL_VALUE";
	
	/** key for getting a parameter's lower limit */
	static public final String LOWER_LIMIT_KEY = CoreParameter.LOWER_LIMIT_KEY;
	
	/** key for getting a parameter's upper limit */
	static public final String UPPER_LIMIT_KEY = CoreParameter.UPPER_LIMIT_KEY;
	
	/** Message center for dispatching events to registered listeners. */
	private final MessageCenter _messageCenter;
	
	/** Proxy which forwards events to registered listeners. */
	private final LiveParameterListener _eventProxy;
	
	/** node agent for which this is a parameter */
	protected final NodeAgent _nodeAgent;
	
	/** core parameter that provides the custom settings and may be shared by other live parameters */
	protected final CoreParameter _coreParameter;
	
	/** parameter name */
	protected final String _name;
	
	/** latest readback value */
	volatile protected double _latestReadbackValue;
	
	/** latest control value */
	volatile protected double _latestControlValue;
	
	/** control limits cache */
	protected double[] _controlLimits;
	
	/** readback channel */
	protected final Channel _readbackChannel;
	
	/** control channel */
	protected final Channel _controlChannel;
	
	/** readback channel monitor */
	protected final MonitorController _readbackMonitor;
	
	/** handles readback monitor events */
	protected MonitorEventListener _readbackEventHandler;
	
	/** control channel monitor */
	protected final MonitorController _controlMonitor;
	
	/** handles control monitor events */
	protected MonitorEventListener _controlEventHandler;
	
	/** design value */
	protected final double _designValue;
	
	/** design limits */
	protected final double[] _designLimits;
	
	
	// static initializer
	static {
		VALUE_GETTERS = new HashMap<String, ValueGetter>();
		populateValueGetters();
	}
	
	
	/** 
	 * Primary constructor
	 * @param nodeAgent the node agent for which this is a parameter
	 * @param coreParameter the core parameter that provides the custom settings and may be shared by other live parameters
	 */
	public LiveParameter( final NodeAgent nodeAgent, final CoreParameter coreParameter ) {
		_messageCenter = new MessageCenter( "Live Parameter" );
		_eventProxy = _messageCenter.registerSource( this, LiveParameterListener.class );
		
		final ParameterTypeAdaptor typeAdaptor = coreParameter.getTypeAdaptor();
		
		_name = typeAdaptor.getName();
		_nodeAgent = nodeAgent;
		_coreParameter = coreParameter;
		
		_coreParameter.addCoreParameterListener( this );
		_coreParameter.addLiveParameter( this );
		
		
		_designValue = typeAdaptor.getDesignValue( nodeAgent );
		_designLimits = typeAdaptor.getDesignLimits( nodeAgent, _designValue );
		
		if ( getActiveSource() == DESIGN_SOURCE ) {
			// be sure to set the limits first
			setLowerLimit( _designLimits[0] );
			setUpperLimit( _designLimits[1] );
			setInitialValue( _designValue );
		}
		
		// set limits first
		setCustomLimits( _designLimits );
		setCustomValue( _designValue );
		
		_latestReadbackValue = Double.NaN;
		_latestControlValue = Double.NaN;
		_controlLimits = null;
		
		_readbackChannel = typeAdaptor.getReadbackChannel( nodeAgent );
		_readbackMonitor = new MonitorController( _readbackChannel );
		
		_controlChannel = typeAdaptor.getControlChannel( nodeAgent );
		_controlMonitor = new MonitorController( _controlChannel );
		
		requestMonitor();
	}
	
	
	/** dispose of this parameter's resources */
	public void dispose() {
		_messageCenter.removeSource( this, LiveParameterListener.class );
		
		if ( _readbackMonitor != null ) {
			_readbackMonitor.removeMonitorEventListener( _readbackEventHandler );
			_readbackMonitor.dispose();
		}
		
		if ( _controlChannel != null ) {
			_controlMonitor.removeMonitorEventListener( _controlEventHandler );
			_controlMonitor.dispose();
		}
	}
	
	
	/**
	 * Override the inherited method to provide more information about this parameter.
	 * @return a string representation of this parameter
	 */
	public String toString() {
		return "node: " + _nodeAgent + ", parameter: " + getName();
	}
	
	
	/** Populate the value getters */
	static private void populateValueGetters() {
		VALUE_GETTERS.put( TYPE_KEY, new ValueGetter() {
			public Object getValue( final LiveParameter parameter ) { return parameter.getCoreParameter().getType(); }
		});
		
		VALUE_GETTERS.put( NAME_KEY, new ValueGetter() {
			public Object getValue( final LiveParameter parameter ) { return parameter.getName(); }
		});
		
		VALUE_GETTERS.put( DESIGN_VALUE_KEY, new ValueGetter() {
			public Object getValue( final LiveParameter parameter ) { return new Double( parameter.getDesignValue() ); }
		});
		
		VALUE_GETTERS.put( ACCESSOR_FIELD_KEY, new ValueGetter() {
			public Object getValue( final LiveParameter parameter ) { return parameter.getAccessorField(); }
		});
		
		VALUE_GETTERS.put( VARIABLE_KEY, new ValueGetter() {
			public Object getValue( final LiveParameter parameter ) { return parameter.isVariable() ? IS_VARIABLE : IS_NOT_VARIABLE; }
		});
		
		VALUE_GETTERS.put( INITIAL_VALUE_KEY, new ValueGetter() {
			public Object getValue( final LiveParameter parameter ) { return new Double( parameter.getInitialValue() ); }
		});
		
		VALUE_GETTERS.put( CONTROL_VALUE_KEY, new ValueGetter() {
			public Object getValue( final LiveParameter parameter ) { return new Double( parameter.getLatestControlValue() ); }
		});
		
		VALUE_GETTERS.put( LOWER_LIMIT_KEY, new ValueGetter() {
			public Object getValue( final LiveParameter parameter ) { return new Double( parameter.getLowerLimit() ); }
		});
		
		VALUE_GETTERS.put( UPPER_LIMIT_KEY, new ValueGetter() {
			public Object getValue( final LiveParameter parameter ) { return new Double( parameter.getUpperLimit() ); }
		});		
	}
	
	
	/** Request a monitor for the readback and control channels. */
	private void requestMonitor() {
		_readbackEventHandler = new MonitorEventListener() {
			public void valueChanged( Channel channel, ChannelTimeRecord record ) {
				final double value = _coreParameter.getTypeAdaptor().toPhysical( _nodeAgent, record.doubleValue() );
				if ( value != _latestReadbackValue ) {
					_latestReadbackValue = value;
					_eventProxy.readbackValueChanged( LiveParameter.this, value );					
				}
			}
			
			
			public void connectionChanged( Channel channel, boolean isConnected ) {
				_latestReadbackValue = Double.NaN;
				_eventProxy.readbackConnectionChanged( LiveParameter.this, isConnected );
			}
		};
		_readbackMonitor.addMonitorEventListener( _readbackEventHandler );
		
		_controlEventHandler = new MonitorEventListener() {
			public void valueChanged( Channel channel, ChannelTimeRecord record ) {
				final double value = _coreParameter.getTypeAdaptor().toPhysical( _nodeAgent, record.doubleValue() );
				if ( value != _latestControlValue ) {
					_latestControlValue = value;
					if ( getActiveSource() == CONTROL_SOURCE ) {
						setInitialValue( _latestControlValue );
					}
					_eventProxy.controlValueChanged( LiveParameter.this, value );					
				}
			}
			
			
			public void connectionChanged( Channel channel, boolean isConnected ) {
				//_latestControlValue = Double.NaN;
				if ( getActiveSource() == CONTROL_SOURCE ) {
					setLowerLimit( getControlLimits()[0] );
					setUpperLimit( getControlLimits()[1] );
				}
				_eventProxy.controlConnectionChanged( LiveParameter.this, isConnected );
			}
		};
		_controlMonitor.addMonitorEventListener( _controlEventHandler );
		
		_readbackMonitor.requestMonitor();
		_controlMonitor.requestMonitor();
	}
	
	
	/**
	 * Add the specified listener to receive live parameter event notifications from this object.
	 * @param listener the listener to receive the event notifications.
	 */
	public void addLiveParameterListener( final LiveParameterListener listener ) {
		_messageCenter.registerTarget( listener, this, LiveParameterListener.class );
		listener.controlValueChanged( this , getLatestControlValue() );
		listener.readbackValueChanged( this, getLatestReadbackValue() );
		listener.readbackConnectionChanged( this, _readbackChannel.isConnected() );
		listener.controlConnectionChanged( this, _controlChannel.isConnected() );
		listener.variableStatusChanged( this, isVariable() );
		listener.variableSourceChanged( this, getActiveSource() );
		listener.initialValueChanged( this, getInitialValue() );
		listener.lowerLimitChanged( this, getLowerLimit() );
		listener.upperLimitChanged( this, getUpperLimit() );
	}
	
	
	/**
	 * Remove the specified listener from receiving live parameter event notifications from this object.
	 * @param listener the listener to remove from receiving event notifications.
	 */
	public void removeLiveParameterListener( final LiveParameterListener listener ) {
		_messageCenter.removeTarget( listener, this, LiveParameterListener.class );
	}
	
	
	/**
	 * Get the value associated with the specified key.
	 * @param key The key for which to get the associated value.
	 * @return The value as an Object.
	 */
    public Object valueForKey( final String key ) {
		final ValueGetter valueGetter;
		synchronized( VALUE_GETTERS ) {
			valueGetter = VALUE_GETTERS.get(key);			
		}
		
		return ( valueGetter != null ) ? valueGetter.getValue( this ) : _nodeAgent.valueForKey( key );
	}
	
	
	/**
	 * Get the associated accelerator node.
	 * @return this parameter's associated accelerator node.
	 */
	public AcceleratorNode getNode() {
		return _nodeAgent.getNode();
	}
	
	
	/**
	 * Get the position of this parameter within the selected sequence.
	 * @return the position of this parameter relative to the selected sequence
	 */
	public double getPosition() {
		return _nodeAgent.getPosition();
	}
	
	
	/**
	 * Get a new comparator for sorting live parameters by position
	 * @return a comparator that can sort live parameters by position
	 */
	static public Comparator<LiveParameter> getPositionComparator() {
		return new Comparator<LiveParameter>() {
			/** Compare two parameters */
			public int compare( final LiveParameter firstParameter, final LiveParameter secondParameter ) {
				final double position1 = firstParameter.getPosition();
				final double position2 = secondParameter.getPosition();
				return position1 == position2 ? 0 : position1 < position2 ? -1 : 1;
			}
			
			/** determine if the specified comparator is equal to this one */
			public boolean equals( final Object comparator ) {
				return this == comparator;
			}
		};
	}
	
	
	/**
	 * Sort the parameters by position.
	 * @param parameters the list of parameters to sort in place
	 */
	static public void sortByPosition( final List<LiveParameter> parameters ) {
		Collections.sort( parameters, getPositionComparator() );
	}
	
	
	/**
	 * Get this parameter's node agent.
	 * @return this parameter's node agent.
	 */
	public NodeAgent getNodeAgent() {
		return _nodeAgent;
	}
	
	
	/**
	 * Get the core parameter.
	 * @return the core parameter
	 */
	public CoreParameter getCoreParameter() {
		return _coreParameter;
	}
	
	
	/**
	 * Get the name of this parameter.
	 * @return the name of this parameter.
	 */
	public String getName() {
		return _name;
	}
	
	
	/**
	 * Get the property accessor field.
	 * @return the property accessor field used in online models.
	 */
	protected String getAccessorField() {
		return _coreParameter.getAccessorField();
	}
	
	
	/**
	 * Determine if the readback and control channels are connected.
	 */
	public boolean isConnected() {
		return _controlChannel.isConnected();
	}
	
	
	/**
	 * Get the design value.
	 * @return the design value of this parameter.
	 */
	public double getDesignValue() {
		return _designValue;
	}
	
	
	/**
	 * Get the design limits.
	 * @return the design limits [lower, upper] in that order
	 */
	public double[] getDesignLimits() {
		return _designLimits;
	}
	
	
	/**
	 * Get the latest readback value.
	 * @return the latest readback value.
	 */
	public double getLatestReadbackValue() {
		return _latestReadbackValue;
	}
	
	
	/**
	 * Get the latest control value.
	 * @return the latest control value.
	 */
	public double getLatestControlValue() {
		return _latestControlValue;
	}
	
	
	/**
	 * Get the control limits as a range.
	 * @return the array consisting of the lower and upper control limits.
	 */
	public double[] getControlLimits() {
		if ( _controlLimits == null && _controlChannel.isConnected() ) {
			try {
				_controlLimits = toPhysical( new double[] { 
					_controlChannel.lowerControlLimit().doubleValue(), 
					_controlChannel.upperControlLimit().doubleValue() 
				} );
			}
			catch( Exception exception ) {
				_controlLimits = null;
			}
		}
		
		return _controlLimits;
	}
	
	
	/**
	 * Get this parameter's initial variable value.
	 * @return the initial value
	 */
	public double getInitialValue() {
		return toPhysical( _coreParameter.getInitialValue() );
	}
	
	
	/**
	 * Get this custom value.
	 * @return the custom value
	 */
	public double getCustomValue() {
		return toPhysical( _coreParameter.getCustomValue() );
	}
	
	
	/**
	 * Set this parameter's custom value.
	 * @param value the new custom value
	 */
	public void setCustomValue( final double value ) {
		_coreParameter.setCustomValue( toCA( value ) );
	}
	
	
	/**
	 * Get this custom limits.
	 * @return the custom limits
	 */
	public double[] getCustomLimits() {
		return toPhysical( _coreParameter.getCustomLimits() );
	}
	
	
	/**
	 * Set this parameter's custom limits.
	 * @param limits the new custom limits
	 */
	public void setCustomLimits( final double[] limits ) {
		_coreParameter.setCustomLimits( toCA( limits ) );
	}
	
	
	/** 
	 * Set the custom limits relative to the current custom value.
	 * @param relativeLimit relative limit as a fraction to be applied to the current custom value
	 */
	public void setRelativeCustomLimits( final double relativeLimit ) {
		final double currentValue = getCustomValue();
		final double lowerLimit = currentValue * ( 1.0 - relativeLimit );
		final double upperLimit = currentValue * ( 1.0 + relativeLimit );
		setCustomLimits( new double[] { lowerLimit, upperLimit } );
	}
	
	
	/**
	 * Set this parameter's initial variable value.
	 * @param value the new initial value
	 */
	public void setInitialValue( final double value ) {
		if ( getActiveSource() == CUSTOM_SOURCE ) {
			_coreParameter.setInitialValue( toCA( value ) );
		}
		else {
			final double boundedValue = Math.max( getLowerLimit(), Math.min( value, getUpperLimit() ) );
			_coreParameter.setInitialValue( toCA( boundedValue ) );
		}
	}
	
	
	/**
	 * Upload the initial value to the control system.
	 * @return true upon successful request (which does not guarrantee successful upload) and false upon failure
	 */
	public boolean uploadInitialValue() {
		try {
			final boolean status = _coreParameter.getTypeAdaptor().uploadValue( this, getInitialValue() );
			if ( status == true ) {
				final String message = getNodeAgent() + " " + getName() +  " " + getInitialValue() + " upload to the accelerator requested.";
				Logger.getLogger( "global" ).log( Level.INFO, message );				
			}
			return status;
		}
		catch( Exception exception ) {
			final String message = getNodeAgent() + " " + getName() +  " " + getInitialValue() + " upload request to the accelerator has failed.";
			Logger.getLogger( "global" ).log( Level.WARNING, message, exception );
			return false;
		}
	}
	
	
	/**
	 * Get this parameter's lower variable limit.
	 * @return the lower limit
	 */
	public double getLowerLimit() {
		return Math.min( toPhysical( _coreParameter.getLowerLimit() ), toPhysical( _coreParameter.getUpperLimit() ) );
	}
	
	
	/**
	 * Set this parameter's lower variable limit.
	 * @param limit the new lower limit
	 */
	public void setLowerLimit( final double limit ) {
		final double caLimit = toCA( limit );
		
		// check if the conversion flips the sign of the values
		if ( toPhysical( _coreParameter.getLowerLimit() ) <= toPhysical( _coreParameter.getUpperLimit()  ) ) {
			_coreParameter.setLowerLimit( caLimit );
		}
		else {
			_coreParameter.setUpperLimit( caLimit );
		}
	}
	
	
	/**
	 * Get this parameter's upper variable limit.
	 * @return this parameter's upper limit.
	 */
	public double getUpperLimit() {
		return Math.max( toPhysical( _coreParameter.getLowerLimit() ), toPhysical( _coreParameter.getUpperLimit() ) );
	}
	
	
	/**
	 * Set this parameter's upper variable limit.
	 * @param limit the new upper limit
	 */
	public void setUpperLimit( final double limit ) {
		final double caLimit = toCA( limit );
		
		// check if the conversion flips the sign of the values
		if ( toPhysical( _coreParameter.getLowerLimit() ) <= toPhysical( _coreParameter.getUpperLimit()  ) ) {
			_coreParameter.setUpperLimit( caLimit );
		}
		else {
			_coreParameter.setLowerLimit( caLimit );
		}
	}
	
	
	/**
	 * Determine if this parameter is variable.
	 * @return true if this parameter is variable and false if not.
	 */
	public boolean isVariable() {
		return _coreParameter.isVariable();
	}
	
	
	/**
	 * Set whether this parameter should be variable.
	 * @param isVariable true to indicate that this parameter is variable and false to indicate that it is not
	 */
	public void setIsVariable( final boolean isVariable ) {
		_coreParameter.setIsVariable( isVariable );
	}
	
	
	/**
	 * Get the name of the specified source.
	 * @param source the indicator of the specified source
	 * @return the name of the specified source.
	 */
	static public String getSourceName( final int source ) {
		return CoreParameter.getSourceName( source );
	}
	
	
	/**
	 * Get the name of the active source.
	 * @return the name of the active source.
	 */
	public String getActiveSourceName() {
		return _coreParameter.getActiveSourceName();
	}
	
	
	/**
	 * Get the indicator of the current active source.
	 * @return the indicator of this variable's source.
	 */
	public int getActiveSource() {
		return _coreParameter.getActiveSource();
	}
	
	
	/**
	 * Set this parameter's active source to the one specified.
	 * @param source DESIGN_SOURCE, CONTROL_SOURCE or CUSTOM_SOURCE
	 */
	public void setActiveSource( final int source ) {
		_coreParameter.setActiveSource( source );
	}
	
	
	/** Copy design settings to custom settings. */
	public void copyDesignToCustom() {
		setCustomValue( getDesignValue() );
		setCustomLimits( getDesignLimits() );
	}
	
	
	/** Copy control settings to custom settings. */
	public void copyControlToCustom() {
		if ( isConnected() ) {
			setCustomValue( getLatestControlValue() );
			setCustomLimits( getControlLimits() );			
		}
	}
	
	
	/** Copy control limits to custom limits. */
	public void copyControlLimitsToCustom() {
		if ( isConnected() ) {
			setCustomLimits( getControlLimits() );			
		}
	}
	
	
	/**
	 * Convert the channel access value to a physical one.
	 */
	public double toPhysical( final double caValue ) {
		return _coreParameter.getTypeAdaptor().toPhysical( _nodeAgent, caValue );
	}
	
	
	/**
	 * Convert the channel access value to a physical one.
	 */
	protected double[] toPhysical( final double[] caLimits ) {
		final double lowerLimit = toPhysical( caLimits[0] );
		final double upperLimit = toPhysical( caLimits[1] );
		
		// conversion may change order, so take care
		if ( lowerLimit < upperLimit ) {
			return new double[] { lowerLimit, upperLimit };
		}
		else {
			return new double[] { upperLimit, lowerLimit };
		}
	}
	
	
	/**
	 * Convert the physical value to a channel access one.
	 */
	public double toCA( final double value ) {
		return _coreParameter.getTypeAdaptor().toCA( _nodeAgent, value );
	}
	
	
	/**
	 * Convert the physical value to a channel access one.
	 */
	protected double[] toCA( final double[] limits ) {
		final double lowerLimit = toCA( limits[0] );
		final double upperLimit = toCA( limits[1] );
		
		// conversion may change order, so take care
		if ( lowerLimit < upperLimit ) {
			return new double[] { lowerLimit, upperLimit };
		}
		else {
			return new double[] { upperLimit, lowerLimit };
		}
	}
	
	
	/**
	 * Handle the event in which the parameter's variable status has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param isVariable indicates whether the parameter is now variable or not
	 */
	public void variableStatusChanged( final CoreParameter parameter, final boolean isVariable ) {
		_eventProxy.variableStatusChanged( this, isVariable );
	}
	
	
	/**
	 * Handle the event in which the parameter's custom value has changed.
	 * @param parameter the core parameter whose value has changed.
	 * @param value the new custom value
	 */
	public void customValueChanged( final CoreParameter parameter, final double value ) {
		_eventProxy.customValueChanged( this, getCustomValue() );
	}
	
	
	/**
	 * Handle the event in which the parameter's custom limits have changed.
	 * @param parameter the core parameter whose limits have changed.
	 * @param limits the new custom limits
	 */
	public void customLimitsChanged( final CoreParameter parameter, final double[] limits ) {
		_eventProxy.customLimitsChanged( this, getCustomLimits() );
	}
	
	
	/**
	 * Handle the event in which the parameter's initial variable value has changed.
	 * @param parameter the core parameter whose value has changed.
	 * @param value the new initial variable value
	 */
	public void initialValueChanged( final CoreParameter parameter, final double value ) {
		_eventProxy.initialValueChanged( this, getInitialValue() );
	}
	
	
	/**
	 * Handle the event in which the parameter's lower variable limit has changed.
	 * @param parameter the core parameter whose value has changed.
	 * @param caLimit the new lower variable CA limit
	 */
	public void lowerLimitChanged( final CoreParameter parameter, final double caLimit ) {
		// must send both since the physical limits may be flipped with the CA limits
		_eventProxy.lowerLimitChanged( this, getLowerLimit() );
		_eventProxy.upperLimitChanged( this, getUpperLimit() );
	}
	
	
	/**
	 * Handle the event in which the parameter's upper variable limit has changed.
	 * @param parameter the core parameter whose value has changed.
	 * @param caLimit the new upper variable CA limit
	 */
	public void upperLimitChanged( final CoreParameter parameter, final double caLimit ) {
		// must send both since the physical limits may be flipped with the CA limits
		_eventProxy.upperLimitChanged( this, getUpperLimit() );
		_eventProxy.lowerLimitChanged( this, getLowerLimit() );
	}
	
	
	/**
	 * Handle the event in which the parameter's variable source has changed.
	 * @param parameter the core parameter whose variable source has changed.
	 * @param source the indicator of the parameter's variable source
	 */
	public void variableSourceChanged( CoreParameter parameter, int source ) {
		switch( source ) {
			case CONTROL_SOURCE:
				setInitialValue( _latestControlValue );
				final double[] limits = getControlLimits();
				setLowerLimit( limits != null ? limits[0] : 0 );
				setUpperLimit( limits != null ? limits[1] : 0 );
				break;
			case DESIGN_SOURCE:
				setInitialValue( _designValue );
				setLowerLimit( _designLimits[0] );
				setUpperLimit( _designLimits[1] );
				break;
			case CUSTOM_SOURCE:
				setInitialValue( getCustomValue() );
				setLowerLimit( getCustomLimits()[0] );
				setUpperLimit( getCustomLimits()[1] );
				break;
			default:
				break;
		}
		
		_eventProxy.variableSourceChanged( this, source );		
	}
	
	
	
	/** Instances can get a particular value from a parameter. */
	interface ValueGetter {
		/**
		 * Get a particular value from the specified parameter.
		 * @param parameter the parameters whose particular value is returned.
		 */
		public Object getValue( final LiveParameter parameter );
	}
}


