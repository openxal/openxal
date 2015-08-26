/**
 * TestSuiteProbe.java
 * 
 * Created  : December, 2006
 * Author   : Christopher K. Allen
 */

package xal.model.probe;


import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * JUnit 4.x test suite for performing tests on the <code>xal.tools.math</code> 
 * package classes.
 * 
 * @author Christopher K. Allen
 *
 */
public class TestSuiteProbe {

    /**
     *
     * @return  a test suite for probe objects
     *
     * @author Christopher K. Allen
     * @since  Aug 26, 2011
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Test suite for xal.model.probe.tests");
        //$JUnit-BEGIN$
        suite.addTest(TestTwissProbe.getJUnitTest());
        //$JUnit-END$
        return suite;
    }

}
