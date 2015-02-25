/**
 * MainScanController.java
 *
 *  Created	: Dec 8, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta;

import xal.app.pta.tools.ca.SmfPvMonitor;
import xal.app.pta.tools.ca.SmfPvMonitorPool;
import xal.app.pta.tools.logging.IEventLogger;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.MonitorException;
import xal.ca.PutException;
import xal.smf.AcceleratorNode;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireScanner;
import xal.smf.scada.XalPvDescriptor;

import java.awt.Color;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * Controls scanning diagnostic hardware to perform data 
 * acquisition.
 * </p>
 * <p>
 * Note that since there is only one set of DAQ hardware,
 * maybe there should be only <code>ScannerController</code>
 * object that any application instance must ask 
 * to use.  Or another possibility is to allow multiple
 * instances of the DAQ controller but each instance
 * refuse a scan request if one is already in progress.
 * I have tried the second approach for now.
 * </p>
 * <p>
 * Note that this class does no data acquisition itself,
 * that is, it only operates the hardware.  You must
 * retrieve the data else where once the scan is complete. 
 * </p>
 * <p>
 * In the current implementation a <i>scan</i> is complete once all
 * the actuators have spanned their entire stroke length.  The
 * data is available at this time.  The entire <em>scan event</em>
 * is completed when all the device actuators have returned to
 * their parked position.  It is not until this time that another
 * scan may be started (i.e., with the {@link #scanStart(List, SCAN_MODE)}
 * method.
 * </p>
 *
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Dec 8, 2009
 * @author Christopher K. Allen
 */
public class MainScanController {

    /**
     * Interface defining events occurring during a data
     * acquisition scan.  Objects wishing to receive these
     * events should expose this interface then register 
     * with the <code>MainScanController</code> object.
     *
     * @since  Nov 17, 2009
     * @author Christopher K. Allen
     */
    public interface IScanControllerListener {
        
        /**
         * A data acquisition scan has been initiated.  The type of scan
         * being run is given.
         *
         * @param lstDevs       list of active DAQ devices       
         * @param mode          whether or not the scan was make in easy mode
         *                     
         * 
         * @since  Nov 17, 2009
         * @author Christopher K. Allen
         */
        public void scanInitiated(List<WireScanner> lstDevs, SCAN_MODE mode);
        
        /**
         * The data acquisition portion of the scan has completed.
         * 
         * @param lstDevs       list of all the acquisition devices that 
         *                      have completed successfully.
         *
         * 
         * @since  Nov 17, 2009
         * @author Christopher K. Allen
         */
        public void scanCompleted(List<WireScanner> lstDevs);
        
        /**
         * The entire data acquisition scan has been aborted.
         *
         * 
         * @since  Nov 17, 2009
         * @author Christopher K. Allen
         */
        public void scanAborted();
        
        /**
         * <p>
         * Stops the scan actuator in its current position.
         * 
         * <h4>CAUTION</h4>
         * This action has the potential to damage the accelerator
         * if the measurement sensor is a wire and it is left in 
         * the beam path.
         * </p>
         * 
         * @since  Dec 3, 2009
         * @author Christopher K. Allen
         */
        public void scanActuatorsStopped();
        
        /**
         * All scanning actuators have been parked.
         *
         * 
         * @since  Dec 8, 2009
         * @author Christopher K. Allen
         */
        public void scanActuatorsParked();
        
        /**
         * The given device has reported a failure condition
         * during the data acquisition scan.
         *
         * @param smfDev
         * 
         * @since  Dec 2, 2009
         * @author Christopher K. Allen
         */
        public void scanDeviceFailure(WireScanner smfDev);
    }

    
    /**
     *  Enumeration of the possible scan configuration used for
     *  scanning and data acquisition.  For example, the <code>DEFAULT</code>
     *  configuration is pre-programmed into the diagnostics hardware and
     *  represents a very general set of parameter that will work in most
     *  situations, although not necessarily optimally.
     *
     * @author Christopher K. Allen
     * @since   Nov 15, 2011
     */
    public enum SCAN_MODE {
        
        /** Use the default scan configuration - the configuration pre-programmed into the controller */
        EASY,
        
        /** Use the expert scan configuration - the configuration specified by the user */
        EXPERT;
        
    }
    
    /**
     * <p>
     * Enumeration of all the supported motion states
     * for the scan actuators.
     * </p>
     * <p>
     * They have the same values as the motion status
     * process variable.
     * </p>
     *
     * @since  Nov 18, 2009
     * @author Christopher K. Allen
     */
    public enum MOTION_STATE {
        /** Unknown state (or unset) */
        UNKNOWN("Unknown", -1, Color.GRAY),
        
        /** The actuator is stopped */
        HALTED("Stopped",   0, Color.BLACK),
        
        /** The actuator is in motion */
        MOVING("Moving",    1, Color.GREEN),
        
        /** The scan has failed */
        FAIL("Failed",      2, Color.RED),
        
        /** The actuator is frozen */
        LOCKED("Frozen",    3, Color.BLUE);
        
    
        /**
         * Returns the motion state enumeration constant
         * corresponding to the given motion state value.
         * Returns the <code>UNKNOWN</code> constant if the
         * given value is not a motion state value.
         *
         * @param intMotionStatVal  actuator motion state value
         * 
         * @return  enumeration constant corresponding to given value,
         *          <code>UNKNOWN</code> if argument is not a value
         * 
         * @since  Nov 6, 2009
         * @author Christopher K. Allen
         */
        public static MOTION_STATE getState(int intMotionStatVal) {
            switch (intMotionStatVal) {
            case 0:  return HALTED;
            case 1:  return MOVING;
            case 2:  return FAIL;
            default: return UNKNOWN;
            }
        }
        
        /** 
         * Return the value used by the motion status
         * Process Value corresponding to this state.
         *
         * @return  Motion Status PV value for this state
         * 
         * @since  Nov 5, 2009
         * @author Christopher K. Allen
         */
        public int  getMotionStatusVal() { return this.intVal; };
        
        /** 
         * Return the description string for the motion state
         *  
         * @return      string description used for motion state 
         */
        public String   getDescription()        { return this.strDscr; };
        
        /** 
         * Returns the display color for the motion state
         * 
         * @return      the color used for the motion state
         */
        public Color    getColor() { return this.color; };
    
        
        
        /*
         * Private
         */
        
        /** String description of the state */
        private final String    strDscr;
        
        /** State enumeration value */
        private final int       intVal;
        
        /** Display color for the state */
        private final Color     color;
        
        /** Create new motion state enumeration constant 
         * @param strDscr text description of motion condition
         * @param intVal  integer value of the state
         * @param color   intrinsic color used to display the state 
         */
        private MOTION_STATE(String strDscr, int intVal, Color color) {
            this.strDscr = strDscr;
            this.intVal  = intVal;
            this.color   = color;
        }
    }




    /**
     *  Reacts to errors reported by the 
     *  Scan Error PV, calls the {@link MainScanController#deviceFailure(AcceleratorNode)}
     *  method.
     *
     * @since  Dec 10, 2009
     * @author Christopher K. Allen
     */
    private class DeviceErrorAction implements SmfPvMonitor.IAction {
    
        /** PV value indicating normal operation */
        static public final int         OKAY = 0;
    
        /** PV value indicating error condition */
        static public final int         ERROR = 1;
    
    
        /*
         * Instance Attributes
         */
    
        /** The error flag */
        private boolean         bolError;
    
    
        /**
         * Create a new <code>DeviceErrorAction</code> object.
         *
         * @since     Dec 15, 2009
         * @author    Christopher K. Allen
         */
        public DeviceErrorAction() {
            this.bolError = false;
        }
    
    
        /**
         * Responds to a change in the scan error PV of the
         * device associated with this object.
         * 
         *  @param      recValue        new PV value
         *  @param      mon             the PV monitor object
         *
         * @since 	Dec 10, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(gov.sns.ca.ChannelRecord, gov.sns.apps.pta.tools.ca.SmfPvMonitor)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void valueChanged(ChannelRecord recValue, SmfPvMonitor mon) {
    
            // Check if we are already in failure mode 
            if (this.bolError)
                return;
    
            int intValue = recValue.intValue();

            // PV value is OKAY - nothing to do
            if (intValue == OKAY)
                return;
            
            // PV reports an error condition
            if (intValue == ERROR) {
                this.bolError = true;
                
                MainScanController.this.deviceFailure(mon.getDevice());
                
                String      strMsg = "Device Error Monitor action invoked for " + mon.getDevice().getId();
                
                System.err.println(strMsg);
                MainApplication.getEventLogger().logError(this.getClass(), strMsg);
                return;
            }
            
        }
    }


    /**
     * <p>
     * Response to the change in scan sequence
     * identifier (<i>SeqId</i>).  
     * </p>
     * <p>
     * At the completion of each scan the <tt>SeqId</tt>
     * is incremented (i.e., at the moment the actuators
     * have reached their full extension - before they are
     * retracted.).  This action object is called in the
     * response to the new scan ID.  
     * </p>
     * <p>
     * Currently we launch a <code>RevLimitAction</code> object,
     * since the data does not seem to be available 
     * until the actuator is parked.
     * </p>
     *
     * @since  Dec 10, 2009
     * @author Christopher K. Allen
     */
    private class ScanIdIncrAction implements SmfPvMonitor.IAction {

        /*
         * Instance Attributes
         */

        /** The initializing event flag */
        private boolean                       bolFirstEvent;

        /** The reverse limit switch action for the device we are monitoring */
        private final RevLimitAction          actRevLim;


        /**
         * Create a new <code>ScanIdIncrAction</code> object.
         *
         * @param actRevLim     the reverse limit monitor action for the same device 
         *
         * @since     Dec 10, 2009
         * @author    Christopher K. Allen
         */
        public ScanIdIncrAction(RevLimitAction   actRevLim) {
            this.bolFirstEvent = true;

            this.actRevLim = actRevLim;
        }

        /**
         * Skip the first event (the PV value initialization).  Next
         * event unblock the <code>ScannerController.RevLimitAction</code>
         * response for the device.  Then launch the <code>DeviceCompletedThread</code>
         * thread on the given device to check if it was the last
         * device to finish.
         *
         * @since 	Dec 10, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(gov.sns.ca.ChannelRecord, gov.sns.apps.pta.tools.ca.SmfPvMonitor)
         */
        @Override
        public void   valueChanged(ChannelRecord val, SmfPvMonitor mon) {
            if (this.bolFirstEvent) {
                this.bolFirstEvent = false;
                return;
            }

//            System.out.println("ScanIdIncrAction#valueChanged():");
//            System.out.println("  Device ID = " + mon.getDevice().getId());
//            System.out.println("  Scan   ID = " + val.intValue());

            // Unblock the reverse limit switch action to look for a parked actuator
            this.actRevLim.unblock();
            
            // Run the Scan complete thread
            DeviceCompletedThread thdScnCmp = new DeviceCompletedThread( (WireScanner)mon.getDevice() );
            
            thdScnCmp.start();
//            
//            System.out.println("Unblocking rev. limit monitor action for " + mon.getDevice().getId());
        }
    }

    
    /**
     * Reacts to an activation of the reverse limit
     * switch by deactivating the DAQ device.
     *
     * @since  Dec 15, 2009
     * @author Christopher K. Allen
     */
    private class RevLimitAction implements SmfPvMonitor.IAction{

        /*
         * Instance Attributes
         */

        /** The initializing event flag */
        private boolean         bolBlocked;


        /**
         * Create a new <code>ScanIdIncrAction</code> object.
         *
         *
         * @since     Dec 10, 2009
         * @author    Christopher K. Allen
         */
        public RevLimitAction() {
            this.bolBlocked = true;
        }


        /**
         * Unblock the monitor action.
         *
         * 
         * @since  Dec 15, 2009
         * @author Christopher K. Allen
         */
        public void     unblock() {
            this.bolBlocked = false;
        }

        /**
         *
         * @since 	Dec 15, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(gov.sns.ca.ChannelRecord, gov.sns.apps.pta.tools.ca.SmfPvMonitor)
         */
        @Override
        public void valueChanged(ChannelRecord recValue, SmfPvMonitor mon) {

//            System.out.println("The Rev. Limit Action has been called for " + mon.getDevice().getId());
            
            // Only do something if monitor action is unblocked
            if (this.bolBlocked) 
                return;

            // Check the limit switch value and deactivate if set
            int         intVal = recValue.intValue();

            if (intVal == 1) {
//                System.out.println("MainScanController.RevLimitAction#valueChanged() - Rev. Limit switch activated for " + mon.getDevice().getId());

//                // Re-block - we are not going to fire again.
//                this.bolBlocked = true;

                ActuatorParkedThread thdDeact = new ActuatorParkedThread( mon.getDevice() );
                
                thdDeact.start();
            }
        }
    }

    
    /**
     * <p>
     * Thread to be run by the
     * {@link MainScanController.ScanIdIncrAction#valueChanged(ChannelRecord, SmfPvMonitor)}
     * method.  The thread removes the given device from the
     * list of actively scanning devices.  If the device is the
     * last device to finish the thread notifies all the 
     * <code>ScannerController.IDaqControllerListener</code>s that the DAQ
     * portion has completed. 
     * </p>
     * <p>
     * Note the calling <code>SeqIdIncrAction</code>unblocks its
     * associated <code>ScannerController.RevLimitAction</code>
     * on the <code>WireScanner.SCAN.LIMREV</code> PV.  Thus,
     * once the actuator hits the reverse limit switch the 
     * <code>RevLimitAction</code> is able to fire.
     * </p>
     *
     * @since  Dec 15, 2009
     * @author Christopher K. Allen
     */
    private class DeviceCompletedThread extends Thread {

        /** The device that has been deactivated */
        private final WireScanner           smfDev;
        
        /**
         * Create a new <code>ScanCompletedThread</code> object.
         *
         * @param smfDev
         *
         * @since     Dec 15, 2009
         * @author    Christopher K. Allen
         */
        public DeviceCompletedThread(WireScanner smfDev) {
            this.smfDev = smfDev;
        }

        /**
         * Begin thread operation.  Remove the device from 
         * the list of actively scanning devices then 
         * unblock its <code>RevLimitAction</code> on the
         * <code>WireScanner.SCAN.LIMREV</code> PV.
         *
         * @since   Dec 15, 2009
         * @author  Christopher K. Allen
         *
         * @see java.lang.Thread#run()
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void run() {

            synchronized (BOL_SCAN_ACTIVE) {

//                System.out.println("DeviceCompletedThread#run():");
//                System.out.println("  Device ID = " + smfDev.getId());

                // Remove the device from active device list
                //  If the device was not in the list (???)
                //  then something is wrong - just ignore it for now.
//                if (! lstDevScanning.remove(smfDev) )
//                    return;
                lstDevScanning.remove(smfDev);

                // Place it in the list of completed devices
                lstDevCompleted.add(smfDev);

                // If there are more active devices then return
                if (lstDevScanning.size() > 0) 
                    return;
                
                // Scan complete book keeping
                MainScanController.this.scanCompleted();
            }
        }
        
    }
    
    
    /**
     * Thread to be run by the 
     * {@link MainScanController.RevLimitAction#valueChanged(ChannelRecord, SmfPvMonitor)}
     * event response.  The thread removes the attached device from the
     * list of unparked actuators then checks if it is the last one.
     * If so, clean up of the DAQ scan occurs and the 
     * <code>ScannerController.IDaqControllerListener</code>'s
     * are notified.
     *
     *
     * @since  Dec 15, 2009
     * @author Christopher K. Allen
     */
    private class ActuatorParkedThread extends Thread {


        /** The device that has been deactivated */
        private final AcceleratorNode           smfDev;
        
        
        /**
         * Create a new <code>ActuatorParkedThread</code> object.
         *
         * @param smfDev
         *
         * @since     Dec 15, 2009
         * @author    Christopher K. Allen
         */
        public ActuatorParkedThread(AcceleratorNode smfDev) {
            this.smfDev = smfDev;
        }
        
        /**
         * Begins the thread operation.  Remove device
         * from the list of unparked devices and check
         * if it's the last one.  Release lock if so
         * then clean up.
         *
         * @since 	Dec 15, 2009
         * @author  Christopher K. Allen
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void run() {
            
            synchronized (BOL_SCAN_ACTIVE) {
                
//                System.out.println("MainScanController.ActuatorParkedThread#run() - flagged park for device " + this.smfDev.getId());

                // Remove the device from active device list
                //  If the device was not in the list (???)
                //  then something is wrong - just ignore it for now.
                if (! lstDevUnparked.remove(this.smfDev) )
                    return;

                // If there are more active devices then return
                if (lstDevUnparked.size() > 0) 
                    return;

                // Otherwise it was the last active device - Clear active scan flag
                BOL_SCAN_ACTIVE = false;
            }
            
            // Clean up the DAQ
            MainScanController.this.devicesParked();
        }

    }


    
    
    /*-------------------------------------
     * 
     * 
     * Global Attributes
     * 
     * 
     */


    /** 
     * The single data acquisition controller  instance 
     */
    private static MainScanController          DAQ_CTRLR = null;
    


//    /** 
//     * Active scan semaphore for entire machine 
//     */
//    private static Object       OBJ_CMDR_LOCK = null;
    
    
    /** Scan-Active flag for controller */
    private static Boolean      BOL_SCAN_ACTIVE = false;


    
    /*----------------------------------------
     * 
     * 
     * Global Methods
     * 
     * 
     */
    

    /**
     * Returns the application's main controller object for 
     * data acquisition.
     *
     * @return  DAQ central controller
     * 
     * @since  Dec 11, 2009
     * @author Christopher K. Allen
     */
    public static MainScanController  getInstance() {
        if (DAQ_CTRLR != null)
            return DAQ_CTRLR;
        
        MainScanController.DAQ_CTRLR = new MainScanController();
        
        return MainScanController.DAQ_CTRLR;
    }
    
    
    
    /*-------------------------------------
     * 
     * 
     * Instance Attributes
     * 
     * 
     */


    /*
     * Application Tools
     */
    
//    /** The scan configuration we will use for scanning and data acquisition */
//    private SCAN_MODE                       enmScanType;

    
    /*
     * RT: Action Event Handlers
     */
    /** Real Time: pool of scan monitors */
    private final SmfPvMonitorPool          mplScanEvts;
    
    /*
     * RT: Active DAQ Devices 
     */
    /** Real Time: List of actively scanning devices - used for synchronization */
    private final List<WireScanner>         lstDevScanning;
    
    /** Real Time: List of devices whose actuators are still not parked */
    private final List<WireScanner>         lstDevUnparked;

    /** Real Time: List of devices that have completed a scan */
    private final List<WireScanner>         lstDevCompleted;


    /** Registered handlers of the DAQ scan events */
    private final List<IScanControllerListener>   lstLsnCtlEvts;


    

    /*
     * Initialization
     */
    
    /**
     * Create a new <code>ScannerController</code> object which
     * operates devices obtained from the given device selection 
     * panel.
     *
     * @since     Sep 16, 2009
     * @author    Christopher K. Allen
     */
    private MainScanController() {
        
//        // The initial scan configuration
//        this.enmScanType = SCAN_MODE.SIMPLE;
        
        // The active scan lists
        this.lstDevScanning  = new LinkedList<WireScanner>();
        this.lstDevUnparked  = new LinkedList<WireScanner>();
        this.lstDevCompleted = new LinkedList<WireScanner>();
        
        // DAQ event monitors
        this.mplScanEvts = new SmfPvMonitorPool();

        // Registered controller event listeners
        this.lstLsnCtlEvts = new LinkedList<IScanControllerListener>();
    }

    
//    /**
//     * Set the scan configuration to be used during the following
//     * scanning and data acquisition.
//     *
//     * @param enmCfg    enumeration constant for the desired scan configuration
//     *
//     * @author Christopher K. Allen
//     * @since  Nov 15, 2011
//     */
//    public void setScanMode(SCAN_MODE enmCfg) {
//        this.enmScanType = enmCfg;
//    }
//    
    /**
     * Register the given handler to receive the scan
     * initiated event.
     *
     * @param handler   event sink for scan starts
     * 
     * @since  Nov 14, 2009
     * @author Christopher K. Allen
     */
    public void registerControllerListener(IScanControllerListener handler) {
        this.lstLsnCtlEvts.add(handler);
    }
    
    /**
     * Remove the given object from the set of registered 
     * controller event listeners. 
     *
     * @param lsnEvts   event listener to be removed.
     * 
     * @since  Dec 11, 2009
     * @author Christopher K. Allen
     */
    public void removeControllerListener(IScanControllerListener lsnEvts) {
        this.lstLsnCtlEvts.remove(lsnEvts);
    }
    
    
    /*
     * Query
     */
    
//    /**
//     *  Returns the enumeration constant for the scan configuration
//     *  currently used by the DAQ controller.
//     *  
//     * @return  current scan configuration
//     *
//     * @author Christopher K. Allen
//     * @since  Nov 15, 2011
//     */
//    public SCAN_MODE getScanMode() {
//        return this.enmScanType;
//    }
//    
//    /**
//     * <p>
//     * Register to receive monitor events for the given PV.
//     * The events are sent to the given event handler.
//     * </p>
//     * <p>
//     * Note that a monitor is created for each device in the
//     * scanning set.  The same event handler (<var>lsnPv</var>)
//     * is call for each device PV.  To determine the actual 
//     * device which invoked the event use the {@link SmfPvMonitor#getDevice()}
//     * method.
//     * </p>
//     *
//     * @param pvdMon
//     * @param lsnPv
//     * 
//     * @since  Dec 10, 2009
//     * @author Christopher K. Allen
//     * 
//     * @see     SmfPvMonitor#getDevice()
//     */
//    public void registerPvListener(PvDescriptor pvdMon, IAction lsnPv) {
//        this.lstPvLsnPairs.add( new PvListenerPair(pvdMon, lsnPv) );
//    }
    
//    /**
//     * registerDaqMonitor
//     *
//     * @param mon
//     * 
//     * @since  Dec 8, 2009
//     * @author Christopher K. Allen
//     */
//    public void registerDaqMonitor(DaqEventMonitor mon) {
//     
//        mon.registerDaqEventListener(
//                        new IDaqEventListener() {
//
//                            public void valueChanged(ChannelRecord val, DaqEventMonitor mon) {
//                                userRegMonitorCallback(val, mon);
//                            }
//                        }
//        );
//    }
//    
    

    /*
     * Operations
     */
    
    /**
     * <p>
     * <h4>Real-Time Methods</h4>
     * 
     * These methods use the lock <code>BOL_SCAN_ACTIVE</code>
     * to synchronize across the entire virtual machine.  This is in 
     * lieu of using the <code>synchronized</code> qualifier to set up
     * a monitor on class instances. The locks must be held with respect
     * to the entire machine since only one scan may be active at a time.
     * </p>
     */
    
    /**
     * <p>
     * Initiates the data acquisition scan.
     * </p>
     * <p>
     * The scan is started simultaneously for the given list of DAQ 
     * devices and the controller ignores all commands, except the 
     * abort command, until the scan event completes (when all device
     * actuators are parked).  If a serious error occurs during the
     * scan event, the controller state may also reset.
     * </p>
     * <p>
     * This method blocks (synchronizes) on the list of active DAQ devices. 
     *  The operation of initiating the scan sequence must be atomic.
     * Otherwise, a highly motivated DAQ device could complete its scan before we have 
     * finished launching the next device.  This would signal an empty active device list
     * indicated a completed scan.
     * </p>
     * <p>
     * When running an <em>EASY SCAN</em> (i.e., <var>enmMode</var> = <code>SCAN_MODE.EASY</code>),
     * this command uses a default set of configuration parameters for the wire scanners
     * which provides reasonable (but not optimal) data sets in most cases.
     * For optimized data sets the user should configure each wire scanner
     * independently and run an expert scan. 
     * </p>
     * <p>
     * <h4>NOTE:</h4>
     * &middot; Running this scan sets all configuration parameters on the wire scanner
     * IOC to the default (Easy Scan) parameters.  The original parameters are clobbered.
     * </p>
     * 
     * @param lstDevs   the list of DAQ devices used for the scan
     * @param enmMode   the type of scan to run (EASY or EXPERT) 
     * 
     * @return  <code>true</code> if scan was successfully initiated, 
     *          <code>false</code> otherwise
     *  
     * @since  Nov 4, 2009
     * @author Christopher K. Allen
     */
    public boolean scanStart(List<WireScanner> lstDevs, SCAN_MODE enmMode) {
        // Check if there is anything to do
        if (lstDevs==null || lstDevs.size()==0)
            return false;

        // Check if there is already a scan in progress
        //      If not, grab the SCAN lock  
        synchronized (MainScanController.BOL_SCAN_ACTIVE) {

            if (MainScanController.BOL_SCAN_ACTIVE)
                return false;


            // Initialize the scan - create monitoring threads
            String      strErrMsg = "Unable to create monitor pool for devices " + lstDevs;
            
            try {
                this.monitorInitiate(lstDevs);

            } catch (ConnectionException e) {
                getLogger().logException(getClass(), e, strErrMsg);
                return false;

            } catch (MonitorException e) {
                getLogger().logException(getClass(), e, strErrMsg);
                return false;

            } catch (NoSuchChannelException e) {
                getLogger().logException(getClass(), e, strErrMsg);
                return false;

            }


            // Begin the DAQ scan
            try {
                this.scanInitiate(lstDevs, enmMode);

            } catch (InterruptedException e) {
                String      strMsg = "ERROR in scan start: Command buffer reset thread interrupted";

                getLogger().logException(this.getClass(), e, strMsg);
                this.scanTerminate();

            } catch (ConnectionException e) {
                String      strMsg = "ERROR in scan start: Unable to connect to device in " + lstDevs;

                this.getLogger().logException(this.getClass(), e, strMsg);
                this.scanTerminate();

            } catch (PutException e) {
                String      strMsg = "ERROR in scan start: Unable to set parameter in devices " + lstDevs;

                this.getLogger().logException(this.getClass(), e, strMsg);
                this.scanTerminate();

            }


            // Set the active scan flag
            MainScanController.BOL_SCAN_ACTIVE = true;
        }

        return true;
    }

//    /**
//     * <p>
//     * Initiates the wire scan in the easy scan configuration.
//     * </p>
//     * <p>
//     * The scan is started simultaneously for the given list of DAQ 
//     * devices and the controller ignores all commands, except the 
//     * abort command, until the scan event completes (when all device
//     * actuators are parked).  If a serious error occurs during the
//     * scan event, the controller state may also reset.
//     * </p>
//     * <p>
//     * This command uses a default set of configuration parameters for the wire scanners
//     * which provides reasonable (but not optimal) data sets in most cases.
//     * For optimized data sets the user should configure each wire scanner
//     * independently and run an expert scan 
//     * (see <code>{@link #scanStartExpert(List)}</code>).
//     * </p>
//     * <p>
//     * <h4>NOTE:</h4>
//     * &middot; Running this scan sets all configuration parameters on the wire scanner
//     * IOC to the default (Easy Scan) parameters.  The original parameters are clobbered.
//     * </p>
//     * 
//     * @param lstDevs   the list of DAQ devices used for the scan 
//     * 
//     * @return  <code>true</code> if scan was successfully initiated, 
//     *          <code>false</code> otherwise
//     *  
//     * @since  Nov 4, 2009
//     * @author Christopher K. Allen
//     */
//    public boolean scanStartEasy(List<WireScanner> lstDevs) {
//        // Check if there is anything to do
//        if (lstDevs==null || lstDevs.size()==0)
//            return false;
//
//        // Check if there is already a scan in progress
//        //      If not, grab the SCAN lock  
//        synchronized (ScannerController.BOL_SCAN_ACTIVE) {
//
//            if (ScannerController.BOL_SCAN_ACTIVE)
//                return false;
//
//
//            // Initialize the scan - create monitoring threads
//            String      strErrMsg = "Unable to create monitor pool for devices " + lstDevs;
//            
//            try {
//                this.monitorInitiate(lstDevs);
//
//            } catch (ConnectionException e) {
//                getLogger().logException(getClass(), e, strErrMsg);
//                return false;
//
//            } catch (MonitorException e) {
//                getLogger().logException(getClass(), e, strErrMsg);
//                return false;
//
//            } catch (NoSuchChannelException e) {
//                getLogger().logException(getClass(), e, strErrMsg);
//                return false;
//
//            }
//
//
//            // Begin the DAQ scan
//            try {
//                this.scanInitiate(lstDevs, SCAN_MODE.EASY);
//
//            } catch (InterruptedException e) {
//                String      strMsg = "ERROR in easy scan start: Command buffer reset thread interrupted";
//
//                getLogger().logException(this.getClass(), e, strMsg);
//                this.scanTerminate();
//
//            } catch (ConnectionException e) {
//
//                String      strMsg = "ERROR in easy scan start: Unable to connect to device in " + lstDevs;
//                this.getLogger().logException(this.getClass(), e, strMsg);
//                this.scanTerminate();
//
//            } catch (PutException e) {
//                String      strMsg = "ERROR in easy scan start: Unable to set parameter in devices " + lstDevs;
//
//                this.getLogger().logException(this.getClass(), e, strMsg);
//                this.scanTerminate();
//
//            }
//
//
//            // Set the active scan flag
//            ScannerController.BOL_SCAN_ACTIVE = true;
//        }
//
//        return true;
//    }
    
    /**
     * Aborts a currently active scan. Sends the 
     * abort command to all the active DAQ devices
     * then eradicates all active threads.  The
     * DAQ event listeners are then notified of the
     * event.
     * 
     * @since  Nov 10, 2009
     * @author Christopher K. Allen
     */
    public void scanAbort() {

        // This must be done atomically
        synchronized (BOL_SCAN_ACTIVE) {

            // For each active scanning device send the 
            //  abort command and remove it from the 
            //  active device list.
            for (AcceleratorNode nodeDev : this.lstDevScanning) {
                if ( !(nodeDev instanceof WireScanner) ) 
                    continue;

                WireScanner ws  = (WireScanner)nodeDev;

                try {
                    ws.runCommand(WireScanner.CMD.ABORT);

                } catch (InterruptedException e) {
                    String      strMsg = "ERROR in Scan Abort: Command buffer reset thread interrupted" + ws.getId();

                    getLogger().logException(this.getClass(), e, strMsg);

                } catch (ConnectionException e) {
                    String      strMsg = "ERROR in Scan Abort: Unable to connect to " + ws.getId();

                    this.getLogger().logException(this.getClass(), e, strMsg);

                } catch (PutException e) {
                    String      strMsg = "ERROR in Scan Abort: Unable to send abort command to " + ws.getId();

                    this.getLogger().logException(this.getClass(), e, strMsg);
                }
            }

            // Shutdown scan
            this.scanTerminate();

            // Make notifications of the aborted scan
            for (IScanControllerListener hndlr : this.lstLsnCtlEvts) {
                hndlr.scanAborted();
            }

            // Post log of aborted scan
            this.getLogger().logError(this.getClass(), "User abort during DAQ scan");
        }

    }


    /**
     * Command to immediately park all the scan actuators.
     * The actuators are parked, the scan is terminated, then the DAQ controller is
     * initialized to its pre-scan state.
     * 
     * @since  Dec 3, 2009
     * @author Christopher K. Allen
     */
    public void scanActuatorsPark() {

        // This must be done atomically
        synchronized (BOL_SCAN_ACTIVE) {

            // For each active scanning device send the 
            //  park command and remove it from the 
            //  active device list.
            for (AcceleratorNode smfDev : this.lstDevUnparked) {
                if ( !(smfDev instanceof WireScanner) ) 
                    continue;

                WireScanner ws  = (WireScanner)smfDev;

                try {
                    ws.runCommand(WireScanner.CMD.PARK);

                } catch (InterruptedException e) {
                    String      strMsg = "ERROR in PARK CMD: Command buffer reset thread interruped for " + ws.getId();

                    this.getLogger().logError(this.getClass(), strMsg);

                } catch (ConnectionException e) {
                    String      strMsg = "ERROR in PARK CMD: Unable to connect to " + ws.getId();

                    this.getLogger().logError(this.getClass(), strMsg);

                } catch (PutException e) {
                    String      strMsg = "ERROR in PARK CMD: Unable to send park command to " + ws.getId();

                    this.getLogger().logError(this.getClass(), strMsg);
                }

            }
            
            // Terminate scan
            this.scanTerminate();

            // Make notifications of the park actuators command
            for (IScanControllerListener hndlr : this.lstLsnCtlEvts) {
                hndlr.scanActuatorsParked();
            }

            // Post log of park
            this.getLogger().logError(this.getClass(), "User parking scan actuators");
        }
    }

    /**
     * <p>
     * Stops the scan actuators in its current position.
     * 
     * <h4>CAUTION</h4>
     * This action has the potential to damage the accelerator
     * if the measurement sensor is a wire and it is left in 
     * the beam path.
     * </p>
     * 
     * @since  Dec 3, 2009
     * @author Christopher K. Allen
     */
    public void scanActuatorsStop() {
        
        // This must be done atomically
        synchronized (BOL_SCAN_ACTIVE) {

            // For each active scanning device send the 
            //  stop command. 
            for (AcceleratorNode smfDev : this.lstDevUnparked) {
                if ( !(smfDev instanceof WireScanner) ) 
                    continue;

                WireScanner ws  = (WireScanner)smfDev;

                try {
                    ws.runCommand(WireScanner.CMD.STOP);

                } catch (InterruptedException e) {
                    String      strMsg = "ERROR in STOP CMD: Command buffer reset thread interruped for " + ws.getId();

                    this.getLogger().logError(this.getClass(), strMsg);

                } catch (ConnectionException e) {
                    String      strMsg = "ERROR in STOP CMD: Unable to connect to " + ws.getId();

                    this.getLogger().logError(this.getClass(), strMsg);

                } catch (PutException e) {
                    String      strMsg = "ERROR in STOP CMD: Unable to send command to " + ws.getId();

                    this.getLogger().logError(this.getClass(), strMsg);
                }

            }

            // Make notifications of the park actuators command
            for (IScanControllerListener hndlr : this.lstLsnCtlEvts) {
                hndlr.scanActuatorsStopped();
            }

            // Post log of aborted scan
            this.getLogger().logWarning(this.getClass(), "User stopping actuators during DAQ scan");

        }
    }
    

    
    
    /*
     * Debugging
     */
    
    /**
     * Prints out the scan ID for each device in the list to the
     * standard output.
     * 
     * @param lstDevs   list of wire scanners to output
     *
     * @author Christopher K. Allen
     * @since  Oct 6, 2011
     */
    public void printScanIds(List<WireScanner> lstDevs) {
        try {
            System.out.println("Device scan IDs");
            for (WireScanner ws : lstDevs) {

                WireScanner.DevStatus datStat = WireScanner.DevStatus.acquire(ws);

                System.out.println("Device " + ws.getId() + " scan ID = " + datStat.idScan);
            }
            
        } catch (ConnectionException e) {
            e.printStackTrace();

        } catch (GetException e) {
            e.printStackTrace();

        }
    }
    
    /**
     * Prints out the lists of each device in each device queue at the
     * current time.
     *
     * @author Christopher K. Allen
     * @since  Oct 6, 2011
     */
    public void printDeviceQueues() {
        System.out.println("DEVICE QUEUES");
        System.out.println("Scanning devices  = " + this.lstDevScanning);
        System.out.println("Unparked devices  = " + this.lstDevUnparked);
        System.out.println("Completed devices = " + this.lstDevCompleted);

    }


    /**
     * <p> 
     * <h4>DAQ Support</h4>
     * 
     * These methods should not be synchronized.  They are typically
     * called within synchronized blocked of the real-time methods.
     * </p>
     */
    
    /**
     * <p>
     * Removes the given device from the list of active
     * DAQ devices.  When the last device on the list
     * reports in this method indicates that the scan
     * is finished by calling the <code>scanCompleted</code>
     * method.
     * </p>
     * <p>
     * Note that this method blocks (synchronizes) on the
     * list of active devices to be consistent with the
     * blocking of the method <code>scanBegin()</code>.
     * </p>
     *
     * @param smfDev    the device to remove from the active DAQ device list
     * 
     * @since  Nov 9, 2009
     * @author Christopher K. Allen
     * 
     * @see     #scanInitiate()
     */
//    private void deviceDeactivate(AcceleratorNode smfDev) {

//        ActuatorParkedThread         thdDeact = new ActuatorParkedThread(this, smfDev);
//        
//        thdDeact.start();
//        
//        synchronized (BOL_SCAN_ACTIVE) {
//            
//            // Remove the device from active device list
//            //  If the device was not in the list (???)
//            //  then something is wrong - just ignore it for now.
//            if (! this.lstDevActive.remove(smfDev) )
//                return;
//
//            // If there are more active devices then return
//            if (this.lstDevActive.size() > 0) 
//                return;
//         
//            // Otherwise it was the last active device - Release SCAN lock
//            BOL_SCAN_ACTIVE = false;
//        }
//        
//        // Clean up the DAQ
//        this.scanCompleted();
//    }
    
    /**
     * Respond to DAQ device failure.
     *
     * @param smfDev
     * 
     * @since  Nov 9, 2009
     * @author Christopher K. Allen
     */
    private void deviceFailure(AcceleratorNode smfDev) {

        if ( !(smfDev instanceof WireScanner) ) 
            return;

        WireScanner ws  = (WireScanner)smfDev;
        
//        WireScanner ws = smfDev;
//        
        // Remove all the monitors associated with this device
        this.mplScanEvts.stopActive(smfDev);
        

        // Attempt to park the failed device
        try {
            ws.runCommand(WireScanner.CMD.PARK);

        } catch (InterruptedException e) {
            String      strMsg = "ERROR in PARK CMD: Command buffer reset thread interruped for " + ws.getId();

            this.getLogger().logError(this.getClass(), strMsg);

        } catch (ConnectionException e) {
            String      strMsg = "ERROR in PARK CMD: Unable to connect to " + ws.getId();

            this.getLogger().logError(this.getClass(), strMsg);

        } catch (PutException e) {
            String      strMsg = "ERROR in PARK CMD: Unable to send park command to " + ws.getId();

            this.getLogger().logError(this.getClass(), strMsg);
        }

        
        // Remove the device from the active scanning and unparked lists.
        this.lstDevScanning.remove(smfDev);
        this.lstDevUnparked.remove(smfDev);

        
        this.getLogger().logError(this.getClass(), "WIRE SCANNER FAILURE: deviceFailure() for " + smfDev.getId());
        
        // Notify all the registered listeners
        for (IScanControllerListener hndler : this.lstLsnCtlEvts) 
            hndler.scanDeviceFailure(ws);
        
    }
    
    /**
     * Perform cleanup after all DAQ actuators are parked,
     * notify all registered listeners.
     *
     * 
     * @since  Dec 15, 2009
     * @author Christopher K. Allen
     */
    private void devicesParked() {
    
//        System.out.println("\nMainScanController#devicesParked() - entered");
//        this.printDeviceQueues();
//        this.printScanIds(lstDevCompleted);
    
        // Empty the active monitor pool
        mplScanEvts.emptyPool();
    
//        System.out.println("\nMainScanController#devicesParked() - past this.mplScanEvts.emptyPool()");
        
        // String notification
        getLogger().logInfo(this.getClass(), "Actuators parked: " + Calendar.getInstance().getTime().toString());

//        System.out.println("\nMainScanController#devicesParked() - past getLogger().logInfo()");
        
        // Notify the registered scan complete event handlers
        for (IScanControllerListener hndlr : lstLsnCtlEvts)
            hndlr.scanActuatorsParked();
    }



    /**
     * Creates the PV monitors needed to asses the
     * progress of the DAQ scan.  These monitors
     * are put in the common monitor pool for
     * all types of DAQ monitoring.
     *
     *  @param  lstDevs         list of active DAQ devices
     * 
     * @since  Dec 10, 2009
     * @author Christopher K. Allen
     * 
     * @throws NoSuchChannelException   attempted to monitor non-existent channel 
     * @throws ConnectionException      unable to connect to PV device
     * @throws MonitorException         unable to instantiate channel monitor
     */
    private void monitorInitiate(List<WireScanner> lstDevs) throws ConnectionException, MonitorException, NoSuchChannelException {

        // Create all our internal scanning monitors
        for (AcceleratorNode smfDev : lstDevs) {

//            if ( !(smfDev instanceof WireScanner) )
//                continue;
//            

            // Create the reverse limit switch monitor
//            PvDescriptor        pvdLimRev = WireScanner.DevStatus.PARAM.LIM_REV.getPvDescriptor();
            XalPvDescriptor     pvdLimRev = WireScanner.DevStatus.FLD_MAP.get("limRev");
            RevLimitAction      actLimRev = new RevLimitAction();
            SmfPvMonitor        monLimRev = new SmfPvMonitor(smfDev, pvdLimRev);
            
            monLimRev.addAction(actLimRev);
            this.mplScanEvts.addMonitor(monLimRev);
            
            
            // Create the Scan Sequence Id Monitor
//            PvDescriptor     pvdSeqId = WireScanner.DevStatus.PARAM.ID_SCAN.getPvDescriptor();
            XalPvDescriptor     pvdSeqId = WireScanner.DevStatus.FLD_MAP.get("idScan");
            ScanIdIncrAction actSeqId = new ScanIdIncrAction(actLimRev);
            SmfPvMonitor     monSeqId = new SmfPvMonitor(smfDev, pvdSeqId);
            
            monSeqId.addAction(actSeqId);
            this.mplScanEvts.addMonitor(monSeqId);
            
            
            // Create the error monitor
//            PvDescriptor        pvdDevErr = WireScanner.DevStatus.PARAM.ERR_SCAN.getPvDescriptor();
            XalPvDescriptor        pvdDevErr = WireScanner.DevStatus.FLD_MAP.get("errScan");
            DeviceErrorAction   actDevErr = new DeviceErrorAction();
            SmfPvMonitor        monDevErr = new SmfPvMonitor(smfDev, pvdDevErr);
            
            monDevErr.addAction(actDevErr);
            this.mplScanEvts.addMonitor(monDevErr);
        }

        // Start all the monitors
        this.mplScanEvts.begin();
    }
    
    
   /**
     * <p>
     * Begins the data acquisition scan by
     * send the <tt>SCAN</code> command to
     * each valid DAQ device (i.e., node in
     * the active device list).
     * </p>
     * <p>
     * This method blocks (synchronizes) on the list
     * of active DAQ devices.  The operation of 
     * initiating the scan sequence must be atomic.
     * Otherwise, a highly motivated DAQ device
     * could complete its scan before we have 
     * finished launching the next device.  This
     * would signal an empty active device list
     * indicated a completed scan.
     * </p>
     * <p>
     * <h4>IMPORTANT NOTE:</h4>
     * &middot; This is where the <code>BOL_SCAN_ACTIVE</code> state variable is
     * set.
     * <br/>
     * &middot; The synchronization is currently disabled.  The synchronization is
     * handled externally by the calling method, 
     * see <code>{@link #scanStartExpert(List)}</code> or
     * see <code>{@link #scanStartEasy(List)}</code>.
     * 
     * </p>
     * 
     * @param lstDevs   list of devices selected for DAQ
     * @param enmMode   the type of scan to run for all devices in the list
     * 
     * @throws InterruptedException     the command buffer reset thread was interrupted
     * @throws ConnectionException      cannot connect to wire scanner
     * @throws PutException             cannot send command to wire scanner
     * 
     * @see #scanStartExpert(List)
     * @see #scanStartEasy(List)
     *  
     * @since  Dec 3, 2009
     * @author Christopher K. Allen
     */
    private void scanInitiate(List<WireScanner> lstDevs, SCAN_MODE enmMode) throws InterruptedException, ConnectionException, PutException {

        //
        // Set the Active-Scan flag
        //
        MainScanController.BOL_SCAN_ACTIVE = true;
        
        // Clear out the list of devices with previously completed scans
        this.lstDevCompleted.clear();
                
        // Begin data acquisition for each active device
        for (WireScanner ws : lstDevs) {

            // Initiate data acquisition
            this.getLogger().logInfo(this.getClass(), "Sending CMD.SCAN to Wire Scanner " + ws.getId()); 

            // Send the SCAN command to the device
            switch (enmMode) {

            case EASY:
                ws.runCommand(WireScanner.CMD.EZ_SCAN);
                break;
                
            case EXPERT:
                ws.runCommand(WireScanner.CMD.XPRT_SCAN);
                break;
            }

            // Add the device to the list of active devices 
            //  (SCAN command was successful)
            this.lstDevScanning.add(ws);
            this.lstDevUnparked.add(ws);

        }

        // Notify all listeners that scan is initiating
        for (IScanControllerListener hndlr : this.lstLsnCtlEvts) {
            hndlr.scanInitiated(lstDevs, enmMode);
        }

        // Let everyone know we are in DAQ mode
        String strTmStart = "DAQ in progress: " + Calendar.getInstance().getTime().toString();
        this.getLogger().logInfo(this.getClass(), strTmStart);
    }
    
    /**
     * Clean up after a profile scan.  Kill
     * all the Channel Access monitor objects,
     * re-enable the scan button, then
     * notify all the scan event listeners that
     * the scan has completed.
     * 
     * @since  Nov 4, 2009
     * @author Christopher K. Allen
     */
    private void    scanCompleted() {

//        System.out.println("\nMainScanController#scanCompleted()");
//        this.printDeviceQueues();
//        this.printScanIds(lstDevCompleted);
    
//        // Clear out all monitors
//        this.mplScanEvts.emptyPool();   // CKA April 25, 2014
        
        // String notification
        getLogger().logInfo(this.getClass(), "DAQ CTRL - Scan completed and event notification: " + Calendar.getInstance().getTime().toString());

        // Notify the registered scan complete event handlers
        for (IScanControllerListener hndlr : lstLsnCtlEvts)
            hndlr.scanCompleted(lstDevCompleted);
    }
    
    /**
     * <p>
     * Forced quit of a DAQ scan in progress.
     * </p>
     * <p>
     * We clear all the active devices, shutdown
     * the monitors, and re-enable the scan button.
     * This is a hatchet job, and not thread-safe,
     * but hey, I'm just the computer. 
     * </p>
     * 
     * @since  Dec 3, 2009
     * @author Christopher K. Allen
     */
    private void scanTerminate() {
        
        this.lstDevScanning.clear();
        this.lstDevUnparked.clear();
        this.lstDevCompleted.clear(); // CKA April 25, 2014  
        this.mplScanEvts.emptyPool();
        
        this.getLogger().logInfo(getClass(), "DAQ scan prematurely terminated");

        // Clear scan-active flag
        MainScanController.BOL_SCAN_ACTIVE = false;
    }
    
    
    
    
    
    /*
     * Support Methods
     */
    
    /**
     * Returns the event logging object for the application
     * (available through the main window).
     *
     * @return  application event logger
     * 
     * @since  Nov 30, 2009
     * @author Christopher K. Allen
     */
    private IEventLogger getLogger() {
        return MainApplication.getEventLogger();
    }

    
}
