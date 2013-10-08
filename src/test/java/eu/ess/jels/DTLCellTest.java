package eu.ess.jels;
import java.io.File;
import java.util.Iterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import xal.model.IComponent;
import xal.model.IElement;
import xal.model.ModelException;
import xal.model.Sector;
import xal.model.elem.ElementSeq;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.sim.scenario.Scenario;
import xal.sim.scenario.ScenarioGenerator2;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.RfGap;
import xal.tools.beam.Twiss;
import eu.ess.jels.smf.impl.ESSRfCavity;

@RunWith(JUnit4.class)
public class DTLCellTest {

	@Test
	public void doDTLCellTest() throws InstantiationException, ModelException {
		System.out.println("Running\n");
		AcceleratorSeq sequence = new AcceleratorSeq("DTLCellTest");
		
		// input from TraceWin
		double frequency = 4.025e8; // this is global in TraceWin
		
		double L = 68.534 * 1e-3;
		double Lq1 = 22.5 * 1e-3;
		double Lq2 = 22.5 * 1e-3;
		double g = 0.00864202 * 1e-3;
		double B1 = 0;
		double B2 = 46.964;
		
		double E0TL = 148174 * 1e-6; // Effective gap voltage
		double Phis = -90;  // RF phase (deg) absolute or relative
		double R = 10; // aperture
		double p = 0; // 0: relative phase, 1: absolute phase
		
		double betas = 0.0805777; // particle reduced velocity
		double Ts = 0.772147;  // transit time factor
		double kTs = -0.386355;
		double k2Ts = -0.142834;
		double kS = 0;
		double k2S = 0;
		
		// setup		
		// QUAD1,2
		Quadrupole quad1 = new Quadrupole("quad1") { // there's no setter for type (you need to extend class)
			{_type="Q"; }
		};
		quad1.setPosition(Lq1*0.5); //always position on center!
		quad1.setLength(Lq1); // effLength below is actually the only one read 
		quad1.getMagBucket().setEffLength(Lq1);
		quad1.setDfltField(B1);
		quad1.getMagBucket().setPolarity(1);
		
		Quadrupole quad2 = new Quadrupole("quad2") { // there's no setter for type (you need to extend class)
			{_type="Q"; }
		};
		quad2.setPosition(L-Lq2*0.5); //always position on center!
		quad2.setLength(Lq2); // effLength below is actually the only one read 
		quad2.getMagBucket().setEffLength(Lq2);
		quad2.setDfltField(B2);
		quad2.getMagBucket().setPolarity(1);
		
		
		// GAP
		RfGap gap = new RfGap("g");
		gap.setFirstGap(true); // this uses only phase for calculations
		gap.getRfGap().setEndCell(0);
		gap.setLength(2*g); // used only for positioning
		gap.setPosition((L+Lq1-Lq2)/2+g);
		// following are used to calculate E0TL
		double length = 1.0; // length is not given in TraceWin, but is used only as a factor in E0TL in OpenXal
		gap.getRfGap().setLength(length); 		
		gap.getRfGap().setAmpFactor(1.0);
		/*gap.getRfGap().setGapOffset(dblVal)*/		
		
		ESSRfCavity dtlTank = new ESSRfCavity("d"); // this could also be rfcavity, makes no difference
		dtlTank.addNode(quad1);
		dtlTank.addNode(gap);
		dtlTank.addNode(quad2);
		dtlTank.getRfField().setPhase(Phis);		
		dtlTank.getRfField().setAmplitude(E0TL / length);
		dtlTank.getRfField().setFrequency(frequency * 1e-6);		
		/*cavity.getRfField().setStructureMode(dblVal);*/
		
		// TTF		
		if (betas == 0.0) {
			gap.getRfGap().setTTF(1.0);		
			dtlTank.getRfField().setTTFCoefs(new double[] {1.0});
		} else {
			dtlTank.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			dtlTank.getRfField().setTTF_endCoefs(new double[] {betas, Ts, kTs, k2Ts});
			dtlTank.getRfField().setSTFCoefs(new double[] {betas, 0., kS, k2S});
			dtlTank.getRfField().setSTF_endCoefs(new double[] {betas, 0., kS, k2S});
		}		
		
		sequence.addNode(dtlTank);
		sequence.setLength(L);
				
		// Generates lattice from SMF accelerator
		Scenario oscenario = Scenario.newScenarioFor(sequence);
		Scenario scenario = new ScenarioGenerator2(sequence).generateScenario();
		scenario.resync();
		oscenario.resync();
		//Scenario oscenario = Scenario.newAndImprovedScenarioFor(sequence);
		
		// Ensure directory
		new File("temp/dtlcelltest").mkdirs();
		
		// Outputting lattice elements
		TestCommon.saveLattice(scenario.getLattice(), "temp/dtlcelltest/lattice.xml");
		TestCommon.saveLattice(oscenario.getLattice(), "temp/dtlcelltest/elattice.xml");
		TestCommon.saveSequence(sequence, "temp/dtlcelltest/seq.xml");
		
		// Creating a probe		
		EnvelopeProbe probe = TestCommon.setupProbeViaJavaCalls();					
		scenario.setProbe(probe);			
		
		// Prints transfer matrices
		for (IComponent el : ((ElementSeq)((Sector)scenario.getLattice().getElementList().get(0))).getElementList() )
		{		
			((IElement)el).transferMap(probe, el.getLength()).getFirstOrder().print();
		}
		
		
		// Setting up synchronization mode
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);					
		scenario.resync();
				
		// Running simulation
		scenario.run();
				
		// Getting results
		Iterator<ProbeState> it = scenario.getTrajectory().stateIterator();
		while (it.hasNext()) {
			EnvelopeProbeState ps = (EnvelopeProbeState)it.next();
			Twiss[] t = ps.getCorrelationMatrix().computeTwiss();
			
			double[] beta = new double[3];
			for (int i=0; i<3; i++) beta[i] = t[i].getBeta();
			beta[2]/=ps.getGamma()*ps.getGamma();
			
			double[] sigma = new double[3];
			double gamma = ps.getGamma();
			for (int i=0; i<3; i++)
				sigma[i] = Math.sqrt(beta[i]*t[i].getEmittance()/Math.sqrt(1.0 - 1.0/(gamma*gamma))/gamma);		
			System.out.printf("%E %E %E %E ",ps.getPosition(), sigma[0], sigma[1], sigma[2]);
			System.out.printf("%E %E %E\n", beta[0], beta[1], beta[2]);
			
		}
		
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
