/**
 * SmfPvMonitor.java
 *
 *  Created	: Dec 8, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.ca;

import xal.ca.Channel;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.IEventSinkValue;
import xal.ca.Monitor;
import xal.ca.MonitorException;
import xal.smf.AcceleratorNode;
import xal.smf.NoSuchChannelException;
import xal.smf.scada.XalPvDescriptor;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * Encapsulates Channel Access process variable monitors for 
 * XAL hardware objects, that is, classes within the package
 * <code>xal.smf.impl</code> of base type
 * <code>{@link xal.smf.AcceleratorNode}</code>.
 * </p>
 * <p>
 * The monitoring will not begin until the method 
 * <code>{@link SmfPvMonitor#begin()}</code> is called.  This
 * is in contrast to the usual CA monitor which begins monitoring
 * immediately upon creation.  The user also has the option of
 * ignoring the first monitor event, which is an initializing event
 * occurring immediately after the call to 
 * <code>{@link SmfPvMonitor#begin()}</code>.  To do so, use the
 * method <code>{@link SmfPvMonitor#begin(boolean)}</code> with 
 * argument <code>true</code>. 
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * @see xal.smf.AcceleratorNode
 * @see xal.smf.scada.XalPvDescriptor
 *
 * @since  Dec 8, 2009
 * @author Christopher K. Allen
 */
public class SmfPvMonitor implements IEventSinkValue {



    /**
     * Classes wishing to receive DAQ monitoring events from
     * this class must expose this interface in order to define
     * the call back function.
     *
     *
     * @since  Dec 8, 2009
     * @author Christopher K. Allen
     */
    public interface IAction {

        /**
         * Derived classes catch this event to respond
         * to a change in the PV value being monitored.
         *
         * @param val   the new PV value
         * @param mon   the monitor object
         * 
         * @since  Dec 7, 2009
         * @author Christopher K. Allen
         */
        public void   valueChanged(ChannelRecord val, SmfPvMonitor mon);

    }






    /*
     * Instance Attribute
     */

    /*
     * Defining Attributes
     */
    /** The hardware device being monitored */
    private final AcceleratorNode smfDev;

    /** The XAL channel handle of monitored CA channel */
    private final String           strHandle;

    /** The XAL process variable descriptor for the channel we are monitoring */
    private final XalPvDescriptor  dscrPv;


    /*
     * Channel Access Tools
     */
    /** The CA channel we are monitoring */
    private Channel          chanDevice;

    /** The CA monitor sending events the we are catching */
    private Monitor          monDevice;


    /*
     * State Variables
     */
    /** The monitoring active flag */
    private boolean          bolMonitoring;

    /** Initial value flag */
    private boolean          bolInitEvt;


    /** List of registered event actions */
    private final List<IAction>       lstActions;



    /*
     * Initialization
     */

    /**
     * Create a new <code>DaqMonitor</code> object.
     *
     * @param smfDev         the DAQ device to monitor
     * @param dscrPv         XAL process variable descriptor of monitored channel  
     *
     * @since     Dec 7, 2009
     * @author    Christopher K. Allen
     */
    public SmfPvMonitor(AcceleratorNode smfDev, XalPvDescriptor dscrPv)   {
        this.smfDev = smfDev;
        this.dscrPv = dscrPv;
        this.strHandle = dscrPv.getRbHandle();

        this.bolMonitoring = false;
        this.bolInitEvt = false;

        this.lstActions = new LinkedList<IAction>();
    }

    /**
     * Registers a new action for monitor events.
     * Specifically, these actions are invoked whenever
     * the value of the PV changes.
     *
     * @param snkEvents
     * 
     * @since  Dec 8, 2009
     * @author Christopher K. Allen
     */
    public void addAction(IAction snkEvents) {
        this.lstActions.add(snkEvents);
    }



    /*
     * Attributes
     */

    /**
     * Returns the DAQ device being monitored
     *
     * @return       XAL device 
     * 
     * @since  Dec 7, 2009
     * @author Christopher K. Allen
     */
    public AcceleratorNode        getDevice() {
        return this.smfDev;
    }

    /**
     * Returns the XAL channel handle of the
     * device's read back channel that we wish
     * to monitor.
     *
     * @return       XAL channel handle
     * 
     * @since  Dec 8, 2009
     * @author Christopher K. Allen
     */
    public String    getChannelHandle() {
        return this.strHandle;
    }

    /**
     * Returns a descriptor object
     * for the process variable we are monitoring.
     *
     * @return       Process Variable description object
     * 
     * @since  Dec 8, 2009
     * @author Christopher K. Allen
     */
    public XalPvDescriptor   getPvDescriptor() {
        return this.dscrPv;
    }

    /**
     * Get the actual Channel Access 
     * <code>Channel</code> object which
     * is being monitored.
     *
     * @return       monitored channel
     * 
     * @since  Dec 7, 2009
     * @author Christopher K. Allen
     */
    public Channel  getChannel() {
        return this.chanDevice;
    }
    
    /**
     * Returns the current monitoring condition of this monitor object.
     * 
     * @return  <code>true</code> if we are currently monitoring a PV
     *          <false>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since  Apr 30, 2014
     */
    public boolean  isMonitoring() {
        return this.bolMonitoring;
    }



    /*
     * Operations
     */


    /**
     * Begin channel monitoring catching the first (initializing)
     * event.  That is, we call <code>begin(false)</code>
     * 
//   * @return    <code>true</code> if the PV monitoring was successfully started,
//   *            <code>false</code> otherwise
     *            
     * @throws NoSuchChannelException  A monitor PV descriptor was invalid 
     * @throws ConnectionException     Unable to form to a PV channel connection
     * @throws MonitorException        Unable to create a monitor on a connected channel
     * 
     * @since  Dec 8, 2009
     * @author Christopher K. Allen
     * 
     * @see       #begin(boolean)
     */
    public void begin() throws ConnectionException, MonitorException, NoSuchChannelException  {
        this.begin(false);
    }


    /**
     * <p>
     * Begin channel monitoring.  We catch all channel access
     * exceptions and simply return a <code>false</code> value
     * if the request was unsuccessful.  The following are the
     * possible exceptions:
     * <br/>
     * <br/>@throws ConnectionException -  could not connect to channel
     * <br/>@throws NoSuchChannelException - channel handle is invalid
     * <br/>@throws MonitorException - CA monitor failed to start
     * </p>
     * 
     * @param  bolInitEvent  ignore initializing event if <code>true</code> 
     *                      (default is <code>false</code> - we catch first monitor event)
     *
//   * @return    <code>true</code> if the PV monitoring was successfully started,
//   *            <code>false</code> otherwise
     *  
     * @throws NoSuchChannelException  A monitor PV descriptor was invalid 
     * @throws ConnectionException     Unable to form to a PV channel connection
     * @throws MonitorException        Unable to create a monitor on a connected channel
     * 
     * @since  Dec 7, 2009
     * @author Christopher K. Allen
     */
    public void begin(boolean bolInitEvent) throws ConnectionException, NoSuchChannelException, MonitorException {
        // Do we ignore the initializing (first) event?
        this.bolInitEvt = bolInitEvent;

        //      try {
        this.chanDevice    = smfDev.getAndConnectChannel(this.strHandle);
        this.monDevice     = this.chanDevice.addMonitorValue(this, Monitor.VALUE);

        //    } catch (ConnectionException e) {
        //        System.err.println("ConnectionException: could not connect to channel");
        //        e.printStackTrace();
        //        return false;
        //        
        //    } catch (NoSuchChannelException e) {
        //        System.err.println("NoSuchChannelException: channel handle is invalid");
        //        e.printStackTrace();
        //        return false;
        //        
        //    } catch (MonitorException e) {
        //        System.err.println("MonitorException: CA monitor failed to start");
        //        e.printStackTrace();
        //        return false;
        //    }

        this.bolMonitoring = true;
    }


    /**
     * Discontinue monitor of the channel.
     * (No more events are fired.)
     *
     * 
     * @since  Dec 7, 2009
     * @author Christopher K. Allen
     */
    public void clear() {
        if (!this.bolMonitoring)
            return;

//        System.out.println("SmfPvMonitor#clear() - clearing monitor for " + this.dscrPv.toString());
        
//        this.chanDevice.disconnect();  // CKA April 30, 2014
//        this.chanDevice = null;

        this.monDevice.clear();
        this.monDevice = null;
        
        this.bolMonitoring = false;
    }


    /*
     * IEventSinkValue Interface
     */

    /**
     * Responds to a change in the channel's value
     * set by the CA monitor.  We call the abstract method 
     * <code>valueChanged(ChannelRecord)</code>.
     *
     * @since       Nov 4, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.ca.IEventSinkValue#eventValue(xal.ca.ChannelRecord, xal.ca.Channel)
     */
    public void eventValue(ChannelRecord record, Channel chan) {

        // Check if this is the first time through (this initializing
        //   call-back.
        //   Ignore if instructed to.
        if (this.bolInitEvt) {
            this.bolInitEvt = false;
            return;
        }

        // Notify all the listeners that the value changed.
        for (IAction snkEvent : this.lstActions) {
            snkEvent.valueChanged(record, this);
        }

    }
    
    
    /*
     * Internal Support
     */
    
    /**
     * Forces reference release of Channel
     * Access objects.
     *
     * @since   Feb 11, 2010
     * @author  Christopher K. Allen
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        this.chanDevice = null;
        this.monDevice  = null;
        this.lstActions.clear();
    }

}


