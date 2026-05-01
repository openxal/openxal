//
//  ChannelEventListener.java
//  xal
//
//  Created by Thomas Pelaia on 8/1/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.service.tripmonitor;

import xal.ca.*;


/**
 * ChannelEventListener is an interface for a listener of channel events.
 * @author  tap
 */
public interface ChannelEventListener {
	/**
	 * The PV's monitored trip count has been incremented.
	 * @param monitor the channel monitor whose trip count has changed
	 * @param tripRecord The record of the trip
	 */
	public void handleTrip( ChannelMonitor monitor, TripRecord tripRecord );
	
	
	/**
	 * The PV's monitored value has changed.
	 * @param monitor the channel monitor whose value has changed
	 * @param record The channel time record of the new value
	 */
	public void valueChanged( ChannelMonitor monitor, ChannelTimeRecord record );
	
	
	/**
	 * The channel's connection has changed.  Either it has established a new connection or the existing connection has dropped.
	 * @param monitor The channel monitor whose connection status has changed.
	 * @param connected The channel's new connection state
	 */
	public void connectionChanged( ChannelMonitor monitor, boolean connected );
}
