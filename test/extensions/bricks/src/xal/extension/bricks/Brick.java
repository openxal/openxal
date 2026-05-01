//
//  Brick.java
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

import xal.tools.messaging.MessageCenter;


/** Base node for holding views and other items */
abstract public class Brick {
	/** the tree node */
	final protected DefaultMutableTreeNode TREE_NODE;
	
	/** message center which dispatches events */
	final protected MessageCenter MESSAGE_CENTER;
	
	/** proxy which forwards events to registered listeners */
	final protected BrickListener EVENT_PROXY;
	
	
	/** Primary Constructor */
	public Brick() {		
		TREE_NODE = new DefaultMutableTreeNode( this );
		TREE_NODE.setAllowsChildren( true );
		
		MESSAGE_CENTER = new MessageCenter( "View Node" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, BrickListener.class );
	}
	
	
	/**
	 * Add a listener of view node events
	 * @param listener the listener to register for receiving events
	 */
	public void addBrickListener( final BrickListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, BrickListener.class );
	}
	
	
	/**
	 * Remove the listener from receiving view node events
	 * @param listener the listener to remove for receiving events from this node
	 */
	public void removeBrickListener( final BrickListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, BrickListener.class );
	}
	
	
	/**
	* Get the tree node.
	 * @return the tree node
	 */
	public DefaultMutableTreeNode getTreeNode() {
		return TREE_NODE;
	}
	
	
	/**
	* Get the parent view node
	 * @return the parent view node
	 */
	public Object getParent() {
		final Object treeParent = TREE_NODE.getParent();
		return treeParent instanceof DefaultMutableTreeNode ? ((DefaultMutableTreeNode)treeParent).getUserObject() : null;
	}
	
	
	/**
	 * Get the containing node
	 * @return the parent view node
	 */
	public Brick getContainingBrick() {
		final Object parent = getParent();
		return parent instanceof Brick ? (Brick)getParent() : null;
	}
	
	
	/**
	 * Determine if this brick is the ancestor of the specified brick
	 * @param brick the brick to test
	 * @return true if this brick is an ancestor of the specified brick and false if not
	 */
	public boolean isAncestorOf( final Brick brick ) {
		Brick ancestor = brick.getContainingBrick();
		while ( ancestor != null ) {
			if ( ancestor.equals( this ) ) {
				return true;
			}
			ancestor = ancestor.getContainingBrick();
		}
		return false;
	}
	
	
	/**
	 * Determine if the brick can add the specified view
	 * @return true if it can add the specified view and false if not
	 */
	abstract public boolean canAdd( final BeanProxy<?> beanProxy );
	
	
	/**
	 * Determine if the brick can be inserted in this view's parent
	 * @return true the view can be a sibling to this view and false if not
	 */
	public boolean canBeSibling( final BeanProxy<?> beanProxy ) {
		final Brick parent = getContainingBrick();
		return parent != null && parent.canAdd( beanProxy );
	}
	
	
	/**
	 * Determine if all views can be sibling views
	 * @return true if all views can be siblings and false if not
	 */
	public boolean canAllBeSiblings( final List<BeanProxy<?>> beanProxies ) {
		for ( final BeanProxy<?> beanProxy : beanProxies ) {
			if ( !canBeSibling( beanProxy ) )  return false;
		}
		return true;
	}
	
	
	/**
	 * Determine if the brick can add all of the specified views
	 * @return true if it can add the specified view and false if not
	 */
	public boolean canAddAll( final List<BeanProxy<?>> views ) {
		for ( final BeanProxy<?> view : views ) {
			if ( !canAdd( view ) )  return false;
		}
		return true;
	}
	
	
	/**
	 * Determine if this brick can add the specified view node
	 * @param node the node to test for addition
	 * @return true if it can add the specified view node and false if not
	 */
	public boolean canAddNode( final BeanNode<?> node ) {
		// neither a node nor any of its ancestors can be added to itself
		return ( !equals( node ) ) && ( !node.isAncestorOf( this ) ) && canAdd( node.getBeanProxy() );
	}
	
	
	/**
	 * Determine if the brick can add all of the specified view nodes
	 * @return true if it can add all of the specified view nodes and false if not
	 */
	public boolean canAddAllNodes( final List<BeanNode<?>> nodes ) {
		for ( final BeanNode<?> node : nodes ) {
			if ( !canAddNode( node ) )  return false;
		}
		return true;
	}
	
	
	/** 
	 * Determine if the node can be added as a sibling
	 * @param node the node to test
	 * @return true if the node can be added as a sibling and false if not
	 */
	public boolean canNodeBeSibling( final BeanNode<?> node ) {
		final Brick parent = getContainingBrick();
		return parent != null && parent.canAddNode( node );
	}
	
	
	/**
	 * Determine if all views can be sibling views
	 * @return true if all views can be siblings and false if not
	 */
	public boolean canAllNodesBeSiblings( final List<BeanNode<?>> nodes ) {
		for ( final BeanNode<?> node : nodes ) {
			if ( !canNodeBeSibling( node ) )  return false;
		}
		return true;
	}
	
	
	/** Remove this brick from its parent */
	abstract public void removeFromParent();
}
