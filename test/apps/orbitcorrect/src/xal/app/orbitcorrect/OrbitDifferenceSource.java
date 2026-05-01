/*
 *  OrbitDifferenceSource.java
 *
 *  Created on Fri Aug 13 10:10:53 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

//import xal.tools.Lock;
import xal.smf.*;
import xal.smf.impl.BPM;

import java.util.*;


/**
 * OrbitDifferenceSource
 *
 * @author   tap
 * @since    Aug 13, 2004
 */
public class OrbitDifferenceSource extends OrbitSource implements OrbitSourceListener {
	/** The primary orbit source */
	protected OrbitSource _primarySource;

	/** The reference orbit source */
	protected OrbitSource _referenceSource;

	/** the latest orbit */
	protected Orbit _orbit;


	/**
	 * Primary Constructor
	 * @param label            Label for this orbit source.
	 * @param sequence         The sequence for which this orbit source supplies orbits.
	 * @param bpmAgents        The BPM agents to include in the Orbit.
	 * @param primarySource    Primary orbit source.
	 * @param referenceSource  Reference orbit source.
	 */
	public OrbitDifferenceSource( final String label, final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents, final OrbitSource primarySource, final OrbitSource referenceSource ) {
		super( label, sequence, bpmAgents );

		_orbit = null;

		setPrimarySource( primarySource );
		setReferenceSource( referenceSource );
	}


	/**
	 * Constructor
	 * @param label     Label for this orbit source.
	 * @param sequence  The sequence for which this orbit source supplies orbits.
	 * @param bpmAgents The BPM agents to include in the Orbit.
	 */
	public OrbitDifferenceSource( final String label, final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {
		this( label, sequence, bpmAgents, null, null );
	}


	/**
	 * Constructor
	 * @param label  Label for this orbit source.
	 */
	public OrbitDifferenceSource( final String label ) {
		this( label, null, new ArrayList<BpmAgent>() );
	}


	/**
	 * Set the sequence and its BPMs.
	 * @param sequence  the accelerator sequence which the orbit covers
	 * @param bpmAgents      the list of BPM agents to monitor
	 */
	public void setSequence( final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {
        super.setSequence( sequence, bpmAgents );
        
        // perform this outside of the state lock
        _proxy.sequenceChanged( this, sequence, bpmAgents );                
	}
    
    
    /** Subclasses may implement this method to perform action immediately after sequence assignment */
    protected void afterSequenceAssignment( final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {
        if ( _primarySource != null ) {
            _primarySource.setSequence( sequence, bpmAgents );
        }
        
        if ( _referenceSource != null ) {
            _referenceSource.setSequence( sequence, bpmAgents );
        }
    }


	/**
	 * Set the new primary source.
	 * @param primarySource  The new primarySource value
	 * @since                Aug 13, 2004
	 */
	public void setPrimarySource( final OrbitSource primarySource ) {
        synchronized ( STATE_LOCK ) {
            _orbit = null;
            
            if ( _primarySource != null ) {
                _primarySource.removeOrbitSourceListener( OrbitDifferenceSource.this );
            }
            
            _primarySource = primarySource;
            
            if ( primarySource != null ) {
                primarySource.setSequence( getSequence(), getBpmAgents() );
                primarySource.addOrbitSourceListener( OrbitDifferenceSource.this );
            }
        }
	}


	/**
	 * Set the new reference orbit source.
	 * @param referenceSource  The new referenceSource value
	 * @since                  Aug 13, 2004
	 */
	public void setReferenceSource( final OrbitSource referenceSource ) {
        synchronized ( STATE_LOCK ) {
            _orbit = null;
            
            if ( _referenceSource != null ) {
                _referenceSource.removeOrbitSourceListener( OrbitDifferenceSource.this );
            }
            
            _referenceSource = referenceSource;
            
            if ( referenceSource != null ) {
                referenceSource.setSequence( getSequence(), getBpmAgents() );
                referenceSource.addOrbitSourceListener( OrbitDifferenceSource.this );
            }
        }        
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


	/**
	 * Recalculate the source's orbit from the primary and reference orbits.
	 * @since   Aug 13, 2004
	 */
	protected void updateOrbit() {
        synchronized ( STATE_LOCK ) {
            if ( _primarySource != null && _referenceSource != null ) {
                Orbit primaryOrbit = _primarySource.getOrbit();
                Orbit referenceOrbit = _referenceSource.getOrbit();
                if ( primaryOrbit != null && referenceOrbit != null ) {
                    _orbit = Orbit.calcDifference( primaryOrbit, referenceOrbit );
                }
            }
        }
        
        _proxy.orbitChanged( this, _orbit );
	}


	/**
	 * Event indicating that the specified orbit source has generated a new orbit.
	 * @param source    the orbit source generating the new orbit
	 * @param newOrbit  the new orbit
	 */
	public void orbitChanged( final OrbitSource source, final Orbit newOrbit ) {
		updateOrbit();
	}


	/**
	 * Event indicating that the orbit source's sequence has changed.
	 * @param source       the orbit source generating the new orbit
	 * @param newSequence  the new sequence
	 * @param newBPMAgents the new BPM agents
	 */
	public void sequenceChanged( final OrbitSource source, final AcceleratorSeq newSequence, final List<BpmAgent> newBPMAgents ) {}
	
	
	/**
	 * Handle the event indicating that the orbit source enable state has changed.
	 * @param source the orbit source generating the event
	 * @param isEnabled the new enable state of the orbit source
	 */
	public void enableChanged( final OrbitSource source, final boolean isEnabled ) {}
}

