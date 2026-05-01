//
//  PVNodeFormatGroup.java
//  xal
//
//  Created by Tom Pelaia on 2/12/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import java.util.*;

import xal.tools.data.*;
import xal.smf.*;
import xal.smf.data.*;


/** group of patterns for matching PVs based on a node name */
public class PVNodeFormatGroup {
	/** tag for the data adaptor */
	final static public String DATA_LABEL = "PVNodeFormatGroup";
	
	/** list of formats which can generate PVs from node names */
	final protected List<PVNodeFormat> PV_NDOE_FORMATS;
	
	
	/** Constructor */
	public PVNodeFormatGroup( final DataAdaptor adaptor ) {
		PV_NDOE_FORMATS = new ArrayList<PVNodeFormat>();
		
		final List<DataAdaptor> formatAdaptors = adaptor.childAdaptors( PVNodeFormat.DATA_LABEL );
		for ( final DataAdaptor formatAdaptor : formatAdaptors ) {
			PV_NDOE_FORMATS.add( new PVNodeFormat( formatAdaptor ) );
		}
	}
	
	
	/** extract the node key from the specified pv */
	public String getNodeKey( final String pv ) {
		for ( final PVNodeFormat nodeFormat : PV_NDOE_FORMATS ) {
			final String nodeKey = nodeFormat.getNodeKey( pv );
			if ( nodeKey != null )  return nodeKey;
		}
		return null;
	}
}