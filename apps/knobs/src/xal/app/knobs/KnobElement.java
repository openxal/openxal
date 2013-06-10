//
//  KnobElement.java
//  xal
//
//  Created by Thomas Pelaia on 12/5/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.smf.Accelerator;
import xal.smf.NodeChannelRef;
import xal.ca.*;
import xal.tools.data.*;
import xal.tools.messaging.MessageCenter;
import xal.tools.StringJoiner;

import java.util.Date;


/** Represents a knob element */
public class KnobElement implements DataListener {
	/** factor by which to multiply the raw limits to get the effective limits when wrapping values */
	static private final double EFFECTIVE_LIMIT_FACTOR = 100;

	/** default limit */
	static protected final double DEFAULT_LIMIT;
	
	/** indicates if default limits has been supplied */
	static protected final boolean HAS_DEFAULT_LIMITS;
	
	/** The DataAdaptor label */
	static public final String DATA_LABEL = "KnobElement";
	
	/** message center for posting events */
	final protected MessageCenter MESSAGE_CENTER;
	
	/** forwards events to registered listeners */
	final protected KnobElementListener EVENT_PROXY;
	
	/** handles connection events */
	final protected ConnectionHandler CONNECTION_HANDLER;
	
	/** handles monitor events */
	final protected IEventSinkValue MONITOR_HANDLER;
	
	/** handle the channel's put events */
	final protected PutListener PUT_HANDLER;
	
	/** the channel whose setting is tracked/changed as the knob changes */
	protected Channel _channel;
	
	/** an optional node channel reference which identifies the channel with respect to a node */
	protected NodeChannelRef _nodeChannelRef;
	
	/** the channel monitor */
	protected Monitor _monitor;
	
	/** the knob coefficient for this element */
	protected double _coefficient;
	
	/** latest value from the monitor */
	protected double _monitoredValue;
	
	/** wall clock time when the last monitor event was received */
	protected Date _lastMonitorTime;
	
	/** latest setting value */
	protected double _settingValue;
	
	/** wall clock time when the setting was last changed */
	protected Date _lastSettingTime;
	
	/** latest value from either the monitor or the setting */
	protected double _latestValue;
	
	/** the handler of lower and upper limits */
	protected LimitsHandler _limitsHandler;
	
	/** monitor limits from channel access */
	final protected CALimitsHandler CA_LIMITS_HANDLER;
	
	/** custom limits handler */
	final protected CustomLimitsHandler CUSTOM_LIMITS_HANDLER;
	
	/** lower limit */
	protected double _lowerLimit;
	
	/** upper limit */
	protected double _upperLimit;

	/** indicates whether the value should be wrapped around the limits */
	private boolean _wrapsValueAroundLimits;
	
	/** specifies whether a put operation is currently pending */
	volatile protected boolean _isPutPending;
	
	
	// static initializer
	static {
		final String defaultLimitsProperty = System.getProperty( "gov.sns.apps.knobs.KnobElement.DEFAULT_LIMIT" );
		HAS_DEFAULT_LIMITS = ( defaultLimitsProperty != null );
		double limit = Double.NaN;
		try {
			limit = HAS_DEFAULT_LIMITS ? Double.parseDouble( defaultLimitsProperty ) : Double.NaN;
		}
		catch( NumberFormatException exception ) {
			System.out.println( "Error:  Default limits cannot be parsed:  >>>" + defaultLimitsProperty + "<<<" );
			exception.printStackTrace();
		}
		finally {
			DEFAULT_LIMIT = limit;
		}
	}
	
	
	/** Constructor */
	public KnobElement() {
		MESSAGE_CENTER = new MessageCenter( "Knob Element" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, KnobElementListener.class );
		
		CONNECTION_HANDLER = new ConnectionHandler();
		MONITOR_HANDLER = new MonitorHandler();
		PUT_HANDLER = new PutHandler();
		
		CA_LIMITS_HANDLER = new CALimitsHandler();
		CUSTOM_LIMITS_HANDLER = new CustomLimitsHandler();
		
		_limitsHandler = CA_LIMITS_HANDLER;
		
		_lastSettingTime = new Date();
		_lastMonitorTime = new Date();
		
		_lowerLimit = Double.NaN;
		_upperLimit = Double.NaN;
		_coefficient = 1.0;
		_wrapsValueAroundLimits = false;
		
		_isPutPending = false;
	}
	
	
	/**
	 * Get the string representation of this element.
	 * @return the element's PV
	 */
	public String toString() {
		return getChannelString();
	}
	
	
	/**
	 * Add the specified listener as a receiver of notifications from this element.
	 * @param listener the listener to be notified
	 */
	public void addKnobElementListener( final KnobElementListener listener ) {
		synchronized( this ) {
			MESSAGE_CENTER.registerTarget( listener, this, KnobElementListener.class );
			listener.channelChanged( this, getChannel() );
			listener.connectionChanged( this, isConnected() );
			listener.readyStateChanged( this, isReady() );
			listener.coefficientChanged( this, getCoefficient() );
			listener.valueChanged( this, getLatestValue() );
		}
	}
	
	
	/**
	 * Remove the specified listener from receiving notifications from this element.
	 * @param listener the listener to remove from receiving notifications
	 */
	public void removeKnobElementListener( final KnobElementListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, KnobElementListener.class );
	}
	
	
    /** 
	 * dataLabel() provides the name used to identify the class in an external data source.
	 * @return The tag for this data node.
	 */
    public String dataLabel() {
        return DATA_LABEL;
    }
	
	
	/**
	 * Instantiate a new KnobElement from the specified adaptor
	 * @param accelerator the accelerator to use for node channel references
	 * @param adaptor the adaptor from which to instantiate the element
	 * @return a new knob element
	 */
	static public KnobElement getInstance( final Accelerator accelerator, final DataAdaptor adaptor ) {
		final KnobElement element = new KnobElement();
		element.update( accelerator, adaptor );
		return element;
	}
    
    
    /**
	 * Update the data based on the given adaptor.
	 * @param accelerator the accelerator to use for node channel references
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update( final Accelerator accelerator, final DataAdaptor adaptor ) {
		if ( accelerator != null && adaptor.hasAttribute( "nodeChannelRef" ) ) {
			setNodeChannelRef( NodeChannelRef.getInstance( accelerator, adaptor.stringValue( "nodeChannelRef" ) ) );
		}
		else if ( adaptor.hasAttribute( "pv" ) ) {
			setPV( adaptor.stringValue( "pv" ) );
		}
		
		setCoefficient( adaptor.doubleValue( "coefficient" ) );
		
		if ( adaptor.hasAttribute( "customLowerLimit" ) ) {
			setCustomLowerLimit( adaptor.doubleValue( "customLowerLimit" ) );
		}
		if ( adaptor.hasAttribute( "customUpperLimit" ) ) {
			setCustomUpperLimit( adaptor.doubleValue( "customUpperLimit" ) );
		}
		if ( adaptor.hasAttribute( "usingCustomLimits" ) ) {
			setUsingCustomLimits( adaptor.booleanValue( "usingCustomLimits" ) );
		}
		if ( adaptor.hasAttribute( "wrapsValueAroundLimits" ) ) {
			setWrapsValueAroundLimits( adaptor.booleanValue( "wrapsValueAroundLimits" ) );
		}
    }
    
    
    /**
	 * Update the data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update( final DataAdaptor adaptor ) {
		update( null, adaptor );
    }
    
    
    /**
	 * Write the data to the adaptor for external storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write( final DataAdaptor adaptor ) {
		if ( _nodeChannelRef != null ) {
			adaptor.setValue( "nodeChannelRef", _nodeChannelRef.toString() );
		}
		else if ( _channel != null ) {
			adaptor.setValue( "pv", _channel.channelName() );
		}
		adaptor.setValue( "coefficient", _coefficient );
		adaptor.setValue( "customLowerLimit", CUSTOM_LIMITS_HANDLER.getLowerLimit() );
		adaptor.setValue( "customUpperLimit", CUSTOM_LIMITS_HANDLER.getUpperLimit() );
		adaptor.setValue( "usingCustomLimits", isUsingCustomLimits() );
		adaptor.setValue( "wrapsValueAroundLimits", wrapsValueAroundLimits() );
    }
	
	
	/**
	 * Get the element's name.
	 * @return this element's name
	 */
	public String getPV() {
		return _channel != null ? _channel.channelName() : "";
	}
	
	
	/**
	 * Set the element's PV.
	 * @param name the name for the PV
	 */
	public void setPV( final String name ) {
		if ( name != null && !name.equals( "" ) ) {
			setChannel( ChannelFactory.defaultFactory().getChannel( name ) );
		}
		else {
			setChannel( null );
		}
		
		_nodeChannelRef = null;
	}
	
	
	/**
	 * Get a string representation of the channel
	 * @return a string representation of the channel
	 */
	public String getChannelString() {
		return _nodeChannelRef != null ? _nodeChannelRef.toString() : getPV();
	}
	
	
	/**
	 * Set the node channel reference.
	 * @param nodeChannelRef channel reference
	 */
	public void setNodeChannelRef( final NodeChannelRef nodeChannelRef ) {
		synchronized( this ) {
			_nodeChannelRef = nodeChannelRef;
			setChannel( nodeChannelRef != null ? nodeChannelRef.getChannel() : null );
		}		
	}
	
	
	/** update the channel reference if any with the new accelerator */
	public void setAccelerator( final Accelerator accelerator ) {
		final NodeChannelRef oldChannelRef = _nodeChannelRef;
		if ( oldChannelRef != null ) {
			final NodeChannelRef ref = new NodeChannelRef( accelerator.getNode( oldChannelRef.getNode().getId() ), oldChannelRef.getHandle() );
			setNodeChannelRef( ref );
		}
	}
	
	
	/**
	 * Get a this element's channel
	 * @return this element's channel
	 */
	public Channel getChannel() {
		synchronized( this ) {
			return _channel;
		}
	}
	
	
	/**
	 * Set a new channel for this element
	 * @param channel the new channel to set
	 */
	public void setChannel( final Channel channel ) {
		synchronized( this ) {
			if ( _channel != null ) {
				if ( channel != null && channel.getId() == _channel.getId() )  return;		// no need to change anything
				
				_channel.removeConnectionListener( CONNECTION_HANDLER );
				if ( _monitor != null ) {
					_monitor.clear();
					_monitor = null;
				}
			}
			
			_latestValue = Double.NaN;
			_settingValue = Double.NaN;
			_monitoredValue = Double.NaN;
			_lowerLimit = Double.NaN;
			_upperLimit = Double.NaN;
			if ( HAS_DEFAULT_LIMITS ) {
				_lowerLimit = - DEFAULT_LIMIT;
				_upperLimit = DEFAULT_LIMIT;
			}
			_channel = channel;
			if ( channel != null ) {
				channel.addConnectionListener( CONNECTION_HANDLER );
				channel.requestConnection();
				CA_LIMITS_HANDLER.setChannel( channel );
				Channel.flushIO();
			}
		}
		
		EVENT_PROXY.channelChanged( this, channel );
		EVENT_PROXY.readyStateChanged( this, isReady() );
	}
	
	
	/** 
	 * Set whether to use custom limits or CA limits.
	 * @param useCustomLimits true to use custom limits and false to use CA limits
	 */
	public void setUsingCustomLimits( final boolean useCustomLimits ) {
		_limitsHandler = useCustomLimits ? CUSTOM_LIMITS_HANDLER : CA_LIMITS_HANDLER;
		_limitsHandler.updateLimits();
	}
	
	
	/**
	 * Determine if custom limits are being used
	 * @return true if custom limits are being used and false if not
	 */
	public boolean isUsingCustomLimits() {
		return _limitsHandler == CUSTOM_LIMITS_HANDLER;
	}
	
	
	/**
	 * Set the custom lower limit
	 * @param lowerLimit the new custom lower limit
	 */
	public void setCustomLowerLimit( final double lowerLimit ) {
		CUSTOM_LIMITS_HANDLER.setLowerLimit( lowerLimit );
	}
	
	
	/**
	 * Set the custom upper limit
	 * @param upperLimit the new custom upper limit
	 */
	public void setCustomUpperLimit( final double upperLimit ) {
		CUSTOM_LIMITS_HANDLER.setUpperLimit( upperLimit );
	}
	
	
	/** Determine if this element is connected */
	public boolean isConnected() {
		return _channel != null && _channel.isConnected();
	}
	
	
	/** Determine if this element is ready (i.e. the channel is connected and we have a new value) */
	public boolean isReady() {
		synchronized( this ) {
			return isConnected() && isSettingValueWithinLimits();
		}
	}


	/** Determinw whether the element's current value is within its limits */
	public boolean isSettingValueWithinLimits() {
		final double settingValue = _settingValue;
		final double lowerLimit = _lowerLimit;
		final double upperLimit = _upperLimit;
		
		return !Double.isNaN( settingValue ) && !Double.isNaN( lowerLimit ) && !Double.isNaN( upperLimit ) && ( settingValue >= lowerLimit && settingValue <= upperLimit );
	}
	
	
	/**
	 * Determine the reason for not being ready.
	 * @return the excuse for not being ready
	 */
	public String getInactiveExcuse() {
		if ( _channel == null || isReady() )  return null;
		
		final StringJoiner joiner = new StringJoiner();
		if ( !_channel.isConnected() ) {
			joiner.append( _channel.channelName() + " is not connected" );
		}
		else {
			if ( Double.isNaN( _settingValue ) ) {
				joiner.append( _channel.channelName() + " set point has not been found" );
			}
			if ( !_limitsHandler.isReady() ) {
				joiner.append( _limitsHandler.getInactiveExcuse() );
			}
			else if ( !isSettingValueWithinLimits() ) {
				joiner.append( _channel.channelName() + " set point ( " + _settingValue + " ) outside limits [ " + _lowerLimit + ", " + _upperLimit + " ]" );
			}
		}
		
		return joiner.toString();
	}
	
	
	/**
	 * Get the knob coefficient
	 * @return the knob coefficient
	 */
	public double getCoefficient() {
		return _coefficient;
	}
	
	
	/**
	 * Set the knob coefficient.
	 * @param coefficient the new knob coefficient
	 */
	public void setCoefficient( final double coefficient ) {
		setCoefficient( coefficient, true );
	}
	
	
	/**
	 * Set the knob coefficient.
	 * @param coefficient the new knob coefficient
	 * @param notify indicates whether or not to notify listeners
	 */
	public void setCoefficient( final double coefficient, final boolean notify  ) {
		_coefficient = coefficient;
		if ( notify ) {
			EVENT_PROXY.coefficientChanged( this, coefficient );
		}
	}
	
	
	/**
	 * Get the lower limit of the channel's value.
	 * @return the channel's lower value limit
	 */
	public double getLowerLimit() {
		synchronized( this ) {
			return _lowerLimit;
		}
	}
	
	
	/**
	 * Get the channel's upper limit.
	 * @return the channel's upper value limit
	 */
	public double getUpperLimit() {
		synchronized( this ) {
			return _upperLimit;
		}
	}


	/**
	 * Get the effective lower limit of the channel's value.
	 * @return the channel's lower value limit modified by a factor of 1000 if allows wrapping
	 */
	public double getEffectiveLowerLimit() {
		final double lowerLimit = getLowerLimit();
		return _wrapsValueAroundLimits ? EFFECTIVE_LIMIT_FACTOR * lowerLimit : lowerLimit;
	}


	/**
	 * Get the channel's upper limit.
	 * @return the channel's upper value limit modified by a factor of 1000 if allows wrapping
	 */
	public double getEffectiveUpperLimit() {
		final double upperLimit = getUpperLimit();
		return _wrapsValueAroundLimits ? EFFECTIVE_LIMIT_FACTOR * upperLimit : upperLimit;
	}

	
	/**
	 * Get the latest value.
	 * @return the value from either the latest setting or the latest monitored value whichever is most current
	 */
	public double getLatestValue() {
		synchronized( this ) {
			return _latestValue;
		}
	}
	
	
	/**
	 * Get the latest monitor value
	 * @return the latest value from the monitor
	 */
	public double getMonitoredValue() {
		synchronized( this ) {
			return _monitoredValue;
		}
	}
	
	
	/**
	 * Get the latest value which was set.
	 * @return the latest value which was set
	 */
	public double getSettingValue() {
		synchronized( this ) {
			return _settingValue;
		}
	}
	
	
	/**
	 * Determine if this element's setting value is tracking with its monitored value.
	 * @return true if the values are reasonably tracking and false if not
	 */
	public boolean isTracking() {
		synchronized( this ) {
			final long lastMonitorTime = _lastMonitorTime.getTime();
			final long lastSettingTime = _lastSettingTime.getTime();
			
			if ( lastMonitorTime - lastSettingTime > 2000 ) {	// allow two seconds for the new value to be applied
				final double scale = 1.0e-3 * ( _upperLimit - _lowerLimit );
				return Math.abs( _settingValue - _monitoredValue ) < scale;
			}
			else {
				return true;
			}
		}
	}
	
	
	/**
	 * Determine if a put operation is pending.
	 * @return true if a put operation is pending and false if all put operations have completed
	 */
	public boolean isPutPending() {
		return _isPutPending;
	}


	/** Indicates whether this elements wraps the value around the limits */
	public boolean wrapsValueAroundLimits() {
		return _wrapsValueAroundLimits;
	}


	/** Sets whether values wrap around limits */
	public void setWrapsValueAroundLimits( final boolean wrapsAround ) {
		if ( wrapsAround != _wrapsValueAroundLimits ) {		// avoid updating limits unless actual change
			_wrapsValueAroundLimits = wrapsAround;
			_limitsHandler.updateLimits();
		}
	}


	/** wrap the value (if necessary) around the limits (e.g. value of 190 wrapped around [-180,180] would be -170) */
	private double wrapValueAroundLimits( final double value ) {
		final double lowerLimit = _lowerLimit;
		final double upperLimit = _upperLimit;
		final double range = upperLimit - lowerLimit;
		final double wrappedValue = ( value - lowerLimit ) % range;
		final double constrainedWrappedValue = ( wrappedValue < 0 ) ? wrappedValue + range : wrappedValue;
		return constrainedWrappedValue + lowerLimit;
	}


	/**
	 * Set the channel to the value specified wrapping to the limits if configured to do so.
	 * @param value the value for which to set this element
	 */
	public void setValue( final double value ) {
		final double rawValue = _wrapsValueAroundLimits ? wrapValueAroundLimits( value ) : value;
		setRawValue( rawValue );
	}

	
	/**
	 * Set the channel to the value specified.
	 * @param value the value for which to set this element
	 */
	private void setRawValue( final double value ) {
		synchronized( this ) {
			if ( _channel != null ) {
				try {
					_isPutPending = true;
					_channel.putValCallback( value, PUT_HANDLER );
					_settingValue = value;
					_latestValue = value;
					_lastSettingTime = new Date();
				}
				catch ( Exception exception ) {
					_isPutPending = false;
					throw new RuntimeException( "Exception attempting to set value for channel:  " + _channel.channelName(), exception );
				}
			}
		}
		EVENT_PROXY.valueChanged( this, value );
	}
	
	
	/** Resynchronize the setting value to the latest monitored value */
	public void resync() {
		synchronized( this ) {
			_isPutPending = false;
			setValue( _monitoredValue );
		}
	}
	
	
	/**
	 * Change the element's value and scale it by the element's coefficient.
	 * @param delta the amount to scale by the coefficient and then change this element's value
	 */
	public void changeValueAndScale( final double delta ) {
		setValue( getSettingValue() + _coefficient * delta );
	}
	
	
	
	/** Connection handler */
	protected class ConnectionHandler implements ConnectionListener {
		/** handle the connection event */
		public void connectionMade( final Channel channel ) {
			try {
				synchronized( KnobElement.this ) {					
					if ( _monitor == null ) {
						_monitor = channel.addMonitorValue( MONITOR_HANDLER, Monitor.VALUE );
					}
				}

				EVENT_PROXY.connectionChanged( KnobElement.this, true );
				EVENT_PROXY.readyStateChanged( KnobElement.this, isReady() );
			}
			catch ( ConnectionException exception ) {}
			catch ( MonitorException exception ) {
				exception.printStackTrace();
			}
		}
		
		
		/** handle the disconnect event */
		public void connectionDropped( final Channel channel ) {
			EVENT_PROXY.connectionChanged( KnobElement.this, false );
			EVENT_PROXY.readyStateChanged( KnobElement.this, false );
		}
	}
	
	
	
	/** Handle limits */
	protected interface LimitsHandler {
		/** update the limits */
		public void updateLimits();
		
		
		/** Determine if the limits handler is ready */
		public boolean isReady();
		
		
		/** Determine the reason for not being ready */
		public String getInactiveExcuse();
	}
	
	
	
	/** Custom limits handler */
	protected class CustomLimitsHandler implements LimitsHandler {
		/** lower custom limit */
		protected double _lowerCustomLimit;
		
		/** upper custom limit */
		protected double _upperCustomLimit;
		
		
		/** Constructor */
		public CustomLimitsHandler() {
			if ( HAS_DEFAULT_LIMITS ) {
				_lowerCustomLimit = - DEFAULT_LIMIT;
				_upperCustomLimit = DEFAULT_LIMIT;
			}
			else {
				_lowerCustomLimit = -1.0;
				_upperCustomLimit = 1.0;
			}
		}
		
		
		/**
		 * Set the lower custom limit.
		 * @param lowerLimit the new custom lower limit
		 */
		public void setLowerLimit( final double lowerLimit ) {
			_lowerCustomLimit = lowerLimit;
			updateLimits();
		}
		
		
		/**
		 * Get the lower custom limit.
		 * @return the lower custom limit
		 */
		public double getLowerLimit() {
			return _lowerCustomLimit;
		}
		
		
		/**
		 * Set the upper custom limit.
		 * @param upperLimit the new custom upper limit
		 */
		public void setUpperLimit( final double upperLimit ) {
			_upperCustomLimit = upperLimit;
			updateLimits();
		}
		
		
		/**
		 * Get the upper custom limit.
		 * @return the upper custom limit
		 */
		public double getUpperLimit() {
			return _upperCustomLimit;
		}
		
		
		/** update the limits */
		public void updateLimits() {
			if ( this == _limitsHandler ) {
				_lowerLimit = _lowerCustomLimit;
				_upperLimit = _upperCustomLimit;
				EVENT_PROXY.channelChanged( KnobElement.this, KnobElement.this.getChannel() );
				EVENT_PROXY.readyStateChanged( KnobElement.this, KnobElement.this.isReady() );
			}
		}
		
		
		/** Determine if the limits handler is ready */
		public boolean isReady() {
			return true;
		}
		
		
		/** Determine the reason for not being ready */
		public String getInactiveExcuse() {
			return "No limits excuse";
		}
	}
	
	
	
	/** CA limits handler */
	protected class CALimitsHandler implements LimitsHandler, ConnectionListener, IEventSinkValDbl {
		/** the channel with the lower limit */
		protected Channel _lowerChannel;
		
		/** the channel with the upper limit */
		protected Channel _upperChannel;
		
		
		/** set a new channel whose limits we wish to monitor */
		public void setChannel( final Channel channel ) {
			synchronized( this ) {
				if ( _lowerChannel != null ) {
					_lowerChannel.removeConnectionListener( this );
				}
				
				final String lowerLimitPV = _channel.channelName() + ".LOPR";
				_lowerChannel = ChannelFactory.defaultFactory().getChannel( lowerLimitPV );
				_lowerChannel.addConnectionListener( this );
				_lowerChannel.requestConnection();
				
				if ( _upperChannel != null ) {
					_upperChannel.removeConnectionListener( this );
				}
				
				final String upperLimitPV = _channel.channelName() + ".HOPR";
				_upperChannel = ChannelFactory.defaultFactory().getChannel( upperLimitPV );
				_upperChannel.addConnectionListener( this );
				_upperChannel.requestConnection();
			}
		}
		
		
		/** handle the connection event */
		public void connectionMade( final Channel channel ) {
			synchronized( KnobElement.this ) {
				try {
					channel.getValDblCallback( this );
					Channel.flushIO();
				}
				catch ( ConnectionException exception ) {}
				catch ( GetException exception ) {}
			}
		}
		
		
		/** handle the disconnect event */
		public void connectionDropped( final Channel channel ) {
		}
		
		
		/** handle the get value callback */
		public void eventValue( final double value, final Channel channel ) {			
			synchronized( KnobElement.this ) {
				if ( this != _limitsHandler )  return;	// don't update the element if this isn't the active limits handler
				
				if ( channel == _lowerChannel ) {
					_lowerLimit = value;
				}
				else if ( channel == _upperChannel ) {
					_upperLimit = value;
				}
			}
			
			EVENT_PROXY.readyStateChanged( KnobElement.this, isReady() );
		}
		
		
		/** update the limits */
		public void updateLimits() {
			synchronized( KnobElement.this ) {
				if ( this != _limitsHandler )  return;	// don't update the element if this isn't the active limits handler
				
				if ( _lowerChannel != null && _lowerChannel.isConnected() ) {
					try {
						_lowerChannel.getValDblCallback( this );
					}
					catch ( ConnectionException exception ) {}
					catch ( GetException exception ) {}
				}
				if ( _upperChannel != null && _upperChannel.isConnected() ) {
					try {
						_upperChannel.getValDblCallback( this );
					}
					catch ( ConnectionException exception ) {}
					catch ( GetException exception ) {}
				}
				Channel.flushIO();
			}
			EVENT_PROXY.channelChanged( KnobElement.this, KnobElement.this.getChannel() );
			EVENT_PROXY.readyStateChanged( KnobElement.this, KnobElement.this.isReady() );
		}
		
		
		/** Determine if the limits handler is ready */
		public boolean isReady() {
			return _lowerChannel.isConnected() && !Double.isNaN( _lowerLimit ) && _upperChannel.isConnected() && !Double.isNaN( _upperLimit );
		}
		
		
		/** Determine the reason for not being ready */
		public String getInactiveExcuse() {
			if ( isReady() )  return null;
			
			final StringJoiner joiner = new StringJoiner();
			if ( _lowerChannel != null ) {
				if ( !_lowerChannel.isConnected() ) {
					joiner.append( "Lower limit field:  " + _lowerChannel.channelName() + " is not connected" );
				}
				else if ( Double.isNaN( _lowerLimit ) ) {
					joiner.append( "Lower limit has not been found" );
				}
			}
			if ( _upperChannel != null ) {
				if ( !_upperChannel.isConnected() ) {
					joiner.append( "Upper limit field:  " + _upperChannel.channelName() + " is not connected" );
				}
				else if ( Double.isNaN( _upperLimit ) ) {
					joiner.append( "Upper limit has not been found" );
				}
			}
			
			return joiner.toString();
		}
	}
	
	
	
	/** Monitor handler */
	protected class MonitorHandler implements IEventSinkValue {
		public void eventValue( final ChannelRecord record, final Channel channel ) {
			boolean readyStateChanged = false;
			double latestValue = 0;
			synchronized( KnobElement.this ) {
				_monitoredValue = record.doubleValue();
				_latestValue = _monitoredValue;
				latestValue = _latestValue;
				_lastMonitorTime = new Date();

				// while this element isn't ready, synchronize the setting value with monitor value
				if ( !isReady() ) {
					_settingValue = _latestValue;
					readyStateChanged = true;
				}
			}

			EVENT_PROXY.valueChanged( KnobElement.this, latestValue );
			if ( readyStateChanged ) {
				EVENT_PROXY.readyStateChanged( KnobElement.this, isReady() );
			}
		}
	}
	
	
	
	/** Put handler */
	protected class PutHandler implements PutListener {
		public void putCompleted( final Channel channel ) {
			_isPutPending = false;
			EVENT_PROXY.valueSettingPublished( KnobElement.this );
		}
	}
}


