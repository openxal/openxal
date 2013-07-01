/*
 *  OrbitTraceAdaptor.java
 *
 *  Created on Thu Jul 15 14:00:36 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.smf.AcceleratorSeq;

import java.awt.Polygon;
import java.awt.Shape;
import java.util.*;


/**
 * OrbitTraceAdaptor maps an orbit to a X, Y and amplitude traces.
 * @author   tap
 * @since    Jul 15, 2004
 */
public class OrbitTraceAdaptor implements OrbitSourceListener {
	/** type of trace source for X */
	final static public String X_AVG_TYPE = "BPM X AVG";
	
	/** type of trace source for Y */
	final static public String Y_AVG_TYPE = "BPM Y AVG";
	
	/** type of trace source for amplitude */
	final static public String AMP_AVG_TYPE = "BPM AMP AVG";
	
	/** trace source index of the X data */
	final protected int X_AVG_TRACE_INDEX = 0;
	
	/** trace source index of the Y data */
	final protected int Y_AVG_TRACE_INDEX = 1;
	
	/** trace source index of the amplitude data */
	final protected int AMP_AVG_TRACE_INDEX = 2;
	
	/** synchronization lock */
	final protected Object LOCK;
	
	/** beam excursion/orbit adaptor */
	final protected BeamExcursionOrbitAdaptor BEAM_EXCURSION_ORBIT_ADAPTOR;
	
	/** orbit source */
	protected OrbitSource _orbitSource;

	/** latest orbit */
	protected Orbit _orbit;

	/** x, y and amplitude traces */
	protected MutableTrace _xAvgTrace, _yAvgTrace, _ampAvgTrace;
	
	/** x, y and amplitude trace sources */
	protected TraceSource[] _traceSources;


	/**
	 * Constructor
	 * @param orbitSource  the orbit source
	 * @param enabled      sets whether the trace sources are enabled or disabled
	 */
	public OrbitTraceAdaptor( final OrbitSource orbitSource, final boolean enabled ) {
		LOCK = new Object();
		BEAM_EXCURSION_ORBIT_ADAPTOR = null;
		
		final String sourceLabel = orbitSource.getLabel();
		
		final float POINT_MARK_LENGTH = 10.0f;
		final float POINT_MARK_OFFSET = - POINT_MARK_LENGTH / 2.0f;
		
		final Shape circle = new java.awt.geom.Ellipse2D.Float( POINT_MARK_OFFSET, POINT_MARK_OFFSET, POINT_MARK_LENGTH, POINT_MARK_LENGTH );
		_xAvgTrace = new MutableTrace( sourceLabel + ": X Avg", circle, null );
				
		final Polygon triangle = new Polygon();
		triangle.addPoint( 0, (int)POINT_MARK_OFFSET );
		triangle.addPoint( - (int)POINT_MARK_OFFSET, - (int)POINT_MARK_OFFSET );
		triangle.addPoint( (int)POINT_MARK_OFFSET, -(int)POINT_MARK_OFFSET );
		final float[] dashedLine = new float[2];
		dashedLine[0] = 4.0f;
		dashedLine[1] = 4.0f;		
		_yAvgTrace = new MutableTrace( sourceLabel + ": Y Avg", triangle, dashedLine );
		
		final Shape square = new java.awt.geom.Rectangle2D.Float( POINT_MARK_OFFSET, POINT_MARK_OFFSET, POINT_MARK_LENGTH, POINT_MARK_LENGTH );
		final float[] dottedLine = new float[2];
		dottedLine[0] = 1.0f;
		dottedLine[1] = 4.0f;
		_ampAvgTrace = new MutableTrace( sourceLabel + ": Amp Avg", square, dottedLine );
		
		_traceSources = new TraceSource[3];
		_traceSources[X_AVG_TRACE_INDEX] = new OrbitTraceSource( _xAvgTrace, X_AVG_TYPE, enabled );
		_traceSources[Y_AVG_TRACE_INDEX] = new OrbitTraceSource( _yAvgTrace, Y_AVG_TYPE, enabled );
		_traceSources[AMP_AVG_TRACE_INDEX] = new OrbitTraceSource( _ampAvgTrace, AMP_AVG_TYPE, enabled );
		
		setOrbitSource( orbitSource );
	}


	/**
	 * Set the orbit source to monitor for new orbits.
	 * @param orbitSource  the new orbit source
	 */
	public void setOrbitSource( final OrbitSource orbitSource ) {
		synchronized ( LOCK ) {
			if ( _orbitSource != null ) {
				_orbitSource.removeOrbitSourceListener( this );
			}

			_orbitSource = orbitSource;
			if ( _orbitSource != null ) {
				_orbitSource.addOrbitSourceListener( this );
			}
		}
	}
	
	
	/**
	 * Get the trace sources.
	 * @return the array of trace sources
	 */
	public TraceSource[] getTraceSources() {
		return _traceSources;
	}


	/**
	 * Get the trace source which provides a trace of X Avg values.
	 * @return   the trace source for X Avg values
	 */
	public TraceSource getXAvgTraceSource() {
		return _traceSources[X_AVG_TRACE_INDEX];
	}


	/**
	 * Get the trace source which provides a trace of Y Avg values.
	 * @return   the trace source for Y Avg values
	 */
	public TraceSource getYAvgTraceSource() {
		return _traceSources[Y_AVG_TRACE_INDEX];
	}


	/**
	 * Get the trace source which provides a trace of Amplitude Avg values.
	 * @return   the trace source for Amplitude Avg values
	 */
	public TraceSource getAmpAvgTraceSource() {
		return _traceSources[AMP_AVG_TRACE_INDEX];
	}
	
	
	/**
	 * Event indicating that the specified orbit source has generated a new orbit.
	 * @param source    the orbit source generating the new orbit
	 * @param newOrbit  the new orbit
	 */
	public void orbitChanged( final OrbitSource source, final Orbit newOrbit ) {
		//updateTracesFromBeamExcursion( newOrbit );
		updateTraces( newOrbit );
	}
	
	
	/**
	 * Event indicating that the orbit source's sequence has changed.
	 * @param source       the orbit source generating the new orbit
	 * @param newSequence  the new sequence
	 * @param newBPMs      the new BPMs
	 */
	public void sequenceChanged( final OrbitSource source, final AcceleratorSeq newSequence, final List<BpmAgent> newBPMs ) {}
	
	
	/**
	 * Handle the event indicating that the orbit source enable state has changed.
	 * @param source the orbit source generating the event
	 * @param isEnabled the new enable state of the orbit source
	 */
	public void enableChanged( final OrbitSource source, final boolean isEnabled ) {}
	
	
	/**
	 * Update the traces with the beam excursion for the specified orbit
	 * @param orbit  the new orbit
	 */
	protected void updateTracesFromBeamExcursion( final Orbit orbit ) {
		synchronized ( LOCK ) {
			if ( orbit != _orbit ) {
				_orbit = orbit;
				if ( BEAM_EXCURSION_ORBIT_ADAPTOR != null ) {
					try {
						final BeamExcursion beamExcursion = BEAM_EXCURSION_ORBIT_ADAPTOR.getBeamExcursion( orbit );
						if ( beamExcursion != null ) {
							final double[] positions = beamExcursion.getPositions();
							final double[] xAvgValues = beamExcursion.getXAvgDisplacements();
							final double[] yAvgValues = beamExcursion.getYAvgDisplacements();
							final double[] ampAvgValues = beamExcursion.getAmpAvgValues();
							
							_xAvgTrace.update( orbit.getTimeStamp(), positions, xAvgValues );
							_yAvgTrace.update( orbit.getTimeStamp(), positions, yAvgValues );
							_ampAvgTrace.update( orbit.getTimeStamp(), positions, ampAvgValues );
						}
						else {
							updateTraces( orbit );
						}
					}
					catch( RuntimeException exception ) {
						exception.printStackTrace();
						updateTraces( orbit );
					}
				}
				else {
					updateTraces( orbit );
				}
			}
		}
	}
	
	
	/**
	 * Update the traced directly from the orbit rather than using the beam excursion
	 * @param orbit  the new orbit
	 */
	protected void updateTraces( final Orbit orbit ) {
		synchronized ( LOCK ) {
			final double[] positions = orbit.getPositions();
			final double[] xAvgValues = orbit.getXAvgDisplacements();
			final double[] yAvgValues = orbit.getYAvgDisplacements();
			final double[] ampAvgValues = orbit.getAmpAvgValues();
			
			_xAvgTrace.update( orbit.getTimeStamp(), positions, xAvgValues );
			_yAvgTrace.update( orbit.getTimeStamp(), positions, yAvgValues );
			_ampAvgTrace.update( orbit.getTimeStamp(), positions, ampAvgValues );
		}
	}
	
	
	/** Implement a TraceSource that gets its data from an OrbitData instance.  */
	private class OrbitTraceSource extends TraceSource {
		/** Description of the Field */
		protected MutableTrace _trace;
		
		
		/**
		 * Constructor
		 * @param trace    the trace
		 * @param type	the type of trace source
		 * @param enabled  whether or not the trace is enabled
		 */
		public OrbitTraceSource( final MutableTrace trace, final String type, final boolean enabled ) {
			super( trace.getLabel(), type );
			_trace = trace;
			setEnabled( enabled );
		}


		/**
		 * Get the latest available trace
		 * @return   the latest trace available or null if none is available
		 */
		public Trace getTrace() {
			return _trace.getTrace();
		}
	}
}

