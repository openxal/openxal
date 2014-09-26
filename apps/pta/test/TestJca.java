/**
 * TestJca.java
 *
 *  Created	: Jun 19, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.test;

import static org.junit.Assert.fail;
import gov.aps.jca.CAException;
import gov.aps.jca.Context;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.Monitor;
import gov.aps.jca.TimeoutException;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.IEventSinkValue;
import xal.ca.MonitorException;
import xal.ca.PutException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.NoSuchChannelException;
import xal.smf.data.XMLDataManager;
import xal.smf.impl.WireScanner;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cosylab.epics.caj.CAJContext;

/**
 *
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * @since  Jun 19, 2009
 * @author Christopher K. Allen
 */
public class TestJca {
    
    
    /** The wire scanner accelerator node we are using for tests */
    public static final String          STR_NODE_ID = "TEST_Diag:WS006";
    
    /** PV of the XAL channel used to check a CA_GET connection */
    public static final String          STR_PV_GET = STR_NODE_ID + ":Ver_trace_raw";
    
    /** PV of the XAL channel used to check a CA_PUT connection */
    public static final String          STR_PV_SET = STR_NODE_ID + ":Scan_InitialMove_set";

    
    /**
     *
     *
     * @since  Nov 4, 2009
     * @author Christopher K. Allen
     */
    class JcaMonitorSink implements IEventSinkValue {

        /**
         *
         * @since 	Nov 4, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.ca.IEventSinkValue#eventValue(xal.ca.ChannelRecord, xal.ca.Channel)
         */
        public void eventValue(ChannelRecord record, Channel chan) {
            String      strPvName = chan.channelName();
            int         intVal    = record.intValue();
            
            System.out.println("Monitor event for PV " + strPvName + ", value = " + intVal);
        }
        
    }

    
    
    /**
     * setUpBeforeClass
     *
     * @throws java.lang.Exception
     * 
     * @since  Jun 19, 2009
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
     * @since  Jun 19, 2009
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
     * @since  Jun 19, 2009
     * @author Christopher K. Allen
     */
    @Before
    public void setUp() throws Exception {
//        System.out.println("setting up test " + this.getClass().getName());
    }

    /**
     * tearDown
     *
     * @throws java.lang.Exception
     * 
     * @since  Jun 19, 2009
     * @author Christopher K. Allen
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Print out Java virtual machine properties
     *
     * 
     * @since  Nov 4, 2009
     * @author Christopher K. Allen
     */
    @Test
    public void testSystem() {
        System.out.println();
        System.out.println("Java Virtual Machine Properties");
        System.out.print( System.getProperties().toString() );
    }
    
    /**
     * testJcaExample - example code (slightly modified) on 
     * APS's web site 
     * <a href=http://www.aps.anl.gov/bcda/jca/jca/2.1.2/tutorial.html#5>APS Example</a>.
     *
     * 
     * @since  Jun 19, 2009
     * @author Christopher K. Allen
     */
    @Test
    public void testJcaPrintInfo() {

        System.out.println();
        System.out.println();
        System.out.println("This is JCA version");
        try {
            // Get the JCALibrary instance.
            JCALibrary jca= JCALibrary.getInstance();

            // Create a single threaded context with default configuration values.
//            Context ctxt= jca.createContext(JCALibrary.JNI_SINGLE_THREADED);
            Context ctxt = jca.createContext(JCALibrary.CHANNEL_ACCESS_JAVA);

            // Display basic information about the context.
            ctxt.printInfo();

            // Create the Channel to connect to the PV.
            gov.aps.jca.Channel ch= ctxt.createChannel(STR_NODE_ID + ":Command");


            // send the request and wait 5.0 seconds for the channel to connect to the PV.
            ctxt.pendIO(5.0);

            // If we're here, then everything went fine.
            // Display basic information about the channel.
            ch.printInfo();


            // Disconnect the channel.
            ch.destroy();

            // Destroy the context.
            ctxt.destroy();


        } catch(Exception ex) {
            System.err.println(ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test method for {@link gov.aps.jca.JCALibrary#printInfo()}.
     */
    @Test
    public final void testCajPrintInfo() {
        System.out.println();
        System.out.println();
        System.out.println("This is the CAJ version");
        
        CAJContext ctxt = new CAJContext();
        
        
        // Display basic information about the context.
        ctxt.printInfo();

        // Create the Channel to connect to the PV.
        gov.aps.jca.Channel ch;
        try {
            ch = ctxt.createChannel(STR_NODE_ID + ":Command");
            
            // send the request and wait 5.0 seconds for the channel to connect to the PV.
            ctxt.pendIO(5.0);

            // If we're here, then everything went fine.
            // Display basic information about the channel.
            ch.printInfo();


            // Disconnect the channel.
            ch.destroy();

            // Destroy the context.
            ctxt.destroy();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            fail("CAJ IllegalArgumentException ");
            
        } catch (IllegalStateException e) {
            e.printStackTrace();
            fail("CAJ IllegalStateException ");
            
        } catch (CAException e) {
            e.printStackTrace();
            fail("CAJ CAException ");
            
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail("CAJ TimeoutException ");
        }
    }
    
    
    /**
     * Test method for {@link gov.aps.jca.JCALibrary#printInfo()}.
     */
    @Test
    public final void testXalChannel() {
//        String  strPvNmGet = "SCL_Diag:WS007:Ver_point_sig";
//        String  strPvNmGet = STR_NODE_ID + ":Motion_Speed_Init_rb";
        String strPvNmGet = STR_PV_GET;
        
//        String  strPvNmGet = "HEBT_Diag:WS09:Hor_trace_raw";
//        String  strPvNmSet = STR_NODE_ID + ":Scan_InitialMove_set";
        String strPvNmSet = STR_PV_SET;
        
        System.out.println();
        System.out.println();
        System.out.println("Testing XAL connection to PV " + strPvNmGet);
        
        
        // Create the Channel to connect to the PV.
        ChannelFactory  factory = ChannelFactory.defaultFactory();
        Channel         chan = factory.getChannel(strPvNmGet);
        
        try {
        
            double     dblVal = chan.getValDbl();
            
            System.out.println("The current value of " + strPvNmGet + " = " + dblVal);
            
            dblVal = chan.lowerControlLimit().doubleValue();
            System.out.println("The current lower display limit is " + dblVal);
            
            dblVal = chan.upperControlLimit().doubleValue();
            System.out.println("The current upper display limit is " + dblVal);
            
            dblVal = chan.lowerAlarmLimit().doubleValue();
            System.out.println("The current lower alarm limit is " + dblVal);
            
            dblVal = chan.upperAlarmLimit().doubleValue();
            System.out.println("The current upper alarm limit is " + dblVal);
            
            Channel chput = factory.getChannel(strPvNmSet);
            dblVal = chan.getValDbl();
            chput.putVal(dblVal);

        } catch (ConnectionException e) {
            System.err.println("ERROR: Unable to connect to " + strPvNmGet);
            e.printStackTrace();
            fail("ERROR: Unable to connect to " + strPvNmGet);
            
        } catch (GetException e) {
            System.err.println("ERROR: general caget exception for " + strPvNmGet);
            e.printStackTrace();
            fail("ERROR: general caget exception for " + strPvNmGet);
            
        } catch (PutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Test method for {@link gov.aps.jca.JCALibrary#printInfo()}.
     */
    @Test
    public final void testStrokeChannel() {
//        String  strPvNmGet = "SCL_Diag:WS007:Ver_point_sig";
        String  strPvNmGet = STR_NODE_ID + ":Stroke";
        
        System.out.println();
        System.out.println();
        System.out.println("Testing XAL connection to PV " + strPvNmGet);
        
        
        // Create the Channel to connect to the PV.
        ChannelFactory  factory = ChannelFactory.defaultFactory();
        Channel         chan = factory.getChannel(strPvNmGet);
        
        try {
        
            double     dblVal = chan.getValDbl();
            
            System.out.println("The current value of " + strPvNmGet + " = " + dblVal);
            
            dblVal = chan.lowerControlLimit().doubleValue();
            System.out.println("The current lower display limit is " + dblVal);
            
            dblVal = chan.upperControlLimit().doubleValue();
            System.out.println("The current upper display limit is " + dblVal);
            
            dblVal = chan.lowerAlarmLimit().doubleValue();
            System.out.println("The current lower alarm limit is " + dblVal);
            
            dblVal = chan.upperAlarmLimit().doubleValue();
            System.out.println("The current upper alarm limit is " + dblVal);
            

        } catch (ConnectionException e) {
            System.err.println("ERROR: Unable to connect to " + strPvNmGet);
            e.printStackTrace();
            fail("ERROR: Unable to connect to " + strPvNmGet);
            
        } catch (GetException e) {
            System.err.println("ERROR: general caget exception for " + strPvNmGet);
            e.printStackTrace();
            fail("ERROR: general caget exception for " + strPvNmGet);
            
        }
    }
    
    
    /**
     * Test the ability to get Live Data from the
     * wire scanner.
     *
     * 
     * @since  Feb 5, 2010
     * @author Christopher K. Allen
     */
    @Test
    public final void textWireScannerData() {
        Accelerator     accel = XMLDataManager.loadDefaultAccelerator();
        
        AcceleratorNode smfNode = accel.getNode(STR_NODE_ID);
        if ( !(smfNode instanceof WireScanner) ) {
            fail(smfNode.getId() + " is not a wire scanner");
            return;
        }
        WireScanner     ws = (WireScanner)smfNode;
            
        try {
            WireScanner.ActrConfig data = WireScanner.ActrConfig.aquire(ws);
            System.out.println();
            System.out.println("WireScanner.DataLive data structure:");
            System.out.print(data.toString());
            
        } catch (ConnectionException e) {
            fail("Could not retrieve data from " + ws.getId());
            e.printStackTrace();
            
        } catch (GetException e) {
            fail("Could not retrieve data from " + ws.getId());
            e.printStackTrace();
            
        } catch (NoSuchChannelException e) {
            fail("Could not retrieve data from " + ws.getId());
            e.printStackTrace();
            
        }
    }
    
    /**
     * testXalMonitor
     *
     * 
     * @since  Nov 4, 2009
     * @author Christopher K. Allen
     */
//    @Test
    public final void testXalMonitor() {
        String  strPvName = STR_NODE_ID + ":Ver_point_sig";
        
        System.out.println();
        System.out.println();
        System.out.println("Testing XAL monitor on PV " + strPvName);
        
        
        // Create the Channel to connect to the PV.
        ChannelFactory  factory = ChannelFactory.defaultFactory();
        Channel         chan = factory.getChannel(strPvName);
        
        try {
            JcaMonitorSink     sink = new JcaMonitorSink();
            xal.ca.Monitor monitor = chan.addMonitorValue(sink, Monitor.VALUE);
            
        } catch (ConnectionException e) {
            System.err.println("ERROR: XAL Connection exception " + strPvName); 
            e.printStackTrace();
            fail("ERROR: XAL Connection exception " + strPvName);
            
        } catch (MonitorException e) {
            System.err.println("ERROR: XAL monitor exception " + strPvName); 
            e.printStackTrace();
            fail("ERROR: XAL monitor exception " + strPvName);
            
        }
    }
    /**
     * testXalMonitor
     *
     * 
     * @since  Nov 4, 2009
     * @author Christopher K. Allen
     */
//    @Test
    public final void testProfileScan() {
        int     intWaitTm = 2;         
        String  strSigName = STR_NODE_ID + ":WS007:Command";
        String  strMonName = STR_NODE_ID + ":WS007:MotionStat";
        
        System.out.println();
        System.out.println();
        System.out.println("Testing Profile Scan using " + strSigName);
        System.out.println("  monitoring on PV " + strMonName);
        
        
        // Create the Channel to connect to the PV.
        ChannelFactory  factory = ChannelFactory.defaultFactory();
        Channel         chanCmd = factory.getChannel(strSigName);
        Channel         chanMon = factory.getChannel(strMonName);
        
        try {
            chanCmd.connectAndWait(10);
            chanMon.connectAndWait(10);
            
            JcaMonitorSink     sink = new JcaMonitorSink();
            xal.ca.Monitor monitor = chanMon.addMonitorValue(sink, Monitor.VALUE);
            
            chanCmd.putVal(3);

            System.out.println("  Scan started.");  
            System.out.println("  Sleeping for " + intWaitTm + " minutes while waiting for monitor events");
            for (int iMinute=1; iMinute<=intWaitTm; iMinute++) {
                Thread.sleep(60000);
                System.out.println("    " + iMinute  + " minute(s)");
            }
            
            System.out.println("Awakened.  Returning from scan test");
            System.out.println("  (NOTE: scan may still be in progress)");
            monitor.clear();
            
        } catch (ConnectionException e) {
            System.err.println("ERROR: XAL Connection exception " + strSigName); 
            e.printStackTrace();
            fail("ERROR: XAL Connection exception " + strSigName);
            
        } catch (PutException e) {
            System.err.println("ERROR: Profile Scan exception " + strSigName); 
            e.printStackTrace();
            fail("ERROR: Profile Scann exception " + strSigName);
            
        } catch (MonitorException e) {
            System.err.println("ERROR: Profile Scan monitoring exception " + strMonName); 
            e.printStackTrace();
            fail("ERROR: Profile Scann monitoring exception " + strMonName);
            
        } catch (InterruptedException e) {
            System.err.println("Scanning sleep thread interrupted while scanning");
            e.printStackTrace();
            fail("Scanning sleep thread interrupted while scanning");
        }
    }


    
    /**
     * Test method for {@link gov.aps.jca.JCALibrary#createContext(java.lang.String)}.
     */
    @Test
    public final void testCreateContextString() {
//        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link gov.aps.jca.JCALibrary#createContext(gov.aps.jca.configuration.Configuration)}.
     */
    @Test
    public final void testCreateContextConfiguration() {
//        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link gov.aps.jca.JCALibrary#createServerContext(java.lang.String, gov.aps.jca.cas.Server)}.
     */
    @Test
    public final void testCreateServerContextStringServer() {
//        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link gov.aps.jca.JCALibrary#createServerContext(gov.aps.jca.configuration.Configuration, gov.aps.jca.cas.Server)}.
     */
    @Test
    public final void testCreateServerContextConfigurationServer() {
//        fail("Not yet implemented"); // TODO
    }

}
