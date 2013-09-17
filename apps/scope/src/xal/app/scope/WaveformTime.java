/*
 * WaveformTime.java
 *
 * Created on Fri Aug 22 11:26:20 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;


/**
 * WaveformTime is a simple container that holds the delay and sample period for a waveform.
 *
 * @author  tap
 */
class WaveformTime {
	/** The waveform's time delay in turns from cycle start */
	final public double delay;
	
	/** The waveform's sample period in turns */
	final public double samplePeriod;
	
	
	/**
	 * WaveformTime constructor
	 * @param aDelay The waveform's time delay in turns from cycle start.
	 * @param aSamplePeriod The waveform's sample period in turns.
	 */
	public WaveformTime(double aDelay, double aSamplePeriod) {
		delay = aDelay;
		samplePeriod = aSamplePeriod;
	}
}

