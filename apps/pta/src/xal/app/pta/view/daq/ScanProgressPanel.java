/**
 * PositionDisplayPanel.java
 *
 *  Created	: Apr 6, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.daq;

import xal.app.pta.MainApplication;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.DeviceProperties;
import xal.app.pta.tools.ca.SmfPvMonitor;
import xal.app.pta.tools.ca.SmfPvMonitorPool;
import xal.app.pta.tools.ca.SwingFieldMonitorActions.BooleanFieldUpdateAction;
import xal.app.pta.tools.ca.SwingFieldMonitorActions.MultiStateSelectorUpdateAction;
import xal.app.pta.tools.ca.SwingFieldMonitorActions.NumberFieldUpdateAction;
import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.tools.swing.BooleanIndicatorPanel;
import xal.app.pta.tools.swing.MultiStateSelectorPanel;
import xal.app.pta.tools.swing.NumberTextField;
import xal.app.pta.tools.swing.NumberTextField.FMT;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.MonitorException;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireScanner;
import xal.smf.scada.XalPvDescriptor;
import xal.smf.scada.ScadaFieldDescriptor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Displays (dynamically) the device actuator position, velocity,
 * and accelerator in text format and in graphical format.
 *
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since   Apr 6, 2010
 * @version Oct 12, 2011
 * @author  Christopher K. Allen
 */
public class ScanProgressPanel extends JPanel {

    
    /*
     * Internal Classes
     */
    
    
    
    
    /**
     * This is the response to the change in device actuator scan length
     * event.  We tell the progress bar that it has a new maximum value.
     *
     * @author Christopher K. Allen
     * @since   Nov 11, 2011
     */
    public class ScanLengthChgAction implements SmfPvMonitor.IAction {

        
        /** progress bar we are updating */
        private final JProgressBar      pbar;
        
        /**
         * Creates a new instance of <code>ScanLengthChgAction</code> and 
         * attaches it to the given progress bar.
         * 
         * @param pbar  the progress bar that we update
         *
         * @author  Christopher K. Allen
         * @since   Nov 11, 2011
         */
        public ScanLengthChgAction(JProgressBar pbar) {
            this.pbar = pbar;
        }
        
        /**
         * Set the progress bars maximum value from the PV value.
         * 
         * @since Nov 11, 2011
         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
         */
        @Override
        public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {
            int         intMaxVal = val.intValue();
            
            this.pbar.setMaximum(intMaxVal);
//            System.out.println("Hi there! My value is " + intMaxVal + ", my monitor is " + mon.getChannelHandle());
        }
        
    }
    
    /**
     * Monitors an acquisition channel an updates
     * a <code>JProgressBar</code> with the channel
     * values.
     *
     * @since  Apr 16, 2010
     * @author Christopher K. Allen
     */
    public class ActuatorMvtAction implements SmfPvMonitor.IAction {

        /** progress bar we are updating */
        private final JProgressBar      pbar;
        
        /**
         * Create a new <code>ProgressBarAction</code> object.
         *
         * @param pbar
         *
         * @since     Apr 16, 2010
         * @author    Christopher K. Allen
         */
        public ActuatorMvtAction(JProgressBar pbar) {
            this.pbar = pbar;
        }
        /**
         *
         * @since 	Apr 16, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
         */
        @Override
        public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {
            int         intVal = val.intValue();
            
            this.pbar.setValue(intVal);
        }
        
    }
    
    
    
    /*
     * Global Constants
     */
    
    /**  Serialization version number */
    private static final long serialVersionUID = 1L;

    
    /**  Number of columns in the input text fields */
    protected static final int CNT_COLS = AppProperties.TEXTFLD.COLS.getValue().asInteger();

    /**  Size of the vertical struts separating the GUI components */
    protected static final int SZ_VSTRUT_SPACER = AppProperties.TEXTFLD.PADY.getValue().asInteger();

    /**  Size of horizontal struts separating label and GUI component */
    private static final int SZ_HSTRUT_LABEL = 8;


    
//    private static final ScadaFieldDescriptor FD_MVT_STATUS = ScadaStruct.getFieldDescriptor("mvtStatus", WireScanner.DevStatus.class);
//    private static final ScadaFieldDescriptor FD_MVT_STATUS = WireScanner.DevStatus.FLD_MAP.get("mvtStatus");
//    
//    private static final ScadaFieldDescriptor FD_LIM_REV = WireScanner.DevStatus.FLD_MAP.get("limRev");

    /** Set of wire scanner status parameters presented with text field displays */
    private static final ScadaFieldDescriptor[] ARR_TXT_PARAMS = {
        WireScanner.DevStatus.FLD_MAP.get("wirePos"),
        WireScanner.DevStatus.FLD_MAP.get("wireVel"),
        WireScanner.ScanConfig.FLD_MAP.get("lngScan"),
//        WireScanner.DevStatus.FLD_MAP.get("wireMax"),
        WireScanner.DevStatus.FLD_MAP.get("idScan")
    };
    
    
    
    
    /*
     * Local Attributes
     */
    
    //
    // GUI Components
    //
    
    /** The layout manager constraint object */
    private GridBagConstraints       gbcLayout;

    
    /** Actuator parked indicator */
    private BooleanIndicatorPanel    pnlPark;
    
    /** Actuator forward limit activated */
    private BooleanIndicatorPanel    pnlFwdLmt;
    
    /** Scan progress bar */
    private JProgressBar             pbrScan;
    
    /** Movements status display */
    private MultiStateSelectorPanel  pnlMvtStat;
    
    
    /** Collection of status parameters with numerical values */
    private final Map<ScadaFieldDescriptor, NumberTextField>               mapTxtFlds;
    
    
    
    //
    // Channel Access
    //
    
    /** Status parameters pool of PV monitors */
    private final SmfPvMonitorPool            polMonitors;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>PositionDisplayPanel</code> object.
     *
     * @since     Apr 6, 2010
     * @author    Christopher K. Allen
     */
    public ScanProgressPanel() {
        this.mapTxtFlds  = new HashMap<ScadaFieldDescriptor, NumberTextField>();
        this.polMonitors = new SmfPvMonitorPool();
        
        this.initLayout();
        this.initProgressBar();
        this.initParkIndicator();
        this.initMvtIndicatorPanel();
        this.initFwdLmtIndicator();
        this.initNumberFields();
    }

    
    /**
     * We empty the monitor pool.
     * 
     * 
     * @since Nov 11, 2011
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        
        this.polMonitors.emptyPool();
    }

    

    /*
     * Operations
     */
    



    /**
     * Attached the given device to the panel and displays
     * its scan configuration parameters.
     *
     * @param ws
     * 
     * @since  Nov 13, 2009
     * @author Christopher K. Allen
     */
    public void setDevice(WireScanner ws) {
        
        if (ws == null) 
            return;

        this.buildMonitorPool(ws);
        
        try {
            WireScanner.ScanConfig   cfgScan = WireScanner.ScanConfig.acquire(ws);
            
            this.pbrScan.setMaximum((int) cfgScan.lngScan);
            this.polMonitors.begin();
            
        } catch (ConnectionException e) {
            appLogger().logError(getClass(), "unable to start monitor pool for " + ws.getId());
            
        } catch (MonitorException e) {
            appLogger().logError(getClass(), "unable to start monitor pool for " + ws.getId());
            
        } catch (NoSuchChannelException e) {
            appLogger().logError(getClass(), "unable to start monitor pool for " + ws.getId());
            
        } catch (GetException e) {
            appLogger().logError(getClass(), "unable to read parameters for " + ws.getId());
            
        }
            
    }
    
    /**
     * Clears the GUI display to zero
     * for all parameters.
     *
     * 
     * @since  Nov 18, 2009
     * @author Christopher K. Allen
     */
    public void clearDevice() {
        this.polMonitors.emptyPool();

        // Clear the text fields
        for (Map.Entry<ScadaFieldDescriptor, NumberTextField> entry: this.mapTxtFlds.entrySet()) {
            NumberTextField     txtFld = entry.getValue();
            
            txtFld.clearDisplay();
        }

        // Clear the movement state field
        this.pnlMvtStat.clearDisplay();
        
        // Clear the park actuator indicator
        this.pnlPark.clearDisplay();
        
        // Clear the forward limit switch indicator
        this.pnlFwdLmt.clearDisplay();
        
        // Reset the progress bar
        this.pbrScan.setValue(0);
    }
    
    
    
    /*
     * Support Methods
     */
    

    /**
     * Initialize the GUI layout of this panel.
     *
     * 
     * @since  Jan 26, 2010
     * @author Christopher K. Allen
     */
    private void        initLayout() {
        
        this.setLayout( new GridBagLayout() );
        
        this.gbcLayout = new GridBagConstraints();
        this.gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.gbcLayout.gridx = 0;
        this.gbcLayout.gridy = 0;
        this.gbcLayout.gridheight = 1;
        this.gbcLayout.gridwidth = 2;
        this.gbcLayout.fill = GridBagConstraints.NONE;
    }
    
    /**
     * Creates and configures the the "actuator parked"
     * indicator button.
     *
     * 
     * @since  Jan 26, 2010
     * @author Christopher K. Allen
     */
    private void        initParkIndicator() {
        ScadaFieldDescriptor     fdLimRev = WireScanner.DevStatus.FLD_MAP.get("limRev");
        
        String  strLabel = DeviceProperties.getLabel(fdLimRev);
        Integer intPrked = DeviceProperties.getNormalValue(fdLimRev);
        Integer intUnPrk = DeviceProperties.getErrorValue(fdLimRev);
        
        this.pnlPark = new BooleanIndicatorPanel(strLabel, intPrked, intUnPrk);
        this.pnlPark.setOffColor(BooleanIndicatorPanel.CLR_UNSET);
        this.pnlPark.setOnColor(Color.GREEN);
    
        GridBagConstraints      gbc = (GridBagConstraints) this.gbcLayout.clone();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        this.add(this.pnlPark, gbc);
    }

    /**
     * Creates and configures the the "actuator parked"
     * indicator button.
     *
     * 
     * @since  Jan 26, 2010
     * @author Christopher K. Allen
     */
    private void        initFwdLmtIndicator() {
        ScadaFieldDescriptor     fdFwdLim = WireScanner.DevStatus.FLD_MAP.get("limFwd");
        
        String  strLabel = DeviceProperties.getLabel(fdFwdLim);
        Integer intClear = DeviceProperties.getNormalValue(fdFwdLim);
        Integer intActiv = DeviceProperties.getErrorValue(fdFwdLim);
        
        this.pnlFwdLmt = new BooleanIndicatorPanel(strLabel, intActiv, intClear);
        this.pnlFwdLmt.setOffColor(BooleanIndicatorPanel.CLR_UNSET);
        this.pnlFwdLmt.setOnColor(Color.RED);
    
        GridBagConstraints      gbc = (GridBagConstraints) this.gbcLayout.clone();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 2;
        gbc.gridy = 0;
        
        this.add(this.pnlFwdLmt, gbc);
    }


    /**
     * Creates and places the scan progress bar.
     *
     * 
     * @since  Apr 16, 2010
     * @author Christopher K. Allen
     */
    private void        initProgressBar() {
        this.pbrScan = new JProgressBar();
        
        this.pbrScan.setForeground(Color.BLACK);
    
        GridBagConstraints      gbc = (GridBagConstraints) this.gbcLayout.clone();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        
        this.add(this.pbrScan, gbc);
        this.gbcLayout.gridy++;
    }


    /**
         * Creates, configures, and attaches the movement status
         * indicator display.
         *
         * 
         * @since  Jan 25, 2010
         * @author Christopher K. Allen
         */
        private void        initMvtIndicatorPanel() {
            ScadaFieldDescriptor    sfdMvtStat = WireScanner.DevStatus.FLD_MAP.get("mvtStatus");
            
            String          strLabels = DeviceProperties.getLabel(sfdMvtStat);
            String[]        arrLabels = strLabels.split(",");
            Integer[]       arrValues = DeviceProperties.getValuesInt(sfdMvtStat);
    
            this.pnlMvtStat = new MultiStateSelectorPanel(arrLabels, arrValues);
            this.pnlMvtStat.setEditable(false);
            this.add(this.pnlMvtStat, this.gbcLayout);
            this.gbcLayout.gridy++;
        }


    /**
     * Initializes a text field for the given device status parameter
     * (an element of <code>{@link WireScanner.DevStatus.PROP}</code>).
     * The (number) text field is created, a label attached, then added
     * to this panel's GUI.  The reference to the text field is 
     *
     * @param enmFld    either <code>WIRE_POS</code> or <code>WIRE_VEL</code>
     * 
     * @since  Jan 25, 2010
     * @author Christopher K. Allen
     */
    private void initNumberFields() {
        
        for (ScadaFieldDescriptor enmFld : ARR_TXT_PARAMS) {
        
        // Create the number field and configure it
        String          strLbl = DeviceProperties.getLabel(enmFld);
        NumberFormat    fmtFld = DeviceProperties.getDisplayFormat(enmFld);
        JLabel          lblFld = new JLabel(strLbl);
        NumberTextField txtFld = new NumberTextField(FMT.DEC, CNT_COLS);
        txtFld.setDisplayFormat(fmtFld);
        txtFld.setEditable(false);
        
        
        // Add number field to the map of number fields
        this.mapTxtFlds.put(enmFld, txtFld);
        
        
        // Layout the GUI
        Box     boxFld = Box.createHorizontalBox();
        boxFld.add(txtFld);
        boxFld.add( Box.createHorizontalStrut(SZ_HSTRUT_LABEL));
        boxFld.add(lblFld);
        
        // Add to the panel
        this.add(boxFld, this.gbcLayout );
        this.gbcLayout.gridy++;
        this.add(Box.createVerticalStrut(SZ_VSTRUT_SPACER), this.gbcLayout);
        this.gbcLayout.gridy++;
        }
        
    }
    
    /**
     * Builds the pool of monitors for all the PV values
     * we are to display on this panel.  
     *
     * @param ws        the device whose status will be displayed
     * 
     * @since  Jan 25, 2010
     * @author Christopher K. Allen
     */
    private void        buildMonitorPool(WireScanner ws) {

        // Just in case
        this.polMonitors.emptyPool();
        
        // Monitors for the text displays of numeric values
        for (ScadaFieldDescriptor enmTxt : this.mapTxtFlds.keySet()) {
            NumberTextField           txtFld = this.mapTxtFlds.get(enmTxt);
            
            SmfPvMonitor              monTxt = new SmfPvMonitor(ws, enmTxt);
            NumberFieldUpdateAction   actTxt = new NumberFieldUpdateAction(txtFld);
            monTxt.addAction(actTxt);
            this.polMonitors.addMonitor(monTxt);
        }
        
        // Monitor for the Movement status
        XalPvDescriptor                   pvdMvt = WireScanner.DevStatus.FLD_MAP.get("mvtStatus");
        SmfPvMonitor                   monMvt = new SmfPvMonitor(ws, pvdMvt);
        MultiStateSelectorUpdateAction actMvt = new MultiStateSelectorUpdateAction(this.pnlMvtStat);
        monMvt.addAction(actMvt);
        this.polMonitors.addMonitor(monMvt);
        
        // Monitor for actuator park indicator
        XalPvDescriptor                pvdPrk = WireScanner.DevStatus.FLD_MAP.get("limRev");
        SmfPvMonitor                monPrk = new SmfPvMonitor(ws, pvdPrk);
        BooleanFieldUpdateAction    actPrk = new BooleanFieldUpdateAction(this.pnlPark);
        monPrk.addAction(actPrk);
        this.polMonitors.addMonitor(monPrk);
        
        // Monitor for the forward limit switch indicator
        XalPvDescriptor                pvdFwd = WireScanner.DevStatus.FLD_MAP.get("limFwd");
        SmfPvMonitor                monFwd = new SmfPvMonitor(ws, pvdFwd);
        BooleanFieldUpdateAction    actFwd = new BooleanFieldUpdateAction(this.pnlFwdLmt);
        monFwd.addAction(actFwd);
        this.polMonitors.addMonitor(monFwd);
        
        /// Monitor for progress bar
        XalPvDescriptor            pvdPos = WireScanner.DevStatus.FLD_MAP.get("wirePos");
        SmfPvMonitor            monPos = new SmfPvMonitor(ws, pvdPos);
        ActuatorMvtAction       actPos = new ActuatorMvtAction(this.pbrScan);
        monPos.addAction(actPos);
        this.polMonitors.addMonitor(monPos);
        
        // Monitor the maximum scan length
        XalPvDescriptor            pvdMax = WireScanner.ScanConfig.FLD_MAP.get("lngScan");
        SmfPvMonitor            monMax = new SmfPvMonitor(ws, pvdMax);
        ScanLengthChgAction     actMax = new ScanLengthChgAction(this.pbrScan);
        monMax.addAction(actMax);
        this.polMonitors.addMonitor(monMax);
    }
    
    /**
     * Returns the application's main event logger object.
     *
     * @return  application logger 
     * 
     * @since  Dec 18, 2009
     * @author Christopher K. Allen
     */
    private IEventLogger        appLogger() {
        return MainApplication.getEventLogger();
    }


}
