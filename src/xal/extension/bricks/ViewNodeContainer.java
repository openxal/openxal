//
//  ViewNodeContainer.java
//  xal
//
//  Created by Thomas Pelaia on 7/11/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;


/** interface of nodes which can contain view nodes */
public interface ViewNodeContainer extends BrickListener {
	/**
	 * Add the views to this node
	 * @param beanProxies the views to add to this node
	 */
	public void add( final List<BeanProxy<?>> beanProxies );
	
	
	/**
	 * Insert the views in this node beginning at the specified index
	 * @param beanProxies the views to add to this node
	 */
	public void insertSiblings( final List<BeanProxy<?>> beanProxies );
	
	
	/**
	 * Add the views nodes to this node
	 * @param nodes the nodes to add to this node
	 */
	public void addNodes( final List<BeanNode<?>> nodes );

	
	/**
	 * Get the tree index offset from the view index
	 * @return the tree index offset
	 */
	public int getTreeIndexOffsetFromViewIndex();
	
	
	/**
	 * Insert the bean nodes in this node beginning at the specified index
	 * @param node the node to add to this node
	 * @param viewIndex the initial index at which to begin inserting the nodes
	 */
	public void insertViewNode( final ViewNode node, final int viewIndex );
		
	
	/**
	 * Insert the view nodes in this node beginning at the specified index
	 * @param nodes the nodes to add to this node
	 */
	public void insertSiblingNodes( final List<BeanNode<?>> nodes );
	
	
	/** Move the specified nodes down */
	public void moveDownNodes( final List<BeanNode<?>> nodes );
	
	
	/** Move the specified nodes up */
	public void moveUpNodes( final List<BeanNode<?>> nodes );
	
	
	/**
	 * Remove the view node from this container
	 * @param node the node to remove
	 */
	public void removeNode( final BeanNode<?> node );
	
	
	/**
	 * Remove the view nodes from this container
	 * @param nodes the nodes to remove
	 */
	public void removeNodes( final List<BeanNode<?>> nodes );
	
	
	/**
	 * Determine if the brick can add the specified view
	 * @return true if it can add the specified view and false if not
	 */
	public boolean canAdd( final BeanProxy<?> beanProxy );
	
	
	/**
	 * Determine if the brick can be inserted in this view's parent
	 * @return true the view can be a sibling to this view and false if not
	 */
	public boolean canBeSibling( final BeanProxy<?> beanProxy );
	
	
	/**
	 * Determine if all views can be sibling views
	 * @return true if all views can be siblings and false if not
	 */
	public boolean canAllBeSiblings( final List<BeanProxy<?>> beanProxies );
	
	
	/**
	 * Determine if the brick can add all of the specified views
	 * @return true if it can add the specified view and false if not
	 */
	public boolean canAddAll( final List<BeanProxy<?>> views );
	
	
	/**
	 * Determine if the brick can add the specified view node
	 * @return true if it can add the specified view node and false if not
	 */
	public boolean canAddNode( final BeanNode<?> node );
	
	
	/**
	 * Determine if the brick can add all of the specified view nodes
	 * @return true if it can add all of the specified view nodes and false if not
	 */
	public boolean canAddAllNodes( final List<BeanNode<?>> nodes );
	
	
	/**
	 * Determine if all views can be sibling views
	 * @return true if all views can be siblings and false if not
	 */
	public boolean canAllNodesBeSiblings( final List<BeanNode<?>> nodes );
}
