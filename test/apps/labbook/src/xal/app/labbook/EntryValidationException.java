//
//  EntryValidationException.java
//  xal
//
//  Created by Pelaia II, Tom on 9/26/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;


/** exception indicating that the entry is not valid for publication */
public class EntryValidationException extends RuntimeException {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
	/** Constructor */
	public EntryValidationException( final String message ) {
		super( message );
	}
}
