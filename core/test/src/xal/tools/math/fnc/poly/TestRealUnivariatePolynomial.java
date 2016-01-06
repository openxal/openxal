/**
 * TestRealUnivariatePolynomial.java
 * 
 * Tests the class <code>RealUnivariatePolynomial</code>.
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 25, 2015
 */
package xal.tools.math.fnc.poly;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.tools.math.fnc.poly.RealUnivariatePolynomial;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Sep 25, 2015
 */
public class TestRealUnivariatePolynomial {

    
    /*
     * Global Constants
     */
    
    /** A test polynomial */
    static private RealUnivariatePolynomial     POLY_TEST;
    
    
    /*
     * Global Methods
     */
    /**
     * @throws java.lang.Exception
     *
     * @since  Sep 25, 2015   by Christopher K. Allen
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        double[]    arrCoeffs = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0 };
        
        POLY_TEST = new RealUnivariatePolynomial(arrCoeffs);
    }

    /**
     * @throws java.lang.Exception
     *
     * @since  Sep 25, 2015   by Christopher K. Allen
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    
    /*
     * Tests
     */
    
    /**
     * Test method for {@link xal.tools.math.fnc.poly.RealUnivariatePolynomial#evaluateAt(double)}.
     */
    @Test
    public final void testEvaluateAt() {
        assertTrue(POLY_TEST.evaluateAt(0.0) == 1.0);
        assertTrue(POLY_TEST.evaluateAt(1.0) == 15.0);
        assertTrue(POLY_TEST.evaluateAt(2.0) == 129.0);
    }

    /**
     * Test method for {@link xal.tools.math.fnc.poly.RealUnivariatePolynomial#derivativeAt(double)}.
     */
    @Test
    public final void testDerivativeAtDouble() {
        
    }

    /**
     * Test method for {@link xal.tools.math.fnc.poly.RealUnivariatePolynomial#derivativeAt(int, double)}.
     */
    @Test
    public final void testDerivativeAtIntDouble() {
        assertTrue(POLY_TEST.derivativeAt(0, 0.0) == 1.0);
        assertTrue(POLY_TEST.derivativeAt(0, 1.0) == 15.0);
        assertTrue(POLY_TEST.derivativeAt(0, 2.0) == 129.0);
        
        assertTrue(POLY_TEST.derivativeAt(1, 0.0) == 2.0);
        assertTrue(POLY_TEST.derivativeAt(1, 1.0) == 40.0);
        assertTrue(POLY_TEST.derivativeAt(1, 2.0) == 222.0);
        
        assertTrue(POLY_TEST.derivativeAt(2, 0.0) == 6.0);
        assertTrue(POLY_TEST.derivativeAt(2, 1.0) == 90.0);
        assertTrue(POLY_TEST.derivativeAt(2, 2.0) == 294.0);
        
    }

}
