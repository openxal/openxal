//
//  HostGenerator.java
//  xal
//
//  Created by Thomas Pelaia on 9/8/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;


/** interface for host generators */
public interface HostGenerator {
	/** get the next host */
	public String nextHost();
	
	/** get the type of the host generator */
	public String getType();
}
