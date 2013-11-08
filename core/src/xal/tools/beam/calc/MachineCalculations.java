/**
 * MachineCalculations.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 7, 2013
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
 * Class for performing the calculations expressed in the
 * <code>ISimulationResults</code> interface in the context of
 * a particle beam system without regard to the particle.
 * That is, only properties of the machine are computed, no
 * attributes or properties of the beam are required for the 
 * computations. 
 *
 *
 * @author Christopher K. Allen
 * @since  Nov 7, 2013
 */
public class MachineCalculations extends CalculationEngine  implements ISimulationResults<TransferMapState> {

    
    /*
     * Local Attributes
     */
    
    /** The trajectory around one turn of the ring */
    private final TransferMapTrajectory trjSimFull;
    
    /** The final transfer map probe state (at the end of the ring) */
    private final TransferMapState      staFinal;
    
    /** The transfer map (matrix) for the entire trajectory */
    private final PhaseMap              mapPhiFull;

    /** The matched beam Twiss parameters at the start of the ring */
    private final Twiss[]               arrTwsMch;
    

    /*
     * Initialization
     */
    
    /**
     * <p>
     * Constructor for <code>MachineCalculations</code>.  Accepts the 
     * <code>TransferMapTrajectory</code>
     * object and extracts the final state and full trajectory transfer map.  
     * Quantities that are 
     * required for subsequent machine property calculations are also computed, such as
     * phase advance through the trajectory (modulo 2&pi;), entrance position 
     * "fixed orbit" (that is, the orbit that is invariant for repeated applications of the 
     * linear part of the transfer map (separating the projective part), 
     * and the matched envelope when treating the full transfer map as the map
     * for a periodic cell.
     * </p>
     *
     * @param  trjSim  the simulation data for the ring, a "transfer map trajectory" object
     *
     * @throws IllegalArgumentException the trajectory does not contain <code>TransferMapState</code> objects
     *
     * @author Christopher K. Allen
     * @since  Nov 7, 2013
     */
    public MachineCalculations(TransferMapTrajectory trjSim) throws IllegalArgumentException {
        ProbeState  pstFinal = trjSim.finalState();
        
        // Check for correct probe types
        if ( !( pstFinal instanceof TransferMapState) )
            throw new IllegalArgumentException(
                    "Trajectory states are not TransferMapStates? - " 
                    + pstFinal.getClass().getName()
                    );
        
        this.trjSimFull = trjSim;
        this.staFinal   = (TransferMapState)pstFinal;
        this.mapPhiFull = this.staFinal.getTransferMap();
        this.arrTwsMch  = super.calculateMatchedTwiss(this.mapPhiFull.getFirstOrder()); 
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
    public TransferMapTrajectory   getTrajectory() {
        return this.trjSimFull;
    }
    
    /**
     * Returns the transfer map of the full machine lattice represented by the
     * associated simulation trajectory.
     * 
     * @return  the transfer map of the last state of the associated trajectory
     *
     * @author Christopher K. Allen
     * @since  Nov 7, 2013
     */
    public PhaseMap getFullTransferMap() {
        return this.mapPhiFull;
    }
    
    /**
     * <p>
     * Returns the collection of matched Courant-Snyder parameters for each phase
     * plane. The Courant-Snyder parameters describe the matched beam envelopes
     * when treating the trajectory of transfer maps as that for a periodic
     * lattice.  There the beam envelopes would have the same characteristics at the
     * beginning and end of the lattice.
     * </p>
     * <p>
     * These are computed parameters calculated once at construction time.
     * </p>
     * 
     * @return  the Courant-Snyder parameters of a beam needed to match it into the transfer
     *          maps of the associated trajectory when treated as a periodic lattice
     *
     * @author Christopher K. Allen
     * @since  Nov 7, 2013
     */
    public Twiss[]  getMatchedTwiss() {
        return this.arrTwsMch;
    }
    
    
    /*
     * ISimulationResults Interface
     */
    
    /**
     * <p>
     * Computes the closed orbit Twiss parameters at this state location representing
     * the matched beam envelope when treating the trajectory as a lattice of 
     * periodic cells.
     * </p>
     * <p>
     * Returns the array of twiss objects for this state for all three planes.
     * </p>
     * <p>
     * Calculates the matched Courant-Snyder parameters for the given
     * period cell transfer matrix and phase advances.  When the given transfer matrix
     * is the full-turn matrix for a ring the computed Twiss parameters are the matched
     * envelopes for the ring at that point.
     * </p>
     * <p>
     * Let <b>&Phi;</b> denote the transfer matrix from the machine beginning to the given
     * state location (it is contained in the given state). 
     * It is assumed to be the transfer matrix through
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
    @Override
    public Twiss[] computeTwissParameters(TransferMapState state) {
        PhaseMatrix matFullTrn = this.calculateFullLatticeMatrixAt(state);
        Twiss[]     arrTwsMtch = super.calculateMatchedTwiss(matFullTrn);
        
        return arrTwsMtch;
    }

    /**
     * <p>
     * This is the phase advance for the given state location.
     * </p>
     * <p>
     * Compute and return the particle phase advance from the trajectory beginning
     * to the given state location.
     * </p>
     * <p>
     * Internally the method calculates the phase advances using the initial and final 
     * Courant-Snyder &alpha; and &beta; values for the matched beam at the trajectory
     * beginning and the location of the given state, respectively. 
     * These Courant-Snyder parameters are computed as the matched beam envelope at the
     * trajectory beginning and the given state location.
     * </p>
     * <p>
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
    @Override
    public R3 computeBetatronPhase(TransferMapState state) {
        PhaseMatrix matFullTrn = this.calculateFullLatticeMatrixAt(state);
        Twiss[]     arrTwsLoc  = super.calculateMatchedTwiss(matFullTrn);
    
        PhaseMatrix matPhiLoc  = state.getTransferMap().getFirstOrder();
        R3          vecPhsAdv  = super.calculatePhaseAdvance(matPhiLoc, this.arrTwsMch, arrTwsLoc);
        
        return vecPhsAdv;
    }

    /**
     * We return the zero phase vector since there are no well-defined phase 
     * coordinates for a transfer map. 
     *
     * @see xal.model.probe.traj.ICoordinateState#getPhaseCoordinates()
     *
     * @author Christopher K. Allen
     * @since  Oct 22, 2013
     * 
     * @deprecated  The notion of phase coordinates is ill-defined or ambiguous
     */
    @Deprecated
    @Override
    public PhaseVector computePhaseCoordinates(TransferMapState state) {
        return PhaseVector.newZero();
    }

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
    @Override
    public PhaseVector computeFixedOrbit(TransferMapState state) {
        PhaseMatrix matFullTrn = this.calculateFullLatticeMatrixAt(state);
        PhaseVector vecFixedPt = super.calculateFixedPoint(matFullTrn);
        
        return vecFixedPt; 
    }

    /**
     *
     * @see xal.tools.beam.calc.ISimulationResults#computeChromDispersion(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 8, 2013
     */
    @Override
    public PhaseVector computeChromDispersion(TransferMapState state) {
        PhaseMatrix matFullTn = this.calculateFullLatticeMatrixAt(state);
        double      dblGamma  = state.getGamma();
        
        double[]    arrDisp   = super.calculateDispersion(matFullTn, dblGamma);
        R4          vecDispR4 = new R4(arrDisp);
        PhaseVector vecDisp   = PhaseVector.embed(vecDispR4);
        
        return vecDisp;
    }


    /*
     * Support Methods
     */
    
    /**
     * <p>
     * Calculates and returns the full lattice matrix for the machine at the
     * given state location.  Let <i>S<sub>n</sub></i> be the given state object at
     * location <i>s<sub>n</sub></i>, and let <b>T</b><sub><i>n</i></sub> be the
     * transfer matrix between locations <i>s</i><sub>0</sub> and <i>s<sub>n</sub></i> ,
     * where <i>s</i><sub>0</sub> is the location of the full transfer matrix 
     * <b>&Phi;</b><sub>0</sub> for this machine (end to end).  Then the full turn matrix 
     * <b>&Phi;</b><sub><i>n</i></sub> for the machine at location <i>s<sub>n</sub></i>
     * is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <b>&Phi;</b><sub><i>n</i></sub> = <b>T</b><sub><i>n</i></sub> &sdot; <b>&Phi;</b><sub>0</sub>
     *               &sdot; <b>T</b><sub><i>n</i></sub><sup>-1</sup> .
     * <br/>
     * <br/>
     * That is, we conjugate the full transfer map for this machine by the transfer map 
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
    protected PhaseMatrix calculateFullLatticeMatrixAt(TransferMapState state) {
        PhaseMap    mapPhiState = state.getTransferMap();
        PhaseMatrix matPhiState = mapPhiState.getFirstOrder();
        PhaseMatrix matPhiStInv = matPhiState.inverse();
    
        PhaseMatrix matPhiFull = this.mapPhiFull.getFirstOrder();
        PhaseMatrix matFullTnLoc;
        
        matFullTnLoc = matPhiFull.times(matPhiStInv);
        matFullTnLoc = matPhiState.times(matFullTnLoc);
    
        return matFullTnLoc;
    }

}
