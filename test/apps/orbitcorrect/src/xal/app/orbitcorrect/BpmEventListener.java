/*
 *  BpmEventListener.java
 *
 *  Created on Thu Jan 08 09:08:11 EST 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;


/**
 * BpmEventListener
 *
 * @author   tap
 * @since    Jan 8, 2004
 */
public interface BpmEventListener {
	/**
	 * The BPM's monitored state has changed.
	 *
	 * @param agent   The BPM agent with the channel whose value has changed
	 * @param record  The record of the new BPM state
	 */
	public void stateChanged( BpmAgent agent, BpmRecord record );


	/**
	 * The channel's connection has changed. Either it has established a new connection or the
	 * existing connection has dropped.
	 *
	 * @param agent      The BPM agent with the channel whose connection has changed
	 * @param handle     The handle of the BPM channel whose connection has changed.
	 * @param connected  The channel's new connection state
	 */
	public void connectionChanged( BpmAgent agent, String handle, boolean connected );
}

