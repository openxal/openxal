package se.lu.esss.ics.jels;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.Probe;
import xal.smf.AcceleratorSeq;

public class DriftQuadTest {

	
	@Test @Ignore
	public void runGeneralTests() throws IOException, ModelException
	{
		Probe probea = GeneralTest.loadProbeFromXML(GeneralTest.class.getResource("probe.0.xml").toString());
		Probe probeb = GeneralTest.loadProbeFromXML(GeneralTest.class.getResource("probe.3.xml").toString());
		probeb = TestCommon.setupOpenXALProbe(3e6, 4.025e8, 0.0,
				new double[][]{{-0.1763,0.3,0.2098},
		  		{-0.3247,0.4,0.2091},
		  		{-0.5283,0.8,0.2851}}); 
		JElsDemo.saveProbe((EnvelopeProbe)probeb, "probe.xml");
		AcceleratorSeq quad = QuadTest.quad(70., -16., 15., 0., 0., 0., 0., 0.);
        AcceleratorSeq seq = new AcceleratorSeq("drift-quad");
        seq.addNode(quad);
        for (int i=0; i<3; i++) {
        	quad.setPosition(i*0.035);
        	seq.setLength(i*0.035+0.070);
        	probea.reset();
    		double dataOX[][] = GeneralTest.run(probea, seq);
            System.out.printf("%s\n", probea.getComment());
            GeneralTest.saveResults("openxal.quada."+i+".txt", dataOX);
        }
        for (int i=0; i<3; i++) {
        	quad.setPosition(i*0.035);
        	seq.setLength(i*0.035+0.070);
        	probeb.reset();
        	double dataOX[][] = GeneralTest.run(probeb, seq);
            System.out.printf("%s\n", probeb.getComment());
            GeneralTest.saveResults("openxal.quadb."+i+".txt", dataOX);
        }
		
	}
	
}
