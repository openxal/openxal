/*
 * MPSModel.java
 *
 * Created on Fri Feb 06 10:02:49 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.mpstool;


/**
 * MPSModel is the main model for the service.  It manages monitors for each MPS type.
 * @author  jdg
 * @author  tap
 */
public class MPSModel {
	/** Source of MPS signals */
	final private SignalSource SIGNAL_SOURCE;
	
	/** MPS Monitors */
	final private MPSMonitor[] MONITORS;
	
	/** flag indicating whether the first faults statisics should be logged */
	final private boolean LOG_STATISTICS;
	
	
	/**
	 * MPSModel Constructor
	 */
	public MPSModel() {
		LOG_STATISTICS = Boolean.getBoolean( "logstats" );
		
		SIGNAL_SOURCE = new SQLSignalSource();
		
		MONITORS = new MPSMonitor[2];
		MONITORS[0] = new MPSMonitor( "FPL", SIGNAL_SOURCE, LOG_STATISTICS );
		MONITORS[1] = new MPSMonitor( "FPAR", SIGNAL_SOURCE, LOG_STATISTICS );
	}
	
	
	/** determine whether the monitors log statistics */
	public boolean logsStatistics() {
		return LOG_STATISTICS;
	}
	
	
	/** 
	 * Shutdown the application.
	 * @param code The exit code.
	 */
	public void shutdown(int code) {
		try {
			for ( int index = 0 ; index < MONITORS.length ; index++ ) {
				MONITORS[index].dispose();
			}
		}
		finally {
			System.exit( code );
		}
	}
	
	
	/**
	 * Get all of the monitors.
	 * @return an array of all MPS monitors (one for each MPS type)
	 */
	public MPSMonitor[] getMonitors() {
		return MONITORS;
	}
	
	
	/**
	 * Convenience method for an MPS monitor from the array of monitors.
	 * @param index The index of the MPS monitor to get.
	 * @return The MPS monitor at the specified index in the MPS monitor array
	 */
	public MPSMonitor getMonitor( final int index ) {
		return MONITORS[index];
	}
}

