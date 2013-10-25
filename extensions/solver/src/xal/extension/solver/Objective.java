/*
 *  Objective.java
 *
 *  Created Wednesday June 9, 2004 2:45pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import java.util.*;

/**
 * Objective represents a goal to achieve in optimization.  An objective corresponds to some
 * specified measure and determines that satisfaction achieved by a particular value of the measure. 
 *
 * @author   ky6
 * @author   t6p
 */
public abstract class Objective {
	/** the name of the objective */
	protected final String _name;

	/**
	 * Constructor
	 *
	 * @param name  the name to assign the objective
	 */
	public Objective( final String name ) {
		_name = name;
	}


	/**
	 * Determines how satisfied the user is with the specified value for this objective.
	 *
	 * @param value  The value associated with this objective for a particular trial
	 * @return       the user satisfaction for the specified value
	 */
	public abstract double satisfaction( double value );


	/**
	 * Get the name of this objective.
	 *
	 * @return   The name of this objective.
	 */
	public String getName() {
		return _name;
	}


	/**
	 * A description of this objective.
	 *
	 * @return   The string representation of an objective.
	 */
	public String toString() {
		return getName();
	}
}

