//
// BatchConnectionRequestListener.java
// xal
//
// Created by Tom Pelaia on 6/14/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.ca;


/** BatchConnectionRequestListener */
public interface BatchConnectionRequestListener {
	/** 
	 * Event indicating that the batch request is complete 
	 * @param request	request for which the connection completed
	 * @param connectedCount	number of channels connected
	 * @param disconnectedCount	number of channels disconnected
	 * @param exceptionCount	number of channels for which there were connection exceptions
	 */
	public void batchConnectionRequestCompleted( BatchConnectionRequest request, int connectedCount, int disconnectedCount, int exceptionCount );
	
	/** 
	 * Event indicating that an exception has been thrown for a channel 
	 * @param request in which the exception occured
	 * @param channel for which the exception occured
	 * @param exception the exception thrown while attempting to connect
	 */
	public void connectionExceptionInBatch( BatchConnectionRequest request, Channel channel, Exception exception );
	
	/** 
	 * Event indicating that a connection change has occurred for a channel
	 * @param request in which the connection changed
	 * @param channel for which the connection changed
	 * @param connected status of the connection (true for connected and false for disconnected)
	 */
	public void connectionChangeInBatch( BatchConnectionRequest request, Channel channel, boolean connected );
}
