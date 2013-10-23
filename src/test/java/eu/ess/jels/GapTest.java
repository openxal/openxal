package eu.ess.jels;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.sim.scenario.ElementMapping;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.RfGap;
import eu.ess.jels.smf.impl.ESSRfCavity;

@RunWith(Parameterized.class)
public class GapTest extends TestCommon {
	public GapTest(Probe probe, ElementMapping elementMapping) {
		super(probe, elementMapping);
	}

	@Test
	public void doGapTest() throws InstantiationException, ModelException {		
		AcceleratorSeq sequence = new AcceleratorSeq("GapTest");
		
		// GAP 78019.7 -80 14.5 0 0 0 0 0 0 0
		
		// input from TraceWin
		double frequency = 4.025e8; // this is global in TraceWin
		
		double E0TL = 78019.7 * 1e-6; // Effective gap voltage
		double Phis = -80;  // RF phase (deg) absolute or relative
		double R = 14.5; // aperture
		double p = 0; // 0: relative phase, 1: absolute phase
		
		double betas = 0; // particle reduced velocity
		double Ts = 0;  // transit time factor
		double kTs = 0;
		double k2Ts = 0;
		double kS = 0;
		double k2S = 0;
		
		/*double betas = 0.0805777; // particle reduced velocity
		double Ts = 0.772147;  // transit time factor
		double kTs = -0.386355;
		double k2Ts = -0.142834;*/
		
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
		cavity.getRfField().setAmplitude(E0TL / length);
		cavity.getRfField().setFrequency(frequency * 1e-6);		
		/*cavity.getRfField().setStructureMode(dblVal);*/
		
		// TTF		
		if (betas == 0.0) {
			gap.getRfGap().setTTF(1.0);		
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

		
		checkTWResults(1.003211730, new double[][] 
				{{+6.387305e-13, +8.900741e-13, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+8.900741e-13, +1.195123e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.035973e-12, +1.542166e-12, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.542166e-12, +8.855526e-12, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.073912e-12, -2.272640e-12}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -2.272640e-12, +5.790190e-12} 
				});
	}
}
