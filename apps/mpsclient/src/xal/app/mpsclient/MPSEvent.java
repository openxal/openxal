/*
 * MPSEvent.java
 *
 * Created on Wed Apr 14 10:17:34 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.mpsclient;

import java.util.*;
import java.text.*;


/**
 * MPSEvent
 *
 * @author  tap
 */
public class MPSEvent {
	/** date formatter for displaying timestamps */
	static final protected DateFormat TIMESTAMP_FORMAT;
	
	final protected Date _timestamp;
	final protected List<SignalEvent> _signalEvents;
	
	
	/**
	 * static initializer
	 */
	static {
		TIMESTAMP_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
	}
	
	
	/**
	 * Constructor
	 */
	public MPSEvent(Date timestamp, List<SignalEvent> signalEvents) {
		_timestamp = timestamp;
		_signalEvents = signalEvents;
	}
	
	
	/** 
	 * Get the timestamp of the MPS event
	 * @return the timestamp of the MPS event
	 */
	public Date getTimestamp() {
		return _timestamp;
	}
	
	
	/**
	 * Get the list of signal events
	 * @return the list of signal events
	 */
	public List<SignalEvent> getSignalEvents() {
		return _signalEvents;
	}
	
	
	/**
	 * Get the signal event at the specified index
	 * @param index the index identifying the signal event to get
	 * @return the signal event at the specified index
	 */
	public SignalEvent getSignalEvent(final int index) {
		return _signalEvents.get(index);
	}
	
	
	/**
	 * Get the number of signal events
	 * @return the number of signal events
	 */
	public int getSignalEventCount() {
		return _signalEvents.size();
	}
	
	
	/**
	 * Override toString() to provide a description of the event
	 * @return a description of the event
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append( TIMESTAMP_FORMAT.format(_timestamp) + "\n");
		for ( Iterator<SignalEvent> iter = _signalEvents.iterator() ; iter.hasNext() ; ) {
			buffer.append( iter.next() );
			buffer.append("\n");
		}
		
		return buffer.toString();
	}
}

