/*
 * ScopeScreen.java
 *
 * Created on December 17, 2002, 1:44 PM
 */

package xal.app.scope;

import xal.tools.data.*;
import xal.extension.widgets.plot.*;
import xal.tools.messaging.MessageCenter;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.text.*;
import java.lang.reflect.*;

/**
 * Controller of the scope display.
 *
 * @author  tap
 */
public class ScopeScreen extends Box implements TraceListener, DataListener, SwingConstants {
	/** constant required to keep serializable happy */
	static final private long serialVersionUID = 1L;

    // constants
    final static String dataLabel = "ScopeScreen";
    final static Color[] defaultTraceColors = {Color.red, Color.white, Color.blue, Color.green.darker(), Color.magenta.darker(), Color.yellow, Color.cyan, Color.orange.darker(), Color.darkGray};
    
    // messaging
    final private MessageCenter MESSAGE_CENTER;
    final private SettingListener SETTING_EVENT_PROXY;
    
    // models
    protected ScopeModel model;
	
	// handlers
	protected AbstractScreenHandler _screenHandler;
	protected DefaultScreenHandler _defaultHandler;
	protected FFTScreenHandler _fftHandler;
    
        
    /** Creates a new instance of ScopeScreen */
    public ScopeScreen(ScopeModel aModel) {
		super(HORIZONTAL);
		
        MESSAGE_CENTER = new MessageCenter("ScopeScreen Center");
        SETTING_EVENT_PROXY = MESSAGE_CENTER.registerSource(this, SettingListener.class);
        
        model = aModel;
		_defaultHandler = new DefaultScreenHandler(aModel);
		_fftHandler = new FFTScreenHandler(aModel);
		setScreenHandler(_defaultHandler);
		
        initView();
		
        model.addTraceListener(this);
    }
    
    
    /** 
     * Empty initialization
     */
    protected void initView() {
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
		final int version =  adaptor.hasAttribute("version") ? adaptor.intValue("version") : 0;

		switch(version) {
			case 0:
				_defaultHandler.update( adaptor, version );
				break;
			case 1:
				{
					final List<DataAdaptor> handlerAdaptors = adaptor.childAdaptors( AbstractScreenHandler.dataLabel );
					updateHandlers( handlerAdaptors );
				}
				break;
			default:
				break;
		}
    }
	
	
	/**
	 * Update all of the handlers found in the document.
	 * @param handlerAdaptorIter The iterator of screen handler adaptors found in the document.
	 */
	public void updateHandlers( final List<DataAdaptor> handlerAdaptors ) {
		for ( final DataAdaptor handlerAdaptor : handlerAdaptors ) {
			final int handlerType = handlerAdaptor.intValue( "type" );
			switch(handlerType) {
				case AbstractScreenHandler.DEFAULT_HANDLER:
					_defaultHandler.update(handlerAdaptor);
					break;
				case AbstractScreenHandler.FFT_HANDLER:
					_fftHandler.update(handlerAdaptor);
					break;
			}
		}
	}
    
    
    /**
     * Write the receiver's data to the adaptor for external persistent storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write(DataAdaptor adaptor) {
		adaptor.setValue("version", 1);
		
		adaptor.writeNode(_defaultHandler);
        adaptor.writeNode(_fftHandler);
    }
    
    
    /**
     * Add the listener to be notified when a setting has changed.
     * @param listener Object to receive setting change events.
     */
    void addSettingListener(SettingListener listener) {
        MESSAGE_CENTER.registerTarget(listener, this, SettingListener.class);
		_defaultHandler.addSettingListener(listener);
		_fftHandler.addSettingListener(listener);
    }
    
    
    /**
     * Remove the listener as a receiver of setting change events.
     * @param listener Object to remove from receiving setting change events.
     */
    void removeSettingListener(SettingListener listener) {
        MESSAGE_CENTER.removeTarget(listener, this, SettingListener.class);
		_defaultHandler.removeSettingListener(listener);
		_fftHandler.removeSettingListener(listener);
    }
	
	
	/**
	 * Get the default screen handler
	 * @return The default screen handler.
	 */
	public DefaultScreenHandler getDefaultScreenHandler() {
		return _defaultHandler;
	}
	
	
	/**
	 * Determine if the scope screen is using the FFT Screen Handler.
	 * @return true if it is using the FFTScreenHandler and false if not.
	 */
	public boolean usingFFTHandler() {
		return _screenHandler == _fftHandler;
	}
	
	
	/**
	 * Toggle between using the FFT Screen Handler and using the default screen handler.
	 */
	public void toggleFFTHandler() {
		if (_screenHandler == _fftHandler) {
			setScreenHandler(_defaultHandler);
		}
		else {
			setScreenHandler(_fftHandler);
		}
	}
	
	
	/**
	 * Set the screen handler to the specified screen handler.
	 * @param newHandler The desired screen handler to use.
	 */
	private void setScreenHandler(AbstractScreenHandler newHandler) {
		if( _screenHandler != null )  remove( _screenHandler.getChart() );
		_screenHandler = newHandler;
		add( _screenHandler.getChart() );
		validate();
	}
	
	
	/**
	 * Find the most recently captured waveforms and make them visible onscreen.
	 * @throws java.lang.RuntimeException if there are no current waveforms available.
	 */
	public void findWaveforms() throws RuntimeException {
		_screenHandler.findWaveforms();
	}
    
    
    /** 
     * Update the scope screen for the latest captured data.
     * @param sender Identifies who sent the message.
     * @param traceEvents Array of trace events to display.
     * @param timestamp timestamp of the trace events.
     */
    public void updateTraces(Object sender, final TraceEvent[] traceEvents, Date timestamp) {
		_screenHandler.updateTraces(sender, traceEvents, timestamp);
    }
	
	
	/**
	 * Get the label of the trace source for the specified series.
	 * @param seriesIndex The index of the series
	 * @return the label of the trace source for the specified series
	 */
	public String getLabel(final int seriesIndex) {
		return model.getTraceSource(seriesIndex).getLabel();
	}
	
	
    /** 
	 * Get the default color of the indexed series
	 * @param seriesIndex The index of the series for which to get the color
	 * @return The default color for the specified series
	 */
    static public Color getDefaultSeriesColor(final int seriesIndex) {
        return defaultTraceColors[seriesIndex];
    }
    
    
    /** 
     * Get the background brightness of the chart whose color is a shade of gray.
     * @return The brightness of the chart from 0 (black) to 1 (white)
     */
    public float getBrightness() {
		return _screenHandler.getBrightness();
    }
    
    
    /** 
     * Set the background brightness of the chart whose color is a shade of gray.
     * @param brightness The brightness of the chart from 0 (black) to 1 (white)
     */
    public void setBrightness(final float brightness) {
		_screenHandler.setBrightness(brightness);
    }
    
    
    /** 
     * Returns the visibility of the grid.
     * @return True if the grid is visible and False if the grid is not visible.
     */
    public boolean isGridVisible() {
		return _screenHandler.isGridVisible();
    }
    
    
    /** 
     * Sets the visibility of the grid.
     * @param isVisible True if the grid should be made visible and False if it should be hidden.
     */
    public void setGridVisible(boolean isVisible) {
		_screenHandler.setGridVisible(isVisible);
    }
    
    
    /** 
     * Toggle the visibility of the chart grid.
     */
    public void toggleGridVisible() {
        _screenHandler.toggleGridVisible();
    }
    
    
    /** 
     * Returns the visibility of the legend.
     * @return True if the legend is visible and False if the legend is not visible.
     */
    public boolean isLegendVisible() {
		return _screenHandler.isLegendVisible();
    }
    
    
    /** 
     * Sets the visibility of the legend.
     * @param isVisible True if the legend should be made visible and False if it should be hidden.
     */
    public void setLegendVisible(boolean isVisible) {
		_screenHandler.setLegendVisible(isVisible);
    }
    
    
    /** 
     * Toggle the visibility of the chart legend.
     */
    public void toggleLegendVisible() {
        _screenHandler.toggleLegendVisible();
    }
}
