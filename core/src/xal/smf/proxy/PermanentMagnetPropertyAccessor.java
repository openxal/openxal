/*
 * Created on Oct 14, 2014
 */
package xal.smf.proxy;

import java.util.*;

import xal.smf.AcceleratorNode;
import xal.smf.impl.PermanentMagnet;
import xal.ca.Channel;

/**
 * Access property values for a permanent magnet
 * @author Tom Pelaia
 */
public class PermanentMagnetPropertyAccessor extends AbstractPropertyAccessor {
	
	// Constants ===============================================================
	
	// Property Names
	public static final String PROPERTY_FIELD = PermanentMagnet.Property.FIELD.name();
	
	
	// Static Variables ========================================================

	final private static List<String> PROPERTY_NAMES;
		
	
	// Static Initialization ===================================================
	
	static {
		PROPERTY_NAMES = new ArrayList<String>();
		PROPERTY_NAMES.add( PROPERTY_FIELD );
	}


	/** get the map of design values keyed by property name */
	public Map<String,Double> getDesignValueMap( final AcceleratorNode node ) {
		return getDesignValueMap( node, PROPERTY_NAMES );
	}


	/** get the map of live values keyed by property name */
	public Map<String,Double> getLiveValueMap( final AcceleratorNode node, final Map<Channel,Double> channelValues ) {
		return getLiveValueMap( node, channelValues, PROPERTY_NAMES );
	}


	/** get the channels for live property access */
	public Collection<Channel> getLiveChannels( final AcceleratorNode node ) {
		return getLiveChannels( node, PROPERTY_NAMES );
	}


	/** get the map of live RF design values keyed by property name */
	public Map<String,Double> getLiveRFDesignValueMap( final AcceleratorNode node, final Map<Channel,Double> channelValues ) {
		return getLiveValueMap( node, channelValues );
	}

	
	/** get the channels for live property access with design RF */
	public Collection<Channel> getLiveRFDesignChannels( final AcceleratorNode node ) {
		return getLiveChannels( node );
	}


	/** get the list of property names */
    @SuppressWarnings( "unchecked" )    // clone doesn't support generics, so we must cast
	public List<String> propertyNames() {
		return new ArrayList<String>( PROPERTY_NAMES );
	}

}
