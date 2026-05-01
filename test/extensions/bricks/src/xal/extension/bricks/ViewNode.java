//
//  ViewNode.java
//  xal
//
//  Created by Thomas Pelaia on 4/17/06.
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
import java.util.Map;
import java.util.Enumeration;

import xal.tools.data.*;


/** brick which represents a view */
public class ViewNode extends BeanNode<Component> implements ViewNodeContainer {
	/** data label */
	public static String DATA_LABEL = "ViewNode";
	
	/** the associated border node if any */
	protected BorderNode _borderNode;
	
	
	/** Primary Constructor */
    @SuppressWarnings( "unchecked" )    // nothing we can do to type ViewProxy any tighter without typing ViewNode
	public ViewNode( final ViewProxy<Component> viewProxy, final Map<String,Object> beanSettings, final String tag ) {
		super( viewProxy, beanSettings, tag );
		
		final DropTarget dropTarget = new DropTarget();
		try {
			dropTarget.addDropTargetListener( new DropHandler() );
			BEAN_OBJECT.setDropTarget( dropTarget );
			if ( isWindow() ) {
				getView().addComponentListener( new ComponentEventHandler() );
			}
		}
		catch( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/** Primary Constructor */
	public ViewNode( final ViewProxy<Component> viewProxy ) {
		this( viewProxy, null, viewProxy.getName() );
	}
	
	
	/** Constructor */
	@SuppressWarnings( "rawtypes" )		// DefaultMutableTreeNode returns an untyped Enumeration
	public ViewNode( final ViewNode node ) {
		this ( node.getViewProxy(), node.BEAN_SETTINGS, node.getTag() );
		
		setCustomBeanClassName( node.getCustomBeanClassName() );
		
		final List<BeanNode<?>> beanNodes = new ArrayList<BeanNode<?>>( TREE_NODE.getChildCount() );
		final Enumeration childNodeEnumerator = node.getTreeNode().children();
		while ( childNodeEnumerator.hasMoreElements() ) {
			final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)childNodeEnumerator.nextElement();
			final Object userObject = treeNode.getUserObject();
			if ( userObject != null && userObject instanceof BeanNode ) {
				beanNodes.add( (BeanNode)userObject );
			}
		}
		addNodes( beanNodes );
		
		final BorderNode borderNode = node.getBorderNode();
		if ( borderNode != null ) {
			setBorderNode( new BorderNode( borderNode ) );
		}
		
		if ( isWindow() ) {
			getView().setSize( node.getView().getSize() );
		}
	}
	
	
	/** generator */
	@SuppressWarnings( "unchecked" )	// must cast view proxy to have Component type
	static public ViewNode getInstance( final DataAdaptor adaptor ) {		
		final DataAdaptor proxyAdaptor = adaptor.childAdaptor( ViewProxy.DATA_LABEL );
		final ViewProxy<Component> viewProxy = (ViewProxy<Component>)ViewProxy.getInstance( proxyAdaptor );
		final String tag = adaptor.stringValue( "tag" );
		final ViewNode node = new ViewNode( viewProxy, null, tag );
				
		node.update( adaptor );
		
		return node;
	}
	
	
	/** get the bean instance */
	protected Component getPrototypeBean( final BeanProxy<Component> beanProxy ) {
		final Component view = (Component)((ViewProxy)beanProxy).getPrototype();
		view.setName( beanProxy.getName() );
		return view;
	}
	
	
	/**
	 * Get the view.
	 * @return the view
	 */
	public Component getView() {
		return BEAN_OBJECT;
	}
	
	
	/**
	 * Get the view proxy
	 * @return the view proxy
	 */
	public ViewProxy<Component> getViewProxy() {
		return (ViewProxy<Component>)BEAN_PROXY;
	}
	
	
	/**
	 * Get the index of the view node in the container
	 * @param node the node to locate
	 * @return the index of the view in the container or -1 if it isn't contained in this node
	 */
	public int getViewIndex( final ViewNode node ) {
		final Container container = getContainer();
		final Component[] components = container.getComponents();
		for ( int index = 0 ; index < components.length ; index++ ) {
			if ( components[index] == node.getView() )  return index;
		}
		return -1;
	}
	
	
	/**
	 * Determine if the underlying view is a window
	 * @return true if the view is a window and false if not
	 */
	public boolean isWindow() {
		return getViewProxy().isWindow();
	}
	
	
	/**
	 * Get the container.
	 * @return the view as a container
	 */
    @SuppressWarnings( "unchecked" )    // can't type this anymore since the ViewNode isn't typed
	public Container getContainer() {
		final ViewProxy<Component> viewProxy = getViewProxy();
		return viewProxy.isContainer() ? viewProxy.getContainer( getView() ) : null;
	}

	
	/**
	 * Determine if the brick can add the specified view
	 * @return true if it can add the specified view and false if not
	 */
	public boolean canAdd( final BeanProxy<?> beanProxy ) {
		if ( beanProxy instanceof ViewProxy ) {
			return !((ViewProxy)beanProxy).isWindow() && getViewProxy().isContainer();
		}
		else if ( beanProxy instanceof BorderProxy ) {
			return BEAN_OBJECT instanceof JComponent;
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * Add the beans to this node
	 * @param beanProxies the beans to add to this node
	 */
	@SuppressWarnings( "unchecked" )	// must cast bean proxy to view proxy
	public void add( final List<BeanProxy<?>> beanProxies ) {
		final List<BeanNode<?>> nodes = new ArrayList<BeanNode<?>>( beanProxies.size() );
		final Container container = getContainer();
		for ( final BeanProxy<?> beanProxy : beanProxies ) {
			if ( beanProxy instanceof ViewProxy ) {
				if ( container == null )  return;
				final ViewNode node = new ViewNode( (ViewProxy<Component>)beanProxy );
				container.add( node.getView() );
				node.addBrickListener( this );
				nodes.add( node );
				TREE_NODE.add( node.getTreeNode() );
			}
			else if ( beanProxy instanceof BorderProxy ) {
				final BorderNode node = new BorderNode( (BorderProxy)beanProxy );
				setBorderNode( node );
				nodes.add( node );
			}
		}
		refreshDisplay();
		EVENT_PROXY.nodesAdded( this, this, nodes );
		EVENT_PROXY.treeNeedsRefresh( this, this );
	}
	
	
	/**
	 * Add the views nodes to this node
	 * @param nodes the nodes to add to this node
	 */
	public void addNodes( final List<BeanNode<?>> nodes ) {
		final List<BeanNode<?>> nodeCopies = new ArrayList<BeanNode<?>>( nodes.size() );
		final Container container = getContainer();
		for ( final BeanNode<?> node : nodes ) {
			if ( node instanceof ViewNode ) {
				if ( container == null )  return;
				final ViewNode nodeCopy = new ViewNode( (ViewNode)node );
				container.add( nodeCopy.getView() );
				nodeCopy.addBrickListener( this );
				nodeCopies.add( nodeCopy );
				TREE_NODE.add( nodeCopy.getTreeNode() );
			}
			else if ( node instanceof BorderNode ) {
				final BorderNode nodeCopy = new BorderNode( (BorderNode)node );
				setBorderNode( nodeCopy );
				nodeCopies.add( nodeCopy );
			}
		}
		refreshDisplay();
		EVENT_PROXY.nodesAdded( this, this, nodeCopies );
		EVENT_PROXY.treeNeedsRefresh( this, this );
	}
	
	
	/**
	 * Insert the views in this node beginning at the specified index
	 * @param beanProxies the views to add to this node
	 */
	@SuppressWarnings( "unchecked" )	// must cast bean proxy to view proxy
	public void insertSiblings( final List<BeanProxy<?>> beanProxies ) {
		int treeIndex = TREE_NODE.getParent().getIndex( TREE_NODE );
		final ViewNodeContainer target = getViewNodeContainer();
		int viewIndex = treeIndex - target.getTreeIndexOffsetFromViewIndex();
		
		final List<BeanNode<?>> nodes = new ArrayList<BeanNode<?>>( beanProxies.size() );
		for ( final BeanProxy<?> beanProxy : beanProxies ) {
			if ( beanProxy instanceof ViewProxy ) {
				final ViewNode node = new ViewNode( (ViewProxy<Component>)beanProxy );
				target.insertViewNode( node, viewIndex++ );
				nodes.add( node );
			}
		}
		refreshDisplay();
		EVENT_PROXY.nodesAdded( this, (Brick)target, nodes );
		EVENT_PROXY.treeNeedsRefresh( this, (Brick)target );
	}
	
	
	/**
	 * Get the tree index offset from the view index
	 * @return the tree index offset
	 */
	public int getTreeIndexOffsetFromViewIndex() {
		return _borderNode != null ? 1 : 0;
	}
	
	
	/**
	 * Insert the view node in this node beginning at the specified index
	 * @param node the node to add to this node
	 * @param viewIndex the initial index at which to insert the node
	 */
	public void insertViewNode( final ViewNode node, final int viewIndex ) {
		if ( getContainer() != null ) {
			node.addBrickListener( this );
			final int treeIndex = viewIndex + getTreeIndexOffsetFromViewIndex();
			TREE_NODE.insert( node.getTreeNode(), treeIndex );
			reconstructContainer( false );
			EVENT_PROXY.treeNeedsRefresh( this, this );
		}
	}
	
	
	/**
	 * Insert the specified view nodes immediately above this node
	 * @param nodes the nodes to add to this node
	 */
	public void insertSiblingNodes( final List<BeanNode<?>> nodes ) {
		int treeIndex = TREE_NODE.getParent().getIndex( TREE_NODE );
		final ViewNodeContainer target = getViewNodeContainer();
		int viewIndex = treeIndex - target.getTreeIndexOffsetFromViewIndex();
		
		final List<BeanNode<?>> nodeCopies = new ArrayList<BeanNode<?>>( nodes.size() );
		for ( final BeanNode<?> node : nodes ) {
			if ( node instanceof ViewNode ) {
				final ViewNode nodeCopy = new ViewNode( (ViewNode)node );
				target.insertViewNode( nodeCopy, viewIndex++ );
				nodeCopies.add( nodeCopy );
			}
			else if ( node instanceof BorderNode && target instanceof ViewNode ) {
				final BorderNode nodeCopy = new BorderNode( (BorderNode)node );
				((ViewNode)target).setBorderNode( nodeCopy );
				nodeCopies.add( nodeCopy );
			}
		}
		redrawView();
		EVENT_PROXY.nodesAdded( this, (Brick)target, nodeCopies );
		EVENT_PROXY.treeNeedsRefresh( this, (Brick)target );
	}
	
	
	/** Move the specified nodes down */
	public void moveDownNodes( final List<BeanNode<?>> nodes ) {
		if ( nodes.size() > 0 ) {
			final int brickCount = TREE_NODE.getChildCount();
			
			final List<Integer> indicesToMove = new ArrayList<Integer>( nodes.size() );
			for ( final BeanNode<?> node : nodes ) {
				final int index = TREE_NODE.getIndex( node.TREE_NODE );
				indicesToMove.add( index );
			}
			
			Collections.sort( indicesToMove );
			Collections.reverse( indicesToMove );
			
			if ( indicesToMove.get( 0 ) < brickCount - 1 ) {
				final Container container = getContainer();
				if ( container == null )  return;
				
				for ( final int index : indicesToMove ) {
					final TreeNode treeNode = TREE_NODE.getChildAt( index );
					if ( treeNode instanceof DefaultMutableTreeNode ) {
						final ViewNode viewNode = (ViewNode)((DefaultMutableTreeNode)treeNode).getUserObject();
						final int nodeIndex = index + 1;
						TREE_NODE.insert( (MutableTreeNode)treeNode, nodeIndex );
					}
				}
				reconstructContainer( true );
				
				EVENT_PROXY.nodesAdded( this, this, nodes );
				EVENT_PROXY.treeNeedsRefresh( this, this );
			}
		}
	}
	
	
	/** Move the specified nodes up */
	public void moveUpNodes( final List<BeanNode<?>> nodes ) {
		if ( nodes.size() > 0 ) {
			final int brickCount = TREE_NODE.getChildCount();
			
			final List<Integer> indicesToMove = new ArrayList<Integer>( nodes.size() );
			for ( final BeanNode<?> node : nodes ) {
				final int index = TREE_NODE.getIndex( node.TREE_NODE );
				indicesToMove.add( index );
			}
			
			Collections.sort( indicesToMove );
			
			final int treeIndexViewIndexOffset = getTreeIndexOffsetFromViewIndex();
			if ( indicesToMove.get( 0 ) > treeIndexViewIndexOffset ) {
				final Container container = getContainer();
				if ( container == null )  return;
				
				for ( final int index : indicesToMove ) {
					final TreeNode treeNode = TREE_NODE.getChildAt( index );
					if ( treeNode instanceof DefaultMutableTreeNode ) {
						final ViewNode viewNode = (ViewNode)((DefaultMutableTreeNode)treeNode).getUserObject();
						final int nodeIndex = index - 1;
						TREE_NODE.insert( (MutableTreeNode)treeNode, nodeIndex );
					}
				}
				reconstructContainer( true );
				
				EVENT_PROXY.nodesAdded( this, this, nodes );
				EVENT_PROXY.treeNeedsRefresh( this, this );
			}
		}
	}
	
	
	/** reconstruct the container from the tree node */
	private void reconstructContainer( final boolean redraw ) {
		final Container container = getContainer();
		if ( container != null ) {
			final int treeIndexViewIndexOffset = getTreeIndexOffsetFromViewIndex();
			container.removeAll();
			final int nodeCount = TREE_NODE.getChildCount();
			for ( int index = 0 ; index < nodeCount ; index++ ) {
				final int viewIndex = index - treeIndexViewIndexOffset;
				if ( viewIndex >= 0 ) {
					final TreeNode treeNode = TREE_NODE.getChildAt( index );
					if ( treeNode instanceof DefaultMutableTreeNode ) {
						final ViewNode viewNode = (ViewNode)((DefaultMutableTreeNode)treeNode).getUserObject();
						container.add( viewNode.getView() );
					}
				}
			}
			if ( redraw ) {
				redrawView();
			}
		}
	}
	
	
	/** force the view to be redrawn */
	private void redrawView() {
		final Component view = getView();
		final Component window = SwingUtilities.getWindowAncestor( view );
		if ( window != null ) {
			window.repaint();
			window.validate();
		}		
	}
	
	
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
		final Container container = getContainer();
		if ( container == null )  return;
		for ( final BeanNode<?> node : nodes ) {
			node.removeBrickListener( this );
			if ( node instanceof ViewNode ) {
				container.remove( ((ViewNode)node).getView() );
				TREE_NODE.remove( node.getTreeNode() );
			}
			else if ( node instanceof BorderNode ) {
				setBorderNode( null );
			}
		}
		container.repaint();
		SwingUtilities.getWindowAncestor( container ).validate();
		EVENT_PROXY.nodesRemoved( this, this, nodes );
		EVENT_PROXY.treeNeedsRefresh( this, this );
	}
	
	
	/** get the border node */
	protected BorderNode getBorderNode() {
		return _borderNode;
	}
	
	
	/** set the border node */
	protected void setBorderNode( final BorderNode node ) {
		final BorderNode oldNode = _borderNode;
		if ( _borderNode != null ) {
			TREE_NODE.remove( _borderNode.getTreeNode() );
		}
		
		final JComponent view = (JComponent)getView();
		
		_borderNode = node;
		if ( node != null ) {
			view.setBorder( node.getBorder() );
			TREE_NODE.insert( node.getTreeNode(), 0 );
		}
		else {
			view.setBorder( null );
		}
		if ( node != null ) {
			final List<BeanNode<?>> nodesAdded = new ArrayList<BeanNode<?>>(1);
			nodesAdded.add( node );
			EVENT_PROXY.nodesAdded( this, this, nodesAdded );
			EVENT_PROXY.treeNeedsRefresh( this, this );
		}
		if ( oldNode != null ) {
			final List<BeanNode<?>> nodesRemoved = new ArrayList<BeanNode<?>>(1);
			nodesRemoved.add( node );
			EVENT_PROXY.nodesRemoved( this, this, nodesRemoved );
			EVENT_PROXY.treeNeedsRefresh( this, this );
		}
	}
	
	
	/** get the containing or being the view */
	public Window getWindow() {
		final Component view = getView();
		return (Window) ( view instanceof Window ? view : SwingUtilities.getWindowAncestor( getView() ) );
	}
	
	
	/** refresh display */
	public void refreshDisplay() {
		final Component view = getView();
		if ( view != null ) {
			view.invalidate();
		}
		
		final Window window = getWindow();
		if ( window != null ) {
			window.validate();
			window.repaint();
		}
	}
	
	
	/**
	 * Handle the event in which nodes have been added to a container
	 * @param source the source of the event
	 * @param container the node to which nodes have been added
	 * @param nodes the nodes which have been added
	 */
	public void nodesAdded( final Object source, final Brick container, final List<BeanNode<?>> nodes ) {
	}
	
	
	/**
	 * Handle the event in which nodes have been removed from a container
	 * @param source the source of the event
	 * @param container the node from which nodes have been removed
	 * @param nodes the nodes which have been removed
	 */
	public void nodesRemoved( final Object source, final Brick container, final List<BeanNode<?>> nodes ) {
	}
	
	
	/**
	 * Handle the event in which a bean's property has been changed
	 * @param beanNode the node whose property has changed
	 * @param propertyDescriptor the property which has changed
	 * @param value the new value
	 */
	public void propertyChanged( final BeanNode<?> beanNode, final PropertyDescriptor propertyDescriptor, final Object value ) {
		getViewProxy().handleChildNodePropertyChange( this, beanNode, propertyDescriptor, value );
		treeNeedsRefresh( this, beanNode );
	}
	
	
	/**
	 * Handle the event in which a brick's tree path needs refresh
	 * @param source the source of the event
	 * @param brick the brick at which the refresh needs to be done
	 */
	public void treeNeedsRefresh( final Object source, final Brick brick ) {
		EVENT_PROXY.treeNeedsRefresh( this, brick );
	}
	
	
	/** Remove this brick from its parent */
	public void removeFromParent() {
		getViewNodeContainer().removeNode( this );
	}
	
	
	/** Display the bean's window */
	public void display() {
		final Component view = getView();
		final Component window = view instanceof Window ? view : SwingUtilities.getWindowAncestor( view );
		if ( window != null && window instanceof Window ) {
			window.setVisible( true );
			((Window)window).toFront();
		}
	}
	
	
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
		final DataAdaptor borderAdaptor = adaptor.childAdaptor( BorderNode.DATA_LABEL );
		if ( borderAdaptor != null ) {
			final BorderNode borderNode = BorderNode.getInstance( borderAdaptor );
			setBorderNode( borderNode );
		}
		
		final List<DataAdaptor> nodeAdaptors = adaptor.childAdaptors( ViewNode.DATA_LABEL );
		final List<BeanNode<?>> nodes = new ArrayList<BeanNode<?>>( nodeAdaptors.size() );
		for ( final DataAdaptor nodeAdaptor : nodeAdaptors ) {
			nodeAdaptor.setValue( "contextURL", adaptor.stringValue( "contextURL" ) );
			nodes.add( ViewNode.getInstance( nodeAdaptor ) );
		}
		addNodes( nodes );
		
		if ( isWindow() && adaptor.hasAttribute( "width" ) ) {
			final int width = adaptor.intValue( "width" );
			final int height = adaptor.intValue( "height" );
			getView().setSize( width, height );
		}
		
		super.update( adaptor );
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
 	@SuppressWarnings( "rawtypes" )		// DefaultMutableTreeNode returns an untyped Enumeration
   public void write( final DataAdaptor adaptor ) {
		super.write( adaptor );
		
		if ( isWindow() ) {
			final Dimension size = getView().getSize();
			adaptor.setValue( "width", size.width );
			adaptor.setValue( "height", size.height );
		}
		
		final Enumeration childNodeEnumerator = getTreeNode().children();
		while ( childNodeEnumerator.hasMoreElements() ) {
			final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)childNodeEnumerator.nextElement();
			final Object userObject = treeNode.getUserObject();
			if ( userObject != null && userObject instanceof BeanNode ) {
				adaptor.writeNode( (BeanNode)userObject );
			}
		}
	}
	
	
	
	/** handle component events */
	protected class ComponentEventHandler extends ComponentAdapter {
		public void componentResized( final ComponentEvent event ) {
			ViewNode.this.treeNeedsRefresh( ViewNode.this, ViewNode.this );
		}
	}
	
	
	
	/** handle drop events */
	protected class DropHandler extends DropTargetAdapter {
		public void dragEnter( final DropTargetDragEvent event ) {}
		
        @SuppressWarnings( "unchecked" )    // we have no choice but to cast the transfered data
		public void drop( final DropTargetDropEvent event ) {
			try {
				final List<BeanProxy<?>> beanProxies = (List<BeanProxy<?>>)event.getTransferable().getTransferData( ViewTransferable.VIEW_FLAVOR );
				if ( canAddAll( beanProxies ) ) {
					add( beanProxies );
					event.dropComplete( true );
				}
				else {
					event.dropComplete( false );
				}
			}
			catch( Exception exception ) {
				exception.printStackTrace();
				event.dropComplete( false );
			}
		}
	}
}
