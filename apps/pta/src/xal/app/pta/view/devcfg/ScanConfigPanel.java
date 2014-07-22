/**
 * ScanConfigPanel.java
 *
 *  Created	: Jan 16, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.devcfg;

import xal.app.pta.rscmgt.DeviceProperties;
import xal.app.pta.tools.ca.SmfPvMonitor;
import xal.app.pta.tools.ca.SwingFieldMonitorActions.BooleanFieldUpdateAction;
import xal.app.pta.tools.ca.SwingFieldMonitorActions.NumberFieldUpdateAction;
import xal.app.pta.tools.swing.BndNumberTextField;
import xal.app.pta.tools.swing.BooleanIndicatorPanel;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.MonitorException;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireScanner;
import xal.smf.impl.WireScanner.ScanConfig;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.scada.XalPvDescriptor;
import xal.smf.scada.ScadaFieldDescriptor;
import xal.smf.scada.ScadaFieldList;
import xal.smf.scada.ScadaFieldMap;

import java.awt.Color;
import java.util.List;

/**
 * This panel displays and accepts user modifications to the
 * set of scanning parameter for the wire scanner.  These
 * parameters are located in the data structure
 * <code>{@link xal.smf.impl.WireScanner.ScanConfig}</code>.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jan 16, 2010
 * @author Christopher K. Allen
 */
public class ScanConfigPanel extends DeviceConfigBasePanel<WireScanner.ScanConfig> {

    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;

    
    
    /**  Title of GUI component */
    private static final String STR_TITLE = "Scan Parameters"; //$NON-NLS-1$
    
    
    
    /** Scan range error: normal display color */
    private static final Color  CLR_SCAN_RNG_OK = Color.GREEN;
    
    /** Scan range error: error condition color */
    private static final Color  CLR_SCAN_RNG_ERR = Color.RED;
    
    
    /*
     * Global Attributes
     */
    
    /** Ordered list of field descriptors that are auto-managed (by the base class) */
    private static final ScadaFieldList LST_FDS_AUTO = new ScadaFieldList(WireScanner.ScanConfig.class);

    /** Map of all the field descriptors in WireScanner.ScanConfig.  We need this to extract errScanRng */
    private static final ScadaFieldMap  MAP_FDS_ALL = new ScadaFieldMap(LST_FDS_AUTO);
    
    
    /** Array of names for all numeric fields that are read-only */
    private static final String[]       ARR_NUM_FLDS = { "lngScan", "lngStroke" };
    
    /** Array of names for all boolean (error) fields we are managing */
    private static final String[]       ARR_ERR_FLDS = { "errScanRng" };
    
    /** 
     * Initialization of the global attributes.  We need to remove from the list of 
     * field descriptors those that we are going to manage here.  
     */
    static {
        
        for (String strFldName : ARR_ERR_FLDS) {
        
            ScadaFieldDescriptor    fd = MAP_FDS_ALL.get(strFldName);
        
            if (fd != null)
                LST_FDS_AUTO.remove(fd);
            else
                throw new RuntimeException("Could not field the field errScanRng in order to exclude it");
        }
    }

    
    /*
     * Internal Classes
     */
    
//    /**
//     * A PV monitor action which updates error indicator panels.
//     *
//     * @since  Jan 25, 2010
//     * @author Christopher K. Allen
//     */
//    public class BooleanFieldUpdateAction implements SmfPvMonitor.IAction {
//
//        
//        /*
//         * Local Attributes
//         */
//        
//        /** The error panel that we update */
//        private final BooleanIndicatorPanel       pnlBool;
//        
//        /**
//         * Create a new <code>ErrorFieldMonitor</code> object.
//         *
//         * @param pnlError      error indicator panel that we are updating
//         *
//         * @since     Jan 25, 2010
//         * @author    Christopher K. Allen
//         */
//        public BooleanFieldUpdateAction(BooleanIndicatorPanel    pnlError) {
//            this.pnlBool = pnlError;
//        }
//
//        /**
//         *
//         * @since   Jan 25, 2010
//         * @author  Christopher K. Allen
//         *
//         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
//         */
//        @Override
//        public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {
//            Integer     intVal = val.intValue();
//            
//            this.pnlBool.setDisplayValue(intVal);
//        }
//    }
//    
//    /**
//     * A PV monitor action which updates error indicator panels.
//     *
//     * @since  Jan 25, 2010
//     * @author Christopher K. Allen
//     */
//    public class NumberFieldUpdateAction implements SmfPvMonitor.IAction {
//
//        
//        /*
//         * Local Attributes
//         */
//        
//        /** The error panel that we update */
//        private final NumberTextField       txtNumber;
//        
//        /**
//         * Create a new <code>ErrorFieldMonitor</code> object.
//         *
//         * @param txtNumber      error indicator panel that we are updating
//         *
//         * @since     Jan 25, 2010
//         * @author    Christopher K. Allen
//         */
//        public NumberFieldUpdateAction(NumberTextField  txtNumber) {
//            this.txtNumber = txtNumber;
//        }
//
//        /**
//         *
//         * @since   Jan 25, 2010
//         * @author  Christopher K. Allen
//         *
//         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
//         */
//        @Override
//        public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {
//            Integer     intVal = val.intValue();
//            
//            this.txtNumber.setDisplayValueSilently(intVal);
//        }
//    }
//    
    /**
     * <p>
     * An event response action for the <tt>set device/clear device</tt> events generated by
     * the base class <code>{@link DeviceConfigBasePanel}</code>.  The action involves the
     * monitoring of an error PV, that is, a PV with only two values {0,1}.  The values
     * of the PV are used to update a <code>{@link BooleanIndicatorPanel}</code> object
     * via the monitor.  
     * </p>
     * When a <tt>set device</tt> event is captured, this action creates a 
     * PV monitor for the given PV descriptor, any previous monitors are stopped
     * and destroyed. The PV monitor continually updates the error (status) panel.  If a 
     * <tt>clear device</tt> event is captured the current monitor is shutdown and destroyed.
     * </p>
     *
     * @author Christopher K. Allen
     * @since   Oct 13, 2011
     */
    public class MonitorFieldEvent implements DeviceConfigBasePanel.EventListener {

        /*
         * Local Attributes
         */
        
        /** The descriptor of the PV to be monitored */
        private final   XalPvDescriptor            pvdErrStat;
        
        /** The monitor for the error PV */
        private         SmfPvMonitor            monErrStat;
        
        /** The action for the monitor */
        private         SmfPvMonitor.IAction    actMonitor;
        
        /*
         * Initialization
         */
        
        /**
         * Create a new <code>ErrorPvMonitorEvent</code> object.
         *
         * @param sfdErrStat      descriptor for the error PV we are monitoring and displaying on the panel
         * @param actMonitor      the action used by the monitor to be created on SETDEV event
         *
         * @author  Christopher K. Allen
         * @since   Oct 13, 2011
         */
        public MonitorFieldEvent(ScadaFieldDescriptor sfdErrStat, SmfPvMonitor.IAction actMonitor) {
            this.pvdErrStat = sfdErrStat;
            this.actMonitor = actMonitor;
            
            this.monErrStat = null;
        }
        
        /*
         * EventListener Interface
         */
        
        /**
         * In the case of a <code>EVENT.SETDEV</code> event, any previous monitor is 
         * shutdown (and destroyed - <code>null</code>ed), and a new one is created and started
         * for the given wire scanner device.  If the event is of type <code>EVENT.CLEARDEV</code>
         * the current PV monitor is cleared and destroyed.
         * 
         * @since Oct 13, 2011
         * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel.EventListener#eventAction(xal.app.pta.view.devcfg.DeviceConfigBasePanel.EventListener.EVENT, xal.smf.impl.WireScanner)
         */
        @Override
        public void eventAction(EVENT evt, ProfileDevice ws) {
            
            // Stop any currently running monitors
            if (this.monErrStat != null)
                this.monErrStat.clear();

            // If it's a set device event then we start the monitor on the new device
            if (evt == EVENT.SETDEV) {
                if ( !(ws instanceof WireScanner) )
                    throw new IllegalArgumentException("Argument must be a WireScanner instead of " + ws.getClass());
                
                this.createMonitor((WireScanner)ws);
            }
        }
        
        /**
         * Creates the PV monitor for the given wire scanner device.  Once created
         * (for the defined <code>XalPvDescriptor</code>) the monitor is started.
         * @param ws
         *
         * @author Christopher K. Allen
         * @since  Oct 13, 2011
         */
        private void createMonitor(WireScanner ws) {

            // Create a new monitor for the given
            this.monErrStat = new SmfPvMonitor(ws, this.pvdErrStat);
            this.monErrStat.addAction(this.actMonitor);

            try {
                this.monErrStat.begin();

            } catch (ConnectionException e) {
                appLogger().logException(getClass(), e, "unable to start monitor for " + ws.getId());
                
            } catch (MonitorException e) {
                appLogger().logException(getClass(), e, "unable to start monitor for " + ws.getId());
                
            } catch (NoSuchChannelException e) {
                appLogger().logException(getClass(), e, "unable to start monitor for " + ws.getId());
                
            }
        }
    };

    
    /*
     * Local Attributes
     */
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>ScanConfigPanel</code> object.
     *
     * @since     Jan 16, 2010
     * @author    Christopher K. Allen
     */
    public ScanConfigPanel() {
        super(WireScanner.ScanConfig.class);
        
        this.initErrStatusComponents();
        this.initNumReadbackComponents();
    }

    
    
    /*
     * Abstract Method Implementations
     */
    
    /**
     * Returns the title of this GUI panel.
     * 
     * @return  the panel title
     *
     * @since   Jan 16, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getTitle()
     */
    @Override
    public String getTitle() {

        return STR_TITLE;
    }

    /**
     * Returns the ordered list of descriptors for the device configuration
     * parameters under management.
     * 
     * @return  order list of configuration parameter descriptors
     *
     * @since 	Jan 16, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getParamDescriptors()
     */
    @Override
    public List<ScadaFieldDescriptor> getParamDescriptors() {
        
        return LST_FDS_AUTO;
    }


    /**
     * Returns a populated data structure of device configuration
     * parameters obtained from the given device.
     * 
     * @return  set of current configuration parameters for the given device
     *
     * @since   Jan 16, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getDeviceParameters(xal.smf.impl.WireScanner)
     */
    @Override
    public ScanConfig getDeviceParameters(ProfileDevice smfDev) throws ConnectionException, GetException {
        
        if ( !(smfDev instanceof WireScanner) ) 
            throw new IllegalArgumentException("Argument must be of type WireScanner, instead = " + smfDev.getClass());
        
        WireScanner            smfScan = (WireScanner)smfDev;
        WireScanner.ScanConfig cfgScan = WireScanner.ScanConfig.acquire(smfScan);
        
        return cfgScan;
    }




    
    
    /*
     * Base Class Overrides
     */

//    /**
//     * Sets the final position field in this GUI panel.
//     *
//     * @since 	Jan 16, 2010
//     * @author  Christopher K. Allen
//     *
//     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#setGuiFieldVals(xal.smf.impl.WireScanner.ParameterSet)
//     */
//    @Override
//    protected void setGuiFieldVals(ScanConfig setVals) {
//        
////        Double  dblScanLng = setVals.lngScan;
////        this.pnlScanLng.setDisplayValueSilently(dblScanLng);
//        
//        Double  dblFnlPos = setVals.compFinalPosition();
//        this.pnlScanLng.setDisplayValueSilently(dblFnlPos);
//        
//        super.setGuiFieldVals(setVals);
//    }



//    /**
//     * Catch the update device parameters event and the set of configuration
//     * parameters so that we can set the final position field which we are
//     * self managing.
//     * 
//     * @return  the <code>ScanConfig</code> parameter set sent to the device
//     *
//     * @since 	Jan 16, 2010
//     * @author  Christopher K. Allen
//     *
//     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#setDeviceVals()
//     */
//    @Override
//    protected ScanConfig setDeviceVals() {
//        WireScanner.ScanConfig cfgScan = super.setDeviceVals();
//        
////        Double dblScanLng = cfgScan.lngScan;
////        this.pnlScanLng.setDisplayValue(dblScanLng);
//        
//        Double dblFnlPos = cfgScan.compFinalPosition();
//        this.pnlScanLng.setDisplayValue(dblFnlPos);
//
//        return cfgScan;
//    }
    
    

    /*
     * Support Methods
     */
    
    /**
     * Creates and initializes GUI components that display error
     * conditions.  We need to manage process variables of this
     * type because the base class will not.
     * 
     * @since  Jan 16, 2010
     * @author Christopher K. Allen
     */
    private void initErrStatusComponents() {


        // Initialize and insert the Scan range error indicator
        //  We setup the channel monitor here, which is instantiated whenever
        //  a new device is selected, and destroyed when it is cleared.
        //        this.monScanRngErr = null;

        for (String strFldName : ARR_ERR_FLDS) {
            ScadaFieldDescriptor     fdScanRng = MAP_FDS_ALL.get(strFldName);

            String  strLblErr = DeviceProperties.getLabel(fdScanRng);
            Integer intValOk  = DeviceProperties.getNormalValue(fdScanRng);
            Integer intValErr = DeviceProperties.getErrorValue(fdScanRng);

            BooleanIndicatorPanel pnlErrStat = new BooleanIndicatorPanel(strLblErr, intValErr, intValOk);
            pnlErrStat.setOffColor(CLR_SCAN_RNG_OK);
            pnlErrStat.setOnColor(CLR_SCAN_RNG_ERR);

            BooleanFieldUpdateAction    actUpdate = new BooleanFieldUpdateAction(pnlErrStat);
            MonitorFieldEvent evtSetDev = new MonitorFieldEvent(fdScanRng, actUpdate);

            super.registerEventListener(evtSetDev);
            super.insertComponentTop(pnlErrStat);
        }
    }
    
    /**
     * Modifies the attributes of the text fields for read-only
     * fields.  We make them un-editable and change the color.
     *
     * @author Christopher K. Allen
     * @since  Oct 14, 2011
     */
    private void initNumReadbackComponents() {
        
        for (String strFldName : ARR_NUM_FLDS) {
            // Turn off the edit function for the scan length - it is read only
            ScadaFieldDescriptor    fdNumFld  = MAP_FDS_ALL.get(strFldName);
            BndNumberTextField      txtNumFld = super.getGuiNumberFieldFrom(fdNumFld);

            txtNumFld.setEditable(false);
            txtNumFld.setForeground(Color.GRAY);
            
            NumberFieldUpdateAction actUpdate = new NumberFieldUpdateAction(txtNumFld);
            MonitorFieldEvent       lsnUpdate = new MonitorFieldEvent(fdNumFld, actUpdate);
            
            this.registerEventListener(lsnUpdate);
        }

    }
    
}
