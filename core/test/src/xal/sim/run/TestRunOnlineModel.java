package xal.sim.run;

import static org.junit.Assert.*;

import xal.model.IAlgorithm;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.alg.ParticleTracker;
import xal.model.alg.Tracker;
import xal.model.alg.TransferMapTracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.TransferMapProbe;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.sim.sync.PVLoggerDataSource;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.test.ResourceManager;

import org.junit.BeforeClass;
import org.junit.Test;



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
    
    
    /** String identifier for accelerator sequence used in testing */
    static public String            STR_SEQ_ID       = "HEBT1";
    
    /** String identifier where Courant-Snyder parameters are to be reconstructed */
    static public String            STR_TARG_ELEM_ID = "Begin_Of_HEBT1";
    
    
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
            PROBE_PARTL_TEST = ProbeFactory.createParticleProbe(SEQ_TEST, new ParticleTracker());
            
            // Create and initialize transfer map probe
            PROBE_XFER_TEST = ProbeFactory.getTransferMapProbe(SEQ_TEST, new TransferMapTracker() );

        } catch (Exception e) {
            System.err.println("Unable to instantiate TransferMatrixObject");
            
        }
    }

    
    
    
    /*
     * Tests
     */
    
    /**
     * Test the <i>create default algorithm</i> method of the <code>{@link Tracker}</code>
     * base class (see <code>{@link Tracker#newFromEditContext(AcceleratorSeq)}</code>).
     * This method has been deprecated, however, and should not be used. 
     *
     * @author Christopher K. Allen
     * @since  Oct 29, 2012
     */
    @Test
    public void testTrackerNewFromEditContext() {
        IAlgorithm algDef = Tracker.newFromEditContext(SEQ_TEST);
        
        if (algDef == null) 
            fail("Tracker#newFromEditContext() failure");
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
	public void testRunEnvelopeModel() throws ModelException {
		
        PROBE_ENV_TEST.reset();
        MODEL_TEST.setProbe(PROBE_ENV_TEST);
        MODEL_TEST.resync();
        MODEL_TEST.run();
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
    }
    
	/**
	 * Test the ability of the online model to synchronize to an historical machine configuration.
	 * 
	 * @throws ModelException      general synchronization or simulation error ?
	 *
	 * @author Christopher K. Allen
	 * @since  Oct 12, 2012
	 */
//	@Test
	public void testPvLoggerDataSource() throws ModelException {
	    
        PVLoggerDataSource  srcPvLog = new PVLoggerDataSource(LNG_PVLOGID);
        Scenario    modHistory = srcPvLog.setModelSource(SEQ_TEST, MODEL_TEST);
	    
        PROBE_ENV_TEST.reset();
        modHistory.setProbe(PROBE_ENV_TEST);
        modHistory.resync();
        modHistory.run();
	}
	
	

}
