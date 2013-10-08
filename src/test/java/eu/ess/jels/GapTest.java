package eu.ess.jels;
import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import xal.model.IElement;
import xal.model.ModelException;
import xal.model.Sector;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.Scenario;
import xal.sim.scenario.ScenarioGenerator2;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.RfGap;
import xal.tools.beam.Twiss;
import eu.ess.jels.smf.impl.ESSRfCavity;

@RunWith(JUnit4.class)
public class GapTest {

	@Test
	public void doGapTest() throws InstantiationException, ModelException {
		System.out.println("Running\n");
		AcceleratorSeq sequence = new AcceleratorSeq("GapTest");
		
		// input from TraceWin
		double frequency = 4.025e8; // this is global in TraceWin
		
		double E0TL = 78019.7 * 1e-6; // Effective gap voltage
		double Phis = -90;  // RF phase (deg) absolute or relative
		double R = 14.5; // aperture
		double p = 0; // 0: relative phase, 1: absolute phase
		
		/*double betas = 0; // particle reduced velocity
		double Ts = 0;  // transit time factor
		double kTs = 0;
		double k2Ts = 0;*/
		double kS = 0;
		double k2S = 0;
		
		double betas = 0.0805777; // particle reduced velocity
		double Ts = 0.772147;  // transit time factor
		double kTs = -0.386355;
		double k2Ts = -0.142834;
		
		// setup		
		RfGap gap = new RfGap("g");
		gap.setFirstGap(true); // this uses only phase for calculations
		gap.getRfGap().setEndCell(0);
		gap.setLength(0.0); // used only for positioning
		
		// following are used to calculate E0TL
		double length = 1.0; // length is not given in TraceWin, but is used only as a factor in E0TL in OpenXal
		gap.getRfGap().setLength(length); 		
		gap.getRfGap().setAmpFactor(1.0);
		/*gap.getRfGap().setGapOffset(dblVal)*/	
		
		ESSRfCavity cavity = new ESSRfCavity("c");
		cavity.addNode(gap);
		cavity.getRfField().setPhase(Phis);		
		cavity.getRfField().setAmplitude(E0TL / length);
		cavity.getRfField().setFrequency(frequency * 1e-6);		
		/*cavity.getRfField().setStructureMode(dblVal);*/
		
		// TTF		
		if (betas == 0.0) {
			gap.getRfGap().setTTF(1.0);		
			cavity.getRfField().setTTFCoefs(new double[] {1.0});
			cavity.getRfField().setTTF_endCoefs(new double[] {1.0});
		} else {
			cavity.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setTTF_endCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setSTFCoefs(new double[] {betas, 0., kS, k2S});
			cavity.getRfField().setSTF_endCoefs(new double[] {betas, 0., kS, k2S});
		}		
		
		sequence.addNode(cavity);
		sequence.setLength(0.0);
				
		// Generates lattice from SMF accelerator
		Scenario oscenario = Scenario.newScenarioFor(sequence);
		Scenario scenario = new ScenarioGenerator2(sequence).generateScenario();
		scenario.resync();
		//Scenario oscenario = Scenario.newAndImprovedScenarioFor(sequence);
				
		// Ensure directory
		new File("temp/gaptest").mkdirs();
		
		// Outputting lattice elements
		TestCommon.saveLattice(scenario.getLattice(), "temp/gaptest/lattice.xml");
		TestCommon.saveLattice(oscenario.getLattice(), "temp/gaptest/elattice.xml");
		TestCommon.saveSequence(sequence, "temp/gaptest/seq.xml");
		
		// Creating a probe		
		EnvelopeProbe probe = TestCommon.setupProbeViaJavaCalls();					
		scenario.setProbe(probe);			
		
		// Prints transfer matrices
		IElement el = (IElement)((Sector)scenario.getLattice().getElementList().get(0)).getChild(1);
		el.transferMap(probe, el.getLength()).getFirstOrder().print();
		
		
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
