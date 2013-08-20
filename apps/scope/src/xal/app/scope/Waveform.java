/*
 * Waveform.java
 *
 * Created on Fri Aug 22 10:26:34 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;


/**
 * Waveform is a representation of a waveform.
 *
 * @author  tap
 */
final class Waveform {
	/** The waveform's name. */
	protected String name;
	
	/** The waveform samples. */
	protected WaveformSample[] samples;
	
	
	/**
	 * Waveform constructor.
	 * @param aName The name of the waveform.
	 * @param values The array of the waveform's sample values.
	 * @param timeInfo Structure holding the delay from cycle start and the sample period.
	 */
	public Waveform(String aName, double[] values, WaveformTime timeInfo) {
		name = aName;
		samples = new WaveformSample[values.length];
		
		double turn = timeInfo.delay;
		for ( int index = 0 ; index < samples.length ; index++ ) {
			samples[index] = new WaveformSample(turn, values[index]);
			turn += timeInfo.samplePeriod;
		}
	}
	
	/**
	 * Get the waveform's name.
	 * @return The waveform's name.
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Get the number of samples in the waveform.
	 * @return The number of samples in the waveform.
	 */
	public int getSampleCount() {
		return samples.length;
	}
	
	
	/**
	 * Get the minimum value found in the waveform.
	 * @return The minimum value found in the waveform.
	 */
	public double getMinValue() {
		double minValue = Double.MAX_VALUE;
		for ( int index = 0 ; index < samples.length ; index++ ) {
			minValue = Math.min(minValue, samples[index].getValue() );
		}
		
		return minValue;
	}
	
	
	/**
	 * Get the maximum value found in the waveform.
	 * @return The maximum value found in the waveform.
	 */
	public double getMaxValue() {
		double maxValue = Double.MIN_VALUE;
		for ( int index = 0 ; index < samples.length ; index++ ) {
			maxValue = Math.max(maxValue, samples[index].getValue() );
		}
		
		return maxValue;
	}
	
	
	/**
	 * Get the time of the first sample.
	 * @return the time of the first sample in turns relative to cycle start.
	 */
	public double getStartTime() {
		return samples[0].getTime();
	}
	
	
	/**
	 * Get the time of the last sample.
	 * @return the time of the last sample in turns relative to cycle start.
	 */
	public double getEndTime() {
		return samples[samples.length-1].getTime();
	}
	
	
	/**
	 * Overrides inherited toString() to provide a string representation of the waveform.  It presents the
	 * waveform's name, the number of samples and a list of the samples.
	 * @return A string representation of the waveform.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("PV: " + name);
		buffer.append("\nSamples: " + samples.length);
		buffer.append("\n");
		for ( int index = 0 ; index < samples.length ; index++ ) {
			buffer.append(samples[index]);
			buffer.append("\n");
		}
		return buffer.toString();
	}
}

