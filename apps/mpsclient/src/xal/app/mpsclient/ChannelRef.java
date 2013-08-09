/*
 *  ChannelRef.java
 *
 *  Created on Mon Mar 15 14:22:26 EST 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.mpsclient;

import java.util.*;


/**
 * ChannelRef is a reference to a channel on a remote service. It is used to represent the
 * state of the remote channel. ChannelRef instances are immutable.
 *
 * @author   tap
 */
public class ChannelRef {
	/** The signal name */
	protected final String _pv;

	/** The connection state */
	protected final boolean _connected;


	/**
	 * Primary constructor
	 *
	 * @param pv         The signal name
	 * @param connected  The connection state
	 */
	public ChannelRef( String pv, boolean connected ) {
		_pv = pv;
		_connected = connected;
	}


	/**
	 * Constructor
	 *
	 * @param pv         The signal name
	 * @param connected  The connection state
	 */
	public ChannelRef( String pv, Boolean connected ) {
		this( pv, connected.booleanValue() );
	}


	/**
	 * Get the signal name
	 *
	 * @return   the signal name
	 */
	public String getPV() {
		return _pv;
	}


	/**
	 * Get the channel's connection status
	 *
	 * @return   true if the remote channel is connected and false if not
	 */
	public boolean isConnected() {
		return _connected;
	}


	/**
	 * Get a string representation of the channel
	 *
	 * @return   the signal name
	 */
	public String toString() {
		return _pv;
	}


	/**
	 * Generate a comparator for channel references that compares channel refs by PV.
	 *
	 * @return   a comparator that compares channel references by PV
	 */
	static public Comparator<ChannelRef> signalComparator() {
		return
			new Comparator<ChannelRef>() {
				public int compare( final ChannelRef ref1, final ChannelRef ref2 ) {
					final String pv1 = ref1._pv;
					final String pv2 = ref2._pv;
					
					return pv1.compareTo( pv2 ); 
				}
				
				public boolean equals( final Object comparator ) {
					return this == comparator;
				}
			};
	}
}

