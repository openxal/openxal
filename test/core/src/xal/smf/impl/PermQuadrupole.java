/*
 * PermQuadrupole.java
 *
 * Created on January 31, 2002, 9:48 AM
 */

package xal.smf.impl;

import xal.smf.*;
import xal.smf.impl.qualify.*;
import xal.ca.*;
import xal.tools.data.*;


/** 
 * PermQuadrupole implements an Permanent magnet Quadrupole. Unlike many other  
 * beam line elements, PermQuadrupole represents more than one official
 * type (PMQH and PMQV) as specified by the naming convention.  
 * In order to support this feature we override the getType(),
 * update() and isKindOf() methods.  The vertical and horizontal 
 * reference to a quadrapole isn't of consequence to behavior since the 
 * field of the quadrupole (including its sign) and its length characterizes 
 * the quadrupole.
 *
 * @author  tap
 */
public class PermQuadrupole extends PermanentMagnet {
    // Constants
    public static final String s_strType  = "PQ";
    public static final String HORIZONTAL_TYPE = "PMQH";
    public static final String VERTICAL_TYPE = "PMQV";

    // instance variables
    protected String type;


	// static initializer
    static {
        registerType();
    }

    
    /**
     * Register type for qualification.  These are the types that are common 
     * to all instances.  The <code>isKindOf</code> method handles the 
     * type qualification specific to an instance.
     * @see #isKindOf
     */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( PermQuadrupole.class, s_strType, "permquad", MagnetType.QUADRUPOLE );
    }


	/**
	 * PermQuadrupole constructor
	 */
	public PermQuadrupole( final String strId, final ChannelFactory channelFactory )     {
		super( strId, channelFactory );
	}


    /**
     * PermQuadrupole constructor
     */
    public PermQuadrupole(String strId)     {
        this( strId, null );
    }

    
    /** 
     * Override to provide the correct type signature per instance.  This is 
     * necessary since the PermQuadrupole class can represent more than one 
     * official type (PMQH or PMQV).
     * @return The official type consistent with the naming convention.
     */
    public String getType()   { 
        return type; 
    }
    
    
    /**
     * Update the instance with data from the data adaptor.  Overrides the 
     * default implementation to set the quadrupole type since a 
     * quadrupole type can be either "PMQH" or "PMQV".
     * @param adaptor The data provider.
     */
    public void update(DataAdaptor adaptor) {
        if ( adaptor.hasAttribute("type") ) {
            type = adaptor.stringValue("type");
        }
        super.update(adaptor);
    }
    
    
    /*
     * Determine whether this magnet is of the pole specified.
     * @param compPole The pole against which this magnet is being compared.
     * @return true if this magnet matches the specified pole.
     */ 
    public boolean isPole(String compPole) {
        return compPole.equals( MagnetType.QUADRUPOLE );
    }    
    
    
    /**
     * Get the orientation of the magnet as defined by MagnetType.  The orientation
     * of the quad is determined by its type: PMQH or PMQV
     * @return One of HORIZONTAL or VERTICAL
     */
    public int getOrientation() {
        return ( type.equalsIgnoreCase(HORIZONTAL_TYPE) ) ? HORIZONTAL : VERTICAL;
    }

    
    /** 
     * Determine if this node is of the specified type.  Override the default
     * method since a permanent quadrupole could represent either a vertical or 
     * horizontal type.  Must also handle inheritance checking so we must or the 
     * direct type comparison with the inherited type checking.
     * @param compType The type to compare against.
     * @return true if the node is a match and false otherwise.
     */
    public boolean isKindOf(String compType) {
        return compType.equalsIgnoreCase(this.type) || super.isKindOf(compType);
    }
}
