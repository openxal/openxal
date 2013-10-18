//
//  RemoteLoggingCenter.java
//  xal
//
//  Created by Tom Pelaia on 4/13/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.service.pvlogger;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import xal.extension.service.ServiceDirectory;
import xal.extension.service.ServiceListener;
import xal.extension.service.ServiceRef;


/** Center for communicating with a remote PV logger */
public class RemoteLoggingCenter {
	/** list of remote services */
	final private Map<String,RemoteLogging> REMOTE_SERVICES_MAP;
	
	
	/** Constructor */
	public RemoteLoggingCenter() {
		REMOTE_SERVICES_MAP = new Hashtable<String,RemoteLogging>();
		monitorLoggers();
	}
	
	
	/**
	 * take and publish a snapshot of the specified group
	 * @param groupID ID of the group to log
	 * @param comment to apply to the machine snapshot
	 * @return machine snapshot ID or an error code less than 0 if an error occurs
	 */
	public long takeAndPublishSnapshot( final String groupID, final String comment ) {
		try {
			final RemoteLogging service = findLogger( groupID, 5 );
			return service != null ? service.takeAndPublishSnapshot( groupID, comment ) : -1;
		}
		catch( Exception exception ) {
			return -2;
		}
	}
	
	
	/** 
	 * Determine whether a logger is available for the specified group.
	 * @param groupID group for which we are requesting logging
	 * @param timeout time (in seconds) to wait for a response
	 */
	public boolean hasLogger( final String groupID, final int timeout ) {
		final RemoteLogging logger = findLogger( groupID, timeout );
		return logger != null;
	}
	
	
	/** 
	 * Determine whether a logger is immediately available for the specified group.
	 * @param groupID group for which we are requesting logging
	 */
	public boolean hasLogger( final String groupID ) {
		return hasLogger( groupID, 0 );
	}
	
	
	/**
	 * get remote services as a collection
	 * @return collection of remote services
	 */
	private Collection<RemoteLogging> getRemoteServices() {
		synchronized ( REMOTE_SERVICES_MAP ) {
			return REMOTE_SERVICES_MAP.values();
		}
	}
	
	
	/**
	 * Find the first logger which is accessible and can log the specified group
	 * @param groupID ID of the group for which to locate a logger
	 * @param maxAttempts maximum number of attempts to make
	 */
	private RemoteLogging findLogger( final String groupID, final int maxAttempts ) {
		final Collection<RemoteLogging> remoteServices = getRemoteServices();
		for ( final RemoteLogging service : remoteServices ) {
			try {
				if ( service.hasLoggerSession( groupID ) ) {
					return service;
				}
			}
			catch ( Exception exception ) {}
		}
		
		if ( maxAttempts > 0 ) {
			try {
				Thread.sleep( 1000 );
				return findLogger( groupID, maxAttempts - 1 );
			}
			catch( Exception exception ) { return null; }
		}
		else {
			return null;
		}
	}
	
	
	/** get an instance of a service listener for the remote logging */
	private ServiceListener getServiceListenerInstance() {
		return new ServiceListener() {
			/**
			 * Handle a new service being added
			 * @param directory The service directory.
			 * @param serviceRef A reference to the new service.
			 */
			public void serviceAdded( final ServiceDirectory directory, final ServiceRef serviceRef ) {
				final RemoteLogging proxy = directory.getProxy( RemoteLogging.class, serviceRef );
				final String name = serviceRef.getRawName();
				synchronized( REMOTE_SERVICES_MAP ) {
					REMOTE_SERVICES_MAP.put( name, proxy );
				}
			}
			
			/**
			 * Handle a service being removed
			 * @param directory The service directory.
			 * @param name The unique name of the service.
			 */
			public void serviceRemoved( final ServiceDirectory directory, final String type, final String name ) {
				synchronized( REMOTE_SERVICES_MAP ) {
					REMOTE_SERVICES_MAP.remove( name );
				}
			}
		};
	}
	
	
	/** Monitor the addition and removal of XAL applications. */
	private void monitorLoggers() {
		ServiceDirectory.defaultDirectory().addServiceListener( RemoteLogging.class, getServiceListenerInstance() );
	}
}
