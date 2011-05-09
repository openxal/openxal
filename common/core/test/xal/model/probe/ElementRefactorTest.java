package xal.model.probe;

import xal.model.Lattice;
import xal.model.alg.ParticleTracker;
import xal.model.probe.ParticleProbe;
import xal.model.probe.Probe;
import xal.model.xml.LatticeXmlParser;
import xal.model.xml.ParsingException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Verifies changes made during thick element / transfer map refactoring.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class ElementRefactorTest extends TestCase {
	
	private static final String LATTICE = 
		"xml/SimpleTestLattice.lat.mod.xal.xml";
		
	private Lattice lattice;
	private Probe probe;


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
	 * Utility method for creating a JUnit 3 <code>Test<code>
	 * object initialized to this class type.
	 *
	 * @return a <code>new TestSuite(ElementRefactorTest.class)</code> object
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public static Test suite() {
		return new TestSuite(ElementRefactorTest.class);
	}
	
	
    // ********** TestCase overrides
    
    
	// ********** test cases
	
	
//	public void testSequenceTransferMap() {
//		ElementSeq seq = getLattice();
//		double seqLength = seq.getLength();
//		
//		PhaseMap greaterMap = null;
//		PhaseMap equalMap = null;
//		PhaseMap lessMap = null;
//		
//		try {
//			
//			// try to get transfer map for length greater than seqLength
//			greaterMap = seq.transferMap(getProbe(), seqLength * 1.2);
//			
//			// try to get transfer map for length of seqLength
//			equalMap = seq.transferMap(getProbe(), seqLength);
//			
//			// get transfer map for length less than seqLength
//			lessMap = seq.transferMap(getProbe(), seqLength*.8);
//			
//		} catch (ModelException e) {
//			fail("Exception getting transferMap");
//		}
//		
//		assertTrue(greaterMap.equals(equalMap));
//		assertFalse(equalMap.equals(lessMap));
//	}
	
	
	// ********** support methods
	
	
	@SuppressWarnings("unused")
    private Lattice getLattice() {
		if (lattice == null) {
			//create a Lattice
	        LatticeXmlParser parser = new LatticeXmlParser();
			try {
				lattice = parser.parseUrl(LATTICE, false);
			} catch (ParsingException e) {
				e.printStackTrace();
				fail("Lattice Parsing Exception: " + e.getMessage());
			} catch (Exception e)   {
	                    e.printStackTrace();
	                    fail("Lattice parsing general exception: " + e.getMessage());
	        }
		}
		return lattice;
	}
	
	@SuppressWarnings("unused")
    private Probe getProbe() {
		if (probe == null) {
			probe = new ParticleProbe();
			probe.setAlgorithm(new ParticleTracker());
//	probe.setPhaseCoordinates(PhaseVector.zero());	
			probe.setSpeciesRestEnergy(939.3014e+6);
			probe.setSpeciesCharge(1.602e-19);
			probe.setKineticEnergy(2.5e+6);	
		}
		return probe;
	}

		
}
