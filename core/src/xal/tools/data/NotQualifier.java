//
//  NotKeyValueQualifier.java
//  xal
//
//  Created by Thomas Pelaia on 5/2/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;


/** Qualifier which negates another qualifier's matching. */
public class NotQualifier implements Qualifier {
	/** the qualifier whose matching is negated. */
	private Qualifier _qualifier;
	
	
	/**
	 * Constructor
	 * @param qualifier The qualifier to negate.
	 */
	public NotQualifier( final Qualifier qualifier ) {
		_qualifier = qualifier;
	}
	
	
	/** 
	 * Determine if the specified object is not a match to the enclosed qualifier.
	 * @param object the object to test for matching
	 * @return true if the object does not match the enclosed qualifier's critiera and false if it does match.
	 */
	public boolean matches( final Object object ) {
		return !_qualifier.matches( object );
	}
	
	
	/**
	 * Get a string represenation of this instance.
	 * @return "!" followed by this instance's associated qualifier.
	 */
	public String toString() {
		return "!(" + _qualifier + ")";
	}
}
