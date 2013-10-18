/*
 * DefaultScreenHandler.java
 *
 * Created on Tue Sep 02 16:09:39 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import xal.tools.data.*;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.text.*;


/**
 * DefaultScreenHandler is a screen handler which displays scope traces on the scope screen with the 
 * x axis as time.
 *
 * @author  tap
 */
class DefaultScreenHandler extends AbstractScreenHandler implements TimeModelListener, SettingListener, DataListener {	
	// constants
	final static String handlerType = "DefaultScreenHandler";
	
	/** lower chart value limit */
	final static double LOWER_VALUE_LIMIT = -4.0;
	
	/** upper chart value limit */
	final static double UPPER_VALUE_LIMIT = 4.0;
	
	/** chart tick step size */	
	final static double TICK_STEP = 1.0;
	
	// state
	private TimeDisplaySettings _timeSettings;
	
	
	/**
	 * DefaultScreenHandler constructor
	 */
	public DefaultScreenHandler(ScopeModel aModel, TimeDisplaySettings timeSettings) {
		super(aModel);
		
		_timeSettings = timeSettings;
		_timeSettings.addSettingListener(this);
		updateTimeSettings();
		
        model.getTimeModel().addTimeModelListener(this);
	}
	
	
	/**
	 * DefaultScreenHandler constructor
	 */
	public DefaultScreenHandler(ScopeModel aModel) {
		this(aModel, new TimeDisplaySettings());
	}
    
    
    /**
     * Update the receiver's data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
	 * @param version The version of the adaptor
     */
    public void update(DataAdaptor adaptor, int version) {
		super.update(adaptor, version);
		
		switch(version) {
			case 0:
				_timeSettings.setTimeDivision( adaptor.doubleValue("turnsPerDivision") );
				_timeSettings.setLowerLimit( adaptor.doubleValue("lowerTurnsLimit") );
				break;
			case 1:
				final DataAdaptor timeAdaptor = adaptor.childAdaptor( TimeDisplaySettings.dataLabel );
				if ( timeAdaptor != null ) {
					_timeSettings.update(timeAdaptor);
				}
				break;
			default:
				break;
		}
    }
	
	
	/**
     * Write the receiver's custom data to the adaptor for external persistent storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
	 */
	void writeCustom(DataAdaptor adaptor) {
		adaptor.setValue("version", 1);
        adaptor.writeNode(_timeSettings);
	}
	
	
	/**
	 * Get the screen handler type
	 * @return The enum identifying the screen handler type
	 */
	int getType() {
		return AbstractScreenHandler.DEFAULT_HANDLER;
	}
	
	
	/**
	 * Get the time display settings for this handler
	 * @return The time display settings used by this handler
	 */
	public TimeDisplaySettings getTimeDisplaySettings() {
		return _timeSettings;
	}
    
    
    /**
     * Add the listener to be notified when a setting has changed.
     * @param listener Object to receive setting change events.
     */
    void addSettingListener(SettingListener listener) {
        super.addSettingListener(listener);
		_timeSettings.addSettingListener(listener);
    }
    
    
    /**
     * Remove the listener as a receiver of setting change events.
     * @param listener Object to remove from receiving setting change events.
     */
    void removeSettingListener(SettingListener listener) {
        super.removeSettingListener(listener);
		_timeSettings.removeSettingListener(listener);
    }
	
    
    /** 
     * Create the chart for the scope screen trace display.
     */
    protected void createChart() {
		super.createChart();
		
        SimpleChartPopupMenu popupMenu = SimpleChartPopupMenu.addPopupMenuTo(chart);
		popupMenu.setActionEnabled(SimpleChartPopupMenu.SCALE_ONCE_ID, false);
		popupMenu.setActionEnabled(SimpleChartPopupMenu.X_AUTOSCALE_ID, false);
		popupMenu.setActionEnabled(SimpleChartPopupMenu.Y_AUTOSCALE_ID, false);
		popupMenu.setActionEnabled(SimpleChartPopupMenu.OPTIONS_DIALOG_ID, false);
		
        chart.setName("");
        chart.setAxisNameY("Units");
        
        chart.setLimitsAndTicksY(LOWER_VALUE_LIMIT, UPPER_VALUE_LIMIT, TICK_STEP);
        setTimeAxisUnitsLabel( model.getTimeModel().getUnitsLabel() );
    }
	
	
	/**
	 * Apply the time settings to the chart.
	 */
	private void updateTimeSettings() {
		_timeSettings.applySettingsTo(chart);
	}
    
    
    /** 
     * Update the scope screen for the latest captured data.
     * @param sender Identifies who sent the message.
     * @param traceEvents Array of trace events; one event for each channel.
     * @param timestamp Average time stamp of all events.
     */
    public void updateTraces(Object sender, final TraceEvent[] traceEvents, Date timestamp) {
		double[][] traces = new double[traceEvents.length][];
		double[][] elementTimes = new double[traceEvents.length][];
		String[] labels = new String[traceEvents.length];
		
		for ( int index = 0 ; index < traceEvents.length ; index++ ) {
			if ( traceEvents[index] != null ) {
				traces[index] = traceEvents[index].getDefaultTrace();
				elementTimes[index] = traceEvents[index].getElementTimes();
				if ( traces[index].length != elementTimes[index].length ) {
					traces[index] = new double[0];
					elementTimes[index] = new double[0];
				}
			}
			else {
				traces[index] = new double[0];
				elementTimes[index] = new double[0];
			}
		}
		
        dataModel.setSeriesData(traces, elementTimes);
        chart.removeAllGraphData();
        chart.setName( timestamp.toString() );
        chart.addGraphData( getGraphData() );
    }
    
    
    /** 
     * Get the array of series as a Vector of GraphData instances.
     * @return The series data to display in the FunctionGraphsJPanel chart.
     */
    public Vector<BasicGraphData> getGraphData() {
        int numSeries = dataModel.getNumSeries();

        final Vector<BasicGraphData> series = new Vector<>(numSeries);
        for ( int seriesIndex = 0 ; seriesIndex < numSeries ; seriesIndex++ ) {
            final double[] xValues = dataModel.getXSeries(seriesIndex);
            final double[] yValues = dataModel.getYSeries(seriesIndex);
            final BasicGraphData graphData = new BasicGraphData();
            graphData.addPoint(xValues, yValues);
            graphData.setGraphColor( getSeriesColor(seriesIndex) );
			graphData.setGraphProperty(chart.getLegendKeyString(), model.getTraceSource(seriesIndex).getLabel());
            
            series.add( graphData );
        }
        return series;
    }
	
	
	/**
	 * Find the most recently captured waveforms and make them visible onscreen.
	 * @throws java.lang.RuntimeException if there are no current waveforms available.
	 */
	public void findWaveforms() throws RuntimeException {
		WaveformSnapshot snapshot = model.getRawWaveformSnapshot();
		
		double[] timeRange = snapshot.getTimeRange();
		_timeSettings.setLimits(timeRange[0], timeRange[1]);
		
		final Waveform[] waveforms = snapshot.getWaveforms();
		for ( int index = 0 ; index < waveforms.length ; index++ ) {
			final double epsilon = 1.0e-6;
			final double minValue = waveforms[index].getMinValue();
			final double maxValue = waveforms[index].getMaxValue();
			final double span = maxValue - minValue;
			
			String pvName = waveforms[index].getName();
						
			ChannelModel channelModel = model.getChannelModelWithPV(pvName);
			if ( channelModel == null )  continue;
			
			// fit the waveform one unit inset from the top and bottom
			final double scale;
			final double offset;
			
			if ( span < epsilon ) {
				scale = 1.0;
				offset = - ( minValue + maxValue ) / 2;
			}
			else {
				scale = (UPPER_VALUE_LIMIT - LOWER_VALUE_LIMIT - 2) / span;
				offset = UPPER_VALUE_LIMIT - 1 - scale * maxValue;
			}
			
			channelModel.setSignalScale(scale);
			channelModel.setSignalOffset(offset);
		}
	}
    
    
    /**
     * Set the time axis units label to the specified label.
     * @param units The units label.
     */
    protected void setTimeAxisUnitsLabel(String units) {
        chart.setAxisNameX( "Time (" + units + ")" );
    }
    
    
    /**
     * Event indicating that the time units of the time model sender has changed.
     * @param sender The sender of the event.
     */
    public void timeUnitsChanged(TimeModel sender) {
        setTimeAxisUnitsLabel( sender.getUnitsLabel() );
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
	
	
    /**
     * A setting from the sender has changed.
     * @param source The object whose setting changed.
     */
    public void settingChanged(Object source) {
		if ( source == _timeSettings ) {
			updateTimeSettings();
		}
	}
}

