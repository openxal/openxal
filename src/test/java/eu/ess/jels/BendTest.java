package eu.ess.jels;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import eu.ess.jels.smf.impl.Bend;
import xal.model.IComponent;
import xal.model.IElement;
import xal.model.ModelException;
import xal.model.Sector;
import xal.model.elem.ElementSeq;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.Scenario;
import xal.sim.scenario.ScenarioGenerator2;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.IConstants;
import xal.tools.beam.Twiss;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlDataAdaptor.ParseException;
import xal.tools.xml.XmlDataAdaptor.ResourceNotFoundException;

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
		double rho = 9375.67*1e-3; // absolute curvature radius (in m)
		double N = 0.; // field Index
		int HV = 0;  // 0 - horizontal, 1 - vertical 
		double G = 50;
		double entrK1 = 0.45;
		double entrK2 = 2.80;
		double exitK1 = 0.45;
		double exitK2 = 2.80;
		
		// calculations
		double alpha = alpha_deg * Math.PI/180.0;		
		double len = Math.abs(rho*alpha);
		double quadComp = N / (rho*rho);
		
		// following are used to calculate field
		EnvelopeProbe probe = TestCommon.setupProbeViaJavaCalls();
		probe.initialize();
	    double c  = IConstants.LightSpeed;	      
	    double e = probe.getSpeciesCharge();
	    double Er = probe.getSpeciesRestEnergy();
	    double gamma = probe.getGamma();
	    double b  = probe.getBeta();
	    double B0 = b*gamma*Er/(e*c*rho)*Math.signum(alpha);
		
	    
		Bend bend = new Bend("b", HV);
		bend.setPosition(len*0.5); //always position on center!
		bend.setLength(len); // both paths are used in calculation
		bend.getMagBucket().setPathLength(len);
		
		bend.getMagBucket().setDipoleEntrRotAngle(-entry_angle_deg);
		bend.getMagBucket().setBendAngle(alpha_deg);
		bend.getMagBucket().setDipoleExitRotAngle(-exit_angle_deg);		
		bend.setDfltField(B0);		
		bend.getMagBucket().setDipoleQuadComponent(quadComp);
		
		bend.setGap(G);
		bend.setEntrK1(entrK1);
		bend.setEntrK2(entrK2);
		bend.setExitK1(exitK1);
		bend.setExitK2(exitK2);
		
		sequence.addNode(bend);
		sequence.setLength(len);
				
		
		XmlDataAdaptor a = XmlDataAdaptor.newDocumentAdaptor(sequence, null);
			try {
				a.writeTo(new File("blah.xml"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		// Generates lattice from SMF accelerator
		Scenario oscenario = Scenario.newScenarioFor(sequence);
		Scenario scenario = new ScenarioGenerator2(sequence).generateScenario();
		//Scenario oscenario = Scenario.newAndImprovedScenarioFor(sequence);

		// Ensure directory
		new File("temp/bendtest").mkdirs();
		
		// Outputting lattice elements
		TestCommon.saveLattice(scenario.getLattice(), "temp/bendtest/lattice.xml");
		TestCommon.saveLattice(oscenario.getLattice(), "temp/bendtest/elattice.xml");
		
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
}
