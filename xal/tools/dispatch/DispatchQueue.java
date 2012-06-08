//
// DispatchQueue.java
// xal
//
// Created by Tom Pelaia on 4/18/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.dispatch;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;


/** DispatchQueue which attempts to implement a subset of the open source libdispatch library */
abstract public class DispatchQueue implements DispatchOperationListener {
	/** priority for a high priority queue */
	final static public int DISPATCH_QUEUE_PRIORITY_HIGH;
	
	/** priority for the default priority queue */
	final static public int DISPATCH_QUEUE_PRIORITY_DEFAULT;
	
	/** priority for the low priority queue */
	final static public int DISPATCH_QUEUE_PRIORITY_LOW;
	
	/** priority for the background priority queue */
	final static public int DISPATCH_QUEUE_PRIORITY_BACKGROUND;
	
	/** executor which processes the queue */
	final protected ExecutorService QUEUE_PROCESSOR;
	
	/** thread factory for dispatch this queue */
	final protected ThreadFactory DISPATCH_THREAD_FACTORY;
	
	/** executor for processing dispatched operations */
	final protected ExecutorService DISPATCH_EXECUTOR;
	
	/** optional labelf for this queue */
	final private String LABEL;
	
	/** number of operations currently running */
	protected volatile int _runningOperationCount;
	
	/** indicates whether this queue is submitting pending operations for execution */
	protected volatile boolean _isProcessingPendingOperationQueue;
	
	/** queue of pending operations which have not yet been submitted for execution */
	final protected LinkedBlockingQueue<DispatchOperation<?>> PENDING_OPERATION_QUEUE;
	
	
	// static initializer
	static {
		DISPATCH_QUEUE_PRIORITY_HIGH = ( Thread.MAX_PRIORITY + Thread.NORM_PRIORITY ) / 2;
		DISPATCH_QUEUE_PRIORITY_DEFAULT = Thread.NORM_PRIORITY;
		DISPATCH_QUEUE_PRIORITY_LOW = ( Thread.MIN_PRIORITY + Thread.NORM_PRIORITY ) / 2;
		DISPATCH_QUEUE_PRIORITY_BACKGROUND = Thread.MIN_PRIORITY;
	}
		
	
	/** Primary Constructor */
    protected DispatchQueue( final String label, final int priority ) {
		LABEL = label;
		
		DISPATCH_THREAD_FACTORY = new DispatchThreadFactory( this, priority );
		DISPATCH_EXECUTOR = createDispatchExecutor();
		
		PENDING_OPERATION_QUEUE = new LinkedBlockingQueue<DispatchOperation<?>>();
		QUEUE_PROCESSOR = Executors.newSingleThreadExecutor();
		
		_isProcessingPendingOperationQueue = true;
		_runningOperationCount = 0;
	}
	
	
	/** Constructor */
    protected DispatchQueue( final String label ) {
		this( label, Thread.NORM_PRIORITY );
	}
	
	
	/** get this queue's label */
	public String getLabel() {
		return LABEL;
	}
	
	
	/** suspend execution of pending operations */
	public void suspend() {
		_isProcessingPendingOperationQueue = false;
	}
	
	
	/** resume execution of pending operations */
	public void resume() {
		if ( !_isProcessingPendingOperationQueue ) {
			_isProcessingPendingOperationQueue = true;
			processOperationQueue();
		}
	}
	
	
	/** create the executor for dispatching operations */
	abstract protected ExecutorService createDispatchExecutor();
	
	
	/** 
	 * Create a concurrent queue 
	 * @param label optional label for the queue to create
	 * @return a new concurrent queue
	 */
	static public DispatchQueue createConcurrentQueue( final String label ) {
		return new ConcurrentDispatchQueue( label );
	}
	
	
	/** 
	 * Create a serial queue 
	 * @param label optional label for the queue to create
	 * @return a new serial queue
	 */
	static public DispatchQueue createSerialQueue( final String label ) {
		return new SerialDispatchQueue( label );
	}
	
	
	/** Get the main queue on which Swing events are dispatched. The main queue cannot be suspended or resumed. */
	static public DispatchQueue getMainQueue() {
		return MainDispatchQueue.defaultQueue();
	}
	
	
	/** 
	 * Get the global queue corresponding to the specified priority. The global queue cannot be suspended or resumed.
	 * @param priority one of DISPATCH_QUEUE_PRIORITY_HIGH, DISPATCH_QUEUE_PRIORITY_DEFAULT, DISPATCH_QUEUE_PRIORITY_LOW, DISPATCH_QUEUE_PRIORITY_BACKGROUND
	 * @return the global queue corresponding to the specified priority or null if none exists.
	 */
	static public DispatchQueue getGlobalQueue( final int priority ) {
		if ( priority == DISPATCH_QUEUE_PRIORITY_DEFAULT ) {
			return GlobalDispatchQueue.DEFAULT_PRIORITY_DISPATCH_QUEUE;
		}
		else if ( priority == DISPATCH_QUEUE_PRIORITY_HIGH ) {
			return GlobalDispatchQueue.HIGH_PRIORITY_DISPATCH_QUEUE;
		}
		else if ( priority == DISPATCH_QUEUE_PRIORITY_LOW ) {
			return GlobalDispatchQueue.LOW_PRIORITY_DISPATCH_QUEUE;
		}
		else if ( priority == DISPATCH_QUEUE_PRIORITY_BACKGROUND ) {
			return GlobalDispatchQueue.BACKGROUND_PRIORITY_DISPATCH_QUEUE;
		}
		else {
			return null;
		}
	}
	
	
	/** Get the global default priority queue */
	static public DispatchQueue getGlobalDefaultPriorityQueue() {
		return GlobalDispatchQueue.DEFAULT_PRIORITY_DISPATCH_QUEUE;
	}
	
	
	/** Get the global high priority queue */
	static public DispatchQueue getGlobalHighPriorityQueue() {
		return GlobalDispatchQueue.HIGH_PRIORITY_DISPATCH_QUEUE;
	}
	
	
	/** Get the global low priority queue */
	static public DispatchQueue getGlobalLowPriorityQueue() {
		return GlobalDispatchQueue.LOW_PRIORITY_DISPATCH_QUEUE;
	}
	
	
	/** Get the global background priority queue */
	static public DispatchQueue getGlobalBackgroundPriorityQueue() {
		return GlobalDispatchQueue.BACKGROUND_PRIORITY_DISPATCH_QUEUE;
	}
	
	
	/** get the current queue or null if the current thread does not belong to a queue */
	@SuppressWarnings( "unchecked" )	// need to cast thread to DispatchThread after checking
	static public DispatchQueue getCurrentQueue() {
		final Thread currentThread = Thread.currentThread();
		if ( currentThread instanceof DispatchThread ) {
			final DispatchThread currentDispatchThread = (DispatchThread)currentThread;
			return currentDispatchThread.getQueue();
		}
		else if ( SwingUtilities.isEventDispatchThread() ) {
			return MainDispatchQueue.defaultQueue();
		}
		else {
			return null;
		}
	}
	
	
	/** Determine whether this queue is the current queue */
	public boolean isCurrentQueue() {
		return this == getCurrentQueue();
	}
	
	
	/** submit the operation for execution on the queue and wait for it to complete */
	public <ReturnType> ReturnType dispatchSync( final Callable<ReturnType> rawOperation ) {
		final DispatchOperation<ReturnType> operation = makeDispatchOperation( rawOperation );
		enqueueOperation( operation );
		
		// wait until the operation completes
		operation.waitForCompletion();
		
		return operation.getResult();
	}
	
	
	/** submit the operation for execution on the queue and wait for it to complete */
	public void dispatchSync( final Runnable rawOperation ) {
		dispatchSync( rawOperation, false );
	}
	
	
	/** submit the operation for execution on the queue and wait for it to complete */
	protected void dispatchSync( final Runnable rawOperation, final boolean isBarrier ) {
		final DispatchOperation<Void> operation = makeDispatchOperation( rawOperation, isBarrier );
		enqueueOperation( operation );
		
		operation.waitForCompletion();
	}
	
	
	/** submit the operation for execution on the queue without waiting for completion */
	public void dispatchAsync( final Runnable rawOperation ) {
		dispatchAsync( null, rawOperation );
	}
	
	
	/** submit the operation for execution on the queue and add it to the specified group without waiting for completion */
	public void dispatchAsync( final DispatchGroup group, final Runnable rawOperation ) {
		dispatchAsync( group, rawOperation, false );
	}
	
	
	/** submit the operation for execution on the queue and add it to the specified group without waiting for completion */
	protected void dispatchAsync( final DispatchGroup group, final Runnable rawOperation, final boolean isBarrier ) {
		final DispatchOperation<Void> operation = makeDispatchOperation( rawOperation, isBarrier );
		if ( group == null ) {
			DispatchGroup.addOperationToCurrentGroups( operation );
		}
		else {
			group.addOperationToThisGroupAndCurrentGroups( operation );
		}
		enqueueOperation( operation );
	}
	
	
	/** dispatch the operation after the specified time without blocking */
	public void dispatchAfter( final Date dispatchTime, final Runnable rawOperation ) {
		final DispatchOperation<Void> operation = makeDispatchOperation( rawOperation, false );
		DispatchGroup.addOperationToCurrentGroups( operation );
		final TimerTask enqueueTask = new TimerTask() {
			public void run() {
				enqueueOperation( operation );
			}
		};
		new Timer().schedule( enqueueTask, dispatchTime );
	}
	
	
	/** 
	 * Submit a barrier block for execution on the queue without waiting for completion. 
	 * The barrier waits for all operations on the queue to complete before executing and then blocks all other threads on the concurrent queue until it completes.
	 * Only relevant on a concurrent queue created using createConcurrentQueue(). On all other queues, it is equivalent to dispatchAsync(). 
	 * @param operation the operation to execute
	 */
	public void dispatchBarrierAsync( final Runnable operation ) {
		dispatchAsync( null, operation, true );
	}
	
	
	/** 
	 * Submit a barrier block for execution on the queue and wait for completion. 
	 * The barrier waits for all operations on the queue to complete before executing and then blocks all other threads on the concurrent queue until it completes.
	 * Only relevant on a concurrent queue created using createConcurrentQueue(). On all other queues, it is equivalent to dispatchSync(). 
	 * @param operation the operation to execute
	 */
	public void dispatchBarrierSync( final Runnable operation ) {
		dispatchSync( operation, true );
	}
	
	
	/** Performs all the specified iterations of the kernel asynchronously and waits for them to complete. */
	public void dispatchApply( final int iterations, final DispatchIterationKernel iterationKernel ) {
 		final DispatchGroup group = new DispatchGroup();
		for ( int index = 0 ; index < iterations ; index++ ) {
			final int iteration = index;
			dispatchAsync( group, new Runnable() {
				public void run() {
					iterationKernel.evaluateIteration( iteration );
				}
			});
		}
		group.waitForCompletion();
	}
	
	
	/** Enqueue the operation and process make sure the operation queue gets processed */
	protected <ReturnType> void enqueueOperation( final DispatchOperation<ReturnType> operation ) {
		PENDING_OPERATION_QUEUE.add( operation );
		processOperationQueue();
	}
	
	
	/** Process the operation queue by processing the next pending operation using the serial queue processor to guarantee the operations are queue serially */
	abstract protected void processOperationQueue();
	
	
	/** call this method when an operation has completed execution */
	protected <ReturnType> void postProcessOperation( final DispatchOperation<ReturnType> operation ) {
		decrementRunningOperationCount();
	}
	
	
	/** Make a callable operation wrapper from a raw runnable operation */
	protected <ReturnType> DispatchOperation<ReturnType> makeDispatchOperation( final Callable<ReturnType> rawOperation ) {
		return makeDispatchOperation( rawOperation, false );
	}
	
	
	/** Make a callable operation wrapper from a raw runnable operation */
	protected DispatchOperation<Void> makeDispatchOperation( final Runnable rawOperation ) {
		return makeDispatchOperation( rawOperation, false );
	}
	
	
	/** Make a callable operation wrapper from a raw runnable operation */
	protected DispatchOperation<Void> makeDispatchOperation( final Runnable rawOperation, final boolean isBarrier ) {
		return DispatchOperation.getInstance( rawOperation, this, isBarrier );
	}
	
	
	/** Make a callable operation wrapper from a raw runnable operation */
	protected <ReturnType> DispatchOperation<ReturnType> makeDispatchOperation( final Callable<ReturnType> rawOperation, final boolean isBarrier ) {
		return DispatchOperation.getInstance( rawOperation, this, isBarrier );
	}
	
	
	/** Event indicating that an operation in this group has completed */
	public <ReturnType> void operationCompleted( final DispatchOperation<ReturnType> operation ) {
		postProcessOperation( operation );
	}
	
	
	/** increment the running operations count */
	synchronized protected void incrementRunningOperationCount() {
		++_runningOperationCount;
	}
	
	
	/** decrement the running operations count */
	synchronized protected void decrementRunningOperationCount() {
		--_runningOperationCount;
	}
}



/** concurrent queue */
class ConcurrentDispatchQueue extends DispatchQueue {
	/** indicates that a barrier operation is currently running */
	private boolean _isRunningBarrierOperation;
	
	
	/** Primary Constructor */
	protected ConcurrentDispatchQueue( final String label, final int priority ) {
		super( label, priority );
	}
	
	
	/** Constructor */
	public ConcurrentDispatchQueue( final String label ) {
		this( label, Thread.NORM_PRIORITY );
	}
	
	
	/** create the executor for dispatching operations */
	protected ExecutorService createDispatchExecutor() {
		return Executors.newCachedThreadPool( DISPATCH_THREAD_FACTORY );
	}
	
	
	/** process the pending operations */
	private void processPendingOperations() {
		while ( _isProcessingPendingOperationQueue ) {		// process (in order) all pending operations which can be processed 
			final DispatchOperation nextOperation = PENDING_OPERATION_QUEUE.peek();
			if ( nextOperation == null ) {
				return;
			}
			else if ( nextOperation.isBarrier() ) {		// if the next operation is a barrier operation, wait until all currently running operations are complete
				if ( _runningOperationCount == 0 ) {
					processNextPendingOperation();
				}
				else {
					return;
				}
			}
			else if ( !_isRunningBarrierOperation ) {	// make sure there is no barrier operation currently running before executing any other operation
				processNextPendingOperation();
			}
			else {
				return;
			}
		}
	}
	
	
	/** call this method when an operation has completed execution */
	protected <ReturnType> void postProcessOperation( final DispatchOperation<ReturnType> operation ) {
		super.postProcessOperation( operation );
		_isRunningBarrierOperation = false;		// this is correct whether the operation just completed is a barrier or another operation just completed
		processOperationQueue();	// make sure the operation queue is processed in case other operations are awaiting completion of this operation
	}

	
	/** process the next pending operation */
	@SuppressWarnings( "unchecked" )	// executor expects a known type but the operations are arbitrary
	private void processNextPendingOperation() {
		final DispatchOperation operation = PENDING_OPERATION_QUEUE.remove();
		if ( operation.isBarrier() ) {
			_isRunningBarrierOperation = true;
		}
		incrementRunningOperationCount();
		DISPATCH_EXECUTOR.submit( operation );
	}
	
	
	/** Process the operation queue by processing the next pending operation using the serial queue processor to guarantee the operations are queue serially */
	protected void processOperationQueue() {
		QUEUE_PROCESSOR.submit( new Runnable() {
			public void run() {
				processPendingOperations();
			}
		});
	}
}


/** Concurrent dispatch queue suitable as a global queue which does not support suspend and resume */
class GlobalDispatchQueue extends ConcurrentDispatchQueue {
	/** high priority global dispatch queue */
	static public final GlobalDispatchQueue HIGH_PRIORITY_DISPATCH_QUEUE;
	
	/** default priority global dispatch queue */
	static public final GlobalDispatchQueue DEFAULT_PRIORITY_DISPATCH_QUEUE;

	/** low priority global dispatch queue */
	static public final GlobalDispatchQueue LOW_PRIORITY_DISPATCH_QUEUE;
	
	/** background priority global dispatch queue */
	static public final GlobalDispatchQueue BACKGROUND_PRIORITY_DISPATCH_QUEUE;
	
	
	// static initializer
	static {
		HIGH_PRIORITY_DISPATCH_QUEUE = new GlobalDispatchQueue( "global high", DISPATCH_QUEUE_PRIORITY_HIGH );
		DEFAULT_PRIORITY_DISPATCH_QUEUE = new GlobalDispatchQueue( "global default", DISPATCH_QUEUE_PRIORITY_DEFAULT );
		LOW_PRIORITY_DISPATCH_QUEUE = new GlobalDispatchQueue( "global low", DISPATCH_QUEUE_PRIORITY_LOW );
		BACKGROUND_PRIORITY_DISPATCH_QUEUE = new GlobalDispatchQueue( "global background", DISPATCH_QUEUE_PRIORITY_BACKGROUND );
	}
	
	
	/** Constructor */
	private GlobalDispatchQueue( final String label, final int priority ) {
		super( label, priority );
	}
	
	
	/** Suspend execution of pending operations. Overriden to do nothing since the main queue cannot be suspended or resumed. */
	public void suspend() {}
	
	
	/** resume execution of pending operations. Overriden to do nothing since the main queue cannot be suspended or resumed. */
	public void resume() {}
	
	
	/** 
	 * Overriden to simply call dispatchAsync() since the global queues do not support barriers. 
	 * @param operation the operation to execute
	 */
	public void dispatchBarrierAsync( final Runnable operation ) {
		dispatchAsync( operation );
	}
	
	
	/** 
	 * Overriden to simply call dispatchSync() since the global queues do not support barriers. 
	 * @param operation the operation to execute
	 */
	public void dispatchBarrierSync( final Runnable operation ) {
		dispatchSync( operation );
	}
}



/** serial queue */
class SerialDispatchQueue extends DispatchQueue {
	/** Constructor */
	public SerialDispatchQueue( final String label ) {
		super( label );
	}
	
	/** create the executor for dispatching operations */
	protected ExecutorService createDispatchExecutor() {
		return Executors.newSingleThreadExecutor( DISPATCH_THREAD_FACTORY );
	}
	
	
	/** called when an operation has completed execution */
	protected <ReturnType> void postProcessOperation( final DispatchOperation<ReturnType> operation ) {
		super.postProcessOperation( operation );
		processOperationQueue();		// attempt to process the next pending operation
	}
	
	
	/** Process the operation queue by processing the next pending operation using the serial queue processor to guarantee the operations are queue serially */
	protected void processOperationQueue() {
		QUEUE_PROCESSOR.submit( new Runnable() {
			public void run() {
				processNextPendingOperation();
			}
		});
	}
	
	
	/** if no process is currently running, process the next pending operation (if any) without blocking */
	protected void processNextPendingOperation() {
		if ( _isProcessingPendingOperationQueue && _runningOperationCount == 0 ) {
			final Callable<?> nextOperation = PENDING_OPERATION_QUEUE.poll();
			if ( nextOperation != null ) {
				incrementRunningOperationCount();
				DISPATCH_EXECUTOR.submit( nextOperation );
			}
		}
	}
}



/** serial queue to the Swing event dispatch thread */
class MainDispatchQueue extends SerialDispatchQueue {
	/** queue on which to submit operations to the Swing dispatch thread */
	final static private MainDispatchQueue MAIN_DISPATCH_QUEUE;
	
	
	// static initializer
	static {
		MAIN_DISPATCH_QUEUE = new MainDispatchQueue();
	}

	
	/** Constructor */
	private MainDispatchQueue() {
		super( "main" );
	}
	
	
	/** Get the default queue */
	static public MainDispatchQueue defaultQueue() {
		return MAIN_DISPATCH_QUEUE;
	}
	
	
	/** create the executor for dispatching operations */
	protected ExecutorService createDispatchExecutor() {
		return null;	// there is no executor since events are submitted to the swing dispatch thread
	}
	
	
	/** Suspend execution of pending operations. Overriden to do nothing since the main queue cannot be suspended or resumed. */
	public void suspend() {}
	
	
	/** resume execution of pending operations. Overriden to do nothing since the main queue cannot be suspended or resumed. */
	public void resume() {}
	
	
	/** submit the operation for execution on the queue and wait for it to complete */
	public <ReturnType> ReturnType dispatchSync( final Callable<ReturnType> rawOperation ) {
		final CallRunnable<ReturnType> runnableOperation = new CallRunnable<ReturnType>( rawOperation );
		dispatchSync( runnableOperation );
		return runnableOperation.getResult();
	}
	
	
	/** process the next pending operation if any */
	protected void processNextPendingOperation() {
		if ( _isProcessingPendingOperationQueue && _runningOperationCount == 0 ) {
			final Callable<?> nextOperation = PENDING_OPERATION_QUEUE.poll();
			if ( nextOperation != null ) {
				incrementRunningOperationCount();
				final Runnable runnableOperation = new Runnable() {
					public void run() {
						try {
							nextOperation.call();
						}
						catch( Exception exception ) {
							throw new RuntimeException( exception );
						}
					}
				};
				SwingUtilities.invokeLater( runnableOperation );
			}
		}
	}
}



/** Wrapper of a callable object as Runnable */
class CallRunnable<ReturnType> implements Runnable {
	/** result returned by the call */
	private ReturnType _result;
	
	/** wrapped callable */
	private final Callable<ReturnType> WRAPPED_CALLABLE;
	
	
	/** Constructor */
	public CallRunnable( final Callable<ReturnType> callable ) {
		WRAPPED_CALLABLE = callable;
	}
	
	
	/** Get the result */
	public ReturnType getResult() {
		return _result;
	}
	
	
	/** run the call */
	public void run() {
		try {
			_result = WRAPPED_CALLABLE.call();
		}
		catch( Exception exception ) {
			throw new RuntimeException( exception );
		}
	}
}



/** thread factory for dispatch queues */
class DispatchThreadFactory implements ThreadFactory {
	/** target queue for the thread */
	final private DispatchQueue DISPATCH_QUEUE;
	
	/** thread priority */
	final private int PRIORITY;
	
	
	/** Constructor */
	public DispatchThreadFactory( final DispatchQueue queue, final int priority ) {
		DISPATCH_QUEUE = queue;
		PRIORITY = priority;
	}
	
	
	/** create a new thread for the queue */
	public Thread newThread( final Runnable handler ) {
		final Thread thread = new DispatchThread( DISPATCH_QUEUE, handler );
		if ( PRIORITY != Thread.NORM_PRIORITY ) {
			try {
				thread.setPriority( PRIORITY );
			}
			catch( Exception exception ) {
				throw new RuntimeException( "Cannot set priority on new thread." );
			}
		}
		return thread;
	}
}



/** thread for dispatch queues */
class DispatchThread extends Thread {
	/** stores the current queue */
	private static final ThreadLocal<DispatchQueue> QUEUE_THREAD_LOCAL;
	
	
	// static initializer
	static {
		QUEUE_THREAD_LOCAL = new ThreadLocal<DispatchQueue>();
	}
	
	
	/** constructor */
	public DispatchThread( final DispatchQueue queue, final Runnable handler ) {
		super( handler );
		
		QUEUE_THREAD_LOCAL.set( queue );
	}
	
	
	/** get the thread's queue */
	public DispatchQueue getQueue() {
		return QUEUE_THREAD_LOCAL.get();
	}
}

