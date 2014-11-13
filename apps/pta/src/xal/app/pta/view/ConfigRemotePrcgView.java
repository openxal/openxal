/**
 * ConfigRemotePrcgView.java
 *
 *  Created	: May 12, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view;

import xal.app.pta.IConfigView;
import xal.app.pta.MainApplication;
import xal.app.pta.MainConfiguration;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.view.analysis.ProfileAttrsDisplayPanel;
import xal.app.pta.view.analysis.ProfileAttrsDisplayPanel.LAYOUT;
import xal.app.pta.view.cmn.DeviceSelectorPanel;
import xal.app.pta.view.cmn.DeviceSelectorPanel.IDeviceSelectionListener;
import xal.app.pta.view.plt.MultiGraphDisplayPanel;
import xal.app.pta.view.plt.SavedTraceDisplayPanel;
import xal.app.pta.view.sel.ProcessWindowParametersPanel;
import xal.app.pta.view.sel.TraceIndexSelector;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireScanner;
import xal.smf.impl.WireScanner.CMD;
import xal.smf.impl.WireScanner.CMDARG;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


/**
 * <p>
 * This panel allows the using to adjust the on-board profile
 * analysis parameters (i.e., the analysis done on the profile
 * hardware control computer).  The user is given visual feedback
 * as to where the parameters are with respect to the parameter
 * domain, and how the parameter choices affect the analysis
 * results of profile data.
 * </p>
 * <p>
 * The data from the last acquisition is left on the control computer
 * until another scan is run.  This data is used for the analysis.
 * The users is able to see how the processed data is affected by
 * the processing parameters.  This includes the profile measurements,
 * the profile fits, and the statistics.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  May 12, 2010
 * @author Christopher K. Allen
 */
public class ConfigRemotePrcgView extends JPanel 
    implements IConfigView, IDeviceSelectionListener 
{

    
    /*
     * Inner Classes
     */
    
    /**
     * Responds to a change in the processing window parameters initialed
     * by the <code>{@link SavedTraceDisplayPanel}</code>.  Then on-board
     * data is reanalyzed then the data is replotted.
     *
     * @author Christopher K. Allen
     * @since   Apr 1, 2011
     */
    private class ProcessWindowUpdateAction implements ActionListener {

        /**
         * Reanalyzing the data on the device controller then acquires
         * the new data and displays it.
         * 
         * @since Apr 1, 2011
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @SuppressWarnings("synthetic-access")
        @Override
        public void actionPerformed(ActionEvent e) {
            ConfigRemotePrcgView.this.reanalyzeProfileData();
            ConfigRemotePrcgView.this.plotCurrentProfiles();
        }
        
    }
    /**
     * Responds to changes in the slider which selects the
     * interested trace pattern.  This is the trace pattern of
     * the scan step index, the index displayed on the slider.
     *
     * @since  Jun 7, 2010
     * @author Christopher K. Allen
     */
    private class TraceSelectorAction implements ActionListener {

        /*
         * ActionListener Interface
         */
        
        /**
         * This method is basically a delegate to 
         * the <code>{@link ConfigRemotePrcgView#reanalyzeProfileData()}</code>
         * method of the outer class.  We do screen the method invocation
         * for final values of the slider, ignoring all other change
         * events.
         *
         * @since   Jun 8, 2010
         * @author  Christopher K. Allen
         *
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @SuppressWarnings("synthetic-access")
        @Override
        public void actionPerformed(ActionEvent e) {
            ConfigRemotePrcgView.this.reanalyzeProfileData();
            ConfigRemotePrcgView.this.plotCurrentProfiles();
        }
    }
    

    
    
    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;

    

    
    /*
     * Local Attributes
     */
    
    //
    // Application
    
    /** The application accelerator hardware */
    private final Accelerator           smfAccel;
    
    /** The wire scanner device under focus */
    private WireScanner                 smfDev;                 

    
    //
    // GUI Components
    // 

    //
    // Input
    /** The application's common device selection panel */
    private DeviceSelectorPanel        pnlDevSelector;

    
    //
    // Trace combination panel
    /** Slide selector for the trace index */
    private TraceIndexSelector              selTrcInd;
    
    /** The text field display of the processing window parameters */
    private ProcessWindowParametersPanel    pnlTxtPrcg;
    
    /** Plot of the trace at the current actuator position */
    private SavedTraceDisplayPanel          pnlTrcDisplay;
    
    /** Panel of the above three GUI components */
    private JPanel                          pnlTrace;

    
    //
    // Output
    /** GUI component displaying statistical properties of profile */
    private ProfileAttrsDisplayPanel    pnlProfStats;
    
    /** Graph displaying profile signals */
    private MultiGraphDisplayPanel      pltProfiles;
    

    //
    // Event Response
    /** Response to a change in trace index by user */
    private final TraceSelectorAction   actNewIndex;

    /** Button to refresh display of analyzed data */
    private JButton                     butRefresh;
    
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>ConfigRemotePrcgView</code> object.
     *
     * @param smfAccel  accelerator containing the hardware devices
     *
     * @since     May 14, 2010
     * @author    Christopher K. Allen
     */
    public ConfigRemotePrcgView(Accelerator     smfAccel) {
        this.smfAccel     = smfAccel;
        this.smfDev       = null;
        this.actNewIndex  = new TraceSelectorAction();
        
        this.buildGuiComponents();
        this.initGuiActions();
        this.layoutTracePanel();
        this.layoutGuiComponents();
    }
    
    /**
     * We need to turn of the lights before we leave the room.
     * The processing parameters monitors must be shut down.
     *
     * @since 	Jun 11, 2010
     * @author  Christopher K. Allen
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() {
    }
    
    
//    /*
//     * IDocView Interface
//     */
//    
//    /**
//     * Reset the device selector display in the event of 
//     * a new accelerator.
//     *
//     * @since 	May 12, 2010
//     * @author  Christopher K. Allen
//     *
//     * @see xal.app.pta.view.IDocView#updateAccelerator(xal.app.pta.MainDocument)
//     */
//    @Override
//    public void updateAccelerator(MainDocument docMain) {
//        if (this.pnlDevSelector != null)
//            this.pnlDevSelector.resetAccelerator(docMain.getAccelerator());
//    }
//
//    /**
//     * Since this view is not concerned with measurement data per se,
//     * we do nothing.
//     *
//     * @since 	May 12, 2010
//     * @author  Christopher K. Allen
//     *
//     * @see xal.app.pta.view.IDocView#updateMeasurementData(xal.app.pta.MainDocument)
//     */
//    @Override
//    public void updateMeasurementData(MainDocument docMain) {
//    }
//    
    
    /*
     * IConfigView Interface
     */

    /**
     * Not used.
     * 
     * @since Jul 10, 2012
     * @see xal.app.pta.IConfigView#updateConfiguration(xal.app.pta.MainConfiguration)
     */
    @Override
    public void updateConfiguration(MainConfiguration cfgMain) {
    }

    /**
     * Reset the device selector display in the event of 
     * a new accelerator.
     * 
     * @since Jul 10, 2012
     * @see xal.app.pta.IConfigView#updateAccelerator(xal.app.pta.MainConfiguration)
     */
    @Override
    public void updateAccelerator(MainConfiguration cfgMain) {
        if (this.pnlDevSelector != null)
            this.pnlDevSelector.resetAccelerator(cfgMain.getAccelerator());
    }
    

    
    /**
     * Tell the GUI components that a new device has been selected so they can update
     * their displays as necessary.
     *
     * @since 	May 12, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.cmn.DeviceSelectorPanel.IDeviceSelectionListener#newDeviceSelection(java.util.List)
     */
    @Override
    public void newDeviceSelection(List<AcceleratorNode> lstDevs) {
        
        // Check for a valid device to display
        if (lstDevs.size() == 0) {
            this.clearAllPanels();
            this.selTrcInd.setEnabled(false);
            
            return;
        }
        
        // There will be only one selected device
        AcceleratorNode smfNode = lstDevs.get(0);
        if (smfNode == null || !(smfNode instanceof WireScanner)) {
            this.clearAllPanels();
            this.selTrcInd.setEnabled(false);
            
            return;
        }

        
        // We're in business - the device is valid
        this.smfDev = (WireScanner)smfNode;
        

        try {
            
            // Get the scan configuration parameters in order 
            //  to set trace index bounds
            WireScanner.ScanConfig cfgScan = WireScanner.ScanConfig.acquire(this.smfDev);

            this.selTrcInd.setEnabled(true);
            this.selTrcInd.addActionListener(this.actNewIndex);
            this.selTrcInd.setRange(1, cfgScan.stepCount);
            this.selTrcInd.setDisplayValueSilently(cfgScan.stepCount/2);
            
            this.pnlTrcDisplay.setDevice(this.smfDev);
            this.pnlTxtPrcg.setDevice(this.smfDev);

            this.plotCurrentProfiles();

        } catch (ConnectionException e) {
            getLogger().logException(getClass(), e, "Unable to connect with " + this.smfDev.getId());
            
        } catch (GetException e) {
            getLogger().logException(getClass(), e, "Unable to read from " + this.smfDev.getId());
            
        } catch (NoSuchChannelException e) {
            getLogger().logException(getClass(), e, "Missing channel for trace processing PV monitors in " + this.smfDev.getId());
            
        }
    }
    
    /**
     * Instantiates all the GUI components used on the GUI
     * face.
     *
     * 
     * @since  May 14, 2010
     * @author Christopher K. Allen
     */
    @SuppressWarnings("unchecked")
    private void buildGuiComponents() {
        // The device selector panel
        this.pnlDevSelector = new DeviceSelectorPanel(this.smfAccel, WireScanner.class);
        this.pnlDevSelector.setSingleSelectionMode(true);
        this.pnlDevSelector.setDeviceTableVisible(false);
        this.pnlDevSelector.registerDeviceSelectedListener(this);
        this.pnlDevSelector.setBorder(new TitledBorder("Current Devices"));
        
        
        // Build all the GUI components
        //      The panel containing all the trace-oriented components
        this.pnlTrace = new JPanel();
        this.pnlTrace.setBorder( new TitledBorder("Trace") );
        
        //      Build the processing window parameters text monitor
        this.pnlTxtPrcg = new ProcessWindowParametersPanel();

        // Selector for the trace index
        this.selTrcInd = new TraceIndexSelector();
        this.selTrcInd.setEnabled(false);
        
        this.pnlTrcDisplay = new SavedTraceDisplayPanel();
        this.pnlTrcDisplay.setModifyHardwareParameters(true);
        this.pnlTrcDisplay.addParameterChangeListener( new ProcessWindowUpdateAction() );

        //      Statistical properties of the profiles
        this.pnlProfStats = new ProfileAttrsDisplayPanel(LAYOUT.HOR);
        this.pnlProfStats.setDisplayDatasetTitle(true);
        this.pnlProfStats.setBorder( new TitledBorder("Profile Characteristics") );
        
        //      The profile graphs
        this.pltProfiles = new MultiGraphDisplayPanel(MultiGraphDisplayPanel.LAYOUT.HOR);
        this.pltProfiles.setBorder( new TitledBorder("Profile") );
        
        // Event generation
        //  The refresh button
        ImageIcon       icnRefresh = AppProperties.ICON.REFRESH.getValue().asIcon();
        
        this.butRefresh = new JButton();
        this.butRefresh.setIcon(icnRefresh);
        this.butRefresh.setText("Refresh");
    }
    
    /**
     * Creates and initializes the user event
     * responses handled directly by the GUI components.
     *
     * @author Christopher K. Allen
     * @since  Jan 27, 2011
     */
    private void initGuiActions() {

        // Create the refresh event response
        ActionListener  actRefresh = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (smfDev == null)
                    return;
                
                reanalyzeProfileData();
                plotCurrentProfiles();
            }
        };
        
        // Attach it to the refresh button
        this.butRefresh.addActionListener(actRefresh);
    }
    
    /**
     * Lays out the components of the trace display
     * and interaction panel.
     *
     * @author Christopher K. Allen
     * @since  Apr 4, 2011
     */
    private void layoutTracePanel() {
    
        this.pnlTrace.setLayout( new GridBagLayout() );
        GridBagConstraints      gbcLayout = new GridBagConstraints();
        gbcLayout.insets  = new Insets(0, 5, 0, 5);
    
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.fill  = GridBagConstraints.HORIZONTAL;
        this.pnlTrace.add(this.pnlTxtPrcg, gbcLayout);
        
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.fill  = GridBagConstraints.HORIZONTAL;
        this.pnlTrace.add(this.selTrcInd, gbcLayout);
        
        gbcLayout.gridx = 2;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.fill  = GridBagConstraints.NONE;
        this.pnlTrace.add(this.butRefresh, gbcLayout);
        
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth = 3;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 1;
        gbcLayout.weighty = 1;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.fill  = GridBagConstraints.BOTH;
        this.pnlTrace.add(this.pnlTrcDisplay, gbcLayout);
    }

    /**
     * Lay out the GUI components on the view's
     * GUI face.
     *
     * 
     * @since  May 14, 2010
     * @author Christopher K. Allen
     */
    private void layoutGuiComponents() {
        
        this.setLayout( new GridBagLayout() );
        GridBagConstraints      gbcLayout = new GridBagConstraints();
        gbcLayout.insets  = new Insets(0, 5, 0, 5);

        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 2;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        gbcLayout.fill  = GridBagConstraints.BOTH;
        this.add(this.pnlDevSelector, gbcLayout);
        
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 2;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.fill  = GridBagConstraints.HORIZONTAL;
        this.add(this.pnlProfStats, gbcLayout);
        
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth = 2;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.5;
        gbcLayout.weighty = 0.5;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.fill  = GridBagConstraints.BOTH;
        this.add(this.pltProfiles, gbcLayout);

        gbcLayout.gridx = 0;
        gbcLayout.gridy = 2;
        gbcLayout.gridwidth = 3;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 1.0;
        gbcLayout.weighty = 1.0;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.fill  = GridBagConstraints.BOTH;
        this.add( this.pnlTrace, gbcLayout );
        
    }

    
    
    /**
     * Commands the current profile device to recompute the
     * profile data using the current processing parameters.
     * The data is then displayed on the GUI plots.
     *
     * 
     * @since  Jun 11, 2010
     * @author Christopher K. Allen
     */
    private void reanalyzeProfileData() {
        
        WireScanner.CmdPck      cmdUpdtTrc = null; 
        try {
            cmdUpdtTrc = new WireScanner.CmdPck(CMD.UPDATE, CMDARG.PROF);

            this.smfDev.runCommand(CMD.RESET);
            this.smfDev.runCommand(cmdUpdtTrc);
            this.smfDev.runCommand(WireScanner.CMD.REANALYZE);

        } catch (ConnectionException e) {
            getLogger().logException(this.getClass(), e, "Unable to connect to " + smfDev.getId());
            
        } catch (PutException e) {
            getLogger().logException(this.getClass(), e, "Command " + cmdUpdtTrc + " failed for device " + smfDev.getId());

        } catch (IllegalArgumentException e) {
            getLogger().logException(getClass(), e, "Malformed command " + cmdUpdtTrc + " for device " + smfDev.getId());
            
        } catch (InterruptedException e) {
            getLogger().logException(this.getClass(), e, "Command thread interrupted for " + smfDev.getId());
            
        } catch (NoSuchChannelException e) {
            getLogger().logException(this.getClass(), e, "Bad profile data channel for " + smfDev.getId());
            
        }
        
    }
    
    /**
     * Reads the profile data from the buffer in the currently focused
     * wire scanner device.  The data is then plotted on the GUI
     * face.
     *
     * 
     * @since  May 26, 2010
     * @author Christopher K. Allen
     */
    private void plotCurrentProfiles() {
    
        // Make sure device is selected
        if (this.smfDev == null)
            return;
        
        try {
            WireScanner.DataRaw            datDev   = WireScanner.DataRaw.acquire(this.smfDev);
            WireScanner.StatisticalAttrSet attrStat = WireScanner.StatisticalAttrSet.acquire(smfDev);
            
            this.pnlTrcDisplay.refresh();
            
            this.pltProfiles.clear();
            this.pltProfiles.displayProfile(datDev);
    
            this.pnlProfStats.clearDisplay();
            this.pnlProfStats.display(attrStat);
            
        } catch (ConnectionException e) {
            getLogger().logException(getClass(), e, "Could not connect with " + this.smfDev.getId());
            
        } catch (GetException e) {
            getLogger().logException(getClass(), e, "Could not read from " + this.smfDev.getId());
            
        } catch (NoSuchChannelException e) {
            getLogger().logException(getClass(), e, "Channel not found for " + this.smfDev.getId());
            
        }
    }

    /**
     * Clear all GUI panels of device information.
     *
     * 
     * @since  May 27, 2010
     * @author Christopher K. Allen
     */
    private void clearAllPanels() {
        this.pnlTrcDisplay.clearDevice();
        this.pnlProfStats.clearDisplay();
        this.pnlTrcDisplay.clearDevice();
        this.pltProfiles.clear();
        
        this.selTrcInd.removeActionListener(this.actNewIndex);
        this.selTrcInd.clearDisplay();
    }
    
    /**
     * Return the application's event
     * logger.
     *
     * @return  Event logger used by application
     * 
     * @since  May 26, 2010
     * @author Christopher K. Allen
     */
    private IEventLogger getLogger() {
        return MainApplication.getEventLogger();
    }

}
