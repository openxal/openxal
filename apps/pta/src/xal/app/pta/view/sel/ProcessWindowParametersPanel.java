/**
 * TriggeringSelectorPanel.java
 *
 *  Created	: Apr 13, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.sel;

import xal.app.pta.MainApplication;
import xal.app.pta.rscmgt.DeviceProperties;
import xal.app.pta.tools.ca.SmfPvMonitor;
import xal.app.pta.tools.ca.SmfPvMonitorPool;
import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.tools.swing.BndNumberTextField;
import xal.app.pta.tools.swing.NumberTextField.FMT;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.MonitorException;
import xal.ca.PutException;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireScanner;
import xal.smf.scada.XalPvDescriptor;
import xal.smf.scada.ScadaFieldDescriptor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;



/**
 * Second version of the (sub)panel used for selecting
 * the trigger timing for wire scanner devices. 
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Apr 13, 2010
 * @author Christopher K. Allen
 */
public class ProcessWindowParametersPanel extends JPanel {

    
    /**
     * Classes that respond to the reanalyze data command sent
     * to the hardware device can implement this interface to
     * catch  these events.
     *
     * @author Christopher K. Allen
     * @since   Mar 29, 2011
     * 
     * @see xal.smf.impl.WireScanner.CMD#REANALYZE
     */
    public interface IReanalyzeListener {
        
        /**
         * This is called whenever the reanalyze command is send to
         * hardware device.
         *
         * @param   ws  the device whose reanalyze command was invoked
         *
         * @author Christopher K. Allen
         * @since  Mar 29, 2011
         */
        public void dataReanalyzed(WireScanner ws);
    }
    
    
    /*
     * Internal Classes
     */
    
    
    /**
     * Responds to changes in the data processing PV
     * for "begin averaging" parameter.  The GUI widget
     * is updated to the correct value.
     *
     * @since  May 13, 2010
     * @author Christopher K. Allen
     */
    class AvgBgnMonAction implements SmfPvMonitor.IAction {
    
        /**
         * Create a new <code>AvgBgnMonAction</code> object.
         *
         * @since     Apr 14, 2010
         * @author    Christopher K. Allen
         */
        AvgBgnMonAction() {
        }
        
        /**
         * Update the two trigger delay controls
         *
         * @since       Apr 14, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {
            double      dblVal = val.doubleValue();
            double      dblScl = DBL_KNB_SCALE*dblVal;
            
            txtAvgBgn.setDisplayValueSilently(dblScl);
        }
    }


    /**
     * Responses to changes in the hardware trigger delay value
     * by updating the GUI controls displaying the value.
     * We take the new value from the PV monitor call.
     *
     * @since  Apr 15, 2010
     * @author Christopher K. Allen
     */
    class AvgLngMonAction implements SmfPvMonitor.IAction {
        
        
        /**
         * Create a new <code>AvgLngMonAction</code> object.
         *
         * @since     Apr 14, 2010
         * @author    Christopher K. Allen
         */
        public AvgLngMonAction() {
        }
        
        /**
         * Update the two trigger delay controls
         *
         * @since 	Apr 14, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {
            double      dblVal = val.doubleValue();
            double      dblScl = DBL_KNB_SCALE*dblVal;

            txtAvgLng.setDisplayValueSilently(dblScl);
        }
    }

    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;

    
//    /** Label for processing window start time */
//    private static final JLabel         LBL_AVG_BGN = new JLabel("Start (micro-sec)");
//    
//    /** Label for processing window length */
//    private static final JLabel         LBL_AVG_LNG = new JLabel("Duration (micro-sec)");
//    
    
    /** Scaling factor for the dial knobs */
    private static final double         DBL_KNB_SCALE = 1.0;
    
    
    
    
    
    /*
     * Local Attributes
     */
    
    
    //
    // Application
    
    //
    // Timing parameter modification
    
    /** The current wire scanner device */
    private       WireScanner           smfDev;
    
    
    //
    // Event notification

    /** monitor pool for timing parameters */
    private final SmfPvMonitorPool          mplPrcgParms;
    
    /** Set of call back objects to received the "data reanalyzed" event */
    private final Set<IReanalyzeListener>   setLsnReanalyze;

    
    //
    // GUI Components
    
    /** Timing trigger delay text label */
    private JLabel                      lblAvgLng;
    
    /** Timing trigger delay text field */
    private BndNumberTextField          txtAvgLng;
    
    
    /** Averaging start time text label */
    private JLabel                      lblAvgBgn;
    
    /** Averaging start time text field */
    private BndNumberTextField          txtAvgBgn;

    
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>ProcessWindowParametersPanel</code> object.
     *
     * @since     Apr 13, 2010
     * @author    Christopher K. Allen
     */
    public ProcessWindowParametersPanel() {
        this.mplPrcgParms    = new SmfPvMonitorPool();
        this.setLsnReanalyze = new HashSet<IReanalyzeListener>();
        
        this.buildGuiComponents();
        this.buildGuiActions();
        this.layoutGuiComponents();
    }
    
    /**
     * We need to turn off any active monitors that
     * are still around at the demise of this class.
     *
     * @since 	Apr 15, 2010
     * @author  Christopher K. Allen
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() {
        this.mplPrcgParms.emptyPool();
    }
    
//    /**
//     * Add a listener to receive event notifications
//     * whenever a <tt>reanalyze</tt> command is issued
//     * to the current hardware device.
//     *
//     * @param lsnCmd    the reanalyze event listener
//     *
//     * @author Christopher K. Allen
//     * @since  Mar 29, 2011
//     */
//    public void addCommandListener(IReanalyzeListener lsnCmd) {
//        this.setLsnReanalyze.add(lsnCmd);
//    }
//    
//    /**
//     * Removes the given object from the current set
//     * of event listener receiving notifications of the
//     * <tt>reanalyze</tt> command invocation.  It the given
//     * object is not in the set nothing is done.
//     *
//     * @param lsnCmd    object currently receiving reanalyze event notifications.
//     *
//     * @author Christopher K. Allen
//     * @since  Mar 29, 2011
//     */
//    public void removeCommandListener(IReanalyzeListener lsnCmd) {
//        this.setLsnReanalyze.remove(lsnCmd);
//    }
    
    
    /*
     * Operations
     */
    
    /**
     * Sets the current device to monitor and adjust. 
     * 
     * @param   ws      newly selected device 
     *
     * @since   Apr 14, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.cmn.DeviceSelectorPanel.IDeviceSelectionListener#newDeviceSelection(java.util.List)
     */
    public void setDevice(WireScanner   ws) {
        if (ws == null) 
            return;

        this.smfDev = ws;
        
        try {
            this.mplPrcgParms.emptyPool();
            this.buildMonitorPool(ws);
            this.mplPrcgParms.begin();


            this.txtAvgLng.setEnabled(true);
            this.txtAvgBgn.setEnabled(true);
            
        } catch (ConnectionException e) {
            getLogger().logException(getClass(), e, "Unable to connect to a PV in monitor pool."); //$NON-NLS-1$

        } catch (MonitorException e) {
            getLogger().logException(getClass(), e, "Unable to create a monitor in monitor pool."); //$NON-NLS-1$

        } catch (NoSuchChannelException e) {
            getLogger().logException(getClass(), e, "There is a bad handle name in monitor pool."); //$NON-NLS-1$

        }
    }

    /**
     * Clears out the device whose parameters are 
     * being modified.
     *
     * 
     * @since  Apr 16, 2010
     * @author Christopher K. Allen
     */
    public void clearDevice() {
        this.smfDev = null;
        
        this.mplPrcgParms.emptyPool();
        
        this.txtAvgLng.setEnabled(false);
        this.txtAvgLng.clearDisplay();
        
        this.txtAvgBgn.setEnabled(false);
        this.txtAvgBgn.clearDisplay();
    }

    /**
     * Sets the trigger delay to the given value.  The value is written to 
     * hardware and to the GUI.
     *
     * @param dblAvgBgn         start time of the averaging window (seconds)
     * 
     * @since  Apr 15, 2010
     * @author Christopher K. Allen
     */
    public void putAvgBegin(double dblAvgBgn) {

        // Check if there is a device selected
        if (this.smfDev == null)
            return;
        
        // Update the GUI display
        //      Scale the argument to micro-seconds
        double  dblAvgBgnScl = DBL_KNB_SCALE*dblAvgBgn;
        
        this.txtAvgBgn.setDisplayValueSilently(dblAvgBgnScl);
        
        // Update the device's process variable
        WireScanner.PrcgConfig cfgPrcg;
        try {
            cfgPrcg = WireScanner.PrcgConfig.acquire(this.smfDev);
            cfgPrcg.avgBgn = dblAvgBgn;
            
            this.smfDev.configureHardware(cfgPrcg);
//            this.smfDev.runCommand(WireScanner.CMD.UPDATE);
//            this.smfDev.runCommand(WireScanner.CMD.RESET);
//            this.smfDev.runCommand(WireScanner.CMD.REANALYZE);
            
            // Notify all listeners that the hardware data has been reanalyzed
            for (IReanalyzeListener lsn : this.setLsnReanalyze)
                lsn.dataReanalyzed(this.smfDev);

        } catch (ConnectionException e) {
            getLogger().logException(getClass(), e, "Unable to connect to device " + this.smfDev.getId()); //$NON-NLS-1$
            
        } catch (GetException e) {
            getLogger().logException(getClass(), e, "Unable to read from device " + this.smfDev.getId()); //$NON-NLS-1$
            
        } catch (PutException e) {
            getLogger().logException(getClass(), e, "Unable to write to device " + this.smfDev.getId()); //$NON-NLS-1$
            
//        } catch (InterruptedException e) {
//            getLogger().logException(getClass(), e, "Unable reanalyze data in device " + this.smfDev.getId());

        }
    }
    
    /**
     * Sets the averaging window duration to the given value.  
     * The value is written to 
     * hardware and to the GUI.
     *
     * @param dblAvgLng         duration of the processing window (seconds) 
     * 
     * @since  Apr 15, 2010
     * @author Christopher K. Allen
     */
    public void putAvgLength(double dblAvgLng) {
        
        // Check if there is a device selected
        if (this.smfDev == null)
            return;
        
        // Update the GUI display
        //      Scale the argument to micro-seconds
        double  dblAvgLngScl = DBL_KNB_SCALE*dblAvgLng;
        
        this.txtAvgLng.setDisplayValueSilently(dblAvgLngScl);
        
        // Update the device's Process Variable
        WireScanner.PrcgConfig cfgPrcg;
        try {
            cfgPrcg = WireScanner.PrcgConfig.acquire(this.smfDev);
            cfgPrcg.avgLng = dblAvgLng;
            
            this.smfDev.configureHardware(cfgPrcg);
//            this.smfDev.runCommand(CMD.UPDATE);
//            this.smfDev.runCommand(CMD.RESET);
//            this.smfDev.runCommand(CMD.REANALYZE);

            // Notify all listeners that the hardware data has been reanalyzed
            for (IReanalyzeListener lsn : this.setLsnReanalyze)
                lsn.dataReanalyzed(this.smfDev);

        } catch (ConnectionException e) {
            getLogger().logException(getClass(), e, "Unable to connect to device " + this.smfDev.getId()); //$NON-NLS-1$
            
        } catch (GetException e) {
            getLogger().logException(getClass(), e, "Unable to read from device " + this.smfDev.getId()); //$NON-NLS-1$
            
        } catch (PutException e) {
            getLogger().logException(getClass(), e, "Unable to write to device " + this.smfDev.getId()); //$NON-NLS-1$
            
//        } catch (InterruptedException e) {
//            getLogger().logException(getClass(), e, "Unable reanalyze data in device " + this.smfDev.getId());
            
        }
    }
    
    /**
     * Returns the event logger associated with the 
     * application.
     *
     * @return  application event logger
     * 
     * @since  Apr 15, 2010
     * @author Christopher K. Allen
     */
    public IEventLogger getLogger() {
        return MainApplication.getEventLogger();
    }

    /**
     * Construct all the visible GUI components.
     *
     * 
     * @since  Apr 15, 2010
     * @author Christopher K. Allen
     */
    private void buildGuiComponents() {
//        String  strFmt = "%5.3g"; //$NON-NLS-1$
//        String  strUnt = "usec"; //$NON-NLS-1$
//        String  strUnt = "&mu;sec";
        
        //
        // GUI Component for the processing window length
        ScadaFieldDescriptor fdAvgLng = WireScanner.PrcgConfig.FLD_MAP.get("avgLng"); //$NON-NLS-1$
        
        String  strLbl = DeviceProperties.getLabel(fdAvgLng);
        double  dblMin = DBL_KNB_SCALE*DeviceProperties.getMinLimit(fdAvgLng).asDouble();
        double  dblMax = DBL_KNB_SCALE*DeviceProperties.getMaxLimit(fdAvgLng).asDouble();

        //      Create the text label and field
        this.lblAvgLng = new JLabel(strLbl + " (max " + dblMax + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        
        this.txtAvgLng = new BndNumberTextField(FMT.ENGR_3);
        this.txtAvgLng.setMinValue(dblMin);
        this.txtAvgLng.setMaxValue(dblMax);
        this.txtAvgLng.setEditable(true);

        //
        // GUI Components for the processing window start time
        ScadaFieldDescriptor fdAvgBgn = WireScanner.PrcgConfig.FLD_MAP.get("avgBgn"); //$NON-NLS-1$
        
        strLbl = DeviceProperties.getLabel(fdAvgBgn);
        dblMin = DBL_KNB_SCALE*DeviceProperties.getMinLimit(fdAvgBgn).asDouble();
        dblMax = DBL_KNB_SCALE*DeviceProperties.getMaxLimit(fdAvgBgn).asDouble();
        
        //      Create the text label and field
        this.lblAvgBgn = new JLabel(strLbl + " (max " + dblMax + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        
        this.txtAvgBgn = new BndNumberTextField(FMT.ENGR_3);
        this.txtAvgBgn.setMinValue(dblMin);
        this.txtAvgBgn.setMaxValue(dblMax);
        this.txtAvgBgn.setEditable(true);

        
        // 
        // We don't let anyone play until a hardware device is selected
        this.txtAvgLng.setEnabled(false);
        this.txtAvgBgn.setEnabled(false);
    }
    
    /**
     * Define the event responses for the
     * GUI. 
     *
     * 
     * @since  May 27, 2010
     * @author Christopher K. Allen
     */
    private void buildGuiActions() {
        
        // Create the actions for changes in the processing window size
        ActionListener  actAvgLng = new ActionListener() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent e) {
                double dblVal     = txtAvgLng.getDisplayValue().doubleValue();
                double  dblValScl = dblVal/DBL_KNB_SCALE;
                
                putAvgLength(dblValScl);
            }
        };
        this.txtAvgLng.addActionListener(actAvgLng);
        
        
        // Create the action for changes in the processing window start time
        ActionListener  actAvgBgn = new ActionListener() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent e) {
                double  dblVal    = txtAvgBgn.getDisplayValue().doubleValue();
                double  dblValScl = dblVal/DBL_KNB_SCALE;

                putAvgBegin(dblValScl);
            }
        };
        this.txtAvgBgn.addActionListener(actAvgBgn);
    }
    
    /**
     * Layout the GUI display.
     *
     * 
     * @since  Apr 16, 2010
     * @author Christopher K. Allen
     */
    private void layoutGuiComponents() {
        this.setLayout( new GridBagLayout() );

        GridBagConstraints      gbcLayout = new GridBagConstraints();
        gbcLayout.insets  = new Insets(0, 5, 0, 5);

//        gbcLayout.gridx = 1;
//        gbcLayout.gridy = 0;
//        gbcLayout.gridwidth = 1;
//        gbcLayout.gridheight = 1;
//        gbcLayout.insets.right = 5;
//        gbcLayout.weightx = 0.1;
//        gbcLayout.weighty = 0.1;
//        gbcLayout.anchor = GridBagConstraints.LINE_START;
//        gbcLayout.fill  = GridBagConstraints.BOTH;
//        this.add( this.whlAvgLng, gbcLayout );

        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        gbcLayout.fill  = GridBagConstraints.HORIZONTAL;
        this.add( this.lblAvgBgn, gbcLayout );

//        gbcLayout.gridx = 0;
//        gbcLayout.gridy = 1;
//        gbcLayout.gridwidth = 1;
//        gbcLayout.gridheight = 1;
//        gbcLayout.insets.right = 5;
//        gbcLayout.weightx = 0.1;
//        gbcLayout.weighty = 0.1;
//        gbcLayout.anchor = GridBagConstraints.LINE_START;
//        gbcLayout.fill  = GridBagConstraints.HORIZONTAL;
//        this.add( this.knbAvgBgn, gbcLayout );
        
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_END;
        gbcLayout.fill  = GridBagConstraints.HORIZONTAL;
        this.add( this.txtAvgBgn, gbcLayout );

        gbcLayout.gridx = 0;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        gbcLayout.fill  = GridBagConstraints.HORIZONTAL;
        this.add( this.lblAvgLng, gbcLayout);

        gbcLayout.gridx = 1;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_END;
        gbcLayout.fill  = GridBagConstraints.BOTH;
        this.add(this.txtAvgLng, gbcLayout);
    }

    
    /**
     * <p>
     * Builds the pool of monitors for all the PV values
     * we are to display on this panel.  One monitor per
     * PV.  
     * </p>
     * <p>
     * Fires up the monitor on the trigger delay process
     * variable for the given device.  The monitor distributes
     * the current value of the PV to all the GUI devices
     * displaying this value.
     * </p>
     * <p>
     * The Trigger event PV is easier, there is only one
     * GUI component involved 
     * (i.e., the <code>TriggerEventSelector</code> object).
     * </p>
     *
     * @param ws        the device we are going to monitor
     * 
     * @since  Apr 15, 2010
     * @author Christopher K. Allen
     */
    private void buildMonitorPool(WireScanner ws) {
        this.mplPrcgParms.emptyPool();
        
        // Monitor changes in the processing window start time PV
        XalPvDescriptor         pvdAvgBgn = WireScanner.PrcgConfig.FLD_MAP.get("avgBgn"); //$NON-NLS-1$
        SmfPvMonitor.IAction actAvgBgn = new AvgBgnMonAction();
        SmfPvMonitor         monAvgBgn = new SmfPvMonitor(ws, pvdAvgBgn);
        monAvgBgn.addAction(actAvgBgn);
        this.mplPrcgParms.addMonitor(monAvgBgn);
        
        // Monitor changes in the processing window duration PV
        XalPvDescriptor         pvdAvgLng = WireScanner.PrcgConfig.FLD_MAP.get("avgLng"); //$NON-NLS-1$
        SmfPvMonitor.IAction actAvgLng = new AvgLngMonAction();
        SmfPvMonitor         monAvgLng = new SmfPvMonitor(ws, pvdAvgLng);
        monAvgLng.addAction(actAvgLng);
        this.mplPrcgParms.addMonitor(monAvgLng);
    }
}
