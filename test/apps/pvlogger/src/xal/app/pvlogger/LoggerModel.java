/*
 * LoggerModel.java
 *
 * Created on Thu Jan 15 09:52:39 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogger;

import java.util.*;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.concurrent.Callable;

import xal.service.pvlogger.*;
import xal.extension.service.*;
import xal.tools.dispatch.DispatchQueue;
import xal.tools.messaging.MessageCenter;


/**
 * LoggerModel is the main model for the pvlogger client.
 * @author  tap
 */
public class LoggerModel {
	/** queue to manage access to the remote loggers list and map */
	final private DispatchQueue REMOTE_LOGGERS_ACCESS_QUEUE;

	/** list of remote loggers */
	final private List<RemoteLoggerRecord> REMOTE_LOGGERS;

	/** table of remote loggers keyed by serviceID */
	final private Map<String,RemoteLoggerRecord> REMOTE_LOGGERS_TABLE;

	// messaging
	private final MessageCenter MESSAGE_CENTER;
	private final LoggerModelListener POST_PROXY;
	
	
	/**
	 * LoggerModel Constructor
	 */
	public LoggerModel() {
		MESSAGE_CENTER = new MessageCenter("LoggerModel");
		POST_PROXY = MESSAGE_CENTER.registerSource(this, LoggerModelListener.class);
		
		REMOTE_LOGGERS_ACCESS_QUEUE = DispatchQueue.createConcurrentQueue( "Remote PV Loggers Access" );
		REMOTE_LOGGERS = new ArrayList<RemoteLoggerRecord>();
		REMOTE_LOGGERS_TABLE = new HashMap<String,RemoteLoggerRecord>();

		monitorLoggers();
		System.out.println("monitoring loggers...");
	}
	
	
	/**
	 * Dispose of the logger model.  Stop the timer and dispose of the service directory.
	 */
	protected void dispose() {
		ServiceDirectory.defaultDirectory().dispose();
		System.out.println("disposed of model...");
	}


	/** Get the list of remote logger records */
	public List<RemoteLoggerRecord> getRemoteLoggers() {
		return REMOTE_LOGGERS_ACCESS_QUEUE.dispatchSync( new Callable<List<RemoteLoggerRecord>>() {
			public List<RemoteLoggerRecord> call() {
				return new ArrayList<RemoteLoggerRecord>( REMOTE_LOGGERS );
			}
		});
	}
	
	
	/**
	 * Add a listener of events from this model.
	 * @param listener The listener to register to receiver events from this model.
	 */
	public void addLoggerModelListener( final LoggerModelListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, LoggerModelListener.class );
	}
	
	
	/**
	 * Remove a listener of events from this model.
	 * @param listener The listener to remove from receiving events from this model.
	 */
	public void removeLoggerModelListener( final LoggerModelListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, LoggerModelListener.class );
	}
	
	
	/**
	 * Monitor the addition and removal of XAL applications.
	 */
	public void monitorLoggers() {
		System.out.println( "Begin monitoring for remote PV Loggers..." );
		ServiceDirectory.defaultDirectory().addServiceListener( RemoteLogging.class, new ServiceListener() {
			/**
			 * This method is called when a new service has been added.
			 * @param directory identifies the directory sending this notification
			 * @param serviceRef The service reference of service provided.
			 */
			public void serviceAdded( final ServiceDirectory directory, final ServiceRef serviceRef ) {
				final RemoteLogging proxy = directory.getProxy( RemoteLogging.class, serviceRef );
				final RemoteLoggerRecord remoteRecord = new RemoteLoggerRecord( proxy );
				final String serviceID = serviceRef.getRawName();

				System.out.println( "Found remote PV Logger with ID: " + serviceID );

				REMOTE_LOGGERS_ACCESS_QUEUE.dispatchBarrierAsync( new Runnable() {
					public void run() {
						REMOTE_LOGGERS_TABLE.put( serviceID, remoteRecord );
						REMOTE_LOGGERS.add( remoteRecord );
					}
				});

				POST_PROXY.loggersChanged( LoggerModel.this, getRemoteLoggers() );
			}


			/**
			 * This method is called when a service has been removed.
			 * @param directory identifies the directory sending this notification
			 * @param type The type of the removed service.
			 * @param name The name of the removed service.
			 */
			public void serviceRemoved( final ServiceDirectory directory, final String type, final String name ) {
				System.out.println( "Removing PV Logger with name: " + name );

				REMOTE_LOGGERS_ACCESS_QUEUE.dispatchBarrierAsync( new Runnable() {
					public void run() {
						final RemoteLoggerRecord remoteRecord = REMOTE_LOGGERS_TABLE.get( name );
						remoteRecord.setUpdateListener( null );		// stop observing updates
						REMOTE_LOGGERS_TABLE.remove( name );
						REMOTE_LOGGERS.remove( remoteRecord );
					}
				});

				POST_PROXY.loggersChanged( LoggerModel.this, getRemoteLoggers() );
			}
		});
	}
}

