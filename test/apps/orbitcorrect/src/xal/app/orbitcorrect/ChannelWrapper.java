/*
 *  ChannelWrapper.java
 *
 *  Created on Wed Dec 03 17:11:08 EST 2003
 *
 *  Copyright (c) 2003 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.ca.*;
import xal.tools.messaging.MessageCenter;

import java.util.logging.*;


/**
 * ChannelWrapper is a wrapper for a Channel that handles connecting to the channel and setting
 * up a monitor when its channel is connected.
 *
 * @author    tap
 * @since   Dec 3, 2003
 */
public class ChannelWrapper {
	/** The channel to wrap */
	protected Channel _channel;

	/** The monitor for the channel */
	protected Monitor _monitor;

	/** event message center */
	protected MessageCenter _messageCenter;

	/** proxy for posting channel events */
	protected ChannelEventListener _eventProxy;

	/** synchronization lock */
	protected Object _eventLock;

	/** last record captured */
	protected ChannelTimeRecord _lastRecord;
	
	/** connection listener */
	protected ConnectionListener _connectionListener;


	/**
	 * ChannelWrapper constructor
	 *
	 * @param channel  The channel to wrap.
	 */
	public ChannelWrapper( Channel channel ) {
		_eventLock = new Object();
		_channel = channel;
		_messageCenter = new MessageCenter();
		_eventProxy = _messageCenter.registerSource( this, ChannelEventListener.class );
		_lastRecord = null;
	}


	/**
	 * ChannelWrapper constructor
	 *
	 * @param pv  The PV for which to create a channel.
	 */
	public ChannelWrapper( String pv ) {
		this( ChannelFactory.defaultFactory().getChannel( pv ) );
	}


	/**
	 * Register the listener as a receiver of channel events from the wrapped channel
	 *
	 * @param listener  The listener to receive channel events
	 */
	public void addChannelEventListener( ChannelEventListener listener ) {
		synchronized(_eventLock) {
			_messageCenter.registerTarget( listener, this, ChannelEventListener.class );
			if ( _channel != null ) {
				listener.connectionChanged( _channel, isConnected() );
				if ( _lastRecord != null ) {
					listener.valueChanged( _channel, _lastRecord );
				}
			}
		}
	}


	/**
	 * Unregister the listener as a receiver of channel events from the wrapped channel
	 *
	 * @param listener  The listener to unregister from receiving channel events
	 */
	public void removeChannelEventListener( ChannelEventListener listener ) {
		_messageCenter.removeTarget( listener, this, ChannelEventListener.class );
	}


	/**
	 * Get the PV for the channel being wrapped.
	 *
	 * @return   the PV
	 */
	public String getPV() {
		return _channel.channelName();
	}


	/**
	 * Get the wrapped channel.
	 *
	 * @return   the wrapped channel
	 */
	public Channel getChannel() {
		return _channel;
	}


	/**
	 * Determine if the channel is connected.
	 *
	 * @return   true if the channel is connected and false if not.
	 */
	public boolean isConnected() {
		return _channel.isConnected();
	}
	
	
	/**
	 * Get the latest record
	 * @return the latest record
	 */
	public ChannelTimeRecord getLatestRecord() {
		synchronized( _eventLock ) {
			return _lastRecord;
		}
	}
	
	
	/**
	 * Get the latest value as a double.
	 * @return the latest value or NaN if there is none.
	 */
	public double doubleValue() {
		final ChannelTimeRecord record = getLatestRecord();
		return record != null ? record.doubleValue() : Double.NaN;
	}
	

	/**
	 * Request that the channel be connected. When the channel connection occurs, create a
	 * monitor.
	 */
	public void requestConnection() {
		if ( _connectionListener == null ) {
			_connectionListener = new ConnectionListener() {
				/**
				 * Indicates that a connection to the specified channel has been established.
				 *
				 * @param channel  The channel which has been connected.
				 */
				public void connectionMade( Channel channel ) {
					synchronized(_eventLock) {
						_lastRecord = null;
						if ( _monitor == null ) {
							makeMonitor();
						}
						_eventProxy.connectionChanged( channel, true );
					}
				}


				/**
				 * Indicates that a connection to the specified channel has been dropped.
				 *
				 * @param channel  The channel which has been disconnected.
				 */
				public void connectionDropped( Channel channel ) {
					synchronized(_eventLock) {
						_lastRecord = null;
						_eventProxy.connectionChanged( channel, false );
					}
				}
			};
			
			_channel.addConnectionListener( _connectionListener );
		}
		
		if ( !_channel.isConnected() ) {		// initiate a connection
			_channel.requestConnection();
		}
	}


	/**
	 * Create a monitor to listen for new channel records. An instance of an internal anonymous
	 * class is the listener of the monitor events and caches the latest channel record.
	 */
	protected void makeMonitor() {
		try {
			_monitor = _channel.addMonitorValTime(
				new IEventSinkValTime() {
					/**
					 * handle the monitor event by caching the latest channel record
					 *
					 * @param record   Description of the Parameter
					 * @param channel  Description of the Parameter
					 */
					public void eventValue( ChannelTimeRecord record, Channel channel ) {
						synchronized ( _eventLock ) {
							_lastRecord = record;
							if ( _eventProxy != null ) {
								_eventProxy.valueChanged( channel, record );
							}
						}
					}
				}, Monitor.VALUE );
		}
		catch ( ConnectionException exception ) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log( Level.SEVERE, "Connection exception.", exception );
			exception.printStackTrace();
		}
		catch ( MonitorException exception ) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log( Level.SEVERE, "Monitor exception.", exception );
			exception.printStackTrace();
		}
	}


	/**
	 * Dispose of the channel wrapper resources by clearing the monitor (if any) and disposing of
	 * of the messaging resources.
	 */
	public void dispose() {
		synchronized ( _eventLock ) {
			if ( _connectionListener != null ) {
				_channel.removeConnectionListener( _connectionListener );
				_connectionListener = null;
			}
			if ( _monitor != null ) {
				_monitor.clear();
			}
			_eventProxy = null;
			_messageCenter = null;
			_monitor = null;
			_channel = null;
		}
	}
}

