package xal.smf.impl;

import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;
import xal.ca.*;

/** 
 * The abstract Vacuum Class element. Different types of 
 * vacuum guages are derived from this class.
 * 
 * @author  J. Galambos
 * 
 */

public abstract class Vacuum extends AcceleratorNode  {
	// static initializer
    static {
        registerType();
    }
    

	/** Standard type for instances of this class */
    public static final String      s_strType   = "vacuum";


    /**
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(Vacuum.class, s_strType);
    }
  

    // vacuum channel handles
    /**
     * vacuum official pressure channel handle
     */
    public static final String PRESS_HANDLE = "P";
    private Channel pressC = null;


    /** Override to provide type signature */
    public String getType()   { return s_strType; };


	/** Primary Constructor */
	public Vacuum( final String strId, final ChannelFactory channelFactory )     {
		super( strId, channelFactory );
	}


	/** Constructor */
    public Vacuum( final String strId )     {
        this( strId, null );
    }

    
    public boolean isVacuum() {
        return true;
    }


    /**
     * returns pressure (Torr)    
     */
    public double   getPressure()  throws ConnectionException, GetException {
		pressC = lazilyGetAndConnect(PRESS_HANDLE, pressC);
		return pressC.getValDbl();
    }
}













