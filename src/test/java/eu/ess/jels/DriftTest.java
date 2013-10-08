package eu.ess.jels;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.Scenario;
import xal.sim.scenario.ScenarioGenerator2;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.Twiss;

@RunWith(JUnit4.class)
public class DriftTest {

	@Test
	public void doDriftTest() throws InstantiationException, ModelException {
		System.out.println("Running\n");
		
		AcceleratorSeq sequence = new AcceleratorSeq("DriftTest");
		
		double L = 95e-3; //length
		double R = 0.; //aperture
		double Ry = 0.; //aperture y
		
		sequence.setLength(L);
				
		// Generates lattice from SMF accelerator
		//Scenario oscenario = Scenario.newScenarioFor(sequence);
		Scenario scenario = new ScenarioGenerator2(sequence).generateScenario();
		//Scenario scenario = Scenario.newAndImprovedScenarioFor(sequence);
				
		// Outputting lattice elements
		//saveLattice(scenario.getLattice(), "lattice.xml");
		//saveLattice(escenario.getLattice(), "elattice.xml");
		
		// Creating a probe
		EnvelopeProbe probe = TestCommon.setupProbeViaJavaCalls();					
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
		
		/* ELS output: 9.500000E-02 9.523765E-04 1.177297E-03 1.952594E-03 3.158031E-01 4.841974E-01 9.768578E-01 */
	}

	
}
