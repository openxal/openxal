/*
 * LoggerService.java
 *
 * Created on Thu Jan 15 11:22:50 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger;

import xal.extension.service.ServiceDirectory;
import xal.ca.Channel;

import java.util.*;


/**
 * LoggerService is the implementation of LoggerPortal that responds to
 * requests from remote clients on behalf of the logger model.
 *
 * @author  tap
 */
public class LoggerService implements RemoteLogging {
	// constants
	protected final String IDENTITY = "PV Logger";
	
	// model
	protected final LoggerModel _model;
	
	
	/**
	 * LoggerService constructor
	 */
	public LoggerService( final LoggerModel model ) {
		_model = model;
		broadcast();
	}
	
	
	/**
	 * Begin broadcasting the service
	 */
	public void broadcast() {
		ServiceDirectory.defaultDirectory().registerService( RemoteLogging.class, IDENTITY, this );
		System.out.println( "broadcasting..." );
	}
	
	
	/**
	 * Set the period between events where we take and store machine snapshots.
	 * @param groupType identifies the group by type 
	 * @param period The period in seconds between events where we take and store machine snapshots.
	 */
	public void setLoggingPeriod(String groupType, double period) {
		final LoggerSession session = _model.getLoggerSession( groupType );
		if ( session != null ) {
			session.setLoggingPeriod( period );
		}
	}
	
	
	/**
	 * Get the logging period.
	 * @param groupType identifies the group by type 
	 * @return The period in seconds between events where we take and store machine snapshots.
	 */
	public double getLoggingPeriod(String groupType) {
		final LoggerSession session = _model.getLoggerSession( groupType );
		if ( session != null ) {
			return session.getLoggingPeriod();
		}
		else {
			return 0;
		}
	}
	
	
	/**
	 * Take a snapshot and publish it.
	 * @param groupID ID of the group for which to take the snapshot
	 * @param comment snapshot comment
	 * @return machine snapshot ID or an error code (less than 0) if the attempt fails
	 */
	public int takeAndPublishSnapshot( final String groupID, final String comment ) {
		try {
			final LoggerSession loggerSession = _model.getPVLogger().getLoggerSession( groupID );
			return loggerSession != null ? (int)loggerSession.takeAndPublishSnapshot( comment ).getId() : -1;
		}
		catch( Exception exception ) {
			return -2;
		}
	}
	
	
	/** publish snapshots in the snapshot buffer */
	public void publishSnapshots() {
		_model.publishSnapshots();
	}

	
	/**
	 * Determine if a logger session exists for the specified group
	 * @param groupID group ID of the logger session for which to look
	 * @return true if a session exists for the group and false if not
	 */
	public boolean hasLoggerSession( final String groupID ) {
		return _model.getPVLogger().hasLoggerSession( groupID );
	}
	
	
	/**
	 * Determine if the logger is presently logging
	 * @param groupType identifies the group by type 
	 * @return true if the logger is logging and false if not
	 */
	public boolean isLogging( final String groupType ) {
		final LoggerSession session = _model.getLoggerSession( groupType );
		if ( session != null ) {
			return session.isLogging();
		}
		else {
			return false;
		}
	}
	
	
	/** reload the logger session identified by the group type */
	public boolean reloadLoggerSession( final String groupType ) {
		return _model.reloadLoggerSession( groupType );
	}
	
	
	/** Stop logging, reload groups from the database and resume logging. */
	public void restartLogger() {
		_model.restartLogger();
	}
	
	
	/** Resume the logger logging. */
	public void resumeLogging() {
		_model.resumeLogging();
	}
	
	
	/** Stop the logger. */
	public void stopLogging() {
		_model.stopLogging();
	}
	
	
	/**
	 * Shutdown the process.
	 * @param code The shutdown code which is normally just 0.
	 */
	public void shutdown( final int code ) {
		_model.shutdown(code);
	}
	
	
	/**
	 * Get the name of the host where the application is running.
	 * @return The name of the host where the application is running.
	 */
	public String getHostName() {
		try {
			return java.net.InetAddress.getLocalHost().getHostName();
		}
		catch(java.net.UnknownHostException exception) {
			return "";
		}
	}
	
	
	/**
	 * Get the launch time of the service.
	 * @return the launch time in seconds since the Java epoch of January 1, 1970.
	 */
	public Date getLaunchTime() {
		return LoggerModel.getLaunchTime();
	}


	/**
	 * Get a heartbeat from the service.
	 * @return the time measured from the service at which the heartbeat was sent
	 */
	public Date getHeartbeat() {
		return new Date();
	}

	
	/**
	 * Get the timestamp of the last channel event (e.g. channel connected/disconnected event)
	 * @param groupType identifies the group by type 
	 * @return the wall clock timestamp of the last channel event
	 */
	public Date getLastChannelEventTime( final String groupType ) {
		final LoggerSession session = _model.getLoggerSession( groupType );
		if ( session != null ) {
			return session.getChannelGroup().getLastChannelEventTime();
		}
		else {
			return new Date(0);
		}
	}
	
	
	/**
	 * Get the timestamp of the last logger event
	 * @param groupType identifies the group by type 
	 * @return the wall clock timestamp of the last logger event
	 */
	public Date getLastLoggerEventTime( final String groupType ) {
		return _model.getSessionModel(groupType).getLastLoggerEventTime();
	}
	
	
	/**
	 * Get the list of group types
	 * @return a list of the group types
	 */
	public List<String> getGroupTypes() {
		return new ArrayList<String>( _model.getSessionTypes() );
	}
	
	
	/**
	 * Get the number of channels we wish to log.
	 * @param groupType identifies the group by type 
	 * @return the number of channels we wish to log
	 */
	public int getChannelCount( final String groupType ) {
		return _model.getLoggerSession(groupType).getChannelGroup().getChannelCount();
	}
	
	
	/**
	 * Get the list of channel info tables.  Each channel info table contains
	 * the PV signal name and the channel connection status.
	 * @param groupType identifies the group by type 
	 * @return The list channel info tables corresponding to the channels we wish to log
	 */
	public List<Map<String,Object>> getChannels( final String groupType ) {
		final LoggerSession session = _model.getLoggerSession( groupType );
		if ( session != null ) {
			final Collection<Channel> channels = session.getChannels();
			final List<Map<String,Object>> channelInfoList = new ArrayList<Map<String,Object>>( channels.size() );
			for ( final Channel channel : channels ) {
				final Map<String,Object> info = new HashMap<String,Object>();
				info.put( CHANNEL_SIGNAL, channel.channelName() );
				info.put( CHANNEL_CONNECTED, new Boolean( channel.isConnected() ) );
				channelInfoList.add( info );
			}
			
			return channelInfoList;
		}
		else {
			return new ArrayList<Map<String,Object>>();
		}
	}
	
	
	/**
	 * Get the timestamp of the last published snapshot
	 * @param groupType identifies the group by type 
	 * @return the timestamp of the last published snapshot
	 */
	public Date getTimestampOfLastPublishedSnapshot(String groupType) {
		MachineSnapshot snapshot = _model.getSessionModel(groupType).getLastPublishedSnapshot();
		return (snapshot != null) ? snapshot.getTimestamp() : new Date(0);
	}
	
	
	/**
	 * Get the textual dump of the last published snapshot
	 * @param groupType identifies the group by type 
	 * @return the textual dump of the last published snapshot or null if none exists
	 */
	public String getLastPublishedSnapshotDump(String groupType) {
		Object snapshot = _model.getSessionModel(groupType).getLastPublishedSnapshot();
		return (snapshot != null) ? snapshot.toString() : "";
	}
}

