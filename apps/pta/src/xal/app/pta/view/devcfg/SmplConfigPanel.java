/**
 * SmplConfigPanel.java
 *
 *  Created	: Jan 16, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.devcfg;

import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.PtaResourceManager;
import xal.app.pta.view.cmn.GainSelectorPanel;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.impl.WireScanner;
import xal.smf.impl.WireScanner.SmplConfig;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.scada.ScadaFieldDescriptor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextArea;

/**
 * GUI panel for configuring and displaying the data acquisition sampling parameters of 
 * a wire scanner device.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since   Jan 16, 2010
 * @version Nov 3, 2011
 * @author  Christopher K. Allen
 */
public class SmplConfigPanel extends DeviceConfigBasePanel<WireScanner.SmplConfig> {

    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;


    
    /**  Title of GUI component */
    private static final String STR_TITLE = "Sampling Parameters"; //$NON-NLS-1$

    
    /** Government warning label */
    static public final String STR_WARNING = "\n\n  WARNING:\n" +
                                             "  Changing these parameters can      \n" +
                                             "  create inconsistencies between     \n" + 
                                             "  data acquistion and data analysis. \n";

    
    /** ordered list of field descriptors that we manage */
    static private final List<ScadaFieldDescriptor> LST_FLD_DESCRPS;
    
    /** Initialize the list of field descriptors */
    static {
        
        LST_FLD_DESCRPS = ScadaFieldDescriptor.makeFieldDescriptorList(WireScanner.SmplConfig.class);
        
        for (ScadaFieldDescriptor fd : LST_FLD_DESCRPS) {
            if (fd.getFieldName().equalsIgnoreCase("signalGain")) //$NON-NLS-1$
                LST_FLD_DESCRPS.remove(fd);
        }
    }
    
//    /**
//     * Implements the user interface for selecting the DAQ
//     * gain circuit as a group of radio buttons.
//     *
//     * @since  Dec 23, 2009
//     * @author Christopher K. Allen
//     */
//    class GainSelectorPanel extends JPanel {
//        
//        /**
//         * Response to a radio button push.
//         *
//         * @since  Dec 23, 2009
//         * @author Christopher K. Allen
//         */
//        class SelectGainAction implements ActionListener {
//
//            /** The gain value */
//            private final int           intGainNew;
//            
//            /**
//             * Create a new <code>SelectGainAction</code> object 
//             * with the given gain value.
//             *
//             * @param intGain   the (constant) gain value for this action
//             *
//             * @since     Dec 22, 2009
//             * @author    Christopher K. Allen
//             */
//            public SelectGainAction(int intGain) {
//                this.intGainNew = intGain;
//            }
//            
//            /**
//             * Sets the gain.
//             *
//             * @since   Dec 22, 2009
//             * @author  Christopher K. Allen
//             *
//             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//             */
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                GainSelectorPanel.this.chooseDesiredGain(this.intGainNew);
//            }
//        }
//
//        
//        /*
//         * Global Constants
//         */
//        /**  Serialization version*/
//        private static final long serialVersionUID = 1L;
//
//
//        /*
//         * Parameter Values
//         */
//        
//        /** The desired data acquisition gain circuit */
//        private int     intGain;
//        
//        
//        /*
//         * GUI Components
//         */
//        
//        /** Low gain button */
//        private final JRadioButton    butLow;
//        
//        /** Med gain button */
//        private final JRadioButton    butMed;
//        
//        /** High gain button */
//        private final JRadioButton    butHigh;
//        
//        /** The gain button group */
//        private final ButtonGroup     grpGain;
//        
//        
//        /*
//         * Registered action listeners 
//         */
//        
//        /** List of action listeners (for button pressed events) */
//        private final List<ActionListener>      lstLsnSelect;
//        
//        
//        
//        /*
//         * Initialization
//         */
//        
//        
//        /**
//         * Create a new <code>GainSelectorPanel</code> object.
//         *
//         *
//         * @since     Dec 22, 2009
//         * @author    Christopher K. Allen
//         */
//        public GainSelectorPanel() {
//            this.lstLsnSelect = new LinkedList<ActionListener>();
//            
//            int intValLow = ProfileDevice.GAIN.LOW.getGainValue();
//            this.butLow = new JRadioButton("Low"); //$NON-NLS-1$
//            this.butLow.addActionListener( new SelectGainAction(intValLow) );
//            
//            int intValMed = ProfileDevice.GAIN.MED.getGainValue();
//            this.butMed = new JRadioButton("Med"); //$NON-NLS-1$
//            this.butMed.addActionListener( new SelectGainAction(intValMed) );
//            
//            int intValHigh = ProfileDevice.GAIN.HIGH.getGainValue();
//            this.butHigh = new JRadioButton("High"); //$NON-NLS-1$
//            this.butHigh.addActionListener( new SelectGainAction(intValHigh) );
//            
//            this.grpGain = new ButtonGroup();
//            this.grpGain.add(this.butLow);
//            this.grpGain.add(this.butMed);
//            this.grpGain.add(this.butHigh);
//            
//            this.add(this.butLow);
//            this.add(this.butMed);
//            this.add(this.butHigh);
//            this.add( new JLabel("    Signal Gain") ); //$NON-NLS-1$
//        }
//        
//        /**
//         * Register the given object to receive button selected
//         * events.
//         *
//         * @param lsnSelect     object to receive event notifications
//         * 
//         * @since  Dec 23, 2009
//         * @author Christopher K. Allen
//         */
//        public void registerSelectionListener(ActionListener lsnSelect) {
//            this.lstLsnSelect.add(lsnSelect);
//        }
//        
//        /**
//         * Sets the displayed gain value from outside the GUI.
//         *
//         * @param gain  gain circuit to display as selected.
//         * 
//         * @since  Dec 22, 2009
//         * @author Christopher K. Allen
//         */
//        public void setGain(ProfileDevice.GAIN gain) {
//            switch (gain) {
//            case LOW:
//                this.grpGain.setSelected(this.butLow.getModel(), true);
//                return;
//            case MED:
//                this.grpGain.setSelected(this.butLow.getModel(), true);
//                return;
//            case HIGH:
//                this.grpGain.setSelected(this.butHigh.getModel(), true);
//                return;
//            case UNKNOWN:
//                this.grpGain.clearSelection();
//                return;
//            }
//        }
//        
//        /**
//         * Return the current gain circuit selected by the user.
//         *
//         * @return      desired DAQ gain setting
//         * 
//         * @since  Dec 23, 2009
//         * @author Christopher K. Allen
//         */
//        public ProfileDevice.GAIN getGain() {
//            ProfileDevice.GAIN    gain = ProfileDevice.GAIN.getGainFromValue(this.intGain);
//            
//            return gain;
//        }
//        
//        /**
//         * Sets the desired gain value from inside the GUI.
//         * That is, this method is called by the button
//         * actions.
//         *
//         * @param intGain       gain value [0,1,2]
//         * 
//         * @since  Dec 22, 2009
//         * @author Christopher K. Allen
//         */
//        void   chooseDesiredGain(int intGain) {
//            this.intGain = intGain;
//            
//            ActionEvent evtAction = new ActionEvent(this, intGain, "Gain Selected"); //$NON-NLS-1$
//            for (ActionListener lsn : this.lstLsnSelect)
//                lsn.actionPerformed(evtAction);
//        }
//    }

    
    
    /*
     * Local Attributes
     */
    
    /** Warning displayed concerning the altering of sampling parameters */
    private JTextArea           txtWarning;
    
    /** The GUI box containing the warning text and its icon */
    private Box                 boxWarning;
    

    /** The gain selection GUI */
    private GainSelectorPanel       pnlGain;
    

    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>DaqConfigPanel</code> object.
     *
     * @since     Jan 16, 2010
     * @author    Christopher K. Allen
     */
    public SmplConfigPanel() {
        super(WireScanner.SmplConfig.class);
        
        this.buildGuiComponents();
        this.buildGuiActions();
        this.layoutGuiComponents();
    }

    
    /*
     * Abstract Method Implementation
     */
    
    /**
     * Returns the title of this GUI panel.
     * 
     * @return  title of this panel
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
     * Returns the set of descriptors for the device configuration parameters
     * to be managed by the base class.
     * 
     * @return  ordered list of descriptors for parameters that we do not manage
     *
     * @since 	Jan 16, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getParamDescriptors()
     */
    @Override
    public List<ScadaFieldDescriptor> getParamDescriptors() {
        return LST_FLD_DESCRPS;
    }

    /**
     * Creates, populates, and returns the data structure of
     * data acquisition configuration parameters for the given
     * device.
     * 
     * @param   smfDev from which configuration parameters are fetched
     * 
     * @return  the current configuration parameters for the given device
     *
     * @since   Jan 16, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getDeviceParameters(xal.smf.impl.WireScanner)
     */
    @Override
    public SmplConfig getDeviceParameters(ProfileDevice smfDev) throws ConnectionException, GetException {
        
        if ( !(smfDev instanceof WireScanner) ) 
            throw new IllegalArgumentException("Argument must be of type WireScanner, instead = " + smfDev.getClass());
        
        WireScanner             smfScan = (WireScanner)smfDev;
        WireScanner.SmplConfig  cfgDaq  = WireScanner.SmplConfig.acquire(smfScan);
        
        return cfgDaq;
    }
    
    
    /*
     * Base Class Overrides
     */

    

    
    /**
     * Adds the value of the signal gain configuration
     * parameter to the data structure of device
     * configuration parameters
     * 
     * @return  device configuration parameters including signal gain
     *
     * @since 	Jan 16, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getGuiFieldVals()
     */
    @Override
    protected SmplConfig retreiveParamValsFromGui() {
        WireScanner.SmplConfig cfgDaq = super.retreiveParamValsFromGui();
        
        cfgDaq.signalGain = this.pnlGain.getGain().getGainValue();
        
        return cfgDaq;
    }


    /**
     * Intercepts the <code>setGuiParameters()</code> call
     * to add the current value of the signal gain parameter.
     * 
     * @param   setVals set of configuration parameters sans the value of the signal gain
     *
     * @since 	Jan 16, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#setGuiFieldVals(xal.smf.impl.WireScanner.ParameterSet)
     */
    @Override
    protected void displayParameterVals(SmplConfig setVals) {
        
        int             intGain = setVals.signalGain;
        ProfileDevice.GAIN  gainVal = ProfileDevice.GAIN.getGainFromValue(intGain);
        
        this.pnlGain.setGain(gainVal);
        super.displayParameterVals(setVals);
    }


    
    /*
     * Support Methods
     */
    
    /**
     * Builds all the GUI components used on this panel.
     *
     * @author Christopher K. Allen
     * @since  Oct 28, 2011
     */
    private void    buildGuiComponents() {
        this.pnlGain = new GainSelectorPanel();
        
        String      strPathIconWarn = AppProperties.ICON.SMPL_WARNING.getValue().asString();
        ImageIcon   imgIconWarning  = PtaResourceManager.getImageIcon(strPathIconWarn);
        JLabel      lblIconWarning  = new JLabel(imgIconWarning);
        Color       clrTextWarning  = this.getBackground();
        
        this.txtWarning = new JTextArea(STR_WARNING);
        this.txtWarning.setBackground(clrTextWarning);
        this.txtWarning.setEditable(false);
        this.txtWarning.setFocusable(false);

        this.boxWarning = Box.createHorizontalBox();
        this.boxWarning.add(lblIconWarning);
        this.boxWarning.add(Box.createHorizontalStrut(10));
        this.boxWarning.add(this.txtWarning);
    }
    
    /**
     * Create the event responses to the user operation of the
     * GUI components.  We attach one response to each component,
     * plus a panel response to the <tt>CLEAR DEVICE</tt> event
     * which is used to clear the trigger event panel.
     *
     * @author Christopher K. Allen
     * @since  Oct 28, 2011
     */
    private void    buildGuiActions() {

        ActionListener lsnGainSel = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDeviceVals();
            }
        };
        this.pnlGain.registerSelectionListener(lsnGainSel);
        
        EventListener   lsnClear = new EventListener() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void eventAction(EVENT evt, ProfileDevice ws) {
                if (evt == EVENT.CLEARDEV)
                    SmplConfigPanel.this.pnlGain.setGain(ProfileDevice.GAIN.UNKNOWN);
            }
        };
        this.registerEventListener(lsnClear);
    }
    
    /**
     * Arranges the individual GUI components on the
     * panel after they are made.
     *
     * @author Christopher K. Allen
     * @since  Oct 28, 2011
     */
    private void    layoutGuiComponents() {
        super.insertComponentTop(this.pnlGain);
        super.insertComponentTop(this.boxWarning);
    }
    

}
