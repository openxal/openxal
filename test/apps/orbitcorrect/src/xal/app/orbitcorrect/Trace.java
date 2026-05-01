/*
 * Trace.java
 *
 * Created on Fri Jun 11 15:48:38 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import xal.tools.ArrayTool;

import java.awt.Shape;
import java.util.Date;


/**
 * Trace
 *
 * @author  tap
 * @since Jun 11, 2004
 */
public class Trace {
	/** the value array */
	protected double[] _values;
	
	/** the position array */
	protected double[] _positions;
	
	/** the trace's label */
	final protected String _label;
	
	/** mark for rendering the points */
	final protected Shape _pointMark;
	
	/** dash pattern for rendering the line */
	final protected float[] _dashPattern;
	
	/** the trace's time stamp */
	protected Date _timeStamp;
	
	
	/**
	 * Primary constructor
	 * @param label the trace's label
	 * @param pointMark for rendering points
	 * @param dashPattern for rendering the line
	 * @param timeStamp the trace's time stamp
	 * @param positions the array of positions for the trace
	 * @param values the array of trace values
	 */
	public Trace( final String label, final Shape pointMark, final float[] dashPattern, final Date timeStamp, final double[] positions, final double[] values ) {
		_label = label;
		_pointMark = pointMark;
		
		if ( dashPattern != null ) {
			_dashPattern = dashPattern;
		}
		else {
			_dashPattern = new float[1];
			_dashPattern[0] = 1.0f;
		}
		
		_timeStamp = timeStamp;
		_positions = positions;
		_values = values;
	}
	
	
	/**
	 * Constructor with no data
	 * @param label the trace's label
	 * @param pointMark for rendering points
	 * @param dashPattern for rendering the line
	 */
	public Trace( final String label, final Shape pointMark, final float[] dashPattern ) {
		this( label, pointMark, dashPattern, new Date(), new double[0], new double[0] );
	}
	
	
	/**
	 * Constructor with no data
	 * @param label the trace's label
	 */
	public Trace( final String label ) {
		this( label, null, null );
	}
	
	
	/** get the mark for displaying points */
	public Shape getPointMark() {
		return _pointMark;
	}
	
	
	/** get the dash pattern used for displaying this trace's line */
	public float[] getDashPattern() {
		return _dashPattern;
	}
	
	
	/**
	 * Get the trace's position array.
	 * @return the trace's position array
	 */
	public double[] getPositions() {
		return _positions;
	}
	
	
	/**
	 * Get the trace's values.
	 * @return the trace's values
	 */
	public double[] getValues() {
		return _values;
	}
	
	
	/**
	 * Get the label
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}
	
	
	/**
	 * Get the trace's time stamp
	 * @return the trace's time stamp
	 */
	public Date getTimeStamp() {
		return _timeStamp;
	}
	
	
	/**
	 * Generate a string representation of this trace.
	 * @return a string representation of the this trace
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append( "trace: (" );
		buffer.append( "label: " + _label );
		buffer.append( "time stamp: " + _timeStamp );
		buffer.append( ", positions: " + ArrayTool.asString( _positions ) );
		buffer.append( ", values: " + ArrayTool.asString( _values ) );
		buffer.append( ")" );
		
		return buffer.toString();
	}
}

