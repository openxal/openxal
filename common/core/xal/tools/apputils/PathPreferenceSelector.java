/** PathPreferenceSelector.java
 *
 * Created on December 30, 2003, 9:08 AM
 */

package xal.tools.apputils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.util.prefs.*;
import java.io.*;
import java.net.*;
import java.text.*;


/**
 * Dialog box for selecting a file path and saving it as the default URL spec in a preference.
 * The dialog box presents the user with a field for entering the path or the option of 
 * browsing to the path.  A button allows the user to make the path the default path.
 * The default path is stored as a URL spec via a Preferences instance provided in the constructor.
 *
 * @author  tap
 */
public class PathPreferenceSelector extends JDialog implements ScrollPaneConstants {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	/** indicates whether the user saved changes */
	private boolean _userSavedChanges;
	
	// file browser
	protected CustomChooser chooser;
	
    // controls
    protected JTextArea urlField;
    protected JButton browseButton;
    protected JButton commitButton;
    protected JButton revertButton;
    
	
    /** 
	 * Constructor 
	 * @param defaults The Preferences instance that stores the default URL spec of the path.
	 * @param urlKey The preference key used for associating the URL spec to store.
	 * @param suffix The suffix used to filter files in the file chooser.
	 * @param description  The description used to label the files.
	 */
    public PathPreferenceSelector(final Preferences defaults, final String urlKey, final String suffix, final String description) {
		super();
		setTitle(description);
		setup(defaults, urlKey, suffix, description);
    }
    
	
    /** 
	 * Constructor 
	 * @param owner The frame which owns this dialog.
	 * @param defaults The Preferences instance that stores the default URL spec of the path.
	 * @param urlKey The preference key used for associating the URL spec to store.
	 * @param suffix The suffix used to filter files in the file chooser.
	 * @param description  The description used to label the files.
	 */
    public PathPreferenceSelector(final Frame owner, final Preferences defaults, final String urlKey, final String suffix, final String description) {
		super(owner, description);
		setup(defaults, urlKey, suffix, description);
    }
    
	
    /** 
	 * Constructor 
	 * @param owner The dialog which owns this dialog.
	 * @param defaults The Preferences instance that stores the default URL spec of the path.
	 * @param urlKey The preference key used for associating the URL spec to store.
	 * @param suffix The suffix used to filter files in the file chooser.
	 * @param description  The description used to label the files.
	 */
    public PathPreferenceSelector(final Dialog owner, final Preferences defaults, final String urlKey, final String suffix, final String description) {
		super(owner, description);
		setup(defaults, urlKey, suffix, description);
    }
	
	
	/**
	 * Initialize the dialog box and the file chooser
	 * @param defaults The Preferences instance that stores the default URL spec of the path.
	 * @param urlKey The preference key used for associating the URL spec to store.
	 * @param suffix The suffix used to filter files in the file chooser.
	 * @param description  The description used to label the files.
	 */
	protected void setup(final Preferences defaults, final String urlKey, final String suffix, final String description) {
		_userSavedChanges = false;
		setModal( true );
		chooser = new CustomChooser( defaults, urlKey, suffix, description );
        initComponents();
	}
    
    
    /** 
     * initialize the view
     */
    protected void initComponents() {
        // setup the frame to add components
        final int width = 500, height = 100;
        setSize(width, height);
        setResizable(true);
		
        // add the main panel
		Box mainView = new Box(BoxLayout.Y_AXIS);
        getContentPane().add(mainView);
        
        // make a row
		Box row = new Box(BoxLayout.X_AXIS);
		
        // add the path field label and the path field
        row.add( new JLabel("URL: ") );
        urlField = new JTextArea();
        urlField.setColumns(40);
		urlField.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(urlField, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
        row.add(scrollPane);
		
        // add the browse button
        browseButton = new JButton("Browse");
        row.add(browseButton);
        
        // browse button event handler
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                browsePath();
            }
        });
		
		mainView.add(row);
        
        // add listener of text field actions
        urlField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent event) {
                textChanged(event);
            }
            public void removeUpdate(DocumentEvent event) {
                textChanged(event);
            }
            public void insertUpdate(DocumentEvent event) {
                textChanged(event);
            }
        });
        
        // make a new row
		row = new Box(BoxLayout.X_AXIS);
		row.add( Box.createGlue() );
		
		// add a close button
		JButton closeButton = new JButton("Close");
		row.add(closeButton);
		
		// close button event handler
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				close();
			}
		});
        
        // add the revert button
        revertButton = new JButton("Revert");
        row.add(revertButton);

        // commit button event handler
        revertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                revertPath();
            }
        });
        
        // add the commit button
        commitButton = new JButton("Make Default");
        row.add(commitButton);

        // commit button event handler
        commitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                commitChanges();
            }
        });
		
		// add the button row to the main view
		mainView.add(row);
        
        // update the view to reflect the model
        updateView();
		
		addWindowListener( new WindowAdapter() {
			public void windowOpened(WindowEvent event) {
				revertPath();
			}
		});
		
		// initialize the view
		revertPath();
        
        // pack the frame
        pack();
    }
    
    
    /** 
	 * Handle the browse button action 
	 */
    protected void browsePath() {
        chooser.showWithOwner(this);
        if ( chooser.approved() ) {
            File file = chooser.selection();
			try {
				urlField.setText( file.toURI().toURL().toString() );
			}
			catch( Exception exception ) {
				revertPath();
			}
        }
    }
    
    
    /** 
	 * Handle the commit button action 
	 */
    protected void commitChanges() {
		try {
			chooser.setDefaultURLSpec( urlField.getText() );     // make this file the new default
			updateView();
			_userSavedChanges = true;
		}
		catch(Exception exception) {
			Toolkit.getDefaultToolkit().beep();
		}
    }
    
    
    /** 
	 * Handle the revert button action 
	 */
    protected void revertPath() {
		try {
			final URL url = chooser.getDefaultURL();
			urlField.setText( url != null ? url.toString() : "" );
			updateView();
		}
		catch(MalformedURLException exception) {
			urlField.selectAll();
			Toolkit.getDefaultToolkit().beep();
		}
    }
	
	
	/**
	 * Hide and dispose of the dialog box.
	 */
	protected void close() {
		setVisible( false );
		dispose();
	}
    
    
    /** 
	 * Handle the text changed event by updating the view to reflect the present state.
	 * @param event The document's "text changed" event.
	 */
    protected void textChanged(DocumentEvent event) {
        updateView();
    }
	
	
	/**
	 * Determines whether the user has actually committed changes.
	 * @return true if the user has saved changes
	 */
	public boolean hasSavedChanges() {
		return _userSavedChanges;
	}
	
	
	/**
	 * Determine if there are any unsaved changes
	 * @return true if there are unsaved changes and false if not.
	 */
	public boolean hasUnsavedChanges() {
		try {
			return !urlField.getText().equals( chooser.getDefaultURL().toString() );
		}
		catch(MalformedURLException exception) {
			return true;
		}
	}
    
    
    /** 
	 * Update the view to reflect the model 
	 */
    protected void updateView() {
        final boolean hasUnsavedChanges = hasUnsavedChanges();
        commitButton.setEnabled(hasUnsavedChanges);
        revertButton.setEnabled(hasUnsavedChanges);
    }
}



/** 
 * The custom file chooser which allows the user to select the default file. 
 */
class CustomChooser extends JFileChooser {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	// constants
	final protected Preferences DEFAULTS;
	final protected String URL_KEY;
	
	// state variables
    protected int status;
    
    
	/**
	 * Constructor
	 * @param defaults The preference that stores the default location of the path.
	 * @param urlKey The preference url key.
	 * @param suffix The suffix used to filter files in the file chooser.
	 * @param description  The description used to label the files.
	 */
    public CustomChooser(final Preferences defaults, final String urlKey, final String suffix, final String description) {
        super();
		
		DEFAULTS = defaults;
		URL_KEY = urlKey;
        
        // only accept files of the correct type
        setFileFilter(
            new javax.swing.filechooser.FileFilter() {
                public boolean accept(File file) { 
                    String name = file.getName().toLowerCase();
                    if ( file.isDirectory() || name.endsWith(suffix) ) {
                        return true;
                    }
                    return false;
                }
                public String getDescription() { return description; } 
        });
		
		try {
			final File defaultFile = getDefaultFile();
			if ( defaultFile != null ) {
				setSelectedFile( defaultFile );
			}
		}
		catch( MalformedURLException exception ) {
			exception.printStackTrace();
		}
		catch( URISyntaxException exception ) {
			exception.printStackTrace();
		}
    }
	
	
	/**
	 * Get the user preferences for this class
	 * @return the user preferences for this class
	 */
	protected Preferences getDefaults() {
		return DEFAULTS;
	}
	
	
	/**
	 * Get the default URL as a file path.
	 * @return the default file path
	 * @throws java.net.MalformedURLException if the default URL spec is not a valid URL
	 */
	public String getDefaultPath() throws MalformedURLException {
		try {
			final File defaultFile = getDefaultFile();
			return defaultFile != null ? defaultFile.getPath() : null;
		}
		catch( URISyntaxException exception ) {
			exception.printStackTrace();
			return null;
		}
	}
	
	
	/** Get the default file */
	private File getDefaultFile() throws MalformedURLException, URISyntaxException {
		final URL defaultURL = getDefaultURL();
		return defaultURL != null ? new File( defaultURL.toURI() ) : null;
	}
	
	
	/**
	 * Get the default URL.
	 * @return the default URL
	 * @throws java.net.MalformedURLException if the default URL spec is not a valid URL
	 */
	public URL getDefaultURL() throws MalformedURLException {
		final String urlSpec = getDefaultURLSpec();
		
		return (urlSpec == null) ? null : new URL(urlSpec);
	}
	
	
	/**
	 * Get the default URL spec.
	 * @return the default URL spec.
	 */
	public String getDefaultURLSpec() {
		return DEFAULTS.get(URL_KEY, "");
	}
	
	
	/**
	 * Set the default URL from the specified file path.
	 * @param filePath the file path to set as the default
	 * @throws java.net.MalformedURLException if the file path cannot form a valid URL
	 */
	public void setDefaultPath(final String filePath) throws MalformedURLException {
		setDefaultFile( new File(filePath) );
	}
	
	
	/**
	 * Set the default URL from the specified file.
	 * @param file The file whose URL will become the default.
	 * @throws java.net.MalformedURLException if the file cannot form a valid URL
	 */
	public void setDefaultFile( final File file ) throws MalformedURLException {
		setDefaultURL( file.toURI().toURL() );
	}
	
	
	/**
	 * Set the default URL from the specified URL.
	 * @param url The URL to make the default.
	 */
	public void setDefaultURL(final URL url) {
		setDefaultURLSpec( url.toString() );
	}
	
	
	/**
	 * Set the default URL from the specified URL spec.
	 * @param urlSpec The URL spec to make the default.
	 */
	public void setDefaultURLSpec(final String urlSpec) {
		try {
			DEFAULTS.put(URL_KEY, urlSpec);
			DEFAULTS.flush();
		}
		catch(BackingStoreException exception) {
			exception.printStackTrace();
		}
	}
    
    
	/**
	 * Show this file chooser with the specified component as the owner.
	 * @param owner The component that owns this file chooser.
	 */
    public void showWithOwner(Component owner) {
        status = showOpenDialog(owner);
    }
    
    
	/**
	 * Get the file that was selected by the user.
	 * @return The file that was selected by the user.
	 */
    public File selection() {
        return getSelectedFile();
    }
    
    
	/**
	 * Determine if the file chooser was approved by the user.
	 * @return true if the file chooser was approved and false if it wasn't approved.
	 */
    public boolean approved() {
        return status == JFileChooser.APPROVE_OPTION;
    }
    
    
	/**
	 * Determine if the file chooser was canceled by the user.
	 * @return true if the file chooser was canceled and false if it wasn't canceled.
	 */
    public boolean canceled() {
        return status == JFileChooser.CANCEL_OPTION;
    }    
}


