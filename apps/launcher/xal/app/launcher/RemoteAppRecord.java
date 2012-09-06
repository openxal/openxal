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

import xal.tools.services.RemoteServiceDroppedException;
import xal.application.ApplicationStatus;
import xal.tools.dispatch.DispatchQueue;


/** RemoteAppRecord wraps the remote proxy so it can be hashed and included in collections */
public class RemoteAppRecord {
	/** remote proxy */
	private final ApplicationStatus REMOTE_PROXY;

	/** cache for the application name */
	private final RemoteDataCache<String> APPLICATION_NAME_CACHE;

	/** cache for the host name */
	private final RemoteDataCache<String> HOST_NAME_CACHE;

	/** cache for the launch time */
	private final RemoteDataCache<Date> LAUNCH_TIME_CACHE;

	/** timestamp of last update */
	private Date _lastUpdate;

	/** indicates whether the service is reachable */
	private volatile boolean _shouldRefresh;

	
	/** Constructor */
    public RemoteAppRecord( final ApplicationStatus proxy ) {
		REMOTE_PROXY = proxy;
		
		_lastUpdate = null;
		_shouldRefresh = true;	// assume so until proven otherwise

		// don't need to keep making remote requests for application name as it won't change
		APPLICATION_NAME_CACHE = createRemoteOperationCache( new Callable<String>() {
			public String call() {
				return REMOTE_PROXY.getApplicationName();
			}
		});

		// don't need to keep making remote requests for host name as it won't change
		HOST_NAME_CACHE = createRemoteOperationCache( new Callable<String>() {
			public String call() {
				return REMOTE_PROXY.getHostName();
			}
		});

		// don't need to keep making remote requests for launch time as it won't change
		LAUNCH_TIME_CACHE = createRemoteOperationCache( new Callable<Date>() {
			public Date call() {
				return REMOTE_PROXY.getLaunchTime();
			}
		});
    }



	/** Create a remote operation cache for the given operation */
	private <DataType> RemoteDataCache<DataType> createRemoteOperationCache( final Callable<DataType> operation ) {
		return new RemoteDataCache<DataType>( operation );
	}


	/** Get the timestamp of the last update */
	public Date getLastUpdate() {
		return _lastUpdate;
	}


	/**
	 * Get the total memory consumed by the application instance.
	 * @return The total memory consumed by the application instance.
	 */
	public double getTotalMemory() {
		if ( _shouldRefresh ) {
			try {
				final double memory = REMOTE_PROXY.getTotalMemory();
				_lastUpdate = new Date();
				return memory;
			}
			catch ( RemoteServiceDroppedException exception ) {
				_shouldRefresh = false;
				return Double.NaN;
			}
		}
		else {
			return Double.NaN;
		}
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
	/** remote operation to perform */
	private final Callable<DataType> REMOTE_OPERATION;

	/** latest data that has been cached */
	private volatile RemoteData<DataType> _cachedData;


	/** Constructor */
	public RemoteDataCache( final Callable<DataType> remoteOperation ) {
		REMOTE_OPERATION = remoteOperation;
		
		_cachedData = null;
	}


	/** Get the timestamp of the last fetch */
	public Date getTimestamp() {
		return _cachedData != null ? _cachedData.getTimestamp() : null;
	}


	/** Fetch the value and cache it for future requests */
	public DataType getValue() {
		if ( _cachedData == null ) {
			fetchData();
		}

		return _cachedData != null ? _cachedData.getValue() : null;
	}


	/** Fetch the data from the remote service */
	private void fetchData() {
		try {
			final DataType result = REMOTE_OPERATION.call();
			_cachedData = new RemoteData<DataType>( result );
		}
		catch ( RemoteServiceDroppedException exception ) {
			_cachedData = null;
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
			_cachedData = null;
		}
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
