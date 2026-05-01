//
// BatchChannelSyncRequest.java
// xal
//
// Created by Tom Pelaia on 4/18/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.service.errantbeamcapture;

import java.util.*;

import xal.ca.*;
import xal.tools.dispatch.DispatchQueue;


/** Monitor a batch of channels and gather records which occur within the specified tolerance of the timestamp */
public class BatchChannelSyncRequest {
	/** timestamp against which record timestamps are compared */
	final private Timestamp REFERENCE_TIMESTAMP;
	
	/** tolerance (seconds) range within which the record timestamps must match */
	final private double[] TIMESTAMP_TOLERANCE;
	
	/** request handler */
	final private RequestHandler REQUEST_HANDLER;

	/** set of channels for which to request batch operations */
	final private Set<Channel> CHANNELS;
	
	/** table of channel records keyed by channel */
	final private Map<Channel,ChannelTimeRecord> RECORDS;
	
	/** table of get request exceptions keyed by channel */
	final private Map<Channel,Exception> EXCEPTIONS;
	
	/** table of monitors keyed by channel */
	final private Map<Channel,Monitor> MONITORS;
	
	/** channels pending completion */
	final private Set<Channel> PENDING_CHANNELS;
	
	/** executor pool for processing the get requests */
	final protected DispatchQueue PROCESSING_QUEUE;
	
	/** lock to wait for and notify upon completion */
	final private Object COMPLETION_LOCK;

	
	/** 
	 * Constructor 
	 * @param channels the channels to monitor and gather records
	 * @param timestamp the timestamp to which to compare record timestamps
	 * @param timestampTolerance the tolerance (seconds) range within which record timestamps must match the the specified timestamp
	 */
    public BatchChannelSyncRequest( final Collection<Channel> channels, final Timestamp timestamp, final double[] timestampTolerance ) {
		COMPLETION_LOCK = new Object();
		
		REFERENCE_TIMESTAMP = timestamp;
		TIMESTAMP_TOLERANCE = timestampTolerance;
				
		PROCESSING_QUEUE = DispatchQueue.createConcurrentQueue( "Batch Sync Request Processing" );

		MONITORS = new HashMap<Channel,Monitor>();
		RECORDS = new HashMap<Channel,ChannelTimeRecord>();
		EXCEPTIONS = new HashMap<Channel,Exception>();
		PENDING_CHANNELS = new HashSet<Channel>();
		
		CHANNELS = new HashSet<Channel>( channels.size() );
		CHANNELS.addAll( channels );
		
		REQUEST_HANDLER = new RequestHandler();
    }


	/** dispose of the queue */
	protected void finalize() throws Throwable {
		dispose();
	}

	
	/** dispose of the resources used by this instance (e.g. any remaining monitors) */
	public void dispose() {
		clearMonitors();
		PROCESSING_QUEUE.dispose();
	}
	
	
	/** clear monitors */
	private void clearMonitors() {
		synchronized( MONITORS ) {
			for ( final Monitor monitor : MONITORS.values() ) {
				monitor.clear();
			}
			MONITORS.clear();
		}
	}
	
	
	/** add a new channel to the batch request */
	public void addChannel( final Channel channel ) {
		synchronized ( CHANNELS ) {
			CHANNELS.add( channel );
		}
	}
	
	
	/** get the collection of channels to process */
	public Collection<Channel> getChannels() {
		return copyChannels();
	}
	
	
	/** submit as a batch the get requests for each channel */
	synchronized public void submit() {
		final Set<Channel> channels = copyChannels();
		
		clearMonitors();

		synchronized( PENDING_CHANNELS ) {
			PENDING_CHANNELS.clear();
			PENDING_CHANNELS.addAll( channels );
		}
		synchronized ( RECORDS ) {
			RECORDS.clear();
		}		
		
		try {
			for ( final Channel channel : channels ) {
				processRequest( channel );
			}
			Channel.flushIO();
		}
		catch( Exception exception ) {
			throw new RuntimeException( "Exception while submitting a batch Get request.", exception );
		}
	}
	
	
	/** 
	 * Submit a batch of get requests and wait for the requests to be completed or timeout.
	 * Note that if this is called, within a channel access callback, requests will not be processed until the 
	 * callback completes, so it is useless to wait. Instead, call waitForCompletion separately outside of the callback.
	 * @param timeout the maximum time in seconds to wait for completion
	 */
	public boolean submitAndWait( final double timeout ) {
		submit();
		return waitForCompletion( timeout );
	}
	
	
	/** 
	 * Wait up to the specified timeout for completion. This method should be called outside of a Channel Access callback
	 * otherwise events will not be processed.
	 * @param timeout the maximum time in seconds to wait for completion
	 */
	public boolean waitForCompletion( final double timeout ) {
		final long milliTimeout = (long) ( 1000 * timeout );			// timeout in milliseconds
		final long maxTime = new Date().getTime() + milliTimeout;		// time after which we should stop waiting
		while( !isComplete() && new Date().getTime() < maxTime ) {		// loop in case completion lock is released early
			try {
				synchronized( COMPLETION_LOCK ) {
					COMPLETION_LOCK.wait( milliTimeout );
				}
			}
			catch( Exception exception ) {
				throw new RuntimeException( "Exception waiting for the batch get requests to be completed.", exception );
			}
		}
		
		return isComplete();
	}
	
	
	/** process the get request for a single channel */
	protected void processRequest( final Channel channel ) {
		try {
			if ( channel.isConnected() ) {
				requestChannelData( channel );
			}
			else {
				throw new ConnectionException( channel, "Exception connecting to channel " + channel.channelName() + " during batch get request." );
			}
		}
		catch ( Exception exception ) {
			synchronized( EXCEPTIONS ) {
				EXCEPTIONS.put( channel, exception );
				synchronized ( PENDING_CHANNELS ) {
					PENDING_CHANNELS.remove( channel );
				}
			}
		}
	}
	
	
	/** determine if there are any channels pending for either an exception or a completed get request */
	public boolean isComplete() {
		synchronized ( PENDING_CHANNELS ) {
			return PENDING_CHANNELS.isEmpty();
		}
	}
	
	
	/** determine if there were any exceptions */
	public boolean hasExceptions() {
		synchronized ( EXCEPTIONS ) {
			return !EXCEPTIONS.isEmpty();
		}
	}
	
	
	/** get the number of records */
	public int getRecordCount() {
		synchronized ( RECORDS ) {
			return RECORDS.size();
		}
	}
	
	
	/** get the number of exceptions */
	public int getExceptionCount() {
		synchronized ( EXCEPTIONS ) {
			return EXCEPTIONS.size();
		}
	}
	
	
	/** get the record if any for the specified channel */
	public ChannelTimeRecord getRecord( final Channel channel ) {
		synchronized ( RECORDS ) {
			return RECORDS.get( channel );
		}
	}
	
	
	/** get the exception if any for the specified channel */
	public Exception getException( final Channel channel ) {
		synchronized ( EXCEPTIONS ) {
			return  EXCEPTIONS.get( channel );
		}
	}
	
	
	/** get the failed channels */
	public Set<Channel> getFailedChannels() {
		synchronized ( EXCEPTIONS ) {
			return new HashSet<Channel>( EXCEPTIONS.keySet() );
		}
	}
	
	
	/** get the channels which produced a result */
	public Set<Channel> getResultChannels() {
		synchronized( RECORDS ) {
			return new HashSet<Channel>( RECORDS.keySet() );
		}
	}
	
	
	/** copy channels to a new set */
	private Set<Channel> copyChannels() {
		synchronized ( CHANNELS ) {
			return new HashSet<Channel>( CHANNELS );
		}
	}
	
	
	/** request to get the data for the channel */
	protected void requestChannelData( final Channel channel ) throws Exception {
		final Monitor monitor = channel.addMonitorValTime( REQUEST_HANDLER, Monitor.VALUE );
		synchronized( MONITORS ) {
			MONITORS.put( channel, monitor );
		}
	}
	
	
	/** handle get request events */
	protected class RequestHandler implements IEventSinkValTime {
		public void eventValue( final ChannelTimeRecord record, final Channel channel ) {
			final Timestamp timestamp = record.getTimestamp();
			final double timestampOffset = timestamp.getSeconds() - REFERENCE_TIMESTAMP.getSeconds();
			if ( timestampOffset >= TIMESTAMP_TOLERANCE[0] && timestampOffset <= TIMESTAMP_TOLERANCE[1] ) {
				synchronized ( RECORDS ) {
					RECORDS.put( channel, record );
					synchronized( PENDING_CHANNELS ) {
						PENDING_CHANNELS.remove( channel );
					}
				}
				
				Monitor monitor = null;
				synchronized ( MONITORS ) {
					monitor = MONITORS.remove( channel );
				}
				
				// clear the monitor in a thread outside of the monitor event
				if ( monitor != null ) {
					final Monitor localMonitor = monitor;		// need a local copy that is explicitly declared final
					PROCESSING_QUEUE.dispatchAsync( new Runnable() {
						public void run() {
							localMonitor.clear();
						}
					});
				}
				
				if ( isComplete() ) {
					synchronized( COMPLETION_LOCK ) {
						try {
							COMPLETION_LOCK.notifyAll();
						}
						catch( Exception exception ) {
							System.err.println( "Exception notifying threads waiting on batch completion" );
							exception.printStackTrace();
						}
					}
				}
			}
		}
	}
}
