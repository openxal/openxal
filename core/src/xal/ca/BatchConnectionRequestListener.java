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
	/** event indicating that the batch request is complete */
	public void batchConnectionRequestCompleted( BatchConnectionRequest request, int connectedCount, int disconnectedCount, int exceptionCount );
	
	/** event indicating that an exception has been thrown for a channel */
	public void connectionExceptionInBatch( BatchConnectionRequest request, Channel channel, Exception exception );
	
	/** event indicating that a connection change has occurred for a channel */
	public void connectionChangeInBatch( BatchConnectionRequest request, Channel channel, boolean connected );
}
