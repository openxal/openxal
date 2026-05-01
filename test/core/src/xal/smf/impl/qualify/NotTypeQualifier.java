/*
 * NotTypeQualifier.java
 *
 * Created on March 12, 2002, 10:42 AM
 */

package xal.smf.impl.qualify;

import xal.smf.*;


/**
 * NotTypeQualifier tests whether its root qualifier does not match
 * the accelerator node.  The NotTypeQualifier is constructed by specifying
 * a root qualifier and its matching criteria yields the opposite result of
 * its root qualifier.
 *
 * @author  tap
 */
public class NotTypeQualifier implements TypeQualifier {
	/** root qualifier to "not" */
    private TypeQualifier rootQualifier;
    

    /** 
	 * Creates new NotTypeQualifier off of a node type
	 * @param kind The node type used to define the root qualifier
	 */
    public NotTypeQualifier(String kind) {
        this( new KindQualifier(kind) );
    }
    
    
    /** 
	 * Creates new NotTypeQualifier off of an existing qualifier 
	 * @param qualifier The root qualifier
	 */
    public NotTypeQualifier(TypeQualifier qualifier) {
        rootQualifier = qualifier;
    }


    /** 
	 * Determine if the specified node is a match based on this qualifier's criteria
	 * @param node The node to test
	 * @return true if the node is a match and false if not
	 */
    public boolean match(AcceleratorNode node) {
        return !rootQualifier.match(node);
    }

}
