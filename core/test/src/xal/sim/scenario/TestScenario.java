/**
 * TestScenario.java
 *
 * @author Christopher K. Allen
 * @since  Nov 9, 2011
 *
 */

/**
 * TestScenario.java
 *
 * @author  Christopher K. Allen
 * @since	Nov 9, 2011
 */
package xal.sim.scenario;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.IAlgorithm;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.test.ResourceManager;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;

/**
 * Testing scenario generation for the Open XAL online model.
 *
 * @author Christopher K. Allen
 * @since   Nov 9, 2011
 */
public class TestScenario {

    
    /** Accelerator sequence used for testing */
//    public static final String     STR_ACCL_SEQ_ID = "MEBT";
    public static final String     STR_ACCL_SEQ_ID = "HEBT2";
    
    
    
    /**
     *
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Nov 9, 2011
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * xal.sim.scenario
     *
     * @author Christopher K. Allen
     * @since  Nov 9, 2011
     *
     */

    /**
     *
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Nov 9, 2011
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test method for {@link xal.sim.scenario.Scenario#newScenarioFor(xal.smf.AcceleratorSeq)}.
     */
    @Test
    public void testNewScenarioForAcceleratorSeq() {

        Accelerator     accel = ResourceManager.getTestAccelerator();
        AcceleratorSeq  seq   = accel.getSequence(STR_ACCL_SEQ_ID);
        
        try {
            Scenario        model = Scenario.newScenarioFor(seq);
            
        } catch (ModelException e) {

            fail("Unable to create Scenario");
            e.printStackTrace();
        }
        
    }

    /**
     * Test method for {@link xal.sim.scenario.Scenario#run()}.
     * Uses an adaptive envelope probe.
     */
    @Test
    public void testRunFromFactories() {
        Accelerator     accel = ResourceManager.getTestAccelerator();
        AcceleratorSeq  seq   = accel.getSequence(STR_ACCL_SEQ_ID);
        
        try {
            Scenario        model = Scenario.newScenarioFor(seq);
            IAlgorithm      algor = AlgorithmFactory.createEnvTrackerAdapt(seq);
            EnvelopeProbe   probe = ProbeFactory.getEnvelopeProbe(seq, algor);
            
            probe.initialize();
            model.setProbe( probe );
            model.resync();
            
            model.run();
            
        } catch (ModelException | InstantiationException e) {

            fail("Unable to run Scenario");
            e.printStackTrace();
        }
    }

    /**
     * Test method for {@link xal.sim.scenario.Scenario#run()}.
     * Uses an adaptive envelope probe.
     */
    @Test
    public void testRunParticleProbeFromFactories() {
        Accelerator     accel = ResourceManager.getTestAccelerator();
        AcceleratorSeq  seq   = accel.getSequence(STR_ACCL_SEQ_ID);
        
        try {
            Scenario        model = Scenario.newScenarioFor(seq);
            IAlgorithm      algor = AlgorithmFactory.createParticleTracker(seq);
            ParticleProbe   probe = ProbeFactory.createParticleProbe(seq, algor);
            
            probe.initialize();
            model.setProbe( probe );
            model.resync();
            
            model.run();
            
        } catch (ModelException | InstantiationException e) {

            fail("Unable to run Scenario");
            e.printStackTrace();
        }
    }

    /**
     * Test method for {@link xal.sim.scenario.Scenario#run()}.
     */
    @Test
    public void testRunViaLoad() {
        Accelerator     accel = ResourceManager.getTestAccelerator();
        AcceleratorSeq  seq   = accel.getSequence(STR_ACCL_SEQ_ID);
        
        try {
            Scenario        model = Scenario.newScenarioFor(seq);

            IAlgorithm      algor = new EnvTrackerAdapt();
            algor.load(seq.getEntranceID(), accel.editContext());
            
            EnvelopeProbe   probe = ProbeFactory.getEnvelopeProbe(seq, algor);
            probe.initialize();
            
            model.setProbe( probe );
            model.resync();
            model.run();
            
        } catch (ModelException e) {

            fail("Unable to run Scenario");
            e.printStackTrace();
        }
        
    }

    /**
     * Test method for {@link xal.sim.scenario.Scenario#getProbe()}.
     */
    @Test
    public void testGetProbe() {

    }

}
