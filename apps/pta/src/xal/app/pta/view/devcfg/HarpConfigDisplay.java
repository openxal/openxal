/**
 * DeviceConfigPanel.java
 *
 * @author Christopher K. Allen
 * @since  Oct 10, 2011
 *
 */

/**
 * DeviceConfigPanel.java
 *
 * @author  Christopher K. Allen
 * @since	Oct 10, 2011
 */
package xal.app.pta.view.devcfg;

import xal.app.pta.MainApplication;
import xal.app.pta.MainConfiguration;
import xal.app.pta.rscmgt.AppProperties;
import xal.smf.impl.WireHarp;
import xal.smf.impl.WireScanner;
import xal.smf.impl.profile.ParameterSet;
import xal.smf.scada.ScadaFieldDescriptor;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <p>
 * GUI panel that contains all the specialized device configuration panels.
 * This class aggregates all the panels that display configuration parameters
 * for the <code>{@link WireScanner}</code> device; that is, the parameter sets derived
 * from <code>{@link ParameterSet}</code>.
 * </p>
 * <p>
 * Currently the separate pane are displayed using a <code>{@linkplain JTabbedPane}</code>,
 * of which this class is a child.  In this manner we can save a lot of real estate
 * although at the cost of tabbing through all the panels.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Oct 10, 2011
 */
public class HarpConfigDisplay extends JTabbedPane  {

    
    
    /*
     * Global Constants 
     */
    
    /** Serialization version */
    private static final long serialVersionUID = 1L;

    /** Check channel connections to devices before proceeding */
    protected static final boolean BOL_DO_CONNTEST = AppProperties.DEVICE.EPICS_CA_CHK.getValue().asBoolean();
    
    /** Time out to use when checking connections to the device */
    protected static final double DBL_TMO_CONNTEST = AppProperties.DEVICE.TMO_CONNTEST.getValue().asDouble();

    
    
    /*
     * Internal Classes
     */
    
    
    /**
     * Enumeration of all the tabbed panes displayed in this panel.
     * The constants contain the title of the pane seen on the GUI and
     * the class type of the configuration panel to be displayed (of
     * derived type <code>{@link DeviceConfigBasePanel}</code>.
     *
     * @author Christopher K. Allen
     * @since   Oct 11, 2011
     */
    public enum PANE {
        
        /** Signal processing parameters panel */
        DAQ("Harp DAQ", HarpDaqCfgPanel.class),
        
        /** The scan configurations panel */
        HARP("Harp Config", HarpDevCfgPanel.class);
        
        
        /*
         * Operations
         */
        
        /**
         * Returns the title of the parameter configuration tabbed pane as specified
         * in the enumeration definition.
         *
         * @return  tabbed pane title
         *
         * @author Christopher K. Allen
         * @since  Oct 11, 2011
         */
        public String getPaneTitle() {
            return this.strTitle;
        }

        /**
         * Creates and returns an instance of the parameter configuration panel which this
         * enumeration constant represents.
         * 
         * @return  new instance of the corresponding parameter configuration panel,
         *          or null if an error occurred
         *
         * @author Christopher K. Allen
         * @since  Oct 11, 2011
         */
        public DeviceConfigBasePanel<? extends ParameterSet>    createPane() {
            
            try {
                DeviceConfigBasePanel<? extends ParameterSet> pnl = this.clsPnl.newInstance();
                
                return pnl;
                
            } catch (InstantiationException e) {
                MainApplication.getEventLogger().logException(this.getClass(), e, "Could not instantiate type " + this.clsPnl.getName());
                System.err.println("Serious Error: DeviceConfigPanel.PANE was not able to create panel " + this.clsPnl.getName());
                
            } catch (IllegalAccessException e) {
                MainApplication.getEventLogger().logException(this.getClass(), e, "Could not instantiate type " + this.clsPnl.getName());
                System.err.println("Serious Error: DeviceConfigPanel.PANE was not able to create panel " + this.clsPnl.getName());
                
            } catch (ExceptionInInitializerError e) {
                MainApplication.getEventLogger().logError(this.getClass(), "Could not instantiate type " + this.clsPnl.getName());
                System.err.println("Serious Error: DeviceConfigPanel.PANE was not able to create panel " + this.clsPnl.getName());
                
            } catch (SecurityException e) {
                MainApplication.getEventLogger().logException(this.getClass(), e, "Could not instantiate type " + this.clsPnl.getName());
                System.err.println("Serious Error: DeviceConfigPanel.PANE was not able to create panel " + this.clsPnl.getName());
                
            }
            
            return null;
        }
        
        
        /*
         * Initialization
         */
        
        /** The title of the panel */
        private final String      strTitle;
        
        /** The class type of the panel */
        private final Class<? extends DeviceConfigBasePanel<? extends ParameterSet>>  clsPnl;
        
        /** 
         * Create a new, initialized <code>PANE</code> enumeration constant
         *  
         * @param strTitle  title of the tab pane  
         * @param clsPnl    class type of the tab pane
         */
        private PANE(String strTitle, Class<? extends DeviceConfigBasePanel<? extends ParameterSet>> clsPnl) {
            this.strTitle = strTitle;
            this.clsPnl   = clsPnl;
        }
        
    }

    
    /**
     * This class is an action event that responds to the user changing tabs
     * on the tabbed pane (<code>JTabbedPane</code>) GUI.  When the new
     * configuration panel is brought into focus its displayed parameters
     * are refreshed from the current device.  We provide this response
     * in case the parameters were modified elsewhere while the configuration
     * panel was hidden.
     * 
     *
     * @author Christopher K. Allen
     * @since   Jan 27, 2011
     */
    private class PanelFocusEvent implements ChangeListener {

        /** This is the parameter configuration panel we are refreshing */
        private final DeviceConfigBasePanel<? extends ParameterSet> pnlCfg;
        
        /** This is the list of field descriptors for the channels needed by the panel */
        private final List<ScadaFieldDescriptor>                                lstFds;

        
        /**
         * Create a new <code>PanelFocusEvent</code> object which
         * refreshes the given configuration panel.
         * 
         * @param pnlCfg    device parameter configuration panel we are managing
         *
         * @author  Christopher K. Allen
         * @since   Jan 27, 2011
         */
        public PanelFocusEvent(DeviceConfigBasePanel<? extends ParameterSet>  pnlCfg) {
            this.pnlCfg = pnlCfg;
            this.lstFds = new LinkedList<ScadaFieldDescriptor>();
            
            this.lstFds.addAll( pnlCfg.getParamDescriptors() );
        }
        
        
        /**
         * Responds to the tab selection by refreshing
         * the parameters displayed on the panel.
         * 
         * @since Jan 27, 2011
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        @Override
        public void stateChanged(ChangeEvent arg0) {
            WireHarp smfHarp = HarpConfigDisplay.this.getCurrentDevice();
            
            if (smfHarp == null)
                return;
            
            if (!BOL_DO_CONNTEST) {
                this.pnlCfg.setDevice(smfHarp);
                
                return;
            }

            if (this.pnlCfg.connectionTest(smfHarp)) {
                this.pnlCfg.setDevice(smfHarp);
                
                return;
            }
            
            this.pnlCfg.clearDevice();
        }
        
    }
    
    
    /*
     * Local Attributes
     */
    
    /** the currently selected wire harp device */
    private WireHarp                    smfSelDev;
    
    
    /** List of all the parameter configuration panels */
    private final List<DeviceConfigBasePanel<? extends ParameterSet>>      lstCfgPnls;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>DeviceConfigPanel</code> object
     * initializes all the separate configuration parameter
     * panels.
     *
     * @param strTitle  the title of the panel displayed in the GUI 
     * @param enmAlign  the alignment of the tabs on the tabbed pane,
     *                  this one of the integer components of interface 
     *                  <code>{@link SwingConstants}</code>

     * @since     Oct 10, 2011
     * @author    Christopher K. Allen
     */
    public HarpConfigDisplay(String strTitle, int enmAlign) {
        super(enmAlign);
        this.setBorder( new TitledBorder(strTitle) );
        
        this.lstCfgPnls = new LinkedList<DeviceConfigBasePanel<? extends ParameterSet>>();
        
        this.buildGuiComponents();
        this.buildEventActions();
    }


    
    
    /*
     * Operations
     */
    
    /**
     * Returns the wire scanner device currently being displayed
     * in the GUI.
     *  
     * @return      current wire scanner device
     *
     * @author Christopher K. Allen
     * @since  Oct 11, 2011
     */
    public WireHarp  getCurrentDevice() {
        return this.smfSelDev;
    }
    /**
     *  Returns the configuration parameter set panel current in focus on the
     *  tabbed pane.
     *  
     * @return  the panel enjoying the current focus on the GUI screen
     *
     * @author Christopher K. Allen
     * @since  Oct 11, 2011
     */
    public DeviceConfigBasePanel<? extends ParameterSet> getPanelInFocus() {
        
        // Get the panel through the panel list and the tab index, so we don't have to
        //  cast anything
        int indTab = this.getSelectedIndex();
        
        DeviceConfigBasePanel<? extends ParameterSet> pnlFocus = this.lstCfgPnls.get(indTab);
        
        return pnlFocus;
    }
    
    /**
     * Responds to the new devices selected event from the device selector panel.
     * All the configuration panels are cleared, the device status panel is cleared,
     * the connection to the new device (which must be one) is tested, then all the
     * panels are loaded with the new device information from Channel Access. 
     *
     * @param smfHarp    wire scanner for which configuration parameters are to be displayed
     *  
     * @since   Oct 10, 2011
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.cmn.DeviceSelectorPanel.IDeviceSelectionListener#newDeviceSelection(java.util.List)
     */
    public void setDevice(WireHarp smfHarp) {

        // Clear out the previous device if it existed
        this.clearDevice();
        
        // Set the current device reference
        this.smfSelDev = smfHarp;
        
        // We only need to set the panel currently visible - the event
        //  responses will set the device when new tabs are selected
        DeviceConfigBasePanel<? extends ParameterSet> pnlFocus = this.getPanelInFocus();
        
        // Directly set the devices for the panels if there is no connection testing
        if (! BOL_DO_CONNTEST ) {
            pnlFocus.setDevice(smfHarp);
            
            return;
        }
        
        if ( pnlFocus.connectionTest(smfHarp) ) {
            pnlFocus.setDevice(smfHarp);
            
            return;
        }

        pnlFocus.clearDevice();
    }

    /**
     * Clears all the device configuration parameter
     * panels in the GUI display.
     *
     * 
     * @since  Jan 14, 2010
     * @author Christopher K. Allen
     */
    public void clearDevice() {
        
        this.smfSelDev = null;

        for (DeviceConfigBasePanel<? extends ParameterSet> pnl : this.lstCfgPnls)
            pnl.clearDevice();
    }
    
    /**
     *  Does a forced refresh of all the parameter display panels.
     *
     * @author Christopher K. Allen
     * @since  Jul 17, 2012
     */
    public void refreshDisplay() {
        
        for (DeviceConfigBasePanel<? extends ParameterSet> pnlParams : this.lstCfgPnls)
            pnlParams.refreshDisplay();
    }


  
    /*
     * Support Methods
     */
    
    /**
     * Initializes all the components of the
     * GUI display.
     * 
     * @since  Aug 19, 2009
     * @author Christopher K. Allen
     */
    private void buildGuiComponents(){
        
        // Create the device configuration panels
        for (PANE pane : PANE.values()) {
            String                                                      strTitle = pane.getPaneTitle();
            DeviceConfigBasePanel<? extends ParameterSet>   pnlPSet  = pane.createPane();

            MainConfiguration.getInstance().registerView(pnlPSet);
            this.lstCfgPnls.add(pnlPSet);
            this.addTab(strTitle, pnlPSet);
        }
    }
    
    /**
     * Add the event actions to the GUI.  Currently these are all
     * responses to the tab change event.  The parameters on the
     * panel coming into focus are refreshed using the 
     * <code>{@link PanelFocusEvent}</code> event handler.
     *
     * @author Christopher K. Allen
     * @since  Jan 27, 2011
     */
    private void buildEventActions() {

        for (DeviceConfigBasePanel<? extends ParameterSet> pnl : this.lstCfgPnls) {
            PanelFocusEvent     evtTabChg = new PanelFocusEvent(pnl);
            
            this.addChangeListener(evtTabChg);
        }
    }

}
