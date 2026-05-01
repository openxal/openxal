//
// ChannelWrapper.java
// xal
//
// Created by Tom Pelaia on 3/30/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.service.errantbeamcapture;

import java.util.*;

import xal.ca.*;


/** wrap a channel to manage its connection, monitor and latest record */
class ChannelWrapper implements ConnectionListener, IEventSinkValTime {
	/** channel to monitor */
	final private Channel CHANNEL;
	
	/** handler of the monitor events */
	final private ChannelWrapperDelegate MONITOR_EVENT_HANDLER;
	
	/** value that indicates the state is good (all others indicate a trip) */
	final private double GOOD_STATE;
	
	/** channel event monitor */
	private Monitor _monitor;
	
	/** latest record captured */
	private ChannelTimeRecord _latestRecord;
	
	
	/** Constructor */
	public ChannelWrapper( final Channel channel, final double goodState, final ChannelWrapperDelegate monitorEventHandler ) {
		GOOD_STATE = goodState;
		
		_latestRecord = null;
		_monitor = null;
		
		CHANNEL = channel;
		MONITOR_EVENT_HANDLER = monitorEventHandler;
		
		channel.addConnectionListener( this );
		channel.requestConnection();
	}
	
	
	/** Get the channel */
	public Channel getChannel() {
		return CHANNEL;
	}
	
	
	/** determine whether the channel is valid (has a state) */
	public boolean isValid() {
		return _latestRecord != null;
	}
	
	
	/** determine whether the channel is in a good state */
	public boolean isGood() {
		return _latestRecord.doubleValue() == GOOD_STATE;
	}
	
	
	/** Get the latest record */
	public ChannelTimeRecord getLatestRecord() {
		return _latestRecord;
	}
	
	
	/** Dispose of the channel */
	public void dispose() {
		final Monitor monitor = _monitor;
		_monitor = null;
		monitor.clear();
	}
	
	
    /**
     * Indicates that a connection to the specified channel has been established.
     * @param channel The channel which has been connected.
     */
    public void connectionMade( final Channel channel ) {
		if ( _monitor == null ) {
			try {
				_monitor = CHANNEL.addMonitorValTime( this, Monitor.VALUE );
				Channel.flushIO();
			}
			catch( Exception exception ) {
				System.err.println( "Exception creating monitor for channel: " + channel.getId() );
				exception.printStackTrace();
			}
		}
	}
    
	
    /**
     * Indicates that a connection to the specified channel has been dropped.
     * @param channel The channel which has been disconnected.
     */
    public void connectionDropped( final Channel channel ) {}
	
	
	/** Handle monitor event */
	public void eventValue( final ChannelTimeRecord record, final Channel channel ) {
		_latestRecord = record;
		//System.out.println( "Captured record: " + record );
		MONITOR_EVENT_HANDLER.channelStateChanged( this, record );		// forward the event to the delegate
	}
}
