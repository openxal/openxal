/*
 *  MPSPortal.java
 *
 *  Created on Thu Feb 17 13:07:32 EST 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.service.mpstool;

import xal.extension.service.OneWay;

import java.util.*;


/**
 * MPSPortal is the interface to remote clients
 * @author    tap
 */
public interface MPSPortal {
	/** Date format for passing dates as strings */
	public final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'T'Z";

	/** size of the MPS event buffer */
//    public final static int MPS_EVENT_BUFFER_SIZE = MPSMonitor.MPS_EVENT_BUFFER_SIZE;
    public final static int MPS_EVENT_BUFFER_SIZE = 1000;
    
	/** Description of the Field */
	public final static String CHANNEL_PV_KEY = "PV";
	
	/** Description of the Field */
	public final static String CHANNEL_CONNECTED_KEY = "CONNECTED";

	/** key used in the latest MPS event table */
	public final static String TIMESTAMP_KEY = "TIMESTAMP";

	/** key used in the event time table to indicate signal events */
	public final static String SIGNAL_EVENTS_KEY = "SIGNAL_EVENTS";

	/** key used in the event time table to indicate an MPS channel event */
	public final static int MPS_CHANNEL_EVENT_ID = 0;

	/** key used in the event time table to indicate an MPS channel event */
	public final static int INPUT_CHANNEL_EVENT_ID = MPS_CHANNEL_EVENT_ID + 1;

	/** key used in the event time table to indicate an MPS event */
	public final static int MPS_EVENT_ID = INPUT_CHANNEL_EVENT_ID + 1;

	/** count of the event IDs */
	public final static int EVENT_ID_COUNT = MPS_EVENT_ID + 1;
	
	
	/** Get the process ID of the process in which the service runs or 0 if it is unknown */
	public int getProcessID();


	/**
	 * Shutdown the process.
	 * @param code  The shutdown code which is normally just 0.
	 */
    @OneWay
	public void shutdown( int code );


	/**
	 * Get the name of the host where the application is running.
	 * @return   The name of the host where the application is running.
	 */
	public String getHostName();


	/**
	 * Get the launch time of the service.
	 * @return   the launch time in seconds since the Java epoch of January 1, 1970.
	 */
	public Date getLaunchTime();
	
	
	/** determine whether the monitors log statistics */
	public boolean logsStatistics();
	

	/**
	 * Get the list of MPS latch types. Monitors are listed as an array and their
	 * index in this array can be used in several methods to reference a specific monitor.
	 * @return   the list of MPS latch types as strings (e.g. "FPL", "FPAR")
	 */
	public List<String> getMPSTypes();


	/**
	 * Determing if the correlator is running.
	 * @param monitorIndex  index of the monitor to test if its correlator is running
	 * @return true if the correlator is running and false otherwise.
	 */
	public boolean isRunning( int monitorIndex );


	/**
	 * Stop looking for MPS trips
	 * @param monitorIndex  index of the monitor that should stop its correlator
	 */
	public void stopCorrelator( int monitorIndex );


	/**
	 * Restart the poster after a pause
	 * @param monitorIndex  index of the monitor that should restart its correlator
	 */
	public void restartCorrelator( int monitorIndex );


	/**
	 * Reload the MPS signals from the signal data source for the specified monitor.
	 * @param monitorIndex  index of the monitor that should reload its signals
	 */
	public void reloadSignals( int monitorIndex );


	/**
	 * Get an arry of timestamps corresponding to the different event ids.
	 * The values are Date objects that indicate the time the last event of the specified type had happened. This method may
	 * be used by clients to determine if their information is current.
	 * @param monitorIndex  index of the monitor whose timestamps are requested
	 * @return an array of timestamps
	 */
	public Date[] getLastEventTimes( int monitorIndex );


	/**
	 * Get the list of all MPS PVs we are attempting to monitor and log. The
	 * information is returned as a list of channel info tables (one entry for each
	 * PV). The channel info table has the CHANNEL_PV_KEY and CHANNEL_CONNECTED_KEY
	 * keys and provides the signal name and the connection status of a channel.
	 * @param monitorIndex  index of the monitor whose channel info is requested
	 * @return  The list of all MPS PV info tables for the PVs we are attempting to monitor and log
	 */
	public List<Map<String, Object>> getMPSChannelInfo( int monitorIndex );


	/**
	 * Get the list of all Input PVs we are attempting to monitor and log. The
	 * information is returned as a list of channel info tables (one entry for each
	 * PV). The channel info table has the CHANNEL_PV_KEY and CHANNEL_CONNECTED_KEY
	 * keys and provides the signal name and the connection status of a channel.
	 * @param monitorIndex  index of the monitor whose channel info is requested
	 * @return  The list of all PV info tables for the Input PVs we are attempting to monitor and log
	 */
	public List<Map<String, Object>> getInputChannelInfo( int monitorIndex );


	/**
	 * Get the summary of first hit statistics
	 * @param monitorIndex  index of the monitor whose statistics are to be fetched
	 * @return  the first hit statistics summary
	 */
	public String getFirstHitText( int monitorIndex );


	/**
	 * Get the summary of MPS trips.
	 * @param monitorIndex  index of the monitor whose statistics are to be fetched
	 * @return  summary of MPS trips
	 */
	public String getMPSTripSummary( int monitorIndex );


	/**
	 * Get the list of MPS events since the specified time
	 * @param monitorIndex  index of the monitor whose statistics are to be fetched
	 * @param timeStr  a string representation of the reference time
	 * @return  the latest processed MPS events since the specified time
	 */
	public List<Map<String, Object>> getMPSEventsSince( final int monitorIndex, final String timeStr );


	/**
	 * Get the list of latest MPS events
	 * @param monitorIndex  index of the monitor whose statistics are to be fetched
	 * @return  the latest ordered list of MPS events
	 */
	public List<Map<String, Object>> getLatestMPSEvents( int monitorIndex );
}

