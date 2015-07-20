/**
 * ScadaCheckConnect.java
 *
 * @author  Christopher K. Allen
 * @since	Mar 4, 2011
 */
package xal.smf.scada;

import xal.ca.Channel;
import xal.ca.ConnectionListener;
import xal.smf.AcceleratorNode;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is used to test the connections between
 * a SCADA structure and an <code>AcceleratorNode</code>
 * object.  This is done without sending any data (i.e.,
 * a <tt>caget</tt> or <tt>caput</tt> command.  Moreover,
 * all the connections are tested in batch and the timeout
 * period is specified by the user (i.e., it is not the default
 * channel access timeout).
 * 
 * @deprecated      This class is replaced by <code>{@link BatchConnectionTest}</code>
 *
 * @author Christopher K. Allen
 * @since   Mar 4, 2011
 */
@Deprecated
public class ScadaCheckConnect {

    
    /**
     * This class is used is a Channel Access connection
     * monitor.  When Channel Access calls to report a channel
     * connection, the monitor removes the channel from the list
     * of channels requesting confirmation (this list was passed
     * in at construction time).
     * 
     * @author Christopher K. Allen
     * @since   Feb 4, 2011
     */
    public final static class ConnectionMonitor implements ConnectionListener {

        /*
         * Local Attributes
         */
        
        /** the channel we are monitoring */
        final private Channel             chnFld;
        
        /** list of channels that have had a connection request issued */
        final private TestChannelList     lstReqs;
        
       
        /*
         * Initialization
         */
        
        /**
         * Create a new <code>ConnectionMonitor</code> object for monitoring
         * the given channel.  The list of all channels being monitored is all
         * passed so that the given channel can be removed upon confirmation.
         * 
         * @param chnFld   EPICS channel connecting to a field within the parameter set.
         * @param lstReqs   list of channels that have been requested to connect
         *
         * @author  Christopher K. Allen
         * @since   Feb 4, 2011
         */
        public ConnectionMonitor(final Channel chnFld, final TestChannelList lstReqs) {
            this.chnFld = chnFld;
            this.lstReqs = lstReqs;
        }
        
        /**
         * Just in case, we remove all the links.
         * 
         * @since Mar 4, 2011
         * @see java.lang.Object#finalize()
         */
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            
            this.chnFld.removeConnectionListener(this);
            this.lstReqs.remove(this.chnFld);
        }
        
        
        /*
         * Operations
         */
        
        /**
         * Get the channel this monitor is servicing
         * 
         * @return  channel that this monitor is connected to
         */
        public Channel  getChannel() {
            return this.chnFld;
        }

        
        /*
         * Connection Listener Interface
         */
        
        /**
         * <p>
         * The requested connection was made.  Consequently, 
         * <br>
         * &nbsp; &sdot; We remove ourself from the channel's set of connection listeners
         * <br>
         * &nbsp; &sdot; We remove the channel from the list of open requests
         * </p>
         * 
         * @since Feb 4, 2011
         * @see xal.ca.ConnectionListener#connectionMade(xal.ca.Channel)
         */
        @Override
        public void connectionMade(Channel channel) {
            this.chnFld.removeConnectionListener(this);
            this.lstReqs.remove(this.chnFld);
        }

        /**
         * Nothing really to do here - as a precaution we remove ourself from
         * the channel's set of connection listeners.  We should have been removed
         * when the connection request was fulfilled.   However, something wrong
         * could have happened.
         * 
         * @since Feb 4, 2011
         * @see xal.ca.ConnectionListener#connectionDropped(xal.ca.Channel)
         */
        @Override
        public void connectionDropped(Channel channel) {
            this.chnFld.removeConnectionListener(this);
        }
    }
    
    
    /**
     * List of channels under test.  Access to the internal list is synchronized
     * for batch operation of channel access connection monitor objects.
     * 
     *
     * @author Christopher K. Allen
     * @since   Mar 7, 2011
     */
    public final static class TestChannelList {
        
        /*
         * Local Attributes
         */
        
        /** List of channels pending connect */
        private final List<Channel>     lstChnPend;
        
        /** List of channels passing connection check */
        private final List<Channel>     lstChnPass;
        
        /** List of all the connection listeners (needed to remove from channels) */
        private final List<ConnectionMonitor>  lstLsnCon;
        
        /** The primary application thread */
        private Thread                  thdCurr;

        
        
        /*
         * Initialization
         */
        
        /**
         * Create a new, empty <code>TestChannelList</code>
         * object.
         *
         * @author  Christopher K. Allen
         * @since   Mar 7, 2011
         */
        public TestChannelList() {
            this.lstChnPend  = new LinkedList<Channel>();
            this.lstChnPass  = new LinkedList<Channel>();
            this.lstLsnCon   = new LinkedList<ConnectionMonitor>();
        }
        
        
        /**
         * Returns the size of the list, i.e., the number
         * of unconnected channels in the list.
         *
         * @return  number of unconnected channels in list
         *
         * @author Christopher K. Allen
         * @since  Mar 7, 2011
         */
        public int sizePending() {
            return this.lstChnPend.size();
        }
        
        /** 
         * Returns the number of the channels that passed the connection
         * test.
         * 
         * @return  number of connected channels
         */
        public int sizeConnected() {
            return this.lstChnPass.size();
        }
        
        /**
         * Returns the list of channels which never connected,
         * that is, they are still pending.
         * 
         * @return  all the channels that failed to connect
         *
         * @author Christopher K. Allen
         * @since  Mar 10, 2011
         */
        public List<Channel>    getPending() {
            return this.lstChnPend;
        }
        
        //
        //  Operations
        //
        
        /**
         * Add the given channel to the list.
         *
         * @param chn   new list channel
         *
         * @author Christopher K. Allen
         * @since  Mar 7, 2011
         */
        public void add(Channel chn) {
            synchronized (this.lstChnPend) {
                if (this.lstChnPend.contains(chn))
                    return;
                
                if (chn.isConnected())
                    return;
                
                ConnectionMonitor   monConn = new ConnectionMonitor(chn, this);
                chn.addConnectionListener(monConn);
                this.lstChnPend.add(chn);
                this.lstLsnCon.add(monConn);
            }
        }
        
        /**
         * Removes the given channel from the the channel
         * list.  If there are not more channels in the
         * list we send an interrupt to the main thread.
         *
         * @param chn   channel to be removed
         *
         * @author Christopher K. Allen
         * @since  Mar 7, 2011
         */
        public void remove(Channel chn) {
            synchronized (this.lstChnPend) {
                this.lstChnPend.remove(chn);
                this.lstChnPass.add(chn);
                
                if (this.lstChnPend.size() == 0)
                    this.thdCurr.interrupt();
            }
        }
        
        /**
         * Sends a "connection request" to Channel
         * Access for all the channels within the list.  If
         * all connection monitors return before the timeout
         * period the method {@link #remove(Channel)} will
         * issue an interrupt waking up the current thread.
         * 
         * @param dblTmOut  time (in seconds) to wait before connection attempt is aborted
         *  
         * @return  <code>true</code> if all connections were made in the alloted time, 
         *          false if otherwise
         *
         * @author Christopher K. Allen
         * @since  Mar 7, 2011
         */
        public boolean testChannelConnects(double dblTmOut) {
            Double      dblMsec  = new Double(dblTmOut*1000.0);
            int         intTmOut = dblMsec.intValue();
            
            synchronized (this.lstChnPend) {
                this.thdCurr = Thread.currentThread();
                this.lstChnPass.clear();
                
                for (Channel chnReq : this.lstChnPend) {

                    chnReq.requestConnection();
                }
            }
            
            try {
                Thread.sleep(intTmOut);

//                // We must stop all the connection monitor from remove the
//                //  channels after the failed connection attempt
//                synchronized (this.lstChnPend) {
//                    for (ConnectionMonitor mon : this.lstLsnCon) {
//                        Channel     chn = mon.getChannel(); 
//                        chn.removeConnectionListener(mon);
//                    }
//
//                }
                return false;
                
            } catch (InterruptedException e) {
                
                return true;
                
            }
        }
    }
    
    

    
    /*
     * Local Attributes
     */
    
    /** The accelerator device under test */
    private final AcceleratorNode           smfDev;
    
    /** List of failed channels */
    private List<Channel>            lstChnFail;
    
    
//    /** The time out period after which connection test fails (in seconds) */
//    private final double                    dblTmOut;
    
    
    /*
     * Initialization
     */
    
//    /**
//     * Create a new <code>ScadaCheckConnect</code> object connected
//     * to the given accelerator device.
//     * 
//     * @param smfDev        accelerator device under test
//     * @param dblTmOut    time to wait before test fails (in seconds)
//     *
//     * @author  Christopher K. Allen
//     * @since   Mar 4, 2011
//     */
//    public ScadaCheckConnect(AcceleratorNode smfDev, double dblTmOut) {
//        this.smfDev = smfDev;
//        this.dblTmOut = dblTmOut;
//    }
    
    /**
     * Create a new <code>ScadaCheckConnect</code> object connected
     * to the given accelerator device.
     * 
     * @param smfDev        accelerator device under test
     *
     * @author  Christopher K. Allen
     * @since   Mar 4, 2011
     */
    public ScadaCheckConnect(AcceleratorNode smfDev) {
        this.smfDev = smfDev;
        this.lstChnFail = null;
    }
    
    
    
    /**
     * Test the connections in all the channels in this DAQ data structure for the
     * given accelerator device.  The test will wait up to the given length
     * of time before declaring failure.
     *
     * @param clsScada      the SCADA data structure defining the connection channels
     * @param dblTmOut      time to wait for establishing connection
     * 
     * @return              <code>true</code> if all connections were successful,
     *                      <code>false</code> if not all connection were made within given time
     *                      
     * @throws BadStructException      the data structure is not annotated by <code>AScada</code>
     *
     * @author Christopher K. Allen
     * @since  Feb 4, 2011
     */
    public synchronized boolean  testConnection(Class<?> clsScada, double dblTmOut) 
        throws BadStructException 
    {
        
        // Check for no test
        if (dblTmOut <= 0.0)
            return true;
        
//        // Check that data structure is tagged for channel access
//        if (! clsScada.isAnnotationPresent(AScada.class) )
//            throw new BadStructException("The data structure is not annotated as 'AScada'");
        
        // Here is where we store all the requests
        TestChannelList         lstRequests = new TestChannelList( );
        
        // Request connection for all the channels in the DAQ data structure
        Field[]             arrFld      = clsScada.getFields();
        
        for (Field fld : arrFld) {
            AScada.Field     annFld = fld.getAnnotation(AScada.Field.class);
            if (annFld == null) 
                continue;
            
            String          strHndRb  = annFld.hndRb();
            String          strHndSet = annFld.hndSet();
            boolean         bolPvCtrl = annFld.ctrl();
            
            this.loadChannel(this.smfDev, strHndRb, lstRequests);
            if (bolPvCtrl)
                this.loadChannel(this.smfDev, strHndSet, lstRequests);
            
        }
        
        if (lstRequests.sizePending() == 0) 
            return true;
        
        if (lstRequests.testChannelConnects(dblTmOut) == false) {

            this.lstChnFail = lstRequests.getPending();
            
            return false;
        }
        
        return true;
    }
    
    /**
     * Returns the list of channels that failed to connect.
     *
     * @return  channels not passing the connection test
     *
     * @author Christopher K. Allen
     * @since  Mar 10, 2011
     */
    public List<Channel>    getFailedChannels() {
        return this.lstChnFail;
    }
    
    
    /*
     * Support Methods
     */
    
    /**
     * <p>
     * Retrieves the given channel from the accelerator device (by the given
     * <em>channel handle</em>.  Then,
     * we create a <code>ConnectionMonitor</code> object for the given channel.
     * The channel is added to the list of requested channel connects then
     * a request is made for connection.
     * </p>
     * <p>
     * If the channel is already connected the method simply returns.
     * </p>
     *
     * @param smfDev        the device containing the channel
     * @param strHnd        the XAL channel handle for the channel under test
     * @param lstRequests   the channel is added to this request connect list 
     *
     * @throws BadStructException  the given device does not contain a requested channel
     *  
     * @author Christopher K. Allen
     * @since  Feb 4, 2011
     */
    private void loadChannel(AcceleratorNode smfDev, String strHnd, TestChannelList lstRequests) 
        throws BadStructException
    {
        // Retrieve the channel object from the accelerator device
        Channel     chnReq = smfDev.findChannel(strHnd);
    
        if (chnReq == null) {
            String strMsg = "No channel " + strHnd  //$NON-NLS-1$
            + " on device " + smfDev.getId(); //$NON-NLS-1$
            throw new BadStructException(strMsg);
    
        }
    
        // If the channel is already connected there is nothing to do
        if (chnReq.isConnected())
            return;
    
        // We are going to request connection for this channel
        lstRequests.add(chnReq);
    }
    
    
}
