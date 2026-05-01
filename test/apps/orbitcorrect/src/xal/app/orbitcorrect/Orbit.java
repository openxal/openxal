/*
 *  Orbit.java
 *
 *  Created on Thu Jun 17 11:14:23 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import java.util.*;

import xal.smf.*;
import xal.smf.impl.BPM;

/**
 * Orbit
 * @author     tap
 * @since      Jun 17, 2004
 */
public class Orbit {
	/** the index of the X Plane portion of the orbit */
	final static public int X_PLANE = 0;
	
	/** the index of the Y Plane portion of the orbit */
	final static public int Y_PLANE = 1;
	
	/** time indicating when the orbit was generated (may be wall clock time for some orbits) */
	protected Date _timeStamp;
	
	/**  BPM Records keyed by BPM agent and sorted by BPM position in an accelerator sequence */
	protected SortedMap<BpmAgent, BpmRecord> _bpmRecords;
	
	/**  the accelerator sequence over which the orbit is taken */
	final protected AcceleratorSeq _sequence;
	
	/** cache of positions */
	protected double[] _positions;
	
	/** X and Y Orbit planes */
	protected OrbitPlane[] _orbitPlanes;
	
	/** cache of amplitude-avg values */
	protected double[] _ampAvgValues;
	
	/** indicates whether the orbit data needs to be cached */
	protected boolean _needsCache;
	
	/** lock for synchronizing data access */
	final protected Object _lock;
	
	
	/**
	 * Primary constructor
	 * @param sequence    the accelerator sequence over which the orbit is taken
	 * @param bpmRecords  BPM Records keyed by BPM and sorted by BPM position in this orbit's sequence
	 * @param timeStamp   the time stamp of this orbit
	 */
	protected Orbit( final AcceleratorSeq sequence, final SortedMap<BpmAgent, BpmRecord> bpmRecords, final Date timeStamp ) {
		_lock = new Object();
		_needsCache = true;
		
		_timeStamp = timeStamp;
		
		_sequence = sequence;
		_timeStamp = new Date();
		
		// initialize the cached values to null
		_positions = null;
		_orbitPlanes = new OrbitPlane[2];
		
		_bpmRecords = ( bpmRecords == null ) ? new TreeMap<BpmAgent, BpmRecord>( new BPMComparator( _sequence ) ) : new TreeMap<BpmAgent, BpmRecord>( bpmRecords );
	}
	
	
	/**
	 * Constructor using the current wall clock time as the time stamp
	 * @param  sequence    the accelerator sequence over which the orbit is taken
	 * @param  bpmRecords  BPM Records keyed by BPM and sorted by BPM position in this orbit's sequence 
	 */
	protected Orbit( final AcceleratorSeq sequence, final SortedMap<BpmAgent, BpmRecord> bpmRecords ) {
		this( sequence, bpmRecords, new Date() );
	}
	
	
	/**
	 * Constructor with an empty set of bpmRecords
	 * @param  sequence  the accelerator sequence over which the orbit is taken
	 */
	public Orbit( AcceleratorSeq sequence ) {
		this( sequence, null );
	}
	
	
	/**
	 * Copy constructor
	 * @param  orbit  Description of the Parameter
	 */
	public Orbit( Orbit orbit ) {
		this( orbit._sequence, orbit._bpmRecords );
	}
	
	
	/**
	 * Calculate the difference between a primary orbit and a reference orbit.
	 * @param primaryOrbit    The primary orbit
	 * @param referenceOrbit  The reference orbit
	 * @return                The difference between the primary orbit and reference orbit
	 */
	static public Orbit calcDifference( final Orbit primaryOrbit, final Orbit referenceOrbit ) {
		if ( primaryOrbit == null || referenceOrbit == null ) {
			return null;
		}
		
		final MutableOrbit differenceOrbit = new MutableOrbit( primaryOrbit.getSequence() );
		final List<BpmRecord> primaryRecords = primaryOrbit.getRecords();
		final Iterator<BpmRecord> primaryRecordIter = primaryRecords.iterator();
		while ( primaryRecordIter.hasNext() ) {
			final BpmRecord primaryRecord = primaryRecordIter.next();
			final BpmAgent bpmAgent = primaryRecord.getBpmAgent();
			final BpmRecord referenceRecord = referenceOrbit.getRecord( bpmAgent );
			
			if ( referenceRecord == null ) {
				continue;
			}
			
			final BpmRecord diffRecord = BpmRecord.calcDifference( primaryRecord, referenceRecord );
			differenceOrbit.addRecord( diffRecord );
		}
		
		return differenceOrbit.getOrbit();
	}
	
	
	/**
	 * Get the time stamp of this orbit.
	 * @return the time stamp of this orbit
	 */
	public Date getTimeStamp() {
		return _timeStamp;
	}
	
	
	/**
	 * Get the Orbit's sequence
	 * @return    the Orbit's sequence
	 */
	public AcceleratorSeq getSequence() {
		return _sequence;
	}
	
	
	/**
	 * Get the BPM record associated with the specifed BPM.
	 * @param  bpmAgent  the BPM agent for which to fetch the BPM record
	 * @return the BPM record corresponding to the specified BPM
	 */
	public BpmRecord getRecord( BpmAgent bpmAgent ) {
		return _bpmRecords.get( bpmAgent );
	}
	
	
	/**
	 * Get the list of BPM records in the orbit sorted by BPM position.
	 * @return    the list of BPM records in the orbit
	 */
	public List<BpmRecord> getRecords() {
		return new ArrayList<BpmRecord>( _bpmRecords.values() );
	}
	
	
	/** Cache the position, xAvg, yAvg and ampAvg data for efficient access. */
	protected void cacheData() {
		synchronized ( _lock ) {
			if ( _needsCache ) {
				List<BpmRecord> records = getRecords();
				int count = records.size();
				
				_positions = new double[count];
				final double[] xAvgValues = new double[count];
				final double[] yAvgValues = new double[count];
				_ampAvgValues = new double[count];
				
				for ( int index = 0; index < count; index++ ) {
					BpmRecord record = records.get( index );
					_positions[index] = record.getPositionIn( _sequence );
					xAvgValues[index] = record.getXAvg();
					yAvgValues[index] = record.getYAvg();
					_ampAvgValues[index] = record.getAmpAvg();
				}
				
				_orbitPlanes[X_PLANE] = new OrbitPlane( "X Orbit", _positions, xAvgValues );
				_orbitPlanes[Y_PLANE] = new OrbitPlane( "Y Orbit", _positions, yAvgValues );
				
				_needsCache = false;
			}
		}
	}
	
	
	/**
	 * Get the number of BPM records.
	 * @return the number of BPM records
	 */
	public int getRecordCount() {
		synchronized( _lock ) {
			return _bpmRecords.size();
		}
	}
	
	
	/**
	 * Get the array of BPM positions from the BPM records
	 * @return    the array of BPM positions from the BPM records
	 */
	public double[] getPositions() {
		synchronized ( _lock ) {
			if ( _needsCache ) {
				cacheData();
			}
			
			return _positions;
		}
	}
	
	
	/**
	 * Get the array of xAvg displacements.
	 * @return    this orbit's array of xAvg values
	 */
	public double[] getXAvgDisplacements() {
		synchronized ( _lock ) {
			if ( _needsCache ) {
				cacheData();
			}
			
			return _orbitPlanes[X_PLANE].getAvgDisplacements();
		}
	}
	
	
	/**
	 * Get the array of yAvg displacements.
	 * @return    this orbit's array of yAvg values
	 */
	public double[] getYAvgDisplacements() {
		synchronized ( _lock ) {
			if ( _needsCache ) {
				cacheData();
			}
			
			return _orbitPlanes[Y_PLANE].getAvgDisplacements();
		}
	}
	
	
	/**
	 * Get the array of ampAvg values.
	 * @return    this orbit's array of ampAvg values
	 */
	public double[] getAmpAvgValues() {
		synchronized ( _lock ) {
			if ( _needsCache ) {
				cacheData();
			}
			
			return _ampAvgValues;
		}
	}
	
	
	/**
	 * Get the orbit plane corresponding to the specified plane index.
	 * @param planeIndex either X_PLANE or Y_PLANE to indicate the desired plane
	 * @return the orbit plane corresponding to the desired plane index
	 */
	final public OrbitPlane getOrbitPlane( final int planeIndex ) {
		if ( _needsCache ) {
			cacheData();
		}
		
		return _orbitPlanes[planeIndex];
	}
	
	
	/**
	 * Get the orbit for the X plane.
	 * @return the orbit for the X plane
	 */
	public OrbitPlane getXOrbitPlane() {
		return getOrbitPlane( X_PLANE );
	}
	
	
	/**
	 * Get the orbit for the Y plane.
	 * @return the orbit for the Y plane
	 */
	public OrbitPlane getYOrbitPlane() {
		return getOrbitPlane( Y_PLANE );
	}
	
	
	/**
	 * Generate a description of the orbit.
	 * @return    a description of the orbit
	 */
	public String toString() {
		return _bpmRecords.values().toString();
	}
}

