/*
 * ChannelTraceEvent.java
 *
 * Created on Mon Nov 03 11:33:17 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import xal.tools.ArrayMath;


/**
 * ChannelTraceEvent is an event which wraps the capture of a trace from a channel model.
 *
 * @author  tap
 */
public class ChannelTraceEvent implements TraceEvent {
	final protected ChannelModel _source;
	final protected double scale;
	final protected double offset;
	protected double[] _defaultTrace;
	final protected double[] _rawTrace;
	final protected double[] _elementTimes;
	
	
	/**
	 * Constructor of a ChannelTraceEvent
	 */
	public ChannelTraceEvent(ChannelModel source, double[] trace, double[] elementTimes) {
		_source = source;
		scale = source.getSignalScale();
		offset = source.getSignalOffset();
		_rawTrace = trace;
		_elementTimes = elementTimes;
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
		if ( _defaultTrace == null && _rawTrace != null ) {
			_defaultTrace = ArrayMath.transform(_rawTrace, scale, offset);
		}
		return _defaultTrace;
	}
	
	
	/**
	 * Get the element times.
	 * @return the element times which specify the time of each waveform element.
	 */
	public double[] getElementTimes() {
		return _elementTimes;
	}
}

