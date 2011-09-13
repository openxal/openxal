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
 * OpticsSwitcher is a dialog that allows the user to select an optics file as the default
 * optics.
 *
 * @author  tap
 * @since May 28, 2004
 */
public class OpticsSwitcher extends JDialog {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	// GUI components
    protected AcceleratorChooser accelChooser;
	protected JTextField pathField;
	protected JButton revertButton;
	protected JButton commitButton;
    
    /** indicates whether the dialog was canceled */
    private boolean isCanceled;
	
    
    /** Constructor */
    public OpticsSwitcher() {
		super();
		setup();
    }

    
    /** 
	 * Constructor 
	 * @param owner the window that owns this dialog
	 */
    public OpticsSwitcher(Dialog owner) {
		super(owner);
		setup();
    }

    
    /** 
	 * Constructor 
	 * @param owner the window that owns this dialog
	 */
    public OpticsSwitcher(Frame owner) {
		super(owner);
		setup();
    }

    
    /** 
	 * Constructor 
	 * @param owner the window that owns this dialog
	 * @param modal true for a modal dialog and false for a non-modal dialog
	 */
    public OpticsSwitcher(Dialog owner, boolean modal) {
		super(owner, modal);
		setup();
    }

    
    /** 
	 * Constructor 
	 * @param owner the window that owns this dialog
	 * @param modal true for a modal dialog and false for a non-modal dialog
	 */
    public OpticsSwitcher(Frame owner, boolean modal) {
		super(owner, modal);
		setup();
    }
	
	
	/**
	 * Setup the switcher with the default title
	 */
	protected void setup() {
        isCanceled = false;
		setup("Set the Default Optics");
	}
	
	
	/**
	 * Setup the switcher
	 * @param title the title to appear in the dialog box
	 */
	protected void setup(final String title) {
		setTitle(title);
        initComponents();
        accelChooser = AcceleratorChooser.getChooser();        
	}
	
	
	/**
	 * Show the dialog near the specified view
	 * @param view the view near which to show this dialog
	 */
	public void showNear(Component view) {
        isCanceled = false;
		setLocationRelativeTo(view);
		setVisible( true );
	}
	
	
	/**
	 * Show the dialog near the dialog's owner
	 */
	public void showNearOwner() {
		showNear( getOwner() );
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
        return isCanceled;
    }
    
    
    /** 
	 * Create the main view
     */
    protected void initComponents() {
        // add the main panel
        final Box mainView = new Box(BoxLayout.Y_AXIS);
        //mainView.setPreferredSize(new Dimension(width, height));
        getContentPane().add(mainView);
        
        // add the label
		Box labelRow = new Box(BoxLayout.X_AXIS);
		mainView.add(labelRow);
        labelRow.add( new JLabel("Path to Default Optics:") );
		labelRow.add( Box.createHorizontalGlue() );
        
		// add a row for specifying and browsing to the path
		Box pathRow = new Box(BoxLayout.X_AXIS);
		mainView.add(pathRow);
		
        // add the path field
        pathField = new JTextField( AcceleratorChooser.defaultPath() );
        pathField.setColumns(40);
		pathField.setMaximumSize(pathField.getPreferredSize());
        pathRow.add(pathField);
        
        
        // add listener of text field actions
        pathField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent evt) {
                textChanged(evt);
            }
            public void removeUpdate(javax.swing.event.DocumentEvent evt) {
                textChanged(evt);
            }
            public void insertUpdate(javax.swing.event.DocumentEvent evt) {
                textChanged(evt);
            }
        });
        
        
        // add the browse button
        final JButton browseButton = new JButton("Browse");
        pathRow.add(browseButton);
        
        // browse button event handler
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent event) {
				browseButtonAction(event);
            }
        });
				
		
		// create a row of buttons for commiting changes
		final Box commitRow = new Box(BoxLayout.X_AXIS);
		mainView.add(commitRow);
		commitRow.add( Box.createHorizontalGlue() );
        
        
        // add a cancel button
        final JButton cancelButton = new JButton( "Cancel" );
		cancelButton.setToolTipText( "Cancel the dialog without applying any uncommitted changes and without selecting an accelerator." );
		commitRow.add( cancelButton );
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
                isCanceled = true;
				dispose();
			}
		});
        
        
		// add a close button (close with no changes)
		JButton closeButton = new JButton("Close");
		closeButton.setToolTipText("Close the dialog without applying any uncommitted changes.");
		commitRow.add(closeButton);
		closeButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});

		
        // add the revert button
        revertButton = new JButton("Revert");
		revertButton.setToolTipText("Revert the display back to the default optics settings.");
        commitRow.add(revertButton);

        // commit button event handler
        revertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
				// revert to the present default optics file
				pathField.setText( AcceleratorChooser.defaultPath() );
				updateView();
            }
        });
        
        
        // add the commit button
        commitButton = new JButton("Make Default");
		commitButton.setToolTipText("Commit the selected path to become the default optics path.");
        commitRow.add(commitButton);

        // commit button event handler
        commitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
				final String path = pathField.getText();
				if ( new File(path).exists() ) {
					// make this file the new default optics
					accelChooser.setDefaultPath(path);
				}
				else {
					String title = "Error: File does not exist...";
					String message = "The specified file does not exist.\nThe path will not be set.";
					int messageType = JOptionPane.ERROR_MESSAGE;
					JOptionPane.showMessageDialog(OpticsSwitcher.this, message, title, messageType); 
				}
				updateView();
            }
        });
        
        // update the view to reflect the model
        updateView();
        
        // pack the window
        pack();
        setResizable(false);        
    }
    
    
    /** 
	 * Browse button action to spawn the file selector
	 * @param event the action event
	 */
    protected void browseButtonAction(ActionEvent event) {
        accelChooser.showWithOwner(this);
        if ( accelChooser.approved() ) {
            File file = accelChooser.selection();
            pathField.setText( file.getAbsolutePath() );
        }
    }
    
    
    /** 
	 * Handle the text changed event
	 * @param event the document event
	 */
    protected void textChanged(DocumentEvent event) {
        updateView();
    }
    
    
    /** 
	 * Update the view to reflect the model 
	 */
    protected void updateView() {
        boolean textSame = pathField.getText().equals( AcceleratorChooser.defaultPath() );
        commitButton.setEnabled(!textSame);
        revertButton.setEnabled(!textSame);
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
    public void showWithOwner(javax.swing.JDialog owner) {
        status = showOpenDialog(owner);
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

