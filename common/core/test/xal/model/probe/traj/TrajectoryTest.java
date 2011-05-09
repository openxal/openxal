package xal.model.probe.traj;

import java.io.PrintWriter;
import java.util.Iterator;

import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.alg.resp.ParticleResponse;
import xal.model.probe.DiagnosticProbe;
import xal.model.probe.resp.ParticlePerturb;
import xal.model.probe.resp.traj.ParticlePerturbProbeState;
import xal.model.probe.resp.traj.ParticlePerturbProbeTrajectory;
import xal.model.xml.LatticeXmlParser;
import xal.model.xml.ParsingException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * class comment
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
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
        LatticeXmlParser parser = new LatticeXmlParser();
		Lattice lattice = null;
		try {
			lattice = parser.parseUrl(XML_IN, false);
		} catch (ParsingException e) {
			e.printStackTrace();
			fail("Lattice Parsing Exception: " + e.getMessage());
		}
		DiagnosticProbe probe = null;
		try {
			probe = new DiagnosticProbe();
			lattice.propagate(probe);
		} catch (ModelException e) {
			fail("ModelException propagating DiagnosticProbe through Lattice");
		}
		Trajectory trajectory = probe.getTrajectory();
		Iterator<ProbeState> it = trajectory.stateIterator();
		while (it.hasNext()) {
			ProbeState state = it.next();
			System.out.println(state);
		}
		ProbeState[] statesByElem = trajectory.statesForElement("DR1");
		for (int i=0 ; i<statesByElem.length ; i++) {
			System.out.println(statesByElem[i]);
		}
		ProbeState[] statesByPos = trajectory.statesInPositionRange(0.16, 0.174);
		for (int i=0 ; i<statesByPos.length ; i++) {
			System.out.println(statesByPos[i]);
		}
	}
	
	/**
	 * Test the particle perturbation state
	 * retrieval.
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	@SuppressWarnings("deprecation")
	public void testParticlePerturbStateResponse() {
		LatticeXmlParser parser = new LatticeXmlParser();
		Lattice lattice = null;
		try {
			lattice = parser.parseUrl(XML_IN, false);
		} catch (ParsingException e) {
			e.printStackTrace();
			fail("Lattice Parsing Exception: " + e.getMessage());
		}
		ParticlePerturb probe = null;
		try {
			probe = new ParticlePerturb();
			probe.setAlgorithm(new ParticleResponse());
			probe.setSpeciesRestEnergy(939.3014e+6);
			probe.setSpeciesCharge(-1.602e-19);
			probe.setKineticEnergy(2.5e+6);	
			lattice.propagate(probe);
		} catch (ModelException e) {
			fail("ModelException propagating DiagnosticProbe through Lattice");
		}
		ParticlePerturbProbeTrajectory trajectory = 
			(ParticlePerturbProbeTrajectory) probe.getTrajectory();
			
		Iterator<ProbeState> stIt = trajectory.stateIterator();
		while (stIt.hasNext()) {
			ParticlePerturbProbeState state = (ParticlePerturbProbeState) stIt.next();
			PrintWriter out = new PrintWriter(System.out);
			state.getResponse().print(out);	
			out.flush();
			System.out.println();
		}
		
	}

		
	
}
