/*
 *  CorrectorDistribution.java
 *
 *  Created on Tue Sep 28 13:22:18 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.smf.*;
import xal.smf.impl.*;

import java.util.*;


/**
 * CorrectorDistribution
 * @author   tap
 * @since    Sep 28, 2004
 */
public class CorrectorDistribution {
	/** the records of corrector state keyed by corrector */
	protected Map<CorrectorSupply,CorrectorRecord> _correctorRecords;

	/** lock for synchronizing data access */
	protected Object _lock;


	/**
	 * Primary Constructor
	 * @param correctorRecords  the records of corrector state
	 */
	public CorrectorDistribution( final Map<CorrectorSupply,CorrectorRecord> correctorRecords ) {
		_lock = new Object();

		_correctorRecords = ( correctorRecords == null ) ? new HashMap<CorrectorSupply,CorrectorRecord>() : new HashMap<CorrectorSupply,CorrectorRecord>( correctorRecords );
	}


	/**
	 * Copy Constructor
	 * @param distribution  the distribution to copy
	 */
	public CorrectorDistribution( final CorrectorDistribution distribution ) {
		this( distribution._correctorRecords );
	}


	/** Constructor */
	public CorrectorDistribution() {
		this( (Map<CorrectorSupply,CorrectorRecord>)null );
	}


	/**
	 * Get the corrector record associated with the specifed corrector supply.
	 * @param supply  the corrector supply for which to fetch the corrector record
	 * @return           the corrector record corresponding to the specified corrector
	 */
	public CorrectorRecord getRecord( final CorrectorSupply supply ) {
		return _correctorRecords.get( supply );
	}
	
	
	/**
	 * Get the recorded field for the specified corrector supply.
	 * @param supply  the corrector supply for which to get the recorded field
	 * @return           the recorded field of the specified corrector
	 */
	public double getField( final CorrectorSupply supply ) {
		return getRecord( supply ).getField();
	}


	/**
	 * Get the list of corrector records in the orbit sorted by corrector position.
	 * @return   the list of corrector records in the orbit
	 */
	public List<CorrectorRecord> getRecords() {
		return new ArrayList<CorrectorRecord>( _correctorRecords.values() );
	}
	
	
	/**
	 * Get the fields for the specified corrector supplies in the same order as the agents are specified.
	 * @param supplies the corrector supplies
	 * @return the strengths of the specified corrector agents
	 */
	public double[] getFields( final List<CorrectorSupply> supplies ) {
		final double[] fields = new double[supplies.size()];
		int index = 0;
		synchronized( _lock ) {
			for ( CorrectorSupply supply : supplies ) {
				fields[index++] = getRecord( supply ).getField();
			}
		}
		
		return fields;
	}


	/**
	 * Calculate the duty load based on how close the fields are to the limits and bias the weight to the worst.
	 */
	public double worstBiasedLoad() {
		final Collection<CorrectorRecord> records = _correctorRecords.values();
		final int recordCount = records.size();

		if ( recordCount == 0 )  return 0.0;	// nothing more to do

		final List<Double> duties = new ArrayList<>( recordCount );

		for ( CorrectorRecord record : records ) {
			final CorrectorSupply supply = record.getCorrectorSupply();
			final double lowerLimit = supply.getLowerFieldLimit();
			final double upperLimit = supply.getUpperFieldLimit();
			final double field = record.getField();
			final double duty = calculateDutyLoad( field, lowerLimit, upperLimit );
			duties.add( duty );
		}

		// sort the duties from biggest to smallest
		Collections.sort( duties );
		Collections.reverse( duties );

		double dutySum = 0.0;
		double weightSum = 0.0;
		double weight = 1.0;
		for ( final double duty : duties ) {
			dutySum += duty * weight;
			weightSum += weight;
			weight /= 2;	// each successive weight gets halved so most weight on worst duty
		}

		return dutySum / weightSum;
	}


	/**
	 * Calculate the duty load based on how close the fields are to the limits and take the mean square.
	 */
	public double meanSquareLoad() {
		final Collection<CorrectorRecord> records = _correctorRecords.values();
		final int recordCount = records.size();

		if ( recordCount == 0 )  return 0.0;	// nothing more to do

		double squareLoadSum = 0;
		for ( CorrectorRecord record : records ) {
			final CorrectorSupply supply = record.getCorrectorSupply();
			final double lowerLimit = supply.getLowerFieldLimit();
			final double upperLimit = supply.getUpperFieldLimit();
			final double field = record.getField();
			final double duty = calculateDutyLoad( field, lowerLimit, upperLimit );
			squareLoadSum += duty * duty;
		}

		return squareLoadSum / recordCount;
	}


	/** calculate the duty load based on the field and the limits */
	private double calculateDutyLoad( final double field, final double lowerLimit, final double upperLimit ) {
		final double range = upperLimit - lowerLimit;

		if ( range == 0.0 || field <= lowerLimit || field >= upperLimit ) {
			return 1.0;
		}
		else {
			// duty = amplitude * ( field - center )^2 where amplitude is chosen for duty = 1.0 at limits
			final double center = lowerLimit + range / 2;
			final double amplitude = 4.0 / ( range * range );
			return amplitude * ( field - center ) * ( field - center );
		}
	}


	/**
	 * Generate a description of this distribution.
	 */
	public String toString() {
		return _correctorRecords.values().toString();
	}
}

