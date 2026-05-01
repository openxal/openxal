/*
 * SignalEvent.java
 *
 * Created on Thu Mar 25 17:08:06 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.mpsclient;

import java.math.BigDecimal;
import xal.ca.Timestamp;


/**
 * SignalEvent encapsulates an MPS event for one signal within the MPS event
 *
 * @author  tap
 */
public class SignalEvent {
	protected final String _signal;
	protected final Timestamp _timestamp;
	
	
	/**
	 * Constructor
	 * @param signal The signal in the event
	 * @param timestamp The timestamp of the signal event
	 */
	public SignalEvent(String signal, BigDecimal timestamp) {
		_signal = signal;
		_timestamp = new Timestamp(timestamp);
	}
	
	
	/**
	 * Get the signal name
	 * @return the signal name
	 */
	public String getSignal() {
		return _signal;
	}
	
	
	/**
	 * Get the event timestamp
	 * @return the event timestamp
	 */
	public String getTimestamp() {
		return _timestamp.toString();
	}
	
	
	/**
	 * Override toString() to provide a description of the event
	 * @return a description of the event
	 */
	public String toString() {
		return _signal + "   " + _timestamp;
	}
}

