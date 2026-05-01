//
//  InitialDeltaDomain.java
//  xal
//
//  Created by Thomas Pelaia on 4/18/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.extension.solver.hint;

import xal.extension.solver.*;

import java.util.*;


/** A hint that indicates a good initial search space about the initial variable values. */
public class InitialDelta extends DomainHint {
	final static public String TYPE = "InitialDelta";
	
    /** delta keyed by variable */
	final protected Map<Variable,Double> VARIABLE_DELTAS;
    
    /** default delta */
	final protected double _defaultDelta;
	
	
	/** Primary Constructor */
	public InitialDelta( final double delta ) {
		super( "Initial Delta" );

		_defaultDelta = delta;
		
		VARIABLE_DELTAS = new HashMap<Variable,Double>();
	}
	
	
	/** Constructor */
	public InitialDelta() {
		this( Double.NaN );
	}
	
	
	/** 
	 * Get the type identifier of this Hint which will be used to fetch this hint in a table of hints.
	 * @return the unique type identifier of this Hint
	 */
	public String getType() {
		return TYPE;
	}
	
	
	/** Determine if there is an entry for the variable */
	public boolean hasVariable( final Variable variable ) {
		return VARIABLE_DELTAS.containsKey( variable );
	}
	
	
	/** add the initial delta for the specified variable */
	public void addInitialDelta( final Variable variable, final double delta ) {
		VARIABLE_DELTAS.put( variable, new Double( delta ) );
	}
	
	
	/** Get the domain for the specified variable. */
	public double[] getRange( final Variable variable ) {
		final Double deltaD = VARIABLE_DELTAS.get( variable );
		
		if ( deltaD != null ) {
			return getRange( variable, deltaD.doubleValue() );
		}
		else if ( !Double.isNaN( _defaultDelta ) ) {
			return getRange( variable, _defaultDelta );
		}
		else {
			return new double[] { variable.getLowerLimit(), variable.getUpperLimit() };			
		}
	}
	
	
	/** Get the range given the variable's initial value, limits and the specified delta. */
	static private double[] getRange( final Variable variable, final double delta ) {
		final double initialValue = variable.getInitialValue();
		final double lowerLimit = Math.max( initialValue - delta, variable.getLowerLimit() );
		final double upperLimit = Math.min( initialValue + delta, variable.getUpperLimit() );
		
		return new double[] { lowerLimit, upperLimit };
	}
}

