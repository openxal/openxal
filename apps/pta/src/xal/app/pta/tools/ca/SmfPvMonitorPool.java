/**
 * SmfPvMonitorPool.java
 *
 *  Created	: Dec 8, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.ca;

import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.MonitorException;
import xal.smf.AcceleratorNode;
import xal.smf.NoSuchChannelException;
import xal.smf.scada.XalPvDescriptor;

import java.util.LinkedList;
import java.util.List;



/**
 * Manages a pool of <code>SmfMonitors</codes>.  It is assumed
 * that all monitors are associated in some fashion.  This class
 * provides the capability of initiating and terminating monitor
 * operation simultaneously.  It also has some safety and synchronization
 * features to make monitoring more thread safe.
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Dec 8, 2009
 * @author Christopher K. Allen
 */
public class SmfPvMonitorPool {

    
    /**
     * We attach a monitor action of this type to every monitor
     * in the pool when the "dead pool checking" feature is
     * enabled.  The action calls {@link SmfPvMonitorPool#deadPoolCheck(ChannelRecord, SmfPvMonitor)},
     * if the monitor pool has been stopped, this is an error that can be seen.
     *
     * @since  Dec 14, 2009
     * @author Christopher K. Allen
     */
    class LiveMonitorAction implements SmfPvMonitor.IAction {

        /**
         * Call {@link SmfPvMonitorPool#deadPoolCheck(xal.app.pta.tools.ca.SmfPvMonitor.IAction, SmfPvMonitor)}
         * to acknowledge our liveness. 
         *
         * @since 	Dec 14, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
         */
        @SuppressWarnings("synthetic-access")
        @Override
        public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {
            deadPoolCheck(this, mon);
        }
        
    }
    
    
    /*
     * Instance Attributes
     */
    
    
    /** Static: List of registered monitors */
    private final List<SmfPvMonitor>         lstMonPool;
    
    /** Real Time: List of all active monitors */
    private final List<SmfPvMonitor>         lstMonActive;
    
    
    /** Active pool flag */
    private boolean                          bolActive;
    
    /** Dead pool monitoring */
    private final boolean                    bolChkDeadPool;
    
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>SmfPvMonitorPool</code> object.
     *
     * @since     Dec 8, 2009
     * @author    Christopher K. Allen
     */
    public SmfPvMonitorPool() {

        this.lstMonPool = new LinkedList<SmfPvMonitor>();
        this.lstMonActive = new LinkedList<SmfPvMonitor>();
        
        this.bolActive = false;
        this.bolChkDeadPool = true;
    }


    /**
     * Turn on dead-pool checking.  Dead pool checking is
     * enabled by default so one would typically call this
     * with a <code>false</code> argument to disable it.
     * Dead pool checking consumes a minimal amount of resources,
     * but could become significant in the case of many 
     * monitor threads. 
     *
     * @param bolChkDeadPool    dead pool checking on or off.
     * 
     * @since  Dec 14, 2009
     * @author Christopher K. Allen
     */
    public void setDeadPoolChecking(boolean bolChkDeadPool) {
        this.bolActive = bolChkDeadPool;
    }
    

    /**
     * <p>
     * Adds an existing monitor to the monitor pool.
     * </p>
     * <p>
     * Note that this method add
     * </p>
     *
     * @param mon       PV monitor to add to pool
     * 
     * @since  Dec 8, 2009
     * @author Christopher K. Allen
     */
    public void addMonitor(SmfPvMonitor mon) {
        
        if (this.bolChkDeadPool)
            mon.addAction(  new LiveMonitorAction() );
        
        this.lstMonPool.add(mon);
    }
    
    /**
     * Creates a new pooled monitor object and attaches to the given 
     * device Process Variable.  The event listener is assumed
     * to be attached by the user after creation.
     *
     * @param smfDev    the SMF device sending monitor events
     * @param dscrPv    the PV to be monitored
     * 
     * @return          the event monitor object
     * 
     * @since  Dec 8, 2009
     * @author Christopher K. Allen
     */
    public SmfPvMonitor createMonitor(AcceleratorNode smfDev, XalPvDescriptor dscrPv) {
        
        return this.createMonitor(smfDev, dscrPv, null);
    }
    
    /**
     * Creates a new pooled monitor object and attaches to the given 
     * device Process Variable.
     *
     * @param smfDev    the SMF device sending monitor events
     * @param dscrPv    the PV to be monitored
     * @param actEvt    listener to receive monitor events 
     * 
     * @return          the event monitor object
     * 
     * @since  Dec 8, 2009
     * @author Christopher K. Allen
     */
    public SmfPvMonitor   createMonitor(AcceleratorNode smfDev, XalPvDescriptor dscrPv, SmfPvMonitor.IAction actEvt) {
        SmfPvMonitor mon = new SmfPvMonitor(smfDev, dscrPv);
        
        if (actEvt != null)
            mon.addAction(actEvt);

        this.addMonitor(mon);
        
        return mon;
    }
    

    
    /*
     * Operations
     */
    
    /**
     * Is the pool currently actively monitoring?
     *
     * @return  <code>true</code> if the pool has at least one active monitor
     * 
     * @since  Dec 10, 2009
     * @author Christopher K. Allen
     */
    public boolean isActive() {
        return this.bolActive;
    }
    
    /**
     * <p>
     * Start up all monitors in pool.  Note that this is an atomic
     * operation.  Either all the monitors are started, or none of
     * them are.  Thus, any exceptions that are thrown are first 
     * caught within the method, clean up operations are performed,
     * then the exception is bubbled up.
     * </p>
     * <p>
     * The monitors are started in default form - that is, in whatever
     * form the default <code>{@link SmfPvMonitor#begin()}</code>
     * produces.  (For example, currently it catches the initializing
     * event from the <tt>CA</tt> monitor.)
     *  
     *
     * @throws NoSuchChannelException - A monitor PV descriptor was invalid 
     * @throws ConnectionException    - Unable to form to a PV channel connection
     * @throws MonitorException       - Unable to create a monitor on a connected channel
     * 
     * @since  Dec 8, 2009
     * @author Christopher K. Allen
     * 
     * @see     SmfPvMonitor#begin()
     */
    synchronized public void begin() throws NoSuchChannelException, ConnectionException, MonitorException {

        this.bolActive = true;
        
        // For each monitor begin 
        for (SmfPvMonitor monPool : this.lstMonPool) {

            // Start the monitors
            try {
                monPool.begin();
                
            } catch (NoSuchChannelException e) {
                this.stopActiveMonitors();
                throw e;
                
            } catch (ConnectionException e) {
                this.stopActiveMonitors();
                throw e;
                
            } catch (MonitorException e) {
                this.stopActiveMonitors();
                throw e;
            }
            
            
            this.lstMonActive.add(monPool);
        }
    }
    
    /**
     * <p>
     * Start up all monitors in pool.  Note that this is an atomic
     * operation.  Either all the monitors are started, or none of
     * them are.  Thus, any exceptions that are thrown are first 
     * caught within the method, clean up operations are performed,
     * then the exception is bubbled up.
     * </p>
     * <p>
     * The monitors are started with a call to 
     * <code>{@link SmfPvMonitor#begin(boolean)}</code>.
     * Thus, the monitors in the pool can be instructed to ignore
     * the initial event from the <tt>CA</tt> monitor 
     * (value <code>true</code>) or accept it (value <code>false</code>).
     *  
     * @param bolInitEvt        ignores initial <tt>CA</tt> event if <code>true</code>,
     *                          catches it if <code>false</code>
     *
     * @throws NoSuchChannelException - A monitor PV descriptor was invalid 
     * @throws ConnectionException    - Unable to form to a PV channel connection
     * @throws MonitorException       - Unable to create a monitor on a connected channel
     * 
     * @since  Dec 8, 2009
     * @author Christopher K. Allen
     * 
     * @see     SmfPvMonitor#begin(boolean)
     */
    synchronized public void begin(boolean bolInitEvt) 
        throws NoSuchChannelException, ConnectionException, MonitorException 
    {

        this.bolActive = true;
        
        // For each monitor begin 
        for (SmfPvMonitor monPool : this.lstMonPool) {

            // Start the monitors
            try {
                monPool.begin(bolInitEvt);
                
            } catch (NoSuchChannelException e) {
                this.stopActiveMonitors();
                throw e;
                
            } catch (ConnectionException e) {
                this.stopActiveMonitors();
                throw e;
                
            } catch (MonitorException e) {
                this.stopActiveMonitors();
                throw e;
            }
            
            
            this.lstMonActive.add(monPool);
        }
    }
    
    /**
     * <p>
     * Stops the monitor corresponding to the given SMF device.  
     * </p>
     * <p>
     * If the
     * given device has a corresponding monitor in this pool,
     * and it is active, then the monitor is terminated.  Specifically,
     * <br/>
     * <br/>
     * &nbsp; &middot; Clear the monitor for that device.
     * <br/>
     * &nbsp; &middot; Remove the monitor from the active monitor list
     * <br/>
     * &nbsp; &middot; If the list is empty, then clear the active monitor pool flag
     * </p>
     *
     * @param smfDev    targets the monitor corresponding to this device
     *
     * @author Christopher K. Allen
     * @since  Sep 15, 2011
     */
    synchronized public void stopActive(AcceleratorNode smfDev) {
        
//        // The PV monitor corresponding to the given hardware node
//        SmfPvMonitor    monTarget = null;
//        
        // Iterate through the list of active monitors looking for the 
        //    one for the given device
        for (SmfPvMonitor monActive : this.lstMonActive) {
            AcceleratorNode     smfMonitored = monActive.getDevice();
            
            // Check if the current monitor's device is the target device
            if (smfMonitored == smfDev) {
                // We have found the target device.
                //   Clear the monitor for that device.
                //   Remove the monitor from the active monitor list
                //   If the list is empty, then clear the active monitor pool flag
                
                monActive.clear();
                this.lstMonActive.remove(monActive);
                if (this.lstMonActive.size() == 0)
                    this.bolActive = false;
                return;
            }
        }

    }
    
    /**
     * Stop all active monitors in pool.  These
     * are all the monitors that were started
     * with a call to <code>begin</code>.
     *
     * 
     * @since  Dec 8, 2009
     * @author Christopher K. Allen
     * 
     * @see     #begin()
     */
    synchronized public void stopActive() {
        this.stopActiveMonitors();
    }
    
    /**
     * Stops all monitors in the pool,
     * whether they were started with
     * a call to <code>begin</code> or
     * not.
     *
     * 
     * @since  Dec 15, 2009
     * @author Christopher K. Allen
     */
    synchronized public void stopAll() {
        this.stopAllMonitors();
    }
    
    /**
     * Terminates any active monitors
     * then empties the monitor pool
     * (clears pool of all contained monitors).
     *
     * 
     * @since  Dec 9, 2009
     * @author Christopher K. Allen
     */
    synchronized public void emptyPool() {
        this.stopAllMonitors();
        this.lstMonActive.clear();
        this.lstMonPool.clear();
        this.bolActive = false;
    }
    
    
    /*
     * Support Methods
     */
    
    /**
     * Clears all active monitors.  This is
     * an method used internally in order to
     * avoid synchronization deadlocks.
     *
     * 
     * @since  Dec 9, 2009
     * @author Christopher K. Allen
     */
    private void stopActiveMonitors() {

        for (SmfPvMonitor monAct : this.lstMonActive) {
            monAct.clear();
        }
        
        System.gc();   // CKA April 30, 2014
        this.lstMonActive.clear();
        this.bolActive = false;
    }
    
    /**
     * Does a forced quit of all monitors
     * in the pool.
     *
     * 
     * @since  Dec 15, 2009
     * @author Christopher K. Allen
     */
    private void stopAllMonitors() {
        for (SmfPvMonitor monPool : this.lstMonPool) 
            monPool.clear();
        this.lstMonActive.clear();
        this.bolActive = false;
    }
    
    /**
     * The dead pool checking method.
     * any live monitor calls this method whenever
     * dead-pool checking is enabled.  When the 
     * monitor pool is stopped the 
     * dead-pool flag goes <code>false</code>.  If any monitor
     * continues to fire, then we catch it here and
     * turn it off. 
     *
     * @param ifcAct    the monitor action calling this method
     * @param monSrc    the PV monitor firing events.
     * 
     * @since  Dec 8, 2009
     * @author Christopher K. Allen
     */
    private void deadPoolCheck(SmfPvMonitor.IAction ifcAct, SmfPvMonitor monSrc) {
        if (!this.bolActive) {
            System.err.println("Active monitor  in dead pool for device: " 
                            + monSrc.getDevice() 
                            + " handle: " 
                            + monSrc.getChannelHandle());
            System.err.println("  shutting it down");
            
            monSrc.clear();
        }

    }
    
    
}
