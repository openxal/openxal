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
	 * Calculate the mean square of the corrector strengths taken as a fraction of full scale.
	 * @return   Description of the Return Value
	 */
	public double meanSquareLoad() {
		final Collection<CorrectorRecord> records = _correctorRecords.values();
		
		double squareLoadSum = 0;
		for ( CorrectorRecord record : records ) {
			final double limit = 0.01;
			final double duty = record.getField() / limit;
			squareLoadSum += duty * duty;
		}
		
		return squareLoadSum / records.size();
	}


	/**
	 * Generate a description of this distribution.
	 * @return   a description of this distribution
	 */
	public String toString() {
		return _correctorRecords.values().toString();
	}
}

