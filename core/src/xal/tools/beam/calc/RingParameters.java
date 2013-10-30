/**
 * RingParameters.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 22, 2013
 */
package xal.tools.beam.calc;

import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.TransferMapState;
import xal.model.probe.traj.TransferMapTrajectory;
import xal.tools.beam.PhaseMap;
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
/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Oct 30, 2013
 */
public class RingParameters extends CalcEngine  {
    
    /*
     * Local Attributes
     */
    
    /** The trajectory around one turn of the ring */
    private final TransferMapTrajectory     trjRing;
    
    /** The final transfer map probe state (at the end of the ring) */
    private final TransferMapState          staFinal;

    /** The one turn map (matrix) for the ring */
    private final PhaseMatrix               matPhiRng;

    
    /** The betatron phase advances at the ring entrance position */
    private final R3                        vecPhsAdv;
    
    /** The fixed orbit position at the ring entrance */
    private final PhaseVector               vecFxdPt;
    
    /** The matched beam Twiss parameters at the start of the ring */
    private final Twiss[]                   arrTwsMch;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * <p>
     * Constructor for RingParameters.  Accepts the <code>TransferMapTrajectory</code>
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
     * @param  trjRing  the simulation data for the ring, a "transfer map trajectory" object
     *
     * @author Christopher K. Allen
     * @since  Oct 22, 2013
     */
    public RingParameters(TransferMapTrajectory trjRing) {
        ProbeState  pstFinal = trjRing.finalState();
        
        // Check for correct probe types
        if ( !( pstFinal instanceof TransferMapState) )
            throw new IllegalArgumentException(
                    "Trajectory states are not TransferMapStates? - " 
                    + pstFinal.getClass().getName()
                    );
        
        this.trjRing   = trjRing;
        this.staFinal  = (TransferMapState)pstFinal;
        this.matPhiRng = this.staFinal.getTransferMap().getFirstOrder();
        
        this.vecPhsAdv = super.calculatePhaseAdvPerCell(this.matPhiRng);
        this.vecFxdPt  = super.calculateFixedPoint(this.matPhiRng);
        this.arrTwsMch = super.calculateMatchedTwiss(this.matPhiRng); 
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
//        // TODO Auto-generated method stub
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
    public R3   entranceBetatronPhaseAdvance() {
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
    public PhaseVector  entranceFixedOrbitPt() {
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
    public Twiss[]  entranceMatchedTwiss() {
        return this.arrTwsMch;
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
        Twiss[]     twsMchPrv = this.arrTwsMch;
        Twiss[]     twsMchCur;
        
        PhaseMatrix matXfrPrv = PhaseMatrix.identity();
        PhaseMatrix matXfrCur;
        
        // We sum up the partial phase advance from each trajectory state
        for (ProbeState state : this.trjRing) {
            
            // Compute the full-turn map at this state location
            TransferMapState    tmsCurr = (TransferMapState)state;
            PhaseMatrix         matFull = this.calculateFullTurnMatrixAt(tmsCurr);
            
            // For this state location, compute the matched twiss parameters and 
            //  the transfer matrix from the previous state to here. 
            twsMchCur = super.calculateMatchedTwiss(matFull);
            matXfrCur = tmsCurr.getTransferMap().getFirstOrder();
            
            PhaseMatrix matXfrStep = matXfrCur.times(  matXfrPrv.inverse()  );
            
            // Compute the phase advance through this state then add it to the sum
            R3          vecPhsStep = super.calculatePhaseAdvance(matXfrStep, twsMchPrv, twsMchCur);

            vecPhsAdv.plusEquals( vecPhsStep );
            
            // Reset the loop
            twsMchPrv = twsMchCur;
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
        PhaseMatrix     matFullTrn = this.calculateFullTurnMatrixAt(state);
        double          dblGamma   = state.getGamma();
        
        double[]        arrDisp = super.calculateDispersion(matFullTrn, dblGamma);
        
        return new R4(arrDisp);
    }
    
    
//    /**
//     * Calculate the x, y and z tunes
//     * <br/>
//     * This calculates the tune of the one-turn map, that is, the
//     * tune of the entire ring.
//     * 
//     * @deprecated  replaced by calculateTunePerCell(PhaseMatrix)
//     */
//    //sako version look at the sign of M12, and determine the phase
//    // since M12=beta*sin(mu) and beta>0, if M12>0, sin(mu)>0, 0<mu<pi
//    //                                    if M12<0, sin(mu)<0, -pi<mu<0
//    // sako
//    private void calculateTunesIfNeeded() {
//        if ( _needsTuneCalculation ) {
//            final double PI2 = 2 * Math.PI;
//            final PhaseMatrix matrix = _originFullTurnMap.getFirstOrder();
//            
//            for ( int mode = 0 ; mode < NUM_MODES ; mode++ ) {
//                final int index = 2 * mode;
//                double trace = matrix.getElem( index, index ) + matrix.getElem( index + 1, index + 1 );
//
//                double m12   = matrix.getElem( index, index+1 );                               
//                double mu    = Math.acos( trace / 2 );
//                // problem is when abs(trace)>1, then _tunes are Double.NaN
//                if (m12<0) {
//                    mu *= (-1);
//                }
//                _tunes[mode] = mu / PI2;            
//            }
//            _needsTuneCalculation = false;
//        }
//    }

    
    

    
    /*
     * IPhaseState Interface 
     */

    /**
     * <p>
     * Returns the array of twiss objects for this state for all three planes.
     * </p>
     * <p>
     * These are the closed orbit Twiss parameters at this state location representing
     * the matched beam envelope around the ring.
     * </p>
     * <p>
     * Calculates the matched Courant-Snyder parameters for the given
     * period cell transfer matrix and phase advances.  When the given transfer matrix
     * is the full-turn matrix for a ring the computed Twiss parameters are the matched
     * envelopes for the ring at that point.
     * </p>
     * <p>
     * Let <b>&Phi;</b> denote the transfer matrix from the ring beginning to the given
     * state location. It is assumed to be the transfer matrix through
     * at least one cell in a periodic lattice.  Internally, the array of phase advances 
     * {&sigma;<sub><i>x</i></sub>, &sigma;<sub><i>y</i></sub>, &sigma;<sub><i>x</i></sub>}
     * are assumed to be the particle phase advances through the cell for the matched 
     * solution.   These are computed with the method 
     * <code>{@link #calculatePhaseAdvPerCell(PhaseMatrix)}</code>.
     * </p> 
     * <p>
     * The returned Courant-Snyder parameters (&alpha;, &beta;, &epsilon;) are invariant
     * under the action of the given phase matrix, that is, they are matched.  All that 
     * is require are &alpha; and &beta; since &epsilon; specifies the size of the beam
     * envelope.  Consequently the returned &epsilon; is <code>NaN</code>.
     * </p>
     * The following are the calculations used for the Courant-Snyder parameters of a 
     * single phase plane:
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &alpha; &equiv; -<i>ww'</i> = (&phi;<sub>11</sub> - &phi;<sub>22</sub>)/(2 sin &sigma;) ,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &beta; &equiv; <i>w</i><sup>2</sup> = &phi;<sub>12</sub>/sin &sigma;
     * <br/>
     * <br/>
     * where &phi;<sub><i>ij</i></sub> are the elements of the 2&times;2 diagonal blocks of
     * <b>&Phi;</b> corresponding the the particular phase plane, the function <i>w</i>
     * is taken from Reiser, and &sigma; is the phase advance through the cell for the 
     * particular phase plance.
     * </p>
     * 
     * @param   state   state containing transfer map and location used in these calculations
     * 
     * @return array (twiss-H, twiss-V, twiss-L)
     *
     * @see xal.model.probe.traj.IPhaseState#getTwiss()
     *
     * @author Christopher K. Allen
     * @since  Aug 14, 2013
     */
    public Twiss[] getTwiss(TransferMapState state) {
        PhaseMatrix matFullTrn = this.calculateFullTurnMatrixAt(state);
        Twiss[]     arrTwsMtch = super.calculateMatchedTwiss(matFullTrn);
        
        return arrTwsMtch;
    }


    /**
     * <p>
     * This is the phase advance for the given state location.
     * </p>
     * <p>
     * Compute and return the particle phase advance from the ring beginning
     * to the given state location.
     * </p>
     * <p>
     * Internally the method calculates the phase advances given the initial and final 
     * Courant-Snyder &alpha; and &beta; values for the matched beam at the ring
     * beginning and the location of the given state, respectively. 
     * Let <b>&Phi;</b> represent the transfer matrix from the initial ring location to
     * the given state location.  The computed quantity is the general
     * phase advance of the particle through the transfer matrix <b>&Phi;</b>, and no special
     * requirements are placed upon <b>&Phi;</b> (e.g., periodicity).  One phase
     * advance is provided for each phase plane, i.e., 
     * (&sigma;<sub><i>x</i></sub>, &sigma;<sub><i>z</i></sub>, &sigma;<sub><i>z</i></sub>).  
     * </p>
     * <p>
     * The definition of phase advance &sigma; is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &sigma;(<i>s</i>) &equiv; &int;<sup><i>s</i></sup> [1/&beta;(<i>t</i>)]<i>dt</i> ,
     * <br/>
     * <br/>
     * where &beta;(<i>s</i>) is the Courant-Snyder, envelope function, and the integral 
     * is taken along the interval between the initial and final Courant-Snyder 
     * parameters.
     * </p>
     * <p>
     * The basic relation used to compute &sigma; is the following:
     * <br/>
     * <br/>
     * &nbsp; &nbsp;  &sigma; = sin<sup>-1</sup> &phi;<sub>12</sub>/(&beta;<sub>1</sub>&beta;<sub>2</sub>)<sup>&frac12;</sup> ,
     * <br/>
     * <br/>
     * where &phi;<sub>12</sub> is the element of <b>&Phi;</b> in the upper right corner of each 
     * 2&times;2 diagonal block, &beta;<sub>1</sub> is the initial beta function value (provided)
     * and &beta;<sub>2</sub> is the final beta function value (provided).
     * </p>
     * 
     * @param   state   state containing transfer map and location used in these calculations
     * 
     * @see calculatePhaseAdvance(PhaseMatrix, Twiss[], Twiss[])
     *
     * @see xal.model.probe.traj.IPhaseState#getBetatronPhase()
     *
     * @author Christopher K. Allen
     * @since  Aug 14, 2013
     */
    public R3 getBetatronPhase(TransferMapState state) {
        PhaseMatrix matFullTrn = this.calculateFullTurnMatrixAt(state);
        Twiss[]     arrTwsLoc  = super.calculateMatchedTwiss(matFullTrn);

        PhaseMatrix matPhiLoc  = state.getTransferMap().getFirstOrder();
        R3          vecPhsAdv  = super.calculatePhaseAdvance(matPhiLoc, this.arrTwsMch, arrTwsLoc);
        
        return vecPhsAdv;
    }


    /*
     * ICoordinateState Interface
     */

//    /**
//     *
//     * @see xal.model.probe.traj.ICoordinateState#getPhaseCoordinates()
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 22, 2013
//     * 
//     * @deprecated  This needs to be refactored, renamed, commented, and put into context 
//     */
//    @Override
//    public PhaseVector getPhaseCoordinates() {
//        // TODO Auto-generated method stub
//        return null;
//    }

    /**
     * <p>
     * Get the fixed point at this state location about which betatron oscillations occur.
     * </p>
     * <p> 
     * Calculate the fixed point solution vector representing the closed orbit at the 
     * location of this element.
     * We first attempt to find the fixed point for the full six phase space coordinates.
     * Let <b>&Phi;</b> denote the one-turn map for a ring.  The fixed point 
     * <b>z</b> &in; <b>R</b><sup>6</sup>&times;{1} in homogeneous phase space coordinates
     * is that which is invariant under <b>&Phi;</b>, that is,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <b>&Phi;z</b> = <b>z</b> .
     * <br/>
     * <br/> 
     * This method returns that vector <b>z</b>.  
     * </p>
     * <p>
     * Recall that the <i>homogeneous</i> transfer matrix <b>&Phi;</b> for the ring 
     * has final row that represents the translation <b>&Delta;</b> of the particle
     * for the circuit around the ring.  The 6&times;6 sub-matrix of <b>&Phi;</b> represents
     * the (linear) action of the bending magnetics and quadrupoles and corresponds to the
     * matrix <b>T</b> &in; <b>R</b><sup>6&times;</sup> (here <b>T</b> is linear). 
     * Thus, we can write the linear operator <b>&Phi;</b>
     * as the augmented system 
     * <br/>
     * <br/>
     * <pre>
     * &nbsp; &nbsp; <b>&Phi;</b> = |<b>T</b> <b>&Delta;</b> |,   <b>z</b> &equiv; |<b>p</b>| ,
     *         |<b>0</b> 1 |        |1|
     * </pre> 
     * where <b>p</b> is the projection of <b>z</b> onto the ambient phase space
     * <b>R</b><sup>6</sup> (without homogeneous the homogeneous coordinate).
     * coordinates). 
     * </p>
     * <p>
     * Putting this together we get
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <b>&Phi;z</b> = <b>Tp</b> + <b>&Delta;</b> = <b>p</b> , 
     * <br/>
     * <br/>
     * to which the solution is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <b>p</b> = -(<b>T</b> - <b>I</b>)<sup>-1</sup><b>&Delta;</b>
     * <br/>
     * <br/>
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
     * <b>T</b> = proj<sub>4&times;4</sub> <b>&Phi;</b>.  The returned value is then
     * <b>z</b> = (<b>p</b> 0 0 1)<sup><i>T</i></sup>.
     * 
     * @param   state   state containing transfer map and location used in these calculations
     * 
     * @return              fixed point solution  (<i>x,x',y,y',z,z'</i>,1) for the given phase matrix
     *
     * @see xal.model.probe.traj.ICoordinateState#getFixedOrbit()
     *
     * @author Christopher K. Allen
     * @since  Oct 25, 2013
     */
    public PhaseVector getFixedOrbit(TransferMapState state) {
        PhaseMatrix matFullTrn = this.calculateFullTurnMatrixAt(state);
        PhaseVector vecFixedPt = super.calculateFixedPoint(matFullTrn);
        
        return vecFixedPt; 
    }

    //    /**
    //     * Calculate the x, y and z tunes
    //     * <br/>
    //     * This calculates the tune of the one-turn map, that is, the
    //     * tune of the entire ring.
    //     * 
    //     * @deprecated  replaced by calculateTunePerCell(PhaseMatrix)
    //     */
    //    //sako version look at the sign of M12, and determine the phase
    //    // since M12=beta*sin(mu) and beta>0, if M12>0, sin(mu)>0, 0<mu<pi
    //    //                                    if M12<0, sin(mu)<0, -pi<mu<0
    //    // sako
    //    private void calculateTunesIfNeeded() {
    //        if ( _needsTuneCalculation ) {
    //            final double PI2 = 2 * Math.PI;
    //            final PhaseMatrix matrix = _originFullTurnMap.getFirstOrder();
    //            
    //            for ( int mode = 0 ; mode < NUM_MODES ; mode++ ) {
    //                final int index = 2 * mode;
    //                double trace = matrix.getElem( index, index ) + matrix.getElem( index + 1, index + 1 );
    //
    //                double m12   = matrix.getElem( index, index+1 );                               
    //                double mu    = Math.acos( trace / 2 );
    //                // problem is when abs(trace)>1, then _tunes are Double.NaN
    //                if (m12<0) {
    //                    mu *= (-1);
    //                }
    //                _tunes[mode] = mu / PI2;            
    //            }
    //            _needsTuneCalculation = false;
    //        }
    //    }
    
        
    /*
     * Support Methods
     */
    /**
     * <p>
     * Calculates and returns the full turn matrix for the ring at the
     * given state location.  Let <i>S<sub>n</sub></i> be the given state object at
     * location <i>s<sub>n</sub></i>, and let <b>T</b><sub><i>n</i></sub> be the
     * transfer matrix between locations <i>s</i><sub>0</sub> and <i>s<sub>n</sub></i> ,
     * where <i>s</i><sub>0</sub> is the location of the full turn matrix 
     * <b>&Phi;</b><sub>0</sub> for this ring.  Then the full turn matrix 
     * <b>&Phi;</b><sub><i>n</i></sub> for the ring at location <i>s<sub>n</sub></i>
     * is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <b>&Phi;</b><sub><i>n</i></sub> = <b>T</b><sub><i>n</i></sub> &sdot; <b>&Phi;</b><sub>0</sub>
     *               &sdot; <b>T</b><sub><i>n</i></sub><sup>-1</sup> .
     * <br/>
     * <br/>
     * That is, we conjugate the full-turn map for this ring by the transfer map 
     * for the given state.
     * </p> 
     * 
     * @param state     state object <i>S<sub>n</sub></i> for location <i>s<sub>n</sub></i>
     *                  containing transfer matrix <b>T</b><sub><i>n</i></sub>
     *                  
     * @return          the full-turn map <b>&Phi;</b><sub><i>n</i></sub> at the location
     *                  <i>s<sub>n</sub></i> of the given state
     *
     * @author Christopher K. Allen
     * @since  Oct 28, 2013
     */
    private PhaseMatrix calculateFullTurnMatrixAt(TransferMapState state) {
        PhaseMap    mapPhiState = state.getTransferMap();
        PhaseMatrix matPhiState = mapPhiState.getFirstOrder();
        PhaseMatrix matPhiStInv = matPhiState.inverse();

        PhaseMatrix matFullTnLoc;
        matFullTnLoc = this.matPhiRng.times(matPhiStInv);
        matFullTnLoc = matPhiState.times(matFullTnLoc);

        return matFullTnLoc;
    }

}
