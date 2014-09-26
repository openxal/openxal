/**
 * ConfigTriggeringView.java
 *
 *  Created	: April 1, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import xal.app.pta.IConfigView;
import xal.app.pta.MainConfiguration;
import xal.app.pta.MainScanController;
import xal.app.pta.view.cmn.DeviceSelectorPanel;
import xal.app.pta.view.daq.ScanControlPanel;
import xal.app.pta.view.daq.ScanProgressPanel;
import xal.app.pta.view.plt.LiveDisplayBase;
import xal.app.pta.view.plt.LiveDisplayBase.FORMAT;
import xal.app.pta.view.plt.LiveScanDisplayPanel;
import xal.app.pta.view.plt.LiveTraceDisplayPanel;
import xal.app.pta.view.sel.TriggerSelectorPanel;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.impl.WireScanner;

/**
 * Displays the triggering configuration parameters for each wire scanner,
 * allowing the using to adjust the timing for best signal integrity.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  April 1, 2010
 * @author Christopher K. Allen
 */
public class ConfigTriggeringView extends JPanel 
    implements IConfigView, DeviceSelectorPanel.IDeviceSelectionListener 
{

    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;
    
    
    
    /*
     * Local Attributes
     */
    
    /** The application hardware devices */
    private final Accelerator           smfAccel;
    
//    /** handle to the main document of the application */
//    private final MainDocument          docMain;
    
    //
    // Device Control 
    //
    
    /** 
     * The local device controller. 
     * We cannot use the main application instance because
     * it would prompt all listeners that actual data 
     * taking was occurring.
     */
    private final MainScanController         ctrDevCtrl;
    
    
    //
    // GUI Components
    // 
    
    /** The application's common device selection panel */
    private DeviceSelectorPanel        pnlDevSelector;
    
    
    /** Edit display of the timing configuration parameters */
    private TriggerSelectorPanel        pnlTrgSel;
    
//    /** The scan control panel */
//    private ScanControlPanelDepr            pnlScanCtrl;

    /** The scan control panel */
    private ScanControlPanel            pnlScanCtrl;
    
    /** The scan actuator position display */
    private ScanProgressPanel        pnlPosDspl;
    
    

    /** Display graphs for the live signal trace */
    private LiveTraceDisplayPanel       pnlPltTrace;
    
    /** Graph displaying profile signals */
    private LiveScanDisplayPanel         pnlPltSignal;
    
    
    /**
     * Create a new <code>DacqConfigurationView</code> object
     * and attach it to the given data document.
     *
     * @param smfAccel  the hardware devices
     *
     * @since     Nov 12, 2009
     * @author    Christopher K. Allen
     */
    public ConfigTriggeringView(Accelerator smfAccel) {
        this.smfAccel = smfAccel;

//        this.docMain    = MainApplication.getApplicationDocument();
        this.ctrDevCtrl = MainScanController.getInstance();
        
        this.buildGuiComponents();
        this.buildGui();
    }


    
    /*
     * DeviceSelectionPanel.IDeviceSelectionListener Interface
     */

    /**
     * Check that there is a valid device, then send
     * it to the position display panel, the timing configuration
     * panel, and the scan control panel.
     *
     * @since   Nov 13, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.cmn.DeviceSelectorPanel.IDeviceSelectionListener#newDeviceSelection(java.util.List)
     */
    @Override
    public void newDeviceSelection(List<AcceleratorNode> lstDevs) {

        // Check for empty device list 
        if (lstDevs.size() == 0) {
            this.clearAllPanels();
            return;
        }
        
        // Parse the device list for wire scanner types
        List<WireScanner>   lstScnrs = new LinkedList<WireScanner>();
        for (AcceleratorNode smfNode : lstDevs) {
            if (smfNode instanceof WireScanner) {
                WireScanner ws = (WireScanner)smfNode;
                lstScnrs.add(ws);
            }
        }

        // Check for empty wire scanner list
        if (lstScnrs.size() == 0) {
            this.clearAllPanels();
            return;
        }

        // Active scan configuration for given device 
        WireScanner     ws = lstScnrs.get(0);
        
        this.pnlPosDspl.setDevice(ws);
        this.pnlTrgSel.setDevice(ws);
        this.pnlScanCtrl.setDaqDevices(lstScnrs);
    }

    /*
     * IConfigView Interface
     */
    
    /**
     *
     * @since 	Nov 12, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.IConfigView#updateAccelerator(xal.app.pta.MainConfiguration)
     */
    @Override
    public void updateAccelerator(MainConfiguration cfgMain) {
        this.pnlDevSelector.resetAccelerator(cfgMain.getAccelerator());
        this.clearAllPanels();
    }

    /**
     * Nothing to do.
     * 
     * @since Jul 12, 2012
     * @see xal.app.pta.IConfigView#updateConfiguration(xal.app.pta.MainConfiguration)
     */
    @Override
    public void updateConfiguration(MainConfiguration cfgMain) {
    }


    /*
     * Support Methods
     */
    
    
    /**
     * Initializes all the components of the
     * GUI display.
     *
     * 
     * @since  Aug 19, 2009
     * @author Christopher K. Allen
     */
    private void buildGuiComponents(){

        // The device selector panel
        this.pnlDevSelector = new DeviceSelectorPanel(this.smfAccel, WireScanner.class );
        this.pnlDevSelector.setSingleSelectionMode(true);
        this.pnlDevSelector.setDeviceTableVisible(false);
        this.pnlDevSelector.registerDeviceSelectedListener(this);
        this.pnlDevSelector.setBorder(new TitledBorder("Current Devices"));
        
        // Create the timing editor panel 
        this.pnlTrgSel = new TriggerSelectorPanel();
        this.pnlTrgSel.setBorder( new TitledBorder("Timing Parameters") );
        
        // The device status monitor panel
//        this.pnlScanCtrl = new ScanControlPanelDepr();
//        this.pnlScanCtrl.setBorder( new TitledBorder("Actuator Control") );
//      this.pnlScanCtrl.setDaqController(this.ctrDevCtrl);
        this.pnlScanCtrl = new ScanControlPanel();
        
        // The position display panel
        this.pnlPosDspl = new ScanProgressPanel();
        this.pnlPosDspl.setBorder( new TitledBorder("Actuator Status") );
        
        // Create the display panels
        this.pnlPltTrace = new LiveTraceDisplayPanel(LiveDisplayBase.FORMAT.MULTIGRAPH_HOR);
        this.pnlPltTrace.setBorder( new TitledBorder("Trace") );
        this.ctrDevCtrl.registerControllerListener(this.pnlPltTrace);
        
        this.pnlPltSignal = new LiveScanDisplayPanel(FORMAT.MULTIGRAPH_HOR);
        this.pnlPltSignal.setBorder( new TitledBorder("Profile") );
        this.ctrDevCtrl.registerControllerListener(this.pnlPltSignal);
    }


    /**
     * Lays out and build the GUI using
     * the initialized components.
     *
     * 
     * @since  Aug 19, 2009
     * @author Christopher K. Allen
     */
    private void buildGui(){

        // Lay out the user GUI
        LayoutManager      mgrLayout = new GridBagLayout();

        this.setLayout(mgrLayout);

        GridBagConstraints gbcLayout = new GridBagConstraints();
        Insets             insLayout    = new Insets(0, 8, 0, 8);

        gbcLayout.insets = insLayout;
        
        // Device selector
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 2;
        insLayout.left = 7;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.2;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        gbcLayout.fill  = GridBagConstraints.BOTH;
        this.add(this.pnlDevSelector, gbcLayout);

        // Timing selector (editor)
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        insLayout.right = 7;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.fill  = GridBagConstraints.NONE;
        this.add(this.pnlTrgSel, gbcLayout);
        
        // Signal plots
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth = 2;
        insLayout.right = 0;
        gbcLayout.weightx = 0.5;
        gbcLayout.weighty = 0.3;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        gbcLayout.fill  = GridBagConstraints.BOTH;
        this.add(this.pnlPltSignal, gbcLayout);
        
        // Wire position display
        gbcLayout.gridx = 2;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        insLayout.right = 0;
        gbcLayout.weightx = 0.2;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_END;
        gbcLayout.fill  = GridBagConstraints.HORIZONTAL;
        this.add(this.pnlPosDspl, gbcLayout);
        
        // Trace plots
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 2;
        gbcLayout.gridwidth = 2;
        gbcLayout.gridheight = 1;
        insLayout.right = 0;
        gbcLayout.weightx = 0.5;
        gbcLayout.weighty = 0.2;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        gbcLayout.fill  = GridBagConstraints.BOTH;
        this.add(this.pnlPltTrace, gbcLayout);
        
        // Scan control panel
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 2;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        insLayout.right = 0;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.fill  = GridBagConstraints.HORIZONTAL;
        this.add(this.pnlScanCtrl, gbcLayout);
        
    }


    /**
     * Clears all the device configuration parameter
     * panels in the GUI display.
     *
     * 
     * @since  Jan 14, 2010
     * @author Christopher K. Allen
     */
    private void clearAllPanels() {
//        this.pnlScanCtrl.clearDevice();
        this.pnlScanCtrl.clearDevices();
        this.pnlPosDspl.clearDevice();
        this.pnlTrgSel.clearDevice();
        this.pnlPltTrace.clearGraphs();
        this.pnlPltSignal.clearGraphs();
    }

}
