/*
 * ActionFactory.java
 *
 * Created on March 18, 2003, 5:12 PM
 */

package xal.extension.application;

import xal.tools.URLReference;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.net.URL;
import java.util.*;
import java.beans.*;

/**
 * ActionFactory is a factory class with methods that can instantiate actions for 
 * use in menus and the toolbar.  All of the actions are standard document-based 
 * application actions.  In addition to actions, it also defines menu handlers that 
 * get invoked when a menu is activated.  Typically it menu handlers are used
 * to dynamically populate menus.  For example there is "documents" submenu under 
 * the "Window" menu that populates itself upon selection with a list of menu items 
 * having one menu item for each open document.
 *
 * @author  t6p
 */
public class ActionFactory {
    static final int MENU_KEY_SHORTCUT_MASK = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    
    /** Creates a new instance of ActionFactory */
    private ActionFactory() {}
    
    
    // --------- File menu actions ---------------------------------------------
    
    /**
     * Make an action that creates a new document in the application.  
     * This action is usually associated with the "new" menu item in the "File" menu.
     * @return An action that creates a new document
     */
    static Action newAction() {
        final Action action = new AbstractAction() {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().newDocument();
            }
        };
        
        action.putValue( Action.NAME, "new-document" );
        action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_N, MENU_KEY_SHORTCUT_MASK ) );
        
        return action;
    }
    
    
    /**
     * Make an action that creates a new document from a user selected template.  
     * This action is usually associated with the "New from Template..." menu item in the "File" menu.
     * @return An action that creates a new document from a user selected template
     */
    static Action newDocumentFromTemplateAction() {
        final Action action = new AbstractAction() {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().newDocumentFromTemplate();
            }
        };
        
        action.putValue( Action.NAME, "new-document-from-template" );
        final int keyMask = MENU_KEY_SHORTCUT_MASK | java.awt.event.InputEvent.SHIFT_MASK;  // shortcut key plus shift key
        action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_N, keyMask ) );
        
        return action;
    }
    
    
    /**
	 * Make an action that creates a new document for the associated document type.
     * @return An action that produces a new document of the appropriate type.
     */
    static Action newDocumentByTypeAction() {
        final Action action = new AbstractAction() {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				final JMenuItem menuItem = (JMenuItem)event.getSource();
                Application.getApp().newDocument( menuItem.getText() );
            }
        };
        
        action.putValue( Action.NAME, "new-document-by-type" );
        
        return action;
    }
    
    
    /**
     * Make an action that presents the user with an open file dialog.  If the user 
     * selects one or more files, those files are opened as XalDocument subclasses and 
     * added to the application's list of open documents.
     * This action is usually associated with the "Open..." menu item in the "File" menu.
     * @return An action that opens a document
     */
    static Action openDocumentAction() {
        final Action action = new AbstractAction( "open-document" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().openDocument();
            }
        };
        
        action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_O, MENU_KEY_SHORTCUT_MASK ) );
		
		action.setEnabled( Application.getAdaptor().readableDocumentTypes().length > 0 );
        
        return action;
    }
    

    /**
     * Make an action that is used internally to open a file as specified by the 
     * given URL specification into a new document.  This action is used by the 
     * menu items in the dynamically generated submenu "Open Recent" in the "File" menu.
     * The "Open Recent" submenu lists the URL specifications of the recently opened 
     * files.
     * @param urlSpec The URL specification of the file to open
     * @return An action that opens a file corresponding to the given URL specification
     * @see #openRecentHandler
     */
    static protected Action openURLAction( final String urlSpec ) {
        final Action action = new AbstractAction( "open-file" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().openURL( urlSpec );
            }
        };
        
        return action;
    }
    
    
    /**
     * Make a menu handler that is used to generate a list of recently opened files to 
     * be available under the "Open Recent" submenu of the "File" menu.  A "clear" menu 
     * item appears at the end of the list and allows the user to clear the list.   When 
     * the submenu is selected, the submenu gets populated with the list of recently opened 
     * files.  The list is stored in the user preferences and the Application class manages the list. 
     * @return An action that opens a file corresponding to the given URL specification
     * @see #openURLAction
     */
    static MenuListener openRecentHandler() {
        MenuListener handler = new MenuListener() {
            /** MenuListener interface */
            public void menuSelected( final MenuEvent event ) {
                final JMenu menu = (JMenu)event.getSource();
                menu.removeAll();
				
                final URL defaultFolder = Application.getApp().getDefaultDocumentFolderURL();
                final String[] recentURLSpecs = Application.getApp().getRecentURLSpecs();
                for ( int index = 0 ; index < recentURLSpecs.length ; index++ ) {
                    final String urlSpec  = recentURLSpecs[index];
                    final URLReference urlRef = URLReference.getInstance( defaultFolder, urlSpec );
                    JMenuItem menuItem = new JMenuItem( urlRef.toString() );
                    menu.add( menuItem );
                    menuItem.setAction( openURLAction( urlSpec ) );
                    menuItem.setText( urlRef.toString() );
                    menuItem.setEnabled( urlRef.isValid() );
                }
				
                menu.addSeparator();
				
                final JMenuItem clearItem = new JMenuItem( "Clear" );
                clearItem.setAction( new AbstractAction() {
                    private static final long serialVersionUID = 1L;
                    
                    public void actionPerformed( final ActionEvent event ) {
                        Application.getApp().clearRecentItems();
                    }
                });
                clearItem.setText( "Clear" );
                menu.add( clearItem );
            }


            /** MenuListener interface */
            public void menuCanceled( final MenuEvent event ) {}


            /** MenuListener interface */
            public void menuDeselected( final MenuEvent event ) {}
        };
        
        return handler;
    }
    
    
    /**
     * Make an action that presents the user with an open dialog to select a version of the current document to open
     * @param document The document for which to open a version.
     * @return An action that opens the document version
     */
    static Action openDocumentVersionAction( final XalDocument document ) {
        final Action action = new AbstractAction( "open-version" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().openDocumentVersion( document );
            }
        };
        
        
        // listen if the document has changes to be saved
        document.addXalDocumentListener( new XalDocumentListener() {
            public void titleChanged( final XalDocument document, final String newTitle ) {
                action.setEnabled( document.getSource() != null && Application.getApp().getDefaultDocumentFolder() != null );
            }
            
            public void hasChangesChanged( final XalDocument document, final boolean newHasChangesStatus ) {}
            public void documentWillClose( final XalDocument document ) {}
            public void documentHasClosed( final XalDocument document ) {}
        });
        
        // to open a version for a document, the document must have a backing source and the default document folder must be set since versions are stored under that tree
        action.setEnabled( document.getSource() != null && Application.getApp().getDefaultDocumentFolder() != null );
        
        return action;
    }

    
    /**
     * Make an action that closes the document where the from which the menu item was selected.
     * This action is usually associated with the "Close" menu item in the "File" menu.
     * @param document The document to close
     * @return An action that closes the document
     */
    static Action closeDocumentAction( final XalAbstractDocument document ) {        
        final Action action = new AbstractAction( "close-document" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().closeDocument( document );
            }
        };
        
        action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_W, MENU_KEY_SHORTCUT_MASK ) );
        
        return action;
    }
    
    
    /**
     * Make an action that closes all documents in the application which leads to 
     * termination of the application.
     * This action is usually associated with the "Close All" menu item in the "File" menu.
     * @return An action that closes all documents in the application
     */
    static Action closeAllDocumentsAction() {
        final Action action = new AbstractAction( "close-all-documents" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().closeAllDocuments();
            }
        };
        
        return action;
    }
    
    
    /**
     * Make an action that presents the user with an save file dialog for a new document or
     * just save the present document to its existing file source if it is not new.  
     * In the case were the document is new: if the user 
     * selects "Save" after navigating to a folder and naming a new file, the present 
     * document is saved to the selected file.
     * This action is usually associated with the "Save" menu item in the "File" menu.
     * @param document The document to save.
     * @return An action that saves a document
     * @see Application#saveDocument
     */
    static Action saveDocumentAction( final XalDocument document ) {
        final Action action = new AbstractAction( "save-document" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().saveDocument( document );
            }
        };
        
        // listen if the document has changes to be saved
        document.addXalDocumentListener( new XalDocumentListener() {
            public void titleChanged( final XalDocument document, final String newTitle ) {}
            
            public void hasChangesChanged( final XalDocument document, final boolean newHasChangesStatus ) {
                action.setEnabled( document.hasChanges() );
            }
            
            public void documentWillClose( final XalDocument document ) {}
            public void documentHasClosed( final XalDocument document ) {}
        });
        
        action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_S, MENU_KEY_SHORTCUT_MASK ) );
        action.setEnabled( document.hasChanges() );

        return action;
    }
    
    
    /**
	 * Make an action that presents the user with an save file dialog for a new document or
     * just save the present document to its existing file source if it is not new.  
     * In the case were the document is new: if the user 
     * selects "Save" after navigating to a folder and naming a new file, the present 
     * document is saved to the selected file.
     * This action is usually associated with the "Save" menu item in the "File" menu.
     * @param document The document to save.
     * @return An action that saves a document
     * @see Application#saveDocument
     */
    static Action saveDocumentAction( final XalInternalDocument document ) {        
        final Action action = new AbstractAction( "save-document" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().saveDocument( document );
            }
        };
        
        // listen if the document has changes to be saved
        document.addXalInternalDocumentListener( new XalInternalDocumentListener() {
            public void titleChanged( final XalInternalDocument document, final String newTitle ) {}
            
            public void hasChangesChanged( final XalInternalDocument document, final boolean newHasChangesStatus ) {
                action.setEnabled( document.hasChanges() );
            }
            
            public void documentWillClose( final XalInternalDocument document ) {}
            public void documentHasClosed( final XalInternalDocument document ) {}
			public void documentActivated( XalInternalDocument document ) {}
			public void documentDeactivated( XalInternalDocument document ) {}
		});
        
        action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_S, MENU_KEY_SHORTCUT_MASK ) );
        action.setEnabled( document.hasChanges() );
		
        return action;
    }
    
    
    /**
     * Make an action that presents the user with an save file dialog.  If the user 
     * selects "Save" after navigating to a folder and naming a new file, the present 
     * document is saved to the selected file.
     * This action is usually associated with the "Save As..." menu item in the "File" menu.
     * @param document The document to save.
     * @return An action that saves a document to a selected file
     * @see Application#saveDocument
     */
    static Action saveAsDocumentAction( final XalAbstractDocument document ) {
        final Action action = new AbstractAction( "save-as-document" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().saveAsDocument( document );
            }
        };
        
		action.setEnabled( document.writableDocumentTypes().length > 0 );
		
        return action;
    }
    
    
    /**
     * Make an action that saves all open documents.
     * @return An action that saves all open documents
     * @see #saveDocumentAction
     */
    static Action saveAllDocumentsAction() {
        final Action action = new AbstractAction( "save-all-documents" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().saveAllDocuments();
            }
        };
		
		action.setEnabled( Application.getAdaptor().writableDocumentTypes().length > 0 );
        
        return action;
    }
    
    
    /**
     * Make an action that reverts a document to its source disposing of any 
     * changes the user has made to the document.
     * @param document The document to be reverted
     * @return An action that reverts a document to its source
     */
    static Action revertToSavedAction( final XalDocument document ) {
        final Action action = new AbstractAction( "revert-to-saved" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().revertToSaved( document );
            }
        };
		
		action.setEnabled((document.getSource() != null) && document.hasChanges());
        
        // listen if the document has changes to be saved
        document.addXalDocumentListener( new XalDocumentListener() {
            public void titleChanged( final XalDocument document, final String newTitle ) {
				action.setEnabled( shouldEnableRevert() );
			}
            
            public void hasChangesChanged( final XalDocument document, final boolean newHasChangesStatus ) {
				action.setEnabled( shouldEnableRevert() );
			}
            public void documentWillClose( final XalDocument document ) {}
            public void documentHasClosed( final XalDocument document ) {}
			public boolean shouldEnableRevert() {
				return ( document.getSource() != null ) && ( document.hasChanges() );
			}
        });
        
        return action;
    }
    
    
    /**
	 * Make an action that reverts a document to its source disposing of any 
     * changes the user has made to the document.
     * @param document The document to be reverted
     * @return An action that reverts a document to its source
     */
    static Action revertToSavedAction( final XalInternalDocument document ) {
        final Action action = new AbstractAction( "revert-to-saved" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().revertToSaved( document );
            }
        };
		
		action.setEnabled((document.getSource() != null) && document.hasChanges());
        
        // listen if the document has changes to be saved
        document.addXalInternalDocumentListener( new XalInternalDocumentListener() {
            public void titleChanged( final XalInternalDocument document, final String newTitle ) {
				action.setEnabled( shouldEnableRevert() );
			}
            
            public void hasChangesChanged( final XalInternalDocument document, final boolean newHasChangesStatus ) {
				action.setEnabled( shouldEnableRevert() );
			}
            public void documentWillClose( final XalInternalDocument document ) {}
            public void documentHasClosed( final XalInternalDocument document ) {}
			
			public boolean shouldEnableRevert() {
				return ( document.getSource() != null ) && ( document.hasChanges() );
			}
			
			public void documentActivated( XalInternalDocument document ) {}
			public void documentDeactivated( XalInternalDocument document ) {}
        });
        
        return action;
    }
    
    
    /**
     * Make an action that shows a standard page-setup dialog box for formatting printing.
     * @return An action for showing a standard page-setup dialog box
     */
    static Action printAction( final XalAbstractDocument document ) {
        final Action action = new AbstractAction( "print-document" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                PrintManager.defaultManager().print( document );
            }
        };
        
        action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_P, MENU_KEY_SHORTCUT_MASK ) );
        
        return action;
    }
    
    
    /**
     * Make an action that shows a standard page-setup dialog box for formatting printing.
     * @return An action for showing a standard page-setup dialog box
     */
    static Action pageSetupAction() {
        return new AbstractAction( "page-setup" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                PrintManager.defaultManager().pageSetup();
            }
        };
    }
    
    
    /**
     * Make an action for quitting the application.
     * @return An action for quitting the application
     */
    static Action quitAction() {
        final Action action = new AbstractAction( "quit-application" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().quit();
            }
        };
        
        action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_Q, MENU_KEY_SHORTCUT_MASK ) );
        
        return action;
    }
    
    
    
    // --------- Edit menu actions ---------------------------------------------

	
	/**
	 * Perform a copy operation.
	 * @return an action for performing a copy operation on the component in focus
	 */
	static Action copyAction() {
		final ComponentFocusTracker focusTracker = new ComponentFocusTracker();
		
        final Action action = new AbstractAction( "copy-to-clipboard" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				final TransferHandler transferHandler = focusTracker.getLastTransferHandler();
				if ( transferHandler != null ) {
					final JComponent sourceComponent = focusTracker.getLastFocusedComponent();
					final int supportedActions = transferHandler.getSourceActions( sourceComponent );
					if ( canPerformCopyOnComponent( sourceComponent ) ) {
						transferHandler.exportToClipboard( sourceComponent, Toolkit.getDefaultToolkit().getSystemClipboard(), TransferHandler.COPY );
					}
					else {
						Toolkit.getDefaultToolkit().beep();
					}
				}
            }
        };
		
		action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_C, ActionFactory.MENU_KEY_SHORTCUT_MASK ) );
		action.setEnabled( canPerformCopyOnComponent( focusTracker.getLastFocusedComponent() ) );
 		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener( "permanentFocusOwner", new PropertyChangeListener() {
			public void propertyChange( final PropertyChangeEvent event ) {
				action.setEnabled( canPerformCopyOnComponent( focusTracker.getLastFocusedComponent() ) );
			}
		});		
		
        return action;		
	}
	
	
	/** determine if a copy can be performed on the specified component */
	static boolean canPerformCopyOnComponent( final JComponent component ) {
		if ( component != null ) {
			final TransferHandler transferHandler = component.getTransferHandler();
			if ( transferHandler != null ) {
				final int supportedActions = transferHandler.getSourceActions( component );
				return supportedActions == TransferHandler.COPY || supportedActions == TransferHandler.COPY_OR_MOVE;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * Perform a cut operation.
	 * @return an action for performing a cut operation on the component in focus
	 */
	static Action cutAction() {
 		final ComponentFocusTracker focusTracker = new ComponentFocusTracker();
		
        final Action action = new AbstractAction( "cut-to-clipboard" ) {
           private static final long serialVersionUID = 1L;
           
            public void actionPerformed( final ActionEvent event ) {
				final TransferHandler transferHandler = focusTracker.getLastTransferHandler();
				if ( transferHandler != null ) {
					final JComponent sourceComponent = focusTracker.getLastFocusedComponent();
					if ( canPerformCutOnComponent( sourceComponent ) ) {
						transferHandler.exportToClipboard( sourceComponent, Toolkit.getDefaultToolkit().getSystemClipboard(), TransferHandler.MOVE );
					}
					else {
						System.out.println( "Beeping..." );
						Toolkit.getDefaultToolkit().beep();
					}
				}
            }
        };
		
		action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_X, ActionFactory.MENU_KEY_SHORTCUT_MASK ) );
		action.setEnabled( canPerformCutOnComponent( focusTracker.getLastFocusedComponent() ) );
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener( "permanentFocusOwner", new PropertyChangeListener() {
			public void propertyChange( final PropertyChangeEvent event ) {
				action.setEnabled( canPerformCutOnComponent( focusTracker.getLastFocusedComponent() ) );
			}
		});		
		
        return action;		
	}
	
	
	/** determine if a cut can be performed on the specified component */
	static boolean canPerformCutOnComponent( final JComponent component ) {
		if ( component != null ) {
			final TransferHandler transferHandler = component.getTransferHandler();
			if ( transferHandler != null ) {
				final int supportedActions = transferHandler.getSourceActions( component );
				return supportedActions == TransferHandler.MOVE || supportedActions == TransferHandler.COPY_OR_MOVE;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * Perform a paste operation.
	 * @return an action for performing a paste operation on the component in focus
	 */
	static Action pasteAction() {
	   final ComponentFocusTracker focusTracker = new ComponentFocusTracker();
		
       final Action action = new AbstractAction( "paste-from-clipboard" ) {
           private static final long serialVersionUID = 1L;
           
           public void actionPerformed( final ActionEvent event ) {
               final TransferHandler transferHandler = focusTracker.getLastTransferHandler();
               if ( transferHandler != null ) {
                   transferHandler.importData( focusTracker.getLastFocusedComponent(), Toolkit.getDefaultToolkit().getSystemClipboard().getContents( this ) );
               }
           }
        };
		
		action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_V, ActionFactory.MENU_KEY_SHORTCUT_MASK ) );
		action.setEnabled( canPerformPasteOnComponent( focusTracker.getLastFocusedComponent() ) );
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener( "permanentFocusOwner", new PropertyChangeListener() {
			public void propertyChange( final PropertyChangeEvent event ) {
				action.setEnabled( canPerformPasteOnComponent( focusTracker.getLastFocusedComponent() ) );
			}
		});		
		
        return action;		
	}
	
	
	/** determine if a paste can be performed on the specified component */
	static boolean canPerformPasteOnComponent( final JComponent component ) {
		if ( component != null ) {
			final TransferHandler transferHandler = component.getTransferHandler();
			if ( transferHandler != null ) {
				return transferHandler.canImport( component, Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors() );
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	
    /**
     * Make an action for editing a preference panel.
     * @return An action for editing a preference panel
     */
	@SuppressWarnings( "rawtypes" )
    static Action editPreferencesAction( final XalDocument document ) {
        final Action action = new AbstractAction( "edit-preferences" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                ((ApplicationAdaptor)Application.getAdaptor()).editPreferences( document );
            }
        };

        try {
            Application.getAdaptor().getClass().getDeclaredMethod( "editPreferences", new Class[] { XalDocument.class } );		// need to suppress raw types
            action.setEnabled( true );
        }
        catch( NoSuchMethodException exception ) {
            action.setEnabled( false );
        }
        
        return action;
    }
	
    /**
	 * Make an action for editing a preference panel.
     * @return An action for editing a preference panel
     */
	@SuppressWarnings( "rawtypes" )
    static Action editPreferencesAction( final XalInternalDocument document ) {
        final Action action = new AbstractAction( "edit-preferences" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                ((DesktopApplicationAdaptor)Application.getAdaptor()).editPreferences( document );
            }
        };
		
        try {
            Application.getAdaptor().getClass().getDeclaredMethod( "editPreferences", new Class[] { XalInternalDocument.class } );	// need to suppress rawtypes
            action.setEnabled( true );
        }
        catch( NoSuchMethodException exception ) {
            action.setEnabled( false );
        }
        
        return action;
    }
    

    
    // --------- View menu actions ---------------------------------------------
    
    /**
     * Make an action that shows the console window.
     * @return An action that shows the console window.
     */
    static Action showConsoleAction() {
        return new AbstractAction( "show-console" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Console.showNear( Application.getActiveWindow() );
            }
        };
    }
    
	
    /**
     * Make an action that shows the logger window.
     * @return An action that shows the logger window.
     */
    static Action showLoggerAction() {
        return new AbstractAction( "show-logger" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                LoggerWindow.getDefault().showFirstTimeNear( Application.getActiveWindow() );
            }
        };
    }
    
    
    // --------- Window menu actions -------------------------------------------
    
    /**
     * Make a menu handler that updates the list of open documents that should  
     * appear in the documents submenu under the Window menu.
     * @return An action that updates the list of open documents
     */
    static MenuListener documentsHandler() {
        MenuListener handler = new MenuListener() {
            /** MenuListener interface */
            public void menuSelected( final MenuEvent event ) {
                JMenu menu = (JMenu)event.getSource();
                menu.removeAll();
				for ( final XalAbstractDocument document : Application.getApp().getDocuments() ) {
                    String title = document.getTitle();
                    JMenuItem menuItem = new JMenuItem( title );
                    menu.add( menuItem );
                    menuItem.setAction( showDocumentAction( document ) );
                    menuItem.setText( title );
                }
            }
            
            
            /** MenuListener interface */
            public void menuCanceled( final MenuEvent event ) {}
            
            
            /** MenuListener interface */
            public void menuDeselected( final MenuEvent event ) {}
        };
        
        return handler;
    }

    
    /**
     * Make an internal action used by the documentsHandler to show a specified document
     * when that document is selected from the documents submenu.
     * @param document The document to be shown
     * @return An action that displays the window of the specified document 
     * @see #documentsHandler
     */
    static protected Action showDocumentAction( final XalAbstractDocument document ) {
        return new AbstractAction( "show-document" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                document.showDocument();
            }
        };
    }
    

    /**
     * Cascade all document windows relative to the location of the window 
     * where the action was fired.
     * @param document The document about whose window all other windows will cascade
     * @return An action that cascades all windows in the application 
     */
    static protected Action cascadeWindowsAction( final XalAbstractDocument document ) {
        return new AbstractAction( "cascade-windows" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().cascadeWindowsAbout( document );
            }
        };
    }
    

    /**
     * Make an action that shows all of the open documents.
     * @return An action that displays the main window for each open document
     */
    static Action showAllWindowsAction() {
        return new AbstractAction( "show-all-windows" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().showAllWindows();
            }
        };
    }
    
    
    /**
     * Make an action taht hides all windows.
     * @return An action that hides all windows.
     */
    static Action hideAllWindowsAction() {
        final Action action = new AbstractAction( "hide-all-windows" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.getApp().hideAllWindows();
            }
        };
        
        action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_H, MENU_KEY_SHORTCUT_MASK ) );
        
        return action;        
    }
    
    
    /**
     * Make an action that captures the main window corresponding to the document
     * as a PNG image and allows the user to save it to a file.
     * @param document The document whose main window should be captured
     * @return An action that captures the main window as a PNG image
     */
    static public Action captureWindowAsImageAction( final XalAbstractDocument document ) {
        return new AbstractAction( "capture-as-image" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                document.getDocumentView().captureAsImage();
            }
        };
    }
    
    
    // --------- Help menu actions ---------------------------------------------

    /**
     * Make an action that shows the about box which presents information 
     * about the application.
     * @return An action that shows the about box
     */
    static Action showAboutBoxAction() {
        final Action action = new AbstractAction( "show-about-box" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                Application.showAboutBox();
            }
        };
        
        action.setEnabled( AboutBox.isAvailable() );
        
        return action;
    }
    

    /**
     * Make an action that shows the help window.
     * @return An action that shows the help window
     */
    static Action showHelpWindow() {
        final Action action = new AbstractAction( "show-help-contents" ) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                HelpWindow.showNear( Application.getActiveWindow() );
            }
        };
        
        action.setEnabled( HelpWindow.isAvailable() );
        action.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_HELP, 0 ) );
        
        return action;        
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
	
	
	/** get the last transfer handler */
	public TransferHandler getLastTransferHandler() {
		refresh();
		return _lastFocusedComponent != null ? _lastFocusedComponent.getTransferHandler() : null;
	}
}

