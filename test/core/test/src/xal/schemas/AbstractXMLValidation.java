package xal.schemas;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Abstract test case for validation of XML schemas or XML files.<br>
 * Test case implements one JUnit test method {@link #basicSchemaValidation()}
 * which can test validity of both XML schema or an XML file.<br>
 * Extending classes must implement {@link #getTestDocument()}
 * and {@link #getSchema()} methods, so generally speaking, each extending JUnit test
 * will test one XML file - XML schema pair.<br>
 * Some <code>protected</code> utility methods for reading documents and schemas are also provided.
 * @author <a href='jakob.battelino@cosylab.com'>Jakob Battelino Prelog</a>
 */
public abstract class AbstractXMLValidation {
	
	/**	Path to a directory where the XML files to be tested against XML schemas are.	*/
	protected static final String DIR_EXTERNAL_XMLS = SchemaProperties.getProperty(SchemaProperties.KEY_DIR_EXTERNAL_XMLS);
	/**	Path to a directory where the XML schemas that will be tested are.	*/
	protected static final String DIR_SCHEMAS = SchemaProperties.getProperty(SchemaProperties.KEY_DIR_SCHEMAS);
	/**	Path to a directory where the XML files to test the XML schemas with are.	*/
	protected static final String DIR_TEST_XMLS = SchemaProperties.getProperty(SchemaProperties.KEY_DIR_TEST_XMLS);
	
	/**
	 * Basic XML schema validation method.<br>
	 * This method retrieves a test XMl document and an appropriate XML schema
	 * by calling {@link #getTestDocument()} and {@link #getSchema()} methods
	 * and then performs document validation against the provided schema.<br>
	 * Test fails, if any exceptions are caught, or if either retrieved document or schema is <code>null</code>.
	 * @see #getTestDocument()
	 * @see #getSchema()
	 */
	@Test
	public void basicSchemaValidation() {
		Document document = null;
		Schema schema = null;
		
		try {
			document = getTestDocument();
		} catch(Exception e) {
			fail("Exception caught while getting test document: "+e.getMessage());
		}
		
		try {
			schema = getSchema();
		} catch(Exception e) {
			fail("Exception caught while getting schema: "+e.getMessage());
		}

		assertNotNull(document);
		assertNotNull(schema);
		
		assertTrue(validate(document, schema));
	}
	
	/**
	 * Progressive XML schema validation method.<br>
	 * Since these tests should all be specific for each schema,
	 * the declaration is made <code>abstract</code>.
	 */
	@Test
	public abstract void progressiveSchemaValidation();
	
	/**
	 * External XML validation method.<br>
	 * This method retrieves an external XMl document and an appropriate XML schema
	 * by calling {@link #getExternalDocument()} and {@link #getSchema()} methods
	 * and then performs document validation against the provided schema.<br>
	 * Test fails, if any exceptions are caught, or if either retrieved document or schema is <code>null</code>.
	 * @see #getExternalDocument()
	 * @see #getSchema()
	 */
	@Test
	public void externalXMLValidation() {
		Document document = null;
		Schema schema = null;
		
		try {
			document = getExternalDocument();
		} catch(Exception e) {
			fail("Exception caught while getting external document: "+e.getMessage());
		}
		
		try {
			schema = getSchema();
		} catch(Exception e) {
			fail("Exception caught while getting schema: "+e.getMessage());
		}

		assertNotNull(document);
		assertNotNull(schema);
		
		assertTrue(validate(document, schema));
	}
	
	/**
	 * Returns the XML document to be tested in {@link #basicSchemaValidation()}.
	 * @return {@link Document} to be tested in {@link #basicSchemaValidation()}.
	 * @throws Exception if there is any errors while preparing the document.
	 * @see #basicSchemaValidation()
	 */
	protected abstract Document getTestDocument() throws Exception;
	
	/**
	 * Returns the XML schema to be tested in {@link #basicSchemaValidation()}.
	 * @return {@link Schema} to be tested in {@link #basicSchemaValidation()}.
	 * @throws Exception if there is any errors while preparing the schema.
	 * @see #basicSchemaValidation()
	 */
	protected abstract Schema getSchema() throws Exception;
	
	/**
	 * Returns the XML document to be tested in {@link #externalXMLValidation()}.
	 * @return {@link Document} to be tested in {@link #externalXMLValidation()}.
	 * @throws Exception if there is any errors while preparing the document.
	 * @see #externalXMLValidation()
	 */
	protected abstract Document getExternalDocument() throws Exception;
	
	/**
	 * Validates the provided DOM document against the provided XML schema.<br>
	 * Returns <code>true</code>, if the document has been successfully validated.
	 * @param document {@link Document} DOM document to be validated.
	 * @param schema {@link Schema} XML schema against which the document will be validated.
	 * @return <code>true</code>, if the document has been successfully validated.
	 */
	protected static boolean validate(Document document, Schema schema) {
		Validator validator = schema.newValidator();
		Source domSource = new DOMSource(document);
		try {
			validator.validate(domSource);
			return true;
		} catch(Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	/**
	 * Reads an XML {@link Document} from an XML file with the specified name.</br>
	 * {@link DocumentBuilder} used for parsing the file is obtained using {@link #getDocumentBuilder()}.
	 * @param xmlFileName {@link String} that is the name of the XML file to be read.
	 * @return {@link Document} XML document read from the specified file.
	 * @throws ParserConfigurationException if a satisfactory {@link DocumentBuilder} could not be created.
	 * @throws SAXException if any parse errors occur while parsing the specified file.
	 * @throws IOException if any IO errors occur while parsing the specified file.
	 * @see #getDocumentBuilder()
	 */
	protected static Document readDocument(String xmlFileName) throws ParserConfigurationException, SAXException, IOException {
		Document document = getDocumentBuilder().parse(AbstractXMLValidation.class.getResourceAsStream(xmlFileName));
		return document;
	}
	
	/**
	 * Reads an XML {@link Schema} from a file with the specified name.<br>
	 * {@link SchemaFactory} used for creating the schema uses the specified schema language.
	 * See {@link XMLConstants} for a list of valid schema language Namespace URIs.
	 * @param schemaFileName {@link String} that is the name of the XML schema file to be read.
	 * @param schemaLanguage {@link String} a valid schema language Namespace URI.
	 * @return {@link Schema} XML schema read from the specified file.
	 * @throws SAXException if any SAX errors occur while parsing the specified file.
	 * @throws NullPointerException if schema is <code>null</code>.
	 */
	protected static Schema readSchema(String schemaFileName, String schemaLanguage) throws SAXException, NullPointerException {
		SchemaFactory factory = SchemaFactory.newInstance(schemaLanguage);
		Schema schema = factory.newSchema(AbstractXMLValidation.class.getResource(schemaFileName));
		return schema;
	}
	
	/**
	 * Returns a {@link DocumentBuilder} created with default settings.<br>
	 * {@link DocumentBuilderFactory} used for creating the {@link DocumentBuilder}
	 * is set to ignore comments, ignore element content white space and to be Namespace aware.
	 * @return {@link DocumentBuilder} created with default settings.
	 * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the configuration requested.
	 */
	protected static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		documentFactory.setIgnoringComments(true);
		documentFactory.setIgnoringElementContentWhitespace(true);
		documentFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
		return documentBuilder;
	}
}
