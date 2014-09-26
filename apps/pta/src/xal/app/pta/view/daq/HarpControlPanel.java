/**
 * DataAcquisitionPanel.java
 *
 *  Created	: Sep 16, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.daq;

import xal.app.pta.MainApplication;
import xal.app.pta.MainDocument;
import xal.app.pta.MainHarpController;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.daq.HarpData;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.PtaResourceManager;
import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.tools.swing.NumberTextField;
import xal.ca.BadChannelException;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.service.pvlogger.PvLoggerException;
import xal.smf.AcceleratorNode;
import xal.smf.impl.WireHarp;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.impl.profile.ProfileDevice.IProfileData;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 * <p>
 * Controls harp diagnostic hardware to perform data 
 * acquisition.
 * </p>
 * <p>
 * Note that this class does no data acquisition itself,
 * that is, it only operates the hardware.  You must
 * retrieve the data elsewhere once the scan is complete. 
 * </p>
 *
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * @since  April 24, 2014
 * @author Christopher K. Allen
 */
public class HarpControlPanel extends JPanel implements MainHarpController.IHarpControllerListener {



    /*
     * Inner Classes
     */
    
//    /**
//     * Monitors of actuator movements should send their
//     * events to one of these.  It will update the progress
//     * bar associated with this object.
//     *
//     * @since  Nov 4, 2009
//     * @author Christopher K. Allen
//     */
//    class SampleTakenAction implements SmfPvMonitor.IAction {
//
//        
//        /*
//         * Instance Attributes
//         */
//
//        /** The DAQ controller GUI panel */
//        private final DeviceProgressPanel        pnlProg;
//        
//        
//        /**
//         * Create a new <code>PositionMonitor</code> object for
//         * monitoring the given data acquisition device.
//         *
//         * @param pnlProg       the progress panel where the updates go
//         *
//         * @since     Nov 4, 2009
//         * @author    Christopher K. Allen
//         */
//        public SampleTakenAction(DeviceProgressPanel pnlProg) {
//            this.pnlProg = pnlProg;
//        }
//
//
//        /**
//         * Send the new DAQ device actuator position to the
//         * progress panel.
//         *
//         * @param recValue      new position value 
//         * @param mon           monitor on the DAQ device position PV
//         *  
//         * @since 	Nov 4, 2009
//         * @author  Christopher K. Allen
//         *
//         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction
//         */
//        public void valueChanged(ChannelRecord recValue, SmfPvMonitor mon) {
//            Integer     intValue = recValue.intValue();
//            
//            this.pnlProg.setProgressValue(mon.getDevice(), intValue);
////            
////            System.out.println("Scan Monitor:Position = " + dblVal);
//        }
//        
//    }


    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;

    
    /** The 'do connection test' flag */
    private static final boolean BOL_CONN_TST = AppProperties.DEVICE.EPICS_CA_CHK.getValue().asBoolean();
    
    /** The connection test device time out */
    private static final double  DBL_CONN_TMO = AppProperties.DEVICE.TMO_CONNTEST.getValue().asDouble();
    
    /** Weighting factor to use for sample averaging */
    private static final double  DBL_SMPL_AVGWT = AppProperties.HARP.WT_SMPLS.getValue().asDouble();  
    

    
    /*
     * Global Variables
     */

    
    
    /*
     * Instance Attributes
     */
    
    
    //
    // Back References 
    //
    
    /** Reference to the main data document */
    private final MainDocument              docMain;
    
    /** The application's DAQ central controller */
    private final MainHarpController        ctlDaq;
    


    /*
     * GUI Components 
     */
    
    /** initiate scan button */
    private JButton     butDaq;
    
    /** abort the current profile scan */
    private JButton     butAbort;
    
    /** Update data button */
    private JButton     butAcquire;
    
    
    /** Number of data sets to take */
    private NumberTextField     txtSampleCnt;
    
    /** the DAQ progress read back */
    private DeviceProgressPanel   pnlDaqProgress;
    

    /*
     * DAQ Tools
     */
   
    /** List of currently selected devices for data acquisition */
    private List<WireHarp>           lstSelDevs;
    
    /** List of currently selected devices with connection problems */
    private final List<WireHarp>     lstMalDevs;
    
//    /** The current set of measurement data for the current device set */
//    private final List<HarpData>           lstDataSmpls;
    
    /** The current averaged measurements during a DAQ averaging */
    private MeasurementData          mmtSmpls;
    
    
    
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
    public HarpControlPanel(MainDocument docMain) {
        this.docMain = docMain;
        this.ctlDaq  = MainHarpController.getInstance();
        this.ctlDaq.registerControllerListener(this);
        
        this.lstSelDevs = null;
        this.lstMalDevs = new LinkedList<WireHarp>();
//        this.lstDataSmpls = new LinkedList<HarpData>();
        
        this.mmtSmpls = null;
        
        // Build the GUI
        this.buildGuiComponents();
        this.buildEventActions();
        this.layoutGuiPanel();
    }

    
    /*
     * Operations
     */
    
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
    public void setProfileDevices(List<WireHarp> lstDevs) {
        this.lstSelDevs = lstDevs;
        
        this.lstMalDevs.clear();
        this.pnlDaqProgress.clear();

        this.enableDaqButtons(true);
    }

    /**
     * Clears the list of DAQ devices under management.
     * 
     * @since  Apr 8, 2010
     * @author Christopher K. Allen
     */
    public void clearDevices() {
        this.lstSelDevs = null;
        this.pnlDaqProgress.clear();
        
        this.enableDaqButtons(false);
        this.enableAbortButtons(false);
    }
    
    
    /*
     * IHarpControllerListener Interface
     */

    /**
     * Enable and disable the GUI control buttons according to
     * the state of the DAQ controller.
     * 
     * @param   lstHarps     list of currently active DAQ devices
     * @param   cntSamples  the number of sampled data sets requested
     *
     * @since   Dec 11, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainHarpController.IHarpControllerListener#daqInitiated(java.util.List, int)
     */
    @Override
    public void daqInitiated(List<WireHarp> lstHarps, int cntSamples) {
        this.enableAbortButtons(true);

//        System.out.println("HarpControlPanel.daqInitiated(List<WireHarp>,int) - List<Harp> = " + lstHarps + ", int = " + cntSamples);
        
        try {
            List<ProfileDevice>    lstDevs = new LinkedList<ProfileDevice>(lstHarps);

            this.mmtSmpls = MeasurementData.acquire( lstDevs );  

        } catch (ConnectionException e) {
            getLogger().logException(getClass(), e, "DAQ Initialization Failure: unable to connect to a device in " + lstHarps);
            JOptionPane.showMessageDialog(this, "Error in data qcquisition - see log", "WARNING", JOptionPane.WARNING_MESSAGE);

        } catch (GetException e) {
            getLogger().logException(getClass(), e, "DAQ Initialization Failure: unable to read from a device in " + lstHarps);
            JOptionPane.showMessageDialog(this, "Error in data qcquisition - see log", "WARNING", JOptionPane.WARNING_MESSAGE);

        } catch (PvLoggerException e) {
            getLogger().logException(getClass(), e, "DAQ Initialization Failure: Unable to take PV Logger snapshot for measurement " + lstHarps);
            JOptionPane.showMessageDialog(this, "Error in PV Logger capture - see log", "WARNING", JOptionPane.WARNING_MESSAGE);
        }
        
//        System.out.println("HarpControlPanel.daqInitiated(List<WireHarp>,int) made it through successful.");
        
    }

    /**
     * We need to update the DAQ progress panel
     *
     * @see xal.app.pta.MainHarpController.IHarpControllerListener#daqSampled(xal.smf.impl.WireHarp)
     *
     * @author Christopher K. Allen
     * @since  Apr 25, 2014
     */
    @Override
    public void daqSampled(WireHarp smfHarp, int cntSample) {
        
        try {
//            System.out.println("HarpControlPanel.daqSampled(WireHarp,int) - this.mmtSmpls = " + this.mmtSmpls);
            
            IProfileData iPrfDat = this.mmtSmpls.getDataForDeviceId( smfHarp.getId() );
            
//            System.out.println("HarpControlPanel.daqSampled(WireHarp,int) - iPrfDat = " + iPrfDat.getDeviceId());
            
            if ( !(iPrfDat instanceof HarpData) ) {
                getLogger().logError(getClass(), "Serious DAQ Data Type Error: Unable to save sampled data for " + smfHarp);
                 
                return;
            }

            HarpData     datPrv = (HarpData)iPrfDat;
            HarpData     datNew  = HarpData.acquire(smfHarp);
            
            datPrv.average(datNew, DBL_SMPL_AVGWT);
            
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
//        System.out.println("HarpControlPannel.daqSampled(WireHarp,int) Uh, huh, huh.");
        
        this.pnlDaqProgress.setProgressValue(smfHarp, cntSample);
    }


    /**
     * Acknowledge that the data acquisition
     * portion of the scan has completed then acquire the profile
     * data from the device and set it to the application main data.
     *
     * @since   Dec 11, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainHarpController.IHarpControllerListener#daqCompleted(java.util.List)
     */
    @Override
    public void daqCompleted(List<WireHarp> lstDevs) {

        //
        // Save the measurement data to the main application document
//        try {
//            
//            // First loosen the device data type to fit into MeasurementData DAQ process
//            List<ProfileDevice> lstDevProf = new LinkedList<ProfileDevice>( lstDevs );
//            
//            MeasurementData  setMsmt = MeasurementData.acquire(lstDevProf);
//
//            this.docMain.setMeasurementData(setMsmt);
            
        this.docMain.setMeasurementData(this.mmtSmpls);

        // String notification
        this.updateStatusComment("Harp data acquisition completed");

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
        
        // Reset the progress panel
        
//        System.out.println("HarpControlPanel.daqCompleted(List<WireHarp>) - about to clear the DAQ Progress Panel");
        
        this.pnlDaqProgress.clear();
        
        // Reset the control buttons for another scan
        this.enableAbortButtons(false);
        this.enableDaqButtons(true);
    }

    /**
     * Acknowledge the failure of a scanning DAQ device
     *
     * @since   Dec 11, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainHarpController.IHarpControllerListener#scanDeviceFailure(xal.smf.impl.WireHarp)
     */
    @Override
    public void daqDeviceFailure(WireHarp smfDev) {
        this.ctlDaq.daqAbort();
        this.daqTerminate();
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
    public void daqAborted() {

//        this.mplScan.emptyPool();
        
        // String notification
        this.updateStatusComment("Harp DAQ aborted");

        // Reset the control buttons for another scan
        this.enableAbortButtons(false);
        this.enableDaqButtons(true);
    }

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
     * @since  April 25, 2014
     * @author Christopher K. Allen
     */
    private void evtSamplingStart() {
        
//        System.out.println("HarpControlPanel.evtSamplingStart() - Hi there");
        
        // Check if there is anything to do
        if (this.lstSelDevs==null || this.lstSelDevs.size()==0)
            return;
        
        // Get the number of samples to take
        int cntSamples = this.txtSampleCnt.getDisplayValue().intValue();

        // Disable scan button until finished
        this.enableDaqButtons(false);

        // Check the connections between the devices, removing devices that fail connection test
        if (BOL_CONN_TST)
            if (!this.checkDeviceConnections()) {
                this.daqTerminate();
                return;
            }
        
        // Initialize the progress panel GUI
        if (!this.daqInitProgressPanel(cntSamples)) {
            this.getLogger().logError(getClass(), "Failed to initialize progress panel for DAQ");
            this.daqTerminate();
            return;
        }
        
//        // Initialize the scan - create monitoring threads
//        if (!this.monitorStartup()) {
//            this.getLogger().logError(getClass(), "Failed to initialize progress monitors for DAQ");
//            this.scanTerminate();
//            return;
//        }
        
        // Begin the DAQ scan
        if (!this.daqInitiate(cntSamples)) {
            this.getLogger().logError(getClass(), "Failed to initiate scan sequence for DAQ");
            this.daqTerminate();
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
    private void evtSamplingAbort() {
        
        System.out.println("HarpControlPanel.evtSamplingAbort() - Hi there ");
        
        this.ctlDaq.daqAbort();
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
            this.updateStatusComment("Acquired harp data");
            
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
        for (WireHarp smfHarp : this.lstSelDevs) {
            
            // Test the channel connections for the device
            try {
                
                // If all connections check out move to the next one
                if (HarpData.testConnection(smfHarp, DBL_CONN_TMO) == true)
                    continue;

            } catch (BadChannelException e) {
                // Worse than no connection - a channel is unbound
                this.getLogger().logException(this.getClass(), e, "Channel unbound for device " + smfHarp.getId() );
//                JOptionPane.showMessageDialog(this, "Unbound channel - see log", "WARNING", JOptionPane.WARNING_MESSAGE);
                
                // Otherwise add it to the malfunctioning device list
                this.lstMalDevs.add(smfHarp);
                
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
    
    
//    /**
//     * Initializes the system for the DAQ
//     * monitors prior to a DAQ scan.  
//     *
//     * @return  <code>true</code> if all monitors were successfully started,
//     *          <code>false</code> otherwise  
//     * 
//     * @since  Dec 3, 2009
//     * @author Christopher K. Allen
//     */
//    private boolean monitorStartup() {
//        
//        // Clear out any remaining monitors (e.g., from an abort event)
//        this.mplScan.emptyPool();
//        
//        // PVs to monitor
//        PvDescriptor    pvdMoStat = WireScanner.DevStatus.FLD_MAP.get("mvtStatus");
//        PvDescriptor    pvdPosVal = WireScanner.DevStatus.FLD_MAP.get("wirePos");
//        
//        // For each device add monitors into pool
//        for (AcceleratorNode smfDev : this.lstSelDevs) {
//
//            if ( !(smfDev instanceof WireScanner) )
//                continue;
//            
//            MotionStatusAction actMoStat = new MotionStatusAction(this.pnlDaqProgress);
//            this.mplScan.createMonitor(smfDev, pvdMoStat, actMoStat);
//
//            SampleTakenAction actPosVal = new SampleTakenAction(this.pnlDaqProgress);
//            this.mplScan.createMonitor(smfDev, pvdPosVal, actPosVal);
//        }
//
//        
//        // Fire up the PV monitors
//        try {
//            this.mplScan.begin();
//            
//        } catch (ConnectionException e) {
//            getLogger().logException(getClass(), e, "unable to start monitor pool");
//            return false;
//
//        } catch (MonitorException e) {
//            getLogger().logException(getClass(), e, "unable to start monitor pool");
//            return false;
//            
//        } catch (NoSuchChannelException e) {
//            getLogger().logException(getClass(), e, "unable to start monitor pool");
//            return false;
//            
//        }
//        
//        return true;
//    }
    
    
    /**
     * Initialize the DAQ progress GUI panel before
     * DAQ begins.
     * 
     * @param   cntSamples  number of data sets to take
     *
     * @return  always returns <code>true</code> 
     * 
     * @since  April 25, 2014
     * @author Christopher K. Allen
     */
    private boolean daqInitProgressPanel(int cntSamples) {
        
        // Set the currently active devices and notify listeners
        this.pnlDaqProgress.addAllDaqHardware( new LinkedList<AcceleratorNode>(this.lstSelDevs) );
        
        // For each device begin the acquisition process
        for (AcceleratorNode smfDev : this.lstSelDevs) 

                this.pnlDaqProgress.initProgress(smfDev, cntSamples);
                
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
    private boolean daqInitiate(int cntSamples) {
        
        // Enable abort scan buttons until finished
        this.enableAbortButtons(true);
        
        // Let everyone know we are in DAQ mode
        String strTmStart = "DAQ in progress: " + Calendar.getInstance().getTime().toString();
        this.updateStatusComment(strTmStart);
        
        // Order the scan
        return this.ctlDaq.daqStart(this.lstSelDevs, cntSamples);
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
    private void daqTerminate() {
        String  strMsg = "DAQ scan prematurely terminated";

        this.updateStatusComment(strMsg);
        this.getLogger().logInfo(getClass(), strMsg);
        
//        this.mplScan.emptyPool();

        this.enableDaqButtons(true);
        
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
        
        // Create the sampling size text input box
        int     cntSmpl   = AppProperties.HARP.CNT_SMPLS.getValue().asInteger();
        this.txtSampleCnt = new NumberTextField(NumberTextField.FMT.INT);
        this.txtSampleCnt.setDisplayValueSilently(cntSmpl);

        // Create the DAQ control buttons
        String  strPathIconStart = AppProperties.ICON.SCAN_START.getValue().asString(); 
        this.butDaq  = new JButton(" Start Sampling ",
                          PtaResourceManager.getImageIcon(strPathIconStart) );
        
        String  strPathIconAbort = AppProperties.ICON.SCAN_ABORT.getValue().asString();
        this.butAbort = new JButton(" Abort Sampling ",
                        PtaResourceManager.getImageIcon(strPathIconAbort) );
        
        String  strPathIconAcquire = AppProperties.ICON.DAQ_ACQUIRE.getValue().asString();
        this.butAcquire = new JButton(" (re)Acquire ",
                        PtaResourceManager.getImageIcon(strPathIconAcquire) );

        this.enableDaqButtons(false);
        this.enableAbortButtons(false);
    }
    
    /**
     * Sets the response actions to the data acquisition
     * user events.
     * 
     * @since  Oct 29, 2009
     * @author Christopher K. Allen
     */
    private void buildEventActions(){

        // Add the DAQ button listener action
        this.butDaq.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                new Thread( new Runnable() { 
                                    @Override
                                    public void run() {
                                        HarpControlPanel.this.evtSamplingStart();
                                    }
                                }
                                ).start();
                            }
                        }
        ); 
        
        // Add the ABORT button listener action
        this.butAbort.addActionListener( 
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                evtSamplingAbort();
                            }
                        }
        );

        // Add the ACQUIRE button action listener
        this.butAcquire.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                new Thread( new Runnable() { 
                                    @Override
                                    public void run() {
                                        HarpControlPanel.this.evtAcquireData();
                                    }
                                }
                                ).start();
                            }
                        }
        );
        
        // Add the listener action for when users touch the number of samples button
        this.txtSampleCnt.addActionListener(
                new ActionListener() {

                    // Get the sample count from the text field 
                    //  then store it for the default value in the application properties 
                    public void actionPerformed(ActionEvent e) {
                        Number  numCntSmpls = HarpControlPanel.this.txtSampleCnt.getDisplayValue();
                        
                        AppProperties.HARP.CNT_SMPLS.getValue().set( numCntSmpls.toString() );
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

        Box     boxSmpCnt = Box.createHorizontalBox();
        boxSmpCnt.add( new JLabel("Number of Samples (averaging) ") );
        boxSmpCnt.add( this.txtSampleCnt );
        
        Box     boxRight = Box.createVerticalBox();
        boxRight.add( boxSmpCnt );
        boxRight.add( Box.createVerticalStrut(5) );
        boxRight.add( this.pnlDaqProgress );
        
        Box     boxLeft = Box.createVerticalBox();
        boxLeft.add( new JLabel("Averaging") );
        boxLeft.add( Box.createVerticalStrut(5) );
        boxLeft.add(this.butDaq );
        boxLeft.add( Box.createVerticalStrut(5) );
        boxLeft.add( this.butAbort );
        boxLeft.add( Box.createVerticalStrut(20) );
        boxLeft.add( new JSeparator(SwingConstants.HORIZONTAL) );
//        boxLeft.add( Box.createVerticalStrut(20) );
        boxLeft.add( new JLabel("Single Sample") );
        boxLeft.add( Box.createVerticalStrut(5) );
        boxLeft.add( this.butAcquire );
        
        
        Box     boxDaq = Box.createHorizontalBox();
        boxDaq.add( boxLeft );
        boxDaq.add( Box.createHorizontalStrut(10) );
        boxDaq.add( boxRight );
        boxDaq.setBorder(new TitledBorder("Data Acquisition"));
        
        this.add(boxDaq);
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
     * Enable or disable buttons associated with initiating a scan
     * or data acquisition. 
     *
     * @param bolEnabled    enables the scan initiate buttons if <code>true</code>,
     *                      disables them if <code>false</code>
     *
     * @author Christopher K. Allen
     * @since  Feb 28, 2012
     */
    private void enableDaqButtons(boolean bolEnabled) {
        this.butDaq.setEnabled(bolEnabled);
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
    private void enableAbortButtons(boolean bolEnabled) {
        this.butAbort.setEnabled(bolEnabled);
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
