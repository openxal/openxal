package xal.schemas;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test case for <code>xdxf.xsd</code> XML schema using <code>hardware_status.xdxf</code> structure.
 * @author <a href='jakob.battelino@cosylab.com'>Jakob Battelino Prelog</a>
 */
public class HardwareStatusTest extends AbstractXMLValidation {
	
	/**
	 * Progressive XML schema validation method.<br>
	 * This method retrieves the XML schema by calling {@link #getSchema()}
	 * and then builds a test XML {@link Document} from scratch,
	 * testing it against the schema after each step in the building procedure.
	 * @see #getSchema()
	 */
	@Override
	public void progressiveSchemaValidation() {
		//Get XML schema.
		Schema schema = null;
		try {
			schema = getSchema();
		} catch(Exception e) {
			fail(e.getMessage());
		}
		assertNotNull(schema);
		
		//Create a new DOM document.
		Document document = null;
		try {
			document = getDocumentBuilder().newDocument();
		} catch(Exception e) {
			fail(e.getMessage());
		}
		assertNotNull(document);
		
		Validator validator = schema.newValidator();
		
		//Blank document should be valid.
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Blank document should be valid!");
		}
		
		//Add and test xdxf element.
		Element root = testRoot(document, validator);
		
		//Add and test sequence elements.
		testSequences(document, root, validator);
	}
	
	@Override
	protected Document getTestDocument() throws Exception {
		return readDocument(DIR_TEST_XMLS+"hardwarestatus_test.xml");
	}
	
	@Override
	protected Schema getSchema() throws Exception {
		return readSchema(DIR_SCHEMAS+"xdxf.xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
	}
	
	@Override
	protected Document getExternalDocument() throws Exception {
		return readDocument(DIR_TEST_XMLS+"hardwarestatus_test.xml");
	}
	
	/**
	 * Tests the <code>xdxf</code> element and returns it.<br>
	 * Provided DOM {@link Document} gets updated in the process.<br>
	 * Test checks against invalid root element tag and against invalid child elements.
	 * @param document {@link Document} DOM document built for testing.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 * @return {@link Element} <code>deviceMapping</code> element.
	 */
	private static Element testRoot(Document document, Validator validator) {
		//Fake root element.
		try {
			Document testDoc = (Document)document.cloneNode(true);
			Element fakeElement = testDoc.createElement("fake1");
			testDoc.appendChild(fakeElement);
			validator.validate(new DOMSource(testDoc));
			fail("Validation with incorrect root element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Cannot find the declaration of element 'fake1'."));
		}
		
		//Correct root element.
		Element root = document.createElement("xdxf");
		root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:noNamespaceSchemaLocation", "http://sourceforge.net/p/xaldev/openxal/ci/master/tree/core/resources/xal/schemas/xdxf.xsd?format=raw");
		document.appendChild(root);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Fake root child.
		try {
			Document testDoc = (Document)document.cloneNode(true);
			Element testRoot = (Element)testDoc.getElementsByTagName("xdxf").item(0);
			Element fakeElement = testDoc.createElement("fake1");
			testRoot.appendChild(fakeElement);
			validator.validate(new DOMSource(testDoc));
			fail("Validation with incorrect root child element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Invalid content was found starting with element 'fake1'."));
		}
		
		return root;
	}
	
	/**
	 * Tests the <code>sequence</code> and <code>node</code> elements.<br>
	 * Provided DOM {@link Document} gets updated in the process.<br>
	 * Test checks against missing required attributes, adding optional attributes
	 * and uniqueness of the attributes.
	 * @param document {@link Document} DOM document built for testing.
	 * @param root {@link Element} root element of the document.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 */
	private static void testSequences(Document document, Element root, Validator validator) {
		//Add sequence element.
		Element sequence1 = document.createElement("sequence");
		root.appendChild(sequence1);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete sequence element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'id' must appear on element"));
		}
		
		//Add 'id' attribute.
		sequence1.setAttribute("id", "seq1");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Add node element.
		Element node1 = document.createElement("node");
		sequence1.appendChild(node1);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete node element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'id' must appear on element"));
		}
		
		//Add 'id' attribute.
		node1.setAttribute("id", "Seq1:Node1");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Add optional attributes.
		node1.setAttribute("status", "false");
		node1.setAttribute("exclude", "false");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should still be valid!");
		}
		
		//Attribute 'id' must be unique.
		Element sequence2 = document.createElement("sequence");
		sequence2.setAttribute("id", "seq1");
		root.appendChild(sequence2);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with invalid sequence element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Duplicate key value"));
		}
		sequence2.setAttribute("id", "seq2");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			System.out.println(e.getMessage());
			fail("Document should still be valid!");
		}
		
		//Add nodes.
		Element node2 = document.createElement("node");
		node2.setAttribute("id", "Seq2:Node1");
		node2.setAttribute("status", "false");
		node2.setAttribute("exclude", "false");
		sequence2.appendChild(node2);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should still be valid!");
		}
		
//		//Attribute 'id' must be unique.
//		Element node3 = document.createElement("node");
//		node3.setAttribute("id", "Seq2:Node1");
//		node3.setAttribute("status", "false");
//		node3.setAttribute("exclude", "false");
//		sequence2.appendChild(node3);
//		try {
//			validator.validate(new DOMSource(document));
//			fail("Validation with invalid node element should not be successful!");
//		} catch(Exception e) {
//			assertTrue(e.getMessage().contains("Duplicate key value"));
//		}
//		node3.setAttribute("id", "Seq2:Node2");
//		try {
//			validator.validate(new DOMSource(document));
//		} catch(Exception e) {
//			System.out.println(e.getMessage());
//			fail("Document should still be valid!");
//		}
	}
}
