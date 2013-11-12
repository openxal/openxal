//
//  IPhaseState.java
//  xal
//
//  Created by Thomas Pelaia on 2/9/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.model.probe.traj;

import xal.tools.math.r3.R3;
import xal.tools.beam.Twiss;


/**
 * Interface for exposing machine parameters involving the state data produced by
 * numerical simulation.  The context of these
 * quantities should depend upon the type <code>S</code> of the probe
 * states produced by the simulation. 
 *
 * @author Thomas Pelaia
 * @author Christopher K. Allen
 * @since   2/9/05
 * @version Oct 29, 2013
 * 
 * @deprecated  The ICoordinateState/IPhaseState interface is replaced by 
 *              <code>ISimulationResults&lt;S&gt;</code>
 */
public interface IPhaseState extends ICoordinateState {
	
    /*
     * Constants
     */
    
    /** index of the X coordinate result */
	final public int X = 0;
	
	/** index of the Y coordinate result */
	final public int Y = 1;
	
	/** index of the Z coordinate result */
	final public int Z = 2;
		
	
	/*
	 * Interface Methods
	 */
	
    /** 
	* Returns the array of twiss objects for this state for all three planes.
	* @return array (twiss-H, twiss-V, twiss-L)
	*/
    public Twiss[] getTwiss();		
	
	
    /**
	 * Get the betatron phase for all three phase planes.
     * 
     * @return  vector (psix,psiy,psiz) of phases in radians
     */
    public R3 getBetatronPhase();
}