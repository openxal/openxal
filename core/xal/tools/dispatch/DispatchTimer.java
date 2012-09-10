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
	
	/** indicates whether this timer is suspended */
	private volatile boolean _isSuspended;
	
	/** milliseconds of the interval between when the timer fires */
	private volatile long _milliInterval;
	
	/** nanoseconds of the interval between when the timer fires */
	private volatile int _nanoInterval;
	
	/** next scheduled event */
	private ScheduledEvent _nextScheduledEvent;
	
	
	/** Constructor */
    public DispatchTimer( final DispatchQueue eventQueue, final Runnable eventHandler ) {
		EVENT_QUEUE = eventQueue;
		_eventHandler = eventHandler;
		
		SCHEDULE_QUEUE = DispatchQueue.createSerialQueue( null );
		
		_isSuspended = false;
		_isCanceled = false;
		
		_nextScheduledEvent = null;
    }


	/** dispose of this timer's resources */
	public void dispose() {
		if ( !_isSuspended ) {
			suspend();
		}

		SCHEDULE_QUEUE.dispose();
	}


	/** release resources held by this timer */
	protected void finalize() throws Throwable {
		dispose();
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
		cancelNextScheduledEvent();		// since the start time is changing, we need to cancel the next pending event
		
		SCHEDULE_QUEUE.dispatchAsync( new Runnable() {
			public void run() {
				_milliInterval = milliInterval;
				_nanoInterval = nanoInterval;
				
				final ScheduledEvent nextScheduledEvent = new ScheduledEvent();		// schedule an event that will execute immediately upon dispatch
				_nextScheduledEvent = nextScheduledEvent;
				SCHEDULE_QUEUE.dispatchAfter( startTime, nextScheduledEvent );		// dispatch after the start time
			}
		});
	}
	
	
	/** Schedule the next event */
	private void scheduleNextEvent() {
		SCHEDULE_QUEUE.dispatchAsync( new Runnable() {
			public void run() {
				final ScheduledEvent nextScheduledEvent = new ScheduledEvent( true );	// schedule an event that will execute at the specified interval
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
	}
	
	
	/** suspend this timer */
	public void suspend() {
		_isSuspended = true;
	}
	
	
	/** resume this timer */
	public void resume() {
		_isSuspended = false;
		
		final ScheduledEvent nextScheduledEvent = _nextScheduledEvent;
		if ( nextScheduledEvent != null ) {
			nextScheduledEvent.resume();
		}
	}
	
	
	
	/** Event scheduled for execution */
	private class ScheduledEvent implements Runnable {		
		/** indicates whether the event would have fired if it had not been suspended */
		private boolean _isPastDue;
		
		/** indicates whether this event is canceled */
		private volatile boolean _isCanceled;
		
		/** indicates whether this event fires immediately or waits for the interval */
		private volatile boolean _waitsForInterval;
		
		
		/** Primary Constructor */
		public ScheduledEvent( final boolean waitsForInterval ) {
			_isCanceled = false;
			_isPastDue = false;
			_waitsForInterval = waitsForInterval;
		}
		
		
		/** Constructor */
		public ScheduledEvent() {
			this( false );
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
				if ( _isSuspended ) {
					_isPastDue = true;
				}
				else {
					final Runnable eventHandler = _eventHandler;
					if ( eventHandler != null ) {
						EVENT_QUEUE.dispatchAsync( eventHandler );
					}
					if ( !_isCanceled )  scheduleNextEvent();
				}
			}
		}
		
		
		/** Executes the event */
		public void run() {
			if ( _waitsForInterval ) {
				synchronized( SCHEDULE_QUEUE ) {
					if ( !_isCanceled ) {
						try {
							final long maxTime = new Date().getTime() + _milliInterval;		// maximum millisecond time limit
							while ( true ) {
								final long milliTimeout = maxTime - new Date().getTime();	// milliseconds left to wait
								final int nanoTimeout = _nanoInterval;
								if ( milliTimeout >= 0 && ( milliTimeout > 0 || nanoTimeout > 0 ) ) {	// if remaining milliseconds is zero or greater then wait longer and at least one of nanoTimeout or milliTimeout is greater than zero
//									System.out.println( "Will wait " + milliTimeout + " msec, " + _nanoInterval + " ns"  );
									SCHEDULE_QUEUE.wait( milliTimeout, nanoTimeout );		// wait the remaining millisecond timeout and the nano interval
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
			else {
				dispatchEventIfEnabled();
			}
			
		}
	}
}

