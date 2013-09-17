//
//  NodeMonitor.java
//  xal
//
//  Created by Thomas Pelaia on 8/1/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import java.util.*;

import xal.ca.ChannelTimeRecord;
import xal.smf.AcceleratorNode;
import xal.tools.messaging.MessageCenter;


/** monitors a single cavity */
public class NodeMonitor implements ChannelEventListener {
	/** event message center */
	protected MessageCenter MESSAGE_CENTER;
	
	/** proxy for posting node monitor events */
	protected NodeMonitorListener EVENT_PROXY;
	
	/** trip monitor filter */
	final protected TripMonitorFilter MONITOR_FILTER;
	
	/** accelerator node to monitor */
	final protected AcceleratorNode NODE;
	
	/** channel monitors */
	final protected List<ChannelMonitor> CHANNEL_MONITORS;
	
	
	/** Constructor */
	public NodeMonitor( final AcceleratorNode node, final TripMonitorFilter monitorFilter ) {
		MESSAGE_CENTER = new MessageCenter( "Node Monitor" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, NodeMonitorListener.class );
		
		NODE = node;
		MONITOR_FILTER = monitorFilter;
		
		CHANNEL_MONITORS = MONITOR_FILTER.getChannelMonitors( NODE );
		listenToChannelMonitorEvents();
	}
	
	
	/** add a node monitor listener */
	public void addNodeMonitorListener( final NodeMonitorListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, NodeMonitorListener.class );
	}
	
	
	/** remove the node monitor listener */
	public void removeNodeMonitorListener( final NodeMonitorListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, NodeMonitorListener.class );
	}
	
	
	/** get the accelerator node */
	public AcceleratorNode getNode() {
		return NODE;
	}
	
	
	/** get the node ID */
	public String getID() {
		return NODE.getId();
	}
	
	
	/** get the channel monitors */
	public List<ChannelMonitor> getChannelMonitors() {
		return CHANNEL_MONITORS;
	}
	
	
	/** run the monitor */
	public void run() {
		if ( TripMonitorManager.isVerbose() ) {
			System.out.println( "Run monitor for node:  " + NODE.getId() );
		}
		for ( final ChannelMonitor channelMonitor : CHANNEL_MONITORS ) {
			channelMonitor.requestConnection();
		}
	}
	
	
	/** listen for channel monitor events */
	protected void listenToChannelMonitorEvents() {
		for ( final ChannelMonitor channelMonitor : CHANNEL_MONITORS ) {
			channelMonitor.addChannelEventListener( this );
		}
	}
	
	
	/**
	 * The PV's monitored trip count has been incremented.
	 * @param monitor the channel monitor whose trip count has changed
	 * @param tripRecord record of the trip
	 */
	public void handleTrip( final ChannelMonitor monitor, final TripRecord tripRecord ) {
		TripMonitorManager.printlnIfVerbose( "Trip:  " + tripRecord );
		EVENT_PROXY.handleTrip( this, tripRecord );
	}
	
	
	/**
	 * The PV's monitored value has changed.
	 * @param monitor the channel monitor whose value has changed
	 * @param record The channel time record of the new value
	 */
	public void valueChanged( final ChannelMonitor monitor, final ChannelTimeRecord record ) {}
	
	
	/**
	 * The channel's connection has changed.  Either it has established a new connection or the existing connection has dropped.
	 * @param monitor The channel monitor whose connection status has changed.
	 * @param isConnected The channel's new connection state
	 */
	public void connectionChanged( final ChannelMonitor monitor, final boolean isConnected ) {
		TripMonitorManager.printlnIfVerbose( monitor + " is connected:  " + isConnected );
		EVENT_PROXY.connectionChanged( this, monitor, isConnected );
	}
	
	
	/** description of this instance */
	public String toString() {
		return "Node:  " + NODE + ", channel monitors:  " + CHANNEL_MONITORS;
	}
}
