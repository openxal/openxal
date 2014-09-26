/**
 * TestDeviceConfig.java
 *
 * Author  : Christopher K. Allen
 * Since   : Apr 16, 2014
 */
package xal.app.pta.test;

import static org.junit.Assert.fail;
import xal.app.pta.daq.DeviceConfig;
import xal.app.pta.daq.HarpConfig;
import xal.app.pta.daq.ScannerConfig;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.smf.impl.WireHarp;
import xal.smf.impl.WireScanner;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test cases for testing the class <code>DeviceConfig</code> and its
 * derived classes.
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 *
 * @author Christopher K. Allen
 * @since  Apr 16, 2014
 */
public class TestDeviceConfig {

    
    
    /*
     * Global Constants
     */
    
    /** Relative location of the XAL configuration file */
    public static final String  STR_URL_XAL_MAIN = "gov/sns/apps/pta/test/resources/main.xal";

    /** ID of sequence containing our test device */
    public static final String  STR_ID_SEQ = "RTBT";
    
    /** ID of test device */
    public static final String  STR_ID_HARP = "RTBT_Diag:Harp30";
    
    /** ID of test device */
    public static final String  STR_ID_SCANNER = "RTBT_Diag:WS02";
    
    
    /** File to store the device configuration information  */
    public static final String  STR_FILE_WRITE = "gov/sns/apps/pta/test/output/TestDevConfWrite.xml";
    
    /** File to read the device configuration information  */
    public static final String  STR_FILE_UPDATE = "gov/sns/apps/pta/test/output/TestDevConfRead.xml";
    
    
    /*
     * Global Variables
     */
    
    /** The device under test */
    private static AcceleratorNode       SMF_HARP;
    
    /** Device under test */
    private static AcceleratorNode       SMF_SCANNER;
    
    /** collection of devices under test */
    private static List<AcceleratorNode>    LST_SMF_DEVS = new LinkedList<AcceleratorNode>();
    
    
    /** Root data adaptor node for writing */
    private static XmlDataAdaptor       XML_ROOT_WRITE;
    
    /** Data adaptor used to test file storage writing */
    private static DataAdaptor          DAPT_FILE_WRITE;
    
    /** Data adaptor used to test file storage reading */
    private static DataAdaptor          DAPT_FILE_READ;
    
    
    
    /**
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Apr 16, 2014
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // Load the accelerator object and find the devices under test 
        Accelerator     xalAccel = XMLDataManager.acceleratorWithPath(STR_URL_XAL_MAIN);
        AcceleratorSeq  xalSeq   = xalAccel.findSequence(STR_ID_SEQ);
        
        SMF_HARP = xalSeq.getNodeWithId(STR_ID_HARP);
        System.out.println("Node ID = " + STR_ID_HARP + ", Node obj = " + SMF_HARP);
        
        SMF_SCANNER = xalSeq.getNodeWithId(STR_ID_SCANNER);
        System.out.println("Node ID = " + STR_ID_HARP + ", Node obj = " + SMF_SCANNER);
        
        LST_SMF_DEVS.add(SMF_HARP);
        LST_SMF_DEVS.add(SMF_SCANNER);
        
        // Setup the data adaptors
        File        fileRead  = new File(STR_FILE_UPDATE);

        XmlDataAdaptor  daptRootRd = XmlDataAdaptor.adaptorForFile(fileRead, false); 
        System.out.println("HEY 1!");
        DAPT_FILE_READ  = daptRootRd.childAdaptor("ProfileDevices");
        System.out.println("HEY 2!");
        
        XML_ROOT_WRITE = XmlDataAdaptor.newEmptyDocumentAdaptor();
        System.out.println("HEY 3!");
        DAPT_FILE_WRITE = XML_ROOT_WRITE.createChild("ProfileDevices");
        
        System.out.println("HEY 4!");
    }

    /**
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Apr 16, 2014
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        File        fileWrite = new File(STR_FILE_WRITE);

        XML_ROOT_WRITE.writeTo(fileWrite);
    }

    
    /*
     * Test Cases
     */
    
    /**
     * Test method for {@link xal.app.pta.daq.DeviceConfig#update(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public void testUpdate() {

        List<DeviceConfig>  lstDevCfg = new LinkedList<DeviceConfig>();
        
        for (DataAdaptor daptScan : DAPT_FILE_READ.childAdaptors(ScannerConfig.class.getName())) {
            ScannerConfig cfgScan = ScannerConfig.load(daptScan);
            
            lstDevCfg.add(cfgScan);
        }
        
        for (DataAdaptor daptHarp : DAPT_FILE_READ.childAdaptors(HarpConfig.class.getName())) {
            HarpConfig cfgHarp = HarpConfig.load(daptHarp);
            
            lstDevCfg.add(cfgHarp);
        }
    
        System.out.println("Configurations Read from File");
        for (DeviceConfig cfgDev : lstDevCfg)
            System.out.println( cfgDev.toString() );
        
    }

    /**
     * Test method for {@link xal.app.pta.daq.DeviceConfig#write(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public void testWrite() {
        
        for (AcceleratorNode smfDev : LST_SMF_DEVS) 
            if (smfDev instanceof WireHarp) {
                WireHarp    smfHarp = (WireHarp)SMF_HARP;

                try {
                    HarpConfig cfgHarp = HarpConfig.acquire(smfHarp);

                    cfgHarp.write(DAPT_FILE_WRITE);

                } catch (ConnectionException e) {
                    e.printStackTrace();
                    fail("Unable to acquire configuration information from " + smfHarp.getId());

                } catch (GetException e) {
                    e.printStackTrace();
                    fail("Unable to acquire configuration information from " + smfHarp.getId());

                }
                
            } else if (smfDev instanceof WireScanner) {
                WireScanner smfScan = (WireScanner)smfDev;
                
                try {
                    ScannerConfig cfgScan = ScannerConfig.acquire(smfScan);
                    
                    cfgScan.write(DAPT_FILE_WRITE);
                    
                } catch (ConnectionException e) {
                    e.printStackTrace();
                    fail("Unable to acquire configuration information from " + smfScan.getId());

                } catch (GetException e) {
                    e.printStackTrace();
                    fail("Unable to acquire configuration information from " + smfScan.getId());

                }

            } else {
                
                fail("Device " + smfDev + " is not a profile diagnostic device" );
                
            }
    }

}
