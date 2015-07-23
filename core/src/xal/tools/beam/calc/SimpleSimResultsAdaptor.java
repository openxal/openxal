/**
 * SimpleSimResultsAdaptor.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 12, 2013
 */
package xal.tools.beam.calc;

import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.probe.traj.TransferMapState;

/**
 * <p>
 * This class reduces the general operation of the base class
 * <code>SimResultsAdaptor</code> to the specific use of the calculation
 * engine <code>{@link CalculationsOnRings}</code> for simulation data
 * of type <code>TransferMapTrajectory</code>, and use of calculation
 * engine <code>{@link CalculationsOnBeams}</code> for simulation data
 * of type <code>EnvelopeTrajectory</code>.
 * Thus, probes states of either type <code>TransferMapState</code>
 * or <code>EnvelopeProbeState</code> may be passed to the interface, 
 * according to which trajectory type was passed to the constructor.
 * </p>
 * <p>
 * Again, note that this adaptor will not recognize any simulation data other
 * that the type <code>TransferMapTrajectory</code> and 
 * <code>EnvelopeTrajectory</code>.
 * </p>
 * <h3>NOTE:</h3>
 * <p>
 * - Calculations for the <code>ParticleProbeTrajectory</code> have been added.  The
 * calculation engine is <code>CalculationsOnParticles</code>.  Note that only the
 * methods of interface <code>ISimLocResults</code> will be recognized.  Methods of
 * interface <code>ISimEnvResults</code> will results in an exception.
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Nov 7, 2013
 * 
 * @see SimResultsAdaptor
 * @see CalculationsOnRings
 * @see CalculationsOnBeams
 */
public class SimpleSimResultsAdaptor extends SimResultsAdaptor {

    
    /*
     * Local Attributes
     */
    
//    /** The machine parameter calculation engine for rings */
//    private ISimEnvResults<TransferMapState>   cmpRingParams;
//    
//    /** The machine parameter calculation engine for linacs */
//    private ISimEnvResults<EnvelopeProbeState> cmpLinacParams;
    
    
    /*
     * Initialization
     */
    
    /**
     * Constructor for <code>SimpleSimResultsAdaptor</code>.  We create an internal
     * machine calculation engine based upon the type of the given simulation
     * trajectory. 
     *
     * @param trajectory  simulation data that is going to be processed
     * 
     * @throws IllegalArgumentException the simulation data is of an unknown type
     *
     * @author Christopher K. Allen
     * @since  Nov 7, 2013
     */
	public SimpleSimResultsAdaptor(Trajectory<?> trajectory) throws IllegalArgumentException {
        super();
        

        Class<?> clsTrajState = trajectory.getStateClass();
        
        if ( clsTrajState.equals(TransferMapState.class) ) {
//        	SimResultsAdaptor<TransferMapState> sra = new SimResultsAdaptor<TransferMapState>();
            @SuppressWarnings("unchecked")
            CalculationsOnRings calRings  = new CalculationsOnRings((Trajectory<TransferMapState>)trajectory);
            super.registerCalcEngine(TransferMapState.class, calRings);;

        } else if (clsTrajState.equals(EnvelopeProbeState.class)) {
//        	SimResultsAdaptor<EnvelopeProbeState> sra = new SimResultsAdaptor<EnvelopeProbeState>();
            @SuppressWarnings("unchecked")
            CalculationsOnBeams calBeams = new CalculationsOnBeams((Trajectory<EnvelopeProbeState>)trajectory);
            super.registerCalcEngine(EnvelopeProbeState.class, calBeams);
            
        } else if (clsTrajState.equals(ParticleProbeState.class)) {  
//        	SimResultsAdaptor<ParticleProbeState> sra = new SimResultsAdaptor<ParticleProbeState>();
            @SuppressWarnings("unchecked")
            CalculationsOnParticles calPart = new CalculationsOnParticles((Trajectory<ParticleProbeState>)trajectory);
            super.registerCalcEngine(ParticleProbeState.class, calPart);

        } else {

            throw new IllegalArgumentException("Unknown simulation data type " + trajectory.getClass().getName());
        }
        
//        // Create the machine parameter calculation engine according to the
//        //    type of probe we are given
//        if (datSim instanceof TransferMapTrajectory) {
//            TransferMapTrajectory   trj = (TransferMapTrajectory)datSim;
//
//            this.cmpRingParams  = new CalculationsOnRings(trj);
//            this.cmpLinacParams = null;
//
//        } else if (datSim instanceof EnvelopeTrajectory) {
//            EnvelopeTrajectory trj = (EnvelopeTrajectory)datSim;
//
//            this.cmpLinacParams = new CalculationsOnBeams(trj);
//            this.cmpRingParams  = null;
//
//        } else {
//
//            throw new IllegalArgumentException("Unknown simulation data type " + datSim.getClass().getName());
//        }
    }


//    /* 
//     * ISimLocResults Interface
//     */
//
//    /**
//     *
//     * @see xal.tools.beam.calc.ISimEnvResults#computeCoordinatePosition(xal.model.probe.traj.ProbeState)
//     *
//     * @author Christopher K. Allen
//     * @since  Nov 7, 2013
//     */
//    @Override
//    public PhaseVector computeCoordinatePosition(ProbeState state) {
//        return (PhaseVector)this.compute("computePhaseCoordinates", state);
//    }
//
//    /**
//     * <p>
//     * For rings, this is the location of the fixed orbit (invariant under applications
//     * of the full-turn map at the give state) about which betatron oscillations occur.
//     * For linacs, this is the fixed point of the end-to-end transfer map.
//     * </p>
//     *
//     * @see xal.tools.beam.calc.ISimEnvResults#computeFixedOrbit(xal.model.probe.traj.ProbeState)
//     *
//     * @author Christopher K. Allen
//     * @since  Nov 7, 2013
//     */
//    @Override
//    public PhaseVector computeFixedOrbit(ProbeState state) {
//        return (PhaseVector)this.compute("computeFixedOrbit", state);
//    }
//
//    /**
//     *
//     * @see xal.tools.beam.calc.ISimLocResults#computeChromAberration(xal.model.probe.traj.ProbeState)
//     *
//     * @author Christopher K. Allen
//     * @since  Nov 15, 2013
//     */
//    @Override
//    public PhaseVector computeChromAberration(ProbeState state) {
//        Object  objResult     = this.compute("computeChromaticAberration", state);
//        PhaseVector vecResult = (PhaseVector)objResult;
//
//        return vecResult;
//    }
//
//
//    /*
//     * ISimEnvResults Interface
//     */
//
//    /**
//     * <p>
//     * This returns the matched envelope Courant-Snyder parameters at the given state 
//     * location for a ring.  For a linace the actual Courant-Snyder parameters for the beam
//     * envelope (including space charge) are returned, computed from the beam's second
//     * moments. 
//     * </p>
//     *
//     * @see xal.tools.beam.calc.ISimEnvResults#computeTwissParameters(xal.model.probe.traj.ProbeState)
//     *
//     * @author Christopher K. Allen
//     * @since  Nov 7, 2013
//     */
//    @Override
//    public Twiss[] computeTwissParameters(ProbeState state) throws IllegalArgumentException {
//
//        Object  objResult = this.compute("computeTwissParameters", state);
//        Twiss[] arrTwiss = (Twiss[])objResult;
//
//        return arrTwiss;
//    }
//
//    /**
//     * Compute and return the betatron phase &psi; at this state location.  Depending upon the
//     * context, this value could be the phase advance of a particle when traversing a ring at 
//     * this location, or the
//     * phase advance from entrance location of a linac to current position.  In either case
//     * the result should be in the range 0 to 2&pi;.
//     *
//     * @see xal.tools.beam.calc.ISimEnvResults#computeBetatronPhase(xal.model.probe.traj.ProbeState)
//     *
//     * @author Christopher K. Allen
//     * @since  Nov 7, 2013
//     */
//    @Override
//    public R3 computeBetatronPhase(ProbeState state) {
//        return (R3)this.compute("computeBetatronPhase",  state);
//    }
//
//    /**
//     * <p>
//     * Computes and returns the dispersion coefficients for the machine and/or beam.
//     * </p>
//     * <p>
//     * For rings this is sensitivity of the transverse fixed orbit to the off energy
//     * component of the beam.  For linacs the sensitivity of any position in phase space
//     * to the chromatic dispersion 
//     * &delta; &equiv; (<i>p</i> - <i>p</i><sub>0</sub>)/<i>p</i><sub>0</sub>.  This is 
//     * because in linac calculation we use the state response matrix (including space charge)
//     * which expresses the sensitivity of the final states to changes in the initial state.
//     * </p>
//     *
//     * @see xal.tools.beam.calc.ISimEnvResults#computeChromDispersion(xal.model.probe.traj.ProbeState)
//     *
//     * @author Christopher K. Allen
//     * @since  Nov 11, 2013
//     */
//    @Override
//    public PhaseVector computeChromDispersion(ProbeState state) {
//        return (PhaseVector)this.compute("computeChromDispersion", state);
//    }
//
//
//    /*
//     * Support Methods
//     */
//
//    /**
//     * Determines the sub-type of the <code>ProbeState</code> object
//     * and uses that information to determine which <code>ISimEnvResults</code>
//     * computation engine is used to compute the machine parameters.  (That is,
//     * are we looking at a ring or at a Linac.)  The result is passed back up to the
//     * <code>ISimEnvResults</code> interface exposed by this class (i.e.,
//     * the interface method that invoked this method) where its type is identified
//     * and returned to the user of this class.
//     * 
//     * @param strMthName    name of the method in the <code>ISimEnvResults</code> interface
//     * @param staArg        <code>ProbeState</code> derived object that is an argument to one of the
//     *                      methods in the <code>ISimEnvResults</code> interface
//     *                      
//     * @return              result of invoking the given <code>ISimEnvResults</code> method on 
//     *                      the given <code>ProbeState</code> argument
//     *  
//     * @author Christopher K. Allen
//     * @since  Nov 8, 2013
//     */
//    private Object  compute(String strMthName, ProbeState staArg) {
//
//        try {
//
//            if (staArg instanceof TransferMapState) {
//                TransferMapState    staXfer = (TransferMapState)staArg;
//
//                Method mthCmp;
//                mthCmp = this.cmpRingParams.getClass().getDeclaredMethod(strMthName, TransferMapState.class);
//                Object  objRes = mthCmp.invoke(this.cmpRingParams, staXfer);
//
//                return objRes;
//
//            } else if (staArg instanceof EnvelopeProbeState) {
//                EnvelopeProbeState    staEnv = (EnvelopeProbeState)staArg;
//
//                Method  mthCmp = this.cmpLinacParams.getClass().getDeclaredMethod(strMthName, EnvelopeProbeState.class);
//                Object  objRes = mthCmp.invoke(this.cmpLinacParams, staEnv);
//
//                return objRes;
//
//            } else {
//
//                throw new IllegalArgumentException("Unknown probe state type " + staArg.getClass().getName());
//            }
//
//        } catch (ClassCastException | 
//                NoSuchMethodException | 
//                SecurityException | 
//                IllegalAccessException | 
//                IllegalArgumentException | 
//                InvocationTargetException e
//                ) {
//
//            throw new IllegalArgumentException("Included exception thrown invoking method " + strMthName, e);
//        }
//
//    }
}