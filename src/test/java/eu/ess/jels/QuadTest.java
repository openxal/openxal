package eu.ess.jels;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.sim.scenario.ElementMapping;
import xal.smf.AcceleratorSeq;
import xal.smf.attr.ApertureBucket;
import xal.smf.impl.Quadrupole;

@RunWith(Parameterized.class)
public class QuadTest extends TestCommon {	
	public QuadTest(Probe probe, ElementMapping elementMapping) {
		super(probe, elementMapping);
	}

	@Test
	public void doQuadTest() throws InstantiationException, ModelException {		
		AcceleratorSeq sequence = new AcceleratorSeq("QuadTest");
		
		//QUAD 70 -16 15 0 0 0 0 0
		
		double L = 70e-3; // length
		double G = -16; // field
		double R = 15; // aperture
		double Phi = 0; // skew angle
		double G3 = 0; // sextupole gradient (T/m^2)
		double G4 = 0; // octupole gradient (T/m^2)
		double G5 = 0; // decapole gradient (T/m^2)
		double G6 = 0; // dodecapole gradient (T/m^2)
		
		Quadrupole quad = new Quadrupole("quad") { // there's no setter for type (you need to extend class)
			{_type="Q"; }
		};
		quad.setPosition(L*0.5); //always position on center!
		quad.setLength(L); // effLength below is actually the only one read 
		quad.getMagBucket().setEffLength(L);
		quad.setDfltField(G*Math.signum(probe.getSpeciesCharge()));
		quad.getMagBucket().setPolarity(1);
		quad.getAper().setAperX(15e-3);
		quad.getAper().setAperY(15e-3);
		quad.getAper().setShape(ApertureBucket.iRectangle);
		sequence.addNode(quad);
		sequence.setLength(70e-3);		
		
		run(sequence);		

		printResults();
		checkELSResults(7.000000E-02, new double[] {1.060867E-03, 9.629023E-04, 1.920023E-03},
				new double [] {3.918513E-01, 3.239030E-01, 9.445394E-01});
		
		checkTWResults(1.003197291, new double[][] {
				{+1.001561e-12, +5.228219e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+5.228219e-12, +3.415331e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +8.733876e-13, -2.953358e-12, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, -2.953358e-12, +1.780296e-11, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.361266e-12, +2.249328e-12}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.249328e-12, +5.280827e-12} 
		});
		
	}


}
