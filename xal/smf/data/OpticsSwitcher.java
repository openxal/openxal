/*
 * OpticsSwitcher.java
 *
 * Created on Fri May 28 09:12:08 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.smf.data;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import java.text.*;


/**
 * OpticsSwitcher is a provides a view that allows the user to select an optics file as the default optics.
 *
 * @author  tap
 * @since May 28, 2004
 */
public class OpticsSwitcher {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    /** editor for selecting the optics file */
    private final OpticsSelectionEditor SELECTION_EDITOR;
	
    
    /** 
     * Primary Constructor 
     * @param includeDisposeButtons indicates whether to show cancel and close buttons which are appropriate for a hosting dialog
     * @param disposalHandler callback for cancel and close events
     */
    protected OpticsSwitcher( final boolean includeDisposeButtons, final Runnable disposalHandler ) {
        SELECTION_EDITOR = new OpticsSelectionEditor( includeDisposeButtons );
        SELECTION_EDITOR.setDisposalHandler( disposalHandler );
    }
	
    
    /** 
     * Constructor 
     * @param includeDisposeButtons indicates whether to show cancel and close buttons which are appropriate for a hosting dialog
     */
    protected OpticsSwitcher( final boolean includeDisposeButtons ) {
        this( includeDisposeButtons, null );
    }
	
    
    /** Constructor which defaults to showing dispose buttons */
    protected OpticsSwitcher() {
        this( true );
    }
    
    
    /** Create an instance with an automatically configured hosting dialog  */
    static public OpticsSwitcher getInstance() {
        return new OpticsSwitcher( true );
    }
    
    
    /** Create an instance appropriate for a hosted custom frame */
    static public OpticsSwitcher getInstanceForHostedFrame() {
        return new OpticsSwitcher( false );
    }
    
    
    /** Create an instance appropriate for a hosted custom dialog */
    static public OpticsSwitcher getInstanceForHostedDialog( final Runnable disposalHandler ) {
        return new OpticsSwitcher( true, disposalHandler );
    }
    
    
    /** Get the view which may optionally be hosted in a custom window */
    public Component getView() {
        return SELECTION_EDITOR.getView();
    }
    
    
    /** Set the callback to capture the user cancel and close events. Call this method if the view will be hosted in a custom dialog */
    public void setDisposalHandler( final Runnable handler ) {
        SELECTION_EDITOR.setDisposalHandler( handler );
    }
    
    
    /** reset editor to the default settings */
    public void reset() {
        SELECTION_EDITOR.reset();
    }
	
	
	/**
	 * Show the dialog near the dialog's owner
     * @param owner window which owns the dialog
	 */
	public void showDialogNearOwner( final Frame owner ) {
		showDialogWithOwnerNear( owner, owner );
	}
	
	
	/**
	 * Show the dialog near the specified view
	 * @param owner window which owns the dialog
	 * @param view the view near which to show this dialog
	 */
	public void showDialogWithOwnerNear( final Frame owner, final Component view ) {
        final JDialog dialog = new JDialog( owner, true );
        showDialogNear( dialog, view );
	}
	
	
	/**
	 * Show the dialog near the dialog's owner
     * @param owner window which owns the dialog
	 */
	public void showDialogNearOwner( final Dialog owner ) {
		showDialogWithOwnerNear( owner, owner );
	}
	
	
	/**
	 * Show the dialog near the specified view
	 * @param owner window which owns the dialog
	 * @param view the view near which to show this dialog
	 */
	public void showDialogWithOwnerNear( final Dialog owner, final Component view ) {
        final JDialog dialog = new JDialog( owner, true );
        showDialogNear( dialog, view );
	}
	
	
	/**
	 * Show the dialog near the specified view
	 * @param view the view near which to show this dialog
	 */
	private void showDialogNear( final JDialog dialog, final Component view ) {
        reset();
        
        dialog.setTitle( "Set the Default Optics" );
        dialog.getContentPane().add( SELECTION_EDITOR.getView() );  // host the editor view as the dialog's main content
        dialog.pack();
		dialog.setLocationRelativeTo( view );
        
        setDisposalHandler( new Runnable() {
            public void run() {
                dialog.setVisible( false );     // close the dialog
            }
        });
        
		dialog.setVisible( true );
	}
	
	
	/**
	 * Get the path to the default optics file
	 * @return the path to the default optics file
	 */
	public String getDefaultOpticsPath() {
		return AcceleratorChooser.defaultPath(); 
	}
    
    
    /** determine whether the dialog was canceled */
    public boolean isCanceled() {
        return SELECTION_EDITOR.isCanceled();
    }
}



/** Editor of the optics selection */
class OpticsSelectionEditor {
    /** file chooser to select path to optics */
    final private AcceleratorChooser ACCELERATOR_CHOOSER;
    
    /** main view */
    final private Component MAIN_VIEW;
    
    /** field to display and edit the file path */
    final private JTextField PATH_FIELD;
    
    /** button to revert the path selection to the saved value */
    final private JButton REVERT_BUTTON;
    
    /** button to commit the current selection to user preferences */
    final private JButton COMMIT_BUTTON;
    
    /** handler to call when the user cancels or closes the view */
    private Runnable _disposalHandler;
    
    /** indicates whether the dialog was canceled */
    private boolean _isCanceled;

    
    /** Constructor */
    public OpticsSelectionEditor( final boolean includeDisposeButtons ) {
        _isCanceled = false;
        ACCELERATOR_CHOOSER = AcceleratorChooser.getChooser();

        PATH_FIELD = new JTextField( AcceleratorChooser.defaultPath() );
        REVERT_BUTTON = new JButton( "Revert" );
        COMMIT_BUTTON = new JButton( "Make Default" );

        MAIN_VIEW = createView( includeDisposeButtons );
    }
    
    
    /** set the handler to call when the user cancels or closes the view */
    public void setDisposalHandler( final Runnable handler ) {
        _disposalHandler = handler;
    }
    
    
    /** get this editor's view */
    public Component getView() {
        return MAIN_VIEW;
    }
    
    
    /** reset the editor */
    public void reset() {
        PATH_FIELD.setText( AcceleratorChooser.defaultPath() );
        _isCanceled = false;
    }
    
    
    /** determine whether the dialog was canceled */
    public boolean isCanceled() {
        return _isCanceled;
    }
    
    
    /** call the disposal handler if any */
    private void dispose() {
        if ( _disposalHandler != null )  _disposalHandler.run();
    }
    
    
    /** 
	 * Create the main view
     * @return the main editor view
     */
    private Component createView( final boolean includeDisposeButtons ) {
        // add the main panel
        final Box mainView = new Box( BoxLayout.Y_AXIS );
        
        // add the label
		final Box labelRow = new Box( BoxLayout.X_AXIS );
		mainView.add( labelRow );
        labelRow.add( new JLabel( "Path to Default Optics:" ) );
		labelRow.add( Box.createHorizontalGlue() );
        
		// add a row for specifying and browsing to the path
		final Box pathRow = new Box( BoxLayout.X_AXIS );
		mainView.add( pathRow );
		
        // add the path field
        PATH_FIELD.setColumns(40);
		PATH_FIELD.setMaximumSize( PATH_FIELD.getPreferredSize() );
        pathRow.add(PATH_FIELD);
        
        
        // add listener of text field actions
        PATH_FIELD.getDocument().addDocumentListener( new DocumentListener() {
            public void changedUpdate( DocumentEvent evt) {
                textChanged( evt );
            }
            public void removeUpdate( DocumentEvent evt ) {
                textChanged( evt );
            }
            public void insertUpdate( DocumentEvent evt ) {
                textChanged( evt );
            }
        });
        
        
        // add the browse button
        final JButton browseButton = new JButton( "Browse" );
        pathRow.add(browseButton);
        
        // browse button event handler
        browseButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
				browseButtonAction( event );
            }
        });
        
		
		// create a row of buttons for commiting changes
		final Box commitRow = new Box( BoxLayout.X_AXIS );
		mainView.add( commitRow );
		commitRow.add( Box.createHorizontalGlue() );
        
        
        // only include cancel and close buttons if requested (typically included only for a dialog)
        if ( includeDisposeButtons ) {
            // add a cancel button
            final JButton cancelButton = new JButton( "Cancel" );
            cancelButton.setToolTipText( "Cancel the dialog without applying any uncommitted changes and without selecting an accelerator." );
            commitRow.add( cancelButton );
            cancelButton.addActionListener( new ActionListener() {
                public void actionPerformed( final ActionEvent event ) {
                    _isCanceled = true;
                    dispose();
                }
            });
            
            
            // add a close button (close with no changes)
            JButton closeButton = new JButton( "Close" );
            closeButton.setToolTipText( "Close the dialog without applying any uncommitted changes." );
            commitRow.add( closeButton );
            closeButton.addActionListener( new ActionListener() {
                public void actionPerformed( final ActionEvent event ) {
                    dispose();
                }
            });
        }
        
		
        // add the revert button
		REVERT_BUTTON.setToolTipText( "Revert the display back to the default optics settings." );
        commitRow.add( REVERT_BUTTON );
        
        // commit button event handler
        REVERT_BUTTON.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
				// revert to the present default optics file
				PATH_FIELD.setText( AcceleratorChooser.defaultPath() );
				updateView();
            }
        });
        
        
        // add the commit button
		COMMIT_BUTTON.setToolTipText( "Commit the selected path to become the default optics path." );
        commitRow.add( COMMIT_BUTTON );
        
        // commit button event handler
        COMMIT_BUTTON.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
				final String path = PATH_FIELD.getText();
				if ( new File(path).exists() ) {
					// make this file the new default optics
					AcceleratorChooser.setDefaultPath( path );
				}
				else {
					String title = "Error: File does not exist...";
					String message = "The specified file does not exist.\nThe path will not be set.";
					int messageType = JOptionPane.ERROR_MESSAGE;
					JOptionPane.showMessageDialog( mainView, message, title, messageType ); 
				}
				updateView();
            }
        });
        
        // update the view to reflect the model
        updateView();
        
        return mainView;
    }
    
    
    /** 
	 * Browse button action to spawn the file selector
	 * @param event the action event
	 */
    private void browseButtonAction( final ActionEvent event ) {
        ACCELERATOR_CHOOSER.showWithOwner( MAIN_VIEW );
        if ( ACCELERATOR_CHOOSER.approved() ) {
            final File file = ACCELERATOR_CHOOSER.selection();
            PATH_FIELD.setText( file.getAbsolutePath() );
        }
    }
    
    
    /** 
	 * Handle the text changed event
	 * @param event the document event
	 */
    private void textChanged( final DocumentEvent event ) {
        updateView();
    }
    
    
    /** 
	 * Update the view to reflect the model 
	 */
    private void updateView() {
        boolean textSame = PATH_FIELD.getText().equals( AcceleratorChooser.defaultPath() );
        COMMIT_BUTTON.setEnabled( !textSame );
        REVERT_BUTTON.setEnabled( !textSame );
    }
}



/** 
 * Display an open dialog box so the user can pick an accelerator input file 
 */
class AcceleratorChooser extends JFileChooser {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	/** global accelerator chooser */
    static protected AcceleratorChooser chooser;
    protected int status;
    
	
	/**
	 * static initializer
	 */
    static {
        chooser = new AcceleratorChooser();
    }
    
    
	/**
	 * Constructor
	 */
    public AcceleratorChooser() {
        super();
        
        // only accept optics files
        setFileFilter(
            new javax.swing.filechooser.FileFilter() {
                public boolean accept(File file) { 
                    String name = file.getName().toLowerCase();
                    if ( file.isDirectory() || name.endsWith("xal") ) {
                        return true;
                    }
                    return false;
                }
                public String getDescription() { return "Optics Files"; } 
        });

        String defaultFilePath = defaultPath();
        if ( defaultFilePath != null ) {
            File defaultFile = new File(defaultFilePath);
            setSelectedFile(defaultFile);
        }
    }
    
    
	/**
	 * Get the globally accessible accelerator chooser
	 * @return the accelerator chooser
	 */
    static public AcceleratorChooser getChooser() {
        return chooser;
    }
    
    
	/**
	 * Show the chooser relative to the owner
	 * @param owner the owner of the file chooser
	 */
    public void showWithOwner( final Component owner ) {
        status = showOpenDialog( owner );
    }
    
    
	/**
	 * Get the file selection
	 * @return the user's file selection
	 */
    public File selection() {
        return getSelectedFile();
    }
    
    
	/**
	 * Get the default optics path
	 * @return the default optics path
	 */
    static public String defaultPath() {
        return XMLDataManager.defaultPath();
    }
    
    
	/**
	 * Set the default optics path
	 * @param path the default optics path
	 */
    static public void setDefaultPath(String path) {
        XMLDataManager.setDefaultPath(path);
    }
    
    
	/**
	 * Determine if the file selection was approved by the user
	 * @return true if the user approved the file selection and false if not
	 */
    public boolean approved() {
        return status == JFileChooser.APPROVE_OPTION;
    }
    
    
	/**
	 * Determine if the file selection was canceled by the user
	 * @return true if the user canceled the file selection and false if not
	 */
    public boolean canceled() {
        return status == JFileChooser.CANCEL_OPTION;
    }
}

