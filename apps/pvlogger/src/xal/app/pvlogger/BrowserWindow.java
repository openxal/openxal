/*
 * BrowserWindow.java
 *
 * Created on Thu Mar 25 08:58:58 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogger;

import xal.service.pvlogger.*;
import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.tools.database.*;
import xal.tools.apputils.files.RecentFileTracker;
import xal.service.pvlogger.apputils.browser.*;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.KeyValueTableModel;
import xal.extension.widgets.swing.KeyValueFilteredTableModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

import java.awt.Dimension;
import java.awt.Container;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * BrowserWindow is the main window for browsing the snapshots.
 * @author tap
 */
public class BrowserWindow extends AcceleratorWindow implements SwingConstants, ScrollPaneConstants {
	/** specify the serializable version as requied */
	static private final long serialVersionUID = 1;

	/** browser model */
	private final BrowserModel BROWSER_MODEL;

	/** controller of the selection state */
	protected BrowserController _controller;

	/** menu for displaying the list of available pvlogger groups */
	protected JComboBox<String> _groupMenu;

	private JButton _exportButton;

	private RecentFileTracker _savedFileTracker;

	
	/**
	 * Constructor
	 * @param aDocument this window's document
	 * @param model document's model
	 */
	public BrowserWindow( final BrowserDocument aDocument, final BrowserModel model ) {
		super( aDocument );

		BROWSER_MODEL = model;
		_controller = new BrowserController( BROWSER_MODEL );
		
		_savedFileTracker = new RecentFileTracker(1, this.getClass(),
		"recent_saved_file");

		makeContent();
		handleWindowEvents();
	}
	
	
	/** Handle window events. When the window opens, request a connection. */
	protected void handleWindowEvents() {
		addWindowListener( new WindowAdapter() {
			public void windowOpened( final WindowEvent event ) {
				try {
					final ConnectionDictionary dictionary = PVLogger.newBrowsingConnectionDictionary();
					if ( dictionary != null ) {
						BROWSER_MODEL.connect( dictionary );
						updateGroupMenu();
					} 
					else {
						requestUserConnection();
					}
				} catch (Exception exception) {
					requestUserConnection();
				}
			}
		});
	}
	
	
	/** Display a connection dialog to the user and connect to the database using the resulting connection dictionary. */
	protected void requestUserConnection() {
		ConnectionDictionary dictionary = PVLogger.newBrowsingConnectionDictionary();
		ConnectionDialog dialog = ConnectionDialog.getInstance( this, dictionary );
		Connection connection = dialog.showConnectionDialog( DatabaseAdaptor.getInstance() );
		if ( connection != null ) {
			BROWSER_MODEL.setDatabaseConnection( connection, dialog.getConnectionDictionary() );
		}

		updateGroupMenu();
	}

	/**
	 * Disable the toolbar
	 * @return false
	 */
	public boolean usesToolbar() {
		return false;
	}
	
	
	/** Build the component contents of the window. */
	protected void makeContent() {
		setSize( 1200, 700 );
		Box mainView = new Box(VERTICAL);
		getContentPane().add(mainView);

		mainView.add(buildQueryView());
		mainView.add(buildContentView());
	}
	
	
	/**
	 * Build the view for querying the database for the machine snapshots.
	 * @return the query view
	 */
	protected Container buildQueryView() {
		Box queryView = new Box(HORIZONTAL);
		queryView.setBorder(BorderFactory.createEtchedBorder());
		final int BUTTON_GAP = 20;

		JButton connectButton = new JButton( "Connect..." );
		queryView.add( connectButton );
		connectButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				requestUserConnection();
			}
		});

		queryView.add(Box.createHorizontalStrut(BUTTON_GAP));
		_groupMenu = new JComboBox<>();
		_groupMenu.setMaximumSize(new Dimension(200, 25));
		queryView.add(_groupMenu);
		_groupMenu.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				try {
					BROWSER_MODEL.selectGroup( (String)_groupMenu.getSelectedItem() );
				}
				catch( Exception exception ) {
					displayError( "Database Exception", "Exception selecting a group:", exception );
				}
			}
		});

		queryView.add(Box.createHorizontalStrut(BUTTON_GAP));
		queryView.add(new JLabel("From:"));
		final SpinnerDateModel fromDateModel = new SpinnerDateModel();
		fromDateModel.setValue( new Date( new Date().getTime() - 24 * 3600 * 1000 ) );		// go back one day
		JSpinner fromSpinner = new JSpinner(fromDateModel);
		fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner,
				"MMM dd, yyyy HH:mm:ss"));
		fromSpinner.setMaximumSize(new Dimension(200, 25));
		queryView.add(fromSpinner);

		queryView.add(Box.createHorizontalStrut(10));
		queryView.add(new JLabel("To:"));
		final SpinnerDateModel toDateModel = new SpinnerDateModel();
		toDateModel.setValue( new Date( new Date().getTime() + 3600 * 1000 ) );		// look ahead an hour
		JSpinner toSpinner = new JSpinner(toDateModel);
		toSpinner.setEditor(new JSpinner.DateEditor(toSpinner,
				"MMM dd, yyyy HH:mm:ss"));
		toSpinner.setMaximumSize(new Dimension(200, 25));
		queryView.add(toSpinner);

		queryView.add(Box.createHorizontalStrut(BUTTON_GAP));
		JButton fetchButton = new JButton("Fetch");
		queryView.add(fetchButton);
		fetchButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				try {
					final String groupType = (String)_groupMenu.getSelectedItem();
					final Date startDate = fromDateModel.getDate();
					final Date endDate = toDateModel.getDate();
					BROWSER_MODEL.fetchMachineSnapshots( startDate, endDate );
					_exportButton.setEnabled( false );
				}
				catch( Exception exception ) {
					displayError( "Database Exception", "Exception fetching snapshots:", exception );
				}
			}
		});

		queryView.add(Box.createHorizontalGlue());

		return queryView;
	}
	
	
	/**
	 * Build the content view that excludes the query view.
	 * @return the content view
	 */
	protected Container buildContentView() {
		final JSplitPane selectionPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, buildSignalView(), buildSnapshotListView() );
		selectionPane.setResizeWeight( 0.5 );
		
		final JSplitPane mainPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, selectionPane, buildSnapshotDetailView() );
		mainPane.setResizeWeight( 0.5 );
		
		addWindowListener( new WindowAdapter() {
			public void windowOpened( final WindowEvent event ) {
				mainPane.setDividerLocation( 0.5 );
			}
		});

		Box contentView = new Box( BoxLayout.X_AXIS );
		contentView.add( mainPane );

		return contentView;
	}
	
	
	/**
	 * Build the view that displays the list of signals for the selected channel group.
	 * @return the signal view
	 */
	protected Container buildSignalView() {
		Box signalView = new Box( BoxLayout.Y_AXIS );

		signalView.add( new JLabel( "Signals:" ) );

		Box filterRow = new Box(BoxLayout.X_AXIS);
		signalView.add(filterRow);
		filterRow.add( new JLabel( "Contains: " ) );
		final JTextField signalFilterField = new JTextField( 50 );
		signalFilterField.setMaximumSize( signalFilterField.getPreferredSize() );
		filterRow.add(signalFilterField);
		JButton clearButton = new JButton( "X" );
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				signalFilterField.setText("");
			}
		});
		filterRow.add(clearButton);

		Box buttonRow = new Box(BoxLayout.X_AXIS);
		signalView.add(buttonRow);
		JButton selectButton = new JButton("Select");
		selectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_controller.selectSignals( true );
			}
		});
		buttonRow.add(selectButton);
		JButton unselectButton = new JButton("Unselect");
		buttonRow.add(unselectButton);
		unselectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_controller.selectSignals( false );
			}
		});

		final KeyValueFilteredTableModel<PVRecord> signalTableModel = _controller.getPVTableModel();
		signalTableModel.setInputFilterComponent( signalFilterField );
		final JTable channelTable = new JTable( signalTableModel );
		signalView.add( channelTable.getTableHeader() );
		signalView.add( new JScrollPane( channelTable ) );
		final TableColumn selectionColumn = channelTable.getColumnModel().getColumn( 0 );
		selectionColumn.setMaxWidth( new JLabel(" Use ").getPreferredSize().width );

		return signalView;
	}
	
	
	/**
	 * Build the view that displays the list of fetched machine snapshots
	 * @return the snapshot list view
	 */
	protected Container buildSnapshotListView() {
		Box listView = new Box( BoxLayout.Y_AXIS );

		listView.add( new JLabel( "Machine Snapshots:" ) );
		Box tableView = new Box( BoxLayout.Y_AXIS );
		listView.add( tableView );
		final KeyValueTableModel<MachineSnapshot> machineSnapshotTableModel = _controller.getMachineSnapshotTableModel();
		final JTable snapshotTable = new JTable( machineSnapshotTableModel );
		snapshotTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		tableView.add( snapshotTable.getTableHeader() );
		tableView.add( new JScrollPane( snapshotTable ) );

		final JButton plotButton = new JButton( "Plot" );
		_exportButton = new JButton( "Export Plot Data" );
		_exportButton.setEnabled( false );
		Box buttonView = new Box( BoxLayout.X_AXIS );
		buttonView.add( plotButton );
		buttonView.add( _exportButton );

		listView.add( buttonView );

		snapshotTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			public void valueChanged( ListSelectionEvent event ) {
				if (!event.getValueIsAdjusting()) {
					final int selectedRow = snapshotTable.getSelectedRow();
					final int selectedModelRow = snapshotTable.convertRowIndexToModel( selectedRow );
					if ( selectedModelRow < 0 ) {
						_controller.setSelectedSnapshot( null );
					}
					else if ( selectedModelRow < machineSnapshotTableModel.getRowCount() ) {
						final MachineSnapshot selectedSnapshot = machineSnapshotTableModel.getRecordAtRow( selectedModelRow );
						_controller.setSelectedSnapshot( selectedSnapshot );
					}
					else {
						snapshotTable.clearSelection();
					}
				}
			}
		});

		final SignalHistoryPlotWindow plotWindow = new SignalHistoryPlotWindow( _controller );
		
		plotButton.addActionListener(new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				plotWindow.showNear( BrowserWindow.this );
				_exportButton.setEnabled( true );
			}
		});

		_exportButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				String currentDirectory = _savedFileTracker.getRecentFolderPath();

				JFrame frame = new JFrame();
				JFileChooser fileChooser = new JFileChooser( currentDirectory );

				int status = fileChooser.showSaveDialog( frame );
				if ( status == JFileChooser.APPROVE_OPTION ) {
					_savedFileTracker.cacheURL( fileChooser.getSelectedFile() );

					File file = fileChooser.getSelectedFile();
					try {
						FileWriter fileWriter = new FileWriter( file );
						NumberFormat nf = NumberFormat.getNumberInstance();
						nf.setMaximumFractionDigits(12);
						nf.setMinimumFractionDigits(6);
						
						final FunctionGraphsJPanel graph = plotWindow.getChart();
						// write data to the selected file
						for ( int i = 0 ; i < graph.getNumberOfInstanceOfGraphData() ; i++ ) {
							fileWriter.write( "time\t\t" + graph.getInstanceOfGraphData(i).getGraphName() + "\t" );
						}
						fileWriter.write("\n");
						
						for ( int i = 0 ; i < graph.getInstanceOfGraphData(0).getNumbOfPoints() ; i++ ) {
							for (int j=0; j<graph.getNumberOfInstanceOfGraphData(); j++) {
								fileWriter.write( nf.format( graph.getInstanceOfGraphData(j).getX(i) ) + "\t" + nf.format( graph.getInstanceOfGraphData(j).getY(i) ) + "\t");
							}
							fileWriter.write( "\n" );
						}
						
						fileWriter.close();
					} catch (IOException ie) {
						JFrame frame1 = new JFrame();
						JOptionPane.showMessageDialog( frame1, "Cannot open the file" + file.getName() + "for writing", "Warning!", JOptionPane.PLAIN_MESSAGE );
						frame1.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
					}

				}

			}
		});

		return listView;
	}
	
	
	/**
	 * Build the snapshot detail view which displays detailed information about
	 * the snapshot including the comment and the list of channel snapshots associated with the selected signals.
	 * @return the snapshot detail view
	 */
	protected Container buildSnapshotDetailView() {
		Box detailView = new Box( VERTICAL );
		final JLabel titleLabel = new JLabel( "Selected Snapshot:" );
		detailView.add( titleLabel );
		detailView.add( new JLabel( "Comment:" ) );
		final JTextArea commentTextView = new JTextArea();
		commentTextView.setEditable( false );

		final KeyValueTableModel<ChannelSnapshot> detailTableModel = _controller.getChannelSnapshotTableModel();
		final JTable snapshotDetailTable = new JTable( detailTableModel );
		snapshotDetailTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

		final KeyValueTableModel<ArrayItemRecord<Double>> valueTableModel = new KeyValueTableModel<ArrayItemRecord<Double>>();
		valueTableModel.setKeyPaths( "arrayIndex", "value" );
		final JTable valueTable = new JTable( valueTableModel );
				
		final JSplitPane tablesPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, new JScrollPane( snapshotDetailTable ), new JScrollPane( valueTable ) );
		tablesPane.setResizeWeight( 0.5 );
		final JSplitPane mainPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, commentTextView, tablesPane );
		detailView.add( mainPane );
		
		snapshotDetailTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			public void valueChanged( final ListSelectionEvent event ) {
				if ( !event.getValueIsAdjusting() ) {
					final int selectedRow = snapshotDetailTable.getSelectedRow();
					final int detailModelRow = snapshotDetailTable.convertRowIndexToModel( selectedRow );
					final ChannelSnapshot channelSnapshot = detailModelRow >= 0 ? detailTableModel.getRecordAtRow( detailModelRow ) : null;
					if ( channelSnapshot != null ) {
						final double[] snapshotValueArray = channelSnapshot.getValue();
						if ( snapshotValueArray != null ) {
							final List<ArrayItemRecord<Double>> valueRecords = new ArrayList<ArrayItemRecord<Double>>();
							for ( int index = 0 ; index < snapshotValueArray.length ; index++ ) {
								valueRecords.add( new ArrayItemRecord<Double>( index, snapshotValueArray[index]	)	);
							}
							valueTableModel.setRecords( valueRecords );
						}
						else {
							valueTableModel.setRecords( Collections.<ArrayItemRecord<Double>>emptyList() );
						}
					}
					else {
						valueTableModel.setRecords( Collections.<ArrayItemRecord<Double>>emptyList() );
					}
				}
			}
		});
		
		_controller.addBrowserControllerListener( new BrowserControllerListener() {
			/**
			 * event indicating that a snapshot has been selected
			 * @param controller manages selection state
			 * @param snapshot The snapshot that has been selected
			 */
			public void snapshotSelected( BrowserController controller, MachineSnapshot snapshot ) {
				if ( snapshot != null ) {
					commentTextView.setText( snapshot.getComment() );
					String title = "Selected Snapshot has " + snapshot.getChannelCount() + " channels logged";
					titleLabel.setText( title );
				} 
				else {
					commentTextView.setText( "" );
					valueTableModel.setRecords( Collections.<ArrayItemRecord<Double>>emptyList() );
				}
			}

			/**
			 * event indicating that the selected channel group changed
			 * @param source  browser controller sending this notice
			 * @param newGroup newly selected channel group
			 */
			public void selectedChannelGroupChanged( final BrowserController source, final ChannelGroup newGroup) {}

			/**
			 * Event indicating that the selected signals have changed
			 * @param source  controller sending the event
			 * @param selectedSignals  new collection of selected signals
			 */
			public void selectedSignalsChanged( final BrowserController source, final Collection<String> selectedSignals ) {}
		});

		return detailView;
	}
	
	
	/** Update the menu that displays the list of channel groups. */
	protected void updateGroupMenu() {
		try {
			final JComboBox<String> groupMenu = _groupMenu;
			groupMenu.removeAllItems();
			final String[] types = BROWSER_MODEL.getLoggerTypes();
			
			if ( types == null ) {
				BROWSER_MODEL.selectGroup( null );
				return;
			}
			
			final List<String> typeList = new ArrayList<String>( types.length );
			for ( int index = 0 ; index < types.length ; index++ ) {
				typeList.add( types[index] );
			}
			Collections.sort( typeList );
			for ( final String type : typeList ) {
				groupMenu.addItem( type );
			}
			if ( types.length > 0 ) {
				BROWSER_MODEL.selectGroup( typeList.get( 0 ) );
			}
		}
		catch( Exception exception ) {
			displayError( "Database Exception", "Exception updating the group menu:", exception );
		}
	}
}



/** Record for an item in an array */
class ArrayItemRecord<ValueType> {
	/** value for the item */
	final private ValueType VALUE;
	
	/** index of the item in the array */
	final private int ARRAY_INDEX;


	/** Constructor */
	public ArrayItemRecord( final int arrayIndex, final ValueType value ) {
		ARRAY_INDEX = arrayIndex;
		VALUE = value;
	}


	/** Get the value */
	public ValueType getValue() {
		return VALUE;
	}


	/** Get the array index */
	public int getArrayIndex() {
		return ARRAY_INDEX;
	}
}
