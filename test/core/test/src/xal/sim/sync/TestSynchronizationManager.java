/**
 * TestSynchronizationManager.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 13, 2014
 */
package xal.sim.sync;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

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
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.run.TestRunOnlineModel;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.smf.impl.Quadrupole;
import xal.test.ResourceManager;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Oct 13, 2014
 */
public class TestSynchronizationManager {

    /*
     * Global Constants
     */
    
    /** PV Logger ID of machine state when data was taken */
    static public final long        LNG_PVLOGID = 19650065;
    
    
    /** URL of the accelerator hardware description file */
    static public String            STRL_URL_ACCEL   = ResourceManager.getTestAcceleratorURL().toString();

    
    /** Output file location */
    static private String           STR_FILE_OUTPUT = TestSynchronizationManager.class.getName().replace('.', '/') + ".txt";
    
    
    /** URL where we are dumping the output */
    static public File              FILE_OUTPUT    = ResourceManager.getOutputFile(STR_FILE_OUTPUT);
    
    
    /** String identifier for accelerator sequence used in testing */
//    static public String            STR_SEQ_ID       = "HEBT1";
//    static public String            STR_SEQ_ID       = "MEBT-SCL";
    static public String            STR_SEQ_ID       = "CCL";
    
    
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
//        this.printSimData(trjData);
    }
    
    /**
     * Test method for {@link xal.sim.sync.SynchronizationManager#resync()}.
     * 
     */
    @Test
    public final void testResync() {
        try {
            PROBE_ENV_TEST.reset();
            
            MODEL_TEST.setProbe(PROBE_ENV_TEST);
            MODEL_TEST.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
            MODEL_TEST.resync();

            MODEL_TEST.run();

            Trajectory<EnvelopeProbeState>   trjData = MODEL_TEST.getTrajectory();
            this.saveSimData(trjData);
            
            List<AcceleratorNode> lstSmfQuads = SEQ_TEST.getNodesOfType("q", true);
            Quadrupole            smfQuad1    = (Quadrupole)lstSmfQuads.get(0);
            double                dblFldOld   = smfQuad1.getDesignField();
            double                dblFldNew   = dblFldOld*1.1;
            
//            System.out.println("Changing " + smfQuad1.getId() + " design field from " + dblFldOld + " to " + dblFldNew);
            
            Map<String, Double> mapPrpToValOld = MODEL_TEST.propertiesForNode(smfQuad1);
//            System.out.println("Old property map for " + smfQuad1.getId() + ": " + mapPrpToValOld.toString());
            
            smfQuad1.setDfltField(dblFldNew);
            MODEL_TEST.resync();
            Map<String, Double> mapPrpToValNew = MODEL_TEST.propertiesForNode(smfQuad1);
//            System.out.println("New property map for " + smfQuad1.getId() + ": " + mapPrpToValNew.toString());
            
            PROBE_ENV_TEST.reset();
            MODEL_TEST.run();
            
            trjData = MODEL_TEST.getTrajectory();
            this.saveSimData(trjData);
                    

        } catch (SynchronizationException e) {
            e.printStackTrace();
            
            fail("Synchronziation exception " + e.getMessage());
            
        } catch (ModelException e) {
            e.printStackTrace();
            
            fail("Online model run exception " + e.getMessage());
        }
        
    }

    /**
     * Test method for {@link xal.sim.sync.SynchronizationManager#setModelInput(xal.smf.AcceleratorNode, java.lang.String, double)}.
     */
    @Test
    public final void testSetModelInput() {
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
        WTR_OUTPUT.println("  RF Gap Phases " + MODEL_TEST.getProbe().getAlgorithm().getRfGapPhaseCalculation() );
        
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
        System.out.println("  RF Gap Phases " + MODEL_TEST.getProbe().getAlgorithm().getRfGapPhaseCalculation() );
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
