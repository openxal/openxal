//
// UpdateListener.java
// Open XAL
//
// Created by Pelaia II, Tom on 10/2/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.tools;


/** UpdateListener */
public interface UpdateListener extends java.util.EventListener {
	/** called when the source posts an update to this observer */
	public void observedUpdate( final Object source );
}
