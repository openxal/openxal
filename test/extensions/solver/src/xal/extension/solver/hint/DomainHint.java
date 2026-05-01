//
//  DomainHint.java
//  xal
//
//  Created by Thomas Pelaia on 4/18/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.extension.solver.hint;

import xal.extension.solver.*;

import java.util.*;


/** A hint that indicates a variable domain. */
abstract public class DomainHint extends Hint {
	final static public int LOWER_IND = 0;
	final static public int UPPER_IND = 1;
	
	
	/** Constructor */
	public DomainHint( final String label ) {
		super( label );
	}
	
	
	/** Determine if there is an entry for the variable */
	abstract public boolean hasVariable( final Variable variable );	
		
	
	/** Get the domain for the specified variable. */
	abstract public double[] getRange( final Variable variable );
}
