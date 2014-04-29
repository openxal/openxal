package se.lu.esss.ics.jels;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.sim.scenario.ElementMapping;
import xal.smf.AcceleratorSeq;
import xal.smf.attr.ApertureBucket;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.VDipoleCorr;

@RunWith(Parameterized.class)
public class SteererTest extends TestCommon {	
	public SteererTest(Probe probe, ElementMapping elementMapping) {
		super(probe, elementMapping);
	}

	@Test
	public void doQuadTest() throws InstantiationException, ModelException {
		probe.reset();
		System.out.println("QUAD 70 -16 15 0 0 0 0 0");
		//QUAD 70 -16 15 0 0 0 0 0		
		AcceleratorSeq sequence = quad_steerer(70., -16., 15., 0., 0., 0., 0., 0., 10, -20);
		
		run(sequence);		

		//printResults();
		if (initialEnergy == 3e6) {
			checkELSResults(7.000000E-02, new double[] {1.000780E-03, 9.345521E-04, 1.833376E-03},
					new double[] {3.820541E-01, 3.342766E-01, 9.435362E-01});
			
			checkTWTransferMatrix(new double[][]{
					{+1.160625e+00, +7.370925e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+4.708370e+00, +1.160625e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +8.475396e-01, +6.640505e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, -4.241796e+00, +8.475396e-01, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +6.955452e-02}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00}, 		
			});
			
			checkTWResults(1.003197291, new double[][] {
					{+1.001561e-12, +5.228219e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+5.228219e-12, +3.415331e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +8.733876e-13, -2.953358e-12, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, -2.953358e-12, +1.780296e-11, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.361266e-12, +2.249328e-12}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.249328e-12, +5.280827e-12} 
			
			
			});
		}
		if (initialEnergy == 2.5e9)
		{
			checkTWTransferMatrix(new double[][]{
					{+1.003555e+00, +7.008293e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+1.016284e-01, +1.003555e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +9.964493e-01, +6.991713e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, -1.013880e-01, +9.964493e-01, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +5.213029e-03}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00}, 		
			});
			
			checkTWResults(3.664409209, new double[][] {
					{+1.734644e-14, +2.979657e-14, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+2.979657e-14, +2.553578e-13, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +2.689426e-14, +2.809946e-14, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +2.809946e-14, +1.601717e-13, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.719022e-15, +5.106308e-14}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.106308e-14, +1.599526e-12}, 
			}, new double[] {4.4423e-3,126.998e-3,2.21983e-3,63.3862e-3,0,0});
			 /*      dx  = 4.4423 mm
					 dx' = 126.998 mrad
					 dy  = 2.21983 mm
					 dy' = 63.3862 mrad*/ 
		}		
	}
	
	/**
	 * 
	 * @param L length
	 * @param G field
	 * @param R aperture
	 * @param Phi skew angle 
	 * @param G3 sextupole gradient (T/m^2)
	 * @param G4 octupole gradient (T/m^2)
	 * @param G5 decapole gradient (T/m^2)
	 * @param G6 dodecapole gradient (T/m^2)
	 * @return
	 */
	public AcceleratorSeq quad_steerer(double L, double G, double R, double Phi, double G3, double G4, double G5, double G6, double Bx, double By)
	{
		AcceleratorSeq sequence = new AcceleratorSeq("QuadTest");
		Quadrupole quad = new Quadrupole("quad") { // there's no setter for type (you need to extend class)
			{_type="Q"; }
		};
		quad.setPosition(L*1e-3*0.5); //always position on center!
		quad.setLength(L*1e-3); // effLength below is actually the only one read 
		quad.getMagBucket().setEffLength(L*1e-3);
		quad.setDfltField(G*Math.signum(probe.getSpeciesCharge()));
		quad.getMagBucket().setPolarity(1);
		quad.getAper().setAperX(R*1e-3);
		quad.getAper().setAperY(R*1e-3);
		quad.getAper().setShape(ApertureBucket.iRectangle);
		sequence.addNode(quad);
		
		VDipoleCorr vcorr = new VDipoleCorr("VC");
		vcorr.setPosition(L*1e-3*0.5);
		vcorr.getMagBucket().setEffLength(L*1e-3);
		vcorr.setDfltField(Bx);
		sequence.addNode(vcorr);
		
		HDipoleCorr hcorr = new HDipoleCorr("HC");
		hcorr.setPosition(L*1e-3*0.5);
		hcorr.getMagBucket().setEffLength(L*1e-3);
		hcorr.setDfltField(-By);
		sequence.addNode(hcorr);
		
		sequence.setLength(L*1e-3);	
		return sequence;
	}
}
