//
//  TripFilterFactory.java
//  xal
//
//  Created by Tom Pelaia on 11/30/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import xal.tools.data.DataAdaptor;

import java.util.*;


/** factory for generating trip filters */
class TripFilterFactory {
	/** data adator label */
	final static public String DATA_LABEL = "TripFilterFactory";
	
	/** trip filters keyed by name */
	final protected HashMap<String,TripFilter> TRIP_FILTERS;
	
		
	/** Constructor */
	public TripFilterFactory( final DataAdaptor adaptor ) {
		TRIP_FILTERS = new HashMap<String,TripFilter>();
		final List<DataAdaptor> filterAdaptors = adaptor.childAdaptors( "TripFilter" );
		for ( final DataAdaptor filterAdaptor : filterAdaptors ) {
			final String name = filterAdaptor.stringValue( "name" );
			final String type = filterAdaptor.stringValue( "type" );
			
			TripFilter filter = null;
			if ( type.equalsIgnoreCase( "TripCounter" ) ) {
				filter = getTripCounterFilter();
			}
			else if ( type.equalsIgnoreCase( "OkayValue" ) ) {
				final int target = filterAdaptor.intValue( "target" );
				filter = getOkayValueFilter( target );
			}
			TRIP_FILTERS.put( name, filter );
		}
	}
	
	
	/** get the trip filter with the specified name */
	public TripFilter getTripFilter( final String name ) {
		return TRIP_FILTERS.get( name );
	}
	
	
	/** get a trip filter which filters based on a trip counter */
	static public TripFilter getTripCounterFilter() {
		return new TripFilter() {
			/** a new trip is indicated by the new value being greater than the old value and also greater than zero */
			public boolean isTripped( final int oldValue, final int newValue ) {
				return newValue > oldValue && newValue > 0;
			}
		};
	}
	
	
	/** get a trip filter which filters based on a particular value indicating okay */
	static public TripFilter getOkayValueFilter( final int okayValue ) {
		return new TripFilter() {
			/** a new trip is indicated by the new value being greater than the old value and also greater than zero */
			public boolean isTripped( final int oldValue, final int newValue ) {
				return newValue != okayValue;
			}
		};
	}
}
