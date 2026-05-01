//
//  HistoryController.java
//  xal
//
//  Created by Thomas Pelaia on 8/9/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.tripviewer;

import xal.service.tripmonitor.*;
import xal.extension.application.*;
import xal.extension.bricks.WindowReference;
import xal.tools.database.*;

import java.net.URL;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.util.*;
import java.util.Date;
import java.sql.Connection;


/** controller of the history view */
public class HistoryController {
	/** table model for displaying trips */
	final protected TripsTableModel TRIPS_TABLE_MODEL;
	
	/** reference to the main window */
	final protected WindowReference _mainWindowReference;
	
	/** the current database connection */
	protected Connection _connection;
	
	/** the current connection dictionary */
	protected ConnectionDictionary _connectionDictionary;
	
	/** the trip monitor configuration */
	final protected Configuration _monitorConfiguration;
	
	/** set the trip monitor filter */
	protected TripMonitorFilter _tripMonitorFilter;
	
	/** all fetched trip records */
	protected List<TripRecord> _tripRecords;
	
	/** trip records sorted by the trip record ordering and filtered by specified filters */
	protected List<TripRecord> _sortedTripRecords;
	
	/** list for displaying the trip counter PV formats */
	protected JList<String> _tripCounterPVFormatList;
	
	/** list of node keys */
	protected JList<String> _nodeKeyList;
	
	/** comparator used to sort the trip records */
	protected Comparator<TripRecord> _tripRecordOrdering;
	
	
	
	/** Create a new empty document */
    public HistoryController( final WindowReference mainWindowReference ) {
		_mainWindowReference = mainWindowReference;
		
		_tripRecords = new ArrayList<TripRecord>();
		_sortedTripRecords = _tripRecords;
		_monitorConfiguration = new Configuration();
		
		TRIPS_TABLE_MODEL = new TripsTableModel();
		
		generateDatabaseActions();
		generateDataViews();
		generateSortActions();
	}
	
	
	/** get the main window */
	protected XalWindow getMainWindow() {
		return (XalWindow)_mainWindowReference.getWindow();
	}
	
	
	/** handle trip monitor filter selection */
	protected void handleTripMonitorFilterSelection( final JComboBox<String> tripMonitorTypeMenu ) {
		final TripMonitorFilter monitorFilter = getSelectedTripMonitorFilter( tripMonitorTypeMenu );
		setTripMonitorFilter( monitorFilter );
	}
	
	
	/** make the database actions */
    // Had to suppress warnings getView returns object that cannot be cast.
    @SuppressWarnings ("unchecked")
	protected void generateDatabaseActions() {
		final WindowReference windowReference = _mainWindowReference;
		
		final JComboBox<String> tripMonitorTypeMenu = (JComboBox<String>)windowReference.getView( "tripMonitorTypeMenu" );
		final List<String> tripMonitorFilterNames = _monitorConfiguration.getTripMonitorFilterNames();
		for ( final String name : tripMonitorFilterNames ) {
			tripMonitorTypeMenu.addItem( name );
		}
		
		final JButton connectButton = (JButton)windowReference.getView( "connectButton" );
		connectButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				requestUserConnection();
			}
		});
		
		final JSpinner fromSpinner = (JSpinner)windowReference.getView( "fromSpinner" );
		final SpinnerDateModel fromSpinnerModel = new SpinnerDateModel();
		fromSpinnerModel.setValue( new java.util.Date( new java.util.Date().getTime() - 24 * 3600 * 1000 ) );		// go back one day
		fromSpinner.setModel( fromSpinnerModel );
		
		final JSpinner toSpinner = (JSpinner)windowReference.getView( "toSpinner" );
		final SpinnerDateModel toSpinnerModel = new SpinnerDateModel();
		toSpinnerModel.setValue( new java.util.Date( new java.util.Date().getTime() + 3600 * 1000 ) );		// go forward one hour
		toSpinner.setModel( toSpinnerModel );
		
		final JButton fetchButton = (JButton)windowReference.getView( "fetchButton" );
		fetchButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final Date fromTime = (Date)fromSpinner.getValue();
				final Date toTime = (Date)toSpinner.getValue();
				handleTripMonitorFilterSelection( tripMonitorTypeMenu );
				final List<TripRecord> tripRecords = fetchTripRecordsBetween( _tripMonitorFilter, fromTime, toTime );
				setTripRecords( tripRecords );
				filterTripRecords();
			}
		});
	}
	
	
	/** generate data views */
    
    // Had to suppress warnings getView returns object that cannot be cast.
    @SuppressWarnings ("unchecked")
    
	protected void generateDataViews() {
		final WindowReference windowReference = _mainWindowReference;
		
		_tripCounterPVFormatList = (JList<String>)windowReference.getView( "tripCounterPVList" );
		_tripCounterPVFormatList.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		_tripCounterPVFormatList.addListSelectionListener( new ListSelectionListener() {
			public void valueChanged( final ListSelectionEvent event ) {
				if ( !event.getValueIsAdjusting() ) {
					filterTripRecords();
				}
			}
		});
		
		_nodeKeyList = (JList<String>)windowReference.getView( "nodeList" );
		_nodeKeyList.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		_nodeKeyList.addListSelectionListener( new ListSelectionListener() {
			public void valueChanged( final ListSelectionEvent event ) {
				if ( !event.getValueIsAdjusting() ) {
					filterTripRecords();
				}
			}
		});
		
		final JTable tripRecordTable = (JTable)windowReference.getView( "tripRecordTable" );
		tripRecordTable.setModel( TRIPS_TABLE_MODEL );
	}
	
	
	/** generate sort actions */
	protected void generateSortActions() {
		final WindowReference windowReference = _mainWindowReference;
		
		final JRadioButton timeSortButton = (JRadioButton)windowReference.getView( "timeSortButton" );
		final JRadioButton pvSortButton = (JRadioButton)windowReference.getView( "PVSortButton" );
		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add( timeSortButton );
		buttonGroup.add( pvSortButton );
		
		timeSortButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( timeSortButton.isSelected() ) {
					setSortOrdering( TripRecord.timestampComparator() );
				}
			}
		});
		
		pvSortButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( pvSortButton.isSelected() ) {
					setSortOrdering( TripRecord.pvComparator() );
				}
			}
		});
	}
	
	
	/** fetch trip records for the given monitor and between the specified dates */
	final protected List<TripRecord> fetchTripRecordsBetween( final TripMonitorFilter monitorFilter, final Date fromDate, final Date toDate ) {
		final PersistentStore persistentStore = monitorFilter.getPersistentStore();
		
		if ( _connection == null ) {
			connectToDatabase();
		}
		
		if ( _connection != null ) {
			final List<TripRecord> tripRecords = persistentStore.fetchTripRecordsBetween( _connection, fromDate, toDate );
			if ( tripRecords == null ) {
				connectToDatabase();
				return fetchTripRecordsBetween( monitorFilter, fromDate, toDate );
			}
			return tripRecords;
		}
		else {
			return new ArrayList<TripRecord>();
		}
	}
	
	
	/** set the trip monitor filter */
	public void setTripMonitorFilter( final TripMonitorFilter monitorFilter ) {
		_tripMonitorFilter = monitorFilter;
		updateTripMonitorPVFormatList();
	}
	
	
	/** set the trip records */
	protected void setTripRecords( final List<TripRecord> tripRecords ) {
		//System.out.println( tripRecords );
		_tripRecords = tripRecords;
		updateNodeKeyList();
		setAndSortFilteredTripRecords( tripRecords );
	}
	
	
	/** apply the sort ordering to the trip records */
	protected void applySortOrdering() {
		final Comparator<TripRecord> ordering = _tripRecordOrdering;
		applySortOrderingToRecords( _tripRecords, ordering );
	}
	
	
	/** apply the specified sort ordering to the trip records */
	static protected void applySortOrderingToRecords( final List<TripRecord> tripRecords, final Comparator<TripRecord> ordering ) {
		if ( ordering != null ) {
			Collections.sort( tripRecords, ordering );
		}
	}
	
	
	/** set the specified sort ordering */
	protected void setSortOrdering( final Comparator<TripRecord> ordering ) {
		_tripRecordOrdering = ordering;
		applySortOrderingToRecords( _sortedTripRecords, ordering );
		TRIPS_TABLE_MODEL.setTripRecords( _sortedTripRecords );
	}
	
	
	/** set and sort the specified trip records */
	protected void setAndSortFilteredTripRecords( final List<TripRecord> filteredTripRecords ) {
		applySortOrderingToRecords( filteredTripRecords, _tripRecordOrdering );
		_sortedTripRecords = filteredTripRecords;
		TRIPS_TABLE_MODEL.setTripRecords( filteredTripRecords );
	}
	
	
	/** update the trip monitor PV format list */
	protected void updateTripMonitorPVFormatList() {
		final List<TripChannelFilter> channelFilters = _tripMonitorFilter.getTripChannelFilters();
		final Vector<String> listData = new Vector<String>( channelFilters.size() );
		
		for ( final TripChannelFilter channelFilter : channelFilters ) {
			listData.add( channelFilter.getPVFormat() );
		}
		
		_tripCounterPVFormatList.setListData( listData );
	}
	
	
	/** update the node key list */
	protected void updateNodeKeyList() {
		final TripMonitorFilter tripMonitorFilter = _tripMonitorFilter;
		final Vector<String> nodeKeys = new Vector<String>();
		final Set<String> pvSet = new HashSet<String>();
		final List<TripRecord> tripRecords = _tripRecords;
		
		for ( final TripRecord record : tripRecords ) {
			final String pv = record.getPV();
			if ( !pvSet.contains( pv ) ) {
				pvSet.add( pv );
				final String nodeKey = tripMonitorFilter.getNodeKey( pv );
				if ( !nodeKeys.contains( nodeKey ) ) {
					nodeKeys.add( nodeKey );
				}
			}
		}
		
		Collections.sort( nodeKeys );
		_nodeKeyList.setListData( nodeKeys );
	}
	
	
	/** get the selected trip monitor filter */
	protected TripMonitorFilter getSelectedTripMonitorFilter( final JComboBox<String> menu ) {
		final String selection = menu.getSelectedItem().toString();
		return _monitorConfiguration.getTripMonitorFilter( selection );
	}
	
	
	/** filter the trip records */
	protected void filterTripRecords() {
		final List<TripRecord> tripRecords = _tripRecords;
		
		final Object[] selectedPVFormats = _tripCounterPVFormatList.getSelectedValuesList().toArray();
		final Set<String> pvFormats = new HashSet<String>( selectedPVFormats.length );
		for ( final Object pvFormat : selectedPVFormats ) {
			pvFormats.add( pvFormat.toString() );
		}
		
		final List<TripChannelFilter> allChannelFilters = _tripMonitorFilter.getTripChannelFilters();
		final List<TripChannelFilter> channelFilters = new ArrayList<TripChannelFilter>();
		final int pvFormatCount = pvFormats.size();
		if ( pvFormatCount == 0 || pvFormatCount == allChannelFilters.size() ) {
			for ( final TripChannelFilter channelFilter : allChannelFilters ) {
				channelFilters.add( channelFilter );
			}
		}
		else {
			for ( final TripChannelFilter channelFilter : allChannelFilters ) {
				if ( pvFormats.contains( channelFilter.getPVFormat() ) ) {
					channelFilters.add( channelFilter );
				}
			}
		}
		
		final Set<String> nodeKeys = getNodeKeysForFiltering();
		
		final Set<String> pvs = new HashSet<String>();
		for ( final String nodeKey : nodeKeys ) {
			for ( final TripChannelFilter channelFilter : channelFilters ) {
				pvs.add( channelFilter.getPV( nodeKey ) );
			}
		}
		
		final List<TripRecord> filteredRecords = new ArrayList<TripRecord>( tripRecords.size() );
		for ( final TripRecord tripRecord : tripRecords ) {
			if ( pvs.contains( tripRecord.getPV() ) ) {
				filteredRecords.add( tripRecord );
			}
		}
		
		setAndSortFilteredTripRecords( filteredRecords );
	}
	
	
	/** get the node keys */
	protected Set<String> getNodeKeysForFiltering() {
		final Object[] selectedKeys = _nodeKeyList.getSelectedValuesList().toArray();
		final ListModel<String> model = _nodeKeyList.getModel();
		final int keyCount = model.getSize();
		
		if ( selectedKeys == null || selectedKeys.length == 0 || selectedKeys.length == keyCount ) {
			final Set<String> nodeKeys = new HashSet<String>( keyCount );
			for ( int index = 0 ; index < keyCount ; index++ ) {
				final Object element = model.getElementAt( index );
				if ( element != null ) {
					nodeKeys.add( element.toString() );
				}
			}
			return nodeKeys;
		}
		else {
			final Set<String> nodeKeys = new HashSet<String>( selectedKeys.length );
			
			for ( final Object nodeKey : selectedKeys ) {
				nodeKeys.add( nodeKey.toString() );
			}
			
			return nodeKeys;
		}
	}
	
	
	/** set the database connection */
	protected void setDatabaseConnection( final Connection connection ) {
		clearDatabaseConnection();
		_connection = connection;
	}
	
	
	/** connect to the existing dictionary if it exists */
	protected void connectToDatabase() {
		final ConnectionDictionary baseDictionary = _connectionDictionary != null ? _connectionDictionary : ConnectionDictionary.getPreferredInstance( "reports" );
		if ( baseDictionary == null || !baseDictionary.hasRequiredInfo() ) {
			requestUserConnection();
		}
		else {
			connectToDatabase( baseDictionary );
		}
	}
	
	
	/** connect to the database using the specified connection dictionary */
	protected void connectToDatabase( final ConnectionDictionary dictionary ) {
		_connectionDictionary = dictionary;
		DatabaseAdaptor adaptor = dictionary.getDatabaseAdaptor();
		if ( adaptor == null ) {
			adaptor = DatabaseAdaptor.getInstance();
			if ( adaptor == null ) {
				getMainWindow().displayError( "Connection Error", "Cannot find a database adaptor." );
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
			getMainWindow().displayError( "Connection Exception", "Error connecting to the database", exception );
			return;
		}
		setDatabaseConnection( connection );
	}
	
	
	/** Display a connection dialog to the user and connect to the database using the resulting connection dictionary. */
	protected void requestUserConnection() {
		final ConnectionDictionary baseDictionary = _connectionDictionary != null ? _connectionDictionary : ConnectionDictionary.defaultDictionary();
		final ConnectionDictionary dictionary = ConnectionDialog.showDialog( getMainWindow(), baseDictionary, "Connect" );
		
		if ( dictionary == null ) {
			setDatabaseConnection( null );
			return;
		}
		else if ( dictionary.hasRequiredInfo() ) {
			connectToDatabase( dictionary );
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
