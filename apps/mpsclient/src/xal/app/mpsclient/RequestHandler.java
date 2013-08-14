/*
 * RequestHandler.java
 *
 * Created on Tue Feb 17 16:30:56 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.mpsclient;

import xal.service.mpstool.MPSPortal;
import xal.tools.data.*;
//import xal.tools.Lock;
import xal.tools.dispatch.DispatchQueue;
import xal.tools.services.*;
import xal.tools.messaging.MessageCenter;

import java.util.*;
import java.util.logging.*;
import java.text.*;
import java.math.BigDecimal;
import java.util.concurrent.Callable;



/**
 * RequestHandler handles the requests for a single remote MPS service.
 * @author  tap
 */
//Cannot have generic arrays
@SuppressWarnings({ "unchecked", "rawtypes" })
class RequestHandler implements DataKeys {
	/** Formatter for translating a date to and from a string */
	protected final static DateFormat DATE_FORMATTER;
	
	/** ID for this request handler */
	protected String _id;
    
    /** DispatchQueue for synchronization */
    protected final DispatchQueue _queue;
    
	/** Proxy for messaging the remote service */
	protected MPSPortal _remoteProxy;
	
	
	// constant properties
	protected Date launchTime;
	protected String host = "?";
	protected List<String> _mpsTypes;
	protected int _processID;
	protected boolean _logsStatistics;
	
	// state
	protected volatile boolean hasInitialized;
	protected List[] _mpsPVs;
//    protected Vector<Vector<ChannelRef>> _mpsPVs;
	protected List[] _inputPVs;
	
	protected String[] _firstHitText;
	protected String[] _tripSummary;
	protected List[] _latestMPSEvents;
	
	protected Date[] _inputChannelEventTime;
	protected Date[] _mpsChannelEventTime;
	protected Date[] _mpsEventTime;
	protected Date _lastCheck;
	
	protected boolean _isRemoteStatusOkay;
	
	// messaging
	protected MessageCenter _messageCenter;
	protected RequestHandlerListener _proxy;
    
	
	
	// static initializer
	static {
		DATE_FORMATTER = new SimpleDateFormat( MPSPortal.DATE_FORMAT );
	}
	
	
	/**
	 * Constructor
	 * @param id The unique ID of the remote service managed by this request handler
	 * @param remoteProxy The proxy for sending messages to the remote service
	 */
	public RequestHandler( final String id, final MPSPortal remoteProxy ) {
		_messageCenter = new MessageCenter("Request Handler");
		_proxy = _messageCenter.registerSource(this, RequestHandlerListener.class);
		
        _queue = DispatchQueue.createSerialQueue( "Request Handler Queue" );

		_id = id;
		_remoteProxy = remoteProxy;
		hasInitialized = false;
		
		_mpsTypes = Collections.emptyList();
		_mpsPVs = new Vector[0];
		_inputPVs = new Vector[0];
		
		_firstHitText = new String[0];
		_tripSummary = new String[0];
		_latestMPSEvents = new List[0];
		
		_mpsChannelEventTime = new Date[0];
		_mpsEventTime = new Date[0];
		_isRemoteStatusOkay = true;
		
		_lastCheck = null;
	}
	
    protected void finalize() throws Throwable {
		try {
			_queue.dispose();
		}
		finally {
			super.finalize();
		}
	}
	
	/**
	 * Get the unique request handler ID
	 * @return the unique ID of the remote service managed by this request handler
	 */
	String getID() {
		return _id;
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
	
	
	/**
	 * Get the remote proxy managed by this handler
	 * @return the remote proxy
	 */
	public MPSPortal getProxy() {
		return _remoteProxy;
	}
		
	
	/**
	 * Fetch and store constant application information.
	 */
	protected void firstFetch() throws RemoteMessageException {
		host = _remoteProxy.getHostName();
		
		try {
			launchTime = _remoteProxy.getLaunchTime();
			_mpsTypes = _remoteProxy.getMPSTypes();
			_processID = _remoteProxy.getProcessID();
			_logsStatistics = _remoteProxy.logsStatistics();
			
			final int numTypes = _mpsTypes.size();
			
			_mpsPVs = new List[numTypes];
            //_mpsPVs = new Vector<Vector<ChannelRef>>(numTypes);
			_inputPVs = new List[numTypes];
			
			_firstHitText = new String[numTypes];
			_tripSummary = new String[numTypes];
			_latestMPSEvents = new List[numTypes];
			
			_inputChannelEventTime = new Date[numTypes];
			_mpsChannelEventTime = new Date[numTypes];
			_mpsEventTime = new Date[numTypes];
			
			for ( int type = 0 ; type < numTypes ; type++ ) {
				_inputChannelEventTime[type] = new Date(0);
				_mpsChannelEventTime[type] = new Date(0);
				_mpsEventTime[type] = new Date(0);
			}
		}
		catch(RemoteMessageException exception) {
			Logger.getLogger("global").log( Level.SEVERE, "Initial remote fetch exception", exception );
			System.err.println("Got an initial remote fetch exception...");
			System.err.println(exception);
			_isRemoteStatusOkay = false;
		}
		
		hasInitialized = true;
	}
	
	
	/**
	 * Update the record with the current information from the remote application.  Try to get a lock
	 * for updating data from the remote application.  If the lock is unsuccessful simply return false.
	 * Only fetch information if the service state has anything new.
	 * @param record 
	 * @return true if the record was successfully updated and false if not.
	 */
	public boolean update(final GenericRecord record) {
		//if ( _lock.tryLock() ) {
        return _queue.dispatchSync( new Callable<Boolean>() {
            public Boolean call() {
                //lock
                
                try {
                    if ( !_isRemoteStatusOkay )  return true;
                    
                    if ( !hasInitialized ) {
                        firstFetch();
                        if ( !hasInitialized )  return true;		// just return and dont' update anything else
                    }
                    
//                    record.setValueForKey( launchTime, LAUNCH_TIME_KEY );
//                    record.setValueForKey( host, HOST_KEY );
//                    record.setValueForKey( _processID, PROCESS_ID_KEY );
//                    record.setValueForKey( _logsStatistics, LOGS_STATS_KEY );
                    
                    final int numTypes = _mpsTypes.size();
                    
                    List[] mpsPVs = new List[numTypes];
                    List[] inputPVs = new List[numTypes];
                    for ( int type = 0 ; type < numTypes ; type++ ) {
                        Map<String, String> lastEventTimeTable = _remoteProxy.getLastEventTimes( type );
                        
                        // check to see if any MPS channel events occurred since the last update
                        Date serviceMPSChannelEventTime = asDate( lastEventTimeTable.get( MPSPortal.MPS_CHANNEL_EVENT ) );
                        if ( !serviceMPSChannelEventTime.equals( _mpsChannelEventTime[type] ) ) {
                            mpsPVs[type] = _remoteProxy.getMPSChannelInfo( type );
                            _mpsChannelEventTime[type] = serviceMPSChannelEventTime;
                            processMPSPVs( mpsPVs, type );
                        }
                        
                        // check to see if any input channel events occurred since the last update
                        Date serviceInputChannelEventTime = asDate( lastEventTimeTable.get( MPSPortal.INPUT_CHANNEL_EVENT ) );
                        if ( !serviceInputChannelEventTime.equals( _inputChannelEventTime[type] ) ) {
                            inputPVs[type] = _remoteProxy.getInputChannelInfo( type );
                            _inputChannelEventTime[type] = serviceInputChannelEventTime;
                            processInputPVs( inputPVs, type );
                        }
                        
                        // check to see if any MPS events occurred since the last update
                        Date serviceMPSEventTime = asDate( lastEventTimeTable.get( MPSPortal.MPS_EVENT ) );
                        if ( !serviceMPSEventTime.equals( _mpsEventTime[type] ) ) {
                            _firstHitText[type] = _remoteProxy.getFirstHitText(type);
                            _tripSummary[type] = _remoteProxy.getMPSTripSummary(type);
                            _mpsEventTime[type] = serviceMPSEventTime;
                            List<HashMap<String, Object>> latestEventsList;
                            if ( _latestMPSEvents[type] != null && _latestMPSEvents[type].size() > 0 ) {
                                //MPSEvent lastEvent = _latestMPSEvents.get(type).get(0);
                                //String lastEventTime = asString( lastEvent.getTimestamp() );
                                //latestEventsList = _remoteProxy.getMPSEventsSince(type, lastEventTime);
                            }
                            else {
                                latestEventsList = _remoteProxy.getLatestMPSEvents(type);
                            }
                            //processLatestEvents(latestEventsList, type);
                            //_proxy.mpsEventsUpdated(RemoteMPSRecord.this, type);
                        }
                    }
                    
                    Date lastCheck = new Date();
                    record.setValueForKey(lastCheck, LAST_CHECK_KEY);
                    setLastCheck(lastCheck);
                    return true;
                }
                catch(Exception exception) {
                    Logger.getLogger("global").log( Level.SEVERE,  "Update exception", exception );
                    System.err.println("Got an update exception...");
                    exception.printStackTrace();
                   // _isRemoteStatusOkay = false;
                    return true;
                }
                finally {
                    //_lock.unlock();
                    //record.setValueForKey(_id, ID_KEY);
                    //record.setValueForKey(_isRemoteStatusOkay, SERVICE_OKAY_KEY);
                }
                
            }
            //lock
		});
        //return false;
   }
	
	
	/**
	 * Process the list of raw MPS event tables into a list of MPS event instances
	 * @param rawEvents The list of new raw MPS events to process
	 * @param type The MPS latch type for the list of MPS events we wish to process
	 */
	protected void processLatestEvents(final List<HashMap<String, Object>> rawEvents, final int type) {
		List<MPSEvent> mpsEvents = new ArrayList<>( rawEvents.size() );
		
		for ( Iterator<HashMap<String, Object>> iter = rawEvents.iterator() ; iter.hasNext() ; ) {
			Map<String, Object> eventInfo = iter.next();
			mpsEvents.add( processMPSEvent(eventInfo) );
		}
		// append existing events at the end up to the buffer size
		int room = MPSPortal.MPS_EVENT_BUFFER_SIZE - mpsEvents.size();
		if ( _latestMPSEvents[type] != null && room > 0 ) {
			room = Math.min(room, _latestMPSEvents[type].size());
			mpsEvents.addAll( _latestMPSEvents[type].subList(0, room) );
		}
		
		_latestMPSEvents[type] = mpsEvents;
	}
	
	
	/**
	 * Process an MPS event table into an MPS event instance.  Each raw MPS event table
	 * contains an MPS timestamp and a list of raw signal event tables.  Each raw signal
	 * event table is processed into an instance of SignalEvent.
	 * @param mpsInfo A table of raw MPS event data
	 * @return an instance of MPSEvent corresponding to the MPS event data
	 */
	private MPSEvent processMPSEvent( final Map<String, Object> mpsInfo ) {
		final Date mpsTimestamp = asDate( (String)mpsInfo.get(MPSPortal.TIMESTAMP_KEY) );
		final List<HashMap<String, Object>> rawSignalEvents = (List<HashMap<String, Object>>)mpsInfo.get(MPSPortal.SIGNAL_EVENTS_KEY);
		final List<SignalEvent> signalEvents = new ArrayList<>( rawSignalEvents.size() );
		
		for ( Iterator<HashMap<String, Object>> iter = rawSignalEvents.iterator() ; iter.hasNext() ; ) {
			final Map<String, Object> eventInfo = iter.next();
			final String signal = (String)eventInfo.get(MPSPortal.CHANNEL_PV_KEY);
			final String timestamp = (String)eventInfo.get(MPSPortal.TIMESTAMP_KEY);
			signalEvents.add( new SignalEvent(signal, new BigDecimal(timestamp)) );
		}
		
		return new MPSEvent( mpsTimestamp, signalEvents );
	}
	
	
	/**
	 * Process the channels by converting each PV table into a convenient ChannelRef.
	 * @param pvs Array (indexed by MPS type) of lists of PV tables 
	 * @param type The MPS latch type for the list of PV tables we wish to process
	 */
	protected void processMPSPVs( final List<HashMap<String, Object>>[] pvs, final int type ) {
		final List<ChannelRef> channelList = new ArrayList<>();		// list of channel references to be made
		// iterate through the pv tables in the list corresponding to the correct MPS type
		for ( Iterator<HashMap<String, Object>> iter = pvs[type].iterator() ; iter.hasNext() ; ) {
			// get the PV table indicating the PV name and the connection status for a single channel
			Map<String, Object> channelMap = (Map<String, Object>)iter.next();
			String pv = (String)channelMap.get( MPSPortal.CHANNEL_PV_KEY );
			Boolean connected = (Boolean)channelMap.get( MPSPortal.CHANNEL_CONNECTED_KEY );
			channelList.add( new ChannelRef(pv, connected) );	// make the channel reference
		}
		_mpsPVs[type] = channelList;
//        _mpsPVs.get(type) = channelList;
		//_proxy.mpsChannelsUpdated( this, type, channelList );
	}
	
	
	/**
	 * Process the channels by converting each PV table into a convenient ChannelRef.
	 * @param pvs Array (indexed by MPS type) of lists of PV tables 
	 * @param type The MPS latch type for the list of PV tables we wish to process
	 */
	protected void processInputPVs( final List<HashMap<String, Object>>[] pvs, final int type ) {
		final List<ChannelRef> channels = new ArrayList<>();		// list of channel references to be made
		// iterate through the pv tables in the list corresponding to the correct MPS type
		for ( Iterator<HashMap<String, Object>> iter = pvs[type].iterator() ; iter.hasNext() ; ) {
			// get the PV table indicating the PV name and the connection status for a single channel
			Map<String, Object> channelMap = iter.next();
			String pv = (String)channelMap.get( MPSPortal.CHANNEL_PV_KEY );
			Boolean connected = (Boolean)channelMap.get( MPSPortal.CHANNEL_CONNECTED_KEY );
			channels.add( new ChannelRef(pv, connected) );	// make the channel reference
		}
		// sort the refs alphabetically by PV since the inputs are not ordered 
		Collections.sort( channels, ChannelRef.signalComparator() ); 
		_inputPVs[type] = channels;
		//_proxy.inputChannelsUpdated( this, type, channels );
	}
	
	
	/**
	 * Get the list of MPS latch types
	 * @return the list of MPS latch type names
	 */
	public List<String> getMPSTypes() {
		return _mpsTypes;
	}
	
	
	/**
	 * Get the list of all MPS PVs we are attempting to monitor and log
	 * @return The list of all MPS PVs we are attempting to monitor and log
	 */
	public List<ChannelRef> getMPSPVs( final int mpsType ) {
		return _mpsPVs[mpsType];
	}
	
	
	/**
	 * Get the list of all input PVs we are attempting to monitor and log
	 * @return The list of all input PVs we are attempting to monitor and log
	 */
	public List<ChannelRef> getInputPVs( final int mpsType ) {
		return _inputPVs[mpsType];
	}
	
	
	/**
	 * Get the time of the most recent MPS event
	 * @return the wall clock time from the MPS service process of the most recent MPS event
	 */
	public Date getLastMPSEventTime( final int mpsType ) {
		return _mpsEventTime[mpsType];
	}
	
	
	/**
	 * Get the most recent summary of first hit MPS events
	 * @param mpsType The index of the MPS latch type for which to get the summary
	 * @return the summary of first hit statistics 
	 */
	public String getFirstHitText(int mpsType) {
		return _firstHitText[mpsType];
	}
	
	
	/**
	 * Get the most recent summary of MPS trips.
	 * @param mpsType The index of the MPS latch type for which to get the summary
	 * @return the summary of MPS Trips
	 */
	public String getTripSummary(int mpsType) {
		return _tripSummary[mpsType];
	}
	
	
	/**
	 * Get a copy of the list of latest MPS events
	 * @param mpsType The index of the MPS latch type for which to get the event list
	 * @return the latest list of MPS events
	 */
	public List<MPSEvent> getLatestMPSEvents(int mpsType) {
		return new ArrayList<MPSEvent>(_latestMPSEvents[mpsType]);
	}
	
	
	/**
	 * Get the most recent MPS event
	 * @param mpsType The index of the MPS latch type for which to get the event list
	 * @return the latest MPS event or null if there is none
	 */
	public MPSEvent getLatestMPSEvent(int mpsType) {
		List<MPSEvent> latestEvents = _latestMPSEvents[mpsType];
		return (latestEvents.size() > 0) ? latestEvents.get(0) : null;
	}
	
	
	/**
	 * Get the timestamp of the last check for new information from the remote service
	 * @return the wall clock timestamp of the last check
	 */
	public Date getLastCheck() {
		return _lastCheck;
	}
	
	
	/**
	 * Set the timestamp of the last check for new information from the remote service
	 * @param date the wall clock timestamp of the last check
	 */
	protected void setLastCheck(Date date) {
		_lastCheck = date;
		//_proxy.lastCheck(this, date);
	}


	/**
	 * Generate a date represented by the specified string.
	 *
	 * @param dateStr  String representation of a date.
	 * @return         The date represented by the specified string.
	 */
	static protected Date asDate( String dateStr ) {
		try {
			synchronized(DATE_FORMATTER) {     // date format access must be synchronized
				return DATE_FORMATTER.parse( dateStr );
			}
		}
		catch(java.text.ParseException exception) {
			return null;
		}
	}


	/**
	 * Generate a string representation of a string.
	 *
	 * @param date  The date to represent with a string.
	 * @return      The string representation of the date.
	 */
	static protected String asString( Date date ) {
		synchronized(DATE_FORMATTER) {		// date format access must be synchronized
			return DATE_FORMATTER.format( date );
		}
	}
	
	
	/**
	 * Request the remote service to reload signals from the global database.
	 */
	public void reloadSignals() {
		if ( _remoteProxy != null ) {
			try {
				final int numTypes = _mpsTypes.size();
				for ( int type = 0 ; type < numTypes ; type++ ) {
					_remoteProxy.reloadSignals(type);
				}
			}
			catch(RemoteMessageException exception) {
				_isRemoteStatusOkay = false;
				throw new RuntimeException("Remote message exception while reloading signals.", exception);
			}
		}
	}
	
	
	/**
	 * Request to shutdown the service.
	 * @param exitCode the exit code for shutting down the service (typically 0 for normal exit)
	 */
	public void shutdownService(int exitCode) {
		if ( _remoteProxy != null ) {
			try {
				_remoteProxy.shutdown(exitCode);
			}
			catch(RemoteMessageException exception) {
				// we don't expect any response
			}
		}
	}
}

