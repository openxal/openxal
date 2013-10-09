/*
 * WaveformSample.java
 *
 * Created on Fri Aug 22 10:27:54 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;


/**
 * WaveformSample represents a sample from a waveform.  The sample simply consists of the sample time in turns 
 * from cycle start and the value of the sample.
 *
 * @author  tap
 */
final class WaveformSample {
	/** The sample's time relative to cycle start measured in turns. */
	final public double turn;
	
	/** The sample's value. */
	final public double value;
	
	
	/**
	 * The WaveformSample constructor.
	 * @param newTurn The sample's time relative to cycle start measured in turns.
	 * @param newValue The sample's value.
	 */
	public WaveformSample(double newTurn, double newValue) {
		turn = newTurn;
		value = newValue;
	}
	
	
	/**
	 * Get the sample's time from cycle start.
	 * @return the sample's time measured in turns from cycle start.
	 */
	public double getTime() {
		return turn;
	}
	
	
	/**
	 * Get the sample's value.
	 * @return the sample's value;
	 */
	public double getValue() {
		return value;
	}
	
	
	/**
	 * Overrides inherited toString() to provide a string representation of the WaveformSample.  It provides the
	 * time/value pair.
	 * @return A string representation of the WaveformSample.
	 */
	public String toString() {
		return String.valueOf(turn) + " " + String.valueOf(value);
	}
}

