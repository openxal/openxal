/**
 * TestMagnetBucket.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jun 16, 2016
 */
package xal.smf.attr;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Quadrupole;
import xal.test.ResourceManager;

/**
 * Currently this class is primarily for testing the fringe field
 * integral attributes.
 *
 *
 * @author Christopher K. Allen
 * @since  Jun 16, 2016
 */
public class TestMagnetBucket {

    
    
    /*
     * Global Attributes
     */
    
    /** test accelerator containing special quadrupoles in Ring */
    private static Accelerator      ACCL_TEST;
    
    /** The "Ring 1" sequence of the test ring hardware */
    private static AcceleratorSeq   SEQ_RING1;
    
    /** The "Ring 2" sequence of the test ring hardware */
    private static AcceleratorSeq   SEQ_RING2;
    
    
    
    /*
     * Global Methods
     */
    
    /**
     * @throws java.lang.Exception
     *
     * @since  Jun 16, 2016,   Christopher K. Allen
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        ACCL_TEST = ResourceManager.getTestAccelerator();
        
        SEQ_RING1 = ACCL_TEST.getSequence("Ring1");
        SEQ_RING2 = ACCL_TEST.getSequence("Ring2");
    }

    /**
     * @throws java.lang.Exception
     *
     * @since  Jun 16, 2016,   Christopher K. Allen
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }
    
    
    /*
     * Tests
     */

    /**
     * Test method for {@link xal.smf.attr.MagnetBucket#getFringeFieldIntegralK0()}.
     */
    @Test
    public final void testGetFringeFieldIntegralK0() {
        List<Quadrupole> lstFrQuadH = SEQ_RING1.getNodesOfType("QFH");
        List<Quadrupole> lstFrQuadV = SEQ_RING1.getNodesOfType("QFV");
        
        for (Quadrupole quad : lstFrQuadH) {
            MagnetBucket    bucMag = quad.getMagBucket();
            double          dblFrIntK0 = bucMag.getFringeFieldIntegralK0();
            
            Assert.assertTrue(dblFrIntK0 != 0.0);
        }

        for (Quadrupole quad : lstFrQuadV) {
            MagnetBucket    bucMag = quad.getMagBucket();
            double          dblFrIntK0 = bucMag.getFringeFieldIntegralK0();
            
            Assert.assertTrue(dblFrIntK0 != 0.0);
        }
    }

    /**
     * Test method for {@link xal.smf.attr.MagnetBucket#setFringeFieldIntegralK0(double)}.
     */
    @Test
    public final void testSetFringeFieldIntegralK0() {
        List<Quadrupole> lstFrQuadH = SEQ_RING2.getNodesOfType("QFH");
        List<Quadrupole> lstFrQuadV = SEQ_RING2.getNodesOfType("QFV");
        
        for (Quadrupole quad : lstFrQuadH) {
            MagnetBucket    bucMag = quad.getMagBucket();
            bucMag.setFringeFieldIntegralK0(10.0);
            
            double          dblFrIntK0 = bucMag.getFringeFieldIntegralK0();
            Assert.assertTrue(dblFrIntK0 == 10.0);
        }

        for (Quadrupole quad : lstFrQuadV) {
            MagnetBucket    bucMag = quad.getMagBucket();
            bucMag.setFringeFieldIntegralK0(10.0);

            double          dblFrIntK0 = bucMag.getFringeFieldIntegralK0();
            Assert.assertTrue(dblFrIntK0 == 10.0);
        }
    }

}
