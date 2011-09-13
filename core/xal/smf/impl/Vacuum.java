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
  
    
    static {
        registerType();
    }
    
   /*
     *  Constants
     */
    
    public static final String      s_strType   = "vacuum";
    
    /*
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
      
 
    /*
     *  Local Attributes
     */


    /*
     *  User Interface
     */

    public Vacuum(String strId)     { 
        super(strId); 
        
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













