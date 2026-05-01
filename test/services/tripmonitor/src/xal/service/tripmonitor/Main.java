/*
 * Main.java
 *
 * Created on Mon July 31 11:03:13 EST 2006
 *
 * Copyright (c) 2006 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.tripmonitor;

import java.util.Date;


/**
 * Main
 * @author  tap
 */
public class Main {
	/** The time at which the application was launched */
	final static protected Date LAUNCH_TIME;
	
	/** The trip monitor model */
	protected TripMonitorManager MODEL;
	
	
	/**
	 * Static initializer 
	 */
	static {
		LAUNCH_TIME = new Date();
	}
	
	
	/** Main Constructor */
	public Main() {
		MODEL = new TripMonitorManager();
		new TripMonitorService( MODEL );
	}
	
	
	/** run the service by starting the logger */
	protected void run() {
		MODEL.run();
	}
	
	
	/**
	 * Main entry point to the service.  Run the service.
	 * @param args The launch arguments to the service.
	 */
	static public void main( final String[] args ) {
		new Main().run();
	}
	
	
	/**
	 * Get the time when this application was launched.
	 * @return the time when this application was launched
	 */
	static public Date getLaunchTime() {
		return LAUNCH_TIME;
	}
}

