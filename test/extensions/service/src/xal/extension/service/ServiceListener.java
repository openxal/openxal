/*
 * ServiceListener.java
 *
 * Created on Tue Oct 07 11:08:45 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.extension.service;


/**
 * ServiceListener is the interface for listeners of service availability and removal.
 *
 * @author  tap
 */
public interface ServiceListener {
	/**
	 * This method is called when a new service has been added.
	 * @param directory identifies the directory sending this notification
	 * @param serviceRef The service reference of service provided.
	 */
	public void serviceAdded(ServiceDirectory directory, ServiceRef serviceRef);
	
	
	/**
	 * This method is called when a service has been removed.
	 * @param directory identifies the directory sending this notification
	 * @param type The type of the removed service.
	 * @param name The name of the removed service.
	 */
	public void serviceRemoved(ServiceDirectory directory, String type, String name);
}

