/*
 * Created on Oct 24, 2003
 */
package xal.smf.proxy;

import java.util.*;

import xal.smf.AcceleratorNode;
import xal.ca.Channel;


/**
 * Access property values for a node
 * @author Tom Pelaia
 */
abstract public class AbstractPropertyAccessor implements PropertyAccessor {
	/** Get the scale factor for the specified property */
	protected double getPropertyScale( final String propertyName ) {
		return 1.0;
	}


	/** get the map of design values keyed by property name */
	protected Map<String,Double> getDesignValueMap( final AcceleratorNode node, final List<String> propertyNames ) {
		final Map<String,Double> valueMap = new HashMap<String,Double>();
		for( final String propertyName : propertyNames	) {
			final double scale = getPropertyScale( propertyName );
			final double value = scale * node.getDesignPropertyValue( propertyName );
			valueMap.put( propertyName, value );
		}
		
		return valueMap;
	}

	
	/** get the map of live values keyed by property name */
	protected Map<String,Double> getLiveValueMap( final AcceleratorNode node, final Map<Channel,Double> channelValues, final List<String> propertyNames ) {
		final Map<String,Double> valueMap = new HashMap<String,Double>();
		for( final String propertyName : propertyNames	) {
			final Channel[] propertyChannels = node.getLivePropertyChannels( propertyName );
			final double[] propertyChannelValues = new double[propertyChannels.length];
			for ( int index = 0 ; index < propertyChannels.length ; index++ ) {
				final Channel channel = propertyChannels[index];
				propertyChannelValues[index] = channelValues.get( channel );
			}
			final double scale = getPropertyScale( propertyName );
			final double propertyValue = scale * node.getLivePropertyValue( propertyName, propertyChannelValues );
			valueMap.put( propertyName, propertyValue );
		}

		return valueMap;
	}


	/** get the channels for live property access */
	protected Collection<Channel> getLiveChannels( final AcceleratorNode node, final List<String> propertyNames ) {
		final Set<Channel> channels = new HashSet<Channel>();

		for( final String propertyName : propertyNames	) {
			final Channel[] propertyChannels = node.getLivePropertyChannels( propertyName );
			for ( final Channel channel : propertyChannels ) {
				channels.add( channel );
			}
		}

		return channels;
	}
}
