//
//  BeamExcursion.java
//  xal
//
//  Created by Tom Pelaia on 1/4/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import java.util.*;

import xal.smf.*;


/** collection of beam displacements at beam markers */
public class BeamExcursion {
	/** the index of the X Plane portion of the excursion */
	final static public int X_PLANE = 0;
	
	/** the index of the Y Plane portion of the excursion */
	final static public int Y_PLANE = 1;
	
	/** time indicating when the excursion was generated (may be wall clock time for some excursions) */
	protected Date _timeStamp;
	
	/**  Beam marker records keyed by beam marker and sorted by beam marker position within an accelerator sequence */
	protected SortedMap<BeamMarker<?>, BeamMarkerRecord<?>> _records;
	
	/**  the accelerator sequence over which the excursion is taken */
	final protected AcceleratorSeq _sequence;

	/** cache of positions */
	protected double[] _positions;
	
	/** X and Y Orbit planes */
	protected OrbitPlane[] _excursionPlanes;

	/** cache of amplitude-avg values */
	protected double[] _ampAvgValues;
	
	/** indicates whether the excursion data needs to be cached */
	protected boolean _needsCache;

	/** lock for synchronizing data access */
	final protected Object LOCK;
	
	
	/**
	 * Primary constructor
	 * @param sequence    the accelerator sequence over which the excursion is taken
	 * @param records  beam marker Records keyed by beam marker and sorted by beam marker position in this excursion's sequence
	 * @param timeStamp   the time stamp of this excursion
	 */
	protected BeamExcursion( final AcceleratorSeq sequence, final SortedMap<BeamMarker<?>, BeamMarkerRecord<?>> records, final Date timeStamp ) {
		LOCK = new Object();
		_needsCache = true;
		
		_timeStamp = timeStamp;
		
		_sequence = sequence;
		_timeStamp = new Date();

		// initialize the cached values to null
		_positions = null;
		_excursionPlanes = new OrbitPlane[2];

		_records = ( records == null ) ? new TreeMap<BeamMarker<?>, BeamMarkerRecord<?>>( BeamMarker.getPositionComparator( sequence ) ) : new TreeMap<BeamMarker<?>, BeamMarkerRecord<?>>( records );
	}
	
	
	/**
	 * Constructor using the current wall clock time as the time stamp
	 * @param  sequence    the accelerator sequence over which the excursion is taken
	 * @param  records  beam marker records keyed by beam marker and sorted by beam marker position in this excursion's sequence 
	 */
	protected BeamExcursion( final AcceleratorSeq sequence, final SortedMap<BeamMarker<?>, BeamMarkerRecord<?>> records ) {
		this( sequence, records, new Date() );
	}
	

	/**
	 * Constructor with an empty set of records
	 * @param  sequence  the accelerator sequence over which the excursion is taken
	 */
	public BeamExcursion( AcceleratorSeq sequence ) {
		this( sequence, null );
	}


	/**
	 * Copy constructor
	 * @param  excursion  Description of the Parameter
	 */
	public BeamExcursion( BeamExcursion excursion ) {
		this( excursion._sequence, excursion._records );
	}
	
	
	/**
	 * Calculate the difference between a primary excursion and a reference excursion.
	 * @param primaryExcursion    The primary excursion
	 * @param referenceExcursion  The reference excursion
	 * @return                The difference between the primary excursion and reference excursion
	 */
    @SuppressWarnings("unchecked") //calcDifference could not be called when using <?> on BeamMarkerRecord
	static public BeamExcursion calcDifference( final BeamExcursion primaryExcursion, final BeamExcursion referenceExcursion ) {
		if ( primaryExcursion == null || referenceExcursion == null ) {
			return null;
		}
		
		final MutableBeamExcursion differenceExcursion = new MutableBeamExcursion( primaryExcursion.getSequence() );
		final List<BeamMarkerRecord<?>> primaryRecords = primaryExcursion.getRecords();
        for ( final BeamMarkerRecord<?> primaryRecord : primaryRecords ) {
			final BeamMarker<AcceleratorNode> marker = (BeamMarker<AcceleratorNode>)primaryRecord.getBeamMarker();
			final BeamMarkerRecord<AcceleratorNode> referenceRecord = (BeamMarkerRecord<AcceleratorNode>)referenceExcursion.getRecord( marker );
			
			if ( referenceRecord == null ) {
				continue;
			}
			
			final BeamMarkerRecord<AcceleratorNode> diffRecord = BeamMarkerRecord.calcDifference( (BeamMarkerRecord<AcceleratorNode>)primaryRecord, referenceRecord );
			differenceExcursion.addRecord( diffRecord );
		}
		
		return differenceExcursion.getBeamExcursion();
	}
	
	
	/**
	 * Get the time stamp of this excursion.
	 * @return the time stamp of this excursion
	 */
	public Date getTimeStamp() {
		return _timeStamp;
	}
	

	/**
	 * Get the Excursion's sequence
	 * @return    the Excursion's sequence
	 */
	public AcceleratorSeq getSequence() {
		return _sequence;
	}


	/**
	 * Get the beam marker record associated with the specifed beam marker.
	 * @param  beamMarker  the beam marker for which to fetch the record
	 * @return the record corresponding to the specified beam marker
	 */
	public BeamMarkerRecord<?> getRecord( BeamMarker<?> beamMarker ) {
		return _records.get( beamMarker );
	}


	/**
	 * Get the list of beam marker records in the excursion sorted by beam marker position.
	 * @return    the list of beam marker records in the excursion
	 */
	public List<BeamMarkerRecord<?>> getRecords() {
		return new ArrayList<BeamMarkerRecord<?>>( _records.values() );
	}


	/** Cache the position, xAvg, yAvg and ampAvg data for efficient access. */
	protected void cacheData() {
		synchronized ( LOCK ) {
			if ( _needsCache ) {
				List<BeamMarkerRecord<?>> records = getRecords();
				int count = records.size();

				_positions = new double[count];
				final double[] xAvgValues = new double[count];
				final double[] yAvgValues = new double[count];
				_ampAvgValues = new double[count];

				for ( int index = 0; index < count; index++ ) {
					final BeamMarkerRecord<?> record = records.get( index );
					_positions[index] = record.getPositionIn( _sequence );
					xAvgValues[index] = record.getXAvg();
					yAvgValues[index] = record.getYAvg();
					_ampAvgValues[index] = record.getAmpAvg();
				}
				
				_excursionPlanes[X_PLANE] = new OrbitPlane( "X Orbit", _positions, xAvgValues );
				_excursionPlanes[Y_PLANE] = new OrbitPlane( "Y Orbit", _positions, yAvgValues );
				
				_needsCache = false;
			}
		}
	}
	
	
	/**
	 * Get the number of beam marker records.
	 * @return the number of beam marker records
	 */
	public int getRecordCount() {
		synchronized( LOCK ) {
			return _records.size();
		}
	}


	/**
	 * Get the array of beam marker positions from the beam marker records
	 * @return    the array of beam marker positions from the beam marker records
	 */
	public double[] getPositions() {
		synchronized ( LOCK ) {
			if ( _needsCache ) {
				cacheData();
			}

			return _positions;
		}
	}


	/**
	 * Get the array of xAvg displacements.
	 * @return    this excursion's array of xAvg values
	 */
	public double[] getXAvgDisplacements() {
		synchronized ( LOCK ) {
			if ( _needsCache ) {
				cacheData();
			}

			return _excursionPlanes[X_PLANE].getAvgDisplacements();
		}
	}


	/**
	 * Get the array of yAvg displacements.
	 * @return    this excursion's array of yAvg values
	 */
	public double[] getYAvgDisplacements() {
		synchronized ( LOCK ) {
			if ( _needsCache ) {
				cacheData();
			}

			return _excursionPlanes[ Y_PLANE ].getAvgDisplacements();
		}
	}


	/**
	 * Get the array of ampAvg values.
	 * @return    this excursion's array of ampAvg values
	 */
	public double[] getAmpAvgValues() {
		synchronized ( LOCK ) {
			if ( _needsCache ) {
				cacheData();
			}

			return _ampAvgValues;
		}
	}
	
	
	/**
	 * Get the excursion plane corresponding to the specified plane index.
	 * @param planeIndex either X_PLANE or Y_PLANE to indicate the desired plane
	 * @return the excursion plane corresponding to the desired plane index
	 */
	final public OrbitPlane getOrbitPlane( final int planeIndex ) {
		if ( _needsCache ) {
			cacheData();
		}
		
		return _excursionPlanes[planeIndex];
	}
	
	
	/**
	 * Get the excursion for the X plane.
	 * @return the excursion for the X plane
	 */
	public OrbitPlane getXOrbitPlane() {
		return getOrbitPlane( X_PLANE );
	}
	
	
	/**
	 * Get the excursion for the Y plane.
	 * @return the excursion for the Y plane
	 */
	public OrbitPlane getYOrbitPlane() {
		return getOrbitPlane( Y_PLANE );
	}


	/**
	 * Generate a description of the excursion.
	 * @return    a description of the excursion
	 */
	public String toString() {
		return _records.values().toString();
	}
}
