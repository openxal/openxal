package eu.ess.jels;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import xal.model.IElement;
import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.sim.scenario.DefaultElementMapping;
import xal.sim.scenario.ElementMapping;
import xal.sim.scenario.ElsElementMapping;
import xal.sim.scenario.TWElementMapping;
import xal.smf.AcceleratorSeq;
import eu.ess.jels.smf.impl.ESSRfCavity;
import eu.ess.jels.smf.impl.ESSRfGap;

@RunWith(Parameterized.class)
public class NCellsTest extends TestCommon {
	@Parameters
	public static Collection<Object[]> probes() {
		double energy = 2.5e6, frequency = 4.025e8, current = 0;
		return Arrays.asList(new Object[][]{
					{setupOpenXALProbe(energy, frequency, current), DefaultElementMapping.getInstance()},
					{setupElsProbe(energy, frequency, current), ElsElementMapping.getInstance()},
					{setupOpenXALProbe(energy, frequency, current), TWElementMapping.getInstance()}
				});
	}
	
	public NCellsTest(Probe probe, ElementMapping elementMapping) {
		super(probe, elementMapping);
	}

	@Test
	public void doGapTest() throws InstantiationException, ModelException {						 
		// NCELLS 0 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0.386525 0.664594 0.423349 0.350508 0.634734 0.628339 0.249724 0.639103 0.622128 0.25257
		AcceleratorSeq sequence = ncells(4.025e8, 0, 3, 0.5, 5.27924e+06, -72.9826, 31, 0, 
				0.493611, 0.488812, 12.9359, -14.4824, 
				0.386525, 0.664594, 0.423349, 0.350508, 0.634734, 0.628339, 0.249724, 0.639103, 0.622128, 0.25257);
		
		run(sequence);
		  
		printResults();
		
		checkELSResults(1.117239E+00, new double[] {2.365512E-01, 2.661233E-01, 8.937822E-02},
				new double [] {5.192974E+04, 6.594518E+04, 5.455538E+03});
			
		checkTWResults( 1.014228386, new double[][] 
				{{+2.417781e-08, +9.451023e-08, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+9.451023e-08, +3.694372e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +3.069079e-08, +1.199687e-07, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.199687e-07, +4.689511e-07, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.999586e-06, +2.225560e-05}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.225560e-05, +1.651268e-04}});
	}
	
	
	public AcceleratorSeq ncells(double frequency, int m, int n, double betag, double E0T, double Phis, double R, double p,
			double kE0Ti, double kE0To, double dzi, double dzo, 
			double betas, double Ts, double kTs, double k2Ts, double Ti, double kTi, double k2Ti, double To, double kTo, double k2To)
	{
		AcceleratorSeq sequence = new AcceleratorSeq("GapTest");
		ESSRfCavity cavity = new ESSRfCavity("c");
		cavity.getRfField().setPhase(Phis);
		cavity.getRfField().setAmplitude(E0T * 1e-6);
		cavity.getRfField().setFrequency(frequency * 1e-6);	

		// TTF		
		if (betas == 0.0) {
			cavity.getRfField().setTTF_startCoefs(new double[] {1.0});
			cavity.getRfField().setTTFCoefs(new double[] {1.0});
			cavity.getRfField().setTTF_endCoefs(new double[] {1.0});
		} else {
			cavity.getRfField().setTTF_startCoefs(new double[] {betas, Ti, kTi, k2Ti});
			cavity.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setTTF_endCoefs(new double[] {betas, To, kTo, k2To});			
		}		

		
		// setup		
		ESSRfGap firstgap = new ESSRfGap("g0");
		
		double lambda = IElement.LightSpeed/frequency;
		double Lc0,Lc,Lcn;
		double amp0,amp,ampn;
		
		amp0 = (1+kE0Ti)*(Ti/Ts);
		amp = (1+kE0Ti)*(Ti/Ts); // verify this
		amp = 1;
		ampn = (1+kE0To)*(To/Ts);
		if (m==0) {
			Lc = Lc0 = Lcn = betag * lambda;			
			amp = 1;			
		} else if (m==1) {
			Lc = Lc0 = Lcn = 0.5 * betag * lambda;		
			cavity.getRfField().setStructureMode(1);
		} else { //m==2
			Lc0 = Lcn = 0.75 * betag * lambda;
			Lc = betag * lambda;				
		}
						
		firstgap.setFirstGap(true); // this uses only phase for calculations
		firstgap.getRfGap().setEndCell(0);
		firstgap.setLength(0); // used only for positioning
		firstgap.setPosition(0.5*Lc0 + dzi*1e-3);
		
		// following are used to calculate E0TL		
		firstgap.getRfGap().setLength(Lc0); 		
		firstgap.getRfGap().setAmpFactor(amp0);
		firstgap.getRfGap().setTTF(1);
		
		cavity.addNode(firstgap);
				
		for (int i = 1; i<n-1; i++) {
			ESSRfGap gap = new ESSRfGap("g"+i);
			gap.getRfGap().setTTF(1);
			gap.setPosition(Lc0 + (i-0.5)*Lc);
			gap.setLength(0);
			gap.getRfGap().setLength(Lc);
			gap.getRfGap().setAmpFactor(amp);
			cavity.addNode(gap);
		}
		
		ESSRfGap lastgap = new ESSRfGap("g"+(n-1));
		lastgap.getRfGap().setEndCell(1);
		lastgap.setLength(0); // used only for positioning
		lastgap.setPosition(Lc0 + (n-2)*Lc + 0.5*Lcn + dzo*1e-3);
		
		// following are used to calculate E0TL		
		lastgap.getRfGap().setLength(Lcn); 		
		lastgap.getRfGap().setAmpFactor(ampn);
		lastgap.getRfGap().setTTF(1);
		cavity.addNode(lastgap);		
		
		sequence.addNode(cavity);
		sequence.setLength(Lc0+(n-2)*Lc+Lcn);

		return sequence;
	}	
}
