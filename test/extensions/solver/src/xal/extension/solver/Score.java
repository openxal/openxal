/*
 *  Score.java
 *
 *  Created Monday June 14, 2004 3:29 pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import java.util.*;

/**
 * Score is a collection of objectives with scores mapped to those objectives.
 *
 * @author   ky6
 */
public class Score {
	/** Description of the Field */
	protected final Objective _objective;
	/** Description of the Field */
	protected final double _value;
	/** Description of the Field */
	protected final double _satisfaction;

	/**
	 * Creates a new instance of Score.
	 *
	 * @param anObjective  The objective to be scored.
	 * @param aValue       The value given to the objective.
	 */
	public Score( Objective anObjective, double aValue ) {
		_objective = anObjective;
		_value = aValue;
		_satisfaction = _objective.satisfaction( _value );
	}


	/**
	 * Get the satisfaction.
	 *
	 * @return   The satisfaction as a double.
	 */
	public double getSatisfaction() {
		return _satisfaction;
	}


	/**
	 * Get the objective.
	 *
	 * @return   The objective to be scored.
	 */
	public Objective getObjective() {
		return _objective;
	}


	/**
	 * Get the objective's value.
	 *
	 * @return   The value
	 */
	public double getValue() {
		return _value;
	}


	/**
	 * A string for displaying a score. The string consist of a objective and a value.
	 *
	 * @return   The string representation of a score.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "Objective: " + _objective.toString() + ", " );
		buffer.append( "Value: " + _value + ", " );
		buffer.append( "Satisfaction: " + _satisfaction );

		return buffer.toString();
	}
}

