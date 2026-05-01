//
//  CoreParameter.java
//  xal
//
//  Created by Thomas Pelaia on 6/8/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.tools.data.*;
import xal.tools.messaging.MessageCenter;
import xal.tools.data.*;

import java.util.*;


/** agent for managing a parameter's unique custom settings */
public class CoreParameter implements KeyedRecord, DataListener {
	/** data label */
	static public final String DATA_LABEL = "CoreParameter";
	
	/** indicates that the variable settings should come frm teh design values */
	static public final int DESIGN_SOURCE = 0;
	
	/** indicates that the variable settings should come from the current control values */
	static public final int CONTROL_SOURCE = 1;
	
	/** indicates that the variable settings should come from the custom settins */
	static public final int CUSTOM_SOURCE = 2;
	
	/** constant variable state value indicating that a parameter is variable */
	static public final Integer IS_VARIABLE = new Integer(1);
	
	/** constant variable state value indicating that a parameter is not variable */
	static public final Integer IS_NOT_VARIABLE = new Integer(0);
	
	/** table of instances which can fetch a particular value from a parameter */
	static private final Map<String, ValueGetter> VALUE_GETTERS;
	
	/** key for getting a paramter's name */
	static public final String NAME_KEY = "PARAMETER_NAME";
	
	/** key for getting a parameter's type */
	static public final String TYPE_KEY = "TYPE";
	
	/** key for getting a paramter's property accessor field */
	static public final String ACCESSOR_FIELD_KEY = "ACCESSOR_FIELD";
	
	/** key for getting a parameter's variable state */
	static public final String VARIABLE_KEY = "VARIABLE";
	
	/** key for getting a parameter's initial value */
	static public final String INITIAL_VALUE_KEY = "INITIAL_VALUE";
	
	/** key for getting a parameter's lower limit */
	static public final String LOWER_LIMIT_KEY = "LOWER_LIMIT";
	
	/** key for getting a parameter's upper limit */
	static public final String UPPER_LIMIT_KEY = "UPPER_LIMIT";
	
	/** Message center for dispatching events to registered listeners. */
	private final MessageCenter _messageCenter;
	
	/** Proxy which forwards events to registered listeners. */
	private final CoreParameterListener _eventProxy;
	
	/** parameter name */
	protected final String _name;
	
	/** adaptor which allows this parameter to perform operations correctly for the specific type of parameter */
	protected final ParameterTypeAdaptor _typeAdaptor;
	
	/** property accessor field */
	protected final String _accessorField;
	
	/** associated live parameters */
	final protected List<LiveParameter> _liveParameters;
	
	/** source for the variable settings */
	protected int _activeSource;
	
	/** custom value in CA units since they can be shared */
	protected double _customValue;
	
	/** custom limits in CA units since they can be shared */
	protected final double[] _customLimits;
	
	/** initial value for the variable in CA units since they can be shared */
	protected double _initialValue;
	
	/** lower variable limit in CA units since they can be shared */
	protected double _lowerLimit;
	
	/** upper variable limit in CA units since they can be shared */
	protected double _upperLimit;
	
	/** indicates whether this parameter is variable */
	protected boolean _isVariable;
	
	
	// static initializer
	static {
		VALUE_GETTERS = new HashMap<String, ValueGetter>();
		populateValueGetters();
	}
	
	
	/** 
	 * Primary constructor
	 * @param name the name of the parameter
	 * @param typeAdaptor the type specific adaptor that allows the parameter to perform operations correctly
	 */
	public CoreParameter( final String name, final ParameterTypeAdaptor typeAdaptor ) {
		_messageCenter = new MessageCenter( "Core Parameter" );
		_eventProxy = _messageCenter.registerSource( this, CoreParameterListener.class );
		
		_name = name;
		_typeAdaptor = typeAdaptor;
		_liveParameters = new ArrayList<LiveParameter>();
		
		_customValue = 0;
		_customLimits = new double[2];
		
		_accessorField = typeAdaptor.getPropertyAccessor();
		
		_isVariable = false;		
		setActiveSource( CUSTOM_SOURCE );
	}
	
	
	/** dispose of this parameter's resources */
	public void dispose() {
		_messageCenter.removeSource( this, CoreParameterListener.class );
	}
	
	
	/**
	 * Override the inherited method to provide more information about this parameter.
	 * @return the name of this parameter
	 */
	public String toString() {
		return getName();
	}
	
	
	/** Populate the value getters */
	static private void populateValueGetters() {
		VALUE_GETTERS.put( NAME_KEY, new ValueGetter() {
			public Object getValue( final CoreParameter parameter ) { return parameter.getName(); }
		});
		
		VALUE_GETTERS.put( TYPE_KEY, new ValueGetter() {
			public Object getValue( final CoreParameter parameter ) { return parameter.getTypeAdaptor().getID(); }
		});
		
		VALUE_GETTERS.put( ACCESSOR_FIELD_KEY, new ValueGetter() {
			public Object getValue( final CoreParameter parameter ) { return parameter.getAccessorField(); }
		});
		
		VALUE_GETTERS.put( VARIABLE_KEY, new ValueGetter() {
			public Object getValue( final CoreParameter parameter ) { return parameter.isVariable() ? IS_VARIABLE : IS_NOT_VARIABLE; }
		});
		
		VALUE_GETTERS.put( INITIAL_VALUE_KEY, new ValueGetter() {
			public Object getValue( final CoreParameter parameter ) { return new Double( parameter.getInitialValue() ); }
		});
		
		VALUE_GETTERS.put( LOWER_LIMIT_KEY, new ValueGetter() {
			public Object getValue( final CoreParameter parameter ) { return new Double( parameter.getLowerLimit() ); }
		});
		
		VALUE_GETTERS.put( UPPER_LIMIT_KEY, new ValueGetter() {
			public Object getValue( final CoreParameter parameter ) { return new Double( parameter.getUpperLimit() ); }
		});		
	}
	
	
	/**
	 * Add the specified listener to receive core parameter event notifications from this object.
	 * @param listener the listener to receive the event notifications.
	 */
	public void addCoreParameterListener( final CoreParameterListener listener ) {
		_messageCenter.registerTarget( listener, this, CoreParameterListener.class );
		listener.variableStatusChanged( this, _isVariable );
		listener.customValueChanged( this, _customValue );
		listener.customLimitsChanged( this, _customLimits );
		listener.initialValueChanged( this, _initialValue );
		listener.lowerLimitChanged( this, _lowerLimit );
		listener.upperLimitChanged( this, _upperLimit );
	}
	
	
	/**
	 * Remove the specified listener from receiving core parameter event notifications from this object.
	 * @param listener the listener to remove from receiving event notifications.
	 */
	public void removeCoreParameterListener( final LiveParameterListener listener ) {
		_messageCenter.removeTarget( listener, this, LiveParameterListener.class );
	}
	
	
	/**
	 * Get this parameter's type adaptor.
	 * @return this parameter's type adaptor
	 */
	public ParameterTypeAdaptor getTypeAdaptor() {
		return _typeAdaptor;
	}
	
	
	/**
	 * Add the specified live parameter.
	 */
	public void addLiveParameter( final LiveParameter liveParameter ) {
		_liveParameters.add( liveParameter );
	}
	
	
	/**
	 * Get the live parameters associated with this core parameter.
	 */
	public List<LiveParameter> getLiveParameters() {
		return _liveParameters;
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
		return ( valueGetter != null ) ? valueGetter.getValue( this ) : null;
	}
	
	
	/**
	 * Get the name of this parameter.
	 * @return the name of this parameter.
	 */
	public String getName() {
		return _name;
	}
	
	
	/**
	 * Get this parameter's type.
	 * @return the type of this parameter
	 */
	public String getType() {
		return _typeAdaptor.getID();
	}
	
	
	/**
	 * Get the property accessor field.
	 * @return the property accessor field used in online models.
	 */
	protected String getAccessorField() {
		return _accessorField;
	}
	
	
	/**
	 * Get the name of the specified source.
	 * @param source the indicator of the specified source
	 * @return the name of the specified source.
	 */
	static public String getSourceName( final int source ) {
		switch( source ) {
			case DESIGN_SOURCE:
				return "Design";
			case CONTROL_SOURCE:
				return "Control";
			case CUSTOM_SOURCE:
				return "Custom";
			default:
				return "?";
		}		
	}
	
	
	/**
	 * Get the name of the active source.
	 * @return the name of the active source.
	 */
	public String getActiveSourceName() {
		return getSourceName( _activeSource );
	}
	
	
	/**
	 * Get the indicator of the variable source.
	 * @return the indicator of this variable's source.
	 */
	public int getActiveSource() {
		return _activeSource;
	}
	
	
	/**
	 * Set the variable source to the one specified.
	 * @param source the identifier of the variable source
	 */
	public void setActiveSource( final int source ) {
		_activeSource = source;
		_eventProxy.variableSourceChanged( this, source );
	}
	
	
	/**
	 * Get this custom value.
	 * @return the custom value
	 */
	public double getCustomValue() {
		return _customValue;
	}
	
	
	/**
	 * Set this parameter's custom value.
	 * @param value the new custom value
	 */
	public void setCustomValue( final double value ) {
		if ( value != _customValue && !( Double.isNaN( value ) && Double.isNaN( _customValue ) ) ) {
			// if the value falls outside the current limits then adjust the limits to include the value
			if ( value < _customLimits[0] ) {
				setCustomLimits( new double[] { value, _customLimits[1] } );
			}
			else if ( value > _customLimits[1] ) {
				setCustomLimits( new double[] { _customLimits[0], value } );
			}
			
			_customValue = value;
			_eventProxy.customValueChanged( this, value );
			
			if ( _activeSource == CUSTOM_SOURCE ) {
				setInitialValue( value );
			}			
		}
	}
	
	
	/**
	 * Get this custom limits.
	 * @return the custom limits
	 */
	public double[] getCustomLimits() {
		return _customLimits;
	}
	
	
	/**
	 * Set this parameter's custom limits.
	 * @param limits the new custom limits
	 */
	public void setCustomLimits( final double[] limits ) {
		if ( limits[0] != _customLimits[0] || limits[1] != _customLimits[1] ) {
			_customLimits[0] = limits[0];
			_customLimits[1] = limits[1];
			
			if ( _activeSource == CUSTOM_SOURCE ) {
				setLowerLimit( limits[0] );
				setUpperLimit( limits[1] );
			}
			
			_eventProxy.customLimitsChanged( this, limits );
		}
	}
	
	
	/**
	 * Get this parameter's initial variable value.
	 * @return the initial value
	 */
	public double getInitialValue() {
		return _initialValue;
	}
	
	
	/**
	 * Set this parameter's initial variable value.
	 * @param value the new initial value
	 */
	public void setInitialValue( final double value ) {
		if ( value != _initialValue && !( Double.isNaN( value ) && Double.isNaN( _initialValue ) ) ) {
			_initialValue = value;
			
			if ( _activeSource == CUSTOM_SOURCE ) {
				setCustomValue( value );
			}
			
			_eventProxy.initialValueChanged( this, value );
		}
	}
	
	
	/**
	 * Get this parameter's lower variable limit.
	 * @return the lower limit
	 */
	public double getLowerLimit() {
		return _lowerLimit;
	}
	
	
	/**
	 * Set this parameter's lower variable limit.
	 * @param limit the new lower limit
	 */
	public void setLowerLimit( final double limit ) {
		if ( limit != _lowerLimit ) {
			_lowerLimit = limit;
			
			if ( _activeSource == CUSTOM_SOURCE ) {
				_customLimits[0] = limit;
				_eventProxy.customLimitsChanged( this , _customLimits );
			}
			
			_eventProxy.lowerLimitChanged( this, limit );			
		}
	}
	
	
	/**
	 * Get this parameter's upper variable limit.
	 * @return this parameter's upper limit.
	 */
	public double getUpperLimit() {
		return _upperLimit;
	}
	
	
	/**
	 * Set this parameter's upper variable limit.
	 * @param limit the new upper limit
	 */
	public void setUpperLimit( final double limit ) {
		if ( limit != _upperLimit ) {
			_upperLimit = limit;
			
			if ( _activeSource == CUSTOM_SOURCE ) {
				_customLimits[1] = limit;
				_eventProxy.customLimitsChanged( this , _customLimits );
			}
			
			_eventProxy.upperLimitChanged( this, limit );			
		}
	}
	
	
	/**
	 * Determine if this parameter is variable.
	 * @return true if this parameter is variable and false if not.
	 */
	public boolean isVariable() {
		return _isVariable;
	}
	
	
	/**
	 * Set whether this parameter should be variable.
	 * @param isVariable true to indicate that this parameter is variable and false to indicate that it is not
	 */
	public void setIsVariable( final boolean isVariable ) {
		if ( isVariable != _isVariable ) {
			_isVariable = isVariable;
			_eventProxy.variableStatusChanged( this, isVariable );			
		}
	}
	
	
    /** 
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    public String dataLabel() {
		return DATA_LABEL;
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		setIsVariable( adaptor.booleanValue( "variable" ) );
		setActiveSource( adaptor.intValue( "variableSource") );
		setCustomValue( adaptor.doubleValue( "customValue" ) );
		
		final double lowerLimit = adaptor.doubleValue( "customLowerLimit" );
		final double upperLimit = adaptor.doubleValue( "customUpperLimit" );
		setCustomLimits( new double[] { lowerLimit, upperLimit } );
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "name", getName() );
		adaptor.setValue( "variable", isVariable() );
		adaptor.setValue( "variableSource", getActiveSource() );
		adaptor.setValue( "customValue", getCustomValue() );
		adaptor.setValue( "customLowerLimit", getCustomLimits()[0] );
		adaptor.setValue( "customUpperLimit", getCustomLimits()[1] );
	}
	
	
	
	/** Instances can get a particular value from a parameter. */
	interface ValueGetter {
		/**
		 * Get a particular value from the specified parameter.
		 * @param parameter the parameters whose particular value is returned.
		 */
		public Object getValue( final CoreParameter parameter );
	}
}
