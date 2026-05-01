/**
 * ScanProgressPanel.java
 *
 *  Created	: Jan 21, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.diag;

import xal.app.pta.MainApplication;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.DeviceProperties;
import xal.app.pta.tools.ca.SmfPvMonitor;
import xal.app.pta.tools.ca.SmfPvMonitorPool;
import xal.app.pta.tools.ca.SwingFieldMonitorActions.BooleanFieldUpdateAction;
import xal.app.pta.tools.ca.SwingFieldMonitorActions.MultiStateSelectorUpdateAction;
import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.tools.swing.BooleanIndicatorPanel;
import xal.app.pta.tools.swing.MultiStateSelectorPanel;
import xal.ca.BadChannelException;
import xal.ca.ConnectionException;
import xal.ca.MonitorException;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireScanner;
import xal.smf.impl.WireScanner.DevStatus;
import xal.smf.scada.BadStructException;
import xal.smf.scada.ScadaFieldDescriptor;
import xal.smf.scada.ScadaFieldList;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * Displays the status parameters of a wire scanner device.
 * The parameters are monitored on channel access and displayed
 * in real time.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jan 21, 2010
 * @author Christopher K. Allen
 */
public class ScannerStatusPanel extends JPanel {

    
    
    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;


    
    /**  Title of GUI component */
    private static final String STR_TITLE = "Scanner Status";

    /**  Number of columns in the input text fields */
    protected static final int CNT_COLS = AppProperties.TEXTFLD.COLS.getValue().asInteger();

    /**  Size of the vertical struts separating the GUI components */
    protected static final int INT_STRUT_SIZE = AppProperties.TEXTFLD.PADY.getValue().asInteger();

    /** Time out to use when checking connections to the device */
    protected static final double DBL_TMO_CONNTEST = AppProperties.DEVICE.TMO_CONNTEST.getValue().asDouble();


    
    /** Set of wire scanner status parameters monitored as error state */
    private static final ScadaFieldDescriptor[] ARR_ERR_PARAMS = {
        DevStatus.FLD_MAP.get("almSgnl"),
//        DevStatus.FLD_MAP.get("limFwd"),
        DevStatus.FLD_MAP.get("errScanRng"),
        DevStatus.FLD_MAP.get("dmgHor"),
        DevStatus.FLD_MAP.get("dmgVer"),
        DevStatus.FLD_MAP.get("dmgDia"),
        DevStatus.FLD_MAP.get("almTmg"),
        DevStatus.FLD_MAP.get("errMps0"),
        DevStatus.FLD_MAP.get("errMps1"),
        DevStatus.FLD_MAP.get("errPs"),
        DevStatus.FLD_MAP.get("errScan"),
        DevStatus.FLD_MAP.get("errCollsn")
    };


    /*
     * Local Attributes
     */
    
    
    //
    // GUI Components
    //
    
    /** The layout manager constraint object */
    private GridBagConstraints       gbcLayout;

    
    /** Forward limit switch indicator */
    private BooleanIndicatorPanel    pnlFwdLmt;
    
    /** Actuator parked indicator */
    private BooleanIndicatorPanel    pnlPark;
    
    /** Movements status display */
    private MultiStateSelectorPanel  pnlMvtStat;
    
    
    //
    //  Management
    //
    
//    /** Collection of status parameters with numerical values */
//    private final Map<ScadaFieldDescriptor, NumberTextField>               mapTxtFlds;
    
    /** Map of error parameter constants to the PV display */
    private final Map<ScadaFieldDescriptor, BooleanIndicatorPanel>         mapErrPnls;
    
    
    
    //
    // Channel Access
    //
    
    /** The current hardware device */
    private WireScanner                       smfDevice;
    
    /** Status parameters pool of PV monitors */
    private final SmfPvMonitorPool            polMonitors;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>ScanProgressPanel</code> object.
     *
     * @since     Jan 21, 2010
     * @author    Christopher K. Allen
     */
    public ScannerStatusPanel() {
        this.mapErrPnls  = new HashMap<ScadaFieldDescriptor, BooleanIndicatorPanel>();
        this.polMonitors = new SmfPvMonitorPool();
        
        this.initLayout();
        this.initParkIndicator();
        this.initFwdLmtIndicator();
        this.initActuatorStatPanel();
        this.addVerSpacer();
        this.addVerSpacer();
        this.initErrorPanels();
        this.addVerSpacer();
        this.initMvtIndicatorPanel();
    }

    
    /*
     * Operations
     */
    
    /**
     * Returns the wire scanner device currently being monitored.
     *
     * @return  device that we are status monitoring
     *
     * @author Christopher K. Allen
     * @since  Oct 12, 2011
     */
    public WireScanner  getDevice() {
        return this.smfDevice;
    }
    
    /**
     * Attached the given device to the panel and displays
     * its scan configuration parameters.
     *
     * @param ws    new hardware device to monitor
     * 
     * @since  Nov 13, 2009
     * @author Christopher K. Allen
     */
    public void setDevice(WireScanner ws) {
        this.smfDevice = ws;
        
        if (ws == null) 
            return;

        this.buildMonitorPool(ws);
        
        try {
            this.polMonitors.begin();
            
        } catch (ConnectionException e) {
            appLogger().logException(getClass(), e, "unable to start monitor pool for " + ws.getId());
            
        } catch (MonitorException e) {
            appLogger().logException(getClass(), e, "unable to start monitor pool for " + ws.getId());
            
        } catch (NoSuchChannelException e) {
            appLogger().logException(getClass(), e, "unable to start monitor pool for " + ws.getId());
            
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
        this.smfDevice = null;

        for (Map.Entry<ScadaFieldDescriptor, BooleanIndicatorPanel> entry: this.mapErrPnls.entrySet()) {
            BooleanIndicatorPanel pnlErr = entry.getValue();
            
            pnlErr.clearDisplay();
        }
        
//        for (Map.Entry<ScadaFieldDescriptor, NumberTextField> entry: this.mapTxtFlds.entrySet()) {
//            NumberTextField     txtFld = entry.getValue();
//            
//            txtFld.clearDisplay();
//        }

        this.pnlMvtStat.clearDisplay();
    }
    
    
    
    /**
     * Returns the panel title.
     * 
     * @return  the title of this panel
     * 
     * @since   Jan 21, 2010
     * @author  Christopher K. Allen
     */
    public String getTitle() {
        return STR_TITLE;
    }
    
    
    /**
     * Performs a channel connection test for the given
     * device.  Specifically, all the channels necessary to 
     * populate the SCADA data structure <code>PSet</code>
     * are checked for connection.
     *
     * @param ws    device we are checking
     * 
     * @return      <code>true</code> if all channels check out for above device,
     *              <code>false</code> if the <code>PSet</code> structure is bad 
     *                                 or at least one connection is missing
     *
     * @author Christopher K. Allen
     * @since  Feb 16, 2011
     */
    public boolean testConnection(WireScanner ws) {
        Class<?>                     clsScada    = WireScanner.DevStatus.class;
        List<ScadaFieldDescriptor>   lstFldDescr = new ScadaFieldList(clsScada);
        
        try {
            boolean bolConnect = ws.testConnection(lstFldDescr, DBL_TMO_CONNTEST); 

            if (!bolConnect) {

                this.appLogger().logWarning(getClass(), "Unable to connect to device " + ws.getId() + " for paramters in " + clsScada);
                return false;
            }
            
        } catch (BadStructException e) {
            this.appLogger().logWarning(getClass(), e.getMessage() +  " for device " + ws.getId() + " for parameter in " + clsScada);
            return false;

        } catch (BadChannelException e) {
            this.appLogger().logWarning(getClass(), e.getMessage() +  " for device " + ws.getId() + " for parameter in " + clsScada);
            return false;
            
        }
        
        return true;
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
    }
    /**
     * Builds the map of <code>{@link WireScanner.DevStatus.PROP}<code>
     * PV enumeration values to the error display panel.
     *
     * 
     * @since  Jan 25, 2010
     * @author Christopher K. Allen
     */
    private void        initErrorPanels() {

        // Build the error PV map
        for (ScadaFieldDescriptor fdKey : ScannerStatusPanel.ARR_ERR_PARAMS) {

            String        strLabel  = DeviceProperties.getLabel(fdKey);
            Integer       intValErr = DeviceProperties.getErrorValue(fdKey);
            Integer       intValNml = DeviceProperties.getNormalValue(fdKey);
            
            BooleanIndicatorPanel   pnlErr = new BooleanIndicatorPanel(strLabel, intValErr, intValNml);
            
            this.mapErrPnls.put(fdKey, pnlErr);
            this.add(pnlErr, this.gbcLayout);
            this.gbcLayout.gridy++;
        }
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
        
        ScadaFieldDescriptor fdMvStat =  WireScanner.DevStatus.FLD_MAP.get("mvtStatus");
        
        String          strLabels = DeviceProperties.getLabel(fdMvStat);
        String[]        arrLabels = strLabels.split(",");
        Integer[]       arrValues = DeviceProperties.getValuesInt(fdMvStat);

        this.pnlMvtStat = new MultiStateSelectorPanel(arrLabels, arrValues);
        this.pnlMvtStat.setEditable(false);
        this.pnlMvtStat.setFocusable(false);
        this.pnlMvtStat.setBorder( new BevelBorder(BevelBorder.RAISED) );
        this.add(this.pnlMvtStat, this.gbcLayout);
        this.gbcLayout.gridy++;
    }
    
    /**
     * Creates and configures the the "actuator parked"
     * indicator button.
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
    
        this.mapErrPnls.put(fdLimRev, this.pnlPark);
//        this.add(this.pnlPark, this.gbcLayout);
//        this.gbcLayout.gridy++;
    }


    /**
     * Creates and configures the the forward limit switch activated
     * indicator button.
     * 
     * @since  Jan 26, 2010
     * @author Christopher K. Allen
     */
    private void        initFwdLmtIndicator() {
        ScadaFieldDescriptor     fdLmtFwd = WireScanner.DevStatus.FLD_MAP.get("limFwd");
        
        String  strLabel = DeviceProperties.getLabel(fdLmtFwd);
        Integer intOff   = DeviceProperties.getNormalValue(fdLmtFwd);
        Integer intActvd = DeviceProperties.getErrorValue(fdLmtFwd);
        
        this.pnlFwdLmt = new BooleanIndicatorPanel(strLabel, intActvd, intOff);
        this.pnlFwdLmt.setOffColor(BooleanIndicatorPanel.CLR_UNSET);
        this.pnlFwdLmt.setOnColor(Color.GREEN);

        this.mapErrPnls.put(fdLmtFwd, this.pnlFwdLmt);
//        this.add(this.pnlFwdLmt, this.gbcLayout);
//        this.gbcLayout.gridy++;
    }

    /**
     * Creates an offset for the two status parameters so the user
     * is encouraged to separate them from the error parameters.
     *
     * @author Christopher K. Allen
     * @since  Nov 10, 2011
     */
    private void    initActuatorStatPanel() {
        JPanel pnlStatus = new JPanel();
        pnlStatus.setBorder( new BevelBorder(BevelBorder.LOWERED) );
        pnlStatus.setLayout(new GridBagLayout() );
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;
         
        pnlStatus.add(this.pnlPark, gbc);
        gbc.gridy++;
        
        pnlStatus.add(this.pnlFwdLmt, gbc);
        gbc.gridy++;

        this.add(pnlStatus, this.gbcLayout);
        this.gbcLayout.gridy++;
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

        // Monitors for the error status parameters
        for (ScadaFieldDescriptor       fdErr : this.mapErrPnls.keySet()) {
            BooleanIndicatorPanel       pnlErr  = this.mapErrPnls.get(fdErr);
            BooleanFieldUpdateAction    actErr  = new BooleanFieldUpdateAction(pnlErr);

            SmfPvMonitor        monErr  = new SmfPvMonitor(ws, fdErr);
            monErr.addAction(actErr);
            this.polMonitors.addMonitor(monErr);
        }

        // Monitor for the Movement status
         ScadaFieldDescriptor           pvdMvt = WireScanner.DevStatus.FLD_MAP.get("mvtStatus");
        
        SmfPvMonitor                    monMvt = new SmfPvMonitor(ws, pvdMvt);
        MultiStateSelectorUpdateAction  actMvt = new MultiStateSelectorUpdateAction(this.pnlMvtStat);
        monMvt.addAction(actMvt);
        this.polMonitors.addMonitor(monMvt);
    }
    
    /**
     * Add a vertical strut to the GUI at the current insert location.
     *
     * @author Christopher K. Allen
     * @since  Nov 3, 2011
     */
    private void addVerSpacer() {
        Component   cmpVerSpacer = Box.createVerticalStrut(10);
        
        this.add(cmpVerSpacer, this.gbcLayout);
        this.gbcLayout.gridy++;
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
