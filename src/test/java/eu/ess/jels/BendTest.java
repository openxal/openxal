package eu.ess.jels;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import xal.model.IComponent;
import xal.model.IElement;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.Sector;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.elem.ElementSeq;
import xal.model.probe.EnvelopeProbe;
import xal.model.xml.LatticeXmlWriter;
import xal.sim.scenario.Scenario;
import xal.sim.scenario.ScenarioGenerator2;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Bend;
import xal.tools.beam.IConstants;
import xal.tools.beam.Twiss;

@RunWith(JUnit4.class)
public class BendTest {
	@Test
	public void doBendTest() throws InstantiationException, ModelException {
		System.out.println("Running\n");
		AcceleratorSeq sequence = new AcceleratorSeq("BendTest");
		
		// input from TraceWin
		double entry_angle_deg = -5.5;
		double exit_angle_deg = -5.5;
		double alpha_deg = -11; // angle in degrees
		double rho = 9375.67*1e-3; // curvature radius (in m)
		double N = 0.; // field Index
		final int HV = 0;  // 0 - horizontal, 1 - vertical 
		/* G,K1,K2 - gap, fringe field factors are supported in the model but not SMF (use G*1.e-3)*/
		
		// calculations
		double alpha = alpha_deg * Math.PI/180.0;		
		double len = Math.abs(rho*alpha);
		double quadComp = N / (rho*rho);
		
		// following are used to calculate field
		EnvelopeProbe probe = setupProbeViaJavaCalls();
		probe.initialize();
	    double c  = IConstants.LightSpeed;	      
	    double e = probe.getSpeciesCharge();
	    double Er = probe.getSpeciesRestEnergy();
	    double gamma = probe.getGamma();
	    double b  = probe.getBeta();
	    double B0 = b*gamma*Er/(e*c*rho)*Math.signum(alpha);
		
	    
		Bend bend = new Bend("b") {
			@Override
			public int getOrientation() {
				if (HV == 0) return HORIZONTAL;  
				else return VERTICAL; // currently impossible to put it into a file
				
			}			
		};
		bend.setPosition(len*0.5); //always position on center!
		bend.setLength(len); // both paths are used in calculation
		bend.getMagBucket().setPathLength(len);
		
		bend.getMagBucket().setDipoleEntrRotAngle(-entry_angle_deg);
		bend.getMagBucket().setBendAngle(alpha_deg);
		bend.getMagBucket().setDipoleExitRotAngle(-exit_angle_deg);		
		bend.setDfltField(B0);		
		bend.getMagBucket().setDipoleQuadComponent(quadComp);
		 
		
		sequence.addNode(bend);
		sequence.setLength(len);
				
		// Generates lattice from SMF accelerator
		Scenario oscenario = Scenario.newScenarioFor(sequence);
		Scenario scenario = new ScenarioGenerator2(sequence).generateScenario();
		//Scenario oscenario = Scenario.newAndImprovedScenarioFor(sequence);

		// Ensure directory
		new File("temp/bendtest").mkdirs();
		
		// Outputting lattice elements
		saveLattice(scenario.getLattice(), "temp/bendtest/lattice.xml");
		saveLattice(oscenario.getLattice(), "temp/bendtest/elattice.xml");
		
		// Creating a probe						
		scenario.setProbe(probe);			
		
		// Prints transfer matrices
		for (IComponent comp : ((ElementSeq)((Sector)scenario.getLattice().getElementList().get(0)).getChild(1)).getElementList()) {
			IElement el = (IElement)comp;
			el.transferMap(probe, el.getLength()).getFirstOrder().print();
		}
		
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
