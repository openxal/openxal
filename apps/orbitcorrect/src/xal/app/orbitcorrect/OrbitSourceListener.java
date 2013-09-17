/*
 *  OrbitSourceListener.java
 *
 *  Created on Wed Jun 16 14:44:03 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.smf.AcceleratorSeq;

import java.util.*;


/**
 * OrbitSourceListener
 *
 * @author   tap
 * @since    Jun 16, 2004
 */
public interface OrbitSourceListener {
	/**
	 * Handle the event indicating that the specified orbit source has generated a new orbit.
	 * @param source    the orbit source generating the new orbit
	 * @param newOrbit  the new orbit
	 */
	public void orbitChanged( OrbitSource source, Orbit newOrbit );


	/**
	 * Handle the event indicating that the orbit source's sequence has changed.
	 * @param source       the orbit source generating the new orbit
	 * @param newSequence  the new sequence
	 * @param newBPMAgents      the new BPM agents
	 */
	public void sequenceChanged( OrbitSource source, AcceleratorSeq newSequence, List<BpmAgent> newBPMAgents );
	
	
	/**
	 * Handle the event indicating that the orbit source enable state has changed.
	 * @param source the orbit source generating the event
	 * @param isEnabled the new enable state of the orbit source
	 */
	public void enableChanged( OrbitSource source, boolean isEnabled );
}

