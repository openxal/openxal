/**
 * TestScadaStruct.java
 *
 * @author Christopher K. Allen
 * @since  Mar 3, 2011
 *
 */
package xal.app.pta.test;

import static org.junit.Assert.fail;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.data.XMLDataManager;
import xal.smf.impl.WireScanner;
import xal.smf.scada.ScadaCheckConnect;
import xal.smf.scada.ScadaFieldDescriptor;
import xal.smf.scada.ScadaRecord;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test cases for the <code>ScadaStruct</code> class.
 *
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * @author Christopher K. Allen
 * @since   Mar 3, 2011
 */
public class TestScadaStruct {

    
    /** The wire scanner accelerator node we are using for tests */
    public static final String          STR_NODE_ID = "TEST_Diag:WS006"; //$NON-NLS-1$

    /** The test wire scanner device with the prescribed ID */
    private static WireScanner          SMF_WS;
    
//    /** The default XAL accelerator object */
//    private static Accelerator          SMF_ACCEL;
    
    
    /** Array of SCADA data structure to test for connectivity */
    private final static List<Class<? extends ScadaRecord> > LST_TYPE_CONNCHK = new LinkedList<Class<? extends ScadaRecord>>();
        

    
    
    /**
     * Create the XAL accelerator and get the test wire scanner device.
     *
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Mar 3, 2011
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LST_TYPE_CONNCHK.add(WireScanner.DevStatus.class);
        LST_TYPE_CONNCHK.add(WireScanner.ScanConfig.class);
        LST_TYPE_CONNCHK.add(WireScanner.ActrConfig.class);
        LST_TYPE_CONNCHK.add(WireScanner.SmplConfig.class);
        LST_TYPE_CONNCHK.add(WireScanner.PrcgConfig.class);
        LST_TYPE_CONNCHK.add(WireScanner.TrgConfig.class);
        
        Accelerator     SMF_ACCEL = XMLDataManager.loadDefaultAccelerator();
        
        AcceleratorNode smfNode = SMF_ACCEL.getNode(STR_NODE_ID);
        if ( !(smfNode instanceof WireScanner) ) {
            fail(smfNode.getId() + " is not a wire scanner"); //$NON-NLS-1$
            return;
        }
        
        SMF_WS = (WireScanner)smfNode;
    }

    /**
     * gov.sns.apps.pta.test
     *
     * @author Christopher K. Allen
     * @since  Mar 3, 2011
     *
     */

    /**
     *
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Mar 3, 2011
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test method for {@link xal.smf.scada.ScadaFieldDescriptor#makeFieldDescriptor(String, Class)}.
     */
    @Test
    public void testMakeFieldDescriptor() {
        try {
            ScadaFieldDescriptor     fd = ScadaFieldDescriptor.makeFieldDescriptor("limFor", WireScanner.DevStatus.class); //$NON-NLS-1$
            
            System.out.println("Field Descriptor = " + fd); //$NON-NLS-1$
            
        } catch (SecurityException e) {
            e.printStackTrace();
            fail("Private or protected field"); //$NON-NLS-1$
            
        }
    }

    /**
     * Test method for {@link xal.smf.scada.ScadaFieldDescriptor#makeFieldDescriptor(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testGetFieldDescriptor() {
    }

    /**
     * Test method for {@link xal.smf.scada.ScadaFieldDescriptor#makeFieldDescriptorList(java.lang.Class)}.
     */
    @Test
    public void testGetFieldDescriptorList() {
    }

    /**
     * Test method for {@link xal.smf.scada.ScadaFieldDescriptor#makeFieldDescriptorArray(java.lang.Class)}.
     */
    @Test
    public void testGetFieldDescriptors() {
    }

    /**
     * Use the <code>ScadaCheckConnect</code> class.
     */
    @Test
    public void testConnectionClass()  {
        System.out.println("\nConnectionCheck Class: Connections to " + SMF_WS + ":"); //$NON-NLS-1$ //$NON-NLS-2$
        
        boolean     bolResult = true;
        @SuppressWarnings("deprecation")
        ScadaCheckConnect      tstConn = new ScadaCheckConnect(SMF_WS);
        
        for (Class<? extends ScadaRecord> clsScada : LST_TYPE_CONNCHK) {
            
            @SuppressWarnings("deprecation")
            boolean bolConnect = tstConn.testConnection(clsScada, 1.0);
            
            System.out.println(clsScada + "reports " + bolConnect); //$NON-NLS-1$
            
            bolResult &= bolConnect;
        }
        
        
        if (!bolResult)
            fail("Connection failure to " + SMF_WS + " before timeout"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
//    /**
//     * Test method for {@link xal.smf.scada.ScadaStruct#testConnection(java.lang.Class, xal.smf.AcceleratorNode, double)}.
//     */
//    @Test
//    public void testTestConnection() {
//
//        System.out.println("\nScadaPacket Class: Connections to " + SMF_WS + ":");
//        
//        boolean     bolResult = true;
//        for (Class<? extends ScadaStruct> clsScada : LST_TYPE_CONNCHK) {
//            boolean bolConnect = ScadaStruct.testConnection(clsScada, SMF_WS, 1.0);
//            System.out.println(clsScada + "reports " + bolConnect);
//            
//            bolResult &= bolConnect;
//        }
//        
//        
//        if (!bolResult)
//            fail("Connection failure to " + SMF_WS + " before timeout");
//    }


}
