package xal.app.mpsclient;

import java.util.concurrent.Callable;
import java.util.*;

import xal.tools.messaging.MessageCenter;
import xal.service.mpstool.MPSPortal;
import xal.tools.UpdateListener;
import xal.tools.services.*;
import xal.tools.dispatch.DispatchQueue;
import xal.service.pvlogger.RemoteLogging;

public class RemoteMPSRecord implements UpdateListener {

    /* Service proxy */
    private final MPSPortal REMOTE_PROXY;
    
    /* Data caches */
    private final RemoteDataCache<Date> LAUNCH_TIME_CACHE;
    
    private final RemoteDataCache<Date> LAST_CHECK_TIME_CACHE;
    
    private final RemoteDataCache<String> HOST_CACHE;
    
    private final RemoteDataCache<Integer> PROCESS_ID_CACHE;
    
    private final RemoteDataCache<Boolean> LOGS_STATS_CACHE;
    
    private final RemoteDataCache<Boolean> SERVICE_OKAY_CACHE;
    
    private final RemoteDataCache<String[]> FIRST_HIT_TEXT_CACHE;
    
    private final RemoteDataCache<List<String>> MPS_TYPES_CACHE;
    
    private final RemoteDataCache<List<HashMap<String, Object>>>[] LATEST_MPS_EVENTS_CACHE;
    
    private final RemoteDataCache<List<HashMap<String, Object>>>[] MPS_PVS_CACHE;
    
    private final RemoteDataCache<List<HashMap<String, Object>>>[] INPUT_PVS_CACHE;
    
    private final RemoteDataCache<String>[] TRIP_SUMMARY_CACHE;
    
    private String[] _firstHitText;
    
    /* Remote address */
    private final String REMOTE_ADDRESS;
    
    /* If the service is connected */
    private Boolean serviceOkay = false;
    
    /* List of mps types */
    private final List<String> MPS_TYPES;
    
    private UpdateListener _updateListener;
    
    /* Used for instantiating caches in the constructor */
    private int mpsTypeIndex = 0;
    
    /* Number of mps types */
    final private int numTypes;
    
    /* Selected mps type index */
    private int selectedMPSType = 0;
    
    // messaging
	protected MessageCenter _messageCenter;
	protected RequestHandlerListener _proxy;

	
	@SuppressWarnings( {"rawtypes", "unchecked"} )		// Generics are incompatible with arrays
    public RemoteMPSRecord( final MPSPortal proxy ) {
        _messageCenter = new MessageCenter("MPS Record");

        REMOTE_PROXY = proxy;
        REMOTE_ADDRESS = ((ServiceState)proxy).getServiceHost();
        
        MPS_TYPES = REMOTE_PROXY.getMPSTypes();
        
        numTypes = MPS_TYPES.size();
        
        System.out.println("MPS TYPES=" + numTypes);
        
        _firstHitText = new String[numTypes];

        LATEST_MPS_EVENTS_CACHE = new RemoteDataCache[numTypes];
        TRIP_SUMMARY_CACHE = new RemoteDataCache[numTypes];
        MPS_PVS_CACHE = new RemoteDataCache[numTypes];
        INPUT_PVS_CACHE = new RemoteDataCache[numTypes];
        

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
        
        SERVICE_OKAY_CACHE = createRemoteOperationCache( new Callable<Boolean> () {
            public Boolean call() {
                return getServiceOkay();
            }
        });
        
        LAST_CHECK_TIME_CACHE = createRemoteOperationCache( new Callable<Date> () {
            public Date call() {
                return getLastCheckTime();
            }
        });
        
        FIRST_HIT_TEXT_CACHE = createRemoteOperationCache ( new Callable<String[]> () {
            public String[] call() {
                for(int type =0; type < numTypes; type++) {
                    _firstHitText[type] = REMOTE_PROXY.getFirstHitText(type);
                }
                return _firstHitText;
            }
        });
        
        MPS_TYPES_CACHE = createRemoteOperationCache ( new Callable<List<String>> () {
            public List<String> call() {
                return REMOTE_PROXY.getMPSTypes();
            }
        });

        
        for(mpsTypeIndex = 0; mpsTypeIndex < numTypes; mpsTypeIndex++) {
            System.out.println("Creating remote latest event cache (" + mpsTypeIndex + ").");
            
            LATEST_MPS_EVENTS_CACHE[mpsTypeIndex] = createRemoteOperationCache( new Callable<List<HashMap<String, Object>>>() {
                
                public List<HashMap<String, Object>> call() {
                    return REMOTE_PROXY.getLatestMPSEvents(selectedMPSType);
                }
                
            });
            
            LATEST_MPS_EVENTS_CACHE[mpsTypeIndex].setUpdateListener( this );
            
            TRIP_SUMMARY_CACHE[mpsTypeIndex] = createRemoteOperationCache(new Callable<String> () {
                public String call() {
                    return REMOTE_PROXY.getMPSTripSummary(selectedMPSType);
                }
            });
            
            TRIP_SUMMARY_CACHE[mpsTypeIndex].setUpdateListener( this );
            
            MPS_PVS_CACHE[mpsTypeIndex] = createRemoteOperationCache( new Callable<List<HashMap<String, Object>>>() {
               
                public List<HashMap<String, Object>> call() {
                    return REMOTE_PROXY.getMPSChannelInfo(selectedMPSType);
                }
                
            });
            
            MPS_PVS_CACHE[mpsTypeIndex].setUpdateListener( this );
            
            INPUT_PVS_CACHE[mpsTypeIndex] = createRemoteOperationCache( new Callable<List<HashMap<String, Object>>>() {
               
                public List<HashMap<String, Object>> call() {
                    return REMOTE_PROXY.getInputChannelInfo( selectedMPSType );
                }
                
            });
            
            INPUT_PVS_CACHE[mpsTypeIndex].setUpdateListener( this );
        }
        
        LAUNCH_TIME_CACHE.setUpdateListener( this );
        LAST_CHECK_TIME_CACHE.setUpdateListener( this );
        SERVICE_OKAY_CACHE.setUpdateListener( this );
        HOST_CACHE.setUpdateListener( this );
        PROCESS_ID_CACHE.setUpdateListener( this );
        LOGS_STATS_CACHE.setUpdateListener( this );
        FIRST_HIT_TEXT_CACHE.setUpdateListener( this );
        MPS_TYPES_CACHE.setUpdateListener( this );
    }
    
    /**
	 * Add the specified listener as a receiver of events from this request handler
	 * @param listener the listener to add as a receiver of events
	 */
	public void addRequestHandlerListener(RequestHandlerListener listener) {
		_messageCenter.registerTarget(listener, this, RequestHandlerListener.class);
    }

	
	/**
	 * Remove the specified listener from being a receiver of events from this request handler
	 * @param listener the listener to remove from receiving events from this request handler
	 */
	public void removeRequestHandlerListener(RequestHandlerListener listener) {
        _messageCenter.removeTarget(listener, this, RequestHandlerListener.class);
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
        selectedMPSType = mpsType;
        int mpsEvents = getLatestMPSEvents(mpsType).size();
        if(mpsEvents > 0) {
            Map<String, Object> eventsTable = getLatestMPSEvents(mpsType).get(0);
            MPSEvent event;
            
            Date eventTimeStamp = (Date)eventsTable.get(MPSPortal.TIMESTAMP_KEY);
            List<SignalEvent> signalEvents = (List<SignalEvent>)eventsTable.get(MPSPortal.SIGNAL_EVENTS_KEY);
            
            event = new MPSEvent(eventTimeStamp, signalEvents);
            
            return event;
        }
        else {
            return null;
        }
	}

    
	@SuppressWarnings( "unchecked" )	// have to cast
    public List<MPSEvent> processMPSEvents(int mpsType) {
        selectedMPSType = mpsType;
        List<MPSEvent> mpsEvents = new ArrayList<MPSEvent>();
        
        int mpsEventCount = getLatestMPSEvents(mpsType).size();
        
        for(int mpsTypeIndex = 0; mpsTypeIndex < mpsEventCount; mpsTypeIndex++) {
            Map<String, Object> eventsTable = getLatestMPSEvents(mpsType).get(mpsTypeIndex);
            MPSEvent event;
            
            Date eventTimeStamp = (Date)eventsTable.get(MPSPortal.TIMESTAMP_KEY);
            List<SignalEvent> signalEvents = (List<SignalEvent>)eventsTable.get(MPSPortal.SIGNAL_EVENTS_KEY);
            
           mpsEvents.add(new MPSEvent(eventTimeStamp, signalEvents));
        }

        return mpsEvents.size() > 0 ? mpsEvents : null;
        
    }
    
    public List<HashMap<String, Object>> getLatestMPSEvents(int mpsType) {
        selectedMPSType = mpsType;
        return REMOTE_PROXY.getLatestMPSEvents(mpsType);
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
            /**
             *  TODO: refresh chaches
             */  
//            for(int mpsTypeIndex = 0; mpsTypeIndex < numTypes; mpsTypeIndex++) {
//                TRIP_SUMMARY_CACHE[mpsTypeIndex].refresh();
//                LATEST_MPS_EVENTS_CACHE[mpsTypeIndex].refresh();
//                MPS_PVS_CACHE[mpsTypeIndex].refresh();
//                INPUT_PVS_CACHE[mpsTypeIndex].refresh();
//            }
            FIRST_HIT_TEXT_CACHE.refresh();
            SERVICE_OKAY_CACHE.refresh();
            LAST_CHECK_TIME_CACHE.refresh();
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
				final int numTypes = MPS_TYPES.size();
				for ( int type = 0 ; type < numTypes ; type++ ) {
					REMOTE_PROXY.reloadSignals(type);
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
    protected List<ChannelRef> getMPSPVs(int mpsType) {
        selectedMPSType = mpsType;
        final List<ChannelRef> channels = new ArrayList<ChannelRef>();
        //refresh();
        List<HashMap<String, Object>> pvs = MPS_PVS_CACHE[selectedMPSType].getValue();
        if(pvs == null) return null;
        if(pvs.size() > 0) {
        
            for(Iterator<HashMap<String, Object>> iter = pvs.iterator() ; iter.hasNext() ; ) {
                Map<String, Object> channelMap = (Map<String, Object>)iter.next();
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
    
    
    protected List<ChannelRef> getInputPVs(int mpsType) {
        selectedMPSType = mpsType;
        final List<ChannelRef> channels = new ArrayList<ChannelRef>();
        List<HashMap<String, Object>> pvs = INPUT_PVS_CACHE[mpsType].getValue();
        if(pvs.size() > 0) {
            for(Iterator<HashMap<String, Object>> iter = pvs.iterator() ; iter.hasNext() ; ) {
                Map<String, Object> channelMap = (Map<String, Object>)iter.next();
                String pv = (String)channelMap.get( MPSPortal.CHANNEL_PV_KEY );
                Boolean connected = (Boolean)channelMap.get( MPSPortal.CHANNEL_CONNECTED_KEY );
                //System.out.println("Input PV is connected=" + connected);
                channels.add( new ChannelRef(pv, connected) );	// make the channel reference
            }
            return channels;
        }
        else {
            return null;
        }
    }

}