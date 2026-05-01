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
import xal.app.pta.view.cmn.TriggerEventPanel;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.impl.WireHarp;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.scada.ScadaFieldDescriptor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
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
 * @since   May 6, 2014
 * @author  Christopher K. Allen
 */
public class HarpDevCfgPanel extends DeviceConfigBasePanel<WireHarp.DevConfig> {

    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;


    
    /**  Title of GUI component */
    private static final String STR_TITLE = "Harp Configuration"; //$NON-NLS-1$

    
//    /** Government warning label */
//    static public final String STR_WARNING = "\n\n  WARNING:\n" +
//                                             "  Changing these parameters can      \n" +
//                                             "  create inconsistencies between     \n" + 
//                                             "  data acquistion and data analysis. \n";
//    /** Government warning label part 1 */
//    static public final String STR_WARNING = "\n\n  WARNING:\n" +
//                                             "  Do not change these parameters unless  \n" +
//                                             "  you really know what you are doing.  \n" + 
//                                             "  Invalid settings cause hardware failure.  \n";
    /** Government warning label part 1 */
    static public final String STR_WARNING = "\n\n  WARNING:\n" +
                                             "  Changing these parameters can cause death  \n" + 
                                             "  of the timing hardware systems. ";

    
    /** ordered list of field descriptors that we manage */
    static private final List<ScadaFieldDescriptor> LST_FLD_DESCRPS;
    
    /** Initialize the list of field descriptors */
    static {
        
        LST_FLD_DESCRPS = ScadaFieldDescriptor.makeFieldDescriptorList(WireHarp.DevConfig.class);
        
        List<ScadaFieldDescriptor>     lstFdsRm = new LinkedList<ScadaFieldDescriptor>();
        
        for (ScadaFieldDescriptor fd : LST_FLD_DESCRPS) {
            
            if (fd.getFieldName().equalsIgnoreCase("gainCmn")) //$NON-NLS-1$
                lstFdsRm.add(fd);
            if (fd.getFieldName().equalsIgnoreCase("trgEvent"))
                lstFdsRm.add(fd);
        }
        
        LST_FLD_DESCRPS.removeAll(lstFdsRm);
    }
    
    
    /*
     * Local Attributes
     */
    
    /** Warning displayed concerning the altering of sampling parameters */
    private JTextArea           txtWarning;
    
    /** The GUI box containing the warning text and its icon */
    private Box                 boxWarning;
    

    /** The gain selection GUI */
    private GainSelectorPanel       pnlGain;
    
    /** The trigger event selection GUI */
    private TriggerEventPanel       pnlTrgEvt;
    

    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>DaqConfigPanel</code> object.
     *
     * @since     Jan 16, 2010
     * @author    Christopher K. Allen
     */
    public HarpDevCfgPanel() {
        super(WireHarp.DevConfig.class);
        
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
    public WireHarp.DevConfig getDeviceParameters(ProfileDevice smfDev) throws ConnectionException, GetException {
        
        if ( !(smfDev instanceof WireHarp) )
            throw new IllegalArgumentException("Argument must be of type WireHarp, instead it is " + smfDev.getClass());
        
        WireHarp           smfHarp = (WireHarp)smfDev;
        WireHarp.DevConfig cfgDaq  = WireHarp.DevConfig.acquire(smfHarp);
        
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
    protected WireHarp.DevConfig retreiveParamValsFromGui() {
        WireHarp.DevConfig cfgDev = super.retreiveParamValsFromGui();
        
        cfgDev.trgEvent = this.pnlTrgEvt.getTriggerEvent().getEventValue();
        cfgDev.gainCmn  = this.pnlGain.getGain().getGainValue();
        
        return cfgDev;
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
    protected void displayParameterVals(WireHarp.DevConfig setVals) {

        int                 intTrgEvt = setVals.trgEvent;
        ProfileDevice.TRGEVT evt      = ProfileDevice.TRGEVT.getEventFromValue(intTrgEvt);
        this.pnlTrgEvt.setTriggerEventSilently(evt);
        
        int             intGain = setVals.gainCmn;
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
        
        String      strPathIconWarn = AppProperties.ICON.PRCG_WARNING.getValue().asString();
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
        
        this.pnlTrgEvt = new TriggerEventPanel();
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
                if (evt == EVENT.CLEARDEV) {
                    HarpDevCfgPanel.this.pnlGain.setGain(ProfileDevice.GAIN.UNKNOWN);
                    HarpDevCfgPanel.this.pnlTrgEvt.clearTriggerEvent();
                }
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
        super.insertComponentBottom(this.pnlTrgEvt);
    }
    

}
