/*
 * PermanentMagnet.java
 *
 * Created on January 30, 2002, 2:02 PM
 */

package xal.smf.impl;

import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;
import xal.ca.*;


/**
 * PermanentMagnet is the superclass of all permanent magnet classes.
 * @author  tap
 */
abstract public class PermanentMagnet extends Magnet {
	/** accessible properties */
	public enum Property { FIELD }


	// static initializer
    static {
        registerType();
    }

    
    /*
     * Register type for qualification
     */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( PermanentMagnet.class, "pmag", "permanentmagnet" );
    }


	/** Creates new PermanentMagnet */
	public PermanentMagnet( final String strId, final ChannelFactory channelFactory ) {
		super( strId, channelFactory );
	}


    /** Creates new PermanentMagnet */
    public PermanentMagnet( final String strId ) {
        this( strId, null );
    }
      
         
    /**
     * Since this is a permanent magnet we override the inherited method to 
     * advertise this characteristic.
     * @return true since all PermanentMagnet instances are permanent magnets.
     */
    public boolean isPermanent() {
        return true;
    }


	/** Get the design value for the specified property */
	public double getDesignPropertyValue( final String propertyName ) {
		try {
			final Property property = Property.valueOf( propertyName );		// throws IllegalArgumentException if no matching property
			switch( property ) {
				case FIELD:
					return getDesignField();
				default:
					throw new IllegalArgumentException( "Unsupported Electromagnet design value property: " + propertyName );
			}
		}
		catch ( IllegalArgumentException exception ) {
			return super.getDesignPropertyValue( propertyName );
		}
	}


	/** Get the live property value for the corresponding array of channel values in the order given by getLivePropertyChannels() */
	public double getLivePropertyValue( final String propertyName, final double[] channelValues ) {
		try {
			final Property property = Property.valueOf( propertyName );		// throws IllegalArgumentException if no matching property
			switch( property ) {
				case FIELD:
					return getDesignField();	// design same as live for permanent magnets
				default:
					throw new IllegalArgumentException( "Unsupported Electromagnet live value property: " + propertyName );
			}
		}
		catch( IllegalArgumentException exception ) {
			return super.getLivePropertyValue( propertyName, channelValues );
		}
	}


	/** Get the array of channels for the specified property */
	public Channel[] getLivePropertyChannels( final String propertyName ) {
		try {
			final Property property = Property.valueOf( propertyName );		// throws IllegalArgumentException if no matching property
			switch( property ) {
				case FIELD:
					return new Channel[0];
				default:
					throw new IllegalArgumentException( "Unsupported Electromagnet live channels property: " + propertyName );
			}
		}
		catch( IllegalArgumentException exception ) {
			return super.getLivePropertyChannels( propertyName );
		}
	}

    
    /** 
     * returns the field of the magnet (T /(m^ (n-1))), n=1 for dipole,
     * 2 for quad etc.
     */

    public double getField() {
        
        return getDesignField();
    }

    /** 
     * returns the integrated field of the magnet (T-m /(m^ (n-1))), 
     *  n=1 for dipole, 2 for quad etc.
     */
    public double getFieldInt() {
        
        return (getDesignField() * getEffLength());
    }
}
