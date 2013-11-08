//
//  ISimulationResults.java
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
 * <p>
 * Interface for exposing machine parameters involving the state data produced by
 * numerical simulation.  The context of these
 * quantities should depend upon the type <code>S</code> of the probe
 * states produced by the simulation.
 * </p>
 * <p>
 * In particular, for <code>S</code> = <code>xal.model.probe.traj.TransferMapState</code> the
 * quantities of this interface are regarded as machine parameters, since the beam
 * does not influence transfer map calculations in the 
 * <code>xal.model.probe.TransferMapProbe</code>.  However, when 
 * <code>S</code> = <code>xal.model.probe.traj.EnvelopeProbeState</code> then the interface
 * quantities are essentially beam parameters since the beam dynamics figure into the
 * states of <code>xal.model.probe.EnvelopeProbe</code>.
 * </p> 
 *
 * @param <S>   probe state type
 * @param <T>   simulation trajectory type (container of probe states)
 *
 * @author Christopher K. Allen
 * @author Thomas Pelaia
 * @since   2/9/05
 * @version Nov 7, 2013
 */
public interface ISimulationResults <S extends ProbeState> {
	

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
     * <p>
     * <h4>Deprecated</h4>
     *  This quantity is obtuse and not well defined - PhaseCoordinates of what?  
     *              Is this a centroid location?
     *              From which starting orbit?  
     *              Not all simulation results have quantities naturally associated with phase coordinates
     *              We need to stop use this or call it something else.
     * </p>
     * <p> 
     *  Returns homogeneous phase space coordinates of the simulation centroid.  
     *  I believe this quantity is open for interpretation; we can be referring to
     *  the position of the design trajectory or the location of the beam centroid,
     *  whatever "beam" means in the context.  The units
     *  are meters and radians.
     *
     *  @param   state   simulation state where parameters are computed
     *  
     *  @return     vector (<i>x,x',y,y',z,z'</i>,1) of phase space coordinates
     *             
     */
    @Deprecated
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
    
    /**
     * Compute and return the dispersion function at the given state location 
     * due to energy spread.  The returned value <b>&Delta;</b> is the vector
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <b>&Delta;</b> &equiv; 
     * (&Delta;<i>x</i>, &Delta;<i>x'</i>, &Delta;<i>y</i>, &Delta;<i>y'</i>, 0, 0, 1)
     * <br/>
     * <br/>
     * where, when multiplied by momentum spread &delta; &equiv; &Delta;<i>p</i>/<i>p</i> yields
     * the change in fixed orbit position.  That is <b>z</b> = <b>z</b><sub>0</sub> + &delta;<b>&Delta;</b>.
     * 
     * @param state simlulation state where parameters are computed 
     * 
     * @return  the vector <b>&Delta;</b> of dispersion coefficients
     *
     * @author Christopher K. Allen
     * @since  Nov 8, 2013
     */
    public PhaseVector computeChromDispersion(S state);
    
}
