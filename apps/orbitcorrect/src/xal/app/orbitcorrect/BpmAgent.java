/*
 *  BpmAgent.java
 *
 *  Created on Wed Jan 07 16:54:59 EST 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.smf.*;
import xal.smf.impl.BPM;
import xal.ca.*;
import xal.ca.correlator.*;
import xal.tools.correlator.*;
import xal.tools.messaging.MessageCenter;

import java.util.*;
import java.util.concurrent.*;


/**
 * BpmAgent represents a live BPM which may be connected and monitored.
 * @author   tap
 * @since    Jan 7, 2004
 */
public class BpmAgent extends BeamMarker<BPM> implements RepRateListener {
	/** default time window in seconds for correlating this BPM's signal events */
	public final static double DEFAULT_CORRELATION_WINDOW = 0.1;
	
	/** default amplitude threshold below which this BPM signal is filtered out */
	protected final static double DEFAULT_AMPLITUDE_THRESHOLD = 10.0;

	/** event message center */
	final protected MessageCenter MESSAGE_CENTER;
	
	/** proxy for posting channel events */
	final protected BpmEventListener EVENT_PROXY;
    
	/** synchronize events dispatch to a single queue */
    final private ExecutorService EVENT_QUEUE;
    
	/** indicates whether this BPM is enabled for orbit display */
	protected boolean _enabled;
	
	/** indicates whether this BPM is enabled for flattening */
	private boolean _flattenEnabled;
	
	/** channel for xAvg */
	protected Channel _xAvgChannel;
	
	/** channel for yAvg */
	protected Channel _yAvgChannel;
	
	/** channel for amplitude average */
	protected Channel _ampAvgChannel;

	/** map of channels keyed by the corresponding BPM handles */
	protected Map<String,Channel> _channelTable;

	/** signal correlator */
	protected ChannelCorrelator _correlator;

	/** last record */
	protected volatile BpmRecord _lastRecord;
    

	/**
	 * Primary constructor
	 * @param bpm                the BPM to monitor
	 * @param correlationWindow  the time in seconds for resolving correlated signals
	 */
	public BpmAgent( final BPM bpm, final double correlationWindow ) {
		super( bpm );
		
		MESSAGE_CENTER = new MessageCenter();
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, BpmEventListener.class );
        EVENT_QUEUE = Executors.newSingleThreadExecutor();
        
		_lastRecord = null;
		
		_enabled = true;
		_flattenEnabled = _enabled;

		if ( isAvailable() ) {
			monitorSignals( correlationWindow );
		}
	}


	/**
	 * Constructor
	 * @param bpm  the BPM to monitor
	 */
	public BpmAgent( final BPM bpm ) {
		this( bpm, DEFAULT_CORRELATION_WINDOW );
	}
	
	
	/**
	 * Get the BPM ID.
	 * @return the unique BPM ID
	 */
	public String getID() {
		return NODE.getId();
	}


	/**
	 * Add the specified listener as a receiver of BPM events
	 * @param listener  the listener to receive BPM events
	 */
	public void addBpmEventListener( final BpmEventListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, BpmEventListener.class );
        
        EVENT_QUEUE.submit( new Runnable() {
            public void run() {
                listener.connectionChanged( BpmAgent.this, BPM.X_AVG_HANDLE, _xAvgChannel.isConnected() );
                listener.connectionChanged( BpmAgent.this, BPM.Y_AVG_HANDLE, _yAvgChannel.isConnected() );
                listener.connectionChanged( BpmAgent.this, BPM.AMP_AVG_HANDLE, _ampAvgChannel.isConnected() );
                                
                final BpmRecord lastRecord = _lastRecord;
                if ( lastRecord != null ) {
                    listener.stateChanged( BpmAgent.this, lastRecord );
                }
            }
        });
	}


	/**
	 * Remove the specified listener from receiving BPM events
	 * @param listener  the listener to remove from receiving BPM events
	 */
	public void removeBpmEventListener( final BpmEventListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, BpmEventListener.class );
	}


	/**
	 * Get the BPM manage by this agent
	 * @return   the BPM managed by this agent
	 */
	public BPM getBPM() {
		return NODE;
	}
	
	
	/** set whether this BPM is enabled for orbit display */
	public void setEnabled( final boolean enabled ) {
		_enabled = enabled;
	}
	
	
	/** determine if this BPM is enabled for orbit display */
	public boolean isEnabled() {
		return _enabled;
	}
	
	
	/** set whether this BPM is enabled for flattening */
	public void setFlattenEnabled( final boolean enabled ) {
		_flattenEnabled = enabled;
	}
	
	
	/** determine whether this BPM is enabled for flattening */
	public boolean getFlattenEnabled() {
		return _flattenEnabled;
	}


	/**
	 * Determine if this BPM is valid and has a good status.
	 * @return   true if this BPM has a good status and is valid; false otherwise.
	 */
	public boolean isAvailable() {
		return NODE.getStatus() && NODE.getValid();
	}


	/**
	 * Determine if this BPM's channels are all connected.
	 * @return   true if this BPM is connected and false if not.
	 */
	public boolean isConnected() {
		try {
			return isChannelConnectedIfValid( _xAvgChannel ) && isChannelConnectedIfValid( _yAvgChannel ) && isChannelConnectedIfValid( _ampAvgChannel );
		}
		catch ( NullPointerException exception ) {
			return false;
		}
	}


	/** Check whether valid channels are connected. If the channel is invalid, then just return true since it doesn't need to be connected. */
	private boolean isChannelConnectedIfValid( final Channel channel ) {
		return channel.isValid() ? channel.isConnected() : true;
	}


	/**
	 * Determine if this BPM is available and all of its channels are connected.
	 * @return   true if this BPM is online and false if not.
	 */
	public boolean isOnline() {
		// if all the channels are connected then the BPM must be available.
		return isConnected();
	}
	
	
	/**
	 * Get the latest record.
	 * @return the latest BPM record
	 */
	public BpmRecord getLatestRecord() {
		return _lastRecord;
	}
	

	/**
	 * Get the position of the BPM relative to the start of the specified sequence.
	 * @param sequence  The sequence relative to which the BPM's position is measured
	 * @return          the position of the BPM relative to the sequence in meters
	 */
	public double getPositionIn( AcceleratorSeq sequence ) {
		return sequence.getPosition( NODE );
	}


	/**
	 * Get the string representation of the BPM.
	 * @return   the BPM's string representation
	 */
	public String toString() {
		return NODE.toString();
	}


	/** Note the channel's validity */
	static private void noteChannelValidity( final Channel channel ) {
		if ( !channel.isValid() ) {
			System.out.println( channel.channelName() + " is marked invalid. Will ignore this channel." );
		}
	}


	/**
	 * Monitor and correlated the xAvg, yAvg and ampAvg signals for the BPM.
	 * @param binTimespan  the timespan for the correlation bin
	 */
	protected void monitorSignals( double binTimespan ) {
		if ( _correlator != null ) {
			_correlator.dispose();
		}

		_correlator = new ChannelCorrelator( binTimespan );

		_channelTable = new Hashtable<String,Channel>( 3 );
		_xAvgChannel = monitorChannel( BPM.X_AVG_HANDLE );
		_yAvgChannel = monitorChannel( BPM.Y_AVG_HANDLE );
		_ampAvgChannel = monitorChannel( BPM.AMP_AVG_HANDLE, null );

		noteChannelValidity( _xAvgChannel );
		noteChannelValidity( _yAvgChannel );
		noteChannelValidity( _ampAvgChannel );

		_correlator.addListener( new CorrelationNotice<ChannelTimeRecord>() {
			final String X_AVG_ID = _xAvgChannel.getId();
			final String Y_AVG_ID = _xAvgChannel.getId();
			final String AMP_AVG_ID = _ampAvgChannel.getId();

			final boolean xAvgChannelValid = _xAvgChannel.isValid();
			final boolean yAvgChannelValid = _yAvgChannel.isValid();
			final boolean ampAvgChannelValid = _ampAvgChannel.isValid();
			

			/**
			 * Handle the correlation event. This method gets called when a correlation was posted.
			 * @param sender       The poster of the correlation event.
			 * @param correlation  The correlation that was posted.
			 */
			public void newCorrelation( Object sender, Correlation<ChannelTimeRecord> correlation ) {
				final Date timestamp = correlation.meanDate();

				// post a BPM record if all valid channels in the BPM have an entry in the correlation, otherwise ignore this correlation
				
				final double xAvg = getValue( BPM.X_AVG_HANDLE, correlation );
				if ( xAvgChannelValid && !correlation.isCorrelated( X_AVG_ID ) ) {
					return;
				}
				
				final double yAvg = getValue( BPM.Y_AVG_HANDLE, correlation );
				if ( yAvgChannelValid && !correlation.isCorrelated( Y_AVG_ID ) ) {
					return;
				}
				
				final double ampAvg = getValue( BPM.AMP_AVG_HANDLE, correlation );
				if ( ampAvgChannelValid && !correlation.isCorrelated( AMP_AVG_ID ) ) {
					return;
				}
				
				final BpmRecord record = new BpmRecord( BpmAgent.this, timestamp, xAvg, yAvg, ampAvg );
				EVENT_QUEUE.submit( new Runnable() {
					public void run() {
						_lastRecord = record;
						EVENT_PROXY.stateChanged( BpmAgent.this, record );
					}
				});
			}


			/**
			 * Handle the no correlation event. This method gets called when no correlation was found within some prescribed time period.
			 * @param sender  The poster of the "no correlation" event.
			 */
			public void noCorrelationCaught( Object sender ) {
				System.out.println( "No BPM event." );
			}


			/**
			 * Get the value for the specified field from the correlation.
			 * @param handle       the handle of the BPM field
			 * @param correlation  the correlation with the correlated data for the BPM event
			 * @return             the correlation's BPM field value corresponding to the handle
			 */
			private double getValue( final String handle, final Correlation<ChannelTimeRecord> correlation ) {
				final Channel channel = getChannel( handle );
				if ( channel.isValid() ) {
					final String channelID = channel.getId();
					final ChannelTimeRecord record = correlation.getRecord( channelID );
					return ( record != null ) ? record.doubleValue() : Double.NaN;
				}
				else {
					return Double.NaN;
				}
			}
		} );

		_correlator.startMonitoring();
	}


	/**
	 * Connect to the channel and monitor it with the correlator.
	 * @param handle  the handle of the channel to monitor with the correlator.
	 * @param filter  the channel's record filter for the correlation.
	 * @return        the channel for which the monitor was requested
	 */
	protected Channel monitorChannel( final String handle, final RecordFilter<ChannelTimeRecord> filter ) {
		Channel channel = NODE.getChannel( handle );
		_channelTable.put( handle, channel );

		// only monitor the channel if the channel is marked valid
		if ( channel.isValid() ) {
			correlateSignal( channel, filter );

			channel.addConnectionListener(
				new ConnectionListener() {
					/**
					 * Indicates that a connection to the specified channel has been established.
					 * @param channel  The channel which has been connected.
					 */
					public void connectionMade( final Channel channel ) {
						EVENT_QUEUE.submit( new Runnable() {
							public void run() {
								_lastRecord = null;
								correlateSignal( channel, filter );
								EVENT_PROXY.connectionChanged( BpmAgent.this, handle, true );
							}
						});
					}


					/**
					 * Indicates that a connection to the specified channel has been dropped.
					 * @param channel  The channel which has been disconnected.
					 */
					public void connectionDropped( final Channel channel ) {
						EVENT_QUEUE.submit( new Runnable() {
							public void run() {
								_lastRecord = null;
								EVENT_PROXY.connectionChanged( BpmAgent.this, handle, false );
							}
						});
					}
				} );

			if ( !channel.isConnected() ) {
				channel.requestConnection();
			}
		}

		return channel;
	}


	/**
	 * Connect to the channel and monitor it with the correlator. A null record filter is used for the channel.
	 * @param handle  the handle of the channel to monitor with the correlator.
	 * @return        the channel for which the monitor was requested
	 */
	protected Channel monitorChannel( final String handle ) {
		return monitorChannel( handle, null );
	}


	/**
	 * Monitor the channel with the correlator.
	 * @param channel  the channel to monitor with the correlator.
	 * @param filter   the channel's record filter for the correlation.
	 */
	protected void correlateSignal( final Channel channel, final RecordFilter<ChannelTimeRecord> filter ) {
		if ( !_correlator.hasSource( channel.getId() ) && channel.isConnected() ) {
			_correlator.addChannel( channel, filter );
		}
	}


	/**
	 * Get this agent's channel corresponding to the specified handle.
	 * @param handle  Description of the Parameter
	 * @return        The channel value
	 */
	public Channel getChannel( final String handle ) {
		return _channelTable.get( handle );
	}


	/**
	 * Notification that the rep-rate has changed.
	 * @param monitor  The monitor announcing the new rep-rate.
	 * @param repRate  The new rep-rate.
	 */
	public void repRateChanged( RepRateMonitor monitor, double repRate ) {
		if ( _correlator != null ) {
			// if repRate is undefined or outside the expected range, revert to default otherwise
			// make the time window half of the rep-rate
			double timeWindow = ( !Double.isNaN( repRate ) && ( repRate > 0 ) && ( repRate < 10000 ) ) ? 0.5 / repRate : DEFAULT_CORRELATION_WINDOW;
			_correlator.setBinTimespan( timeWindow );
		}
	}
}

