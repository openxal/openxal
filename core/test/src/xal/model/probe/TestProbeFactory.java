/**
 * TestProbeFactory.java
 *
 * @author Christopher K. Allen
 * @since  Nov 9, 2011
 *
 */

/**
 * TestProbeFactory.java
 *
 * @author  Christopher K. Allen
 * @since	Nov 9, 2011
 */
package xal.model.probe;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.alg.EnvTrackerAdapt;
import xal.test.ResourceManager;
import xal.sim.scenario.ProbeFactory;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.CovarianceMatrix;

/**
 * Tests the <code>ProbeFactory</code> class factory of Open XAL.
 *
 * @author Christopher K. Allen
 * @since   Nov 9, 2011
 */
public class TestProbeFactory {
    
    
    /** Accelerator sequence used for testing */
    public static final String     STR_ACCL_SEQ_ID = "MEBT";
    
    
    /** The Accelerator Sequence object used to create probe - it is created once */ 
    private static AcceleratorSeq     SEQ_TEST;

    
    
    /**
     *
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Nov 9, 2011
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        Accelerator     accel   = ResourceManager.getTestAccelerator();
        SEQ_TEST = accel.getSequence(STR_ACCL_SEQ_ID);
    }

    /**
     * xal.model.probe
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
     * Test method for {@link xal.sim.scenario.ProbeFactory#createParticleProbe(xal.smf.AcceleratorSeq, xal.model.IAlgorithm)}.
     */
    @Test
    public void testGetParticleProbeAcceleratorSeqIAlgorithm() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link xal.sim.scenario.ProbeFactory#getTransferMapProbe(xal.smf.AcceleratorSeq, xal.model.IAlgorithm)}.
     */
    @Test
    public void testGetTransferMapProbeAcceleratorSeqIAlgorithm() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link xal.sim.scenario.ProbeFactory#getEnvelopeProbe(xal.smf.AcceleratorSeq, xal.model.IAlgorithm)}.
     */
    @Test
    public void testGetEnvelopeProbeAcceleratorSeqIAlgorithm() {
        
        EnvelopeProbe prbTest = ProbeFactory.getEnvelopeProbe( SEQ_TEST, new EnvTrackerAdapt() );
        
        CovarianceMatrix matCov = prbTest.getCovariance();
        
        System.out.println(matCov);
        
    }

}
