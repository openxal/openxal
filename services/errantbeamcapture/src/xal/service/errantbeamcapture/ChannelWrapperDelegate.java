//
// ChannelWrapperDelegate.java
// xal
//
// Created by Tom Pelaia on 4/11/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.service.errantbeamcapture;

import xal.ca.*;


/** ChannelWrapperDelegate */
public interface ChannelWrapperDelegate {
	/** Indicates that the channel state has changed */
	public void channelStateChanged( final ChannelWrapper channelWrapper, final ChannelTimeRecord record );
}
