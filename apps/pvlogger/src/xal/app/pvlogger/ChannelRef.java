/*
 * ChannelRef.java
 *
 * Created on Thu Mar 18 09:01:49 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogger;


/**
 * ChannelRef is a reference to a channel on a remote service.  It is used to represent the state 
 * of the remote channel.  ChannelRef instances are immutable.
 *
 * @author  tap
 */
public class ChannelRef implements Comparable<ChannelRef> {
	/** The signal name */
	final protected String _pv;
	
	/** The connection state */
	final protected boolean _connected;
	
	
	/**
	 * Primary constructor
	 * @param pv The signal name
	 * @param connected The connection state
	 */
	public ChannelRef(String pv, boolean connected) {
		_pv = pv;
		_connected = connected;
	}
	
	
	/**
	 * Constructor 
	 * @param pv The signal name
	 * @param connected The connection state
	 */
	public ChannelRef(String pv, Boolean connected) {
		this(pv, connected.booleanValue());
	}
	
	
	/**
	 * Get the signal name
	 * @return the signal name
	 */
	public String getPV() {
		return _pv;
	}
	
	
	/**
	 * Get the channel's connection status
	 * @return true if the remote channel is connected and false if not
	 */
	public boolean isConnected() {
		return _connected;
	}
	
	
	/**
	 * Compare this channel reference with the one specified.
	 * 
	 * @param object the channel reference against which to compare this channel reference
	 * @return negative, zero or positive for this less than, equal or greater than the specified object
	 */
	public int compareTo( final ChannelRef ref ) {
		return _pv.compareTo( ref._pv );
	}
	
	
	/**
	 * Get a string representation of the channel
	 * @return the signal name
	 */
	public String toString() {
		return _pv;
	}
}

