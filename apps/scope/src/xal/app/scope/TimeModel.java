/*
 * TimeModel.java
 *
 * Created on July 14, 2003, 9:34 AM
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import xal.extension.application.Util;
import xal.tools.data.*;
import xal.tools.messaging.MessageCenter;
import xal.ca.*;


/**
 * TimeModel manages the time settings for the scope including the units
 * of time used.
 *
 * @author  tap
 */
public class TimeModel implements DataListener {
    // miscellaneous constants
    final static String dataLabel = "TimeModel";
    
    // time units type enumeration options
    final static public int NONE = -1;
    final static public int TURN = 0;
    final static public int MICROSECOND = 1;
    
    // messaging variables
    private final MessageCenter MESSAGE_CENTER;
    private final TimeModelListener TIME_MODEL_EVENT_PROXY;
    private final SettingListener SETTING_EVENT_PROXY;
    
    // state variables
    protected int unitsType = NONE;
    protected TimeUnitsHandler unitsHandler;
    protected Object unitLock = new Object();
    
    
    /** Creates a new instance of TimeModel */
    public TimeModel() {
        // setup messaging notifications
        MESSAGE_CENTER = new MessageCenter("Time Model");
        TIME_MODEL_EVENT_PROXY = MESSAGE_CENTER.registerSource(this, TimeModelListener.class);
        SETTING_EVENT_PROXY = MESSAGE_CENTER.registerSource(this, SettingListener.class);
        
        // set the default units
        setUnitsType(TURN);
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
        if ( adaptor.hasAttribute("unitsType") ) {
            setUnitsType( adaptor.intValue("unitsType") );
        }
    }
    
    
    /**
     * Write the receiver's data to the adaptor for external persistent storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write(DataAdaptor adaptor) {
        adaptor.setValue("unitsType", getUnitsType());
    }
    
    
    /** 
     * Add a TimeModelListener.
     * @param listener The object to add as a listener of time model events.
     */
    public void addTimeModelListener(TimeModelListener listener) {
        MESSAGE_CENTER.registerTarget(listener, this, TimeModelListener.class);
    }
    
    
    /** 
     * Remove a TimeModelListener.
     * @param listener The object to remove as a listener of time model events.
     */
    public void removeTimeModelListener(TimeModelListener listener) {
        MESSAGE_CENTER.removeTarget(listener, this, TimeModelListener.class);
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
     * Get the units type in use.
     * @return The units type.
     */
    public int getUnitsType() {
        return unitsType;
    }
    
    
    /**
     * Use the specified time units which should be one of the time units 
     * type constants: TURNS, MICROSECONDS
     * @param newUnitsType one of TURNS or MICROSECONDS indicating the time units
     */
    public void setUnitsType(int newUnitsType) throws RuntimeException {
        if ( unitsType == newUnitsType )  return;
        
        synchronized(unitLock) {
            if (unitsHandler != null) { 
               unitsHandler.dispose(); 
            }
        
            switch(newUnitsType) {
                case TURN:
                    unitsHandler = new TurnUnitsHandler();
                    break;
                case MICROSECOND:
                    unitsHandler = new MicrosecondUnitsHandler( this, TIME_MODEL_EVENT_PROXY );
                    break;
                default:
                    throw new IllegalArgumentException("Time units must be one of the enumerated types!  Units specified: " + newUnitsType);
            }
            
            unitsType = newUnitsType;
            
            SETTING_EVENT_PROXY.settingChanged( this );
            TIME_MODEL_EVENT_PROXY.timeUnitsChanged( this );
            TIME_MODEL_EVENT_PROXY.timeConversionChanged( this );
        }
    }
    
    
    /**
     * Convert in place an array of times in turns to an array of times in 
     * the active time units.
     * @param times The array of times in units of turns to be converted in place to the active time units.
     */
    public void convertTurns(final double[] times) {
        synchronized (unitLock) {
            unitsHandler.convertTurns(times);
        }
    }
    
    
    /**
     * Get the array of available units types.
     * @return The array of available units types.
     */
    static public int[] getAvailableUnitsTypes() {
        return new int[] {TURN, MICROSECOND};
    }
    
    
    /**
     * Get the units label corresponding to the units type.
     * @return The units label.
     */
    public String getUnitsLabel() {
        synchronized(unitLock) {
            return getUnitsLabel(this.unitsType);
        }
    }
    
    
    /**
     * Get the units label corresponding to the units type.
     * @param unitsFlag The units type for the label to fetch.
     * @return The units label.
     */
    public String getUnitsLabel(int unitsFlag) {
        switch(unitsFlag) {
            case TURN:
                return TurnUnitsHandler.units;
            case MICROSECOND:
                return MicrosecondUnitsHandler.units;
            default:
                return "unknown";
        }
    }
    
    
    /**
     * Get the active time units handler.
     * @return The active time units handler.
     */
    TimeUnitsHandler getUnitsHandler() {
        synchronized(unitLock) {
            return unitsHandler;
        }
    }
    
    
    /**
     * Get the object used for locking out unit related calls.
     * @return The object that acts as the unit lock.
     */
    Object getUnitLock() {
        return unitLock;
    }
    
    
    /**
     * Dispose of the resources held by the TimeModel.
     */
    void dispose() {
        unitsHandler.dispose();
    }
}



/**
 * TurnUnitsHandler is a no operation converter that takes turns
 * and simply returns the same value.  It exists to simplify the 
 * code so the time model can always use a units handler.
 */
class TurnUnitsHandler implements TimeUnitsHandler {
    final static public String units = "turns";
    
    /**
     * Construct a TurnUnitsHandler.
     */
    public TurnUnitsHandler() {
    }
    
    
    /**
     * Get the label of the units.
     * @return The label of the units.
     */
    public String getUnits() {
        return units;
    }
    
    
    /**
     * Convert in place an array of times in turns to an array of times in 
     * the turns.  This is a no-operation.
     * @param times The array of times in units of turns.
     */
    final public void convertTurns(final double[] times) {
    }
    
    
    /**
     * Dispose of any resources.  Nothing to dispose.
     */
    public void dispose() {
    }
}



/**
 * MicrosecondUnitsHandler monitors the ring frequency to provide the 
 * correct time scale conversion from turns.
 */
class MicrosecondUnitsHandler implements TimeUnitsHandler, IEventSinkValue {
    final static public String units = "microseconds";
	final static private String turnMHzPVName; 
    
    private volatile Monitor monitor;
    private volatile double period;     // microseconds per turn

    private final TimeModel model;
    private final TimeModelListener TIME_MODEL_EVENT_PROXY;
    
    static {
		final String TURN_MHZ_PV_KEY = "turnMHzPV";
		java.util.Map<String,String> properties = Util.getPropertiesForResource( "scope.properties" );
		// use the scope.properties file to get the defaul value, but allow it to be overriden as a command line property
		turnMHzPVName = System.getProperties().getProperty( TURN_MHZ_PV_KEY, properties.get(TURN_MHZ_PV_KEY) );
	}
	
	
    /**
     * Constructor
     */
    public MicrosecondUnitsHandler( final TimeModel aModel, final TimeModelListener aModelProxy ) {
        model = aModel;
        TIME_MODEL_EVENT_PROXY = aModelProxy;
        
        try {
            final Channel ringFrequencyChannel = ChannelFactory.defaultFactory().getChannel( turnMHzPVName );
			if ( ringFrequencyChannel.connectAndWait() ) {
				period = 1 / ringFrequencyChannel.getValDbl();
				monitor = ringFrequencyChannel.addMonitorValue( this, Monitor.VALUE );				
			}
			else {
				throw new RuntimeException( "Microsecond units handler failed due to connection failure for PV:\n" + turnMHzPVName );
			}
        }
        catch( ConnectionException exception ) {
            throw new RuntimeException( "Microsecond units handler failed due to connection exception:\n" + exception.getMessage() );
        }
        catch( GetException exception ) {
            throw new RuntimeException( "Microsecond units handler failed due to get exception:\n" + exception.getMessage() );
        }
        catch( MonitorException exception ) {
            throw new RuntimeException( "Microsecond units handler failed due to monitor exception:\n" + exception.getMessage() );
        }
    }
    
    
    /**
     * Get the label of the units.
     * @return The label of the units.
     */
    public String getUnits() {
        return units;
    }

    
    /**
     * Convert in place an array of times in turns to an array of times in 
     * the microseconds.
     * @param times The array of times in units of turns to be converted in place to microseconds.
     */
    final public void convertTurns( final double[] times ) {
        for ( int index = 0 ; index < times.length ; index++ ) {
            times[index] = period * times[index];
        }
    }
    
    
    /**
     * Dispose of any resources.
     */
    public void dispose() {
        monitor.clear();
        monitor = null;
    }
    
    
    /**
     * Capture a ring frequency monitor event.  If the frequency has 
     * changed then change the scale factor and post a notice so the 
     * time elements can be recalculated.
     * @param record The channel record with the ring frequency.
     * @param chan The channel being monitored.
     */
    public void eventValue( final ChannelRecord record, final Channel chan ) {
        // make sure we block the model from changing the handler during the post and vice-versa
        synchronized( model.getUnitLock() ) {
            if ( monitor == null )  return;     // never post if handler has been disabled
            
            double newPeriod = 1 / record.doubleValue();
            // The period is meaningful down to the nanosecond so anything finer is irrelevant
            if ( Math.abs(period - newPeriod) >= 1.0e-3 ) {
                System.out.println("old period: " + period + ", new period: " + newPeriod);
                period = newPeriod;
                TIME_MODEL_EVENT_PROXY.timeConversionChanged( model );
            }
        }
    }
}