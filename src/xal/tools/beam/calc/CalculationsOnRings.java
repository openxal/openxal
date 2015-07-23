/**
 * CalculationsOnRings.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 22, 2013
 */
package xal.tools.beam.calc;

import xal.model.probe.traj.Trajectory;
import xal.model.probe.traj.TransferMapState;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss3D;
import xal.tools.math.r3.R3;

/**
 * <p>
 * Class for computing ring parameters from simulation data.
 * Accepts a <code>TransferMapTrajectory</code> as ring simulation data
 * (from the online model) and computes the ring parameters from the 
 * transfer maps stored around the ring.  Thus, the trajectory provided 
 * <b>must</b> be the end-to-end simulation results of the ring.  The entrance
 * of the first state in the trajectory and the exit of the last state in the
 * trajectory will be treated as the same point, <i>s</i> = 0.
 * </p>
 * Do not supply partial simulations!  Use the trajectory for the full ring
 * unless it is your intent to create a sub-ring.  That is, do not use the start element
 * and stop element features of the online model unless you wish to simulate
 * a smaller ring that excludes all elements before the stop element and all 
 * elements after the stop element.  In such a case the entrance of the start
 * element and the exit of the stop element would close the ring and be given
 * the point <i>s</i> = 0.
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
 * <p>
 * Do not hesitate to request new features and computations that could be provided
 * by this class, it is by no means complete.
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Oct 22, 2013
 */
public class CalculationsOnRings extends CalculationsOnMachines {
    
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
     * Constructor for CalculationsOnRings. The the ring is closed by identifying 
     * the first and last states in the provided trajectory.
     * </p>
     * <p>  
     * Parameters that are required for subsequent ring parameter calculations are 
     * computed, such as "entrance" position phase advance, "entrance" position 
     * fixed orbit, and "entrance" position matched envelope. By entrance position
     * we imply the location <i>s</i> = 0, the location of ring closure.  Once these
     * quantities are computed we can propagate them to other ring locations as needed.
     * </p>
     * <p>
     * For example, the transfer map of the last state in the trajectory
     * is the full turn map of the ring at the entrance position.  By conjugation
     * with transfer map of any other state we can form the transfer map at that
     * state location.
     * </p>
     *
     * @param  trjSimFull  the simulation data for <em>entire</em> ring, a "transfer map trajectory" object
     * 
     * @throws IllegalArgumentException the trajectory does not contain <code>TransferMapState</code> objects
     *
     * @author Christopher K. Allen
     * @since  Oct 22, 2013
     */
    
    public CalculationsOnRings(Trajectory<TransferMapState> datSim) {
        super(datSim);
//      ProbeState  pstFinal = trjRing.finalState();
//      
//      // Check for correct probe types
//      if ( !( pstFinal instanceof TransferMapState) )
//          throw new IllegalArgumentException(
//                  "Trajectory states are not TransferMapStates? - " 
//                  + pstFinal.getClass().getName()
//                  );
      

//      this.trjSimFull   = trjRing;
//      this.staFinal  = (TransferMapState)pstFinal;
//      this.matPhiFull = this.staFinal.getTransferMap().getFirstOrder();
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
     * <h3>NOTES:</h3>
     * <p>  
     * &middot; The ring tunes and betatron phase advances differ by a factor 2&pi;.
     * <br>
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
     * <h3>NOTES:</h3>
     * <p>  
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
     * Returns the matched Courant-Snyder parameters at the "entrance" of the ring. These
     * are the envelopes taken from the "closed envelope" solution at the beginning
     * of the ring, <i>s</i> = 0.
     * </p>
     * <p>
     * Note that emittance &epsilon; is the parameter used to describe the extend of
     * the actual beam (rather than the normalized size &beta;), or "acceptance".  Thus it
     * cannot be computed here and <code>NaN</code> is returned instead.
     * </p>
     * <h3>NOTES:</h3>
     * <p>  
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
     * Returns the one-turn map <b>&Phi;</b><sub>0</sub> for the ring at the location
     * <i>s</i> = 0.
     *  
     * @return      ring one-turn map at join location
     *
     * @author Christopher K. Allen
     * @since  Nov 4, 2014
     */
    public PhaseMap  getOneTurnMap() {
        return super.getFullTransferMap();
    }
    
    /**
     * Computes the phase advances between the given state locations for each phase plane.
     * This is done by computing the matched Twiss parameters at the two locations, along
     * with the transfer matrix, then computing the phase advances &sigma; using is
     * its entries in the transfer matrix.  
     * 
     * @param state1    state defining the start location in the ring
     * @param state2    state defining the stop location in the ring
     * 
     * @return      phases advances (&sigma;<sub><i>x</i></sub>, &sigma;<sub><i>y</i></sub>, &sigma;<sub><i>z</i></sub>) 
     *
     * @author Christopher K. Allen
     * @since  Nov 7, 2014
     */
    public R3   computePhaseAdvanceBetween(TransferMapState state1, TransferMapState state2) {
        Twiss[]     arrTws1 = this.computeMatchedTwissAt(state1);
        Twiss[]     arrTws2 = this.computeMatchedTwissAt(state2);
        PhaseMatrix matPhi  = computeTransferMatrix(state1, state2);
                
        R3      vecPhsAdv = this.calculatePhaseAdvance(matPhi, arrTws1, arrTws2);
        
        return vecPhsAdv;
    }

    /**
     * <p>
     * Calculates the fractional phase tune for the ring from its one-turn matrix.
     * The Courant-Snyder parameters 
     * of the machine (beam) must be invariant under the action of the one-turn matrix 
     * (this indicates a periodic focusing structure 
     * where the beam envelope is modulated by that structure) for the returned values
     * to be accurate.  One tune parameter
     * is provided for each phase plane, i.e., 
     * (&nu;<sub><i>x</i></sub>, &nu;<sub><i>y</i></sub>, &nu;<sub><i>z</i></sub>).  
     * The betatron phase advances for the ring are then given by 
     * (2&pi;&nu;<sub><i>x</i></sub>, 2&pi;&nu;<sub><i>y</i></sub>, 2&pi;&nu;<sub><i>z</i></sub>).  
     * Specifically, the above values are the sinusoidal phase that a particle
     * advances after each completion of a ring traversal, modulo 2&pi; 
     * (that is, we only take the fractional part).
     * </p>
     * <p>
     * The basic computation is 
     * <br>
     * <br>
     * &nbsp; &nbsp;  &nu; = (1/2&pi;) cos<sup>-1</sup>[&frac12; Tr <b>&Phi;</b><sub>&alpha;&alpha;</sub>] ,
     * <br>
     * <br>
     * where <b>&Phi;</b></b><sub>&alpha;&alpha;</sub> is the 2&times;2 block diagonal 
     * of the the provided transfer matrix for the &alpha; phase plane, 
     * and Tr <b>&Phi;</b></b><sub>&alpha;&alpha;</sub> indicates the trace of matrix 
     * <b>&Phi;</b></b><sub>&alpha;&alpha;</sub>.
     * </p>
     * 
     * @return  vector of fractional tunes (&nu;<sub><i>x</i></sub>, &nu;<sub><i>y</i></sub>, &nu;<sub><i>z</i></sub>)
     *
     * @author Christopher K. Allen
     * @since  Nov 4, 2014
     */
    public R3   computeFractionalTunes() {
        
        PhaseMatrix matPhi   = this.getOneTurnMap().getFirstOrder();
        R3          vecSigma = super.calculatePhaseAdvPerCell(matPhi);
        R3          vecNu    = vecSigma.times(1/(2.0*Math.PI));
        
        return vecNu;
    }
    
    /**
     * <p>
     * Calculates and returns the full tune around the ring including the integer portion.
     * The tunes are computed for the start of the ring.
     * The tune for each phase plane is returned in the 3-dimensional vector.
     * </p>
     * <p>
     * The full tunes are computed by summing all the partial phase advances
     * through each of the trajectory states (see 
     * <code>{@link #calculatePhaseAdvance(PhaseMatrix, Twiss[], Twiss[])}</code>).  
     * Thus, the partial phase advance
     * through each state must also be computed so <em>this can be an
     * expensive operation</em>.
     * </p>
     * 
     * @return  the number <i>n.&nu;</i> for each phase plane where <i>n</i> is the 
     *          integer portion and <i>&nu;</i> is the fractional phase advance.
     *
     * @author Christopher K. Allen
     * @since  Oct 24, 2013
     * 
     */
    public R3 computeFullTunes() {

        // Initialize the vector of full tunes
        R3  vecPhsAdv = new R3();
        
        // Initialize the loop
        Twiss[]     arrTwsPrv = super.getMatchedTwiss();
        Twiss[]     arrTwsCur;
        
        PhaseMatrix matXfrPrv = PhaseMatrix.identity();
        PhaseMatrix matXfrCur;
        
        // We sum up the partial phase advance from each trajectory state
        for (TransferMapState state : super.getTrajectory()) {
            
//            String strElemId = state.getElementId();
            
            // Compute the full-turn map at this state location
            //TransferMapState    tmsCurr = (TransferMapState)state;
            PhaseMatrix         matFull = this.calculateFullLatticeMatrixAt(state);
            
            // For this state location, compute the matched twiss parameters and 
            //  the transfer matrix from the previous state to here. 
            arrTwsCur = super.calculateMatchedTwiss(matFull);
            matXfrCur = state.getTransferMap().getFirstOrder();
            
            PhaseMatrix matXfrStep = matXfrCur.times(  matXfrPrv.inverse()  );
            
            // Compute the phase advance through this state then add it to the sum
            R3          vecDelPhs = super.calculatePhaseAdvance(matXfrStep, arrTwsPrv, arrTwsCur);

            vecPhsAdv.plusEquals( vecDelPhs );
            
            // Reset the loop
            arrTwsPrv = arrTwsCur;
            matXfrPrv = matXfrCur;
            
//            // Type out
//            System.out.println("Element ID " + state.getElementId() + " phase advance=" + vecPhsAdv);
        }
        
        //  Normalize the phase in radians to unitless tunes then return
        vecPhsAdv.timesEquals( 1.0/(2.0*Math.PI) );
        
        return vecPhsAdv;
    }

//    /**
//     * <p>
//     * Calculates and returns the full tune around the ring including the integer portion.
//     * The tunes are computed for the start of the ring.
//     * The tune for each phase plane is returned in the 3-dimensional vector.
//     * </p>
//     * <p>
//     * The full tunes are computed by summing all the partial phase advances
//     * through each of the trajectory states (see 
//     * <code>{@link #calculatePhaseAdvance(PhaseMatrix, Twiss[], Twiss[])}</code>).  
//     * Thus, the partial phase advance
//     * through each state must also be computed so this can be a somewhat
//     * expensive operation.
//     * </p>
//     * 
//     * @return  the number <i>n.&nu;</i> for each phase plane where <i>n</i> is the 
//     *          integer portion and <i>&nu;</i> is the fractional phase advance.
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 24, 2013
//     * 
//     * @deprecated  The integration technique employed is not accurate enough,
//     *              it cannot track the beta function through long elements.
//     */
//    @Deprecated
//    public R3 computeFullTunes_integration() {
//
//        // Initialize the vector of full tunes
//        R3  vecPhsAdv = R3.zero();
//        
//        // Initialize the loop
//        Twiss[]     arrTws = super.getMatchedTwiss();
//        
//        double      dblPos1 = 0.0;
//        double      dblPos2;
//        
//        // We sum up the partial phase advance from each trajectory state
//        for (TransferMapState state : super.getTrajectory()) {
//            
//            dblPos2 = state.getPosition();
//            double  dblDelPos = dblPos2 - dblPos1;
//            
//            // Compute the change in phase advance and add it in
//            for (Twiss3D.IND_3D index : Twiss3D.IND_3D.values()) {
//                Twiss   twsMatched = arrTws[index.val()];
//                double  dblBetaMch = twsMatched.getBeta();
//                double  dblDelPhs  = (1.0/dblBetaMch)*dblDelPos;
//                double  dblPhsCur  = vecPhsAdv.getElem(index.val());
//                double  dblPhsNew  = dblPhsCur + dblDelPhs;
//                
//                vecPhsAdv.set(index.val(), dblPhsNew);
//            }
//
//            //
//            // Compute the values for the next iteration
//            
//            // Compute the full-turn map at this state location
//            PhaseMatrix         matFull = this.computeRingFullTurnMatrixAt(state);
//            
//            // For this state location, compute the matched twiss parameters
//            dblPos1 = dblPos2;
//            arrTws  = super.calculateMatchedTwiss(matFull);
//        }
//        
//        //  Normalize the phase in radians to unitless tunes then return
//        vecPhsAdv.timesEquals( 1.0/(2.0*Math.PI) );
//        
//        return vecPhsAdv;
//    }
//    
//    /**
//     * <p>
//     * Calculates and returns the full tune around the ring including the integer portion.
//     * The tunes are computed for the start of the ring.
//     * The tune for each phase plane is returned in the 3-dimensional vector.
//     * </p>
//     * <p>
//     * The full tunes are computed by summing all the partial phase advances
//     * through each of the trajectory states (see 
//     * <code>{@link #calculatePhaseAdvance(PhaseMatrix, Twiss[], Twiss[])}</code>).  
//     * Thus, the partial phase advance
//     * through each state must also be computed so this can be a somewhat
//     * expensive operation.
//     * </p>
//     * 
//     * @return  the number <i>n.&nu;</i> for each phase plane where <i>n</i> is the 
//     *          integer portion and <i>&nu;</i> is the fractional phase advance.
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 24, 2013
//     * 
//     * @deprecated  The method uses {@link CalculationEngine#calculatePhaseAdvance(PhaseMatrix, Twiss[], Twiss[])}
//     *              which does not give accurate results
//     */
//    @Deprecated
//    public R3 computeFullTunes_old() {
//
//        // Initialize the vector of full tunes
//        R3  vecPhsAdv = new R3();
//        
//        // Initialize the loop
//        Twiss[]     arrTwsPrv = super.getMatchedTwiss();
//        Twiss[]     arrTwsCur;
//        
//        PhaseMatrix matXfrPrv = PhaseMatrix.identity();
//        PhaseMatrix matXfrCur;
//        
//        // We sum up the partial phase advance from each trajectory state
//        for (TransferMapState state : super.getTrajectory()) {
//            
//            // Compute the full-turn map at this state location
//            //TransferMapState    tmsCurr = (TransferMapState)state;
//            PhaseMatrix         matFull = this.calculateFullLatticeMatrixAt(state);
//            
//            // For this state location, compute the matched twiss parameters and 
//            //  the transfer matrix from the previous state to here. 
//            arrTwsCur = super.calculateMatchedTwiss(matFull);
//            matXfrCur = state.getTransferMap().getFirstOrder();
//            
//            PhaseMatrix matXfrStep = matXfrCur.times(  matXfrPrv.inverse()  );
//            
//            // Compute the phase advance through this state then add it to the sum
//            R3          vecPhsStep = super.calculatePhaseAdvance(matXfrStep, arrTwsPrv, arrTwsCur);
//
//            vecPhsAdv.plusEquals( vecPhsStep );
//            
//            // Reset the loop
//            arrTwsPrv = arrTwsCur;
//            matXfrPrv = matXfrCur;
//        }
//        
//        //  Normalize the phase in radians to unitless tunes then return
//        vecPhsAdv.timesEquals( 1.0/(2.0*Math.PI) );
//        
//        return vecPhsAdv;
//    }
//
    /**
     * Computes the one-turn matrix of the ring at the given state location.
     * Let <i>S<sub>n</sub></i> be the given state object at
     * location <i>s<sub>n</sub></i>, and let <b>T</b><sub><i>n</i></sub> be the
     * transfer matrix between locations <i>s</i><sub>0</sub> and <i>s<sub>n</sub></i> ,
     * where <i>s</i><sub>0</sub> is the location of the full one-turn matrix 
     * <b>&Phi;</b><sub>0</sub> for this machine at position <i>s</i> = 0 (which is the
     * beginning and the end of the trajectory object used to construct this class
     * instance).  Then the full turn matrix 
     * <b>&Phi;</b><sub><i>n</i></sub> for the machine at location <i>s<sub>n</sub></i>
     * is given by
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>&Phi;</b><sub><i>n</i></sub> = <b>T</b><sub><i>n</i></sub> &sdot; <b>&Phi;</b><sub>0</sub>
     *               &sdot; <b>T</b><sub><i>n</i></sub><sup>-1</sup> .
     * <br>
     * <br>
     * That is, we conjugate the full transfer map for this machine by the transfer map 
     * for the given state.
     * </p> 
     *       
     * @param state     state <i>S<sub>n</sub></i> identifying the position <i>s<sub>n</sub></i>
     *   
     * @return          The one-turn matrix <b>&Phi;</b><sub><i>n</i></sub> of the ring at <i>s<sub>n</sub></i>
     *
     * @author Christopher K. Allen
     * @since  Nov 4, 2014
     */
    public PhaseMatrix  computeRingFullTurnMatrixAt(TransferMapState state) {
        PhaseMatrix     matFull = super.calculateFullLatticeMatrixAt(state);
        
        return matFull;
    }
    
    /**
     * <p>
     * Returns the transfer matrix <b>&Phi;</b><sub>2,1</sub> taking phase 
     * coordinates from state position 
     * <i>S</i><sub>1</sub> to state position <i>S</i><sub>2</sub> within the ring.
     * This is the first-order portion of the ring's transfer map <b>T</b><sub>2,1</sub>
     * between states <i>S</i><sub>1</sub> and <i>S</i><sub>2</sub> 
     * (see method 
     * <code>{@link #computeTransferMap(TransferMapState, TransferMapState)}</code>). 
     * </p>
     * <p>
     * Because of the ring topology, position <i>s</i><sub>1</sub> of state 
     * <i>S</i><sub>1</sub> and position  <i>s</i><sub>2</sub> of state 
     * <i>S</i><sub>2</sub> are really equivalence classes of real numbers
     * [<i>s</i><sub>1</sub>] &sub; <b>R</b> and [<i>s</i><sub>2</sub>] &sub; <b>R</b>,
     * respectively.  Each equivalence class can be represented
     * <br>
     * <br>
     * &nbsp; &nbsp; [<i>s<sub>i</sub></i> ] = { <i>s<sub>i</sub></i> + <i>nL</i> | <i>n</i> &in; <b>Z</b><sub>+</sub> } 
     * <br>
     * <br>
     * where <i>L</i> is the circumference of the ring.  Because of the way the ring
     * is represented as a data structure, we have <i>s</i> &in; [0,<i>L</i>]. 
     * However, we must enforce the condition that <i>s</i><sub>2</sub> is always
     * "down stream" of <i>s</i><sub>1</sub>.  Specifically,
     * we do not reverse directions when computing the transfer matrix.
     * Here we describe the calculations in practical detail.
     * </p>
     * <p>
     * If while traveling downstream from position <i>s</i><sub>1</sub> we need to
     * determine whether or not we pass the position <i>s</i> = 0 before we encounter
     * the position <i>s</i><sub>2</sub>. If so we to represent the position of 
     * state <i>S</i><sub>2</sub> as 
     * <i>s</i><sub>2</sub> + <i>L</i> &in; [<i>s</i><sub>2</sub>], since 
     * <i>s</i><sub>2</sub> &lt; <i>s</i></sub>1</sub> indicating <i>s</i><sub>2</sub>
     * is upstream of <i>s</i><sub>1</sub> (according to our model).  This condition
     * requires that we must include the ring full turn matrix <b>&Phi;</b><sub>0</sub>
     * when computing the transfer matrix.  Recall that <b>&Phi;</b><sub>0</sub> takes
     * phase coordinates from position <i>s</i> = 0
     * to position <i>s</i = 0 going all the way around the ring. 
     * </p>
     * <p>
     * Collecting all of the above, if <i>s</i><sub>2</sub> &lt; <i>s</i><sub>1</sub> 
     * then we have a propagation through point <i>s</i> = 0 and we must include the
     * full turn matrix according to
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>&Phi;</b><sub>2,1</sub> = <b>&Phi;</b><sub>2</sub><b>&Phi;</b><sub>0</sub><b>&Phi;</b><sub>1</sub><sup>-1</sup> .
     * <br>
     * <br> 
     * If <i>s</i><sub>2</sub> &gt; <i>s</i><sub>1</sub> then we can compute the transfer
     * matrix <b>&Phi;</b><sub>2,1</sub> in the usual fashion
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>&Phi;</b><sub>2,1</sub> = <b>&Phi;</b><sub>2</sub><b>&Phi;</b><sub>1</sub><sup>-1</sup> .
     * <br>
     * <br> 
     * </p>
     * 
     * @param state1    phase state <i>S</i><sub>1</sub> defining ring position <i>s</i><sub>1</sub>
     * @param state2    phase state <i>S</i><sub>2</sub> defining ring position <i>s</i><sub>2</sub>
     * 
     * @return  the transfer matrix <b>&Phi;</b><sub>2,1</sub> taking phase coordinates <b>z</b> from
     *          position <i>s</i><sub>1</sub> on the ring to position <i>s</i><sub>2</sub>
     *
     * @author Christopher K. Allen
     * @since  Nov 3, 2014
     */
    public PhaseMatrix  computeRingTransferMatrix(TransferMapState state1, TransferMapState state2) {
        
        double      dblPos1 = state1.getPosition();
        double      dblPos2 = state2.getPosition();
        
        if (dblPos1 < dblPos2) {
        
            PhaseMatrix matTrn = CalculationsOnMachines.computeTransferMatrix(state1, state2);
        
            return matTrn;
            
        } else {
            
            PhaseMatrix matPhi1 = state1.getTransferMap().getFirstOrder();
            PhaseMatrix matPhi2 = state2.getTransferMap().getFirstOrder();
            PhaseMatrix matFull = this.getFullTransferMap().getFirstOrder();
            
            PhaseMatrix matTrn = matPhi2.times( matFull.times( matPhi1.inverse() ) );
            
            return matTrn;

        }
    }

    /**
     * <p>
     * Returns the transfer map <b>T</b><sub>2,1</sub> taking phase 
     * coordinates from state position 
     * <i>S</i><sub>1</sub> to state position <i>S</i><sub>2</sub> within the ring.
     * </p>
     * <p>
     * Because of the ring topology, position <i>s</i><sub>1</sub> of state 
     * <i>S</i><sub>1</sub> and position  <i>s</i><sub>2</sub> of state 
     * <i>S</i><sub>2</sub> are really equivalence classes of real numbers
     * [<i>s</i><sub>1</sub>] &sub; <b>R</b> and [<i>s</i><sub>2</sub>] &sub; <b>R</b>,
     * respectively.  Each equivalence class can be represented
     * <br>
     * <br>
     * &nbsp; &nbsp; [<i>s<sub>i</sub></i> ] = { <i>s<sub>i</sub></i> + <i>nL</i> | <i>n</i> &in; <b>Z</b><sub>+</sub> } 
     * <br>
     * <br>
     * where <i>L</i> is the circumference of the ring.  Because of the way the ring
     * is represented as a data structure, we have <i>s</i> &in; [0,<i>L</i>]. 
     * However, we must enforce the condition that <i>s</i><sub>2</sub> is always
     * "down stream" of <i>s</i><sub>1</sub>.  Specifically,
     * we do not reverse directions when computing the transfer map.
     * Here we describe the calculations in practical detail.
     * </p>
     * <p>
     * If while traveling downstream from position <i>s</i><sub>1</sub> we need to
     * determine whether or not we pass the position <i>s</i> = 0 before we encounter
     * the position <i>s</i><sub>2</sub>. If so we to represent the position of 
     * state <i>S</i><sub>2</sub> as 
     * <i>s</i><sub>2</sub> + <i>L</i> &in; [<i>s</i><sub>2</sub>], since 
     * <i>s</i><sub>2</sub> &lt; <i>s</i></sub>1</sub> indicating <i>s</i><sub>2</sub>
     * is upstream of <i>s</i><sub>1</sub> (according to our model).  This condition
     * requires that we must include the ring full turn map <b>T</b><sub>0</sub>
     * when computing the transfer map.  Recall that <b>T</b><sub>0</sub> takes
     * phase coordinates from position <i>s</i> = 0
     * to position <i>s</i = 0 going all the way around the ring. 
     * </p>
     * <p>
     * Collecting all of the above, if <i>s</i><sub>2</sub> &lt; <i>s</i><sub>1</sub> 
     * then we have a propagation through point <i>s</i> = 0 and we must include the
     * full turn map according to
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>T</b><sub>2,1</sub> = <b>T</b><sub>2</sub><b>T</b><sub>0</sub><b>T</b><sub>1</sub><sup>-1</sup> .
     * <br>
     * <br> 
     * If <i>s</i><sub>2</sub> &gt; <i>s</i><sub>1</sub> then we can compute the transfer
     * map <b>T</b><sub>2,1</sub> in the usual fashion
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>T</b><sub>2,1</sub> = <b>T</b><sub>2</sub><b>T</b><sub>1</sub><sup>-1</sup> .
     * <br>
     * <br> 
     * </p>
     * 
     * @param state1    phase state <i>S</i><sub>1</sub> defining ring position <i>s</i><sub>1</sub>
     * @param state2    phase state <i>S</i><sub>2</sub> defining ring position <i>s</i><sub>2</sub>
     * 
     * @return  the transfer map <b>T</b><sub>2,1</sub> taking phase coordinates <b>z</b> from
     *          position <i>s</i><sub>1</sub> on the ring to position <i>s</i><sub>2</sub>
     *
     * @author Christopher K. Allen
     * @since  Nov 3, 2014
     */
    public PhaseMap computeRingTransferMap(TransferMapState state1, TransferMapState state2) {
        
        double      dblPos1 = state1.getPosition();
        double      dblPos2 = state2.getPosition();
        
        if (dblPos1 < dblPos2) {
        
            PhaseMap mapTrn = CalculationsOnMachines.computeTransferMap(state1, state2);
        
            return mapTrn;
            
        } else {
            
            PhaseMap    mapPhi1 = state1.getTransferMap();
            PhaseMap    mapPhi2 = state2.getTransferMap();
            PhaseMap    mapFull = this.getFullTransferMap();

            PhaseMap mapTrn = mapPhi2.compose( mapFull.compose( mapPhi1.inverse() ) );
            
            return mapTrn;
        }
    }
    
    /**
     * <p>
     * Computes and returns the turn-by-turn phase positions 
     * {<b>z</b><sub><i>n</i></sub> &in; <b>R</b><sup>6</sup> &times; {1} | <i>n</i>=0,...,<i>N</i>-1 }
     * at the given location <i>s</i><sub>obs</sub> of state <i>S</i><sub>obs</sub> resulting 
     * from a particle injected
     * at location <i>s</i><sub>inj</sub> &in; <i>S</i><sub>inj</sub> with initial phase 
     * coordinates <b>z</b><sub>inj</sub>.  
     * </p>
     * <p>
     * The coordinates <b>z</b><sub><i>n</i></sub>
     * are taken with respect to the ring's fixed point orbit <b>p</b> at location 
     * <i>s</i><sub>obj</sub>.  That is, each <b>z</b><sub><i>n</i></sub> is a displacement
     * from <b>p</b> in the global coordinate system.
     * </p>
     * <p>
     * Currently the computation is entirely matrix based.  Only transfer matrices
     * are used and not transfer maps.  Specifically, let <b>&Phi;</b><sub>2,1</sub> be the
     * transfer matrix from <i>s</i><sub>1</sub> &#8796; <i>s</i><sub>inj</sub> to 
     * <i>s</i><sub>2</sub> &#8796; <i>s</i><sub>obs</sub> and let 
     * <b>&Phi;</b><sub>2,2</sub> be the one-turn map at position <i>s</i><sub>2</sub> in 
     * the ring. Then the returned array of phase vectors {<b>z</b><sub><i>n</i></sub> } is
     * given by
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>z</b><sub><i>n</i></sub> = [<b>&Phi;</b><sub>2,2</sub>]<sup><i>n</i></sup> 
     *                                             <b>&Phi;</b><sub>2,1</sub> <b>z</b><sub>inj</sub> .
     * <br>
     * <br>
     * Note that <b>z</b><sub>0</sub> is <b>&Phi;</b><sub>2,1</sub> <b>z</b><sub>inj</sub> , the
     * phase coordinates of the particle after propagating from the injection location to the
     * observation location.                                     
     * </p>
     *  
     * @param stateInj  trajectory state at the injection location
     * @param stateObs  trajectory state at the observation location
     * @param cntTurns  number of turns <i>N</i> to observe particle
     * @param vecInj    the initial phase coordinates in the ring global coordinate system 
     * 
     * @return  an array of phase coordinates representing displacements from the fixed orbit
     *
     * @author Christopher K. Allen
     * @since  Nov 4, 2014
     */
    public PhaseVector[] computeTurnByTurnResponse(TransferMapState stateInj, TransferMapState stateObs, int cntTurns, PhaseVector vecInj) {

        PhaseMatrix     matPhi = this.computeRingTransferMatrix(stateInj, stateObs);
        PhaseMatrix     matFull = this.computeRingFullTurnMatrixAt(stateObs);
        
        // Convert the injected particle coordinates to that w.r.t. the fixed orbit
        PhaseVector vec1 = matPhi.times( vecInj);

//        // type outs
//        System.out.println("CalculationsOnRings#computeTurnByTurnResponse(): matPhi");
//        System.out.print(matPhi.toStringMatrix());
//        System.out.println();
//        
//        System.out.println("CalculationsOnRings#computeTurnByTurnResponse(): matFull");
//        System.out.print(matFull.toStringMatrix());
//        System.out.println();
//        
//        System.out.println("CalculationsOnRings#computeTurnByTurnResponse(): vec1");
//        System.out.print(vec1.toString());
//        System.out.println();
        
        PhaseVector[]   arrPosVec = new PhaseVector[cntTurns];
        arrPosVec[0] = vec1;
        for (int i=1; i<cntTurns; i++) {
            PhaseVector vec2 = matFull.times(vec1);
            
            arrPosVec[i] = vec2;
            vec1 = vec2;
        }
        
        return arrPosVec;
    }
    
    /**
     * <p>
     * Computes and returns the turn-by-turn phase positions 
     * {<b>z</b><sub><i>n</i></sub> &in; <b>R</b><sup>6</sup> &times; {1} | <i>n</i>=0,...,<i>N</i>-1 }
     * at the given location <i>s</i><sub>obs</sub> of state <i>S</i><sub>obs</sub> resulting 
     * from a particle injected
     * at location <i>s</i><sub>inj</sub> &in; <i>S</i><sub>inj</sub> with initial phase 
     * coordinates <b>z</b><sub>inj</sub>.
     * </p>
     * <p>
     * Currently the computation is entirely matrix based.  Only transfer matrices
     * are used and not transfer maps.  Specifically, let <b>&Phi;</b><sub>2,1</sub> be the
     * transfer matrix from <i>s</i><sub>1</sub> &#8796; <i>s</i><sub>inj</sub> to 
     * <i>s</i><sub>2</sub> &#8796; <i>s</i><sub>obs</sub> and let 
     * <b>&Phi;</b><sub>2,2</sub> be the one-turn map at position <i>s</i><sub>2</sub> in 
     * the ring. Then the returned array of phase vectors {<b>z</b><sub><i>n</i></sub> } is
     * given by
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>z</b><sub><i>n</i></sub> = [<b>&Phi;</b><sub>2,2</sub>]<sup><i>n</i></sup> 
     *                                             <b>&Phi;</b><sub>2,1</sub> <b>z</b><sub>inj</sub> .
     * <br>
     * <br>
     * Note that <b>z</b><sub>0</sub> is <b>&Phi;</b><sub>2,1</sub> <b>z</b><sub>inj</sub> , the
     * phase coordinates of the particle after propagating from the injection location to the
     * observation location.                                     
     * </p>
     *  
     * @param stateInj  trajectory state at the injection location
     * @param stateObs  trajectory state at the observation location
     * @param cntTurns  number of turns <i>N</i> to observe particle
     * @param vecInj    the initial phase coordinates w.r.t. to the fixed orbit location 
     * 
     * @return  an array of phase coordinates representing displacements from the fixed orbit
     *
     * @author Christopher K. Allen
     * @since  Nov 4, 2014
     */
    public PhaseVector[] computeTurnByTurnRespWrtFixedOrbit(TransferMapState stateInj, TransferMapState stateObs, int cntTurns, PhaseVector vecInj) {

        PhaseMatrix     matPhi = this.computeRingTransferMatrix(stateInj, stateObs);
        PhaseMatrix     matFull = this.computeRingFullTurnMatrixAt(stateObs);
        
        // Convert the injected particle coordinates to that w.r.t. the fixed orbit
        PhaseVector vecFxdOrb = super.computeFixedOrbit(stateObs);
        
        PhaseVector vec0 = vecFxdOrb.plus( vecInj ); 
        PhaseVector vec1 = matPhi.times( vec0 );

//        // type outs
//        System.out.println("CalculationsOnRings#computeTurnByTurnResponse(): matPhi");
//        System.out.print(matPhi.toStringMatrix());
//        System.out.println();
//        
//        System.out.println("CalculationsOnRings#computeTurnByTurnResponse(): matFull");
//        System.out.print(matFull.toStringMatrix());
//        System.out.println();
//        
//        System.out.println("CalculationsOnRings#computeTurnByTurnResponse(): vec1");
//        System.out.print(vec1.toString());
//        System.out.println();
        
        PhaseVector[]   arrPosVec = new PhaseVector[cntTurns];
        arrPosVec[0] = vec1;
        for (int i=1; i<cntTurns; i++) {
            PhaseVector vec2     = matFull.times(vec1);
            PhaseVector vec2FOrb = vec2.minus( vecFxdOrb );
            
            arrPosVec[i] = vec2FOrb;
            vec1 = vec2;
        }
        
        return arrPosVec;
    }
    
    /**
     * <p>
     * Calculates the matched Courant-Snyder parameters for the given
     * state location.  The computed Twiss parameters are the matched
     * envelopes for the ring at that point.
     * </p>
     * <p>
     * Internally, the array of phase advances 
     * {&sigma;<sub><i>x</i></sub>, &sigma;<sub><i>y</i></sub>, &sigma;<sub><i>x</i></sub>}
     * are assumed to be the particle phase advances through the ring for the matched 
     * solution.   These are computed with the base class method 
     * <code>{@link #calculatePhaseAdvPerCell(PhaseMatrix)}</code>.
     * </p> 
     * <p>
     * The returned Courant-Snyder parameters (&alpha;, &beta;, &epsilon;) are invariant
     * under the action of the one-turn matrix at the state location, that is, 
     * they are matched.  All that 
     * is required are &alpha; and &beta; since &epsilon; specifies the size of the beam
     * envelope.  Consequently the returned &epsilon; is <code>NaN</code>.
     * </p>
     * The following are the calculations used for the Courant-Snyder parameters of a 
     * single phase plane:
     * <br>
     * <br>
     * &nbsp; &nbsp; &alpha; &equiv; -<i>ww'</i> = (&phi;<sub>11</sub> - &phi;<sub>22</sub>)/(2 sin &sigma;) ,
     * <br>
     * <br>
     * &nbsp; &nbsp; &beta; &equiv; <i>w</i><sup>2</sup> = &phi;<sub>12</sub>/sin &sigma;
     * <br>
     * <br>
     * where &phi;<sub><i>ij</i></sub> are the elements of the 2&times;2 diagonal blocks of
     * the one-turn matrix <b>&Phi;</b> for the the particular phase plane. The particle 
     * amplitude function <i>w</i>
     * is taken from Reiser, and &sigma; is the phase advance through the cell for the 
     * particular phase plane.
     * </p>
     * 
     * @param state     state defining the location for computing the matched Twiss parameters
     * 
     * @return          matched Twiss parameters at the given state location
     *
     * @author Christopher K. Allen
     * @since  Nov 5, 2014
     */
    public Twiss[]  computeMatchedTwissAt(TransferMapState state) {
        PhaseMatrix matPhi = this.computeRingFullTurnMatrixAt(state);
        Twiss[]     arrTws = super.calculateMatchedTwiss(matPhi);
        
        return arrTws;
    }
    
//    /**
//     * <p>
//     * Calculates the fixed point (closed orbit) in transverse phase space
//     * at the given state location in the presence of dispersion.  
//     * </p>
//     * <p>
//     * Let the full-turn map a the state location be denoted <b>&Phi;</b>.
//     * The transverse plane dispersion vector <b>&Delta;</b> is defined  
//     * <br>
//     * <br> 
//     * &nbsp; &nbsp; <b>&Delta;</b><sub><i>t</i></sub> &equiv; -(1/&gamma;<sup>2</sup>)[d<i>x</i>/d<i>z'</i>, d<i>x'</i>/d<i>z'</i>, d<i>y</i>/d<i>z'</i>, d<i>y'</i>/d<i>z'</i>]<sup><i>T</i></sup> .
//     * <br>
//     * <br>  
//     * It can be identified as the first 4 entries of the 6<sup><i>th</i></sup> 
//     * column in the transfer matrix <b>&Phi;</b>. The above vector
//     * quantifies the change in the transverse particle phase 
//     * coordinate position versus the change in particle momentum.  
//     * The factor -(1/&gamma;<sup>2</sup>) is needed to convert from longitudinal divergence
//     * angle <i>z'</i> used by XAL to momentum &delta;<i>p</i> &equiv; &Delta;<i>p</i>/<i>p</i> used in 
//     * the dispersion definition.  Specifically,
//     * <br>
//     * <br>
//     * &nbsp; &nbsp; &delta;<i>p</i> &equiv; &Delta;<i>p</i>/<i>p</i> = &gamma;<sup>2</sup><i>z</i>'
//     * <br>
//     * <br>
//     * As such, the above vector can be better described
//     * <br>
//     * <br> 
//     * &nbsp; &nbsp; <b>&Delta;</b><sub><i>t</i></sub> &equiv; [&Delta;<i>x</i>/&delta;<i>p</i>, &Delta;<i>x'</i>/&delta;<i>p</i>, &Delta;<i>y</i>/&delta;<i>p</i>, &Delta;<i>y'</i>/&delta;<i>p</i>]<sup><i>T</i></sup>
//     * <br>
//     * <br>
//     * explicitly describing the change in transverse phase coordinate for fractional
//     * change in momentum &delta;<i>p</i>.  
//     * </p>
//     * <p>
//     * Since we are only concerned with transverse phase space coordinates, we restrict ourselves to the 
//     * 4&times;4 upper diagonal block of <b>&Phi;</b>, which we denote take <b>T</b>.  
//     * That is, <b>T</b> = &pi; &sdot; <b>&Phi;</b>
//     * where &pi; : <b>R</b><sup>6&times;6</sup> &rarr; <b>R</b><sup>4&times;4</sup> is the
//     * projection operator. 
//     * </p>
//     * <p>
//     * This method finds that point <b>z</b><sub><i>t</i></sub> &equiv; 
//     * (<i>x<sub>t</sub></i>, <i>x'<sub>t</sub></i>, <i>y<sub>t</sub></i>, <i>y'<sub>t</sub></i>)
//     * in transvse phase space that is invariant under the action of the ring for a given momentum spread
//     * &delta;<i>p</i>.  That is, the particle ends up
//     * in the same location each revolution. With a finite momentum spread of &delta;<i>p</i> &gt; 0
//     * we require this require that
//     * <br>
//     * <br>
//     * &nbsp; &nbsp; <b>Tz</b><sub><i>t</i></sub> + &delta;<i>p</i><b>&Delta;</b><sub><i>t</i></sub> = <b>z</b><sub><i>t</i></sub> ,
//     * <br>
//     * <br>
//     * which can be written
//     * <br>
//     * <br>
//     * &nbsp; <b>z</b><sub><i>t</i></sub> = &delta;<i>p</i>(<b>T</b> - <b>I</b>)<sup>-1</sup><b>&Delta;</b><sub><i>t</i></sub> ,
//     * <br>
//     * <br>
//     * where <b>I</b> is the identity matrix.  Dividing both sides by &delta;<i>p</i> yields the final
//     * result
//     * <br>
//     * <br>
//     * &nbsp; <b>z</b><sub>0</sub> &equiv; <b>z</b><sub><i>t</i></sub>/&delta;<i>p</i> = (<b>T</b> - <b>I</b>)<sup>-1</sup><b>&Delta;</b><sub><i>t</i></sub> ,
//     * <br>
//     * <br>
//     * which is the returned value of this method.  It is normalized by
//     * &delta;<i>p</i> so that we can compute the closed orbit for any given momentum spread.
//     * </p>
//     *   
//     * @param matPhi    we are calculating the dispersion of a ring at this state location
//     * 
//     * @return         The closed orbit fixed point <b>z</b><sub>0</sub> for finite 
//     *                 dispersion, normalized by momentum spread.
//     *                 Returned as an array [<i>x</i><sub>0</sub>,<i>x'</i><sub>0</sub>,<i>y</i><sub>0</sub>,<i>y'</i><sub>0</sub>]/&delta;<i>p</i>
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 30, 2013
//     */
//    public R4 calculateDispersion(TransferMapState state) {
//        PhaseMatrix     matFullTrn = this.calculateFullLatticeMatrixAt(state);
//        double          dblGamma   = state.getGamma();
//        
//        R4              vecDisp    = super.calculateDispersion(matFullTrn, dblGamma);
//        
//        return vecDisp;
//    }
    
 
 
}
