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
 * 
 *
 * @author Thomas Pelaia
 * @since   2/9/05
 */
public interface ICoordinateState extends IProbeState {
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
