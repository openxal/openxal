//
// MonitorModel.java
// Open XAL
//
// Created by Pelaia II, Tom on 9/4/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import java.util.*;
import java.util.concurrent.Callable;

import xal.extension.service.*;
import xal.tools.dispatch.DispatchQueue;
import xal.extension.application.ApplicationStatus;
import xal.tools.messaging.MessageCenter;


/** MonitorModel */
public class MonitorModel extends java.lang.Object {
	/** queue to manage access to the remote apps list and map */
	final private DispatchQueue REMOTE_APPS_ACCESS_QUEUE;

	/** list of remote applications */
	final private List<RemoteAppRecord> REMOTE_APPS;

	/** table of remote apps keyed by name */
	final private Map<String,RemoteAppRecord> REMOTE_APPS_TABLE;

	/** proxy for dispatching events */
	final private MonitorModelListener EVENT_PROXY;


	/** Constructor */
    public MonitorModel() {
		EVENT_PROXY = MessageCenter.defaultCenter().registerSource( this, MonitorModelListener.class );

		REMOTE_APPS_ACCESS_QUEUE = DispatchQueue.createConcurrentQueue( "Remote Apps Access" );
		REMOTE_APPS = new ArrayList<RemoteAppRecord>();
		REMOTE_APPS_TABLE = new HashMap<String,RemoteAppRecord>();

		monitorRemoteApplications();
    }


	/** Get the list of remote apps */
	public List<RemoteAppRecord> getRemoteApps() {
		return REMOTE_APPS_ACCESS_QUEUE.dispatchSync( new Callable<List<RemoteAppRecord>>() {
			public List<RemoteAppRecord> call() {
				return new ArrayList<RemoteAppRecord>( REMOTE_APPS );
			}
		});
	}


	/** Add the listener to receive events from this monitor */
	public void addMonitorModelListener( final MonitorModelListener listener ) {
		MessageCenter.defaultCenter().registerTarget( listener, this, MonitorModelListener.class );
	}


	/** Remove the listener from receiving events from this monitor */
	public void removeMonitorModelListener( final MonitorModelListener listener ) {
		MessageCenter.defaultCenter().removeTarget( listener, this, MonitorModelListener.class );
	}


	/** Monitor remote applications */
	private void monitorRemoteApplications() {
		System.out.println( "Begin monitoring for remote applications..." );
		ServiceDirectory.defaultDirectory().addServiceListener( ApplicationStatus.class, new ServiceListener() {
			/**
			 * This method is called when a new service has been added.
			 * @param directory identifies the directory sending this notification
			 * @param serviceRef The service reference of service provided.
			 */
			public void serviceAdded( final ServiceDirectory directory, final ServiceRef serviceRef ) {
				final ApplicationStatus proxy = directory.getProxy( ApplicationStatus.class, serviceRef );
				final RemoteAppRecord remoteRecord = new RemoteAppRecord( proxy );
				final String serviceID = serviceRef.getRawName();
				
				System.out.println( "Found remote application with ID: " + serviceID );

				REMOTE_APPS_ACCESS_QUEUE.dispatchBarrierAsync( new Runnable() {
					public void run() {
						REMOTE_APPS_TABLE.put( serviceID, remoteRecord );
						REMOTE_APPS.add( remoteRecord );
					}
				});

				EVENT_PROXY.remoteAppsChanged( MonitorModel.this, getRemoteApps() );
			}


			/**
			 * This method is called when a service has been removed.
			 * @param directory identifies the directory sending this notification
			 * @param type The type of the removed service.
			 * @param name The name of the removed service.
			 */
			public void serviceRemoved( final ServiceDirectory directory, final String type, final String name ) {
				System.out.println( "Removing application with name: " + name );				

				REMOTE_APPS_ACCESS_QUEUE.dispatchBarrierAsync( new Runnable() {
					public void run() {
						final RemoteAppRecord remoteRecord = REMOTE_APPS_TABLE.get( name );
						REMOTE_APPS_TABLE.remove( name );
						REMOTE_APPS.remove( remoteRecord );
					}
				});
				
				EVENT_PROXY.remoteAppsChanged( MonitorModel.this, getRemoteApps() );
			}
		});
	}
}
