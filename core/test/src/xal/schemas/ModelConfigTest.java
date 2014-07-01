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
 * Unit test case for <code>ModelConfig.xsd</code> XML schema using <code>ModelConfig.xml</code> structure.
 * @author <a href='jakob.battelino@cosylab.com'>Jakob Battelino Prelog</a>
 */
public class ModelConfigTest extends AbstractXMLValidation {
	
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
		
		//Add and test elements element.
		testElements(document, root, validator);
		
		//Add and test hardware element.
		/*testHardware(document, root, validator);*/
		
		//Add and test associations element.
		testAssociations(document, root, validator);
	}
	
	@Override
	protected Document getTestDocument() throws Exception {
		return readDocument(DIR_TEST_XMLS+"modelconfig_test.xml");
	}
	
	@Override
	protected Schema getSchema() throws Exception {
		return readSchema(DIR_SCHEMAS+"ModelConfig.xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
	}
	
	@Override
	protected Document getExternalDocument() throws Exception {
		return readDocument(DIR_TEST_XMLS+"modelconfig_test.xml");
	}
	
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
		Element root = document.createElement("configuration");
		root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:noNamespaceSchemaLocation", "http://sourceforge.net/p/xaldev/openxal/ci/master/tree/core/resources/xal/schemas/ModelConfig.xsd?format=raw");
		document.appendChild(root);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete root element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'configuration' is not complete."));
		}
		
		//Fake root child
		try {
			Document testDoc = (Document)document.cloneNode(true);
			Element testRoot = (Element)testDoc.getElementsByTagName("configuration").item(0);
			Element fakeElement = testDoc.createElement("fake1");
			testRoot.appendChild(fakeElement);
			validator.validate(new DOMSource(testDoc));
			fail("Validation with incorrect root child element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Invalid content was found starting with element 'fake1'."));
		}
		
		return root;
	}
	
	private static Element testElements(Document document, Element root, Validator validator) {
		//Add elements element.
		Element elements = document.createElement("elements");
		root.appendChild(elements);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete elements element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'elements' is not complete."));
		}
		
		//Add default element.
		Element defaultElement = document.createElement("default");
		elements.appendChild(defaultElement);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete default element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'type' must appear on element"));
		}
		
		//Add 'type' attribute.
		defaultElement.setAttribute("type", "fake.package.model.Default");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete elements element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'elements' is not complete."));
			assertFalse(e.getMessage().contains("default"));
		}
		
		//Add drift element.
		Element driftElement = document.createElement("drift");
		elements.appendChild(driftElement);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete drift element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'type' must appear on element"));
		}
		
		//Add 'type' attribute.
		driftElement.setAttribute("type", "fake.package.model.Drift");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete configuration element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'configuration' is not complete."));
			assertFalse(e.getMessage().contains("drift"));
			assertFalse(e.getMessage().contains("elements"));
		}
		
		return elements;
	}
	
	/*
	private static Element testHardware(Document document, Element root, Validator validator) {
		//Add hardware element.
		Element hardware = document.createElement("hardware");
		root.appendChild(hardware);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete hardware element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'hardware' is not complete."));
		}
		
		//Add thin elements.
		Element thin1 = document.createElement("thin");
		hardware.appendChild(thin1);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete thin element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'type' must appear on element"));
		}
		
		//Add 'type' attribute.
		thin1.setAttribute("type", "fake.package.model.Thin1");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete hardware element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'hardware' is not complete."));
		}
		
		Element thin2 = document.createElement("thin");
		hardware.appendChild(thin2);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete thin element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'type' must appear on element"));
		}
		
		//Add 'type' attribute.
		thin2.setAttribute("type", "fake.package.model.Thin2");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete hardware element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'hardware' is not complete."));
		}
		
		//Add thick elements.
		Element thick1 = document.createElement("thick");
		hardware.appendChild(thick1);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete thick element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'type' must appear on element"));
		}
		
		//Add 'type' attribute.
		thick1.setAttribute("type", "fake.package.model.Thick1");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete hardware element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'hardware' is not complete."));
		}
		
		Element thick2 = document.createElement("thick");
		hardware.appendChild(thick2);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete thick element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'type' must appear on element"));
		}
		
		//Add 'type' attribute.
		thick2.setAttribute("type", "fake.package.model.Thick2");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete hardware element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'hardware' is not complete."));
		}
		
		//Add split element.
		Element split = document.createElement("split");
		hardware.appendChild(split);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete configuration element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'configuration' is not complete."));
			assertFalse(e.getMessage().contains("thin"));
			assertFalse(e.getMessage().contains("thick"));
		}
		
		return hardware;
	}*/
	
	private static Element testAssociations(Document document, Element root, Validator validator) {
		//Add associations element.
		Element associations = document.createElement("associations");
		root.appendChild(associations);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete associations element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'associations' is not complete."));
		}
		
		//Add and test basic map element.
		testBasicMap(document, associations, validator);
		
		//Add and test basic map element.
		/*testStaticMap(document, associations, validator);
		
		//Add and test basic map element.
		testSynchronizedMap(document, associations, validator);
		*/
		
		return associations;
	}
	
	private static Element testBasicMap(Document document, Element associations, Validator validator) {
		//Add basic map element.
		Element map = document.createElement("map");
		associations.appendChild(map);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete map element should not be successful!");
		} catch(Exception e) {
			assertTrue(	e.getMessage().contains("Attribute 'smf' must appear on element") ||
						e.getMessage().contains("Attribute 'model' must appear on element"));
		}
		
		//Add 'smf' and 'model' attributes.
		map.setAttribute("smf", "fake.package.BasicMap");
		map.setAttribute("model", "fake.package.model.BasicMap");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		return map;
	}
	
	/*
	private static Element testStaticMap(Document document, Element associations, Validator validator) {
		//Add static map element.
		Element map = document.createElement("map");
		associations.appendChild(map);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete map element should not be successful!");
		} catch(Exception e) {
			assertTrue(	e.getMessage().contains("Attribute 'smf' must appear on element") ||
						e.getMessage().contains("Attribute 'model' must appear on element"));
		}
		
		//Add 'smf' and 'model' attributes.
		map.setAttribute("smf", "fake.package.BasicMap");
		map.setAttribute("model", "fake.package.model.BasicMap");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Add static element.
		Element staticElement = document.createElement("static");
		map.appendChild(staticElement);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete static element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'static' is not complete."));
		}
		
		//Add first property element.
		Element property1 = document.createElement("property");
		staticElement.appendChild(property1);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete property element should not be successful!");
		} catch(Exception e) {
			assertTrue(	e.getMessage().contains("Attribute 'value' must appear on element") ||
						e.getMessage().contains("Attribute 'name' must appear on element"));
		}
		
		//Add 'name' and 'value' attributes.
		property1.setAttribute("name", "test1");
		property1.setAttribute("value", "1");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Add second property element.
		Element property2 = document.createElement("property");
		property2.setAttribute("name", "test2");
		property2.setAttribute("value", "2");
		staticElement.appendChild(property2);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with more than one property element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("No child element is expected at this point."));
		}
		staticElement.removeChild(property2);
		
		return map;
	}
	
	private static Element testSynchronizedMap(Document document, Element associations, Validator validator) {
		//Add synchronized map element.
		Element map = document.createElement("map");
		associations.appendChild(map);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete map element should not be successful!");
		} catch(Exception e) {
			assertTrue(	e.getMessage().contains("Attribute 'smf' must appear on element") ||
						e.getMessage().contains("Attribute 'model' must appear on element"));
		}
		
		//Add 'smf' and 'model' attributes.
		map.setAttribute("smf", "fake.package.BasicMap");
		map.setAttribute("model", "fake.package.model.BasicMap");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Add first synchronize element.
		Element synchronize1 = document.createElement("synchronize");
		map.appendChild(synchronize1);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete synchronize element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'mode' must appear on element"));
		}
		
		//Add 'mode' attribute.
		synchronize1.setAttribute("mode", "INIT");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete synchronize element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'synchronize' is not complete."));
			assertTrue(e.getMessage().contains("parameter"));
		}
		
		//Add first parameter element.
		Element parameter11 = document.createElement("parameter");
		synchronize1.appendChild(parameter11);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete parameter element should not be successful!");
		} catch(Exception e) {
			assertTrue(	e.getMessage().contains("Attribute 'name' must appear on element") ||
						e.getMessage().contains("Attribute 'hget' must appear on element") ||
						e.getMessage().contains("Attribute 'mset' must appear on element") ||
						e.getMessage().contains("Attribute 'type' must appear on element"));
		}
		
		//Add 'name', 'hget', 'mset' and 'type' attributes.
		parameter11.setAttribute("name", "A");
		parameter11.setAttribute("hget", "getField");
		parameter11.setAttribute("mset", "setField");
		parameter11.setAttribute("type", "java.lang.Double");
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Add second parameter element.
		Element parameter12 = document.createElement("parameter");
		parameter12.setAttribute("name", "B");
		parameter12.setAttribute("hget", "getField");
		parameter12.setAttribute("mset", "setField");
		parameter12.setAttribute("type", "java.lang.Double");
		synchronize1.appendChild(parameter12);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		//Add second synchronize element.
		Element synchronize2 = document.createElement("synchronize");
		Element parameter21 = document.createElement("parameter");
		parameter21.setAttribute("name", "A");
		parameter21.setAttribute("hget", "getField");
		parameter21.setAttribute("mset", "setField");
		parameter21.setAttribute("type", "java.lang.Double");
		synchronize2.appendChild(parameter21);
		map.appendChild(synchronize2);
		
		//Test different synchronize modes.
		try {
			synchronize2.setAttribute("mode", "INIT");
			validator.validate(new DOMSource(document));
			synchronize2.setAttribute("mode", "LIVE");
			validator.validate(new DOMSource(document));
			synchronize2.setAttribute("mode", "DESIGN");
			validator.validate(new DOMSource(document));
			synchronize2.setAttribute("mode", "RF_DESIGN");
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			System.out.println(e.getMessage());
			fail("Schema should support modes: INIT, LIVE, DESIGN, RF_DESIGN!");
		}
		
		return map;
	}*/
}
