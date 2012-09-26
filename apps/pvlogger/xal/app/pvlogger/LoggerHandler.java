/*
 * LoggerHandler.java
 *
 * Created on Fri Oct 17 09:45:58 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogger;

import xal.service.pvlogger.*;
import xal.tools.data.*;
import xal.tools.dispatch.DispatchQueue;
import xal.tools.services.*;
import xal.tools.messaging.MessageCenter;

import java.util.*;


/**
 * LoggerHandler handles the requests for a single remote logger.
 *
 * @author  tap
 */
class LoggerHandler implements DataKeys {
	protected String _id;
	protected RemoteLogging _remoteProxy;

	/** serial queue for performing serialized record updates */
	private final DispatchQueue UPDATE_QUEUE;

	// constant properties
	protected Date _launchTime;
	protected String _host = "?";
	protected List<String> _groupTypes;
	
	// messaging
	protected final MessageCenter _messageCenter;
	protected final LoggerHandlerListener _postProxy;
	
	// state
	protected boolean _hasInitialized;
	protected boolean _isRemoteStatusOkay;
	
	/** logger sessions */
	protected Map<String,LoggerSessionHandler> _loggerSessions;
	
	
	/**
	 * Constructor
	 */
	public LoggerHandler(String id, RemoteLogging remoteProxy) {
		_id = id;
		_remoteProxy = remoteProxy;

		UPDATE_QUEUE = DispatchQueue.createSerialQueue( "Update Queue" );

		_hasInitialized = false;
		_isRemoteStatusOkay = true;
		
		_groupTypes = Collections.<String>emptyList();
		_loggerSessions = new HashMap<String,LoggerSessionHandler>();
		
		_messageCenter = new MessageCenter("Logger Handler");
		_postProxy = _messageCenter.registerSource( this, LoggerHandlerListener.class );
	}


	/** dispose of the update queue */
	protected void finalize() throws Throwable {
		try {
			UPDATE_QUEUE.dispose();
		}
		finally {
			super.finalize();
		}
	}

	
	/**
	 * Add a listener of logger handler events from this handler
	 * @param listener the listener to add
	 */
	public void addLoggerHandlerListener(LoggerHandlerListener listener) {
		_messageCenter.registerTarget(listener, this, LoggerHandlerListener.class);
	}
	
	
	/**
	 * Remove the listener of logger handler events from this handler
	 * @param listener the listener to remove
	 */
	public void removeLoggerHandlerListener(LoggerHandlerListener listener) {
		_messageCenter.removeTarget(listener, this, LoggerHandlerListener.class);
	}
	
	
	/**
	 * Get the remote proxy managed by this handler
	 * @return the remote proxy
	 */
	public RemoteLogging getRemoteProxy() {
		return _remoteProxy;
	}
	
	
	/** Fetch and store constant application information. */
	protected void firstFetch() throws RemoteMessageException {
		try {
			_host = _remoteProxy.getHostName();
			_launchTime = _remoteProxy.getLaunchTime();
			_groupTypes = _remoteProxy.getGroupTypes();
			System.out.println( _groupTypes );
			makeLoggerSessions();
		}
		catch(RemoteMessageException exception) {
			System.err.println("Got an initial remote fetch exception...");
			System.err.println(exception);
			_isRemoteStatusOkay = false;
		}
		
		_hasInitialized = true;
	}
	
	
	/** Make a logger session for each group available. */
	protected void makeLoggerSessions() {
		final List<String> groups = _remoteProxy.getGroupTypes();
		_loggerSessions = new HashMap<String,LoggerSessionHandler>( groups.size() );
		
		for ( final String groupType : groups ) {
			_loggerSessions.put( groupType, new LoggerSessionHandler( groupType, _remoteProxy ) );
		}
	}
	
	
	/**
	 * Update the record with the current information from the remote application.
	 * @param record the record to update
	 * @return true if the record was successfully updated and false if not.
	 */
	public boolean update( final GenericRecord record ) {
		UPDATE_QUEUE.dispatchSync( new Runnable() {
			public void run() {
				try {
					if ( !_isRemoteStatusOkay )  return;

					if ( !_hasInitialized ) {
						firstFetch();
						if ( !_hasInitialized )  return;		// just return and don't update anything else
					}

					record.setValueForKey( _launchTime, LAUNCH_TIME_KEY );
					record.setValueForKey( _host, HOST_KEY );

					updateSessions();

					record.setValueForKey( new Date(), LAST_CHECK_KEY );
				}
				catch(Exception exception) {
					System.err.println( "Got an update exception..." );
					System.err.println( exception );
				}
				finally {
					record.setValueForKey( _id, ID_KEY );
					record.setValueForKey( _isRemoteStatusOkay, SERVICE_OKAY_KEY );
				}
			}
		});

		_postProxy.recordUpdated( this, record );

		return true;
	}
	
	
	/** Update each logger session. */
	protected void updateSessions() {
		for ( final LoggerSessionHandler sessionHandler : _loggerSessions.values() ) {
			sessionHandler.update();
		}
	}
	
	
	/**
	 * Get the group types that are being logged.  Each type has an associated PVLogger session.
	 * @return the list of pvlogger groups 
	 */
	public List<String> getGroupTypes() {
		return _groupTypes;
	}
	
	
	/**
	 * Get a logger session by group type
	 * @param groupType the type of group for which to get the logger session
	 * @return the named logger session
	 */
	public LoggerSessionHandler getLoggerSession(final String groupType) {
		return _loggerSessions.get( groupType );
	}
	
	
	/** Publish snapshots remaining in the buffer */
	public void publishSnapshots() {
		if ( _remoteProxy != null ) {
			try {
				_remoteProxy.publishSnapshots();
			}
			catch(RemoteMessageException exception) {
				_isRemoteStatusOkay = false;
				System.err.println( "Remote exception while trying to publish snapshots..." );
				exception.printStackTrace();
			}
		}
	}
	
	
	/** Restart the logger by stopping the logger, reloading the groups from the database and resume logging for the new sessions. */
	public void restartLogger() {
		if ( _remoteProxy != null ) {
			try {
				_remoteProxy.restartLogger();
			}
			catch(RemoteMessageException exception) {
				_isRemoteStatusOkay = false;
				System.err.println( "Remote exception while trying to restart the logger..." );
				exception.printStackTrace();
			}
		}
	}
	
	
	/** Request that the remote logging resume. */
	public void resumeLogging() {
		if ( _remoteProxy != null ) {
			try {
				_remoteProxy.resumeLogging();
			}
			catch(RemoteMessageException exception) {
				_isRemoteStatusOkay = false;
				System.err.println( "Remote exception while trying to resume logging..." );
				exception.printStackTrace();
			}
		}
	}
	
	
	/** Request that the remote logging stop. */
	public void stopLogging() {
		if ( _remoteProxy != null ) {
			try {
				_remoteProxy.stopLogging();
			}
			catch(RemoteMessageException exception) {
				_isRemoteStatusOkay = false;
				System.err.println( "Remote exception while trying to stop logging..." );
				exception.printStackTrace();
			}
		}
	}
	
	
	/** Request that the remote service shutdown. */
	public void shutdown() {
		if ( _remoteProxy != null ) {
			try {
				_remoteProxy.shutdown(0);
			}
			catch(RemoteMessageException exception) {
				// We expect exceptions here due to service exiting.
			}
		}
	}
}

