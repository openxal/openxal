/**
 * SimResultsAdaptor.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 12, 2013
 */
package xal.tools.beam.calc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.EnvelopeTrajectory;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.probe.traj.TransferMapState;
import xal.model.probe.traj.TransferMapTrajectory;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.math.r3.R3;

/**
 * <p>
 * Class <code>SimResultsAdaptor</code>.  This class generalized the
 * interface <code>ISimEnvelopeResults&lt;S&gt;</code> for use with simulation
 * results of either of the two types <code>TransferMapTrajectory</code>
 * or <code>EnvelopeTrajectory</code>.  The types to the methods of the
 * interface are specified as the general <code>ProbeState</code> base
 * class. Thus, probes states of either type <code>TransferMapState</code>
 * or <code>EnvelopeProbeState</code> may be passed to the interface
 * <code>ISimEnvelopeResults</code>. 
 * </p>
 * <p>
 * This class maintains an internal machine parameter calculation engine.
 * The type of engine depends on the type of simulation data.
 * Correct types and interpretations are determined at run time.
 * </p>
 *
 *
 * @author Christopher K. Allen
 * @since  Nov 7, 2013
 */
public class SimResultsAdaptor implements ISimEnvelopeResults<ProbeState>, ISimLocationResults<ProbeState> {

    
    /*
     * Local Attributes
     */
    
    /** The machine parameter calculation engine for rings */
    private ISimEnvelopeResults<TransferMapState>   cmpRingParams;
    
    /** The machine parameter calculation engine for linacs */
    private ISimEnvelopeResults<EnvelopeProbeState> cmpLinacParams;
    
    
    /*
     * Initialization
     */
    
    /**
     * Constructor for <code>SimResultsAdaptor</code>.  We create an internal
     * machine calculation engine based upon the type of the given simulation
     * trajectory. 
     *
     * @param datSim  simulation data that is going to be processed
     * 
     * @throws IllegalArgumentException the simulation data is of an unknown type
     *
     * @author Christopher K. Allen
     * @since  Nov 7, 2013
     */
    public SimResultsAdaptor(Trajectory datSim) throws IllegalArgumentException {

        // Create the machine parameter calculation engine according to the
        //    type of probe we are given
        if (datSim instanceof TransferMapTrajectory) {
            TransferMapTrajectory   trj = (TransferMapTrajectory)datSim;

            this.cmpRingParams  = new RingCalculations(trj);
            this.cmpLinacParams = null;

        } else if (datSim instanceof EnvelopeTrajectory) {
            EnvelopeTrajectory trj = (EnvelopeTrajectory)datSim;

            this.cmpLinacParams = new BeamCalculations(trj);
            this.cmpRingParams  = null;

        } else {

            throw new IllegalArgumentException("Unknown simulation data type " + datSim.getClass().getName());
        }
    }


    /* 
     * ISimLocationResults Interface
     */

    /**
     *
     * @see xal.tools.beam.calc.ISimEnvelopeResults#computeCoordinatePosition(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 7, 2013
     */
    @Override
    public PhaseVector computeCoordinatePosition(ProbeState state) {
        return (PhaseVector)this.compute("computePhaseCoordinates", state);
    }

    /**
     * <p>
     * For rings, this is the location of the fixed orbit (invariant under applications
     * of the full-turn map at the give state) about which betatron oscillations occur.
     * For linacs, this is the fixed point of the end-to-end transfer map.
     * </p>
     *
     * @see xal.tools.beam.calc.ISimEnvelopeResults#computeFixedOrbit(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 7, 2013
     */
    @Override
    public PhaseVector computeFixedOrbit(ProbeState state) {
        return (PhaseVector)this.compute("computeFixedOrbit", state);
    }

    /**
     *
     * @see xal.tools.beam.calc.ISimLocationResults#computeChromAberration(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 15, 2013
     */
    @Override
    public PhaseVector computeChromAberration(ProbeState state) {
        Object  objResult     = this.compute("computeChromaticAberration", state);
        PhaseVector vecResult = (PhaseVector)objResult;

        return vecResult;
    }


    /*
     * ISimEnvelopeResults Interface
     */

    /**
     * <p>
     * This returns the matched envelope Courant-Snyder parameters at the given state 
     * location for a ring.  For a linace the actual Courant-Snyder parameters for the beam
     * envelope (including space charge) are returned, computed from the beam's second
     * moments. 
     * </p>
     *
     * @see xal.tools.beam.calc.ISimEnvelopeResults#computeTwissParameters(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 7, 2013
     */
    @Override
    public Twiss[] computeTwissParameters(ProbeState state) throws IllegalArgumentException {

        Object  objResult = this.compute("computeTwissParameters", state);
        Twiss[] arrTwiss = (Twiss[])objResult;

        return arrTwiss;
    }

    /**
     * Compute and return the betatron phase &psi; at this state location.  Depending upon the
     * context, this value could be the phase advance of a particle when traversing a ring at 
     * this location, or the
     * phase advance from entrance location of a linac to current position.  In either case
     * the result should be in the range 0 to 2&pi;.
     *
     * @see xal.tools.beam.calc.ISimEnvelopeResults#computeBetatronPhase(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 7, 2013
     */
    @Override
    public R3 computeBetatronPhase(ProbeState state) {
        return (R3)this.compute("computeBetatronPhase",  state);
    }

    /**
     * <p>
     * Computes and returns the dispersion coefficients for the machine and/or beam.
     * </p>
     * <p>
     * For rings this is sensitivity of the transverse fixed orbit to the off energy
     * component of the beam.  For linacs the sensitivity of any position in phase space
     * to the chromatic dispersion 
     * &delta; &equiv; (<i>p</i> - <i>p</i><sub>0</sub>)/<i>p</i><sub>0</sub>.  This is 
     * because in linac calculation we use the state response matrix (including space charge)
     * which expresses the sensitivity of the final states to changes in the initial state.
     * </p>
     *
     * @see xal.tools.beam.calc.ISimEnvelopeResults#computeChromDispersion(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 11, 2013
     */
    @Override
    public PhaseVector computeChromDispersion(ProbeState state) {
        return (PhaseVector)this.compute("computeChromDispersion", state);
    }


    /*
     * Support Methods
     */

    /**
     * Determines the sub-type of the <code>ProbeState</code> object
     * and uses that information to determine which <code>ISimEnvelopeResults</code>
     * computation engine is used to compute the machine parameters.  (That is,
     * are we looking at a ring or at a Linac.)  The result is passed back up to the
     * <code>ISimEnvelopeResults</code> interface exposed by this class (i.e.,
     * the interface method that invoked this method) where its type is identified
     * and returned to the user of this class.
     * 
     * @param strMthName    name of the method in the <code>ISimEnvelopeResults</code> interface
     * @param staArg        <code>ProbeState</code> derived object that is an argument to one of the
     *                      methods in the <code>ISimEnvelopeResults</code> interface
     *                      
     * @return              result of invoking the given <code>ISimEnvelopeResults</code> method on 
     *                      the given <code>ProbeState</code> argument
     *  
     * @author Christopher K. Allen
     * @since  Nov 8, 2013
     */
    private Object  compute(String strMthName, ProbeState staArg) {

        try {

            if (staArg instanceof TransferMapState) {
                TransferMapState    staXfer = (TransferMapState)staArg;

                Method mthCmp;
                mthCmp = this.cmpRingParams.getClass().getDeclaredMethod(strMthName, TransferMapState.class);
                Object  objRes = mthCmp.invoke(this.cmpRingParams, staXfer);

                return objRes;

            } else if (staArg instanceof EnvelopeProbeState) {
                EnvelopeProbeState    staEnv = (EnvelopeProbeState)staArg;

                Method  mthCmp = this.cmpLinacParams.getClass().getDeclaredMethod(strMthName, EnvelopeProbeState.class);
                Object  objRes = mthCmp.invoke(this.cmpLinacParams, staEnv);

                return objRes;

            } else {

                throw new IllegalArgumentException("Unknown probe state type " + staArg.getClass().getName());
            }

        } catch (ClassCastException | 
                NoSuchMethodException | 
                SecurityException | 
                IllegalAccessException | 
                IllegalArgumentException | 
                InvocationTargetException e
                ) {

            throw new IllegalArgumentException("Included exception thrown invoking method " + strMthName, e);
        }

    }
}