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
 * JUnit 4.x test suite for performing tests on the <code>gov.sns.tools.math</code> 
 * package classes.
 * 
 * @author Christopher K. Allen
 *
 */
public class TestSuiteProbe {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test suite for gov.sns.xal.model.probe.tests");
        //$JUnit-BEGIN$
        suite.addTest(TestTwissProbe.getJUnitTest());
        //$JUnit-END$
        return suite;
    }

}
