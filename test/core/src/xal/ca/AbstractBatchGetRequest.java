//
// AbstractBatchGetRequest.java
// xal
//
// Created by Tom Pelaia on 4/2/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.ca;

import xal.tools.messaging.MessageCenter;
import xal.tools.dispatch.DispatchQueue;

import java.util.*;


/** AbstractBatchGetRequest */
abstract public class AbstractBatchGetRequest<RecordType extends ChannelRecord> implements BatchConnectionRequestListener {
	/** message center for dispatching events */
	final private MessageCenter MESSAGE_CENTER;
	
	/** proxy which forwards events to registered listeners */
	final private BatchGetRequestListener<RecordType> EVENT_PROXY;
	
	/** set of channels for which to request batch operations */
	final private Set<Channel> CHANNELS;
	
	/** table of channel records keyed by channel */
	final private Map<Channel,RecordType> RECORDS;
	
	/** table of get request exceptions keyed by channel */
	final private Map<Channel,Exception> EXCEPTIONS;
	
	/** channels pending completion */
	final private Set<Channel> PENDING_CHANNELS;

	/** channels that are connected and pending the get request */
	final private Set<Channel> PENDING_CONNECTED_CHANNELS;

	/** serial queue on which channels are submitted for get requests */
	final private DispatchQueue GET_REQUEST_PROCESSING_QUEUE;

	/** indicates that pending connected channels are queued for processing */
	private volatile boolean _pendingChannelProcessingQueued;
	
	/** object used for waiting and notification */
	final private Object COMPLETION_LOCK;

	/** batch request for connecting to the pending channels */
	private BatchConnectionRequest _batchConnectionRequest;
	
	
	/** 
	 * Primary Constructor 
	 * @param channels the channels for which get requests will be handled
	 */
	@SuppressWarnings( "unchecked" )	// No way to pass BatchGetRequestListener.class with the specified RecordType
	public AbstractBatchGetRequest( final Collection<Channel> channels ) {
		MESSAGE_CENTER = new MessageCenter( "BatchGetRequest" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, BatchGetRequestListener.class );
		
		COMPLETION_LOCK = new Object();
		
		RECORDS = new HashMap<Channel,RecordType>();
		EXCEPTIONS = new HashMap<Channel,Exception>();
		PENDING_CHANNELS = new HashSet<Channel>();
		PENDING_CONNECTED_CHANNELS = new HashSet<Channel>();
		GET_REQUEST_PROCESSING_QUEUE = DispatchQueue.createSerialQueue( "Batch Get Request Processing" );

		_batchConnectionRequest = null;
		_pendingChannelProcessingQueued = false;

		CHANNELS = new HashSet<Channel>( channels.size() );
		CHANNELS.addAll( channels );
	}


	/** dispose of the executors */
	protected void finalize() throws Throwable {
		GET_REQUEST_PROCESSING_QUEUE.dispose();
		super.finalize();
	}

	
	/** 
	 * add the specified listener as a receiver of batch get request events from this instance 
	 * @param listener a receiver which will receive events
	 */
	public void addBatchGetRequestListener( final BatchGetRequestListener<RecordType> listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, BatchGetRequestListener.class );
	}
	
	
	/** 
	 * remove the specified listener from receiving batch get request events from this instance 
	 * @param listener receiver to remove from receiving events
	 */
	public void removeBatchGetRequestListener( final BatchGetRequestListener<RecordType> listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, BatchGetRequestListener.class );
	}
	
	
	/** 
	 * add a new channel to the batch request 
	 * @param channel a channel to add to the batch request
	 */
	public void addChannel( final Channel channel ) {
		synchronized ( CHANNELS ) {
			CHANNELS.add( channel );
		}
	}
	
	
	/** 
	 * get the collection of channels to process 
	 * @return a copy of the list of channels in the batch request
	 */
	public Collection<Channel> getChannels() {
		return copyChannels();
	}
	
	
	/** submit as a batch the get requests for each channel */
	synchronized public void submit() {
		final Set<Channel> channels = copyChannels();
		synchronized( PENDING_CHANNELS ) {
			PENDING_CHANNELS.clear();
			PENDING_CHANNELS.addAll( channels );
		}
		synchronized ( RECORDS ) {
			RECORDS.clear();
		}

		// dispose of the old batch channel connection request
		final BatchConnectionRequest oldBatchConnectionRequest = _batchConnectionRequest;
		if ( oldBatchConnectionRequest != null ) {
			oldBatchConnectionRequest.cancel();
			oldBatchConnectionRequest.removeBatchConnectionRequestListener( this );
		}

		// determine which channels are connected and process them immediately
		final Set<Channel> unconnectedChannels = new HashSet<Channel>();
		for ( final Channel channel : channels ) {
			if ( channel.isConnected() ) {
				PENDING_CONNECTED_CHANNELS.add( channel );
			}
			else {
				unconnectedChannels.add( channel );
			}
		}
		if ( PENDING_CONNECTED_CHANNELS.size() > 0 ) {
			processPendingConnectedChannels();
		}

		// create a fresh batch channel connection request for unconnected channels if any
		if ( unconnectedChannels.size() > 0 ) {
			final BatchConnectionRequest batchConnectionRequest = new BatchConnectionRequest( unconnectedChannels );
			_batchConnectionRequest = batchConnectionRequest;
			batchConnectionRequest.addBatchConnectionRequestListener( this );
			batchConnectionRequest.submit();
		}
	}
	
	
	/** 
	 * Submit a batch of get requests and wait for the requests to be completed or timeout.
	 * Note that if this is called, within a channel access callback, requests will not be processed until the 
	 * callback completes, so it is useless to wait. Instead, call waitForCompletion separately outside of the callback.
	 * @param timeout the maximum time in seconds to wait for completion
	 * @return true if complete or false if not
	 */
	public boolean submitAndWait( final double timeout ) {
		submit();
		return waitForCompletion( timeout );
	}


	/**
	 * Synonym for waitForCompletion. Wait up to the specified timeout for completion. This method should be called outside of a Channel Access callback
	 * otherwise events will not be processed.
	 * @param timeout the maximum time in seconds to wait for completion
	 * @return true if complete or false if not
	 */
	public boolean await( final double timeout ) {
		return waitForCompletion( timeout );
	}

	
	/** 
	 * Wait up to the specified timeout for completion. This method should be called outside of a Channel Access callback
	 * otherwise events will not be processed.
	 * @param timeout the maximum time in seconds to wait for completion
	 * @return true if complete or false if not
	 */
	public boolean waitForCompletion( final double timeout ) {
		final long milliTimeout = (long) ( 1000 * timeout );		// timeout in milliseconds
		final long maxTime = new Date().getTime() + milliTimeout;	// maximum time until expiration
		while( !isComplete() && new Date().getTime() < maxTime ) {
			final long remainingTime = Math.max( 0, maxTime - new Date().getTime() );
			if ( remainingTime > 0 ) {		// remaining time must be strictly greater than zero to prevent waiting forever should it be identically zero
				try {
					synchronized( COMPLETION_LOCK ) {
						COMPLETION_LOCK.wait( remainingTime );
					}
				}
				catch( Exception exception ) {
					throw new RuntimeException( "Exception waiting for the batch get requests to be completed.", exception );
				}
			}
		}
		
		return isComplete();
	}
	
	
	/** 
	 * Request to get the data for the channel 
	 * @param channel the channel for which to request data
	 * @throws Exception when the request fails
	 */
	abstract protected void requestChannelData( final Channel channel ) throws Exception;
	
	
	/** 
	 * Process the get request for a single channel 
	 * @param channel the channel for which to process the request
	 */
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
			EVENT_PROXY.exceptionInBatch( this, channel, exception );
			processCurrentStatus();
		}
	}
	
	
	/** 
	 * determine if there are any channels pending for either an exception or a completed get request 
	 * @return true if complete and false if not
	 */
	public boolean isComplete() {
		synchronized ( PENDING_CHANNELS ) {
			return PENDING_CHANNELS.isEmpty();
		}
	}
	
	
	/** 
	 * determine if there were any exceptions 
	 * @return true if there are any exceptions and false if not
	 */
	public boolean hasExceptions() {
		synchronized ( EXCEPTIONS ) {
			return !EXCEPTIONS.isEmpty();
		}
	}
	
	
	/** 
	 * get the number of records 
	 * @return the number of records
	 */
	public int getRecordCount() {
		synchronized ( RECORDS ) {
			return RECORDS.size();
		}
	}
	
	
	/** 
	 * Get the number of exceptions
	 * @return the number of channels for which there was an exception during the request
	 */
	public int getExceptionCount() {
		synchronized ( EXCEPTIONS ) {
			return EXCEPTIONS.size();
		}
	}
	
	
	/** 
	 * Get the record if any for the specified channel 
	 * @param channel the channel for which the record is fetched
	 * @return the record for the specified channel or null if there is none
	 */
	public RecordType getRecord( final Channel channel ) {
		synchronized ( RECORDS ) {
			return RECORDS.get( channel );
		}
	}
	
	
	/** 
	 * Get the exception if any for the specified channel 
	 * @param channel the channel for which the exception is fetched
	 * @return the exception for the specified channel or null if there is none
	 */
	public Exception getException( final Channel channel ) {
		synchronized ( EXCEPTIONS ) {
			return  EXCEPTIONS.get( channel );
		}
	}
	
	
	/** 
	 * Get the failed channels for which exceptions were thrown during the request
	 * @return the set of failed channels
	 */
	public Set<Channel> getFailedChannels() {
		synchronized ( EXCEPTIONS ) {
			return new HashSet<Channel>( EXCEPTIONS.keySet() );
		}
	}
	
	
	/** 
	 * Get the channels which produced a result 
	 * @return the set of channels each for which a record was successfully fetched
	 */
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


	/** 
	 * Process the receipt of a new record event 
	 * @param channel the channel for which the event will be processed
	 * @param record the fetched record
	 */
	protected void processRecordEvent( final Channel channel, final RecordType record ) {
		synchronized ( RECORDS ) {
			RECORDS.put( channel, record );
			synchronized( PENDING_CHANNELS ) {
				PENDING_CHANNELS.remove( channel );
			}
		}

		EVENT_PROXY.recordReceivedInBatch( this, channel, record );
		processCurrentStatus();
	}


	/** check for the current status and post notifications if necessary */
	protected void processCurrentStatus() {
		if ( isComplete() ) {
			synchronized( COMPLETION_LOCK ) {
				try {
					COMPLETION_LOCK.notifyAll();
				}
				catch( Exception exception ) {
					System.out.println( "Excepting notifying " );
					exception.printStackTrace();
				}
			}

			// once this batch get request is complete we can cancel the batch connection request
			final BatchConnectionRequest batchConnectionRequest = _batchConnectionRequest;
			if ( batchConnectionRequest != null ) {
				batchConnectionRequest.cancel();
			}
			
			EVENT_PROXY.batchRequestCompleted( this, getRecordCount(), getExceptionCount() );
		}
	}


	/** process any pending connected channels */
	private void processPendingConnectedChannels() {
		if ( !_pendingChannelProcessingQueued ) {		// flag allows pending connected channels to be accumulated so get requests can be submitted in batches
			_pendingChannelProcessingQueued = true;

			GET_REQUEST_PROCESSING_QUEUE.dispatchAsync( new Runnable() {
				public void run() {
					Thread.yield();		// yield to other threads so we can accumulate a batch of channels to process
					
					_pendingChannelProcessingQueued = false;

					final Set<Channel> channels = new HashSet<Channel>();
					synchronized( PENDING_CONNECTED_CHANNELS ) {
						channels.addAll( PENDING_CONNECTED_CHANNELS );
						PENDING_CONNECTED_CHANNELS.clear();
					}

//					System.out.println( "Processing " + channels.size() + " channels." );

					if ( channels.size() > 0 ) {
						try {
							for ( final Channel channel : channels ) {
								processRequest( channel );
							}
							Channel.flushIO();
							Thread.yield();		// yield to other threads so we can accumulate a batch of channels to process 
						}
						catch( Exception exception ) {
							exception.printStackTrace();
						}
					}
				}
			});
		}
	}


	/** event indicating that the batch request is complete */
	public void batchConnectionRequestCompleted( final BatchConnectionRequest connectionRequest, final int connectedCount, final int disconnectedCount, final int exceptionCount ) {}

	
	/** event indicating that an exception has been thrown for a channel */
	public void connectionExceptionInBatch( final BatchConnectionRequest connectionRequest, final Channel channel, final Exception exception ) {
		synchronized( EXCEPTIONS ) {
			EXCEPTIONS.put( channel, exception );
			synchronized ( PENDING_CHANNELS ) {
				PENDING_CHANNELS.remove( channel );
			}
		}
		final ConnectionException connectionException = new ConnectionException( channel, "Exception connecting to channel " + channel.channelName() + " during batch get request." );
		EVENT_PROXY.exceptionInBatch( this, channel, connectionException );
		processCurrentStatus();
	}


	/** event indicating that a connection change has occurred for a channel */
	public void connectionChangeInBatch( BatchConnectionRequest connectionRequest, Channel channel, boolean connected ) {
		synchronized( PENDING_CONNECTED_CHANNELS ) {
			if ( connected ) {
				PENDING_CONNECTED_CHANNELS.add( channel );
			}
			else {
				PENDING_CONNECTED_CHANNELS.remove( channel );
			}
		}

		if ( connected ) {
			processPendingConnectedChannels();
		}
	}
}
