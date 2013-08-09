/*
 *  MPSService.java
 *
 *  Created on Thu Feb 17 14:55:31 EST 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.service.mpstool;

import xal.tools.services.*;

import java.util.*;
import java.text.*;


/**
 * MPSService implements the portal interface for remote clients
 * @author    tap
 */
public class MPSService implements MPSPortal {
	/** identifies the service type */
	protected final String IDENTITY = "MPS Monitor";

	/** The MPS model */
	protected final MPSModel _model;

	/** Formatter for translating a date to and from a string */
	protected final static DateFormat DATE_FORMATTER;

	/*
	 *  static initializer
	 */
	static {
		DATE_FORMATTER = new SimpleDateFormat( MPSPortal.DATE_FORMAT );
		System.out.println( "PID:  " + System.getProperty( "pid", "unknown" ) );
	}


	/**
	 * LoggerService constructor
	 *
	 * @param model  The MPS model
	 */
	public MPSService( final MPSModel model ) {
		_model = model;
		broadcast();
	}


	/** Begin broadcasting the service  */
	public void broadcast() {
		ServiceDirectory.defaultDirectory().registerService( MPSPortal.class, IDENTITY, this );
		System.out.println( "broadcasting..." );
	}
	
	
	/** 
	 * Get the process ID of the process in which the service runs or 0 if it is unknown.  
	 * When the java process is launched, the calling script needs to pass the process ID as a java property (exec java -Dpid=$$ -jar mpsservice.jar) for this to work.
	 */
	public int getProcessID() {
		return Integer.getInteger( "pid", 0 );
	}
	

	/**
	 * Shutdown the process.
	 *
	 * @param code  The shutdown code which is normally just 0.
	 */
	public void shutdown( int code ) {
		_model.shutdown( code );
	}


	/**
	 * Get the name of the host where the application is running.
	 *
	 * @return   The name of the host where the application is running.
	 */
	public String getHostName() {
		try {
			return java.net.InetAddress.getLocalHost().getHostName();
		}
		catch ( java.net.UnknownHostException exception ) {
			return "";
		}
	}


	/**
	 * Get the launch time of the service.
	 *
	 * @return   the launch time of the application.
	 */
	public Date getLaunchTime() {
		return Main.getLaunchTime();
	}
	
	
	/** determine whether the monitors log statistics */
	public boolean logsStatistics() {
		return _model.logsStatistics();
	}


	/**
	 * Get the list of MPS latch types. Monitors are listed as an array and their
	 * index in this array can be used in several methods to reference a specific
	 * monitor.
	 *
	 * @return   the list of MPS latch types as strings (e.g. "FPL", "FPAR")
	 */
	public Vector<String> getMPSTypes() {
		MPSMonitor[] monitors = _model.getMonitors();
		final Vector<String> types = new Vector<>( monitors.length );
		for ( int index = 0; index < monitors.length; index++ ) {
			types.add( monitors[index].getMPSType() );
		}
		return types;
	}


	/**
	 * Determing if the correlator is running.
	 *
	 * @param monitorIndex  index of the monitor to test if its correlator is
	 *      running
	 * @return              true if the correlator is running and false otherwise.
	 */
	public boolean isRunning( final int monitorIndex ) {
		return _model.getMonitor( monitorIndex ).isRunning();
	}


	/**
	 * Stop looking for MPS trips
	 *
	 * @param monitorIndex  index of the monitor that should stop its correlator
	 */
	public void stopCorrelator( final int monitorIndex ) {
		_model.getMonitor( monitorIndex ).stopCorrelator();
	}


	/**
	 * Restart the poster after a pause
	 *
	 * @param monitorIndex  index of the monitor that should restart its correlator
	 */
	public void restartCorrelator( final int monitorIndex ) {
		_model.getMonitor( monitorIndex ).restartCorrelator();
	}


	/**
	 * Get the table of event timestamps. The table keys indicate the type of event
	 * (e.g. MPS-CHANNEL-EVENT, INPUT-CHANNEL-EVENT or MPS-EVENT). 
	 * The values are Date objects that indicate
	 * the time the last event of the specified type had happened. This method may
	 * be used by clients to determine if their information is current.
	 * Since XML-RPC does not preserve sub-second accuracy of dates, we must pass
	 * dates as a string.
	 *
	 * @param monitorIndex  index of the monitor whose timestamps are requested
	 * @return              a table of latest event timestamps keyed by event type
	 */
	public HashMap<String, String> getLastEventTimes( int monitorIndex ) {
		MPSMonitor monitor = _model.getMonitor( monitorIndex );
		HashMap<String, String> eventTimes = new HashMap<>();
		eventTimes.put( MPS_CHANNEL_EVENT, asString( monitor.getLastMPSChannelEventTime() ) );
		eventTimes.put( INPUT_CHANNEL_EVENT, asString( monitor.getLastInputChannelEventTime() ) );
		eventTimes.put( MPS_EVENT, asString( monitor.getLastMPSEventTime() ) );

		return eventTimes;
	}


	/**
	 * Get the list of all MPS PVs we are attempting to monitor and log. The
	 * information is returned as a list of channel info tables (one entry for each
	 * PV). The channel info table has the CHANNEL_PV_KEY and CHANNEL_CONNECTED_KEY
	 * keys and provides the signal name and the connection status of a channel.
	 *
	 * @param monitorIndex  index of the monitor whose channel info is requested
	 * @return              The list of all PV info tables for the MPS PVs we are
	 *      attempting to monitor and log
	 */
	public List<HashMap<String, Object>> getMPSChannelInfo( int monitorIndex ) {
		final ChannelWrapper[] wrappers = _model.getMonitor( monitorIndex ).getMPSChannelWrappers();
		final List<HashMap<String, Object>> channelInfo = new ArrayList<>( wrappers.length );

		for ( int index = 0; index < wrappers.length; index++ ) {
			final ChannelWrapper wrapper = wrappers[index];
			final HashMap<String, Object> info = new HashMap<>();
			info.put( CHANNEL_PV_KEY, wrapper.getPV() );
			info.put( CHANNEL_CONNECTED_KEY, new Boolean( wrapper.isConnected() ) );
			channelInfo.add( info );
		}

		return channelInfo;
	}


	/**
	 * Get the list of all Input PVs we are attempting to monitor and log. The
	 * information is returned as a list of channel info tables (one entry for each
	 * PV). The channel info table has the CHANNEL_PV_KEY and CHANNEL_CONNECTED_KEY
	 * keys and provides the signal name and the connection status of a channel.
	 *
	 * @param monitorIndex  index of the monitor whose channel info is requested
	 * @return              The list of all PV info tables for the Input PVs we are
	 *      attempting to monitor and log
	 */
	public List<HashMap<String, Object>> getInputChannelInfo( int monitorIndex ) {
		final Collection<InputMonitor> inputs = new HashSet<>( _model.getMonitor( monitorIndex ).getInputMonitors() );
		final List<HashMap<String, Object>> channelInfo = new ArrayList<>( inputs.size() );

		final Iterator<InputMonitor> inputIter = inputs.iterator();
		while( inputIter.hasNext() ) {
			final InputMonitor input = inputIter.next();
			final HashMap<String, Object> info = new HashMap<>();
			info.put( CHANNEL_PV_KEY, input.getSignal() );
			info.put( CHANNEL_CONNECTED_KEY, new Boolean( input.isConnected() ) );
			channelInfo.add( info );
		}

		return channelInfo;
	}


	/**
	 * Reload the MPS signals from the signal data source for the specified
	 * monitor.
	 *
	 * @param monitorIndex  index of the monitor that should reload its signals
	 */
	public void reloadSignals( int monitorIndex ) {
		_model.getMonitor( monitorIndex ).reloadSignals();
	}


	/**
	 * Get the summary of first hit statistics
	 *
	 * @param monitorIndex  index of the monitor whose statistics are to be fetched
	 * @return              the first hit statistics summary
	 */
	public String getFirstHitText( int monitorIndex ) {
		return _model.getMonitor( monitorIndex ).getFirstHitText();
	}


	/**
	 * Does nothing at this time.
	 *
	 * @param monitorIndex  Description of the Parameter
	 * @return              null
	 */
	public Hashtable<?,?> getFirstHitInfo( int monitorIndex ) {
		return null;
	}


	/**
	 * Get the summary of MPS trips.
	 *
	 * @param monitorIndex  index of the monitor whose statistics are to be fetched
	 * @return              summary of MPS trips
	 */
	public String getMPSTripSummary( int monitorIndex ) {
		return _model.getMonitor( monitorIndex ).getMPSTripSummary();
	}


	/**
	 * Get the list of MPS events since the specified time.  Unfortunately, XML-RPC does
	 * not preserve the millisecond accuracy of time, so we must pass a string representation.
	 *
	 * @param monitorIndex  index of the monitor whose statistics are to be fetched
	 * @param timeStr       String representation of the reference time 
	 * @return              the latest processed MPS events since the specified
	 *      time
	 */
	public List<HashMap<String, Object>> getMPSEventsSince( final int monitorIndex, final String timeStr ) {
		final Date time = asDate(timeStr);
		final List<MPSEvent> events = _model.getMonitor( monitorIndex ).getMPSEventsSince( time );

		return processMPSEvents( events );
	}


	/**
	 * Get the list of latest MPS events.
	 *
	 * @param monitorIndex  index of the monitor whose statistics are to be fetched
	 * @return              the latest list of processed MPS events
	 */
	public List<HashMap<String, Object>> getLatestMPSEvents( final int monitorIndex ) {
		final List<MPSEvent> events = _model.getMonitor( monitorIndex ).getMPSEventBuffer();

		return processMPSEvents( events );
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
		synchronized(DATE_FORMATTER) {      // date format access must be synchronized
			return DATE_FORMATTER.format( date );
		}
	}


	/**
	 * Get the list of processed MPS events packaged for delivery
	 *
	 * @param mpsEvents  The input list of MPS events
	 * @return           the processed MPS events
	 */
	private List<HashMap<String, Object>> processMPSEvents( final List<MPSEvent> mpsEvents ) {
		List<HashMap<String, Object>> eventList = new ArrayList<>( mpsEvents.size() );

		for ( Iterator<MPSEvent> iter = mpsEvents.iterator(); iter.hasNext();  ) {
			MPSEvent event = iter.next();
			List<HashMap<String, Object>> signalEvents = packageMPSEvent( event );
			HashMap<String, Object> info = new HashMap<String, Object>();
			info.put( TIMESTAMP_KEY, asString(event.getTimestamp()) );
			info.put( SIGNAL_EVENTS_KEY, signalEvents );
			eventList.add( info );
		}

		return eventList;
	}


	/**
	 * Package an MPSEvent into a vector of signal event maps
	 *
	 * @param mpsEvent  The MPSEvent to package
	 * @return          a vector of signal event maps
	 */
	private List<HashMap<String, Object>> packageMPSEvent( MPSEvent mpsEvent ) {
		List<SignalEvent> events = mpsEvent.getSignalEvents();
		List<HashMap<String, Object>> eventList = new ArrayList<>( events.size() );

		for ( Iterator<SignalEvent> iter = events.iterator(); iter.hasNext();  ) {
			SignalEvent event = iter.next();
			HashMap<String, Object> info = new HashMap<>();
			info.put( CHANNEL_PV_KEY, event._signal );
			info.put( TIMESTAMP_KEY, event._timestamp.getFullSeconds().toString() );
			eventList.add( info );
		}

		return eventList;
	}
}

