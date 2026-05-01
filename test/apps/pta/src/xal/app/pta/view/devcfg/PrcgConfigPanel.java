/**
 * ProcessingConfigPanel.java
 *
 *  Created	: Jan 15, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.devcfg;

import xal.app.pta.MainApplication;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.DeviceProperties;
import xal.app.pta.rscmgt.PtaResourceManager;
import xal.app.pta.tools.swing.BndNumberTextField;
import xal.app.pta.tools.swing.NumberTextField.FMT;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireScanner;
import xal.smf.impl.WireScanner.PrcgConfig;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.scada.ScadaFieldDescriptor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.BevelBorder;

/**
 * Displays the on-board data processing
 * parameters.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jan 15, 2010
 * @author Christopher K. Allen
 */
public class PrcgConfigPanel extends DeviceConfigBasePanel<WireScanner.PrcgConfig> {

    
    /*
     * Global Constants
     */
    


    /**  Serialization version */
    private static final long serialVersionUID = 1L;


    
    /**  Title of GUI component */
    private static final String     STR_TITLE = "Data Analysis Parameters"; //$NON-NLS-1$
    
    /** 
     * Label for the beam fraction text input.  
     * This is where we can choose for the beam head or tail. 
     */
    private static final String STR_BM_FRAC_LBL = " Beam fraction [0,1] ";


    //
    // Resources used to special panel to our specific configuration parameters
    //
    
    /** The signal inversion field descriptor/enumeration - special attention due to boolean type */
    private static ScadaFieldDescriptor                 DSR_SIGINV; 
    
    /** ordered list of field descriptors that we manage */
    static private final List<ScadaFieldDescriptor>     LST_FLD_DESCRPS;

    
    
    /** Initialize the list of field descriptors */
    static {
        
        LST_FLD_DESCRPS = ScadaFieldDescriptor.makeFieldDescriptorList(WireScanner.PrcgConfig.class);
        
        for (ScadaFieldDescriptor fd : LST_FLD_DESCRPS) {
            if (fd.getFieldName().equalsIgnoreCase("sigInv")) { //$NON-NLS-1$
                DSR_SIGINV = fd;
            }
        }

        LST_FLD_DESCRPS.remove(DSR_SIGINV);
    }
    
    
    /*
     * Internal Classes
     */
    
    /**
     * Responds to the beam portion selection of the user.  The user is choosing a
     * part of the beam to be used the on-board data processing of the measurement
     * trace.  This action sets the analysis parameters on the wire scanner device
     * to match the user's choice.
     *
     * @author Christopher K. Allen
     * @since   Nov 16, 2011
     */
    private class SelectBeamPartAction implements ActionListener {

        /*
         * Local Attributes
         */
        
        /** The portion of the beam desired for analysis */
        private final WireScanner.MACROPULSE          enmBmSect;
        
        /*
         * Initialization
         */
        
        /**
         * Create a new <code>SelectBeamPartAction</code> that selects for the
         * given beam section.
         * 
         * @param enmBmSect     will set processing parameters to select for this part of the beam
         *
         * @author  Christopher K. Allen
         * @since   Nov 16, 2011
         */
        public SelectBeamPartAction(WireScanner.MACROPULSE enmBmSect) {
            this.enmBmSect = enmBmSect;
        }
        
        
        /**
         * Respond to the user's choice by setting the processing parameters on the wire
         * scanner such that the desired beam portion is selected for analysis.
         * 
         * @since Nov 16, 2011
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e) {
            ProfileDevice   smfDev      = PrcgConfigPanel.super.getCurrentProfileDevice();
            Number          numFrac = PrcgConfigPanel.this.txtBmFrac.getDisplayValue();
            
            if (smfDev == null)
                return;
            
            if ( !(smfDev instanceof WireScanner) )
                return;
            
            WireScanner smfScan = (WireScanner)smfDev;
            
            try {
                smfScan.analysisParametersSelect(this.enmBmSect, numFrac.doubleValue());
                
            } catch (RejectedExecutionException e1) {
                MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam head");
                JOptionPane.showMessageDialog(PrcgConfigPanel.this, e1.getMessage(), "WARNING", JOptionPane.WARNING_MESSAGE);
                
            } catch (ConnectionException e1) {
                MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam head");
                JOptionPane.showMessageDialog(PrcgConfigPanel.this, e1.getMessage(), "WARNING", JOptionPane.WARNING_MESSAGE);
                
            } catch (GetException e1) {
                MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam head");
                JOptionPane.showMessageDialog(PrcgConfigPanel.this, e1.getMessage(), "WARNING", JOptionPane.WARNING_MESSAGE);
                
            } catch (PutException e1) {
                MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam head");
                JOptionPane.showMessageDialog(PrcgConfigPanel.this, e1.getMessage(), "WARNING", JOptionPane.WARNING_MESSAGE);
                
            } catch (NoSuchChannelException e1) {
                MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam head");
                JOptionPane.showMessageDialog(PrcgConfigPanel.this, e1.getMessage(), "WARNING", JOptionPane.WARNING_MESSAGE);
                
            }
        }
    }

    
    /**
     * GUI component for displaying the signal inversion parameters.
     * This is essentially a boolean value so the GUI presents it
     * as a radio button.
     *
     * @since  Jan 15, 2010
     * @author Christopher K. Allen
     */
    class SignalInvertPanel extends JPanel {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;
        
        
        /*
         * Local Attributes
         */
        
        /** Label of the panel */
        private final String                  strLabel;
        
        /** Numeric value of false */
        private final Integer                 intValFalse;
        
        /** Numeric value of true */
        private final Integer                 intValTrue;
        
        
        /** The GUI display */
        private JRadioButton                    butSigInv;
        
        
        /*
         * Registered action listeners 
         */
        
        /** List of action listeners (for button pressed events) */
        private final List<ActionListener>      lstLsnSelect;
        
        
        
        /**
         * Create a new <code>SignalInvertPanel</code> object.
         *
         * @since     Jan 15, 2010
         * @author    Christopher K. Allen
         */
        @SuppressWarnings("synthetic-access")
        public SignalInvertPanel() {
            
            this.lstLsnSelect = new LinkedList<ActionListener>();
            
            
            this.strLabel    = DeviceProperties.getLabel(DSR_SIGINV);
            this.intValFalse = DeviceProperties.getMinLimit(DSR_SIGINV).asInteger();
            this.intValTrue  = DeviceProperties.getMaxLimit(DSR_SIGINV).asInteger();
            
            this.initButton();
        }
        
        /**
         * Returns the value used to represent <tt>FALSE</tt>.
         *
         * @return      the numeric value of <code>false</code>
         * 
         * @since  Jan 16, 2010
         * @author Christopher K. Allen
         */
        public Integer  getFalseValue() {
            return this.intValFalse;
        }
        
        /**
         * Returns the value used to represent <tt>TRUE</tt>.
         *
         * @return      the numeric value of <code>true</code>
         * 
         * @since  Jan 16, 2010
         * @author Christopher K. Allen
         */
        public Integer  getTrueValue() {
            return this.intValTrue;
        }

        /**
         * Register the given object to receive button selected
         * events.
         *
         * @param lsnSelect     object to receive event notifications
         * 
         * @since  Dec 23, 2009
         * @author Christopher K. Allen
         */
        public void registerSelectionListener(ActionListener lsnSelect) {
            this.lstLsnSelect.add(lsnSelect);
        }
        
        
        /**
         * Checks the button if the given value is equal to 
         * the numeric value for <code>false</code>, unchecks the
         * button if the given value is equal to the numeric value
         * for <code>true</code>, and does nothing otherwise.
         *
         * @param intVal        value to be evaluated as <code>true</code> or <code>false</code>
         * 
         * @since  Jan 15, 2010
         * @author Christopher K. Allen
         */
        public void     setDisplayValue(Integer intVal) {
            
            if (intVal == this.intValFalse) {
                this.butSigInv.setSelected(true);
            
            } else if (intVal == this.intValTrue) {
                this.butSigInv.setSelected(false);
                
            } else {
                // do nothing
                
            }
        }
        
        /**
         * Unchecks the button.
         *
         * 
         * @since  Jan 21, 2010
         * @author Christopher K. Allen
         */
        public void     clearDisplay() {
            this.butSigInv.setSelected(false);
        }
            
            
        /**
         * Return the numeric value corresponding to the state
         * of the button.  Specifically, we return the value
         * for <code>false</code> when unchecked and the value for
         * <code>true</code> when checked.
         *
         * @return      numeric value of the current button state  
         * 
         * @since  Jan 15, 2010
         * @author Christopher K. Allen
         */
        public Integer  getDisplayValue() {
            
            boolean     bolChk = this.butSigInv.isSelected();
            
            if (bolChk)
                return this.intValFalse;
            
            return this.intValTrue;
        }

        
        /*
         * Support Methods
         */
        
        
        /**
         * Initializes the signal inversion selection
         * button then adds it to the GUI.
         *
         * 
         * @since  Jan 15, 2010
         * @author Christopher K. Allen
         */
        private void    initButton() {
            
            ActionListener      lsnToggle = new ActionListener() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void actionPerformed(ActionEvent e) {
                    notifyListeners();
                }
            };
            
            this.butSigInv   = new JRadioButton(this.strLabel);
            this.butSigInv.addActionListener(lsnToggle);
            
            this.add(this.butSigInv);
        }
        
        /**
         * Sets the desired gain value from inside the GUI.
         * That is, this method is called by the button
         * actions.
         *
         * @param intGain       gain value [0,1,2]
         * 
         * @since  Dec 22, 2009
         * @author Christopher K. Allen
         */
        private void    notifyListeners() {
            
            ActionEvent evtAction = new ActionEvent(this, this.getDisplayValue(), "Toggle Event"); //$NON-NLS-1$
            for (ActionListener lsn : this.lstLsnSelect)
                lsn.actionPerformed(evtAction);
        }
    }

    
    /*
     * Local Attributes
     */
    
    /** The signal inversion selection panel */
    private SignalInvertPanel     pnlSigInv;
    
    
    /** Scan beam head button - sets triggering to pick off the beam head */
    private JButton             butHead;
    
    /** Scan beam tail button - sets triggering to pick off the beam tail */
    private JButton             butBody;
    
    /** Scan beam tail button - sets triggering to pick off the beam tail */
    private JButton             butTail;
    
    
    /** The text field containing the fraction of beam used in the analysis (use w/ buttons) */
    private BndNumberTextField  txtBmFrac;
    
    /** The GUI box containing the text field and label for the beam fraction */
    private Box                 boxBmFrac;
  

    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>ProcessingConfigPanel</code> object.
     *
     * @since     Jan 15, 2010
     * @author    Christopher K. Allen
     */
    public PrcgConfigPanel() {
        super(WireScanner.PrcgConfig.class);

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
     * @return  title of this parameter configuration panel
     *
     * @since   Jan 15, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getTitle()
     */
    @Override
    public String getTitle() {
        
        return STR_TITLE;
    }

    /**
     * Returns the set of field descriptors that the base class
     * will manage.  (We will manage the signal inversion parameter.)
     * 
     * @return  ordered list of analysis parameter descriptors (less the signal inversion)
     *
     * @since 	Jan 15, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getParamDescriptors()
     */
    @Override
    public List<ScadaFieldDescriptor> getParamDescriptors() {
        
        return LST_FLD_DESCRPS;
    }

    /**
     * Return the current analysis parameter set from the given device.
     * 
     * @param   smfDev      retrieve parameter set from this device
     * 
     * @return          analysis parameter set 
     *
     * @throws ConnectionException unable to connect parameter read back channel
     * @throws GetException        general field initialization exception 
     *
     * @since   Jan 15, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getDeviceParameters(xal.smf.impl.WireScanner)
     */
    @Override
    public PrcgConfig getDeviceParameters(ProfileDevice smfDev) throws ConnectionException, GetException {
        
        if ( !(smfDev instanceof WireScanner) ) 
            throw new IllegalArgumentException("Argument must be of type WireScanner, instead = " + smfDev.getClass());
        
        WireScanner             smfScan = (WireScanner)smfDev;
        return WireScanner.PrcgConfig.acquire(smfScan);
    }

    
    
    /*
     * Base Class Overrides
     */

    /**
     * Get the data structure of device configuration
     * parameters populated from the fields on the GUI
     * display.  Adds the value of the signal  inversion parameter.
     * 
     * @return  set of device configuration parameters
     *
     * @since 	Jan 15, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getGuiFieldVals()
     */
    @Override
    protected WireScanner.PrcgConfig retreiveParamValsFromGui() {
        WireScanner.PrcgConfig cfgAnl = super.retreiveParamValsFromGui();
        
        cfgAnl.sigInv = this.pnlSigInv.getDisplayValue();
        
        return cfgAnl;
    }

    /**
     * Displays the current device configurations parameters 
     * in the GUI.  Adds the value of the signal  inversion parameter.
     * 
     * @param setParamVals      the set of device configuration parameters
     * 
     * @since 	Jan 15, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#setGuiFieldVals(xal.smf.impl.WireScanner.ParameterSet)
     */
    @Override
    protected void displayParameterVals(WireScanner.PrcgConfig setParamVals) {
        Integer intSigInv = setParamVals.sigInv;
        
        this.pnlSigInv.setDisplayValue(intSigInv);
        super.displayParameterVals(setParamVals);
    }


    /*
     * Support Methods
     */

    /**
     * Creates the GUI components used on this panel.
     *
     * @author Christopher K. Allen
     * @since  Oct 31, 2011
     */
    private void buildGuiComponents() {

        // Create the signal invert panel
        this.pnlSigInv = new SignalInvertPanel();
        
        
        // Create the choose head, body, and tail buttons
        String      strPathIconHead = AppProperties.ICON.PRCG_BMHEAD.getValue().asString();
        ImageIcon   imgIconBeamHead = PtaResourceManager.getImageIcon(strPathIconHead);
        this.butHead = new JButton(" Select for Beam Head ", imgIconBeamHead);

        String      strPathIconBody = AppProperties.ICON.PRCG_BMBODY.getValue().asString();
        ImageIcon   imgIconBeamBody = PtaResourceManager.getImageIcon(strPathIconBody);
        this.butBody = new JButton(" Select for Beam Body ", imgIconBeamBody);

        
        String      strPathIconTail = AppProperties.ICON.PRCG_BMTAIL.getValue().asString();
        ImageIcon   imgIconBeamTail = PtaResourceManager.getImageIcon(strPathIconTail);
        this.butTail = new JButton(" Select for Beam Tail ", imgIconBeamTail);
        
        
        // Create the choose head/tail fraction input
        double      dblBmFrac = AppProperties.NUMERIC.PRCG_BMFRACT.getValue().asDouble();
        JLabel      lblBmFrac = new JLabel(STR_BM_FRAC_LBL);
        
        this.txtBmFrac = new BndNumberTextField(FMT.DEFAULT, 0.0, 1.0, dblBmFrac);
        this.boxBmFrac = Box.createHorizontalBox();
        this.boxBmFrac.add(this.txtBmFrac);
        this.boxBmFrac.add(Box.createHorizontalStrut(10));
        this.boxBmFrac.add(lblBmFrac);
       
    }
    
    /**
     * Creates and initializes the GUI event response actions.
     * 
     * @since  Oct 31, 2011
     * @author Christopher K. Allen
     */
    private void buildGuiActions() {

        // Set the update parameters event
        ActionListener actUpdate = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDeviceVals();
            }
        };
        this.pnlSigInv.registerSelectionListener(actUpdate);
        
        // Catch the clear device event and uncheck button
        EventListener   lsnClear = new EventListener() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void eventAction(EVENT evt, ProfileDevice ws) {
                if (evt == EVENT.CLEARDEV) {
                    pnlSigInv.clearDisplay();
                }
            }
        };
        this.registerEventListener(lsnClear);

        // Beam head selection button
//        ActionListener  actBmHead = new ActionListener() {
//
//            @Override
//            @SuppressWarnings("synthetic-access")
//            public void actionPerformed(ActionEvent e) {
//                WireScanner ws      = PrcgConfigPanel.super.getCurrentWireScanner();
//                Number      numFrac = PrcgConfigPanel.this.txtBmFrac.getDisplayValue();
//                
//                if (ws == null)
//                    return;
//                
//                try {
//                    ws.analysisSelectBeamHead(numFrac.doubleValue());
//                    
//                } catch (RejectedExecutionException e1) {
//                    MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam head");
//                    JOptionPane.showMessageDialog(PrcgConfigPanel.this, e1.getMessage(), "WARNING", JOptionPane.WARNING_MESSAGE);
//                    
//                } catch (ConnectionException e1) {
//                    MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam head");
//                    JOptionPane.showMessageDialog(PrcgConfigPanel.this, e1.getMessage(), "WARNING", JOptionPane.WARNING_MESSAGE);
//                    
//                } catch (GetException e1) {
//                    MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam head");
//                    JOptionPane.showMessageDialog(PrcgConfigPanel.this, e1.getMessage(), "WARNING", JOptionPane.WARNING_MESSAGE);
//                    
//                } catch (PutException e1) {
//                    MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam head");
//                    JOptionPane.showMessageDialog(PrcgConfigPanel.this, e1.getMessage(), "WARNING", JOptionPane.WARNING_MESSAGE);
//                    
//                } catch (NoSuchChannelException e1) {
//                    MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam head");
//                    JOptionPane.showMessageDialog(PrcgConfigPanel.this, e1.getMessage(), "WARNING", JOptionPane.WARNING_MESSAGE);
//                    
//                }
//            }
//        };
        SelectBeamPartAction    actBmHead = new SelectBeamPartAction(WireScanner.MACROPULSE.HEAD);
        this.butHead.addActionListener(actBmHead);
        
        // Beam body selection button
//        ActionListener  actBmBody = new ActionListener() {
//
//            @Override
//            @SuppressWarnings("synthetic-access")
//            public void actionPerformed(ActionEvent e) {
//                WireScanner ws      = PrcgConfigPanel.super.getCurrentWireScanner();
//                Number      numFrac = PrcgConfigPanel.this.txtBmFrac.getDisplayValue();
//                
//                if (ws == null)
//                    return;
//                
//                try {
//                    ws.analysisSelectBeamBody(numFrac.doubleValue());
//                    
//                } catch (ConnectionException e1) {
//                    MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam tail");
//                    
//                } catch (GetException e1) {
//                    MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam tail");
//                    
//                } catch (PutException e1) {
//                    MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam tail");
//                    
//                } catch (NoSuchChannelException e1) {
//                    MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam tail");
//                    
//                }
//            }
//        };
        SelectBeamPartAction    actBmBody = new SelectBeamPartAction(WireScanner.MACROPULSE.BODY);
        this.butBody.addActionListener(actBmBody);
        
        // Beam tail selection button
//        ActionListener  actBmTail = new ActionListener() {
//
//            @Override
//            @SuppressWarnings("synthetic-access")
//            public void actionPerformed(ActionEvent e) {
//                WireScanner ws      = PrcgConfigPanel.super.getCurrentWireScanner();
//                Number      numFrac = PrcgConfigPanel.this.txtBmFrac.getDisplayValue();
//                
//                if (ws == null)
//                    return;
//                
//                try {
//                    ws.analysisSelectBeamTail(numFrac.doubleValue());
//                    
//                } catch (ConnectionException e1) {
//                    MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam tail");
//                    
//                } catch (GetException e1) {
//                    MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam tail");
//                    
//                } catch (PutException e1) {
//                    MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam tail");
//                    
//                } catch (NoSuchChannelException e1) {
//                    MainApplication.getEventLogger().logException(PrcgConfigPanel.class, e1, "Unable to select beam tail");
//                    
//                }
//            }
//        };
        SelectBeamPartAction    actBmTail = new SelectBeamPartAction(WireScanner.MACROPULSE.TAIL);
        this.butTail.addActionListener(actBmTail);
        
        // Action for the beam fraction text box ?
        ActionListener  actCfgParams = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            }
            
        };
        
        this.txtBmFrac.addActionListener(actCfgParams);
    }
    
    /**
     * Arranges the individual GUI components on the
     * panel after they are made.
     *
     * @author Christopher K. Allen
     * @since  Oct 28, 2011
     */
    private void    layoutGuiComponents() {
        // Add the component to the top of the main panel
        super.insertComponentTop(this.pnlSigInv);
        
        // Add the beam head/body/tail selection buttons in separated box
        JPanel  pnlButts = new JPanel();
        pnlButts.setBorder( new BevelBorder(BevelBorder.LOWERED) );
        pnlButts.setLayout( new GridBagLayout() );
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        pnlButts.add(this.butHead, gbc);
        gbc.gridy++;

        pnlButts.add(this.butBody, gbc);
        gbc.gridy++;

        pnlButts.add(this.butTail, gbc);
        gbc.gridy++;

        pnlButts.add(this.boxBmFrac, gbc);
        
        super.insertComponentBottom(pnlButts);
    }
    
}
