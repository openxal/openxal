//
// ServiceState.java
// 
//
// Created by Tom Pelaia on 9/28/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.service;


/** Provides methods for getting information and controlling the client side of the service through the proxy */
public interface ServiceState {
    /**
     * Get the name of the remote service.
     * @return The name of the remote service.
     */
    public String getServiceName();

	
    /**
     * Get the host name of the remote service.
     * @return The host name of the remote service.
     */
    public String getServiceHost();

	
    /**
     * Get the port of the remote service.
     * @return The port of the remote service.
     */
    public int getServicePort();


	/** dispose of this proxy's resources */
	public void disposeServiceResources();
}
