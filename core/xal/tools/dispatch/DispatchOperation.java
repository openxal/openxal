//
// DispatchOperation.java
// xal
//
// Created by Tom Pelaia on 4/23/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.dispatch;

import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.*;


/** Wraps a operation so status can be monitored */
class DispatchOperation<ReturnType> implements Callable<ReturnType> {
	/** indicates whether the taks is a barrier operation */
	final private boolean IS_BARRIER;
	
	/** wrapped operation */
	final Callable<ReturnType> RAW_OPERATION;
	
	/** flag indicating whether this operation is currently running */
	private volatile boolean _isRunning;
	
	/** flag indicating whether this operation is done execution */
	private volatile boolean _isComplete;
	
	/** listeners for events from this operation (e.g. queue and groups) */
	private final Set<DispatchOperationListener> EVENT_LISTENERS;
	
	/** result of the operation upon successful completion */
	private ReturnType _result;
	
	
	/** Primary Constructor */
	public DispatchOperation( final Callable<ReturnType> rawOperation, final boolean isBarrier ) {
		IS_BARRIER = isBarrier;
		RAW_OPERATION = rawOperation;
		
		EVENT_LISTENERS = new HashSet<DispatchOperationListener>();
		
		_isRunning = false;
		_isComplete = false;
		_result = null;
	}
	
	
	/** Constructor for a operation which is not a barrier */
	public DispatchOperation( final Callable<ReturnType> rawOperation ) {
		this( rawOperation, false );
	}
	
	
	/** wait for this operation to complete */
	public void waitForCompletion() {
		while( !_isComplete ) {
			try {
				synchronized( this ) {
					this.wait();
				}
			}
			catch( Exception exception ) {}
		}
	}
	
	
	/** Add the event listener */
	public void addDispatchOperationListener( final DispatchOperationListener listener ) {
		EVENT_LISTENERS.add( listener );
	}
	
	
	/** determine whether this operation is a barrier operation */
	public boolean isBarrier() {
		return IS_BARRIER;
	}
	
	
	/** Determine whether this operation is currently running */
	public boolean isRunning() {
		return _isRunning;
	}
	
	
	/** Determine whether this operation is done execution */
	public boolean isComplete() {
		return _isComplete;
	}
	
	
	/** Get the result */
	public ReturnType getResult() {
		return _result;
	}
	
	
	/** perform the operation */
	public ReturnType call() {
		try {
			_isRunning = true;
			final ReturnType result = RAW_OPERATION.call();
			_result = result;
			return result;
		}
		catch ( Exception exception ) {
			throw new RuntimeException( exception );
		}
		finally {
			_isRunning = false;
			_isComplete = true;
			
			try {
				synchronized( this ) {
					this.notifyAll();
				}
			}
			catch( Exception exception ) {
				System.err.println( "Failed attempt to awake threads waiting on this operation to complete." );
				exception.printStackTrace();
			}
			
			sendCompletionNotification();
		}
	}
	
	
	/** notify the queue and groups that the operation has completed */
	public void sendCompletionNotification() {
		for ( final DispatchOperationListener handler : EVENT_LISTENERS ) {
			handler.operationCompleted( this );
		}
	}
}
