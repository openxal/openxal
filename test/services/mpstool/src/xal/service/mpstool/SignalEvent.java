/*
 *  SignalEvent.java
 *
 *  Created on Tue Apr 13 14:41:04 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.service.mpstool;

import xal.ca.Timestamp;
import xal.ca.ChannelTimeRecord;


/**
 * SignalEvent holds one event for a signal.
 *
 * @author    tap
 */
public class SignalEvent implements Comparable<Object> {
	/** the event's timestamp */
	protected final Timestamp _timestamp;

	/** the PV name */
	protected final String _signal;


	/**
	 * Constructor
	 *
	 * @param signal     The PV signal
	 * @param timestamp  The timestamp of the event
	 */
	public SignalEvent( String signal, Timestamp timestamp ) {
		_timestamp = timestamp;
		_signal = signal;
	}


	/**
	 * Get the signal
	 *
	 * @return   the signal
	 */
	public String getSignal() {
		return _signal;
	}


	/**
	 * Get the timestamp of the signal event
	 *
	 * @return   the timestamp of the signal event
	 */
	public Timestamp getTimestamp() {
		return _timestamp;
	}


	/**
	 * compare timestamp of other SignalEvent instance with this one's timestamp
	 *
	 * @param other  the SignalEvent instance against which to compare this one
	 * @return       -1 if this is earlier than the specified record or +1 if it is
	 *      later or the same
	 */
	public int compareTo( Object other ) {
		Timestamp otherTimestamp = ( (SignalEvent)other )._timestamp;
		// Do not provide 0 val for the case when they are ==
		return ( _timestamp.compareTo( otherTimestamp ) < 0 ) ? -1 : 1;
	}


	/**
	 * Generate a string description of this instance.
	 *
	 * @return   description of this instance
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("signal: " + _signal);
		buffer.append(", timestamp: " + _timestamp);
		
		return buffer.toString();
	}
}

