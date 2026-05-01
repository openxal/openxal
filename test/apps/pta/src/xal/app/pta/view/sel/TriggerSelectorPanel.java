/**
 * TriggerSelectorPanel.java
 *
 *  Created	: Apr 13, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.sel;

import xal.app.pta.MainApplication;
import xal.app.pta.rscmgt.DeviceProperties;
import xal.app.pta.tools.ca.SmfPvMonitor;
import xal.app.pta.tools.ca.SmfPvMonitorPool;
import xal.app.pta.tools.logging.IEventLogger;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.MonitorException;
import xal.ca.PutException;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireScanner;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.scada.ScadaFieldDescriptor;
import xal.smf.scada.XalPvDescriptor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.cosylab.events.SetEvent;
import com.cosylab.events.SetListener;
import com.cosylab.gui.components.DialKnob;
import com.cosylab.gui.components.LabelledWheelswitch;

/**
 * Second version of the (sub)panel used for selecting
 * the trigger timing for wire scanner devices. 
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Apr 13, 2010
 * @author Christopher K. Allen
 */
public class TriggerSelectorPanel extends JPanel {

    
    /*
     * Internal Classes
     */
    
    
    /**
     * GUI component providing the selection of the 
     * supported timing triggering event configuration
     * parameter.
     *
     *
     * @since  Jan 20, 2010
     * @author Christopher K. Allen
     */
    class TriggerEventSelector extends JPanel {

        /*
         * Global Constants
         */

        /**  Serialization version */
        private static final long serialVersionUID = 1L;

        /*
         * Local Attributes
         */

        /** The GUI display listing the various triggering events */
        private final JComboBox<ProfileDevice.TRGEVT>   cbxTrigEvt;

        /**
         * Create a new <code>TriggerEventSelector</code> object.
         *
         * @since     Jan 19, 2010
         * @author    Christopher K. Allen
         */
        public TriggerEventSelector() {
            ScadaFieldDescriptor fdEvt     = WireScanner.TrgConfig.FLD_MAP.get("event");
            String          strLabel  = DeviceProperties.getLabel(fdEvt);
            
//            System.out.println("JUST USED THE SCADA getFieldDescriptor() for TrgConfig in TriggerSelectorPane");

            JLabel lblTrgEvt = new JLabel(strLabel);
            this.cbxTrigEvt  = new JComboBox<ProfileDevice.TRGEVT>( ProfileDevice.TRGEVT.values() );
            this.cbxTrigEvt.setEditable(false);
            
            this.setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );
            this.add(lblTrgEvt);
            this.add(this.cbxTrigEvt);
        }

        /**
         * Registers an item selected event listener which should
         * respond by setting the appropriate triggering event
         * in the configuration parameters.
         *
         * @param lsnAction     item selected event action
         * 
         * @since  Jan 20, 2010
         * @author Christopher K. Allen
         */
        public void     registerSelectionListener(ActionListener lsnAction) {
            this.cbxTrigEvt.addActionListener(lsnAction);
        }


        /**
         * Return the user-selected trigger event from the 
         * GUI's combo box.
         * 
         * @return      desired timing trigger event, 
         *              or <code>null</code> if there was an initialization error
         *              with the combo box 
         *
         * @since  Jan 20, 2010
         * @author Christopher K. Allen
         */
        public ProfileDevice.TRGEVT        getTriggerEvent() {
            Object      objSel = this.cbxTrigEvt.getSelectedItem();

            if (objSel instanceof ProfileDevice.TRGEVT)
                return (ProfileDevice.TRGEVT)objSel;

            return null;
        }

        /**
         * Sets the timing trigger event seen in the selected
         * field of the combo box.  This method will not
         * fire an action event.
         *
         * @param evt   the desired triggering event
         * 
         * @since  Jan 20, 2010
         * @author Christopher K. Allen
         */
        public void     setTriggerEventSilently(ProfileDevice.TRGEVT evt) {
            ActionListener[]    arrLsns = this.cbxTrigEvt.getActionListeners();
            for (ActionListener lsn : arrLsns)
                this.cbxTrigEvt.removeActionListener(lsn);

            this.cbxTrigEvt.setSelectedItem(evt);

            for (ActionListener lsn : arrLsns)
                this.cbxTrigEvt.addActionListener(lsn);
        }

        /**
         * Clears the selection of the combo box
         * (i.e., blank).
         *
         * 
         * @since  Jan 21, 2010
         * @author Christopher K. Allen
         */
        public void     clearTriggerEvent() {
            this.cbxTrigEvt.setSelectedIndex(-1);
        }
        
        /**
         * Enable combo-box user interaction.
         * 
         *  @param      bolEnabled      enables user interaction if <code>true</code>
         *
         * @since 	Apr 21, 2010
         * @author  Christopher K. Allen
         *
         * @see javax.swing.JComponent#setEnabled(boolean)
         */
        @Override
        public void setEnabled(boolean bolEnabled) {
            this.cbxTrigEvt.setEnabled(bolEnabled);
        }
    }

    
    /**
     * Responds to a change in the timing triggering
     * event by updating the visible GUI components.
     * This action does not affect any device hardware.
     *
     * @since  Apr 22, 2010
     * @author Christopher K. Allen
     */
    class TriggerEventGuiAction implements ActionListener {

        
        /** The main GUI component */
        private final TriggerSelectorPanel       pnlTrg;
        
        
        /**
         * Create a new <code>TriggerEventGuiAction</code> object.
         *
         * @param pnlTrg        the main GUI panel
         *
         * @since     Apr 16, 2010
         * @author    Christopher K. Allen
         */
        public TriggerEventGuiAction(TriggerSelectorPanel pnlTrg) {
            this.pnlTrg = pnlTrg;
        }

        /**
         * Get the value of the new trigger event and pass it along to
         * the main GUI panel.
         *
         * @since 	Apr 21, 2010
         * @author  Christopher K. Allen
         *
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         * @see TriggerSelectorPanel#updateTriggerEvent(TRGEVT)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            @SuppressWarnings("unchecked")
            JComboBox<ProfileDevice.TRGEVT> cbxSrc = (JComboBox<ProfileDevice.TRGEVT>) e.getSource();
            ProfileDevice.TRGEVT            enmEvt = (ProfileDevice.TRGEVT) cbxSrc.getSelectedItem();
            
            if (enmEvt == null)
                return;
            
            this.pnlTrg.updateTriggerEvent(enmEvt);
        }
    }
    
    /**
     * Responds to a change in the trigger event value by
     * updating the referenced <code>TriggerEventSelector</code>
     * from hardware PV value.
     *
     * @since  Apr 15, 2010
     * @author Christopher K. Allen
     */
    class TriggerEventMonAction implements SmfPvMonitor.IAction {

        /** The trigger event display panel */
        private final TriggerEventSelector         pnlTrgEvt;


        /**
         * Create a new <code>TrgDelayMonAction</code> object.
         *
         * @param pnlTrgEvt   the trigger event editor panel 
         *
         * @since     Apr 14, 2010
         * @author    Christopher K. Allen
         */
        public TriggerEventMonAction(TriggerEventSelector pnlTrgEvt) {
            this.pnlTrgEvt = pnlTrgEvt;
        }

        /**
         *
         * @since       Apr 14, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
         */
        @Override
        public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {
            int                  iEvtVal = val.intValue();
            ProfileDevice.TRGEVT trgEvt  = ProfileDevice.TRGEVT.getEventFromValue(iEvtVal);

            this.pnlTrgEvt.setTriggerEventSilently(trgEvt);
        }
    }

    /**
     * Response to user action of setting values on 
     * Cosylab widgets.  This action makes sure that all
     * widgets display consistent values for the 
     * trigger delay paramter value.
     *
     * @since  Apr 15, 2010
     * @author Christopher K. Allen
     */
    class TriggerDelayGuiAction implements SetListener {
        
        
        /** The main GUI component */
        private final TriggerSelectorPanel       pnlTrg;
        
        
        /**
         * Create a new <code>TriggerDelayGuiAction</code> object.
         *
         * @param pnlTrg        the GUI panel we are updating
         *
         * @since     Apr 15, 2010
         * @author    Christopher K. Allen
         */
        public TriggerDelayGuiAction(TriggerSelectorPanel pnlTrg) {
            this.pnlTrg = pnlTrg;
        }
        
        
        /**
         * Sets the value of the trigger delay PV to the given
         * value contained in the event structure.
         * 
         * @param       evt     parameters of the set event
         *
         * @since 	Apr 15, 2010
         * @author  Christopher K. Allen
         *
         * @see com.cosylab.events.SetListener#setPerformed(com.cosylab.events.SetEvent)
         */
        @Override
        public void setPerformed(SetEvent evt) {
            double  dblVal = evt.getDoubleValue();

            this.pnlTrg.updateTriggerDelay(dblVal);
        }

    }
    
    /**
     * Responses to changes in the hardware trigger delay value
     * by updating the GUI controls displaying the value.
     * We take the new value from the PV monitor call.
     *
     * @since  Apr 15, 2010
     * @author Christopher K. Allen
     */
    class TrgDelayMonAction implements SmfPvMonitor.IAction {

        /** wheel switch also attached to the timing value */
        private final LabelledWheelswitch       whlValue;
        
        /** The dial knob also attached the timing value */
        private final DialKnob                  slrValue;
        
        /**
         * Create a new <code>TrgDelayMonAction</code> object.
         *
         * @param knb   the dial attached to the timing value
         * @param whl   the wheel switch attached to the timing value
         *
         * @since     Apr 14, 2010
         * @author    Christopher K. Allen
         */
        public TrgDelayMonAction(DialKnob knb, LabelledWheelswitch whl) {
            this.whlValue = whl;
            this.slrValue = knb;
        }
        
        /**
         * Update the two trigger delay controls
         *
         * @since 	Apr 14, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
         */
        @Override
        public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {
            double      dblVal = val.doubleValue();
            
            this.whlValue.setValue(dblVal);
            this.slrValue.setValue(dblVal);
        }
        
    }

    
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;
    
    
    
    
    
    /*
     * Local Attributes
     */
    
    
    //
    // Application
    
//    /** Application main user window */
//    private final MainWindow            winMain;
//    
    //
    // Timing parameter modification
    
    /** The current wire scanner device */
    private WireScanner                 smfDev;
    
    /** monitor pool for timing parameters */
    private final SmfPvMonitorPool      mplTrgParms;
    

    
    //
    // GUI Components
    
    /** The timing trigger delay time selector dial for course tuning */
    private DialKnob                    knbTrgDly;
    
    /** Numeric display of the trigger delay for fine tuning */
    private LabelledWheelswitch         whlTrgDly;

    /** Display selector (list box) for the triggering event */
    private TriggerEventSelector        pnlTrgEvt;
    
    
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>TriggerSelectorPanel</code> object.
     *
     * @since     Apr 13, 2010
     * @author    Christopher K. Allen
     */
    public TriggerSelectorPanel() {
//        this.winMain = winMain;
        this.mplTrgParms = new SmfPvMonitorPool();
        
        this.buildGuiComponents();
        this.layoutGui();
    }
    
    /**
     * We need to turn off any active monitors that
     * are still around at the demise of this class.
     *
     * @since 	Apr 15, 2010
     * @author  Christopher K. Allen
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() {
        this.mplTrgParms.emptyPool();
    }
    
    
    
    /*
     * Operations
     */
    
    /**
         * Sets the current device to monitor and adjust. 
         * 
         * @param   ws      newly selected device 
         *
         * @since   Apr 14, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.view.cmn.DeviceSelectorPanel.IDeviceSelectionListener#newDeviceSelection(java.util.List)
         */
        public void setDevice(WireScanner   ws) {
            if (ws == null) 
                return;
            
            this.smfDev = ws;
            this.buildMonitorPool(ws);
    
            try {
                this.mplTrgParms.begin();
                
            } catch (ConnectionException e) {
                getLogger().logException(getClass(), e, "Unable to start trigger delay PV monitor.");
    
            } catch (MonitorException e) {
                getLogger().logException(getClass(), e, "Unable to start trigger delay PV monitor.");
    
            } catch (NoSuchChannelException e) {
                getLogger().logException(getClass(), e, "Unable to start trigger delay PV monitor.");
    
            }

            this.pnlTrgEvt.setEnabled(true);
            this.whlTrgDly.setEditable(true);
            this.knbTrgDly.setEditable(true);
            this.knbTrgDly.synchronize();
    }

    /**
     * Clears out the device being modified.
     *
     * 
     * @since  Apr 16, 2010
     * @author Christopher K. Allen
     */
    public void clearDevice() {
        this.smfDev = null;
        
        this.mplTrgParms.emptyPool();
        
        this.knbTrgDly.setEditable(false);
        this.knbTrgDly.setValue(0);
        
        this.whlTrgDly.setEditable(false);
        this.whlTrgDly.setValue(0);
        
        this.pnlTrgEvt.clearTriggerEvent();
    }

    /**
     * Sets the trigger delay to the given value.  The value is written to 
     * hardware and to the GUI.
     *
     * @param dblTrgDly         the timing trigger delay (milli-seconds)
     * 
     * @since  Apr 15, 2010
     * @author Christopher K. Allen
     */
    public void updateTriggerDelay(double dblTrgDly) {
        this.whlTrgDly.setValue(dblTrgDly);
        this.knbTrgDly.setValue(dblTrgDly);
        this.knbTrgDly.synchronize();
        
        WireScanner.TrgConfig cfgTrg;
        try {
            cfgTrg = WireScanner.TrgConfig.acquire(this.smfDev);
            cfgTrg.delay = dblTrgDly;
            
            this.smfDev.configureHardware(cfgTrg);

        } catch (ConnectionException e) {
            getLogger().logException(getClass(), e, "Unable to connect to device " + this.smfDev.getId());
            
        } catch (GetException e) {
            getLogger().logException(getClass(), e, "Unable to read from device " + this.smfDev.getId());
            
        } catch (PutException e) {
            getLogger().logException(getClass(), e, "Unable to write to device " + this.smfDev.getId());
            
        }
    }
    
    /**
     * Sets the trigger event to the given value.  The value is written to 
     * hardware and to the GUI.
     *
     * @param enmEvt the timing trigger event 
     * 
     * @since  Apr 15, 2010
     * @author Christopher K. Allen
     */
    public void updateTriggerEvent(ProfileDevice.TRGEVT enmEvt) {
        this.pnlTrgEvt.setTriggerEventSilently(enmEvt);
        
        WireScanner.TrgConfig cfgTmg;
        try {
            cfgTmg = WireScanner.TrgConfig.acquire(this.smfDev);
            cfgTmg.event = enmEvt.getEventValue();
            
            this.smfDev.configureHardware(cfgTmg);

        } catch (ConnectionException e) {
            getLogger().logException(getClass(), e, "Unable to connect to device " + this.smfDev.getId());
            
        } catch (GetException e) {
            getLogger().logException(getClass(), e, "Unable to read from device " + this.smfDev.getId());
            
        } catch (PutException e) {
            getLogger().logException(getClass(), e, "Unable to write to device " + this.smfDev.getId());
            
        }
    }
    
    /**
     * Returns the event logger associated with the 
     * application.
     *
     * @return  application event logger
     * 
     * @since  Apr 15, 2010
     * @author Christopher K. Allen
     */
    public IEventLogger getLogger() {
        return MainApplication.getEventLogger();
    }

    /**
     * Construct all the visible GUI components.
     *
     * 
     * @since  Apr 15, 2010
     * @author Christopher K. Allen
     */
    private void buildGuiComponents() {
        String  strFmt = "%5.3d";
        String  strUnt = "msec";
        
        ScadaFieldDescriptor fdDelay = WireScanner.TrgConfig.FLD_MAP.get("delay");
        
        String  strLbl = DeviceProperties.getLabel(fdDelay);
        double  dblMin = DeviceProperties.getMinLimit(fdDelay).asDouble();
        double  dblMax = DeviceProperties.getMaxLimit(fdDelay).asDouble();
        
        SetListener     actUpdate = new TriggerDelayGuiAction(this);

        this.knbTrgDly = new DialKnob();
        this.knbTrgDly.setMinimum(dblMin);
        this.knbTrgDly.setMaximum(dblMax);
        this.knbTrgDly.setTitle(strLbl);
        this.knbTrgDly.setUnits(strUnt);
        this.knbTrgDly.setEditable(false);
        this.knbTrgDly.addSetListener(actUpdate);

        this.whlTrgDly = new LabelledWheelswitch();
        this.whlTrgDly.setLayoutOrientation(LabelledWheelswitch.VERTICAL_LAYOUT);
        this.whlTrgDly.setMinimum(dblMin);
        this.whlTrgDly.setMaximum(dblMax);
        this.whlTrgDly.setTitle(strLbl);
        this.whlTrgDly.setUnits(strUnt);
        this.whlTrgDly.setFormat(strFmt);
        this.whlTrgDly.setEditable(false);
        this.whlTrgDly.addSetListener(actUpdate);
        
        TriggerEventGuiAction         actTrgEvt = new TriggerEventGuiAction(this);
        
        this.pnlTrgEvt = new TriggerEventSelector();
        this.pnlTrgEvt.setEnabled(false);
        this.pnlTrgEvt.registerSelectionListener(actTrgEvt);
    }
    
    /**
     * Layout the GUI display.
     *
     * 
     * @since  Apr 16, 2010
     * @author Christopher K. Allen
     */
    private void layoutGui() {
        this.setLayout( new GridBagLayout() );

        GridBagConstraints      gbcLayout = new GridBagConstraints();
        gbcLayout.insets  = new Insets(0, 5, 0, 5);

        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        gbcLayout.fill  = GridBagConstraints.NONE;
        this.add(this.pnlTrgEvt, gbcLayout);
        
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        gbcLayout.fill  = GridBagConstraints.NONE;
        this.add( this.whlTrgDly, gbcLayout );

        gbcLayout.gridx = 1;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 2;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.fill  = GridBagConstraints.BOTH;
        this.add( this.knbTrgDly, gbcLayout );
    }

    
    /**
     * <p>
     * Builds the pool of monitors for all the PV values
     * we are to display on this panel.  One monitor per
     * PV.  
     * </p>
     * <p>
     * Fires up the monitor on the trigger delay process
     * variable for the given device.  The monitor distributes
     * the current value of the PV to all the GUI devices
     * displaying this value.
     * </p>
     * <p>
     * The Trigger event PV is easier, there is only one
     * GUI component involved 
     * (i.e., the <code>TriggerEventSelector</code> object).
     * </p>
     *
     * @param ws        the device we are going to monitor
     * 
     * @since  Apr 15, 2010
     * @author Christopher K. Allen
     */
    private void buildMonitorPool(WireScanner ws) {
        this.mplTrgParms.emptyPool();
        
//        XalPvDescriptor      pvdTrgDly = WireScanner.TrgConfig.PARAM.DELAY.getXalPvDescriptor();
        XalPvDescriptor         pvdTrgDly = WireScanner.TrgConfig.FLD_MAP.get("delay");
        SmfPvMonitor.IAction actTrgDly = new TrgDelayMonAction(this.knbTrgDly, this.whlTrgDly);
        SmfPvMonitor         monTrgDly = new SmfPvMonitor(ws, pvdTrgDly);
        monTrgDly.addAction(actTrgDly);
        this.mplTrgParms.addMonitor(monTrgDly);
        
//        XalPvDescriptor        pvdTrgEvt = WireScanner.TrgConfig.PARAM.TRGEVT.getXalPvDescriptor();
        XalPvDescriptor           pvdTrgEvt = WireScanner.TrgConfig.FLD_MAP.get("event");
        SmfPvMonitor.IAction   actTrgEvt = new TriggerEventMonAction(this.pnlTrgEvt);
        SmfPvMonitor           monTrgEvt = new SmfPvMonitor(ws, pvdTrgEvt);
        monTrgEvt.addAction(actTrgEvt);
        this.mplTrgParms.addMonitor(monTrgEvt);
    }
}
