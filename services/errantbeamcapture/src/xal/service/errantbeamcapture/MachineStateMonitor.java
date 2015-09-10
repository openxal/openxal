//
// MachineStateMonitor.java
// xal
//
// Created by Tom Pelaia on 3/30/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.service.errantbeamcapture;

import java.util.*;

import xal.ca.*;


/** MachineMonitor */
public class MachineStateMonitor implements ChannelWrapperDelegate {
	/** list of channel wrappers managed by this monitor */
	final private List<ChannelWrapper> CHANNEL_WRAPPERS;
		
	/** delegate to handle machine state change events */
	private MachineStateChangeListener _stateChangeListener;
	
	/** latest timestamp in seconds */
	private Timestamp _latestTimestamp;
	
	/** latest state */
	private boolean _latestState;
	
	
	/** Constructor */
    public MachineStateMonitor() {
		CHANNEL_WRAPPERS = new ArrayList<ChannelWrapper>();
		_latestState = true;
    }
	
	
	/** set the state change listener */
	public void setStateChangeListener( final MachineStateChangeListener changeListener ) {
		_stateChangeListener = changeListener;
	}
	
	
	/** Remove all channels */
	public void clear() {
		for ( final ChannelWrapper channelWrapper : CHANNEL_WRAPPERS ) {
			channelWrapper.dispose();
		}
		CHANNEL_WRAPPERS.clear();
	}
	
	
	/** Add the specified channel for monitoring */
	public void addChannel( final Channel channel, final double goodStateValue ) {
		final ChannelWrapper channelWrapper = new ChannelWrapper( channel, goodStateValue, this );
		CHANNEL_WRAPPERS.add( channelWrapper );
	}
	
	
	/** Determine whether all channels are in a good state (all channels must be in a good state for the machine to be in a good state) */
	public boolean isGoodState() {
		// determine whether any valid channels are tripped; if not the machine state is assumed good
		for ( final ChannelWrapper wrapper : CHANNEL_WRAPPERS ) {
			if ( wrapper.isValid() && !wrapper.isGood() )  return false;
		}
		
		return true;
	}
	
	
	/** Get the current machine state */
	public MachineState getCurrentMachineState() {
		final List<ChannelWrapper> wrappers = new ArrayList<ChannelWrapper>( CHANNEL_WRAPPERS );
		return new MachineState( wrappers );
	}
	
	
	/** Indicates that the channel state has changed */
	public void channelStateChanged( final ChannelWrapper channelWrapper, final ChannelTimeRecord record ) {
		final Timestamp timestamp = record.getTimestamp();
		final boolean state = isGoodState();
		if ( state != _latestState ) {	// captured a distinct machine state event
			_latestState = state;
			_latestTimestamp = timestamp;
			if ( _stateChangeListener != null ) {
				_stateChangeListener.machineStateChanged( this, state, timestamp );
			}			
		}
	}
}
