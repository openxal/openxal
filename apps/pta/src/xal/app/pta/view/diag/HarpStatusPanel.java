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
import xal.app.pta.tools.ca.SwingFieldMonitorActions.NumberFieldUpdateAction;
import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.tools.swing.BooleanIndicatorPanel;
import xal.app.pta.tools.swing.NumberTextField;
import xal.app.pta.tools.swing.NumberTextField.FMT;
import xal.ca.BadChannelException;
import xal.ca.ConnectionException;
import xal.ca.MonitorException;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireHarp;
import xal.smf.impl.WireScanner;
import xal.smf.scada.BadStructException;
import xal.smf.scada.ScadaFieldDescriptor;
import xal.smf.scada.ScadaFieldList;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JLabel;
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
 * @author Christopher K. Allen
 * @since  May 6, 2014
 */
public class HarpStatusPanel extends JPanel {

    
    
    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;


    
    /**  Title of GUI component */
    private static final String STR_TITLE = "Harp Status";

    /**  Number of columns in the input text fields */
    protected static final int CNT_COLS = AppProperties.TEXTFLD.COLS.getValue().asInteger();

    /**  Size of the vertical struts separating the GUI components */
    protected static final int INT_STRUT_SIZE = AppProperties.TEXTFLD.PADY.getValue().asInteger();

    /** Time out to use when checking connections to the device */
    protected static final double DBL_TMO_CONNTEST = AppProperties.DEVICE.TMO_CONNTEST.getValue().asDouble();


    
    /** Set of wire scanner status parameters monitored as error state */
    private static final ScadaFieldDescriptor[] ARR_ERR_PARAMS = {
        WireHarp.DevStatus.FLD_MAP.get("statDev"),
        WireHarp.DevStatus.FLD_MAP.get("statMps"),
        WireHarp.DevStatus.FLD_MAP.get("statCtrl"),
        WireHarp.DevStatus.FLD_MAP.get("hrpIns"),
        WireHarp.DevStatus.FLD_MAP.get("hrpRetr"),
        WireHarp.DevStatus.FLD_MAP.get("hrpStop")
    };


    /*
     * Local Attributes
     */
    
    
    //
    //  GUI Management
    //
    
    /** The layout manager constraint object */
    private GridBagConstraints       gbcLayout;

    
    //
    // GUI Components
    //
    
    /** The text display of the current harp command result */
    private NumberTextField         txtCmdRes;
    
    /** Map of error parameter constants to the PV display */
    private final Map<ScadaFieldDescriptor, BooleanIndicatorPanel>   mapErrPnls;
    
    
    //
    // Channel Access
    //
    
    /** The current hardware device */
    private WireHarp                         smfDevice;
    
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
    public HarpStatusPanel() {
        this.mapErrPnls  = new HashMap<ScadaFieldDescriptor, BooleanIndicatorPanel>();
        this.polMonitors = new SmfPvMonitorPool();
        
        this.initLayout();
        this.initErrorPanels();
        this.addVerSpacer();
        this.initCmdResultField();
    }

    
    /*
     * Operations
     */
    
    /**
     * Returns the harp device currently being monitored.
     *
     * @return  device that we are status monitoring
     *
     * @author Christopher K. Allen
     * @since  Oct 12, 2011
     */
    public WireHarp getDevice() {
        return this.smfDevice;
    }
    
    /**
     * Attached the given device to the panel and displays
     * its scan configuration parameters.
     *
     * @param smfHarp    new hardware device to monitor
     * 
     * @since  Nov 13, 2009
     * @author Christopher K. Allen
     */
    public void setDevice(WireHarp smfHarp) {
        this.smfDevice = smfHarp;
        
        if (smfHarp == null) 
            return;

        this.buildMonitorPool(smfHarp);
        
        try {
            this.polMonitors.begin();
            
        } catch (ConnectionException e) {
            appLogger().logException(getClass(), e, "unable to start monitor pool for " + smfHarp.getId());
            
        } catch (MonitorException e) {
            appLogger().logException(getClass(), e, "unable to start monitor pool for " + smfHarp.getId());
            
        } catch (NoSuchChannelException e) {
            appLogger().logException(getClass(), e, "unable to start monitor pool for " + smfHarp.getId());
            
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
        
        this.txtCmdRes.clearDisplay();
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
     * @param smfHarp    device we are checking
     * 
     * @return      <code>true</code> if all channels check out for above device,
     *              <code>false</code> if the <code>PSet</code> structure is bad 
     *                                 or at least one connection is missing
     *
     * @author Christopher K. Allen
     * @since  Feb 16, 2011
     */
    public boolean testConnection(WireHarp smfHarp) {
        Class<?>                     clsScada    = WireHarp.DevStatus.class;
        List<ScadaFieldDescriptor>   lstFldDescr = new ScadaFieldList(clsScada);
        
        try {
            boolean bolConnect = smfHarp.testConnection(lstFldDescr, DBL_TMO_CONNTEST); 

            if (!bolConnect) {

                this.appLogger().logWarning(getClass(), "Unable to connect to device " + smfHarp.getId() + " for paramters in " + clsScada);
                return false;
            }
            
        } catch (BadStructException e) {
            this.appLogger().logWarning(getClass(), e.getMessage() +  " for device " + smfHarp.getId() + " for parameter in " + clsScada);
            return false;

        } catch (BadChannelException e) {
            this.appLogger().logWarning(getClass(), e.getMessage() +  " for device " + smfHarp.getId() + " for parameter in " + clsScada);
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
        for (ScadaFieldDescriptor fdKey : HarpStatusPanel.ARR_ERR_PARAMS) {

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
     * Creates, configures, and attaches the command result text
     * field.
     * 
     * @since  Jan 25, 2010
     * @author Christopher K. Allen
     */
    private void        initCmdResultField() {
        
        ScadaFieldDescriptor fdMvStat =  WireHarp.DevStatus.FLD_MAP.get("cmdResult");
        
        String          strLabel = DeviceProperties.getLabel(fdMvStat);
        JLabel          lblCmdRes = new JLabel(strLabel);
        
        this.txtCmdRes = new NumberTextField(FMT.INT, 2);
        this.txtCmdRes.setEditable(false);
        this.txtCmdRes.setFocusable(false);
        this.txtCmdRes.setBorder( new BevelBorder(BevelBorder.RAISED) );
        
        Box     boxCmdRes = Box.createHorizontalBox();
        boxCmdRes.add(this.txtCmdRes);
        boxCmdRes.add(Box.createHorizontalStrut(5));
        boxCmdRes.add(lblCmdRes);
        
        this.add(boxCmdRes, this.gbcLayout);
        this.gbcLayout.gridy++;
    }
    
    /**
     * Builds the pool of monitors for all the PV values
     * we are to display on this panel.  
     *
     * @param smfHarp        the device whose status will be displayed
     * 
     * @since  Jan 25, 2010
     * @author Christopher K. Allen
     */
    private void        buildMonitorPool(WireHarp smfHarp) {

        // Monitors for the error status parameters
        for (ScadaFieldDescriptor       fdErr : this.mapErrPnls.keySet()) {
            BooleanIndicatorPanel       pnlErr  = this.mapErrPnls.get(fdErr);
            BooleanFieldUpdateAction    actErr  = new BooleanFieldUpdateAction(pnlErr);

            SmfPvMonitor        monErr  = new SmfPvMonitor(smfHarp, fdErr);
            monErr.addAction(actErr);
            this.polMonitors.addMonitor(monErr);
        }

        // Monitor for the Movement status
         ScadaFieldDescriptor           pvdMvt = WireHarp.DevStatus.FLD_MAP.get("cmdResult");
        
        SmfPvMonitor             monMvt = new SmfPvMonitor(smfHarp, pvdMvt);
        NumberFieldUpdateAction  actMvt = new NumberFieldUpdateAction(this.txtCmdRes);
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
