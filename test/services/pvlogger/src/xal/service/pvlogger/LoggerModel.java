/*
 * LoggerModel.java
 *
 * Created on Wed Jan 14 14:44:39 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger;

import xal.tools.database.*;
import xal.extension.service.ServiceDirectory;

import java.util.*;


/**
 * LoggerModel is the main model for the pvlogger service.  It manages the logger sessions.
 * @author  tap
 */
public class LoggerModel {
	/** The time when this process was launched in seconds since the Java epoch */
	static final private Date LAUNCH_TIME;
	
	/** PV Logger */
	final private PVLogger PV_LOGGER;
	
	/** session models keyed by group ID */
	final private Map<String,SessionModel> SESSION_MODELS;
	
	/** ID of the service to log */
	final private String SERVICE_ID;

	
	/**
	 * static initialization
	 */
	static {
		LAUNCH_TIME = new Date();
	}
	
	
	/**
	 * LoggerModel constructor
	 */
	public LoggerModel() {
		SESSION_MODELS = new HashMap<String,SessionModel>();
		SERVICE_ID = System.getProperty( "serviceID", "PHYSICS" );
		
		PV_LOGGER = new PVLogger();
		
		reloadGroups();
	}
	
	
	/** Start logging */
	public void startLogging() {
		PV_LOGGER.start();
	}
	
	
	/** Restart the logger. Stop logging, reload groups from the database and resume logging. */
	public void restartLogger() {
		stopLogging();
		reloadGroups();
		startLogging();
	}
	
	
	/** Reload the channel groups from the persistent store */
	public void reloadGroups() {
		SESSION_MODELS.clear();
		
		PV_LOGGER.removeAllLoggerSessions();
		
		try {
			final List<LoggerSession> loggerSessions = PV_LOGGER.requestEnabledLoggerSessionsForService( SERVICE_ID );
			for ( final LoggerSession session : loggerSessions ) {
				final String groupType = session.getChannelGroup().getLabel();
				SESSION_MODELS.put( groupType, new SessionModel( session ) );
			}
		}
		catch( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/** reload the group identified by the group ID */
	public boolean reloadLoggerSession( final String groupID ) {
		try {
			PV_LOGGER.reloadLoggerSession( groupID );
			return true;
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * Fetch the channel group types from the state store
	 * @return an array of channel group types in the state store
	 */
	protected String[] fetchChannelGroupTypes() {
		try {
			return PV_LOGGER.fetchTypes( SERVICE_ID );
		}
		catch( Exception exception ) {
			return new String[0];
		}
	}
	
	
	/**
	 * Get the session model identified by the specified group type
	 * @param groupType the identifier of the group
	 * @return the session model for the specified type or null if there is no match
	 */
	public SessionModel getSessionModel( final String groupType ) {
		return SESSION_MODELS.get( groupType );
	}
	
	
	/**
	 * Get the logger session identified by the specified group type
	 * @param groupType the identifier of the group
	 * @return the logger session for the specified type or null if there is no match
	 */
	public LoggerSession getLoggerSession( final String groupType ) {
		SessionModel sessionModel = getSessionModel( groupType );
		return sessionModel != null ? sessionModel.getLoggerSession() : null;
	}
	
	
	/**
	 * Get the PV Logger
	 * @return the PV Logger
	 */
	public PVLogger getPVLogger() {
		return PV_LOGGER;
	}
	
	
	/**
	 * Get the list of session types
	 * @return the list of session types
	 */
	public Collection<String> getSessionTypes() {
		return SESSION_MODELS.keySet();
	}
	
	
	/** publish snapshots in the buffer */
	public void publishSnapshots() {
		PV_LOGGER.publishSnapshots();
	}
	
	
	/**
	 * Resume logging.
	 */
	public void resumeLogging() {
		PV_LOGGER.restart();
	}
	
	
	/** Stop logging */
	public void stopLogging() {
		PV_LOGGER.stop();
	}
	
	
	/**
	 * Shutdown the service
	 * @param code The shutdown code which is normally just 0.
	 */
	public void shutdown( final int code ) {
		System.exit( code );
	}
	
	
	/**
	 * Get the launch time of the service.
	 * @return the launch time in seconds since the Java epoch of January 1, 1970.
	 */
	static public Date getLaunchTime() {
		return LAUNCH_TIME;
	}
}

