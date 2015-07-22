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

import java.io.File;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.alg.EnvTrackerAdapt;
import xal.sim.run.TestRunOnlineModel;
import xal.sim.scenario.ProbeFactory;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.test.ResourceManager;
import xal.tools.beam.CovarianceMatrix;

/**
 * Tests the <code>ProbeFactory</code> class factory of Open XAL.
 *
 * @author Christopher K. Allen
 * @since   Nov 9, 2011
 */
public class TestProbeFactory {
    
    

    /*
     * Global Constants
     */
    
    /** Flag used for indicating whether to type out to stout or file */
    private static final boolean        BOL_TYPE_STOUT = false;

    
    /** Accelerator sequence used for testing */
    public static final String     STR_ACCL_SEQ_ID = "MEBT";
    
    /** The Accelerator Sequence object used to create probe - it is created once */ 
    private static AcceleratorSeq     SEQ_TEST;

    
    /*
     * Global Attributes
     */
    
    /** Output file location */
    static private String           STR_FILE_OUTPUT = TestRunOnlineModel.class.getName().replace('.', '/') + ".txt";
    
    
    /** URL where we are dumping the output */
    static public File              FILE_OUTPUT    = ResourceManager.getOutputFile(STR_FILE_OUTPUT);
    
    
    /** Persistent storage for test output */
    private static PrintStream     OSTR_OUTPUT;
    
    

    /*
     * Global Methods
     */
    
    /**
     * Creates a new output file in the testing output directory with the 
     * given file name.
     * 
     * @param strFileName   name of the output file
     * 
     * @return              new output file object
     *
     * @author Christopher K. Allen
     * @since  Sep 11, 2014
     */
    private static File createOutputFile(String strFileName) {
        String  strPack     = TestProbeFactory.class.getPackage().getName();
        String  strPathRel  = strPack.replace('.', '/');
        String  strPathFile = strPathRel + '/' + strFileName; 
        File    fileOutput  = xal.test.ResourceManager.getOutputFile(strPathFile);
        
        return fileOutput;
    }
    
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
        
        if (BOL_TYPE_STOUT) {
            OSTR_OUTPUT = System.out;
            
        } else {
            File       fileOut = createOutputFile(STR_FILE_OUTPUT);
            
            OSTR_OUTPUT = new PrintStream(fileOut);
        }
    }

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
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link xal.sim.scenario.ProbeFactory#getTransferMapProbe(xal.smf.AcceleratorSeq, xal.model.IAlgorithm)}.
     */
    @Test
    public void testGetTransferMapProbeAcceleratorSeqIAlgorithm() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link xal.sim.scenario.ProbeFactory#getEnvelopeProbe(xal.smf.AcceleratorSeq, xal.model.IAlgorithm)}.
     */
    @Test
    public void testGetEnvelopeProbeAcceleratorSeqIAlgorithm() {
        
        EnvelopeProbe prbTest = ProbeFactory.getEnvelopeProbe( SEQ_TEST, new EnvTrackerAdapt() );
        
        CovarianceMatrix matCov = prbTest.getCovariance();
        
        OSTR_OUTPUT.println(matCov);
        
    }

}
