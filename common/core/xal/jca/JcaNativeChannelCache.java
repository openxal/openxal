//
//  JcaNativeChannelCache.java
//  xal
//
//  Created by Thomas Pelaia on 9/19/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.jca;

import java.util.*;

import gov.aps.jca.Channel;
import gov.aps.jca.Context;


/** Cache JCA native channels for reuse among several XAL channels.  JCA won't allow us to create more than one channel for the same PV signal. */
class JcaNativeChannelCache {
	/** JCA System */
	final protected JcaSystem _jcaSystem;
	
	/** map of native channel's keyed by PV signal name */
	final protected Map _channelMap;
	
	
	/** Constructor */
	public JcaNativeChannelCache( final JcaSystem jcaSystem ) {
		_jcaSystem = jcaSystem;
		_channelMap = new HashMap();
	}
	
	
	/**
	 * Get an existing channel if available or else, create a new channel for specified signal name.
	 * @param signalName the PV signal name.
	 * @return the native JCA channel corresponding to the specified PV signal
	 * @throws gov.aps.jca.CAException if the channel fails to be created
	 */
	public Channel getChannel( final String signalName ) throws gov.aps.jca.CAException {
		Channel channel;
		
		synchronized( _channelMap ) {
			channel = (Channel)_channelMap.get( signalName );
			
			if ( channel == null ) {
				channel = _jcaSystem.getJcaContext().createChannel( signalName );
				_channelMap.put( signalName, channel );
			}
		}
		
		return channel;
	}
}
