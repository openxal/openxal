//
//  NodeRecordCompoundFilter.java
//  xal
//
//  Created by Tom Pelaia on 10/25/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

package xal.app.opticseditor;

import java.util.ArrayList;
import java.util.List;


/** compound filter for node records */
public class NodeRecordCompoundFilter implements NodeRecordFilter {
	/** list of node record filters to check */
	private List<NodeRecordFilter> _filters;
	
	
	/** Constructor */
	public NodeRecordCompoundFilter() {
		_filters = new ArrayList<NodeRecordFilter>();
	}
	
	
	/** add the specified filter to the list of filters to check */
	public void addFilter( final NodeRecordFilter filter ) {
		_filters.add( filter );
	}
	
	
	/** determine whether to accept the specified record by verifying whether the record is accepted by all filters */
	public boolean accept( final NodeRecord record ) {
		for ( final NodeRecordFilter filter : _filters ) {
			if ( !filter.accept( record ) ) {
				return false;
			}
		}
		return true;
	}
}
