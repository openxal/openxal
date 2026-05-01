//
// DispatchTimer.java
// xal
//
// Created by Tom Pelaia on 5/3/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.dispatch;

import java.util.Date;


/** DispatchTimer */
public class DispatchTimer {
	/** possible dispatch modes */
	public enum DispatchTimerMode { FIXED_RATE, COALESCING }

	/** possible run states of the dispatch timer */
	private enum DispatchTimerRunState { PROCESSING, SUSPENDED, DISPOSED }

	/** queue to which to dispatch events */
	final private DispatchQueue EVENT_QUEUE;

	/** internal queue used to schedule the timer events */
	final private DispatchQueue SCHEDULE_QUEUE;

	/** event to execute when this timer fires */
	private Runnable _eventHandler;

	/** event to execute when this timer is canceled */
	private Runnable _cancelHandler;

	/** indicates whether this timer is canceled */
	private volatile boolean _isCanceled;

	/** run state of this timer */
	private volatile DispatchTimerRunState _runState;

	/** milliseconds of the interval between when the timer fires */
	private volatile long _milliInterval;

	/** nanoseconds of the interval between when the timer fires */
	private volatile int _nanoInterval;

	/** next scheduled event */
	private ScheduledEvent _nextScheduledEvent;

	/** delegate for handling the specified dispatch mode */
	final private DispatchTimerModeDelegate DISPATCH_MODE_DELEGATE;


	/** Primary Constructor */
    public DispatchTimer( final DispatchTimerMode dispatchMode, final DispatchQueue eventQueue, final Runnable eventHandler ) {
		DISPATCH_MODE_DELEGATE = getDispatchModeDelegate( dispatchMode );

		EVENT_QUEUE = eventQueue;
		_eventHandler = eventHandler;

		SCHEDULE_QUEUE = DispatchQueue.createSerialQueue( "Dispatch Timer Scheduling Queue" );

		_runState = DispatchTimerRunState.PROCESSING;
		_isCanceled = false;

		_nextScheduledEvent = null;
    }


	/** Constructor */
    public DispatchTimer( final DispatchQueue eventQueue, final Runnable eventHandler ) {
		this ( DispatchTimerMode.FIXED_RATE, eventQueue, eventHandler );
    }


	/** Create a new fixed rate timer */
	static public DispatchTimer getFixedRateInstance( final DispatchQueue eventQueue, final Runnable eventHandler ) {
		return new DispatchTimer( DispatchTimerMode.FIXED_RATE, eventQueue, eventHandler );
	}


	/** Create a new coalescing timer */
	static public DispatchTimer getCoalescingInstance( final DispatchQueue eventQueue, final Runnable eventHandler ) {
		return new DispatchTimer( DispatchTimerMode.COALESCING, eventQueue, eventHandler );
	}


	/** Get the dispatch mode delegate for the corresponding mode enum */
	private DispatchTimerModeDelegate getDispatchModeDelegate( final DispatchTimerMode dispatchMode ) {
		switch( dispatchMode ) {
			case FIXED_RATE:
				return new DispatchTimerFixedRateDispatch();
			case COALESCING:
				return new DispatchTimerCoalescingDispatch();
			default:
				return new DispatchTimerFixedRateDispatch();
		}
	}


	/** release resources held by this timer */
	protected void finalize() throws Throwable {
		try {
			dispose();
		}
		finally {
			super.finalize();
		}
	}


	/** Set the event handler which is dispatched to the queue when the timer fires */
	public void setEventHandler( final Runnable eventHandler ) {
		_eventHandler = eventHandler;
	}


	/** Set the cancel handler which is dipsatched to the queue when the timer is canceled */
	public void setCancelHandler( final Runnable cancelHandler ) {
		_cancelHandler = cancelHandler;
	}


	/**
	 * Start the timer now and set the interval between when the timer fires.
	 * @param milliInterval milliseconds of the interval between when the timer fires
	 * @param nanoInterval nanoseconds of the interval between when the timer fires
	 */
	public void startNowWithInterval( final long milliInterval, final int nanoInterval ) {
		setStartTimeAndInterval( new Date(), milliInterval, nanoInterval );
	}


	/**
	 * Set the time at which this timer starts and the interval between when the timer fires.
	 * @param milliInterval milliseconds of the interval between when the timer fires
	 * @param nanoInterval nanoseconds of the interval between when the timer fires
	 */
	public void setStartTimeAndInterval( final Date startTime, final long milliInterval, final int nanoInterval ) {
		cancelNextScheduledEvent();		// Since the start time is changing, we need to immediately cancel the next pending event here plus later on the schedule queue (see code below).

		SCHEDULE_QUEUE.dispatchAsync( new Runnable() {
			public void run() {
				cancelNextScheduledEvent();		// Cancel any currently scheduled event on the schedule queue in addition to immediately (see code above)

				_milliInterval = milliInterval;
				_nanoInterval = nanoInterval;

				final ScheduledEvent nextScheduledEvent = new ScheduledEvent( startTime );		// schedule an event that will execute immediately upon dispatch
				_nextScheduledEvent = nextScheduledEvent;
				SCHEDULE_QUEUE.dispatchAfter( startTime, nextScheduledEvent );		// dispatch after the start time
			}
		});
	}


	/** Schedule the next event */
	private void scheduleNextEvent( final ScheduledEvent nextScheduledEvent ) {
		SCHEDULE_QUEUE.dispatchAsync( new Runnable() {
			public void run() {
				_nextScheduledEvent = nextScheduledEvent;
				SCHEDULE_QUEUE.dispatchAsync( nextScheduledEvent );
			}
		});
	}


	/** Cancel this timer */
	public void cancel() {
		_isCanceled = true;

		cancelNextScheduledEvent();

		final Runnable cancelHandler = _cancelHandler;
		if ( cancelHandler != null ) {
			EVENT_QUEUE.dispatchAsync( cancelHandler );
		}
	}


	/** Cancel the next scheduled event if any */
	private void cancelNextScheduledEvent() {
		final ScheduledEvent nextScheduledEvent = _nextScheduledEvent;
		if ( nextScheduledEvent != null ) {
			nextScheduledEvent.cancel();
		}
		_nextScheduledEvent = null;
	}


	/** Determines whether this queue is suspended (disposed implies suspended) */
	public boolean isSuspended() {
		return _runState != DispatchTimerRunState.PROCESSING;	// disposed states are also suspended
	}


	/** suspend this timer if it is processing (do nothing if disposed or already suspended) */
	public void suspend() {
		switch( _runState ) {
			case PROCESSING:
				_runState = DispatchTimerRunState.SUSPENDED;
				break;
			default:
				break;
		}
	}


	/** resume this timer */
	public void resume() {
		switch( _runState ) {
			case SUSPENDED:
				_runState = DispatchTimerRunState.PROCESSING;
				resumeScheduling();
				break;
			case DISPOSED:
				throw new RuntimeException( "Cannot resume the disposed dispatch timer." );
			default:
				break;
		}
	}


	/** resume scheduling events */
	private void resumeScheduling() {
		final ScheduledEvent nextScheduledEvent = _nextScheduledEvent;
		if ( nextScheduledEvent != null ) {
			nextScheduledEvent.resume();
		}
	}


	/** dispose of this timer's resources */
	public void dispose() {
		_runState = DispatchTimerRunState.DISPOSED;

		SCHEDULE_QUEUE.dispose();
	}


	/** determine whether this timer has been disposed */
	public boolean isDisposed() {
		return _runState == DispatchTimerRunState.DISPOSED;
	}



	/** Event scheduled for execution */
	private class ScheduledEvent implements Runnable {
		/** indicates whether the event would have fired if it had not been suspended */
		private boolean _isPastDue;

		/** indicates whether this event is canceled */
		private volatile boolean _isCanceled;

		/** time at which the event should fire */
		private final long TARGET_TIME;

		/** additional nanosecond time */
		private final int NANO_OFFSET;


		/** Primary Constructor */
		public ScheduledEvent( final long targetTime, final int nanoOffset ) {
			TARGET_TIME = targetTime;
			NANO_OFFSET = nanoOffset;

			_isCanceled = false;
			_isPastDue = false;
		}


		/** Constructor */
		public ScheduledEvent( final Date targetDate, final int nanoOffset ) {
			this( targetDate.getTime(), nanoOffset );
		}


		/** Constructor */
		public ScheduledEvent( final long targetTime ) {
			this( targetTime, 0 );
		}


		/** Constructor */
		public ScheduledEvent( final Date targetDate ) {
			this( targetDate.getTime() );
		}


		/** Constructor with event scheduled immediately */
		public ScheduledEvent() {
			this( new Date() );
		}


		/** Get the target time */
		public long getTargetTime() {
			return TARGET_TIME;
		}


		/** Get the next scheduled event relative to this one using the timer's millisecond and nanosecond intervals */
		public ScheduledEvent nextScheduledEvent() {
			return nextScheduledEvent( _milliInterval, _nanoInterval );
		}


		/** Get the next scheduled event relative to this one using the specified delays */
		public ScheduledEvent nextScheduledEvent( final long milliDelay, final int nanoDelay ) {
			// Calculate the new target time and nano offset. If nanos accumulate more than a millisecond, shift that amount to the milliseconds.
			final int nanoShift = NANO_OFFSET + nanoDelay;
			final long targetTime = TARGET_TIME + milliDelay + nanoShift / 1000000;
			final int nanoOffset = nanoShift % 1000000;
			return new ScheduledEvent( targetTime, nanoOffset );
		}


		/** Cancel this timer */
		public void cancel() {
			_isCanceled = true;
			synchronized( SCHEDULE_QUEUE ) {
				try {
					SCHEDULE_QUEUE.notifyAll();
				}
				catch( Exception exception  ) {}
			}
		}


		/** Resume the timer and fire the event if it is past due */
		public void resume() {
			if ( _isPastDue ) {
				dispatchEventIfEnabled();
			}
		}


		/** dispatch the event if timer is active */
		private void dispatchEventIfEnabled() {
			if ( !_isCanceled ) {
				switch ( _runState ) {
					case PROCESSING:
						DISPATCH_MODE_DELEGATE.processTimerEvent( _eventHandler );
						break;
					default:
						_isPastDue = true;
						break;
				}
			}
		}


		/** Executes the event */
		public void run() {
			synchronized( SCHEDULE_QUEUE ) {
				if ( !_isCanceled ) {
					try {
						while ( !_isCanceled ) {
							final long milliTimeout = TARGET_TIME - new Date().getTime();	// milliseconds left to wait
							final int nanoTimeout = NANO_OFFSET;

							if ( milliTimeout > 0 ) {
								SCHEDULE_QUEUE.wait( milliTimeout, 0 );		// wait the remaining millisecond timeout
							}
							else if ( milliTimeout == 0 && nanoTimeout > 0 ) {
								SCHEDULE_QUEUE.wait( 0, nanoTimeout );		// wait the remaining nano interval
								break;		// assume the nano timeout was successful as we have no way to verify otherwise
							}
							else {
								break;
							}
						}
					}
					catch ( Exception exception ) {
						exception.printStackTrace();
					}
					finally {
						dispatchEventIfEnabled();
					}
				}
			}
		}
	}


	/** Make the next scheduled event */
	private ScheduledEvent makeNextScheduledEvent() {
		final ScheduledEvent nextEvent = _nextScheduledEvent;
		return nextEvent != null ? nextEvent.nextScheduledEvent() : null;
	}



	/** Delegate interface for handling the various dispatch modes */
	interface DispatchTimerModeDelegate {
		/** process the current timer event */
		public void processTimerEvent( final Runnable eventHandler );
	}



	/** Dispatches events at a fixed rate */
	private class DispatchTimerFixedRateDispatch implements DispatchTimerModeDelegate {
		/** process the current timer event */
		public void processTimerEvent( final Runnable eventHandler ) {
			final ScheduledEvent nextEvent = makeNextScheduledEvent();

			if ( eventHandler != null ) {
				EVENT_QUEUE.dispatchAsync( eventHandler );
			}

			if ( !_isCanceled && nextEvent != null )  scheduleNextEvent( nextEvent );
		}
	}



	/** Dispatches events at a fixed rate but coalesces events that are concurrent thus preventing events from backing up in the queue */
	private class DispatchTimerCoalescingDispatch implements DispatchTimerModeDelegate {
		/** process the current timer event */
		public void processTimerEvent( final Runnable eventHandler ) {
			final ScheduledEvent nextEvent = makeNextScheduledEvent();

			try {
				if ( eventHandler != null ) {
					EVENT_QUEUE.dispatchSync( eventHandler );
				}
			}
			finally {
				if ( !_isCanceled && nextEvent != null )  scheduleNextEvent( nextEvent );
			}
		}
	}
}
