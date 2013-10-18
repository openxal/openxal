/*
 * ApplicationStatusService.java
 *
 * Created on Fri Oct 10 12:37:45 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.extension.application;

import java.util.Date;


/**
 * ApplicationStatusService handles application status queries on behalf of the running
 * application instance.  Provides status information to clients on the local network.
 *
 * @author  tap
 */
public class ApplicationStatusService implements ApplicationStatus {
	/**
	 * Get the free memory available to the application instance.
	 * @return The free memory available on this virtual machine in kB.
	 */
	public double getFreeMemory() {
		return ( (double)Runtime.getRuntime().freeMemory() ) / 1024;
	}
	
	
	/**
	 * Get the total memory consumed by the application instance.
	 * @return The total memory consumed by the application instance in kB.
	 */
	public double getTotalMemory() {
		return ( (double)Runtime.getRuntime().totalMemory() ) / 1024;
	}
	
	
	/** reveal the application by bringing all windows to the front */
	public void showAllWindows() {
		Application.getApp().showAllWindows();
	}
	
	
	/** Request that the virtual machine run the garbage collector. */
	public void collectGarbage() {
		System.gc();
	}
	
	
	/**
	 * Quit the application normally.
	 * @param code An unused status code.
	 */
	public void quit( final int code ) {
		Application.getApp().quit();
	}
	
	
	/**
	 * Force the application to quit immediately without running any finalizers.
	 * @param code The status code used for halting the virtual machine.
	 */
	public void forceQuit(int code) {
		Runtime.getRuntime().exit(code);
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
	 * Get the application name.
	 * @return The application name.
	 */
	public String getApplicationName() {
		return Application.getAdaptor().applicationName();
	}
	
	
	/**
	 * Get the launch time of the application in seconds since the epoch (midnight GMT, January 1, 1970)
	 * @return the time at with the application was launched in seconds since the epoch
	 */
	public Date getLaunchTime() {
		return Application.getApp().getLaunchTime();
	}


	/**
	 * Get a heartbeat from the service.
	 * @return the time measured from the service at which the heartbeat was sent
	 */
	public Date getHeartbeat() {
		return new Date();
	}
}

