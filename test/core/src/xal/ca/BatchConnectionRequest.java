//
// BatchConnectionRequest.java
// xal
//
// Created by Tom Pelaia on 6/14/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.ca;

import xal.tools.messaging.*;
import xal.tools.dispatch.*;

import java.util.*;
import java.util.concurrent.Callable;


/** BatchConnectionRequest */
public class BatchConnectionRequest extends java.lang.Object {
	/** time (msec) to periodically watch for status when waiting for connections */
	static final private long WATCH_TIME = 100;
	
	/** message center for dispatching events */
	final private MessageCenter MESSAGE_CENTER;
	
	/** proxy which forwards events to registered listeners */
	final private BatchConnectionRequestListener EVENT_PROXY;
	
	/** set of channels for which to request batch operations */
	final private Set<Channel> CHANNELS;
	
	/** table of get request exceptions keyed by channel */
	final private Map<Channel,Exception> EXCEPTIONS;
	
	/** channels pending completion */
	final private Set<Channel> PENDING_CHANNELS;
	
	/** channels that have been connected */
	final private Set<Channel> CONNECTED_CHANNELS;
	
	/** channels that have been connected */
	final private Set<Channel> DISCONNECTED_CHANNELS;
	
	/** object used for waiting and notification */
	final private Object COMPLETION_LOCK;
	
	/** queue to synchronize access to resources */
	final private DispatchQueue RESOURCE_SYNC_QUEUE;
	
	/** request handler */
	final private RequestHandler REQUEST_HANDLER;
	
	/** indicates whether this request has been canceled */
	private volatile boolean _isCanceled;

	
	/** 
	 * Constructor 
	 * @param channels for which the connections will be requrested
	 */
    public BatchConnectionRequest( final Collection<Channel> channels ) {
		MESSAGE_CENTER = new MessageCenter( "BatchConnectionRequest" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, BatchConnectionRequestListener.class );
		
		COMPLETION_LOCK = new Object();
		RESOURCE_SYNC_QUEUE = DispatchQueue.createConcurrentQueue( "Resource Synchronization" );
		
		CONNECTED_CHANNELS = new HashSet<Channel>();
		DISCONNECTED_CHANNELS = new HashSet<Channel>();
		EXCEPTIONS = new HashMap<Channel,Exception>();
		PENDING_CHANNELS = new HashSet<Channel>();
		
		CHANNELS = new HashSet<Channel>( channels.size() );
		CHANNELS.addAll( channels );
		
		REQUEST_HANDLER = new RequestHandler();
    }


	/** dispose of the queue */
	protected void finalize() throws Throwable {
		try {
			RESOURCE_SYNC_QUEUE.dispose();
		}
		finally {
			super.finalize();
		}
	}
	
	
	/** 
	 * add the specified listener as a receiver of batch connection request events from this instance 
	 * @param listener to receive connection events
	 */
	public void addBatchConnectionRequestListener( final BatchConnectionRequestListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, BatchConnectionRequestListener.class );
	}
	
	
	/** 
	 * remove the specified listener from receiving batch connection request events from this instance 
	 * @param listener to remove from receiving connection events
	 */
	public void removeBatchConnectionRequestListener( final BatchConnectionRequestListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, BatchConnectionRequestListener.class );
	}
	
	
	/** 
	 * Get a copy of the channels to connect
	 * @return channels for which connections are requested
	 */
	public Set<Channel> getChannels() {
		return Collections.unmodifiableSet( CHANNELS );
	}
	
	
	/** 
	 * Get the number of channels requested 
	 * @return the number of channels for which connections are requested
	 */
	public int getChannelCount() {
		return RESOURCE_SYNC_QUEUE.dispatchSync( new Callable<Integer>() {
			public Integer call() {
				return CHANNELS.size();
			}
		});
	}
	
	
	/** 
	 * Get the channels that were connected 
	 * @return set of connected channels
	 */
	public Set<Channel> getConnectedChannels() {
		return RESOURCE_SYNC_QUEUE.dispatchSync( new Callable<HashSet<Channel>>() {
			public HashSet<Channel> call() {
				return new HashSet<Channel>( CONNECTED_CHANNELS );
			}
		});
	}
	
	
	/** 
	 * Get the number of channels that were connected
	 * @return the number of channels that were connected
	 */
	public int getConnectedCount() {
		return RESOURCE_SYNC_QUEUE.dispatchSync( new Callable<Integer>() {
			public Integer call() {
				return CONNECTED_CHANNELS.size();
			}
		});
	}
	
	
	/** 
	 * Get the channels that were connected 
	 * @return set of channels that were connected
	 */
	public Set<Channel> getDisconnectedChannels() {
		return RESOURCE_SYNC_QUEUE.dispatchSync( new Callable<HashSet<Channel>>() {
			public HashSet<Channel> call() {
				return new HashSet<Channel>( DISCONNECTED_CHANNELS );
			}
		});
	}
	
	
	/** 
	 * Get the number of channels that were connected 
	 * @return the number of disconnected channels
	 */
	public int getDisconnectedCount() {
		return RESOURCE_SYNC_QUEUE.dispatchSync( new Callable<Integer>() {
			public Integer call() {
				return DISCONNECTED_CHANNELS.size();
			}
		});
	}
	
	
	/** 
	 * Get the channels pending connection 
	 * @return set of channels that are pending connection
	 */
	public Set<Channel> getPendingChannels() {
		return RESOURCE_SYNC_QUEUE.dispatchSync( new Callable<HashSet<Channel>>() {
			public HashSet<Channel> call() {
				return new HashSet<Channel>( PENDING_CHANNELS );
			}
		});
	}
	
	
	/** 
	 * get the exception if any for the specified channel 
	 * @param channel for which to get the exception if any
	 * @return the exception for the specified channel or null if none
	 */
	public Exception getException( final Channel channel ) {
		return RESOURCE_SYNC_QUEUE.dispatchSync( new Callable<Exception>() {
			public Exception call() {
				return EXCEPTIONS.get( channel );
			}
		});
	}
	
	
	/** 
	 * Get the failed channels
	 * @return set of channels that failed to connect
	 */
	public Set<Channel> getFailedChannels() {
		return RESOURCE_SYNC_QUEUE.dispatchSync( new Callable<HashSet<Channel>>() {
			public HashSet<Channel> call() {
				return new HashSet<Channel>( EXCEPTIONS.keySet() );
			}
		});
	}
	
	
	/** 
	 * Get the number of exceptions 
	 * @return the number of channels that had exceptions connecting
	 */
	public int getExceptionCount() {
		return RESOURCE_SYNC_QUEUE.dispatchSync( new Callable<Integer>() {
			public Integer call() {
				return EXCEPTIONS.size();
			}
		});
	}
	
	
	/** submit this batch for processing */
	public void submit() {
		RESOURCE_SYNC_QUEUE.dispatchBarrierSync( new Runnable() {
			public void run() {
				_isCanceled = false;
				PENDING_CHANNELS.clear();
				PENDING_CHANNELS.addAll( CHANNELS );
				DISCONNECTED_CHANNELS.addAll( CHANNELS );	// assume all channels disconnected until notified otherwise
				CONNECTED_CHANNELS.clear();
			}
		});
		
		try {
			for ( final Channel channel : CHANNELS ) {
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
	 * @return true upon completion and false if not complete
	 */
	public boolean submitAndWait( final double timeout ) {
		submit();
		return await( timeout );
	}
	
	
	/** 
	 * Wait up to the specified timeout for completion. This method may be called many times as needed.
	 * @param timeout the maximum time in seconds to wait for completion
	 * @return true upon completion and false if not complete
	 */
	public boolean await( final double timeout ) {
		final long milliTimeout = (long) ( 1000 * timeout );		// timeout in milliseconds
		final long maxTime = new Date().getTime() + milliTimeout;	// maximum time until expiration
		while( !_isCanceled && !isComplete() && new Date().getTime() < maxTime ) {
			final long remainingTime = Math.max( 0, maxTime - new Date().getTime() );
			if ( remainingTime > 0 ) {		// remaining time must be strictly greater than zero to prevent waiting forever should it be identically zero
				final long waitTime = remainingTime > WATCH_TIME ? WATCH_TIME : remainingTime;	// want to watch for cancel periodically when the remaining time is long
				try {
					synchronized( COMPLETION_LOCK ) {
						COMPLETION_LOCK.wait( waitTime );
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
	 * Determine whether this request has been caceled 
	 * @return true if canceled and false otherwise
	 */
	public boolean isCanceled() {
		return _isCanceled;
	}
	
	
	/** Cancel this request to stop monitoring and dispatching events. */
	public void cancel() {
		_isCanceled = true;
		RESOURCE_SYNC_QUEUE.dispatchBarrierSync( new Runnable() {
			public void run() {
				for ( final Channel channel : CHANNELS ) {
					channel.removeConnectionListener( REQUEST_HANDLER );
				}
			}
		});
	}
	
	
	/** process the get request for a single channel */
	private void processRequest( final Channel channel ) {
		try {
			channel.addConnectionListener( REQUEST_HANDLER );
			channel.requestConnection();
		}
		catch ( final Exception exception ) {
			RESOURCE_SYNC_QUEUE.dispatchBarrierAsync( new Runnable() {
				public void run() {
					EXCEPTIONS.put( channel, exception );
					PENDING_CHANNELS.remove( channel );
				}
			});			
			
			if ( !_isCanceled )  EVENT_PROXY.connectionExceptionInBatch( this, channel, exception );
			processCurrentStatus();
		}
	}
	
	
	/** 
	 * Determine if there are any channels pending for either an exception or a completed get request 
	 * @return true if complete and false otherwise
	 */
	public boolean isComplete() {
		return RESOURCE_SYNC_QUEUE.dispatchSync( new Callable<Boolean>() {
			public Boolean call() {
				return PENDING_CHANNELS.isEmpty();
			}
		});
	}
	
	
	/** check for the current status and post notifications if necessary */
	private void processCurrentStatus() {
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
			
			if ( !_isCanceled ) {
				int[] counts = RESOURCE_SYNC_QUEUE.dispatchSync( new Callable<int[]>() {
					public int[] call() {
						return new int[] { CONNECTED_CHANNELS.size(), DISCONNECTED_CHANNELS.size(), EXCEPTIONS.size() };
					}
				});
			
				if ( !_isCanceled )  EVENT_PROXY.batchConnectionRequestCompleted( this, counts[0], counts[1], counts[2] );
			}
		}
	}
	
	
	/** handle get request events */
	protected class RequestHandler implements ConnectionListener {
		/**
		 * Indicates that a connection to the specified channel has been established.
		 * @param channel The channel which has been connected.
		 */
		public void connectionMade( final Channel channel ) {
			RESOURCE_SYNC_QUEUE.dispatchBarrierAsync( new Runnable() {
				public void run() {
					CONNECTED_CHANNELS.add( channel );
					DISCONNECTED_CHANNELS.remove( channel );
					PENDING_CHANNELS.remove( channel );
				}
			});
			
			if ( !_isCanceled )  EVENT_PROXY.connectionChangeInBatch( BatchConnectionRequest.this, channel, true ); 
			processCurrentStatus();
		}
		
		/**
		 * Indicates that a connection to the specified channel has been dropped.
		 * @param channel The channel which has been disconnected.
		 */
		public void connectionDropped( final Channel channel ) {
			RESOURCE_SYNC_QUEUE.dispatchBarrierAsync( new Runnable() {
				public void run() {
					DISCONNECTED_CHANNELS.add( channel );
					CONNECTED_CHANNELS.remove( channel );
					PENDING_CHANNELS.remove( channel );
				}
			});
			
			if ( _isCanceled )  EVENT_PROXY.connectionChangeInBatch( BatchConnectionRequest.this, channel, false ); 
			processCurrentStatus();
		}
	}
}

