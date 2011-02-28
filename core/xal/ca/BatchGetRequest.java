//
//  BatchGetRequest.java
//  xal
//
//  Created by Tom Pelaia on 2/22/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.ca;

import xal.tools.messaging.MessageCenter;

import java.util.*;
import java.util.concurrent.*;


/** batch of CA Get requests with convenient batch operations */
public class BatchGetRequest {
	/** message center for dispatching events */
	final protected MessageCenter MESSAGE_CENTER;
	
	/** proxy which forwards events to registered listeners */
	final protected BatchGetRequestListener EVENT_PROXY;
	
	/** set of channels for which to request batch operations */
	final protected Set<Channel> CHANNELS;
	
	/** request handler */
	final protected RequestHandler REQUEST_HANDLER;
	
	/** table of channel records keyed by channel */
	final protected Map<Channel,ChannelRecord> RECORDS;
	
	/** table of get request exceptions keyed by channel */
	final protected Map<Channel,Exception> EXCEPTIONS;
	
	/** channels pending completion */
	final protected Set<Channel> PENDING_CHANNELS;
	
	/** executor pool for processing the get requests */
	final protected ExecutorService PROCESSING_POOL;
	
	
	/** Primary Constructor */
	public BatchGetRequest( final Collection<Channel> channels ) {
		MESSAGE_CENTER = new MessageCenter( "BatchGetRequest" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, BatchGetRequestListener.class );
		
		REQUEST_HANDLER = new RequestHandler();
		RECORDS = new HashMap<Channel,ChannelRecord>();
		EXCEPTIONS = new HashMap<Channel,Exception>();
		PENDING_CHANNELS = new HashSet<Channel>();
		PROCESSING_POOL = Executors.newCachedThreadPool();
		
		CHANNELS = new HashSet<Channel>( channels.size() );
		CHANNELS.addAll( channels );
	}
	
	
	/** Constructor */
	public BatchGetRequest() {
		this( Collections.EMPTY_SET );
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
		final Collection<Callable<Channel>> tasks = new HashSet<Callable<Channel>>( channels.size() );
		for ( final Channel channel : channels ) {
			tasks.add( new Callable<Channel>() {
				public Channel call() {
					processRequest( channel );
					return channel;
				}
			});
		}
		try {
			PROCESSING_POOL.invokeAll( tasks );
			Channel.flushIO();
		}
		catch( Exception exception ) {
			throw new RuntimeException( "Exception while submitting a batch Get request.", exception );
		}
	}
	
	
	/** 
	 * Submit a batch of get requests and wait for the requests to be completed or timeout 
	 * @param timeout the maximum time in seconds to wait for completion
	 */
	public boolean submitAndWait( final double timeout ) {
		final long delay = (long) ( 1000 * Math.min( timeout, 0.1 ) );		// delay in milliseconds
		final long maxTime = new Date().getTime() + (long) ( 1000 * timeout );		// time after which we should stop waiting
		submit();
		while( !isComplete() && new Date().getTime() < maxTime ) {
			try {
				Thread.sleep( delay );
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
				channel.getValueCallback( REQUEST_HANDLER, false );
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
	public ChannelRecord getRecord( final Channel channel ) {
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
			EVENT_PROXY.batchRequestCompleted( this, getRecordCount(), getExceptionCount() );
		}
	}
	
	
	
	/** handle get request events */
	protected class RequestHandler implements IEventSinkValue {
		public void eventValue( final ChannelRecord record, final Channel channel ) {
			synchronized ( RECORDS ) {
				RECORDS.put( channel, record );
				synchronized( PENDING_CHANNELS ) {
					PENDING_CHANNELS.remove( channel );
				}
			}
			EVENT_PROXY.recordReceivedInBatch( BatchGetRequest.this, channel, record ); 
			processCurrentStatus();
		}
	}
}
