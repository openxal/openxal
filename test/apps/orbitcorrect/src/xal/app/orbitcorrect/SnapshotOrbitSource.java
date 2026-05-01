/*
 *  SnapshotOrbitSource.java
 *
 *  Created on Mon Aug 23 14:48:08 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.smf.*;
import xal.smf.impl.BPM;
import xal.tools.data.*;

import java.util.*;


/**
 * SnapshotOrbitSource
 * @author   tap
 * @since    Aug 23, 2004
 */
public class SnapshotOrbitSource extends OrbitSource {
	/** the Snapshot BPM record data */
	protected List<BpmRecord> _bpmRecords;
	
	/** orbit */
	protected Orbit _orbit;
	
	
	/**
	 * Primary Constructor
	 * @param label       Label for this orbit source.
	 * @param bpmRecords  The BPM records which define the orbit
	 * @param sequence    The sequence for which this orbit source supplies orbits.
	 * @param bpmAgents   The BPM agents to include in the Orbit.
	 */
	public SnapshotOrbitSource( final String label, final List<BpmRecord> bpmRecords, final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {
		super( label, sequence, bpmAgents );
		
		setSnapshot( bpmRecords );
	}
	
	
	/**
	 * Constructor
	 * @param label       the orbit source's label
	 * @param bpmRecords  the records from which to generate the orbit
	 */
	public SnapshotOrbitSource( final String label, final List<BpmRecord> bpmRecords ) {
		this( label, bpmRecords, null, null );
	}
	
	
	/**
	 * Constructor
	 * @param label  the orbit source's label
	 * @param orbit  the orbit from which to get the BPM records of the Snapshot orbit
	 */
	public SnapshotOrbitSource( final String label, final Orbit orbit ) {
		this( label, orbit.getRecords() );
	}
	
	
	/**
	 * Constructor
	 * @param label  the orbit source's label
	 */
	public SnapshotOrbitSource( final String label ) {
		this( label, Collections.<BpmRecord>emptyList() );
	}
    
    
    /** Create a new instance from a data adaptor */
    static public SnapshotOrbitSource getInstance( final DataAdaptor snapshotSourceAdaptor, final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {
        final SnapshotOrbitSource orbitSource = new SnapshotOrbitSource( "" );
        orbitSource.setSequence( sequence, bpmAgents );
        orbitSource.update( snapshotSourceAdaptor );
        return orbitSource;
    }
	
	
	/**
	 * Generate a snapshot orbit from the list of BPMs and initialized to zero displacement.
	 * @param label the orbit source's label
	 * @param bpmAgents	the bpms for which the orbit is specified
	 */
	static public SnapshotOrbitSource getInstanceWithBPMs( final String label, final List<BpmAgent> bpmAgents, final AcceleratorSeq sequence ) {
		final SnapshotOrbitSource source = new SnapshotOrbitSource( label );
		
		final List<BpmRecord> records = new ArrayList<BpmRecord>( bpmAgents.size() );
		for ( BpmAgent bpmAgent : bpmAgents ) {
			records.add( new BpmRecord( bpmAgent ) );
		}
		
		return new SnapshotOrbitSource( label, records, sequence, bpmAgents );
	}
	
	
	/**
	 * Set the sequence and its BPMs.
	 * @param sequence  the accelerator sequence which the orbit covers
	 * @param bpmAgents the list of BPM agents to monitor
	 */
	public void setSequence( final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {
        super.setSequence( sequence, bpmAgents );
        
        // perform the notification outside the state synchronization
        _proxy.sequenceChanged( this, sequence, bpmAgents );
	}
    
    
    /** Subclasses may implement this method to perform action immediately after sequence assignment */
    protected void afterSequenceAssignment( final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {
        generateOrbit();
    }

	
	/**
	 * Set the orbit snapshot data.
	 * @param bpmRecords   the BPM records of the snapshot orbit
	 */
	public void setSnapshot( final List<BpmRecord> bpmRecords ) {
		_bpmRecords = new ArrayList<BpmRecord>( bpmRecords );
		generateOrbit();
	}
	
	
	/**
	 * Set the orbit snapshot data.
	 * @param orbit   the orbit to snapshot
	 */
	public void setSnapshot( final Orbit orbit ) {
		setSnapshot( orbit.getRecords() );
	}
    
    
	/**
	 * Get the latest orbit.
	 * @return   the latest orbit.
	 */
	public Orbit getOrbit() {
        synchronized ( STATE_LOCK ) {
            return _orbit;
        }
	}
    
	
	/** Generate a new orbit from the BPM records for the specified sequence.  */
	protected void generateOrbit() {
		final MutableOrbit orbit = new MutableOrbit( _sequence );
		
		if ( ( _sequence != null ) && ( _bpmAgents != null ) && ( _bpmRecords != null ) ) {
			// since BPM agents may be different we need to get the records which match the BPMs
			final Map<BPM,BpmAgent> bpmAgentTable = new HashMap<BPM,BpmAgent>( _bpmAgents.size() );
			for ( final BpmAgent bpmAgent : _bpmAgents ) {
				bpmAgentTable.put( bpmAgent.getBPM(), bpmAgent );
			}
			
			for ( final BpmRecord record : _bpmRecords ) {
				final BpmAgent bpmAgent = bpmAgentTable.get( record.getBpmAgent().getBPM() );
				if ( bpmAgent != null ) {
					orbit.addRecord( record );
				}
			}
		}
		
		_orbit = orbit.getOrbit();
		_proxy.orbitChanged( this, _orbit );
	}
	
	
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		super.update( adaptor );
		
		final Map<String,BpmAgent> bpmAgents = new HashMap<String,BpmAgent>( _bpmAgents.size() );
		for ( BpmAgent bpmAgent : _bpmAgents ) {
			bpmAgents.put( bpmAgent.getID(), bpmAgent );
		}
		
		final List<DataAdaptor> recordAdaptors = adaptor.childAdaptors( BpmRecord.DATA_LABEL );
		final List<BpmRecord> records = new ArrayList<BpmRecord>( recordAdaptors.size() );
		for ( DataAdaptor recordAdaptor : recordAdaptors ) {
			final BpmAgent bpmAgent = bpmAgents.get( recordAdaptor.stringValue( "BPM" ) );
			if ( bpmAgent != null ) {
				final BpmRecord record = new BpmRecord( bpmAgent );
				record.update( recordAdaptor );
				records.add( record );
			}
		}
		setSnapshot( records );
	}
	
	
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "type", "snapshot" );
		super.write( adaptor );
		adaptor.writeNodes( _bpmRecords );
	}
}

