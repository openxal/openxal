package se.lu.esss.ics.jels;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.sim.scenario.ElementMapping;
import xal.smf.AcceleratorSeq;

public abstract class SingleElementTest extends TestCommon {
	protected SingleElementTestData data;
	
	public static class SingleElementTestData {
		// input
		Probe probe;
		ElementMapping elementMapping;
		AcceleratorSeq sequence;
		
		// TW output
		double[][] TWTransferMatrix, TWCorrelationMatrix;
		double TWGamma;
		
		// ELS output
		double elsPosition;
		double[] elsSigma, elsBeta;
	}
	
	public SingleElementTest(SingleElementTestData data) {
		super(data.probe, data.elementMapping);
		this.data = data;
	}

	@Test
	public void test() throws ModelException
	{
		run(data.sequence);
		
		//printResults();
		if (data.elsSigma != null)
			checkELSResults(data.elsPosition, data.elsSigma, data.elsBeta);
		
		checkTWTransferMatrix(data.TWTransferMatrix);
			
		checkTWResults( data.TWGamma, data.TWCorrelationMatrix);
	}
	
	@Parameters
	public static Collection<Object[]> probes() {
		return new ArrayList<Object[]>(0);
	}
}
