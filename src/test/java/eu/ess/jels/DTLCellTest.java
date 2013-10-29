package eu.ess.jels;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.sim.scenario.DefaultElementMapping;
import xal.sim.scenario.ElementMapping;
import xal.sim.scenario.ElsElementMapping;
import xal.sim.scenario.TWElementMapping;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.RfGap;
import eu.ess.jels.smf.impl.ESSRfCavity;

@RunWith(Parameterized.class)
public class DTLCellTest extends TestCommon {
	@Parameters
	public static Collection<Object[]> probes() {
		double energy = 2.5e6, frequency = 4.025e8, current = 0;
		return Arrays.asList(new Object[][]{
					{setupOpenXALProbe(energy, frequency, current), DefaultElementMapping.getInstance()},
					{setupElsProbe(energy, frequency, current), ElsElementMapping.getInstance()},
					{setupOpenXALProbe(energy, frequency, current), TWElementMapping.getInstance()}
				});
	}
	
	public DTLCellTest(Probe probe, ElementMapping elementMapping) {
		super(probe, elementMapping);
	}

	@Test
	public void doDTLCellTest() throws InstantiationException, ModelException {
		System.out.println("DTL Cell test");
		probe.reset();
		// DTL_CEL 68.534 22.5 22.5 0.00864202 0 46.964 148174 -35 10 0 0.0805777 0.772147 -0.386355 -0.142834
		AcceleratorSeq sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 46.964,
				148174, -35, 10, 0,
				0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
				
		run(sequence);
		
		printResults();
		checkELSResults(6.853400E-02, new double[] { 9.128969E-04, 1.266179E-03, 1.698689E-03},
				new double [] {2.901633E-01, 5.600682E-01, 7.393248E-01});
		
		checkTWTransferMatrix(new double[][]{
				{+9.749908e-01, +6.610770e-02, -1.552884e-08, -8.222842e-10, +0.000000e+00, +0.000000e+00}, 
				{-3.621475e+00, +7.571430e-01, -1.384981e-06, -7.827199e-08, +0.000000e+00, +0.000000e+00}, 
				{-1.552884e-08, -8.222842e-10, +1.077946e+00, +7.155936e-02, +0.000000e+00, +0.000000e+00}, 
				{-1.384981e-06, -7.827199e-08, +5.560820e+00, +1.276079e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.388414e-01, +6.531479e-02}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.794280e+00, +9.164720e-01}, 
		});
		
		checkTWResults( 1.002787652, new double[][] {
				{+7.849968e-13, -1.616913e-12, -3.357130e-20, -1.224097e-18, +0.000000e+00, +0.000000e+00}, 
				{-1.616913e-12, +1.337361e-11, -1.886005e-18, -9.191956e-18, +0.000000e+00, +0.000000e+00}, 
				{-3.357130e-20, -1.886005e-18, +1.505982e-12, +9.194360e-12, +0.000000e+00, +0.000000e+00}, 
				{-1.224097e-18, -9.191956e-18, +9.194360e-12, +6.133377e-11, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.249099e-12, -3.800717e-12}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.800717e-12, +8.926809e-12}, 
				});
	}

	@Test
	public void doDTLCellTestOnlyRf() throws InstantiationException, ModelException {
		System.out.println("DTL Cell test only RF");
		probe.reset();		
		// DTL_CEL 68.534 22.5 22.5 0.00864202 0 0 148174 -35 10 0 0.0805777 0.772147 -0.386355 -0.142834
		AcceleratorSeq sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 0,
				148174, -35, 10, 0,	0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
		
		run(sequence);
		
		printResults();
		checkELSResults(6.853400E-02, new double[] { 9.128969E-04, 1.266179E-03, 1.698689E-03},
				new double [] {2.901633E-01, 5.600682E-01, 7.393248E-01});
		
		checkTWTransferMatrix(new double[][]{
				{+1.026033e+00, +6.881175e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+8.922687e-01, +1.012651e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +1.026033e+00, +6.881175e-02, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +8.922687e-01, +1.012651e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.388414e-01, +6.531479e-02}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.794280e+00, +9.164720e-01}, 

		});
		
		checkTWResults(1.002787652, new double[][] {
				{+8.672837e-13, +2.044325e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+2.044325e-12, +1.390906e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +1.366517e-12, +2.619081e-12, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +2.619081e-12, +1.075062e-11, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.249099e-12, -3.800717e-12}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.800717e-12, +8.926809e-12}, 



				});
	}
	

	@Test
	public void doDTLCellTestOnlyRfNoTTF() throws InstantiationException, ModelException {
		System.out.println("DTL Cell test Only RF no TTF");
		probe.reset();
		// DTL_CEL 68.534 22.5 22.5 0.00864202 0 0 148174 -35 10 0 0 0 0 0 0 
		AcceleratorSeq sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 0,
				148174, -35, 10, 0,
				0, 0, 0, 0, 0, 0);
				
		run(sequence);
		
		printResults();
		checkELSResults(6.853400E-02, new double[] { 9.128969E-04, 1.266179E-03, 1.698689E-03},
				new double [] {2.901633E-01, 5.600682E-01, 7.393248E-01});
		
		checkTWTransferMatrix(new double[][]{
				{+1.020163e+00, +6.881804e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+9.344715e-01, +1.020147e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +1.020163e+00, +6.881804e-02, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +9.344715e-01, +1.020147e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.359490e-01, +6.517928e-02}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.879158e+00, +9.125075e-01}, 

		});
		
		checkTWResults( 1.002793768, new double[][] {
				{+8.584677e-13, +2.079518e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+2.079518e-12, +1.419830e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +1.352122e-12, +2.670547e-12, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +2.670547e-12, +1.105212e-11, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.229414e-12, -4.077278e-12}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -4.077278e-12, +9.645998e-12}, 
				});
	}
	/**
	 * 
	 * @param L
	 * @param Lq1
	 * @param Lq2
	 * @param g
	 * @param B1
	 * @param B2
	 * @param E0TL Effective gap voltage
	 * @param Phis RF phase (deg) absolute or relative
	 * @param R aperture
	 * @param p 0: relative phase, 1: absolute phase
	 * @param betas particle reduced velocity
	 * @param Ts transit time factor
	 * @param kTs
	 * @param k2Ts
	 * @param kS
	 * @param k2S
	 * @return
	 */
	public AcceleratorSeq dtlcell(double frequency, double L, double Lq1, double Lq2, double g, double B1, double B2, 
			double E0TL, double Phis, double R,	double p, 
			double betas, double Ts, double kTs, double k2Ts, double kS, double k2S)
	{
		AcceleratorSeq sequence = new AcceleratorSeq("DTLCellTest");
		
		// mm -> m
		L *= 1e-3;
		Lq1 *= 1e-3;
		Lq2 *= 1e-3;
		g *= 1e-3;
		
		// setup		
		// QUAD1,2
		Quadrupole quad1 = new Quadrupole("quad1") { // there's no setter for type (you need to extend class)
			{_type="Q"; }
		};
		quad1.setPosition(0.5*Lq1); //always position on center!
		quad1.setLength(Lq1); // effLength below is actually the only one read 
		quad1.getMagBucket().setEffLength(Lq1);
		quad1.setDfltField(0);//B1 * Math.signum(probe.getSpeciesCharge()));
		quad1.getMagBucket().setPolarity(1);
		
		Quadrupole quad2 = new Quadrupole("quad2") { // there's no setter for type (you need to extend class)
			{_type="Q"; }
		};
		quad2.setPosition(L-0.5*Lq2); //always position on center!
		quad2.setLength(Lq2); // effLength below is actually the only one read 
		quad2.getMagBucket().setEffLength(Lq2);
		quad2.setDfltField(B2 * Math.signum(probe.getSpeciesCharge()));
		quad2.getMagBucket().setPolarity(1);
		
		
		// GAP
		RfGap gap = new RfGap("g");
		gap.setFirstGap(true); // this uses only phase for calculations
		gap.getRfGap().setEndCell(0);
		gap.setLength(0.0); // used only for positioning
		gap.setPosition(0.5*L-g);
		// following are used to calculate E0TL
		double length = g; // length is not given in TraceWin, but is used only as a factor in E0TL in OpenXal
		gap.getRfGap().setLength(length); 		
		gap.getRfGap().setAmpFactor(1.0);
		gap.getRfGap().setTTF(1.0);		
		/*gap.getRfGap().setGapOffset(dblVal)*/		
		
		ESSRfCavity dtlTank = new ESSRfCavity("d"); // this could also be rfcavity, makes no difference
		dtlTank.addNode(quad1);
		dtlTank.addNode(gap);
		dtlTank.addNode(quad2);
		dtlTank.getRfField().setPhase(Phis);		
		dtlTank.getRfField().setAmplitude(E0TL * 1e-6 / length);
		dtlTank.getRfField().setFrequency(frequency * 1e-6);		
		/*cavity.getRfField().setStructureMode(dblVal);*/
				
		// TTF		
		if (betas == 0.0) {			
			dtlTank.getRfField().setTTFCoefs(new double[] {0.0});
		} else {
			dtlTank.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			dtlTank.getRfField().setTTF_endCoefs(new double[] {betas, Ts, kTs, k2Ts});
			dtlTank.getRfField().setSTFCoefs(new double[] {betas, 0., kS, k2S});
			dtlTank.getRfField().setSTF_endCoefs(new double[] {betas, 0., kS, k2S});
		}		
		
		sequence.addNode(dtlTank);
		sequence.setLength(L);
		
		return sequence;		
	}
	
}
