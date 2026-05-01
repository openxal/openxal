/*
 * AbstractScreenHandler.java
 *
 * Created on Tue Sep 02 16:10:12 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import xal.extension.widgets.plot.*;
import xal.tools.messaging.MessageCenter;
import xal.tools.data.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.text.*;


/**
 * AbstractScreenHandler is the abstract superclass of all screen handlers.  A screen handler displays the 
 * scope traces on the scope screen.
 *
 * @author  tap
 */
abstract class AbstractScreenHandler implements DataListener {
	// Screen handler types
	static final int DEFAULT_HANDLER = 0;
	static final int FFT_HANDLER = 1;
	
	// constants
	static String dataLabel = "ScreenHandler";
	
    // components
    protected FunctionGraphsJPanel chart;
    
    // messaging
    final private MessageCenter MESSAGE_CENTER;
    final private SettingListener SETTING_EVENT_PROXY;
    
    // models
    protected ScopeDataModel dataModel;
    protected ScopeModel model;
	
	
	/**
	 * AbstractScreenHandler constructor
	 * @param aModel The scope model.
	 */
	public AbstractScreenHandler(ScopeModel aModel) {
        MESSAGE_CENTER = new MessageCenter("ScopeScreen Center");
        SETTING_EVENT_PROXY = MESSAGE_CENTER.registerSource( this, SettingListener.class );
		
		model = aModel;
        dataModel = new ScopeDataModel();
		createChart();
	}
	
    
    /** 
     * Create the chart for the scope screen trace display.
     */
    protected void createChart() {
        chart = new FunctionGraphsJPanel();
        chart.setBackground(Color.gray);
        
        chart.setName("");
		
        chart.setNumberFormatX( new DecimalFormat("0.00E0") );
        chart.setNumberFormatY( new DecimalFormat("0.00E0") );
        
        // add the horizontal and vertical guides
        chart.addVerticalLine(chart.getCurrentMinX(), Color.black);
        chart.addVerticalLine(chart.getCurrentMaxX(), Color.black);
        chart.addHorizontalLine(chart.getCurrentMinY(), Color.black);
        chart.addHorizontalLine(chart.getCurrentMaxY(), Color.black);
        
        // Hide buttons on the plot since they are handled by the scope
        chart.setChooseModeButtonVisible(false);
        chart.setHorLinesButtonVisible(false);
        chart.setVerLinesButtonVisible(false);
        
        // Show the horizontal and vertical guides
        chart.setDraggingHorLinesGraphMode(true);
        chart.setDraggingVerLinesGraphMode(true);
		
		// add legend support
		chart.setLegendPosition(FunctionGraphsJPanel.LEGEND_POSITION_ARBITRARY);
		chart.setLegendKeyString("Legend");
		chart.setLegendBackground(Color.lightGray);
		chart.setLegendColor(Color.black);
		chart.setLegendVisible(true);
    }
    
    
    /** 
     * dataLabel() provides the name used to identify the class in an 
     * external data source.
     * @return The tag for this data node.
     */
    public String dataLabel() {
        return dataLabel;
    }
    
    
    /**
     * Update the receiver's data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update(DataAdaptor adaptor) {
		update(adaptor, adaptor.intValue("version"));
	}
    
    
    /**
     * Update the receiver's data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
	 * @param version The version of the adaptor
     */
    public void update(DataAdaptor adaptor, int version) {
		if ( adaptor.hasAttribute("gridVisible") ) {
			setGridVisible( adaptor.booleanValue("gridVisible") );
		}
		if ( adaptor.hasAttribute("brightness") ) {
			setBrightness( (float)adaptor.doubleValue("brightness") );
		}
	}
	
	
    /**
     * Write the receiver's data to the adaptor for external persistent storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    final public void write(DataAdaptor adaptor) {
		adaptor.setValue("type", getType());
        adaptor.setValue("gridVisible", isGridVisible());
        adaptor.setValue("brightness", (double)getBrightness());
		writeCustom(adaptor);
    }
	
	
	/**
     * Write the receiver's custom data to the adaptor for external persistent storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
	 */
	abstract void writeCustom(DataAdaptor adaptor);
	
	
	/**
	 * Get the screen handler type
	 * @return The enum identifying the screen handler type
	 */
	abstract int getType();
    
    
    /**
     * Add the listener to be notified when a setting has changed.
     * @param listener Object to receive setting change events.
     */
    void addSettingListener(SettingListener listener) {
        MESSAGE_CENTER.registerTarget(listener, this, SettingListener.class);
    }
    
    
    /**
     * Remove the listener as a receiver of setting change events.
     * @param listener Object to remove from receiving setting change events.
     */
    void removeSettingListener(SettingListener listener) {
        MESSAGE_CENTER.removeTarget(listener, this, SettingListener.class);
    }
	
	
	/**
	 * Get the chart managed by this screen handler
	 * @return The chart managed by this screen handler
	 */
	Component getChart() {
		return chart;
	}
	
	
	/**
	 * Find the most recently captured waveforms and make them visible onscreen.
	 * @throws java.lang.RuntimeException if there are no current waveforms available.
	 */
	abstract public void findWaveforms() throws RuntimeException;
    
    
    /** 
     * Update the scope screen for the latest captured data.
     * @param sender Identifies who sent the message.
     * @param traceEvents Array of trace events; one event for each channel.
     * @param timestamp Average time stamp of all events.
     */
    abstract public void updateTraces(Object sender, final TraceEvent[] traceEvents, Date timestamp);
    
    
    /** 
	 * Get the color of the indexed series
	 * @param seriesIndex The index of the series for which to get the color
	 * @return The color used to display the series
	 */
    public Color getSeriesColor(final int seriesIndex) {
        return ScopeScreen.getDefaultSeriesColor(seriesIndex);
    }
    
    
    /** 
     * Get the background brightness of the chart whose color is a shade of gray.
     * @return The brightness of the chart from 0 (black) to 1 (white)
     */
    public float getBrightness() {
        final Color backColor = chart.getGraphBackGroundColor();
        float[] hsbVals = Color.RGBtoHSB(backColor.getRed(), backColor.getGreen(), backColor.getBlue(), null);
        
        return hsbVals[2];
    }
    
    
    /** 
     * Set the background brightness of the chart whose color is a shade of gray.
     * @param brightness The brightness of the chart from 0 (black) to 1 (white)
     */
    public void setBrightness(final float brightness) {
		if ( Math.abs(brightness - getBrightness()) < 0.01 )  return;	// nothing to do
		
        final Color backColor = chart.getBackground();
        float[] hsbVals = Color.RGBtoHSB(backColor.getRed(), backColor.getGreen(), backColor.getBlue(), null);
        Color newBackColor = Color.getHSBColor(hsbVals[0], hsbVals[1], brightness);
        chart.setGraphBackGroundColor(newBackColor);
        SETTING_EVENT_PROXY.settingChanged(this);
    }
    
    
    /** 
     * Returns the visibility of the grid.
     * @return True if the grid is visible and False if the grid is not visible.
     */
    public boolean isGridVisible() {
        return chart.getGridLinesVisibleX() || chart.getGridLinesVisibleY();
    }
    
    
    /** 
     * Sets the visibility of the grid.
     * @param isVisible True if the grid should be made visible and False if it should be hidden.
     */
    public void setGridVisible(boolean isVisible) {
		if ( isVisible == isGridVisible() )  return;	// nothing to do
		
        chart.setGridLinesVisibleX(isVisible);
        chart.setGridLinesVisibleY(isVisible);
        SETTING_EVENT_PROXY.settingChanged(this);
    }
    
    
    /** 
     * Toggle the visibility of the chart grid.
     */
    public void toggleGridVisible() {
        setGridVisible( !isGridVisible() );
    }
    
    
    /** 
     * Returns the visibility of the legend.
     * @return True if the legend is visible and False if the legend is not visible.
     */
    public boolean isLegendVisible() {
		return chart.isLegendVisible();
    }
    
    
    /** 
     * Sets the visibility of the legend.
     * @param isVisible True if the legend should be made visible and False if it should be hidden.
     */
    public void setLegendVisible(boolean isVisible) {
		chart.setLegendVisible(isVisible);
    }
    
    
    /** 
     * Toggle the visibility of the chart legend.
     */
    public void toggleLegendVisible() {
        chart.setLegendVisible( !chart.isLegendVisible() );
    }
}


