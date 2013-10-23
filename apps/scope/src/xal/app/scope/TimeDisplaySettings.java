/*
 * TimeDisplaySettings.java
 *
 * Created on Wed Sep 03 12:27:35 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import xal.extension.widgets.plot.*;
import xal.tools.messaging.MessageCenter;
import xal.tools.data.*;


/**
 * TimeDisplaySettings holds the time division and lower time limit for the display.  It also 
 * generates the upper limit from these values.
 *
 * @author  tap
 */
public class TimeDisplaySettings implements DataListener {
	// constants
	static String dataLabel = "TimeDisplaySettings";
	final private int NUM_DIVISIONS = 5;	// five major time divisions per display
	
	// settings
    protected double _timeDivision;
	protected double _lowerLimit;
	
	// messaging
    final private SettingListener SETTING_EVENT_PROXY;
    final private MessageCenter MESSAGE_CENTER;
	
	
	/**
	 * TimeDisplaySettings constructor
	 */
	public TimeDisplaySettings(double lowerLimit, double timeDivision) {
        MESSAGE_CENTER = new MessageCenter( "ScopeScreen Center" );
        SETTING_EVENT_PROXY = MESSAGE_CENTER.registerSource( this, SettingListener.class );
		
		setLowerLimitAndTimeDivision(lowerLimit, timeDivision);
	}
	
	
	/**
	 * Default constructor
	 */
	public TimeDisplaySettings() {
		this(0, 100);
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
        setTimeDivision( adaptor.doubleValue("timeDivision") );
        setLowerLimit( adaptor.doubleValue("lowerLimit") );
    }
    
    
    /**
     * Write the receiver's data to the adaptor for external persistent storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write(DataAdaptor adaptor) {
        adaptor.setValue("timeDivision", getTimeDivision());
        adaptor.setValue("lowerLimit", getLowerLimit());
    }
    
    
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
	 * Calculate the upper limit given the lower limit and the time division.
     * @param lowerLimit The lower time limit.
     * @param timeDivision The time division.
	 */
	private double upperLimit(double lowerLimit, double timeDivision) {
		return lowerLimit + NUM_DIVISIONS * timeDivision;
	}
    
    
    /**
     * Set the lower time limit and the time division.
     * The time division defines the major ticks of the chart and along with the 
     * lower limit defines the upper time limit.
     * @param lowerLimit The lower time limit.
     * @param timeDivision The time division.
     */
    public void setLowerLimitAndTimeDivision(double lowerLimit, double timeDivision) {
		_lowerLimit = lowerLimit;
		_timeDivision = timeDivision;
        SETTING_EVENT_PROXY.settingChanged(this);
    }
	
	
	/**
	 * Set the lower and upper limits.
	 * @param lowerLimit The lower time limit
	 * @param upperLimit The upper time limit
	 */
	public void setLimits(double lowerLimit, double upperLimit) {
		double timeDivision = (upperLimit - lowerLimit) / NUM_DIVISIONS;
		setLowerLimitAndTimeDivision(lowerLimit, timeDivision);
	}
    
    
    /**
     * Sets the number of turns per division keeping the lower time limit fixed.
     * @param newTimeDivision The new time division in turns.
     */
    public void setTimeDivision(double newTimeDivision) {
        if ( newTimeDivision != getTimeDivision() ) {
            _timeDivision = newTimeDivision;
            SETTING_EVENT_PROXY.settingChanged(this);
        }
    }
    
    
    /**
     * Gets the number of turns displayed per major chart tick.
     * @return The number of turns displayed per major chart tick.
     */
    final public double getTimeDivision() {
        return _timeDivision;
    }
    
    
    /**
     * Sets the lower time limit for the chart.
     * @param lowerLimit The lower time limit in turns.
     */
    public void setLowerLimit(double lowerLimit) {
        if ( lowerLimit != getLowerLimit() ) {
			setLowerLimitAndTimeDivision(lowerLimit, _timeDivision);
            SETTING_EVENT_PROXY.settingChanged(this);
        }
    }
    
    
    /**
     * Gets the lower time limit displayed in the chart.
     * @return The lower time limit in turns.
     */
    final public double getLowerLimit() {
		return _lowerLimit;
    }
    
    
    /**
     * Gets the upper time limit displayed in the chart.
     * @return The upper time limit in turns.
     */
    final public double getUpperLimit() {
		return upperLimit(_lowerLimit, _timeDivision);
    }
	
	
	/**
	 * Apply the time settings to the specified chart.
	 * @param chart The chart to which to apply the settings
	 */
	final public void applySettingsTo(FunctionGraphsJPanel chart) {
		chart.setLimitsAndTicksX(_lowerLimit, getUpperLimit(), _timeDivision);
	}
}

