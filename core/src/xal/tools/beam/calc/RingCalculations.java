/**
 * RingCalculations.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 22, 2013
 */
package xal.tools.beam.calc;

import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.TransferMapState;
import xal.model.probe.traj.TransferMapTrajectory;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.math.r3.R3;
import xal.tools.math.r4.R4;

/**
 * <p>
 * Class for computing ring parameters from simulation data.
 * Accepts a <code>TransferMapTrajectory</code> as ring simulation data
 * (from the online model) and computes the ring parameters from the 
 * transfer maps stored around the ring.
 * </p>
 * <p>
 * The method names are those of interfaces <code>ICoordinateState</code>
 * and </code>IPhaseState</code> to reflect their intent.  However, they
 * should be changed to something more descriptive once refactoring
 * is finished.  Preferably a method prefixed with <tt>calculate</tt>
 * since the current naming scheme conflicts with Javabeans, specifically,
 * the <code>get</code> prefix indicates a property of this class where in 
 * actuality it is a computation.
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Oct 22, 2013
 */
public class RingCalculations extends MachineCalculations {
    
    /*
     * Local Attributes
     */
    
    /** The betatron phase advances at the ring entrance position */
    private final R3                        vecPhsAdv;
    
    /** The fixed orbit position at the ring entrance */
    private final PhaseVector               vecFxdPt;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * <p>
     * Constructor for RingCalculations.  Accepts the <code>TransferMapTrajectory</code>
     * object and extracts the final state and one-turn map.  Parameters that are 
     * required for subsequent ring parameter calculations are also computed, such as
     * entrance position phase advance, entrance position fixed orbit, and entrance position
     * matched envelope.
     * </p>
     * <p>
     * The entrance of the ring is assume to be at the first and last states in the 
     * provided trajectory.  For example, the transfer matrix of the last state
     * is the full turn map of the ring at the entrance position. 
     * </p>
     *
     * @param  trjSimFull  the simulation data for the ring, a "transfer map trajectory" object
     * 
     * @throws IllegalArgumentException the trajectory does not contain <code>TransferMapState</code> objects
     *
     * @author Christopher K. Allen
     * @since  Oct 22, 2013
     */
    public RingCalculations(TransferMapTrajectory trjRing) throws IllegalArgumentException {
        super(trjRing);
//        ProbeState  pstFinal = trjRing.finalState();
//        
//        // Check for correct probe types
//        if ( !( pstFinal instanceof TransferMapState) )
//            throw new IllegalArgumentException(
//                    "Trajectory states are not TransferMapStates? - " 
//                    + pstFinal.getClass().getName()
//                    );
        
//        this.trjSimFull   = trjRing;
//        this.staFinal  = (TransferMapState)pstFinal;
//        this.matPhiFull = this.staFinal.getTransferMap().getFirstOrder();
        PhaseMatrix matPhiFull = super.getFullTransferMap().getFirstOrder();
        
        this.vecPhsAdv = super.calculatePhaseAdvPerCell(matPhiFull);
        this.vecFxdPt  = super.calculateFixedPoint(matPhiFull);
    }

    
    /*
     * Ring Attributes at Entrance
     */

//    /**
//     *
//     * @see xal.model.probe.traj.ICoordinateState#getPhaseCoordinates()
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 22, 2013
//     */
//    @Override
//    public PhaseVector getPhaseCoordinates() {
//        return null;
//    }
    
    /**
     * <p>
     * Returns the betatron phase advances for the ring entrance (which are computed 
     * at instantiation).  The returned value is the calculation
     * <code>{@link #calculatePhaseAdvPerCell(PhaseMatrix)}</code> given the
     * full turn matrix at the ring entrance.
     * </p>
     * <p>  
     * <h4>NOTES:</h4>
     * &middot; The ring tunes and betatron phase advances differ by a factor 2&pi;.
     * <br/>
     * &middot; The entrance of the ring is assumed to be the location of the
     * first and last states of the solution trajectory.
     * </p>
     * 
     * @return  vector particle betatron phase advances (in radians)
     *
     * @author Christopher K. Allen
     * @since  Oct 30, 2013
     */
    public R3   ringBetatronPhaseAdvance() {
        return this.vecPhsAdv;
    }
    
    /**
     * <p>
     * Returns the phase space location of the fixed orbit at the ring entrance 
     * (which is computed at instantiation). The returned value <b>z</b> is the result of the
     * calculation <code>{@link #calculateFixedPoint(PhaseMatrix)}</code> given the
     * full turn matrix <b>&Phi;</b> at the ring entrance. It is invariant under 
     * the action of <b>&Phi;</b>, that is, <b>&Phi;z</b> = <b>z</b>. 
     * </p>
     * <p>  
     * <h4>NOTES:</h4>
     * &middot; The entrance of the ring is assumed to be the location of the
     * first and last states of the solution trajectory.
     * </p>
     * 
     * @return
     *
     * @author Christopher K. Allen
     * @since  Oct 30, 2013
     */
    public PhaseVector  ringFixedOrbitPt() {
        return this.vecFxdPt;
    }
    
    /**
     * <p>
     * Returns the matched Courant-Snyder parameters at the entrance of the ring. These
     * are the "envelopes" taken from the "closed envelope" solution at the beginning
     * of the ring.
     * </p>
     * <p>
     * Note that emittance &epsilon; is the parameter used to describe the extend of
     * the actual beam (rather than the normalized size &beta;), or "acceptance".  Thus it
     * cannot be computed here and <code>NaN</code> is returned instead.
     * </p>
     * <p>  
     * <h4>NOTES:</h4>
     * &middot; The entrance of the ring is assumed to be the location of the
     * first and last states of the solution trajectory.
     * </p>
     * 
     * @return  array of Twiss parameter sets (&alpha;, &beta;, NaN)
     *
     * @author Christopher K. Allen
     * @since  Oct 30, 2013
     */
    public Twiss[]  ringMatchedTwiss() {
        return super.getMatchedTwiss();
    }


    /**
     * <p>
     * Calculates and returns the full tune around the ring include the integer portion.
     * The tunes are computed for the start of the ring.
     * The tune for each phase plane is returned in the 3-dimensional vector.
     * </p>
     * <p>
     * The full tunes are computed by summing all the partial phase advances
     * through each of the trajectory states (see 
     * <code>{@link #calculatePhaseAdvance(PhaseMatrix, Twiss[], Twiss[])}</code>).  
     * Thus, the partial phase advance
     * through each state must also be computed so this can be a somewhat
     * expensive operation.
     * </p>
     * 
     * @return  the number <i>n.&nu;</i> for each phase plane where <i>n</i> is the 
     *          integer portion and <i>&nu;</i> is the fractional phase advance.
     *
     * @author Christopher K. Allen
     * @since  Oct 24, 2013
     */
    public R3 calculateFullTurnFullTunes() {

        // Initialize the vector of full tunes
        R3  vecPhsAdv = new R3();
        
        // Initialize the loop
        Twiss[]     arrTwsPrv = super.getMatchedTwiss();
        Twiss[]     arrTwsCur;
        
        PhaseMatrix matXfrPrv = PhaseMatrix.identity();
        PhaseMatrix matXfrCur;
        
        // We sum up the partial phase advance from each trajectory state
        for (ProbeState state : super.getTrajectory()) {
            
            // Compute the full-turn map at this state location
            TransferMapState    tmsCurr = (TransferMapState)state;
            PhaseMatrix         matFull = this.calculateFullLatticeMatrixAt(tmsCurr);
            
            // For this state location, compute the matched twiss parameters and 
            //  the transfer matrix from the previous state to here. 
            arrTwsCur = super.calculateMatchedTwiss(matFull);
            matXfrCur = tmsCurr.getTransferMap().getFirstOrder();
            
            PhaseMatrix matXfrStep = matXfrCur.times(  matXfrPrv.inverse()  );
            
            // Compute the phase advance through this state then add it to the sum
            R3          vecPhsStep = super.calculatePhaseAdvance(matXfrStep, arrTwsPrv, arrTwsCur);

            vecPhsAdv.plusEquals( vecPhsStep );
            
            // Reset the loop
            arrTwsPrv = arrTwsCur;
            matXfrPrv = matXfrCur;
        }
        
        //  Normalize the phase in radians to unitless tunes then return
        vecPhsAdv.timesEquals( 1.0/(2.0*Math.PI) );
        
        return vecPhsAdv;
    }
    
    /**
     * <p>
     * Calculates the fixed point (closed orbit) in transverse phase space
     * at the given state location in the presence of dispersion.  
     * </p>
     * <p>
     * Let the full-turn map a the state location be denoted <b>&Phi;</b>.
     * The transverse plane dispersion vector <b>&Delta;</b> is defined  
     * <br/>
     * <br/> 
     * &nbsp; &nbsp; <b>&Delta;</b><sub><i>t</i></sub> &equiv; -(1/&gamma;<sup>2</sup>)[d<i>x</i>/d<i>z'</i>, d<i>x'</i>/d<i>z'</i>, d<i>y</i>/d<i>z'</i>, d<i>y'</i>/d<i>z'</i>]<sup><i>T</i></sup> .
     * <br/>
     * <br/>  
     * It can be identified as the first 4 entries of the 6<sup><i>th</i></sup> 
     * column in the transfer matrix <b>&Phi;</b>. The above vector
     * quantifies the change in the transverse particle phase 
     * coordinate position versus the change in particle momentum.  
     * The factor -(1/&gamma;<sup>2</sup>) is needed to convert from longitudinal divergence
     * angle <i>z'</i> used by XAL to momentum &delta;<i>p</i> &equiv; &Delta;<i>p</i>/<i>p</i> used in 
     * the dispersion definition.  Specifically,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &delta;<i>p</i> &equiv; &Delta;<i>p</i>/<i>p</i> = &gamma;<sup>2</sup><i>z</i>'
     * <br/>
     * <br/>
     * As such, the above vector can be better described
     * <br/>
     * <br/> 
     * &nbsp; &nbsp; <b>&Delta;</b><sub><i>t</i></sub> &equiv; [&Delta;<i>x</i>/&delta;<i>p</i>, &Delta;<i>x'</i>/&delta;<i>p</i>, &Delta;<i>y</i>/&delta;<i>p</i>, &Delta;<i>y'</i>/&delta;<i>p</i>]<sup><i>T</i></sup>
     * <br/>
     * <br/>
     * explicitly describing the change in transverse phase coordinate for fractional
     * change in momentum &delta;<i>p</i>.  
     * </p>
     * <p>
     * Since we are only concerned with transverse phase space coordinates, we restrict ourselves to the 
     * 4&times;4 upper diagonal block of <b>&Phi;</b>, which we denote take <b>T</b>.  
     * That is, <b>T</b> = &pi; &sdot; <b>&Phi;</b>
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
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <b>Tz</b><sub><i>t</i></sub> + &delta;<i>p</i><b>&Delta;</b><sub><i>t</i></sub> = <b>z</b><sub><i>t</i></sub> ,
     * <br/>
     * <br/>
     * which can be written
     * <br/>
     * <br/>
     * &nbsp; <b>z</b><sub><i>t</i></sub> = &delta;<i>p</i>(<b>T</b> - <b>I</b>)<sup>-1</sup><b>&Delta;</b><sub><i>t</i></sub> ,
     * <br/>
     * <br/>
     * where <b>I</b> is the identity matrix.  Dividing both sides by &delta;<i>p</i> yields the final
     * result
     * <br/>
     * <br/>
     * &nbsp; <b>z</b><sub>0</sub> &equiv; <b>z</b><sub><i>t</i></sub>/&delta;<i>p</i> = (<b>T</b> - <b>I</b>)<sup>-1</sup><b>&Delta;</b><sub><i>t</i></sub> ,
     * <br/>
     * <br/>
     * which is the returned value of this method.  It is normalized by
     * &delta;<i>p</i> so that we can compute the closed orbit for any given momentum spread.
     * </p>
     *   
     * @param matPhi    we are calculating the dispersion of a ring at this state location
     * 
     * @return         The closed orbit fixed point <b>z</b><sub>0</sub> for finite 
     *                 dispersion, normalized by momentum spread.
     *                 Returned as an array [<i>x</i><sub>0</sub>,<i>x'</i><sub>0</sub>,<i>y</i><sub>0</sub>,<i>y'</i><sub>0</sub>]/&delta;<i>p</i>
     *
     * @author Christopher K. Allen
     * @since  Oct 30, 2013
     */
    public R4 calculateDispersion(TransferMapState state) {
        PhaseMatrix     matFullTrn = this.calculateFullLatticeMatrixAt(state);
        double          dblGamma   = state.getGamma();
        
        double[]        arrDisp = super.calculateDispersion(matFullTrn, dblGamma);
        
        return new R4(arrDisp);
    }
    
 
 
}
