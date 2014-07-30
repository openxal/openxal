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
 * Unit test case for <code>xdxf.xsd</code> XML schema using <code>timing_pvs.tim</code> structure.
 * @author <a href='jakob.battelino@cosylab.com'>Jakob Battelino Prelog</a>
 */
public class TimingSourceTest extends AbstractXMLValidation {
	
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
		
		//Add and test timing element.
		Element root = testRoot(document, validator);
		
		//Add and test channelsuite element.
		testChannelSuite(document, root, validator);
	}
	
	@Override
	protected Document getTestDocument() throws Exception {
		return readDocument(DIR_TEST_XMLS+"timingsource_test.xml");
	}
	
	@Override
	protected Schema getSchema() throws Exception {
		return readSchema(DIR_SCHEMAS+"xdxf.xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
	}
	
	@Override
	protected Document getExternalDocument() throws Exception {
		return readDocument(DIR_TEST_XMLS+"timingsource_test.xml");
	}
	
	/**
	 * Tests the <code>timing</code> element and returns it.<br/>
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
		Element root = document.createElement("timing");
		root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:noNamespaceSchemaLocation", "http://sourceforge.net/p/xaldev/openxal/ci/master/tree/core/resources/xal/schemas/xdxf.xsd?format=raw");
		document.appendChild(root);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete root element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'timing' is not complete."));
		}
		
		//Fake root child
		try {
			Document testDoc = (Document)document.cloneNode(true);
			Element testRoot = (Element)testDoc.getElementsByTagName("timing").item(0);
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
	 * Tests the <code>channelsuite</code> elements.<br/>
	 * Provided DOM {@link Document} gets updated in the process.<br/>
	 * Test checks adding optional attributes and then tests channels in a separate method.
	 * See {@link #testChannels(Document, Element, Validator)} for details on channel testing.
	 * @param document {@link Document} DOM document built for testing.
	 * @param root {@link Element} root element of the document.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 * @see #testChannels(Document, Element, Validator)
	 */
	private static Element testChannelSuite(Document document, Element root, Validator validator) {
		//Add channelsuite element.
		Element channelSuite = document.createElement("channelsuite");
		root.appendChild(channelSuite);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Add 'name' attribute.
		channelSuite.setAttribute("name", "testsuite");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should still be valid!");
		}
		
		//Add and test attribute elements.
		testChannels(document, channelSuite, validator);
		
		return channelSuite;
	}
	
	/**
	 * Tests the <code>channel</code> elements.<br/>
	 * Provided DOM {@link Document} gets updated in the process.<br/>
	 * Test checks against missing required attributes, against adding optional attributes,
	 * uniqueness of the attributes and then tests transforms in a separate method.
	 * See {@link #testTransforms(Document, Element, Validator)} for details on transform testing.
	 * @param document {@link Document} DOM document built for testing.
	 * @param channelSuite {@link Element} root element of the document.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 * @see #testTransforms(Document, Element, Validator)
	 */
	private static void testChannels(Document document, Element channelSuite, Validator validator) {
		//Add first channel
		Element channel1 = document.createElement("channel");
		channelSuite.appendChild(channel1);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete channel element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'handle' must appear on element"));
		}
		
		//Add 'handle' attribute.
		channel1.setAttribute("handle", "test1");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Add optional attributes.
		channel1.setAttribute("signal", "Test_Timing:Test_Device:PV1");
		channel1.setAttribute("settable", "true");
		channel1.setAttribute("valid", "false");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Attribute 'handle' must be unique.
		Element channel2 = document.createElement("channel");
		channel2.setAttribute("handle", "test1");
		channelSuite.appendChild(channel2);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with invalid channel element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Duplicate key value"));
		}
		
		channel2.setAttribute("handle", "test2");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Add and test transform elements.
		testTransforms(document, channelSuite, validator);
	}
	
	/**
	 * Tests the <code>transform</code> elements.<br/>
	 * Provided DOM {@link Document} gets updated in the process.<br/>
	 * Test checks against missing required attributes, against against element-to-element references
	 * and correct attribute content types.
	 * @param document {@link Document} DOM document built for testing.
	 * @param channelSuite {@link Element} root element of the document.
	 * @param validator {@link Validator} for the tested XML {@link Schema}.
	 */
	private static void testTransforms(Document document, Element channelSuite, Validator validator) {
		//Add test channels.
		Element channel1 = document.createElement("channel");
		channel1.setAttribute("handle", "transTest1");
		channel1.setAttribute("transform", "doubleScaleTransform");
		channelSuite.appendChild(channel1);
		
		Element channel2 = document.createElement("channel");
		channel2.setAttribute("handle", "transTest2");
		channel2.setAttribute("transform", "doubleScaleTransform");
		channelSuite.appendChild(channel2);
		
		Element channel3 = document.createElement("channel");
		channel3.setAttribute("handle", "transTest3");
		channel3.setAttribute("transform", "doubleLinearTransform");
		channelSuite.appendChild(channel3);
		
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with missing referenced transform element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("not found for identity constraint of element 'channelsuite'."));
			assertTrue(	e.getMessage().contains("with value 'doubleScaleTransform'") ||
						e.getMessage().contains("with value 'doubleLinearTransform'"));
		}
		
		//Add referenced transform elements.
		Element transform1 = document.createElement("transform");
		transform1.setAttribute("name", "doubleScaleTransform");
		transform1.setAttribute("type", "doubleScale");
		channelSuite.appendChild(transform1);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with missing referenced transform element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("not found for identity constraint of element 'channelsuite'."));
			assertFalse(e.getMessage().contains("with value 'doubleScaleTransform'"));
			assertTrue(e.getMessage().contains("with value 'doubleLinearTransform'"));
		}
		
		Element transform2 = document.createElement("transform");
		transform2.setAttribute("name", "doubleLinearTransform");
		transform2.setAttribute("type", "doubleLinear");
		channelSuite.appendChild(transform2);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Add another transform element.
		Element transform3 = document.createElement("transform");
		channelSuite.appendChild(transform3);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete transform element should not be successful!");
		} catch(Exception e) {
			assertTrue(	e.getMessage().contains("Attribute 'name' must appear on element") ||
						e.getMessage().contains("Attribute 'type' must appear on element"));
		}
		
		//Add 'name' and 'type' attributes.
		transform3.setAttribute("name", "doubleTranslationTransform");
		transform3.setAttribute("type", "doubleTranslation");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Test invalid transform type enumeration.
		transform3.setAttribute("type", "fake1");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with invalid transform type should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Value 'fake1' is not facet-valid with respect to enumeration"));
		}
		transform3.setAttribute("type", "doubleTranslation");
		
		//Test missing transform type enumerations.
		Element transform4 = document.createElement("transform");
		transform4.setAttribute("name", "enum4");
		transform4.setAttribute("type", "doubleArrayScale");
		channelSuite.appendChild(transform4);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should still be valid!");
		}
		
		Element transform5 = document.createElement("transform");
		transform5.setAttribute("name", "enum5");
		transform5.setAttribute("type", "doubleArrayTranslation");
		channelSuite.appendChild(transform5);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should still be valid!");
		}
		
		Element transform6 = document.createElement("transform");
		transform6.setAttribute("name", "enum6");
		transform6.setAttribute("type", "doubleArrayLinear");
		channelSuite.appendChild(transform6);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should still be valid!");
		}
	}
}
