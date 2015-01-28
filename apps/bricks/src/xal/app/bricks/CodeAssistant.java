//
//  CodeAssistant.java
//  xal
//
//  Created by Thomas Pelaia on 7/28/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

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


/** assists in generating code snippets */
public class CodeAssistant {
	/** tree selection handler */
	final protected TreeHandler TREE_HANDLER;
	
	/** the target view tree */
	protected JTree _viewTree;
	
	/** copy tag action */
	protected Action _copyTagAction;
	
	/** copy Java declaration action */
	protected Action _copyJavaDeclarationAction;
	
	/** copy Java reference action */
	protected Action _copyJavaReferenceAction;
	
	/** copy XAL reference action */
	protected Action _copyXALReferenceAction;
	
	/** copy Jython reference action */
	protected Action _copyJythonReferenceAction;
	
	/** copy Java import action */
	protected Action _copyJavaImportAction;
	
	/** copy Jython import action */
	protected Action _copyJythonImportAction;
	
	
	/** Constructor */
	public CodeAssistant() {
		TREE_HANDLER = new TreeHandler();
	}
	
	
	/** set the view tree */
	public void setViewTree( final JTree tree ) {
		if ( _viewTree != null ) {
			_viewTree.removeTreeSelectionListener( TREE_HANDLER );
		}
		
		_viewTree  = tree;
		
		tree.addTreeSelectionListener( TREE_HANDLER );
	}
	
	
	/** register actions for code assistance */
	public void registerActions( final Commander commander ) {
        // define the "copy tag" action
        _copyTagAction = new AbstractAction( "copy-tag") {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				final BeanNode<?> node = TreeUtility.getSelectedBeanNode( _viewTree );
				if ( node != null ) {
					final StringSelection stringSelection = new StringSelection( node.getTag() );
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents( stringSelection, stringSelection );
				}
            }
        };
        commander.registerAction( _copyTagAction );

		// define the "copy java declaration" action
        _copyJavaDeclarationAction = new AbstractAction( "copy-java-declaration") {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				final BeanNode<?>[] nodes = TreeUtility.getSelectedBeanNodes( _viewTree );
				if ( nodes.length > 0 ) {
					final StringBuffer buffer = new StringBuffer();
					for ( final BeanNode<?> node : nodes ) {
						buffer.append( node.getJavaDeclarationSnippet() );
						buffer.append( System.getProperty( "line.separator" ) );
					}
					final StringSelection stringSelection = new StringSelection( buffer.toString() );
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents( stringSelection, stringSelection );
				}
            }
        };
        commander.registerAction( _copyJavaDeclarationAction );
		
		// define the "copy java declaration" action
        _copyJavaReferenceAction = new AbstractAction( "copy-java-reference") {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				final BeanNode<?>[] nodes = TreeUtility.getSelectedBeanNodes( _viewTree );
				if ( nodes.length > 0 ) {
					final StringBuffer buffer = new StringBuffer();
					for ( final BeanNode<?> node : nodes ) {
						buffer.append( node.getJavaReferenceSnippet() );
						buffer.append( System.getProperty( "line.separator" ) );
					}
					final StringSelection stringSelection = new StringSelection( buffer.toString() );
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents( stringSelection, stringSelection );
				}
            }
        };
        commander.registerAction( _copyJavaReferenceAction );
		
		// define the "copy java declaration" action
        _copyXALReferenceAction = new AbstractAction( "copy-xal-reference") {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				final BeanNode<?>[] nodes = TreeUtility.getSelectedBeanNodes( _viewTree );
				if ( nodes.length > 0 ) {
					final StringBuffer buffer = new StringBuffer();
					for ( final BeanNode<?> node : nodes ) {
						buffer.append( node.getXALReferenceSnippet() );
						buffer.append( System.getProperty( "line.separator" ) );
					}
					final StringSelection stringSelection = new StringSelection( buffer.toString() );
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents( stringSelection, stringSelection );
				}
            }
        };
        commander.registerAction( _copyXALReferenceAction );
		
		// define the "copy jython declaration" action
        _copyJythonReferenceAction = new AbstractAction( "copy-jython-reference") {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				final BeanNode<?>[] nodes = TreeUtility.getSelectedBeanNodes( _viewTree );
				if ( nodes.length > 0 ) {
					final StringBuffer buffer = new StringBuffer();
					for ( final BeanNode<?> node : nodes ) {
						buffer.append( node.getJythonReferenceSnippet() );
						buffer.append( System.getProperty( "line.separator" ) );
					}
					final StringSelection stringSelection = new StringSelection( buffer.toString() );
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents( stringSelection, stringSelection );
				}
            }
        };
        commander.registerAction( _copyJythonReferenceAction );
		
		// define the "copy java import" action
        _copyJavaImportAction = new AbstractAction( "copy-java-import") {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				final StringBuffer buffer = new StringBuffer();
				buffer.append( "import xal.extension.bricks.WindowReference;" );
				buffer.append( System.getProperty( "line.separator" ) );
				final StringSelection stringSelection = new StringSelection( buffer.toString() );
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents( stringSelection, stringSelection );
            }
        };
        commander.registerAction( _copyJavaImportAction );
		
		// define the "copy jython import" action
        _copyJythonImportAction = new AbstractAction( "copy-jython-import") {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				final StringBuffer buffer = new StringBuffer();
				buffer.append( "from xal.extension.bricks import WindowReference" );
				buffer.append( System.getProperty( "line.separator" ) );
				final StringSelection stringSelection = new StringSelection( buffer.toString() );
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents( stringSelection, stringSelection );
            }
        };
        commander.registerAction( _copyJythonImportAction );
		
		updateActions();
	}
	
	
	/** update the actions */
	@SuppressWarnings( "rawtypes" )		// generics are not compatible with arrays
	protected void updateActions() {
		final BeanNode<?> selectedNode = _viewTree != null ? TreeUtility.getSelectedBeanNode( _viewTree ) : null;
		final BeanNode[] selectedNodes = _viewTree != null ? TreeUtility.getSelectedBeanNodes( _viewTree ) : new BeanNode[0];
		
		final boolean hasSelectedNode = selectedNode != null;
		final boolean hasSelectedNodes = selectedNodes.length > 0;
		
		_copyTagAction.setEnabled( hasSelectedNode );
		_copyJavaDeclarationAction.setEnabled( hasSelectedNodes );
		_copyJavaDeclarationAction.setEnabled( hasSelectedNodes );
		_copyXALReferenceAction.setEnabled( hasSelectedNodes );
		_copyJavaReferenceAction.setEnabled( hasSelectedNodes );
		_copyJythonReferenceAction.setEnabled( hasSelectedNodes );
	}
	
	
	
	/** tree event handler */
	protected class TreeHandler implements TreeSelectionListener {
		public void valueChanged( final TreeSelectionEvent event ) {
			updateActions();
		}
	}
}
