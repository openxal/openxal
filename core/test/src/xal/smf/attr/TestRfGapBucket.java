/**
 * TestRfGapBucket.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jun 1, 2015
 */
package xal.smf.attr;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.RfGap;
import xal.test.ResourceManager;
import xal.tools.math.fnc.poly.RealUnivariatePolynomial;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Jun 1, 2015
 */
public class TestRfGapBucket {
    
    /*
     * Global Variables
     */
    
    /** The accelerator object containing the bucket structure under test */
    private static Accelerator        ACCL_TEST;
    
    /** Stream where output is directed */
    private static PrintStream        OSTR_TYPEOUT;
    

    /**
     * Sets up the global variables used in tests.
     * 
     * @throws java.lang.Exception
     *
     * @since  Jun 1, 2015   by Christopher K. Allen
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        File    fileOutput = ResourceManager.getOutputFile(TestRfGapBucket.class, TestRfGapBucket.class.getName() + ".txt");
        OSTR_TYPEOUT = new PrintStream(fileOutput);
//        OSTR_TYPEOUT = System.out;
        
       OSTR_TYPEOUT.println("Loading Test Accelerator");
       ACCL_TEST = ResourceManager.getTestAccelerator();
       OSTR_TYPEOUT.println("Test Accelerator Loaded");
    }

    /**
     * @throws java.lang.Exception
     *
     * @since  Jun 1, 2015   by Christopher K. Allen
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * Test method for {@link xal.smf.attr.RfGapBucket#getTCoefficients()}.
     */
    @Test
    public final void testGetTCoefficients() {
        AcceleratorSeq  seqMebt = ACCL_TEST.getSequence("MEBT");
        
        
        List<RfGap>   lstGaps = seqMebt.getAllNodesOfType("RG");
        for (RfGap smfGap : lstGaps) {
            RealUnivariatePolynomial polyTFit = smfGap.getTTFFit();
            RealUnivariatePolynomial polySFit = smfGap.getSFit();
            RealUnivariatePolynomial polyTpFit = smfGap.getTTFPrimeFit();
            RealUnivariatePolynomial polySpFit = smfGap.getSPrimeFit();
            
            OSTR_TYPEOUT.println("\nNODE: " + smfGap.getId());
            OSTR_TYPEOUT.println("T(x) = " + polyTFit.toString());
            OSTR_TYPEOUT.println("T'(x) = " + polyTpFit.toString());
            OSTR_TYPEOUT.println("S(x) = " + polySFit.toString());
            OSTR_TYPEOUT.println("S'(x) = " + polySpFit.toString());
        }
    }

    /**
     * Test method for {@link xal.smf.attr.RfGapBucket#getTCoefficients()}.
     */
    @Test
    public final void testGetTCoefficientsCCL() {
        AcceleratorSeq  seqDtl = ACCL_TEST.getComboSequence("CCL");
        
        
        List<RfGap>   lstGaps = seqDtl.getAllNodesOfType("RG");
        for (RfGap smfGap : lstGaps) {
            RealUnivariatePolynomial polyTFit = smfGap.getTTFFit();
            RealUnivariatePolynomial polySFit = smfGap.getSFit();
            RealUnivariatePolynomial polyTpFit = smfGap.getTTFPrimeFit();
            RealUnivariatePolynomial polySpFit = smfGap.getSPrimeFit();
            
            OSTR_TYPEOUT.println("\nNODE: " + smfGap.getId());
            OSTR_TYPEOUT.println("T(x) = " + polyTFit.toString());
            OSTR_TYPEOUT.println("T'(x) = " + polyTpFit.toString());
            OSTR_TYPEOUT.println("S(x) = " + polySFit.toString());
            OSTR_TYPEOUT.println("S'(x) = " + polySpFit.toString());
        }
    }
    
//    /**
//     * Test method for {@link xal.smf.attr.RfGapBucket#getTpCoefficients()}.
//     */
//    @Test
//    public final void testGetTpCoefficients() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link xal.smf.attr.RfGapBucket#getSCoefficients()}.
//     */
//    @Test
//    public final void testGetSCoefficients() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link xal.smf.attr.RfGapBucket#getSpCoefficients()}.
//     */
//    @Test
//    public final void testGetSpCoefficients() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link xal.smf.attr.RfGapBucket#setTCoefficients(double[])}.
//     */
//    @Test
//    public final void testSetTCoefficients() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for {@link xal.smf.attr.RfCavityBucket#setTpCoefficients(double[])}.
//     */
//    @Test
//    public final void testSetTpCoefficients() {
//        fail("Not yet implemented"); // TODO
//    }
//
}
