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
		AcceleratorSeq sequence = new AcceleratorSeq("DriftTest");
		
		double L = 95e-3; //length
		double R = 0.; //aperture
		double Ry = 0.; //aperture y
		
		sequence.setLength(L);
		
		run(sequence);
						
		printResults(9.500000E-02, new double[]{9.523765E-04, 1.177297E-03, 1.952594E-03},
				new double[] {3.158031E-01, 4.841974E-01, 9.768578E-01});
	}
}
