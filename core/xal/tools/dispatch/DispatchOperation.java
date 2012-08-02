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
abstract class DispatchOperation<ReturnType> implements Callable<ReturnType> {
	/** indicates whether the taks is a barrier operation */
	final private boolean IS_BARRIER;
	
	/** flag indicating whether this operation is currently running */
	private volatile boolean _isRunning;
	
	/** flag indicating whether this operation is done execution */
	private volatile boolean _isComplete;
	
	/** listeners for events from this operation (e.g. queue and groups) */
	private final Set<DispatchOperationListener> EVENT_LISTENERS;
	
	/** result of the operation upon successful completion */
	private ReturnType _result;
	
	
	/** Primary Constructor */
	protected DispatchOperation( final DispatchOperationListener delegate, final boolean isBarrier ) {
		IS_BARRIER = isBarrier;
		
		EVENT_LISTENERS = new HashSet<DispatchOperationListener>();
		addDispatchOperationListener( delegate );
		
		_isRunning = false;
		_isComplete = false;
		_result = null;
	}
	
	
	/** Get a new dispatch operation that wraps the specified raw operation */
	static public DispatchOperation<Void> getInstance( final Runnable rawOperation, final DispatchOperationListener delegate, final boolean isBarrier ) {
		return new DispatchOperationRawRunnable( rawOperation, delegate, isBarrier );
	}
	
	
	/** Get a new dispatch operation that wraps the specified raw operation */
	static public <ReturnType> DispatchOperation<ReturnType> getInstance( final Callable<ReturnType> rawOperation, final DispatchOperationListener delegate, final boolean isBarrier ) {
		return new DispatchOperationRawCallable<ReturnType>( rawOperation, delegate, isBarrier );
	}
	
	
	/** wait for this operation to complete */
	final public void waitForCompletion() {
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
	final public void addDispatchOperationListener( final DispatchOperationListener listener ) {
		EVENT_LISTENERS.add( listener );
	}
	
	
	/** determine whether this operation is a barrier operation */
	final public boolean isBarrier() {
		return IS_BARRIER;
	}
	
	
	/** Determine whether this operation is currently running */
	final public boolean isRunning() {
		return _isRunning;
	}
	
	
	/** Determine whether this operation is done execution */
	final public boolean isComplete() {
		return _isComplete;
	}
	
	
	/** Get the result */
	final public ReturnType getResult() {
		return _result;
	}
	
	
	/** perform the operation */
	final public ReturnType call() {
		try {
			_isRunning = true;
			final ReturnType result = executeRawOperation();
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
	
	
	abstract protected ReturnType executeRawOperation() throws java.lang.Exception;
	
	
	/** notify the queue and groups that the operation has completed */
	private void sendCompletionNotification() {
		for ( final DispatchOperationListener handler : EVENT_LISTENERS ) {
			handler.operationCompleted( this );
		}
	}
}



/** Dispatch operation built to execute a raw runnable operation */
class DispatchOperationRawRunnable extends DispatchOperation<Void> {
	/** wrapped operation */
	final private Runnable RAW_OPERATION;
	
	
	/** Primary Constructor */
	public DispatchOperationRawRunnable( final Runnable rawOperation, final DispatchOperationListener delegate, final boolean isBarrier ) {
		super( delegate, isBarrier );
		
		RAW_OPERATION = rawOperation;
	}
	
	
	protected Void executeRawOperation() throws java.lang.Exception {
		RAW_OPERATION.run();
		return null;
	}
}



/** Dispatch operation built to execute a raw runnable operation */
class DispatchOperationRawCallable<ReturnType> extends DispatchOperation<ReturnType> {
	/** wrapped operation */
	final private Callable<ReturnType> RAW_OPERATION;
	
	
	/** Primary Constructor */
	public DispatchOperationRawCallable( final Callable<ReturnType> rawOperation, final DispatchOperationListener delegate, final boolean isBarrier ) {
		super( delegate, isBarrier );
		
		RAW_OPERATION = rawOperation;
	}
	
	
	protected ReturnType executeRawOperation() throws java.lang.Exception {
		return RAW_OPERATION.call();
	}
}
