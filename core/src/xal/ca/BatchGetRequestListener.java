//
//  BatchGetRequestListener.java
//  xal
//
//  Created by Tom Pelaia on 3/6/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.ca;


/** interface for listeners of batch request events */
public interface BatchGetRequestListener<RecordType extends ChannelRecord> {
	/** 
	 * Event indicating that the batch request is complete 
	 * @param request in which the event occurred
	 * @param recordCount number of records completed
	 * @param exceptionCount number of exceptions
	 */
	public void batchRequestCompleted( AbstractBatchGetRequest<RecordType> request, int recordCount, int exceptionCount );
	
	/** 
	 * Event indicating that an exception has been thrown for a channel 
	 * @param request in which the event occurred
	 * @param channel for which the exception occured
	 * @param exception that occurred
	 */
	public void exceptionInBatch( AbstractBatchGetRequest<RecordType> request, Channel channel, Exception exception );
	
	/** 
	 * event indicating that a get event has been completed for a channel 
	 * @param request in which the event occurred
	 * @param channel for which the record was received
	 * @param record which was received
	 */
	public void recordReceivedInBatch( AbstractBatchGetRequest<RecordType> request, Channel channel, RecordType record );
}
