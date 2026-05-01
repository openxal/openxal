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
 * Unit test case for <code>tablegroup.xsd</code> XML schema using <code>model.params</code> structure.
 * @author <a href='jakob.battelino@cosylab.com'>Jakob Battelino Prelog</a>
 */
public class TableGroupTest extends AbstractXMLValidation {
	
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
		
		//Add and test tablegroup element.
		Element root = testRoot(document, validator);
		
		//Add and test first table element.
		Element tableA = testTableA(document, root, validator);
		
		//Add and test second table element.
		testTableB(document, root, validator);
		
		//Remove optional elements and test only with required.
		root.removeChild(tableA);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
	}
	
	@Override
	protected Document getTestDocument() throws Exception {
		return readDocument(DIR_TEST_XMLS+"tablegroup_test.xml");
	}
	
	@Override
	protected Schema getSchema() throws Exception {
		return readSchema(DIR_SCHEMAS+"tablegroup.xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
	}
	
	@Override
	protected Document getExternalDocument() throws Exception {
		return readDocument(DIR_TEST_XMLS+"tablegroup_test.xml");
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
		Element root = document.createElement("tablegroup");
		root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:noNamespaceSchemaLocation", "http://sourceforge.net/p/xaldev/openxal/ci/master/tree/core/resources/xal/schemas/tablegroup.xsd?format=raw");
		document.appendChild(root);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete root element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'tablegroup' is not complete."));
		}
		
		//Fake root child
		try {
			Document testDoc = (Document)document.cloneNode(true);
			Element testRoot = (Element)testDoc.getElementsByTagName("tablegroup").item(0);
			Element fakeElement = testDoc.createElement("fake1");
			testRoot.appendChild(fakeElement);
			validator.validate(new DOMSource(testDoc));
			fail("Validation with incorrect root child element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Invalid content was found starting with element 'fake1'."));
		}
		
		return root;
	}
	
	private static Element testTableA(Document document, Element root, Validator validator) {
		//Add table element.
		Element tableA = document.createElement("table");
		root.appendChild(tableA);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete table element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'name' must appear on element"));
		}
		
		//Add 'name' attribute.
		tableA.setAttribute("name", "tableA");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete modelConfig_source element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'table' is not complete."));
		}
		
		//Add and test schema element.
		testSchema(document, tableA, validator);
		
		//Add and test record elements.
		testRecords(document, tableA, validator);
		
		return tableA;
	}
	
	private static Element testSchema(Document document, Element tableA, Validator validator) {
		//Add schema element.
		Element schema = document.createElement("schema");
		tableA.appendChild(schema);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete schema element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'schema' is not complete."));
		}
		
		//Add and test attribute elements.
		testAttributes(document, schema, validator);
		
		return schema;
	}
	
	private static void testAttributes(Document document, Element schema, Validator validator) {
		//Add table element.
		Element attribute1 = document.createElement("attribute");
		schema.appendChild(attribute1);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete attribute element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'name' must appear on element"));
		}
		
		//Add 'name' attribute.
		attribute1.setAttribute("name", "id");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete attribute element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'type' must appear on element"));
		}
		
		//Add 'type' attribute.
		attribute1.setAttribute("type", "java.lang.Integer");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete attribute element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Attribute 'isPrimaryKey' must appear on element"));
		}
		
		//Add 'isPrimaryKey' attribute.
		attribute1.setAttribute("isPrimaryKey", "true");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete table element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'table' is not complete."));
		}
		
		//Add 'defaultValue' attribute.
		attribute1.setAttribute("defaultValue", "1.0");
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete table element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'table' is not complete."));
		}
		
		//Add invalid attributes.
		Element invalidName = document.createElement("attribute");
		invalidName.setAttribute("name", "id");
		invalidName.setAttribute("type", "java.lang.Integer");
		invalidName.setAttribute("isPrimaryKey", "false");
		schema.appendChild(invalidName);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with invalid attribute element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Duplicate unique value"));
		}
		schema.removeChild(invalidName);
		
		Element invalidType = document.createElement("attribute");
		invalidType.setAttribute("name", "invalid");
		invalidType.setAttribute("type", "java.lang.Object");
		invalidType.setAttribute("isPrimaryKey", "false");
		schema.appendChild(invalidType);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with invalid attribute element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("is not facet-valid with respect to enumeration"));
		}
		schema.removeChild(invalidType);
		
		//Add attributes with different attributes.
		Element attribute2 = document.createElement("attribute");
		attribute2.setAttribute("name", "name");
		attribute2.setAttribute("type", "java.lang.String");
		attribute2.setAttribute("isPrimaryKey", "false");
		schema.appendChild(attribute2);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete table element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'table' is not complete."));
		}
		
		Element attribute3 = document.createElement("attribute");
		attribute3.setAttribute("name", "active");
		attribute3.setAttribute("type", "java.lang.Boolean");
		attribute3.setAttribute("isPrimaryKey", "false");
		attribute3.setAttribute("defaultValue", "false");
		schema.appendChild(attribute3);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete table element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'table' is not complete."));
		}
		
		Element attribute4 = document.createElement("attribute");
		attribute4.setAttribute("name", "value");
		attribute4.setAttribute("type", "java.lang.Double");
		attribute4.setAttribute("isPrimaryKey", "false");
		attribute4.setAttribute("defaultValue", "0.0");
		schema.appendChild(attribute4);
		try {
			validator.validate(new DOMSource(document));
			fail("Validation with incomplete table element should not be successful!");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("The content of element 'table' is not complete."));
		}
	}
	
	private static void testRecords(Document document, Element table, Validator validator) {
		Element record1 = document.createElement("record");
		record1.setAttribute("id", "1");
		record1.setAttribute("name", "Name1");
		table.appendChild(record1);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		Element record2 = document.createElement("record");
		record2.setAttribute("id", "2");
		record2.setAttribute("name", "Name2");
		record2.setAttribute("active", "true");
		table.appendChild(record2);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		Element record3 = document.createElement("record");
		record3.setAttribute("id", "3");
		record3.setAttribute("name", "Name3");
		record3.setAttribute("value", "1.0");
		table.appendChild(record3);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		Element record4 = document.createElement("record");
		record4.setAttribute("id", "4");
		record4.setAttribute("name", "Name4");
		record4.setAttribute("active", "true");
		record4.setAttribute("value", "3.14159");
		table.appendChild(record4);
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
	}
	
	private static Element testTableB(Document document, Element root, Validator validator) {
		Element tableB = document.createElement("table");
		tableB.setAttribute("name", "tableB");
		root.appendChild(tableB);
		
		Element schema = document.createElement("schema");
		tableB.appendChild(schema);
		
		Element attribute = document.createElement("attribute");
		attribute.setAttribute("name", "id");
		attribute.setAttribute("type", "java.lang.Integer");
		attribute.setAttribute("isPrimaryKey", "true");
		schema.appendChild(attribute);
		
		Element record = document.createElement("record");
		record.setAttribute("id", "1");
		tableB.appendChild(record);
		
		try {
			validator.validate(new DOMSource(document));
		} catch(Exception e) {
			fail("Document should now be valid!");
		}
		
		return tableB;
	}
}
