//
//  BrickNodeListener.java
//  xal
//
//  Created by Thomas Pelaia on 7/10/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.beans.*;
import java.util.List;


/** processor of view node events */
public interface BrickListener {
	/**
	 * Handle the event in which nodes have been added to a container
	 * @param source the source of the event
	 * @param container the node to which nodes have been added
	 * @param nodes the nodes which have been added
	 */
	public void nodesAdded( final Object source, final Brick container, final List<BeanNode<?>> nodes );
	
	
	/**
	 * Handle the event in which nodes have been removed from a container
	 * @param source the source of the event
	 * @param container the node from which nodes have been removed
	 * @param nodes the nodes which have been removed
	 */
	public void nodesRemoved( final Object source, final Brick container, final List<BeanNode<?>> nodes );
	
	
	/**
	 * Handle the event in which a bean's property has been changed
	 * @param node the node whose property has changed
	 * @param propertyDescriptor the property which has changed
	 * @param value the new value
	 */
	public void propertyChanged( final BeanNode<?> node, final PropertyDescriptor propertyDescriptor, final Object value );
	
	
	/**
	 * Handle the event in which a brick's tree path needs refresh
	 * @param source the source of the event
	 * @param brick the brick at which the refresh needs to be done
	 */
	public void treeNeedsRefresh( final Object source, final Brick brick );	
}
