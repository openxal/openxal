//
//  NodeChannelSelector.java
//  xal
//
//  Created by Tom Pelaia on 2/5/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.widgets.smf;

import xal.smf.*;
import xal.extension.widgets.swing.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Collections;


/** display a view that allows users to select channels associtiated with nodes */
public class NodeChannelSelector extends KeyValueRecordSelector<NodeChannelRef> {
	/** Primary Constructor */
	protected NodeChannelSelector( final KeyValueFilteredTableModel<NodeChannelRef> tableModel, final JFrame owner, final String title ) {
		super( tableModel, owner, title, "Channel Filter" );
	}
	
	
	/**
	 * Get an instance of a record selector for allowing users to select node channel references from a list in a table.
	 * @param channelRefs node channel references
	 * @param owner the window that owns the dialog window
	 * @param title the title of the dialog window
	 */
	static public NodeChannelSelector getInstance( final List<NodeChannelRef> channelRefs, final JFrame owner, final String title ) {
		final KeyValueFilteredTableModel<NodeChannelRef> tableModel = new KeyValueFilteredTableModel<NodeChannelRef>( channelRefs, "node.id", "node.class.simpleName", "handle" );
		tableModel.setColumnName( "node.id", "Node" );
		tableModel.setColumnName( "node.class.simpleName", "Device Type" );
		tableModel.setColumnName( "handle", "Channel Handle" );
		return new NodeChannelSelector( tableModel, owner, title );
	}
	
	
	/**
	 * Get an instance of a record selector for allowing users to select node channel references from a list in a table.
	 * @param nodes the nodes for which to get the channel references
	 * @param owner the window that owns the dialog window
	 * @param title the title of the dialog window
	 */
	static public NodeChannelSelector getInstanceFromNodes( final List<AcceleratorNode> nodes, final JFrame owner, final String title ) {
		final List<NodeChannelRef> channelRefs = new ArrayList<NodeChannelRef>();
		for ( final AcceleratorNode node : nodes ) {
			final List<String> handles = new ArrayList<String>( node.getHandles() );
			Collections.sort( handles );	// sort the handles alphabetically
			for ( final String handle : handles ) {
				final NodeChannelRef channelRef = new NodeChannelRef( node, handle );
				channelRefs.add( channelRef );
			}
		}
		return getInstance( channelRefs, owner, title );
	}
	
	
	/**
	 * Get an instance of a record selector for allowing users to select node channel references from a list in a table.
	 * @param nodes the nodes for which to get the channel references
	 * @param owner the window that owns the dialog window
	 * @param title the title of the dialog window
	 * @param handles the channel handles for which to get the channel references
	 */
	static public NodeChannelSelector getInstanceFromNodes( final List<AcceleratorNode> nodes, final JFrame owner, final String title, final String ... handles ) {
		final List<NodeChannelRef> channelRefs = new ArrayList<NodeChannelRef>();
		for ( final AcceleratorNode node : nodes ) {
			final Collection<String> nodeHandles = node.getHandles();
			for ( final String handle : handles ) {
				if ( nodeHandles.contains( handle ) ) {
					final NodeChannelRef channelRef = new NodeChannelRef( node, handle );
					channelRefs.add( channelRef );
				}
			}
		}
		return getInstance( channelRefs, owner, title );
	}
}
