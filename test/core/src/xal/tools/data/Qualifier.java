//
//  Qualifier.java
//  xal
//
//  Created by Thomas Pelaia on 5/2/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;


/** Interface for qualifying objects. */
public interface Qualifier {
	/** 
	* Determine if the specified object is a match to this qualifier's criteria. 
	* @param object the object to test for matching
	* @return true if the object matches the criteria and false if not.
	*/
	public boolean matches( final Object object );	
}
