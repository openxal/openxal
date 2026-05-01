//
//  KnobsMessageListener.java
//  xal
//
//  Created by Thomas Pelaia on 10/31/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;


/** receiver of application wide messages */
public interface KnobsMessageListener {
	/**
	 * Post an application wide message from the source
	 * @param source The source of the message
	 * @param message The message posted
	 */
	public void postMessage( Object source, String message );
	
	
	/**
	 * Post an application wide error message from the source
	 * @param source The source of the message
	 * @param message The message posted
	 */
	public void postErrorMessage( Object source, String message );	
}
