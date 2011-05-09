package xal.model.xml;

import java.io.IOException;

import xal.tools.beam.CorrelationMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.ens.Ensemble;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.model.alg.EnsembleTracker;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.ParticleTracker;
import xal.model.alg.resp.ParticleResponse;
import xal.model.probe.DiagnosticProbe;
import xal.model.probe.EnsembleProbe;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.Probe;
import xal.model.probe.resp.ParticlePerturb;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Junit tests for writing probes to xml and parsing xml documents to instantiate
 * <code>Probe</code> objects.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class ProbeXmlTest extends TestCase {

	private static final String DIAGNOSTIC = 
		"ProbeXmlWriterTest.Diagnostic.probe.mod.xal.xml";
	private static final String PARTICLE = 
		"ProbeXmlWriterTest.Particle.probe.mod.xal.xml";
	private static final String ENVELOPE = 
		"ProbeXmlWriterTest.Envelope.probe.mod.xal.xml";
	private static final String ENSEMBLE = 
		"ProbeXmlWriterTest.Ensemble.probe.mod.xal.xml";
	private static final String PARTICLE_PERTURB = 
		"ProbeXmlWriterTest.ParticlePerturb.probe.mod.xal.xml";

	/**
	 * Entry point for the JUnit 3 test suite.
	 *
	 * @param args command line arguments (not used)
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public static void main(String[] args) {
		TestRunner.run (suite());
	}
	
	
	/**
	 * Utility method for creating a JUnit test suite 
	 * based upon this class.
	 *
	 * @return the value <code>new TestSuite(ProbeXmlTest.class)</code>
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public static Test suite() {
		return new TestSuite(ProbeXmlTest.class);
	}
	
	private void saveProbe(Probe probe, String outfile) {
		try {
			ProbeXmlWriter.writeXml(probe, outfile);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException on: " + outfile);
		}
	}
	
	private Probe parseProbe(String fileUri) {
		try {
			Probe probe = ProbeXmlParser.parse(fileUri);
			return probe;
		} catch (ParsingException e) {
			e.printStackTrace();
			fail("ParsingException reading: " + fileUri);
			return null;
		}		
	}
	
	/**
	 * Test the diagnostic probe
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public void testDiagnosticProbe() {		
		DiagnosticProbe probe = new DiagnosticProbe();
		probe.setElementsVisited(42);		
		saveProbe(probe, DIAGNOSTIC);	
		DiagnosticProbe parsedProbe = (DiagnosticProbe)parseProbe(DIAGNOSTIC);
		assertTrue(parsedProbe.getElementsVisited() == probe.getElementsVisited());			
	}
	
	/**
	 * Test the particle probe
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public void testParticleProbe() {		
		ParticleProbe probe = new ParticleProbe();
		probe.setAlgorithm(new ParticleTracker());
		probe.setSpeciesRestEnergy(939.3014e+6);
		probe.setSpeciesCharge(1.602e-19);
        probe.setKineticEnergy(2.5e+6); 
		probe.setPhaseCoordinates(PhaseVector.zero());	
		saveProbe(probe, PARTICLE);				
		ParticleProbe parsedProbe = (ParticleProbe)parseProbe(PARTICLE);
		assertTrue(parsedProbe.getAlgorithm().getType() == probe.getAlgorithm().getType());
		assertTrue(parsedProbe.getKineticEnergy() == probe.getKineticEnergy());			
		assertTrue(parsedProbe.getSpeciesRestEnergy() == probe.getSpeciesRestEnergy());			
		assertTrue(parsedProbe.getSpeciesCharge() == probe.getSpeciesCharge());
		assertTrue(parsedProbe.phaseCoordinates().equals(probe.phaseCoordinates()));			
		assertTrue(parsedProbe.phaseCoordinates().hashCode() == probe.phaseCoordinates().hashCode());
		// negative test			
		parsedProbe.phaseCoordinates().setElem(3, 14.0);
		assertFalse(parsedProbe.phaseCoordinates().equals(probe.phaseCoordinates()));	
		assertFalse(parsedProbe.phaseCoordinates().hashCode() == probe.phaseCoordinates().hashCode());		
	}
	
	/**
	 * Test the envelope probe
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public void testEnvelopeProbe() {		
		EnvelopeProbe probe = new EnvelopeProbe();
		probe.setAlgorithm(new EnvelopeTracker());
		probe.setSpeciesRestEnergy(939.3014e+6);
		probe.setSpeciesCharge(1.602e-19);
        probe.setKineticEnergy(2.5e+6); 
//		probe.setBeamCharge(0.42);
        probe.setBunchFrequency(324.0e6);
		probe.setBeamCurrent(42.0);
		probe.setCorrelation(new CorrelationMatrix());	
		saveProbe(probe, ENVELOPE);	
		EnvelopeProbe parsedProbe = (EnvelopeProbe)parseProbe(ENVELOPE);		
		assertTrue(parsedProbe.getAlgorithm().getType() == probe.getAlgorithm().getType());
		assertTrue(parsedProbe.getKineticEnergy() == probe.getKineticEnergy());			
		assertTrue(parsedProbe.getSpeciesRestEnergy() == probe.getSpeciesRestEnergy());			
		assertTrue(parsedProbe.getSpeciesCharge() == probe.getSpeciesCharge());
//		assertTrue(parsedProbe.bunchCharge() == probe.bunchCharge());
        assertTrue(parsedProbe.getBeamCurrent() == probe.getBeamCurrent());
        assertTrue(parsedProbe.getBunchFrequency() == probe.getBunchFrequency());
		assertTrue(parsedProbe.getCorrelation().equals(probe.getCorrelation()));
		assertTrue(parsedProbe.getCorrelation().hashCode() == probe.getCorrelation().hashCode());
		// negative test			
		parsedProbe.getCorrelation().setElem(3,3,14.0);
		assertFalse(parsedProbe.getCorrelation().equals(probe.getCorrelation()));			
		assertFalse(parsedProbe.getCorrelation().hashCode() == probe.getCorrelation().hashCode());		
	}
	
	/**
	 * Test the particle perturbation probe
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	@SuppressWarnings("deprecation")
    public void testParticlePerturb() {		
		ParticlePerturb probe = new ParticlePerturb();
		probe.setAlgorithm(new ParticleResponse());
		probe.setSpeciesRestEnergy(939.3014e+6);
		probe.setSpeciesCharge(-1.602e-19);
		probe.setKineticEnergy(2.5e+6);	
		saveProbe(probe, PARTICLE_PERTURB);				
		ParticlePerturb parsedProbe = (ParticlePerturb) parseProbe(PARTICLE_PERTURB);
		assertTrue(parsedProbe.getAlgorithm().getType() == probe.getAlgorithm().getType());
		assertTrue(parsedProbe.getKineticEnergy() == probe.getKineticEnergy());			
		assertTrue(parsedProbe.getSpeciesRestEnergy() == probe.getSpeciesRestEnergy());			
		assertTrue(parsedProbe.getSpeciesCharge() == probe.getSpeciesCharge());
	}
	
	/**
	 * Test the ensemble probe
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public void testEnsembleProbe() {		
		EnsembleProbe probe = new EnsembleProbe();
		probe.setAlgorithm(new EnsembleTracker());
		probe.setKineticEnergy(2.5e+6);	
		probe.setSpeciesRestEnergy(939.3014e+6);
		probe.setSpeciesCharge(1.602e-19);
//		probe.setBeamCharge(0.42);
        probe.setBunchFrequency(324.0e6);
		probe.setBeamCurrent(42.0);
		probe.setEnsemble(new Ensemble());
		saveProbe(probe, ENSEMBLE);	
		EnsembleProbe parsedProbe = (EnsembleProbe)parseProbe(ENSEMBLE);		
		assertTrue(parsedProbe.getAlgorithm().getType() == probe.getAlgorithm().getType());
		assertTrue(parsedProbe.getKineticEnergy() == probe.getKineticEnergy());			
		assertTrue(parsedProbe.getSpeciesRestEnergy() == probe.getSpeciesRestEnergy());			
		assertTrue(parsedProbe.getSpeciesCharge() == probe.getSpeciesCharge());
//		assertTrue(parsedProbe.bunchCharge() == probe.bunchCharge());
        assertTrue(parsedProbe.getBunchFrequency() == probe.getBunchFrequency());
		assertTrue(parsedProbe.getBeamCurrent() == probe.getBeamCurrent());
//		assertTrue(parsedProbe.getEnsemble().equals(probe.getEnsemble()));
//		assertTrue(parsedProbe.getEnsemble().hashCode() == probe.getEnsemble().hashCode());
//		// negative test			
//		parsedProbe.getSigma().setElem(3,3,14.0);
//		assertFalse(parsedProbe.getEnsemble().equals(probe.getEnsemble()));			
//		assertFalse(parsedProbe.getEnsemble().hashCode() == probe.getEnsemble().hashCode());		
	}

	/**
	 * Test the probe XML parser
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public void testParseAdaptor() {
		DataAdaptor adaptor = XmlDataAdaptor.adaptorForUrl(DIAGNOSTIC, false);
		try {
			ProbeXmlParser.parseDataAdaptor(adaptor);
		} catch (ParsingException e) {
			fail("ParsingException reading DataAdaptor");
		}
	}
		
}
