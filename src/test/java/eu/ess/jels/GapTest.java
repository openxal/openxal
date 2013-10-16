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
		
		// GAP 78019.7 -90 14.5 0 0 0 0 0 0 0
		
		// input from TraceWin
		double frequency = 4.025e8; // this is global in TraceWin
		
		double E0TL = 78019.7 * 1e-6; // Effective gap voltage
		double Phis = -90;  // RF phase (deg) absolute or relative
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

		run(sequence);
		
		printResults(0.000000E+00, new double[] {8.374778E-04, 1.066568E-03, 1.836118E-03},
				new double [] {2.442000E-01, 3.974000E-01, 8.637909E-01});
	}
}
