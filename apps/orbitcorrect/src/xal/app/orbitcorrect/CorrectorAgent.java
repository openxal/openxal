/*
 *  CorrectorAgent.java
 *
 *  Created on Wed Jan 07 16:54:59 EST 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.smf.impl.*;
import xal.smf.*;
import xal.ca.*;
import xal.tools.messaging.MessageCenter;


/**
 * CorrectorAgent manages a corrector.
 * @author   tap
 * @since    Jan 7, 2004
 */
public class CorrectorAgent {
	/** corrector for which this is the agent */
	protected final Dipole _corrector;

	/** event message center */
	protected MessageCenter _messageCenter;

	/** proxy for posting channel events */
	protected CorrectorEventListener _eventProxy;
	
	/** indicates whether this corrector is enabled */
	protected boolean _isEnabled;
	
	
	/**
	 * CorrectorAgent constructor
	 * @param corrector  The horizontal corrector to manage.
	 */
	public CorrectorAgent( Dipole corrector ) {
		_corrector = corrector;
		
		_messageCenter = new MessageCenter();
		_eventProxy = _messageCenter.registerSource( this, CorrectorEventListener.class );
	}


	/**
	 * Add the specified listener as a receiver of corrector events from this agent.
	 * @param listener  The listener to receive corrector events.
	 */
	public void addCorrectorEventListener( final CorrectorEventListener listener ) {
		_messageCenter.registerTarget( listener, this, CorrectorEventListener.class );
	}


	/**
	 * Remove the specified listener from receiving corrector events from this agent.
	 * @param listener  The listener to be removed from receiving corrector events.
	 */
	public void removeCorrectorEventListener( final CorrectorEventListener listener ) {
		_messageCenter.removeTarget( listener, this, CorrectorEventListener.class );
	}


	/**
	 * Get the corrector managed by this agent.
	 * @return   This agent's managed corrector.
	 */
	public Dipole getCorrector() {
		return _corrector;
	}
	
	
	/**
	 * Get the unique corrector ID.
	 * @return the corrector's unique ID
	 */
	public String getID() {
		return _corrector.getId();
	}
	
	
	/** Determine if this corrector is enabled */
	public boolean isEnabled() {
		return _isEnabled;
	}
	
	
	/** Enable/disable this corrector */
	public void setEnabled( final boolean enable ) {
		_isEnabled = enable;
		_eventProxy.enableChanged( this, enable );
	}


	/**
	 * Determine if this corrector is valid and has a good status.
	 * @return   true if this corrector has a good status and is valid; false otherwise.
	 */
	public boolean isAvailable() {
		return _corrector.getStatus() && _corrector.getValid();
	}
	
	
	/** Determine if the dipole is a corrector dipole */
	public boolean isCorrector() {
		return _corrector.isCorrector();
	}
	
	
	/** Determine if the corrector is horizontal */
	public boolean isHorizontal() {
		return _corrector.getOrientation() == Magnet.HORIZONTAL;
	}
	
	
	/** Determine if the corrector is vertical */
	public boolean isVertical() {
		return _corrector.getOrientation() == Magnet.VERTICAL;
	}
	
	
	/**
	 * Get the position of the corrector relative to the start of the specified sequence.
	 * @param sequence  The sequence relative to which the corrector's position is measured
	 * @return          the position of the corrector relative to the sequence in meters
	 */
	public double getPositionIn( AcceleratorSeq sequence ) {
		return sequence.getPosition( _corrector );
	}


	/**
	 * Get the string representation of the corrector.
	 * @return   the corrector's string representation
	 */
	public String toString() {
		return _corrector.toString();
	}
}

