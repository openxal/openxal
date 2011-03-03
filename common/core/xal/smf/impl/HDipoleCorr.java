package xal.smf.impl;

import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;
import xal.ca.*;


/** 
 * The implementation of the Horizontal Dipole corrector element. This class 
 * extends the dipole class,  and is meant to hold dipole objects  
 * that are specifically correctors.
 * 
 * @author  J. Galambos (jdg@ornl.gov)
 */

public class HDipoleCorr extends Dipole {
  
    
    /*
     *  Constants
     */
    
    public static final String      s_strType   = "DCH";

  
    
    static {
        registerType();
    }

    
    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType( HDipoleCorr.class, s_strType );
        typeManager.registerType( HDipoleCorr.class, "horzcorr" );
        typeManager.registerType( HDipoleCorr.class, "hcorr" );
    }
 
    
    /*
     *  Local Attributes
     */
    
    
//    static  {
//        AcceleratorNodeFactory.registerClass(s_strType, HDipoleCorr.class);
//    };
    
    
    /** Override to provide type signature */
    public String getType()   { return s_strType; };

    
    /*
     *  User Interface
     */
        
    
    public HDipoleCorr(String strId)     { 
        super(strId); 
        
    };
    
    
    /**
     * Get the orientation of the magnet as defined by MagnetType.  The orientation
     * of all horizontal correctors is HORIZONTAL.
     * @return HORIZONTAL
     */
    public int getOrientation() {
        return HORIZONTAL;
    }
    
    
    /**
     * Determine whether this magnet is a corrector.
     * @return true since horizontal correctors are always correctors.
     */
    public boolean isCorrector() {
        return true;
    }
};

