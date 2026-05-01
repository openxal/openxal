/*
 * FFTScreenHandler.java
 *
 * Created on Mon Oct 20 15:00:40 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import xal.tools.data.*;
import xal.extension.widgets.plot.*;
import xal.tools.LinearInterpolator;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;

import JSci.maths.*;

import java.util.*;

/**
 * FFTScreenHandler
 *
 * @author  tap
 */
public class FFTScreenHandler extends AbstractScreenHandler implements TimeModelListener {
	// constants
	final private static double log2 = Math.log(2.0);
	final static String handlerType = "FFTScreenHandler";
	
	
	/**
	 * FFTScreenHandler constructor
	 * @param aModel The scope model.
	 */
	public FFTScreenHandler(ScopeModel aModel) {
		super(aModel);
		
        model.getTimeModel().addTimeModelListener(this);
		updateFrequencyUnits();
	}
	
    
    /** 
     * Create the chart for the scope screen trace display.
     */
    protected void createChart() {
		super.createChart();
        SimpleChartPopupMenu popupMenu = SimpleChartPopupMenu.addPopupMenuTo(chart);
		
        chart.setName("");
        chart.setAxisNameY("Amplitude");
		chart.setAxisNameX("Frequency");
    }
	
	
	/**
     * Write the receiver's custom data to the adaptor for external persistent storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
	 */
	void writeCustom(DataAdaptor adaptor) {
		adaptor.setValue("version", 0);
	}
	
	
	/**
	 * Get the screen handler type
	 * @return The enum identifying the screen handler type
	 */
	int getType() {
		return AbstractScreenHandler.FFT_HANDLER;
	}
	
	
	/**
	 * Find the most recently captured waveforms and make them visible onscreen.
	 * @throws java.lang.RuntimeException if there are no current waveforms available.
	 */
	public void findWaveforms() throws RuntimeException {
	}
    
    
    /** 
     * Update the scope screen for the latest captured data.
     * @param sender Identifies who sent the message.
     * @param traceEvents Array of trace events; one event for each channel.
     * @param timestamp Average time stamp of all events.
     */
    public void updateTraces(Object sender, final TraceEvent[] traceEvents, Date timestamp) {
		double[][] frequencies = new double[traceEvents.length][];
		double[][] transforms = new double[traceEvents.length][];
		
		for ( int index = 0 ; index < traceEvents.length ; index++ ) {
			if ( traceEvents[index] != null ) {
				double[] trace = traceEvents[index].getRawTrace();
				double[] elementTimes = traceEvents[index].getElementTimes();
				
				if ( trace != null && elementTimes.length > 1 && (trace.length == elementTimes.length) ) {
					frequencies[index] = getFrequencies(elementTimes);
					transforms[index] = getTransform(trace, elementTimes, frequencies[index].length);
				}
			}
			else {
				frequencies[index] = new double[0];
				transforms[index] = new double[0];
			}
		}
		
        dataModel.setSeriesData(transforms, frequencies);
		
        chart.removeAllGraphData();
        chart.setName( timestamp.toString() );
        chart.addGraphData( getGraphData() );
	}
	
	
	/**
	 * Get the FFT frequencies for the specified time range.
	 * @param times The array of times for which we seek FFT frequencies.
	 * @return the frequencies
	 */
	private double[] getFrequencies(double[] times) {
		// frequency count must be a power of 2 and we want it to be the smallest count greater than or equal to number of times
		// divide by 2 since we only care about positive frequencies
		int count = (int)Math.round( Math.pow( 2.0, Math.ceil( Math.log(times.length) / log2 ) ) ) / 2;
		double[] frequencies = new double[count];
		final double timespan = times[times.length-1] - times[0];
		final double frequencyStep = 1 / timespan;
		double frequency = 0;
		for ( int index = 0 ; index < count ; index++ ) {
			frequencies[index] = frequency;
			frequency += frequencyStep;
		}
		
		return frequencies;
	}
	
	
	/**
	 * Get the FFT amplitudes for the specified waveform.
	 * @param y The waveform array for which we want the FFT
	 * @param t The waveform time array
	 * @param count The number of points we want in our transform
	 * @return The FFT of the waveform.
	 */
	private double[] getTransform(final double[] y, final double[] t, final int count) {
		double[] waveform = getInterpolatedWaveform(y, t, 2*count);
		Complex[] fft = FourierMath.transform(waveform);
		final int fft_count = fft.length;
		double[] transform = new double[fft_count];
		
		for ( int index = 0 ; index < fft_count ; index++ ) {
			transform[index] = fft[index].mod();
		}
		
		return transform;
	}
	
	
	/**
	 * Interpolate the waveform to get a waveform with the desired number of points.
	 * @param y original waveform array
	 * @param t array of times over which the waveform has points
	 * @param count the number of points we desire in the new waveform
	 * @return The new waveform with the desired number of points.
	 */
	private double[] getInterpolatedWaveform(final double[] y, final double[] t, final int count) {
		final double timerange = t[t.length-1] - t[0];
		double step = timerange / (t.length - 1);	// original time steps
		double newStep = timerange / (count - 1);	// new time steps
		LinearInterpolator interpolator = new LinearInterpolator(y, t[0], step);
		double[] waveform = new double[count];
		
		double time = t[0];
		for ( int index = 0 ; index < count ; index++ ) {
			waveform[index] = interpolator.calcValueAt(time);
			time += newStep;
		}
		
		return waveform;
	}
	
	
    /** 
     * Get the array of series as a Vector of GraphData instances.
     * @return The series data to display in the FunctionGraphsJPanel chart.
     */
    public Vector<BasicGraphData> getGraphData() {
        int numSeries = dataModel.getNumSeries();

        final Vector<BasicGraphData> series = new Vector<>( numSeries );
        for ( int seriesIndex = 0 ; seriesIndex < numSeries ; seriesIndex++ ) {
            final double[] xValues = dataModel.getXSeries(seriesIndex);
            final double[] yValues = dataModel.getYSeries(seriesIndex);
            final BasicGraphData graphData = new BasicGraphData();
            graphData.addPoint( xValues, yValues );
            graphData.setGraphColor( getSeriesColor(seriesIndex) );
			graphData.setGraphProperty(chart.getLegendKeyString(), model.getTraceSource(seriesIndex).getLabel());
            
            series.add( graphData );
        }
        return series;
	}
	
	
	/**
	 * Update the frequency units displayed on the chart.
	 */
	protected void updateFrequencyUnits() {
		int unitsCode = model.getTimeModel().getUnitsType();
		
		switch(unitsCode) {
			case TimeModel.TURN:
				chart.setAxisNameX("Frequency (turns^-1)");
				break;
			case TimeModel.MICROSECOND:
				chart.setAxisNameX("Frequency (MHz)");			
				break;
			default:
				chart.setAxisNameX("Frequency");			
				break;
		}
	}
    
    
    /**
     * Event indicating that the time units of the time model sender has changed.
     * @param sender The sender of the event.
     */
    public void timeUnitsChanged(TimeModel sender) {
        updateFrequencyUnits();
    }
    
    
    /**
     * Event indicating that the time conversion of the time model sender has changed.
     * This is most likely due to the scaling changing.  For example the turn to 
     * microsecond conversion is monitored and may change during the lifetime of 
     * the application.
     * @param sender The sender of the event.
     */
    public void timeConversionChanged(TimeModel sender) {
    }
}

