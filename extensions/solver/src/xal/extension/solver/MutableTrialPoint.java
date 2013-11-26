/*
 *  MutableTrialPoint.java
 *
 *  Created on Mon Sep 20 09:40:41 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import java.util.*;


/**
 * MutableTrialPoint
 *
 * @author   tap
 * @since    Sep 20, 2004
 */
public class MutableTrialPoint extends TrialPoint {
	/**
	 * Primary Constructor.
	 *
	 * @param values  The new values used to map trial points
	 */
	public MutableTrialPoint( final Map<Variable,Number> values ) {
		super( values );
	}


	/**
	 * Copy constructor.
	 *
	 * @param trialPoint  the trial point to copy
	 */
	public MutableTrialPoint( final TrialPoint trialPoint ) {
		this( trialPoint._values );
	}


	/**
	 * Constructor
	 *
	 * @param size  intial size allocated to the trial point's map of values
	 */
	public MutableTrialPoint( final int size ) {
		this( new HashMap<Variable,Number>( size ) );
	}


	/** Constructor */
	public MutableTrialPoint() {
		this( new HashMap<Variable,Number>() );
	}


	/**
	 * Get an immutable copy of this trial point.
	 *
	 * @return   an immutable copy of this trial point.
	 */
	public TrialPoint getTrialPoint() {
		return new TrialPoint( this );
	}


	/**
	 * Set the value to associate with the variable.
	 *
	 * @param variable  The variable for which to associate the value
	 * @param value     The value of the specified variable
	 */
	public void setValue( final Variable variable, final double value ) {
		_values.put( variable, value );
	}
}

