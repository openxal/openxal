//
//  IMachineParameters.java
//  xal
//
//  Created by Thomas Pelaia on 2/9/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.beam.calc;

import xal.model.probe.traj.ProbeState;
import xal.tools.math.r3.R3;
import xal.tools.beam.PhaseVector;
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
 */
public interface IMachineParameters<S extends ProbeState> {
	

    /*
     * Interface Methods
     */

    /** 
     * Returns the array of twiss objects for this state for all three planes at the
     * location of the given simulation state. These could be the matched beam
     * Twiss parameters for a periodic structure or the dynamics Twiss parameters
     * of a mismatched or otherwise evolving beam.
     * 
     * @param   state   simulation state where Twiss parameters are computed
     * 
     * @return array (twiss-H, twiss-V, twiss-L)
     */
    public Twiss[] computeTwissParameters(S state);		
	
	
    /**
     * Get the betatron phase values at the given state location for 
     * all three phase planes.
     * 
     *  @param   state   simulation state where parameters are computed
     * 
     * @return  vector (&psi;<sub><i>x</i></sub>, &psi;<sub><i>y</i></sub>, &psi;<sub><i>x</i></sub>) of phases in radians
     */
    public R3 computeBetatronPhase(S state);

    /** 
     *  Returns homogeneous phase space coordinates of the simulation centroid.  
     *  I believe this quantity is open for interpretation; we can be referring to
     *  the position of the design trajectory or the location of the beam centroid,
     *  whatever "beam" means in the context.  The units
     *  are meters and radians.
     *
     *  @param   state   simulation state where parameters are computed
     *  
     *  @return     vector (<i>x,x',y,y',z,z'</i>,1) of phase space coordinates
     */
//    public PhaseVector computePhaseLocation(S state);
    public PhaseVector computePhaseCoordinates(S state);


    /**
     * Computes the fixed orbit about which betatron oscillations occur. This
     * value is well-defined for rings but could be ambiguous for beam envelope
     * simulation, especially with regard to method 
     * <code>{@link #computePhaseCoordinates(ProbeState)}</code>.
     * 
     * @param   state   simulation state where parameters are computed
     *  
     * @return the reference orbit vector (<i>x,x',y,y',z,z'</i>,1)
     */
    public PhaseVector computeFixedOrbit(S state);
}