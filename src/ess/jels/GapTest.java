package ess.jels;
import java.io.IOException;

import xal.model.IElement;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.Sector;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.xml.LatticeXmlWriter;
import xal.sim.scenario.ElsScenarioGenerator;
import xal.sim.scenario.OldScenarioMapping;
import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.RfCavity;
import xal.smf.impl.RfGap;
import xal.tools.beam.Twiss;


public class GapTest {


	public static void main(String[] args) throws InstantiationException, ModelException {
		System.out.println("Running\n");
		AcceleratorSeq sequence = new AcceleratorSeq("DriftTest");
		
		// input from TraceWin
		double frequency = 4.025e8; // this is global in TraceWin
		
		double E0TL = 78019.7 * 1e-6; // Effective gap voltage
		double Phis = -90;  // RF phase (deg) absolute or relative
		double R = 14.5; // aperture
		double p = 0; // 0: relative phase, 1: absolute phase
		
		double betas = 0; // particle reduced velocity
		double Ts = 0;  // transit time factor
		double kTs = 0;
		double k2Ts = 0;
		double kS = 0;
		double k2S = 0;
		
		// setup		
		RfGap gap = new RfGap("g");
		gap.setFirstGap(true); // this uses only phase for calculations
		gap.getRfGap().setEndCell(0);
		gap.setLength(0.0); // used only for positioning
		
		// following are used to calculate E0TL
		double length = 1.0;	// length is no given in OpenXal, but is used only as a factor in E0TL
		gap.getRfGap().setLength(length); 		
		gap.getRfGap().setAmpFactor(1.0);
		/*gap.getRfGap().setGapOffset(dblVal)*/		
		
		RfCavity cavity = new RfCavity("c");
		cavity.addNode(gap);
		cavity.getRfField().setPhase(Phis);		
		cavity.getRfField().setAmplitude(E0TL / length);
		cavity.getRfField().setFrequency(frequency * 1e-6);		
		/*cavity.getRfField().setStructureMode(dblVal);*/
		
		// TTF		
		if (betas == 0.0) {
			gap.getRfGap().setTTF(1.0);		
			cavity.getRfField().setTTFCoefs(new double[] {1.0});
		} else {
			// TTF calculation
			// we're equating T_openxal(bs/b)=a + b bs/b + c beta^2
			// and            T_ELS(beta)=Ts + kTs (K-1) + k2Ts (K-1)^2/2, where K=betas/beta
	        /*TTFCoefs="-.0815, 12.154, -41.431"
	        TTFPrimeCoefs=".2018, -1.8634, 5.6742"
	        STFCoefs=".7769, -3.3388, 6.0867"
	        STFPrimeCoefs="-.0643, 1.7099, -6.349"
	        TTF_endCoefs="-.0815, 12.154, -41.431"
	        TTFPrime_EndCoefs=".2018, -1.8634, 5.6742"
	        STF_endCoefs=".7769, -3.3388, 6.0867"
	        STFPrime_endCoefs="-.0643, 1.7099, -6.349"*/
			
			/*cavity.getRfField().setTTFPrimeCoefs(arrVal);
			cavity.getRfField().setTTFPrime_endCoefs(arrVal);
			
			cavity.getRfField().setTTF_endCoefs(arrVal);
			cavity.getRfField().setSTFPrimeCoefs(arrVal);
			cavity.getRfField().setSTFPrime_endCoefs(arrVal);
			cavity.getRfField().setSTFCoefs(arrVal);
			cavity.getRfField().setSTF_endCoefs(arrVal);		    
			*/
		}		
		
		sequence.addNode(cavity);
		sequence.setLength(0.0);
				
		// Generates lattice from SMF accelerator
		Scenario oscenario = Scenario.newScenarioFor(sequence);
		Scenario scenario = new ElsScenarioGenerator(sequence, new OldScenarioMapping()).getScenario();
		scenario.resync();
		//Scenario oscenario = Scenario.newAndImprovedScenarioFor(sequence);
				
		// Outputting lattice elements
		saveLattice(scenario.getLattice(), "lattice.xml");
		saveLattice(oscenario.getLattice(), "elattice.xml");
		
		// Creating a probe		
		EnvelopeProbe probe = setupProbeViaJavaCalls();					
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

	private static EnvelopeProbe setupProbeViaJavaCalls() {
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


	private static void saveLattice(Lattice lattice, String file) {				
		try {
			LatticeXmlWriter.writeXml(lattice, file);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
	}
}
