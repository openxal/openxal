package xal.model.probe;

import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.probe.DiagnosticProbe;
import xal.model.xml.LatticeXmlParser;
import xal.model.xml.ParsingException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Contains Junit test cases for creating and running a <code>DiagnosticProbe
 * </code>.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class DiagnosticProbeTest extends TestCase {

	private static final String XML_IN = "xml/SnsMebt.lat.mod.xal.xml";

	/**
	 * Entry point for the JUnit 3 test suite.
	 *
	 * @param args     command line arguments (not used)
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());
	}
	
	/**
	 * Creates a new JUnit <code>Test</code> object from
	 * this class.
	 *
	 * @return     <code>TestSuite</code> object initialized with this class type
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public static Test suite() {
		return new TestSuite(DiagnosticProbeTest.class);
	}
	
	/**
	 * Propagates a <code>DiagnosticProbe</code> through a 
	 * <code>Lattice</code> object.
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public void testPropagateProbe() {
        LatticeXmlParser parser = new LatticeXmlParser();
		Lattice lattice = null;
		try {
			lattice = parser.parseUrl(XML_IN, false);
		} catch (ParsingException e) {
			e.printStackTrace();
			fail("Lattice Parsing Exception: " + e.getMessage());
		}
		try {
			DiagnosticProbe probe = new DiagnosticProbe();
			lattice.propagate(probe);
		} catch (ModelException e) {
			fail("ModelException propagating DiagnosticProbe through Lattice");
		}
	}
	
}
