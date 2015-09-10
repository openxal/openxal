//
//  NodeKeyConverter.java
//  xal
//
//  Created by Tom Pelaia on 2/12/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import java.util.*;


/** convert between a segment of a PV and a node key */
public class NodeKeyConverter {
	/** table of node-key converters keyed by name */
	final static HashMap<String,NodeKeyConverter> CONVERTERS;
	
	
	// static initializer
	static {
		CONVERTERS = new HashMap<String,NodeKeyConverter>();
		CONVERTERS.put( "default", new NodeKeyConverter() );
		CONVERTERS.put( "NumericToAlpha", new NumericToAlphaNodeKeyConverter() );
	}
	
	
	/** get the default converter */
	static public NodeKeyConverter defaultConverter() {
		return CONVERTERS.get( "default" );
	}
	
	
	/** get a converter with the specified name */
	static public NodeKeyConverter getConverter( final String name ) {
		return CONVERTERS.get( name );
	}
	
	
	/** get a node key from a segment of a PV */
	public String toNodeKey( final String segment ) {
		return segment;
	}
	
	
	/** get a PV segment from the node key */
	public String toSegment( final String nodeKey ) {
		return nodeKey;
	}
}



/** converts the last character of the segment from a digit to the corresponding character (1->a, 2->b, ...) */
class NumericToAlphaNodeKeyConverter extends NodeKeyConverter {
	/** get a node key from a segment of a PV */
	public String toNodeKey( final String segment ) {
		final int location = segment.length() - 1;	// location of the last character
		final char index = segment.charAt( location );
		return segment.substring( 0, location ) + toAlpha( index );
	}
	
	
	/** get a PV segment from the node key */
	public String toSegment( final String nodeKey ) {
		final int location = nodeKey.length() - 1;	// location of the last character
		final char alpha = nodeKey.charAt( location );
		return nodeKey.substring( 0, location ) + toIndex( alpha );
	}
	
	
	/** get the corresponding alpha character */
	protected String toAlpha( final char index ) {
		switch( index ) {
			case '1':  return "a";
			case '2':  return "b";
			case '3':  return "c";
			case '4':  return "d";
			case '5':  return "e";
			case '6':  return "f";
			default: return null;
		}
	}
	
	
	/** get the corresponding index of the alpha character */
	protected String toIndex( final char alpha ) {
		switch( alpha ) {
			case 'a':  return "1";
			case 'b':  return "2";
			case 'c':  return "3";
			case 'd':  return "4";
			case 'e':  return "5";
			case 'f':  return "6";
			default: return null;
		}
	}
}
