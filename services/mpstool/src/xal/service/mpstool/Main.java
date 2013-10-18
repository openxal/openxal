/*
 * Main.java
 *
 * Created on Wed Jan 14 13:03:12 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.mpstool;

import java.util.Date;
import xal.extension.service.ServiceDirectory;


/**
 * Main
 *
 * @author  tap
 */
public class Main {
	/** The time at which the application was launched */
	final static protected Date LAUNCH_TIME;
	
	/** The MPS Model */
	protected MPSModel _model;
	
	
	/**
	 * Static initializer 
	 */
	static {
		LAUNCH_TIME = new Date();
	}
	
	
	/**
	 * Main Constructor
	 */
	public Main() {
		_model = new MPSModel();
	}
	
	
	/**
	 * run the service by starting the logger
	 */
	protected void run() {
		new MPSService(_model);
	}
	
	
	/**
	 * Main entry point to the service.  Run the service.
	 * @param args The launch arguments to the service.
	 */
	static public void main(String[] args) {
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

