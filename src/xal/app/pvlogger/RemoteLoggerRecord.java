//
// RemoteLoggerRecord.java
// Open XAL
//
// Created by Pelaia II, Tom on 9/28/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.app.pvlogger;

import java.util.concurrent.Callable;
import java.util.*;

import xal.tools.UpdateListener;
import xal.extension.service.*;
import xal.tools.dispatch.DispatchQueue;
import xal.service.pvlogger.RemoteLogging;


/** RemoteLoggerRecord */
public class RemoteLoggerRecord implements UpdateListener {
	/** remote proxy */
	private final RemoteLogging REMOTE_PROXY;

	/** cache for the host name */
	private final RemoteDataCache<String> HOST_NAME_CACHE;

	/** cache for the launch time */
	private final RemoteDataCache<Date> LAUNCH_TIME_CACHE;

	/** cache for the remote service heartbeat */
	private final RemoteDataCache<Date> HEARTBEAT_CACHE;

	/** host address of the remote service */
	private final String REMOTE_ADDRESS;

	/** list of group types */
	private final List<String> GROUP_TYPES;

	/** logger sessions keyed by group type */
	private final Map<String,LoggerSessionHandler> LOGGER_SESSIONS;

	/** optional handler of the update event */
	private UpdateListener _updateListener;

	
	/** Constructor */
    public RemoteLoggerRecord( final RemoteLogging proxy ) {
		REMOTE_PROXY = proxy;
		REMOTE_ADDRESS = ((ServiceState)proxy).getServiceHost();
		
		// don't need to keep making remote requests for host name as it won't change
		HOST_NAME_CACHE = createRemoteOperationCache( new Callable<String>() {
			public String call() {
				return REMOTE_PROXY.getHostName();
			}
		});

		// don't need to keep making remote requests for launch time as it won't change
		LAUNCH_TIME_CACHE = createRemoteOperationCache( new Callable<Date>() {
			public Date call() {
				return REMOTE_PROXY.getLaunchTime();
			}
		});

		// insulate this call from hangs of the remote application
		HEARTBEAT_CACHE = createRemoteOperationCache( new Callable<Date>() {
			public Date call() {
				return REMOTE_PROXY.getHeartbeat();
			}
		});

		GROUP_TYPES = REMOTE_PROXY.getGroupTypes();
		
		LOGGER_SESSIONS = new HashMap<String,LoggerSessionHandler>();
		for ( final String groupType : GROUP_TYPES ) {
			LOGGER_SESSIONS.put( groupType, new LoggerSessionHandler( groupType, REMOTE_PROXY ) );
		}

		// observe updates from the caches
		HOST_NAME_CACHE.setUpdateListener( this );
		LAUNCH_TIME_CACHE.setUpdateListener( this );
		HEARTBEAT_CACHE.setUpdateListener( this );
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
		HEARTBEAT_CACHE.refresh();
	}


	/**
	 * Get the group types that are being logged.  Each type has an associated PVLogger session.
	 * @return the list of pvlogger groups
	 */
	public List<String> getGroupTypes() {
		return GROUP_TYPES;
	}


	/**
	 * Get a logger session by group type
	 * @param groupType the type of group for which to get the logger session
	 * @return the named logger session
	 */
	public LoggerSessionHandler getLoggerSession( final String groupType ) {
		return LOGGER_SESSIONS.get( groupType );
	}


	/**
	 * Get the name of the host where the application is running.
	 * @return The name of the host where the application is running.
	 */
	public String getHostName() {
		final String hostName = HOST_NAME_CACHE.getValue();
		return hostName != null ? hostName : REMOTE_ADDRESS;	// if we can't get the host name from the remote service something went wrong and just return the remote address so we have some information
	}


	/**
	 * Get the launch time of the application
	 * @return the time at with the application was launched
	 */
	public Date getLaunchTime() {
		return LAUNCH_TIME_CACHE.getValue();
	}


	/**
	 * Get the heartbeat from the service
	 * @return the time at with the application was launched in seconds since the epoch
	 */
	public Date getHeartbeat() {
		return HEARTBEAT_CACHE.getValue();
	}


	/** Determine whether this record is believed to be connected but don't test */
	public boolean isConnected() {
		return HEARTBEAT_CACHE.isConnected();
	}


	/** Publish snapshots. */
	public void publishSnapshots() {
		REMOTE_PROXY.publishSnapshots();
	}


	/** Stop logging, reload groups from the database and resume logging. */
	public void restartLogger() {
		REMOTE_PROXY.restartLogger();
	}


	/** Resume the logger logging. */
	public void resumeLogging() {
		REMOTE_PROXY.resumeLogging();
	}


	/** Stop the logger. */
	public void stopLogging() {
		REMOTE_PROXY.stopLogging();
	}


	/**
	 * Shutdown the process without waiting for a response.
	 * @param code The shutdown code which is normally just 0.
	 */
	public void shutdown( int code ) {
		REMOTE_PROXY.shutdown( code );
	}
}
