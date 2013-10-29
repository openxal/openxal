//
//  ICoordinateState.java
//  xal
//
//  Created by Thomas Pelaia on 2/9/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.model.probe.traj;

import xal.tools.beam.PhaseVector;


/**
 * Interface for exposing machine parameters which are points in phase space.
 * The calculations require state data produced by numerical simulation.  The context of these
 * quantities should depend upon the type <code>S</code> of the probe
 * states produced by the simulation. 
 *
 * @author Thomas Pelaia
 * @author Christopher K. Allen
 * @since   2/9/05
 * @version Oct 29, 2013
 */
public interface ICoordinateState /* extends IProbeState */ {
    
    /** 
	*  Returns homogeneous phase space coordinates of the particle.  The units
	*  are meters and radians.
	*
	*  @return     vector (x,x',y,y',z,z',1) of phase space coordinates
	*/
    public PhaseVector getPhaseCoordinates();
	
	
	/**
	 * Get the fixed orbit about which betatron oscillations occur.
	 * @return the reference orbit vector (x,x',y,y',z,z',1)
	 */
	public PhaseVector getFixedOrbit();
}
