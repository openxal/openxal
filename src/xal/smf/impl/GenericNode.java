/*
 * GenericNode.java
 *
 * Created on December 3, 2002, 8:48 AM
 */

package xal.smf.impl;

import xal.smf.AcceleratorNode;
import xal.smf.impl.qualify.*;
import xal.tools.data.*;

/**
 * GenericNode represents a node whose properties are defined by the data input.
 * Unlike other nodes, there are no predefined methods associated with the 
 * channels.  Instead, GenericNode is used to load nodes from data on the fly 
 * (for example from an XML file) so they can be manipulated in the accelerator 
 * object graph.
 * One can use the "getHandles()" and "getChannel()" methods of AcceleratorNode 
 * to interact in a meaningful way with the generic nodes.
 *
 * @author  tap
 */
public class GenericNode extends AcceleratorNode {
    protected String m_strType;
    
    
    /** 
     * Creates a new instance of GenericNode.  Use the static "newNode()" 
     * method instead to instantiate a new GenericNode.
     */
    protected GenericNode(String strId) {
        super(strId);
    }
    
    
    /** Overriden to provide type signature */
    public String getType()   { return m_strType; }
    
    
    /** Instantiate a new GenericNode */
    static public GenericNode newNode(String strType, String strId) {
        GenericNode node = new GenericNode(strId);
        node.m_strType = strType;
        ElementTypeManager.defaultManager().registerType(GenericNode.class, strType);
        
        return node;
    }
    
    
    /** 
     * Determine if this node is of the specified type.  Override the inherited
     * method since the types of generic nodes are not associated with the 
     * class unlike typical nodes.
     * @param compType The type to compare against.
     * @return true if the node is a match and false otherwise.
     */
    public boolean isKindOf(String compType) {
        return compType.equalsIgnoreCase(this.m_strType);
    }
}
