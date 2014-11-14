/**
 * DacqConfigurationView.java
 *
 *  Created	: Nov 12, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view;

import xal.app.pta.IConfigView;
import xal.app.pta.MainConfiguration;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.view.cmn.DeviceSelectorPanel;
import xal.app.pta.view.devcfg.DeviceConfigBasePanel;
import xal.app.pta.view.devcfg.HarpConfigDisplay;
import xal.app.pta.view.diag.HarpStatusPanel;
import xal.app.pta.view.plt.LiveDisplayBase.FORMAT;
import xal.app.pta.view.plt.LiveHarpDisplayPanel;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.impl.WireHarp;
import xal.smf.impl.profile.ParameterSet;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;



/**
 * Displays the configuration parameters for each harp
 * device, along with its current status.  Allow the user to make
 * changes to these configuration parameters.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Nov 12, 2009
 * @author Christopher K. Allen
 */
public class ConfigHarpView extends JPanel 
    implements IConfigView, DeviceSelectorPanel.IDeviceSelectionListener 
{

    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;
    
    /** Check channel connections to devices before proceeding */
    protected static final boolean BOL_DO_CONNTEST = AppProperties.DEVICE.EPICS_CA_CHK.getValue().asBoolean();
    
//    /** Time out to use when checking connections to the device */
//    protected static final double DBL_TMO_CONNTEST = AppProperties.DEVICE.TMO_CONNTEST.getValue().asDouble();

    
    
    /*
     * Local Attributes
     */
    
    /** We are configuring the devices of this accelerator */
    private final Accelerator           smfAccel;
    
    
    //
    // GUI Components
    // 
    
    /** The application's common device selection panel */
    private DeviceSelectorPanel        pnlDevSelector;
    

    //
    // Device Configuration
    
    /** The device configuration parameter GUI component */
    private HarpConfigDisplay           pnlDevConfig;
    
    //
    // Device Status
    
    /** The device status monitoring panel */
    private HarpStatusPanel             pnlDevStatus;
    
    
    //
    // Device Testing
    
    /** The panel used for doing scan tests for the currently selected device */
    private LiveHarpDisplayPanel          pnlDevTester;
    

    
    /**
     * Create a new <code>DacqConfigurationView</code> object
     * and attach it to the given data document.
     *
     * @param cfgMain   the source of devices
     *
     * @since     Nov 12, 2009
     * @author    Christopher K. Allen
     */
    public ConfigHarpView(MainConfiguration cfgMain) {
        this.smfAccel = cfgMain.getAccelerator();
        
        this.buildGuiComponents();
        this.layoutGuiComponents();
    }


    
    /*
     * DeviceSelectionPanel.IDeviceSelectionListener Interface
     */

    /**
     * Responds to the new devices selected event from the device selector panel.
     * All the configuration panels are cleared, the device status panel is cleared,
     * the connection to the new device (which must be one) is tested, then all the
     * panels are loaded with the new device information from Channel Access. 
     *
     * @since   Nov 13, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.cmn.DeviceSelectorPanel.IDeviceSelectionListener#newDeviceSelection(java.util.List)
     */
    @Override
    public void newDeviceSelection(List<AcceleratorNode> lstDevs) {

        // Check for a valid device to display
        if (lstDevs.size() == 0) {
            this.clearAllPanels();
            return;
        }
        
        AcceleratorNode smfNode = lstDevs.get(0);
        if (smfNode == null || !(smfNode instanceof WireHarp)) {
            this.clearAllPanels();
            return;
        }
        WireHarp        smfHarp = (WireHarp)smfNode;
        List<WireHarp>  lstHarps = new LinkedList<WireHarp>();
        lstHarps.add( smfHarp);
        
        // Directly set the devices for the panels if there is no connection testing
        if (! BOL_DO_CONNTEST ) {
            this.pnlDevConfig.setDevice(smfHarp);
            this.pnlDevStatus.setDevice(smfHarp);
            this.pnlDevTester.setProfileDevices( lstHarps );
        }

        // Do connection testing before associating the device to the panel
        DeviceConfigBasePanel<? extends ParameterSet>   pnlFocus = this.pnlDevConfig.getPanelInFocus();
        
        if (pnlFocus.connectionTest(smfHarp))
            this.pnlDevConfig.setDevice(smfHarp);
        else
            this.pnlDevConfig.clearDevice();
        
        if (this.pnlDevStatus.testConnection(smfHarp)) {            
          
            this.pnlDevStatus.setDevice(smfHarp);
            this.pnlDevTester.setProfileDevices(lstHarps);
            
        } else {
            this.pnlDevStatus.clearDevice();
            this.pnlDevTester.setLiveData(false);
            this.pnlDevTester.clearGraphs();
        }
    }

    /*
     * IConfigView Interface
     */
    
    /**
     * We need to reset the device selector panel with the new accelerator
     * then clear out all the other panels.
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
     * @since Jul 12, 2012
     * @see xal.app.pta.IConfigView#updateConfiguration(xal.app.pta.MainConfiguration)
     */
    @Override
    public void updateConfiguration(MainConfiguration cfgMain) {
//        this.pnlDevConfig.refreshDisplay();
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
    @SuppressWarnings("unchecked")
    private void buildGuiComponents(){

        // The device selector panel
        this.pnlDevSelector = new DeviceSelectorPanel(this.smfAccel, WireHarp.class);
        this.pnlDevSelector.setSingleSelectionMode(true);
        this.pnlDevSelector.setDeviceTableVisible(false);
        this.pnlDevSelector.registerDeviceSelectedListener(this);
        this.pnlDevSelector.setBorder(new TitledBorder("Current Devices"));

        
        // Create the device configuration panels
        this.pnlDevConfig = new HarpConfigDisplay("Device Configuration", SwingConstants.RIGHT);

        
        // The device status monitor panel
        this.pnlDevStatus = new HarpStatusPanel();
        this.pnlDevStatus.setBorder( new TitledBorder( this.pnlDevStatus.getTitle() ) );
        
        
        // Create the device live display
        this.pnlDevTester = new LiveHarpDisplayPanel(FORMAT.MULTIGRAPH_HOR);
        this.pnlDevTester.setLiveDataButtonVisible(true);
        this.pnlDevTester.setLiveData(false);
        this.pnlDevTester.setClearDataButton(true);
        this.pnlDevTester.setBorder( new TitledBorder( "Device Testing") );
    }
    

    /**
     * Lays out and build the GUI using
     * the initialized components.
     *
     * 
     * @since  Aug 19, 2009
     * @author Christopher K. Allen
     */
    private void layoutGuiComponents(){

        // Lay out the user GUI
        LayoutManager      mgrLayout = new GridBagLayout();

        this.setLayout(mgrLayout);

        GridBagConstraints gbcLayout = new GridBagConstraints();
        Insets             insLayout    = new Insets(0, 8, 0, 8);

        gbcLayout.insets = insLayout;
        
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        insLayout.left = 15;
        gbcLayout.weightx = 0.5;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        gbcLayout.fill  = GridBagConstraints.BOTH;
        this.add(this.pnlDevSelector, gbcLayout);

        gbcLayout.gridx = 1;
        gbcLayout.gridy = 0;
        insLayout.left = 8;
        gbcLayout.weightx = 0.0;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.fill  = GridBagConstraints.VERTICAL;
        this.add(this.pnlDevConfig, gbcLayout);

        gbcLayout.gridx = 2;
        gbcLayout.gridy = 0;
        insLayout.right = 15;
        gbcLayout.weightx = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_END;
        gbcLayout.fill  = GridBagConstraints.BOTH;
        this.add(this.pnlDevStatus, gbcLayout);
        
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth = 3;
        insLayout.right = 0;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_END;
        gbcLayout.fill  = GridBagConstraints.BOTH;
        this.add(this.pnlDevTester, gbcLayout);
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
        this.pnlDevStatus.clearDevice();
        this.pnlDevConfig.clearDevice();
        
        this.pnlDevTester.setLiveData(false);
        this.pnlDevTester.clearGraphs();
    }

}
