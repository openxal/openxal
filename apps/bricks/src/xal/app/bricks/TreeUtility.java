//
//  TreeUtility.java
//  xal
//
//  Created by Thomas Pelaia on 7/28/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//


package xal.app.bricks;

import xal.extension.bricks.*;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.ArrayList;
import java.util.List;


/** utility for performing common bricks operations on the tree of bricks */
public class TreeUtility {
	/** get the selected bean node */
	static BeanNode<?> getSelectedBeanNode( final JTree tree ) {
		final TreePath selectionPath = tree.getSelectionPath();
		return getBeanNode( selectionPath );
	}

	
	/** get the selected bean nodes */
	static BeanNode<?>[] getSelectedBeanNodes( final JTree tree ) {
		final TreePath[] selectionPaths = tree.getSelectionPaths();
		if ( selectionPaths == null )  return new BeanNode<?>[0];
		final List<BeanNode<?>> nodes = new ArrayList<BeanNode<?>>( selectionPaths.length );
		for ( final TreePath treePath : selectionPaths ) {
			final BeanNode<?> node = getBeanNode( treePath );
			if ( node != null ) {
				nodes.add( node );
			}
		}
		
		return nodes.toArray( new BeanNode<?>[ nodes.size() ] );
	}
	
	
	/** get the bean node from the tree path */
	static BeanNode<?> getBeanNode( final TreePath treePath ) {
		if ( treePath != null ) {
			final Object treeNode = treePath.getLastPathComponent();
			if ( treeNode instanceof DefaultMutableTreeNode ) {
				final Object userObject = ((DefaultMutableTreeNode)treeNode).getUserObject();
				if ( userObject instanceof BeanNode ) {
					return (BeanNode<?>)userObject;
				}
				else {
					return null;
				}
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}		
	}
	
}
