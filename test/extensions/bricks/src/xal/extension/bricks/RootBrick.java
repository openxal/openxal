//
//  WindowGroupNode.java
//  xal
//
//  Created by Thomas Pelaia on 7/10/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.beans.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import xal.tools.data.*;


/** root brick to which windows are added */
public class RootBrick extends Brick implements ViewNodeContainer, DataListener {
	/** data label */
	final public static String DATA_LABEL = "RootBrick";
	
	/** list of window nodes */
	final List<ViewNode> WINDOW_NODES;
	
	
	/** Constructor */
	public RootBrick() {
		WINDOW_NODES = new ArrayList<ViewNode>();
	}
	
	
	/** dispose of all windows */
	public void disposeAllWindows() {
		for ( final ViewNode node : WINDOW_NODES ) {
			final Component window = node.getView();
			if ( window instanceof Window ) {
				((Window)window).dispose();
			}
		}
	}
	
	
	/**
	 * Determine if the brick can add the specified view
	 * @return true if it can add the specified view and false if not
	 */
	public boolean canAdd( final BeanProxy<?> beanProxy ) {
		if ( beanProxy instanceof ViewProxy ) {
			return ((ViewProxy)beanProxy).isWindow();
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * Get the label
	 * @return the label for this brick
	 */
	public String toString() {
		return "windows";
	}
	
	
	/**
	 * Add the views to this node
	 * @param beanProxies the views to add to this node
	 */
	@SuppressWarnings( "unchecked" )	// must cast bean proxy to view proxy
	public void add( final List<BeanProxy<?>> beanProxies ) {
		final List<BeanNode<?>> nodes = new ArrayList<BeanNode<?>>( beanProxies.size() );
		for ( final BeanProxy<?> beanProxy : beanProxies ) {
			if ( beanProxy instanceof ViewProxy ) {
				final ViewNode node = new ViewNode( (ViewProxy<Component>)beanProxy );
				WINDOW_NODES.add( node );
				node.addBrickListener( this );
				nodes.add( node );
				TREE_NODE.add( node.getTreeNode() );
				((Window)node.getView()).setVisible( true );
			}
		}
		EVENT_PROXY.nodesAdded( this, this, nodes );
		EVENT_PROXY.treeNeedsRefresh( this, this );
	}
	
	
	/**
	 * Get the tree index offset from the view index
	 * @return the tree index offset
	 */
	public int getTreeIndexOffsetFromViewIndex() {
		return 0;
	}
	
	
	/** move the specified nodes down */
	public void moveDownNodes( final List<BeanNode<?>> nodes ) {}
	
	
	/** move the specified nodes up */
	public void moveUpNodes( final List<BeanNode<?>> nodes ) {}
	
	
	/**
	 * Insert the bean node in this node beginning at the specified index
	 * @param node the node to insert in this node
	 * @param viewIndex the initial index at which to begin inserting the nodes
	 */
	public void insertViewNode( final ViewNode node, final int viewIndex ) {
		WINDOW_NODES.add( viewIndex, node );
		node.addBrickListener( this );
		TREE_NODE.insert( node.getTreeNode(), viewIndex );
		((Window)node.getView()).setVisible( true );
		EVENT_PROXY.treeNeedsRefresh( this, this );
	}
	
	
	/**
	 * Insert the views in this node beginning at the specified index
	 * @param viewProxies the views to add to this node
	 */
	public void insertSiblings( final List<BeanProxy<?>> viewProxies ) {}
	
	
	/**
	 * Add the views nodes to this node
	 * @param originalNodes the nodes to add to this node
	 */
	public void addNodes( final List<BeanNode<?>> originalNodes ) {
		final List<BeanNode<?>> nodes = new ArrayList<BeanNode<?>>( originalNodes.size() );
		for ( final BeanNode<?> originalNode : originalNodes ) {
			if ( originalNode instanceof ViewNode ) {
				final ViewNode node = new ViewNode( (ViewNode)originalNode );
				WINDOW_NODES.add( node );
				node.addBrickListener( this );
				nodes.add( node );
				TREE_NODE.add( node.getTreeNode() );
			}
		}
		EVENT_PROXY.nodesAdded( this, this, nodes );
		EVENT_PROXY.treeNeedsRefresh( this, this );
	}
	
	
	/**
	 * Insert the view nodes in this node beginning at the specified index
	 * @param originalNodes the nodes to add to this node
	 */
	public void insertSiblingNodes( final List<BeanNode<?>> originalNodes ) {}
	
	
	/**
	 * Remove the view node from this container
	 * @param node the node to remove
	 */
	public void removeNode( final BeanNode<?> node ) {
		final List<BeanNode<?>> nodes = Collections.<BeanNode<?>>singletonList( node );
		removeNodes( nodes );
	}
	
	
	/**
	 * Remove the view nodes from this container
	 * @param nodes the nodes to remove
	 */
	public void removeNodes( final List<BeanNode<?>> nodes ) {
		for ( final BeanNode<?> node : nodes ) {
			if ( node instanceof ViewNode ) {
				final ViewNode viewNode = (ViewNode)node;
				viewNode.removeBrickListener( this );
				WINDOW_NODES.remove( viewNode );
				TREE_NODE.remove( viewNode.getTreeNode() );
				final Window window = (Window)viewNode.getView();
				window.dispose();
			}
		}
		EVENT_PROXY.nodesRemoved( this, this, nodes );
		EVENT_PROXY.treeNeedsRefresh( this, this );
	}
	
	
	/** Remove this brick from its parent */
	public void removeFromParent() {}
	
	
    /** 
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    public String dataLabel() {
		return DATA_LABEL;
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		final List<DataAdaptor> nodeAdaptors = adaptor.childAdaptors( ViewNode.DATA_LABEL );
		final List<BeanNode<?>> nodes = new ArrayList<BeanNode<?>>( nodeAdaptors.size() );
		for ( final DataAdaptor nodeAdaptor : nodeAdaptors ) {
			nodeAdaptor.setValue( "contextURL", adaptor.stringValue( "contextURL" ) );
			nodes.add( ViewNode.getInstance( nodeAdaptor ) );
		}
		addNodes( nodes );
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
        adaptor.writeNodes( WINDOW_NODES );
	}
	
	
	/**
	 * Handle the event in which nodes have been added to a container
	 * @param source the source of the event
	 * @param container the node to which nodes have been added
	 * @param nodes the nodes which have been added
	 */
	public void nodesAdded( final Object source, final Brick container, final List<BeanNode<?>> nodes ) {
		EVENT_PROXY.nodesAdded( this, container, nodes );
	}
	
	
	/**
	 * Handle the event in which nodes have been removed from a container
	 * @param source the source of the event
	 * @param container the node from which nodes have been removed
	 * @param nodes the nodes which have been removed
	 */
	public void nodesRemoved( final Object source, final Brick container, final List<BeanNode<?>> nodes ) {
		EVENT_PROXY.nodesRemoved( this, container, nodes );
	}
	
	
	/**
	 * Handle the event in which a bean's property has been changed
	 * @param node the node whose property has changed
	 * @param propertyDescriptor the property which has changed
	 * @param value the new value
	 */
	public void propertyChanged( final BeanNode<?> node, final PropertyDescriptor propertyDescriptor, final Object value ) {
		EVENT_PROXY.propertyChanged( node, propertyDescriptor, value ); 
	}
	
	
	/**
	 * Handle the event in which a brick's tree path needs refresh
	 * @param source the source of the event
	 * @param brick the brick at which the refresh needs to be done
	 */
	public void treeNeedsRefresh( final Object source, final Brick brick ) {
		EVENT_PROXY.treeNeedsRefresh( this, brick );
	}
}
