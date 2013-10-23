//
//  NodeMonitorListener.java
//  xal
//
//  Created by Thomas Pelaia on 8/1/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;


/** interface for receiving node monitor events */
public interface NodeMonitorListener {
	/**
	 * The PV's monitored trip count has been incremented.
	 * @param nodeMonitor the node monitor whose channel has tripped
	 * @param tripRecord record of the trip
	 */
	public void handleTrip( NodeMonitor nodeMonitor, TripRecord tripRecord );
	
	
	/**
	 * The channel's connection has changed.  Either it has established a new connection or the existing connection has dropped.
	 * @param nodeMonitor the node monitor whose channel has changed connection state
	 * @param monitor The channel monitor whose connection status has changed.
	 * @param isConnected The channel's new connection state
	 */
	public void connectionChanged( NodeMonitor nodeMonitor, ChannelMonitor monitor, boolean isConnected );
}
