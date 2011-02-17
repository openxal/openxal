//
//  BatchGetRequestListener.java
//  xal
//
//  Created by Tom Pelaia on 3/6/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.ca;


/** interface for listeners of batch request events */
public interface BatchGetRequestListener {
	/** event indicating that the batch request is complete */
	public void batchRequestCompleted( BatchGetRequest request, int recordCount, int exceptionCount );
	
	/** event indicating that an exception has been thrown for a channel */
	public void exceptionInBatch( BatchGetRequest request, Channel channel, Exception exception );
	
	/** event indicating that a get event has been completed for a channel */
	public void recordReceivedInBatch( BatchGetRequest request, Channel channel, ChannelRecord record );
}
