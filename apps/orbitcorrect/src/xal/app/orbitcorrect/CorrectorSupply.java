//
//  CorrectorSupply.java
//  xal
//
//  Created by Thomas Pelaia on 5/8/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import xal.tools.text.FormattedNumber;
import xal.smf.impl.*;
import xal.smf.*;
import xal.ca.*;
import xal.tools.data.KeyedRecord;
import xal.tools.messaging.MessageCenter;

import java.util.*;


/** Manage a common corrector power supply */
public class CorrectorSupply implements KeyedRecord {
	/** key indicating whether the supply powers a corrector */
	final static public String CORRECTOR_KEY = "CORRECTOR";
	
	/** key indicating whether the supply powers exactly one magnet */
	final static public String SINGLE_MAGNET_KEY = "SINGLE_MAGNET";
	
	/** key indicating the plane of the supplied magnets */
	final static public String PLANE_KEY = "PLANE";
	
	/** wrapped power supply */
	final MagnetMainSupply _powerSupply;
	
	/** correctors fed by this supply */
	final List<CorrectorAgent> _correctors;
	
	/** Channel wrapper for writing the corrector field */
	protected ChannelWrapper _fieldWriteChannelWrapper;
	
	/** event message center */
	final protected MessageCenter MESSAGE_CENTER;
	
	/** proxy for posting channel events */
	final protected CorrectorSupplyListener EVENT_PROXY;
	
	/** the latest value or NaN if the channel is not connected */
	protected volatile double _latestField;
	
	/** handler of put events */
	protected final PutHandler PUT_HANDLER;
	
	/** handle fetching and processing of limits */
	protected final LimitsHandler LIMITS_HANDLER;
	
	/** custom lower field limit */
	protected double _lowerFieldLimit;
	
	/** custom upper field limit */
	protected double _upperFieldLimit;
	
	/** indicates whether this corrector is enabled */
	protected boolean _isEnabled;

	
	/** Constructor */
	public CorrectorSupply( final MagnetMainSupply powerSupply ) {
		PUT_HANDLER = new PutHandler();
		
		_powerSupply = powerSupply;
		
		_correctors = new ArrayList<CorrectorAgent>();
		_latestField = Double.NaN;
		_lowerFieldLimit = Double.NaN;
		_upperFieldLimit = Double.NaN;
				
		MESSAGE_CENTER = new MessageCenter();
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, CorrectorSupplyListener.class );
		
		wrapChannels();
		LIMITS_HANDLER = new LimitsHandler( powerSupply.getChannel( MagnetMainSupply.FIELD_SET_HANDLE ) );
	}
	
	
	/** Get a comparator to sort supplies by first corrector position and then sort with horizontal before vertical */
	static public Comparator<CorrectorSupply> getFirstCorrectorPositionComparator( final AcceleratorSeq sequence ) {
		return new Comparator<CorrectorSupply>() {
			public int compare( final CorrectorSupply supply1, final CorrectorSupply supply2 ) {
				final double supply1Position = supply1.getFirstCorrectorPositionIn( sequence );
				final double supply2Position = supply2.getFirstCorrectorPositionIn( sequence );
				return supply1Position > supply2Position ? 1 : supply1Position < supply2Position ? -1 : supply1.isHorizontal() && supply2.isVertical() ? -1 : supply1.isVertical() && supply2.isHorizontal() ? 1 : 0;
			}
			
			public boolean equals( Object anObject ) {
				return anObject == this;
			}
		};
	}
	
	
	/**
	 * Associate a corrector with this supply.
	 * @param corrector the corrector agent to associate with this supply
	 */
	public void addCorrector( final CorrectorAgent corrector ) {
		_correctors.add( corrector );
		_isEnabled = corrector.isCorrector();	// by default enable only corrector supplies
	}
	
	
	/**
	 * Get the associated correctors.
	 * @return the list of corrector agents powered by this supply
	 */
	public List<CorrectorAgent> getCorrectors() {
		return _correctors;
	}
	
	
	/**
	 * Add the specified listener as a receiver of corrector supply events from this agent.
	 * @param listener  The listener to receive corrector supply events.
	 */
	public void addCorrectorEventListener( final CorrectorSupplyListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, CorrectorSupplyListener.class );
	}
	
	
	/**
	 * Remove the specified listener from receiving corrector supply events from this agent.
	 * @param listener  The listener to be removed from receiving corrector supply events.
	 */
	public void removeCorrectorEventListener( final CorrectorSupplyListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, CorrectorSupplyListener.class );
	}
	
	
	/**
	 * Get the unique corrector ID.
	 * @return the supply's unique ID
	 */
	public String getID() {
		return _powerSupply.getId();
	}
	
	
	/** Determine if this supply is enabled */
	public boolean isEnabled() {
		return _isEnabled;
	}
	
	
	/** synonym for isEnabled */
	public boolean getEnabled() {
		return isEnabled();
	}
	
	
	/** Enable/disable this supply */
	public void setEnabled( final boolean enable ) {
		_isEnabled = enable;
		EVENT_PROXY.enableChanged( this, enable );
	}
	
	
	/**
	 * Determine if this BPM's channels are all connected.
	 * @return   true if this BPM is connected and false if not.
	 */
	public boolean isConnected() {
		try {
			return _fieldWriteChannelWrapper.isConnected();
		}
		catch ( NullPointerException exception ) {
			return false;
		}
	}
	
	
	/**
	 * Determine if this supply is available and all of its channels are connected.
	 * @return   true if this Corrector is online and false if not.
	 */
	public boolean isOnline() {
		return isConnected();		// if all the channels are connected then the corrector supply must be available.
	}
	
	
	/** Determine if the corrector's field limits have been found. */
	public boolean hasFieldLimits() {
		return LIMITS_HANDLER.hasLimits();
	}
    
    
    /** Set the custom field limits as a fraction of the operational limits */
    public void setOperationalFieldLimitsFraction( final double fraction ) {
        final double operationalLowerLimit = getHardLowerFieldLimit();
        final double operationalUpperLimit = getHardUpperFieldLimit();
        
        // verify that both operational limits are valid
        if ( !Double.isNaN( operationalLowerLimit ) && !Double.isNaN( operationalUpperLimit ) ) {
            if ( fraction == 1.0 ) {    // if 100% indicate that the limits are no longer custom
                _lowerFieldLimit = Double.NaN;
                _upperFieldLimit = Double.NaN;
            }
            else {
                final double halfRange = fraction * Math.abs( operationalUpperLimit - operationalLowerLimit ) / 2;
                final double operationalMean = ( operationalUpperLimit + operationalLowerLimit ) / 2;
                final double lowerLimit = operationalMean - halfRange;
                final double upperLimit = operationalMean + halfRange;
                
                setLowerFieldLimit( lowerLimit );
                setUpperFieldLimit( upperLimit );
            }
        }
    }
    
    
    /** determine whether the lower field limit is custom */
    public boolean isLowerFieldLimitCustom() {
        return !Double.isNaN( _lowerFieldLimit );
    }
    
    
    /** determine whether the upper field limit is custom */
    public boolean isUpperFieldLimitCustom() {
        return !Double.isNaN( _upperFieldLimit );
    }
	
	
	/** Get lower field limit and default to the hardware limit if none have been set */
	public double getLowerFieldLimit() {
		return isLowerFieldLimitCustom() ? _lowerFieldLimit : getHardLowerFieldLimit();
	}
	
	
	/** Set the lower field limit */
	public void setLowerFieldLimit( final double limit ) {
		_lowerFieldLimit = limit;
	}
	
	
	/** Get upper field limit and default to the hardware limit if none have been set */
	public double getUpperFieldLimit() {
		return isUpperFieldLimitCustom() ? _upperFieldLimit : getHardUpperFieldLimit();
	}
	
	
	/** Get the upper field limit as a formatted number */
	public FormattedNumber getFormattedUpperFieldLimit() {
		return new FormattedNumber( "0.0000", getUpperFieldLimit() );
	}
	
	
	/** Set the upper field limit as a formatted number */
	public void setFormattedUpperFieldLimit( final FormattedNumber number ) {
        setUpperFieldLimit( number.doubleValue() );
	}
	
	
	/** Set the upper field limit */
	public void setUpperFieldLimit( final double limit ) {
		_upperFieldLimit = limit;
	}
	
	
	/** Get the lower field limit as a formatted number */
	public FormattedNumber getFormattedLowerFieldLimit() {
		return new FormattedNumber( "0.0000", getLowerFieldLimit() );
	}
	
	
	/** Get hardware lower field limit */
	public double getHardLowerFieldLimit() {
		return LIMITS_HANDLER != null ? LIMITS_HANDLER.getFieldLimits()[0] : Double.NaN;
	}
	
	
	/** Get hardware upper field limit */
	public double getHardUpperFieldLimit() {
		return LIMITS_HANDLER != null ? LIMITS_HANDLER.getFieldLimits()[1] : Double.NaN;
	}
	
	
	/**
	 * Read the field set point.
	 * @return the current field set point
	 */
	public double getFieldSetting() throws xal.ca.ConnectionException, xal.ca.GetException {
		return _powerSupply.getFieldSetting();
	}
	
	
	/**
	 * Get the latest value of the field.
	 * @return the most recent field in Tesla
	 */
	public double getLatestField() {
		return _latestField;
	}
	
	
	/** Get the latest field as a formatted number */
	public FormattedNumber getFormattedLatestField() {
		return new FormattedNumber( "0.0000", getLatestField() );
	}
	
	
	/** Set the lower field limit as a formatted number */
	public void setFormattedLowerFieldLimit( final FormattedNumber number ) {
        setLowerFieldLimit( number.doubleValue() );
	}
	
	
	/**
	 * Set the field to the value specified.
	 * @param field the field in Tesla
	 */
	public void setField( final double field ) {
		try {
			_powerSupply.setField( field );
		}
		catch ( Exception exception ) {
			throw new RuntimeException( "Field setting exception.", exception );
		}
	}
	
	
	/**
	 * Set the field to the value specified.
	 * @param field the field in Tesla
	 */
	public void requestFieldSetting( final double field ) {
		try {
			_fieldWriteChannelWrapper.getChannel().putValCallback( field, PUT_HANDLER );
		}
		catch( Exception exception ) {
			throw new RuntimeException( "Field setting request exception.", exception );
		}
	}
	
	
	/** Determine if the supply powers a corrector dipole rather than a bend */
	public boolean isCorrectorSupply() {
		return _correctors.get(0).isCorrector();
	}
	
	
	/**
	 * Get the value associated with the specified key.
	 * @param key The key for which to get the associated value.
	 * @return The value as an Object.
	 */
    public Object valueForKey( final String key ) {
		if ( key.equals( CORRECTOR_KEY ) ) {
			return isCorrectorSupply();
		}
		if ( key.equals( SINGLE_MAGNET_KEY ) ) {
			return _correctors.size() == 1;
		}
		if ( key.equals( PLANE_KEY ) ) {
			return isHorizontal() ? "X" : "Y";
		}
		else {
			return null;
		}
	}
	
	
	/** Determine if the supply powers horizontal correctors */
	public boolean isHorizontal() {
		return _correctors.get(0).isHorizontal();
	}
	
	
	/** Determine if the supply powers vertical correctors */
	public boolean isVertical() {
		return _correctors.get(0).isVertical();
	}
	
	
	/**
	 * Determine whether the corrector supply is horizontal or vertical.
	 * @return   MagnetType.HORIZONTAL for a horizontal corrector and MagnetType.VERTICAL for a vertical one
	 */
	public int getOrientation() {
		return _correctors.get(0).getCorrector().getOrientation();
	}
	
	
	/**
	 * Get the position of the first corrector relative to the start of the specified sequence.
	 * @param sequence  The sequence relative to which the corrector's position is measured
	 * @return          the position of this supply's first corrector relative to the sequence in meters
	 */
	public double getFirstCorrectorPositionIn( AcceleratorSeq sequence ) {
		return sequence.getPosition( _correctors.get(0).getCorrector() );
	}
	
	
	/**
	 * Get the string representation of the corrector supply.
	 * @return   the corrector's string representation
	 */
	public String toString() {
		return getID();
	}
	
	
	/** Wrap the corrector's field write channel. */
	protected void wrapChannels() {
		_fieldWriteChannelWrapper = wrapChannel( MagnetMainSupply.FIELD_SET_HANDLE );
	}
	
	
	/**
	 * Wrap the corrector's channel corresponding to the specified handle.
	 * @param handle  Description of the Parameter
	 * @return        Description of the Return Value
	 */
	protected ChannelWrapper wrapChannel( final String handle ) {
		Channel channel = _powerSupply.getChannel( handle );
		ChannelWrapper wrapper = new ChannelWrapper( channel );
		
		wrapper.addChannelEventListener(
			new ChannelEventListener() {
				/**
				 * The PV's monitored value has changed.
				 * @param channel  the channel whose value has changed
				 * @param record   The channel time record of the new value
				 */
				public void valueChanged( Channel channel, ChannelTimeRecord record ) {
					_latestField = record.doubleValue();
					EVENT_PROXY.fieldChanged( CorrectorSupply.this, record, _latestField );
				}
				
				
				/**
				 * The channel's connection has changed. Either it has established a new connection or the existing connection has dropped.
				 * @param channel    The channel whose connection has changed.
				 * @param connected  The channel's new connection state
				 */
				public void connectionChanged( Channel channel, boolean connected ) {
					_latestField = Double.NaN;
					EVENT_PROXY.connectionChanged( CorrectorSupply.this, connected );
				}
			} );

		wrapper.requestConnection();
		
		return wrapper;
	}
	
	
	
	/** Connection handler */
	protected class LimitsHandler implements ConnectionListener, IEventSinkValue {
		/** the channel with the lower limit */
		protected Channel _lowerChannel;
		
		/** the channel with the upper limit */
		protected Channel _upperChannel;
		
		/** the raw lower limit */
		protected double _rawLowerFieldLimit;
		
		/** the raw upper limit */
		protected double _rawUpperFieldLimit;
		
		/** the lower and upper limits */
		protected double[] _fieldLimits;
		
		/** monitor for lower limit */
		protected Monitor _lowerMonitor;
		
		/** monitor for upper limit */
		protected Monitor _upperMonitor;
		
		
		/** Constructor */
		public LimitsHandler( final Channel channel ) {
			_fieldLimits = new double[2];
			_fieldLimits[0] = _fieldLimits[1] = Double.NaN;
			setChannel( channel );
		}
		
		
		/** Determine if limits have been found */
		public boolean hasLimits() {
			synchronized( this ) {
				return  !Double.isNaN( _rawLowerFieldLimit ) && !Double.isNaN( _rawUpperFieldLimit );
			}
		}
		
		
		/** Get the field limits */
		public double[] getFieldLimits() {
			synchronized( this ) {
				return _fieldLimits;
			}
		}
		
		
		/** set a new channel whose limits we wish to monitor */
		public void setChannel( final Channel channel ) {
			synchronized( this ) {
				if ( _lowerChannel != null ) {
					_lowerChannel.removeConnectionListener( this );
					if ( _lowerMonitor != null ) {
						_lowerMonitor.clear();
						_lowerMonitor = null;
					}
				}
				
				final String lowerLimitPV = channel.channelName() + ".LOPR";
				_lowerChannel = ChannelFactory.defaultFactory().getChannel( lowerLimitPV );
				_lowerChannel.addConnectionListener( this );
				_lowerChannel.requestConnection();
				
				if ( _upperChannel != null ) {
					_upperChannel.removeConnectionListener( this );
					if ( _upperMonitor != null ) {
						_upperMonitor.clear();
						_upperMonitor = null;
					}
				}
				
				final String upperLimitPV = channel.channelName() + ".HOPR";
				_upperChannel = ChannelFactory.defaultFactory().getChannel( upperLimitPV );
				_upperChannel.addConnectionListener( this );
				_upperChannel.requestConnection();
			}
		}
		
		
		/** handle the connection event */
		public void connectionMade( final Channel channel ) {
			try {
				if ( channel == _lowerChannel ) {
					_lowerMonitor = channel.addMonitorValue( this, Monitor.VALUE );
				}
				else if ( channel == _upperChannel ) {
					_upperMonitor = channel.addMonitorValue( this, Monitor.VALUE );
				}
				Channel.flushIO();
			}
			catch ( ConnectionException exception ) {}
			catch ( MonitorException excepiton ) {}
		}
		
		
		/** handle the disconnect event */
		public void connectionDropped( final Channel channel ) {
		}
		
		
		/** handle the get value callback */
		public void eventValue( final ChannelRecord record, final Channel channel ) {
			synchronized( this ) {
				if ( channel == _lowerChannel ) {
					_rawLowerFieldLimit = record.doubleValue();
				}
				else if ( channel == _upperChannel ) {
					_rawUpperFieldLimit = record.doubleValue();
				}
				
				if ( hasLimits() ) {
					_fieldLimits[0] = Math.min( _rawLowerFieldLimit, _rawUpperFieldLimit );
					_fieldLimits[1] = Math.max( _rawLowerFieldLimit, _rawUpperFieldLimit );
					EVENT_PROXY.fieldLimitsChanged( CorrectorSupply.this, _fieldLimits[0], _fieldLimits[1] );
				}
			}
		}
	}
	
	
	
	/** handler of put events */
	protected class PutHandler implements PutListener {
		public void putCompleted( final Channel channel ) {
		}
	}
}
