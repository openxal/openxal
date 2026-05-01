//
// ScanChannelMonitor.java
// xal
//
// Created by Pelaia II, Tom on 1/10/13
// Copyright 2013 ORNL. All rights reserved.
//

package xal.extension.scan;

import xal.ca.*;


/** ScanChannelMonitor */
public class ScanChannelMonitor {
	/** synchronization lock */
	final private Object SYNC_LOCK;

	/** channel to monitor */
	final private Channel CHANNEL;

	/** handler of events */
	final private EventHandler EVENT_HANDLER;

	/** channel event monitor */
	private Monitor _monitor;

	/** indicates whether this wrapper is viable or disposed */
	volatile private boolean _viable;

	/** indicates whether the monitor is allowed */
	volatile private boolean _allowsMonitor;

	/** handler of the monitor events */
	volatile private ScanChannelMonitorDelegate _delegate;

	/** latest record captured */
	volatile private ChannelTimeRecord _latestRecord;


	/** Constructor with null delegate */
	public ScanChannelMonitor( final Channel channel ) {
		this( channel, null );
	}


	/** Constructor */
	public ScanChannelMonitor( final Channel channel, final ScanChannelMonitorDelegate delegate ) {
		this( channel, delegate, true );
	}


	/** Primary Constructor */
	public ScanChannelMonitor( final Channel channel, final ScanChannelMonitorDelegate delegate, final boolean requestEvents ) {
		SYNC_LOCK = new Object();
		EVENT_HANDLER = new EventHandler();

		_viable = true;

		_allowsMonitor = requestEvents;

		_latestRecord = null;
		_monitor = null;
		_delegate = delegate;

		if ( channel == null )	throw new IllegalArgumentException( "Channel Wrapper cannot be assigned a null channel" );

		CHANNEL = channel;

		if ( requestEvents ) {
			start();
		}
	}


	/** Get the channel */
	public Channel getChannel() {
		return CHANNEL;
	}


	/** set the delegate */
	public void setDelegate( final ScanChannelMonitorDelegate delegate ) {
		_delegate = delegate;
	}


	/** determine whether the channel is connected */
	public boolean isConnected() {
		return CHANNEL.isConnected();
	}


	/** determine whether the channel is valid (has a record and is connected) */
	public boolean isValid() {
		return _latestRecord != null && CHANNEL.isConnected();
	}


	/** Get the latest record */
	public ChannelTimeRecord getLatestRecord() {
		return _latestRecord;
	}


	/** stop the monitor */
	public void stop() {
		synchronized( SYNC_LOCK	) {
			CHANNEL.removeConnectionListener( EVENT_HANDLER );
			_allowsMonitor = false;

			final Monitor monitor = _monitor;
			_monitor = null;
			if ( monitor != null )  monitor.clear();
		}
	}


	/** start the monitor */
	public void start() {
		synchronized( SYNC_LOCK	) {
			_allowsMonitor = true;
			CHANNEL.addConnectionListener( EVENT_HANDLER );
			CHANNEL.requestConnection();
		}
	}


	/** start the monitor */
	public void createMonitor() {
		synchronized( SYNC_LOCK	) {
			if ( _allowsMonitor && _viable && _monitor == null ) {
				try {
					_monitor = CHANNEL.addMonitorValTime( EVENT_HANDLER, Monitor.VALUE );
					Channel.flushIO();
				}
				catch( Exception exception ) {
					System.err.println( "Exception creating monitor for channel: " + CHANNEL.getId() );
					exception.printStackTrace();
				}
			}
		}
	}


	/** Dispose of the channel */
	public void dispose() {
		synchronized( SYNC_LOCK ) {
			_viable = false;
			_delegate = null;

			stop();
		}
	}


	/** process events */
	private class EventHandler implements ConnectionListener, IEventSinkValTime {
		/**
		 * Indicates that a connection to the specified channel has been established.
		 * @param channel The channel which has been connected.
		 */
		public void connectionMade( final Channel channel ) {
			createMonitor();

			final ScanChannelMonitorDelegate delegate = _delegate;
			if ( delegate != null ) {
				delegate.channelStateChanged( ScanChannelMonitor.this, _latestRecord != null );
			}
		}


		/**
		 * Indicates that a connection to the specified channel has been dropped.
		 * @param channel The channel which has been disconnected.
		 */
		public void connectionDropped( final Channel channel ) {
			final ScanChannelMonitorDelegate delegate = _delegate;
			if ( delegate != null ) {
				delegate.channelStateChanged( ScanChannelMonitor.this, false );
			}
		}


		/** Handle monitor event */
		public void eventValue( final ChannelTimeRecord record, final Channel channel ) {
			_latestRecord = record;
			//System.out.println( "Captured record: " + record );

			final ScanChannelMonitorDelegate delegate = _delegate;
			if ( delegate != null ) {
				delegate.channelRecordUpdate( ScanChannelMonitor.this, record );		// forward the event to the delegate
			}
		}
	}
}
