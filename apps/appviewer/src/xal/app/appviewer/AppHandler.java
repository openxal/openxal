/*
 * AppHandler.java
 *
 * Created on Fri Oct 17 09:45:58 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.appviewer;

import xal.tools.data.*;
import xal.tools.dispatch.DispatchQueue;
import xal.application.ApplicationStatus;
import xal.tools.services.*;

import java.util.Date;
import java.util.logging.*;


/**
 * AppHandler handles the requests for a single remote application.
 *
 * @author  tap
 */
class AppHandler implements DataKeys {
	protected String _id;
	protected ApplicationStatus _proxy;
	
	//protected Lock _lock;
	
	// constant properties
	protected String applicationName = "?";
	protected Date launchTime;
	protected String host = "?";
	
	// state
	protected boolean hasInitialized;
	protected boolean _isRemoteStatusOkay;
	
	
	/**
	 * Constructor
	 */
	public AppHandler(String id, ApplicationStatus proxy) {
		_id = id;
		_proxy = proxy;
		_lock = new Lock();
		hasInitialized = false;
		_isRemoteStatusOkay = true;
	}
	
	/**
	 *
	 */
	public String getID() {
		return _id;
	}
	
	
	/**
	 * Fetch and store constant application information.
	 */
	protected void firstFetch() throws RemoteMessageException {		
		try {
			applicationName = _proxy.getApplicationName();
			host = _proxy.getHostName();
			long launchTimestamp = (long) ( 1000 * _proxy.getLaunchTime() );
			launchTime = new Date(launchTimestamp);
		}
		catch(RemoteMessageException exception) {
			final String message = "Remote invocation exception for application with ID: \"" + _id + 
			"\" and name \"" + applicationName + "\" on host: \"" + host + "\"";
			Logger.getLogger("global").log( Level.SEVERE, message , exception );
			System.err.println( message );
			System.err.println(exception);
			_isRemoteStatusOkay = false;
		}
		
		hasInitialized = true;
	}
	
	
	/**
	 * Update the record with the current information from the remote application.  Try to get a lock
	 * for updating data from the remote application.  If the lock is unsuccessful simply return false, otherwise
	 * perform the update.
	 * @param record 
	 * @return true if the record was successfully updated and false if not.
	 */
	public boolean update( final GenericRecord record ) throws RemoteMessageException {
		if ( _lock.tryLock() ) {
			try {				
				if ( !_isRemoteStatusOkay )  return true;
				
				if ( !hasInitialized ) {
					firstFetch();
					if ( !hasInitialized )  return true;		// just return and dont' update anything else
				}
				
				record.setValueForKey(applicationName, APPLICATION_KEY);
				record.setValueForKey(launchTime, LAUNCH_TIME_KEY);
				record.setValueForKey(host, HOST_KEY);
				
				record.setValueForKey(_proxy.getTotalMemory(), TOTAL_MEMORY_KEY);
				record.setValueForKey(_proxy.getFreeMemory(), FREE_MEMORY_KEY);
				return true;
			}
			catch(RemoteMessageException exception) {
				final String message = "Remote invocation error during an update for application with " + "ID: \"" + _id + "\" and name \"" + applicationName + "\" on host: \"" + host + "\"";
				Logger.getLogger("global").log( Level.SEVERE, message, exception );
				System.err.println("Remote exception while updating...");
				System.err.println(exception);
				_isRemoteStatusOkay = false;
				return true;
			}
			finally {
				_lock.unlock();
				record.setValueForKey(_id, ID_KEY);
				record.setValueForKey(_isRemoteStatusOkay, SERVICE_OKAY_KEY);
			}
		}
		return false;
	}
	
	
	/**
	 * Request that the remote application run its garbage collector.
	 */
	public void collectGarbage() {
		if ( _proxy != null ) {
			try {
				_proxy.collectGarbage();
			}
			catch(RemoteMessageException exception) {
				final String message = "Remote invocation error while attempting to request a " + 
				"garbage collection on application with ID: \"" + _id + "\" and name \"" + applicationName + "\" on host: \"" + host + "\"";
				Logger.getLogger("global").log( Level.SEVERE, message, exception );
				System.err.println( message );
				System.err.println( exception );
				_isRemoteStatusOkay = false;
			}
		}
	}
	
	
	/** Request that the remote application reveal itself. */
	public void showAllWindows() {
		if ( _proxy != null ) {
			try {
				_proxy.showAllWIndows();
			}
			catch(RemoteMessageException exception) {
				final String message = "Remote invocation error while attempting to request to " + "show all windows on application with ID: \"" + _id + "\" and name \"" + applicationName + "\" on host: \"" + host + "\"";
				Logger.getLogger("global").log( Level.SEVERE, message, exception );
				System.err.println( message );
				System.err.println( exception );
				_isRemoteStatusOkay = false;
			}
		}
	}
	
	
	/**
	 * Request that the remote application quit cleanly.
	 */
	public void quitApplication() {
		if ( _proxy != null ) {
			try {
				_proxy.quit(1);
			}
			catch(RemoteMessageException exception) {
				// We expect exceptions here due to service exiting.
			}
		}
	}
	
	
	/**
	 * Request that the remote application quit immediately.
	 */
	public void forceQuitApplication() {
		if ( _proxy != null ) {
			try {
				_proxy.forceQuit(1);
			}
			catch(RemoteMessageException exception) {
				// We expect exceptions here due to service exiting.
			}
		}
	}
}

