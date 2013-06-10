//
// ContextualChannel.java
// XAL Active
//
// Created by Tom Pelaia on 3/21/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machinerecorder;


/** Represents a channel used within some context */
abstract public class ContextualChannel {
	/** wrapper for the channel */
	final protected ChannelWrapper CHANNEL_WRAPPER;
	
	/** indicates whether this channel is enabled for the context */
	protected boolean _enabled;
	
	
	/** Constructor */
    public ContextualChannel( final ChannelWrapper wrapper, final boolean enable ) {
		CHANNEL_WRAPPER = wrapper;
		_enabled = enable;
    }
	
	
	/** get the channel wrapper */
	public ChannelWrapper getChannelWrapper() {
		return CHANNEL_WRAPPER;
	}
	
	
	/** Determine whether the channel should be used within its context */
	public boolean getEnabled() {
		return _enabled;
	}
	
	
	/** Set whether the channel should be used within its context */
	public void setEnabled( final boolean enable ) {
		_enabled = enable;
	}
}
