/**
 * CalculationsOnParticles.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 14, 2013
 */
package xal.tools.beam.calc;

import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.calc.ISimulationResults.ISimLocResults;
import xal.tools.math.r3.R3;
import xal.tools.math.r6.R6;

/**
 * <p>
 * Provides processing functions appropriate for single particle simulation data.
 * </p>
 * <p>
 * Additional class methods provide access to computed quantities used by the
 * class for the above processing, such as the "periodic fixed orbit", "periodic
 * betatron phase advance", and "period matched Twiss parameter".  These parameters
 * make not make sense here, as we make the very broad assumption that the particle
 * is in a periodic system and the simulation data is taken about <i>n</i> periods,
 * but they are available.  
 * </p>    
 *
 * @author Christopher K. Allen
 * @since  Nov 14, 2013
 * @version Sep 25, 2014
 */
public class CalculationsOnParticles extends CalculationEngine implements ISimLocResults<ParticleProbeState> {

    /*
     * Local Attributes
     */
    
    /** The trajectory around one turn of the ring */
    private final Trajectory<ParticleProbeState>        trjSimul;
    
    /** The final envelope probe state (at the end of the simulation ) */
    private final ParticleProbeState        staFinal;

    /** The response matrix for the linac (between initial state and final state) */
    private final PhaseMatrix               matResp;

    
    /** The betatron phase advances at the at the exit of the linac */
    private final R3                        vecPhsAdv;
    
    /** The fixed orbit position at the entrance of the Linac */
    private final PhaseVector               vecFxdPt;
    
    /** The matched beam Twiss parameters at the start of the ring */
    private final Twiss[]                   arrTwsMch;

    
    /**
     * Constructor for <code>CalculationsOnParticles</code>. Creates a new 
     * <code>CalculationsOnParticles</code> object for process the simulation
     * data contained in the given particle trajectory object.
     *
     * @param   datSim     simulation data for a particle
     *
     * @author Christopher K. Allen
     * @since  Nov 14, 2013
     */
    
    public CalculationsOnParticles(Trajectory<ParticleProbeState> datSim) {
        ParticleProbeState  pstFinal = datSim.finalState();

        this.trjSimul  = datSim;
        this.staFinal  = pstFinal;
        this.matResp   = this.staFinal.getResponseMatrix();
        
        this.vecPhsAdv = super.calculatePhaseAdvPerCell(this.matResp);
        this.vecFxdPt  = super.calculateFixedPoint(this.matResp);
        this.arrTwsMch = super.calculateMatchedTwiss(this.matResp); 
	}

    
    /*
     * Attributes and Properties
     */


	/**
     * Returns the simulation trajectory from which all the machine properties are
     * computed.
     * 
     * @return      simulation trajectory from which this object was initialized
     *
     * @author Christopher K. Allen
     * @since  Nov 7, 2013
     */
    public Trajectory<ParticleProbeState>   getTrajectory() {
        return this.trjSimul;
    }
    
    /**
     * Returns the full response matrix <b>&Phi;</b> providing the sensitivity of the final particle 
     * position <b>z</b> to the initial conditions <b>z</b><sub>0</sub>. 
     * 
     * @return  the response matrix <b>&Phi;</b> = &part;<b>z</b>/&part;<b>z</b><sub>0</sub>
     *
     * @author Christopher K. Allen
     * @since  Nov 7, 2013
     */
    public PhaseMatrix  getFullResponseMatrix() {
        return this.matResp;
    }
    
    /**
     * <p>
     * Returns the betatron phase advances from the simulation beginning to end
     * (which are computed at instantiation).  The results are value for trajectories
     * near the simulation trajectory associated with this computation engine.  The fact
     * being from the use of the response matrix in the calculations.  
     * </p>
     * <p>
     * The returned value is the calculation
     * <code>{@link #calculatePhaseAdvPerCell(PhaseMatrix)}</code> of the super class,
     * and thus assumes simulation trajectory is that for at least one period of 
     * a periodic structure.
     * </p>
     * <h3>NOTES:</h3>
     * <p>  
     * &middot; The betatron phase advances are given in the range [0,2&pi];.
     * <br>
     * </p>
     * 
     * @return  vector particle betatron phase advances (in radians)
     *
     * @author Christopher K. Allen
     * @since  Oct 30, 2013
     */
    public R3   periodBetatronPhaseAdvance() {
        return this.vecPhsAdv;
    }
    
    /**
     * <p>
     * Returns the phase space location of the fixed point at the simulation start 
     * (which is computed at instantiation) assuming the simulation is that for
     * at least one period of a periodic accelerating or transport section.   That is,
     * we assume the beamline is periodic and the returned value is an invariant particle
     * position under the action of the response matrix.
     * </p>
     * <p>
     * The returned value <b>z</b> is the result of the
     * calculation <code>{@link #calculateFixedPoint(PhaseMatrix)}</code> given the
     * full response matrix <b>&Phi;</b> at the simulation exit (see {@link #getFullResponseMatrix()}). 
     * It is invariant under 
     * the action of <b>&Phi;</b>, that is, <b>&Phi;z</b> = <b>z</b>. 
     * </p>
     * <h3>NOTES:</h3>
     * <p>  
     * &middot; The entrance and exit of the beamline should have the same fixed point
     * value.
     * </p>
     * 
     * @return
     *
     * @author Christopher K. Allen
     * @since  Oct 30, 2013
     */
    public PhaseVector  periodFixedOrbitPt() {
        return this.vecFxdPt;
    }
    
    /**
     * <p>
     * Returns the matched Courant-Snyder parameters at the entrance of the simulation
     * assuming the simulation is at least one period of a periodic structure. These
     * are the "envelopes" taken from the "closed envelope" solution under the assume
     * the linac is a periodic transport.
     * </p>
     * <p>
     * Note that emittance &epsilon; is the parameter used to describe the extend of
     * the actual beam (rather than the normalized size &beta;), or "acceptance".  Thus it
     * cannot be computed here and <code>NaN</code> is returned instead.
     * </p>
     * <h3>NOTES:</h3>
     * <p>  
     * &middot; The entrance of the simulation is assumed to be the location of the
     * first and last states of the solution trajectory.
     * </p>
     * 
     * @return  array of Twiss parameter sets (&alpha;, &beta;, NaN)
     *
     * @author Christopher K. Allen
     * @since  Oct 30, 2013
     */
    public Twiss[]  periodMatchedTwiss() {
        return this.arrTwsMch;
    }


    /*
     * ISumLocationResults<ParticleProbeState> Interface
     */

    /**
     * Simply returns the location of the simulated particle at the location of
     * the give state.  No real computation necessary.
     *
     * @see xal.tools.beam.calc.ISimEnvResults#computeCoordinatePosition(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 14, 2013
     */
    @Override
    public PhaseVector computeCoordinatePosition(ParticleProbeState state) {
        PhaseVector     vecPos = state.getPhaseCoordinates();
        
        return vecPos;
    }

    /**
     * <h3>IMPORTANT NOTE</h3>
     * <p>
     * This method has been modified so it returns exactly the same value as 
     * {@link #computeCoordinatePosition(ParticleProbeState)}.  This modification is
     * maintain compatibility with the previous use of <code>computeFixedOrbit()</code>
     * presented by the trajectory classes for particles, beam envelopes, etc.  They
     * responded differently depending upon whether the structure producing the simulation
     * data was from a ring or a linear transport/accelerator structure.
     * <br>
     * <br>
     * Thus, <em>ignore all commenting below!</em>
     * </p>
     * 
     * <p>
     * Consider first the point in phase space that is invariant under repeated application
     * of the response matrix <b>&Phi;</b> for the entire beamline or ring.  This is under 
     * the condition that we decompose <b>&Phi;</b> into its homogeneous and non-homogeneous
     * components.  A particle entering the linac at that location exits at the same location.
     * </p> 
     * <p>
     * To compute this linac fixed point, recall that the <i>homogeneous</i> response 
     * matrix <b>&Phi;</b> for the beamline (or full-turn matrix for a ring)
     * has final row that represents the translation <b>&Delta;</b> of the particle
     * under the action of <b>&Phi;</b>.  The 6&times;6 sub-matrix of <b>&Phi;</b> represents
     * the (linear) action of the bending magnetics and quadrupoles and corresponds to the
     * matrix <b>T</b> &in; <b>R</b><sup>6&times;6</sup> (here <b>T</b> is linear). 
     * Thus, we can write the linear operator <b>&Phi;</b>
     * as the augmented system 
     * <br>
     * <br>
     * <pre>
     * &nbsp; &nbsp; <b>&Phi;</b> = |<b>T</b> <b>&Delta;</b> |,   <b>z</b> &equiv; |<b>p</b>| ,
     *         |<b>0</b> 1 |        |1|
     * </pre> 
     * where <b>p</b> is the projection of <b>z</b> into the embedded phase space
     * <b>R</b><sup>6</sup> (without homogeneous coordinate).
     * coordinates). 
     * </p>
     * <p>
     * Putting this together we get
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>&Phi;z</b> = <b>Tp</b> + <b>&Delta;</b> = <b>p</b> , 
     * <br>
     * <br>
     * to which the solution is
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>p</b> = -(<b>T</b> - <b>I</b>)<sup>-1</sup><b>&Delta;</b>
     * <br>
     * <br>
     * assuming it exists.  The question of solution existence falls upon the
     * resolvent <b>R</b> &equiv; (<b>T</b> - <b>I</b>)<sup>-1</sup> of <b>T</b>.
     * By inspection we can see that <b>p</b> is defined so long as the eigenvalues
     * of <b>T</b> are located away from 1.
     * In this case the returned value is the augmented vector 
     * (<b>p</b> 1)<sup><i>T</i></sup> &in; <b>R</b><sup>6</sup> &times; {1}.
     * </p>
     * <p>
     * When the set of eigenvectors does contain 1, we attempt to find the solution for the
     * transverse phase space.  That is, we take vector <b>p</b> &in; <b>R</b><sup>4</sup>
     * and <b>T</b> &in; <b>R</b><sup>4&times;4</sup> where 
     * <b>T</b> = proj<sub>4&times;4</sub> <b>&Phi;</b>.  The solution value is then
     * <b>z</b> = (<b>p</b> 0 0 1)<sup><i>T</i></sup>.
     * </p>
     * <p>
     * Once we have the fixed point <b>z</b><sub>0</sub> for the linac we compute the trajectory
     * of the fixed point at the location of the given probe state.  To do so, we multiply
     * <b>z</b><sub>0</sub> by the response matrix <b>&Phi;</b><sub><i>n</i></sub> for the given
     * probe state.  That is, we propagate the fixed point of the linac from the linac entrance
     * to the location of the given phase state.
     * </p>
     *
     * @return  The quantity <b>&Phi;</b><sub><i>n</i></sub>&sdot;<b>z</b><sub>0</sub>, the linac 
     *          fixed point <b>z</b><sub>0</sub> propagated to the state location <i>s<sub>n</sub></i>
     *
     * @see xal.tools.beam.calc.ISimEnvResults#computeFixedOrbit(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 14, 2013
     */
    @Override
    public PhaseVector computeFixedOrbit(ParticleProbeState state) {
//        PhaseMatrix matRespLoc = state.getResponseMatrix();
//        PhaseVector vecFxdLoc  = matRespLoc.times( this.vecFxdPt );
        
        PhaseVector vecFxdOrb = this.computeCoordinatePosition(state);
        
        return vecFxdOrb; 
    }

    /**
     * Computes the chromatic aberration for one pass of the particle around the ring 
     * starting at the given state location, 
     * or for a beamline the entrance of the line to this state position. The returned 
     * vector is the displacement from the closed orbit caused 
     * by a unit momentum offset (&delta;<i>p</i> = 1).  See the documentation in 
     * {@link ISimLocResults#computeChromAberration(ProbeState)} for a more detailed
     * exposition.
     *
     * @see xal.tools.beam.calc.ISimLocResults#computeChromAberration(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 14, 2013
     */
    @Override
    public PhaseVector computeChromAberration(ParticleProbeState state) {
        double      dblGamma = state.getGamma();
        PhaseMatrix matResp  = state.getResponseMatrix();
        R6          vecDel   = super.calculateAberration(matResp, dblGamma);
        
        return PhaseVector.embed(vecDel);
    }
    
    
    /*
     * Support Methods
     */
    
//    /**
//     * This method computes Courant-Snyder parameters for the given particle probe state
//     * object.  We do so by considering the particle 
//     * @param state
//     * @param iw
//     * @param iwp
//     * @return
//     *
//     * @author Christopher K. Allen
//     * @since  Nov 14, 2013
//     */
//    private Twiss  computeFakeTwiss(ParticleProbeState state, IND iw, IND iwp) {
//        
//        // Get the amplitude function values for this state
//        //  (We assume the particle is at the maximum orbit.)
//        PhaseVector vecInit  = this.staInit.getPhaseCoordinates();
//
//        double      w  = vecInit.getElem(iw);
//        double      wp = vecInit.getElem(iwp);
//
//        double      dblBeta = w*w;
//        double      dblAlpha = - w*wp;
//        double      dblEmitt = 1.0;
//        Twiss       twsInit  = new Twiss(dblAlpha, dblBeta, dblEmitt);
//        
//        return twsInit;
//    }

}
