/*
 * DataKeys.java
 *
 * Created on Fri Oct 17 10:20:18 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.appviewer;


/**
 * DataKeys is an interface which defines the keys used in the application data table.
 *
 * @author  tap
 */
public interface DataKeys {
	static final String ID_KEY = "ID";
	static final String APPLICATION_KEY = "APPLICATION";
	static final String LAUNCH_TIME_KEY = "LAUNCH_TIME";
	static final String HOST_KEY = "HOST";
	static final String TOTAL_MEMORY_KEY = "TOTAL_MEMORY";
	static final String FREE_MEMORY_KEY = "FREE_MEMORY";
	
	/** key for status of remote service communication */
	static final String SERVICE_OKAY_KEY = "SERVICE_OKAY";
}

