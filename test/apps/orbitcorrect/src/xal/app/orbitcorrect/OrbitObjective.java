/*
 *  OrbitObjective.java
 *
 *  Created on Fri Oct 01 08:31:41 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.tools.messaging.MessageCenter;
import xal.extension.solver.*;


/**
 * OrbitObjective
 * @author   tap
 * @since    Oct 01, 2004
 */
public abstract class OrbitObjective extends Objective {
	/** the message center for dispatching events */
	final protected MessageCenter MESSAGE_CENTER;
	
	/** proxy which forwards events to listeners */
	final protected OrbitObjectiveListener EVENT_PROXY;
	
	/** indicates whether this objective is enabled */
	protected boolean _isEnabled;
	
	
	/**
	 * Primary constructor
	 * @param label  the name of the objective
	 * @param enable true to enable the objective and false to disable it
	 */
	public OrbitObjective( final String label, final boolean enable ) {
		super( label );
		
		MESSAGE_CENTER = new MessageCenter( "Orbit Objective" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, OrbitObjectiveListener.class );
		
		_isEnabled = enable;
	}
	
	
	/**
	 * Constructor
	 * @param label  the name of the objective
	 */
	public OrbitObjective( final String label ) {
		this( label, true );
	}
	
	
	/**
	 * Add the specified listener as a reciever of orbit objective events from this instance.
	 * @param listener the listener to register as a receiver of events
	 */
	public void addOrbitObjectiveListener( final OrbitObjectiveListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, OrbitObjectiveListener.class );
	}
	
	
	/**
	 * Remove the specified listener from receiving orbit objective events from this instance.
	 * @param listener the listener to unregister from receiving events
	 */
	public void removeOrbitObjectiveListener( final OrbitObjectiveListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, OrbitObjectiveListener.class );
	}
	
	
	/**
	 * Determine if this objective is enabled.
	 * @return true if this objective is enabled and false if not.
	 */
	public boolean isEnabled() {
		return _isEnabled;
	}
	
	
	/** synonym for isEnabled */
	public boolean getEnabled() {
		return this.isEnabled();
	}
	
	
	/**
	 * Set whether this objective is enabled.
	 * @param enable true to enable this objective and false to disable it
	 */
	public void setEnabled( final boolean enable ) {
		_isEnabled = enable;
		EVENT_PROXY.enableChanged( this, enable );
	}


	/**
	 * Calculate this objective's score for the specified orbit and corrector distribution.
	 * @param orbit         the orbit to score
	 * @param distribution  the corrector distribution used for the orbit
	 * @return              this objective's score
	 */
	public abstract double score( final Orbit orbit, final CorrectorDistribution distribution );
}

