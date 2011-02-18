//
//  RunTerminationException.java
//  xal
//
//  Created by Thomas Pelaia on 6/30/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.solver;


/** Exception indicating that a run has been terminated */
public class RunTerminationException extends RuntimeException {
	/** Primary Constructor */
	public RunTerminationException( final String text ) {
		super( text );
	}
	
	
	/** Constructor */
	public RunTerminationException() {
		this( "Run has been terminated during an trial point evaluation." );
	}
}
