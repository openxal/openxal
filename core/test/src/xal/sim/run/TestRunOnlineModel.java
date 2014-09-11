package xal.sim.run;

import java.io.File;
import java.io.PrintWriter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.alg.ParticleTracker;
import xal.model.alg.TransferMapTracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.TransferMapProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.probe.traj.TransferMapState;
import xal.test.ResourceManager;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;


/**
 * Check operation of the XAL online model.
 *
 * @author Christopher K. Allen
 * @since   Jul 30, 2012
 */
public class TestRunOnlineModel {


    /*
     * Global Constants
     */
    
    /** PV Logger ID of machine state when data was taken */
    static public final long        LNG_PVLOGID = 19650065;
    
    
    /** URL of the accelerator hardware description file */
    static public String            STRL_URL_ACCEL   = ResourceManager.getTestAcceleratorURL().toString();

    
    /** Output file location */
    static private String           STR_FILE_OUTPUT = TestRunOnlineModel.class.getName().replace('.', '/') + ".txt";
    
    
    /** URL where we are dumping the output */
    static public File              FILE_OUTPUT    = ResourceManager.getOutputFile(STR_FILE_OUTPUT);
    
    
    /** String identifier for accelerator sequence used in testing */
//    static public String            STR_SEQ_ID       = "HEBT1";
//    static public String            STR_SEQ_ID       = "MEBT-SCL";
    static public String            STR_SEQ_ID       = "CCL";
    
//    /** String identifier where Courant-Snyder parameters are to be reconstructed */
//    static public String            STR_TARG_ELEM_ID = "Begin_Of_HEBT1";
    
    
    /*
     * Global Attributes
     */
    
    /** Accelerator object used for testing */
    private static Accelerator                      ACCEL_TEST;
    
    /** Accelerator sequence used for testing */
    private static AcceleratorSeq                   SEQ_TEST;
    
    /** Accelerator sequence (online) model for testing */
    private static Scenario                         MODEL_TEST;
    
    
    /** Envelope probe for model testing */
    private static EnvelopeProbe                    PROBE_ENV_TEST;
    
    /** Particle probe for model testing */
    private static ParticleProbe                    PROBE_PARTL_TEST;
    
    /** Transfer map probe for model testing */
    private static TransferMapProbe                 PROBE_XFER_TEST;
    
    
    /** Persistent storage for test output */
    private static PrintWriter                      WTR_OUTPUT;
    
    
    /*
     * Global Methods
     */
    
    /**
     *
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Jul 16, 2012
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        try {
            ACCEL_TEST   = XMLDataManager.acceleratorWithUrlSpec(STRL_URL_ACCEL);
            SEQ_TEST     = ACCEL_TEST.findSequence(STR_SEQ_ID);
            MODEL_TEST   = Scenario.newScenarioFor(SEQ_TEST);
            
            // Create and initialize the envelope probe
            EnvTrackerAdapt algEnv = AlgorithmFactory.createEnvTrackerAdapt(SEQ_TEST);
            PROBE_ENV_TEST = ProbeFactory.getEnvelopeProbe(SEQ_TEST, algEnv);
            
            // Create and initialize the particle probe
            ParticleTracker algPrt = AlgorithmFactory.createParticleTracker(SEQ_TEST);
            PROBE_PARTL_TEST = ProbeFactory.createParticleProbe(SEQ_TEST, algPrt);
            
            // Create and initialize transfer map probe
            TransferMapTracker  algXfer = AlgorithmFactory.createTransferMapTracker(SEQ_TEST);
            PROBE_XFER_TEST = ProbeFactory.getTransferMapProbe(SEQ_TEST, algXfer );
            
            WTR_OUTPUT = new PrintWriter(FILE_OUTPUT);

        } catch (Exception e) {
            System.err.println("Unable to instantiate TransferMatrixObject");
            
        }
    }

    /**
     * Closes the output file stream.
     * 
     * @throws Exception
     *
     * @author Christopher K. Allen
     * @since  Jan 7, 2014
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        WTR_OUTPUT.close();
    }
    
    
    
    /*
     * Tests
     */
    
//    /**
//     * Test the <i>create default algorithm</i> method of the <code>{@link Tracker}</code>
//     * base class (see <code>{@link Tracker#newFromEditContext(AcceleratorSeq)}</code>).
//     * This method has been deprecated, however, and should not be used. 
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 29, 2012
//     */
//    @Test
//    public void testTrackerNewFromEditContext() {
//        IAlgorithm algDef = Tracker.newFromEditContext(SEQ_TEST);
//        
//        if (algDef == null) 
//            fail("Tracker#newFromEditContext() failure");
//    }
    
	/**
	 * Run the online model for an envelope probe.
	 *
	 * @throws ModelException          general synchronization or simulation error ?
	 *                                 
	 * @author Christopher K. Allen
	 * @since  Jul 20, 2012
	 */
	@Test
	public void testRunEnvelopeModel() throws ModelException {
		
        PROBE_ENV_TEST.reset();
        MODEL_TEST.setProbe(PROBE_ENV_TEST);
        MODEL_TEST.resync();
        MODEL_TEST.run();
        
        Trajectory<EnvelopeProbeState>   trjData = MODEL_TEST.getTrajectory();
        
        this.saveSimData(trjData);
	}
	
    /**
     * Run the online model for an envelope probe.
     *
     * @throws ModelException          general synchronization or simulation error ?
     *                                 
     * @author Christopher K. Allen
     * @since  Jul 20, 2012
     */
    @Test
    public void testRunEnvelopeModelWithRfGapCalc() throws ModelException {
        
        PROBE_ENV_TEST.reset();
    	PROBE_ENV_TEST.getAlgorithm().setRfGapPhaseCalculation(true);
        MODEL_TEST.setProbe(PROBE_ENV_TEST);
        MODEL_TEST.resync();
        MODEL_TEST.run();
        
        Trajectory<EnvelopeProbeState>   trjData = MODEL_TEST.getTrajectory();
        
        this.saveSimData(trjData);
        this.printSimData(trjData);
    }
    
    /**
     * Run the online model for a particle probe.
     *
     * @throws ModelException          general synchronization or simulation error ?
     *                                 
     * @author Christopher K. Allen
     * @since  Jul 20, 2012
     */
    @Test
    public void testRunParticleModel() throws ModelException {
        
        PROBE_PARTL_TEST.reset();
        MODEL_TEST.setProbe(PROBE_PARTL_TEST);
        MODEL_TEST.resync();
        MODEL_TEST.run();
        
        Trajectory<ParticleProbeState>   trjData = MODEL_TEST.getTrajectory();
        
        this.saveSimData(trjData);
    }
    
    /**
     * Run the online model for a particle probe.
     *
     * @throws ModelException          general synchronization or simulation error ?
     *                                 
     * @author Christopher K. Allen
     * @since  Jul 20, 2012
     */
    @Test
    public void testRunParticleModelWithRfGapPhases() throws ModelException {
        
        PROBE_PARTL_TEST.reset();
        PROBE_PARTL_TEST.getAlgorithm().setRfGapPhaseCalculation(true);
        MODEL_TEST.setProbe(PROBE_PARTL_TEST);
        MODEL_TEST.resync();
        MODEL_TEST.run();
        
        Trajectory<ParticleProbeState>   trjData = MODEL_TEST.getTrajectory();
        
        this.saveSimData(trjData);
        this.printSimData(trjData);
    }
    
    /**
     * Run the online model for a transfer map probe.
     *
     * @throws ModelException          general synchronization or simulation error ?
     *                                 
     * @author Christopher K. Allen
     * @since  Jul 20, 2012
     */
    @Test
    public void testRunTransferMapModel() throws ModelException {
        
        PROBE_XFER_TEST.reset();
        MODEL_TEST.setProbe(PROBE_XFER_TEST);
        MODEL_TEST.resync();
        MODEL_TEST.run();
        
        Trajectory<TransferMapState>    trjData = MODEL_TEST.getTrajectory();
        
        this.saveSimData(trjData);
    }
    
    /*
     * Support Methods
     */

    /**
     * Write the current simulation data to disk.
     *
     * @author Christopher K. Allen
     * @since  Jan 7, 2014
     */
    private <S extends ProbeState<S>> void saveSimData(Trajectory<S> trjData) {

        // Write out header line
        String  strSimType = MODEL_TEST.getProbe().getClass().getName();
        WTR_OUTPUT.println("DATA FOR SIMULATION WITH " + strSimType);
        WTR_OUTPUT.println("  RF Gap Phases " + MODEL_TEST.getProbe().getAlgorithm().useRfGapPhaseCalculation() );
        
        // Write out the simulation data
//        Trajectory<?> trjData = MODEL_TEST.getTrajectory();
        
        for (S state : trjData) {
            WTR_OUTPUT.println(state);
        }
        
        // Buffer for the next write
        WTR_OUTPUT.println();
        WTR_OUTPUT.flush();
    }
    
    /**
     * Prints the simulation data to stdout 
     *
     * @author Christopher K. Allen
     * @since  Jan 7, 2014
     */
    private <S extends ProbeState<S>>void printSimData(Trajectory<S> trjData) {

        // Print out the kinetic energy profile to stdout
        System.out.println("DATA FOR SIMULATION WITH " + MODEL_TEST.getProbe().getClass().getName());
        System.out.println("  RF Gap Phases " + MODEL_TEST.getProbe().getAlgorithm().useRfGapPhaseCalculation() );
//        Trajectory<?> trjData = MODEL_TEST.getTrajectory();
        
        for (S state : trjData) {
            
            String strId = state.getElementId();
            double dblW  = state.getKineticEnergy();
            
            System.out.println(strId + ": W = " + dblW);
        }

        System.out.println();
        System.out.println();
    }
}
