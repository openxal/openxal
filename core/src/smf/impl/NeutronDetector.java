package xal.smf.impl;

import xal.ca.*;
import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;


/** 
 * The implementation of the Neutron Detector  class. 
 * ND's are a subclass of loss monitors that detect neutron losses. 
 * A seperate class is provided for these since they may need to
 * be grabbed separatly.
 * 
 * @author  J. Galambos (jdg@ornl.gov)
 */

public class NeutronDetector extends BLM {
      
    /*
     *  Constants
     */
    
    public static final String      s_strType   = "ND";
  

    static {
        registerType();
    }

    
    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(NeutronDetector.class, s_strType);
    }


    /*
     *  Local Attributes
     */
        
    
    
    /** Override to provide type signature */
    public String getType()   { return s_strType; };
  
  
  
    /*
     *  User Interface
     */
    
    
    public NeutronDetector(String strId)     { 
        super(strId); 
        
    };


}
