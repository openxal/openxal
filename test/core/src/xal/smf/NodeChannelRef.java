//
//  NodeChannelRef.java
//  xal
//
//  Created by Thomas Pelaia on 2/24/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.smf;

import xal.ca.Channel;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


/** Provides a reference to a channel within a node */
public class NodeChannelRef {
	/** pattern for parsing a node channel reference from a string */
	final static Pattern PARSE_PATTERN;
	
	/** the node whose channel is being referenced */
	final protected AcceleratorNode _node;
	
	/** the handle referencing the node's channel */
	final protected String _handle;
	
	
	// static initializer
	static {
		PARSE_PATTERN = Pattern.compile( "(\\w|:)+\\[(\\w)+\\]" );
	}
	
	
	/** Constructor */
	public NodeChannelRef( final AcceleratorNode node, final String handle ) {
		_node = node;
		_handle = handle;
	}
	
	
	/** 
	 * attempt to parse the string for a node channel ref
	 * @param accelerator the accelerator for which to search for the node
	 * @param refString the string to parse for the node and handle
	 * @return a node channel ref or null if no match can be found
	 */
	static public NodeChannelRef getInstance( final Accelerator accelerator, final String refString ) {
		final Matcher matcher = PARSE_PATTERN.matcher( refString );
		if ( matcher.matches() ) {
			final String nodeID = refString.substring( 0, matcher.end(1) );
			final AcceleratorNode node = accelerator.getNode( nodeID );
			if ( node == null )  return null;
			final String handle = refString.substring( 1 + matcher.end(1), matcher.end(2) );
			return new NodeChannelRef( node, handle );
		}
		else {
			return null;
		}
	}
		
	
	/**
	 * Get the node
	 * @return the node
	 */
	public AcceleratorNode getNode() {
		return _node;
	}
	
	
	/**
	 * Get the channel handle which references the channel for the node
	 * @return the handle
	 */
	public String getHandle() {
		return _handle;
	}
	
	
	/**
	 * Get the referenced channel.
	 * @return the node's channel corresponding to the handle
	 */
	public Channel getChannel() {
		return _node.getChannel( _handle );
	}
	
	
	/**
	 * Get a string representation of the node/channel reference
	 * @return a string representation of the node/channel reference
	 */
	public String toString() {
		return _node.getId() + "[" + _handle + "]";  
	}
}
