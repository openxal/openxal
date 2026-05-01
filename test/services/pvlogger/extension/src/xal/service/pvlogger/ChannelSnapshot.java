/*
 * ChannelSnapshot.java
 *
 * Created on Thu Dec 04 13:26:25 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger;

import xal.tools.ArrayTool;

import xal.ca.ChannelTimeRecord;
import xal.ca.Timestamp;


/**
 * ChannelSnapshot is a representation of the data associated with a channel at some point in time.  
 * @author  tap
 */
public class ChannelSnapshot {
	/** minimum value of a double supported by Oracle where we assume it can at least handle a small float */
	final static private double MIN_VALUE = (double)Float.MIN_VALUE;
	
	/** raw process variable */
	final protected String _pv;
	
	/** time stamp reported for the channel monitor event */
	final protected Timestamp _timestamp;
	
	/** value array */
	final protected double[] _value;
	
	/** status code */
	final protected int _status;
	
	/** severity code */
	final protected int _severity;
	
	
	/**
	 * Primary constructor of a snapshot.
	 * @param pv The PV identifying the channel.
	 * @param value The value of the channel's data at the time of the snapshot.
	 * @param status The status of the channel at the time of the snapshot.
	 * @param severity The severity of the channel at the time of the snapshot.
	 * @param timestamp The timestamp of the snapshot identifying when the data was acquired.
	 */
	public ChannelSnapshot( final String pv, final double[] value, final int status, final int severity, final Timestamp timestamp ) {
		_pv = pv;
		_value = value;
		_status = status;
		_severity = severity;
		_timestamp = timestamp;
		
		processValue( value );
	}
	
	
	/**
	 * Constructor of a snaphsot from a channel record.
	 * @param pv The PV identifying the channel.
	 * @param record The record holding the channel data, state and timestamp
	 */
	public ChannelSnapshot(String pv, ChannelTimeRecord record) {
		this(pv, record.doubleArray(), record.status(), record.severity(), record.getTimestamp());
	}
	
	
	/** 
	 * Process the value array to make it compatible with Oracle's limited support of doubles. Modify the array inline to avoid an underflow.
	 * @param array the value array to process in place
	 */
	static private void processValue( final double[] array ) {
		final int count = array.length;
		for ( int index = 0 ; index < count ; index++ ) {
			if ( Math.abs( array[index] ) <= MIN_VALUE ) {
				array[index] = 0.0;
			}
		}
	}
	
	
	/**
	 * Get the PV
	 * @return the PV
	 */
	public String getPV() {
		return _pv;
	}
	
	
	/**
	 * Get the value of the PV's data at the time of the snapshot
	 * @return the value of the PV's data
	 */
	public double[] getValue() {
		return _value;
	}


	/** get the scalar value which corresponds to the first element of the value array if it exists or NaN otherwise */
	public double getScalarValue() {
		return _value != null && _value.length > 0 ? _value[0] : Double.NaN;
	}


	/** Get the number of elements in the value array */
	public int getValueCount() {
		return _value != null ? _value.length : 0;
	}
	
	
	/**
	 * Get the status of the PV at the time of the snapshot.
	 * @return the status of the PV 
	 */
	public int getStatus() {
		return _status;
	}
	
	
	/**
	 * Get the severity of the PV at the time of the snapshot.
	 * @return the severity of the PV
	 */
	public int getSeverity() {
		return _severity;
	}
	
	
	/**
	 * Get the timestamp of the PV's data identifying the time the data was acquired.
	 * @return the timestamp of the PV's data
	 */
	public Timestamp getTimestamp() {
		return _timestamp;
	}
	
	
	/**
	 * Override toString() to describe the snapshot in a meaningful way.
	 * @return a string describing the snapshot.
	 */
	public String toString() {
		return _pv + "\t  " + _timestamp + "  " + ArrayTool.asString(_value);
	}
}

