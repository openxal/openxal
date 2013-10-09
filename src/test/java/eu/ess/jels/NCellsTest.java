package eu.ess.jels;
import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import xal.model.IComponent;
import xal.model.IElement;
import xal.model.ModelException;
import xal.model.Sector;
import xal.model.elem.ElementSeq;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.Scenario;
import xal.sim.scenario.ScenarioGenerator2;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.Twiss;
import eu.ess.jels.smf.impl.ESSRfCavity;
import eu.ess.jels.smf.impl.ESSRfGap;

@RunWith(JUnit4.class)
public class NCellsTest {

	@Test
	public void doGapTest() throws InstantiationException, ModelException {
		System.out.println("Running\n");
		AcceleratorSeq sequence = new AcceleratorSeq("GapTest");
		
		// input from TraceWin
		// NCELLS 0 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0.386525 0.664594 0.423349 0.350508 0.634734 0.628339 0.249724 0.639103 0.622128 0.25257
		double frequency = 4.025e8; // this is global in TraceWin
		
		int m = 0;
		int n = 3;
		double betag = 0.5;
		double E0T = 5.27924e+06 * 1e-6; 
		double Phis = -72.9826;
		double R = 31;
		double p = 0;
		
		double kE0Ti = 0.493611;
		double kE0To = 0.488812;
		
		double dzi = 12.9359 * 1e-3;
		double dzo = -14.4824 * 1e-3;
		
		double betas = 0.386525;
		
		double Ts =  0.664594;
		double kTs = 0.423349;
		double k2Ts = 0.350508;
		
		double Ti = 0.634734;
		double kTi = 0.628339;
		double k2Ti = 0.249724;
		
		double To = 0.639103;
		double kTo = 0.622128;
		double k2To = 0.25257;
		
		ESSRfCavity cavity = new ESSRfCavity("c");
		cavity.getRfField().setPhase(Phis);
		cavity.getRfField().setAmplitude(E0T);
		cavity.getRfField().setFrequency(frequency * 1e-6);	

		// TTF		
		if (betas == 0.0) {
			cavity.getRfField().setTTF_startCoefs(new double[] {1.0});
			cavity.getRfField().setTTFCoefs(new double[] {1.0});
			cavity.getRfField().setTTF_endCoefs(new double[] {1.0});
		} else {
			cavity.getRfField().setTTF_startCoefs(new double[] {betas, Ti, kTi, k2Ti});
			cavity.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setTTF_endCoefs(new double[] {betas, To, kTo, k2To});			
		}		

		
		// setup		
		ESSRfGap firstgap = new ESSRfGap("g0");
		
		double lambda = IElement.LightSpeed/frequency;
		double Lc0,Lc,Lcn;
		double amp0,amp,ampn;
		
		amp0 = (1+kE0Ti)*(Ti/Ts);
		amp = (1+kE0Ti)*(Ti/Ts); // verify this
		amp = 1;
		ampn = (1+kE0To)*(To/Ts);
		if (m==0) {
			Lc = Lc0 = Lcn = betag * lambda;			
			amp = 1;			
		} else if (m==1) {
			Lc = Lc0 = Lcn = 0.5 * betag * lambda;		
			cavity.getRfField().setStructureMode(1);
		} else { //m==2
			Lc0 = Lcn = 0.75 * betag * lambda;
			Lc = betag * lambda;				
		}
						
		firstgap.setFirstGap(true); // this uses only phase for calculations
		firstgap.getRfGap().setEndCell(0);
		firstgap.setLength(0); // used only for positioning
		firstgap.setPosition(0.5*Lc0 + dzi);
		
		// following are used to calculate E0TL		
		firstgap.getRfGap().setLength(Lc0); 		
		firstgap.getRfGap().setAmpFactor(amp0);
		firstgap.getRfGap().setTTF(1);
		
		cavity.addNode(firstgap);
				
		for (int i = 1; i<n-1; i++) {
			ESSRfGap gap = new ESSRfGap("g"+i);
			gap.getRfGap().setTTF(1);
			gap.setPosition(Lc0 + (i-0.5)*Lc);
			gap.setLength(0);
			gap.getRfGap().setLength(Lc);
			gap.getRfGap().setAmpFactor(amp);
			cavity.addNode(gap);
		}
		
		ESSRfGap lastgap = new ESSRfGap("g"+(n-1));
		lastgap.getRfGap().setEndCell(1);
		lastgap.setLength(0); // used only for positioning
		lastgap.setPosition(Lc0 + (n-2)*Lc + 0.5*Lcn + dzo);
		
		// following are used to calculate E0TL		
		lastgap.getRfGap().setLength(Lcn); 		
		lastgap.getRfGap().setAmpFactor(ampn);
		lastgap.getRfGap().setTTF(1);
		cavity.addNode(lastgap);		
		
		sequence.addNode(cavity);
		sequence.setLength(Lc0+(n-2)*Lc+Lcn);
				
		// Generates lattice from SMF accelerator
		Scenario oscenario = Scenario.newScenarioFor(sequence);
		Scenario scenario = new ScenarioGenerator2(sequence).generateScenario();
		oscenario.resync();
		scenario.resync();
		//Scenario oscenario = Scenario.newAndImprovedScenarioFor(sequence);
				
		// Ensure directory
		new File("temp/ncellstest").mkdirs();
		
		// Outputting lattice elements
		TestCommon.saveLattice(scenario.getLattice(), "temp/ncellstest/lattice.xml");
		TestCommon.saveLattice(oscenario.getLattice(), "temp/ncellstest/elattice.xml");
		TestCommon.saveSequence(sequence, "temp/ncellstest/seq.xml");
		
		// Creating a probe		
		EnvelopeProbe probe = TestCommon.setupProbeViaJavaCalls();					
		scenario.setProbe(probe);			
		
		// Setting up synchronization mode
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);					
		scenario.resync();
			
		// Prints transfer matrices
		for (IComponent el : ((ElementSeq)((Sector)scenario.getLattice().getElementList().get(0))).getElementList() )
		{		
			//((IElement)el).transferMap(probe, el.getLength()).getFirstOrder().print();			
			if (el instanceof xal.model.elem.IdealRfGap) {
				xal.model.elem.IdealRfGap gap = (xal.model.elem.IdealRfGap)el;
				System.out.printf("gap phase=%f E0TL=%E\n", gap.getPhase()*180./Math.PI, gap.getETL());
			}
		}
		
		
		// Running simulation
		scenario.run();
				
		// Prints transfer matrices
		for (IComponent el : ((ElementSeq)((Sector)scenario.getLattice().getElementList().get(0))).getElementList() )
		{		
			//((IElement)el).transferMap(probe, el.getLength()).getFirstOrder().print();			
			if (el instanceof xal.model.elem.IdealRfGap) {
				xal.model.elem.IdealRfGap gap = (xal.model.elem.IdealRfGap)el;
				double phase = gap.getPhase()*180./Math.PI;
				while (phase>180.) phase -=180;
				while (phase<-180.) phase += 180;
				System.out.printf("gap phase=%f E0TL=%E\n", phase, gap.getETL());
			}
		}
		
		// Getting results
		Twiss[] t = probe.getCovariance().computeTwiss();
		
		double[] betaa = new double[3];
		for (int i=0; i<3; i++) betaa[i] = t[i].getBeta();
		betaa[2]/=probe.getGamma()*probe.getGamma();
		
		double[] sigma = new double[3];
		for (int i=0; i<3; i++)
			sigma[i] = Math.sqrt(betaa[i]*t[i].getEmittance()/probe.getBeta()/probe.getGamma());		
		System.out.printf("%E %E %E %E ",probe.getPosition(), sigma[0], sigma[1], sigma[2]);
		System.out.printf("%E %E %E\n", betaa[0], betaa[1], betaa[2]);
		
		/* ELS output: 7.000000E-02 1.060867E-03 9.629023E-04 1.920023E-03 3.918513E-01 3.239030E-01 9.445394E-01 */
	}
}
