/*
 * GenericNode.java
 *
 * Created on December 3, 2002, 8:48 AM
 */

package xal.smf.impl;

import xal.smf.AcceleratorNode;
import xal.smf.impl.qualify.*;
import xal.tools.data.*;
import xal.ca.ChannelFactory;

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
	 * Primary Constructor.
	 * @param strType type of this node (since it is Generic and there is no default type)
	 * @param strId ID for this node
	 * @param channelFactory fractory from which to generate channels
	 */
	public GenericNode( final String strType, final String strId, final ChannelFactory channelFactory ) {
		super( strId, channelFactory );

		this.m_strType = strType;
		ElementTypeManager.defaultManager().registerType( GenericNode.class, strType );
	}


	/**
	 * Constructor using default channel factory.
	 * @param strType type of this node (since it is Generic and there is no default type)
	 * @param strId ID for this node
	 */
	public GenericNode( final String strType, final String strId ) {
		this( strType, strId, null );
	}

    
    /** Overriden to provide type signature */
    public String getType()   { return m_strType; }


	/** Instantiate a new GenericNode */
	@Deprecated
	static public GenericNode newNode( final String strType, final String strId ) {
		return new GenericNode( strType, strId, null );
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
