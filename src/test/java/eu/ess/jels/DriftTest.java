package eu.ess.jels;
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
		
		checkELSResults(9.500000E-02, new double[]{9.523765E-04, 1.177297E-03, 1.952594E-03},
				new double[] {3.158031E-01, 4.841974E-01, 9.768578E-01});
		
		checkTWTransferMatrix(new double[][]{
				
		});
		
		checkTWResults(1.003197291, new double[][] 
				{{+8.278830e-13, +1.513708e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
				{+1.513708e-12, +1.106878e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +1.265096e-12, +1.538810e-12, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +1.538810e-12, +7.267826e-12, +0.000000e+00, +0.000000e+00}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.476275e-12, +2.380509e-12}, 
				{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.380509e-12, +5.280827e-12}});
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
