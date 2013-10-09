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
 *
 * @author Thomas Pelaia
 * @since   2/9/05
 */
public interface IPhaseState extends ICoordinateState {
	/** index of the X coordinate result */
	final public int X = 0;
	
	/** index of the Y coordinate result */
	final public int Y = 1;
	
	/** index of the Z coordinate result */
	final public int Z = 2;
		
	
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