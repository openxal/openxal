//
// EventFlagChannel.java
// XAL Active
//
// Created by Tom Pelaia on 3/21/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machinerecorder;


/** Represents a channel in the context of a event flagging */
public class EventFlagChannel extends ContextualChannel {
	/** Constructor */
    public EventFlagChannel( final ChannelWrapper wrapper, final boolean enableEventFlagging ) {
		super( wrapper, enableEventFlagging );
    }
	
	
	/** Determine whether the channel should be plotted */
	public boolean getEnableEventFlagging() {
		return super.getEnabled();
	}
	
	
	/** Set whether the channel should be plotted */
	public void setEnableEventFlagging( final boolean shouldFlagEvents ) {
		super.setEnabled( shouldFlagEvents );
	}
}
