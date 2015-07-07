package xal.app.mpsclient;

import java.util.concurrent.Callable;
import java.util.*;
import java.math.BigDecimal;

import xal.tools.messaging.MessageCenter;
import xal.service.mpstool.MPSPortal;
import xal.tools.UpdateListener;
import xal.extension.service.*;
import xal.tools.dispatch.DispatchQueue;
import xal.service.pvlogger.RemoteLogging;

public class RemoteMPSRecord implements UpdateListener {

    /* Service proxy */
    private final MPSPortal REMOTE_PROXY;
    
    /* Data caches */
    private final RemoteDataCache<Date> LAUNCH_TIME_CACHE;
        
    private final RemoteDataCache<String> HOST_CACHE;
    
    private final RemoteDataCache<Integer> PROCESS_ID_CACHE;
    
    private final RemoteDataCache<Boolean> LOGS_STATS_CACHE;

    private final RemoteDataCache<List<String>> MPS_TYPES_CACHE;

    private final RemoteDataCache<String>[] FIRST_HIT_TEXT_CACHE;

	/** cache of the timestamps for the latest events */
	private final RemoteDataCache<Date[]>[] LAST_EVENT_TIMESTAMP_CACHE;

    private final RemoteDataCache<List<Map<String, Object>>>[] LATEST_MPS_EVENTS_CACHE;
    
    private final RemoteDataCache<List<Map<String, Object>>>[] MPS_PVS_CACHE;
    
    private final RemoteDataCache<List<Map<String, Object>>>[] INPUT_PVS_CACHE;
    
    private final RemoteDataCache<String>[] TRIP_SUMMARY_CACHE;
    
    private String[] _firstHitText;
    
    /** Remote address */
    private final String REMOTE_ADDRESS;
    
    /** If the service is connected */
    private Boolean serviceOkay = false;
    
    /** List of mps types */
    final private List<String> MPS_TYPES;

    /** Number of mps types */
    final private int MPS_TYPE_COUNT;

    /** Selected mps type index */
    private int _selectedMPSType = 0;

    private UpdateListener _updateListener;

    /** message center for dispatching events */
	private final MessageCenter MESSAGE_CENTER;

	/** proxy to forward events to registered listeners */
	private final RemoteMPSRecordListener EVENT_PROXY;

	/** timestamps of the last events indexed by event ID */
	private final Date[] LAST_EVENT_TIMESTAMPS;

	
	@SuppressWarnings( {"rawtypes", "unchecked"} )		// Generics are incompatible with arrays
    public RemoteMPSRecord( final MPSPortal proxy ) {
        MESSAGE_CENTER = new MessageCenter("MPS Record");
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, RemoteMPSRecordListener.class );

		LAST_EVENT_TIMESTAMPS = new Date[MPSPortal.EVENT_ID_COUNT];

        REMOTE_PROXY = proxy;
        REMOTE_ADDRESS = ((ServiceState)proxy).getServiceHost();

        MPS_TYPES = REMOTE_PROXY.getMPSTypes();
        
        MPS_TYPE_COUNT = MPS_TYPES.size();
        
        System.out.println("MPS TYPES=" + MPS_TYPE_COUNT);
        
        _firstHitText = new String[MPS_TYPE_COUNT];

		LAST_EVENT_TIMESTAMP_CACHE = new RemoteDataCache[MPS_TYPE_COUNT];
		FIRST_HIT_TEXT_CACHE = new RemoteDataCache[MPS_TYPE_COUNT];
        LATEST_MPS_EVENTS_CACHE = new RemoteDataCache[MPS_TYPE_COUNT];
        TRIP_SUMMARY_CACHE = new RemoteDataCache[MPS_TYPE_COUNT];
        MPS_PVS_CACHE = new RemoteDataCache[MPS_TYPE_COUNT];
        INPUT_PVS_CACHE = new RemoteDataCache[MPS_TYPE_COUNT];


        LAUNCH_TIME_CACHE = createRemoteOperationCache( new Callable<Date>() {
            public Date call() {
                return REMOTE_PROXY.getLaunchTime();
            }        
        });
        
        HOST_CACHE = createRemoteOperationCache( new Callable<String>() {
            public String call() {
                return REMOTE_PROXY.getHostName();
            }
        });
        
        PROCESS_ID_CACHE = createRemoteOperationCache( new Callable<Integer>() {
            public Integer call() {
                try {
                    Integer id = REMOTE_PROXY.getProcessID();
                    serviceOkay = true;
                    return id;
                }
                catch(Exception e) {
                    serviceOkay = false;
                }
                return 0;
            }
        });
        
        LOGS_STATS_CACHE = createRemoteOperationCache( new Callable<Boolean>() {
            public Boolean call() {
                return REMOTE_PROXY.logsStatistics();
            }
        });
        
        
        MPS_TYPES_CACHE = createRemoteOperationCache ( new Callable<List<String>> () {
            public List<String> call() {
                return REMOTE_PROXY.getMPSTypes();
            }
        });

        
        for( int mpsTypeIndex = 0; mpsTypeIndex < MPS_TYPE_COUNT; mpsTypeIndex++ ) {
			final int typeIndex = mpsTypeIndex;		// declare local final variable so it can be used in capture blocks

			LAST_EVENT_TIMESTAMP_CACHE[mpsTypeIndex] = createRemoteOperationCache ( new Callable<Date[]> () {
				public Date[] call() {
					return REMOTE_PROXY.getLastEventTimes( typeIndex );
				}
			});
            LAST_EVENT_TIMESTAMP_CACHE[mpsTypeIndex].setUpdateListener( new UpdateListener() {
				public void observedUpdate( final Object source ) {
					final Date[] lastRemoteEventTimestamps = LAST_EVENT_TIMESTAMP_CACHE[typeIndex].getValue();

					final Date lastLocalMPSChannelEventTimestamp = LAST_EVENT_TIMESTAMPS[MPSPortal.MPS_CHANNEL_EVENT_ID];
					final Date lastRemoteMPSChannelEventTimestamp = lastRemoteEventTimestamps[MPSPortal.MPS_CHANNEL_EVENT_ID];
					if ( lastRemoteMPSChannelEventTimestamp != null	) {
						if ( lastLocalMPSChannelEventTimestamp == null || lastRemoteMPSChannelEventTimestamp.after( lastLocalMPSChannelEventTimestamp ) ) {
							MPS_PVS_CACHE[typeIndex].refresh();
							LAST_EVENT_TIMESTAMPS[MPSPortal.MPS_CHANNEL_EVENT_ID] = lastRemoteMPSChannelEventTimestamp;
						}
					}

					final Date lastLocalInputChannelEventTimestamp = LAST_EVENT_TIMESTAMPS[MPSPortal.INPUT_CHANNEL_EVENT_ID];
					final Date lastRemoteInputChannelEventTimestamp = lastRemoteEventTimestamps[MPSPortal.INPUT_CHANNEL_EVENT_ID];
					if ( lastRemoteInputChannelEventTimestamp != null ) {
						if ( lastLocalInputChannelEventTimestamp == null || lastRemoteMPSChannelEventTimestamp.after( lastLocalInputChannelEventTimestamp ) ) {
							INPUT_PVS_CACHE[typeIndex].refresh();
							LAST_EVENT_TIMESTAMPS[MPSPortal.INPUT_CHANNEL_EVENT_ID] = lastRemoteInputChannelEventTimestamp;
						}
					}

					final Date lastLocalMPSEventTimestamp = LAST_EVENT_TIMESTAMPS[MPSPortal.MPS_EVENT_ID];
					final Date lastRemoteMPSEventTimestamp = lastRemoteEventTimestamps[MPSPortal.MPS_EVENT_ID];
					if ( lastRemoteMPSEventTimestamp != null ) {
						if ( lastLocalMPSEventTimestamp == null || lastRemoteMPSEventTimestamp.after( lastLocalMPSEventTimestamp ) ) {
							LATEST_MPS_EVENTS_CACHE[typeIndex].refresh();
							FIRST_HIT_TEXT_CACHE[typeIndex].refresh();
							TRIP_SUMMARY_CACHE[typeIndex].refresh();
							LAST_EVENT_TIMESTAMPS[MPSPortal.MPS_EVENT_ID] = lastRemoteMPSEventTimestamp;
						}
					}
				}
			});
            
			FIRST_HIT_TEXT_CACHE[mpsTypeIndex] = createRemoteOperationCache ( new Callable<String> () {
				public String call() {
					return REMOTE_PROXY.getFirstHitText( typeIndex );
				}
			});
            FIRST_HIT_TEXT_CACHE[mpsTypeIndex].setUpdateListener( this );

            LATEST_MPS_EVENTS_CACHE[mpsTypeIndex] = createRemoteOperationCache( new Callable<List<Map<String, Object>>>() {
                public List<Map<String, Object>> call() {
                    return REMOTE_PROXY.getLatestMPSEvents( typeIndex );
                }
                
            });
            LATEST_MPS_EVENTS_CACHE[mpsTypeIndex].setUpdateListener( this );

            TRIP_SUMMARY_CACHE[mpsTypeIndex] = createRemoteOperationCache(new Callable<String> () {
                public String call() {
                    return REMOTE_PROXY.getMPSTripSummary( typeIndex );
                }
            });
            TRIP_SUMMARY_CACHE[mpsTypeIndex].setUpdateListener( this );
            
            MPS_PVS_CACHE[mpsTypeIndex] = createRemoteOperationCache( new Callable<List<Map<String, Object>>>() {
                public List<Map<String, Object>> call() {
                    return REMOTE_PROXY.getMPSChannelInfo( typeIndex );
                }
                
            });
            MPS_PVS_CACHE[mpsTypeIndex].setUpdateListener( this );
			
            INPUT_PVS_CACHE[mpsTypeIndex] = createRemoteOperationCache( new Callable<List<Map<String, Object>>>() {
                public List<Map<String, Object>> call() {
                    return REMOTE_PROXY.getInputChannelInfo( typeIndex );
                }
                
            });
            INPUT_PVS_CACHE[mpsTypeIndex].setUpdateListener( this );
        }
        
        LAUNCH_TIME_CACHE.setUpdateListener( this );
        HOST_CACHE.setUpdateListener( this );
        PROCESS_ID_CACHE.setUpdateListener( this );
        LOGS_STATS_CACHE.setUpdateListener( this );
        MPS_TYPES_CACHE.setUpdateListener( this );
    }
    
    /**
	 * Add the specified listener as a receiver of events from this record
	 * @param listener the listener to add as a receiver of events
	 */
	public void addRemoteMPSRecordListener( final RemoteMPSRecordListener listener ) {
		MESSAGE_CENTER.registerTarget(listener, this, RemoteMPSRecordListener.class);
    }

	
	/**
	 * Remove the specified listener from being a receiver of events from this record
	 * @param listener the listener to remove from receiving events from this record
	 */
	public void removeRemoteMPSRecordListener( final RemoteMPSRecordListener listener ) {
        MESSAGE_CENTER.removeTarget(listener, this, RemoteMPSRecordListener.class);
	}


    public String getMPSTripSummary(int mpsType) {
        return TRIP_SUMMARY_CACHE[mpsType].getValue();
    }


    /**
	 * Get the most recent MPS event
	 * @param mpsType The index of the MPS latch type for which to get the event list
	 * @return the latest MPS event or null if there is none
	 */
	@SuppressWarnings( "unchecked" )	// have to cast
	public MPSEvent getLatestMPSEvent(int mpsType) {
        _selectedMPSType = mpsType;
        int mpsEvents = getLatestMPSEvents(mpsType).size();
        if(mpsEvents > 0) {
            Map<String, Object> eventsTable = getLatestMPSEvents(mpsType).get(0);
			final MPSEvent event = toMPSEvent( eventsTable );
            return event;
        }
        else {
            return null;
        }
	}

    
	@SuppressWarnings( "unchecked" )	// have to cast
    public List<MPSEvent> processMPSEvents( final int mpsType ) {
        _selectedMPSType = mpsType;
        List<MPSEvent> mpsEvents = new ArrayList<MPSEvent>();
        
        int mpsEventCount = getLatestMPSEvents(mpsType).size();
        
        for( int mpsTypeIndex = 0; mpsTypeIndex < mpsEventCount; mpsTypeIndex++ ) {
            Map<String, Object> eventsTable = getLatestMPSEvents( mpsType ).get( mpsTypeIndex );
			final MPSEvent event = toMPSEvent( eventsTable );
			mpsEvents.add( event );
        }

        return mpsEvents.size() > 0 ? mpsEvents : null;
        
    }


	/* convert raw MPS event info to MPSEvent instance */
	final private MPSEvent toMPSEvent( final Map<String, Object> eventInfo ) {
		final Date eventTimeStamp = (Date)eventInfo.get( MPSPortal.TIMESTAMP_KEY );

		@SuppressWarnings( "unchecked" )	// have to cast
		final List<Map<String, Object>> rawSignalEvents = (List<Map<String, Object>>)eventInfo.get( MPSPortal.SIGNAL_EVENTS_KEY );

		final List<SignalEvent> signalEvents = new ArrayList<>();
		for ( final Map<String,Object> rawSignalEvent : rawSignalEvents ) {
			final String signal = (String)rawSignalEvent.get( MPSPortal.CHANNEL_PV_KEY );
			final String timestampSecondsString = (String)rawSignalEvent.get( MPSPortal.TIMESTAMP_KEY );
			final BigDecimal timestampSeconds = new BigDecimal( timestampSecondsString );
			final SignalEvent signalEvent = new SignalEvent( signal, timestampSeconds );
			signalEvents.add( signalEvent );
		}

		final MPSEvent event = new MPSEvent( eventTimeStamp, signalEvents );
		return event;
	}


    public List<Map<String, Object>> getLatestMPSEvents(int mpsType) {
        _selectedMPSType = mpsType;
        return REMOTE_PROXY.getLatestMPSEvents( mpsType );
    }
    
    
    /**
	 * Get the most recent summary of first hit MPS events
	 * @param mpsType The index of the MPS latch type for which to get the summary
	 * @return the summary of first hit statistics
	 */
	public String getFirstHitText(int mpsType) {
		return _firstHitText[mpsType];
	}
    
    public Boolean getServiceOkay() {
        return serviceOkay;
    }
    
    public Date getLastCheckTime() {
        return new Date();
    }
    
    
    /** Create a remote operation cache for the given operation */
	static private <DataType> RemoteDataCache<DataType> createRemoteOperationCache( final Callable<DataType> operation ) {
		return new RemoteDataCache<DataType>( operation );
	}

	
    /** set the update handler which is called when the cache has been updated */
	public void setUpdateListener( final UpdateListener handler ) {
		_updateListener = handler;
	}

	
	/** get the update handler */
	public UpdateListener getUpdateListener() {
		return _updateListener;
	}

	
    /** called when the source posts an update to this observer */
	public void observedUpdate( final Object source ) {
		// propagate update notification to the update listener if any
		final UpdateListener updateHandler = _updateListener;
		if ( updateHandler != null ) {
			updateHandler.observedUpdate( this );
		}
	}

	
    /** refresh the record */
	public void refresh() {
        try {
            for( int mpsTypeIndex = 0; mpsTypeIndex < MPS_TYPE_COUNT; mpsTypeIndex++ ) {
				LAST_EVENT_TIMESTAMP_CACHE[mpsTypeIndex].refresh();
            }
            MPS_TYPES_CACHE.refresh();
         }
        catch(Exception e) {
            serviceOkay = false;
        }
	}

	
    public List<String> getMPSTypes() {
        return MPS_TYPES_CACHE.getValue();
    }


    public String getHostName() {
        final String hostName = HOST_CACHE.getValue();
        return hostName != null ? hostName : REMOTE_ADDRESS;
    }


    public Date getLaunchTime() {
        return LAUNCH_TIME_CACHE.getValue();
    }


    public Integer getProcessID() {
        return PROCESS_ID_CACHE.getValue();
    }


    public Boolean getLogsStatistics() {
        return LOGS_STATS_CACHE.getValue();
    }


    public void shutdown(int code) {
        REMOTE_PROXY.shutdown(code);
    }

	
    public void reloadSignals() {
		if ( REMOTE_PROXY != null ) {
			try {
				for ( int type = 0 ; type < MPS_TYPE_COUNT ; type++ ) {
					REMOTE_PROXY.reloadSignals( type );
				}
			}
			catch(RemoteMessageException exception) {
				serviceOkay = false;
				throw new RuntimeException("Remote message exception while reloading signals.", exception);
			}
		}
	}

	
    /**
	 * Process the channels by converting each PV table into a convenient ChannelRef.
	 * @param type The MPS latch type for the list of PV tables we wish to process
     * @return List of MPS PVs as ChannelRefs
	 */
    protected List<ChannelRef> getMPSPVs( final int mpsType ) {
        _selectedMPSType = mpsType;
        final List<ChannelRef> channels = new ArrayList<ChannelRef>();
        final List<Map<String, Object>> pvs = MPS_PVS_CACHE[_selectedMPSType].getValue();

        if( pvs == null ) return null;
        if( pvs.size() > 0 ) {
            for( Iterator<Map<String, Object>> iter = pvs.iterator() ; iter.hasNext() ; ) {
                final Map<String, Object> channelMap = iter.next();
                String pv = (String)channelMap.get( MPSPortal.CHANNEL_PV_KEY );
                Boolean connected = (Boolean)channelMap.get( MPSPortal.CHANNEL_CONNECTED_KEY );
                channels.add( new ChannelRef(pv, connected) );	// make the channel reference
            }
            return channels;
        }
        else {
            return null;
        }
    }
    

	/** Get the input PVs */
    protected List<ChannelRef> getInputPVs(int mpsType) {
        _selectedMPSType = mpsType;
        final List<ChannelRef> channels = new ArrayList<ChannelRef>();
        List<Map<String, Object>> pvs = INPUT_PVS_CACHE[mpsType].getValue();
        if(pvs.size() > 0) {
            for(Iterator<Map<String, Object>> iter = pvs.iterator() ; iter.hasNext() ; ) {
                final Map<String, Object> channelMap = iter.next();
                String pv = (String)channelMap.get( MPSPortal.CHANNEL_PV_KEY );
                Boolean connected = (Boolean)channelMap.get( MPSPortal.CHANNEL_CONNECTED_KEY );
                channels.add( new ChannelRef(pv, connected) );	// make the channel reference
            }
            return channels;
        }
        else {
            return null;
        }
    }

}