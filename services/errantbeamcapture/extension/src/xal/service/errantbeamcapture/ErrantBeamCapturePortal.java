//
// ErrantBeamCapturePortal.java
// xal
//
// Created by Tom Pelaia on 3/28/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.service.errantbeamcapture;

import xal.extension.service.OneWay;


/** ErrantBeamCapturePortal */
public interface ErrantBeamCapturePortal {
	/** reload the channels to monitor */
	public boolean reloadChannels();
	
	/** stop monitoring */
	public boolean stop();
	
	/** start monitoring */
	public boolean run();
	
	/** shutdown the service */
	@OneWay
	public int shutdown();
}
