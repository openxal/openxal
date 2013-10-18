/*
 * ApplicationStatus.java
 *
 * Created on Fri Oct 10 10:50:12 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.extension.application;

import xal.extension.service.OneWay;
import java.util.Date;


/**
 * ApplicationStatus is an interface used in remote access to advertise application status.
 * @author  tap
 */
public interface ApplicationStatus {
	/** 
	 * Get the free memory available to the application instance.
	 * @return The free memory available on this virtual machine.
	 */
	public double getFreeMemory();
	
	
	/**
	 * Get the total memory consumed by the application instance.
	 * @return The total memory consumed by the application instance.
	 */
	public double getTotalMemory();
	
	
	/**
	 * Get the application name.
	 * @return The application name.
	 */
	public String getApplicationName();
	
	
	/**
	 * Get the name of the host where the application is running.
	 * @return The name of the host where the application is running.
	 */
	public String getHostName();
	
	
	/**
	 * Get the launch time of the application in seconds since the epoch (midnight GMT, January 1, 1970)
	 * @return the time at with the application was launched in seconds since the epoch
	 */
	public Date getLaunchTime();
	

	/**
	 * Get a heartbeat from the service.
	 * @return the time measured from the service at which the heartbeat was sent
	 */
	public Date getHeartbeat();
	

	/** reveal the application by bringing all windows to the front */
	public void showAllWindows();
	
	
	/** Request that the virtual machine run the garbage collector. */
	public void collectGarbage();
	
	
	/**
	 * Quit the application normally.
	 * @param code An unused status code.
	 */
    @OneWay
	public void quit( final int code );
	
	
	/**
	 * Force the application to quit immediately without running any finalizers.
	 * @param code The status code used for halting the virtual machine.
	 */
    @OneWay
	public void forceQuit( int code );
}

