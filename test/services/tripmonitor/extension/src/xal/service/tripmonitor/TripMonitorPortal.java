//
//  TripMonitorPortal.java
//
//  Created by Thomas Pelaia on 8/3/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import java.util.Date;
import java.util.List;
import java.util.HashMap;


/** interface to the trip monitor service */
public interface TripMonitorPortal {
	/** Date format for passing dates as strings */
	public final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'T'Z";
	
	/** key for the timestamp */
	public final static String TIMESTAMP_KEY = "TIMESTAMP";
	
	/** key for the PV */
	public final static String PV_KEY = "PV";
	
	/** channel info key for the connection status */
	public final static String CHANNEL_CONNECTION_KEY = "CHANNEL_CONNECTION";
	
	
	/**
	 * Shutdown the process.
	 * @param code  The shutdown code which is normally just 0.
	 * @return      0 Services must always return something
	 */
	public int shutdown( int code );
	
	
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
	
	
	/**
	 * Get the trip monitor labels
	 * @return the list of trip monitor labels
	 */
	public List<String> getTripMonitorNames();
	
	
	/**
	 * Determine if the specified monitor is enabled
	 * @return true if the monitor is enabled and false if not
	 */
	public boolean isEnabled( final String monitorName );
	
	
	/**
	 * Get the list of all PVs we are attempting to monitor and log. The information is returned as a list of channel info tables (one entry for each PV). 
	 * The channel info table has the PV_KEY and CHANNEL_CONNECTION_KEY keys and provides the signal name and the connection status of a channel.
	 * @param monitorName name of the trip monitor for which to get the channel information
	 * @return  list of all PV info tables for the PVs we are attempting to monitor and log
	 */
	public List<HashMap<String, Object>> getChannelInfo( final String monitorName );
	
	
	/** 
	 * Get the number of trip records in the history buffer of the specified trip monitor
	 * @param monitorName name of the trip monitor
	 * @return the number of trips in the monitor's buffer
	 */
	public int getTripHistoryCount( final String monitorName );
	
	
	/** 
	 * Get the trip records in the history buffer of the specified trip monitor
	 * @param monitorName name of the trip monitor
	 * @return the trip records in the monitor's buffer
	 */
	public List<HashMap<String, Object>> getTripRecords( final String monitorName );
	
	
	/**
	 * Publish the trips
	 * @return 1
	 */
	public int publishTrips();
}
