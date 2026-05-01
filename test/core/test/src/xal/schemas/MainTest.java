package xal.schemas;

import static org.junit.Assert.assertFalse;
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
 * Unit test case for <code>main.xsd</code> XML schema using <code>main.xal</code> structure.
 * @author <a href='jakob.battelino@cosylab.com'>Jakob Battelino Prelog</a>
 */
public class MainTest extends AbstractXMLValidation {
	
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
		
		//Add and test sources element.
		Element root = testRoot(document, validator);
		
		//Add and test modelConfig_source element.
		testModelConfig(document, root, validator);
		
		//Add and test deviceMapping_source element.
		testDeviceMapping(document, root, validator);
		
		//Add and test optics_source element.
		testOptics(document, root, validator);
		
		//Add and test optics_extra element.
		Element opticsExtra = testOpticsExtra(document, root, validator);
		
		//Add and test hardware_status element.
		Element hardwareStatus = testHardwareStatus(document, root, validator);
		
		//Add and test timing_source element.
		testTiming(document, root, validator);
		
		//Add and test tablegroup_source element.
		testTablegroup(document, root, validator);
		
		//Remove optional elements and test only with required.
		root.removeChild(opticsExtra);
		root.removeChild(hardwareStatus);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
	}
	
	@Override
	protected Document getTestDocument() throws Exception {
		return readDocument(DIR_TEST_XMLS+"main_test.xml");
	}
	
	@Override
	protected Schema getSchema() throws Exception {
		return readSchema(DIR_SCHEMAS+"main.xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
	}
	
	@Override
	protected Document getExternalDocument() throws Exception {
		return readDocument(DIR_TEST_XMLS+"main_test.xml");
	}
	
	/**
	 * Tests the <code>sources</code> element and returns it.<br>
	 * Provided DOM {@link Document} gets updated in the process.<br>
	 * Test checks against invalid root element tag and against invalid child elements.
	 * @param document {@link Document} DOM document built for testing.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 * @return {@link Element} <code>sources</code> element.
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
		Element root = document.createElement("sources");
		root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:noNamespaceSchemaLocation", "http://sourceforge.net/p/xaldev/openxal/ci/master/tree/core/resources/xal/schemas/main.xsd?format=raw");
		document.appendChild(root);
		/*try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete root element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'sources' is not complete."));
		}*/
		
		//Fake root child
		try {
			Document testDoc = (Document)document.cloneNode(true);
			Element testRoot = (Element)testDoc.getElementsByTagName("sources").item(0);
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
	 * Tests the <code>modelConfig_source</code> element and returns it.<br>
	 * Provided DOM {@link Document} gets updated in the process.<br>
	 * Test checks against premature root element completion and against missing required attributes.
	 * @param document {@link Document} DOM document built for testing.
	 * @param root {@link Element} root element of the document.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 * @return {@link Element} <code>modelConfig_source</code> element.
	 */
	private static Element testModelConfig(Document document, Element root, Validator validator) {
		//Add modelConfig_source element.
		Element modelConfig = document.createElement("modelElementConfig_source");
		root.appendChild(modelConfig);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete modelConfig_source element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'name' must appear on element"));
		}
		
		//Add 'name' attribute.
		modelConfig.setAttribute("name", "modelConfig");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete modelConfig_source element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'url' must appear on element"));
		}
		
		//Add 'url' attribute.
		modelConfig.setAttribute("url", "test.xml");
		/*try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete sources element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'sources' is not complete."));
			assertFalse(e.getMessage().contains("modelConfig_source"));
		}*/
		
		return modelConfig;
	}
	
	/**
	 * Tests the <code>deviceMapping_source</code> element and returns it.<br>
	 * Provided DOM {@link Document} gets updated in the process.<br>
	 * Test checks against premature root element completion and against missing required attributes.
	 * @param document {@link Document} DOM document built for testing.
	 * @param root {@link Element} root element of the document.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 * @return {@link Element} <code>deviceMapping_source</code> element.
	 */
	private static Element testDeviceMapping(Document document, Element root, Validator validator) {
		//Add deviceMapping_source element.
		Element deviceMapping = document.createElement("deviceMapping_source");
		root.appendChild(deviceMapping);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete deviceMapping_source element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'name' must appear on element"));
		}
		
		//Add 'name' attribute.
		deviceMapping.setAttribute("name", "deviceMapping");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete deviceMapping_source element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'url' must appear on element"));
		}
		
		//Add 'url' attribute.
		deviceMapping.setAttribute("url", "test.xml");
		/*try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete sources element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'sources' is not complete."));
			assertFalse(e.getMessage().contains("deviceMapping_source"));
		}*/
		
		return deviceMapping;
	}
	
	/**
	 * Tests the <code>optics_source</code> element and returns it.<br>
	 * Provided DOM {@link Document} gets updated in the process.<br>
	 * Test checks against premature root element completion and against missing required attributes.
	 * @param document {@link Document} DOM document built for testing.
	 * @param root {@link Element} root element of the document.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 * @return {@link Element} <code>optics_source</code> element.
	 */
	private static Element testOptics(Document document, Element root, Validator validator) {
		//Add optics_source element.
		Element optics = document.createElement("optics_source");
		root.appendChild(optics);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete optics_source element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'name' must appear on element"));
		}
		
		//Add 'name' attribute.
		optics.setAttribute("name", "optics");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete optics_source element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'url' must appear on element"));
		}
		
		//Add 'url' attribute.
		optics.setAttribute("url", "test.xml");
		/*try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete sources element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'sources' is not complete."));
			assertFalse(e.getMessage().contains("optics_source"));
		}*/
		
		return optics;
	}
	
	/**
	 * Tests the <code>optics_extra</code> element and returns it.<br>
	 * Provided DOM {@link Document} gets updated in the process.<br>
	 * Test checks against premature root element completion and against missing required attributes.
	 * @param document {@link Document} DOM document built for testing.
	 * @param root {@link Element} root element of the document.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 * @return {@link Element} <code>optics_extra</code> element.
	 */
	private static Element testOpticsExtra(Document document, Element root, Validator validator) {
		//Add optics_extra element.
		Element opticsExtra = document.createElement("optics_extra");
		root.appendChild(opticsExtra);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete optics_extra element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'name' must appear on element"));
		}
		
		//Add 'name' attribute.
		opticsExtra.setAttribute("name", "opticsExtra");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete optics_extra element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'url' must appear on element"));
		}
		
		//Add 'url' attribute.
		opticsExtra.setAttribute("url", "test.xml");
		/*try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete sources element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'sources' is not complete."));
			//Additional optics_extra element should still be expected.
			assertTrue(e.getMessage().contains("optics_extra"));
		}*/
		
		return opticsExtra;
	}
	
	/**
	 * Tests the <code>hardware_status</code> element and returns it.<br>
	 * Provided DOM {@link Document} gets updated in the process.<br>
	 * Test checks against premature root element completion and against missing required attributes.
	 * @param document {@link Document} DOM document built for testing.
	 * @param root {@link Element} root element of the document.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 * @return {@link Element} <code>hardware_status</code> element.
	 */
	private static Element testHardwareStatus(Document document, Element root, Validator validator) {
		//Add hardware_status element.
		Element hardwareStatus = document.createElement("hardware_status");
		root.appendChild(hardwareStatus);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete hardware_status element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'name' must appear on element"));
		}
		
		//Add 'name' attribute.
		hardwareStatus.setAttribute("name", "hardwareStatus");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete hardware_status element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'url' must appear on element"));
		}
		
		//Add 'url' attribute.
		hardwareStatus.setAttribute("url", "test.xml");
		/*try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete sources element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'sources' is not complete."));
			assertFalse(e.getMessage().contains("hardware_status"));
		}*/
		
		return hardwareStatus;
	}
	
	/**
	 * Tests the <code>timing_source</code> element and returns it.<br>
	 * Provided DOM {@link Document} gets updated in the process.<br>
	 * Test checks against premature root element completion and against missing required attributes.
	 * @param document {@link Document} DOM document built for testing.
	 * @param root {@link Element} root element of the document.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 * @return {@link Element} <code>timing_source</code> element.
	 */
	private static Element testTiming(Document document, Element root, Validator validator) {
		//Add timing_source element.
		Element timing = document.createElement("timing_source");
		root.appendChild(timing);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete timing_source element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'name' must appear on element"));
		}
		
		//Add 'name' attribute.
		timing.setAttribute("name", "timing");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete timing_source element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'url' must appear on element"));
		}
		
		//Add 'url' attribute.
		timing.setAttribute("url", "test.xml");
		/*try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete sources element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'sources' is not complete."));
			assertFalse(e.getMessage().contains("timing_source"));
		}*/
		
		return timing;
	}
	
	/**
	 * Tests the <code>tablegroup_source</code> element and returns it.<br>
	 * Provided DOM {@link Document} gets updated in the process.<br>
	 * Test checks against premature root element completion,
	 * against missing required attributes and against unique field duplication.
	 * @param document {@link Document} DOM document built for testing.
	 * @param root {@link Element} root element of the document.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 * @return {@link Element} <code>tablegroup_source</code> element.
	 */
	private static Element testTablegroup(Document document, Element root, Validator validator) {
		//Add tablegroup_source element.
		Element tablegroup = document.createElement("tablegroup_source");
		root.appendChild(tablegroup);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete tablegroup_source element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'name' must appear on element"));
		}
		
		//Add 'name' attribute.
		tablegroup.setAttribute("name", "tablegroup");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete tablegroup_source element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'url' must appear on element"));
		}
		
		//Add 'url' attribute.
		tablegroup.setAttribute("url", "test.xml");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Test uniqueness of the 'name' attribute.
		Element tablegroup2 = document.createElement("tablegroup_source");
		tablegroup2.setAttribute("name", "tablegroup");
		tablegroup2.setAttribute("url", "test.xml");
		root.appendChild(tablegroup2);
		try {
			validator.validate(new DOMSource(document));
			fail("Two tablegroup_source elements should not be able to have the same name!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Duplicate unique value"));
		}
		tablegroup2.setAttribute("name", "tablegroup2");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		root.removeChild(tablegroup2);
		
		return tablegroup;
	}
}
