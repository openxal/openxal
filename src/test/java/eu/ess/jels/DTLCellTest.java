package eu.ess.jels;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.sim.scenario.ElementMapping;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.RfGap;
import eu.ess.jels.smf.impl.ESSRfCavity;

@RunWith(Parameterized.class)
public class DTLCellTest extends TestCommon {

	public DTLCellTest(Probe probe, ElementMapping elementMapping) {
		super(probe, elementMapping);
	}

	@Test
	public void doDTLCellTest() throws InstantiationException, ModelException {		
		AcceleratorSeq sequence = new AcceleratorSeq("DTLCellTest");

		// DTL_CEL 68.534 22.5 22.5 0.00864202 0 46.964 148174 -90 10 0 0 0.0805777 0.772147 -0.386355 -0.142834
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
		quad1.setDfltField(B1 * Math.signum(probe.getSpeciesCharge()));
		quad1.getMagBucket().setPolarity(1);
		
		Quadrupole quad2 = new Quadrupole("quad2") { // there's no setter for type (you need to extend class)
			{_type="Q"; }
		};
		quad2.setPosition(L-Lq2*0.5); //always position on center!
		quad2.setLength(Lq2); // effLength below is actually the only one read 
		quad2.getMagBucket().setEffLength(Lq2);
		quad2.setDfltField(B2 * Math.signum(probe.getSpeciesCharge()));
		quad2.getMagBucket().setPolarity(1);
		
		
		// GAP
		RfGap gap = new RfGap("g");
		gap.setFirstGap(true); // this uses only phase for calculations
		gap.getRfGap().setEndCell(0);
		gap.setLength(2*g); // used only for positioning
		gap.setPosition((L+Lq1-Lq2)/2+g);
		// following are used to calculate E0TL
		double length = 2*g; // length is not given in TraceWin, but is used only as a factor in E0TL in OpenXal
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
			dtlTank.getRfField().setTTFCoefs(new double[] {0.0});
		} else {
			gap.getRfGap().setTTF(1.0);		
			dtlTank.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});			
			dtlTank.getRfField().setSTFCoefs(new double[] {betas, 0., kS, k2S});			
		}		
		
		sequence.addNode(dtlTank);
		sequence.setLength(L);
				
		run(sequence);
		
		printResults(6.853400E-02, new double[] { 9.128969E-04, 1.266179E-03, 1.698689E-03},
				new double [] {2.901633E-01, 5.600682E-01, 7.393248E-01});
		
		
		checkResults(1.003197291, new double[][] 
				{{+7.479994e-13, -1.023415e-12, -2.946095e-20, -1.080781e-18, +0.000000e+00, +0.000000e+00}, 
					{-1.023415e-12, +1.058787e-11, -1.660521e-18, -8.816041e-18, +0.000000e+00, +0.000000e+00}, 
					{-2.946095e-20, -1.660521e-18, +1.414917e-12, +8.722957e-12, +0.000000e+00, +0.000000e+00}, 
					{-1.080781e-18, -8.816041e-18, +8.722957e-12, +5.860168e-11, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.796100e-12, -5.769466e-12}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -5.769466e-12, +1.644345e-11} 
				});
	}
}
