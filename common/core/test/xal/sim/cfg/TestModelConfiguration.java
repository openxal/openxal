/**
 * TestModelConfiguration.java
 *
 * @author Christopher K. Allen
 * @since  May 16, 2011
 *
 */

/**
 * TestModelConfiguration.java
 *
 * @author  Christopher K. Allen
 * @since	May 16, 2011
 */
package xal.sim.cfg;

import static org.junit.Assert.*;

import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.smf.Accelerator;
import xal.tools.xml.XmlDataAdaptor.ParseException;
import xal.tools.xml.XmlDataAdaptor.ResourceNotFoundException;

/**
 * Test suite for the <code>ModelConfiguration</code> class.
 *
 * @author Christopher K. Allen
 * @since   May 16, 2011
 */
public class TestModelConfiguration {


    /** The XAL configuration file */
    final static private String     STR_URL_CONFIG = "common/core/test/resources/config/ModelConfig.xml";
    
    /** The output text dump of the association tree */
    final static private String     STR_URL_TEXT_OUT = "common/core/test/output/xal/sim/cfg/ModelConfig.txt";
    
    
    
    
    
    /**
     *
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  May 16, 2011
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     *
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  May 16, 2011
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test method for {@link xal.sim.cfg.ModelConfiguration#ModelConfiguration(java.lang.String)}.
     */
    @Test
    public void testModelConfigurationString() {
        try {
            ModelConfiguration      mcTest = new ModelConfiguration( STR_URL_CONFIG );
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            fail("The model configuration XML file failed to load: " + e.getMessage());
            
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            fail("The model configuration XML file failed to load: " + e.getMessage());
            
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            fail("The model configuration XML file failed to load: " + e.getMessage());
            
        }
    }

}
