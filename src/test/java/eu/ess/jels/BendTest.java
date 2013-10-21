package eu.ess.jels;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.sim.scenario.ElementMapping;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.qualify.MagnetType;
import xal.tools.beam.IConstants;
import eu.ess.jels.smf.impl.Bend;

@RunWith(Parameterized.class)
public class BendTest extends TestCommon {
	public BendTest(Probe probe, ElementMapping elementMapping) {
		super(probe, elementMapping);
	}
	
	@Test
	public void doBendTest() throws InstantiationException, ModelException {		
		AcceleratorSeq sequence = new AcceleratorSeq("BendTest");
		
		/*
		EDGE -5.5 9375.67 50 0.45 2.8 50 1; this is a magnet length of 1.8 m 
		BEND -11 9375.67 0 50 1
		EDGE -5.5 9375.67 50 0.45 2.8 50 1
		 */
		
		// input from TraceWin
		double entry_angle_deg = -5.5;
		double exit_angle_deg = -5.5;
		double alpha_deg = -11; // angle in degrees
		double rho = 9375.67*1e-3; // absolute curvature radius (in m)
		double N = 0.; // field Index
		int HV = 1;  // 0 - horizontal, 1 - vertical 
		double G = 50 * 1e-3;
		double entrK1 = 0.45;
		double entrK2 = 2.80;
		double exitK1 = 0.45;
		double exitK2 = 2.80;
		
		// calculations
		double alpha = alpha_deg * Math.PI/180.0;		
		double len = Math.abs(rho*alpha);
		double quadComp = N / (rho*rho);
		
		// following are used to calculate field		
	    double c  = IConstants.LightSpeed;	      
	    double e = probe.getSpeciesCharge();
	    double Er = probe.getSpeciesRestEnergy();
	    double gamma = probe.getGamma();
	    double b  = probe.getBeta();
	    
	    double k = b*gamma*Er/(e*c); // = -0.22862458629665997
	    double B0 = k/rho*Math.signum(alpha);
	    //double B0 = b*gamma*Er/(e*c*rho)*Math.signum(alpha);
			    
		Bend bend = new Bend("b", HV == 0 ? MagnetType.HORIZONTAL : MagnetType.VERTICAL);
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
							
		run(sequence);

		printResults(1.799999E+00, new double[] {6.471216E-03, 5.453634E-03, 5.404960E-03},
				new double [] { 1.458045E+01, 1.039017E+01, 7.485008E+00}); // when halfMag=true
		//printResults(1.799999E+00, new double[] {6.471216E-03, 5.453634E-03, 5.385990E-03},				
		//		new double [] {1.458045E+01, 1.039017E+01, 7.432561E+00});// when halfMag = false
		// converges to
		// 1.799999E+00 6.471216E-03 5.453634E-03 5.411296E-03 1.458045E+01 1.039017E+01 7.502568E+00
		
		checkResults(1.003197291, new double[][] {
				{+3.822288e-11, +2.081295e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+2.081295e-11, +1.151277e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +2.730380e-11, +1.338170e-11, +9.152883e-13, -9.096619e-13}, 
				{+0.000000e+00, +0.000000e+00, +1.338170e-11, +6.868014e-12, -7.836318e-13, -9.982870e-13}, 
				{+0.000000e+00, +0.000000e+00, +9.152883e-13, -7.836318e-13, +2.675748e-11, +1.126873e-11}, 
				{+0.000000e+00, +0.000000e+00, -9.096619e-13, -9.982870e-13, +1.126873e-11, +5.280827e-12} 
		});
		
	}
}
