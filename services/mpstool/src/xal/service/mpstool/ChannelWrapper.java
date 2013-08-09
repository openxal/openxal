/*
 * ChannelWrapper.java
 *
 * Created on Wed Dec 03 17:11:08 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.mpstool;

import xal.ca.*;
import xal.ca.correlator.*;
import xal.tools.correlator.*;


/**
 * ChannelWrapper is a wrapper for a Channel that handles connecting to the channel and
 * registering it with the correlator. 
 *
 * @author  tap
 */
public class ChannelWrapper {
	/** The channel to wrap */
	protected Channel _channel;
	
	
	/**
	* ChannelWrapper constructor
	* @param pv The PV for which to wrap.
	*/
	public ChannelWrapper( final String pv ) {
		_channel = ChannelFactory.defaultFactory().getChannel( pv );
	}
	
	
	/**
	 * Add the specified listener as a listener of connection events for the wrapped channel
	 * @param listener The listener of connection events.
	 */
	public void addConnectionListener( final ConnectionListener listener ) {
		_channel.addConnectionListener( listener );
	}
	
	
	/**
	 * Remove the specified listener from receiving connection events from the wrapped channel
	 * @param listener The listener to remove
	 */
	public void removeConnectionListener( final ConnectionListener listener ) {
		_channel.removeConnectionListener( listener );
	}
	
	
	/**
	* Get the PV for the channel being wrapped.
	* @return the PV
	*/
	public String getPV() {
		return _channel.channelName();
	}
	
	
	/**
	* Get the wrapped channel.
	* @return the wrapped channel
	*/
	public Channel getChannel() {
		return _channel;
	}
	
	
	/**
	 * Determine if the channel is connected.
	 * @return true if the channel is connected and false if not.
	 */
	public boolean isConnected() {
		return _channel.isConnected();
	}
	
	
	/**
	 * Request that the wrapped channel be connected.  Connections are made in the background
	 * so this method returns immediately upon making the request.  The connection will be
	 * made in the future as soon as possible.  A connection event will be sent to registered
	 * connection listeners when the connection has been established.
	 */
	public void requestConnection() {		
		_channel.requestConnection();
	}
}


