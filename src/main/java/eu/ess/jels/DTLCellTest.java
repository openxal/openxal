package eu.ess.jels;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import xal.model.IComponent;
import xal.model.IElement;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.Sector;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.elem.ElementSeq;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.xml.LatticeXmlWriter;
import xal.sim.scenario.DefaultElementMapping;
import xal.sim.scenario.Scenario;
import xal.sim.scenario.ScenarioGenerator2;
import xal.smf.AcceleratorSeq;
import xal.smf.attr.ApertureBucket;
import xal.smf.impl.DTLTank;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.RfCavity;
import xal.smf.impl.RfGap;
import xal.tools.beam.Twiss;


public class DTLCellTest {


	public static void main(String[] args) throws InstantiationException, ModelException {
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
		
		double betas = 0; //0.0805777; // particle reduced velocity
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
		
		DTLTank dtlTank = new DTLTank("d"); // this could also be rfcavity, makes no difference
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
		
		sequence.addNode(dtlTank);
		sequence.setLength(L);
				
		// Generates lattice from SMF accelerator
		Scenario oscenario = Scenario.newScenarioFor(sequence);
		Scenario scenario = new ScenarioGenerator2(sequence).generateScenario();
		scenario.resync();
		oscenario.resync();
		//Scenario oscenario = Scenario.newAndImprovedScenarioFor(sequence);
				
		// Outputting lattice elements
		saveLattice(scenario.getLattice(), "lattice.xml");
		saveLattice(oscenario.getLattice(), "elattice.xml");
		
		// Creating a probe		
		EnvelopeProbe probe = setupProbeViaJavaCalls();					
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
