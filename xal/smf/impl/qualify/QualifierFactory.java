//
//  QualifierFactory.java
//  xal
//
//  Created by Thomas Pelaia on 10/26/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.smf.impl.qualify;

import xal.smf.*;


/** Factory to create qualifiers. */
public class QualifierFactory {
	static protected TypeQualifier GOOD_STATUS_QUALIFIER;
	static protected TypeQualifier BAD_STATUS_QUALIFIER;
	
	
	/** Protected constructor */
	protected QualifierFactory() {}
	
	
	/** 
	 * Get a qualifier for testing a node for the specified status 
	 * @param statusFilter the status against which to qualify nodes
	 * @return the status qualifier
	 */
	static public TypeQualifier getStatusQualifier( final boolean statusFilter ) {
		populateStatusQualifiers();
		return statusFilter ? GOOD_STATUS_QUALIFIER : BAD_STATUS_QUALIFIER;
	}
	
	
	/**
	 * Get a qualifier for testing whether a node's software type matches the specified software type
	 * @param softType software type for comparison
	 */
	static public TypeQualifier getSoftTypeQualifier( final String softType ) {
		return new TypeQualifier() {
			public boolean match( final AcceleratorNode node ) {
                final String nodeSoftType = node.getSoftType();
                // if neither soft type is null, then compare strings for equality for best reliability otherwise compare pointers
                return nodeSoftType != null && softType != null ? nodeSoftType.equals( softType ) : nodeSoftType == softType;
			}
		};
	}
	
	
	/**
	 * Get an qualifier for the specified node status and type.
	 * @param nodeStatus the node status
	 * @param type the node type
	 * @return a qualifier restricted to both the status and type specified
	 */
	static public TypeQualifier qualifierWithStatusAndType( final boolean nodeStatus, final String type ) {
		return AndTypeQualifier.qualifierWithStatusAndType( nodeStatus, type );
	}
	
	
	/**
	 * Get a qualifier that matches for any of the specified node types and the specified node status.
	 * @param nodeStatus the status of the nodes to match
	 * @param kinds the array of node types
	 * @return a qualifier that matches for any of the given node types
	 */
	static public TypeQualifier qualifierWithStatusAndTypes( final boolean nodeStatus, final String ... kinds ) {
		return new AndTypeQualifier().andStatus( nodeStatus ).and( OrTypeQualifier.qualifierForKinds( kinds ) );
	}
	
	
	/**
	 * Get a qualifier that matches for any of the specified qualifiers and the specified node status.
	 * @param nodeStatus the status of the nodes to match
	 * @param qualifiers the array of node qualifiers
	 * @return a qualifier that matches for any of the given qualifiers
	 */
	static public TypeQualifier qualifierForQualifiers( final boolean nodeStatus, final TypeQualifier ... qualifiers ) {
		return new AndTypeQualifier().andStatus( nodeStatus ).and( OrTypeQualifier.qualifierForQualifiers( qualifiers ) );
	}
	
	
	/** populate status qualifiers */
	static protected void populateStatusQualifiers() {
		if ( GOOD_STATUS_QUALIFIER == null ) {
			GOOD_STATUS_QUALIFIER = new TypeQualifier() {
				public boolean match( final AcceleratorNode node ) {
					return node.getStatus();
				}
			};
			
			BAD_STATUS_QUALIFIER = new TypeQualifier() {
				public boolean match( final AcceleratorNode node ) {
					return !node.getStatus();
				}
			};
		}
	}
}
