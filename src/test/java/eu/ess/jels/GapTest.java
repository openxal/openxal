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
import xal.smf.impl.RfGap;
import eu.ess.jels.smf.impl.ESSRfCavity;

@RunWith(Parameterized.class)
public class GapTest extends TestCommon {
	
	@Parameters
	public static Collection<Object[]> probes() {
		double energy = 2.5e6, frequency = 4.025e8, current = 0;
		return Arrays.asList(new Object[][]{
				/*	{setupOpenXALProbe(energy, frequency, current), DefaultElementMapping.getInstance()},
					{setupElsProbe(energy, frequency, current), ElsElementMapping.getInstance()},*/
					{setupOpenXALProbe(energy, frequency, current), TWElementMapping.getInstance()}
				});
	}
	
	public GapTest(Probe probe, ElementMapping elementMapping) {
		super(probe, elementMapping);
	}

	@Test
	public void doGapTest() throws InstantiationException, ModelException {
		System.out.println("GAP");
		probe.reset();
		
		// GAP 78019.7 -80 14.5 0 0 0 0 0 0 0
		AcceleratorSeq sequence = gap(4.025e8, 78019.7, -80, 14.5, 0, 0, 0, 0, 0, 0, 0);
		
		//AcceleratorSeq sequence = gap(4.025e8, 78019.7, -80, 14.5, 0, 0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
		
		System.out.println("W0: "+probe.getKineticEnergy());
		
		run(sequence);
		
		printResults();
		checkELSResults(0.000000E+00, new double[] {8.001089E-04, 1.018977E-03, 1.753257E-03},
				new double [] {2.442000E-01, 3.974000E-01, 8.628735E-01});
		
		System.out.println("W: "+probe.getKineticEnergy());
		// -90 0.000000E+00 8.001089E-04 1.018977E-03 1.753257E-03 2.442000E-01 3.974000E-01 8.628735E-01
		// -80 0.000000E+00 7.992067E-04 1.017828E-03 1.753257E-03 2.442000E-01 3.974000E-01 8.648228E-01
		// -45 0.000000E+00 7.964664E-04 1.014338E-03 1.753257E-03 2.442000E-01 3.974000E-01 8.707840E-01
		// -10 0.000000E+00 7.950583E-04 1.012545E-03 1.753258E-03 2.442000E-01 3.974000E-01 8.738712E-01
		// 0 0.000000E+00 7.949815E-04 1.012447E-03 1.753257E-03 2.442000E-01 3.974000E-01 8.740398E-01

		checkTWTransferMatrix(new double[][]{
				{+9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+8.813454e-01, +9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +8.813454e-01, +9.986471e-01, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.772122e+00, +9.972978e-01}, 
		});
		
		checkTWResults(1.002678848, new double[][] 
				{{+6.994725e-13, +1.122296e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+1.122296e-12, +1.353021e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.134492e-12, +1.928183e-12, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.928183e-12, +1.046080e-11, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.371330e-12, -3.918053e-12}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.918053e-12, +9.047444e-12}, 
				});		
	}
	
	
	@Test
	public void doGapTestWithTTF() throws InstantiationException, ModelException {		
		System.out.println("GAP with TTF");
		probe.reset();
		// GAP 78019.7 -35 14.5 0 0.0805777 0.772147 -0.386355 -0.142834 0 0 
		AcceleratorSeq sequence = gap(4.025e8, 78019.7, -35, 14.5, 0, 0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
		
		//AcceleratorSeq sequence = gap(4.025e8, 78019.7, -80, 14.5, 0, 0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
		
		System.out.println("W0: "+probe.getKineticEnergy());
		
		run(sequence);
		
		printResults();
		checkELSResults(0.000000E+00, new double[] {8.001089E-04, 1.018977E-03, 1.753257E-03},
				new double [] {2.442000E-01, 3.974000E-01, 8.628735E-01});
		
		System.out.println("W: "+probe.getKineticEnergy());

		checkTWTransferMatrix(new double[][] {
				{+9.976056e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+4.783932e-01, +9.904352e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +9.976056e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +4.783932e-01, +9.904352e-01, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -9.619538e-01, +9.880637e-01}, 
		});
		
		checkTWResults( 1.002729084, new double[][] {
				{+6.980143e-13, +8.350357e-13, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+8.350357e-13, +1.253634e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +1.132127e-12, +1.461270e-12, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +1.461270e-12, +8.952107e-12, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.371330e-12, -1.205749e-12}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.205749e-12, +4.842405e-12}, 

				});		
	}
	
	/**
	 * 
	 * @param frequency
	 * @param E0TL
	 * @param Phis  RF phase (deg) absolute or relative
	 * @param R aperture
	 * @param p 0: relative phase, 1: absolute phase
	 * @param betas  particle reduced velocity
	 * @param Ts transit time factor
	 * @param kTs
	 * @param k2Ts
	 * @param kS
	 * @param k2S
	 * @return
	 */
	public AcceleratorSeq gap(double frequency, double E0TL, double Phis, double R, double p, double betas, double Ts, double kTs, double k2Ts, double kS, double k2S)
	{
		AcceleratorSeq sequence = new AcceleratorSeq("GapTest");
		
		// setup		
			RfGap gap = new RfGap("g");
			gap.setFirstGap(true); // this uses only phase for calculations
			gap.getRfGap().setEndCell(0);
			gap.setLength(0.0); // used only for positioning
			
			// following are used to calculate E0TL
			double length = 1.0; // length is not given in TraceWin, but is used only as a factor in E0TL in OpenXal
			gap.getRfGap().setLength(length); 		
			gap.getRfGap().setAmpFactor(1.0);
			/*gap.getRfGap().setGapOffset(dblVal)*/	
			
			ESSRfCavity cavity = new ESSRfCavity("c");
			cavity.addNode(gap);
			cavity.getRfField().setPhase(Phis);		
			cavity.getRfField().setAmplitude(E0TL * 1e-6 / length);
			cavity.getRfField().setFrequency(frequency * 1e-6);		
			/*cavity.getRfField().setStructureMode(dblVal);*/
			gap.getRfGap().setTTF(1.0);		
			
			// TTF		
			if (betas == 0.0) {
				cavity.getRfField().setTTFCoefs(new double[] {0.0});
				cavity.getRfField().setTTF_endCoefs(new double[] {0.0});
			} else {				
				cavity.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
				cavity.getRfField().setTTF_endCoefs(new double[] {betas, Ts, kTs, k2Ts});
				cavity.getRfField().setSTFCoefs(new double[] {betas, 0., kS, k2S});
				cavity.getRfField().setSTF_endCoefs(new double[] {betas, 0., kS, k2S});
			}		
			
			sequence.addNode(cavity);
			sequence.setLength(0.0);
			
			return sequence;
	}
	
}
