//
//  CorrelationPlotter.java
//  xal
//
//  Created by Tom Pelaia on 12/16/08.
//  Copyright 2008 Oak Ridge National Lab. All rights reserved.
//

package xal.app.xyzcorrelator;

import java.io.Writer;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Color;
import java.util.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import xal.extension.bricks.WindowReference;
import xal.extension.widgets.plot.*;
import xal.ca.*;
import xal.extension.fit.lsm.Polynomial;



/** plots the correlated data */
abstract class CorrelationPlotter {
	/** limit for the buffer */
	protected int _bufferLimit;
	
	/** polynomial order for the fit */
	protected int _fitOrder;
	
	
	/** Constructor */
	public CorrelationPlotter() {
		_bufferLimit = 100;
		_fitOrder = 1;
	}
	
	
	/** get a correlation plotter that is most suitable for the specified number of channels */
	static public CorrelationPlotter getInstance( final int numChannels, final FunctionGraphsJPanel plot, final WindowReference windowReference, final int bufferLimit, final int fitOrder ) {
		CorrelationPlotter plotter = null;
		switch ( numChannels ) {
			case 0: case 1:
				plotter = new EmptyCorrelationPlotter();
				break;
			case 2:
				plotter = new CorrelationPlotter2D( plot, windowReference );
				break;
			case 3:
				plotter = new CorrelationPlotter3D( plot, windowReference );
				break;
			default:
				plotter = new EmptyCorrelationPlotter();
				break;
		}
		plotter.setBufferLimit( bufferLimit );
		plotter.setFitOrder( fitOrder );
		
		return plotter;
	}


	/** create and return a new instance of EmptyCorrelationPlotter */
	static public CorrelationPlotter getEmptyCorrelationPlotterInstance() {
		return new EmptyCorrelationPlotter();
	}
	
	
	/** plot the new correlation point */
	abstract public void plot( final List<ChannelTimeRecord> records, final Date timeStamp );
	
	
	/** determines whether this plotter can plot the specified number of channels */
	abstract public boolean supportsChannelCount( final int numChannels );
	
	
	/** get the buffer limit */
	public int getBufferLimit() { return _bufferLimit; }
	
	
	/** set the buffer limit */
	public void setBufferLimit( final int bufferLimit ) {
		_bufferLimit = bufferLimit;
	}
	
	
	/** get the fit order */
	public int getFitOrder() { return _fitOrder; }
	
	
	/** set the order for the fit */
	public void setFitOrder( final int order ) {
		_fitOrder = order;
	}
	
	
	/** perform a fit */
	public void performFit() {}
	
	
	/** get the fit equation */
	public String getFitEquation() {
		return "";
	}
	
	
	/** clear the fit */
	public void clearFit() {}
	
	
	/** clear the plot */
	public void clearPlot() {}
}



/** don't plot anything */
class EmptyCorrelationPlotter extends CorrelationPlotter{
	/** plot the new correlation point */
	public void plot( final List<ChannelTimeRecord> records, final Date timeStamp ) {}	// does nothing since there is nothing to plot
	
	
	/** determines whether this plotter can plot the specified number of channels */
	public boolean supportsChannelCount( final int numChannels ) {
		return numChannels < 2;
	}
}



/** plots the 2D correlated data */
class CorrelationPlotter2D extends CorrelationPlotter {
	/** format for writing the time stamp */
	final static protected SimpleDateFormat TIME_STAMP_FORMAT;
	
	/** plot for displaying correlated data */
	final protected FunctionGraphsJPanel CORRELATION_PLOT;
		
	/** bricks window reference */
	final protected WindowReference WINDOW_REFERENCE;
	
	/** cross hair (actually just a box) for displaying the most recent point */
	final protected CurveData RECENT_CROSS_HAIR;
	
	/** fit data */
	final protected BasicGraphData FIT_DATA;
	
	/** buffer of plot data */
	final protected List<CorrelationPoint2D> PLOT_BUFFER;
	
	/** fit equation */
	protected String _fitEquation;
	
	
	// static initializer
	static {
		TIME_STAMP_FORMAT = new SimpleDateFormat( "yyyy/MM/dd'T'HH:mm:ss.SSS" );
	}
	
	/** Constructor */
	public CorrelationPlotter2D( final FunctionGraphsJPanel plot, final WindowReference windowReference ) {
		CORRELATION_PLOT = plot;
		WINDOW_REFERENCE = windowReference;
		
		PLOT_BUFFER = Collections.synchronizedList( new LinkedList<CorrelationPoint2D>() );
		
		_fitEquation = "";
		FIT_DATA = new CubicSplineGraphData();
		FIT_DATA.setDrawPointsOn( false );
		FIT_DATA.setDrawLinesOn( true );
		CORRELATION_PLOT.addGraphData( FIT_DATA );
		
		RECENT_CROSS_HAIR = new CurveData();
		RECENT_CROSS_HAIR.setColor( Color.BLACK );
		
		clearPlot();
	}
	
	
	/** determines whether this plotter can plot the specified number of channels */
	public boolean supportsChannelCount( final int numChannels ) {
		return numChannels == 2;
	}
	
	
	/** perform a fit */
	public void performFit() {
		final List<CorrelationPoint2D> points = new ArrayList<CorrelationPoint2D>( PLOT_BUFFER );	// get a local copy which is not changing
		final int numPoints = points.size();
		if ( numPoints > _fitOrder ) {
			try {
				clearFit();	// clear the previous fit if any
				
				final Polynomial fitter = new Polynomial( _fitOrder );
				for ( int index = 0; index < numPoints; index++ ) {
					final CorrelationPoint2D point = points.get( index );
					fitter.addData( point.getX(), point.getY() );
				}
				fitter.fitFromCenter();
				final String equation = fitter.equation();
				_fitEquation = equation;
				final double xMin = CORRELATION_PLOT.getCurrentMinX();
				final double xMax = CORRELATION_PLOT.getCurrentMaxX();
				final int steps = _fitOrder;
				for ( int index = 0 ; index <= steps ; index++ ) {
					final double x = xMin + index * (xMax - xMin) / steps;
					final double y = fitter.getValue( x );
					FIT_DATA.addPoint( x, y );
					FIT_DATA.setGraphProperty( CORRELATION_PLOT.getLegendKeyString(), equation );
				}
			}
			catch ( Exception exception ) {
				clearFit();
				exception.printStackTrace();
			}
		}
		else {
			clearFit();
		}
	}
	
	
	/** get the fit equation */
	public String getFitEquation() {
		return _fitEquation;
	}
	
	
	/** clear the fit */
	public void clearFit() {
		_fitEquation = "";
		FIT_DATA.removeAllPoints();
	}
	
	
	/** clear the plot */
	synchronized public void clearPlot() {
		super.clearPlot();
		CORRELATION_PLOT.removeAllCurveData();
		CORRELATION_PLOT.addCurveData( RECENT_CROSS_HAIR );
		FIT_DATA.removeAllPoints();
	}
	
	
	/** draw the cross hair indicating the most recent point */
	protected void drawRecentCrossHair( final CorrelationPoint2D point ) {
		final double halfWidth = ( CORRELATION_PLOT.getCurrentMaxX() - CORRELATION_PLOT.getCurrentMinX() ) / 50;
		final double halfHeight = ( CORRELATION_PLOT.getCurrentMaxY() - CORRELATION_PLOT.getCurrentMinY() ) / 50;
		final double x = point.getX();
		final double y = point.getY();
		
		RECENT_CROSS_HAIR.clear();
		RECENT_CROSS_HAIR.addPoint( x - halfWidth, y + halfHeight );
		RECENT_CROSS_HAIR.addPoint( x + halfWidth, y + halfHeight );
		RECENT_CROSS_HAIR.addPoint( x + halfWidth, y - halfHeight );
		RECENT_CROSS_HAIR.addPoint( x - halfWidth, y - halfHeight );			
		RECENT_CROSS_HAIR.addPoint( x - halfWidth, y + halfHeight );			
	}
	
	
	/** plot the new correlation point */
	synchronized public void plot( final List<ChannelTimeRecord> records, final Date timeStamp ) {
		if ( records.size() == 2 ) {
			final CorrelationPoint2D point = new CorrelationPoint2D( records, timeStamp );
			PLOT_BUFFER.add( point );
			CORRELATION_PLOT.addCurveData( point.getCurveData() );
			drawRecentCrossHair( point );
			trimBuffer();
		}
	}
	
	
	/** trim the buffer of any excess points */
	protected void trimBuffer() {
		while( PLOT_BUFFER.size() > _bufferLimit ) {
			final CorrelationPoint2D oldPoint = PLOT_BUFFER.remove( 0 );
			CORRELATION_PLOT.removeCurveData( oldPoint.getCurveData() );
		}
	}
}



/** plots the correlated data using color for the third dimension */
class CorrelationPlotter3D extends CorrelationPlotter2D {
	/** get a format for the range */
	final private DecimalFormat RANGE_FORMAT = new DecimalFormat( "0.00E0" );
	
	/** starting value of Z for the spectrum */
	private double _startZ;
	
	/** ending value of Z for the spectrum */
	private double _endZ;
	
	/** minimum value of Z in the buffer */
	private double _minZ;
	
	/** maximum value of Z in the buffer */
	private double _maxZ;
	
	
	/** Constructor */
	public CorrelationPlotter3D( final FunctionGraphsJPanel plot, final WindowReference windowReference ) {
		super( plot, windowReference );
		
		_minZ = Double.MAX_VALUE;
		_maxZ = -Double.MAX_VALUE;
		_startZ = _minZ;
		_endZ = _maxZ;
	}
	
	
	/** determines whether this plotter can plot the specified number of channels */
	public boolean supportsChannelCount( final int numChannels ) {
		return numChannels == 3;
	}
	
	
	/** plot the new correlation point */
	synchronized public void plot( final List<ChannelTimeRecord> records, final Date timeStamp ) {
		if ( records.size() == 3 ) {
			final CorrelationPoint3D point = new CorrelationPoint3D( records, timeStamp );
			updateSpectrumRangeForNewPoint( point.getZ() );
			point.applyColorForRange( _startZ, _endZ );
			PLOT_BUFFER.add( point );
			CORRELATION_PLOT.addCurveData( point.getCurveData() );
			drawRecentCrossHair( point );
			trimBuffer();
		}
	}
	
	
	/** trim the buffer of any excess points */
	protected void trimBuffer() {
		while( PLOT_BUFFER.size() > _bufferLimit ) {
			final CorrelationPoint3D oldPoint = (CorrelationPoint3D)PLOT_BUFFER.remove( 0 );
			CORRELATION_PLOT.removeCurveData( oldPoint.getCurveData() );
			updateSpectrumRangeForOldPoint( oldPoint.getZ() );
		}
	}
	
	
	/** clear the plot */
	synchronized public void clearPlot() {
		super.clearPlot();
		_minZ = Double.MAX_VALUE;
		_maxZ = -Double.MAX_VALUE;
		_startZ = _minZ;
		_endZ = _maxZ;
	}
	
	
	/** update the spectrum range if the new point's z value is outside of the spectrum range */
	private void updateSpectrumRangeForNewPoint( final double z ) {
		if ( z < _minZ ) {
			_minZ = z;
		}
		if ( z > _maxZ ) {
			_maxZ = z;
		}
		
		if ( z < _startZ || z > _endZ ) {
			makeSpectrumRangeFromExtrema();
		}
	}
	
	
	/** update the spectrum range if the old point's z value matches the minimum or maximum values indicating that it is may have been a range determining point */
	private void updateSpectrumRangeForOldPoint( final double oldZ ) {
		if ( oldZ == _minZ || oldZ == _maxZ ) {
			// need to determine the new extrema from the buffer
			double minZ = Double.MAX_VALUE;
			double maxZ = -Double.MAX_VALUE;
			for ( final CorrelationPoint2D point : PLOT_BUFFER ) {
				final double z = ((CorrelationPoint3D)point).getZ();
				if ( z < minZ ) {
					minZ = z;
				}
				if ( z > maxZ ) {
					maxZ = z;
				}
			}
			_minZ = minZ;
			_maxZ = maxZ;
			
			makeSpectrumRangeFromExtrema();				
		}
	}
	
	
	/** calculate a new spectrum range given the extrema */
	private void makeSpectrumRangeFromExtrema() {
		final double scale = calcRangeScale();
		
		if ( scale == 0 ) {
			_startZ = _minZ;
			_endZ = _maxZ;
		}
		else {
			_startZ = scale * Math.floor( _minZ / scale );
			_endZ = scale * Math.ceil( _maxZ / scale );
		}
		
		final JLabel endZLabel = (JLabel)WINDOW_REFERENCE.getView( "EndZLabel" );
		final JLabel startZLabel = (JLabel)WINDOW_REFERENCE.getView( "StartZLabel" );
		startZLabel.setText( RANGE_FORMAT.format( _startZ ) );
		endZLabel.setText( RANGE_FORMAT.format( _endZ ) );
		
		// apply the new spectrum range to the current points in the buffer
		for ( final CorrelationPoint2D point : PLOT_BUFFER ) {
			final CorrelationPoint3D point3D = (CorrelationPoint3D)point;
			point3D.applyColorForRange( _startZ, _endZ );
		}			
	}
	
	
	/** calculate the power of 10 scale appropriate for the extrema range */
	private double calcRangeScale() {
		final double range = _maxZ - _minZ;
		if ( range <= 0 ) {
			return 0.0;
		}
		else {
			final double power = Math.floor( Math.log10( range ) );
			return Math.pow( 10, power );
		}
	}
}



/** holds the x and y data for a correlation */
class CorrelationPoint2D {
	/** time stamp of the correlated event */
	final protected Date TIME_STAMP;
	
	/** curve data for x and y along with the color corresponding to z */
	final protected CurveData CURVE_DATA;
	
	
	/** Constructor */
	public CorrelationPoint2D( final List<ChannelTimeRecord> records, final Date timeStamp ) {
		TIME_STAMP = timeStamp;
		
		if ( records.size() >= 2 ) {
			CURVE_DATA = new CurveData();
			CURVE_DATA.setColor( Color.BLUE );
			CURVE_DATA.setLineWidth( 10 );
			final double x = records.get(0).doubleValue();
			final double y = records.get(1).doubleValue();
			CURVE_DATA.addPoint( x, y );
		}
		else {
			CURVE_DATA = null;
		}
	}
	
	
	/** get the time stamp of the correlated event */
	public Date getTimeStamp() {
		return TIME_STAMP;
	}
	
	
	/** get the X coordinate */
	public double getX() {
		return CURVE_DATA.getX( 0 );
	}
	
	
	/** get the Y coordinate */
	public double getY() {
		return CURVE_DATA.getY( 0 );
	}
	
	
	/** get the curve data */
	public CurveData getCurveData() {
		return CURVE_DATA;
	}
}



/** holds the x, y and z data for a correlation */
class CorrelationPoint3D extends CorrelationPoint2D {
	/** z data */
	final private double Z;
	
	
	/** Constructor */
	public CorrelationPoint3D( final List<ChannelTimeRecord> records, final Date timeStamp ) {
		super( records, timeStamp );
		
		Z = records.size() == 3 ? records.get(2).doubleValue() : Double.NaN;
	}
	
	
	/** get the z value */
	public double getZ() {
		return Z;
	}
	
	
	/** Apply the color for the specified z range */
	public void applyColorForRange( final double start, final double end ) {
		if ( Z >= start && Z <= end ) {
			// color value is in the range from 0 to 1 and maps to the red to blue spectrum
			final float colorValue = (float)( ( Z - start ) / ( end - start ) );
			CURVE_DATA.setColor( ColorSpectrumPanel.getColor( colorValue ) );
		}
		else {
			CURVE_DATA.setColor( Color.BLACK );
		}
	}
}
