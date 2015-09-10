//
// MachineStateChangeListener.java
// xal
//
// Created by Tom Pelaia on 4/10/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.service.errantbeamcapture;

import xal.ca.Timestamp;


/** MachineStateEventListener */
public interface MachineStateChangeListener {
	/** Event from the specified monitor indicating that the machine state has changed */
	public void machineStateChanged( final MachineStateMonitor monitor, final boolean isGoodState, final Timestamp timestamp );
}
