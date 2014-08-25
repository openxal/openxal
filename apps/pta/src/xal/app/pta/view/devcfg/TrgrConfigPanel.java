/**
 * TriggeringConfigPanel.java
 *
 *  Created	: Jan 19, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.devcfg;

import xal.app.pta.MainApplication;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.PtaResourceManager;
import xal.app.pta.view.cmn.TriggerEventPanel;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.impl.WireScanner;
import xal.smf.impl.WireScanner.TrgConfig;
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
 * GUI panel for displaying and modifying the triggering
 * configuration parameters of a wire scanner device.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jan 19, 2010
 * @author Christopher K. Allen
 */
public class TrgrConfigPanel extends DeviceConfigBasePanel<WireScanner.TrgConfig> {

    /*
     * Global Constants
     */
    
    /**  Serialization version */
    static private final long serialVersionUID = 1L;

    
    /**  Title of GUI component */
    static public final String STR_TITLE = "Hardware Triggering Parameters";
    
    /** Government warning label part 1 */
    static public final String STR_WARNING = "\n\n  WARNING:\n" +
                                             "  Do not change these parameters unless  \n" +
                                             "  you really know what you are doing.  \n" + 
                                             "  Invalid settings cause hardware failure.  \n";

    
    /** The triggering event field descriptor */
    static public ScadaFieldDescriptor                  FD_TRG_EVT;
    
    /** ordered list of field descriptors that we manage */
    static public final List<ScadaFieldDescriptor>      LST_FLD_DESCRPS;

    /** 
     * Initialize the list of field descriptors.  We're going to handle the
     * "event" field by ourselves - we're not telling the base class about
     * it in <code>LST_FLD_DESCRPS</code>.
     * 
     *  @author Christopher K. Allen
     *  @since  Jan 19, 2010
     */
    static {

        LST_FLD_DESCRPS = new LinkedList<ScadaFieldDescriptor>();

        try {
            FD_TRG_EVT = ScadaFieldDescriptor.makeFieldDescriptor("event", WireScanner.TrgConfig.class);
            ScadaFieldDescriptor sfdDly = ScadaFieldDescriptor.makeFieldDescriptor("delay", WireScanner.TrgConfig.class);
//            ScadaFieldDescriptor sfdDur = ScadaFieldDescriptor.makeFieldDescriptor("duration", WireScanner.TrgConfig.class);

            LST_FLD_DESCRPS.add( sfdDly );
//            LST_FLD_DESCRPS.add( sfdDur );

        } catch (SecurityException e) {
            MainApplication.getEventLogger().logError(TrgrConfigPanel.class, 
                    "Unable to reflect fields 'delay/event' in " + 
                    WireScanner.TrgConfig.class + 
                    " AScada structure");

        }
    }
    
    
    
    
    /*
     * Local Attributes
     */
    
    /** Warning displayed concerning the altering of triggering parameters */
    private JTextArea           txtWarning;
    
    /** The GUI box containing the warning text and its icon */
    private Box                 boxWarning;
    
    
    /** The trigger event selector */
    private TriggerEventPanel   pnlTrgEvt;
    
    
    /*
     * Initialization
     */
    

    /**
     * Create a new <code>TriggeringConfigPanel</code> object.
     *
     * @since     Jan 19, 2010
     * @author    Christopher K. Allen
     */
    public TrgrConfigPanel() {
        super(WireScanner.TrgConfig.class);
        
        this.buildGuiComponents();
        this.buildGuiActions();
        this.layoutGuiComponents();
        
        this.initTrgEvtPanel();
    }

    
    
    /*
     * Abstract Method Implementations
     */
    
    /**
     * Returns the panel title.
     * 
     * @return   configuration panel title
     *
     * @since   Jan 19, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getTitle()
     */
    @Override
    public String getTitle() {
        return STR_TITLE;
    }

    /**
     * Returns the list of configuration parameter descriptors
     * managed by the base class.
     * 
     * @return  configuration parameter descriptors
     *
     * @since 	Jan 19, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getParamDescriptors()
     */
    @Override
    public List<ScadaFieldDescriptor> getParamDescriptors() {
        return LST_FLD_DESCRPS;
    }

    /**
     * Creates, and returns a new data structure of
     * configuration parameters populated from the current
     * configuration of the given device.
     * 
     * @param   smfDev  under configuration inquiry
     * 
     * @return  new configuration parameter set for given device
     *
     * @since   Jan 19, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getDeviceParameters(xal.smf.impl.WireScanner)
     */
    @Override
    public TrgConfig getDeviceParameters(ProfileDevice smfDev) throws ConnectionException, GetException {

        if ( !(smfDev instanceof WireScanner) ) 
            throw new IllegalArgumentException("Argument must be of type WireScanner, instead = " + smfDev.getClass());
        
        WireScanner             smfScan = (WireScanner)smfDev;
        
        return WireScanner.TrgConfig.acquire(smfScan);
    }

    
    /*
     * Base Class Overrides
     */
    
    /**
     * Adds the value of the trigger event 
     * to the configuration parameter data structure.
     * 
     * @return  set of timing configuration parameters
     *
     * @since   Jan 20, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getGuiFieldVals()
     */
    @Override
    protected TrgConfig retreiveParamValsFromGui() {
        ProfileDevice.TRGEVT  evt = this.pnlTrgEvt.getTriggerEvent();

        TrgConfig tmgCfg = super.retreiveParamValsFromGui();
        tmgCfg.event     = evt.getEventValue();
        
        return tmgCfg;
    }



    /**
     * Intercept the set of timing configuration parameters in order to
     * set the value on the trigger event GUI component.
     * 
     * @param   setVals         set of current device configuration parameters
     *
     * @since   Jan 20, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#setGuiFieldVals(xal.smf.impl.WireScanner.ParameterSet)
     */
    @Override
    protected void displayParameterVals(TrgConfig setVals) {
        int                  intTrgEvtVal = setVals.event;
        ProfileDevice.TRGEVT evt          = ProfileDevice.TRGEVT.getEventFromValue(intTrgEvtVal);
        
        this.pnlTrgEvt.setTriggerEventSilently(evt);
        super.displayParameterVals(setVals);
    }


    
    
    /*
     * Support Methods
     */
    
    /**
     * Creates and initializes the trigger event
     * selection panel (child class).
     *
     * 
     * @since  Jan 20, 2010
     * @author Christopher K. Allen
     */
    private void        initTrgEvtPanel() {
        
    }

    
    /**
     * Builds all the GUI components used on this panel.
     *
     * @author Christopher K. Allen
     * @since  Oct 28, 2011
     */
    private void    buildGuiComponents() {
        this.pnlTrgEvt = new TriggerEventPanel();
        
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

        // Response to user selecting a new trigger event
        //  Force a set device values
        ActionListener  lsnTrgEvtSel = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDeviceVals();
            }
        };
        this.pnlTrgEvt.registerSelectionListener(lsnTrgEvtSel);

        // Respond to the clear device (base class) event
        //  We clear the trigger event panel
        EventListener   lsnClear = new EventListener() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void eventAction(EVENT evt, ProfileDevice ws) {
                if (evt == EVENT.CLEARDEV)
                    pnlTrgEvt.clearTriggerEvent();
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
        super.insertComponentTop(this.pnlTrgEvt);
        super.insertComponentTop(this.boxWarning);
    }
    
}
