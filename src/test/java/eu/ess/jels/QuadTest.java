package eu.ess.jels;
import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.Scenario;
import xal.sim.scenario.ScenarioGenerator2;
import xal.smf.AcceleratorSeq;
import xal.smf.attr.ApertureBucket;
import xal.smf.impl.Quadrupole;
import xal.tools.beam.Twiss;

@RunWith(JUnit4.class)
public class QuadTest {

	@Test
	public void doQuadTest() throws InstantiationException, ModelException {
		System.out.println("Running\n");
		AcceleratorSeq sequence = new AcceleratorSeq("QuadTest");
		
		double L = 70e-3; // length
		double G = -16; // field
		double R = 15; // aperture
		double Phi = 0; // skew angle
		double G3 = 0; // sextupole gradient (T/m^2)
		double G4 = 0; // octupole gradient (T/m^2)
		double G5 = 0; // decapole gradient (T/m^2)
		double G6 = 0; // dodecapole gradient (T/m^2)
		
		EnvelopeProbe probe = TestCommon.setupProbeViaJavaCalls();
		
		Quadrupole quad = new Quadrupole("quad") { // there's no setter for type (you need to extend class)
			{_type="Q"; }
		};
		quad.setPosition(L*0.5); //always position on center!
		quad.setLength(L); // effLength below is actually the only one read 
		quad.getMagBucket().setEffLength(L);
		quad.setDfltField(G*Math.signum(probe.getSpeciesCharge()));
		quad.getMagBucket().setPolarity(1);
		quad.getAper().setAperX(15e-3);
		quad.getAper().setAperY(15e-3);
		quad.getAper().setShape(ApertureBucket.iRectangle);
		sequence.addNode(quad);
		sequence.setLength(70e-3);		
				
		// Generates lattice from SMF accelerator
		//Scenario oscenario = Scenario.newScenarioFor(sequence);
		Scenario scenario = new ScenarioGenerator2(sequence).generateScenario();
		Scenario oscenario = Scenario.newAndImprovedScenarioFor(sequence);
		
		// Ensure directory
		new File("temp/quadtest").mkdirs();
		
		// Outputting lattice elements
		TestCommon.saveLattice(scenario.getLattice(), "temp/quadtest/lattice.xml");
		TestCommon.saveLattice(oscenario.getLattice(), "temp/quadtest/elattice.xml");
		TestCommon.saveSequence(sequence, "temp/quadtest/seq.xml");
		
		// Creating a probe
							
		scenario.setProbe(probe);			
		
				
		// Setting up synchronization mode
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);					
		scenario.resync();
				
		// Running simulation
		scenario.run();
				
		// Getting results
		Twiss[] t = probe.getCovariance().computeTwiss();
		
		double[] beta = new double[3];
		for (int i=0; i<3; i++) beta[i] = t[i].getBeta();
		beta[2]/=probe.getGamma()*probe.getGamma();
		
		double[] sigma = new double[3];
		for (int i=0; i<3; i++)
			sigma[i] = Math.sqrt(beta[i]*t[i].getEmittance()/probe.getBeta()/probe.getGamma());		
		System.out.printf("%E %E %E %E ",probe.getPosition(), sigma[0], sigma[1], sigma[2]);
		System.out.printf("%E %E %E\n", beta[0], beta[1], beta[2]);
		
		/* ELS output: 7.000000E-02 1.060867E-03 9.629023E-04 1.920023E-03 3.918513E-01 3.239030E-01 9.445394E-01 */
	}


}
