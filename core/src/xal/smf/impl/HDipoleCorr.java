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
	/** standard type for nodes of this class */
    public static final String s_strType   = "DCH";
  

	// static initialization
    static {
        registerType();
    }

    
    /**
     * Register type for qualification
     */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( HDipoleCorr.class, s_strType, "horzcorr", "hcorr" );
    }

    
    /** Override to provide type signature */
    public String getType()   { return s_strType; }


	/** Primary Constructor */
	public HDipoleCorr( final String strId, final ChannelFactory channelFactory )     {
		super( strId, channelFactory );
	}


	/** Constructor */
    public HDipoleCorr( final String strId )     {
        this( strId, null );
    }
    
    
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
}

