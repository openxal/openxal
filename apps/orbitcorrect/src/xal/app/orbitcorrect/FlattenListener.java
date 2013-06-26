/*
 *  FlattenListener.java
 *
 *  Created on Wed Oct 06 13:17:09 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.smf.*;

import java.util.*;


/**
 * FlattenListener
 *
 * @author   tap
 * @since    Oct 06, 2004
 */
public interface FlattenListener {
	/**
	 * sequence changed
	 * @param source     Description of the Parameter
	 * @param simulator  the new simulator
	 */
	public void simulatorChanged( Flattener source, MachineSimulator simulator );

	
	/**
	 * solving time changed
	 * @param source       the flattener whose solving time changed
	 * @param solvingTime  new solving time in seconds
	 */
	public void solvingTimeChanged( Flattener source, double solvingTime );


	/**
	 * sequence changed
	 * @param source    Description of the Parameter
	 * @param sequence  the new sequence
	 */
	public void sequenceChanged( Flattener source, AcceleratorSeq sequence );


	/**
	 * BPMs changed
	 * @param source  Description of the Parameter
	 * @param bpmAgents    Description of the Parameter
	 */
	public void bpmsChanged( Flattener source, List<BpmAgent> bpmAgents );


	/**
	 * Correctors changed
	 * @param source      Description of the Parameter
	 * @param supplies  Description of the Parameter
	 */
	public void correctorSuppliesChanged( Flattener source, List<CorrectorSupply> supplies );


	/**
	 * New optimal orbit found
	 * @param source  Description of the Parameter
	 * @param orbit   Description of the Parameter
	 */
	public void newOptimalOrbit( Flattener source, Orbit orbit );


	/**
	 * indicates that the flattener progress has been updated
	 * @param source            Description of the Parameter
	 * @param fractionComplete  Description of the Parameter
	 * @param message           Description of the Parameter
	 */
	public void progressUpdated( Flattener source, double fractionComplete, String message );
}

