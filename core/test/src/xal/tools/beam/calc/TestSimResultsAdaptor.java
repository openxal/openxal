/**
 * TestSimResultsAdaptor.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 19, 2013
 */
package xal.tools.beam.calc;

import static org.junit.Assert.fail;

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
import xal.model.probe.traj.EnvelopeTrajectory;
import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.ParticleTrajectory;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.TransferMapState;
import xal.model.probe.traj.TransferMapTrajectory;
import xal.resources.ResourceManager;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss3D;

/**
 * Test cases for the <code>SimResultsAdaptBase</code> class. 
 *
 * @author Christopher K. Allen
 * @since  Nov 19, 2013
 */
public class TestSimResultsAdaptor {

    
    /*
     * Global Attributes 
     */
    
    
    /** String identifier for accelerator sequence used in testing */
    static public String            STR_SEQ_ID       = "HEBT1";
    
    /** String identifier where Courant-Snyder parameters are to be reconstructed */
    static public String            STR_TARG_ELEM_ID = "Begin_Of_HEBT1";
    

    
//    /** URL of the accelerator hardware description file */
//    static public String            STRL_URL_ACCEL;
//    
    
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
        
        try {
            ACCEL_TEST   = XMLDataManager.loadDefaultAccelerator();
            SEQ_TEST     = ACCEL_TEST.findSequence(STR_SEQ_ID);
            MODEL_TEST   = Scenario.newScenarioFor(SEQ_TEST);
            MODEL_TEST.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
            
            // Create and initialize the envelope probe
            EnvTrackerAdapt algEnv = AlgorithmFactory.createEnvTrackerAdapt(SEQ_TEST);
            PROBE_ENV_TEST = ProbeFactory.getEnvelopeProbe(SEQ_TEST, algEnv);
            PROBE_ENV_TEST.initialize();
            MODEL_TEST.setProbe(PROBE_ENV_TEST);
            MODEL_TEST.resync();
            MODEL_TEST.run();
            
            // Create and initialize the particle probe
            PROBE_PARTL_TEST = ProbeFactory.createParticleProbe(SEQ_TEST, new ParticleTracker());
            PROBE_PARTL_TEST.reset();
            MODEL_TEST.setProbe(PROBE_PARTL_TEST);
            MODEL_TEST.resync();
            MODEL_TEST.run();
            
            // Create and initialize transfer map probe
            PROBE_XFER_TEST = ProbeFactory.getTransferMapProbe(SEQ_TEST, new TransferMapTracker() );
            PROBE_XFER_TEST.reset();
            MODEL_TEST.setProbe(PROBE_XFER_TEST);
            MODEL_TEST.resync();
            MODEL_TEST.run();

        } catch (Exception e) {
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
    public static void commonCleanup() {
    }

    
    /*
    * Local Attributes
    */
   
    /** Calculation engine for particle parameters using particle probe states */
    private CalculationsOnParticle       calPartPart;
    
    /** Calculation engine for machine parameters using transfer map states */
    private CalculationsOnMachine       calXferMach;
    
    /** Calculation engine for ring parameters using transfer map states */
    private CalculationsOnRing          calXferRing;
    
    /** Calculation engine for beam parameters using envelope probe states */
    private CalculationsOnBeam           calEnvBeam;
    
    
   /** the simulation adaptor */
   private SimResultsAdaptBase           cmpSimResults;
   
   /**
    *
    * @throws java.lang.Exception
    *
    * @author Christopher K. Allen
    * @since  May 3, 2011
    */
   @Before
   public void setUp() throws Exception {
       this.calPartPart = new CalculationsOnParticle( (ParticleTrajectory)PROBE_PARTL_TEST.getTrajectory() );
       this.calXferMach = new CalculationsOnMachine( (TransferMapTrajectory)PROBE_XFER_TEST.getTrajectory() );
       this.calXferRing = new CalculationsOnRing( (TransferMapTrajectory) PROBE_XFER_TEST.getTrajectory() );
       this.calEnvBeam = new CalculationsOnBeam( (EnvelopeTrajectory)PROBE_ENV_TEST.getTrajectory() );
       
       this.cmpSimResults = new SimResultsAdaptBase();

       this.cmpSimResults.registerCalcEngine(ParticleProbeState.class, this.calPartPart);
       this.cmpSimResults.registerCalcEngine(TransferMapState.class, this.calXferMach);
       this.cmpSimResults.registerCalcEngine(EnvelopeProbeState.class, this.calEnvBeam);
}

   
   /*
    * Tests
    */
   
    /**
     * Test method for {@link xal.tools.beam.calc.SimResultsAdaptBase#registerCalcEngine(java.lang.Class, xal.tools.beam.calc.ISimulationResults)}.
     */
    @Test
    public void testRegisterCalcEngine() {
        this.cmpSimResults.registerCalcEngine(ParticleProbeState.class, this.calPartPart);
        this.cmpSimResults.registerCalcEngine(TransferMapState.class, this.calXferMach);
        this.cmpSimResults.registerCalcEngine(EnvelopeProbeState.class, this.calEnvBeam);
    }

    /**
     * Test method for {@link xal.tools.beam.calc.SimResultsAdaptBase#computeCoordinatePosition(xal.model.probe.traj.ProbeState)}.
     */
    @Test
    public void testComputeCoordinatePosition() {

        System.out.println("\nParticle: computeCordinatePosition");
        ParticleTrajectory  trjPart = (ParticleTrajectory)PROBE_PARTL_TEST.getTrajectory();
        for (ProbeState state : trjPart) {
            PhaseVector vecPos = this.cmpSimResults.computeCoordinatePosition(state);
            
            System.out.println(vecPos);
        }
        
        System.out.println("\nTransferMap: computeCoordinatePosition");
        TransferMapTrajectory   trjXfer = (TransferMapTrajectory)PROBE_XFER_TEST.getTrajectory();
        for (ProbeState state : trjXfer) {
            PhaseVector vecPos = this.cmpSimResults.computeCoordinatePosition(state);
            
            System.out.println(vecPos);
        }
    }

    /**
     * Test method for {@link xal.tools.beam.calc.SimResultsAdaptBase#computeTwissParameters(xal.model.probe.traj.ProbeState)}.
     */
    @Test
    public void testComputeTwissParameters() {
        System.out.println("\nMachine: computeTwissParameters");
        TransferMapTrajectory   trjXfer = (TransferMapTrajectory)PROBE_XFER_TEST.getTrajectory();
        for (ProbeState state : trjXfer) {
            Twiss[] arrTwiss = this.cmpSimResults.computeTwissParameters(state);
            Twiss3D t3dMach  = new Twiss3D(arrTwiss);
            
            System.out.println(t3dMach);
        }
        
        System.out.println("\nBeam: computeTwissParameters");
        EnvelopeTrajectory  trjEnv = (EnvelopeTrajectory)PROBE_ENV_TEST.getTrajectory();
        for (ProbeState state : trjEnv) {
            Twiss[] arrTwiss = this.cmpSimResults.computeTwissParameters(state);
            Twiss3D t3dBeam = new Twiss3D(arrTwiss);
            
            System.out.println(t3dBeam);
        }
    }

}
