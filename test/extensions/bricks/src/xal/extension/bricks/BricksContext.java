//
//  BricksContext.java
//  xal
//
//  Created by Tom Pelaia on 6/19/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.net.URL;


/** Context within which Bricks was loaded */
public class BricksContext {
	/** URL of the bricks source definition file */
	private URL _sourceURL;
	
	
	/** Constructor */
	public BricksContext( final URL sourceURL ) {
		setSourceURL( sourceURL );
	}
	
	
	/** get the source URL */
	public URL getSourceURL() {
		return _sourceURL;
	}
	
	
	/** set the source URL */
	public void setSourceURL( final URL sourceURL ) {
		_sourceURL = sourceURL;
	}
}
