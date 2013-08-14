/*
 *  AmplitudeFilter.java
 *
 *  Created on Wed Sep 08 16:47:40 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;


/**
 * AmplitudeFilter
 *
 * @author   tap
 * @since    Sep 08, 2004
 */
public class AmplitudeFilter implements BpmRecordFilter {
	/** default threshold */
	protected final static double DEFAULT_THRESHOLD = 20.0;

	/** the amplitude threshold */
	protected double _threshold;


	/**
	 * Primary constructor
	 *
	 * @param threshold  the amplitude threshold
	 */
	public AmplitudeFilter( double threshold ) {
		setThreshold( threshold );
	}


	/** Constructor using the defaul threshold */
	public AmplitudeFilter() {
		this( DEFAULT_THRESHOLD );
	}


	/**
	 * Set the amplitude threshold.
	 *
	 * @param threshold  The new threshold value
	 */
	public void setThreshold( final double threshold ) {
		_threshold = threshold;
	}


	/**
	 * Get the amplitude threshold.
	 *
	 * @return   The threshold value
	 */
	public double getThreshold() {
		return _threshold;
	}


	/**
	 * Determine if the BPM record should be accepted.
	 *
	 * @param record  the BPM record to filter
	 * @return        true to accept the record and false to reject it
	 */
	public boolean accept( final BpmRecord record ) {
		return record.getAmpAvg() >= _threshold;
	}
}

