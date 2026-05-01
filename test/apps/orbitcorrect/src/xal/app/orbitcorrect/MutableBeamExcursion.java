//
//  MutableBeamExcursion.java
//  xal
//
//  Created by Tom Pelaia on 1/10/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import java.util.*;

import xal.smf.AcceleratorSeq;


/** mutable version of BeamExcursion */
public class MutableBeamExcursion extends BeamExcursion {
	/** cache of the beam excursion since the last time it was modified */
	protected BeamExcursion _excursionCache;
	
	
	/** Primary constructor */
	protected MutableBeamExcursion( final AcceleratorSeq sequence, final SortedMap<BeamMarker<?>, BeamMarkerRecord<?>> records, final Date timeStamp ) {
		super( sequence, records, timeStamp );
		_excursionCache = null;
	}
	
	
	/** Primary constructor */
	protected MutableBeamExcursion( final AcceleratorSeq sequence, final SortedMap<BeamMarker<?>, BeamMarkerRecord<?>> records ) {
		this( sequence, records, new Date() );
	}
	
	
	/** Constructor with an empty set of bpmRecords */
	public MutableBeamExcursion( final AcceleratorSeq sequence ) {
		this( sequence, null );
	}
	
	
	/**
	 * Copy Constructor
	 * @param excursion the excursion to copy
	 */
	public MutableBeamExcursion( final BeamExcursion excursion ) {
		this( excursion._sequence, excursion._records, excursion._timeStamp );
	}
	
	
	/**
	 * Get an immutable copy of this excursion
	 * @return an immutable copy of this excursion
	 */
	public BeamExcursion getBeamExcursion() {
		synchronized( LOCK ) {
			if ( _needsCache ) {
				_excursionCache = new BeamExcursion( _sequence, _records, _timeStamp );
			}
			return _excursionCache;
		}
	}
	
	
	/** Clear the Beam Marker records */
	public void clear() {
		synchronized( LOCK ) {
			_needsCache = true;
			_excursionCache = null;
			_records.clear();
		}
	}
	
	
	/**
	 * Add the specified Beam Marker record to the excursion replacing any previous record for the same Beam Marker
	 * @param record the Beam Marker record to add to the BeamExcursion
	 */
	public void addRecord( final BeamMarkerRecord<?> record ) {
		synchronized( LOCK ) {
			_needsCache = true;
			_records.put( record.getBeamMarker(), record );
			_timeStamp = record.getTimestamp();
		}
	}
	
	
	/**
	 * Remove the specified Beam Marker record from the excursion.
	 * @param record the Beam Marker record to remove from the excursion
	 */
	public void removeRecord( final BeamMarkerRecord<?> record ) {
		synchronized ( LOCK ) {
			_needsCache = true;
			_records.remove( record.getBeamMarker() );
		}
	}
	
	
	/**
	 * Get the beam marker record associated with the specifed beam marker.
	 * @param marker the Beam Marker for which to fetch the Beam Marker record
	 * @return the Beam Marker record corresponding to the specified Beam Marker
	 */
	public BeamMarkerRecord<?> getRecord( final BeamMarker<?> marker ) {
		synchronized( LOCK ) {
			return super.getRecord( marker );
		}
	}
	
	
	/**
	 * Get the list of beam marker records in the excursion sorted by beam marker position.
	 * @return the list of beam marker records in the excursion
	 */
	public List<BeamMarkerRecord<?>> getRecords() {
		synchronized( LOCK ) {
			return super.getRecords();
		}
	}	
	
	
	/**
	 * Generate a description of the excursion.
	 * @return a description of the excursion
	 */
	public String toString() {
		synchronized( LOCK ) {
			return super.toString();
		}
	}
}
