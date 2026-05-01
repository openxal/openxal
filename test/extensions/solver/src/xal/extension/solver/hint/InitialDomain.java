//
//  InitialDomain.java
//  xal
//
//  Created by Thomas Pelaia on 4/8/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.extension.solver.hint;

import xal.extension.solver.*;

import java.util.*;


/** A hint that indicates a good initial search space */
public class InitialDomain extends DomainHint {
	final static public String TYPE = "InitialDomain";
	
	final protected Map<Variable, VariableDomain> _variableDomains;
	final protected VariableDomain _defaultDomain;
	
	
	/** Primary Constructor */
	protected InitialDomain( final VariableDomain domain ) {
		super( "Initial Domain" );
		_defaultDomain = domain;
		_variableDomains = new HashMap<Variable, VariableDomain>();
	}
	
	
	/** Simple range constructor with the specified range limits for each variable */
	public InitialDomain( final double defaultLowerLimit, final double defaultUpperLimit ) {
		this ( RangeDomain.getInstance( defaultLowerLimit, defaultUpperLimit ) );
	}
	
	
	/** Constructor */
	public InitialDomain() {
		this( Double.NaN, Double.NaN );
	}
	
	
	/** Get an initial domain hint whose range for each variable is the specified fraction of the variable's limits. */
	static public InitialDomain getFractionalDomainHint( final double fraction ) {
		return new InitialDomain( new FractionDomain( fraction ) );
	}
		
	
	/** 
	* Get the type identifier of this Hint which will be used to fetch this hint in a table of hints.  Subclasses should
	* override this method to return a unique string identifying the hint.
	* @return the unique type identifier of this Hint
	*/
	public String getType() {
		return TYPE;
	}
	
	
	/** Determine if there is an entry for the variable */
	public boolean hasVariable( final Variable variable ) {
		return _variableDomains.containsKey( variable );
	}
	
	
	/** add the initial range for the specified variable */
	public void addRange( final Variable variable, final double lowerLimit, final double upperLimit ) {
		_variableDomains.put( variable, new RangeDomain( lowerLimit, upperLimit ) );
	}
	
	
	/** add the initial fraction range for the specified variable */
	public void addFractionRange( final Variable variable, final double fraction ) {
		_variableDomains.put( variable, new FractionDomain( fraction ) );		
	}
	
	
	/** Get the domain for the specified variable. */
	public double[] getRange( final Variable variable ) {
		final VariableDomain domain = _variableDomains.get( variable );
		if ( domain != null ) {
			return domain.getRange( variable );
		}
		
		if ( _defaultDomain != null ) {
			return _defaultDomain.getRange( variable );
		}
		
		return new double[] { variable.getLowerLimit(), variable.getUpperLimit() };
	}
}



/** the domain for searching */
interface VariableDomain {
	/** get the range for the specified variable */
	public double[] getRange( final Variable variable );
}



/** a domain specified as a fraction of the variable domain */
class FractionDomain implements VariableDomain {
	/** a fraction of the domain */
	final private double _fraction;
	
	
	/** Primary Constructor */
	public FractionDomain( final double fraction ) {
		_fraction = fraction;
	}
	
	
	/** get the fraction */
	public double getFraction() {
		return _fraction;
	}
	
	
	/** get the range restricted to the variable's limits */
	public double[] getRange( final Variable variable ) {
		final double lowerLimit = variable.getLowerLimit();
		final double upperLimit = variable.getUpperLimit();
		final double lowerEnd = 0.5 * ( ( 1.0 + _fraction ) * lowerLimit + ( 1.0 - _fraction ) * upperLimit );
		final double upperEnd = 0.5 * ( ( 1.0 - _fraction ) * lowerLimit + ( 1.0 + _fraction ) * upperLimit );
		return new double[] { lowerEnd, upperEnd  };
	}
}



/** The variable range domain for searching. */
class RangeDomain implements VariableDomain {
	/** the range to apply to variables */
	final private double[] _range;
	
	
	/** Primary Constructor */
	protected RangeDomain( final double lowerLimit, final double upperLimit ) {
		_range = new double[] { lowerLimit, upperLimit };
	}
	
	
	/** Get an instance or null if either limit is NaN */
	static public RangeDomain getInstance( final double lowerLimit, final double upperLimit ) {
		return !Double.isNaN( lowerLimit ) && !Double.isNaN( upperLimit ) ? new RangeDomain( lowerLimit, upperLimit ) : null;
	}
	
	
	/** get the lower limit */
	public double[] getRange() {
		return _range;
	}
	
	
	/** get the range restricted to the variable's limits */
	public double[] getRange( final Variable variable ) {
		return new double[] { Math.max( _range[InitialDomain.LOWER_IND], variable.getLowerLimit() ), Math.min( _range[InitialDomain.UPPER_IND], variable.getUpperLimit() ) };
	}
}


