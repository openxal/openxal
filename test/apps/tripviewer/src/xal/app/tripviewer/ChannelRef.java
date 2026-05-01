//
//  ChannelRef.java
//  xal
//
//  Created by Thomas Pelaia on 8/11/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.tripviewer;

import xal.service.tripmonitor.*;


/** reference to a channel on the service */
public class ChannelRef {
	/** the process variable */
	final protected String PV;
	
	/** connection status indicator */
	final protected boolean IS_CONNECTED;
	
	/** label of the PV with color indicating connection status */
	final protected String LABEL;
	
	
	/** Constructor */
	public ChannelRef( final String pv, final boolean isConnected ) {
		PV = pv;
		IS_CONNECTED = isConnected;
		LABEL = generateLabel();
	}
	
	
	/** get the channel reference from a channel record */
	static public ChannelRef getInstanceFromInfoRecord( final java.util.HashMap<String, Object> record ) {
		final String pv = (String)record.get( TripMonitorPortal.PV_KEY );
		final boolean isConnected = ((Boolean)record.get( TripMonitorPortal.CHANNEL_CONNECTION_KEY )).booleanValue();
		return new ChannelRef( pv, isConnected );
	}
	
	
	/** get the PV */
	public String getPV() {
		return PV;
	}
	
	
	/** determine the channel connection status */
	public boolean isConnected() {
		return IS_CONNECTED;
	}
	
	
	/** get a label appropriate to channel connection status */
	public String getLabel() {
		return LABEL;
	}
	
	
	/** get a string representation of the channel reference */
	public String toString() {
		return getLabel();
	}
	
	
	/** generate the label */
	protected String generateLabel() {
		if ( IS_CONNECTED ) {
			return "<html><body style=\"color: green;\">" + PV + "</body></html>";
		}
		else {
			return "<html><body style=\"color: red;\">" + PV + "</body></html>";
		}
	}
	
	
	/** determine if this channel referenc equals another */
	public boolean equals( final Object object ) {
		if ( object != null && object instanceof ChannelRef ) {
			final ChannelRef channelRef = (ChannelRef)object;
			return PV.equals( channelRef.PV ) && IS_CONNECTED == channelRef.IS_CONNECTED;
		}
		else {
			return false;
		}
	}


	/** override hashCode() as required to be consistent with equals() */
	public int hashCode() {
		return PV.hashCode();
	}
}
