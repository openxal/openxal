/*
 * DataKeys.java
 *
 * Created on Tue Feb 17 16:30:56 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.mpsclient;


/**
 * DataKeys is an interface which defines the keys used in the MPS data table of request
 * handlers each of which maps to a remote service.
 *
 * @author  tap
 */
public interface DataKeys {
	/** key for the request remote service ID */
	static final String ID_KEY = "ID";
	
	/** key for the launch time of the remote service */
	static final String LAUNCH_TIME_KEY = "LAUNCH_TIME";
	
	/** key for the host name of the remote service */
	static final String HOST_KEY = "HOST";
	
	/** key for the process ID of the remote service */
	static final String PROCESS_ID_KEY = "PROCESS_ID";
	
	/** key for the timestamp of the last status check of the remote service */
	static final String LAST_CHECK_KEY = "LAST_CHECK";
	
	/** key for status of remote service communication */
	static final String SERVICE_OKAY_KEY = "SERVICE_OKAY";
	
	/** key for retrieving whether the service is loggins statistics */
	static final String LOGS_STATS_KEY = "LOGS_STATS";
}

