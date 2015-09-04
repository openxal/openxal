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
		// property values keyed by property name
		final Map<String,Double> valueMap = new HashMap<String,Double>();

		// loop over each property by name
		propertyLoop: for( final String propertyName : propertyNames	) {
			// get the array of channels that are required to computer the property's value
			final Channel[] propertyChannels = node.getLivePropertyChannels( propertyName );

			// array to populate with channel values for the property
			final double[] propertyChannelValues = new double[propertyChannels.length];

			// loop over each channel for the current property and populate the propertyChannelValues with the corresponding value
			for ( int index = 0 ; index < propertyChannels.length ; index++ ) {
				final Channel channel = propertyChannels[index];
				final Double value = channelValues.get( channel );
				if ( value != null ) {
					propertyChannelValues[index] = value.doubleValue();
				} else {
					// Missing property values will likely cause a SynchronizationException later if and when the property is needed, so no need to throw any exceptions here.
					// Just print to standard error for extra diagnostics.
					System.err.println( "Missing channel value for property: " + propertyName + ", node: " + node.getId() + ", channel: " + channel.channelName() );
					// we need all of a property's channel values to compute the property value, so abandon the current property if we are missing any
					continue propertyLoop;		// abandon this property and continue with the next property if any
				}
			}

			// we only get here if all the channel values for the current property are available
			// compute the property value from the the property's channel values and populate the value map
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
