/**
 * TestAssocTree.java
 *
 * @author Christopher K. Allen
 * @since  May 3, 2011
 *
 */

/**
 * TestAssocTree.java
 *
 * @author  Christopher K. Allen
 * @since	May 3, 2011
 */
package xal.sim.latgen.atree;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.sim.latgen.GenerationException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;

/**
 * JUnit test cases for the class <code>{@link HardwareTree}</code>.
 *
 * @author Christopher K. Allen
 * @since   May 3, 2011
 */
public class TestAssocTree {

    
    /** The XAL configuration file */
    final static private String     STR_URL_CONFIG = "common/core/test/resources/config/main.xal";
    
    /** The output text dump of the association tree */
    final static private String     STR_URL_TEXT_OUT = "common/core/test/output/xal/sim/latgen/atree/atree.txt";
    
    
    /** The accelerator object used for testing */
    static private Accelerator      ACCEL_TEST;
    
    /** Accelerator sequence for Tree build test */
    final static private String     STR_ACCELSEQ_BUILD_TEST = "MEBT";
    
    
    /**
     *
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  May 3, 2011
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        ACCEL_TEST = XMLDataManager.acceleratorWithPath(STR_URL_CONFIG);
//        ACCEL_TEST = XMLDataManager.loadDefaultAccelerator();
       
    }

    /**
     * xal.sim.latgen.atree
     *
     * @author Christopher K. Allen
     * @since  May 3, 2011
     *
     */

    /**
     *
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  May 3, 2011
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test method for {@link xal.sim.latgen.atree.HardwareTree#AssocTree(xal.smf.AcceleratorSeq)}.
     */
    @Test
    public void testAssocTree() {
        AcceleratorSeq  seqTest = ACCEL_TEST.getSequence(STR_ACCELSEQ_BUILD_TEST);
        
        try {
            HardwareTree       treTest = new HardwareTree(seqTest);
            String          strTest = treTest.toString();
            
            File                fileOut = new File(STR_URL_TEXT_OUT);
            FileOutputStream    fosOut  = new FileOutputStream(fileOut);
            OutputStreamWriter  oswOut  = new OutputStreamWriter(fosOut);
            
            oswOut.write(strTest);
            oswOut.close();
            
        } catch (GenerationException e) {
            e.printStackTrace();
            fail("Unable to generate association tree");
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail("Unable to open output file for tree data");
            
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unable to write to disk");
            
        }
    }

}
