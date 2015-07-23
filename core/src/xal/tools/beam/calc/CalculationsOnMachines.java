/**
 * CalculationsOnMachines.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 7, 2013
 */
package xal.tools.beam.calc;

import xal.tools.beam.calc.ISimulationResults.ISimLocResults;
import xal.tools.beam.calc.ISimulationResults.ISimEnvResults;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.probe.traj.TransferMapState;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.PhaseMatrix.IND;
import xal.tools.math.r3.R3;
import xal.tools.math.r4.R4;
import xal.tools.math.r6.R6;

/**
 * Class for performing the calculations expressed in the
 * <code>ISimEnvResults</code> interface in the context of
 * a particle beam system without regard to the particle.
 * That is, only properties of the machine are computed, no
 * attributes or properties of the beam are required for the 
 * computations. 
 *
 *
 * @author Christopher K. Allen
 * @since  Nov 7, 2013
 */
public class CalculationsOnMachines extends CalculationEngine  implements ISimLocResults<TransferMapState>, ISimEnvResults<TransferMapState> {

    
    /*
     * Global Operations
     */
    
    /**
     * Convenience method for computing the transfer matrix between two state locations, say <i>S</i><sub>1</sub>
     * and <i>S</i><sub>2</sub>.  Let <i>s</i><sub>0</sub> be the axis location of the beamline
     * entrance, <i>s</i><sub>1</sub> the location of state <i>S</i><sub>1</sub>, and 
     * <i>s</i><sub>2</sub> the location of state <i>S</i><sub>2</sub>.  Each state object <i>S<sub>n</sub></i>
     * contains the transfer matrix <b>&Phi;</b>(<i>s<sub>n</sub></i>,<i>s</i><sub>0</sub>)
     * which takes phases coordinates at the beamline entrance to the position of state <i>S<sub>n</sub></i>. 
     * The transfer matrix
     * <b>&Phi;</b>(<i>s</i><sub>2</sub>,<i>s</i><sub>1</sub>) taking phase coordinates <b>z</b><sub>1</sub>
     * (and covariance matrix <b>&sigma;</b><sub>1</sub>)
     * from position <i>s</i><sub>1</sub> to position <i>s</i><sub>2</sub> is then given
     * by
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>&Phi;</b>(<i>s</i><sub>2</sub>,<i>s</i><sub>1</sub>) = 
     *                  <b>&Phi;</b>(<i>s</i><sub>2</sub>,<i>s</i><sub>0</sub>)
     *                  <b>&Phi;</b>(<i>s</i><sub>1</sub>,<i>s</i><sub>0</sub>)<sup>-1</sup> ,
     * <br>
     * <br>
     * where <b>&Phi;</b>(<i>s</i><sub>2</sub>,<i>s</i><sub>0</sub>) is the transfer matrix between
     * the beamline entrance <i>s</i><sub>0</sub> and the position <i>s</i><sub>2</sub>
     * of state <i>S</i><sub>2</sub>, and <b>&Phi;</b>(<i>s</i><sub>1</sub>,<i>s</i><sub>0</sub>) is the
     * transfer matrix between the beamline entrance <i>s</i><sub>0</sub> and the position <i>s</i><sub>1</sub>
     * of state <i>S</i><sub>1</sub>.
     * 
     * @param state1    trajectory state <i>S</i><sub>1</sub> of starting location <i>s</i><sub>1</sub> 
     * @param state2    trajectory state <i>S</i><sub>2</sub> of final location <i>s</i><sub>2</sub>
     * 
     * @return          transfer matrix <b>&Phi;</b>(<i>s</i><sub>2</sub>,<i>s</i><sub>1</sub>) between
     *                  locations <i>s</i><sub>1</sub> and <i>s</i><sub>2</sub>
     *
     * @author Christopher K. Allen
     * @since  Jun 23, 2014
     */
    public static PhaseMatrix  computeTransferMatrix(TransferMapState state1, TransferMapState state2) {
        PhaseMatrix matPhi1 = state1.getTransferMap().getFirstOrder();
        PhaseMatrix matPhi2 = state2.getTransferMap().getFirstOrder();
        
        PhaseMatrix matPhi1inv = matPhi1.inverse();
        PhaseMatrix matPhi21   = matPhi2.times( matPhi1inv );
        
        return matPhi21;
    }
    
    /**
     * Convenience method for computing the transfer map between two state locations, say <i>S</i><sub>1</sub>
     * and <i>S</i><sub>2</sub>.  Let <i>s</i><sub>0</sub> be the axis location of the beamline
     * entrance, <i>s</i><sub>1</sub> the location of state <i>S</i><sub>1</sub>, and 
     * <i>s</i><sub>2</sub> the location of state <i>S</i><sub>2</sub>.  Each state object <i>S<sub>n</sub></i>
     * contains the transfer map <b>T</b>(<i>s<sub>n</sub></i>,<i>s</i><sub>0</sub>)
     * which takes phases coordinates at the beamline entrance to the position of state <i>S<sub>n</sub></i>. 
     * The transfer map
     * <b>T</b>(<i>s</i><sub>2</sub>,<i>s</i><sub>1</sub>) taking phase coordinates <b>z</b><sub>1</sub>
     * (and covariance matrix <b>&sigma;</b><sub>1</sub>)
     * from position <i>s</i><sub>1</sub> to position <i>s</i><sub>2</sub> is then given
     * by
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>T</b>(<i>s</i><sub>2</sub>,<i>s</i><sub>1</sub>) = 
     *                  <b>T</b>(<i>s</i><sub>2</sub>,<i>s</i><sub>0</sub>) &#x2218;
     *                  <b>T</b>(<i>s</i><sub>1</sub>,<i>s</i><sub>0</sub>)<sup>-1</sup> ,
     * <br>
     * <br>
     * where <b>T</b>(<i>s</i><sub>2</sub>,<i>s</i><sub>0</sub>) is the transfer map between
     * the beamline entrance <i>s</i><sub>0</sub> and the position <i>s</i><sub>2</sub>
     * of state <i>S</i><sub>2</sub>, and <b>T</b>(<i>s</i><sub>1</sub>,<i>s</i><sub>0</sub>) is the
     * transfer map between the beamline entrance <i>s</i><sub>0</sub> and the position <i>s</i><sub>1</sub>
     * of state <i>S</i><sub>1</sub>.
     * 
     * @param state1    trajectory state <i>S</i><sub>1</sub> of starting location <i>s</i><sub>1</sub> 
     * @param state2    trajectory state <i>S</i><sub>2</sub> of final location <i>s</i><sub>2</sub>
     * 
     * @return          transfer map <b>T</b>(<i>s</i><sub>2</sub>,<i>s</i><sub>1</sub>) between
     *                  locations <i>s</i><sub>1</sub> and <i>s</i><sub>2</sub>
     *                  
     * @author Christopher K. Allen
     * @since  Nov 4, 2014
     */
    public static PhaseMap  computeTransferMap(TransferMapState state1, TransferMapState state2) {
        PhaseMap    mapPhi1 = state1.getTransferMap();
        PhaseMap    mapPhi2 = state2.getTransferMap();
        
        PhaseMap    mapPhi1inv = mapPhi1.inverse();
        PhaseMap    mapPhi21   = mapPhi2.compose( mapPhi1inv );
        
        return mapPhi21;
    }


    /*
     * Local Attributes
     */
    
    /** The trajectory around one turn of the ring */
    private final Trajectory<TransferMapState> trjSimFull;
    
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
     * Constructor for <code>CalculationsOnMachines</code>.  Accepts the 
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
     * @param  datSim  the simulation data for the ring, a "transfer map trajectory" object
     *
     * @throws IllegalArgumentException the trajectory does not contain <code>TransferMapState</code> objects
     *
     * @author Christopher K. Allen
     * @since  Nov 7, 2013
     */
    public CalculationsOnMachines(Trajectory<TransferMapState> datSim) throws IllegalArgumentException {
        TransferMapState  pstFinal = datSim.finalState();
        
        this.trjSimFull = datSim;
        this.staFinal   = pstFinal;
        this.mapPhiFull = this.staFinal.getTransferMap();
        this.arrTwsMch  = super.calculateMatchedTwiss(this.mapPhiFull.getFirstOrder()); 
    }

    
    /*
     * Attribute Queries
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
    public Trajectory<TransferMapState>   getTrajectory() {
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
     * Local Operations
     */
    
    /**
     * <p>
     * Returns the state response matrix calculated from the front face of
     * elemFrom to the back face of elemTo. This is a convenience wrapper to
     * the real method in the trajectory class
     * </p>
     * <p>
     * This method was moved here from EnvelopeTrajectory/EnvelopeProbe where
     * it was eliminated since <code>Trajectory</code> was genericized.
     * </p>
     * 
     * @param strIdElemFrom  String identifying starting lattice element
     * @param strIdElemTo    String identifying ending lattice element
     * 
     * @return      response matrix from elemFrom to elemTo
     * 
     * @see EnvelopeTrajectory#computeTransferMatrix(String, String)
     * 
     */
    public PhaseMatrix computeTransferMatrix(String strIdElemFrom, String strIdElemTo) {
        
        Trajectory<TransferMapState> trajectory = this.getTrajectory();
        
        // find starting index
        int[] arrIndFrom = trajectory.indicesForElement(strIdElemFrom);

        int[] arrIndTo = trajectory.indicesForElement(strIdElemTo);

        if (arrIndFrom.length == 0 || arrIndTo.length == 0)
            throw new IllegalArgumentException("unknown element id");

        int indFrom, indTo;
        indTo = arrIndTo[arrIndTo.length - 1]; // use last state before start element

        TransferMapState stateTo = trajectory.stateWithIndex(indTo);
        PhaseMatrix matTo = stateTo.getTransferMap().getFirstOrder();
        
        indFrom = arrIndFrom[0] - 1;
        if (indFrom < 0) return matTo; // response from beginning of machine
        
        TransferMapState stateFrom = trajectory.stateWithIndex(indFrom);
        PhaseMatrix matFrom = stateFrom.getTransferMap().getFirstOrder();
        
        return matTo.times(matFrom.inverse());
    }

    
    /*
     * ISimLocResults Interface
     */
    
    /**
     * <p>
     * We return the projective portion of the full-turn transfer
     * map &phi;<sub><i>n</i></sub> : <b>P</b><sup>6</sup> &rarr; <b>P</b><sup>6</sup>
     * where <i>n</i> is the index of the given state <i>S<sub>n</sub></i>.  This is the
     * image &Delta;<b>z</b> of the value 
     * <b>0</b> &in; <b>P</b><sup>6</sup> &cong; <b>R</b><sup>6</sup> &times; {1}.  
     * That is the value &Delta;<b>z</b> = &phi;<sub><i>n</i></sub>(<b>0</b>).
     * </p>
     * <p>
     * Recall that the transfer
     * map &phi; is a <code>PhaseMap</code> object containing a first-order component which is
     * a linear operator on projective space <b>P</b><sup>6</sup>.  As such, this
     * <code>PhaseMatrix</code> object <b>&Phi;</b> is embedded in <b>R</b><sup>7&times;7</sup>.  
     * The 7<sup><i>th</i></sup> column &Delta;<b>z</b> of <b>&Phi;</b> is the column of 
     * translation operations on a phase vector 
     * <b>z</b> &in; <b>P</b><sup>6</sup> &sub; <b>R</b><sup>6</sup> &times; {1} since
     * <b>z</b> &in; <b>P</b><sup>6</sup> is represented 
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>z</b> = (<i>x, x', y, y', z, z', </i>1)<sup><i>T</i></sup> .
     * <br>
     * <br>  
     * Thus, the action of <b>&Phi;</b> can be (loosely) decomposed as 
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>&Phi; &sdot; z</b> = <b>Mz</b> + &Delta;<b>z</b> ,
     * <br>
     * <br>
     * where <b>M</b> &in; <i>Sp</i>(6), the symplectic group.  This method returns the
     * component &Delta;<b>z</b> of the transfer map.
     * </p>
     * 
     * @param   state   state containing location of returned translation vector
     * 
     * @return      the translation vector &Delta;<b>z</b> of the given transfer map
     *
     * @author Christopher K. Allen
     * @since  Oct 22, 2013
     */
    @Override
    public PhaseVector computeCoordinatePosition(TransferMapState state) {
        
        PhaseMatrix matPhi = this.calculateFullLatticeMatrixAt(state);
        R6          vecDel = matPhi.projectColumn(IND.HOM);
        
        PhaseVector vecTranslate = PhaseVector.embed(vecDel);
        
        return vecTranslate;
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
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>&Phi;z</b> = <b>z</b> .
     * <br>
     * <br> 
     * This method returns that vector <b>z</b>.  
     * </p>
     * <p>
     * Recall that the <i>homogeneous</i> transfer matrix <b>&Phi;</b> for the ring 
     * has final row that represents the translation <b>&Delta;</b> of the particle
     * for the circuit around the ring.  The 6&times;6 sub-matrix of <b>&Phi;</b> represents
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
     * where <b>p</b> is the projection of <b>z</b> onto the ambient phase space
     * <b>R</b><sup>6</sup> (without homogeneous the homogeneous coordinate).
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
     * <b>T</b> = proj<sub>4&times;4</sub> <b>&Phi;</b>.  The returned value is then
     * <b>z</b> = (<b>p</b> 0 0 1)<sup><i>T</i></sup>.
     * 
     * @param   state   state containing transfer map and location used in these calculations
     * 
     * @return              fixed point solution  (<i>x,x',y,y',z,z'</i>,1) for the given phase matrix
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
     * Computes the chromatic aberration for one pass around the ring starting at the given
     * state location, or from the entrance to state position for a linear
     * machine.  The returned vector is the displacement from the closed orbit caused 
     * by a unit momentum offset (&delta;<i>p</i> = 1).  See the documentation in 
     * {@link ISimLocResults#computeChromAberration(ProbeState)} for a more detailed
     * exposition.
     *
     * @see xal.tools.beam.calc.ISimLocResults#computeChromAberration(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 15, 2013
     */
    @Override
    public PhaseVector computeChromAberration(TransferMapState state) {
        double          dblGamma = state.getGamma();
//        PhaseMap        mapPhi   = state.getTransferMap();
//        PhaseMap        mapPhi   = state.getStateTransferMap();
//      PhaseMatrix     matPhi   = mapPhi.getFirstOrder();
        PhaseMatrix     matPhi   = this.calculateFullLatticeMatrixAt(state);
        
        R6              vecDel   = super.calculateAberration(matPhi, dblGamma);

        return PhaseVector.embed(vecDel);
    }

    
    /*
     * ISimEnvResults Interface
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
     * <br>
     * <br>
     * &nbsp; &nbsp; &alpha; &equiv; -<i>ww'</i> = (&phi;<sub>11</sub> - &phi;<sub>22</sub>)/(2 sin &sigma;) ,
     * <br>
     * <br>
     * &nbsp; &nbsp; &beta; &equiv; <i>w</i><sup>2</sup> = &phi;<sub>12</sub>/sin &sigma;
     * <br>
     * <br>
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
     * <br>
     * <br>
     * &nbsp; &nbsp; &sigma;(<i>s</i>) &equiv; &int;<sup><i>s</i></sup> [1/&beta;(<i>t</i>)]<i>dt</i> ,
     * <br>
     * <br>
     * where &beta;(<i>s</i>) is the Courant-Snyder, envelope function, and the integral 
     * is taken along the interval between the initial and final Courant-Snyder 
     * parameters.
     * </p>
     * <p>
     * The basic relation used to compute &sigma; is the following:
     * <br>
     * <br>
     * &nbsp; &nbsp;  &sigma; = sin<sup>-1</sup> &phi;<sub>12</sub>/(&beta;<sub>1</sub>&beta;<sub>2</sub>)<sup>&frac12;</sup> ,
     * <br>
     * <br>
     * where &phi;<sub>12</sub> is the element of <b>&Phi;</b> in the upper right corner of each 
     * 2&times;2 diagonal block, &beta;<sub>1</sub> is the initial beta function value (provided)
     * and &beta;<sub>2</sub> is the final beta function value (provided).
     * </p>
     * 
     * @param   state   state containing transfer map and location used in these calculations
     * 
     * @see calculatePhaseAdvance(PhaseMatrix, Twiss[], Twiss[])
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
     * <p>
     * Calculates the fixed point (closed orbit) in transverse phase space
     * at the given state location in the presence of dispersion.  
     * </p>
     * <p>
     * Let the full-turn map a the state location be denoted <b>&Phi;</b>.
     * The transverse plane dispersion vector <b>&Delta;</b> is defined  
     * <br>
     * <br> 
     * &nbsp; &nbsp; <b>&Delta;</b><sub><i>t</i></sub> &equiv; -(1/&gamma;<sup>2</sup>)[d<i>x</i>/d<i>z'</i>, d<i>x'</i>/d<i>z'</i>, d<i>y</i>/d<i>z'</i>, d<i>y'</i>/d<i>z'</i>]<sup><i>T</i></sup> .
     * <br>
     * <br>  
     * It can be identified as the first 4 entries of the 6<sup><i>th</i></sup> 
     * column in the transfer matrix <b>&Phi;</b>. The above vector
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
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>Tz</b><sub><i>t</i></sub> + &delta;<i>p</i><b>&Delta;</b><sub><i>t</i></sub> = <b>z</b><sub><i>t</i></sub> ,
     * <br>
     * <br>
     * which can be written
     * <br>
     * <br>
     * &nbsp; <b>z</b><sub><i>t</i></sub> = &delta;<i>p</i>(<b>T</b> - <b>I</b>)<sup>-1</sup><b>&Delta;</b><sub><i>t</i></sub> ,
     * <br>
     * <br>
     * where <b>I</b> is the identity matrix.  Dividing both sides by &delta;<i>p</i> yields the final
     * result
     * <br>
     * <br>
     * &nbsp; <b>z</b><sub>0</sub> &equiv; <b>z</b><sub><i>t</i></sub>/&delta;<i>p</i> = (<b>T</b> - <b>I</b>)<sup>-1</sup><b>&Delta;</b><sub><i>t</i></sub> ,
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
     * @see xal.tools.beam.calc.ISimEnvResults#computeChromDispersion(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 8, 2013
     */
    @Override
    public PhaseVector computeChromDispersion(TransferMapState state) {
        PhaseMatrix matFullTn = this.calculateFullLatticeMatrixAt(state);
        double      dblGamma  = state.getGamma();
        
//        double[]    arrDisp   = super.calculateDispersion(matFullTn, dblGamma);
//        R4          vecDispR4 = new R4(arrDisp);
        R4          vecDispR4 = super.calculateDispersion(matFullTn, dblGamma);
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
