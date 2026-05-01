/*
 * ChannelModel.java
 *
 * Created on January 23, 2003, 3:43 PM
 */

package xal.app.scope;

import xal.tools.ArrayMath;
import xal.tools.data.*;
import xal.tools.messaging.MessageCenter;
import xal.tools.dispatch.DispatchQueue;
import xal.ca.*;
import xal.tools.correlator.*;
import xal.extension.application.Util;

/**
 * Model for a single waveform channel.
 *
 * @author  tap
 */
public class ChannelModel implements TraceSource, DataListener, TimeModelListener, ConnectionListener {
    // constants
    final static String DATA_LABEL = "ChannelModel";
	final private static String SAMPLE_PERIOD_PV_SUFFIX;
	final private static String DELAY_PV_SUFFIX;
    
    // attributes
    final private String ID;
    
    // messaging variables
    final protected MessageCenter MESSAGE_CENTER;
    final protected ChannelModelListener CHANNEL_MODEL_PROXY;
    final protected SettingListener SETTING_PROXY;
    
    // model hierarchy
    final private TimeModel _timeModel;
    
    // model state variables
    protected boolean _enabled;      // is the channel enabled for monitoring
    protected double _signalScale;   // units per division
    protected double _signalOffset;  // units offset
    protected volatile boolean _isSettingChannel;    // true if the channel is being set
	protected volatile boolean _isReady;		// true if the channel model is ready for use
	protected volatile boolean _waveformDelayInitialized;	// true if the waveform delay has been initialized
    
    // Channel infrastructure
    protected Channel _channel;      // channel being monitored
    protected Channel _delayChannel;
    protected Monitor _waveformDelayMonitor;
    protected Channel _samplePeriodChannel;
    protected Monitor _samplePeriodMonitor;
    
    // Channel data
    protected double _waveformDelay;
    protected double _samplePeriod;
    protected double[] _elementTimes;

	/** indicates that the current instance supports events and is not just a cheap copy */
	final private boolean SUPPORTS_EVENTS;

	/** queue to synchronize busy state for modifications and access */
	private final DispatchQueue BUSY_QUEUE;


	// static initializer
    static {
		final String SAMPLE_PERIOD_PV_SUFFIX_KEY = "waveformSamplePeriodPvSuffix";
		final String DELAY_PV_SUFFIX_KEY = "waveformDelayPvSuffix";
		java.util.Map<String,String> properties = Util.getPropertiesForResource( "scope.properties" );
		// use the scope.properties file to get the defaul value, but allow it to be overriden as a command line property
		SAMPLE_PERIOD_PV_SUFFIX = System.getProperties().getProperty(SAMPLE_PERIOD_PV_SUFFIX_KEY, properties.get(SAMPLE_PERIOD_PV_SUFFIX_KEY));
		DELAY_PV_SUFFIX = System.getProperties().getProperty(DELAY_PV_SUFFIX_KEY, properties.get(DELAY_PV_SUFFIX_KEY));
	}
    
    
    /** Creates a new instance of ChannelModel */
    public ChannelModel( final String anId, final TimeModel aTimeModel) {
        this( anId, null, aTimeModel );
    }

    
    /** Create a new channel model with the specified channel name */
    public ChannelModel( final String anID, final String channelName, final TimeModel aTimeModel ) {
		this( anID, channelName, aTimeModel, true );
    }


    /** Create a new channel model with the specified channel name */
    private ChannelModel( final String anID, final String channelName, final TimeModel aTimeModel, final boolean supportsEvents ) {
		BUSY_QUEUE = DispatchQueue.createConcurrentQueue( "channel model busy" );
		SUPPORTS_EVENTS = supportsEvents;

		_isReady = false;
        ID = anID;

        _isSettingChannel = false;

        _timeModel = aTimeModel;
		if ( SUPPORTS_EVENTS ) {
			MESSAGE_CENTER = new MessageCenter( "Channel Model" );
			CHANNEL_MODEL_PROXY = MESSAGE_CENTER.registerSource( this, ChannelModelListener.class );
			SETTING_PROXY = MESSAGE_CENTER.registerSource( this, SettingListener.class );
			
			_timeModel.addTimeModelListener( this );
			setChannel( channelName );
			setEnabled( false );
			setSignalScale( 1.0 );
			setSignalOffset( 0 );
		}
		else {
			MESSAGE_CENTER = null;
			CHANNEL_MODEL_PROXY = null;
			SETTING_PROXY = null;
			
			if ( channelName != null ) {
				_channel = ChannelFactory.defaultFactory().getChannel( channelName );
			}
			_enabled = false;
			_signalScale = 1.0;
			_signalOffset = 0;
		}

		_waveformDelayInitialized = false;
        _waveformDelay = 0;
        _samplePeriod = 0;
		_elementTimes = new double[0];
    }


	/** make a cheap (excludes monitors and listeners), safe (synchronized snapshot) copy of basic properties */
	public ChannelModel cheapCopy() {
		final ChannelModel duplicate = new ChannelModel( ID, null, _timeModel, false );
		propertyCopyTo( duplicate );
		return duplicate;
	}


	/** copy basic properties to the target (excludes monitors and listeners) */
	private void propertyCopyTo( final ChannelModel target ) {
		BUSY_QUEUE.dispatchSync( new Runnable() {
			public void run() {
				target._channel = _channel;
				target._delayChannel = _delayChannel;
				target._samplePeriodChannel = _samplePeriodChannel;

				target._enabled = _enabled;
				target._isReady = _isReady;
				target._signalOffset = _signalOffset;
				target._signalScale = _signalScale;

				target._isSettingChannel = _isSettingChannel;
				target._waveformDelayInitialized = _waveformDelayInitialized;
				target._waveformDelay = _waveformDelay;
				target._samplePeriod = _samplePeriod;
				target._elementTimes = _elementTimes;
			}
		});
	}
    
    
    /**
     * Get the channel model's ID.
     * @return The channel model's ID.
     */
    public String getID() {
        return ID;
    }
	
	
	/**
	 * Get the label for this channel model
	 * @return The name of the channel.
	 */
	public String getLabel() {
		return (_channel != null) ? _channel.channelName() : "";
	}
    
    
    /** 
     * Get the name used to identify the class in an external data source.
     * @return The tag for this data node.
     */
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /**
     * Instructs the receiver to update its data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update( final DataAdaptor adaptor ) throws ChannelSetException {
        if ( adaptor.hasAttribute( "channel" ) ) {
            setChannel( adaptor.stringValue( "channel" ) );
        }
        if ( adaptor.hasAttribute( "delayChannel" ) ) {
            setDelayChannel( adaptor.stringValue( "delayChannel" ) );
        }
        if ( adaptor.hasAttribute( "samplePeriodChannel" ) ) {
            setSamplePeriodChannel( adaptor.stringValue( "samplePeriodChannel" ) );
        }
        setSignalScale( adaptor.doubleValue( "signalScale" ) );
        setSignalOffset( adaptor.doubleValue( "signalOffset" ) );
        setEnabled( adaptor.booleanValue( "enabled" ) );
    }
    
    
    /**
     * Instructs the receiver to write its data to the adaptor for external
     * storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write(DataAdaptor adaptor) {
        if ( _channel != null ) {
            adaptor.setValue( "channel", _channel.channelName() );
        }
		if ( _delayChannel != null ) {
			adaptor.setValue( "delayChannel" , _delayChannel.channelName() );
		}
		if ( _samplePeriodChannel != null ) {
			adaptor.setValue( "samplePeriodChannel" , _samplePeriodChannel.channelName() );
		}
        adaptor.setValue( "enabled", _enabled );
        adaptor.setValue( "signalScale", _signalScale );
        adaptor.setValue( "signalOffset", _signalOffset );
    }
    
    
    /**
     * Dispose of the resources held by this model.  In particular, we shutdown
     * the monitors.
     */
    void dispose() {
        try {
            stopChannelEvents();
        }
        catch(Exception exception) {
            System.err.println( "Failed to dispose of channel model: " + ID );
            System.err.println( exception );
            exception.printStackTrace();
        }
        finally {
        }
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
     * Stop listentening for waveform connection events and stop monitoring 
     * the delay and period channels.
     */
    protected void stopChannelEvents() {
        // stop listening to the waveform channel connection status
        if ( _channel != null ) {
            _channel.removeConnectionListener( this );
        }
        
        // stop monitoring time
        stopMonitoringTime();        
    }
    
    
    /** 
     * Add a ChannelModelListener.
     * @param listener The object to add as a listener of channel model events.
     */
    public void addChannelModelListener( final ChannelModelListener listener ) {
        MESSAGE_CENTER.registerTarget( listener, this, ChannelModelListener.class );
    }
    
    
    /** 
     * Remove a ChannelModelListener.
     * @param listener The object to remove as a listener of channel model events.
     */
    public void removeChannelModelListener( final ChannelModelListener listener ) {
        MESSAGE_CENTER.removeTarget( listener, this, ChannelModelListener.class );
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
	private void postChannelChangeEvent( final Channel channel ) {
		DispatchQueue.getGlobalDefaultPriorityQueue().dispatchAsync( new Runnable() {
			public void run() {
				CHANNEL_MODEL_PROXY.channelChanged( ChannelModel.this, channel );    // notify the listeners
			}
		});
	}


	/** post the channel change event */
	private void postChannelEnableChangeEvent( final Channel channel ) {
		DispatchQueue.getGlobalDefaultPriorityQueue().dispatchAsync( new Runnable() {
			public void run() {
				CHANNEL_MODEL_PROXY.enableChannel( ChannelModel.this, channel );    // notify the listeners
			}
		});
	}


	/** post the channel change event */
	private void postChannelDisableChangeEvent( final Channel channel ) {
		DispatchQueue.getGlobalDefaultPriorityQueue().dispatchAsync( new Runnable() {
			public void run() {
				CHANNEL_MODEL_PROXY.disableChannel( ChannelModel.this, channel );    // notify the listeners
			}
		});
	}


	/** post the channel change event */
	private void postElementTimesChangedEvent( final double[] elementTimes ) {
		DispatchQueue.getGlobalDefaultPriorityQueue().dispatchAsync( new Runnable() {
			public void run() {
				CHANNEL_MODEL_PROXY.elementTimesChanged( ChannelModel.this, elementTimes );    // notify the listeners
			}
		});
	}


	/** post the channel change event */
	private void postSettingChangeEvent() {
		DispatchQueue.getGlobalDefaultPriorityQueue().dispatchAsync( new Runnable() {
			public void run() {
				SETTING_PROXY.settingChanged( ChannelModel.this );
			}
		});
	}

    
    /**
     * Determine if the channel is being set.
     * @return true if the channel is being set and false otherwise.
     */
    public boolean isSettingChannel() {
        return _isSettingChannel;
    }

    
    /** 
     * Change the waveform channel to that specified by the channel name.  Also monitor the waveform's offset from cycle start and the width of each element.
     * @param channelName The name of the channel to set.
     * @throws xal.app.scope.ChannelSetException if one or more of the required associated channels cannot connect.
     */
    public void setChannel( final String channelName ) {
		dispatchUpdateOperation( new Runnable() {
			public void run() {
				_isReady = false;
				_isSettingChannel = true;

				setEnabled( false );
				stopChannelEvents();

				_waveformDelayInitialized = false;
				_samplePeriod = 0;
				_waveformDelay = 0;

				if ( channelName == null || channelName.isEmpty() ) {
					_channel = null;
				}
				else {
					_channel = ChannelFactory.defaultFactory().getChannel( channelName );
					postChannelDisableChangeEvent( _channel );

					_channel.addConnectionListener( ChannelModel.this );
					_channel.requestConnection();
					setupTimeChannels();
					Channel.flushIO();
					setEnabled( true );
				}

				_isSettingChannel = false;
				_isReady = true;

				postChannelChangeEvent( _channel );
				postSettingChangeEvent();
			}
		});
    }

    
    /** 
     * Get the waveform channel.
     * @return The waveform channel.
     */
    public Channel getChannel() {
        return _channel;
    }
    
    
    /** 
     * Get the waveform channel name.
     * @return The waveform channel name.
     */
    public String getChannelName() {
        if ( _channel != null ) {
            return _channel.channelName();
        }
        else {
            return null;
        }
    }
	
	
	/**
	 * Set the delay channel to the specified PV.
	 * @param pv the PV of the delay channel
	 */
	public void setDelayChannel( final String pv ) {
		final Channel oldDelayChannel = _delayChannel;
		
		// first check whether there is anything to do
		if ( oldDelayChannel != null && pv.equals( oldDelayChannel.channelName() ) )  return;

		_isReady = false;

		dispatchUpdateOperation( new Runnable() {
			public void run() {
				stopMonitoringDelay();

				if ( oldDelayChannel != null ) {
					oldDelayChannel.removeConnectionListener( ChannelModel.this );
				}

				if ( pv != null && pv.length() > 0 && !pv.equals( "" ) ) {
					_delayChannel = ChannelFactory.defaultFactory().getChannel( pv );
					_delayChannel.addConnectionListener( ChannelModel.this );
					_delayChannel.requestConnection();
					Channel.flushIO();
				}
				else {
					_delayChannel = null;
				}

				postSettingChangeEvent();
			}
		});
	}
	
	
	/**
	 * Get the delay channel
	 * @return the delay channel
	 */
	public Channel getDelayChannel() {
		return _delayChannel;
	}
	
	
	/**
	 * Set the sample period channel to that specified by the PV.
	 * @param pv the PV for which to set the sample period channel
	 */
	public void setSamplePeriodChannel( final String pv ) {
		dispatchUpdateOperation( new Runnable() {
			public void run() {
				final Channel oldSamplePeriodChannel = _samplePeriodChannel;

				// first check whether there is anything to do
				if ( oldSamplePeriodChannel != null && pv.equals( oldSamplePeriodChannel.channelName() ) )  return;

				try {
					stopMonitoringSamplePeriod();

					if ( oldSamplePeriodChannel != null ) {
						oldSamplePeriodChannel.removeConnectionListener( ChannelModel.this );
					}

					if ( pv != null && pv.length() > 0 && !pv.equals( "" ) ) {
						_samplePeriodChannel = ChannelFactory.defaultFactory().getChannel( pv );
						_samplePeriodChannel.addConnectionListener( ChannelModel.this );
						_samplePeriodChannel.requestConnection();
						Channel.flushIO();
					}
					else {
						_samplePeriodChannel = null;
					}
				}
				finally {
					postSettingChangeEvent();
				}
			}
		});
	}
	
	
	/**
	 * Get the period channel
	 * @return the sample period channel
	 */
	public Channel getSamplePeriodChannel() {
		return _samplePeriodChannel;
	}
    
    
    /**
     * Get the number of elements in the waveform.
     * @return The number of elements in the waveform
     */
    public int getNumElements() {
        try {
            return _channel.elementCount();
        }
        catch( ConnectionException exception ) {
            System.err.println( exception );
            return 0;
        }
    }
    
    
    /**
     * Create and connect to the channels that provide the offset form cycle start and the period between the waveform elements.
     * @throws xal.app.scope.ChannelSetException if one or more of the required associated channels cannot connect.
     */
    protected void setupTimeChannels() throws ChannelSetException {
		stopMonitoringTime();
		
		// remove old time channels
		if ( _delayChannel != null ) {
			_delayChannel.removeConnectionListener( this );
		}
		if ( _samplePeriodChannel != null ) {
			_samplePeriodChannel.removeConnectionListener( this );
		}
		
        String channelName = _channel.channelName();
        int handleIndex = channelName.lastIndexOf( ":" );
		if ( handleIndex < 1 )  return;		// not a valid channel name
        String baseName = channelName.substring( 0, handleIndex );
		
		setDelayChannel( baseName + ":" + DELAY_PV_SUFFIX );
		setSamplePeriodChannel( baseName + ":" + SAMPLE_PERIOD_PV_SUFFIX );
    }
	
	
	/**
	 * Handle the waveform connection event.
	 * @param waveformChannel the waveform channel
	 */
	protected void handleWaveformConnection( final Channel waveformChannel ) {
		dispatchUpdateOperation( new Runnable() {
			public void run() {
				try {
					_isReady = false;
					int numElements = _channel.elementCount();
					_elementTimes = new double[numElements];

					updateElementTimes();
					_isReady = true;
				}
				catch( ConnectionException exception ) {
					System.err.println( exception );
				}
			}
		});
	}
	
	
	/** Monitor the delay channel. */
	protected void monitorDelayChannel() {
		dispatchUpdateOperation( new Runnable() {
			public void run() {
				try {
					_isReady = false;

					if ( _waveformDelayMonitor == null ) {
						_waveformDelayMonitor = _delayChannel.addMonitorValue( new WaveformDelayListener(), Monitor.VALUE );
					}

					updateElementTimes();
					_isReady = true;
				}
				catch( ConnectionException exception ) {
					System.err.println( exception );
				}
				catch( MonitorException exception ) {
					System.err.println( exception );
				}
			}
		});
	}
	
	
	/** Monitor the sample period channel. */
	protected void monitorSamplePeriod() {
		dispatchUpdateOperation( new Runnable() {
			public void run() {
				try {
					_isReady = false;

					if ( _samplePeriodMonitor == null ) {
						_samplePeriodMonitor = _samplePeriodChannel.addMonitorValue( new SamplePeriodListener(), Monitor.VALUE );
					}

					updateElementTimes();
					_isReady = true;
				}
				catch( ConnectionException exception ) {
					System.err.println( exception );
				}
				catch( MonitorException exception ) {
					System.err.println( exception );
				}
			}
		});
	}
    
    
    /**
     * Create the array of time elements (one time element for each element of the waveform).
     * The time element array is generated from the length of the waveform, the delay time
     * and the sample period.
     */
    protected void updateElementTimes() {
		dispatchUpdateOperation( new Runnable() {
			public void run() {
				if ( _elementTimes == null || !channelsConnected() )  return;    // nothing to update

				final int numElements = _elementTimes.length;
				double timeMark = _waveformDelay;

				for ( int index = 0 ; index < numElements ; index++ ) {
					_elementTimes[index] = timeMark;
					timeMark += _samplePeriod;
				}
				_timeModel.convertTurns(_elementTimes);

				postElementTimesChangedEvent( _elementTimes );
			}
		});
    }
    
    
    /** Stop monitoring the delay channel. */
    private void stopMonitoringDelay() {
		dispatchUpdateOperation( new Runnable() {
			public void run() {
				_isReady = false;

				// disable the present waveform monitor
				if ( _waveformDelayMonitor != null ) {
					_waveformDelayMonitor.clear();
					_waveformDelayMonitor = null;
				}
			}
		});
    }
    
    
    /** Stop monitoring the sample period channel. */
    private void stopMonitoringSamplePeriod() {
		dispatchUpdateOperation( new Runnable() {
			public void run() {
				_isReady = false;

				// disable the present sample period monitor
				if ( _samplePeriodMonitor != null ) {
					_samplePeriodMonitor.clear();
					_samplePeriodMonitor = null;
				}
			}
		});
    }
    
    
    /** Stop monitoring the time PVs which determine the offset from cycle start and the period between waveform elements. */
    private void stopMonitoringTime() {
		dispatchUpdateOperation( new Runnable() {
			public void run() {
				_isReady = false;

				stopMonitoringDelay();
				stopMonitoringSamplePeriod();
			}
		});
    }
    
    
    /**
     * Get the waveform delay in units of turns.
     * @return the waveform delay in units of turns.
     */
    final public double getWaveformDelay() {
		return _waveformDelay;
    }
    
    
    /**
     * Get the sample period in units of turns.
     * @return the sample period in units of turns.
     */
    final public double getSamplePeriod() {
		return _samplePeriod;
    }
    
    
    /**
     * Get the array of time elements for the waveform.  Each time element represents
     * the time associated with the corresponding waveform element.  The time unit is 
     * a turn.
     * @return The element times in units of turns relative to cycle start.
     */
    final public double[] getElementTimes() {
		return _elementTimes;
    }
    
    
    /**
     * Event indicating that the time units of the time model sender has changed.
     * @param sender The sender of the event.
     */
    public void timeUnitsChanged( final TimeModel sender ) {}
    
    
    /**
     * Event indicating that the time conversion of the time model sender has changed.
     * This is most likely due to the scaling changing.  For example the turn to 
     * microsecond conversion is monitored and may change during the lifetime of 
     * the application.
     * @param sender The sender of the event.
     */
    public void timeConversionChanged( final TimeModel sender ) {
        updateElementTimes();
    }
    
    
    /** 
     * Get the scale to be applied to the signal trace.
     * @return the scale applied to the singal trace.
     */
    final public double getSignalScale() {
        return _signalScale;
    }
    
    
    /** 
     * Set the scale to be applied to the signal trace.
     * @param newScale The new scale to apply to the singal trace.
     */
    final public void setSignalScale( final double newScale ) {
        if ( _signalScale != newScale ) {
            _signalScale = newScale;
			postSettingChangeEvent();
        }
    }
    
    
    /** 
     * Get the offset to be applied to the signal trace.
     * @return The offset applied to the singal trace.
     */
    final public double getSignalOffset() {
        return _signalOffset;
    }
    
    
    /** 
     * Set the scale to be applied to the signal trace.
     * @param newOffset The new offset to apply to the signal trace.
     */
    final public void setSignalOffset( final double newOffset ) {
        if ( _signalOffset != newOffset ) {
            _signalOffset = newOffset;
			postSettingChangeEvent();
        }
    }
    
    
    /**
     * Get the trace for the specified record.  Process the raw record to account
     * for the signal scale and signal offset.
     * @param correlation The correlation from which to get the channel's record and generate the trace.
     * @return the waveform trace
     */
    final public double[] getTrace( final Correlation<ChannelTimeRecord> correlation ) {
        final ChannelRecord record = correlation.getRecord( ID );        
        return record == null ? null : ArrayMath.transform( record.doubleArray(), _signalScale, _signalOffset );
    }
	
	
    /**
     * Get the trace event for this trace source extracted from the correlation.
	 * @param correlation The correlation from which the trace is extracted.
	 * @return the trace event corresponding to this trace source and the correlation
     */
	final public TraceEvent getTraceEvent( final Correlation<ChannelTimeRecord> correlation ) {
        final ChannelRecord record = correlation.getRecord( ID );        
		return record == null ? null : new ChannelTraceEvent( this, record.doubleArray(), _elementTimes );
	}
    
    
    /** 
     * Determine if the waveform is enabled.
     * @return true if the waveform is enabled, false otherwise.
     * @see #setEnabled
     */
    final public boolean isEnabled() {
        return _enabled;
    }
    
    
    /** 
     * Set whether the waveform is enabled.  The waveform is only enabled if the 
     * waveform can be enabled given the status of the settings.
     * @param state true to enable the waveform and false to disable it.
     * @see #isEnabled
     */
    public void setEnabled( final boolean state ) throws ChannelSetException {
		dispatchUpdateOperation( new Runnable() {
			public void run() {
				if ( _enabled != state ) {
					_enabled = (_channel != null) ? state : false;
					if ( _enabled ) {
						if ( channelsConnected() ) {
							monitorDelayChannel();
							monitorSamplePeriod();
						}
						postChannelEnableChangeEvent( _channel );
					}
					else {
						if ( _channel != null ) {
							stopMonitoringTime();
							postChannelDisableChangeEvent( _channel );
						}
					}

					postSettingChangeEvent();
				}
			}
		});
    }


    /** Toggle the channel enable */
    public void toggleEnable() {
        setEnabled( !_enabled );
    }
	
	
	/**
	 * Determine if the waveform channel and time channels are all set and connected
	 * @return true if the channels are set and connected
	 */
	public boolean channelsConnected() {
		return _channel != null && _channel.isConnected() && _delayChannel != null && _delayChannel.isConnected() && _samplePeriodChannel != null && _samplePeriodChannel.isConnected();
	}
	
	
	/**
	 * Determine if the channel can be monitored which indicates that the waveform channel,
	 * delay channel and period channel are all connected and element times are set.
	 * @return true if the waveform can be monitored and false if not
	 */
	public boolean canMonitor() {
		return channelsConnected() && _elementTimes != null && _samplePeriod != 0 && _waveformDelayInitialized && _isReady;
	}
	
    
    /**
     * Indicates that a connection to the specified channel has been established.
     * @param channel The channel which has been connected.
     */
    public void connectionMade( final Channel channel ) {
		if ( channel == _channel ) {
			handleWaveformConnection( channel );
		}
		else if ( channel == _delayChannel ) {
			monitorDelayChannel();
		}
		else if ( channel == _samplePeriodChannel ) {
			monitorSamplePeriod();
		}
		
		postChannelChangeEvent( _channel );
    }
    
    
    /**
     * Indicates that a connection to the specified channel has been dropped.
     * @param channel The channel which has been disconnected.
     */
    public void connectionDropped( final Channel channel ) {
		postChannelChangeEvent( channel );
    }
    
    
    
    /**
     * Listener of monitor events associated with the delay time for the waveform.
     */
    class WaveformDelayListener implements IEventSinkValue {
        /**
         * Callback which updates the waveform delay and then recalculates the 
         * the element time array.
         */
        public void eventValue( final ChannelRecord record, final Channel chan ) {
            final double newDelay = record.doubleValue();

			dispatchUpdateOperation( new Runnable() {
				public void run() {
					boolean postChange = false;
					
					if ( newDelay != _waveformDelay ) {
						_waveformDelay = newDelay;
						updateElementTimes();
					}

					// since the new waveform delay could be zero we need to make sure the receivers start monitoring
					if ( !_waveformDelayInitialized ) {
						postChange = true;
						_waveformDelayInitialized = true;
					}
					
					if ( postChange ) {
						postChannelChangeEvent( _channel );
					}
				}
			});            
        }
    }
    
    
    
    /**
     * Listener of monitor events associated with the sample period for the waveform elements.
     */
    class SamplePeriodListener implements IEventSinkValue {
        /**
         * Callback which updates the waveform sample period and then recalculates the 
         * the element time array.
         */
        public void eventValue( final ChannelRecord record, final Channel chan ) {
            final double newPeriod = record.doubleValue();

			if ( newPeriod != _samplePeriod ) {
				dispatchUpdateOperation( new Runnable() {
					public void run() {
						_samplePeriod = newPeriod;
						updateElementTimes();

						postChannelChangeEvent( _channel );
					}
				});
			}
        }
    }
}
