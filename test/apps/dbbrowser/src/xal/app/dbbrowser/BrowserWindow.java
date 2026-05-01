/*
 *  VAWindow.java
 *
 *  Created on Thu Feb 19 15:16:57 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.dbbrowser;

import xal.extension.application.*;
import xal.tools.database.*;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.event.*;
import java.sql.Connection;


/**
 * BrowserWindow
 *
 * @author   t6p
 */
class BrowserWindow extends XalWindow implements SwingConstants {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    
	/** browser model */
	protected BrowserModel _model;

	/** Action to fetch records from a database table */
	protected Action fetchRecordsAction;


	/**
	 * Creates a new instance of BrowserWindow
	 *
	 * @param aDocument  Description of the Parameter
	 * @param model      Description of the Parameter
	 */
	public BrowserWindow( final BrowserDocument aDocument, final BrowserModel model ) {
		super( aDocument );
		setSize( 850, 600 );

		_model = model;

		makeContents();
	}
    
    
    /** show the connection request */
    void showConnectionRequest() {
        try {
            requestUserConnection();
        }
        catch ( Exception exception ) {
            requestUserConnection();
        }
        finally {
            updateTitle();
        }
    }


	/**
	 * Display a connection dialog to the user and connect to the database using the resulting
	 * connection dictionary.
	 */
	protected void requestUserConnection() {
		final ConnectionDictionary dictionary = ConnectionDictionary.getPreferredInstance( "reports" );
		final ConnectionDialog dialog = ConnectionDialog.getInstance( this, dictionary );
		final Connection connection = dialog.showConnectionDialog( _model.getDatabaseAdaptor() );
		if ( connection != null ) {
			_model.setDatabaseConnection( connection, dialog.getConnectionDictionary() );
		}

		updateTitle();
	}


	/**
	 * Update the window title to reflect the connected user and connected database URL The title
	 * is of the form <code>user - database URL</code>
	 */
	protected void updateTitle() {
		( (BrowserDocument)document ).updateTitle();
	}


	/**
	 * Register actions specific to this window instance.
	 *
	 * @param commander  The commander with which to register the custom commands.
	 */
	public void customizeCommands( Commander commander ) {
		// define the "database connect" action
		Action connectAction =
			new AbstractAction( "connect-database" ) {
                /** serialization identifier */
                private static final long serialVersionUID = 1L;
                
				public void actionPerformed( ActionEvent event ) {
					requestUserConnection();
				}
			};
		commander.registerAction( connectAction );

		// define the "fetch records" action
		fetchRecordsAction =
			new AbstractAction( "fetch-records" ) {
                /** serialization identifier */
                private static final long serialVersionUID = 1L;
                
				public void actionPerformed( ActionEvent event ) {
					fetchRecords();
				}
			};
		commander.registerAction( fetchRecordsAction );
		enableFetchingRecords( false );

		// define the "display query tool" action
		commander.registerAction(
			new AbstractAction( "display-query-tool" ) {
                /** serialization identifier */
                private static final long serialVersionUID = 1L;
            
				public void actionPerformed( ActionEvent event ) {
					Application.getApp().produceDocument( new QueryDocument( _model.getDatabaseConnection() ) );
				}
			} );
	}


	/**
	 * Enable/disable the action for fetching records
	 *
	 * @param enableState  true to enable fetching and false to disable fetching
	 */
	private void enableFetchingRecords( final boolean enableState ) {
		fetchRecordsAction.setEnabled( enableState );
	}


	/** Make the contents of the main view  */
	protected void makeContents() {
		Box mainView = new Box( BoxLayout.X_AXIS );
		getContentPane().add( mainView );
		
		Box selectionView = new Box( BoxLayout.X_AXIS );

		// make the list for displaying the list of schemas in the database
		Box schemaView = new Box( BoxLayout.Y_AXIS );
		selectionView.add( schemaView );
		Dimension schemaLabelSize = new JLabel("TYPICAL_SCHEMA_LABEL").getPreferredSize();
		JLabel schemaLabel = new JLabel( "Schemas: " );
		schemaLabel.setMinimumSize( schemaLabelSize );
		schemaView.add( schemaLabel );
		final JList<String> schemaListView = new JList<>();
		schemaListView.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		schemaView.add( new JScrollPane( schemaListView ) );
		schemaListView.addListSelectionListener(
			new ListSelectionListener() {
				public void valueChanged( final ListSelectionEvent event ) {
					if ( event.getValueIsAdjusting() ) {
						return;
					}
					try {
						final JList<String> list = schemaListView;
						final String selectedSchema = list.getSelectedValue();
						if ( selectedSchema != null ) {
							_model.setSchema( selectedSchema.toString() );
						}
					}
					catch ( DatabaseException exception ) {
						System.err.println( exception );
						displayError( "Database Exception", "Exception while trying to fetch tables:", exception );
					}
				}
			} );
		schemaListView.setModel( new SchemaListModel( _model ) );
		final ListSelectionModel schemaSelectionModel = schemaListView.getSelectionModel();

		// make the list for displaying the list of tables for the selected schema
		Box tableView = new Box( BoxLayout.Y_AXIS );
		selectionView.add( tableView );
		Dimension tableLabelSize = new JLabel("TYPICAL_TABLE_LABEL").getPreferredSize();
		JLabel tableLabel = new JLabel( "Tables: " );
		tableLabel.setMinimumSize( tableLabelSize );
		tableView.add( tableLabel );
		final JList<String> tableListView = new JList<>();
		tableListView.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		tableListView.addListSelectionListener(
			new ListSelectionListener() {
				public void valueChanged( ListSelectionEvent event ) {
					if ( event.getValueIsAdjusting() ) {
						return;
					}
					try {
						@SuppressWarnings( "unchecked" )	// have no choice but to cast since event returns an Object
						final JList<Object> list = (JList<Object>)event.getSource();
						final Object selectedTable = list.getSelectedValue();
						if ( selectedTable != null ) {
							_model.setTable( list.getSelectedValue().toString() );
						}
						enableFetchingRecords( selectedTable != null );
					}
					catch ( DatabaseException exception ) {
						System.err.println( exception );
						displayError( "Database Exception", "Exception while trying to fetch table attributes:", exception );
					}
				}
			} );
		tableView.add( new JScrollPane( tableListView ) );
		tableListView.setModel( new TableListModel( _model ) );
		final ListSelectionModel tableSelectionModel = tableListView.getSelectionModel();

		_model.addBrowserModelListener(
			new BrowserModelListener() {
				/**
				 * Database schema changed notification
				 *
				 * @param model      The browser model whose database schema changed
				 * @param newSchema  The new database schema
				 */
				public void schemaChanged( BrowserModel model, String newSchema ) {
					tableSelectionModel.clearSelection();
				}


				/**
				 * Database table changed notification
				 *
				 * @param model     The browser model whose database table changed
				 * @param newTable  The new database table
				 */
				public void tableChanged( BrowserModel model, String newTable ) { }


				/**
				 * The model's connection has changed
				 *
				 * @param model  The model whose connection changed
				 */
				public void connectionChanged( BrowserModel model ) {
					schemaSelectionModel.clearSelection();
					tableSelectionModel.clearSelection();
				}
			} );

		// make the table for displaying attributes for the selected database table
		Box attributeView = new Box( BoxLayout.Y_AXIS );
		JSplitPane splitView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, selectionView, attributeView);
		mainView.add( splitView );
		attributeView.add( new JLabel( "Table Attributes:" ) );
		JTable attributeTable = new JTable( new AttributeTableModel( _model ) );
		TableColumnModel attributeColumnModel = attributeTable.getColumnModel();
		attributeColumnModel.getColumn( AttributeTableModel.WIDTH_COLUMN ).setCellRenderer( getNumericCellRenderer() );
		attributeView.add( attributeTable.getTableHeader() );
		attributeView.add( new JScrollPane( attributeTable ) );
	}


	/** Fetch the database records for the model's selected schema-table  */
	public void fetchRecords() {
		try {
			final String title = _model.getSchema() + "." + _model.getTable() + " Records";
			new DataWindow( this, title, _model.fetchRecords(), _model.getTableAttributes() ).setVisible( true );
		}
		catch ( DatabaseException exception ) {
			System.err.println( exception );
			displayError( "Fetch Exception", "Database error fetching records:", exception );
		}
		catch ( Exception exception ) {
			System.err.println( exception );
			displayError( exception );
		}
	}


	/**
	 * Right justify text associated with numeric values.
	 *
	 * @return   A renderer for numeric values.
	 */
	private TableCellRenderer getNumericCellRenderer() {
		return
			new DefaultTableCellRenderer() {
                /** serialization identifier */
                private static final long serialVersionUID = 1L;
                
				public java.awt.Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
					JLabel label = (JLabel)super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
					label.setHorizontalAlignment( RIGHT );
					label.setForeground( java.awt.Color.blue );
					return label;
				}
			};
	}
}



