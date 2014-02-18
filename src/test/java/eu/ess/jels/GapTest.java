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
import xal.smf.AcceleratorSeq;
import xal.smf.impl.RfGap;
import eu.ess.jels.model.elem.els.ElsElementMapping;
import eu.ess.jels.model.elem.jels.JElsElementMapping;
import eu.ess.jels.smf.impl.ESSRfCavity;

@RunWith(Parameterized.class)
public class GapTest extends TestCommon {
	
	@Parameters
	public static Collection<Object[]> probes() {
		//double energy = 2.5e6, frequency = 4.025e8, current = 0;
		double energy = 2.5e9, frequency = 4.025e8, current = 0;
		//double energy = 0.2e6, frequency = 4.025e8, current = 0;
		return Arrays.asList(new Object[][]{
					{setupOpenXALProbe(energy, frequency, current), JElsElementMapping.getInstance()},
				//	{setupElsProbe(energy, frequency, current), ElsElementMapping.getInstance()},
				//	{setupOpenXALProbe(energy, frequency, current), DefaultElementMapping.getInstance()},
				});
	}
	
	public GapTest(Probe probe, ElementMapping elementMapping) {
		super(probe, elementMapping);
	}

	@Test
	public void doGapTestWithoutTTF() throws InstantiationException, ModelException {
		System.out.println("GAP");
		
		// GAP 78019.7 -80 14.5 0 0 0 0 0 0 0
		AcceleratorSeq sequence = gap(4.025e8, 78019.7, -80, 14.5, 0, 0, 0, 0, 0, 0, 0);
		
		//AcceleratorSeq sequence = gap(4.025e8, 78019.7, -80, 14.5, 0, 0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
		
		run(sequence);
		
		//printResults();
		
		if (initialEnergy == 2.5e6) {
			checkELSResults(0.000000E+00, new double[] {8.001089E-04, 1.018977E-03, 1.753257E-03},
					new double [] {2.442000E-01, 3.974000E-01, 8.628735E-01});
			
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
		if (initialEnergy == 2.5e9) {
			checkTWTransferMatrix(new double[][]{
					{+9.999979e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+7.883365e-06, +9.999979e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +9.999979e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +7.883365e-06, +9.999979e-01, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -2.117148e-04, +9.999957e-01}, 
 
			});
			
			checkTWResults( 3.664423648, new double[][] { 
					{+1.453284e-14, +1.049209e-14, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+1.049209e-14, +2.512769e-13, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +2.357118e-14, +1.925927e-14, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.925927e-14, +1.649900e-13, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.230104e-15, +4.272341e-14}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.272341e-14, +1.599494e-12}, 
			});		
		}
		if (initialEnergy == 0.2e6)
		{
			checkTWTransferMatrix(new double[][]{
					{+9.836180e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+3.672477e+01, +9.836180e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +9.836180e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +3.672477e+01, +9.836180e-01, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -7.348193e+01, +9.677556e-01},  
			});
			
			checkTWResults(1.000227592, new double[][] { 
					{+2.400604e-12, +9.136307e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+9.136307e-11, +3.517385e-09, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +3.893599e-12, +1.485544e-10, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.485544e-10, +5.692520e-09, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.198528e-11, -8.736424e-10}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -8.736424e-10, +6.369725e-08}, 

			});			
		}
	}
	
	
	@Test
	public void doGapTestWithTTF() throws InstantiationException, ModelException {		
		System.out.println("GAP with TTF");

		// GAP 78019.7 -35 14.5 0 0.0805777 0.772147 -0.386355 -0.142834 0 0 
		AcceleratorSeq sequence = gap(4.025e8, 78019.7, -35, 14.5, 0, 0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
		
		//AcceleratorSeq sequence = gap(4.025e8, 78019.7, -80, 14.5, 0, 0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);		
		
		run(sequence);
		
		//printResults();		

		if (initialEnergy == 2.5e6) {
		
			checkELSResults(0.000000E+00, new double[] {8.001089E-04, 1.018977E-03, 1.753257E-03},
					new double [] {2.442000E-01, 3.974000E-01, 8.628735E-01});
			
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
		if (initialEnergy == 2.5e9) {
			checkTWTransferMatrix(new double[][] {
					{+9.999862e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+6.339641e-06, +9.999861e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +9.999862e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +6.339641e-06, +9.999861e-01, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.702604e-04, +9.999723e-01}, 
			});
			
			checkTWResults(  3.664503260, new double[][] {
					{+1.453250e-14, +1.049182e-14, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+1.049182e-14, +2.512710e-13, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +2.357062e-14, +1.925878e-14, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.925878e-14, +1.649861e-13, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.230104e-15, +4.272263e-14}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.272263e-14, +1.599423e-12}, 	
					});
		}
		if (initialEnergy == 0.2e6)		
		{
			checkTWTransferMatrix(new double[][]{
					{+1.506411e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{-3.068165e+01, +7.955257e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.506411e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, -3.068165e+01, +7.955257e-01, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +6.138550e+01, +1.198389e+00}, 
			});
			
			checkTWResults(  1.000148426, new double[][] { 
					{+5.630599e-12, -1.125339e-10, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{-1.125339e-10, +2.275448e-09, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +9.132409e-12, -1.820628e-10, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, -1.820628e-10, +3.645712e-09, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.198528e-11, +7.444642e-10}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +7.444642e-10, +4.626513e-08}, 
 
			});		
		}
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
				cavity.getRfField().setTTFCoefs(new double[] {});
				cavity.getRfField().setTTF_endCoefs(new double[] {});
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
