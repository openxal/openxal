/**
 * TestHardwareTree.java
 *
 * @author Christopher K. Allen
 * @since  May 3, 2011
 *
 */

/**
 * TestHardwareTree.java
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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.test.ResourceManager;
import xal.sim.latgen.GenerationException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;

/**
 * JUnit test cases for the class <code>{@link HardwareTree}</code>.
 *
 * @author Christopher K. Allen
 * @since   May 3, 2011
 */
public class TestHardwareTree {

    
    /** The XAL configuration file in the test resources (absolute resource path in the test jar) */
    final static private String     CONFIGURATION_RESOURCE_PATH = "/xal/config/main.xal";
    
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
        ACCEL_TEST = ResourceManager.getTestAccelerator();
    }
    
    
    /**
     *
     *
     * @author Christopher K. Allen
     * @since  Nov 9, 2011
     */
    @AfterClass
    public static void commonCleanup() {
        ACCEL_TEST = null;
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
     * Test method for {@link xal.sim.latgen.atree.HardwareTree#HardwareTree(AcceleratorSeq)}
     */
    @Test
    public void testHardwareTree() {
        AcceleratorSeq  seqTest = ACCEL_TEST.getSequence(STR_ACCELSEQ_BUILD_TEST);
        
        try {
            HardwareTree        hwtTest = new HardwareTree(seqTest);
            String              strTest = hwtTest.toString();
            
            File                fileOut = File.createTempFile( "TestHardwareTree", ".txt" );
            fileOut.deleteOnExit();     // mark the output file for deletion upon exit of the process
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
