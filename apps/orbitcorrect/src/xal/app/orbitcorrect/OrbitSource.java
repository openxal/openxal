/*
 *  OrbitSource.java
 *
 *  Created on Tue Jun 15 10:39:24 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

//import xal.tools.Lock;
import xal.tools.messaging.MessageCenter;
import xal.smf.AcceleratorSeq;
import xal.smf.Accelerator;
import xal.smf.impl.BPM;
import xal.tools.data.*;

import java.util.*;


/**
 * OrbitSource
 * @author   tap
 * @since    Jun 15, 2004
 */
public abstract class OrbitSource implements KeyedRecord, DataListener {
	/** lable for data storage */
	final static public String DATA_LABEL = "OrbitSource";
	
	/** key indicating the trace source label */
	final static public String LABEL_KEY = "LABEL";
    
	/** lock to synchronize state modification and fetching */
    final protected Object STATE_LOCK;
	
	/** the selected accelerator sequence */
	protected AcceleratorSeq _sequence;
	
	/** the selected BPM agents */
	protected List<BpmAgent> _bpmAgents;
	
	/** Determines if the source is enabled for display */
	protected boolean _enabled;
	
	/** Label for the trace */
	protected String _label;
	
	/** message center which forwards messages to registered listeners */
	protected MessageCenter _messageCenter;
	
	/** proxy for messages to be forwarded to registered listeners */
	protected OrbitSourceListener _proxy;
	
	
	/**
	 * Primary Constructor
	 * @param label     Label for this orbit source.
	 * @param sequence  The sequence for which this orbit source supplies orbits.
	 * @param bpmAgents The BPM agents to include in the Orbit.
	 */
	public OrbitSource( final String label, final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {
        STATE_LOCK = new Object();
		
		_messageCenter = new MessageCenter( "Orbit Source" );
		_proxy = _messageCenter.registerSource( this, OrbitSourceListener.class );
		
		setLabel( label );
		_enabled = true;
		
		setup();
		
		setSequence( sequence, bpmAgents );
	}
	
	
	/**
	 * Constructor
	 * @param label  the orbit source's label
	 */
	public OrbitSource( final String label ) {
		this( label, null, null );
	}
	
	
	/** Initial setup intended for subclasses to override as necessary. This implementation does nothing. */
	protected void setup() { }
	
	
	/** dispose of this instance's resources */
	public void dispose() {
		setEnabled( false );
		_messageCenter.removeSource( this, OrbitSourceListener.class );
		_messageCenter = null;
		_proxy = null;
		_label = null;
	}
	
	
	/**
	 * Get the latest orbit.
	 * @return   the latest orbit.
	 */
	public abstract Orbit getOrbit();
	
	
	/**
	 * Add a listener of orbit source events
	 * @param listener  a listener which should receive orbit source events from this source
	 */
	public void addOrbitSourceListener( final OrbitSourceListener listener ) {
		_messageCenter.registerTarget( listener, this, OrbitSourceListener.class );
		listener.orbitChanged( this, getOrbit() );
	}
	
	
	/**
	 * Remove a listener of orbit source events
	 * @param listener  a listener which should no longer receive orbit source events from this source
	 */
	public void removeOrbitSourceListener( final OrbitSourceListener listener ) {
		_messageCenter.removeTarget( listener, this, OrbitSourceListener.class );
	}
	
	
	/**
	 * Get the label for the source
	 * @return   a string which labels the source
	 */
	public String getLabel() {
		return _label;
	}
	
	
	/**
	 * Set this orbit source's label.
	 * @param label the new label for this orbit source
	 */
	public void setLabel(final String label) {
		_label = label;
	}
	
	
	/**
	 * Get a description of this orbit source.
	 * @return   a description of this orbit source.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append( _label );
		buffer.append( ": " );
		buffer.append( _sequence );
		return buffer.toString();
	}
	
	
	/**
	 * Set the sequence and its BPMs.  Subclasses should override this method to post a <code>sequenceChanged()</code> event upon setting the sequence.
	 * @param sequence  the accelerator sequence which the orbit covers
	 * @param bpmAgents      the list of BPM Agents to monitor
	 */
	public void setSequence( final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {
        synchronized( STATE_LOCK ) {
            beforeSequenceAssignment( sequence, bpmAgents );
            
            _bpmAgents = bpmAgents;
            _sequence = sequence;
            
            afterSequenceAssignment( sequence, bpmAgents );
        }
	}
    
    
    /** Subclasses may implement this method to prepare for sequence assignment */
    protected void beforeSequenceAssignment( final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {}
    
    
    /** Subclasses may implement this method to perform action immediately after sequence assignment */
    protected void afterSequenceAssignment( final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {}
	
	
	/**
	 * Set the BPM agents to those specified
	 * @param bpmAgents the BPM agents to set
	 */
	public void setBpmAgents( final List<BpmAgent> bpmAgents ) {
        synchronized( STATE_LOCK ) {
            _bpmAgents = bpmAgents;
        }
	}
	
	
	/**
	 * Get a copy of this orbit source's BPM agents.
	 * @return   this orbit source's BPM agents
	 */
	public List<BpmAgent> getBpmAgents() {
        synchronized( STATE_LOCK ) {
            return _bpmAgents != null ? new ArrayList<BpmAgent>( _bpmAgents ) : Collections.<BpmAgent>emptyList();
        }
	}
	
	
	/**
	 * Get this source's accelerator sequence.
	 * @return   this source's accelerator sequence
	 */
	public AcceleratorSeq getSequence() {
        synchronized ( STATE_LOCK ) {
            return _sequence;
        }
	}
	
	
	/**
	 * Get this source's accelerator.
	 * @return   this source's accelerator
	 */
	public Accelerator getAccelerator() {
        synchronized ( STATE_LOCK ) {
            return ( _sequence != null ) ? _sequence.getAccelerator() : null;
        }
	}
	
	
	/**
	 * Determine if the source is enabled for display
	 * @return   true if the source is enabled for display and false if not
	 */
	public boolean isEnabled() {
		return _enabled;
	}
	
	
	/**
	 * Set whether the source is enabled for display
	 * @param enable  true to enable the source for display and false to disable it
	 */
	public void setEnabled( boolean enable ) {
		_enabled = enable;
		_proxy.enableChanged( this, enable );
	}
	
	
	/** Set whether to use the beam event to trigger orbit snapshots */
	public void setUseBeamEventTrigger( final boolean useTrigger ) {}
	
	
	/**
	 * Get the value associated with the specified key.
	 * @param key The key for which to get the associated value.
	 * @return The value as an Object.
	 */
    public Object valueForKey( final String key ) {
		if ( key.equals( LABEL_KEY ) ) {
			return getLabel();
		}
		else {
			return null;
		}
	}
	
	
    /** 
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    public String dataLabel() {
		return DATA_LABEL;
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		setLabel( adaptor.stringValue( LABEL_KEY ) );
		setEnabled( adaptor.booleanValue( "enabled" ) );
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( LABEL_KEY, _label );
		adaptor.setValue( "enabled", _enabled );
	}
}

