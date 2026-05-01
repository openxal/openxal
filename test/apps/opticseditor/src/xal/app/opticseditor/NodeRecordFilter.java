//
//  NodeRecordFilter.java
//  xal
//
//  Created by Tom Pelaia on 10/25/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

package xal.app.opticseditor;


/** filter a node record */
public interface NodeRecordFilter {
	public boolean accept( final NodeRecord record );
}