//
// ChannelWrapper.java: Source file for 'ChannelWrapper'
// Project xal
//
// Created by Tom Pelaia on 6/16/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machinerecorder;

import xal.smf.NodeChannelRef;
import xal.smf.AcceleratorSeq;
import xal.ca.*;


/** ChannelWrapper */
public class ChannelWrapper {
	/** channel source */
	private ChannelSource _channelSource;
	
	
	/** Constructor */
	private ChannelWrapper( final ChannelSource channelSource ) {
		_channelSource = channelSource;
	}
	
	
	/** Construct a channel wrapper with a node channel reference */
	static public ChannelWrapper getInstanceForRef( final NodeChannelRef channelRef ) {
		return new ChannelWrapper( getChannelSourceForRef( channelRef ) );
	}
	
	
	/** Construct a channel wrapper for the process variable */
	static public ChannelWrapper getInstanceForPV( final String pv ) {
		return new ChannelWrapper( getChannelSourceForPV( pv ) );
	}
	
	
	/** Set the channel reference as the channel source */
	private void setChannelSource( final ChannelSource channelSource ) {
		_channelSource = channelSource;
	}
	
	
	/** get the channel source for the specified node channel reference */
	static private ChannelSource getChannelSourceForRef( final NodeChannelRef channelRef ) {
		return new NodeChannelSource( channelRef );
	}
	
	
	/** get the channel source for the specified process variable */
	static private ChannelSource getChannelSourceForPV( final String pv ) {
		return new RawChannelSource( pv );
	}
	
	
	/** Set the channel source for the node channel reference */
	public void setChannelSourceForRef( final NodeChannelRef channelRef ) {
		setChannelSource( getChannelSourceForRef( channelRef ) );
	}
	
	
	/** Set the channel source for the process variable */
	public void setChannelSourceForPV( final String pv ) {
		setChannelSource( getChannelSourceForPV( pv ) );
	}
	
	
	/** Get the wrapped channel */
	public Channel getChannel() {
		return _channelSource != null ? _channelSource.getChannel() : null;
	}
	
	
	/** Get the ID identifying the wrapped channel */
	public String getChannelID() {
		final Channel channel = getChannel();
		return channel != null ? channel.getId() : "";
	}
	
	
	/** Get the code for this wrapper which identifies the channel source */
	public String getChannelCode() {
		return _channelSource != null ? _channelSource.getLabel() : "";
	}
	
	
	/** Set the channel code for this wrapper */
	public void setChannelCode( final String channelCode ) {
		setChannelSourceForPV( channelCode );
	}
	
	
	/** Get the position within the sequence */
	public double getPositionIn( final AcceleratorSeq sequence ) {
		return _channelSource != null ? _channelSource.getPositionIn( sequence ) : Double.NaN;
	}
}



/** Source for the channel */
abstract class ChannelSource {
	/** Get the channel */
	abstract public Channel getChannel();
	
	
	/** Get the label */
	abstract public String getLabel();
	
	
	/** Get the position within the sequence */
	abstract public double getPositionIn( final AcceleratorSeq sequence );
}



/** Channel Source for a Node Channel Ref */
class NodeChannelSource extends ChannelSource {
	/** channel reference */
	final private NodeChannelRef CHANNEL_REF;
	
	
	/** Constructor */
	public NodeChannelSource( final NodeChannelRef channelRef ) {
		CHANNEL_REF = channelRef;
	}
	
	
	/** Get the channel */
	public Channel getChannel() {
		return CHANNEL_REF.getChannel();
	}
	
	
	/** Get the label */
	public String getLabel() {
		return CHANNEL_REF.toString();
	}
	
	
	/** Get the position within the sequence */
	public double getPositionIn( final AcceleratorSeq sequence ) {
		return sequence.getPosition( CHANNEL_REF.getNode() );
	}
}



/** Channel Source for a raw PV */
class RawChannelSource extends ChannelSource {
	/** channel */
	final private Channel CHANNEL;
	
	
	/** Constructor */
	public RawChannelSource( final String pv ) {
		CHANNEL = ChannelFactory.defaultFactory().getChannel( pv );
	}
	
	
	/** Get the channel */
	public Channel getChannel() {
		return CHANNEL;
	}
	
	
	/** Get the label */
	public String getLabel() {
		return CHANNEL.getId();
	}
	
	
	/** Get the position within the sequence */
	public double getPositionIn( final AcceleratorSeq sequence ) {
		return Double.NaN;
	}
}
