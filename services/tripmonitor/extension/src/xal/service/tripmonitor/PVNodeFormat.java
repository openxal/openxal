//
//  PVNodeFormat.java
//  xal
//
//  Created by Tom Pelaia on 2/12/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import java.util.*;
import java.util.regex.*;

import xal.tools.data.*;
import xal.smf.*;
import xal.smf.data.*;


/** pattern for matching PVs based on a node name */
public class PVNodeFormat {
	/** tag for the data adaptor */
	final static public String DATA_LABEL = "PVNodeFormat";
	
	/** PV pattern to match */
	final protected Pattern PV_PATTERN;
	
	/** converts the matching PV segment to a node-key */
	final protected NodeKeyConverter NODE_KEY_CONVERTER;
	
	
	/** Constructor */
	public PVNodeFormat( final DataAdaptor adaptor ) {
		final String pattern = adaptor.stringValue( "pattern" );		
		PV_PATTERN = Pattern.compile( pattern );
		
		if ( adaptor.hasAttribute( "conversion" ) ) {
			final String conversion = adaptor.stringValue( "conversion" );
			NODE_KEY_CONVERTER = NodeKeyConverter.getConverter( conversion );
		}
		else {
			NODE_KEY_CONVERTER = NodeKeyConverter.defaultConverter();
		}
	}
	
	
	/** extract the node key from the specified pv */
	public String getNodeKey( final String pv ) {
		final Matcher matcher = PV_PATTERN.matcher( pv );
		
		if ( matcher.matches() && matcher.groupCount() > 0 ) {
			final String matchingSegment = matcher.group( 1 );
			return NODE_KEY_CONVERTER.toNodeKey( matchingSegment );
		}
		else {
			return null;
		}
	}
}
