/**
 * TestDeviceData.java
 *
 * Author  : Christopher K. Allen
 * Since   : Mar 31, 2014
 */
package xal.app.pta.test;

import static org.junit.Assert.*;
import xal.app.pta.daq.ScannerConfig;
import xal.app.pta.daq.ScannerData;
import xal.smf.AcceleratorNode;
import xal.smf.impl.WireScanner;

import org.junit.Test;

/**
 * Test cases for class <code>DeviceData</code> and its derived classes.
 *
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Mar 31, 2014
 */
public class TestDeviceData {


    /*
     * Global Constants
     */
    
    /** Relative location of the XAL configuration file */
    public static final String  STR_URL_XAL_MAIN = "./gov/sns/apps/pta/test/resources/main.xal";

    /** ID of sequence containing our test device */
    public static final String  STR_ID_SEQ = "SCLMed";
    
    /** ID of test device device */
    public static final String  STR_ID_DEV = "TEST_Diag:WS006";
    
    
    /*
     * Global Variables
     */
    
    /** The device under test */
    public static AcceleratorNode       SMF_DEV;
    
    
    /**
     * Test method for {@link xal.app.pta.daq.ScannerData#acquire(xal.smf.impl.WireScanner)}.
     */
//    @Test
    public void testAcquire() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testDeviceDataAttributes() {
        
        //
        // Measurement Device Configuration
        //
        
        /** Configuration of the measurement device for the data set */
        ScannerConfig                    cfgDevice = new ScannerConfig();

        if (cfgDevice == null)
            fail("Instantiation error: " + cfgDevice.getClass());
        
        
        //
        // Measurement Data
        //
        
        /** The raw profile data acquired from diagnostics */
        final WireScanner.DataRaw        datRaw = new WireScanner.DataRaw();
        
        /** The fitted profile data from the diagnostics */
        final WireScanner.DataFit        datFit = new WireScanner.DataFit();
        
        /** The raw measurement trace generating measurement data */ 
        final WireScanner.Trace          datTrace = new WireScanner.Trace();
        
        
        //
        // Measurement Signal Properties
        //
        
        /** The direct statistical parameters of the profile signals */
        final WireScanner.StatisticalAttrSet     sigStat = new WireScanner.StatisticalAttrSet();
        
        /** The Gaussian fit parameters for the profile signals */
        final WireScanner.GaussFitAttrSet        sigGauss = new WireScanner.GaussFitAttrSet();
        
        /** The Double Gaussian fit parameters of the profile signals */
        final WireScanner.DblGaussFitAttrSet     sigDblGauss = new WireScanner.DblGaussFitAttrSet();
    }

    /**
     * Test method for {@link xal.app.pta.daq.ScannerData#DeviceData()}.
     */
    @Test
    public void testDeviceData() {
        ScannerData  datTest = new ScannerData();
        
        if (datTest == null)
            fail("Unable to create empty ScannerData object");
    }

    /**
     * Test method for {@link xal.app.pta.daq.ScannerData#DeviceData(xal.smf.impl.WireScanner)}.
     */
//    @Test
    public void testDeviceDataWireScanner() {
        fail("Not yet implemented");
    }

}
