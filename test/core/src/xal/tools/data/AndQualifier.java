//
//  AndKeyedValueQualifier.java
//  xal
//
//  Created by Thomas Pelaia on 5/2/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;

import java.util.List;


/** Generate a compound qualifer using the "and" operation. */
public class AndQualifier extends CompoundQualifier {
	/**
	 * Primary Constructor
	 * @param reserve the initial capacity reserved for holding qualifiers.
	 */
	public AndQualifier( final int reserve ) {
		super( reserve );
	}
	
	
	/**
	 * Constructor with a variable number of qualifiers.
	 * @param qualifiers the qualifiers to append
	 */
	public AndQualifier( final Qualifier ... qualifiers ) {
		this( qualifiers.length );
		for ( final Qualifier qualifier : qualifiers ) {
			append( qualifier );
		}
	}
	
	
	/**
	 * Constructor with a list of qualifiers.
	 * @param qualifiers the qualifiers to append
	 */
	public AndQualifier( final List<? extends Qualifier> qualifiers ) {
		this( qualifiers.size() );
		for ( final Qualifier qualifier : qualifiers ) {
			append( qualifier );
		}
	}
	
	
	/** Constructor */
	public AndQualifier() {
		this( DEFAULT_RESERVE_CAPACITY );
	}
	
    
    /** 
	* Determine if the specified object satisfies every sub qualifier's criteria.
	* @param object The object to test
	* @return true if the object is a match and false if not
	*/
    public boolean matches( final Object object ) {
		// verify that every qualifier's criteria is satisfied
        for ( int index = 0 ; index < _qualifierCount ; index++ ) {
			if ( !_qualifiers[index].matches( object ) ) {
				return false;
			}
        }
        
        return true;    // all qualifiers must have been satisfied
    }	
	
	
	/**
	 * The binary operator token.
	 * @return "&" to represent this "and" operation.
	 */
	public String binaryToken() {
		return "&";
	}
}


