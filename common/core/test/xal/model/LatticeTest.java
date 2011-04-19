/*
 * Created on Oct 10, 2003
 */
package xal.model;

import xal.model.xml.LatticeXmlParser;
import xal.model.xml.ParsingException;

import junit.framework.TestCase;

/**
 * Provides basic tests for lattice and factory instance creation methods for
 * providing test data.
 * 
 * @author Craig McChesney
 */
public class LatticeTest extends TestCase {

	/** This file name is incorrect of course */
	public static final String LATTICE_URL = "xml/ModelValidation.lat.mod.xal.xml";
	
	/**
	 * Creates a new model lattice object from the XML file
	 * describing it.
	 *
	 * @return a new lattice described in the above static file
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 15, 2011
	 */
	public static Lattice newTestLattice() {
		try {
			return LatticeXmlParser.parse(LATTICE_URL, false);
		} catch (ParsingException e) {
			e.printStackTrace();
			return null;
		}	
	}

}
