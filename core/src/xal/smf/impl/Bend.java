/*
 * Bend.java
 *
 * Created on February 1, 2002, 1:25 PM
 */

package xal.smf.impl;

import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;
import xal.ca.ChannelFactory;


/**
 * Bend is used to represent a normal horizontal dipole magnet rather than a corrector.
 *
 * @author  tap
 */
public class Bend extends Dipole {

    /*
     *  Constants
     */
    
    public static final String      s_strType   = "DH";

    
    static {
        registerType();
    }

    
    /*
     * Register type for qualification
     */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( Bend.class, s_strType, "bend" );
    }
  

    /** Override to provide type signature */
    public String getType()   { return s_strType; }


	/** Constructor */
	public Bend( final String strId, final ChannelFactory channelFactory )     {
		super( strId, channelFactory );
	}


	/** Constructor */
    public Bend( final String strId )     {
        this( strId, null );
    }

        
    /**
     * Get the orientation of the magnet as defined by MagnetType.  The orientation
     * of all bends is Horizontal.
     * @return HORIZONTAL
     */
    public int getOrientation() {
        return HORIZONTAL;
    }
    

    /**
     * Get the dipole bend magnet bending angle.
     */
    public double getDfltBendAngle() {
        return m_bucMagnet.getBendAngle();
    }
    
    /** returns design path length in meters */
    public double getDfltPathLength() {
        return m_bucMagnet.getPathLength();
    }
    
    /** returns dipole rotation angle for entrance pole face (deg) */
    public double getEntrRotAngle() {
        return m_bucMagnet.getDipoleEntrRotAngle();
    }
    
    /** returns dipole rotation angle for exit pole face (deg) */
    public double getExitRotAngle() {
        return m_bucMagnet.getDipoleExitRotAngle();
    }
    
    /** returns quadrupole component for bend dipole */
    public double getQuadComponent() {
        return m_bucMagnet.getDipoleQuadComponent();
    }
    
}
