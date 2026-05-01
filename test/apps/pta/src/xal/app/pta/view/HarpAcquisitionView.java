/*
 * HarpAquisitionView.java
 * 
 * Dec 13, 2013
 * Christopher K. Allen
 */

package xal.app.pta.view;

import xal.app.pta.IConfigView;
import xal.app.pta.IDocView;
import xal.app.pta.MainApplication;
import xal.app.pta.MainConfiguration;
import xal.app.pta.MainDocument;
import xal.app.pta.MainHarpController;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.view.cmn.DeviceSelectorPanel;
import xal.app.pta.view.daq.HarpControlPanel;
import xal.app.pta.view.plt.LiveDisplayBase.FORMAT;
import xal.app.pta.view.plt.LiveHarpDisplayPanel;
import xal.smf.AcceleratorNode;
import xal.smf.impl.WireHarp;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;



/**
 * <p>
 * GUI for controlling the harp device data acquisition.
 * </p>
 * <p>
 * <h4>NOTE:</h4>
 * - The current installment is a kluge job which hard codes the harp PVs directly to the 
 * GUI.  There is no configuration.
 * <br/>
 * <br/>  
 * - I am working on a robust implementation that generalizes
 * profile devices but it is requiring more effort than anticipated.  This is a stop gap.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Dec 13, 2013
 * @author Christopher K. Allen
 */
public class HarpAcquisitionView extends JPanel implements IDocView, IConfigView, DeviceSelectorPanel.IDeviceSelectionListener {


    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;
    
    
    
    /*
     * Instance Attributes
     */
    
    
    //
    // Application Components
    //
    
    /** The central data of the application */
    private final MainDocument   docMain;
    
    
    
    //
    // GUI Components
    //
    
    /** The profile device selection panel */
    private DeviceSelectorPanel         pnlDevSel;
    
    
    /** The data acquisition control panel */
    private HarpControlPanel            pnlDaqCtrl;

    
    /** The acquisition data display panel */
    private LiveHarpDisplayPanel        pnlAcqDatPlt;
    
    
    
    /*
     * Initialization
     */
    
    
    /**
     * Create a new <code>ScanAcquisitionView</code> object
     * and attach it to the given main data document with
     * it main application window.
     *
     * @param docMain   document where the measurement data is stored
     *
     * @since     Jun 10, 2009
     * @author    Christopher K. Allen
     */
    public HarpAcquisitionView(MainDocument docMain) {
        this.docMain = docMain;
        
        this.buildGuiComponents(); //Creation of all GUI components
        this.layoutGui();          //Add all components to the layout and panels
    }

    /**
     * Returns the main application data object.
     *
     * @return  the data document for the application
     *
     * @since  Jun 10, 2009
     * @author Christopher K. Allen
     */
    public MainDocument getMainDocument() {
        return docMain;
    }
    
    /**
     * Returns the application event lgrApp
     *
     * @return the event lgrApp for the main application
     * 
     * @since  Nov 14, 2009
     * @author Christopher K. Allen
     */
    public IEventLogger getLogger() {
        return MainApplication.getEventLogger();
    }


    
    /*
     * Event Handlers
     */
    
    
    

   
    /*
     * DeviceSelectionPanel.IDeviceSelected Interface
     */
    
    /**
     * <p>
     * Forwards the selected devices to the DAQ controller.
     * </p>
     * <p>
     * I prefer to catch the device selection event at the top
     * level and forward it in order to performs any other action
     * that may be necessary in the future.
     * </p>
     *
     * @author  Christopher K. Allen
     * @since   Nov 17, 2009
     * @version April 14, 2014
     *
     * @see xal.app.pta.view.cmn.DeviceSelectorPanel.IDeviceSelectionListener#newDeviceSelection(java.util.List)
     */
    @Override
    public void newDeviceSelection(List<AcceleratorNode> lstDevs) {
        if (lstDevs.size() == 0)
            return;
        
        List<WireHarp>   lstHarps = new LinkedList<WireHarp>();
        
        for (AcceleratorNode smfDev : lstDevs) 
            if (smfDev instanceof WireHarp)
                lstHarps.add((WireHarp) smfDev);
        
        this.pnlDaqCtrl.setProfileDevices(lstHarps);
        this.pnlAcqDatPlt.setProfileDevices(lstHarps);
    }

    
    /*
     * IConfigView Interface 
     */

    /**
     * Resets the accelerator in the device selection panel
     *  
     * @param cfgMain   main application document 
     *
     * @since   Jun 18, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.IConfigView#updateAccelerator(xal.app.pta.MainConfiguration)
     */
    @Override
    public void updateAccelerator(MainConfiguration cfgMain) {
        
        if (this.pnlDevSel != null)
            this.pnlDevSel.resetAccelerator(cfgMain.getAccelerator());
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
     * IDocView Interface
     */
    
    /**
     * <p>
     * Update the graphs and the device selection display.
     * </p>
     *
     * @since   Mar 1, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.IDocView#updateMeasurementData(xal.app.pta.MainDocument)
     */
    @Override
    public void updateMeasurementData(MainDocument docMain) {
        
        // Get the raw measurement data and the device color map from the device selector
        MeasurementData     datMsmt  = docMain.getMeasurementData();
        Map<String, Color>  mapDevClr = this.pnlDevSel.getDeviceIdColorMap();
        
        // Draw the raw data curves on the graph
        this.pnlAcqDatPlt.clearGraphs();
        this.pnlAcqDatPlt.setLiveData(false);
        this.pnlAcqDatPlt.displayRawData(datMsmt, mapDevClr);

//        Set<String> setDevIds = datMsmt.getDeviceIdSet();
//        for (String strDevId : setDevIds)
//            this.pnlDevSel.setDeviceSelected(strDevId);
    }


    
    /*
     * GUI Support Methods
     */
    
    
    /**
     * Initializes all the components of the
     * GUI display.
     * 
     * @since  Aug 19, 2009
     * @author Christopher K. Allen
     */
    @SuppressWarnings("unchecked")
    private void buildGuiComponents()   {

        
        // The device selection panel
        this.pnlDevSel = new DeviceSelectorPanel( 
                        this.docMain.getAccelerator(),
                        WireHarp.class
                        );
        this.pnlDevSel.setBorder(new TitledBorder("Select Profile Devices") );
        this.pnlDevSel.setDeviceTableColorKeyed(true);
        this.pnlDevSel.registerDeviceSelectedListener(this);
        
        
        // The DAQ device controller GUI component 
        this.pnlDaqCtrl = new HarpControlPanel(this.docMain);
          
        // The live acquisition data display
        this.pnlAcqDatPlt = new LiveHarpDisplayPanel(FORMAT.MULTIGRAPH_HOR);
        MainHarpController.getInstance().registerControllerListener( this.pnlAcqDatPlt );
        this.pnlAcqDatPlt.setBorder(new TitledBorder("Current Data"));
        this.pnlAcqDatPlt.setLiveData(false);
        this.pnlAcqDatPlt.setLiveDataButtonVisible(true);
        this.pnlAcqDatPlt.setDeviceColorMap( this.pnlDevSel.getDeviceColorMap() );
    }

    /**
     * Lays out and build the GUI using
     * the initialized components.
     *
     * 
     * @since  Aug 19, 2009
     * @author Christopher K. Allen
     */
    private void layoutGui() {

        // Setup up the layout manager for this pane
        this.setLayout( new GridBagLayout() );

        GridBagConstraints      gbcLayout = new GridBagConstraints();
        gbcLayout.insets = new Insets(5,5,5,5);;
        
        // The device selection panel
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.weightx = 0.3;
        gbcLayout.weighty = 0.01;
        gbcLayout.fill = GridBagConstraints.BOTH;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.gridheight = 2;
        gbcLayout.gridwidth = 1;
        this.add(this.pnlDevSel, gbcLayout);
        
//        // The scan mode indicator panel
//        gbcLayout.gridx = 1;
//        gbcLayout.gridy = 0;
//        gbcLayout.weightx = 0.1;
//        gbcLayout.weighty = 0.;
//        gbcLayout.fill = GridBagConstraints.NONE;
//        gbcLayout.anchor = GridBagConstraints.LINE_END;
//        gbcLayout.gridheight = 1;
//        gbcLayout.gridwidth = 1;
//        this.add(this.pnlScanInd, gbcLayout);
        
        // The data acquisition controls
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 1;
        gbcLayout.weightx = 0.3;
        gbcLayout.weighty = 0.;
        gbcLayout.fill = GridBagConstraints.BOTH;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.gridheight = 1;
        gbcLayout.gridwidth = 1;
        this.add(this.pnlDaqCtrl, gbcLayout);

        // The data display with update button
        gbcLayout.insets = new Insets(0,0,0,0);
        
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 2;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
        gbcLayout.fill = GridBagConstraints.BOTH;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.gridheight = 1;
        gbcLayout.gridwidth = 2;
        this.add(this.pnlAcqDatPlt, gbcLayout);
    }
    
}








