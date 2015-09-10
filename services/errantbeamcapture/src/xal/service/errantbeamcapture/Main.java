//
// Main.java
// xal
//
// Created by Tom Pelaia on 3/26/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.service.errantbeamcapture;

/**
 * Main
 * @author  tap
 */
public class Main {
	/** main model */
	final private ErrantBeamCaptureModel MAIN_MODEL;
	
	
	/**
	 * Main constructor
	 */
	public Main() {
		MAIN_MODEL = new ErrantBeamCaptureModel();
		new ErrantBeamCaptureService( MAIN_MODEL );
	}
	
	
	/**
	 * Run the service
	 */
	public void run() {
		MAIN_MODEL.run();
	}
	
	
	/**
	 * Main entry point to the service.  Run the service.
	 * @param args The launch arguments to the service.
	 */
	static public void main( final String[] args ) {
		new Main().run();
	}
}

