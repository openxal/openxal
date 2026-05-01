/*
 * TraceEvent.java
 *
 * Created on Mon Nov 03 11:13:15 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;


/**
 * TraceEvent is an interface which trace sources must implement to provide an event which wraps
 * a captured waveform.
 *
 * @author  tap
 */
interface TraceEvent {
	/**
	 * Get the source of the trace event.
	 * @return the source of the trace event.
	 */
	public TraceSource getSource();
	
	
	/**
	 * Get the raw trace.
	 * @return the raw trace.
	 */
	public double[] getRawTrace();
	
	
	/**
	 * Get the default trace with appropriate scale and offsets set by the user.
	 * @return Teh default trace.
	 */
	public double[] getDefaultTrace();
	
	
	/**
	 * Get the element times.
	 * @return the element times which specify the time of each waveform element.
	 */
	public double[] getElementTimes();
}


