//
//  VerticalKicker.java
//  xal
//
//  Created by Tom Pelaia on 1/9/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.smf.impl;

import xal.ca.*;
import xal.smf.*;
import xal.smf.impl.qualify.*;


/** the extraction kicker represents a pulsed magnet for extracting the beam vertically from the ring */
public class ExtractionKicker extends Dipole {
	/** node type */
    public static final String s_strType   = "EKick";
	
    
	// static initializer
    static {
        registerType();
    }
	
	
	/** Constructor */
    public ExtractionKicker( final String strId )     { 
        super( strId );
    }
	
    
    /** Register type for qualification */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType( ExtractionKicker.class, s_strType );
        typeManager.registerType( ExtractionKicker.class, "kicker" );
        typeManager.registerType( ExtractionKicker.class, "vertkicker" );
        typeManager.registerType( ExtractionKicker.class, "extractionkicker" );
    }
	
	
    /** Override to provide type signature */
    public String getType()   { return s_strType; };
	
    
    /**
	 * Get the orientation of the magnet as defined by MagnetType.  The orientation of all vertical correctors is VERTICAL.
     * @return VERTICAL
     */
    public int getOrientation() {
        return VERTICAL;
    }
    
    
    /**
	 * Determine whether this magnet is a corrector.
     * @return false     
	 */
    public boolean isCorrector() {
        return false;
    }	
}
