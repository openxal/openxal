/**
 * ActuatorConfigPanel.java
 *
 *  Created	: Jan 14, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.devcfg;

import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.impl.WireScanner;
import xal.smf.impl.WireScanner.ActrConfig;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.scada.ScadaFieldDescriptor;

import java.util.List;

/**
 * GUI panel for configuring the scan actuator parameters.
 *
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jan 14, 2010
 * @author Christopher K. Allen
 */
public class ActuatorConfigPanel extends DeviceConfigBasePanel<WireScanner.ActrConfig> {

    
    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;

    
    /**  Title of GUI component */
    private static final String STR_TITLE = "Actuator Parameters";

    /** ordered list of field descriptors that we manage */
    static private final List<ScadaFieldDescriptor>      LST_FLD_DESCRPS;

    /** Initialize the list of field descriptors */
    static {
        
        LST_FLD_DESCRPS = ScadaFieldDescriptor.makeFieldDescriptorList(WireScanner.ActrConfig.class);
        
//        LST_FLD_DESCRPS = new LinkedList<ScadaStruct.IFieldDescriptor>();
//        
//        for (WireScanner.ActrConfig.PARAM enmParam : WireScanner.ActrConfig.PARAM.values()) {
//            LST_FLD_DESCRPS.add(enmParam);
//        }
    }
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>ActuatorConfigPanel</code> object.  All the
     * action is done in the base class.
     *
     * @since     Jan 14, 2010
     * @author    Christopher K. Allen
     */
    public ActuatorConfigPanel() {
        super(WireScanner.ActrConfig.class);
    }

    

    /*
     * Abstract Method Implementations
     */
    
    /**
     * Returns the title of this GUI component
     * 
     * @return  panel border title
     *
     * @since 	Jan 14, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getTitle()
     */
    @Override
    public String getTitle() {
        return STR_TITLE;
    }

    /**
     * Returns the set of configuration parameter descriptors for
     * device actuators.  These are the parameters of interest for
     * this GUI component.
     * 
     * @return   set of actuator configuration parameter descriptors
     *
     * @since 	Jan 14, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getParamDescriptors()
     */
    @Override
    public List<ScadaFieldDescriptor> getParamDescriptors() {
        return LST_FLD_DESCRPS;
    }

    /**
     * Returns the actuator configuration parameter set for the given
     * hardware device in its current state.
     * 
     * @return  the device's actuator configuration parameters
     *
     * @since 	Jan 14, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getDeviceParameters(xal.smf.impl.WireScanner)
     */
    @Override
    public ActrConfig getDeviceParameters(ProfileDevice smfDev) 
        throws ConnectionException, GetException 
    {

        if ( !(smfDev instanceof WireScanner) ) 
            throw new IllegalArgumentException("Argument must be of type WireScanner, instead = " + smfDev.getClass());
        
        WireScanner             smfScan = (WireScanner)smfDev;
        WireScanner.ActrConfig  cfgActuator = WireScanner.ActrConfig.aquire(smfScan);
        
        return cfgActuator;
    }

}
