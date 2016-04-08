//
//  BeamArchiveFace.java
//  xal
//
//  Created by Tom Pelaia on 5/16/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.app.rtbtwizard;

import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.sql.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import xal.ca.*;
import xal.smf.*;
import xal.smf.impl.*;

import xal.extension.application.Application;
import xal.extension.bricks.WindowReference;
import xal.tools.database.*;
import xal.tools.data.*;
import xal.tools.StringJoiner;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.text.FormattedNumber;


/** view for archiving beam parameters */
public class BeamArchiveFace {
	/** document */
	final protected GenDocument DOCUMENT;
	
	/** archive view */
	final protected Component FACE_VIEW;
	
	/** persistent store */
	final protected PersistentStore PERSISTENT_STORE;
	
	/** table model for displaying the archive data */
	final protected ArchiveTableModel ARCHIVE_TABLE_MODEL;
	
	/** archive descriptor for target beam parameters */
	final protected ArchiveDescriptor TARGET_BEAM_ARCHIVE_DESCRIPTOR;
	
	/** controller for publishing the latest parameter values */
	protected PublishController _publishController;
	
	
	/** Constructor */
	public BeamArchiveFace( final GenDocument document ) {
		DOCUMENT = document;
		
		PERSISTENT_STORE = loadArchiveConfiguration();
		TARGET_BEAM_ARCHIVE_DESCRIPTOR = PERSISTENT_STORE.getTargetBeamArchiveDescriptor();
		ARCHIVE_TABLE_MODEL = new ArchiveTableModel( TARGET_BEAM_ARCHIVE_DESCRIPTOR.getAttributeDescriptors() );
		
		FACE_VIEW = makeView();		
	}
	
	
	/** get this controller's view */
	public Component getView() {
		return FACE_VIEW;
	}
	
	
	/** get the main window */
	protected JFrame getWindow() {
		return (JFrame)SwingUtilities.windowForComponent( FACE_VIEW );
	}
	
	
	/** make the view for this face */
	protected Component makeView() {
		final Box mainBox = new Box( BoxLayout.Y_AXIS );
		
		final JTable archiveTable = new JTable( ARCHIVE_TABLE_MODEL );
		archiveTable.setColumnSelectionAllowed( true );
		archiveTable.setRowSelectionAllowed( true );
		final JScrollPane archiveScroller = new JScrollPane( archiveTable );
		mainBox.add( archiveScroller );
		
		final Box buttonBox = new Box( BoxLayout.X_AXIS );
		mainBox.add( buttonBox );
		buttonBox.add( Box.createHorizontalGlue() );
		
		final JButton publishButton = new JButton( "Publish..." );
		buttonBox.add( publishButton );
		publishButton.setToolTipText( "Specify and publish the latest Target Beam Parameters." );
		publishButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( _publishController == null ) {
					_publishController = new PublishController( DOCUMENT, TARGET_BEAM_ARCHIVE_DESCRIPTOR, PERSISTENT_STORE );
				}
				_publishController.displayDialog();
			}
		});
		
		buttonBox.add( Box.createHorizontalGlue() );
		
		final JButton connectButton = new JButton( "Connect..." );
		connectButton.addActionListener( new AbstractAction( "Connect..." ) {
            
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				PERSISTENT_STORE.requestUserConnection( getWindow() );
			}
		});
		buttonBox.add( connectButton );
		connectButton.setToolTipText( "Change database connection for fetching beam parameters." );
		
		buttonBox.add( new JLabel( "  From:  " ) );
		final SpinnerDateModel fromSpinnerModel = new SpinnerDateModel();
		fromSpinnerModel.setValue( new java.util.Date( new java.util.Date().getTime() - 26L * 7L * 24L * 3600L * 1000L ) );	// go back 26 weeks
		final JSpinner fromSpinner = new JSpinner( fromSpinnerModel );
		fromSpinner.setMaximumSize( fromSpinner.getPreferredSize() );
		fromSpinner.setToolTipText( "Start date of the fetch range." );
		buttonBox.add( fromSpinner );
		
		buttonBox.add( new JLabel( "  To:  " ) );
		final SpinnerDateModel toSpinnerModel = new SpinnerDateModel();
		toSpinnerModel.setValue( new java.util.Date( new java.util.Date().getTime() + 10L * 7L * 24L * 3600L * 1000L ) );	// go forward 10 weeks
		final JSpinner toSpinner = new JSpinner( toSpinnerModel );
		toSpinner.setMaximumSize( toSpinner.getPreferredSize() );
		toSpinner.setToolTipText( "End date of the fetch range." );
		buttonBox.add( toSpinner );
		
		final JButton fetchButton = new JButton( new AbstractAction( "Fetch" ) {
            
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				final Date fromTime = (Date)fromSpinner.getValue();
				final Date toTime = (Date)toSpinner.getValue();
				try {
					List<TargetBeamRecord> records = PERSISTENT_STORE.fetchTargetBeamRecords( getWindow(), fromTime, toTime );
					if ( records != null ) {
						ARCHIVE_TABLE_MODEL.setTargetBeamRecords( records );
					}
				}
				catch( Exception exception ) {
					JOptionPane.showMessageDialog( getWindow(), exception.toString(), "Database Exception", JOptionPane.ERROR_MESSAGE );
				}
			}
		});
		fetchButton.setToolTipText( "Fetch archived target beam parameters within the specified range." );
		buttonBox.add( fetchButton );
		
		final JButton reportButton = new JButton( new AbstractAction( "Copy Report" ) {
            
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				copyTargetBeamReport();
			}
		});
		reportButton.setToolTipText( "Copy to the clipboard a report of the fetched target beam records." );
		buttonBox.add( reportButton );
		
		return mainBox;
	}
	
	
	/** load the archive configuration file */
	protected PersistentStore loadArchiveConfiguration() {
		final URL configurationURL = Application.getAdaptor().getResourceURL( "archiveConfiguration.xml" );
		final DataAdaptor configurationAdaptor = XmlDataAdaptor.adaptorForUrl( configurationURL, false ).childAdaptor( "ArchiveConfiguration" );
		final DataAdaptor persistentStoreAdaptor = configurationAdaptor.childAdaptor( "PersistentStore" );
		return new PersistentStore( persistentStoreAdaptor );
	}
	
	@SuppressWarnings ({"rawtypes", "unchecked"}) //Cannot figure out how to properly cast AttributeDescriptors
	/** make a report of target beam parameters and copy it to the clipboard */
	protected void copyTargetBeamReport() {
		final List<AttributeDescriptor> attributeDescriptors = TARGET_BEAM_ARCHIVE_DESCRIPTOR.getAttributeDescriptors();
		final List<TargetBeamRecord> targetBeamRecords = ARCHIVE_TABLE_MODEL.getTargetBeamRecords();
		final StringJoiner rowJoiner = new StringJoiner( "\n" );
		final StringJoiner titleColumnJoiner = new StringJoiner( "\t" );
		for ( final AttributeDescriptor attributeDescriptor : attributeDescriptors ) {
			titleColumnJoiner.append( attributeDescriptor.getTitle() );
		}
		rowJoiner.append( titleColumnJoiner.toString() );
		for ( final TargetBeamRecord record : targetBeamRecords ) {
			final StringJoiner columnJoiner = new StringJoiner( "\t" );
			for ( final AttributeDescriptor attributeDescriptor : attributeDescriptors ) {
				final Object rawValue = record.getValue( attributeDescriptor.getAttribute() );
				if ( rawValue != null ) {
					columnJoiner.append( attributeDescriptor.getDisplayValue( rawValue ).toString() );
				}
				else {
					columnJoiner.append( "" );
				}
			}
			rowJoiner.append( columnJoiner.toString() );
		}
		final String report = rowJoiner.toString();
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		final StringSelection contents = new StringSelection( report );
		clipboard.setContents( contents, contents );
			
	}
}


@SuppressWarnings ("rawtypes") //Cannot figure out how to properly cast AttributeDescriptors
/** table data model which gets its data from the database archive */
class ArchiveTableModel extends AbstractTableModel {
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
	/** List of attribute descriptors */
	final protected List<AttributeDescriptor> ATTRIBUTE_DESCRIPTORS;
	
	/** List of target beam parameter records */
	protected List<TargetBeamRecord> _targetBeamRecords;
	
	
	/** Constructor */
	public ArchiveTableModel( final List<AttributeDescriptor> attributeDescriptors ) {
		ATTRIBUTE_DESCRIPTORS = attributeDescriptors;
		_targetBeamRecords = new ArrayList<TargetBeamRecord>();
	}
	
	
	/** get the name of the column */
	public String getColumnName( final int column ) {
		return ATTRIBUTE_DESCRIPTORS.get( column ).getTitle();
	}
	
	
	/** get the class of the column */
	public Class<?> getColumnClass( final int column ) {
		return ATTRIBUTE_DESCRIPTORS.get( column ).getDisplayValueClass();
	}
	
	
	/** get the number of columns to display */
	public int getColumnCount() {
		return ATTRIBUTE_DESCRIPTORS.size();
	}
	
	
	/** get the number of rows to display */
	public int getRowCount() {
		return _targetBeamRecords.size();
	}
	
	
	/** set the target beam parameter records */
	public void setTargetBeamRecords( final List<TargetBeamRecord> targetBeamRecords ) {
		_targetBeamRecords = targetBeamRecords;
		fireTableDataChanged();
	}
	
	
	/** get the target beam records */
	public List<TargetBeamRecord> getTargetBeamRecords() {
		return _targetBeamRecords;
	}
	
	@SuppressWarnings ("unchecked") //Cannot figure out how to properly cast AttributeDescriptors
	/** get the value to display in the table cell */
	public Object getValueAt( final int row, final int column ) {
		final List<TargetBeamRecord> records = _targetBeamRecords;
		if ( row < records.size() && column < ATTRIBUTE_DESCRIPTORS.size() ) {
			final TargetBeamRecord record = records.get( row );
			final AttributeDescriptor attributeDescriptor = ATTRIBUTE_DESCRIPTORS.get( column );
			if ( record != null && attributeDescriptor != null ) {
				final Object rawValue = record.getValue( attributeDescriptor.getAttribute() );
				return attributeDescriptor.getDisplayValue( rawValue );
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
}


@SuppressWarnings ({"rawtypes", "unchecked"}) //Cannot figure out how to properly cast AttributeDescriptors
/** Publish Controller */
class PublishController extends AbstractTableModel {
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
	/** table column for the parameter title */
	final static protected int PARAMETER_TITLE_COLUMN = 0;
	
	/** table column for the parameter value */
	final static protected int PARAMETER_VALUE_COLUMN = 1;
	
	/** archive descriptor for target beam parameters */
	final protected ArchiveDescriptor TARGET_BEAM_ARCHIVE_DESCRIPTOR;
	
	/** List of attribute descriptors */
	final protected List<AttributeDescriptor> ATTRIBUTE_DESCRIPTORS;
	
	/** table of parameters keyed by attribute name */
	final protected Map<String, Object> PARAMETERS;
	
	/** reference to the dialog window */
	final protected WindowReference DIALOG_REFERENCE;
	
	/** the document which is the source of initial data */
	final protected GenDocument DOCUMENT;
	
	/** persistent store front end to the database */
	final protected PersistentStore PERSISTENT_STORE;

	/** table of parameters to publish */
	final private JTable PARAMETER_TABLE;
	
	
	/** Constructor */
	public PublishController( final GenDocument document, final ArchiveDescriptor targetBeamArchiveDescriptor, final PersistentStore persistentStore ) {
		PERSISTENT_STORE = persistentStore;
		DOCUMENT = document;
		TARGET_BEAM_ARCHIVE_DESCRIPTOR = targetBeamArchiveDescriptor;
		ATTRIBUTE_DESCRIPTORS = targetBeamArchiveDescriptor.getWritableAttributeDescriptors();
		PARAMETERS = new HashMap<String, Object>( ATTRIBUTE_DESCRIPTORS.size() );
		
		DIALOG_REFERENCE = GenDocument.getDefaultWindowReference( "TargetBeamParameterDialog", document.getMainWindow() );
		PARAMETER_TABLE = (JTable)DIALOG_REFERENCE.getView( "BeamParameterTable" );
		PARAMETER_TABLE.setModel( this );

		final JButton resetButton = (JButton)DIALOG_REFERENCE.getView( "ResetButton" );
		resetButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				reset();
			}
		});
		
		final JButton cancelButton = (JButton)DIALOG_REFERENCE.getView( "CancelButton" );
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				getDialog().setVisible( false );
			}
		});
		
		final JButton okayButton = (JButton)DIALOG_REFERENCE.getView( "PublishButton" );
		okayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				try {
					publishParameters();
					getDialog().setVisible( false );
				}
				catch ( Exception exception ) {
					exception.printStackTrace();
					JOptionPane.showMessageDialog( getDialog(), exception.toString(), "Publish Exception", JOptionPane.ERROR_MESSAGE );
				}
			}
		});
	}
	
	
	/** display the dialog and initialize the values using the document */
	public void displayDialog() {
		reset();
		final JDialog publishDialog = getDialog();
		publishDialog.setLocationRelativeTo( publishDialog.getOwner() );
		publishDialog.setVisible( true );
	}
	
	
	/** reset the values to initial ones based on the document */
	protected void reset() {
		PARAMETERS.clear();
		PARAMETERS.put( "timestamp", new Date() );
		
		if(DOCUMENT.tdensity != 0.0) PARAMETERS.put( "peakTargetProtonDensity", DOCUMENT.tdensity);
		if(DOCUMENT.tdensity_raw != 0.0) PARAMETERS.put( "rawPeakTargetProtonDensity", DOCUMENT.tdensity_raw);
		if(DOCUMENT.wdensity != 0.0) PARAMETERS.put( "peakBeamWindowProtonDensity",DOCUMENT.wdensity);
		if(DOCUMENT.xsize != 0.0) PARAMETERS.put( "widthTarget", DOCUMENT.xsize);
		if(DOCUMENT.ysize != 0.0) PARAMETERS.put( "heightTarget", DOCUMENT.ysize);
		if(DOCUMENT.xpos != 0.0) PARAMETERS.put( "xTarget", DOCUMENT.xpos);
		if(DOCUMENT.ypos != 0.0) PARAMETERS.put( "yTarget", DOCUMENT.ypos);
		if(DOCUMENT.charge != 0.0) PARAMETERS.put( "protonsPerPulse", new Double(DOCUMENT.charge));
		
		if(DOCUMENT.harpxch.isConnected() && DOCUMENT.harpych.isConnected()){
		    try{
			double harpwidth = DOCUMENT.harpxch.getValDbl();
			double harpheight = DOCUMENT.harpych.getValDbl();
			PARAMETERS.put("widthHarp", harpwidth);
			PARAMETERS.put("heightHarp", harpheight);
		    }
		    catch(ConnectionException e){
			System.out.println("Error in connecting to Harp.");
		    }
		    catch(GetException e){
			System.out.println("Error in getting Harp values.");
		    }
		}
		
		if(DOCUMENT.repratech.isConnected()){ 
		    try{
			double beamreprate = DOCUMENT.repratech.getValDbl();
			PARAMETERS.put("repRate", beamreprate);
		    }
		    catch(ConnectionException e){
			System.out.println("Error in connecting to Timing Center.");
		    }
		    catch(GetException e){
			System.out.println("Error in getting Rep rate values.");
		    }
		}
		if(DOCUMENT.energych.isConnected()){ 
		    try{
			double beamenergy = DOCUMENT.energych.getValDbl();
			PARAMETERS.put("energy", beamenergy);
		    }
		    catch(ConnectionException e){
			System.out.println("Error in connecting to Timing Center.");
		    }
		    catch(GetException e){
			System.out.println("Error in getting Rep rate values.");
		    }
		}
		
		fireTableDataChanged();
	}
	
	
	/** publish the parameters to the database */
	protected void publishParameters() throws Exception {
		// check if users have uncommitted edits and if so commit them now
		if (PARAMETER_TABLE.isEditing()) {
			// commit unsaved edits (prevents issue in which user entered text but didn't hit return and their text was lost)
			PARAMETER_TABLE.getCellEditor().stopCellEditing();
		}

		final Connection connection = PERSISTENT_STORE.newConnection( DOCUMENT.getMainWindow() );
		if ( connection != null ) {
			PERSISTENT_STORE.publishTargetBeamParameters( DOCUMENT.getMainWindow(), PARAMETERS, connection );
		}
		else {
			JOptionPane.showMessageDialog( getDialog(), "Could not get a database connection to publish parameters.", "Publish Failed", JOptionPane.WARNING_MESSAGE );
		}
	}
	
	
	/** get the dialog window */
	public JDialog getDialog() {
		return (JDialog)DIALOG_REFERENCE.getWindow();
	}
	
	
	/** get the name of the column */
	public String getColumnName( final int column ) {
		switch( column ) {
			case PARAMETER_TITLE_COLUMN:
				return "Parameter";
			case PARAMETER_VALUE_COLUMN:
				return "Value";
			default:
				return "";
		}
	}
	
	
	/** get the number of columns to display */
	public int getColumnCount() {
		return 2;
	}
	
	
	/** get the number of rows to display */
	public int getRowCount() {
		return ATTRIBUTE_DESCRIPTORS.size();
	}
	
	
	/** determine if the table cell is editable */
	public boolean isCellEditable( final int row, final int column ) {
		return column == PARAMETER_VALUE_COLUMN;
	}
	
	
	/** get the value to display in the table cell */
	public Object getValueAt( final int row, final int column ) {
		if ( row < ATTRIBUTE_DESCRIPTORS.size() ) {
			final AttributeDescriptor attributeDescriptor = ATTRIBUTE_DESCRIPTORS.get( row );
			switch( column ) {
				case PARAMETER_TITLE_COLUMN:
					return attributeDescriptor.getTitle();
				case PARAMETER_VALUE_COLUMN:
					final Object value = PARAMETERS.get( attributeDescriptor.getAttribute() );
					return value != null ? attributeDescriptor.getDisplayValue( value ) : "";
				default:
					return "";
			}
		}
		else {
			return null;
		}
	}
	
	
	/** set a new value for the specified table cell */
	public void setValueAt( final Object stringValue, final int row, final int column ) {
		if ( column == PARAMETER_VALUE_COLUMN && row < ATTRIBUTE_DESCRIPTORS.size() ) {
			final AttributeDescriptor attributeDescriptor = ATTRIBUTE_DESCRIPTORS.get( row );
			final Object value = stringValue != null ? attributeDescriptor.parseValue( stringValue.toString() ) : null;
			PARAMETERS.put( attributeDescriptor.getAttribute(), value );
		}
	}
}



/** supports database transactions */
class PersistentStore {
	/** descriptor of the target beam archive table */
	final protected ArchiveDescriptor TARGET_BEAM_ARCHIVE_DESCRIPTOR;
	
	/** the current database connection */
	protected Connection _connection;
	
	/** the current connection dictionary */
	protected ConnectionDictionary _connectionDictionary;
	
	/** SQL for the query by time range */
	protected String QUERY_BY_TIME_RANGE;
	
	
	/** Constructor */
	public PersistentStore( final DataAdaptor adaptor ) {
		final DataAdaptor archiveAdaptor = adaptor.childAdaptor( "dbtable" );
		TARGET_BEAM_ARCHIVE_DESCRIPTOR = new ArchiveDescriptor( archiveAdaptor );
		final String timestampColumn = TARGET_BEAM_ARCHIVE_DESCRIPTOR.getTimestampAttributeDescriptor().getColumn();
		final String tableName = TARGET_BEAM_ARCHIVE_DESCRIPTOR.getTableName();
		QUERY_BY_TIME_RANGE = "SELECT * FROM " + tableName + " WHERE " + timestampColumn + " >= ? AND " + timestampColumn + " <= ? ORDER BY " + timestampColumn + " DESC";
	}
	
	
	/** Get the archive descriptor */
	public ArchiveDescriptor getTargetBeamArchiveDescriptor() {
		return TARGET_BEAM_ARCHIVE_DESCRIPTOR;
	}
	
	
	@SuppressWarnings ({"rawtypes", "unchecked"}) //Cannot figure out how to properly cast AttributeDescriptors
    /** fetch archived target beam parameters */
	final protected List<TargetBeamRecord> fetchTargetBeamRecords( final JFrame window, final Date fromDate, final Date toDate ) throws Exception {		
		if ( _connection == null ) {
			connectToDatabase( window );
		}
		
		if ( _connection != null ) {
			final List<TargetBeamRecord> records = new ArrayList<TargetBeamRecord>();
			
			final PreparedStatement query = _connection.prepareStatement( QUERY_BY_TIME_RANGE );
			query.setTimestamp( 1, new java.sql.Timestamp( fromDate.getTime() ) );
			query.setTimestamp( 2, new java.sql.Timestamp( toDate.getTime() ) );
			final ResultSet resultSet = query.executeQuery();			
			final List<AttributeDescriptor> attributeDescriptors = TARGET_BEAM_ARCHIVE_DESCRIPTOR.getAttributeDescriptors();
			while ( resultSet.next() ) {
				final TargetBeamRecord record = new TargetBeamRecord( attributeDescriptors, resultSet );
				records.add( record );
			}
			
			return records;				
		}
		else {
			return new ArrayList<TargetBeamRecord>();
		}
	}
	
	@SuppressWarnings ({"rawtypes", "unchecked"}) //Cannot figure out how to properly cast AttributeDescriptors
	/** publish target beam parameters keyed by attribute name */
	final protected boolean publishTargetBeamParameters( final JFrame window, final Map<String, Object> parameters, final Connection connection ) throws Exception {
		final List<AttributeDescriptor> attributeDescriptors = TARGET_BEAM_ARCHIVE_DESCRIPTOR.getWritableAttributeDescriptors();
		
		final StringJoiner columnJoiner = new StringJoiner( ", " );
		final StringJoiner bindingJoiner = new StringJoiner( ", " );
		for ( final AttributeDescriptor attributeDescriptor : attributeDescriptors ) {
			columnJoiner.append( attributeDescriptor.getColumn() );
			bindingJoiner.append( "?" );
		}
		
		final StringBuffer sqlBuffer = new StringBuffer( "INSERT INTO " );
		sqlBuffer.append( TARGET_BEAM_ARCHIVE_DESCRIPTOR.getTableName() );
		sqlBuffer.append( " ( " );
		sqlBuffer.append( columnJoiner.toString() );
		sqlBuffer.append( " ) VALUES (" );
		sqlBuffer.append( bindingJoiner.toString() );
		sqlBuffer.append( ")" );
		final String insertSQL = sqlBuffer.toString();
		
		if ( connection != null ) {
			final PreparedStatement insertStatement = connection.prepareStatement( insertSQL );
			final int count = attributeDescriptors.size();
			for ( int index = 0 ; index < count ; index++ ) {
				final AttributeDescriptor attributeDescriptor = attributeDescriptors.get( index );
				final Object value = parameters.get( attributeDescriptor.getAttribute() );
				attributeDescriptor.publishValue( insertStatement, index + 1, value );
			}
			insertStatement.execute();
			connection.commit();
			return true;
		}
		else {
			return false;
		}
	}
	
	
	/** set the database connection */
	protected void setDatabaseConnection( final Connection connection ) {
		clearDatabaseConnection();
		_connection = connection;
	}
	
	
	/** get a new database connection and display the dialog about the owning window */
	public Connection newConnection( final JFrame window ) {		
		ConnectionDictionary dictionary = ConnectionDictionary.defaultDictionary();
		if ( dictionary == null ) {
			dictionary = new ConnectionDictionary();
		}
		else {
			dictionary.setUser( "" );
			dictionary.setPassword( "" );
		}
		final ConnectionDialog dialog = ConnectionDialog.getInstance( window, dictionary );
		return dialog.showConnectionDialog( dictionary.getDatabaseAdaptor() );
	}
	
	
	/** connect to the existing dictionary if it exists */
	protected void connectToDatabase( final JFrame window ) {
		final ConnectionDictionary baseDictionary = _connectionDictionary != null ? _connectionDictionary : ConnectionDictionary.defaultDictionary();
		if ( baseDictionary == null || !baseDictionary.hasRequiredInfo() ) {
			requestUserConnection( window );
		}
		else {
			connectToDatabase( window, baseDictionary );
		}
	}
	
	
	/** connect to the database using the specified connection dictionary */
	protected void connectToDatabase( final JFrame window, final ConnectionDictionary dictionary ) {
		_connectionDictionary = dictionary;
		DatabaseAdaptor adaptor = dictionary.getDatabaseAdaptor();
		if ( adaptor == null ) {
			adaptor = DatabaseAdaptor.getInstance();
			if ( adaptor == null ) {
				JOptionPane.showMessageDialog( window, "Cannot find a database adaptor.", "Connection Error", JOptionPane.ERROR_MESSAGE );
				return;
			}
			else {
				dictionary.setDatabaseAdaptorClass( adaptor.getClass() );
			}
		}
		
		Connection connection;
		try {
			connection = adaptor.getConnection( dictionary );
		}
		catch( Exception exception ) {
			JOptionPane.showMessageDialog( window, "Error connecting to the database", "Connection Exception", JOptionPane.ERROR_MESSAGE );
			return;
		}
		setDatabaseConnection( connection );
	}
	
	
	/** Display a connection dialog to the user and connect to the database using the resulting connection dictionary. */
	protected void requestUserConnection( final JFrame window ) {
		final ConnectionDictionary baseDictionary = _connectionDictionary != null ? _connectionDictionary : ConnectionDictionary.defaultDictionary();
		final ConnectionDictionary dictionary = ConnectionDialog.showDialog( window, baseDictionary, "Connect" );
		
		if ( dictionary == null ) {
			setDatabaseConnection( null );
			return;
		}
		else if ( dictionary.hasRequiredInfo() ) {
			connectToDatabase( window, dictionary );
		}		
	}
	
	
	/** close database connection */
	public void clearDatabaseConnection() {
		if ( _connection != null ) {
			try {
				if ( !_connection.isClosed() ) {
					_connection.close();
				}
			}
			catch( Exception exception ) {}
			finally {
				_connection = null;
			}
		}
	}
}



/** Record of target beam parameters */
class TargetBeamRecord {
	/** target beam parameters keyed by attribute */
	final protected Map<String, Object> PARAMETERS;
	
	@SuppressWarnings ({"rawtypes", "unchecked"}) //Cannot figure out how to properly cast AttributeDescriptors
	/** Constructor */
	public TargetBeamRecord( final List<AttributeDescriptor> attributeDescriptors, final ResultSet resultSet ) throws SQLException {
		PARAMETERS = new HashMap<String, Object>( attributeDescriptors.size() );
		
		for ( final AttributeDescriptor attributeDescriptor : attributeDescriptors ) {
			final Object value = attributeDescriptor.getValue( resultSet );
			PARAMETERS.put( attributeDescriptor.getAttribute(), value );
		}
	}
	
	
	/** get the value corresponding to the specified attribute */
	public Object getValue( final String attribute ) {
		return PARAMETERS.get( attribute );
	}
}


@SuppressWarnings ("rawtypes") //Cannot figure out how to properly cast AttributeDescriptors
/** maps to a database table */
class ArchiveDescriptor {
	/** name of the entity */
	final protected String ENTITY;
	
	/** name of the database table */
	final protected String TABLE_NAME;
	
	/** attribute descriptors */
	final protected List<AttributeDescriptor> ATTRIBUTE_DESCRIPTORS;
	
	/** table of attribute descriptors keyed by attribute */
	final protected Map<String,AttributeDescriptor> ATTRIBUTE_DESCRIPTOR_TABLE;
	
	
	/** Constructor */
	public ArchiveDescriptor( final DataAdaptor adaptor ) {
		ENTITY = adaptor.stringValue( "entity" );
		TABLE_NAME = adaptor.stringValue( "table" );
		
		ATTRIBUTE_DESCRIPTOR_TABLE = new HashMap<String,AttributeDescriptor>();
		ATTRIBUTE_DESCRIPTORS = new ArrayList<AttributeDescriptor>();
		final List<DataAdaptor> columnAdaptors = adaptor.childAdaptors( "column" );
		for ( final DataAdaptor columnAdaptor : columnAdaptors ) {
			final AttributeDescriptor<?> attributeDescriptor = AttributeDescriptor.getDescriptor( columnAdaptor );
			ATTRIBUTE_DESCRIPTORS.add( attributeDescriptor );
			ATTRIBUTE_DESCRIPTOR_TABLE.put( attributeDescriptor.getAttribute(), attributeDescriptor );
		}
	}
	
	
	/** get the table name */
	public String getTableName() {
		return TABLE_NAME;
	}
	
	
	/** get the attribute descriptors */
	public List<AttributeDescriptor> getAttributeDescriptors() {
		return ATTRIBUTE_DESCRIPTORS;
	}
	
	
	/** get the attribute descriptors which we can publish */
	public List<AttributeDescriptor> getWritableAttributeDescriptors() {
		final List<AttributeDescriptor> writableDescriptors = new ArrayList<AttributeDescriptor>( ATTRIBUTE_DESCRIPTORS.size() );
		for ( final AttributeDescriptor attributeDescriptor : ATTRIBUTE_DESCRIPTORS ) {
			if ( !attributeDescriptor.isReadOnly() ) {
				writableDescriptors.add( attributeDescriptor );
			}
		}
		
		return writableDescriptors;
	}
	
	
	/** get the attribute descriptor corresponding to the timestamp */
	public AttributeDescriptor getTimestampAttributeDescriptor() {
		return ATTRIBUTE_DESCRIPTOR_TABLE.get( "timestamp" );
	}
}



/** maps to a database column */
abstract class AttributeDescriptor<ColumnType> {
	final protected String ATTRIBUTE;
	final protected String TYPE;
	final protected String COLUMN;
	final protected String DESCRIPTION;
	final protected String UNITS;
	final protected boolean READ_ONLY;
	
	
	/** Constructor */
	public AttributeDescriptor( final String attribute, final String type, final String column, final String description, final String units, final boolean readonly ) {
		ATTRIBUTE = attribute;
		TYPE = type;
		COLUMN = column;
		DESCRIPTION = description;
		UNITS = units;
		READ_ONLY = readonly;
	}
	
	
	/** Generate an attribute descriptor from a data adaptor */
	static public AttributeDescriptor<?> getDescriptor( final DataAdaptor dataAdaptor ) {
		final String attribute = dataAdaptor.stringValue( "attribute" );
		final String type = dataAdaptor.stringValue( "type" );
		final String column = dataAdaptor.stringValue( "column" );
		final String description = dataAdaptor.stringValue( "description" );
		final String units = dataAdaptor.hasAttribute( "units" ) ? dataAdaptor.stringValue( "units" ) : null;
		final boolean readonly = dataAdaptor.hasAttribute( "readonly" ) && dataAdaptor.booleanValue( "readonly" );
		
		if ( type.equalsIgnoreCase( "double" ) ) {
			return new DoubleAttributeDescriptor( dataAdaptor, attribute, type, column, description, units, readonly );
		}
		else if ( type.equalsIgnoreCase( "date" ) ) {
			return new DateAttributeDescriptor( dataAdaptor, attribute, type, column, description, units, readonly );
		}
		else if ( type.equalsIgnoreCase( "string" ) ) {
			return new StringAttributeDescriptor( dataAdaptor, attribute, type, column, description, units, readonly );
		}
		else {
			return null;
		}
	}
	
	
	/** get the attribute name */
	public String getAttribute() {
		return ATTRIBUTE;
	}
	
	
	/** get the database table column */
	public String getColumn() {
		return COLUMN;
	}
	
	
	/** get the units */
	public String getUnits() {
		return UNITS;
	}
	
	
	/** get the description */
	public String getDescription() {
		return DESCRIPTION;
	}
	
	
	/** get the title for the attribute */
	public String getTitle() {
		return UNITS == null ? DESCRIPTION : DESCRIPTION + " (" + UNITS + ")";
	}
	
	
	/** determine if this attribute is read only */
	public boolean isReadOnly() {
		return READ_ONLY;
	}
	
	
	public Class<?> getDisplayValueClass(){
        return getValueClass();
    }
	public Object getDisplayValue( final ColumnType value ) { return value; }
	
	abstract public Class<?> getValueClass();
	abstract public ColumnType getValue( final ResultSet resultSet ) throws SQLException ;
	abstract public ColumnType parseValue( final String valueString );
	abstract public void publishValue( final PreparedStatement statement, final int index, final Object value ) throws SQLException;
}



/** database column of type double */
class DoubleAttributeDescriptor extends AttributeDescriptor<Double> {
	/** number format */
	final protected String FORMAT;
	
	
	/** Constructor */
	public DoubleAttributeDescriptor( final DataAdaptor adaptor, final String attribute, final String type, final String column, final String description, final String units, final boolean readonly ) {
		super( attribute, type, column, description, units, readonly );
		FORMAT = adaptor.hasAttribute( "format" ) ? adaptor.stringValue( "format" ) : "0.0000E0";
	}	
	
	
	public Class<FormattedNumber> getValueClass() {
		return FormattedNumber.class;
	}
	
	
	public Class<FormattedNumber> getDisplayValueClass() { return getValueClass(); }
	
	public Object getDisplayValue( final Double value ) { 
		return value != null ? new FormattedNumber( FORMAT, value ) : null;
	}
	
	
	public Double getValue( final ResultSet resultSet ) throws SQLException {
		final double value = resultSet.getDouble( COLUMN );
		return resultSet.wasNull() ? null : value;
	}

	
	public Double parseValue( final String valueString ) {
		try {
			return new Double( valueString );
		}
		catch( Exception exception ) {
			return null;
		}
	}
	
	
	public void publishValue( final PreparedStatement statement, final int index, final Object value ) throws SQLException {
		if ( value != null ) {
			statement.setDouble( index, (Double)value );
		}
		else {
			statement.setNull( index, java.sql.Types.DOUBLE );
		}
	}
}



/** database column of type string */
class StringAttributeDescriptor extends AttributeDescriptor<String> {
	/** maximum allowed size of string */
	final protected int WIDTH;
	
	
	/** Constructor */
	public StringAttributeDescriptor( final DataAdaptor adaptor, final String attribute, final String type, final String column, final String description, final String units, final boolean readonly ) {
		super( attribute, type, column, description, units, readonly );
		WIDTH = adaptor.hasAttribute( "width" ) ? adaptor.intValue( "width" ) : 255;
	}
	
	
	/** get the width */
	public int getWidth() {
		return WIDTH;
	}
	
	
	public Class<String> getValueClass() {
		return String.class;
	}
	
	
	public String getValue( final ResultSet resultSet ) throws SQLException {
		return resultSet.getString( COLUMN );
	}
	
	
	public String parseValue( final String valueString ) {
		return valueString;
	}
	
	
	public void publishValue( final PreparedStatement statement, final int index, final Object value ) throws SQLException {
		statement.setString( index, (String)value );
	}
}	



/** database column of type date */
class DateAttributeDescriptor extends AttributeDescriptor<Date> {
	/** default date format */
	final static SimpleDateFormat DEFAULT_DATE_FORMAT;
	
	
	// static initializer
	static {
		DEFAULT_DATE_FORMAT = new SimpleDateFormat( "MMM dd, yyyy HH:mm:ss" );
	}
	
	
	/** Constructor */
	public DateAttributeDescriptor( final DataAdaptor adaptor, final String attribute, final String type, final String column, final String description, final String units, final boolean readonly ) {
		super( attribute, type, column, description, units, readonly );
	}	
	
	
	public Class<Date> getValueClass() {
		return Date.class;
	}
	
	
	public Class<String> getDisplayValueClass() {
        return String.class;
    }
	
	
	public Object getDisplayValue( final Date value ) {
        return DEFAULT_DATE_FORMAT.format( value );
    }
	
	
	public Date getValue( final ResultSet resultSet ) throws SQLException {
		return resultSet.getTimestamp( COLUMN );
	}
	
	
	public Date parseValue( final String valueString ) {
		try {
			return DEFAULT_DATE_FORMAT.parse( valueString );
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			return new Date();
		}
	}
	
	
	public void publishValue( final PreparedStatement statement, final int index, final Object value ) throws SQLException {
		statement.setTimestamp( index, new java.sql.Timestamp( ((Date)value).getTime() ) );
	}
}	

