/**
 * TestResourceManager.java
 *
 *  Created	: Jun 16, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.test;

import xal.app.pta.rscmgt.ResourceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit 4.x test case for the class <code>ResourceManager</code>.
 *
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jun 16, 2009
 * @author Christopher K. Allen
 */
public class TestResourceManager {

    /**
     * setUpBeforeClass
     *
     * @throws java.lang.Exception
     * 
     * @since  Jun 16, 2009
     * @author Christopher K. Allen
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * tearDownAfterClass
     *
     * @throws java.lang.Exception
     * 
     * @since  Jun 16, 2009
     * @author Christopher K. Allen
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * setUp
     *
     * @throws java.lang.Exception
     * 
     * @since  Jun 16, 2009
     * @author Christopher K. Allen
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * tearDown
     *
     * @throws java.lang.Exception
     * 
     * @since  Jun 16, 2009
     * @author Christopher K. Allen
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link xal.app.pta.rscmgt.ResourceManager#getPreferences()}.
     */
    @Test
    public final void testGetPreferences() {
        Preferences prefs = ResourceManager.getPreferences();
        
        System.out.println("The resource preferences are " + prefs.toString());
    }

//    /**
//     * Test method for {@link xal.app.pta.rscmgt.ResourceManager#getGlobalResourceJavaName(java.lang.String)}.
//     */
//    @Test
//    public final void testGetGlobalResourceName() {
//        String  strTest = "test";
//        String  strGbl  = ResourceManager.getGlobalResourceJavaName(strTest);
//        
//        System.out.println("The global resource name of " + strTest + " is " + strGbl);
//    }
//
//    /**
//     * Test method for {@link xal.app.pta.rscmgt.ResourceManager#getResourcePath(java.lang.String)}.
//     */
//    @Test
//    public final void testGetResourcePath() {
//        String  strTest = "test";
//        String  strGbl  = ResourceManager.getResourcePath(strTest);
//        
//        System.out.println("The resource path of " + strTest + " is " + strGbl);
//    }

    /**
     * Test method for {@link xal.app.pta.rscmgt.ResourceManager#getResourceUrl(java.lang.String)}.
     */
    @Test
    public final void testGetResourceUrl() {
        String  strTest = "About.properties";
        URL     urlTest = ResourceManager.getResourceUrl(strTest);
        
        System.out.println("The URL of " + strTest + " is " + urlTest.toString());
    }

//    /**
//     * Another test method for 
//     * {@link xal.app.pta.rscmgt.ResourceManager#getPreferences()}.
//     * This one tries to set the preferences
//     *
//     * 
//     * @since  Jun 16, 2009
//     * @author Christopher K. Allen
//     */
//    @Test
//    public void testSetPreferences() {
//        Preferences     cfgMain = ResourceManager.getPreferences();
//        
//        cfgMain.putInt(AppProperties.APP.SCR_WIDTH.propertyName(), 200);
//        cfgMain.putInt(AppProperties.APP.SCR_HEIGHT.propertyName(), 100);
//        try {
//            cfgMain.flush();
//        } catch (BackingStoreException e1) {
//            System.out.println("Unable to store new configuration");
//            fail("Unable to store new configuration");
//            e1.printStackTrace();
//        }
//        
//    }
    
    /**
     * Test the java.util.Properties save/restore
     *
     * 
     * @since  Jun 16, 2009
     * @author Christopher K. Allen
     */
    @Test
    public void testXmlConfiguration() {
        String strPack = TestResourceManager.class.getPackage().getName();
        strPack += ".output.";
        String strPath = strPack.replace('.', '/');
        strPath += "test.xml";
        File fileTest = new File(strPath);
        try {
            OutputStream os = new FileOutputStream(fileTest);

            Properties propMenu = ResourceManager.getProperties("menudef.properties");
            propMenu.storeToXML(os, "testXmlConfiguration() generated");
            os.close();
            
        } catch (IOException e) {
            System.err.println("unable to save properties XML file");
            e.printStackTrace();
        }
    }

}
