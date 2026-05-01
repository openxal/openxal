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
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss3D;
import xal.tools.beam.PhaseMatrix.IND;
import xal.tools.math.r3.R3;

/**
 * Test cases for the <code>SimResultsAdaptor</code> class. 
 *
 * @author Christopher K. Allen
 * @since  Nov 19, 2013
 */
public class TestCalculationsOnRings {

    
    /*
     * Global Constants
     */
    
    /** Output file location */
    static private String             STR_OUTPUT = TestCalculationsOnRings.class.getName().replace('.', '/') + ".txt";
    
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
   
    /** Calculation engine for ring parameters using transfer map states */
    private CalculationsOnRings          calXferRing;
    
   
   /**
    *
    * @throws java.lang.Exception
    *
    * @author Christopher K. Allen
    * @since  May 3, 2011
    */
   @Before
   public void setUp() throws Exception {
       this.calXferRing = new CalculationsOnRings(  PROBE_XFER_TEST.getTrajectory() );
}

   
   /*
    * Tests
    */
   
    /**
     * Test method for {@link xal.tools.beam.calc.SimResultsAdaptor#computeFixedOrbit(xal.model.probe.traj.ProbeState)}.
     * @throws IOException 
     */
    @Test
    public void testComputeFixedOrbit() throws IOException {

        // Do computations on the transfer map trajectory
        OWTR_OUTPUT.write("\nTransferMapTrajectory: computeFixedOrbit");
        OWTR_OUTPUT.write("\n");
        Trajectory<TransferMapState>   trjXfer = PROBE_XFER_TEST.getTrajectory();
        for (TransferMapState state : trjXfer) {
            PhaseVector vecPos = this.calXferRing.computeFixedOrbit(state);

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

        // Do computations on the transfer map trajectory
        OWTR_OUTPUT.write("\nTransferMapTrajectory: computeChromAberration");
        OWTR_OUTPUT.write("\n");
        Trajectory<TransferMapState>   trjXfer = PROBE_XFER_TEST.getTrajectory();
        for (TransferMapState state : trjXfer) {
            PhaseVector vecPos = this.calXferRing.computeChromAberration(state);
            
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
        OWTR_OUTPUT.write("\nCalculationsOnRings: computeTwissParameters() and computeMatchedTwissAt()");
        OWTR_OUTPUT.write("\n");
        Trajectory<TransferMapState> trjXfer = PROBE_XFER_TEST.getTrajectory();
        for (TransferMapState state : trjXfer) {
            Twiss[] arrTwissMt = this.calXferRing.computeMatchedTwissAt(state);
            Twiss[] arrTwissAt = this.calXferRing.computeTwissParameters(state);
            Twiss3D t3dMach  = new Twiss3D(arrTwissMt);
            Twiss3D t3dAt    = new Twiss3D(arrTwissAt);

            OWTR_OUTPUT.write(state.getElementId() + "\n");
            OWTR_OUTPUT.write("  Generic = " + t3dAt.toString() + "\n");
            OWTR_OUTPUT.write("  Matched = " + t3dMach.toString());
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
            R3  vecPhase = this.calXferRing.computeBetatronPhase(state);

            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPhase.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");
    }
    
    /**
     * Test method for {@link xal.tools.beam.calc.SimResultsAdaptor#computeBetatronPhase(xal.model.probe.traj.ProbeState)}.
     */
    @Test
    public void testComputePhaseAdvance() throws IOException {

        // Do computations on the transfer map trajectory
        OWTR_OUTPUT.write("\nComputationsOnRings: computePhaseAdvance");
        OWTR_OUTPUT.write("\n");
        Trajectory<TransferMapState>  trjXfer = PROBE_XFER_TEST.getTrajectory();

        R3                  vecPhsTot = R3.zero();
        TransferMapState    state1    = trjXfer.initialState();
        for (TransferMapState state2 : trjXfer) {
            R3  vecPhsAdv = this.calXferRing.computePhaseAdvanceBetween(state1, state2);

            OWTR_OUTPUT.write(state1.getElementId() + "-" + state2.getElementId() + ": " + vecPhsAdv.toString());
            OWTR_OUTPUT.write("\n");
            
            state1 = state2;
            vecPhsTot.plusEquals(vecPhsAdv);
        }
        OWTR_OUTPUT.write("Total phase advance = " + vecPhsTot);
        OWTR_OUTPUT.write("\n");
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
            PhaseVector vecPhase = this.calXferRing.computeChromDispersion(state);

            OWTR_OUTPUT.write(state.getElementId() + ": " + vecPhase.toString());
            OWTR_OUTPUT.write("\n");
        }
        OWTR_OUTPUT.write("\n");
    }
    
    /**
     * Test method for computing ring tunes.
     * 
     * @throws IOException
     *
     * @author Christopher K. Allen
     * @since  Nov 5, 2014
     */
    @Test
    public void testComputeRingTunes() throws IOException {

        // Compute the ring tunes
        OWTR_OUTPUT.write("\nRing Computation: computeFractionalTunes");
        OWTR_OUTPUT.write("\n");
        R3  vecFracTunes = this.calXferRing.computeFractionalTunes();
        OWTR_OUTPUT.write(" fraction tunes: " + vecFracTunes.toString());
        OWTR_OUTPUT.write("\n");
        
        OWTR_OUTPUT.write("\nRing Computation: computeFullTunes");
        OWTR_OUTPUT.write("\n");
        R3  vecFullTunes = this.calXferRing.computeFullTunes();
        OWTR_OUTPUT.write(" full tunes: " + vecFullTunes.toString());
        OWTR_OUTPUT.write("\n");
        
//        OWTR_OUTPUT.write("\nRing Computation: computeFullTunes via integration");
//        OWTR_OUTPUT.write("\n");
//        vecFullTunes = this.calXferRing.computeFullTunes_integration();
//        OWTR_OUTPUT.write(" full tunes: " + vecFullTunes.toString());
//        OWTR_OUTPUT.write("\n");
//        
        OWTR_OUTPUT.write("\n");
    }
    
    /**
     * Test the turn-by-turn computations of the ring calculation engine.
     * 
     * @throws IOException
     *
     * @author Christopher K. Allen
     * @since  Nov 5, 2014
     */
    @Test
    public void testTurnByTurnResponse() throws IOException {
        String  strElemId1 = "Ring_Inj:Foil";
        String  strElemId2 = "BEGIN_Ring";
        int     cntTurns   = 50;
        PhaseVector vecInit = new PhaseVector(0.00,0, 0,0, 0,0);

        Trajectory<TransferMapState>  trjXfer = PROBE_XFER_TEST.getTrajectory();
        TransferMapState    state1 = trjXfer.stateForElement(strElemId1);
        TransferMapState    state2 = trjXfer.stateForElement(strElemId2); 
        
        // Compute the ring tunes
        
        OWTR_OUTPUT.write("\nRing Computation: computeTurnByTurnResponse");
        OWTR_OUTPUT.write("\n Injection location " + state1.getElementId());
        OWTR_OUTPUT.write("\n Observation location " + state2.getElementId());
        OWTR_OUTPUT.write("\n");
        int cnt = 0;
        PhaseVector[]   arrVecRsp = this.calXferRing.computeTurnByTurnResponse(state1, state2, cntTurns, vecInit);
        for (PhaseVector vecRsp : arrVecRsp) {
            OWTR_OUTPUT.write("\n " + cnt + " coordinates: " + vecRsp.toString());
            cnt++;
        }
        OWTR_OUTPUT.write("\n");
    }
    
    /**
     * Do some experiments with fixed orbit vectors
     * @throws IOException
     *
     * @author Christopher K. Allen
     * @since  Nov 6, 2014
     */
    @Test
    public void testFixedPointOrbit() throws IOException {
        Trajectory<TransferMapState>  trjXfer = PROBE_XFER_TEST.getTrajectory();
        
        String  strElemId = "Ring_Inj:Foil";
        TransferMapState    state = trjXfer.stateForElement(strElemId);
        
        PhaseVector vecFxdOrb = this.calXferRing.computeFixedOrbit(state);
        PhaseMatrix matFull   = this.calXferRing.computeRingFullTurnMatrixAt(state);
        
        OWTR_OUTPUT.write("\nFull Turn Matrix \n");
        OWTR_OUTPUT.write(matFull.toStringMatrix());
        OWTR_OUTPUT.write("\n");
        OWTR_OUTPUT.write("\nProjected Full Turn Matrix \n");
        OWTR_OUTPUT.write(matFull.projectR6x6().toStringMatrix());
        OWTR_OUTPUT.write("\n");
        OWTR_OUTPUT.write("\nProjected Displacement Vector \n");
        OWTR_OUTPUT.write(matFull.projectColumn(IND.HOM).toString());
        OWTR_OUTPUT.write("\n");
        
        
        PhaseVector vec1      = matFull.times( vecFxdOrb );
        PhaseVector vec2      = matFull.inverse().times( vec1 );
        PhaseVector vec3      = matFull.solve( vec1 );
        
        OWTR_OUTPUT.write("\nRing Fixed Point Experiments ");
        OWTR_OUTPUT.write("\n  Fixed point vector : " + vecFxdOrb.toString());
        OWTR_OUTPUT.write("\n  After one turn     : " + vec1.toString());
        OWTR_OUTPUT.write("\n  Applying inverse   : " + vec2.toString());
        OWTR_OUTPUT.write("\n  Using linear solve : " + vec3.toString());
        OWTR_OUTPUT.write("\n");
    }
    
    @Test
    public void testLinearSolve() {
        
    }
    
}
