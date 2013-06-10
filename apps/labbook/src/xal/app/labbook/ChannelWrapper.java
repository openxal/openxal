//
//  ChannelWrapper.java
//  xal
//
//  Created by Pelaia II, Tom on 9/28/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import xal.ca.*;


/** wrap a channel so that its value may be fetched */
abstract public class ChannelWrapper {
	/** the channel */
	final protected Channel CHANNEL;
	
	/** the title for the channel */
	final protected String TITLE;
	
	
	/** Constructor */
	protected ChannelWrapper( final String title, final Channel channel ) throws Exception {
		TITLE = title;
		CHANNEL = channel;
		CHANNEL.requestConnection();
	}
	
	
	/** Get the title */
	public String getTitle() {
		return TITLE;
	}
	
	
	/** Get the channel */
	public Channel getChannel() {
		return CHANNEL;
	}
	
	
	/** determine if the channel is connected */
	public boolean isConnected() {
		return CHANNEL.isConnected();
	}
	
	
	/** get the latest value as a string */
	abstract public String getValueAsString();	
	
	
	/** get a string channel wrapper instance */
	static public ChannelWrapper getStringChannelWrapper( final String title, final Channel channel ) {
		try {
			return new StringChannelWrapper( title, channel );
		}
		catch( Exception exception ) {
			return null;
		}
	}
}



/** string channel wrapper */
class StringChannelWrapper extends ChannelWrapper {
	/** Constructor */
	public StringChannelWrapper( final String title, final Channel channel ) throws Exception {
		super( title, channel );
	}
	
	
	/** get the latest value as a string */
	public String getValueAsString() {
		try {
			return CHANNEL.isConnected() ? CHANNEL.getValString() : null;
		}
		catch( Exception exception ) {
			return null;
		}
	}		
}
