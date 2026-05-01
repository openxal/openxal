/*
 * MutableTrace.java
 *
 * Created on Tue Jul 13 09:49:49 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import java.awt.Shape;
import java.util.Date;


/**
 * MutableTrace
 *
 * @author  tap
 * @since Jul 13, 2004
 */
public class MutableTrace extends Trace {
	/** lock for synchronizing access from competing threads that want to access the trace */
	protected Object _lock;
	
	/** cache of the latest trace */
	protected Trace _cachedTrace;
	
	
	/**
	 * Primary constructor
	 * @param label the trace's label
	 * @param pointMark for rendering points
	 * @param dashPattern for rendering the line
	 * @param timeStamp the trace's time stamp
	 * @param positions the array of positions for the trace
	 * @param values the array of trace values
	 */
	public MutableTrace( final String label, final Shape pointMark, final float[] dashPattern, final Date timeStamp, final double[] positions, final double[] values ) {
		super( label, pointMark, dashPattern, timeStamp, positions, values );
		_cachedTrace = null;
		_lock = new Object();
	}
	
	
	/**
	 * Constructor with no data
	 * @param label the trace's label
	 * @param pointMark for rendering points
	 * @param dashPattern for rendering the line
	 */
	public MutableTrace( final String label, final Shape pointMark, final float[] dashPattern ) {
		this( label, pointMark, dashPattern, new Date(), new double[0], new double[0] );
	}
	
	
	/**
	 * Constructor with no data
	 * @param label the trace's label
	 */
	public MutableTrace( final String label ) {
		this( label, null, null );
	}
	
	
	/**
	 * Clear the trace data.
	 */
	public void clear() {
		update( new Date(), new double[0], new double[0] );
	}
	
	
	/**
	 * Update the positions and values of the trace.
	 * @param positions the new position array
	 * @param values the new value array
	 */
	public void update( final Date timeStamp, final double[] positions, final double[] values ) {
		synchronized(_lock) {
			_timeStamp = timeStamp;
			_positions = positions;
			_values = values;
			_cachedTrace = null;	// clear the cache since the trace data has changed
		}
	}
	
	
	/**
	 * Generate an immutable trace representation of this trace.
	 * @return an immutable trace representation of this trace
	 */
	public Trace getTrace() {
		synchronized(_lock) {
			// see if we need to generate a new trace or if we can use the cached trace
			if ( _cachedTrace == null ) {
				double[] positions = new double[_positions.length];
				double[] values = new double[_values.length];
				System.arraycopy(_positions, 0, positions, 0, _positions.length);
				System.arraycopy(_values, 0, values, 0, _values.length);
				_cachedTrace = new Trace( _label, _pointMark, _dashPattern, _timeStamp, positions, values );
			}
			return _cachedTrace;
		}
	}
}

