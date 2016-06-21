/**
 * TestFringeQuadGeneration.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jun 17, 2016
 */
package xal.sim.scenario;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.IElement;
import xal.model.ModelException;
import xal.model.elem.IdealMagFringeQuad;
import xal.model.elem.IdealMagFringeQuadFace;
import xal.model.elem.IdealMagQuad;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.test.ResourceManager;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Jun 17, 2016
 */
public class TestFringeQuadGeneration {

    
    /*
     * Global Constants
     */
    
    /** string identifier for ring test magnet */
    private static final String STR_ID_TESTMAG1 = "Ring_Mag:QTV_A12";
    
    /** the string identifier for a ring test magnet  */
    private static final String STR_ID_TESTMAG2 = "Ring_Mag:QTV_B01";

    
    /*
     * Global Attributes
     */
    
    /** test accelerator containing special quadrupoles in Ring */
    private static Accelerator      ACCL_TEST;
    
    /** The "Ring 1" sequence of the test ring hardware */
    private static AcceleratorSeq   SEQ_RING1;
    
    /** The "Ring 2" sequence of the test ring hardware */
    private static AcceleratorSeq   SEQ_RING2;
    
    
    /** test hardware test magnet */
    private static AcceleratorNode    SMF_TESTMAG2;
    
    
    
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
        
        SMF_TESTMAG2 = SEQ_RING2.getNodeWithId(STR_ID_TESTMAG2);
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
     * Test method for {@link xal.sim.scenario.Scenario#nodeWithId(java.lang.String)}.
     * 
     * @throws ModelException 
     */
    @Test
    public final void testElementCreation() throws ModelException {
        Scenario        modRing1 = Scenario.newScenarioFor(SEQ_RING1);
        AcceleratorNode smfNode  = modRing1.nodeWithId(STR_ID_TESTMAG1);
        List<IElement>  lstElems = modRing1.elementsMappedTo(smfNode);
        Assert.assertTrue(lstElems.size() > 0);
        
        
        Scenario   modRing2    = Scenario.newScenarioFor(SEQ_RING2);
        smfNode  = modRing2.nodeWithId(STR_ID_TESTMAG2);
        lstElems = modRing2.elementsMappedTo(smfNode);
        Assert.assertTrue(lstElems.size() > 0);
    }

    /**
     * Test method for {@link xal.sim.scenario.Scenario#elementsMappedTo(xal.smf.AcceleratorNode)}.
     * 
     * @throws ModelException 
     */
    @Test
    public final void testElementsMappedTo() throws ModelException {
        Scenario        modRing2 = Scenario.newScenarioFor(SEQ_RING2);
        List<IElement>  lstElems = modRing2.elementsMappedTo(SMF_TESTMAG2);
        for (IElement elem : lstElems) {
            Assert.assertTrue(elem.getClass() == IdealMagQuad.class 
                           || elem.getClass() == IdealMagFringeQuadFace.class
                    );
        }
    }

}
