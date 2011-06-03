package xal.model.xml;

import java.io.IOException;

import xal.model.Lattice;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test cases for {@link LatticeXmlWriter}.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class LatticeXmlWriterTest extends TestCase {
	
	private static final String XML_IN = "xml/ModelValidation.lat.mod.xal.xml";
	private static final String XML_OUT = "build/tests/output/xal/model/xml/LatticeXmlWriterTest.ModelValidation.lat.mod.xal.xml";

	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());
	}
	
	public static Test suite() {
		return new TestSuite(LatticeXmlWriterTest.class);
	}
	
	public void testWriteMebt() {
        LatticeXmlParser parser = new LatticeXmlParser();
		Lattice lattice = null;
		try {
			lattice = parser.parseUrl(XML_IN, false);
		} catch (ParsingException e) {
			e.printStackTrace();
			fail("Lattice Parsing Exception: " + e.getMessage());
		}
		try {
			LatticeXmlWriter.writeXml(lattice, XML_OUT);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException on: " + XML_OUT);
		}
	}
}
