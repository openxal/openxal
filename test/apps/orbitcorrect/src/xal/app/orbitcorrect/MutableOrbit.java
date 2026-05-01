/*
 * MutableOrbit.java
 *
 * Created on Tue Jul 13 15:54:21 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import java.util.*;

import xal.smf.AcceleratorSeq;
import xal.smf.impl.BPM;


/**
 * MutableOrbit
 *
 * @author  tap
 * @since Jul 13, 2004
 */
public class MutableOrbit extends Orbit {
	/** cache of the latest orbit */
	protected Orbit _orbitCache;
	
	
	/**
	 * Primary constructor
	 */
	protected MutableOrbit( final AcceleratorSeq sequence, final SortedMap<BpmAgent, BpmRecord> bpmRecords, final Date timeStamp ) {
		super( sequence, bpmRecords, timeStamp );
		_orbitCache = null;
	}
	
	
	/**
	 * Primary constructor
	 */
	protected MutableOrbit( final AcceleratorSeq sequence, final SortedMap<BpmAgent, BpmRecord> bpmRecords ) {
		this( sequence, bpmRecords, new Date() );
	}
	
	
	/**
	 * Constructor with an empty set of bpmRecords
	 */
	public MutableOrbit( final AcceleratorSeq sequence ) {
		this( sequence, null );
	}
	
	
	/**
	 * Copy Constructor
	 * @param orbit the orbit to copy
	 */
	public MutableOrbit( final Orbit orbit ) {
		this( orbit._sequence, orbit._bpmRecords, orbit._timeStamp );
	}
	
	
	/**
	 * Get an immutable copy of this orbit
	 * @return an immutable copy of this orbit
	 */
	public Orbit getOrbit() {
		synchronized(_lock) {
			if ( _needsCache ) {
				_orbitCache = new Orbit( _sequence, _bpmRecords, _timeStamp );
			}
			return _orbitCache;
		}
	}
	
	
	/**
	 * Clear the BPM records
	 */
	public void clear() {
		synchronized( _lock ) {
			_needsCache = true;
			_orbitCache = null;
			_bpmRecords.clear();
		}
	}
	
	
	/**
	 * Add the specified BPM record to the orbit replacing any previous record for the same BPM
	 * @param record the BPM record to add to the Orbit
	 */
	public void addRecord( final BpmRecord record ) {
		synchronized(_lock) {
			_needsCache = true;
			_bpmRecords.put( record.getBpmAgent(), record );
			_timeStamp = record.getTimestamp();
		}
	}
	
	
	/**
	 * Remove the specified BPM record from the orbit.
	 * @param record the BPM record to remove from the orbit
	 */
	public void removeRecord( final BpmRecord record ) {
		synchronized ( _lock ) {
			_needsCache = true;
			_bpmRecords.remove( record.getBpmAgent() );
		}
	}
	
	
	/**
	 * Get the BPM record associated with the specifed BPM agent.
	 * @param bpmAgent the BPM agent for which to fetch the BPM record
	 * @return the BPM record corresponding to the specified BPM agent
	 */
	public BpmRecord getRecord( final BpmAgent bpmAgent ) {
		synchronized(_lock) {
			return super.getRecord( bpmAgent );
		}
	}
	
	
	/**
	 * Get the list of BPM records in the orbit sorted by BPM position.
	 * @return the list of BPM records in the orbit
	 */
	public List<BpmRecord> getRecords() {
		synchronized(_lock) {
			return super.getRecords();
		}
	}	
	
	
	/**
	 * Generate a description of the orbit.
	 * @return a description of the orbit
	 */
	public String toString() {
		synchronized( _lock ) {
			return super.toString();
		}
	}
}

