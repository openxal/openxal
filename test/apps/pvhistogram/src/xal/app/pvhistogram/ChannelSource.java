//
//  ChannelSource.java
//  xal
//
//  Created by Tom Pelaia on 2/12/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.app.pvhistogram;

import xal.tools.data.*;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.smf.Accelerator;
import xal.smf.NodeChannelRef;


/** channel source for a direct PV or a node channel reference */
abstract class ChannelSource {
	/** factory method to get a channel source for the specified channel */
	static public ChannelSource getInstance( final Channel channel ) {
		return new DirectChannelSource( channel );
	}


	/** factory method to get a channel source for the specified channel PV */
	static public ChannelSource getInstance( final String pv ) {
		return new DirectChannelSource( pv );
	}
	
	
	/** factory method to get a channel source for the specified node channel reference */
	static public ChannelSource getInstance( final NodeChannelRef channelRef ) {
		return new NodeChannelSource( channelRef );
	}	
	
	
	/** Get the source for the specified adaptor */
	static public ChannelSource getChannelSource( final DataAdaptor adaptor, final Accelerator accelerator ) {
		if ( adaptor.hasAttribute( "pv" ) ) {
			final String pv = adaptor.stringValue( "pv" );
			if ( pv != null ) {
				return new DirectChannelSource( pv );
			}
			else {
				return null;
			}
		}
		else if ( adaptor.hasAttribute( "channelRef" ) ) {
			final String channelRefID = adaptor.stringValue( "channelRef" );
			return new NodeChannelSource( accelerator, channelRefID );
		}
		else {
			return null;
		}
	}
	
	
	/** get the channel */
	abstract public Channel getChannel();
	
	
	/** write the channel source to the data adaptor */
	abstract public void write( final DataAdaptor adaptor );
}	



/** channel source for a direct PV */
class DirectChannelSource extends ChannelSource {
	/** the channel */
	final private Channel CHANNEL;
	
	
	/** Primary Constructor */
	public DirectChannelSource( final Channel channel ) {
		CHANNEL = channel;
	}
	
	
	/** Constructor */
	public DirectChannelSource( final String pv ) {
		this( ChannelFactory.defaultFactory().getChannel( pv ) );
	}
	
	
	/** get the channel */
	public Channel getChannel() {
		return CHANNEL;
	}	
	
	
	/** write the channel source to the data adaptor */
	public void write( final DataAdaptor adaptor ) {
		if ( CHANNEL != null ) {
			adaptor.setValue( "pv", CHANNEL.channelName() );
		}
	}
}



/** channel source for a node channel reference */
class NodeChannelSource extends ChannelSource {
	/** node channel reference */
	final private NodeChannelRef NODE_CHANNEL_REF;
	
	
	/** Constructor */
	public NodeChannelSource( final NodeChannelRef channelRef ) {
		NODE_CHANNEL_REF = channelRef;
	}
	
	
	/** Constructor */
	public NodeChannelSource( final Accelerator accelerator, final String channelRefID ) {
		this( NodeChannelRef.getInstance( accelerator, channelRefID ) );
	}
		
	
	/** get the channel */
	public Channel getChannel() {
		return NODE_CHANNEL_REF.getChannel();
	}
	
	
	/** write the channel source to the data adaptor */
	public void write( final DataAdaptor adaptor ) {
		if ( NODE_CHANNEL_REF != null ) {
			adaptor.setValue( "channelRef", NODE_CHANNEL_REF.toString() );
		}
	}	
}