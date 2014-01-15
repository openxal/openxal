//
// ErrantBeamCaptureService.java
// xal
//
// Created by Tom Pelaia on 3/28/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.service.errantbeamcapture;

import xal.extension.service.ServiceDirectory;


/** ErrantBeamCaptureService */
public class ErrantBeamCaptureService implements ErrantBeamCapturePortal {
	/** main model */
	final private ErrantBeamCaptureModel MODEL;
	
	
	/** Constructor */
    public ErrantBeamCaptureService( final ErrantBeamCaptureModel model ) {
		MODEL = model;
		broadcast();
    }
	
	
	/** reload the channels to monitor */
	public boolean reloadChannels() {
		MODEL.stop();
		MODEL.run();
		return true;
	}
	
	
	/** stop monitoring */
	public boolean stop() {
		MODEL.stop();
		return true;
	}
	
	
	/** start monitoring */
	public boolean run() {
		MODEL.run();
		return true;
	}
	
	
	/** shutdown the service */
	public int shutdown() {
		System.exit( 0 );
		return 0;
	}
	
	
	/** Begin broadcasting the service  */
	public void broadcast() {
		ServiceDirectory.defaultDirectory().registerService( ErrantBeamCapturePortal.class, "Errant Beam Capture", this );
		System.out.println( "broadcasting..." );
	}
}
