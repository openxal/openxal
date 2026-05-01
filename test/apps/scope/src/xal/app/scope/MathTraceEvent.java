/*
 * MathTraceEvent.java
 *
 * Created on Mon Nov 03 11:33:41 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import xal.tools.correlator.*;


/**
 * MathTraceEvent is a wrapper of the capture of a waveform from a math model.
 *
 * @author  tap
 */
public class MathTraceEvent implements TraceEvent {
	final protected MathModel _source;
	final protected double[] _rawTrace;
	final protected double[] _elementTimes;
	
	
	/**
	 * Constructor of a MathTraceEvent
	 */
	public MathTraceEvent( final MathModel source, final double[] trace, final double[] elementTimes ) {
		_source = source;
		if ( trace != null ) {
			_rawTrace = trace;
			_elementTimes = elementTimes;
		}
		else {
			_rawTrace = new double[0];
			_elementTimes = new double[0];
		}
	}
	
	
	/**
	 * Get the source of the trace event.
	 * @return the source of the trace event.
	 */
	public TraceSource getSource() {
		return _source;
	}
	
	
	/**
	 * Get the raw trace.
	 * @return the raw trace.
	 */
	public double[] getRawTrace() {
		return _rawTrace;
	}
	
	
	/**
	 * Get the default trace with appropriate scale and offsets set by the user.
	 * @return Teh default trace.
	 */
	public double[] getDefaultTrace() {
		return _rawTrace;
	}
	
	
	/**
	 * Get the element times.
	 * @return the element times which specify the time of each waveform element.
	 */
	public double[] getElementTimes() {
		return _elementTimes;
	}
}

