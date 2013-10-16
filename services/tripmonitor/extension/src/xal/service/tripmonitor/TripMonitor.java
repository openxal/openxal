//
//  TripMonitor.java
//  xal
//
//  Created by Thomas Pelaia on 7/31/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import java.util.*;

import xal.smf.AcceleratorNode;


/** montor trips */
public class TripMonitor implements NodeMonitorListener {
	/** trip monitor filter */
	final protected TripMonitorFilter MONITOR_FILTER;
	
	/** list of node monitors */
	final protected List<NodeMonitor> NODE_MONITORS;
	
	/** trip records */
	final protected List<TripRecord> TRIP_HISTORY;
	
	/** indicates whether this monitor is enabled */
	volatile protected boolean _isEnabled;
	
	
	/** Constructor */
	public TripMonitor( final TripMonitorFilter monitorFilter ) {
		TRIP_HISTORY = new ArrayList<TripRecord>();
		
		MONITOR_FILTER = monitorFilter;
				
		NODE_MONITORS = generateNodeMonitors();
		
		setEnabled( monitorFilter.isEnabled() );
	}
	
	
	/** generate the list of node monitors */
	protected List<NodeMonitor> generateNodeMonitors() {
		final List<AcceleratorNode> nodes = MONITOR_FILTER.getNodes();
		final List<NodeMonitor> nodeMonitors = new ArrayList<NodeMonitor>();
		
		for ( final AcceleratorNode node : nodes ) {
			final NodeMonitor nodeMonitor = new NodeMonitor( node, MONITOR_FILTER );
			nodeMonitor.addNodeMonitorListener( this );
			nodeMonitors.add( nodeMonitor );
		}
		
		return nodeMonitors;
	}
	
	
	/** get the name of the trip monitor */
	public String getName() {
		return MONITOR_FILTER.getName();
	}
	
	
	/** get the persistent store */
	public PersistentStore getPersistentStore() {
		return MONITOR_FILTER.getPersistentStore();
	}
	
	
	/** get the channel monitors */
	public List<ChannelMonitor> getChannelMonitors() {
		final List<ChannelMonitor> channelMonitors = new ArrayList<ChannelMonitor>();
		for ( final NodeMonitor nodeMonitor : NODE_MONITORS ) {
			channelMonitors.addAll( nodeMonitor.getChannelMonitors() );
		}
		
		return channelMonitors;
	}
	
	
	/** get the number of trip records in the history buffer */
	public int getTripHistoryCount() {
		synchronized ( TRIP_HISTORY ) {
			return TRIP_HISTORY.size();
		}
	}
	
	
	/** get the trip records */
	public List<TripRecord> getTripHistory() {
		synchronized( TRIP_HISTORY ) {
			final List<TripRecord> records = new ArrayList<TripRecord>( TRIP_HISTORY.size() );
			records.addAll( TRIP_HISTORY );
			return records;
		}
	}
	
	
	/** remove the specified records from history */
	public void clearTripRecords( final List<TripRecord> records ) {
		synchronized( TRIP_HISTORY ) {
			TRIP_HISTORY.removeAll( records );
		}
	}
	
	
	/** determine if this monitor is enabled */
	public boolean isEnabled() {
		return _isEnabled;
	}
	
	
	/** set whether this monitor is enabled */
	public void setEnabled( final boolean shouldEnable ) {
		_isEnabled = shouldEnable;
		
		if ( shouldEnable ) {
			for ( final NodeMonitor nodeMonitor : NODE_MONITORS ) {
				nodeMonitor.run();
			}
		}
	}
	
	
	/** description of this instance */
	public String toString() {
		return "Filter:  " + MONITOR_FILTER.toString() + ", enabled:  " + isEnabled() + ", node monitors:  " + NODE_MONITORS;
	}
	
	
	/**
	 * The PV's monitored trip count has been incremented.
	 * @param nodeMonitor the node monitor whose channel has tripped
	 * @param tripRecord record of the trip
	 */
	public void handleTrip( final NodeMonitor nodeMonitor, final TripRecord tripRecord ) {
		if ( isEnabled() ) {
			synchronized( TRIP_HISTORY ) {
				TRIP_HISTORY.add( tripRecord );
			}
		}
	}
	
	
	/**
	 * The channel's connection has changed.  Either it has established a new connection or the existing connection has dropped.
	 * @param nodeMonitor the node monitor whose channel has changed connection state
	 * @param monitor The channel monitor whose connection status has changed.
	 * @param connected The channel's new connection state
	 */
	public void connectionChanged( final NodeMonitor nodeMonitor, final ChannelMonitor monitor, final boolean connected ) {
	}
}
