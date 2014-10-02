/*
 * QueryWindow.java
 *
 * Created on Tue Mar 30 11:12:43 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.dbbrowser;

import xal.extension.application.*;
import xal.tools.database.*;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.sql.*;



/**
 * QueryWindow
 * @author  tap
 */
public class QueryWindow extends XalWindow implements SwingConstants {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    
	protected JTextArea _resultsView;
	protected JTextArea _queryView;
	protected Connection _databaseConnection;
	
	
	/**
	 * Constructor
	 */
	public QueryWindow( final QueryDocument aDocument ) {
		super( aDocument );
        setSize( 600, 800 );
		
		_databaseConnection = aDocument.getDefaultDatabaseConnection();

		makeContents();
	}
    
    /**
     * Do not use a toolbar.
	 * @return false
     */
    public boolean usesToolbar() {
        return false;
    }

	
	/**
	 * Make the contents of the main view
	 */
	protected void makeContents() {
		setResizable(true);
		
		_resultsView = new JTextArea();
		_resultsView.setLineWrap( true );
		_resultsView.setWrapStyleWord( true );
		
		_queryView = new JTextArea();
		_queryView.setLineWrap( true );
		_queryView.setWrapStyleWord( true );
			
		final Box buttonBox = new Box( BoxLayout.X_AXIS );
		buttonBox.add( Box.createHorizontalGlue() );
		final JButton executeButton = new JButton( "Execute" );
		executeButton.addActionListener( new AbstractAction() {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				executeQuery();
			}
		});
		buttonBox.add(executeButton);
		
		Box queryBox = new Box(BoxLayout.Y_AXIS);
		queryBox.add( _queryView );
		queryBox.add( buttonBox );
		
		JScrollPane resultsPane = new JScrollPane( _resultsView );
		
		JSplitPane mainBox = new JSplitPane( JSplitPane.VERTICAL_SPLIT, true, queryBox, resultsPane );
		mainBox.setResizeWeight(0.3);
		getContentPane().add(mainBox);
	}
	
    
    /**
     * Register actions specific to this window instance.
     * @param commander The commander with which to register the custom commands.
     */
    public void customizeCommands( final Commander commander ) {
		// define the "execute query" action
		
        commander.registerAction( new AbstractAction( "execute-query" ) {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				executeQuery();
            }
        });
	}
	
	
	/**
	 * execute the database query on the SQL in the query view and put the results
	 * in the results view.
	 */
	protected void executeQuery() {
		try {
			final Connection connection = getDatabaseConnection();
			if ( connection != null ) {
				final String results = QueryModel.executeQuery( connection, _queryView.getText() );
				_resultsView.setText(results);
			}
			else {
				displayError( "Connection Error", "You must connect to the database before you can perform a query." );
			}
		}
		catch(DatabaseException exception) {
			System.err.println(exception);
			displayError("Database Exception", "Error evaluating query:", exception);
		}
	}
	
	
	/**
	 * Get the database connection creating a new one if necessary
	 * @return a database connection
	 */
	protected Connection getDatabaseConnection() {
		if ( _databaseConnection == null ) {
			final ConnectionDictionary dictionary = ConnectionDictionary.defaultDictionary();
			final ConnectionDialog dialog = ConnectionDialog.getInstance( this, dictionary );
			final DatabaseAdaptor adaptor = dictionary != null ? dictionary.getDatabaseAdaptor() : DatabaseAdaptor.getInstance();
			_databaseConnection = dialog.showConnectionDialog( adaptor );
		}
		
		return _databaseConnection;
	}
	
	
	/**
	 * Get the query view
	 * @return the query view
	 */
	public JTextComponent getQueryView() {
		return _queryView;
	}
}

