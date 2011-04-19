/*
 * Created on Jul 29, 2003
 */
package xal.model.probe;

import java.io.PrintWriter;

import xal.tools.beam.PhaseMatrix;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.alg.resp.ParticleResponse;
import xal.model.probe.resp.ParticlePerturb;
import xal.model.probe.resp.traj.ParticlePerturbProbeTrajectory;
import xal.model.xml.LatticeXmlParser;
import xal.model.xml.ParsingException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author Craig McChesney
 */
public class TestParticlePerturbProbeTrajectory extends TestCase {

	private static final String LATTICE = 
		"xml/ModelValidation.lat.mod.xal.xml";
//	private static final String PARTICLE_PERTURB =
//		"ProbeXmlWriterTest.ParticlePerturb.probe.mod.xal.xml";

	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	public static Test suite() {
		return new TestSuite(TestParticlePerturbProbeTrajectory.class);
	}

	public void testParticlePerturbProbeTrajectory() {

		// build the lattice by parsing from XML
		Lattice lattice = null;
		;
		LatticeXmlParser parser = new LatticeXmlParser();
		try {
			lattice = parser.parseUrl(LATTICE, false);
		} catch (ParsingException e) {
			e.printStackTrace();
			fail("Lattice Parsing Exception: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Lattice parsing general exception: " + e.getMessage());
		}

		// create the probe - could also read this from xml, e.g., :
		// ParticlePerturb parsedProbe = 
		//		(ParticlePerturb) ProbeXmlParser.parse("ParticlePerturb.probe.mod.xal.xml");
		ParticlePerturb probe = new ParticlePerturb();
		probe.setAlgorithm(new ParticleResponse());
		probe.setSpeciesRestEnergy(939.3014e+6);
		probe.setSpeciesCharge(-1.602e-19);
		probe.setKineticEnergy(2.5e+6);

		// propagate probe
		try {
			lattice.propagate(probe);
		} catch (ModelException e) {
			fail(
				"ModelException propagating Probe: "
					+ probe.getClass().getName());
		}

		// get response matrix between center markers

		ParticlePerturbProbeTrajectory traj =
			(ParticlePerturbProbeTrajectory) probe.getTrajectory();
		PhaseMatrix response =
			traj.stateResponse(
				"ELEMENT_CENTER:MEBT_Mag:QH01",
				"ELEMENT_CENTER:MEBT_Mag:QV02");
		PrintWriter pw = new PrintWriter(System.out);
		response.print(pw);
		pw.flush();
	}

}

