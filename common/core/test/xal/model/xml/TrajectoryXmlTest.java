package xal.model.xml;

import java.io.IOException;
import xal.tools.beam.CorrelationMatrix;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.ens.Ensemble;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.alg.DiagnosticTracker;
import xal.model.alg.EnsembleTracker;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.ParticleTracker;
import xal.model.alg.resp.ParticleResponse;
import xal.model.probe.DiagnosticProbe;
import xal.model.probe.EnsembleProbe;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.Probe;
import xal.model.probe.resp.ParticlePerturb;
import xal.model.probe.traj.Trajectory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test cases for writing and reading xml from probe trajectory objects.  This
 * class assumes that it is being run from within the work directory, that is
 * the directory containing the xml directory.  Creates and initializes each type
 * of probe, runs it through the test lattice, captures the resulting trajectory,
 * writes it to an xml file, parses the xml file to create a new trajectory,
 * and compares the original and parsed trajectories (only at a high level - 
 * doesn't actually compare states).
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class TrajectoryXmlTest extends TestCase {
	
	private static final String LATTICE = 
		"xml/SimpleTestLattice.lat.mod.xal.xml";
	private static final String DIAGNOSTIC = 
		"build/tests/output/xal/model/xml/TrajectoryXmlTest.Diagnostic.trajectory.mod.xal.xml";
	private static final String PARTICLE = 
		"build/tests/output/xal/model/xml/TrajectoryXmlTest.Particle.trajectory.mod.xal.xml";
	private static final String ENVELOPE = 
		"build/tests/output/xal/model/xml/TrajectoryXmlTest.Envelope.trajectory.mod.xal.xml";
	private static final String ENSEMBLE = 
        "build/tests/output/xal/model/xml/TrajectoryXmlTest.Ensemble.trajectory.mod.xal.xml";
	private static final String PARTICLE_PERTURB = 
		"build/tests/output/xal/model/xml/TrajectoryXmlTest.ParticlePerturb.trajectory.mod.xal.xml";

	private Lattice lattice;

	/**
	 * Entry point for JUnit 3 test suite.
	 *
	 * @param args command line arguments (not used)
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public static void main(String[] args) {
		TestRunner.run (suite());
	}
	
	/**
	 * Returns a JUnit <code>Test</code> object initialized
	 * from this class type.
	 *
	 * @return the object <code>new TestSuite(Trajectory.XalTest.class)</code>
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public static Test suite() {
		return new TestSuite(TrajectoryXmlTest.class);
	}
	
	private Lattice getLattice() {
		if (lattice == null) {
			//create a Lattice
	        LatticeXmlParser parser = new LatticeXmlParser();
			try {
				lattice = parser.parseUrl(LATTICE, false);
			} catch (ParsingException e) {
				e.printStackTrace();
				fail("Lattice Parsing Exception: " + e.getMessage());
			} catch (Exception e)   {
	                    e.printStackTrace();
	                    fail("Lattice parsing general exception: " + e.getMessage());
	        }
		}
		return lattice;
	}
		
	private void runProbe(Probe probe, String outfile) {		
		//run probe through lattice
		try {
			getLattice().propagate(probe);
		} catch (ModelException e) {
			fail("ModelException propagating Probe: " + 
				probe.getClass().getName());
		}		
		Trajectory origTrajectory = probe.getTrajectory();
		origTrajectory.setDescription("Test Probe Run: " + 
				probe.getClass().getName());		
		//write trajectory to xml		
		try {
			TrajectoryXmlWriter.writeXml(origTrajectory, outfile);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException on: " + outfile);
		}
		Trajectory trajectory = parse(outfile);
		assertTrue(trajectory.getDescription().equals(origTrajectory.getDescription()));
		assertTrue(trajectory.getTimestamp().equals(origTrajectory.getTimestamp()));
		assertTrue(trajectory.numStates() == origTrajectory.numStates());
	}
	
	private Trajectory parse(String fileUri) {
		try {
			Trajectory trajectory = TrajectoryXmlParser.parse(fileUri);
			System.out.println(trajectory);
			return trajectory;
		} catch (ParsingException e) {
			e.printStackTrace();
			fail("ParsingException reading: " + fileUri);
			return null;
		}		
	}

	/**
	 * Test the <code>DiagnosticProbeTrajectory</code> class.
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public void testDiagnosticProbeTrajectory() {		
		DiagnosticProbe probe = new DiagnosticProbe();		
        probe.setAlgorithm( new DiagnosticTracker() );
		runProbe(probe, DIAGNOSTIC);				
	}
	
    /**
     * Test the <code>ParticleProbeTrajectory</code> class.
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
	public void testParticleProbeTrajectory() {		
		ParticleProbe probe = new ParticleProbe();
		probe.setAlgorithm(new ParticleTracker());
		probe.setPhaseCoordinates(PhaseVector.zero());	
		probe.setSpeciesRestEnergy(939.3014e+6);
		probe.setSpeciesCharge(1.602e-19);
		probe.setKineticEnergy(2.5e+6);	
		runProbe(probe, PARTICLE);				
	}
	
    /**
     * Test the <code>EnvelopeProbeTrajectory</code> class.
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
	public void testEnvelopeProbeTrajectory() {		
		EnvelopeProbe probe = new EnvelopeProbe();
		probe.setAlgorithm(new EnvelopeTracker());
		probe.setCorrelation(new CorrelationMatrix(PhaseMatrix.identity()));	
		probe.setSpeciesRestEnergy(939.3014e+6);
		probe.setSpeciesCharge(1.602e-19);
        probe.setKineticEnergy(2.5e+6); 
        probe.setBeamCurrent(0.025);
        //doesnt work any longer probe.setBeamCharge(6.11e-11);
        probe.setBunchFrequency(324e+6);
		runProbe(probe, ENVELOPE);	
	}
	
    /**
     * Test the <code>EnsembleProbeTrajectory</code> class.
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
	public void testEnsembleProbeTrajectory() {		
		EnsembleProbe probe = new EnsembleProbe();
		probe.setAlgorithm(new EnsembleTracker());
		probe.setEnsemble(new Ensemble());
		probe.setSpeciesRestEnergy(939.3014e+6);
		probe.setSpeciesCharge(1.602e-19);
        probe.setKineticEnergy(2.5e+6); 
		runProbe(probe, ENSEMBLE);	
	}
	
    /**
     * Test the <code>ParticlePerturbProbeTrajectory</code> class.
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
	@SuppressWarnings("deprecation")
    public void testParticlePerturbProbeTrajectory() {		
		ParticlePerturb probe = new ParticlePerturb();
		probe.setAlgorithm(new ParticleResponse());
		probe.setSpeciesRestEnergy(939.3014e+6);
		probe.setSpeciesCharge(-1.602e-19);
		probe.setKineticEnergy(2.5e+6);	
		runProbe(probe, PARTICLE_PERTURB);	
	}
	
}
