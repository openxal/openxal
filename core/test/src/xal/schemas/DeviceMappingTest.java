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
 * Unit test case for <code>impl.xsd</code> XML schema using <code>*.impl</code> structure.
 * @author <a href='jakob.battelino@cosylab.com'>Jakob Battelino Prelog</a>
 */
public class DeviceMappingTest extends AbstractXMLValidation {
	
	/**
	 * Progressive XML schema validation method.<br/>
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
		
		//Add and test deviceMapping element.
		Element root = testRoot(document, validator);
		
		//Add and test device elements.
		testDevice(document, root, validator);
	}
	
	@Override
	protected Document getTestDocument() throws Exception {
		return readDocument(DIR_TEST_XMLS+"devicemapping_test.xml");
	}
	
	@Override
	protected Schema getSchema() throws Exception {
		return readSchema(DIR_SCHEMAS+"impl.xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
	}
	
	@Override
	protected Document getExternalDocument() throws Exception {
		return readDocument(DIR_TEST_XMLS+"devicemapping_test.xml");
	}
	
	/**
	 * Tests the <code>deviceMapping</code> element and returns it.<br/>
	 * Provided DOM {@link Document} gets updated in the process.<br/>
	 * Test checks against invalid root element tag and against invalid child elements.
	 * @param document {@link Document} DOM document built for testing.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 * @return {@link Element} <code>deviceMapping</code> element.
	 */
	private static Element testRoot(Document document, Validator validator) {
		//Fake root element
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
		Element root = document.createElement("deviceMapping");
		root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:noNamespaceSchemaLocation", "http://sourceforge.net/p/xaldev/openxal/ci/master/tree/core/resources/xal/schemas/impl.xsd?format=raw");
		document.appendChild(root);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete root element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'deviceMapping' is not complete."));
		}
		
		//Fake root child
		try {
			Document testDoc = (Document)document.cloneNode(true);
			Element testRoot = (Element)testDoc.getElementsByTagName("deviceMapping").item(0);
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
	 * Tests the <code>device</code> elements.<br/>
	 * Provided DOM {@link Document} gets updated in the process.<br/>
	 * Test checks against premature root element completion, against missing required attributes
	 * and adding optional attributes.
	 * @param document {@link Document} DOM document built for testing.
	 * @param root {@link Element} root element of the document.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 */
	private static void testDevice(Document document, Element root, Validator validator) {
		//Add tablegroup_source element.
		Element device11 = document.createElement("device");
		root.appendChild(device11);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete device element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'type' must appear on element"));
		}
		
		//Add 'type' attribute.
		device11.setAttribute("type", "D11");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete device element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'class' must appear on element"));
		}
		
		//Add 'class' attribute.
		device11.setAttribute("class", "fake.package.DeviceA");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Test with additional devices.
		Element device12 = document.createElement("device");
		device12.setAttribute("type", "D12");
		device12.setAttribute("class", "fake.package.DeviceA");
		root.appendChild(device12);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should still be valid!");
		}
		
		//Test 'softType' optional attribute.
		Element device21 = document.createElement("device");
		device21.setAttribute("type", "D2");
		device21.setAttribute("class", "fake.package.DeviceB");
		root.appendChild(device21);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should still be valid!");
		}
		
		Element device22 = document.createElement("device");
		device22.setAttribute("type", "D2");
		device22.setAttribute("softType", "v1");
		device22.setAttribute("class", "fake.package.DeviceB");
		root.appendChild(device22);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should still be valid!");
		}
		
		Element device23 = document.createElement("device");
		device23.setAttribute("type", "D2");
		device23.setAttribute("softType", "v2");
		device23.setAttribute("class", "fake.package.DeviceC");
		root.appendChild(device23);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should still be valid!");
		}
	}
}
