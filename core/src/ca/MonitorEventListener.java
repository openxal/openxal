//
//  MonitorEventListener.java
//  xal
//
//  Created by Thomas Pelaia on 4/25/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.ca;


/** 
 * Listener of a channel's monitor events.
 * @author t6p
 */
public interface MonitorEventListener {
	/**
	 * The PV's monitored value has changed.
	 * @param channel the channel whose value has changed
	 * @param record The channel time record of the new value
	 */
	public void valueChanged( Channel channel, ChannelTimeRecord record );
	
	
	/**
	 * The channel's connection state has changed.  Either it has established a new connection or
	 * the existing connection has dropped.
	 * @param channel The channel whose connection has changed.
	 * @param connected The channel's new connection state
	 */
	public void connectionChanged( Channel channel, boolean connected );	
}
