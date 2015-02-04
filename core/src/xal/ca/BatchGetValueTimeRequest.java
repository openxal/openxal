//
// BatchGetValueTimeRequest.java
// xal
//
// Created by Tom Pelaia on 4/9/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.ca;

import java.util.*;


/** batch of CA Get requests including value, status, severity and timestamp with convenient batch operations */
public class BatchGetValueTimeRequest extends AbstractBatchGetRequest<ChannelTimeRecord> {
	/** request handler */
	final protected RequestHandler REQUEST_HANDLER;
	
	
	/** 
	 * Primary Constructor 
	 * @param channels for which to request the value, status and timestamp
	 */
	public BatchGetValueTimeRequest( final Collection<Channel> channels ) {
		super( channels );
		
		REQUEST_HANDLER = new RequestHandler();
	}
	
	
	/** Constructor */
	public BatchGetValueTimeRequest() {
		this( Collections.<Channel>emptySet() );
	}
	
	
	/** request to get the data for the channel */
	protected void requestChannelData( final Channel channel ) throws Exception {
		channel.getValueTimeCallback( REQUEST_HANDLER, false );
	}
	
	
	/** handle get request events */
	protected class RequestHandler implements IEventSinkValTime {
		public void eventValue( final ChannelTimeRecord record, final Channel channel ) {
			processRecordEvent( channel, record );
		}
	}
}
