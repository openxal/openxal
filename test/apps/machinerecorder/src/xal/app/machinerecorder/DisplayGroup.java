//
// DisplayGroup.java: Source file for 'DisplayGroup'
// Project xal
//
// Created by Tom Pelaia on 6/16/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machinerecorder;

import java.util.*;

import xal.smf.*;


/** DisplayGroup */
public class DisplayGroup {
    /** channel references */
    final private List<NodeChannelRef> CHANNEL_REFS;
    // todo: replace channel refs with channel wrappers
    
    
    /** name of the display group */
    private String _name;
    
    /** accelerator sequence for this group */
    private AcceleratorSeq _sequence;
    
    
	/** Constructor */
    public DisplayGroup( final AcceleratorSeq sequence ) {
        this( "Untitled", sequence );
    }
    
    
	/** Primary Constructor */
    public DisplayGroup( final String name, final AcceleratorSeq sequence ) {
        setName( name );
        setSequence( sequence );
        
        CHANNEL_REFS = new ArrayList<NodeChannelRef>();
    }
    
    
    /** duplicate the group */
    public DisplayGroup getDuplicate() {
        return new DisplayGroup( this._name + " copy", this._sequence );
    }
    
    
    /** Get this group's accelerator sequence */
    public AcceleratorSeq getSequence() {
        return _sequence;
    }
    
    
    /** Set the accelerator sequence for this group */
    public void setSequence( final AcceleratorSeq sequence ) {
        _sequence = sequence;
    }
    
    
    /** Set the name of this group */
    public void setName( final String name ) {
        _name = name;
    }
    
    
    /** get the name of this group */
    public String getName() {
        return _name;
    }
    
    
    /** get the channel refs */
    public List<NodeChannelRef> getChannelRefs() {
        return CHANNEL_REFS;
    }
    
    
    /** add a channel reference */
    public void addChannelRef( final NodeChannelRef channelRef ) {
        CHANNEL_REFS.add( channelRef );
    }
    
    
    /** add channel references starting at the specified position */
    public void addChannelRefs( final List<NodeChannelRef> channelRefs ) {
        CHANNEL_REFS.addAll( channelRefs );
    }
    
    
    /** add a channel reference at the specified position */
    public void addChannelRefAt( final NodeChannelRef channelRef, final int position ) {
        CHANNEL_REFS.add( position, channelRef );
    }
    
    
    /** add channel references starting at the specified position */
    public void addChannelRefsAt( final List<NodeChannelRef> channelRefs, final int startingPosition ) {
        int position = startingPosition;
        for ( final NodeChannelRef channelRef : channelRefs ) {
            addChannelRefAt( channelRef, position++ );
        }
    }
    
    
    /** remove the channel ref at the specified position */
    public void removeChannelRefAt( final int position ) {
        CHANNEL_REFS.remove( position );
    }
}
