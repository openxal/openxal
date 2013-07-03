//
//  ChannelListModel.java
//  xal
//
//  Created by Thomas Pelaia on 8/11/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.tripviewer;

import javax.swing.*;
import java.util.*;


/** list model for displaying channels and their status indicated by color */
public class ChannelListModel extends AbstractListModel<Object> {
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
	/** the channel references to display */
	protected List<ChannelRef> _channelRefs;
	
	/** lock for synchronizing channel refs */
	final protected Object LOCK;
	
	
	/** Constructor */
	public ChannelListModel() {
		LOCK = new Object();
		_channelRefs = new ArrayList<ChannelRef>();
	}
	
	
	/** set the channel references */
	public void setChannelRefs( final List<ChannelRef> channelRefs ) {
		synchronized( LOCK ) {
			// only update the list if the data has actually changed
			if ( !_channelRefs.equals( channelRefs ) ) {
				_channelRefs = channelRefs;
			}
			else {
				return;
			}
		}
		
		fireContentsChanged( this, 0, channelRefs.size() );
	}
	
	
	/** get the element at the specified index */
	public Object getElementAt( final int index ) {
		synchronized( LOCK ) {
			if ( _channelRefs.size() > index ) {
				return _channelRefs.get( index );
			}
			else {
				return "";
			}
		}
	}
	
	
	/** get the size */
	public int getSize() {
		synchronized( LOCK ) {
			return _channelRefs.size();
		}
	}
}
