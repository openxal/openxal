//
// MachineState.java
// xal
//
// Created by Tom Pelaia on 3/30/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.service.errantbeamcapture;

import java.util.*;

import xal.ca.*;


/** Store the machine state */
public class MachineState extends java.lang.Object {
	/** table of records keyed by channel ID */
	final private Map<String,ChannelTimeRecord> RECORD_TABLE;
	
	
	/** Constructor */
    public MachineState( final List<ChannelWrapper> channelWrappers ) {
		RECORD_TABLE = new HashMap<String,ChannelTimeRecord>();
		
		for ( final ChannelWrapper channelWrapper : channelWrappers ) {
			final ChannelTimeRecord record = channelWrapper.getLatestRecord();
			final Channel channel = channelWrapper.getChannel();
			RECORD_TABLE.put( channel.getId(), record );
		}
    }
	
	
	/** Get the record for the specified channel ID */
	public ChannelTimeRecord getRecordForChannelID( final String channelID ) {
		return RECORD_TABLE.get( channelID );
	}
	
	
	/** Get the string representation of this machine state */
	public String toString() {
		return RECORD_TABLE.toString();
	}
}
