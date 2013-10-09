/*
 *  MutableCorrectorDistribution.java
 *
 *  Created on Tue Sep 28 14:37:05 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import java.util.*;

import xal.smf.impl.Dipole;


/**
 * MutableCorrectorDistribution
 * @author   tap
 * @since    Sep 28, 2004
 */
public class MutableCorrectorDistribution extends CorrectorDistribution {
	/** cache of the latest corrector distribution */
	protected CorrectorDistribution _distributionCache;


	/**
	 * Primary constructor
	 * @param correctorRecords  Description of the Parameter
	 */
	protected MutableCorrectorDistribution( final Map<CorrectorSupply,CorrectorRecord> correctorRecords ) {
		super( correctorRecords );
		_distributionCache = null;
	}


	/**
	 * Constructor with an empty set of corrector records
	 */
	public MutableCorrectorDistribution() {
		this( null );
	}


	/**
	 * Get an immutable copy of this corrector distribution
	 * @return   an immutable copy of this corrector distribution
	 */
	public CorrectorDistribution getDistribution() {
		synchronized ( _lock ) {
			if ( _distributionCache == null ) {
				_distributionCache = new CorrectorDistribution( this );
			}
			return _distributionCache;
		}
	}


	/** Clear the corrector records  */
	public void clear() {
		synchronized ( _lock ) {
			_distributionCache = null;
			_correctorRecords.clear();
		}
	}


	/**
	 * Add the specified corrector record to the distribution replacing any previous record for the same corrector.
	 * @param record  the corrector record to add to the distribution
	 */
	public void addRecord( final CorrectorRecord record ) {
		synchronized ( _lock ) {
			_distributionCache = null;
			_correctorRecords.put( record.getCorrectorSupply(), record );
		}
	}


	/**
	 * Get the corrector record associated with the specifed corrector supply.
	 * @param supply  the corrector supply for which to fetch this distribution's corrector record
	 * @return           the corrector record corresponding to the specified corrector supply
	 */
	public CorrectorRecord getRecord( final CorrectorSupply supply ) {
		synchronized ( _lock ) {
			return super.getRecord( supply );
		}
	}


	/**
	 * Get the list of corrector records in the distribution sorted by corrector position.
	 * @return   the list of corrector records
	 */
	public List<CorrectorRecord> getRecords() {
		synchronized ( _lock ) {
			return super.getRecords();
		}
	}


	/**
	 * Generate a description of this corrector distribution.
	 * @return   a description of this corrector distribution
	 */
	public String toString() {
		synchronized ( _lock ) {
			return super.toString();
		}
	}
}

