package se.lu.esss.ics.jels;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import se.lu.esss.ics.jels.model.elem.els.ElsElementMapping;
import se.lu.esss.ics.jels.model.elem.jels.JElsElementMapping;
import se.lu.esss.ics.jels.smf.impl.ESSRfCavity;
import se.lu.esss.ics.jels.smf.impl.ESSRfGap;
import xal.model.IElement;
import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.sim.scenario.DefaultElementMapping;
import xal.sim.scenario.ElementMapping;
import xal.smf.AcceleratorSeq;

@RunWith(Parameterized.class)
public class NCellsTest extends TestCommon {
	@Parameters
	public static Collection<Object[]> probes() {
		double energy = 2.5e6, frequency = 4.025e8, current = 0;
		return Arrays.asList(new Object[][]{
				{setupOpenXALProbe(energy, frequency, current), JElsElementMapping.getInstance()},
				{setupElsProbe(energy, frequency, current), ElsElementMapping.getInstance()},
				//{setupOpenXALProbe(energy, frequency, current), DefaultElementMapping.getInstance()},					
				});
	}
	
	public NCellsTest(Probe probe, ElementMapping elementMapping) {
		super(probe, elementMapping);
	}

	@Test
	public void doNCellTest0() throws InstantiationException, ModelException {
		System.out.println("NCELLS m=0");
		// NCELLS 0 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0.386525 0.664594 0.423349 0.350508 0.634734 0.628339 0.249724 0.639103 0.622128 0.25257
		AcceleratorSeq sequence = ncells(4.025e8, 0, 3, 0.5, 5.27924e+06, -72.9826, 31, 0, 
				0.493611, 0.488812, 12.9359, -14.4824, 
				0.386525, 0.664594, 0.423349, 0.350508, 0.634734, 0.628339, 0.249724, 0.639103, 0.622128, 0.25257);
		
		run(sequence);
		  
		//printResults();
					
		checkTWTransferMatrix(new double[][] {
				{-1.251628e+02, -2.816564e+01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{-4.892498e+02, -1.101003e+02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, -1.251628e+02, -2.816564e+01, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, -4.892498e+02, -1.101003e+02, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +8.271148e+02, +1.599042e+02}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +6.136827e+03, +1.186419e+03}, 
		});
		
		checkTWResults( 1.014228386, new double[][] 
				{{+2.417781e-08, +9.451023e-08, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+9.451023e-08, +3.694372e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +3.069079e-08, +1.199687e-07, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.199687e-07, +4.689511e-07, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.999586e-06, +2.225560e-05}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.225560e-05, +1.651268e-04}});
	}
	
	@Test
	public void doNCellTest1() throws InstantiationException, ModelException {
		System.out.println("NCELLS m=1");
		//  NCELLS 1 3 0.5 5.34709e+06 -55.7206 31 0 0.493611 0.488812 12.9604 -14.5077 0.393562 0.672107 0.409583 0.342918 0.645929 0.612576 0.257499 0.650186 0.606429 0.259876
		AcceleratorSeq sequence = ncells(4.025e8, 1, 3, 0.5, 5.34709e+06, -55.7206, 31, 0,
				0.493611, 0.488812, 12.9604, -14.5077,
				0.393562, 0.672107, 0.409583, 0.342918, 0.645929, 0.612576, 0.257499, 0.650186, 0.606429, 0.259876);				
		
		run(sequence);
		  
		//printResults();
					
		checkTWTransferMatrix(new double[][] {
				{+9.781907e+00, +1.608910e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+2.764530e+01, +4.587636e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +9.781907e+00, +1.608910e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +2.764530e+01, +4.587636e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.945048e+01, -1.838470e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.222670e+01, -3.066498e+00}, 
		});
		
		checkTWResults(  1.016786270, new double[][]	{
				{+1.144407e-10, +3.244214e-10, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+3.244214e-10, +9.196951e-10, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +1.587170e-10, +4.494497e-10, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +4.494497e-10, +1.272745e-09, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.442445e-09, +2.390964e-09}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.390964e-09, +3.963208e-09}, 

		});
	}
	
	@Test
	public void doNCellTest2() throws InstantiationException, ModelException {
		System.out.println("NCELLS m=2");
		// NCELLS 2 3 0.5 5.34709e+06 -55.7206 31 0 0.493611 0.488812 12.9604 -14.5077 0.393562 0.672107 0.409583 0.342918 0.645929 0.612576 0.257499 0.650186 0.606429 0.259876
		AcceleratorSeq sequence = ncells(4.025e8, 2, 3, 0.5, 5.34709e+06, -55.7206, 31, 0,
				0.493611, 0.488812, 12.9604, -14.5077,
				0.393562, 0.672107, 0.409583, 0.342918, 0.645929, 0.612576, 0.257499, 0.650186, 0.606429, 0.259876);				
		
		run(sequence);
		  
		//printResults();
					
		checkTWTransferMatrix(new double[][] {
				{+4.158248e+01, +7.331508e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+1.039245e+02, +1.833476e+01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +4.158248e+01, +7.331508e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +1.039245e+02, +1.833476e+01, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.384991e+00, +2.217700e-01}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +6.462182e+01, +6.210999e+00}, 
		});
		
		checkTWResults(  1.011418698, new double[][]	{
				{+2.173306e-09, +5.432880e-09, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+5.432880e-09, +1.358124e-08, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +2.961684e-09, +7.403073e-09, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +7.403073e-09, +1.850484e-08, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.164218e-11, +5.876521e-10}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.876521e-10, +1.595673e-08}, 
		});
	}
	
	@Test
	public void doNCellTestNoTTF0() throws InstantiationException, ModelException {						 
		// NCELLS 0 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0 1 0 0 1 0 0 1 0 0
		System.out.println("NCELLS no TTF m=0");
		AcceleratorSeq sequence = ncells(4.025e8, 0, 3, 0.5, 5.27924e+06, -72.9826, 31, 0, 
				0.493611, 0.488812, 12.9359, -14.4824, 
				0,1,0,0,1,0,0,1,0,0);
		
		run(sequence);
		  
		//printResults();
		
		checkTWTransferMatrix(new double[][] {
				{-1.735870e+03, -4.086639e+02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{-8.865846e+03, -2.087226e+03, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, -1.735870e+03, -4.086639e+02, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, -8.865846e+03, -2.087226e+03, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -6.122390e+02, -1.129077e+02}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.024516e+03, -5.577793e+02}, 
		});
		
		checkTWResults(  1.000303097, new double[][] {
				{+4.857058e-06, +2.480712e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+2.480712e-05, +1.267008e-04, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +6.076266e-06, +3.103415e-05, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +3.103415e-05, +1.585049e-04, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.622443e-06, +8.015025e-06}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +8.015025e-06, +3.959499e-05}, 
		});
	}
		
	@Test
	public void doNCellTestNoTTF1() throws InstantiationException, ModelException {						 
		// NCELLS 1 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0 1 0 0 1 0 0 1 0 0
		System.out.println("NCELLS no TTF m=1");
		AcceleratorSeq sequence = ncells(4.025e8, 1, 3, 0.5, 5.27924e+06, -72.9826, 31, 0, 
				0.493611, 0.488812, 12.9359, -14.4824, 
				0,1,0,0,1,0,0,1,0,0);
		
		run(sequence);
		  
		//printResults();
			
		checkTWTransferMatrix(new double[][] {
				{-2.469811e-01, +1.206558e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{-8.205329e+00, -2.699117e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, -2.469811e-01, +1.206558e-01, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, -8.205329e+00, -2.699117e-01, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.321139e+01, +2.165450e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.969652e+02, +3.243518e+01}, 
		});
		
		checkTWResults(   1.002351947, new double[][] {
				{+1.891455e-13, +5.588942e-13, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+5.588942e-13, +5.034769e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +1.299134e-13, +1.187813e-12, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +1.187813e-12, +8.128660e-11, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.042244e-09, +6.048951e-08}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +6.048951e-08, +9.051856e-07}, 
	
		});
	}
	
	@Test
	public void doNCellTestNoTTF2() throws InstantiationException, ModelException {						 
		// NCELLS 2 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0 1 0 0 1 0 0 1 0 0
		System.out.println("NCELLS no TTF m=2");
		AcceleratorSeq sequence = ncells(4.025e8, 2, 3, 0.5, 5.27924e+06, -72.9826, 31, 0, 
				0.493611, 0.488812, 12.9359, -14.4824, 
				0,1,0,0,1,0,0,1,0,0);
		
		run(sequence);
		  
		//printResults();
					
		checkTWTransferMatrix(new double[][] {
				{-1.323586e+01, -1.982918e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{-6.859146e+02, -1.028191e+02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, -1.323586e+01, -1.982918e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, -6.859146e+02, -1.028191e+02, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.565648e+02, +4.473234e+01}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.890135e+03, +3.126602e+02}, 
		});
		
		checkTWResults(1.004183576, new double[][] {
				{+1.971330e-10, +1.021774e-08, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+1.021774e-08, +5.296029e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +2.793855e-10, +1.448011e-08, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +1.448011e-08, +7.504809e-07, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.158552e-06, +8.097754e-06}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +8.097754e-06, +5.659964e-05}, 
		});
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
			cavity.getRfField().setTTF_startCoefs(new double[] {});
			cavity.getRfField().setTTFCoefs(new double[] {});
			cavity.getRfField().setTTF_endCoefs(new double[] {});
		} else {
			cavity.getRfField().setTTF_startCoefs(new double[] {betas, Ti, kTi, k2Ti});
			cavity.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			cavity.getRfField().setTTF_endCoefs(new double[] {betas, To, kTo, k2To});			
		}		

		
		// setup		
		ESSRfGap firstgap = new ESSRfGap("g0");
		
		double lambda = IElement.LightSpeed/frequency;
		double Lc0,Lc,Lcn;
		double amp0,ampn;
		double pos0, posn;
		
		amp0 = (1+kE0Ti)*(Ti/Ts);		
		ampn = (1+kE0To)*(To/Ts);
		if (m==0) {
			Lc = Lc0 = Lcn = betag * lambda;
			pos0 = 0.5*Lc0 + dzi*1e-3;
			posn = Lc0 + (n-2)*Lc + 0.5*Lcn + dzo*1e-3;			
		} else if (m==1) {
			Lc = Lc0 = Lcn = 0.5 * betag * lambda;
			pos0 = 0.5*Lc0 + dzi*1e-3;
			posn = Lc0 + (n-2)*Lc + 0.5*Lcn + dzo*1e-3;
			cavity.getRfField().setStructureMode(1);
		} else { //m==2
			Lc0 = Lcn = 0.75 * betag * lambda;
			Lc = betag * lambda;			
			pos0 = 0.25 * betag * lambda + dzi*1e-3;
			posn = Lc0 + (n-2)*Lc + 0.5 * betag * lambda + dzo*1e-3;
		}
						
		firstgap.setFirstGap(true); // this uses only phase for calculations
		firstgap.getRfGap().setEndCell(0);
		firstgap.setLength(0); // used only for positioning
		firstgap.setPosition(pos0);
		
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
			gap.getRfGap().setAmpFactor(1.0);
			cavity.addNode(gap);
		}
		
		ESSRfGap lastgap = new ESSRfGap("g"+(n-1));
		lastgap.getRfGap().setEndCell(1);
		lastgap.setLength(0); // used only for positioning
		lastgap.setPosition(posn);
		
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
