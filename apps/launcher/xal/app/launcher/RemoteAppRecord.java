//
// RemoteAppRecord.java
// Open XAL
//
// Created by Pelaia II, Tom on 9/4/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import xal.application.ApplicationStatus;


/** RemoteAppRecord wraps the remote proxy so it can be hashed and included in collections */
public class RemoteAppRecord implements ApplicationStatus {
	/** remote proxy */
	private ApplicationStatus REMOTE_PROXY;

	
	/** Constructor */
    public RemoteAppRecord( final ApplicationStatus proxy ) {
		REMOTE_PROXY = proxy;
    }

	
	/**
	 * Get the free memory available to the application instance.
	 * @return The free memory available on this virtual machine.
	 */
	public double getFreeMemory() {
		return REMOTE_PROXY.getFreeMemory();
	}


	/**
	 * Get the total memory consumed by the application instance.
	 * @return The total memory consumed by the application instance.
	 */
	public double getTotalMemory() {
		return REMOTE_PROXY.getTotalMemory();
	}


	/**
	 * Get the application name.
	 * @return The application name.
	 */
	public String getApplicationName() {
		return REMOTE_PROXY.getApplicationName();
	}


	/**
	 * Get the name of the host where the application is running.
	 * @return The name of the host where the application is running.
	 */
	public String getHostName() {
		return REMOTE_PROXY.getHostName();
	}


	/**
	 * Get the launch time of the application in seconds since the epoch (midnight GMT, January 1, 1970)
	 * @return the time at with the application was launched in seconds since the epoch
	 */
	public java.util.Date getLaunchTime() {
		return REMOTE_PROXY.getLaunchTime();
	}


	/** reveal the application by bringing all windows to the front */
	public boolean showAllWindows() {
		return REMOTE_PROXY.showAllWindows();
	}


	/**
	 * Request that the virtual machine run the garbage collector.
	 * @return true.
	 */
	public boolean collectGarbage() {
		return REMOTE_PROXY.collectGarbage();
	}


	/**
	 * Quit the application normally.
	 * @param code An unused status code.
	 */
	public void quit( final int code ) {
		REMOTE_PROXY.quit( code );
	}


	/**
	 * Force the application to quit immediately without running any finalizers.
	 * @param code The status code used for halting the virtual machine.
	 */
	public void forceQuit( int code ) {
		REMOTE_PROXY.forceQuit( code );
	}
}
