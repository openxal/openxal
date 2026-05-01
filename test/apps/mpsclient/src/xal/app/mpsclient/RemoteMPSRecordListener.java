/*
 * RequestHandlerListener.java
 *
 * Created on Tue Mar 16 09:47:31 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.mpsclient;

import java.util.*;


/**
 * RequestHandlerListener is the interface implemented by listeners of request handler events.
 *
 * @author  tap
 */
public interface RemoteMPSRecordListener {
	/**
	 * Indicates that MPS channels have been updated.
	 * @param handler The handler sending the event
	 * @param mpsTypeIndex index of the MPS type for which the event applies
	 * @param channelRefs The list of the new ChannelRef instances
	 */
	public void mpsChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, List<ChannelRef> channelRefs);
	
	
	/**
	 * Indicates that Input channels have been updated.
	 * @param handler The handler sending the event
	 * @param mpsTypeIndex index of the MPS type for which the event applies
	 * @param channelRefs The list of the new ChannelRef instances
	 */
	public void inputChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, List<ChannelRef> channelRefs);
	
	
	/**
	 * Indicates that an MPS event has happened.
	 * @param handler The handler sending the event
	 * @param mpsTypeIndex index of the MPS type for which the event applies
	 */
	public void mpsEventsUpdated(RemoteMPSRecord handler, int mpsTypeIndex);
	
	
	/**
	 * Indicates that the handler has checked for new status from the MPS service.
	 * @param handler The handler sending the event.
	 * @param timestamp The timestamp of the latest status check
	 */
	public void lastCheck(RemoteMPSRecord handler, Date timestamp);
}

