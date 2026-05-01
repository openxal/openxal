/**
 * BatchChechConnect.java
 *
 * @author Christopher K. Allen
 * @since  Mar 11, 2011
 *
 */

package xal.smf.scada;

import xal.ca.BadChannelException;
import xal.ca.Channel;
import xal.ca.ConnectionListener;
import xal.smf.AcceleratorNode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for checking the connectivity of many EPICS channels
 * simultaneously.
 * 
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Mar 11, 2011
 */
public class BatchConnectionTest {

    
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
    public final class ConnectionMonitor implements ConnectionListener {

        /*
         * Local Attributes
         */
        
       
        /*
         * Initialization
         */
        
        /**
         * Create a new <code>ConnectionMonitor</code> object for monitoring
         * the given channel.  The list of all channels being monitored is all
         * passed so that the given channel can be removed upon confirmation.
         * 
         * @author  Christopher K. Allen
         * @since   Feb 4, 2011
         */
        public ConnectionMonitor() {
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
        @SuppressWarnings("synthetic-access")
        public void connectionMade(Channel channel) {
            channel.removeConnectionListener(this);
            connectionAcknowledged(channel);
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
            channel.removeConnectionListener(this);
        }
    }
    
    
    
    /*
     * Local Attributes
     */
    
    
    /** The hardware device we are checking connections to */
    private final AcceleratorNode           smfDev;
    
    
    /** List of the PV channels that will be tested */
    private final Set<Channel>              setPending;
    
    /** List of channels that passed connection test */
    private final Set<Channel>              setPassed;
    
    /** Lock used to synchronize the channel sets */
    private final Object                    objLock;
    
    
    /** Current process thread for batch testing */
    private Thread                          thdCurr;

    
    /** Turn the connection checking on or off */
    private boolean                         bolChecking;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Creates a new <code>BatchConnectionTest</code> object
     * for the given accelerator device.
     * 
     * @param smfDev    accelerator hardware under test
     *
     * @author  Christopher K. Allen
     * @since   Mar 11, 2011
     */
    public BatchConnectionTest(AcceleratorNode smfDev) {
        this.smfDev = smfDev;
        
        this.setPassed  = new HashSet<Channel>();
        this.setPending = new HashSet<Channel>();
        this.objLock    = new Object();
        
        this.bolChecking = true;
    }
    
    /**
     * Force the connection checking on or off. If connection checking is turned
     * off the checking methods (<code>{@link #testConnection(Class, double)}</code>
     * and <code>{@link #testConnection(Collection, double)}</code>) perform 
     * no operation and return a value of <code>true</code>.
     *
     * @param bolChecking   if <code>true</code> then connection checking continues as normal
     *                      if <code>false</code> then connection checking is turned off regardless
     *
     * @author Christopher K. Allen
     * @since  Mar 28, 2011
     */
    public void setChecking(boolean bolChecking) {
        this.bolChecking = bolChecking;
    }
    
    
    /*
     * Operations
     */
    
    /**
     *
     * Test the given (implied) SCADA class for channel connectivity of .
     * its SCADA fields All the channels for the set of PVs are identified
     * (i.e., read back, set, etc.) then the connections are tested
     * in batch mode, rather than serially.  Thus, the given timeout
     * until failure applies to all the channels at once.
     * 
     * @param clsScada  class type of the SCADA data structure under test
     * @param dblTmOut  timeout before connectivity test fails (in seconds)
     * 
     * @return          <code>true</code> if all connections are available,
     *                  <code>false</code> otherwise
     * 
     * @throws BadStructException  the given class has no SCADA field annotations
     * @throws BadChannelException  bad channel handle, no channel bound to it
     * 
     * @see BatchConnectionTest#testConnection(Collection, double)
     *
     * @author Christopher K. Allen
     * @since  Mar 16, 2011
     */
    public synchronized boolean testConnection(Class<?> clsScada, double dblTmOut) 
        throws BadStructException, BadChannelException 
    {
        ScadaFieldList  lstFldDescr = new ScadaFieldList(clsScada);
        
        if (lstFldDescr.size() == 0)
            throw new BadStructException("Class contains no SCADA field annotations");
        
        return this.testConnection(lstFldDescr, dblTmOut);
    }
    
    /**
     * Test the given set of PV field descriptors for channel
     * connectivity.  All the channels for the set of PVs are identified
     * (i.e., read back, set, etc.) then the connections are tested
     * in batch mode, rather than serially.  Thus, the given timeout
     * until failure applies to all the channels at once.
     * 
     * @param setFds    set of field descriptors
     * @param dblTmOut  timeout before connectivity test fails (in seconds)
     * 
     * @return  <code>true</code> if all channels described in the list are connected,
     *          <code>false</code> otherwise
     *  
     * @throws  BadChannelException         bad channel handle, no channel bound to it
     * @throws  IllegalArgumentException    the given collection of descriptors is empty
     * 
     * @author Christopher K. Allen
     * @since  Mar 11, 2011
     */
    public synchronized boolean testConnection(Collection<ScadaFieldDescriptor> setFds, double dblTmOut) 
        throws BadChannelException, IllegalArgumentException 
    {
        // Check for empty list
        if (setFds.size() == 0) 
            throw new IllegalArgumentException("Empty field descriptor list.");
     
        // Make sure connection checking is turned on
        if (!this.bolChecking)
            return true;
        
        // Take out the trash
        synchronized (this.objLock) {
            this.setPending.clear();
            this.setPassed.clear();
        }
        
        // Store all the channels for connectivity test
        for (ScadaFieldDescriptor fd : setFds) {
            
            String          strHndRb  = fd.getRbHandle();
            String          strHndSet = fd.getSetHandle();
            boolean         bolPvCtrl = fd.isControllable();
            
            this.addChannel(strHndRb);
            if (bolPvCtrl)
                this.addChannel(strHndSet);
        }
        
        // Check if all channels are already connected
        synchronized (this.objLock) {
            if (this.setPending.size()==0)
                return true;
        }
        
        // We launch all the connection requests from a critical code
        //      but first we have to convert from seconds (double) to nanoseconds (int)
        Double      dblMsec  = new Double(dblTmOut*1000.0);
        int         intTmOut = dblMsec.intValue();
        synchronized (this.objLock) {
            this.thdCurr = Thread.currentThread();  // save the current execution thread
            
            //  Launch the connection request
            for (Channel chnReq : this.setPending) {

                chnReq.requestConnection();
            }
        }
        
        // Once all the connection requests are launched, sleep until either ...
        try {
            Thread.sleep(intTmOut);

            //  we awake normally, meaning not all channels connected, or
            return false;
            
        } catch (InterruptedException e) {
            
            //  a) we are interrupted (by a the last connecting channel) 
            return true;
            
        }
        
    }
    
    /**
     * Returns a copy of the current set of channels that passed the connectivity
     * test (even if some channels in the original batch failed).
     *
     * @return  (copy) set channels from the original batch that we able to connect
     *
     * @author Christopher K. Allen
     * @since  Mar 11, 2011
     */
    public Set<Channel>    getConnectedChannels() {
        Set<Channel>    setConnected = new HashSet<Channel>();
        
        synchronized (this.objLock) {
            setConnected.addAll(this.setPassed);
        }
        
        return  setConnected;
    }
    
    
    /*
     * Internal Support
     */

    
    /**
     * <p>
     * Add the given channel to the list of channels pending 
     * connectivity test.
     * </p>
     * <h3>NOTE:</h3>
     * <p>
     * &mdot; Only channel that are not previously connected
     * are added to the list.  That is, if the 
     * <code>@link Channel#isConnected}</code> returns true then
     * the channel is not added.
     * </p>
     *
     * @param strHnd   handle of channel to be added to pending list
     *
     * @throws  BadChannelException  bad channel handle, no channel bound to it
     * 
     * @author Christopher K. Allen
     * @since  Mar 7, 2011
     */
    private void addChannel(String strHnd) throws BadChannelException {
        // Retrieve the channel object from the accelerator device
        Channel     chnReq = smfDev.findChannel(strHnd);
    
        if (chnReq == null) {
            String strMsg = "Channel " + strHnd + " unbound on device " + smfDev.getId();
            throw new BadChannelException(strMsg);
    
        }
    
        // Lock out access to the channel sets
        //   check if channel is already in list
        //   check if channel is already connected
        synchronized (this.objLock) {
            
            if (chnReq.isConnected()) {
                this.setPassed.add(chnReq);
                return;
            }
            
            ConnectionMonitor   monConn = new ConnectionMonitor();
            chnReq.addConnectionListener(monConn);
            this.setPending.add(chnReq);
        }
    }
    
    /**
     * Removes the given channel from the list of 
     * channels pending connection.
     *
     * @param chn   channel to remove
     *
     * @author Christopher K. Allen
     * @since  Mar 11, 2011
     */
    private void connectionAcknowledged(Channel chn) {
        synchronized (this.objLock) {
            this.setPending.remove(chn);
            this.setPassed.add(chn);
            
            if (this.setPending.size() == 0)
                this.thdCurr.interrupt();
        }
    }
    
    
}
