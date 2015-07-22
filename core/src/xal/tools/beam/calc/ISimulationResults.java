/**
 * ISimulationResults.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 15, 2013
 */
package xal.tools.beam.calc;

import xal.model.probe.traj.ProbeState;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.math.r3.R3;

/**
 * <p>
 * Encapsulates base for any interfaces that processes simulation data.
 * Interfaces expose particle, machine, and beam parameters derived from the state 
 * data produced by
 * numerical simulation.  The context of these
 * quantities should depend upon the type <code>S</code> of the probe
 * states produced by the simulation.
 * </p>
 * <p>
 * Probe types can expose multiple interfaces.
 * In particular, for <code>S</code> = <code>xal.model.probe.traj.TransferMapState</code> the
 * quantities of this interface can be regarded as machine parameters, since the beam
 * does not influence transfer map calculations in the 
 * <code>xal.model.probe.TransferMapProbe</code>.  
 * However, a there are particle processors and envelope processors for them.  The
 * results of the calculations must be taken in the context of the data upon
 * which they calculate.  <b>See the associated Javadoc.</b>
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Nov 15, 2013
 */
public interface ISimulationResults {

    
    /**
     * This interface defines methods for computation results that are in the form
     * of points, or locations, in phase space.
     *
     * @param <S>     type of the probe state containing local simulation data
     *
     * @author Christopher K. Allen
     * @since  Nov 15, 2013
     */
    public interface ISimLocResults<S> extends ISimulationResults  {

        /**
         * <p> 
         *  Returns homogeneous phase space coordinates of something involving the simulation
         *  data.  The interpretation is highly dependent upon the context of the data.  
         *  That is, this quantity is open for interpretation; we can be referring to
         *  the position of the design trajectory, an offset, or the location of the beam centroid,
         *  whatever "beam" means in the context.  The units
         *  are meters and radians.
         *  </p>
         * <h3>NOTE:</h3>
         * <p>
         *  This quantity is obtuse and not well defined - PhaseCoordinates of what?
         *  <br>  
         *              &nbsp; &nbsp; &middot; Is this a centroid location?
         *  <br>  
         *              &nbsp; &nbsp; &middot; From which starting orbit?  
         *  <br>  
         *              &nbsp; &nbsp; &middot; Not all simulation results have quantities 
         *                                      naturally associated with phase coordinates
         * </p>
         *
         *  @param   state   simulation state where parameters are computed
         *  
         *  @return     vector (<i>x,x',y,y',z,z'</i>,1) of phase space coordinates
         *             
         */
        public PhaseVector computeCoordinatePosition(S state);


        /**
         * <p>
         * Computes the fixed orbit about which betatron oscillations occur. This
         * value is well-defined for rings but could be ambiguous for beam envelope
         * simulation, especially with regard to method 
         * <code>{@link #computeCoordinatePosition(ProbeState)}</code>.  The returned
         * value for some given simulation types are provided below.
         * </p>
         * <p>
         * In general
         * the idea is that the returned coordinate <b>z</b> in phase space <b>P</b><sup>6</sup> &cong;
         * <b>R</b><sup>6</sup> &times; {1} is invariant under some map 
         * <b>&phi;</b> : <b>P</b><sup>6</sup> &rarr; <b>P</b><sup>6</sup> representing the 
         * dynamics of the system. 
         * </p>
         * <h3>IMPORTANT NOTE</h3>
         * <p>
         * This method is provided to
         * maintain compatibility with the previous use of <code>computeFixedOrbit()</code>
         * presented by the trajectory classes for particles, beam envelopes, etc.  (This method
         * has been deprecated and discontinued.)  The methods
         * responded differently depending upon whether the structure producing the simulation
         * data was from a ring or a linear transport/accelerator structure.  This behavior
         * has now changed, the method produces different results for <em>different simulation
         * types</em> (e.g., particle, transfer map, envelope, etc.) rather than different simulation
         * structures.
         * </p>
         * <p>
         * When the underlying data is produced by a transfer map this method 
         * <em>should return the fixed
         * orbit position</em> at the given state.  
         * When the underlying data is produced by a particle 
         * then the returned value <em>should be the position of the particle</em>
         * at the given state location (for its given initial position).
         * When the underlying data is from a beam envelope then this method <em>should return
         * the centroid location</em> of the beam bunch (for its given initial condition).
         * </p>
         * <p>
         * You must specify the simulation processing engine for each data type to 
         * use a <code>SimResultsAdaptor</code>. To reproduce the behavior of the past 
         * <code>Trajectory#computeFixedOrbit(ProbeState)</code> specify a 
         * <code>{@link CalculationsOnMachines}</code> simulation data processor for ring
         * lattices and a <code>{@link CalculationsOnBeams}</code> simulation processor 
         * for linear lattices.  This configuration is accommodated in the class
         * <code>{@link SimpleSimResultsAdaptor}</code> exposing this interface.
         * </p>
         * 
         * @param   state   simulation state where parameters are computed
         *  
         * @return the reference orbit vector (<i>x,x',y,y',z,z'</i>,1) (see comments)
         */
        public PhaseVector computeFixedOrbit(S state);
        
        /**
         * Compute and return the aberration at the given state location 
         * due to energy spread.  The returned value <b>&Delta;</b> is the vector
         * <br>
         * <br>
         * &nbsp; &nbsp; <b>&Delta;</b> &equiv; 
         * (&Delta;<i>x</i>, &Delta;<i>x'</i>, &Delta;<i>y</i>, &Delta;<i>y'</i>, 0, 0, 1)
         * <br>
         * <br>
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
    
    
    /**
     * <p>
     * Processes simulation data concerned with the beam properties.
     * </p>  
     * <p>
     * For example, when
     * <code>S</code> = <code>xal.model.probe.traj.EnvelopeProbeState</code> then the interface
     * quantities are essentially beam parameters since the beam dynamics figure into the
     * states of <code>xal.model.probe.EnvelopeProbe</code>.
     * </p> 
     *
     * @param <S>     type of the probe state containing local simulation data
     *
     * @author Christopher K. Allen
     * @author Thomas Pelaia
     * @since   2/9/05
     * @version Nov 7, 2013
     */
    public interface ISimEnvResults<S>  extends ISimulationResults {
        

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
         * Calculates the fixed point (closed orbit) in transverse phase space
         * at the given state <i>S<sub>n</sub></i> location <i>s<sub>n</sub></i> in the presence of dispersion.  
         * </p>
         * <p>
         * Let the full-turn map a the state location be denoted <b>&Phi;</b><sub><i>n</i></sub> (or the transfer
         * matrix from entrance to location <i>s<sub>n</sub></i> for a linac).
         * The transverse plane dispersion vector <b>&Delta;</b> is defined  
         * <br>
         * <br> 
         * &nbsp; &nbsp; <b>&Delta;</b><sub><i>t</i></sub> &equiv; -(1/&gamma;<sup>2</sup>)[d<i>x</i>/d<i>z'</i>, d<i>x'</i>/d<i>z'</i>, d<i>y</i>/d<i>z'</i>, d<i>y'</i>/d<i>z'</i>]<sup><i>T</i></sup> .
         * <br>
         * <br>  
         * It can be identified as the first 4 entries of the 6<sup><i>th</i></sup> 
         * column in the transfer matrix <b>&Phi;</b></b><sub><i>n</i></sub>. The above vector
         * quantifies the change in the transverse particle phase 
         * coordinate position versus the change in particle momentum.  
         * The factor -(1/&gamma;<sup>2</sup>) is needed to convert from longitudinal divergence
         * angle <i>z'</i> used by XAL to momentum &delta;<i>p</i> &equiv; &Delta;<i>p</i>/<i>p</i> used in 
         * the dispersion definition.  Specifically,
         * <br>
         * <br>
         * &nbsp; &nbsp; &delta;<i>p</i> &equiv; &Delta;<i>p</i>/<i>p</i> = &gamma;<sup>2</sup><i>z</i>'
         * <br>
         * <br>
         * As such, the above vector can be better described
         * <br>
         * <br> 
         * &nbsp; &nbsp; <b>&Delta;</b><sub><i>t</i></sub> &equiv; [&Delta;<i>x</i>/&delta;<i>p</i>, &Delta;<i>x'</i>/&delta;<i>p</i>, &Delta;<i>y</i>/&delta;<i>p</i>, &Delta;<i>y'</i>/&delta;<i>p</i>]<sup><i>T</i></sup>
         * <br>
         * <br>
         * explicitly describing the change in transverse phase coordinate for fractional
         * change in momentum &delta;<i>p</i>.  
         * </p>
         * <p>
         * Since we are only concerned with transverse phase space coordinates, we restrict ourselves to the 
         * 4&times;4 upper diagonal block of <b>&Phi;</b></b><sub><i>n</i></sub>, which we denote take <b>T</b></b><sub><i>n</i></sub>.  
         * That is, <b>T</b></b><sub><i>n</i></sub> = &pi; &sdot; <b>&Phi;</b></b><sub><i>n</i></sub>
         * where &pi; : <b>R</b><sup>6&times;6</sup> &rarr; <b>R</b><sup>4&times;4</sup> is the
         * projection operator. 
         * </p>
         * <p>
         * This method finds that point <b>z</b><sub><i>t</i></sub> &equiv; 
         * (<i>x<sub>t</sub></i>, <i>x'<sub>t</sub></i>, <i>y<sub>t</sub></i>, <i>y'<sub>t</sub></i>)
         * in transvse phase space that is invariant under the action of the ring for a given momentum spread
         * &delta;<i>p</i>.  That is, the particle ends up
         * in the same location each revolution. With a finite momentum spread of &delta;<i>p</i> &gt; 0
         * we require this require that
         * <br>
         * <br>
         * &nbsp; &nbsp; <b>T</b><sub><i>n</i><b></sub>z</b><sub><i>t</i></sub> + &delta;<i>p</i><b>&Delta;</b><sub><i>t</i></sub> = <b>z</b><sub><i>t</i></sub> ,
         * <br>
         * <br>
         * which can be written
         * <br>
         * <br>
         * &nbsp; <b>z</b><sub><i>t</i></sub> = &delta;<i>p</i>(<b>T</b></b><sub><i>n</i></sub> - <b>I</b>)<sup>-1</sup><b>&Delta;</b><sub><i>t</i></sub> ,
         * <br>
         * <br>
         * where <b>I</b> is the identity matrix.  Dividing both sides by &delta;<i>p</i> yields the final
         * result
         * <br>
         * <br>
         * &nbsp; <b>z</b><sub>0</sub> &equiv; <b>z</b><sub><i>t</i></sub>/&delta;<i>p</i> = (<b>T</b></b><sub><i>n</i></sub> - <b>I</b>)<sup>-1</sup><b>&Delta;</b><sub><i>t</i></sub> ,
         * <br>
         * <br>
         * which is the returned value of this method.  It is normalized by
         * &delta;<i>p</i> so that we can compute the closed orbit for any given momentum spread.
         * </p>
         *   
         * @param state    we are calculating the dispersion at this state location
         * 
         * @return         The closed orbit fixed point <b>z</b><sub>0</sub> for finite 
         *                 dispersion, normalized by momentum spread.
         *                 Returned as an array [<i>x</i><sub>0</sub>,<i>x'</i><sub>0</sub>,<i>y</i><sub>0</sub>,<i>y'</i><sub>0</sub>]/&delta;<i>p</i>
         *
         * @author Christopher K. Allen
         * @since  Nov 8, 2013
         */
        public PhaseVector computeChromDispersion(S state);
        
    }
    
}
