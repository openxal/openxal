package xal.smf.impl;

import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;
import xal.ca.*;

/** 
 * The IonGauge Class element. This class contains
 * the Ion Gauge implementation.
 * 
 * @author  J. Galambos
 * 
 */

public class IonGauge extends Vacuum  {
	/** standard type for nodes of this class */
	public static final String s_strType   = "IG";


    // static initialization
    static {
        registerType();
    }


    /*
     * Register type for qualification
     */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( IonGauge.class, s_strType );
    }


    /** Override to provide type signature */
    public String getType()   { return s_strType; };


	/** Constructor */
	public IonGauge( final String strId, final ChannelFactory channelFactory )     {
		super( strId, channelFactory );
	}


	/** Constructor */
    public IonGauge( final String strId )     {
        this( strId, null );
    }
}













