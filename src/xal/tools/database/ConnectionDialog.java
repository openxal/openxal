/*
 *  ConnectionDialog.java
 *
 *  Created on Fri Feb 20 15:15:21 EST 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.tools.database;

import java.util.*;
import java.util.logging.*;
import java.io.File;
import java.io.IOException;
import java.net.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.*;
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.sql.Connection;


/**
 * ConnectionDialog displays a dialog allowing the user to supply the database URL, their user
 * ID and their password. A connection dictionary is returned to the user based on their input.
 * @author   tap
 */
public class ConnectionDialog extends JDialog {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	/** label for the submit button */
	private final String SUBMIT_LABEL;

	/** the connection dictionary selected by the user */
	private ConnectionDictionary _dictionary;
	
	/** database configuration */
	private DBConfiguration _configuration;
	
	/** file chooser for browsing to a connection dictionary */
	private JFileChooser _dictionaryBrowser;
	
	/** box for the server menu */
	private Box SERVER_OPTION_BOX;
	
	/** box containing a form of custom server fields */
	private Box SERVER_CUSTOM_FORM;
	
	/** menu of available servers */
	private JComboBox<String> SERVER_MENU;

	/** field for entering the adaptor specification */
	private JTextField _adaptorField;
	
	/** field for entering the database URL */
	private JTextField _URLField;
	
	/** field for entering the user's ID */
	private JTextField _userField;
	
	/** field for entering the user's Password */
	private JPasswordField _passwordField;


	/**
	 * Primary Constructor
	 * @param owner        The frame which owns this dialog window.
	 * @param dictionary   The initial connection dictionary.
	 * @param submitLabel  The label to use for the submit button.
	 */
	protected ConnectionDialog( Frame owner, final ConnectionDictionary dictionary, final String submitLabel ) {
		super( owner, "Connection Dialog", true );

		SUBMIT_LABEL = submitLabel;
		setup( dictionary );
	}


	/**
	 * Constructor with a default submit button label of "Connect".
	 * @param owner       The frame which owns this dialog window.
	 * @param dictionary  The initial connection dictionary.
	 */
	protected ConnectionDialog( Frame owner, final ConnectionDictionary dictionary ) {
		this( owner, dictionary, "Submit" );
	}


	/**
	 * Constructor with the default submit button label and an empty connection dictionary.
	 * @param owner  The frame which owns this dialog window.
	 */
	protected ConnectionDialog( Frame owner ) {
		this( owner, new ConnectionDictionary() );
	}


	/**
	 * Primary Constructor
	 * @param owner        The dialog which owns this dialog window.
	 * @param dictionary   The initial connection dictionary.
	 * @param submitLabel  The label to use for the submit button.
	 */
	protected ConnectionDialog( Dialog owner, final ConnectionDictionary dictionary, final String submitLabel ) {
		super( owner, "Connection Dialog", true );
		
		SUBMIT_LABEL = submitLabel;
		setup( dictionary );
	}


	/**
	 * Constructor with a default submit button label of "Connect".
	 * @param owner        The dialog which owns this dialog window.
	 * @param dictionary  The initial connection dictionary.
	 */
	protected ConnectionDialog( Dialog owner, final ConnectionDictionary dictionary ) {
		this( owner, dictionary, "Submit" );
	}


	/**
	 * Constructor with the default submit button label and an empty connection dictionary.
	 * @param owner        The dialog which owns this dialog window.
	 */
	protected ConnectionDialog( Dialog owner ) {
		this( owner, new ConnectionDictionary() );
	}
	
	
	/**
	 * Common initialization.
	 * @param dictionary   The initial connection dictionary.
	 */
	protected void setup( final ConnectionDictionary dictionary ) {
		_dictionary = null;

		final ConnectionDictionary baseDictionary = ( dictionary != null ) ? dictionary : new ConnectionDictionary();

		makeContent();
		loadDictionary( baseDictionary );
	}
	
	
	/**
	 * Load the specified connection dictionary.
	 * @param dictionary the connection dictionary to load
	 */
	public void loadDictionary( final ConnectionDictionary dictionary ) {
		String adaptorClass = null;
		try {
			final DatabaseAdaptor adaptor = dictionary.getDatabaseAdaptor();
			adaptorClass = ( adaptor != null ) ? adaptor.getClass().getName() : null;
		}
		catch ( Exception exception ) {
			Logger.getLogger("global").log( Level.SEVERE, "Error constructing dialog contents.", exception );
		}
		
		_adaptorField.setText( adaptorClass );		
		_URLField.setText( dictionary.getURLSpec() );
		_userField.setText( dictionary.getUser() );
		_passwordField.setText( dictionary.getPassword() );
		
		loadDefaultConfiguration();
	}
	
	
	/** load the default configuration */
	private void loadDefaultConfiguration() {
		final DBConfiguration configuration = DBConfiguration.getInstance();
		_configuration = configuration;
		String selectedServerItem = null;
		if ( configuration != null ) {
			final List<String> servers = new Vector<String>( configuration.getServerNames() );
			servers.add( 0, "Custom" );
			SERVER_MENU.removeAllItems();
			for ( final String server : servers ) {
				SERVER_MENU.addItem( server );
				final ConnectionDictionary serverDictionary = configuration.newConnectionDictionary( null, server );
				if ( serverDictionary != null ) {
					final String urlSpec = serverDictionary.getURLSpec();
					if ( urlSpec != null && urlSpec.equals( _URLField.getText() ) ) {
						selectedServerItem = server;
					}
				}
			}
			if ( selectedServerItem != null ) {
				SERVER_MENU.setSelectedItem( selectedServerItem );
			}
			
			// display the server options if there are options available other than the trivial custom option
			if ( servers.size() > 1 ) {
				setDisplayServerOptions( true );
				setDisplayServerCustomForm(  selectedServerItem == null );
			}
			else {
				setDisplayServerOptions( false );
				setDisplayServerCustomForm( true );
			}
		}
		else {
			SERVER_OPTION_BOX.setVisible( false );
		}		
	}
	
	
	/**
	 * Set whether to display the server options 
	 * @param shouldDisplay true to display server options and false to display the custom options instead
	 */
	private void setDisplayServerOptions( final boolean shouldDisplay ) {
		SERVER_OPTION_BOX.setVisible( shouldDisplay );
		pack();
	}
	
	
	/**
	 * Set whether to display the custom server form
	 * @param shouldDisplay true to display server options and false to display the custom options instead
	 */
	private void setDisplayServerCustomForm( final boolean shouldDisplay ) {
		SERVER_CUSTOM_FORM.setVisible( shouldDisplay );
		pack();
	}
	

	/**
	 * Get the connection user's dictionary.
	 * @return   the user's connection dictionary
	 */
	public ConnectionDictionary getConnectionDictionary() {
		return _dictionary;
	}


	/**
	 * Show the connection dialog
	 * @return   The connection dictionary based on user input
	 */
	protected ConnectionDictionary showDialog() {
		pack();
		setLocationRelativeTo( getOwner() );
		_userField.requestFocusInWindow();	// put the user field in focus since it is the most likely to be edited first by the user
		setVisible( true );
		return _dictionary;
	}


	/**
	 * Attempt to connect to the database using the supplied database adaptor and the connection
	 * dictionary specified by the user via the dialog box.
	 * @param databaseAdaptor  the database adaptor to use for the connection
	 * @return                 the new connection or null if the user canceled the dialog
	 */
	public Connection showConnectionDialog( final DatabaseAdaptor databaseAdaptor ) {
		ConnectionDictionary dictionary = showDialog();

		// check if the user cancelled the dialog
		if ( dictionary == null ) {
			return null;
		}

		try {
			return databaseAdaptor.getConnection( dictionary );
		}
		catch ( Exception exception ) {
			JOptionPane.showMessageDialog( getOwner(), exception.getMessage(), "Connection Error!", JOptionPane.ERROR_MESSAGE );
			Logger.getLogger("global").log( Level.SEVERE, "Database connection error.", exception );
			return showConnectionDialog( (JFrame)getOwner(), databaseAdaptor, _dictionary );
		}
	}


	/**
	 * Display the dialog and return the connection dictionary.
	 * @param owner  The window that owns dialog box
	 * @return       The connection dictionary based on user input
	 */
	public static ConnectionDictionary showDialog( final Frame owner ) {
		return new ConnectionDialog( owner ).showDialog();
	}


	/**
	 * Display the dialog and return the connection dictionary. Initialize the new connection
	 * dictionary with the supplied one except that we ignore the password.
	 * @param owner       The window that owns dialog box
	 * @param dictionary  The dictionary from which to initialize the new connection dictionary
	 * @return            The connection dictionary based on user input
	 */
	public static ConnectionDictionary showDialog( final Frame owner, final ConnectionDictionary dictionary ) {
		return new ConnectionDialog( owner, dictionary ).showDialog();
	}


	/**
	 * Display the dialog and return the connection dictionary. Initialize the new connection
	 * dictionary with the supplied one except that we ignore the password.
	 * @param owner        The window that owns dialog box
	 * @param dictionary   The dictionary from which to initialize the new connection dictionary
	 * @param submitLabel  The label to use for the submit button
	 * @return             The connection dictionary based on user input
	 */
	public static ConnectionDictionary showDialog( final Frame owner, final ConnectionDictionary dictionary, final String submitLabel ) {
		return new ConnectionDialog( owner, dictionary, submitLabel ).showDialog();
	}


	/**
	 * Display the dialog and return the connection dictionary. Initialize the new connection
	 * dictionary with the supplied one except that we ignore the password.
	 * @param owner            The window that owns dialog box
	 * @param databaseAdaptor  The database adaptor to use to make the connection
	 * @param dictionary       The connection dictionary from which to initialize the new connection dictionary
	 * @return                 The connection dictionary based on user input
	 */
	public static Connection showConnectionDialog( final Frame owner, final DatabaseAdaptor databaseAdaptor, final ConnectionDictionary dictionary ) {
		return getInstance( owner, dictionary ).showConnectionDialog( databaseAdaptor );
	}


	/**
	 * Display the dialog and return the connection dictionary. Start with an empty connection dictionary.
	 * @param owner            The window that owns dialog box
	 * @param databaseAdaptor  The database adaptor to use to make the connection
	 * @return                 The connection dictionary based on user input
	 */
	public static Connection showConnectionDialog( final Frame owner, final DatabaseAdaptor databaseAdaptor ) {
		return showConnectionDialog( owner, databaseAdaptor, new ConnectionDictionary() );
	}


	/**
	 * Get a new instance of the connection dialog.
	 * @param owner       The window that owns the new connection dialog box
	 * @param dictionary  The connection dictionary from which to initialize the new connection dictionary
	 * @return            A new instance of the connection dialog
	 */
	public static ConnectionDialog getInstance( final Frame owner, final ConnectionDictionary dictionary ) {
		return new ConnectionDialog( owner, dictionary, "Connect" );
	}


	/**
	 * Get a new instance of the connection dialog.
	 * @param owner       The window that owns the new connection dialog box
	 * @param dictionary  The connection dictionary from which to initialize the new connection dictionary
	 * @return            A new instance of the connection dialog
	 */
	public static ConnectionDialog getInstance( final Dialog owner, final ConnectionDictionary dictionary ) {
		return new ConnectionDialog( owner, dictionary, "Connect" );
	}


	/** Make the Dialog content */
	protected void makeContent() {
		setSize( 250, 130 );
		getContentPane().setLayout( new BorderLayout() );
		Box mainView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( mainView );

		Dimension fieldSize;
		
		SERVER_OPTION_BOX = new Box( BoxLayout.X_AXIS );
		SERVER_OPTION_BOX.add( Box.createGlue() );
		SERVER_MENU = new JComboBox<String>();
		SERVER_OPTION_BOX.add( new JLabel( "Server: " ) );
		SERVER_OPTION_BOX.add( SERVER_MENU );
		mainView.add( SERVER_OPTION_BOX );
		
		SERVER_CUSTOM_FORM = new Box( BoxLayout.Y_AXIS );
		mainView.add( SERVER_CUSTOM_FORM );
		
		Box adaptorBox = new Box( BoxLayout.X_AXIS );
		_adaptorField = new JTextField( 30 );
		fieldSize = _adaptorField.getPreferredSize();
		_adaptorField.setMinimumSize( fieldSize );
		_adaptorField.setMaximumSize( fieldSize );
		adaptorBox.add( Box.createGlue() );
		adaptorBox.add( new JLabel( "Adaptor (optional): " ) );
		adaptorBox.add( _adaptorField );
		SERVER_CUSTOM_FORM.add( adaptorBox );
		
		Box urlBox = new Box( BoxLayout.X_AXIS );
		_URLField = new JTextField( 30 );
		fieldSize = _URLField.getPreferredSize();
		_URLField.setMinimumSize( fieldSize );
		_URLField.setMaximumSize( fieldSize );
		urlBox.add( Box.createGlue() );
		urlBox.add( new JLabel( "Database URL: " ) );
		urlBox.add( _URLField );
		SERVER_CUSTOM_FORM.add( urlBox );
				
		Box userBox = new Box( BoxLayout.X_AXIS );
		_userField = new JTextField( 20 );
		_userField.setMinimumSize( fieldSize );
		_userField.setMaximumSize( fieldSize );
		userBox.add( Box.createGlue() );
		userBox.add( new JLabel( "User: " ) );
		userBox.add( _userField );
		mainView.add( userBox );

		Box passBox = new Box( BoxLayout.X_AXIS );
		passBox.add( Box.createGlue() );
		passBox.add( new JLabel( "Password: " ) );
		_passwordField = new JPasswordField( 20 );
		fieldSize = _passwordField.getPreferredSize();
		_passwordField.setMinimumSize( fieldSize );
		_passwordField.setMaximumSize( fieldSize );
		passBox.add( _passwordField );
		mainView.add( passBox );
		mainView.add( Box.createGlue() );

		Box buttonBox = new Box( BoxLayout.X_AXIS );
		mainView.add( buttonBox );
		
		final JButton configureButton = new JButton( "Configure..." );
		configureButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final boolean changed = ConnectionPreferenceController.displayPathPreferenceSelector( ConnectionDialog.this );
				if ( changed ) {
					loadDefaultConfiguration();
				}
			}
		} );
		
		buttonBox.add( configureButton );
		buttonBox.add( Box.createGlue() );
		
		
		final JButton cancelButton = new JButton( "Cancel" );
		buttonBox.add( cancelButton );
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				setVisible( false );
				dispose();
			}
		} );
		
		final JButton submitButton = new JButton( SUBMIT_LABEL );
		getRootPane().setDefaultButton( submitButton );
		buttonBox.add( submitButton );
		submitButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_dictionary = new ConnectionDictionary();

				if ( _userField.getText() != null ) {
					_dictionary.setUser( _userField.getText() );
				}
				if ( _passwordField.getPassword() != null ) {
					_dictionary.setPassword( String.valueOf( _passwordField.getPassword() ) );
				}
				if ( _URLField.getText() != null ) {
					_dictionary.setURLSpec( _URLField.getText() );
				}
				if ( _adaptorField.getText() != null ) {
					_dictionary.setDatabaseAdaptorClass( _adaptorField.getText() );
				}
				setVisible( false );
				dispose();
			}
		} );
		
		
		SERVER_MENU.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final int selectedIndex = SERVER_MENU.getSelectedIndex();
				if ( selectedIndex > 0 ) {
					final Object selection = SERVER_MENU.getSelectedItem();
					if ( selection != null && _configuration != null ) {
						final String serverName = selection.toString();
						final ConnectionDictionary dictionary = _configuration.newConnectionDictionary( null, serverName );
						final DatabaseAdaptor adaptor = dictionary.getDatabaseAdaptor();
						_adaptorField.setText( adaptor != null ? adaptor.getClass().getCanonicalName() : "" );
						_URLField.setText( dictionary.getURLSpec() );
						setDisplayServerCustomForm( false );
					}
					else {
						setDisplayServerCustomForm( true );
					}
				}
				else if ( selectedIndex == 0 ) {
					setDisplayServerCustomForm( true );
				}
			}
		} );
		
		setResizable( false );
	}
}




