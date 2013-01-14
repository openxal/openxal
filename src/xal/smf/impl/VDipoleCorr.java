package xal.smf.impl;

import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;
import xal.ca.*;


/** 
 * The implementation of the Vertical Dipole corrector element. This class 
 * extends the dipole class,  and is meant to hold dipole objects  
 * that are specifically correctors.
 * 
 * @author  J. Galambos (jdg@ornl.gov)
 */

public class VDipoleCorr extends Dipole {
  
    
    /*
     *  Constants
     */
    
    public static final String      s_strType   = "DCV";

  
    
    static {
        registerType();
    }

    
    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(VDipoleCorr.class, s_strType);
        typeManager.registerType(VDipoleCorr.class, "vertcorr");
        typeManager.registerType(VDipoleCorr.class, "vcorr");
    }
 

     /*
     *  Local Attributes
     */
    
    
    /** Override to provide type signature */
    public String getType()   { return s_strType; };
  
  
//    static  {
//        AcceleratorNodeFactory.registerClass(s_strType, VDipoleCorr.class);
//    };
    
    
  
    /*
     *  User Interface
     */

    
    public VDipoleCorr(String strId)     { 
        super(strId); 
        
    };

    
    /**
     * Get the orientation of the magnet as defined by MagnetType.  The orientation
     * of all vertical correctors is VERTICAL.
     * @return VERTICAL
     */
    public int getOrientation() {
        return VERTICAL;
    }
    
    
    /**
     * Determine whether this magnet is a corrector.
     * @return true since vertical correctors are always correctors.
     */
    public boolean isCorrector() {
        return true;
    }
}

