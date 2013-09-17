//
//  MatrixTrajectory.java
//  xal
//
//  Created by Tom Pelaia on 6/21/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//
package xal.model.probe.traj;

import xal.tools.beam.PhaseMatrix;


/** interface for trajectories that can generate transfer matrices between elements */
public interface MatrixTrajectory {
	/** get the transfer matrix from one node to a second node */
	public PhaseMatrix getTransferMatrix( final String fromElement, final String toElement );	
}
