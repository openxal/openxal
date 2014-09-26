/**
 * DataAcquisitionPanel.java
 *
 *  Created	: Sep 16, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.daq;

import xal.app.pta.MainScanController;
import xal.app.pta.MainApplication;
import xal.app.pta.MainScanController.IScanControllerListener;
import xal.app.pta.MainScanController.MOTION_STATE;
import xal.app.pta.MainScanController.SCAN_MODE;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.PtaResourceManager;
import xal.app.pta.tools.ca.SmfPvMonitor;
import xal.app.pta.tools.ca.SmfPvMonitorPool;
import xal.app.pta.tools.logging.IEventLogger;
import xal.ca.ChannelRecord;
import xal.smf.AcceleratorNode;
import xal.smf.impl.WireScanner;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * <p>
 * Controls diagnostic hardware to perform data 
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
 * @since  Sep 16, 2009
 * @author Christopher K. Allen
 * 
 * @deprecated  I believe this has been replaced by ScanControlPanel
 */
@Deprecated
public class ScanControlPanelDepr extends JPanel implements IScanControllerListener {


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

    
    /**  Number of columns in the input text fields */
    protected static final int CNT_COLS = AppProperties.TEXTFLD.COLS.getValue().asInteger();

    
    
    /*
     * Instance Attributes
     */
    
    
    //
    // Back References 
    //
    
//    /** Reference to the main data document */
//    private final MainWindow            winMain;
    

    //
    // Action Event Handlers
    //
    
    /** pool of PV monitors active during scans */
    private final SmfPvMonitorPool      mplScanEvts;
    
    //
    // Hardware 
    //
   
    /** The device controller used by this GUI component */
    private MainScanController               ctrDaq;

    /** List of devices selected for scanning */
    private List<WireScanner>          lstSelDevs;
    
    
    
    //
    // GUI Components 
    //
    
    /** initiate profile scan button */
    private JButton     butScan;
    
    /** abort the current profile scan */
    private JButton     butAbort;
    
    /** stops the actuator during a scan */
    private JButton     butStop;
    
    /** move the actuate to the home position */
    private JButton     butPark;
    
    
    /** The scan text status display */
    private JTextField  txtStatus;
    

    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>DataAcquisitionPanel</code> object which
     * operates devices obtained from the given device selection 
     * panel.
     *
     *
     * @since     Sep 16, 2009
     * @author    Christopher K. Allen
     */
    public ScanControlPanelDepr() {
        this.lstSelDevs  = new LinkedList<WireScanner>();
        this.mplScanEvts = new SmfPvMonitorPool();
        this.ctrDaq      = null;
        
        // Build the GUI
        this.initGuiComponents();
        this.initEventActions();
        this.buildGuiPanel();
    }

    
    
    /*
     * Operations
     */
    

    /**
     * Sets the device controller that this user interface
     * sits on.  We also register to catch the events 
     * generated by the given DAQ controller. 
     *
     * @param ctrDaq    the controller object running the show
     * 
     * @since  Apr 12, 2010
     * @author Christopher K. Allen
     */
    public void setDaqController(MainScanController ctrDaq) {
        if (this.ctrDaq != null) 
            this.ctrDaq.removeControllerListener(this);
            
        this.ctrDaq = ctrDaq;
        this.ctrDaq.registerControllerListener(this);
        
    }
    
    /**
     * Responds to a new device selection event.  Sets
     * the selected lists of devices.
     *
     * @param lstDevs   current selection set of DAQ devices 
     *  
     * @since   Nov 17, 2009
     * @author  Christopher K. Allen
     */
    public void setDaqDevices(List<WireScanner> lstDevs) {
        this.lstSelDevs.clear();
        
        for (AcceleratorNode smfDev : lstDevs) {
            if (smfDev instanceof WireScanner)
                this.lstSelDevs.add((WireScanner)smfDev);
        }
        
        this.butScan.setEnabled(true);
    }

    /**
     * Clears the list of DAQ devices under management.
     *
     * 
     * @since  Apr 8, 2010
     * @author Christopher K. Allen
     */
    public void clearDevice() {
//        this.lstSelDevs = null;
        this.lstSelDevs.clear();

        this.butScan.setEnabled(false);
    }
    
    /*
     * Query
     */
    

    /*
     * Private Tools
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
        this.txtStatus.setText(strCmt);
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
     * @since  Nov 4, 2009
     * @author Christopher K. Allen
     */
    private void evtScanStart() {
        // Check if there is anything to do
        if (this.lstSelDevs==null || this.lstSelDevs.size()==0)
            return;

        // Begin the scan
        if (!this.scanInitiate()) {
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
        this.ctrDaq.scanAbort();
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
        this.ctrDaq.scanActuatorsStop();
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
        this.ctrDaq.scanActuatorsPark();
    }
    
    
    
    /*
     * Scan Support
     */
    

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
     * @return  <code>true</code> if the scan was successfully initiated,
     *          <code>false</code> upon failure within  
     *          the application's <code>ScannerController</code> object 
     * 
     * @since  Dec 3, 2009
     * @author Christopher K. Allen
     */
    private boolean scanInitiate() {
        
        // Disable scan button until finished
        this.butScan.setEnabled(false);

        // Let everyone know we are in DAQ mode
        String strTmStart = "DAQ in progress: " + Calendar.getInstance().getTime().toString();
        this.updateStatusComment(strTmStart);
        
        // Order the scan
        return this.ctrDaq.scanStart(this.lstSelDevs, SCAN_MODE.EXPERT);
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
        

        this.mplScanEvts.emptyPool();
        this.butScan.setEnabled(true);
        
        this.getLogger().logInfo(getClass(), "DAQ scan prematurely terminated");
    }
    
    
    /*
     * GUI Support
     */
    
    /**
     * Creates the GUI components for device
     * control.
     *
     * 
     * @since  Dec 3, 2009
     * @author Christopher K. Allen
     */
    private void initGuiComponents() {

        // Create the scan control buttons
        String  strPathIconStart = AppProperties.ICON.SCAN_START.getValue().asString(); 
        this.butScan  = new JButton(" Start Scan ",
                          PtaResourceManager.getImageIcon(strPathIconStart) );
        
        String  strPathIconAbort = AppProperties.ICON.SCAN_ABORT.getValue().asString();
        this.butAbort = new JButton(" Abort Scan",
                        PtaResourceManager.getImageIcon(strPathIconAbort) );
        
        String  strPathIconStop = AppProperties.ICON.SCAN_STOP.getValue().asString();
        this.butStop  = new JButton(" Stop Fork   ",
                        PtaResourceManager.getImageIcon(strPathIconStop) );
        
        String strPathIconPark = AppProperties.ICON.SCAN_PARK.getValue().asString();
        this.butPark  = new JButton(" Park              ",
                        PtaResourceManager.getImageIcon(strPathIconPark) );
        

        // Initialize button state
        this.butAbort.setEnabled(false);
        this.butPark.setEnabled(false);
        this.butStop.setEnabled(false);
        this.butScan.setEnabled(false);
        
        // Create the text display
        this.txtStatus = new JTextField(CNT_COLS);
//        this.txtStatus = new JTextField(0);
    }
    
    /**
     * Sets the response actions to the data acquisition
     * user events.
     * 
     * @since  Oct 29, 2009
     * @author Christopher K. Allen
     */
    private void initEventActions(){

        butScan.addActionListener(
                        new ActionListener() {
                            @Override()
                            @SuppressWarnings("synthetic-access")
                            public void actionPerformed(ActionEvent e) {
                                evtScanStart();
                            }
                        }
        ); 

        butAbort.addActionListener( 
                        new ActionListener() {
                            @Override()
                            @SuppressWarnings("synthetic-access")
                            public void actionPerformed(ActionEvent e) {
                                evtScanAbort();
                            }
                        }
        );

        butStop.addActionListener(
                        new ActionListener() {
                            @Override()
                            @SuppressWarnings("synthetic-access")
                            public void actionPerformed(ActionEvent e) {
                                evtActuatorStop();
                            }
                        }
        );

        butPark.addActionListener(
                        new ActionListener() {
                            @Override()
                            @SuppressWarnings("synthetic-access")
                            public void actionPerformed(ActionEvent e) {
                                evtActuatorPark();
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
    private void buildGuiPanel() {

        Box     box = Box.createVerticalBox();
        box.add(this.butScan );
        box.add( Box.createVerticalStrut(5) );
        box.add( this.butAbort );
        box.add( Box.createVerticalStrut(5) );
        box.add( this.butStop );
        box.add( Box.createVerticalStrut(5) );
        box.add( this.butPark );
        box.add( Box.createVerticalStrut(5) );
        box.add( this.txtStatus );
        
        this.add(box);
    }


    /*
     * IScanControllerListener Interface
     */
    

    /**
     *
     * @since   Dec 11, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanInitiated(java.util.List, MainScanController.SCAN_MODE)
     */
    @Override
    public void scanInitiated(List<WireScanner> lstDevs, SCAN_MODE mode) {
        this.butAbort.setEnabled(true);
        this.butStop.setEnabled(true);
        this.butPark.setEnabled(false);
    }

    /**
     * Acknowledge that the data acquisition
     * portion of the scan has completed.
     * 
     *
     * @since   Dec 11, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanCompleted(java.util.List)
     */
    @Override
    public void scanCompleted(List<WireScanner> lstDevs) {

        // Reset the buttons
        this.butAbort.setEnabled(false);
        this.butStop.setEnabled(false);
        this.butPark.setEnabled(false);

        // String notification
        this.updateStatusComment("Scan completed");
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

        // Destroy the PV monitors
        this.mplScanEvts.emptyPool();

        // Reset the control button for another scan
        this.butAbort.setEnabled(false);
        this.butPark.setEnabled(false);
        this.butStop.setEnabled(false);
        this.butScan.setEnabled(true);

        // String notification
        this.updateStatusComment("Scan actuators parked");
    }


    /**
     * Acknowledge the failure of a scanning DAQ device
     *
     * @since 	Dec 11, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanDeviceFailure(xal.smf.impl.WireScanner)
     */
    @Override
    public void scanDeviceFailure(WireScanner smfDev) {
        this.butPark.setEnabled(true);

        this.updateStatusComment("Device Failure: " + smfDev.getId());
    }


    /**
     * Clean up an aborted scan.  Kill scan
     * monitors, acknowledge the abort, and
     * re-enable the scan button.
     *
     * @since 	Dec 11, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanAborted()
     */
    @Override
    public void scanAborted() {

        // Destroy the PV monitors
        this.mplScanEvts.emptyPool();
        
        // Reset the control buttons for another scan
        this.butAbort.setEnabled(false);
        this.butPark.setEnabled(true);
        this.butStop.setEnabled(false);
        this.butScan.setEnabled(true);

        // String notification
        this.updateStatusComment("Scan aborted");
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

        this.butStop.setEnabled(false);
        this.butPark.setEnabled(true);
        
        // String notification
        this.updateStatusComment("WARNING: Scan actuators stopped in beam pipe!");
    }



}
