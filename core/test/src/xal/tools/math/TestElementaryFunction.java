/**
 * TestElementaryFunction.java
 *
 * Author  : Christopher K. Allen
 * Since   : Dec 31, 2015
 */
package xal.tools.math;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test cases for the <code>ElementaryFunction</code> utility class.
 *
 *
 * @author Christopher K. Allen
 * @since  Dec 31, 2015
 */
public class TestElementaryFunction {

    /**
     * @throws java.lang.Exception
     *
     * @since  Dec 31, 2015,   Christopher K. Allen
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     *
     * @since  Dec 31, 2015,   Christopher K. Allen
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * Test method for {@link xal.tools.math.ElementaryFunction#approxEq(double, double)}.
     */
    @Test
    public final void testApproxEq() {
        double      x  = 0.1111111112e-5;
        double      dx = Math.ulp(x);
        double      y = x + (ElementaryFunction.ULPS_DEFLT_BRACKET - 1)*dx;
        
        if (!ElementaryFunction.approxEq(x, y))
            fail("Default ULPs bracketing approximatly equal to failed");
    }

    /**
     * Test method for {@link xal.tools.math.ElementaryFunction#approxEq(double, double, int)}.
     */
    @Test
    public final void testApproxEqUlps() {
        double      x = 0.11111111111112e5;
        double      y = 0.11111111111111e5;
        
        if (!ElementaryFunction.approxEq(x, x, 1))
            fail("Same number seen different by more than 1 ULP");
        
        if (!ElementaryFunction.approxEq(x, y, Integer.MAX_VALUE))
            fail("Numbers are equal to 1000 ULPs");
    }

    /**
     * Test method for {@link xal.tools.math.ElementaryFunction#significantDigitsEqs(double, double, int)}.
     */
    @Test
    public final void testSignificantDigitsEqs() {
        double      x = 0.1111111112;
        double      y = 0.1111111111;
        
        if (!ElementaryFunction.significantDigitsEqs(x, y, 5))
            fail("Numbers should compare to 9 significant digits");
        
        if (ElementaryFunction.significantDigitsEqs(x, y, 10))
            fail("Numbers differ at 10 significant digits");
    }

}
