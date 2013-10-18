//
// RemoteDataCache.java
// Open XAL
//
// Created by Pelaia II, Tom on 10/1/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.service;

import java.util.concurrent.Callable;
import java.util.Date;

import xal.tools.UpdateListener;
import xal.tools.dispatch.DispatchQueue;


/** RemoteDataCache is a utility for managing calls to remote services to avoid deadlock if a service is down. */
public class RemoteDataCache<DataType> {
	/** remote operation to perform */
	private final Callable<DataType> REMOTE_OPERATION;

	/** latest data that has been cached */
	protected volatile RemoteData<DataType> _cachedData;

	/** indicates whether the remote service is connected */
	private volatile boolean _isConnected;

	/** indicates whether a fetch is pending */
	private volatile boolean _isFetchPending;

	/** optional handler of the update event */
	private UpdateListener _updateListener;


	/** Constructor */
	public RemoteDataCache( final Callable<DataType> remoteOperation ) {
		this( remoteOperation, null );
	}


	/** Primary Constructor */
	public RemoteDataCache( final Callable<DataType> remoteOperation, final UpdateListener updateHandler ) {
		REMOTE_OPERATION = remoteOperation;
		
		_updateListener = updateHandler;

		_isFetchPending = false;
		_cachedData = null;
		_isConnected = true;	// assume connected until proven otherwise
	}


	/** set the update handler which is called when the cache has been updated */
	public void setUpdateListener( final UpdateListener handler ) {
		_updateListener = handler;
	}


	/** get the update handler */
	public UpdateListener getUpdateListener() {
		return _updateListener;
	}


	/** Refresh the cache with a fresh call to the remote unless a fetch is already pending */
	public void refresh() {
		// fetch new data only if a fetch is not currently in progress
		if ( !_isFetchPending ) {
			fetchData();
		}
	}


	/** Get the timestamp of the last fetch */
	public Date getTimestamp() {
		final RemoteData<DataType> cachedData = _cachedData;
		return cachedData != null ? cachedData.getTimestamp() : null;
	}


	/** Fetch the value and cache it for future requests */
	public DataType getValue() {
		final RemoteData<DataType> cachedData = _cachedData;
		
		if ( cachedData == null ) {
			refresh();
		}

		return cachedData != null ? cachedData.getValue() : null;
	}


	/** determine whether the remote service is connected */
	public boolean isConnected() {
		return _isConnected;
	}


	/** Fetch the data from the remote service */
	private void fetchData() {
		_isFetchPending = true;

		DispatchQueue.getGlobalDefaultPriorityQueue().dispatchAsync( new Runnable() {
			public void run() {
				try {
					final DataType result = REMOTE_OPERATION.call();
					_cachedData = new RemoteData<DataType>( result );
				}
				catch ( RemoteServiceDroppedException exception ) {
					_cachedData = null;
					_isConnected = false;
				}
				catch ( Exception exception ) {
					exception.printStackTrace();
					_cachedData = null;
				}
				finally {
					_isFetchPending = false;

					// if there is an update listener, notify it of the updated value
					final UpdateListener updateHandler = _updateListener;
					if ( updateHandler != null ) {
						updateHandler.observedUpdate( RemoteDataCache.this );
					}
				}
			}
		});
	}
}



/** data from a remote fetch */
class RemoteData<DataType> {
	/** latest data that has been cached */
	final private DataType VALUE;

	/** time of the last fetch from which the expiration should be measured */
	final private Date FETCH_TIMESTAMP;


	/** Primary Constructor */
	public RemoteData( final DataType value, final Date timestamp ) {
		VALUE = value;
		FETCH_TIMESTAMP = timestamp;
	}


	/** Constructor */
	public RemoteData( final DataType value ) {
		this( value, new Date() );
	}


	/** get the value */
	public DataType getValue() {
		return VALUE;
	}


	/** get the timestamp */
	public Date getTimestamp() {
		return FETCH_TIMESTAMP;
	}


	/** get string representation */
	public String toString() {
		return "Cached value: " + VALUE + ", timestamp: " + FETCH_TIMESTAMP;
	}
}
