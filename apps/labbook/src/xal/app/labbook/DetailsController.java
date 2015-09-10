//
//  DetailsController.java
//  xal
//
//  Created by Thomas Pelaia on 9/25/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import xal.extension.bricks.WindowReference;
import xal.extension.application.Commander;
import xal.tools.IconLib;
import xal.tools.messaging.MessageCenter;

import java.awt.Component;
import java.awt.event.*;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.util.*;


/** Controller for managing the details view */
public class DetailsController extends AbstractController {
	/** text view */
	final protected JEditorPane DETAILS_PANE;
	
	/** HTML corresponding to an empty document */
	final protected String EMPTY_DOCUMENT_HTML;
	
	
    /** Create a new controller */
    public DetailsController( final MessageCenter messageCenter, final WindowReference windowReference ) {
		super( messageCenter, windowReference );
		
		DETAILS_PANE = (JEditorPane)windowReference.getView( "DetailsEditorPane" );
		EMPTY_DOCUMENT_HTML = DETAILS_PANE.getText();
		
		monitorDocumentChanges();
    }
	
	
	/** monitor document changes */
	protected void monitorDocumentChanges() {
        DETAILS_PANE.getDocument().addDocumentListener( new DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent evt) {
				postDocumentChangeEvent();
            }
            public void removeUpdate(DocumentEvent evt) {
				postDocumentChangeEvent();
            }
            public void insertUpdate(DocumentEvent evt) {
				postDocumentChangeEvent();
            }
        });		
	}
	
	
	/** register text actions */
	static protected void registerCommands( final Commander commander ) {
		final int menuKeyShortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		final ComponentFocusTracker focusTracker = new ComponentFocusTracker();
		
        final Action[] actions = new JEditorPane( "text/html", "" ).getActions();
		final List<Action> textActions = new ArrayList<Action>();
        for ( final Action action : actions ) {
			if ( action.getValue( Action.NAME ).toString().equals( "font-bold" ) ) {
				action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_B, menuKeyShortcutMask ) );
				action.putValue( Action.SMALL_ICON, IconLib.getIcon( IconLib.IconGroup.TEXT, "Bold24.gif" ) );
			}
			else if ( action.getValue( Action.NAME ).toString().equals( "font-italic" ) ) {
				action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_I, menuKeyShortcutMask ) );
				action.putValue( Action.SMALL_ICON, IconLib.getIcon( IconLib.IconGroup.TEXT, "Italic24.gif" ) );
			}
			else if ( action.getValue( Action.NAME ).toString().equals( "font-underline" ) ) {
				action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_U, menuKeyShortcutMask ) );
				action.putValue( Action.SMALL_ICON, IconLib.getIcon( IconLib.IconGroup.TEXT, "Underline24.gif" ) );
			}
			else if ( action.getValue( Action.NAME ).toString().equals( "left-justify" ) ) {
				action.putValue( Action.SMALL_ICON, IconLib.getIcon( IconLib.IconGroup.TEXT, "AlignLeft24.gif" ) );
			}
			else if ( action.getValue( Action.NAME ).toString().equals( "center-justify" ) ) {
				action.putValue( Action.SMALL_ICON, IconLib.getIcon( IconLib.IconGroup.TEXT, "AlignCenter24.gif" ) );
			}
			else if ( action.getValue( Action.NAME ).toString().equals( "right-justify" ) ) {
				action.putValue( Action.SMALL_ICON, IconLib.getIcon( IconLib.IconGroup.TEXT, "AlignRight24.gif" ) );
			}
			else {
				continue;
			}
			textActions.add( action );
			commander.registerAction( action );
			action.setEnabled( false );
        }
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener( "permanentFocusOwner", new PropertyChangeListener() {
			public void propertyChange( final PropertyChangeEvent event ) {
				final boolean enableActions = focusTracker.getLastEditorPane() != null;
				for ( final Action action : textActions ) {
					action.setEnabled( enableActions );
				}
			}
		});		
	}
	
	
	/**
	 * Get the details text
	 * @return the details text
	 */
	public String getHTML() {
		return DETAILS_PANE.getText();
	}
	
	
	/**
	 * Determine whether the document is empty
	 * @return true if the document is empty and false if not
	 */
	public boolean isEmpty() {
		return EMPTY_DOCUMENT_HTML.equals( getHTML() );
	}
}



/** track the last non-button, non-null component which was focused for purposes of edit operations */
class ComponentFocusTracker {
	/** last swing component to be focused which is not a button */
	protected JComponent _lastFocusedComponent;
	
	/** last window to be in focus */
	protected Window _focusedWindow;
	
	
	/** refresh the last focused component based on rules to avoid defocusing a component when a button is pressed */
	public void refresh() {
		final KeyboardFocusManager keyboardManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		final Window focusedWindow = keyboardManager.getFocusedWindow();
		final Component component = keyboardManager.getPermanentFocusOwner();
		
		if ( focusedWindow != _focusedWindow ) {	// if the window changes, accept the new focused swing component
			if ( component != null ) {
				_lastFocusedComponent = component instanceof JComponent ? (JComponent)component : null;	// only accept swing components
			}
			else {
				_lastFocusedComponent = null;
			}
		}
		else if ( component == null || component instanceof AbstractButton ) {	// don't consider buttons as having meaningful focus for editing
		}
		else if ( component instanceof JComponent )  {	// only accept swing components
			_lastFocusedComponent = (JComponent)component;
		}
		else {
			_lastFocusedComponent = null;
		}
		
		_focusedWindow = focusedWindow;
	}
	
	
	/** get the last focused component */
	public JComponent getLastFocusedComponent() {
		refresh();
		return _lastFocusedComponent;
	}
	
	
	/** get the last editor pane */
	public JEditorPane getLastEditorPane() {
		refresh();
		return _lastFocusedComponent != null && _lastFocusedComponent instanceof JEditorPane ? (JEditorPane)_lastFocusedComponent : null;
	}
}

