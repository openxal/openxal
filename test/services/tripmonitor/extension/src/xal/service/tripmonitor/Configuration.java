//
//  Configuration.java
//  xal
//
//  Created by Thomas Pelaia on 8/4/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import java.util.*;
import java.net.URL;

import xal.tools.ResourceManager;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;


/** Get basic configuration information */
public class Configuration {
	/** trip monitor filters */
	final protected List<TripMonitorFilter> TRIP_MONITOR_FILTERS;
	
	/** trip monitor filter table keyed by name */
	final protected HashMap<String,TripMonitorFilter> TRIP_MONITOR_FILTER_TABLE;
	
	
	/** Constructor */
	public Configuration() {
		final URL url = ResourceManager.getResourceURL( getClass(), "config.xml" );
		final DataAdaptor mainAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
		final DataAdaptor configurationAdaptor = mainAdaptor.childAdaptor( "Configuration" );
		final List<DataAdaptor> monitorAdaptors = configurationAdaptor.childAdaptors( TripMonitorFilter.DATA_LABEL );
		
		final DataAdaptor tripFilterFactoryAdaptor = configurationAdaptor.childAdaptor( TripFilterFactory.DATA_LABEL );
		final TripFilterFactory tripFilterFactory = new TripFilterFactory( tripFilterFactoryAdaptor );
		
		TRIP_MONITOR_FILTERS = new ArrayList<TripMonitorFilter>( monitorAdaptors.size() );
		TRIP_MONITOR_FILTER_TABLE = new HashMap<String,TripMonitorFilter>( monitorAdaptors.size() );
		for ( final DataAdaptor monitorAdaptor : monitorAdaptors ) {
			final TripMonitorFilter filter = new TripMonitorFilter( monitorAdaptor, tripFilterFactory );
			TRIP_MONITOR_FILTERS.add( filter );
			TRIP_MONITOR_FILTER_TABLE.put( filter.getName(), filter );
		}
	}
	
	
	/** get persistent store for the specified trip monitor */
	public PersistentStore getPersistentStore( final String tripMonitorName ) {
		final TripMonitorFilter filter = TRIP_MONITOR_FILTER_TABLE.get( tripMonitorName );
		return filter != null ? filter.getPersistentStore() : null;
	}
	
	
	/** get the trip monitor filters */
	public List<TripMonitorFilter> getTripMonitorFilters() {
		return TRIP_MONITOR_FILTERS;
	}
	
	
	/** Get the trip monitor filter with the specified name */
	public TripMonitorFilter getTripMonitorFilter( final String tripMonitorFilterName ) {
		return TRIP_MONITOR_FILTER_TABLE.get( tripMonitorFilterName );
	}
	
	
	/** get the trip monitor names */
	public List<String> getTripMonitorFilterNames() {
		final List<String> monitorNames = new ArrayList<String>( TRIP_MONITOR_FILTERS.size() );
		for ( final TripMonitorFilter monitorFilter : TRIP_MONITOR_FILTERS ) {
			monitorNames.add( monitorFilter.getName() );
		}
		return monitorNames;
	}
}
