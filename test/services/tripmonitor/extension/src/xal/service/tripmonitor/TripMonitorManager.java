//
//  TripMonitorManager.java
//  xal
//
//  Created by Thomas Pelaia on 7/31/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import java.net.URL;
import java.util.*;

import xal.tools.ResourceManager;
import xal.tools.data.*;
import xal.tools.xml.XmlDataAdaptor;


/** manage trip monitors */
public class TripMonitorManager {
	/** trip monitor filters */
	final protected List<TripMonitor> TRIP_MONITORS;
    
    
	/** indicates whether verbose printing is enabled */
	final static protected boolean IS_VERBOSE;
	
	/** trip logger */
	protected TripLogger TRIP_LOGGER;
	
	
	/** Constructor */
	public TripMonitorManager() {
		TRIP_MONITORS = new ArrayList<TripMonitor>();
		
		final URL url = ResourceManager.getResourceURL( getClass(), "config.xml" );
        if(url == null) System.out.println("URL IS NULL");
		final DataAdaptor mainAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
		load( mainAdaptor.childAdaptor( "Configuration" ) );		
	}
	
	
    /**
	 * Static initializer
	 */
	static {
		
		final String verboseProperty = System.getProperty( "verbose", "false" );
		IS_VERBOSE = Boolean.parseBoolean( verboseProperty );
	}

    /** determine if verbose mode is set */
	static public boolean isVerbose() {
		return IS_VERBOSE;
	}
	
	
	/** print line to standard out if verbose mode is set */
	static public boolean printlnIfVerbose( final Object object ) {
		if ( IS_VERBOSE ) {
			System.out.println( object );
		}
		
		return IS_VERBOSE;
	}
    
	/** get trip monitors */
	public List<TripMonitor> getTripMonitors() {
		return TRIP_MONITORS;
	}
	
	
	/** publish the trips */
	public void publishTrips() {
		TRIP_LOGGER.publishTrips();
	}
	
	
	/** run the logger */
	public void run() {
		TRIP_LOGGER.start();
	}
	
	
	/** shutdown the service */
	public void shutdown( final int code ) {
		try {
			TRIP_LOGGER.stop();
		}
		finally {
			System.exit( code );
		}
	}
    
    
    /**
	 * Load the configuration from the data adaptor.
     * @param adaptor The adaptor from which to load the configuration
     */
    public void load( final DataAdaptor adaptor ) {
		final DataAdaptor tripFilterFactoryAdaptor = adaptor.childAdaptor( TripFilterFactory.DATA_LABEL );
		final TripFilterFactory tripFilterFactory = new TripFilterFactory( tripFilterFactoryAdaptor );
		
		final List<DataAdaptor> monitorAdaptors = adaptor.childAdaptors( TripMonitorFilter.DATA_LABEL );
		for ( final DataAdaptor monitorAdaptor : monitorAdaptors ) {
			final TripMonitorFilter filter = new TripMonitorFilter( monitorAdaptor, tripFilterFactory );
			TRIP_MONITORS.add( new TripMonitor( filter ) );
		}
		
		final DataAdaptor loggerAdaptor = adaptor.childAdaptor( TripLogger.DATA_LABEL );
		TRIP_LOGGER = new TripLogger( loggerAdaptor, TRIP_MONITORS );
		
		if ( TripMonitorManager.isVerbose() ) {
			System.out.println( "Trip Monitors:  " + TRIP_MONITORS );
		}
	}
}
