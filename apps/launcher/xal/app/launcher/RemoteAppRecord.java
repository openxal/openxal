//
// RemoteAppRecord.java
// Open XAL
//
// Created by Pelaia II, Tom on 9/4/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import java.util.concurrent.Callable;
import java.util.Date;

import xal.application.ApplicationStatus;
import xal.tools.dispatch.DispatchQueue;


/** RemoteAppRecord wraps the remote proxy so it can be hashed and included in collections */
public class RemoteAppRecord {
	/** remote proxy */
	private final ApplicationStatus REMOTE_PROXY;

	/** cache configuration */
	private final RemoteCacheConfig CACHE_CONFIG;

	/** cache for the application name */
	private final RemoteDataCache<String> APPLICATION_NAME_CACHE;

	/** cache for the host name */
	private final RemoteDataCache<String> HOST_NAME_CACHE;

	/** cache for the launch time */
	private final RemoteDataCache<Date> LAUNCH_TIME_CACHE;

	/** cache for the total memory */
	private final RemoteDataCache<Double> TOTAL_MEMORY_CACHE;

	
	/** Constructor */
    public RemoteAppRecord( final ApplicationStatus proxy, final RemoteCacheConfig cacheConfig ) {
		REMOTE_PROXY = proxy;
		CACHE_CONFIG = cacheConfig;

		APPLICATION_NAME_CACHE = createRemoteOperationCache( new Callable<String>() {
			public String call() {
				return REMOTE_PROXY.getApplicationName();
			}
		});

		HOST_NAME_CACHE = createRemoteOperationCache( new Callable<String>() {
			public String call() {
				return REMOTE_PROXY.getHostName();
			}
		});

		LAUNCH_TIME_CACHE = createRemoteOperationCache( new Callable<Date>() {
			public Date call() {
				return REMOTE_PROXY.getLaunchTime();
			}
		});

		TOTAL_MEMORY_CACHE = createRemoteOperationCache( new Callable<Double>() {
			public Double call() {
				return REMOTE_PROXY.getTotalMemory();
			}
		});
    }



	/** Create a remote operation cache for the given operation */
	private <DataType> RemoteDataCache<DataType> createRemoteOperationCache( final Callable<DataType> operation ) {
		return new RemoteDataCache<DataType>( operation, CACHE_CONFIG );
	}


	/** Get the timestamp of the last update */
	public Date getLastUpdate() {
		return APPLICATION_NAME_CACHE.getTimestamp();
	}


	/**
	 * Get the total memory consumed by the application instance.
	 * @return The total memory consumed by the application instance.
	 */
	public double getTotalMemory() {
		final Double memory = TOTAL_MEMORY_CACHE.getValue();
		return memory != null ? memory.doubleValue() : Double.NaN;
	}


	/**
	 * Get the application name.
	 * @return The application name.
	 */
	public String getApplicationName() {
		return APPLICATION_NAME_CACHE.getValue();
	}


	/**
	 * Get the name of the host where the application is running.
	 * @return The name of the host where the application is running.
	 */
	public String getHostName() {
		return HOST_NAME_CACHE.getValue();
	}


	/**
	 * Get the launch time of the application in seconds since the epoch (midnight GMT, January 1, 1970)
	 * @return the time at with the application was launched in seconds since the epoch
	 */
	public java.util.Date getLaunchTime() {
		return LAUNCH_TIME_CACHE.getValue();
	}


	/** reveal the application by bringing all windows to the front */
	public void showAllWindows() {
		DispatchQueue.getGlobalDefaultPriorityQueue().dispatchAsync( new Runnable() {
			public void run() {
				REMOTE_PROXY.showAllWindows();
			}
		});
	}


	/** Request that the virtual machine run the garbage collector. */
	public void collectGarbage() {
		DispatchQueue.getGlobalDefaultPriorityQueue().dispatchAsync( new Runnable() {
			public void run() {
				REMOTE_PROXY.collectGarbage();
			}
		});
	}


	/**
	 * Quit the application normally.
	 * @param code An unused status code.
	 */
	public void quit( final int code ) {
		DispatchQueue.getGlobalDefaultPriorityQueue().dispatchAsync( new Runnable() {
			public void run() {
				REMOTE_PROXY.quit( code );
			}
		});
	}


	/**
	 * Force the application to quit immediately without running any finalizers.
	 * @param code The status code used for halting the virtual machine.
	 */
	public void forceQuit( final int code ) {
		DispatchQueue.getGlobalDefaultPriorityQueue().dispatchAsync( new Runnable() {
			public void run() {
				REMOTE_PROXY.forceQuit( code );
			}
		});
	}
}



/** cache remote data */
class RemoteDataCache<DataType> {
	/** queue on which to make the remote calls */
	private final DispatchQueue REMOTE_CALL_QUEUE;
	
	/** cache configuration */
	private final RemoteCacheConfig CACHE_CONFIG;

	/** remote operation to perform */
	private final Callable<DataType> REMOTE_OPERATION;

	/** latest data that has been cached */
	private volatile RemoteData<DataType> _cachedData;

	/** indicates whether a fetch operation is executing */
	private volatile boolean _isFetching;


	/** Constructor */
	public RemoteDataCache( final Callable<DataType> remoteOperation, final RemoteCacheConfig cacheConfig ) {
		REMOTE_OPERATION = remoteOperation;
		CACHE_CONFIG = cacheConfig;
		REMOTE_CALL_QUEUE = DispatchQueue.createSerialQueue( "Remote Calls" );
		
		_isFetching = false;
	}


	/** Get the timestamp of the last fetch */
	public Date getTimestamp() {
		return _cachedData != null ? _cachedData.getTimestamp() : null;
	}


	/** Fetch the latest value if an operation is not in progress and return the latest value */
	public DataType getValue() {
		if ( !_isFetching ) {
			fetchData();
		}

		return _cachedData != null ? _cachedData.getValue() : null;
	}


	/** Fetch the data from the remote service */
	private void fetchData() {
		REMOTE_CALL_QUEUE.dispatchAsync( new Runnable() {
			public void run() {
				_isFetching = true;		// mark the state as fetching

				try {
					final DataType result = REMOTE_OPERATION.call();
					_cachedData = new RemoteData<DataType>( result );
				}
				catch( Exception exception ) {
					exception.printStackTrace();
					_cachedData = null;
				}

				_isFetching = false;	// we're done fetching
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
}
