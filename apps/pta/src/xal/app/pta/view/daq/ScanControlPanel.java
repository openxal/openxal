/**
 * DataAcquisitionPanel.java
 *
 *  Created	: Sep 16, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.daq;

import xal.app.pta.MainScanController;
import xal.app.pta.MainApplication;
import xal.app.pta.MainDocument;
import xal.app.pta.MainScanController.IScanControllerListener;
import xal.app.pta.MainScanController.MOTION_STATE;
import xal.app.pta.MainScanController.SCAN_MODE;
import xal.app.pta.daq.ScannerData;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.PtaResourceManager;
import xal.app.pta.tools.ca.SmfPvMonitor;
import xal.app.pta.tools.ca.SmfPvMonitorPool;
import xal.app.pta.tools.logging.IEventLogger;
import xal.ca.BadChannelException;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.MonitorException;
import xal.service.pvlogger.PvLoggerException;
import xal.smf.AcceleratorNode;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireScanner;
import xal.smf.impl.WireScanner.ScanConfig;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.scada.XalPvDescriptor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * <p>
 * Controls diagnostic hardware to perform data 
 * acquisition.
 * </p>
 * <p>
 * Note that this class does no data acquisition itself,
 * that is, it only operates the hardware.  You must
 * retrieve the data else where once the scan is complete. 
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Sep 16, 2009
 * @author Christopher K. Allen
 */
public class ScanControlPanel extends JPanel implements IScanControllerListener {


    /**
     * <p>
     * Monitors of the actuation motion send their events to one
     * of these.  There should be one of this class for each
     * actuator in motion.
     * </p>  
     * <p>
     * Each new scanning process needs
     * to fire up one of these event sinks, which has a dynamic
     * state.  After spawning we register ourself with the DACQ
     * controller, that is, let it know we are alive.  When the
     * scanning process completes, this object alerts the 
     * DACQ controller.  (Thus, once all the motion status
     * processes have finished, then we can allow for another scan.)
     * </p>
     *
     * @since  Nov 4, 2009
     * @author Christopher K. Allen
     */
    class MotionStatusAction implements SmfPvMonitor.IAction {
        


        /*
         * Local Attributes
         */
        
        /** The progress panel on the controller GUI */
        private final DeviceProgressPanel       pnlProg;
        
        /** Dynamic state variable - active state value */
        private MOTION_STATE                 stateCurr;
        
        
        /*
         * Initialization
         */
        
        /**
         * Create a new <code>StatusMonitor</code> object.
         *
         * @param pnlProg       the GUI controller panel
         *
         * @since     Nov 4, 2009
         * @author    Christopher K. Allen
         */
        public MotionStatusAction(DeviceProgressPanel pnlProg) {
            this.pnlProg = pnlProg;
            this.stateCurr = MOTION_STATE.UNKNOWN;
        }


        /**
         * Responds to a channel in the motion PV of the
         * device associated with this object. 
         *
         * @since 	Nov 4, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
         */
        public void valueChanged(ChannelRecord recValue, SmfPvMonitor mon) {

            // Check if we are already in a failed state - if so remain there
            if (this.stateCurr == MOTION_STATE.FAIL)
                return;
            
            // Get the current motion state from the CA record and respond
            MOTION_STATE    stateNew = MOTION_STATE.getState(recValue.intValue());

//            System.out.println("MotionStatusAction value is " + stateNew.name());

            switch (stateNew) {
            case HALTED:
                this.stateCurr = MOTION_STATE.HALTED;
                this.pnlProg.setMotionState(mon.getDevice(), MOTION_STATE.HALTED);
                break;

            case MOVING:
                this.stateCurr = MOTION_STATE.MOVING;
                this.pnlProg.setMotionState(mon.getDevice(), MOTION_STATE.MOVING);
                break;

            case FAIL:
                this.stateCurr = MOTION_STATE.FAIL;
                this.pnlProg.setMotionState(mon.getDevice(), MOTION_STATE.FAIL);
                break;
                
            case LOCKED:
                break;
                
            case UNKNOWN:
                break;
            }
        }

    }

    
    /**
     * Monitors of actuator movements should send their
     * events to one of these.  It will update the progress
     * bar associated with this object.
     *
     * @since  Nov 4, 2009
     * @author Christopher K. Allen
     */
    class PositionChangeAction implements SmfPvMonitor.IAction {

        
        /*
         * Instance Attributes
         */

        /** The DAQ controller GUI panel */
        private final DeviceProgressPanel        pnlProg;
        
        
        /**
         * Create a new <code>PositionMonitor</code> object for
         * monitoring the given data acquisition device.
         *
         * @param pnlProg       the progress panel where the updates go
         *
         * @since     Nov 4, 2009
         * @author    Christopher K. Allen
         */
        public PositionChangeAction(DeviceProgressPanel pnlProg) {
            this.pnlProg = pnlProg;
        }


        /**
         * Send the new DAQ device actuator position to the
         * progress panel.
         *
         * @param recValue      new position value 
         * @param mon           monitor on the DAQ device position PV
         *  
         * @since 	Nov 4, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction
         */
        public void valueChanged(ChannelRecord recValue, SmfPvMonitor mon) {
            Integer     intValue = recValue.intValue();
            
            this.pnlProg.setProgressValue(mon.getDevice(), intValue);
//            
//            System.out.println("Scan Monitor:Position = " + dblVal);
        }
        
    }


    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;

    
    /** The 'do connection test' flag */
    private static final boolean BOL_CONN_TST = AppProperties.DEVICE.EPICS_CA_CHK.getValue().asBoolean();
    
    /** The connection test device time out */
    private static final double  DBL_CONN_TMO = AppProperties.DEVICE.TMO_CONNTEST.getValue().asDouble();
    
    
    /** Show the EASY scan warning (clobbers device configuration) */
    private static final boolean BOL_WRN_EZSCAN = AppProperties.DAQGUI.WARN_EZSCAN.getValue().asBoolean();
    

    
    /*
     * Global Variables
     */

    /** This flag is set false after the first warning is displayed, preventing further warnings */
    private static boolean  BOL_EZSCAN_WARNED = false;
    
    
    
    /*
     * Instance Attributes
     */
    
    
    /*
     * Back References 
     */
    
    /** Reference to the main data document */
    private final MainDocument          docMain;
    
    /** The application's DAQ central controller */
    private final MainScanController         ctlDaq;
    


    /*
     * GUI Components 
     */
    
    /** initiate (expert) scan button */
    private JButton     butScanXp;
    
    /** initiate EZ scan button */
    private JButton     butScanEz;
    
    /** stops the actuator during a scan */
    private JButton     butStop;
    
    /** abort the current profile scan */
    private JButton     butAbort;
    
    /** move the actuate to the home position */
    private  JButton    butPark;
    
    /** Update data button */
    private JButton     butAcquire;
    
    /** the DACQ progress read back */
    private DeviceProgressPanel   pnlDaqProgress;
    

    /*
     * DAQ Tools
     */
   
    /** List of currently selected devices for data acquisition */
    private List<WireScanner>           lstSelDevs;
    
    /** List of currently selected devices with connection problems */
    private List<WireScanner>           lstMalDevs;
    
    /** pool of PV monitors active during scans */
    private final SmfPvMonitorPool      mplScan;
    
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>DataControlPanel</code> object which
     * operates devices obtained from the given device selection 
     * panel.
     *
     * @param docMain           the application main document
     *
     * @since     Sep 16, 2009
     * @author    Christopher K. Allen
     */
    public ScanControlPanel() {
//        this.docMain = docMain;
        this.docMain = MainApplication.getApplicationDocument();
        this.ctlDaq  = MainScanController.getInstance();
        this.ctlDaq.registerControllerListener(this);
        
        this.lstSelDevs = null;
        this.lstMalDevs = new LinkedList<WireScanner>();
        
        this.mplScan = new SmfPvMonitorPool();
        
        // Build the GUI
        this.buildGuiComponents();
        this.buildEventActions();
        this.layoutGuiPanel();
    }

    
    /**
     * Responds to a new device selection event.  Sets
     * the selected lists of devices.
     *
     * @param lstDevs   current selection set of DAQ devices 
     *  
     * @since   Nov 17, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.cmn.DeviceSelectorPanel.IDeviceSelectionListener#newDeviceSelection(java.util.List)
     */
    public void setDaqDevices(List<WireScanner> lstDevs) {
        this.lstSelDevs = lstDevs;
        
        this.lstMalDevs.clear();
        this.pnlDaqProgress.clear();

        this.enableScanAndDaqButtons(true);
    }

    /**
     * Clears the list of DAQ devices under management.
     *
     * 
     * @since  Apr 8, 2010
     * @author Christopher K. Allen
     */
    public void clearDevices() {
        this.lstSelDevs = null;
        this.pnlDaqProgress.clear();
        
        this.enableScanAndDaqButtons(false);
        this.enableAbortAndReturnButtons(false);
    }
    
    
    /*
     * Query
     */
    

    
    /*
     * IScanControllerListener
     */

    /**
     * Enable and disable the GUI control buttons according to
     * the state of the DAQ controller.
     * 
     * @param   lstDevs     list of currently active DAQ devices
     * @param   mode        the scan mode requested (expert (default), or
     *
     * @since   Dec 11, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanInitiated(java.util.List, SCAN_MODE)
     */
    @Override
    public void scanInitiated(List<WireScanner> lstDevs, SCAN_MODE mode) {
        this.enableAbortAndReturnButtons(true);
    }

    /**
     * Acknowledge that the data acquisition
     * portion of the scan has completed then acquire the profile
     * data from the device and set it to the application main data.
     *
     * @since   Dec 11, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanCompleted(java.util.List)
     */
    @Override
    public void scanCompleted(List<WireScanner> lstDevScan) {

//        this.enableAbortAndReturnButtons(false);
        
        // String notification
        this.updateStatusComment("Scan completed");

        //
//        // Save the measurement data to the main application document
//        try {
//            
//            // First loosen the device data type to fit into MeasurementData DAQ process
//            List<ProfileDevice> lstDevProf = new LinkedList<ProfileDevice>( lstDevScan );
//            
//            MeasurementData  setMsmt = MeasurementData.acquire(lstDevProf);
//
//            this.docMain.setMeasurementData(setMsmt);
//            
//        } catch (ConnectionException e) {
//            getLogger().logException(getClass(), e, "DAQ Failure: unable to connect to a device in " + this.lstSelDevs);
//            JOptionPane.showMessageDialog(this, "Error in data qcquisition - see log", "WARNING", JOptionPane.WARNING_MESSAGE);
//            
//        } catch (GetException e) {
//            getLogger().logException(getClass(), e, "DAQ Failure: unable to read from a device in " + this.lstSelDevs);
//            JOptionPane.showMessageDialog(this, "Error in data qcquisition - see log", "WARNING", JOptionPane.WARNING_MESSAGE);
//            
//        } catch (PvLoggerException e) {
//            getLogger().logException(getClass(), e, "Unable to take PV Logger snapshot for measurement " + this.lstSelDevs);
//            JOptionPane.showMessageDialog(this, "Error in PV Logger capture - see log", "WARNING", JOptionPane.WARNING_MESSAGE);
//            
//        }
    }

    /**
     * Clean up after a profile scan.  Kill
     * all the Channel Access monitor objects,
     * re-enable the scan button, then
     * notify all the scan event listeners that
     * the scan has completed.
     *
     * @since   Dec 11, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanActuatorsParked()
     */
    @Override
    public void scanActuatorsParked() {

//        System.out.println("ScanControlPanel#scanActuatorsParked() - method called");
        
        this.mplScan.emptyPool();

//        System.out.println("ScanControlPanel#scanActuatorsParked() - past this.mplScan.emptyPool()");
        
        // String notification
        this.updateStatusComment("Scan actuators parked");

        // Reset the control button for another scan
        this.enableAbortAndReturnButtons(false);
        this.enableScanAndDaqButtons(true);
    }


    /**
     * Acknowledge the failure of a scanning DAQ device
     *
     * @since   Dec 11, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanDeviceFailure(xal.smf.impl.WireScanner)
     */
    @Override
    public void scanDeviceFailure(WireScanner smfDev) {
        this.ctlDaq.scanAbort();
        this.scanTerminate();
        this.updateStatusComment("Device Failure: " + smfDev.getId());
    }


    /**
     * Clean up an aborted scan.  Kill scan
     * monitors, acknowledge the abort, and
     * re-enable the scan button.
     *
     * @since   Dec 11, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanAborted()
     */
    @Override
    public void scanAborted() {

//        this.mplScan.emptyPool();
        
        // String notification
        this.updateStatusComment("Scan aborted");

        // Reset the control buttons for another scan
        this.enableAbortAndReturnButtons(false);
        this.enableScanAndDaqButtons(true);
    }


    /**
     * There is nothing really to do here, we
     * just warning the user about the dangerous
     * of stopping the scan.
     * 
     * @since   Apr 1, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanActuatorsStopped()
     */
    @Override
    public void scanActuatorsStopped() {

//        this.butStop.setEnabled(false);
        
        // String notification
        this.updateStatusComment("WARNING: Scan actuators stopped in beam pipe!");

    }
    

    
    /*
     * Event Response Methods
     */
    
    /**
     * <p>
     * Initialize the data acquisition scan.
     * </p>
     * <p>
     * Called by the <tt>Scan</tt> button event action.  The list
     * of DAQ devices (<code>this.lstDevSelected</code> should
     * have been set previously by a call to the method
     * <code>setDaqDevices(List<AcceleratorNode>)</code>.  If
     * no call has been made then there is nothing to do and
     * we simply return.
     * </p>
     * 
     * @param   enmMode     The type of scan to run (EASY or EXPERT)
     * 
     * @since  Nov 4, 2009
     * @author Christopher K. Allen
     */
    private void evtScanStart(MainScanController.SCAN_MODE enmMode) {
        // Check if there is anything to do
        if (this.lstSelDevs==null || this.lstSelDevs.size()==0)
            return;

        // Disable scan button until finished
        this.enableScanAndDaqButtons(false);

        // Check the connections between the devices, removing devices that fail connection test
        if (BOL_CONN_TST)
            if (!this.checkDeviceConnections()) {
                this.scanTerminate();
                return;
            }
        
        // Initialize the progress panel GUI
        if (!this.scanInitProgressPanel()) {
            this.getLogger().logError(getClass(), "Failed to initialize progress panel for DAQ");
            this.scanTerminate();
            return;
        }
        
        // Initialize the scan - create monitoring threads
        if (!this.monitorStartup()) {
            this.getLogger().logError(getClass(), "Failed to initialize progress monitors for DAQ");
            this.scanTerminate();
            return;
        }
        
        // Begin the DAQ scan
        if (!this.scanInitiate(enmMode)) {
            this.getLogger().logError(getClass(), "Failed to initiate scan sequence for DAQ");
            this.scanTerminate();
            return;
        }
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
    private void evtScanAbort() {
        this.ctlDaq.scanAbort();
    }


    /**
     * Parks the DAQ actuators in 
     * the home position.
     *
     * 
     * @since  Dec 3, 2009
     * @author Christopher K. Allen
     */
    private void evtActuatorPark() {
        this.ctlDaq.scanActuatorsPark();
    }
    
    /**
     * Stops the actuator during a scan.  This
     * leaves the actuators at their current positions.
     *
     * 
     * @since  Dec 3, 2009
     * @author Christopher K. Allen
     */
    private void evtActuatorStop() {
        this.ctlDaq.scanActuatorsStop();
    }
    
    /**
     * Acquires the data from the currently selected
     * acquisition devices (i.e., those of <code>this.lstDevSelected</code>)
     * then sets the data into the main application document.
     * 
     * @since  Mar 22, 2010
     * @author Christopher K. Allen
     */
    private void evtAcquireData() {

        // See if there is something to do
        if (this.lstSelDevs==null || this.lstSelDevs.size()==0)
            return;
        
        // Check the connections between the devices, removing devices that fail connection test
        if (BOL_CONN_TST)
            if (!this.checkDeviceConnections()) 
                this.getLogger().logWarning(this.getClass(), "Not all devices are available in selection list " + this.lstSelDevs);
        
        // Save the measurement data to the main application document
        try {
            
            // First loosen the device data type to fit into MeasurementData DAQ process
            List<ProfileDevice> lstDevProf = new LinkedList<ProfileDevice>( this.lstSelDevs );
            
            MeasurementData  setMsmt = MeasurementData.acquire( lstDevProf );

            this.docMain.setMeasurementData(setMsmt);
            this.updateStatusComment("Acquired scanner data");
            
        } catch (ConnectionException e) {
            this.getLogger().logException(getClass(), e, "Static Acquisition: Unable to connect to " + this.lstSelDevs);
            JOptionPane.showMessageDialog(this, "Error in data qcquisition - see log", "WARNING", JOptionPane.WARNING_MESSAGE);

        } catch (GetException e) {
            this.getLogger().logException(getClass(), e, "Static Acquisition: Missing or correct data in " + this.lstSelDevs);
            JOptionPane.showMessageDialog(this, "Error in data qcquisition - see log", "WARNING", JOptionPane.WARNING_MESSAGE);

        } catch (PvLoggerException e) {
            this.getLogger().logException(getClass(), e, "Unable to take PV logger snapshot for measurement" + this.lstSelDevs);
            JOptionPane.showMessageDialog(this, "Error in PV Logger snapshot - see log", "WARNING", JOptionPane.WARNING_MESSAGE);

        }
    }

    
    
    /*
     * DAQ Support
     */
    
    /**
     * <p>
     * Checks all the data acquisition channel connections to the list 
     * of currently selected devices (i.e., those found in the member 
     * <code>{@link DaqControllerPanel#lstDevs}</code>).  If the connections
     * fail for a device, <em>then that device is removed from the
     * internal list of selected devices.</em>
     * </p>
     * <p>
     * In addition, this method returns a boolean flag indicating whether or not
     * all the connections where made.  Specifically, if the returned value
     * is <code>true</code> than all connections were made for all devices.  If the
     * returned value is <code>false</code>, then at least one device failed 
     * to fully connect. 
     * </p>
     *      
     * @return  <code>true</code> if all connections were made for the current device list,
     *          <code>false</code> if at least one connection failed.
     *
     * @author Christopher K. Allen
     * @since  Mar 16, 2011
     */
    private boolean checkDeviceConnections() {

        // We re-check the malfunctioning devices
        this.lstSelDevs.addAll( this.lstMalDevs );
        this.lstMalDevs.clear();
        
        // Overall result of connection test
        boolean bolResult = true;

        // Test connections for each selected device
        for (AcceleratorNode smfNode : this.lstSelDevs) {
            
            // Make sure node is the right type
            if ( !(smfNode instanceof WireScanner ) ) {
                this.lstSelDevs.remove(smfNode);
                continue;
            }
            
            WireScanner ws = (WireScanner)smfNode;
            
            // Test the channel connections for the device
            try {
                
                // If all connections check out move to the next one
                if (ScannerData.testConnection(ws, DBL_CONN_TMO) == true)
                    continue;

            } catch (BadChannelException e) {
                // Worse than no connection - a channel is unbound
                this.getLogger().logException(this.getClass(), e, "Channel unbound for device " + ws.getId() );
//                JOptionPane.showMessageDialog(this, "Unbound channel - see log", "WARNING", JOptionPane.WARNING_MESSAGE);
                
                // Otherwise add it to the malfunctioning device list
                this.lstMalDevs.add(ws);
                
                // flag the failure
                bolResult = false;
            }
        }
        
        // Remove the devices with connection malfunctions
        this.lstSelDevs.removeAll(this.lstMalDevs);

        // If all connections are valid return true
        if (bolResult == true)
            return true;
        
        // If there was a connection error ask the user what to do
        String strMsg = "Connection malfunction detected in set " + this.lstMalDevs;

        this.getLogger().logWarning(this.getClass(), strMsg);

        strMsg += ". Continue with acquisition?"; 
        int intResponse = JOptionPane.showConfirmDialog(this, strMsg, "Connection Error?", JOptionPane.YES_NO_OPTION);
        
        if (intResponse == JOptionPane.NO_OPTION) 
            return false;

        return true;
    }
    
    
    /**
     * Initializes the system for the DAQ
     * monitors prior to a DAQ scan.  
     *
     * @return  <code>true</code> if all monitors were successfully started,
     *          <code>false</code> otherwise  
     * 
     * @since  Dec 3, 2009
     * @author Christopher K. Allen
     */
    private boolean monitorStartup() {
        
        // Clear out any remaining monitors (e.g., from an abort event)
        this.mplScan.emptyPool();
        
        // PVs to monitor
        XalPvDescriptor    pvdMoStat = WireScanner.DevStatus.FLD_MAP.get("mvtStatus");
        XalPvDescriptor    pvdPosVal = WireScanner.DevStatus.FLD_MAP.get("wirePos");
        
        // For each device add monitors into pool
        for (AcceleratorNode smfDev : this.lstSelDevs) {

            if ( !(smfDev instanceof WireScanner) )
                continue;
            
            MotionStatusAction actMoStat = new MotionStatusAction(this.pnlDaqProgress);
            this.mplScan.createMonitor(smfDev, pvdMoStat, actMoStat);

            PositionChangeAction actPosVal = new PositionChangeAction(this.pnlDaqProgress);
            this.mplScan.createMonitor(smfDev, pvdPosVal, actPosVal);
        }

        
        // Fire up the PV monitors
        try {
            this.mplScan.begin();
            
        } catch (ConnectionException e) {
            getLogger().logException(getClass(), e, "unable to start monitor pool");
            return false;

        } catch (MonitorException e) {
            getLogger().logException(getClass(), e, "unable to start monitor pool");
            return false;
            
        } catch (NoSuchChannelException e) {
            getLogger().logException(getClass(), e, "unable to start monitor pool");
            return false;
            
        }
        
        return true;
    }
    
    
    
    /**
     * Initialize the scan progress GUI panel before
     * a scan begins.
     *
     * @return
     * 
     * @since  Dec 3, 2009
     * @author Christopher K. Allen
     */
    private boolean scanInitProgressPanel() {
        
        // Set the currently active devices and notify listeners
        this.pnlDaqProgress.addAllDaqHardware( new LinkedList<AcceleratorNode>(this.lstSelDevs) );
        
        // For each device begin the acquisition process
        for (AcceleratorNode smfDev : this.lstSelDevs) {

            if ( !(smfDev instanceof WireScanner) )
                continue;
            
            WireScanner ws  = (WireScanner)smfDev;

            
            // Retrieve the scan configuration parameters and initialize the progress panel
            try {
                ScanConfig          cfgScan   = WireScanner.ScanConfig.acquire(ws);
                Double              dblPosMax = cfgScan.lngScan;
                int                 intPosMax = dblPosMax.intValue(); 
                
                this.pnlDaqProgress.initProgress(ws, intPosMax);
                
            } catch (ConnectionException e) {
                getLogger().logException(this.getClass(), e);
                return false;
                
            } catch (GetException e) {
                getLogger().logException(this.getClass(), e);
                return false;
                
            } catch (NoSuchChannelException e) {
                getLogger().logException(this.getClass(), e);
                return false;
                
            }
        }
        
        return true;
    }
    
    /**
     * <p>
     * Begins the data acquisition scan by
     * send the <tt>SCAN</tt> command to
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
     * 
     * @param enmMode   the type of scan to use (EASY or EXPERT)
     *  
     * @return  <code>true</code> if the scan was successfully initiated,
     *          <code>false</code> upon failure within  
     *          the application's <code>ScannerController</code> object 
     * 
     * @since  Dec 3, 2009
     * @author Christopher K. Allen
     */
    private boolean scanInitiate(MainScanController.SCAN_MODE enmMode) {
        
        // Enable abort scan buttons until finished
        this.enableAbortAndReturnButtons(true);
        
        // Let everyone know we are in DAQ mode
        String strTmStart = "DAQ in progress: " + Calendar.getInstance().getTime().toString();
        this.updateStatusComment(strTmStart);
        
        // Order the scan
        return this.ctlDaq.scanStart(this.lstSelDevs, enmMode);
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
        String  strMsg = "DAQ scan prematurely terminated";

        this.updateStatusComment(strMsg);
        this.getLogger().logInfo(getClass(), strMsg);
        
        this.mplScan.emptyPool();

        this.enableScanAndDaqButtons(true);
        
    }
    
    
    /*
     * GUI Support
     */
    
    /**
     * Creates the GUI components for DAQ device
     * control.
     *
     * 
     * @since  Dec 3, 2009
     * @author Christopher K. Allen
     */
    private void buildGuiComponents() {

        // Create the DAQ progress display panel 
        this.pnlDaqProgress = new DeviceProgressPanel();
        

        // Create the DAQ control buttons
        String  strPathIconStart = AppProperties.ICON.SCAN_START.getValue().asString(); 
        this.butScanXp  = new JButton(" Start Scan ",
                          PtaResourceManager.getImageIcon(strPathIconStart) );
        
        String  strPathIconEasy = AppProperties.ICON.SCAN_EASY.getValue().asString();
        this.butScanEz    = new JButton(" Easy Scan ", 
                          PtaResourceManager.getImageIcon(strPathIconEasy) );
        
        String  strPathIconAbort = AppProperties.ICON.SCAN_ABORT.getValue().asString();
        this.butAbort = new JButton(" Abort Scan",
                        PtaResourceManager.getImageIcon(strPathIconAbort) );
        
        String  strPathIconStop = AppProperties.ICON.SCAN_STOP.getValue().asString();
        this.butStop  = new JButton(" Stop Fork   ",
                        PtaResourceManager.getImageIcon(strPathIconStop) );
        
        String strPathIconPark = AppProperties.ICON.SCAN_PARK.getValue().asString();
        this.butPark  = new JButton(" Park              ",
                        PtaResourceManager.getImageIcon(strPathIconPark) );
                        
        String  strPathIconAcquire = AppProperties.ICON.DAQ_ACQUIRE.getValue().asString();
        this.butAcquire = new JButton(" (re)Acquire ",
                        PtaResourceManager.getImageIcon(strPathIconAcquire) );

        this.enableScanAndDaqButtons(false);
        this.enableAbortAndReturnButtons(false);
//        this.butScanXp.setEnabled(false);
//        this.butScanEz.setEnabled(false);
//        this.butAbort.setEnabled(false);
//        this.butAcquire.setEnabled(false);
//        this.butPark.setEnabled(false);
//        this.butStop.setEnabled(false);
    }
    
    /**
     * Sets the response actions to the data acquisition
     * user events.
     * 
     * @since  Oct 29, 2009
     * @author Christopher K. Allen
     */
    private void buildEventActions(){

        butScanXp.addActionListener(
                        new ActionListener() {
                            @SuppressWarnings("synthetic-access")
                            public void actionPerformed(ActionEvent e) {
                                // Run the #evtScanStart() method as a thread so we can release this callback
                                new Thread( new Runnable() { 
                                    @Override
                                    public void run() {
                                        ScanControlPanel.this.evtScanStart(MainScanController.SCAN_MODE.EXPERT);
                                    }
                                }
                                ).start();
                            }
                        }
        ); 
        
        butScanEz.addActionListener(
                        new ActionListener() {
                            @SuppressWarnings("synthetic-access")
                            public void actionPerformed(ActionEvent e) {
                                if (ScanControlPanel.this.displayEasyScanWarning())
                                    // Run the #evtScanStart() method as a thread so we can release this callback
                                    new Thread( new Runnable() { 
                                        @Override
                                        public void run() {
                                            ScanControlPanel.this.evtScanStart(MainScanController.SCAN_MODE.EASY);
                                        }
                                    }
                                    ).start();
                            }
                        }
        );

        butAbort.addActionListener( 
                        new ActionListener() {
                            @SuppressWarnings("synthetic-access")
                            public void actionPerformed(ActionEvent e) {
                                evtScanAbort();
                            }
                        }
        );

        butPark.addActionListener(
                        new ActionListener() {
                            @SuppressWarnings("synthetic-access")
                            public void actionPerformed(ActionEvent e) {
                                evtActuatorPark();
                            }
                        }
        );

        butStop.addActionListener(
                        new ActionListener() {
                            @SuppressWarnings("synthetic-access")
                            public void actionPerformed(ActionEvent e) {
                                evtActuatorStop();
                            }
                        }
        );

        butAcquire.addActionListener(
                        new ActionListener() {
                            @SuppressWarnings("synthetic-access")
                            public void actionPerformed(ActionEvent e) {
                                // Run the #evtAcquireData() method as a thread so we can release this callback
                                new Thread( new Runnable() { 
                                    @Override
                                    public void run() {
                                        ScanControlPanel.this.evtAcquireData();
                                    }
                                }
                                ).start();
                            }
                        }
        );
    }

    /**
     * Builds the visible GUI panel that receives
     * user data acquisition commands.
     * 
     * @since  Oct 29, 2009
     * @author Christopher K. Allen
     */
    private void layoutGuiPanel() {

        Box     boxDacqCmd = Box.createVerticalBox();
        boxDacqCmd.add(this.butScanXp );
        boxDacqCmd.add( Box.createVerticalStrut(5) );
        boxDacqCmd.add(this.butScanEz);
        boxDacqCmd.add( Box.createVerticalStrut(5) );
        boxDacqCmd.add( this.butAbort );
        boxDacqCmd.add( Box.createVerticalStrut(5) );
        boxDacqCmd.add( this.butStop );
        boxDacqCmd.add( Box.createVerticalStrut(5) );
        boxDacqCmd.add( this.butPark );
        boxDacqCmd.add( Box.createVerticalStrut(5) );
        boxDacqCmd.add( this.butAcquire );
        
        
        Box     boxDacq = Box.createHorizontalBox();
        boxDacq.add(boxDacqCmd);
        boxDacq.add( Box.createHorizontalStrut(10) );
        boxDacq.add(this.pnlDaqProgress);
        boxDacq.setBorder(new TitledBorder("Data Acquisition"));
        
        this.add(boxDacq);
    }
    
    
    

    /*
     * Support Methods
     */
    
    /**
     * Sets the string displayed in the text box of this panel
     * that is used for displaying data acquisition
     * status.
     *
     * @param strCmt    new status comment to display
     * 
     * @since  Oct 28, 2009
     * @author Christopher K. Allen
     */
    private void updateStatusComment(String strCmt) {
        this.pnlDaqProgress.updateComment(strCmt);
//        System.out.println(strCmt);
    }
    
    /**
     * <p>
     * Displays the EASY scan warning message.  Notifies the user that
     * running an easy scan will over write all the existing configuration
     * parameters for the devices about to be scanned.
     * </p>
     * <p>
     * The use will only be warned once per session.  That is, the class will
     * issue only one warning.
     *
     * @return  <code>true</code> if the user confirms the scan or has already been confirmed,
     *          <code>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since  Feb 28, 2012
     */
    private boolean displayEasyScanWarning() {
        // Check if are supposed to warn about the EASY scan
        if (!ScanControlPanel.BOL_WRN_EZSCAN)
            return true;
        
        // Check if we have already warned user about EASY scan
        if (ScanControlPanel.BOL_EZSCAN_WARNED)
            return true;
        
        // Warn user about the EASY scan
        int intConfirm = JOptionPane.showConfirmDialog(this, 
                "All device configurations will be overwritten. Do you wish to continue?", 
                "WARNING",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        // Return true if the user confirms, false otherwise
        if (intConfirm == JOptionPane.YES_OPTION) {
            ScanControlPanel.BOL_EZSCAN_WARNED = true;
        
            return true;
        }
        
        return false;
    }
    
    /**
     * Enable or disable buttons associated with initiating a scan
     * or data acquisition. 
     *
     * @param bolEnabled    enables the scan initiate buttons if <code>true</code>,
     *                      disables them if <code>false</code>
     *
     * @author Christopher K. Allen
     * @since  Feb 28, 2012
     */
    private void enableScanAndDaqButtons(boolean bolEnabled) {
        this.butScanXp.setEnabled(bolEnabled);
        this.butScanEz.setEnabled(bolEnabled);
        this.butAcquire.setEnabled(bolEnabled);
    }
    
    /**
     * Enables or disables the buttons associated with terminating or
     * interrupting a scan.
     *
     * @param bolEnabled    enables the scan terminate buttons if <code>true</code>,
     *                      disables them if <code>false</code>
     *
     * @author Christopher K. Allen
     * @since  Feb 28, 2012
     */
    private void enableAbortAndReturnButtons(boolean bolEnabled) {
        this.butAbort.setEnabled(bolEnabled);
        this.butStop.setEnabled(bolEnabled);
        this.butPark.setEnabled(bolEnabled);
    }

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
