/*
 * Created on Mar 17, 2004
 */
package xal.smf.proxy;

import java.util.*;

import xal.smf.AcceleratorNode;
import xal.smf.impl.RfCavity;
import xal.ca.Channel;

/**
 * @author Craig McChesney
 * @author Tom Pelaia
 */
public class RfCavityPropertyAccessor extends AbstractPropertyAccessor {
	
	// Constants ===============================================================	
	
	// Property Names	
	public static final String PROPERTY_PHASE = RfCavity.Property.PHASE.name();
	public static final String PROPERTY_AMPLITUDE = RfCavity.Property.AMPLITUDE.name();
	
	// Class Variables =========================================================
	final private static List<String> PROPERTY_NAMES;

	
	// Class Initialization ===================================================
	
	static {
		PROPERTY_NAMES = new ArrayList<String>();
		PROPERTY_NAMES.add( PROPERTY_PHASE );
		PROPERTY_NAMES.add( PROPERTY_AMPLITUDE );
	}




	/** get the map of design values keyed by property name */
	public Map<String,Double> getDesignValueMap( final AcceleratorNode node ) {
		return getDesignValueMap( node, PROPERTY_NAMES );
	}


	/** get the map of live values keyed by property name */
	public Map<String,Double> getLiveValueMap( final AcceleratorNode node, final Map<Channel,Double> channelValues ) {
		return getLiveValueMap( node, channelValues, PROPERTY_NAMES );
	}


	/** get the map of live RF design values keyed by property name */
	public Map<String,Double> getLiveRFDesignValueMap( final AcceleratorNode node, final Map<Channel,Double> channelValues ) {
		return getDesignValueMap( node );
	}


	/** get the channels for live property access */
	public Collection<Channel> getLiveChannels( final AcceleratorNode node ) {
		return getLiveChannels( node, PROPERTY_NAMES );
	}


	/** get the channels for live property access with design RF */
	public Collection<Channel> getLiveRFDesignChannels( final AcceleratorNode node ) {
		return Collections.<Channel>emptySet();
	}


	/** get the list of property names */
    @SuppressWarnings( "unchecked" )    // clone doesn't support generics, so we must cast
	public List<String> propertyNames() {
		return new ArrayList<String>( PROPERTY_NAMES );
	}

}
