/*
 * TemplateWindow.java
 *
 * Created on Fri April 17 15:12:21 EDT 2006
 *
 * Copyright (c) 2006 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.bricks;

import xal.extension.application.*;
import xal.extension.bricks.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.Container;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.Point;
import java.awt.Toolkit;
import javax.swing.event.*;
import java.util.*;
import java.beans.*;


/**
 * BuilderWindow
 * @author  tap
 */
class BricksWindow extends XalWindow implements SwingConstants, BrickListener {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    
	/** tree model of view nodes */
	final protected DefaultTreeModel VIEW_NODE_TREE_MODEL;
	
	/** the tree of views */
	final protected JTree VIEW_TREE;
	
	/** root brick */
	final protected RootBrick ROOT_BRICK;
	
	/** view inspector */
	final private ViewInspector VIEW_INSPECTOR;
	
	/** code assistant for generating code snippets */
	protected CodeAssistant _codeAssistant;
	
	/** context in which the bricks runtime runs */
	private final BricksContext CONTEXT;
	
	/** view displaying the view palette and the view hierarchy */
	private final JSplitPane BUILDER_VIEW;
	
	
    /** Constructor */
    public BricksWindow( final BricksDocument aDocument, final RootBrick rootBrick ) {
        super( aDocument );
		
        setSize( 1000, 600 );
		
		VIEW_INSPECTOR = new ViewInspector();
		BUILDER_VIEW = new JSplitPane();		
		
		CONTEXT = aDocument.getContext();
		ROOT_BRICK = rootBrick;
		final DefaultMutableTreeNode rootNode = ROOT_BRICK.getTreeNode();
		ROOT_BRICK.addBrickListener( this );
		rootNode.setAllowsChildren( true );
		VIEW_NODE_TREE_MODEL = new DefaultTreeModel( rootNode );
		VIEW_TREE = new JTree( VIEW_NODE_TREE_MODEL );
		
		_codeAssistant.setViewTree( VIEW_TREE );
		
		makeContent();
    }
	
	
	/** Make the content for the window. */
	protected void makeContent() {
		final JSplitPane mainView = new JSplitPane();
		getContentPane().add( mainView );
				
		final Box hierarchyView = new Box( BoxLayout.Y_AXIS );
		
		BUILDER_VIEW.setLeftComponent( new ViewPalette() );
		BUILDER_VIEW.setRightComponent( hierarchyView );
		BUILDER_VIEW.setDividerLocation( 250 );
		BUILDER_VIEW.setOneTouchExpandable( true );
				
		mainView.setLeftComponent( BUILDER_VIEW );
		mainView.setRightComponent( VIEW_INSPECTOR );
		mainView.setDividerLocation( 600 );
		
		VIEW_TREE.setDragEnabled( true );
		VIEW_TREE.setTransferHandler( new ViewNodesTransferHandler() );
		
		final DropTarget dropTarget = new DropTarget();
		try {
			dropTarget.addDropTargetListener( new TreeDropHandler() );
			VIEW_TREE.setDropTarget( dropTarget );
		}
		catch( Exception exception ) {
			exception.printStackTrace();
		}
		
		VIEW_TREE.setShowsRootHandles( true );
		hierarchyView.add( new JScrollPane( VIEW_TREE ) );
		
		VIEW_TREE.addTreeSelectionListener( new TreeSelectionListener() {
			public void valueChanged( final TreeSelectionEvent event ) {
				inspectSelection( VIEW_TREE );
			}
		});
		
		// allow the user to double click a row to view the associated parameter inspector
		VIEW_TREE.addMouseListener( new MouseAdapter() {
			public void mouseClicked( final MouseEvent event ) {
				if ( event.getClickCount() == 2 ) {
					displaySelection( VIEW_TREE );
				}
			}
		});
	}
	
	
    /**
	 * Register actions for the custom menu items.
     * @param commander The commander with which to register the custom commands.
     */
    public void customizeCommands( final Commander commander ) {
        final Action moveDownViewAction = new AbstractAction( "move-down-view" ) {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				moveDownSelection( VIEW_TREE );
			}
        };
        commander.registerAction( moveDownViewAction );
		
        final Action moveUpViewAction = new AbstractAction( "move-up-view" ) {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				moveUpSelection( VIEW_TREE );
			}
        };
        commander.registerAction( moveUpViewAction );
		
        final Action toggleViewPaletteAction = new AbstractAction( "toggle-view-palette" ) {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				BUILDER_VIEW.setDividerLocation( BUILDER_VIEW.getDividerLocation() <= BUILDER_VIEW.getDividerSize() ? BUILDER_VIEW.getLastDividerLocation() : 0 );
			}
        };
        commander.registerAction( toggleViewPaletteAction );
		
		_codeAssistant = new CodeAssistant();
		_codeAssistant.registerActions( commander );
	}
    
	
	/** Dispose of custom window resources. */
	public void freeCustomResources() {
		ROOT_BRICK.disposeAllWindows();
	}
	
	
	/** get the root brick */
	public RootBrick getRootBrick() {
		return ROOT_BRICK;
	}
	
	
	/** get the selected bean node */
	private BeanNode<?> getSelectedBeanNode() {
		return TreeUtility.getSelectedBeanNode( VIEW_TREE );
	}
	
	
	/** get the selected bean nodes */
	private BeanNode[] getSelectedBeanNodes() {
		return TreeUtility.getSelectedBeanNodes( VIEW_TREE );
	}
	
	
	/** inspect the selected item */
	private void inspectSelection( final JTree tree ) {
		final BeanNode<?> beanNode = TreeUtility.getSelectedBeanNode( tree );
		VIEW_INSPECTOR.inspect( CONTEXT, beanNode );
	}
	
	
	/** move the selected item down */
	private void moveDownSelection( final JTree tree ) {
		final BeanNode[] beanNodes = TreeUtility.getSelectedBeanNodes( tree );
		if ( beanNodes != null && beanNodes.length > 0 ) {
			if ( beanNodes[0] instanceof ViewNode ) {
				final ViewNodeContainer container = ((ViewNode)beanNodes[0]).getViewNodeContainer();
				if ( container != null ) {
					final List<BeanNode<?>> viewNodes = new ArrayList<BeanNode<?>>( beanNodes.length );
					for ( final BeanNode<?> beanNode : beanNodes ) {
						if ( beanNode instanceof ViewNode ) {
							viewNodes.add( beanNode );
						}
					}
					if ( container.canAllNodesBeSiblings( viewNodes ) ) {
						container.moveDownNodes( viewNodes );
					}
					final TreePath[] treePaths = new TreePath[ viewNodes.size() ];
					for ( int index = 0 ; index < treePaths.length ; index++ ) {
						final TreeNode[] path = viewNodes.get( index ).getTreeNode().getPath();
						treePaths[ index ] = new TreePath( path );
					}
					tree.setSelectionPaths( treePaths );
				}
			}
		}
	}
	
	
	/** move the selected item up */
	private void moveUpSelection( final JTree tree ) {
		final BeanNode[] beanNodes = TreeUtility.getSelectedBeanNodes( tree );
		if ( beanNodes != null && beanNodes.length > 0 ) {
			if ( beanNodes[0] instanceof ViewNode ) {
				final ViewNodeContainer container = ((ViewNode)beanNodes[0]).getViewNodeContainer();
				if ( container != null ) {
					final List<BeanNode<?>> viewNodes = new ArrayList<BeanNode<?>>( beanNodes.length );
					for ( final BeanNode<?> beanNode : beanNodes ) {
						if ( beanNode instanceof ViewNode ) {
							viewNodes.add( beanNode );
						}
					}
					if ( container.canAllNodesBeSiblings( viewNodes ) ) {
						container.moveUpNodes( viewNodes );
					}
					final TreePath[] treePaths = new TreePath[ viewNodes.size() ];
					for ( int index = 0 ; index < treePaths.length ; index++ ) {
						final TreeNode[] path = viewNodes.get( index ).getTreeNode().getPath();
						treePaths[ index ] = new TreePath( path );
					}
					tree.setSelectionPaths( treePaths );
				}
			}
		}
	}
	
	
	/** display the selected item */
	private void displaySelection( final JTree tree ) {
		final BeanNode<?> beanNode = TreeUtility.getSelectedBeanNode( tree );
		if ( beanNode != null ) {
			beanNode.display();
		}
	}
	
	
	/**
	 * Handle the event in which nodes have been added to a container
	 * @param source the source of the event
	 * @param container the node to which nodes have been added
	 * @param nodes the nodes which have been added
	 */
	public void nodesAdded( final Object source, final Brick container, final List<BeanNode<?>> nodes ) {}
	
	
	/**
	 * Handle the event in which nodes have been removed from a container
	 * @param source the source of the event
	 * @param container the node from which nodes have been removed
	 * @param nodes the nodes which have been removed
	 */
	public void nodesRemoved( final Object source, final Brick container, final List<BeanNode<?>> nodes ) {}
	
	
	/**
	 * Handle the event in which a bean's property has been changed
	 * @param beanNode the node whose property has changed
	 * @param propertyDescritpr the property which has changed
	 * @param value the new value
	 */
	public void propertyChanged( final BeanNode<?> beanNode, final PropertyDescriptor propertyDescriptor, final Object value ) {}
	
	
	/**
	 * Handle the event in which a brick's tree path needs refresh
	 * @param source the source of the event
	 * @param brick the brick at which the refresh needs to be done
	 */
	public void treeNeedsRefresh( final Object source, final Brick brick ) {
		VIEW_NODE_TREE_MODEL.reload( brick.getTreeNode() );
	}
	
	
	/** process the dropping of a view node */
	protected static boolean processViewNodeDrop( final TreePath treePath, final List<BeanNode<?>> nodes ) throws Exception {
		if ( treePath != null ) {
			final DefaultMutableTreeNode dropNode = (DefaultMutableTreeNode)treePath.getLastPathComponent();
			final Object dropComponent = dropNode.getUserObject();
			if ( dropComponent instanceof ViewNodeContainer ) {
				final ViewNodeContainer target = (ViewNodeContainer)dropComponent;
				if ( target.canAddAllNodes( nodes ) ) {
					target.addNodes( nodes );
					return true;
				}
				else if ( target.canAllNodesBeSiblings( nodes ) ) {
					target.insertSiblingNodes( nodes );
					return true;
				}
				else {
					return false;
				}
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	
	/** handle drop events on the tree of nodes */
	protected class TreeDropHandler extends DropTargetAdapter {
		public void dragEnter( final DropTargetDragEvent event ) {}
		
		public void drop( final DropTargetDropEvent event ) {
			try {
				final Transferable transferable = event.getTransferable();
				if ( transferable.isDataFlavorSupported( ViewNodeTransferable.VIEW_NODE_FLAVOR ) ) {
					processViewNodeDrop( event );
				}
				else if ( transferable.isDataFlavorSupported( ViewTransferable.VIEW_FLAVOR ) ) {
					processViewDrop( event );
				}
				else {
					System.out.println( "Transferable not supported:  " + transferable );
				}
			}
			catch ( Exception exception ) {
				exception.printStackTrace();
			}
			
		}
		
		
		/** process the dropping of a view node */
        @SuppressWarnings( "unchecked" )    // transferables don't support generics
		void processViewNodeDrop( final DropTargetDropEvent event ) throws Exception {
			final JTree tree = (JTree)event.getDropTargetContext().getComponent();
			final List<BeanNode<?>> nodes = (List<BeanNode<?>>)event.getTransferable().getTransferData( ViewNodeTransferable.VIEW_NODE_FLAVOR );
			final Point location = event.getLocation();
			final TreePath treePath = tree.getClosestPathForLocation( location.x, location.y );
			if ( BricksWindow.processViewNodeDrop( treePath, nodes ) ) {
				event.dropComplete( true );
			}
			else {
				event.dropComplete( false );
			}
		}
		
		
		/** process the dropping of a view */
        @SuppressWarnings( "unchecked" )    // transferables don't support generics
		void processViewDrop( final DropTargetDropEvent event ) throws Exception {
			final JTree tree = (JTree)event.getDropTargetContext().getComponent();
			final List<BeanProxy<?>> views = (List<BeanProxy<?>>)event.getTransferable().getTransferData( ViewTransferable.VIEW_FLAVOR );
			final Point location = event.getLocation();
			final TreePath treePath = tree.getClosestPathForLocation( location.x, location.y );
			if ( treePath != null ) {
				final DefaultMutableTreeNode dropNode = (DefaultMutableTreeNode)treePath.getLastPathComponent();
				final Object dropComponent = dropNode.getUserObject();
				if ( dropComponent instanceof ViewNodeContainer ) {
					final ViewNodeContainer target = (ViewNodeContainer)dropComponent;
					if ( target.canAddAll( views ) ) {
						target.add( views );
						event.dropComplete( true );
					}
					else if ( target.canAllBeSiblings( views ) ) {
						target.insertSiblings( views );
						event.dropComplete( true );
					}
					else {
						event.dropComplete( false );
					}
				}
			}
			else {
				event.dropComplete( false );
			}
		}
	}
	
	
	
	/** View nodes transfer handler */
	class ViewNodesTransferHandler extends TransferHandler {
        /** serialization identifier */
        private static final long serialVersionUID = 1L;
        
		/** transfer view nodes */
		protected Transferable createTransferable( final JComponent component ) {
			final JTree nodeTree = (JTree)component;
			final TreePath[] selections = nodeTree.getSelectionPaths();
			final List<BeanNode<?>> nodes = new ArrayList<BeanNode<?>>( selections.length );
			for ( final TreePath path : selections ) {
				final BeanNode<?> node = (BeanNode<?>)((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
				nodes.add( node );
			}
			return new ViewNodeTransferable( nodes );
		}
		
		
		/** provides copy or move operation */
		public int getSourceActions( final JComponent component ) {
			return COPY_OR_MOVE;
		}
		
		
		/** determine if we can import at least one of the tranferable flavors */
		public boolean canImport( final JComponent component, final DataFlavor[] flavors ) {
			for ( DataFlavor flavor : flavors ) {
				if ( flavor == ViewNodeTransferable.VIEW_NODE_FLAVOR )  return true;
			}
			return false;
		}
		
		
		/** import the transferable */
        @SuppressWarnings( "unchecked" )    // transferables don't support generics
		public boolean importData( final JComponent component, final Transferable transferable ) {
			try {
				final JTree nodeTree = (JTree)component;
				final TreePath treePath = nodeTree.getSelectionPath();
				final List<BeanNode<?>> nodes = (List<BeanNode<?>>)transferable.getTransferData( ViewNodeTransferable.VIEW_NODE_FLAVOR );
				return BricksWindow.processViewNodeDrop( treePath, nodes );
			}
			catch( UnsupportedFlavorException exception ) {
				exception.printStackTrace();
				return false;
			}
			catch( java.io.IOException exception ) {
				exception.printStackTrace();
				return false;
			}
			catch( Exception exception ) {
				exception.printStackTrace();
				return false;
			}
		}
		
		
		/** perform cleanup operations */
        @SuppressWarnings( "unchecked" )    // transferables don't support generics
		protected void exportDone( final JComponent component, Transferable transferable, int action ) {
			switch( action ) {
				case TransferHandler.MOVE:
					if ( transferable != null ) {
						try {
							final List<BeanNode<?>> nodes = (List<BeanNode<?>>)transferable.getTransferData( ViewNodeTransferable.VIEW_NODE_FLAVOR );
							for ( final BeanNode<?> node : nodes ) {
								node.removeFromParent();
							}
						}
						catch ( java.awt.datatransfer.UnsupportedFlavorException exception ) {
							exception.printStackTrace();
						}
						catch ( java.io.IOException exception ) {
							exception.printStackTrace();
						}
					}
					break;
				default:
					break;
			}
		}
	}
}




