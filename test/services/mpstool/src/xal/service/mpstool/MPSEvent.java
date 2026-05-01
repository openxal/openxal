/*
 *  MPSEvent.java
 *
 *  Created on Tue Apr 13 14:42:45 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.service.mpstool;

import java.util.*;

import xal.ca.ChannelTimeRecord;
import xal.tools.correlator.Correlation;


/**
 * MPSEvent
 *
 * @author    tap
 */
public class MPSEvent {
	/** mean timestamp of the correlated signal events */
	protected final Date _timestamp;

	/** sorted list of correlated signal events */
	protected final List<SignalEvent> _signalEvents;


	/**
	 * Constructor
	 *
	 * @param correlation  The correlated MPS trips defining an MPS event.
	 */
	public MPSEvent( Correlation<ChannelTimeRecord> correlation ) {
		_timestamp = correlation.meanDate();

		Collection<String> signals = correlation.names();
		_signalEvents = new ArrayList<>();

        for(String signal : signals) {
            ChannelTimeRecord record = correlation.getRecord( signal );
            SignalEvent signalEvent = new SignalEvent( signal, record.getTimestamp() );
            _signalEvents.add( signalEvent );
        }

		Collections.sort( _signalEvents );
	}


	/**
	 * Get the mean timestamp of the correlated signal events
	 *
	 * @return   the mean timestamp of the correlated signal events
	 */
	public Date getTimestamp() {
		return _timestamp;
	}


	/**
	 * Get the sorted list of correlated signal events
	 *
	 * @return   the list of correlated signal events
	 */
	public List<SignalEvent> getSignalEvents() {
		return _signalEvents;
	}


	/**
	 * Get the first signal event (suspect as the likely cause of correlated MPS
	 * trips)
	 *
	 * @return   the first signal event
	 */
	public SignalEvent getFirstSignalEvent() {
		return _signalEvents.get( 0 );
	}


	/**
	 * Generate a description of this instance.
	 *
	 * @return   description of this event
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append( "timestamp: " + _timestamp );
		buffer.append( "signal events: " + _signalEvents.toString() );

		return buffer.toString();
	}
}

