/**
 * MainHarpController.java
 *
 * Author  : Christopher K. Allen
 * Since   : Apr 23, 2014
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
import xal.smf.impl.WireHarp;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.scada.XalPvDescriptor;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Class which manages data acquisition (DAQ) for wire harp devices.
 * This class should typically be a singleton within an application. 
 * 
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Apr 23, 2014
 */
public class MainHarpController {

    /*
     * Inner Classes
     */

    /**
     * Interface defining events occurring during a data
     * acquisition request for a harp device.  Objects wishing to receive these
     * events should expose this interface then register 
     * with the <code>MainHarpController</code> object.
     *
     * @since  April 24, 2014
     * @author Christopher K. Allen
     */
    public interface IHarpControllerListener {
        
        /**
         * A data acquisition scan has been initiated.  The type of scan
         * being run is given.
         *
         * @param lstDevs       list of active DAQ devices       
         * @param cntSamples    the number of data sets to take during acquisition
         * 
         * @since  April 24, 2014
         * @author Christopher K. Allen
         */
        public void daqInitiated(List<WireHarp> lstDevs, int cntSamples);
        
        /**
         * Notifies listener that the given device has just acquired its 
         * <code>cntSample</code><sup><i>th</i></sup> data set.
         * 
         * @param smfHarp       DAQ device producing profile data
         * @param cntSample     the number of data sets sampled so far
         * 
         * @author Christopher K. Allen
         * @since  Apr 24, 2014
         */
        public void daqSampled(WireHarp smfHarp, int cntSample);
        
        /**
         * The data acquisition has completed.
         * 
         * @param lstDevs       list of all the acquisition devices that 
         *                      have completed successfully.
         * 
         * @since  April 24, 2014
         * @author Christopher K. Allen
         */
        public void daqCompleted(List<WireHarp> lstDevs);
        
        /**
         * The entire data acquisition scan has been aborted.
         *
         * 
         * @since  April 24, 2014
         * @author Christopher K. Allen
         */
        public void daqAborted();
        
        /**
         * The given device has reported a failure condition
         * during the data acquisition scan.
         *
         * @param smfDev
         * 
         * @since  April 24, 2014
         * @author Christopher K. Allen
         */
        public void daqDeviceFailure(WireHarp smfDev);
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
    private class SampleTakenAction implements SmfPvMonitor.IAction {

        /*
         * Instance Attributes
         */

        /** The harp device we are monitoring */
        private final WireHarp      smfHarp;

        /** The current data acquisition sample set */
        private int                 cntSmpl;

        /** The sample index (<i>N</i>-1, ..., 1, 0) for this thread (we monitor this value) */
        private int       indSmpl;
        
        /** The initializing event flag */
        private boolean             bolFirstEvent;
        
        /** The last sample event flag */
        private boolean             bolLastEvent;
        

        /**
         * Create a new <code>SampleTakenAction</code> object.
         *
         * @param actRevLim     the reverse limit monitor action for the same device 
         *
         * @since     Dec 10, 2009
         * @author    Christopher K. Allen
         */
        public SampleTakenAction(WireHarp smfHarp, int cntSamples) {
            this.smfHarp       = smfHarp;

            this.cntSmpl       = cntSamples;
            this.indSmpl       = 0;
            this.bolFirstEvent = true;
            this.bolLastEvent  = false;
        }

        /**
         * Skip the first event (the PV value initialization).  Next
         * event unblock the <code>ScannerController.RevLimitAction</code>
         * response for the device.  Then launch the <code>DeviceCompletedThread</code>
         * thread on the given device to check if it was the last
         * device to finish.
         *
         * @since   Dec 10, 2009
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
            
            if (this.bolLastEvent) {
                mon.clear();
                
                return;
            }

            // Check the count down, stop if we are at the last sample
            if (++this.indSmpl >= this.cntSmpl)
                this.bolLastEvent = true;
                
            Thread thdSmplTkn = new SampleTakenThread( this.smfHarp, this.indSmpl, this.bolLastEvent);

            thdSmplTkn.start();
        }
    }
    
    /**
     * <p>
     * Informs the <code>MainHarpController</code> singleton that another data set
     * has been acquired.  Because Channel Access is not reentrant we must spawn a
     * thread to unblock Channel Access and let any listeners do their thing.
     * </p>
     * <p>
     * This thread also does all the bookkeeping for the data acquisition process.
     * If the sample index is zero then the device has completed
     * all its samples. It is removed from the list of active devices and
     * placed in the list of completed devices.  If the list of
     * active devices is then empty, the Main Harp Controller is notified that
     * data acquisition is complete. 
     * </p> 
     *
     * @author Christopher K. Allen
     * @since  Apr 24, 2014
     */
    private class SampleTakenThread extends Thread {

        /*
         * Local Attributes
         */
        
        /** The DAQ device producing the sample */
        private final WireHarp  smfHarp;
        
        /** The number of samples taken so far (passed to daqSampleTaken() ) */
        private final int       indSmp;
        
        /** indicates that this is the last call for this DAQ sampling and the monitor should be terminated */
        private final boolean   bolStopMon;
        
        
        /**
         * Constructor for SampleTakenThread, initializes the local attributes.
         *
         * @param   indSmpl  the sample index handled by this thread
         * @param   smfHarp the DAQ device producing the sample
         *
         * @author Christopher K. Allen
         * @since  Apr 24, 2014
         */
        public SampleTakenThread(WireHarp smfHarp, int indSmp, boolean bolStopMon) {
            super();
            
            this.smfHarp = smfHarp;
            this.indSmp  = indSmp;
            this.bolStopMon = bolStopMon;
        }

        
        /*
         * Thread Overrides
         */
        
        /**
         *  Inform the Main Harp Controller that the harp device associated 
         *  with this thread has completed another sample. Check then if 
         *  the sample index is zero which means that the device has completed
         *  all its samples. If so, inform the main controller that the device is
         *  finished with its acquisition
         *  
         * @see java.lang.Thread#run()
         *
         * @author Christopher K. Allen
         * @since  Apr 24, 2014
         */
        @Override
        public void run() {

//            System.out.println("Inside MainHarpController.SampleTakenThread.run()");
//            System.out.println(" I am the Walrus " + this.smfHarp + " at sample #" + this.indSmp);
            
            MainHarpController.this.daqSampleTaken(this.smfHarp, this.indSmp);

            // If we are still monitoring the DAQ we can simply return
            if ( !this.bolStopMon ) 
                return;
            // Else we are finished with DAQ
            else
                MainHarpController.this.daqDeviceCompleted(this.smfHarp);
        }

    }

    
    /**
     * <p>
     * Reacts to errors reported by the 
     * any data acquisition error PV, 
     * calls the {@link MainHarpController#daqDeviceFailure(AcceleratorNode)}
     * method.
     * </p>
     * <p>
     * <h4>NOTE:</h4>
     * &middot; This class is currently unused.  No available error PVs on
     * the harps as of yet.
     * </p>
     *
     * @since  Dec 10, 2009
     * @author Christopher K. Allen
     */
    @SuppressWarnings("unused")
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
         * @since   Dec 10, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(gov.sns.ca.ChannelRecord, gov.sns.apps.pta.tools.ca.SmfPvMonitor)
         */
        @Override
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
                
                MainHarpController.this.daqDeviceFailure(mon.getDevice());
                
                String      strMsg = "Device Error Monitor action invoked for " + mon.getDevice().getId();
                
                System.err.println(strMsg);
                MainApplication.getEventLogger().logError(this.getClass(), strMsg);
                return;
            }
            
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
    private static MainHarpController          DAQ_CTRLR = null;
    
    /** DAQ-Active flag for controller.  Used as a mutex semaphore on DAQ resources */
    private static Boolean                     BOL_SCAN_ACTIVE = false;

    
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
    public static MainHarpController  getInstance() {
        if (DAQ_CTRLR != null)
            return DAQ_CTRLR;
        
        DAQ_CTRLR = new MainHarpController();
        
        return DAQ_CTRLR;
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
    
    //
    // RT: Action Event Handlers
    //
    /** Real Time: pool of scan monitors */
    private final SmfPvMonitorPool      mplSampleEvts;
    
    //
    // RT: Active DAQ Devices 
    //
    /** Real Time: List of active sampling devices - used for synchronization */
    private final List<WireHarp>        lstDevSampling;
    
    /** Real Time: List of devices that have completed a scan */
    private final List<WireHarp>        lstDevCompleted;

    // 
    // Callbacks and Notification
    //
    
    /** Registered handlers of the DAQ scan events */
    private final List<IHarpControllerListener>   lstLsnCtlEvts;

    

    /*
     * Initialization
     */
    
    /**
     * Create a new <code>MainHarpController</code> object which
     * operates devices obtained from the given device selection 
     * panel.
     *
     * @since     Sep 16, 2009
     * @author    Christopher K. Allen
     */
    private MainHarpController() {
        
        // The active scan lists
        this.lstDevSampling  = new LinkedList<WireHarp>();
        this.lstDevCompleted = new LinkedList<WireHarp>();
        
        // DAQ event monitors
        this.mplSampleEvts = new SmfPvMonitorPool();

        // Registered controller event listeners
        this.lstLsnCtlEvts = new LinkedList<IHarpControllerListener>();
    }

    
    /**
     * Register the given handler to receive the scan
     * initiated event.
     *
     * @param handler   event sink for scan starts
     * 
     * @since  Nov 14, 2009
     * @author Christopher K. Allen
     */
    public void registerControllerListener(IHarpControllerListener handler) {
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
    public void removeControllerListener(IHarpControllerListener lsnEvts) {
        this.lstLsnCtlEvts.remove(lsnEvts);
    }
    

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
     * Initiates the data acquisition.
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
     * @param cntSamples    the number of data sets to take during acquisition
     * 
     * @return  <code>true</code> if scan was successfully initiated, 
     *          <code>false</code> otherwise
     *  
     * @since  Nov 4, 2009
     * @author Christopher K. Allen
     */
    public boolean daqStart(List<WireHarp> lstDevs, int cntSamples) {
    
//        System.out.println("MainHarpController.daqStart(List<WireHarp>,int) just called - hi there.");
        
        // Check if there is anything to do
        if (lstDevs==null || lstDevs.size()==0)
            return false;

        // Notify all listeners that scan is initiating
        for (IHarpControllerListener hndlr : this.lstLsnCtlEvts) {
            hndlr.daqInitiated(lstDevs, cntSamples);
        }

        // Check if there is already a scan in progress
        //      If not, grab the SCAN lock  
        synchronized (BOL_SCAN_ACTIVE) {

            if (BOL_SCAN_ACTIVE)
                return false;


            // Initialize the scan - create monitoring threads
            String      strErrMsg = "Unable to create monitor pool for devices " + lstDevs;
            
            try {
                this.monitorInitiate(lstDevs, cntSamples);

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
                this.daqInitiate(lstDevs, cntSamples);

                //
                // Set the Active-Scan flag
                //
                BOL_SCAN_ACTIVE = true;
                
            } catch (InterruptedException e) {
                String      strMsg = "ERROR in DAQ start: Command buffer reset thread interrupted";

                getLogger().logException(this.getClass(), e, strMsg);
                this.daqTerminate();

            } catch (ConnectionException e) {
                String      strMsg = "ERROR in DAQ start: Unable to connect to device in " + lstDevs;

                this.getLogger().logException(this.getClass(), e, strMsg);
                this.daqTerminate();

            } catch (PutException e) {
                String      strMsg = "ERROR in DAQ start: Unable to set parameter in devices " + lstDevs;

                this.getLogger().logException(this.getClass(), e, strMsg);
                this.daqTerminate();

            }


//            // Set the active scan flag
//            BOL_SCAN_ACTIVE = true;
        }

        // Let everyone know we are in DAQ mode
        String strTmStart = "DAQ in progress: " + Calendar.getInstance().getTime().toString();
        this.getLogger().logInfo(this.getClass(), strTmStart);

//        System.out.println("MainHarpController.daqStart(List<WireHarp>,int) made it through successful.");
        
        return true;
    }

    
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
    public void daqAbort() {

        // This must be done atomically
        synchronized (BOL_SCAN_ACTIVE) {

            // Shutdown scan
            this.daqTerminate();

            // Make notifications of the aborted scan
            for (IHarpControllerListener hndlr : this.lstLsnCtlEvts) {
                hndlr.daqAborted();
            }
        }
        
        // Post log of aborted scan
        this.getLogger().logError(this.getClass(), "User abort during DAQ sampling");
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
    public void printDeviceIds(List<WireHarp> lstDevs) {
        try {
            System.out.println("Device scan IDs");
            for (WireHarp smfHarp : lstDevs) {

                WireHarp.DevStatus datStat = WireHarp.DevStatus.acquire(smfHarp);

                System.out.println("Device " + smfHarp.getId() + " wire saturation = " + datStat.statDev);
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
        System.out.println("Sampling devices  = " + this.lstDevSampling);
        System.out.println("Completed devices = " + this.lstDevCompleted);

    }


    /*
     * DAQ Support
     */
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
    private void monitorInitiate(List<WireHarp> lstDevs, int cntSmps) throws ConnectionException, MonitorException, NoSuchChannelException {

//        System.out.println("MainHarpController.monitorInitiate(List<WireHarp>, int) entrance");
        
        // Create all our internal scanning monitors
        for (WireHarp smfDev : lstDevs) {

            
            // Create the DAQ sample count monitor
            XalPvDescriptor      pvdSmpTk = ProfileDevice.ANGLE.HOR.getSignalValFd(WireHarp.DataRaw.class); 
//            SmfPvMonitor      monSmpTk = new SmfPvMonitor(smfDev, pvdSmpTk);
            SampleTakenAction actSmpTk = new SampleTakenAction(smfDev, cntSmps);
//            monSmpTk.addAction(actSmpTk);

//            System.out.println("  just made monitor for " + smfDev + " using PV Descriptor " + pvdSmpTk);
            
            this.mplSampleEvts.createMonitor(smfDev, pvdSmpTk, actSmpTk);
//            this.mplSampleEvts.addMonitor(monSmpTk);
            
        }

        // Start all the monitors
        this.mplSampleEvts.begin();
        
//        System.out.println("  just started the entire monitor pool " + this.mplSampleEvts);
    }
    
    
    /**
     *  <p>
     *  This method has been cut down substantially and 
     *  should probably be refactored out.
     *  </p>
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
    private void daqInitiate(List<WireHarp> lstDevs, int... cntSamples) throws InterruptedException, ConnectionException, PutException {

//        // This is the number of data sets we acquire, 
//        //      after decoding from the argument object
//        int     cntSmps = 0;
//
//        if (cntSamples == null)
//            cntSmps = 1;
//
//        if (cntSamples[0] == 0)
//            return;
//
//        cntSmps = cntSamples[0];
//

        // Clear out the list of devices with previously completed scans
        this.lstDevCompleted.clear();

        // Begin data acquisition for each active device
        for (WireHarp   smfHarp : lstDevs) {

            // Initiate data acquisition
            this.getLogger().logInfo(this.getClass(), "Initiating data acquisition for harp " + smfHarp.getId()); 

            // Add the device to the list of active devices 
            //  (SCAN command was successful)
            this.lstDevSampling.add(smfHarp);

        }

        //        // Notify all listeners that scan is initiating
        //        for (IHarpControllerListener hndlr : this.lstLsnCtlEvts) {
        //            hndlr.daqInitiated(lstDevs, cntSmps);
        //        }
        //
        //        // Let everyone know we are in DAQ mode
        //        String strTmStart = "DAQ in progress: " + Calendar.getInstance().getTime().toString();
        //        this.getLogger().logInfo(this.getClass(), strTmStart);
    }

    /**
     * Broadcast to all harp controller listeners that another sample
     * was taken for the given device.
     * 
     * @param smfHarp   device for which new data set is available
     * @param indSmp  the number of samples for the device taken so far
     *
     * @author Christopher K. Allen
     * @since  Apr 25, 2014
     */
    private void daqSampleTaken(WireHarp smfHarp, int cntSmpls) {
        
//        System.out.println("MainHarpController.daqSampleTaken() - firing daqSampled(dev,cnt) event");
        
        for (IHarpControllerListener lsnr : this.lstLsnCtlEvts) {
            lsnr.daqSampled(smfHarp, cntSmpls);
        }
    }
    
    /**
     * Respond to DAQ device failure.
     *
     * @param smfDev
     * 
     * @since  April 25, 2014
     * @author Christopher K. Allen
     */
    private void daqDeviceFailure(AcceleratorNode smfDev) {

        if ( !(smfDev instanceof WireHarp) ) 
            return;

        WireHarp smfHarp  = (WireHarp)smfDev;
        
        // Remove all the monitors associated with this device
        this.mplSampleEvts.stopActive(smfDev);
        

        // Remove the device from the active sampling list and completed list.
        this.lstDevSampling.remove(smfHarp);
        this.lstDevCompleted.remove(smfHarp);

        
        this.getLogger().logError(this.getClass(), "HARP DEVICE FAILURE: deviceFailure() for " + smfDev.getId());
        
        // Notify all the registered listeners
        for (IHarpControllerListener hndler : this.lstLsnCtlEvts) 
            hndler.daqDeviceFailure(smfHarp);
        
    }
    
    /**
     * <p>
     * The given device has completed
     * all its samples. Thus, remove it from the list of active devices and
     * place it in the list of completed devices.  If the list of
     * active devices is then empty, notify the Main Harp Controller that
     * data acquisition is complete.     
     * </p>
     * 
     * @param smfHarp   the profile device that has just finished its acquisition
     *
     * @author Christopher K. Allen
     * @since  May 1, 2014
     */
    private void daqDeviceCompleted(WireHarp smfHarp) {
        
        // This is the last sample, we need to pull this device off the active device list
        //  This action must be done atomically
        synchronized (BOL_SCAN_ACTIVE) {

            // Remove the device from active device list
            //  If the device was not in the list (???)
            //  then something is wrong - just ignore it for now.
            lstDevSampling.remove(smfHarp);

            // Place it in the list of completed devices
            lstDevCompleted.add(smfHarp);

            // If there are more active devices then return
            if (lstDevSampling.size() > 0) 
                return;

        }
        
        // No more active devices, do the DAQ complete book keeping
        MainHarpController.this.daqCompleted();
    }

    /**
     * <p>
     * Clean up after data acquisition.  Kill
     * all the Channel Access monitor objects,
     * then notify all the DAQ event listeners that
     * the DAQ has completed.
     * </p>
     * 
     * @since  April 25, 2014
     * @author Christopher K. Allen
     */
    private void    daqCompleted() {
    
        synchronized(BOL_SCAN_ACTIVE) {

            // Clear out all monitors
            this.mplSampleEvts.emptyPool();   // CKA April 25, 2014

            // Clear scan-active flag
            BOL_SCAN_ACTIVE = false;
        }

        // String notification
        getLogger().logInfo(this.getClass(), "HARP DAQ completed and event notification: " + Calendar.getInstance().getTime().toString());
    
        // Notify the registered scan complete event handlers
        for (IHarpControllerListener hndlr : lstLsnCtlEvts)
            hndlr.daqCompleted(lstDevCompleted);
    }


    /**
     * <p>
     * Forced quit of a DAQ sampling in progress.
     * </p>
     * <p>
     * We clear all the active devices, shutdown
     * the monitors, and re-enable the scan button.
     * This is a hatchet job, and not thread-safe,
     * but hey, I'm just the computer. 
     * </p>
     * 
     * @since  April 25, 2014
     * @author Christopher K. Allen
     */
    private void daqTerminate() {
        
        this.lstDevSampling.clear();
        this.lstDevCompleted.clear();
        this.mplSampleEvts.emptyPool();
        
        this.getLogger().logInfo(getClass(), "DAQ scan prematurely terminated");

        // Clear scan-active flag
        BOL_SCAN_ACTIVE = false;
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
