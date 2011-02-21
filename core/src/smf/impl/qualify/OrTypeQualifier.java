/*
 * OrTypeQualifier.java
 *
 * Created on March 12, 2002, 10:42 AM
 */

package xal.smf.impl.qualify;

import xal.smf.*;
import java.util.*;


/**
 * OrTypeQualifier is a compound qualifier that tests whether
 * any of its qualifiers matches the accelerator node.  If any one of its 
 * root qualifiers returns true for matching a node, then this compound match operation 
 * will return true, otherwise it will not match the node.
 *
 * @author  tap
 */
public class OrTypeQualifier implements TypeQualifier {
	/** set of qualifiers to "or" */
    private Set<TypeQualifier> _qualifiers;
	
	
    /** 
	 * Creates new OrTypeQualifier 
	 */
    public OrTypeQualifier() {
        _qualifiers = new HashSet<TypeQualifier>();
    }
    
    
	/**
	 * Add a "kind" qualifier to the set of root qualifiers.
	 * @param kind The node type of the new root qualifier
	 * @return This instance for convenience of chaining "or" operations.
	 */
    public OrTypeQualifier or( final String kind ) {
        return or( new KindQualifier( kind ) );
    }
    
    
	/**
	 * Add a qualifier to the set of root qualifiers.
	 * @param qualifier A qualifier to "or" with the existing root qualifiers.
	 * @return This instance for convenience of chaining "or" operations.
	 */
    public OrTypeQualifier or( final TypeQualifier qualifier ) {
        _qualifiers.add( qualifier );
		return this;
    }
	
	
	/**
	 * Or the specified qualifiers by kind with this one.
	 * @param kinds the array of node types
	 * @return this qualifier
	 */
	public OrTypeQualifier or( final String ... kinds ) {
		for ( String kind : kinds ) {
			or( kind );
		}
		
		return this;
	}
	
	
	/**
	 * Or the specified qualifiers with this one.
	 * @param qualifiers the array of node qualifiers
	 * @return this qualifier
	 */
	public OrTypeQualifier or( final TypeQualifier ... qualifiers ) {
		for ( TypeQualifier qualifier : qualifiers ) {
			or( qualifier );
		}
		
		return this;
	}
	
	
	/**
	 * Get a qualifier that matches for any of the specified node types.
	 * @param kinds the array of node types
	 * @return a qualifier that matches for any of the given node types
	 */
	static public OrTypeQualifier qualifierForKinds( final String ... kinds ) {
		return new OrTypeQualifier().or( kinds );
	}
	
	
	/**
	 * Get a qualifier that matches for any of the specified qualifiers.
	 * @param qualifiers the array of node qualifiers
	 * @return a qualifier that matches for any of the given qualifiers
	 */
	static public OrTypeQualifier qualifierForQualifiers( final TypeQualifier ... qualifiers ) {
		return new OrTypeQualifier().or( qualifiers );
	}
	
    
    /** 
	 * Determine if the specified node is a match based on this qualifier's criteria.
	 * One or more root qualifiers must match the node otherwise it will return false
	 * even if there are no root qualifiers.
	 * @param node The node to test
	 * @return true if the node is a match to at least one root qualifier and false if not
	 */
    public boolean match( final AcceleratorNode node ) {
		for ( TypeQualifier qualifier : _qualifiers ) {
            if ( qualifier.match( node ) )  return true;
        }
        
        return false;
    }
}
