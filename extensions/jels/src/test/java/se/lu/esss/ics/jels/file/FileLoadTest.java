package se.lu.esss.ics.jels.file;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import se.lu.esss.ics.jels.JElsDemo;
import se.lu.esss.ics.jels.model.probe.ElsProbe;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.tools.beam.Twiss;

@RunWith(JUnit4.class)
public class FileLoadTest {
	@Test
	public void doTest() throws ModelException
	{
		AcceleratorSeq sequence = loadAcceleratorSequence("lebt");
				
		Scenario scenario = Scenario.newScenarioFor(sequence);
		
		EnvelopeProbe probe = setupProbeViaJavaCalls();
		scenario.setProbe(probe);									
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);					
		scenario.resync();
		scenario.run();
		
		
		Twiss[] t;
		t = probe.getCovariance().computeTwiss();
		
		
		double[] beta = new double[3];
		for (int i=0; i<3; i++) beta[i] = t[i].getBeta();
		if (!(probe instanceof ElsProbe)) {			
			beta[2]/=Math.pow(probe.getGamma(),2);
		}
		
		double[] sigma = new double[3];
		for (int i=0; i<3; i++)
			sigma[i] = Math.sqrt(beta[i]*t[i].getEmittance()/probe.getBeta()/probe.getGamma());		
		System.out.printf("%E %E %E %E ",probe.getPosition(), sigma[0], sigma[1], sigma[2]);
		System.out.printf("%E %E %E\n", beta[0], beta[1], beta[2]);
	}


	private static AcceleratorSeq loadAcceleratorSequence(String name) {
		/* Loading SMF model */				
		Accelerator accelerator = XMLDataManager.acceleratorWithUrlSpec(JElsDemo.class.getResource("main.xal").toString());
		if (accelerator == null)
		{			
			throw new Error("Accelerator is empty. Could not load the default accelerator.");
		} 			
		
		/* Selects a section */
		AcceleratorSeq sequence = accelerator.findSequence(name);			
		return sequence;		
	}
	
	public static EnvelopeProbe setupProbeViaJavaCalls() {
		// Envelope probe and tracker
		EnvelopeTracker envelopeTracker = new EnvelopeTracker();			
		envelopeTracker.setRfGapPhaseCalculation(false);
		envelopeTracker.setUseSpacecharge(false);
		envelopeTracker.setEmittanceGrowth(false);
		envelopeTracker.setStepSize(0.004);
		envelopeTracker.setProbeUpdatePolicy(Tracker.UPDATE_EXIT);
		
		EnvelopeProbe envelopeProbe = new EnvelopeProbe();
		envelopeProbe.setAlgorithm(envelopeTracker);
		envelopeProbe.setSpeciesCharge(-1);
		envelopeProbe.setSpeciesRestEnergy(9.3829431e8);
		envelopeProbe.setKineticEnergy(2.5e6);//energy
		envelopeProbe.setPosition(0.0);
		envelopeProbe.setTime(0.0);		
				
		/*
		number of particles = 1000
		beam current in A = 0
		Duty Cycle in %= 4
		normalized horizontal emittance in m*rad= 0.2098e-6
		normalized vertical emittance in m*rad = 0.2091e-6
		normalized longitudinal emittance in m*rad = 0.2851e-6
		kinetic energy in MeV = 3
		alfa x = -0.1763
		beta x in m/rad = 0.2442
		alfa y = -0.3247
		beta y in m/rad = 0.3974
		alfa z = -0.5283
		beta z in m/rad = 0.8684
		 */
		
		envelopeProbe.initFromTwiss(new Twiss[]{new Twiss(-0.1763,0.2442,0.2098e-6),
										  new Twiss(-0.3247,0.3974,0.2091e-6),
										  new Twiss(-0.5283,0.8684,0.2851e-6)});
		envelopeProbe.setBeamCurrent(0.0);
		envelopeProbe.setBunchFrequency(4.025e8);//frequency
		
		return envelopeProbe;
	}

}
