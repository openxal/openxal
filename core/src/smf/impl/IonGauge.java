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
  
    
    static {
        registerType();
    }
    
   /*
     *  Constants
     */
    
    public static final String      s_strType   = "IG";
    
    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(IonGauge.class, s_strType);
    }

    /** Override to provide type signature */
    public String getType()   { return s_strType; };
      
 
    public IonGauge(String strId)     { 
        super(strId);    
    }
}













