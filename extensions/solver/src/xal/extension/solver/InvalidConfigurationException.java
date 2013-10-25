//
//  InvalidConfigurationException.java
//  xal
//
//  Created by Thomas Pelaia on 6/29/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.solver;


/** Exception indicating an invalid solver configuration */
public class InvalidConfigurationException extends RuntimeException {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	/** Constructor */
	public InvalidConfigurationException( final String message ) {
		super( message );
	}
}
