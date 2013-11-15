/**
 * ISimLocationResults.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 15, 2013
 */
package xal.tools.beam.calc;

import xal.model.probe.traj.ProbeState;
import xal.tools.beam.PhaseVector;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Nov 15, 2013
 */
public interface ISimLocationResults<S extends ProbeState> {

    /**
     * <p> 
     *  Returns homogeneous phase space coordinates of something involving the simulation
     *  data.  The interpretation is highly dependent upon the context of the data.  
     *  That is, this quantity is open for interpretation; we can be referring to
     *  the position of the design trajectory, an offset, or the location of the beam centroid,
     *  whatever "beam" means in the context.  The units
     *  are meters and radians.
     *  </p>
     * <p>
     * <h4>NOTE:</h4>
     *  This quantity is obtuse and not well defined - PhaseCoordinates of what?
     *  <br/>  
     *              &nbsp; &nbsp; &middot; Is this a centroid location?
     *  <br/>  
     *              &nbsp; &nbsp; &middot; From which starting orbit?  
     *  <br/>  
     *              &nbsp; &nbsp; &middot; Not all simulation results have quantities 
     *                                      naturally associated with phase coordinates
     *  <br/>  
     *              &nbsp; &nbsp; &middot; We need to stop use this or call it something else.
     * </p>
     *
     *  @param   state   simulation state where parameters are computed
     *  
     *  @return     vector (<i>x,x',y,y',z,z'</i>,1) of phase space coordinates
     *             
     */
    public PhaseVector computeCoordinatePosition(S state);


    /**
     * Computes the fixed orbit about which betatron oscillations occur. This
     * value is well-defined for rings but could be ambiguous for beam envelope
     * simulation, especially with regard to method 
     * <code>{@link #computeCoordinatePosition(ProbeState)}</code>.  In general, however,
     * the idea is that the return coordinate <b>z</b> in phase space <b>P</b><sup>6</sup> &cong;
     * <b>R</b><sup>6</sup> &times; {1} is invariant under some map 
     * <b>&phi;</b> : <b>P</b><sup>6</sup> &rarr; <b>P</b><sup>6</sup> representing the 
     * dynamics of the system. 
     * 
     * @param   state   simulation state where parameters are computed
     *  
     * @return the reference orbit vector (<i>x,x',y,y',z,z'</i>,1)
     */
    public PhaseVector computeFixedOrbit(S state);
    
    /**
     * Compute and return the aberration at the given state location 
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
     * @param state simulation state where parameters are computed 
     * 
     * @return  the vector <b>&Delta;</b> of dispersion coefficients
     *
     * @author Christopher K. Allen
     * @since  Nov 8, 2013
     */
    public PhaseVector computeChromAberration(S state);
}
