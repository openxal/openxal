//
//  RemoteLogging.java
//  xal
//
//  Created by Tom Pelaia on 4/13/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.service.pvlogger;

import java.util.*;
import xal.extension.service.OneWay;


/** Interface for communicating with a remote PV logger */
public interface RemoteLogging {
	final static public String CHANNEL_SIGNAL = "SIGNAL";
	final static public String CHANNEL_CONNECTED = "CONNECTED";
	
	
	/**
	 * Set the period between events where we take and store machine snapshots for the specified group.
	 * @param groupType identifies the group by type 
	 * @param period The period in seconds between events where we take and store machine snapshots.
	 */
	public void setLoggingPeriod( String groupType, double period );
	
	
	/** publish snapshots in the snapshot buffer */
	public void publishSnapshots();
	
	
	/**
	 * Take a snapshot and publish it.
	 * @param groupID ID of the group for which to take the snapshot
	 * @param comment snapshot comment
	 * @return machine snapshot ID or an error code (less than 0) if the attempt fails
	 */
	public int takeAndPublishSnapshot( String groupID, String comment );
	
	
	/**
	 * Get the logging period.
	 * @param groupType identifies the group by type 
	 * @return The period in seconds between events where we take and store machine snapshots.
	 */
	public double getLoggingPeriod( String groupType );
	
	
	
	/**
	 * Determine if a logger session exists for the specified group
	 * @param groupID group ID of the logger session for which to look
	 * @return true if a session exists for the group and false if not
	 */
	public boolean hasLoggerSession( String groupID );
	
	
	/**
	 * Determine if the logger is presently logging
	 * @param groupType identifies the group by type 
	 * @return true if the logger is logging and false if not
	 */
	public boolean isLogging( String groupType );
	
	
	/** reload the logger session identified by the group type */
	public boolean reloadLoggerSession( final String groupType );
	
	
	/** Stop logging, reload groups from the database and resume logging. */
	public void restartLogger();

	
	/** Resume the logger logging. */
	public void resumeLogging();
	
	
	/** Stop the logger. */
	public void stopLogging();
	
	
	/**
	 * Shutdown the process without waiting for a response.
	 * @param code The shutdown code which is normally just 0.
	 */
	@OneWay
	public void shutdown( int code );
	
	
	/**
	 * Get the name of the host where the application is running.
	 * @return The name of the host where the application is running.
	 */
	public String getHostName();
	
	
	/**
	 * Get the launch time of the service.
	 * @return the launch time in seconds since the Java epoch of January 1, 1970.
	 */
	public Date getLaunchTime();


	/**
	 * Get a heartbeat from the service.
	 * @return the time measured from the service at which the heartbeat was sent
	 */
	public Date getHeartbeat();

	
	/**
	 * Get the timestamp of the last channel event (e.g. channel connected/disconnected event)
	 * @param groupType identifies the group by type 
	 * @return the wall clock timestamp of the last channel event
	 */
	public Date getLastChannelEventTime( String groupType );
	
	
	/**
	 * Get the timestamp of the last logger event
	 * @param groupType identifies the group by type 
	 * @return the wall clock timestamp of the last logger event
	 */
	public Date getLastLoggerEventTime( String groupType );
	
	
	/**
	 * Get the list of group types
	 * @return a list of the group types
	 */
	public List<String> getGroupTypes();
	
	
	/**
	 * Get the number of channels we wish to log.
	 * @param groupType identifies the group by type 
	 * @return the number of channels we wish to log
	 */
	public int getChannelCount( String groupType );
	
	
	/**
	 * Get the list of channel info tables.  Each channel info table contains
	 * the PV signal name and the channel connection status.
	 * @param groupType identifies the group by type 
	 * @return The list channel info tables corresponding to the channels we wish to log
	 */
	public List<Map<String,Object>> getChannels( final String groupType );

	
	/**
	 * Get the timestamp of the last published snapshot
	 * @param groupType identifies the group by type 
	 * @return the timestamp of the last published snapshot
	 */
	public Date getTimestampOfLastPublishedSnapshot( String groupType );
	
	
	/**
	 * Get the textual dump of the last published snapshot
	 * @param groupType identifies the group by type 
	 * @return the textual dump of the last published snapshot or null if none exists
	 */
	public String getLastPublishedSnapshotDump( String groupType );
}

