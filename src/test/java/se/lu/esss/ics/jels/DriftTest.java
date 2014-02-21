package se.lu.esss.ics.jels;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.sim.scenario.ElementMapping;
import xal.smf.AcceleratorSeq;

@RunWith(Parameterized.class)
public class DriftTest extends TestCommon {
	
	public DriftTest(Probe probe, ElementMapping elementMapping)
	{
		super(probe, elementMapping);
	}
	
	@Test
	public void doDriftTest() throws InstantiationException, ModelException {
				
		// DRIFT 95 15 0
		AcceleratorSeq sequence = drift(95e-3, 0., 0.);	
			
		run(sequence);
		
		//printResults();
		if (initialEnergy == 3e6) {
			checkELSResults(9.500000E-02, new double[]{9.098807E-04, 1.124765E-03, 1.864477E-03},
					new double[] {3.158031E-01, 4.841974E-01, 9.758203E-01});
			
			
			checkTWTransferMatrix(new double[][]{
					{+1.000000e+00, +9.500000e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +1.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.000000e+00, +9.500000e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +9.439542e-02}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00}, 		
			});
			
			checkTWResults(1.003197291, new double[][] {
					{+8.278830e-13, +1.513708e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
					{+1.513708e-12, +1.106878e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.265096e-12, +1.538810e-12, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.538810e-12, +7.267826e-12, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.476275e-12, +2.380509e-12}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.380509e-12, +5.280827e-12}
			});
		}
		if (initialEnergy == 2.5e9) {
			checkTWTransferMatrix(new double[][]{
					{+1.000000e+00, +9.500000e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +1.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.000000e+00, +9.500000e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +7.074825e-03}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00}, 
	
			});
			
			checkTWResults(3.664409209, new double[][] {
					{+1.879417e-14, +3.436341e-14, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+3.436341e-14, +2.512778e-13, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +2.871956e-14, +3.493326e-14, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +3.493326e-14, +1.649904e-13, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.914705e-15, +5.404107e-14}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.404107e-14, +1.599526e-12}, 
			});
		}
	}
	
	/**
	 * 
	 * @param L length
	 * @param R aperture
	 * @param Ry aperture y
	 * @return sequence with drift
	 */
	public AcceleratorSeq drift(double L, double R, double Ry)
	{
		AcceleratorSeq sequence = new AcceleratorSeq("DriftTest");
		sequence.setLength(L);
		return sequence;
	}
	
}
