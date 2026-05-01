/*
 *  Variable.java
 *
 *  Created Wednesday June 9, 2004 2:31pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import java.util.*;


/**
 * Variable describes a parameter that may be varied by the solver.  It specifies a name, an initial guess and upper and lower limits.
 * @author   ky6
 * @author   t6p
 */
public class Variable {
	/** the name of the variable */
	protected final String _name;
	
	/** the initial value/guess assigned to the variable */
	protected final double _initialValue;
	
	/** the lowest value that can be assigned to the variable */
	protected final double _lowerLimit;
	
	/** the highest value that can be assigned to the variable */
	protected final double _upperLimit;
	
	
	/**
	 * Creates a new instance of Variable.
	 * @param initialValue  the initial first guess for the variable (e.g. starting point)
	 * @param name          the name to assign to the variable
	 * @param lowerLimit    the lowest value that should be assigned to the variable
	 * @param upperLimit    the highest value that should be assigned to the variable
	 */
	public Variable( String name, double initialValue, double lowerLimit, double upperLimit ) {
		_name = name;
		_initialValue = initialValue;
		_lowerLimit = lowerLimit;
		_upperLimit = upperLimit;
	}
	
	
	/**
	 * Copy this variable but substitute the specified initial value for this variable's initial value.
	 * @param initialValue initial value to use for the new variable
	 * @return new variable with the same properties as this instance but substituting the specified initial value
	 */
	public Variable copyWithInitialValue( final double initialValue ) {
		return new Variable( _name, initialValue, _lowerLimit, _upperLimit );
	}
	
	
	/**
	 * Get this variable's name.
	 * @return   this variable's name
	 */
	public String getName() {
		return _name;
	}
	
	
	/**
	 * Get the initial value (i.e. initial guess).
	 * @return   the initial value
	 */
	public double getInitialValue() {
		return _initialValue;
	}
	
	
	/**
	 * Get the lowest value that can be assigned to this variable.
	 * @return   the lower limit
	 */
	public double getLowerLimit() {
		return _lowerLimit;
	}
	
	
	/**
	 * Get the highest value that can be assigned to this variable.
	 * @return   the upper limit
	 */
	public double getUpperLimit() {
		return _upperLimit;
	}
	
	
	/**
	 * A string for displaying a variable. The string consist of a title, an initial value, a
	 * lower limit and an upper limit.
	 * @return   The string representation of a variable.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "Variable: " + _name + ", " );
		buffer.append( "Initial Value: " + _initialValue + ", " );
		buffer.append( "Lower Limit: " + _lowerLimit + ", " );
		buffer.append( "Upper Limit: " + _upperLimit );

		return buffer.toString();
	}
}

