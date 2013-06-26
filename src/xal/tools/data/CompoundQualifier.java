//
//  CompoundQualifier.java
//  xal
//
//  Created by Thomas Pelaia on 5/2/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;


/** Merge multiple qualifiers to form a single qualifier */
abstract public class CompoundQualifier implements Qualifier {
	/** the default initial reserve capacity */
	static final protected int DEFAULT_RESERVE_CAPACITY = 2;
	
	/** set of qualifiers that define this compound qualifier */
	protected Qualifier[] _qualifiers;
	
	/** the actual number of qualifiers that form this compound qualifier */
	protected int _qualifierCount;
	
	
    /**
	 * Primary Constructor 
	 * @param reserve the initial reserve estimate for the number of qualifiers that form this compound qualifier.
	 */
    public CompoundQualifier( final int reserve ) {
		_qualifierCount = 0;
        _qualifiers = new Qualifier[reserve];
    }
	
	
    /** Constructor */
    public CompoundQualifier() {
        this( DEFAULT_RESERVE_CAPACITY );
    }
    
    
	/**
	 * Append a qualifier to the set of root qualifiers.
	 * @param qualifier The qualifier to append with the existing root qualifiers.
	 * @return This instance for convenience of chaining "append" operations.
	 */
    public CompoundQualifier append( final Qualifier qualifier ) {
		Qualifier[] qualifiers;
		
		if ( _qualifiers.length <= _qualifierCount ) {
			// increase the size by atleast two and roughly 10% more
			qualifiers = new Qualifier[ 2 + (int)(1.1 * _qualifiers.length) ];
			System.arraycopy( _qualifiers, 0, qualifiers, 0, _qualifiers.length );			
		}
		else {
			qualifiers = _qualifiers;
		}
		
		qualifiers[_qualifierCount] = qualifier;
		_qualifiers = qualifiers;
		++_qualifierCount;
		
		return this;
    }
	
	
	/**
	 * The binary operator token.
	 * @return a token representing the binary operator.
	 */
	abstract public String binaryToken();
	
	
	/**
	 * Get a string representation of this instance.
	 * @return a string representing this compound qualifier.
	 */
	public String toString() {
		if ( _qualifierCount > 1 ) {
			final StringBuffer buffer = new StringBuffer( "(" + _qualifiers[0].toString() + ")" );
			for ( int index = 1 ; index < _qualifierCount ; index++ ) {
				buffer.append( " " + binaryToken() + " (" );
				buffer.append( _qualifiers[index] + ")" );
			}
			return buffer.toString();
		}
		else if ( _qualifierCount == 1 ) {
			return _qualifiers[0].toString();
		}
		else {
			return "";
		}
	}
}
