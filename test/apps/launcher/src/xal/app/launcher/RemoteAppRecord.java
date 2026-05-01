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

import xal.tools.UpdateListener;
import xal.extension.service.*;
import xal.extension.application.ApplicationStatus;
import xal.tools.dispatch.DispatchQueue;


/** RemoteAppRecord wraps the remote proxy so it can be hashed and included in collections */
public class RemoteAppRecord implements UpdateListener {
	/** remote proxy */
	private final ApplicationStatus REMOTE_PROXY;

	/** cache for the application name */
	private final RemoteDataCache<String> APPLICATION_NAME_CACHE;

	/** cache for the host name */
	private final RemoteDataCache<String> HOST_NAME_CACHE;

	/** cache for the launch time */
	private final RemoteDataCache<Date> LAUNCH_TIME_CACHE;

	/** cache for the total memory */
	private final RemoteDataCache<Double> TOTAL_MEMORY_CACHE;

	/** cache for the remote service heartbeat */
	private final RemoteDataCache<Date> HEARTBEAT_CACHE;

	/** host address of the remote service */
	private final String REMOTE_ADDRESS;

	/** optional handler of the update event */
	private UpdateListener _updateListener;

	
	/** Primary Constructor */
    public RemoteAppRecord( final ApplicationStatus proxy, final UpdateListener updateListener ) {
		REMOTE_PROXY = proxy;
		REMOTE_ADDRESS = ((ServiceState)proxy).getServiceHost();

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

		// insulate this call from hangs of the remote application
		TOTAL_MEMORY_CACHE = createRemoteOperationCache( new Callable<Double>() {
			public Double call() {
				return REMOTE_PROXY.getTotalMemory();
			}
		});

		// insulate this call from hangs of the remote application
		HEARTBEAT_CACHE = createRemoteOperationCache( new Callable<Date>() {
			public Date call() {
				return REMOTE_PROXY.getHeartbeat();
			}
		});
		
		// observe updates from the caches
		APPLICATION_NAME_CACHE.setUpdateListener( this );
		HOST_NAME_CACHE.setUpdateListener( this );
		LAUNCH_TIME_CACHE.setUpdateListener( this );
		TOTAL_MEMORY_CACHE.setUpdateListener( this );
		HEARTBEAT_CACHE.setUpdateListener( this );
    }


	/** Constructor */
    public RemoteAppRecord( final ApplicationStatus proxy ) {
		this( proxy, null );
    }


	/** Create a remote operation cache for the given operation */
	static private <DataType> RemoteDataCache<DataType> createRemoteOperationCache( final Callable<DataType> operation ) {
		return new RemoteDataCache<DataType>( operation );
	}


	/** set the update handler which is called when the cache has been updated */
	public void setUpdateListener( final UpdateListener handler ) {
		_updateListener = handler;
	}


	/** get the update handler */
	public UpdateListener getUpdateListener() {
		return _updateListener;
	}


	/** called when the source posts an update to this observer */
	public void observedUpdate( final Object source ) {
		// propagate update notification to the update listener if any
		final UpdateListener updateHandler = _updateListener;
		if ( updateHandler != null ) {
			updateHandler.observedUpdate( this );
		}
	}


	/** refresh the record */
	public void refresh() {
		TOTAL_MEMORY_CACHE.refresh();		// refresh the cache for future calls
		HEARTBEAT_CACHE.refresh();		// refresh the cache for future calls
	}


	/**
	 * Get the total memory consumed by the application instance.
	 * @return The total memory consumed by the application instance.
	 */
	public double getTotalMemory() {
		if ( isConnected() ) {
			try {
				final Double memory = TOTAL_MEMORY_CACHE.getValue();
				if ( memory != null ) {
					return memory.doubleValue();
				}
				else {
					return Double.NaN;
				}
			}
			catch ( Exception exception ) {
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
		final String applicationName = APPLICATION_NAME_CACHE.getValue();
		// if the service cache is not yet populated, then just get the name from the service info
		return applicationName != null ? applicationName : ((ServiceState)REMOTE_PROXY).getServiceName();
	}


	/**
	 * Get the name of the host where the application is running.
	 * @return The name of the host where the application is running.
	 */
	public String getHostName() {
		final String hostName = HOST_NAME_CACHE.getValue();
		return hostName != null ? hostName : REMOTE_ADDRESS;	// if we can't get the host name from the remote service something went wrong and just return the remote address so we have some information
	}


	/**
	 * Get the launch time of the application
	 * @return the time at with the application was launched
	 */
	public Date getLaunchTime() {
		return LAUNCH_TIME_CACHE.getValue();
	}


	/**
	 * Get the heartbeat from the service
	 * @return the time at with the application was launched in seconds since the epoch
	 */
	public Date getHeartbeat() {
		return HEARTBEAT_CACHE.getValue();
	}


	/** Determine whether this record is believed to be connected but don't test */
	public boolean isConnected() {
		return HEARTBEAT_CACHE.isConnected();
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