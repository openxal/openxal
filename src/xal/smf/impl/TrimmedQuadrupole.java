/*
 * TrimmedQuadrupole.java
 *
 * Created on July 1, 2003, 9:39 AM
 */

package xal.smf.impl;

import java.util.Collection;
import java.util.HashSet;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.tools.data.DataAdaptor;


/**
 * TrimmedQuadrupole is a subclass of Quadrupole that has a trim power supply in addition to a main power supply.
 * @author  tap
 */
public class TrimmedQuadrupole extends Quadrupole implements TrimmedMagnet {
	public static final String s_strType   = "QT";
	public static final String HORIZONTAL_TYPE = "QTH";
	public static final String VERTICAL_TYPE = "QTV";

	/** unique ID for this magnet's trim supply */
	protected String _trimSupplyID;


	// static initializer
	static {
		registerType();
	}


	/**
	 * Register type for qualification.  These are the types that are common to all instances.  The <code>isKindOf</code> method handles the
	 * type qualification specific to an instance.
	 * @see #isKindOf
	 */
	private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( TrimmedQuadrupole.class, s_strType, "trimmedquad" );
	}


	/**
	 * Constructor
	 * @param strID this magnet's unique node ID
	 * @param channelFactory factory for generating this node's channels
	 */
	public TrimmedQuadrupole( final String strID, final ChannelFactory channelFactory ) {
		super( strID, channelFactory );
	}


	/**
	 * Constructor
	 * @param strID this magnet's unique node ID
	 */
	public TrimmedQuadrupole( final String strID ) {
		this( strID, null );
	}


    /**
     * Update data from the power supply data adaptor.
     * @param powerSupplyAdaptor The data provider of power supply information.
     */
    protected void updatePowerSupplies( final DataAdaptor powerSupplyAdaptor ) {
        super.updatePowerSupplies( powerSupplyAdaptor );
        _trimSupplyID = powerSupplyAdaptor.stringValue( "trim" );
    }
    
    
    /**
     * Write data to the power supply data adaptor.
     * @param powerSupplyAdaptor The data sink for the power supply information
     */
    protected void writePowerSupplies( final DataAdaptor powerSupplyAdaptor ) {
        super.writePowerSupplies( powerSupplyAdaptor );
        powerSupplyAdaptor.setValue( "trim", _trimSupplyID );
    }
    
    
    /** 
     * Get the channel handles.  Overrides the default method to add handles from the trim power supply.
     * @return The channel handles associated with this node
     */
    public Collection<String> getHandles() {
        final Collection<String> handles = new HashSet<String>( super.getHandles() );
		try {
			handles.addAll( getTrimSupply().getChannelSuite().getHandles() );
		}
		catch(NullPointerException exception) {
			System.err.println("exception getting handles from the trim supply \"" + getTrimSupply() + "\" for trimmed quadrupole: " + getId());
			throw exception;
		}
        return handles;
    }
    
    
    /**
     * Find the channel for the specified handle checking the trim supply if the channel suite or main supply does not contain the handle
     * @param handle The handle for the channel to get.
     * @return The channel associated with this node and the specified handle or null if there is no match.
     */
    public Channel findChannel( final String handle ) {
		final Channel channel = super.findChannel( handle );
		return channel != null ? channel : getTrimSupply().getChannelSuite().getChannel( handle );
    }
    
    
    /**
     * Get the trim power supply for this magnet.
     * @return The trim power supply for this magnet
     */
    public MagnetTrimSupply getTrimSupply() {
        return getAccelerator().getMagnetTrimSupply( _trimSupplyID );
    }



	/** Get the live property value for the corresponding array of channel values in the order given by getLivePropertyChannels() */
	public double getLivePropertyValue( final String propertyName, final double[] channelValues ) {
		try {
			final Property property = Property.valueOf( propertyName );		// throws IllegalArgumentException if no matching property
			switch( property ) {
				case FIELD:
					// TODO: special logic may be needed to properly handle shunts
					// no matter whether the readback or setpoint, the field is the total of all associated field values
					double totalField = 0.0;
					for ( final double channelValue : channelValues ) {
						totalField += toFieldFromCA( channelValue );
					}
					return totalField;
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
					if ( _useFieldReadback ) {
						return super.getLivePropertyChannels( propertyName );
					}
					else {
						final Channel mainFieldSetChannel = findChannel( MagnetMainSupply.FIELD_SET_HANDLE );
						final Channel trimFieldSetChannel = findChannel( MagnetTrimSupply.FIELD_SET_HANDLE );
						return new Channel[] { mainFieldSetChannel, trimFieldSetChannel };
					}
				default:
					throw new IllegalArgumentException( "Unsupported Electromagnet live channels property: " + propertyName );
			}
		}
		catch( IllegalArgumentException exception ) {
			return super.getLivePropertyChannels( propertyName );
		}
	}

    
    /** 
	 * Set the trim power supply field contribution in the magnet.  If cycle enable 
     * is true then the magnet is cycled before the field is set to the specified value.
	 * @param newField is the new field level in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
     */  
    public void setTrimField( final double newField ) throws ConnectionException, PutException {
        getTrimSupply().setField( toCAFromField( newField ) );
    }
	
	
	/**
	 * Get the value to which the trim supply's field contribution is set.  Note that this is not the readback.
	 * @return the field setting in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
	 */
	public double getTrimFieldSetting() throws ConnectionException, GetException {
        return toFieldFromCA( getTrimSupply().getFieldSetting() );
	}
	
	
	/**
	 * Get the value to which the field is set including both the main supply and trim supply contributions.  
	 * Note that this is not the readback.
	 * @return the field setting in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
	 */
	public double getTotalFieldSetting() throws ConnectionException, GetException {
		return getFieldSetting() + getTrimFieldSetting();
	}
	

    /** Get the trim power supply current in this electromagnet via ca (A) */
    public double getTrimCurrent() throws ConnectionException, GetException {
        return getTrimSupply().getCurrent();
    }
    
    
    /** 
	 * set the trim power supply current in the magnet (A)
	 * @param newCurrent is the new current (A)
     */  
    public void setTrimCurrent( final double newCurrent ) throws ConnectionException, PutException {
        getTrimSupply().setCurrent( newCurrent );
    }
    
    
    /**
     * Get the orientation of the magnet as defined by MagnetType.  The orientation
     * of the quad is determined by its type: QTH or QTV
     * @return One of HORIZONTAL or VERTICAL
     */
    public int getOrientation() {
        return ( _type.equalsIgnoreCase( HORIZONTAL_TYPE ) ) ? HORIZONTAL : VERTICAL;
    }
}
