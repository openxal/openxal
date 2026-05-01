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
	/** standard type for nodes of this class */
    public static final String      s_strType   = "DCV";
  

	// static initialization
    static {
        registerType();
    }

    
    /*
     * Register type for qualification
     */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( VDipoleCorr.class, s_strType, "vertcorr", "vcorr" );
    }

    
    /** Override to provide type signature */
    public String getType()   { return s_strType; }


	/** Constructor */
	public VDipoleCorr( final String strId, final ChannelFactory channelFactory )     {
		super( strId, channelFactory );

	}

	/** Constructor */
    public VDipoleCorr( final String strId )     {
        this( strId, null );
        
    }

    
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

