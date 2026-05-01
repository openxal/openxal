/*
 *  FilterOrbitSource.java
 *
 *  Created on Wed Sep 08 15:22:24 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.smf.*;

import java.util.*;


/**
 * FilterOrbitSource
 *
 * @author   tap
 * @since    Sep 08, 2004
 */
public class FilterOrbitSource extends OrbitSource implements OrbitSourceListener {
	/** root orbit source which will be filtered */
	protected OrbitSource _source;

	/** the filter to apply to the BPM records */
	protected final BpmRecordFilter _filter;
	
	/** the latest orbit source */
	protected Orbit _orbit;


	/**
	 * Primary Constructor
	 *
	 * @param label   This orbit source's label
	 * @param source  The orbit source to filter
	 * @param filter  The BPM record filter to apply
	 */
	public FilterOrbitSource( final String label, final OrbitSource source, final BpmRecordFilter filter ) {
		super( label, source.getSequence(), source.getBpmAgents() );
		setSource( source );
		_filter = filter;
		_orbit = null;
	}


	/**
	 * Constructor
	 *
	 * @param source  The orbit source to filter
	 * @param filter  The BPM record filter to apply
	 */
	public FilterOrbitSource( final OrbitSource source, final BpmRecordFilter filter ) {
		this( "Filtered " + source.getLabel(), source, filter );
	}


	/**
	 * Set the root orbit source.
	 *
	 * @param source  The root orbit source
	 */
	public void setSource( OrbitSource source ) {
		if ( _source != null ) {
			_source.removeOrbitSourceListener( this );
		}
		
		_source = source;
		
		if ( source != null ) {
			_source.addOrbitSourceListener( this );
		}
	}


	/**
	 * Get the latest orbit.
	 *
	 * @return   the latest orbit.
	 */
	public Orbit getOrbit() {
		return _orbit;
	}
	
	
	/**
	 * Event indicating that the specified orbit source has generated a new orbit.
	 *
	 * @param source    the orbit source generating the new orbit
	 * @param newOrbit  the new orbit
	 */
	public void orbitChanged( final OrbitSource source, final Orbit newOrbit ) {
		final BpmRecordFilter filter = _filter;
		final MutableOrbit filteredOrbit = new MutableOrbit( _sequence );
		
		List<BpmRecord> records = newOrbit.getRecords();
		Iterator<BpmRecord> recordIter = records.iterator();
		while ( recordIter.hasNext() ) {
			BpmRecord record = recordIter.next();
			if ( filter.accept( record ) ) {
				filteredOrbit.addRecord( record );
			}
		}
		
		_orbit = filteredOrbit.getOrbit();
		_proxy.orbitChanged( this, _orbit );
	}


	/**
	 * Event indicating that the orbit source's sequence has changed.
	 *
	 * @param source       the orbit source generating the new orbit
	 * @param newSequence  the new sequence
	 * @param newBPMs      the new BPMs
	 */
	public void sequenceChanged( final OrbitSource source, final AcceleratorSeq newSequence, final List<BpmAgent> newBPMs ) {}
	
	
	/**
	 * Handle the event indicating that the orbit source enable state has changed.
	 * @param source the orbit source generating the event
	 * @param isEnabled the new enable state of the orbit source
	 */
	public void enableChanged( final OrbitSource source, final boolean isEnabled ) {}
}

