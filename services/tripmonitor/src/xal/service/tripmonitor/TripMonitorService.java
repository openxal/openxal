//
//  TripMonitorService.java
//  xal
//
//  Created by Thomas Pelaia on 8/3/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import xal.extension.service.*;

import java.util.*;
import java.text.*;


/** service which broadcasts trip monitor status */
public class TripMonitorService implements TripMonitorPortal {
	/** identifies the service type */
	protected final static String IDENTITY = "Trip Monitor";
	
	/** Formatter for translating a date to and from a string */
	protected final static DateFormat DATE_FORMATTER;
	
	/** The trip monitor manager */
	protected final TripMonitorManager TRIP_MANAGER;
	
	
	// static initializer
	static {
		DATE_FORMATTER = new SimpleDateFormat( TripMonitorPortal.DATE_FORMAT );
	}
	
	
	/**
	 * Constructor
	 * @param tripManager  The trip manager model
	 */
	public TripMonitorService( final TripMonitorManager tripManager ) {
		TRIP_MANAGER = tripManager;
		broadcast();
	}
	
	
	/** Begin broadcasting the service  */
	public void broadcast() {
		ServiceDirectory.defaultDirectory().registerService( TripMonitorPortal.class, IDENTITY, this );
		System.out.println( "broadcasting..." );
	}
	
	
	/**
	 * Shutdown the process.
	 * @param code  The shutdown code which is normally just 0.
	 * @return      0 Services must always return something
	 */
	public int shutdown( int code ) {
		TRIP_MANAGER.shutdown( code );
		return 0;
	}
	
	
	/**
	 * Get the name of the host where the application is running.
	 * @return   The name of the host where the application is running.
	 */
	public String getHostName() {
		try {
			return java.net.InetAddress.getLocalHost().getHostName();
		}
		catch ( java.net.UnknownHostException exception ) {
			return "";
		}
	}
	
	
	/**
	 * Get the launch time of the service.
	 * @return   the launch time of the application.
	 */
	public Date getLaunchTime() {
		return Main.getLaunchTime();
	}
	
	
	/**
	 * Get the trip monitor labels
	 * @return the list of trip monitor labels
	 */
	public List<String> getTripMonitorNames() {
		final List<TripMonitor> tripMonitors = TRIP_MANAGER.getTripMonitors();
		final List<String> monitorNames = new ArrayList<String>( tripMonitors.size() );
		
		for ( final TripMonitor tripMonitor : tripMonitors ) {
			monitorNames.add( tripMonitor.getName() );
		}
		
		return monitorNames;
	}
	
	
	/**
	 * Get the list of all PVs we are attempting to monitor and log. The information is returned as a list of channel info tables (one entry for each PV). 
	 * The channel info table has the CHANNEL_PV and CHANNEL_CONNECTION keys and provides the signal name and the connection status of a channel.
	 * @param monitorName name of the trip monitor for which to get the channel information
	 * @return  list of all PV info tables for the PVs we are attempting to monitor and log
	 */
	public List<HashMap<String, Object>> getChannelInfo( final String monitorName ) {
		final TripMonitor tripMonitor = getTripMonitorWithName( monitorName );
		if ( tripMonitor != null ) {
			final List<ChannelMonitor> channelMonitors = tripMonitor.getChannelMonitors();
			final List<HashMap<String, Object>> channelInfo = new ArrayList<HashMap<String, Object>>( channelMonitors.size() );
			for ( final ChannelMonitor channelMonitor : channelMonitors ) {
				final HashMap<String, Object> record = new HashMap<String, Object>();
				record.put( PV_KEY, channelMonitor.getPV() );
				record.put( CHANNEL_CONNECTION_KEY, channelMonitor.isConnected() );
				channelInfo.add( record );
			}
			
			return channelInfo;
		}
		else {
			return new ArrayList<HashMap<String, Object>>();
		}
	}
	
	
	/** 
	 * Get the number of trip records in the history buffer of the specified trip monitor
	 * @param monitorName name of the trip monitor
	 * @return the number of trips in the monitor's buffer
	 */
	public int getTripHistoryCount( final String monitorName ) {
		return getTripMonitorWithName( monitorName ).getTripHistoryCount();
	}
	
	
	/** get the trip monitor with the specified name */
	protected TripMonitor getTripMonitorWithName( final String monitorName ) {
		final List<TripMonitor> tripMonitors = TRIP_MANAGER.getTripMonitors();
		
		for ( final TripMonitor tripMonitor : tripMonitors ) {
			if ( tripMonitor.getName().equals( monitorName ) ) {
				return tripMonitor;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Determine if the specified monitor is enabled
	 * @return true if the monitor is enabled and false if not
	 */
	public boolean isEnabled( final String monitorName ) {
		return getTripMonitorWithName( monitorName ).isEnabled();
	}
	
	
	/** 
	 * Get the number of trip records in the history buffer of the specified trip monitor
	 * @param monitorName name of the trip monitor
	 * @return the number of trips in the monitor's buffer
	 */
	public List<HashMap<String, Object>>  getTripRecords( final String monitorName ) {
		final TripMonitor tripMonitor = getTripMonitorWithName( monitorName );
		final List<TripRecord> tripRecords = tripMonitor.getTripHistory();
		
		final List<HashMap<String, Object>> recordTables = new ArrayList<HashMap<String, Object>>( tripRecords.size() );
		
		for ( final TripRecord record : tripRecords ) {
			final HashMap<String, Object> recordTable = new HashMap<String, Object>();
			recordTable.put( PV_KEY, record.getPV() );
			recordTable.put( TIMESTAMP_KEY, record.getDate() );
			recordTables.add( recordTable );
		}
		
		return recordTables;
	}
	
	
	/**
	 * Publish the trips
	 * @return 1
	 */
	public int publishTrips() {
		TRIP_MANAGER.publishTrips();
		return 1;
	}
}



