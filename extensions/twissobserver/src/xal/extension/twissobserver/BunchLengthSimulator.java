/**
 * BunchLengthSimulator.java
 *
 * @author  Christopher K. Allen
 * @since	Sep 6, 2012
 */
package xal.extension.twissobserver;

import java.util.ArrayList;

import xal.tools.beam.CovarianceMatrix;
import xal.extension.twissobserver.TransferMatrixGenerator.SYNC;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorSeq;

/**
 * This class is an auxiliary tool for providing the longitudinal component of a 
 * <code>{@link Measurement}</code> object.  Typically, a measurement object is 
 * populated with wire scanner data, which contains only transverse information.
 * In order to do computation with space charge it is necessary to have longitudinal
 * information since dynamics in all three phase planes are coupled by the electro-
 * magnetic fields.  This tools provides a simulated longitudinal bunch size to
 * fill in the missing information.
 *
 * @author Christopher K. Allen
 * @since   Sep 6, 2012
 */
public class BunchLengthSimulator {

    
    /*
     * Local Attributes
     */
    /** Accelerator sequence where transfer maps are being computed */
    private AcceleratorSeq     smfSeq;
    

    /** Probe used to generate transfer matrices */
    private EnvelopeProbe     mdlProbe;
    
    /** Model of the above hardware */
    private Scenario           mdlBeamline;
    
    


    
    /*
     * Initialization
     */
    
    /**
     * Creates a new <code>BunchLengthSimulator</code> object using the 
     * given sequence, and synchronizes to the design parameters.
     * 
     * @param   smfSeq   the desired sequence for bunch length computation
     *
     * @throws ModelException  and error occurred when instantiating the machine model
     */
    public BunchLengthSimulator(AcceleratorSeq  smfSeq) throws ModelException {
        this(smfSeq, SYNC.DESIGN);
    }

    /**
     * Creates a new <code>BunchLengthSimulator</code> object using 
     * the given sequence, initializes the model to the historical machine state identified
     * by the given PV Logger ID.
     * 
     * @param smfSeq       the desired sequence for bunch length computation
     * @param lngPvLogId   PV Logger ID of the historical machine snapshot where model parameters are taken  

     * @throws ModelException  and error occurred when instantiating the machine model
     */
    public BunchLengthSimulator(AcceleratorSeq  smfSeq, long lngPvLogId) throws ModelException {
        this(smfSeq);
        this.setSyncToMachineHistory(lngPvLogId);
    }
    
    /**
     * Creates a new <code>BunchLengthSimulator</code> object using the given accelerator (XML definition file)
     * accelerator, the given sequence, and the given synchronization mode.
     * 
     * @param smfSeq       the desired sequence for bunch length computation
     * @param enmSyn       source of machine parameters

     * @throws ModelException           an error occurred when instantiating the machine model
     */
    public BunchLengthSimulator(AcceleratorSeq  smfSeq, SYNC enmSyn) throws ModelException {
        
        // Store the sequence we are working in
        this.smfSeq = smfSeq;
        
        // Create the finite space charge probe
        try {
            EnvTrackerAdapt    algEnvTrk = AlgorithmFactory.createEnvTrackerAdapt(smfSeq);
//            algEnvTrk.setMaxIterations(1000);
//            algEnvTrk.setDebugMode(true);

            // Create and initialize the envelope probe
            this.mdlProbe = ProbeFactory.getEnvelopeProbe(this.smfSeq, algEnvTrk);

        } catch (InstantiationException e) {

            throw new ModelException("Unable to instantial algorithm ", e);
        }
        
        // Create the model and load the parameters from the saved PV Logger data
        this.mdlBeamline = Scenario.newScenarioFor(smfSeq);
        this.setSynchronizationMode(enmSyn);
    }
    
    /**
     * Sets the source of hardware parameters for the simulations.  The given
     * synchronization mode determines where this parameters are taken from (e.g., the live machine,
     * the design parameters, etc.).  It is also possible to synchronize the hardware
     * parameters to a historical machine state with a know PV Logger ID 
     * (see <code>{@link #setSyncToMachineHistory(long)}</code>).
     *
     * @param enmSync  synchronization mode determining parameter source
     *
     * @author Christopher K. Allen
     * @since  Jul 19, 2012
     */
    public void setSynchronizationMode(SYNC enmSync) {
        this.mdlBeamline.setSynchronizationMode(enmSync.getSynchronizationValue());
    }
    
    /**
     * Synchronizes to the machine parameters at the time of the given PV logger
     * snapshot ID.  The bunch lengths provided will be from this machine state.
     *
     * @param lngPvLogId   PV Logger ID for the historical machine state
     *
     * @author Christopher K. Allen
     * @since  Jul 19, 2012
     */
    public void setSyncToMachineHistory(final long lngPvLogId) {
        
        PVLoggerDataSource  srcPvLog = new PVLoggerDataSource(lngPvLogId);
        this.mdlBeamline = srcPvLog.setModelSource(this.smfSeq, this.mdlBeamline);
    }
    
    /**
     * <p>
     * Generates all the bunch lengths at the given device locations for the given
     * measurement data.  The default initial beam state is used to start the beam
     * simulation at the entrance of the accelerator sequence. 
     * </p>
     *
     * @param arrMsmts         the measurement data to be packed with longitudinal dummy data 
     * @param dblBmChrg        beam bunch charge in Coulombs
     * 
     * @throws ModelException  general error during model synchronization or simulation
     *
     * @author Christopher K. Allen
     * @since  Sep 6, 2012
     */
    public void generateBunchLengths(ArrayList<Measurement> arrMsmts, double dblBnchFreq, double dblBmCurr)
        throws ModelException
    {
        this.generateBunchLengths(arrMsmts, 1.0, dblBnchFreq, dblBmCurr);
    }
    
    /**
     * <p>
     * Generates all the bunch lengths at the given device locations for the given
     * measurement data.  The default initial beam state is used to start the beam
     * simulation at the entrance of the accelerator sequence. The beam sizes are multiplied 
     * by the scaling factor provided; this is used to convert units since the online 
     * model units are all MKS.  For example, use 1000.0 to convert from meters (XAL) to millimeters.  
     * </p>
     *
     * @param arrMsmts         the measurement data to be packed with longitudinal dummy data 
     * @param dblScale         multiplicative factor used to scale bunch lengths. 
     * @param dblBmChrg        beam bunch charge in Coulombs
     * 
     * @throws ModelException  general error during model synchronization or simulation
     *
     * @author Christopher K. Allen
     * @since  Sep 6, 2012
     */
    public void generateBunchLengths(ArrayList<Measurement> arrMsmts, double dblScale, double dblBnchFreq, double dblBmCurr)
        throws ModelException
    {
        this.generateBunchLengths(arrMsmts, dblScale, dblBnchFreq, dblBmCurr, null);
    }
    
    /**
     * <p>
     * Generates all the bunch lengths at the given device locations for the given
     * measurement data.  The simulation is performed with the given bunch charge
     * and the given initial beam state at the start
     * of the accelerator sequence.   The beam sizes are multiplied by the scaling factor
     * provided; this is used to convert units since the online model units are all
     * MKS.  For example, use 1000.0 to convert from meters (XAL) to millimeters.  
     * </p>
     *
     * @param arrMsmts         the measurement data to be packed with longitudinal dummy data
     * @param dblScale         multiplicative factor used to scale bunch lengths. 
     * @param dblBmChrg        beam bunch charge in Coulombs
     * @param matInitState     initial state of the beam, initial covariance matrix or the default value is used if <code>null</code>
     * 
     * @throws ModelException  general error during model synchronization or simulation
     *
     * @author Christopher K. Allen
     * @since  Jul 26, 2012
     */
    public void generateBunchLengths(ArrayList<Measurement> arrMsmts, double dblScale, double dblBnchFreq, double dblBmCurr, CovarianceMatrix matInitState) 
        throws ModelException 
    {
        Trajectory<EnvelopeProbeState>  trjEnv = this.runSimulation(dblBnchFreq, dblBmCurr, matInitState);
        
        for (Measurement msmt : arrMsmts) {
            EnvelopeProbeState steProbe  = trjEnv.stateForElement(msmt.strDevId);
            
            CovarianceMatrix   matCov    = steProbe.getCovarianceMatrix();
            double             dblSigLng = matCov.getSigmaZ()*dblScale;
            
            msmt.dblSigLng = dblSigLng;
        }
    }
    
    
    /*
     * Internal Support
     */
    
    /**
     * Runs the actual online model simulation.  Returns the trajectory object resulting
     * from the simulation 
     *
     * @param dblBmChrg        bunch charge in Coulombs 
     * @param matInitState     initial state of the beam, initial covariance matrix
     * 
     * @return  the envelope trajectory resulting from the simulation
     * 
     * @throws ModelException   the simulation trajectory was not of type <code>EnvelopeTrajectory</code> 
     *
     * @author Christopher K. Allen
     * @since  Sep 6, 2012
     */
    private Trajectory<EnvelopeProbeState>  runSimulation(double dblBnchFreq, double dblBmCurr, CovarianceMatrix matInitState) 
        throws ModelException 
    {

        // Create and initialize the envelope probe
        if (matInitState != null)
            this.mdlProbe.setCovariance(matInitState);
        this.mdlProbe.setBunchFrequency(dblBnchFreq);
        this.mdlProbe.setBeamCurrent(dblBmCurr);
        this.mdlProbe.reset();
        
        // Load the probe into the model, synchronize the model parameters, 
        //     set the start location, and run
        this.mdlBeamline.setProbe(this.mdlProbe);
        this.mdlBeamline.resync();
        this.mdlBeamline.run();
        
        // Extract and type the trajectory
        Trajectory<EnvelopeProbeState> trjBase = this.mdlBeamline.getTrajectory();
        
        return trjBase;
    }
}
