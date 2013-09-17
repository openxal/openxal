/*
 * WaveformSnapshot.java
 *
 * Created on Fri Aug 22 11:04:37 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import xal.tools.correlator.Correlation;
import xal.ca.*;

import java.util.*;


/**
 * WaveformSnapshot represents the snapshot of raw waveforms on the scope from the same pulse.
 *
 * @author  tap
 */
public class WaveformSnapshot {
	/** The time stamp of the pulse. */
	protected Date timestamp;
	
	/** The waveforms belonging to the snapshot */
	protected Waveform[] waveforms;
	
	
	/**
	 * WaveformSnapshot constructor.
	 * @param correlation The pulse's correlation event from which we extract the waveforms.
	 * @param pvMap The map of entries each containing a waveform id as the key and PV name as the value.
	 * @param timeMap The map of entries each containing a waveform id as the key and the associated WaveformTime as the value
	 */
	public WaveformSnapshot( final Correlation<ChannelTimeRecord> correlation, final Map<String,String> pvMap, final Map<String,WaveformTime> timeMap ) {
		timestamp = correlation.meanDate();
		
		final Set<String> keys = new HashSet<>( pvMap.keySet() );
		keys.retainAll( correlation.names() );
		
		int count = keys.size();
		waveforms = new Waveform[count];
		int index = 0;
		for ( final String key : keys ) {
			final ChannelRecord record = correlation.getRecord( key );
			final WaveformTime timeInfo = timeMap.get( key );
			final String pvName = pvMap.get( key );
			waveforms[index] = new Waveform( pvName, record.doubleArray(), timeInfo );
			++index;
		}
	}
	
	
	/**
	 * Get the number of waveforms in the snapshot.
	 * @return The number of waveforms in the snapshot.
	 */
	public int getWaveformCount() {
		return waveforms.length;
	}
	
	
	/**
	 * Get the array of waveforms in the snapshot.
	 * @return The array of waveforms in the snapshot.
	 */
	public Waveform[] getWaveforms() {
		return waveforms;
	}
	
	
	/**
	 * Get the time range over all of the waveforms.
	 * @return The time range over all of the waveforms measured in turns.
	 */
	public double[] getTimeRange() {
		double lowerTime = Double.POSITIVE_INFINITY;
		double upperTime = Double.NEGATIVE_INFINITY;
		
		for( int index = 0 ; index < waveforms.length ; index++ ) {
			lowerTime = Math.min( lowerTime, waveforms[index].getStartTime() );
			upperTime = Math.max( upperTime, waveforms[index].getEndTime() );
		}
		
		return new double[] {lowerTime, upperTime};
	}
	
	
	/**
	 * Override toString() to create a string representation of the raw waveform snapshot.  The time stamp
	 * is written along with each waveform and their supporting information.
	 * @return A string representation of the raw waveform snapshot.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#Raw waveform data in the form (time, value) for each sample.");
		buffer.append("\n#Time is in units of turns and is relative to cycle start.");
		buffer.append("\n\nTimestamp: " + timestamp);
		buffer.append("\nWaveforms: " + waveforms.length);
		buffer.append("\n\n");
		for ( int index = 0 ; index < waveforms.length ; index++ ) {
			buffer.append( waveforms[index] );
			buffer.append('\n');
		}
		return buffer.toString();
	}
}

