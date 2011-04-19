/*
 * Created on Oct 10, 2003
 */
package xal.model.probe;

import xal.model.Lattice;
import xal.model.LatticeTest;
import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.Probe;
import xal.model.xml.ParsingException;
import xal.model.xml.ProbeXmlParser;

import junit.framework.TestCase;

/**
 * Verifies basic Probe behaviors and provides factory methods for test data
 * creation.
 * 
 * @author Craig McChesney
 */
public class ProbeTest extends TestCase {
	
	
	// Test Data Urls ==========================================================
	
	
	private static final String PARTICLE = 
		"xml/ModelValidation.particle.probe.mod.xal.xml";
	
	private static final String ENVELOPE =
		"xml/ModelValidation.envelope.probe.mod.xal.xml";
	
	
	// Test Data Factory Methods ===============================================
	
	
	public static ParticleProbe newTestParticleProbe() {
		return (ParticleProbe) parseProbeFromUrl(PARTICLE);
	}
	
	public static EnvelopeProbe newTestEnvelopeProbe() {
		return (EnvelopeProbe) parseProbeFromUrl(ENVELOPE);
	}
	
	public static Probe parseProbeFromUrl(String Url) {
		try {
			return ProbeXmlParser.parse(Url);
		} catch (ParsingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	// Utility and support methods
	
	
	private void propagateProbe(Probe probe, Lattice lattice) {
		try {
			lattice.propagate(probe);
		} catch (ModelException e) {
			e.printStackTrace();
			fail("ModelException propagating probe");
		}
	}
	
	
	// Junit Test Cases (e.g., method names start with "test") =================
	
	
	public void testInitializeParticleProbeFromProbe() {
		
		// create a probe and run it
		ParticleProbe pOrig = newTestParticleProbe();
		Lattice lattice = LatticeTest.newTestLattice();
		propagateProbe(pOrig, lattice);
		
		// create a new probe initialized from the first
		ParticleProbe pNew = (ParticleProbe) Probe.newProbeInitializedFrom(pOrig);
		
		// get the original probe again and compare to the new one (pre-propagate)
		assertTrue(pNew.phaseCoordinates().equals(newTestParticleProbe().phaseCoordinates()));
		assertFalse(pNew.phaseCoordinates().equals(pOrig.phaseCoordinates()));
		
		propagateProbe(pNew, lattice);
	}

	public void testInitializeEnvelopeProbeFromProbe() {
		
		// create a probe and run it
		EnvelopeProbe pOrig = newTestEnvelopeProbe();
		Lattice lattice = LatticeTest.newTestLattice();
		propagateProbe(pOrig, lattice);
		
		// create a new probe initialized from the first
		EnvelopeProbe pNew = (EnvelopeProbe) Probe.newProbeInitializedFrom(pOrig);
		
		// get the original probe again and compare to the new one (pre-propagate)
		assertTrue(pNew.getCorrelation().equals(newTestEnvelopeProbe().getCorrelation()));
		assertFalse(pNew.getCorrelation().equals(pOrig.getCorrelation()));
		
		propagateProbe(pNew, lattice);
	}

}
