/**
 * TestSimResultsAdaptor.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 19, 2013
 */
package xal.tools.beam.calc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.alg.EnvTrackerAdapt;
import xal.model.alg.ParticleTracker;
import xal.model.alg.TransferMapTracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.TransferMapProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.TransferMapState;
import xal.model.probe.traj.Trajectory;
import xal.test.ResourceManager;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss3D;
import xal.tools.math.r3.R3;

/**
 * Test cases for the <code>SimResultsAdaptor</code> class. 
 *
 * @author Christopher K. Allen
 * @since  Nov 19, 2013
 */
public class TestSimResultsAdaptor {

    
    /*
     * Global Constants
     */
    
    /** Output file location */
    static private String             STR_OUTPUT = TestSimResultsAdaptor.class.getName().replace('.', '/') + ".txt";
    
    /** String identifier for accelerator sequence used in testing */
    static private String            STR_SEQ_ID       = "Ring";
    

    /*
     * Global Attributes 
     */
    
    /** The file where we send the testing output */
    private static FileWriter                       OWTR_OUTPUT;
    
    
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
        
//        ResourceManager.clearAllFileLocations();
        
        try {
            
            File fileOutput = ResourceManager.getOutputFile(STR_OUTPUT);
            OWTR_OUTPUT = new FileWriter(fileOutput);
            
            ACCEL_TEST   = ResourceManager.getTestAccelerator();
            SEQ_TEST     = ACCEL_TEST.findSequence(STR_SEQ_ID);
            MODEL_TEST   = Scenario.newScenarioFor(SEQ_TEST);
            MODEL_TEST.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
            
            // Create and initialize the particle probe
            ParticleTracker algPart = AlgorithmFactory.createParticleTracker(SEQ_TEST);
            PROBE_PARTL_TEST = ProbeFactory.createParticleProbe(SEQ_TEST, algPart);
            PROBE_PARTL_TEST.reset();
            MODEL_TEST.setProbe(PROBE_PARTL_TEST);
            MODEL_TEST.resync();
            MODEL_TEST.run();
            
//            System.out.println("\nParticleProbe Trajectory");
//            Trajectory<ParticleProbeState> trjPart = (Trajectory<ParticleProbeState>) MODEL_TEST.getTrajectory();
//            System.out.println(trjPart);

            // Create and initialize transfer map probe
            TransferMapTracker algXferMap = AlgorithmFactory.createTransferMapTracker(SEQ_TEST);
            PROBE_XFER_TEST = ProbeFactory.getTransferMapProbe(SEQ_TEST, algXferMap );
            PROBE_XFER_TEST.reset();
            MODEL_TEST.setProbe(PROBE_XFER_TEST);
            MODEL_TEST.resync();
            MODEL_TEST.run();
            
//            System.out.println("\nTransferMap Trajectory");
//            Trajectory<TransferMapState> trjTrnsMap = (Trajectory<TransferMapState>) MODEL_TEST.getTrajectory();
//            System.out.println(trjTrnsMap);

            // Create and initialize the envelope probe
            EnvTrackerAdapt algEnv = AlgorithmFactory.createEnvTrackerAdapt(SEQ_TEST);
            PROBE_ENV_TEST = ProbeFactory.getEnvelopeProbe(SEQ_TEST, algEnv);
            PROBE_ENV_TEST.reset();
            MODEL_TEST.setProbe(PROBE_ENV_TEST);
            MODEL_TEST.resync();
            MODEL_TEST.run();
            
//            System.out.println("\nEnvelopeProbe Trajectory");
//            Trajectory<EnvelopeProbeState> trjEnv = (Trajectory<EnvelopeProbeState>) MODEL_TEST.getTrajectory();
//            System.out.println(trjEnv);
            
        } catch (Exception e) {
			System.out.println( "Exception: " + e );
			e.printStackTrace();
            System.err.println("Unable to initial the static test resources");
            
        }
    }

    /**
     *
     *
     * @author Christopher K. Allen
     * @since  Nov 9, 2011
     */
    @AfterClass
    public static void commonCleanup() throws IOException {
        OWTR_OUTPUT.flush();
        OWTR_OUTPUT.close();
    }

    
    /*
    * Local Attributes
    */
   
    /** Calculation engine for particle parameters using particle probe states */
    private CalculationsOnParticles       calPartPart;
    
    /** Calculation engine for machine parameters using transfer map states */
    private CalculationsOnMachines       calXferMach;
    
    /** Calculation engine for ring parameters using transfer map states */
    private CalculationsOnRings          calXferRing;
    
    /** Calculation engine for beam parameters using envelope probe states */
    private CalculationsOnBeams           calEnvBeam;
    
    
   /** the simulation adaptor */
   private SimResultsAdaptor           cmpSimResults;
   
   /**
    *
    * @throws java.lang.Exception
    *
    * @author Christopher K. Allen
    * @since  May 3, 2011
    */
   @Before
   public void setUp() throws Exception {
       this.calPartPart = new CalculationsOnParticles( PROBE_PARTL_TEST.getTrajectory() );
       this.calXferMach = new CalculationsOnMachines( PROBE_XFER_TEST.getTrajectory() );
       this.calXferRing = new CalculationsOnRings(  PROBE_XFER_TEST.getTrajectory() );
       this.calEnvBeam  = new CalculationsOnBeams( PROBE_ENV_TEST.getTrajectory() );
       
       this.cmpSimResults = new SimResultsAdaptor();

       this.cmpSimResults.registerCalcEngine(ParticleProbeState.class, this.calPartPart);
       this.cmpSimResults.registerCalcEngine(TransferMapState.class, this.calXferMach);
       this.cmpSimResults.registerCalcEngine(EnvelopeProbeState.class, this.calEnvBeam);
}

   
   /*
    * Tests
    */
   
    /**
     * Test method for {@link xal.tools.beam.calc.SimResultsAdaptor#registerCalcEngine(java.lang.Class, xal.tools.beam.calc.ISimulationResults)}.
     */
    @Test
    public void testRegisterCalcEngine() {
        this.cmpSimResults.registerCalcEngine(ParticleProbeState.class, this.calPartPart);
        this.cmpSimResults.registerCalcEngine(TransferMapState.class, this.calXferMach);
        this.cmpSimResults.registerCalcEngine(EnvelopeProbeState.class, this.calEnvBeam);
        
        this.cmpSimResults.registerCalcEngine(TransferMapState.class, this.calXferRing);
    }

    /**
     * Test method for {@link xal.tools.beam.calc.SimResultsAdaptor#computeCoordinatePosition(xal.model.probe.traj.ProbeState)}.
     * @throws IOException 
     */
    @Test
    public void testComputeCoordinatePosition() throws IOException {

        // Do computations on the particle trajectory
        OWTR_OUTPUT.write("\nParticleTrajectory: computeCordinatePosition");
        OWTR_OUTPUT.write("\n");
        Trajectory<ParticleProbeState>  trjPart = PROBE_PARTL_TEST.getTrajectory();
        for (ParticleProbeState state : trjPart) {
            PhaseVector vecPos = this.cmpSimResults.computeCoordinatePosition(state);
            
            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPos.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");
        
        // Do computations on the transfer map trajectory
        OWTR_OUTPUT.write("\nTransferMapTrajectory: computeCoordinatePosition");
        OWTR_OUTPUT.write("\n");
        Trajectory<TransferMapState>   trjXfer = PROBE_XFER_TEST.getTrajectory();
        for (TransferMapState state : trjXfer) {
            PhaseVector vecPos = this.cmpSimResults.computeCoordinatePosition(state);
            
            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPos.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");

        // Do computations on the envelope trajectory
        OWTR_OUTPUT.write("\nEnvelopeTrajectory: computeCoordinatePosition");
        OWTR_OUTPUT.write("\n");
        Trajectory<EnvelopeProbeState>   trjEnv = PROBE_ENV_TEST.getTrajectory();
        for (EnvelopeProbeState state : trjEnv) {
            PhaseVector vecPos = this.cmpSimResults.computeCoordinatePosition(state);
            
            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPos.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");
}

    /**
     * Test method for {@link xal.tools.beam.calc.SimResultsAdaptor#computeFixedOrbit(xal.model.probe.traj.ProbeState)}.
     * @throws IOException 
     */
    @Test
    public void testComputeFixedOrbit() throws IOException {

        // Do computations on the particle trajectory
        OWTR_OUTPUT.write("\nParticleTrajectory: computeFixedOrbit");
        OWTR_OUTPUT.write("\n");
        Trajectory<ParticleProbeState>  trjPart = PROBE_PARTL_TEST.getTrajectory();
        for (ParticleProbeState state : trjPart) {
            PhaseVector vecPos = this.cmpSimResults.computeFixedOrbit(state);

            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPos.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");

        // Do computations on the transfer map trajectory
        OWTR_OUTPUT.write("\nTransferMapTrajectory: computeFixedOrbit");
        OWTR_OUTPUT.write("\n");
        Trajectory<TransferMapState>   trjXfer = PROBE_XFER_TEST.getTrajectory();
        for (TransferMapState state : trjXfer) {
            PhaseVector vecPos = this.cmpSimResults.computeFixedOrbit(state);

            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPos.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");

        // Do computations on the envelope trajectory
        OWTR_OUTPUT.write("\nEnvelopeTrajectory: computeFixedOrbit");
        OWTR_OUTPUT.write("\n");
        Trajectory<EnvelopeProbeState>   trjEnv = PROBE_ENV_TEST.getTrajectory();
        for (EnvelopeProbeState state : trjEnv) {
            PhaseVector vecPos = this.cmpSimResults.computeFixedOrbit(state);

            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPos.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");
    }

    /**
     * Test method for {@link xal.tools.beam.calc.SimResultsAdaptor#computeChromAberration(xal.model.probe.traj.ProbeState)}.
     */
    @Test
    public void testComputeChromaticAberration() throws IOException {

        // Do computations on the particle trajectory
        OWTR_OUTPUT.write("\nParticleTrajectory: computeChromAberration");
        OWTR_OUTPUT.write("\n");
        Trajectory<ParticleProbeState>  trjPart = PROBE_PARTL_TEST.getTrajectory();
        for (ParticleProbeState state : trjPart) {
            PhaseVector vecPos = this.cmpSimResults.computeChromAberration(state);
            
            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPos.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");
        
        // Do computations on the transfer map trajectory
        OWTR_OUTPUT.write("\nTransferMapTrajectory: computeChromAberration");
        OWTR_OUTPUT.write("\n");
        Trajectory<TransferMapState>   trjXfer = PROBE_XFER_TEST.getTrajectory();
        for (TransferMapState state : trjXfer) {
            PhaseVector vecPos = this.cmpSimResults.computeChromAberration(state);
            
            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPos.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");

        // Do computations on the envelope trajectory
        OWTR_OUTPUT.write("\nEnvelopeTrajectory: computeChromAberration");
        OWTR_OUTPUT.write("\n");
        Trajectory<EnvelopeProbeState>  trjEnv = PROBE_ENV_TEST.getTrajectory();
        for (EnvelopeProbeState state : trjEnv) {
            PhaseVector vecPos = this.cmpSimResults.computeChromAberration(state);

            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPos.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");
    }

    /**
     * Test method for {@link xal.tools.beam.calc.SimResultsAdaptor#computeTwissParameters(xal.model.probe.traj.ProbeState)}.
     */
    @Test
    public void testComputeTwissParameters() throws IOException {

        // Do computations on the transfer map trajectory
        OWTR_OUTPUT.write("\nTransferMapTrajectory: computeTwissParameters");
        OWTR_OUTPUT.write("\n");
        Trajectory<TransferMapState> trjXfer = PROBE_XFER_TEST.getTrajectory();
        for (TransferMapState state : trjXfer) {
            Twiss[] arrTwiss = this.cmpSimResults.computeTwissParameters(state);
            Twiss3D t3dMach  = new Twiss3D(arrTwiss);

            OWTR_OUTPUT.write(state.getElementId() + ": " + t3dMach.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");

        // Do computations on the EnvelopeTrajectory
        OWTR_OUTPUT.write("\nEnvelopeTrajectory: computeTwissParameters");
        Trajectory<EnvelopeProbeState> trjEnv = PROBE_ENV_TEST.getTrajectory();
        for (EnvelopeProbeState state : trjEnv) {
            Twiss[] arrTwiss = this.cmpSimResults.computeTwissParameters(state);
            Twiss3D t3dBeam = new Twiss3D(arrTwiss);

            OWTR_OUTPUT.write(state.getElementId() + ": " + t3dBeam.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");
    }

    /**
     * Test method for {@link xal.tools.beam.calc.SimResultsAdaptor#computeBetatronPhase(xal.model.probe.traj.ProbeState)}.
     */
    @Test
    public void testComputeBetatronPhase() throws IOException {

        // Do computations on the transfer map trajectory
        OWTR_OUTPUT.write("\nTransferMapTrajectory: computeBetatronPhase");
        OWTR_OUTPUT.write("\n");
        Trajectory<TransferMapState>  trjXfer = PROBE_XFER_TEST.getTrajectory();
        for (TransferMapState state : trjXfer) {
            R3  vecPhase = this.cmpSimResults.computeBetatronPhase(state);

            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPhase.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");

        // Do computations on the EnvelopeTrajectory
        OWTR_OUTPUT.write("\nEnvelopeTrajectory: computeBetatronPhase");
        Trajectory<EnvelopeProbeState> trjEnv = PROBE_ENV_TEST.getTrajectory();
        for (EnvelopeProbeState state : trjEnv) {
            R3 vecPhase = this.cmpSimResults.computeBetatronPhase(state);

            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPhase.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");
    }
    
    /**
     * Test method for {@link xal.tools.beam.calc.SimResultsAdaptor#computeChromDispersion(xal.model.probe.traj.ProbeState)}.
     */
    @Test
    public void testComputeChromDispersion() throws IOException {

        // Do computations on the transfer map trajectory
        OWTR_OUTPUT.write("\nTransferMapTrajectory: computeChromDispersion");
        OWTR_OUTPUT.write("\n");
        Trajectory<TransferMapState>  trjXfer = PROBE_XFER_TEST.getTrajectory();
        for (TransferMapState state : trjXfer) {
            PhaseVector vecPhase = this.cmpSimResults.computeChromDispersion(state);

            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPhase.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");

        // Do computations on the EnvelopeTrajectory
        OWTR_OUTPUT.write("\nEnvelopeTrajectory: computeChromDispersion");
        Trajectory<EnvelopeProbeState> trjEnv = PROBE_ENV_TEST.getTrajectory();
        for (EnvelopeProbeState state : trjEnv) {
            PhaseVector vecPhase = this.cmpSimResults.computeChromDispersion(state);

            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPhase.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");
    }
    
}
