//
// AbstractBatchGetRequest.java
// xal
//
// Created by Tom Pelaia on 4/2/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.ca;

import xal.tools.messaging.MessageCenter;

import java.util.*;
import java.util.concurrent.*;


/** AbstractBatchGetRequest */
abstract public class AbstractBatchGetRequest<RecordType extends ChannelRecord> {
	/** message center for dispatching events */
	final protected MessageCenter MESSAGE_CENTER;
	
	/** proxy which forwards events to registered listeners */
	final protected BatchGetRequestListener EVENT_PROXY;
	
	/** set of channels for which to request batch operations */
	final protected Set<Channel> CHANNELS;
	
	/** table of channel records keyed by channel */
	final protected Map<Channel,RecordType> RECORDS;
	
	/** table of get request exceptions keyed by channel */
	final protected Map<Channel,Exception> EXCEPTIONS;
	
	/** channels pending completion */
	final protected Set<Channel> PENDING_CHANNELS;
	
	/** executor pool for processing the get requests */
	final protected ExecutorService PROCESSING_POOL;
	
	/** object used for waiting and notification */
	final private Object COMPLETION_LOCK;
	
	
	/** Primary Constructor */
	public AbstractBatchGetRequest( final Collection<Channel> channels ) {
		MESSAGE_CENTER = new MessageCenter( "BatchGetRequest" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, BatchGetRequestListener.class );
		
		COMPLETION_LOCK = new Object();
		
		RECORDS = new HashMap<Channel,RecordType>();
		EXCEPTIONS = new HashMap<Channel,Exception>();
		PENDING_CHANNELS = new HashSet<Channel>();
		PROCESSING_POOL = Executors.newCachedThreadPool();
		
		CHANNELS = new HashSet<Channel>( channels.size() );
		CHANNELS.addAll( channels );
	}
	
	
	/** add the specified listener as a receiver of batch get request events from this instance */
	public void addBatchGetRequestListener( final BatchGetRequestListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, BatchGetRequestListener.class );
	}
	
	
	/** remove the specified listener from receiving batch get request events from this instance */
	public void removeBatchGetRequestListener( final BatchGetRequestListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, BatchGetRequestListener.class );
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
		final long milliTimeout = (long) ( 1000 * timeout );		// timeout in milliseconds
		final long maxTime = new Date().getTime() + milliTimeout;	// maximum time until expiration
		while( !isComplete() && new Date().getTime() < maxTime ) {
			final long remainingTime = Math.max( 0, maxTime - new Date().getTime() );
			try {
				synchronized( COMPLETION_LOCK ) {
					COMPLETION_LOCK.wait( remainingTime );
				}
			}
			catch( Exception exception ) {
				throw new RuntimeException( "Exception waiting for the batch get requests to be completed.", exception );
			}
		}
		
		return isComplete();
	}
	
	
	/** request to get the data for the channel */
	abstract protected void requestChannelData( final Channel channel ) throws Exception;
	
	
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
			EVENT_PROXY.exceptionInBatch( this, channel, exception );
			processCurrentStatus();
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
	public RecordType getRecord( final Channel channel ) {
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
			EVENT_PROXY.batchRequestCompleted( this, getRecordCount(), getExceptionCount() );
		}
	}
}
