/*
 * Main.java
 *
 * Created on Mon January 6 13:50:00 EST 2012
 *
 * Copyright (c) 2012 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.worker;

import xal.extension.service.ServiceDirectory;

import java.util.Date;


/**
 * Main
 * @author  tap
 */
public class Main {
	/** The time at which the application was launched */
	final static protected Date LAUNCH_TIME;
	
	/** indicates whether verbose printing is enabled */
	final static protected boolean IS_VERBOSE;
	
	
	/**
	 * Static initializer 
	 */
	static {
		LAUNCH_TIME = new Date();
		
		final String verboseProperty = System.getProperty( "verbose", "false" );
		IS_VERBOSE = Boolean.parseBoolean( verboseProperty );
	}
	
	
	/** run the service */
	protected void run() {
        ServiceDirectory.defaultDirectory().registerService( Working.class, "Worker", new WorkService() );
        System.out.println( "Listening for work requests..." );
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
    
    
    /** Shutdown the application */
    static public void shutdown( final int code ) {
        System.out.println( "Shutting down work service..." );
        System.exit( code );
    }
}

