package xal.schemas;

import java.io.InputStream;
import java.util.Properties;

import org.junit.Assert;

/**
 * Wrapper for properties for XML and XML schema JUnit test classes.<br/>
 * Properties are read from file {@value #FILE_TEST_PROPERTIES}.<br/>
 * Key constants are provided for easier access to any known property.
 * @author <a href='jakob.battelino@cosylab.com'>Jakob Battelino Prelog</a>
 */
public final class SchemaProperties {
	
	/**	Property key for directory containing external XML files.	*/
	public static final String KEY_DIR_EXTERNAL_XMLS = "dir.xml.external";
	/**	Property key for directory containing XML schema files.	*/
	public static final String KEY_DIR_SCHEMAS = "dir.schemas";
	/**	Property key for directory containing test XML files.	*/
	public static final String KEY_DIR_TEST_XMLS = "dir.xml.test";
	
	/**	Relative path to the test properties file.	*/
	private static final String FILE_TEST_PROPERTIES = "schema_tests.properties";
	
	/**	Test properties cache.	*/
	private static Properties testProperties;
	
	/**
	 * Returns the value of the property with the specified key, or <code>null</code> if property is not found.
	 * @param propertyKey {@link String} the property key.
	 * @return {@link String} value of property with the specified key, or <code>null</code> if property is not found.
	 */
	public static final String getProperty(String propertyKey) {
		return getProperties().getProperty(propertyKey);
	}
	
	/**
	 * Returns the test properties, or loads them from a file, if they have not been accessed yet.<br/>
	 * Properties are read from file {@value #FILE_TEST_PROPERTIES}.
	 * If there is an exception while loading properties, empty properties are returned.
	 * @return {@link Properties} loaded from {@value #FILE_TEST_PROPERTIES}.
	 */
	private static Properties getProperties() {
		if(testProperties == null) {
			InputStream propertiesFile = SchemaProperties.class.getResourceAsStream(FILE_TEST_PROPERTIES);
			Properties tempProperties = new Properties();
			try {
				tempProperties.load(propertiesFile);
			} catch(Exception e) {
				Assert.fail("Exception "+e);
			}
			testProperties = tempProperties;
		}
		return testProperties;
	}
}
