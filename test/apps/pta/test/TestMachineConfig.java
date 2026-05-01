/**
 * TestMachineConfig.java
 *
 * Author  : Christopher K. Allen
 * Since   : Apr 17, 2014
 */
package xal.app.pta.test;

import static org.junit.Assert.*;
import xal.app.pta.daq.MachineConfig;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.tools.data.DataAdaptor;
import xal.service.pvlogger.PvLoggerException;
import xal.tools.xml.XmlDataAdaptor;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.WireHarp;
import xal.smf.impl.WireScanner;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.data.XMLDataManager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test cases for class <code>MachineConfig</code>.
 *
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Apr 17, 2014
 */
public class TestMachineConfig {

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
    public static final String  STR_FILE_WRITE = "gov/sns/apps/pta/test/output/TestMachineConfWrite.xml";
    
    /** File to read the device configuration information  */
    public static final String  STR_FILE_READ = "gov/sns/apps/pta/test/output/TestMachineConfRead.xml";
    
    
    /*
     * Global Variables
     */
    
    /** The device under test */
    private static AcceleratorNode       SMF_HARP;
    
    /** Device under test */
    private static AcceleratorNode       SMF_SCANNER;
    
    /** collection of devices under test */
    private static List<ProfileDevice>    LST_SMF_PROFDEVS = new LinkedList<ProfileDevice>();
    
    
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
     * @since  Apr 17, 2014
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
        
        LST_SMF_PROFDEVS.add((WireHarp)SMF_HARP);
        LST_SMF_PROFDEVS.add((WireScanner)SMF_SCANNER);
        
        // Setup the data adaptors
        File        fileRead  = new File(STR_FILE_READ);

        DAPT_FILE_READ = XmlDataAdaptor.adaptorForFile(fileRead, false); 
        System.out.println("HEY 1!");
//        DAPT_FILE_READ  = daptRootRd.childAdaptor("ProfileDevices");
        System.out.println("HEY 2!");
        
        XML_ROOT_WRITE = XmlDataAdaptor.newEmptyDocumentAdaptor();
        System.out.println("HEY 3!");
        DAPT_FILE_WRITE = XML_ROOT_WRITE.createChild(MachineConfig.class.getName());
        
        System.out.println("HEY 4!");
    }

    /**
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Apr 17, 2014
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
     * Test method for {@link xal.app.pta.daq.MachineConfig#acquire(java.util.List)}.
     */
    @Test
    public void testAcquire() {
        try {
            
            MachineConfig   cfgMach = MachineConfig.acquire(LST_SMF_PROFDEVS);
            
            System.out.println("Machine Configuration Information Acquired");
            System.out.println( cfgMach.toString() );
            
        } catch (ConnectionException e) {
            e.printStackTrace();
            fail("Unable to acquire machine configuration information - MachineConfig#acquire()");
            
        } catch (GetException e) {
            e.printStackTrace();
            fail("Unable to acquire machine configuration information - MachineConfig#acquire()");
            
        } catch (PvLoggerException e) {
            e.printStackTrace();
            fail("Unable to acquire machine configuration information - MachineConfig#acquire()");
            
        }
        
    }

    /**
     * Test method for {@link xal.app.pta.daq.MachineConfig#load(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public void testLoad() {
        
        try {
            DataAdaptor     daptCfg = DAPT_FILE_READ.childAdaptor(MachineConfig.class.getName());
            MachineConfig   cfgMach = MachineConfig.load(daptCfg);
            
            System.out.println("Machine Configuration Information read from file " + STR_FILE_READ);
            System.out.println(cfgMach.toString());
            
        } catch (IllegalArgumentException e) {
            
            e.printStackTrace();
            fail("Unable to read machine configuration information from file " + STR_FILE_READ);
            
        }
    }

    /**
     * Test method for {@link xal.app.pta.daq.MachineConfig#update(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public void testRead() {
    }

    /**
     * Test method for {@link xal.app.pta.daq.MachineConfig#write(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public void testWrite() {
        try {
            
            MachineConfig   cfgMach = MachineConfig.acquire(LST_SMF_PROFDEVS);
            
            System.out.println("Machine Configuration Information Stored in File: " + STR_FILE_WRITE);
            cfgMach.write(DAPT_FILE_WRITE);
            
        } catch (ConnectionException e) {
            e.printStackTrace();
            fail("Unable to acquire machine configuration information - MachineConfig#acquire()");
            
        } catch (GetException e) {
            e.printStackTrace();
            fail("Unable to acquire machine configuration information - MachineConfig#acquire()");
            
        } catch (PvLoggerException e) {
            e.printStackTrace();
            fail("Unable to acquire machine configuration information - MachineConfig#acquire()");
            
        }
        
    }

}
