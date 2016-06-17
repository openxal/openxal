/**
 * TestQuadrupole.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jun 16, 2016
 */
package xal.smf;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import java.util.List;

import xal.smf.impl.Quadrupole;
import xal.test.ResourceManager;

/**
 * Currently setup for testing the fringe field integral.  Can always add more tests
 * for other quadrupole functions.
 *
 *
 * @author Christopher K. Allen
 * @since  Jun 16, 2016
 */
public class TestQuadrupole {

    
    /*
     * Global Attributes
     */
    
    /** test accelerator containing special quadrupoles in Ring */
    private static Accelerator      ACCL_TEST;
    
    /** The "Ring 1" sequence of the test ring hardware */
    private static AcceleratorSeq   SEQ_RING1;
    
    /** The "Ring 2" sequence of the test ring hardware */
    private static AcceleratorSeq   SEQ_RING2;

    
    /** the string identifier for a ring test magnet  */
    private static final String ID_TESTMAG2 = "Ring_Mag:QTV_B01";
    
    
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
     * See if the normal mechanism can create SMF quadrupoles with a
     * new type identifier "QVF". 
     *
     * @since  Jun 17, 2016,   Christopher K. Allen
     */
    @Test
    public final void testFindQuadWithNewFringeType() {
        AcceleratorNode smfQuad = SEQ_RING1.getNodeWithId("Ring_Mag:QTV_A12");
        Assert.assertNotNull(smfQuad);
        Assert.assertEquals(smfQuad.getClass(), Quadrupole.class);

        String  strType = smfQuad.getType();
        Assert.assertTrue(strType.equalsIgnoreCase("QFV"));
    }
    
    /**
     * We are trying to fetch all quadrupoles with fringe fields using
     * type qualifiers.
     *
     * @since  Jun 16, 2016,   Christopher K. Allen
     */
    @Test
    public final void testFindQuadrupoleTypeQF() {
        List<Quadrupole> lstQuads = SEQ_RING1.getNodesOfType("Q");
        List<Quadrupole> lstHQuads = SEQ_RING1.getNodesOfType("QH");
        List<Quadrupole> lstFrQuads = SEQ_RING1.getNodesOfType("QF");
        List<Quadrupole> lstFrHQuads = SEQ_RING1.getNodesOfType("QFH");

        Assert.assertTrue(lstQuads.size() > 0);
        Assert.assertTrue(lstHQuads.size() == 0);
        Assert.assertTrue(lstFrHQuads.size() > 0);
        Assert.assertTrue(lstFrQuads.size() == 0);
    }

//    /**
//     * We are trying to fetch horizontal quadrupoles with fringe fields using
//     * type qualifiers.
//     *
//     * @since  Jun 16, 2016,   Christopher K. Allen
//     */
//    @Test
//    public final void testFindHQuadrupoleTypeQF() {
////        List<Quadrupole> lstFrHQuads = SEQ_RING1.getNodesOfType("QHF");
////
////        Assert.assertTrue(lstFrHQuads.size() > 0);
//
//        AndTypeQualifier    atqFrQuad = new AndTypeQualifier();
//        atqFrQuad.and("Q");
//        atqFrQuad.and("F");
//        
//        List<Quadrupole>  lstFrQuads = SEQ_RING1.getNodesWithQualifier(atqFrQuad);
//        Assert.assertTrue(lstFrQuads.size() > 0);
//        
//    }

    /**
     * We put Optics_Extra overrides for the quadrupole types in Ring2.
     * For example, type goes from "QH" to "QHF".  Does this work?
     *
     * @since  Jun 16, 2016,   Christopher K. Allen
     */
    @Test
    public final void testQuadrupoleTypeFringeOverride() {
        AcceleratorNode smfFrNode = SEQ_RING2.getNodeWithId(ID_TESTMAG2);
        String  strType = smfFrNode.getType();
        Assert.assertTrue(strType.equalsIgnoreCase("qvf"));
        
        List<Quadrupole> lstFrQuads = SEQ_RING2.getNodesOfType("QVF");
        Assert.assertTrue(lstFrQuads.size() > 0);
    }

}
