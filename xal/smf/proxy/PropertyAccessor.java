/*
 * Created on Oct 23, 2003
 */
package xal.smf.proxy;

import java.util.*;

import xal.ca.Channel;
import xal.smf.AcceleratorNode;

/**
 * Specifies interface for property accessors that return acclerator node property values.
 * @author Craig McChesney
 * @author Tom Pelaia
 */
public interface PropertyAccessor {
	
	// Abstract Interface ======================================================

	/** get the map of design values keyed by property name */
	public Map<String,Double> getDesignValueMap( final AcceleratorNode node );

	/** get the map of live values keyed by property name */
	public Map<String,Double> getLiveValueMap( final AcceleratorNode node, final Map<Channel,Double> channelValues );

	/** get the map of live RF design values keyed by property name */
	public Map<String,Double> getLiveRFDesignValueMap( final AcceleratorNode node, final Map<Channel,Double> channelValues );

	/** get the channels for live property access */
	public Collection<Channel> getLiveChannels( final AcceleratorNode node );

	/** get the channels for live property access with design RF */
	public Collection<Channel> getLiveRFDesignChannels( final AcceleratorNode node );

	/** get the list of property names */
	List<String> propertyNames();
}
