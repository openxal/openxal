/*
 * AndTypeQualifier.java
 *
 * Created on March 12, 2002, 10:42 AM
 */

package xal.smf.impl.qualify;

import xal.smf.*;
import java.util.*;


/**
 * AndTypeQualifier is a compound qualifier that tests whether
 * all of its root qualifiers match the accelerator node.  If and only if the set of
 * root qualifiers all match the node then this qualifier matches the node also.
 *
 * @author  tap
 */
public class AndTypeQualifier implements TypeQualifier {
	/** set of qualifiers to "and" */
    private Set<TypeQualifier> _qualifiers;
	
	
    /** 
	 * Creates a new AndTypeQualifier with no initial root qualifiers
	 */
    public AndTypeQualifier() {
        _qualifiers = new HashSet<TypeQualifier>();
    }
    
    
	/**
	 * Add a "kind" qualifier to the set of root qualifiers.
	 * @param kind The node type of the new root qualifier
	 * @return This instance for convenience of chaining "and" operations.
	 */
    public AndTypeQualifier and( final String kind ) {
        return and( new KindQualifier( kind ) );
    }
    
    
	/**
	 * Add a qualifier to the set of root qualifiers.
	 * @param qualifier A qualifier to "and" with the existing root qualifiers.
	 * @return This instance for convenience of chaining "and" operations.
	 */
    public AndTypeQualifier and( final TypeQualifier qualifier ) {
        _qualifiers.add( qualifier );
		return this;
    }
	
	
	/**
	 * And the specified qualifiers with this one.
	 * @param qualifiers the array of node qualifiers
	 * @return this qualifier
	 */
	public AndTypeQualifier and( final TypeQualifier ... qualifiers ) {
		for ( TypeQualifier qualifier : qualifiers ) {
			and( qualifier );
		}
		
		return this;
	}
	
	
	/**
	 * Get a qualifier that matches for each of the specified qualifiers.
	 * @param qualifiers the array of node qualifiers
	 * @return a qualifier that matches for each of the qualifiers
	 */
	static public AndTypeQualifier qualifierWithQualifiers( final TypeQualifier ... qualifiers ) {
		return new AndTypeQualifier().and( qualifiers );
	}
	
	
	/**
	 * And this qualifier with the specified node status qualification.
	 * @param nodeStatus the status of nodes for which we wish to filter
	 * @return this instance
	 */
	public AndTypeQualifier andStatus( final boolean nodeStatus ) {
		_qualifiers.add( QualifierFactory.getStatusQualifier( nodeStatus ) );
		return this;
	}
	
	
	/**
	 * Get an qualifier for the specified node status and type.
	 * @param nodeStatus the node status
	 * @param type the node type
	 * @return a qualifier restricted to both the status and type specified
	 */
	static public AndTypeQualifier qualifierWithStatusAndType( final boolean nodeStatus, final String type ) {
		return new AndTypeQualifier().andStatus( nodeStatus ).and( type );
	}

    
    /** 
	 * Determine if the specified node is a match based on this qualifier's criteria.
	 * All root qualifiers must match the node to return true.  If the set of 
	 * root qualifiers is empty, the match is still true.
	 * @param node The node to test
	 * @return true if the node is a match and false if not
	 */
    public boolean match( final AcceleratorNode node ) {
        for ( TypeQualifier qualifier : _qualifiers ) {
            if ( !qualifier.match(node) )  return false;
        }
        
        return true;    // all qualifiers must have matched
    }
}
