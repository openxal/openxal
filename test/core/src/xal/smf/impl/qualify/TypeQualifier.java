/*
 * TypeQualifier.java
 *
 * Created on January 23, 2002, 4:39 PM
 */

package xal.smf.impl.qualify;

import xal.smf.*;


/**
 * TypeQualifier is the interface of all qualifiers that can test whether a 
 * node passes a matching criteria.
 * 
 * @author  tap
 */
public interface TypeQualifier {
	/**
	 * Determine if the specified node is a match based on this qualifier's criteria
	 * @param node The node to test
	 * @return true if the node is a match and false if not
	 */
    public abstract boolean match(AcceleratorNode node);
}
