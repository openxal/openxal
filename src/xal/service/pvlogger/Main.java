/*
 * Main.java
 *
 * Created on Wed Jan 14 13:03:12 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger;

import xal.extension.service.ServiceDirectory;


/**
 * Main
 *
 * @author  tap
 */
public class Main {
	protected LoggerModel model;
	
	
	/** Main Constructor */
	public Main() {
		model = new LoggerModel();
	}
	
	
	/**
	 * run the service by starting the logger
	 */
	protected void run() {
		model.startLogging();
		new LoggerService(model);
	}
	
	
	/**
	 * Main entry point to the service.  Run the service.
	 * @param args The launch arguments to the service.
	 */
	static public void main(String[] args) {
		new Main().run();
	}
}

