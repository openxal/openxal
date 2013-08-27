/*
 * Created on Oct 23, 2003
 */
package xal.smf.proxy;

import java.util.*;

import xal.smf.AcceleratorNode;
import xal.smf.impl.RfGap;
import xal.ca.Channel;

/**
 * Returns property values for RfGap nodes.
 * @author Craig McChesney
 * @author Tom Pelaia
 */
public class RfGapPropertyAccessor extends AbstractPropertyAccessor {
	
	// Constants ===============================================================
	
	// Property Names
	public static final String PROPERTY_ETL = RfGap.Property.ETL.name();
	public static final String PROPERTY_PHASE = RfGap.Property.PHASE.name();
	public static final String PROPERTY_FREQUENCY = RfGap.Property.FREQUENCY.name();
	public static final String PROPERTY_E0 = RfGap.Property.FIELD.name();
	
	
	// Scaling Factors for unit conversion	
	public static final double SCALE_ETL = 1.e6;
	public static final double SCALE_PHASE = Math.PI/180.;
	public static final double SCALE_FREQUENCY = 1.e6;
	public static final double SCALE_E0 = 1.e6;


	/** scale factors keyed by property name */
	private static final Map<String,Double> PROPERTY_SCALE_FACTORS;
	
	
	// Static Variables ========================================================
	private static List<String> PROPERTY_NAMES;

	
	// Static Initialization ===================================================
	
	static {
		PROPERTY_NAMES = new ArrayList<String>();
		PROPERTY_NAMES.add( PROPERTY_ETL );
		PROPERTY_NAMES.add( PROPERTY_PHASE );
		PROPERTY_NAMES.add( PROPERTY_FREQUENCY );
		PROPERTY_NAMES.add( PROPERTY_E0 );

		PROPERTY_SCALE_FACTORS = new HashMap<String,Double>();
		PROPERTY_SCALE_FACTORS.put( PROPERTY_ETL, SCALE_ETL );
		PROPERTY_SCALE_FACTORS.put( PROPERTY_PHASE, SCALE_PHASE );
		PROPERTY_SCALE_FACTORS.put( PROPERTY_FREQUENCY, SCALE_FREQUENCY );
		PROPERTY_SCALE_FACTORS.put( PROPERTY_E0, SCALE_E0 );
	}

	
	/** Get the scale factor for the specified property */
	protected double getPropertyScale( final String propertyName ) {
		final Double scaleFactor = PROPERTY_SCALE_FACTORS.get( propertyName );
		return scaleFactor != null ? scaleFactor : 1.0;
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
