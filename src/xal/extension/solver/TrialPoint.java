/*
 *  TrialPoint.java
 *
 *  Created Wednesday June 9, 2004 2:29pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import xal.tools.messaging.MessageCenter;

import java.util.*;


/**
 * TrialPoint is a collection of variables and values for those variables.
 * @author ky6
 * @author t6p
 */
public class TrialPoint {
	/** A table of trial values keyed by variable */
	protected final Map<Variable,Number> _values;

	/**
	 * Primary constructor.
	 * @param values  The new values used to map trial points
	 */
	public TrialPoint( final Map<Variable,Number> values ) {
		_values = new HashMap<Variable,Number>( values );
	}


	/**
	 * Copy constructor.
	 * @param trialPoint  the trial point to copy
	 */
	public TrialPoint( final TrialPoint trialPoint ) {
		this( trialPoint._values );
	}


	/**
	 * Get the value corresponding to the specified variable.
	 * @param variable  The variable for which to fetch the value.
	 * @return          The value of the specified variable.
	 */
	public double getValue( final Variable variable ) {
		return _values.get( variable ).doubleValue();
	}


	/**
	 * Get the map of variable/value pairs.
	 * @return   the map of variable/value pairs.
	 */
	public Map<Variable,Number> getValueMap() {
		return Collections.unmodifiableMap( _values );
	}


	/**
	 * A string for displaying a trial point. The string consist of the values for the trial point.
	 * @return   The string representation of a trial point.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "Values: " + _values + "\n" );

		return buffer.toString();
	}
}

