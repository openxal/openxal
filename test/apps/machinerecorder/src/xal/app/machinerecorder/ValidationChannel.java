//
// ValidationChannel.java
// XAL Active
//
// Created by Tom Pelaia on 3/21/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machinerecorder;


/** Represents a channel in the context of validation */
public class ValidationChannel extends ContextualChannel {
	/** Constructor */
    public ValidationChannel( final ChannelWrapper wrapper, final boolean enableValidation ) {
		super( wrapper, enableValidation );
    }
	
	
	/** Determine whether the channel should be used for validation */
	public boolean getEnableValidation() {
		return super.getEnabled();
	}
	
	
	/** Set whether the channel should be used for validation */
	public void setEnableValidation( final boolean shouldValidate ) {
		super.setEnabled( shouldValidate );
	}
}
