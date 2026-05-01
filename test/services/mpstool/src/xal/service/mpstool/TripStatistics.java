/*
 *  TripStatistics.java
 *
 *  Created on Thu Sep 23 11:59:18 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.service.mpstool;

import java.util.*;


/**
 * TripStatistics
 *
 * @author   tap
 * @since    Sep 23, 2004
 */
public class TripStatistics {
	/** the MPS PV */
	protected final String _mpsSignal;

	/** the Input PV */
	protected final String _inputSignal;

	/** the number of MPS trips recorded */
	protected int _mpsTrips;

	/** the number of times the MPS signal has been the first to trip among a correlated set */
	protected int _firstHits;

	/** the number of times the related input has tripped */
	protected int _inputTrips;


	/**
	 * Primary Constructor
	 * @param mpsSignal    the MPS PV
	 * @param inputSignal  the Input PV
	 * @param mpsTrips     the initial number of MPS trips
	 * @param firstHits    the initial number of first hit MPS trips
	 * @param inputTrips   the initial number of input trips
	 */
	public TripStatistics( final String mpsSignal, final String inputSignal, final int mpsTrips, final int firstHits, final int inputTrips ) {
		_mpsSignal = mpsSignal;
		_inputSignal = inputSignal;

		_mpsTrips = mpsTrips;
		_firstHits = firstHits;
		_inputTrips = inputTrips;
	}


	/**
	 * Constructor
	 * @param mpsSignal    the MPS PV
	 * @param inputSignal  the Input PV
	 */
	public TripStatistics( final String mpsSignal, final String inputSignal ) {
		this( mpsSignal, inputSignal, 0, 0, 0 );
	}
	
	
	/**
	 * Get the MPS PV.
	 * @return the MPS PV
	 */
	public String getMPSPV() {
		return _mpsSignal;
	}
	
	
	/**
	 * Get the input PV.
	 * @return the input PV
	 */
	public String getInputSignal() {
		return _inputSignal;
	}


	/** Increment by one the number of MPS trips. */
	public void incrementMPSTrips() {
		++_mpsTrips;
	}


	/**
	 * Get the number of MPS trips.
	 * @return   The the number of MPS trips recorded
	 */
	final public int getMPSTrips() {
		return _mpsTrips;
	}


	/** Increment by one the number of MPS trips. */
	public void incrementInputTrips() {
		++_inputTrips;
	}


	/**
	 * Get the number of Input trips.
	 * @return   The number of input trips
	 */
	final public int getInputTrips() {
		return _inputTrips;
	}


	/** Increment by one the number of MPS first hit trips. */
	public void incrementFirstHits() {
		++_firstHits;
	}


	/**
	 * Get the number of times the MPS signal was the first to trip among a correlate set of MPS trips.
	 * @return   The number of times the MPS signal was the first to trip
	 */
	final public int getFirstHits() {
		return _firstHits;
	}


	/**
	 * Generate an MPS trip comparator that compares trip statistics records based on the number of MPS trips.
	 * @return   a comparator of trip statistics comparing MPS trips
	 */
	public static Comparator<TripStatistics> mpsTripComparator() {
		return new Comparator<TripStatistics>() {
			public int compare( final TripStatistics stats1, final TripStatistics stats2 ) {
				final int trips1 = stats1.getMPSTrips();
				final int trips2 = stats2.getMPSTrips();
				return trips1 < trips2 ? -1 : trips1 > trips2 ? 1 : 0;
			}
			
			public boolean equals( final Object comparator ) {
				return this == comparator;
			}
		};
	}


	/**
	 * Generate an MPS first hit comparator that compares trip statistics records based on the number of times an MPS signal was the first to trip among a correlated group of trips.
	 * @return   a comparator of trip statistics comparing MPS first hits
	 */
	public static Comparator<TripStatistics> firstHitComparator() {
		return new Comparator<TripStatistics>() {
			public int compare( final TripStatistics stats1, final TripStatistics stats2 ) {
				final int trips1 = stats1.getFirstHits();
				final int trips2 = stats2.getFirstHits();
				return trips1 < trips2 ? -1 : trips1 > trips2 ? 1 : 0;
			}
			
			public boolean equals( final Object comparator ) {
				return this == comparator;
			}
		};
	}


	/**
	 * Generate an input trip comparator that compares trip statistics records based on the number of input trips.
	 * @return   a comparator of trip statistics comparing input trips
	 */
	public static Comparator<TripStatistics> inputTripComparator() {
		return new Comparator<TripStatistics>() {
			public int compare( final TripStatistics stats1, final TripStatistics stats2 ) {
				final int trips1 = stats1.getInputTrips();
				final int trips2 = stats2.getInputTrips();
				return trips1 < trips2 ? -1 : trips1 > trips2 ? 1 : 0;
			}
			
			public boolean equals( final Object comparator ) {
				return this == comparator;
			}
		};
	}
}

