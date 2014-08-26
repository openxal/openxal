package xal.model.probe.traj;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * class comment
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 * @deprecated  Replaced by TestTrajectory
 */
@Deprecated
public class TrajectoryTest extends TestCase {

	private static final String XML_IN = "xml/ModelValidation.lat.mod.xal.xml";

	/**
	 * JUnit 3 test suite entry point.
	 *
	 * @param args command line arguments (not used)
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());
	}
	
	/**
	 * Convenience method creating a new <code>Test</code>
	 * object initialized to this class type.
	 *
	 * @return the object <code>new TestSuite(Trajectory
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public static Test suite() {
		return new TestSuite(TrajectoryTest.class);
	}
	
	/**
	 * Test requesting states from a trajectory object.
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public void testTrajectoryQueries() {
//        LatticeXmlParser parser = new LatticeXmlParser();
//		Lattice lattice = null;
//		try {
//			lattice = parser.parseUrl(XML_IN, false);
//		} catch (ParsingException e) {
//			e.printStackTrace();
//			fail("Lattice Parsing Exception: " + e.getMessage());
//			return;
//		}
//		DiagnosticProbe probe = null;
//		try {
//			probe = new DiagnosticProbe();
//			lattice.propagate(probe);
//		} catch (ModelException e) {
//			fail("ModelException propagating DiagnosticProbe through Lattice");
//		}
//		Trajectory trajectory = probe.getTrajectory();
//		Iterator<ProbeState> it = trajectory.stateIterator();
//		while (it.hasNext()) {
//			ProbeState state = it.next();
//			System.out.println(state);
//		}
//		ProbeState[] statesByElem = trajectory.statesForElement("DR1");
//		for (int i=0 ; i<statesByElem.length ; i++) {
//			System.out.println(statesByElem[i]);
//		}
//		ProbeState[] statesByPos = trajectory.statesInPositionRange(0.16, 0.174);
//		for (int i=0 ; i<statesByPos.length ; i++) {
//			System.out.println(statesByPos[i]);
//		}
	}
	
		
	
}
