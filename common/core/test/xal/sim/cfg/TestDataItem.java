/**
 * TestDataItem.java
 *
 * @author Christopher K. Allen
 * @since  May 20, 2011
 *
 */

/**
 * TestDataItem.java
 *
 * @author  Christopher K. Allen
 * @since	May 20, 2011
 */
package xal.sim.cfg;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.xml.XmlDataAdaptor;

/**
 * JUnit 4 test suite for class <code>DataItem</code>.
 *
 * @author Christopher K. Allen
 * @since   May 20, 2011
 */
public class TestDataItem {

    
    /** The XML data file */
    final static private String     STR_URL_XML_TEST = "common/core/test/resources/xal/sim/cfg/TestDataItem.xml";
    
    /** The output text dump of the data node */
    final static private String     STR_URL_TEXT_OUT = "common/core/test/output/xal/sim/cfg/TestDataItem.txt";
    
    
    
    private enum TEST {
        
        ATTR1("value1"), 
        
        ATTR2("value2"),
        
        ATTR3("value3");
        
        public String getValue() {
            return this.strVal;
        }
        
        final private String    strVal;
        
        private TEST(String strVal) {
            this.strVal = strVal;
        }
    }
    
    
    
    
    /**
     *
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  May 20, 2011
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * output.xal.sim.cfg
     *
     * @author Christopher K. Allen
     * @since  May 20, 2011
     *
     */

    /**
     *
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  May 20, 2011
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test method for {@link xal.sim.cfg.DataItem#save(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public void testSave() {
        DataItem<TEST>        data  = new DataItem<TEST>(TEST.class);
        
        for (TEST attr : TEST.values())
            data.setValue(attr, attr.getValue());

        XmlDataAdaptor  daArchive = XmlDataAdaptor.newEmptyDocumentAdaptor();
        DataAdaptor     daDoc = daArchive.createChild("doc");
        
        try {
            File    fileOut = new File(STR_URL_TEXT_OUT);

            data.save(daDoc);
            daArchive.writeTo(fileOut);

        } catch (IOException e) {
            System.err.println("Write Failed: " + e.getMessage());
            e.printStackTrace();
            fail("Write Failed: " + e.getMessage());
        
        }

    }

    /**
     * Test method for {@link xal.sim.cfg.DataItem#load(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public void testLoad() {
        DataAdaptor daSource = XmlDataAdaptor.adaptorForUrl(STR_URL_XML_TEST, false);
        DataAdaptor daDoc    = daSource.childAdaptor("doc");
        
        try { 
            DataItem<TEST>    datTest = new DataItem<TEST>(TEST.class, daDoc);
            
            System.out.println("Contents of Data Node " + DataItem.extractEnumName(TEST.class));
            for (TEST attr : TEST.values()) {
                String  strAtt = attr.name();
                String  strVal = datTest.getValString(attr);
                
                System.out.println("  " + strAtt + " = " + strVal);
            }
        
        } catch (DataFormatException e) {
            e.printStackTrace();
            System.err.println("Unable to read data file " + STR_URL_XML_TEST + ": " + e.getMessage());
            fail("Unable to read data file " + STR_URL_XML_TEST + ": " + e.getMessage());
            
        }
    }

}
