//
// ScanChannelMonitorDelegate.java
// xal
//
// Created by Pelaia II, Tom on 1/10/13
// Copyright 2013 ORNL. All rights reserved.
//

package xal.extension.scan;

import xal.ca.ChannelTimeRecord;


/** ScanChannelMonitorDelegate */
interface ScanChannelMonitorDelegate {
	/** Indicates that the channel state has changed */
	public void channelStateChanged( final ScanChannelMonitor monitor, final boolean valid );

	/** Indicates that the channel record has been updated */
	public void channelRecordUpdate( final ScanChannelMonitor monitor, final ChannelTimeRecord record );
}

