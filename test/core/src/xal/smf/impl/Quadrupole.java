/*
 * Quadrupole.java
 *
 */
package xal.smf.impl;

import xal.smf.*;
import xal.smf.impl.qualify.*;
import xal.ca.*;
import xal.tools.data.*;


/** 
 * Quadrupole implements an Electromagnet Quadrupole supplied by a single power 
 * supply.  Unlike many other beam line elements, Quadrupole represents more 
 * than one official type (QH and QV) as specified by the naming convention.  
 * In order to support this feature we override the getType(),
 * update() and isKindOf() methods.  The vertical and horizontal 
 * reference to a quadrapole isn't of consequence to behavior since the 
 * field of the quadrupole (including its sign) and its length characterizes 
 * the quadrupole.
 * 
 * @author  Nikolay Malitsky, Christopher K. Allen
 * @author Tom Pelaia
 */

public class Quadrupole extends Electromagnet {
	public static final String s_strType   = "Q";

	/** horizontal quadrupole type */
	public static final String HORIZONTAL_TYPE = "QH";

	/** vertical quadrupole type */
	public static final String VERTICAL_TYPE = "QV";

	/**
	 * skew quadrupole type
	 */
	public static final String SKEW_TYPE = "QSC";

	/** the type of quadrupole (horizontal or vertical) */
	protected String _type;


	// static initializer
	static {
		registerType();
	}


	/**
	 * Register type for qualification.  These are the types that are common to all instances.
	 * The <code>isKindOf</code> method handles the type qualification specific to an instance.
	 * @see #isKindOf
	 */
	private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( Quadrupole.class, s_strType, "emquad", "quad", "quadrupole", MagnetType.QUADRUPOLE );
	}


	/**
	 * Primary Constructor
	 * @param strID the unique node identifier
	 */
	public Quadrupole( final String strID, final ChannelFactory channelFactory ) {
		super( strID, channelFactory );
	}


	/**
	 * Constructor
	 * @param strID the unique node identifier
	 */
	public Quadrupole( final String strID ) {
		this( strID, null );
	}


    /**
     * Override to provide the correct type signature per instance.  This is
     * necessary since the Quadrupole class can represent more than one 
     * official type (QH or QV).
     * @return The official type consistent with the naming convention.
     */
    public String getType()   { 
        return _type; 
    }
    
    
    /**
     * Update the instance with data from the data adaptor.  Overrides the default implementation to 
	 * set the quadrupole type since a quadrupole type can be either "QH" or "QV".
     * @param adaptor The data provider.
     */
    public void update( final DataAdaptor adaptor ) {
        if ( adaptor.hasAttribute( "type" ) ) {
            _type = adaptor.stringValue( "type" );
        }
        super.update( adaptor );
    }
    
    
    /**
     * Determine whether this magnet is of the pole specified.
     * @param compPole The pole against which this magnet is being compared.
     * @return true if this magnet matches the specified pole.
     */ 
    public boolean isPole( final String compPole ) {
        return compPole.equals( MagnetType.QUADRUPOLE );
    }
    
    
    /**
     * Get the orientation of the magnet as defined by MagnetType.  The orientation
     * of the quad is determined by its type: QH or QV
     * @return One of HORIZONTAL or VERTICAL
     */
    public int getOrientation() {
    	if (_type.equalsIgnoreCase(SKEW_TYPE))
    		return NO_ORIENTATION;
    	else
    		return _type.equalsIgnoreCase( HORIZONTAL_TYPE ) ? HORIZONTAL : VERTICAL;
    }
        
    
    /** 
     * Determine if this node is of the specified type.  Override the default method since a quadrupole 
	 * could represent either a vertical or horizontal type.  Must also handle inheritance checking so 
	 * we must or the direct type comparison with the inherited type checking.
     * @param type The type against which to compare this quadrupole's type.
     * @return true if the node is a match and false otherwise.
     */
    public boolean isKindOf( final String type ) {
        return type.equalsIgnoreCase( _type ) || super.isKindOf( type );
    }
}
