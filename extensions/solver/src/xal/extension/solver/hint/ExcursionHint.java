//
// ExcursionHint.java: Source file for 'ExcursionHint'
// Project 
//
// Created by Tom Pelaia on 8/10/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.solver.hint;

import xal.extension.solver.Variable;

import java.util.*;


/** ExcursionHint */
public class ExcursionHint extends Hint {
    /** type identifier for this kind of hint */
    final static public String TYPE = "ExcursionHint";
    
    /** map of domain to use keyed by variable */
	final private Map<Variable, VariableExcursionDomain> VARIABLE_DOMAINS;
    
    /** default domain to use for a variable if one isn't specified specifically for the variable */
	final private VariableExcursionDomain DEFAULT_DOMAIN;
    
    
	/** Primary Constructor */
    protected ExcursionHint( final VariableExcursionDomain defaultDomain ) {
        super( "Excursion Hint" );
        
        DEFAULT_DOMAIN = defaultDomain;
        VARIABLE_DOMAINS = new HashMap<Variable,VariableExcursionDomain>();
    }
    
    
    /** Empty Constructor */
    public ExcursionHint() {
        this( null );
    }
    
	
	/** Get an excursion hint specifying a default fractional excursion about the current value. */
	static public ExcursionHint getFractionalExcursionHint( final double fraction ) {
		return new ExcursionHint( new FractionExcursionDomain( fraction ) );
	}
    
	
	/** Get an excursion hint specifying a default maximum absolute excursion about the current value. */
	static public ExcursionHint getAbsoluteMaxExcursionHint( final double maxExcursion ) {
		return new ExcursionHint( new AbsoluteMaxExcursionDomain( maxExcursion ) );
	}

	
	/** 
     * Get the type identifier of this Hint which will be used to fetch this hint in a table of hints.
     * @return the type identifier of this kind of Hint
     */
	public String getType() {
		return TYPE;
	}
	
	
	/** Determine if there is an entry for the variable */
	public boolean hasVariable( final Variable variable ) {
		return VARIABLE_DOMAINS.containsKey( variable );
	}
	
	
	/** add the absolute maximum excursion for a variable */
	public void addAbsoluteMaxExcursion( final Variable variable, final double maxExcursion ) {
		VARIABLE_DOMAINS.put( variable, new AbsoluteMaxExcursionDomain( maxExcursion ) );
	}
	
	
	/** add fraction excursion range for the specified variable */
	public void addFractionExcursion( final Variable variable, final double fraction ) {
		VARIABLE_DOMAINS.put( variable, new FractionExcursionDomain( fraction ) );		
	}
	
	
	/** Get the domain for the specified variable and the current value. */
	public double[] getRange( final double value, final Variable variable ) {
		final VariableExcursionDomain domain = VARIABLE_DOMAINS.get( variable );
        // if a domain is specified for the variable, then use it
		if ( domain != null ) {
			return domain.getRange( value, variable );
		}
		
        // if no domain is specified for the variable then default to the default domain if available
		if ( DEFAULT_DOMAIN != null ) {
			return DEFAULT_DOMAIN.getRange( value, variable );
		}
		
        // as a last resort, use the variable limits as the range
		return new double[] { variable.getLowerLimit(), variable.getUpperLimit() };
	}
}



/** the domain for searching about the current value */
interface VariableExcursionDomain {
	/** get the range for the specified variable about the specified current value */
	public double[] getRange( final double value, final Variable variable );
}



/** a domain specified as a fraction of the variable domain about the current value */
class FractionExcursionDomain implements VariableExcursionDomain {
	/** a fraction of the domain */
	final private double FRACTION;
	
	
	/** Primary Constructor */
	public FractionExcursionDomain( final double fraction ) {
		FRACTION = fraction;
	}
	
	
	/** get the fraction */
	public double getFraction() {
		return FRACTION;
	}
	
	
	/** get the range restricted to the variable's limits */
	public double[] getRange( final double value, final Variable variable ) {
		final double lowerLimit = variable.getLowerLimit();
		final double upperLimit = variable.getUpperLimit();
        
        final double delta = FRACTION * ( upperLimit - lowerLimit );
		return new double[] { Math.max( value - delta, lowerLimit ), Math.min( value + delta, upperLimit )  };
	}
}



/** The variable range domain for searching about the current value. */
class AbsoluteMaxExcursionDomain implements VariableExcursionDomain {
	/** the change about the current value */
	final private double MAX_EXCURSION;
	
	
	/** Primary Constructor */
	protected AbsoluteMaxExcursionDomain( final double maxExcursion ) {
		MAX_EXCURSION = maxExcursion;
	}
	
	
	/** get the lower limit */
	public double[] getRange( final double value ) {
		return new double[] { value - MAX_EXCURSION, value + MAX_EXCURSION };
	}
	
	
	/** get the range restricted to the variable's limits */
	public double[] getRange( final double value, final Variable variable ) {
        final double[] deltaRange = getRange( value );
		return new double[] { Math.max( deltaRange[0], variable.getLowerLimit() ), Math.min( deltaRange[1], variable.getUpperLimit() ) };
	}
}


