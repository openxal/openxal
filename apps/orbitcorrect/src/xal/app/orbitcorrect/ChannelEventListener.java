/*
 * ChannelEventListener.java
 *
 * Created on Thu Jan 08 09:07:29 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import xal.ca.*;


/**
 * ChannelEventListener is an interface for a listener of channel events.
 *
 * @author  tap
 */
public interface ChannelEventListener {
	/**
	 * The PV's monitored value has changed.
	 * @param channel the channel whose value has changed
	 * @param record The channel time record of the new value
	 */
	public void valueChanged(Channel channel, ChannelTimeRecord record);
	
	
	/**
	 * The channel's connection has changed.  Either it has established a new connection or
	 * the existing connection has dropped.
	 * @param channel The channel whose connection has changed.
	 * @param connected The channel's new connection state
	 */
	public void connectionChanged(Channel channel, boolean connected);
}

