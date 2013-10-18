/*
 * ScoreWindow.java
 *
 * Created on 8/12/2003, 10:25 AM
 */

package xal.app.score;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import javax.swing.DefaultListModel;
import java.awt.Color;
import java.awt.event.*;
import java.sql.Connection;
import javax.swing.event.*;
import javax.swing.table.*;
import java.sql.Timestamp;

import xal.tools.apputils.*;
import xal.extension.application.*;
import xal.tools.IconLib;
import xal.extension.widgets.swing.*;
import xal.tools.data.DataTable;
import xal.tools.database.*;

/**
 * ScoreWindow contains the gui components for the score app.
 *
 * @author  jdg
 */
public class ScoreWindow extends XalWindow {
    /** ID for serializable version */
    private static final long serialVersionUID = 1;
	/** split pane container for the system and type lists */
	private JSplitPane _selectorSplitPane;
	
	protected JTabbedPane theTabbedPane;
	protected JPanel sysSelectPanel, typeSelectPanel, selectorPanel;
	private ScoreDocument theDoc;
	private JLabel snapTimeLabel;
	private DefaultListModel<Object> typeListModel, systemListModel;
	protected JList<Object> typeJList, systemJList;
	protected javax.swing.Timer timer;
	protected JTextField errorText;
	/** menu for displaying the list of available pvlogger groups */
	protected JComboBox<String> _groupMenu;
	
	/** button to fetch the snapshots in the selected range */
	private JButton FETCH_SNAPSHOTS_IN_RANGE_BUTTON;
	
	protected ScoreDataModel _model;
	
	/** Creates a new instance of MainWindow */
	public ScoreWindow(XalDocument aDocument, ScoreDataModel model) {
		super( aDocument );
		
		setSize( 1600, 900 );
		theDoc = (ScoreDocument) aDocument;
		_model = model;
		
		typeListModel = new DefaultListModel<Object>();
		systemListModel = new DefaultListModel<Object>();
		typeJList = new JList<Object>(typeListModel);
		systemJList = new JList<Object>(systemListModel);
		snapTimeLabel = new JLabel("No snapshot data");
		errorText = new JTextField();
		errorText.setMaximumSize( new Dimension( 32000, errorText.getPreferredSize().height ) );
		
		makeContent();
		handleWindowEvents();
	}
	
	/**
	* Create the main window subviews     */
	protected void makeContent() {
		Container container = getContentPane();
		
		final Box mainView = new Box( BoxLayout.Y_AXIS );
		container.add( mainView );
		
		final Box selectionView = new Box( BoxLayout.Y_AXIS );
		
		final Box systemBox = new Box( BoxLayout.Y_AXIS );
		systemBox.add( new JLabel( "Select Systems:" ) );
		systemBox.add( new JScrollPane( systemJList ) );
		
		final Box typeBox = new Box( BoxLayout.Y_AXIS );
		typeBox.add( new JLabel( "Select Subsystems:" ) );
		typeBox.add( new JScrollPane( typeJList ) );
					
		_selectorSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, true, systemBox ,typeBox );
		_selectorSplitPane.setMaximumSize( new Dimension( _selectorSplitPane.getPreferredSize().width, 32000 ) );
		_selectorSplitPane.setDividerLocation( 400 );
		
		systemJList.addListSelectionListener( new ListSelectionListener() {
			public void valueChanged( final ListSelectionEvent event ) {
				if ( !event.getValueIsAdjusting() ) {
					setTypesSystems();
				}
			}
		});
		
		typeJList.addListSelectionListener( new ListSelectionListener() {
			public void valueChanged( final ListSelectionEvent event ) {
				if ( !event.getValueIsAdjusting() ) {
					setTypesSystems();
				}
			}
		});
		
		final JButton selectAllButton = new JButton("Select All");
		selectAllButton.addActionListener( new java.awt.event.ActionListener() {
		    public void actionPerformed( java.awt.event.ActionEvent evt ) {
				selectAllTypesAndSystems();
		    }
		});
				
		selectionView.add( _selectorSplitPane );
		selectionView.add( selectAllButton );
		
		theTabbedPane = new JTabbedPane();
		theTabbedPane.setVisible( true );
		
		// Make a Pane for the open window
		final Box openView = new Box( BoxLayout.Y_AXIS );
		openView.add( buildQueryView() );
		theTabbedPane.add( "Open", openView );
						
		final Box tableBox = new Box( BoxLayout.Y_AXIS );
		tableBox.add( theTabbedPane );
		
		final Box snapLabelBox = new Box( BoxLayout.X_AXIS );
		snapLabelBox.add( snapTimeLabel );
		snapLabelBox.add( Box.createHorizontalGlue() );
		tableBox.add( snapLabelBox );		
		
		final JSplitPane mainSplitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, true, selectionView, tableBox );
		mainSplitPane.setOneTouchExpandable( true );
		mainView.add( mainSplitPane );
		mainView.add( errorText );		
		
		//make a timer to periodically update the tables:
		ActionListener taskPerformer = new ActionListener() {
		    public void actionPerformed(ActionEvent evt) {
				tableTask();
		    }
		};	
		timer = new javax.swing.Timer(2000, taskPerformer);
		timer.start();	
	}
	
	
	/** the action to perform when the tables need updating */
	protected void tableTask() {
		final JTable table = theDoc.getScoreTable();
		if( table != null ) {
			final AbstractTableModel tableModel = (AbstractTableModel)table.getModel();
			final int rowCount = tableModel.getRowCount();
			if ( rowCount > 0 ) {
				tableModel.fireTableRowsUpdated( 0, rowCount - 1 );
			}
			else {
				tableModel.fireTableDataChanged();
			}
		}
	}
	
	
	/** add tables from the document to a seperate scollable pane in the big tabbed pane */
    @SuppressWarnings( "unchecked")
    //Suppressed warning on table.getModel() because it cannot be cast as KeyValueFilteredTableModel<ScoreRecord>
	protected void makeScoreTable() {
		// first clear any existing tables (first tab is for comments)
		while( theTabbedPane.getTabCount() > 2 ) theTabbedPane.remove( theTabbedPane.getTabCount() - 1 );
		
		final JTable table = theDoc.getScoreTable();
		final KeyValueFilteredTableModel<ScoreSnapshot> tableModel = (KeyValueFilteredTableModel<ScoreSnapshot>)table.getModel();
		final JScrollPane tableScrollPane = new JScrollPane( table );
		
		final Box controlBox = new Box( BoxLayout.X_AXIS );
		final JTextField filterField = new JTextField();
		filterField.setMaximumSize( new Dimension(32000,50) );
		filterField.putClientProperty( "JTextField.variant", "search" );
		filterField.putClientProperty( "JTextField.Search.Prompt", "Filter Records" );
		tableModel.setInputFilterComponent( filterField );
		controlBox.add( filterField );
		
		final JButton clearButton = new JButton();
		clearButton.setIcon( IconLib.getIcon( "custom", "Clear24.gif" ) );
		controlBox.add( clearButton );
		clearButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				filterField.setText( "" );
			}
		});
		
		final Box mainBox = new Box( BoxLayout.Y_AXIS );
		mainBox.add( controlBox );
		mainBox.add( tableScrollPane );
		
		theTabbedPane.add( "Score", mainBox );
		
		showScoreTab();
		theTabbedPane.setVisible( true );
	}
	
	
	/** show the score tab if it is available and return true upon success */
	public boolean showScoreTab() {
		if( theTabbedPane.getTabCount() > 1 ) {
			theTabbedPane.setSelectedIndex( 1 );
			return true;
		}
		else {
			return false;
		}
	}
	
	
	/** set  all  types + systems as selected and start the makeTable process */
	public void selectAllTypesAndSystems() {
		if( theDoc.theSnapshot != null ) {
			updateSelectionLists( true ) ;
			theDoc.setSelectedTypes( typeJList.getSelectedValuesList().toArray() );
			theDoc.setSelectedSystems( systemJList.getSelectedValuesList().toArray() );
		}
		else {
			theDoc.dumpErr( "Whoa dude - Load up a score snapshot first!!" );
		}
	}
	
	
	/** set  the selected types + systems start the makeTable process */
	private void setTypesSystems() {
		if( theDoc.theSnapshot != null ) {
			setTypes();
			setSystems();
			theDoc.updateScoreTable();
		}
		else {
			theDoc.dumpErr( "Whoa dude - Load up a score snapshot first!!" );
		}
	}
	
	
	/** select a subset of available types */
	private void setTypes() {
		// get the selected types from the JList:
		Object [] types = typeJList.getSelectedValuesList().toArray();
		theDoc.setSelectedTypes( types );
	}
	
	
	/** select a subset of available systems */
	private void setSystems() {
		Object [] systems = systemJList.getSelectedValuesList().toArray();
		theDoc.setSelectedSystems( systems );
	}
	
	
	/** update the lists for the system and type choices 
	 * @param tf - if true, default status is selected, otherwise it is not selected.
	 */
	protected void updateSelectionLists(boolean tf) {
		
		// first clear them incase they previously were used
		// we now have new lists
		typeListModel.clear();
		systemListModel.clear();
		
		Iterator<Object> itr = theDoc.typeList.iterator();
		while(itr.hasNext()) {
		    typeListModel.addElement(itr.next());
		}
		
		itr = theDoc.systemList.iterator();
		while(itr.hasNext()) {
		    systemListModel.addElement(itr.next());
		}
		
		int listLength;
		
		if(tf) {
		    listLength = typeListModel.size();
		    typeJList.setSelectionInterval(0,listLength-1);
		
		    listLength = systemListModel.size();
		    systemJList.setSelectionInterval(0,listLength-1);
		}
	}
	
	
	/** update the snapshot time label */
	protected void updateSnapLabel( final ScoreSnapshot snapshot ) {
		if ( snapshot != null ) {
			snapTimeLabel.setText( "Snapshot taken: " + snapshot.getTimestamp() + ", " + snapshot.getComment() );
		}
		else snapTimeLabel.setText( "No snapshot info" );
	}
	
	
 	/**
	 * Build the view for querying the database for the machine snapshots.
	 * @return the query view
	 */
	protected Container buildQueryView() {
		final KeyValueFilteredTableModel<ScoreSnapshot> snapshotTableModel = new KeyValueFilteredTableModel<ScoreSnapshot>( _model.getSnapshots(), "timestamp", "comment" );
		snapshotTableModel.setMatchingKeyPaths( "comment" );
		final JTable snapshotTable = new JTable( snapshotTableModel );
		snapshotTable.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
		final int TIMESTAMP_COLUMN_WIDTH = new JLabel( " YYYY-MM-DD HH:MM:SS.SSS " ).getPreferredSize().width;	// constrain the timestamp column width
		final javax.swing.table.TableColumn TIMESTAMP_COLUMN;
		TIMESTAMP_COLUMN = snapshotTable.getColumnModel().getColumn( 0 );
		TIMESTAMP_COLUMN.setMinWidth( TIMESTAMP_COLUMN_WIDTH );
		TIMESTAMP_COLUMN.setMaxWidth( 2 * TIMESTAMP_COLUMN_WIDTH );
		
		final JTextField snapshotFilterField = new JTextField();
		snapshotTableModel.setInputFilterComponent( snapshotFilterField );
		snapshotFilterField.setMaximumSize( new Dimension( 32000, 50 ) );
		snapshotFilterField.putClientProperty( "JTextField.variant", "search" );
		snapshotFilterField.putClientProperty( "JTextField.Search.Prompt", "Filter Snapshots" );
		final Box snapshotFilterBox = new Box( BoxLayout.X_AXIS );
		snapshotFilterBox.add( snapshotFilterField );
		
		final JButton filterClearButton = new JButton();
		filterClearButton.setIcon( IconLib.getIcon( "custom", "Clear24.gif" ) );
		snapshotFilterBox.add( filterClearButton );
		filterClearButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				snapshotFilterField.setText( "" );
			}
		});
		
		Box queryPanel = new Box( BoxLayout.Y_AXIS );		
		Box queryView2 = new Box( BoxLayout.X_AXIS );
		
		queryView2.setBorder( BorderFactory.createEtchedBorder() );
		final int BUTTON_GAP = 20;
		
		JButton connectButton = new JButton("Connect");
		queryView2.add(connectButton);
		connectButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				requestUserConnection();
			}
		});
		queryView2.setMaximumSize( new java.awt.Dimension( 32000, 2 * connectButton.getPreferredSize().height ) );	// constrain the vertical height using reference button
		
		queryView2.add( Box.createHorizontalStrut(BUTTON_GAP) );
		_groupMenu = new JComboBox<String>();
		_groupMenu.setMaximumSize( new Dimension(200, 25) );
		queryView2.add(_groupMenu);
		_groupMenu.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				_model.setType( (String)_groupMenu.getSelectedItem() );
			}
		});
		
		queryView2.add( Box.createHorizontalStrut(BUTTON_GAP) );
		queryView2.add( new JLabel("From:") );
		final SpinnerDateModel fromDateModel = new SpinnerDateModel();
		JSpinner fromSpinner = new JSpinner(fromDateModel);
		fromSpinner.setEditor( new JSpinner.DateEditor(fromSpinner, "MMM dd, yyyy HH:mm:ss") );
		fromSpinner.setMaximumSize( new Dimension( 200, 25 ) );
		Calendar calFrom = Calendar.getInstance();
		calFrom.set(Calendar.MONTH, calFrom.get( Calendar.MONTH)-6 );	// start six months back
		fromSpinner.setValue(calFrom.getTime());
		queryView2.add(fromSpinner);
		
		queryView2.add( Box.createHorizontalStrut(10) );
		queryView2.add( new JLabel("To:") );
		final SpinnerDateModel toDateModel = new SpinnerDateModel();
		JSpinner toSpinner = new JSpinner(toDateModel);
		toSpinner.setEditor( new JSpinner.DateEditor(toSpinner, "MMM dd, yyyy HH:mm:ss") );
		toSpinner.setMaximumSize( new Dimension(200, 25) );
		Calendar calTo = Calendar.getInstance();
		calTo.set(Calendar.DAY_OF_MONTH, calTo.get(Calendar.DAY_OF_MONTH)+1);
		toSpinner.setValue(calTo.getTime());		
		queryView2.add(toSpinner);
		
		queryView2.add( Box.createHorizontalStrut(BUTTON_GAP) );
		FETCH_SNAPSHOTS_IN_RANGE_BUTTON = new JButton("Fetch Snapshots in Range");
		queryView2.add( FETCH_SNAPSHOTS_IN_RANGE_BUTTON );
		
		queryView2.add( Box.createHorizontalGlue() );
		queryPanel.add(queryView2);
		
		snapshotTable.getColumnModel().getColumn(0).setPreferredWidth(75);
		snapshotTable.getColumnModel().getColumn(1).setPreferredWidth(250);
		final JScrollPane tableScrollPane = new JScrollPane(snapshotTable);
		queryPanel.add( snapshotFilterBox );
		queryPanel.add( tableScrollPane );		
		
		FETCH_SNAPSHOTS_IN_RANGE_BUTTON.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				final String groupType = (String)_groupMenu.getSelectedItem();
				final Date startDate = fromDateModel.getDate();
				final Date endDate = toDateModel.getDate();
				_model.fetchScoreSnapshots( _model.getType(), startDate, endDate );
				snapshotTableModel.setRecords( _model.getSnapshots() );
				
				// move the scroll bar to the end of the table after the table has been updated
				SwingUtilities.invokeLater( new Runnable() {
					public void run() {
						final JScrollBar verticalScroller = tableScrollPane.getVerticalScrollBar();
						verticalScroller.setValue( verticalScroller.getMaximum() - verticalScroller.getVisibleAmount() );	// set the vertical scroll bar to its maximum value
					}
				});
			}
		});
				
		// fetch snapshot buttons:
		Box queryView3 = new Box(javax.swing.SwingConstants.HORIZONTAL);		
		JButton selectSnapshotButton = new JButton("Fetch selected snapshot");
		selectSnapshotButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				final int selectedRow = snapshotTable.getSelectedRow();
				if( selectedRow == -1 ) {
					_model.fetchScoreGroup( _model.getType() );
					theDoc.setSnapshot( new ScoreSnapshot(_model.getFetchedScoreGroup()) );
					theDoc.setupFromSnapshot();
				}
				else {
					final int modelRow = snapshotTable.convertRowIndexToModel( selectedRow );
					final ScoreSnapshot snapshot = snapshotTableModel.getRecordAtRow( modelRow );
					if ( snapshot != null ) {
						final Timestamp date  = snapshot.getTimestamp();
						_model.fetchScoreSnapshot( date, _model.getType() );
						if( _model.getFetchedSnapshot().getRowCount() < 1 ) {
							theDoc.dumpErr( "No data is in the selected snapshot" );
						}
						else {
							theDoc.setSnapshot( _model.getFetchedSnapshot() );
							theDoc.setupFromSnapshot();
						}
					}
				}
			}
		});
		queryView3.add(selectSnapshotButton);		
		queryPanel.add(queryView3);
		
		return queryPanel;
	}
	
	
	/** Display a connection dialog to the user and connect to the database using the resulting connection dictionary. */
	protected void requestUserConnection() {
		final ConnectionDictionary dictionary = ScoreDataModel.newConnectionDictionary();
		ConnectionDialog dialog = ConnectionDialog.getInstance( this, dictionary );
		Connection connection = dialog.showConnectionDialog( DatabaseAdaptor.getInstance() );
		if ( connection != null ) {
			_model.setDatabaseConnection( connection, dialog.getConnectionDictionary() );
		}
		
		updateGroupMenu();
		FETCH_SNAPSHOTS_IN_RANGE_BUTTON.doClick();
	}
	
	
	/** populate the menu of available groups */
	protected void updateGroupMenu() {		
		_groupMenu.removeAllItems();
		String[] types = _model.getScoreTypes();
		
		if ( types == null ) {
			_model.setType(null);
			return;
		}
		
		for ( int index = 0 ; index < types.length ; index++ ) {
			_groupMenu.addItem(types[index]);
		}
		if ( types.length > 0 ) {
			_model.setType(types[0]);
		}
	}
	
	
	/** Handle window events.  When the window opens, request a connection. */
	protected void handleWindowEvents() {
		addWindowListener( new WindowAdapter() {
			public void windowOpened(WindowEvent event) {
				try {
					final ConnectionDictionary dictionary = ScoreDataModel.newConnectionDictionary();
					if ( dictionary != null ) {
						_model.connect();
						updateGroupMenu();
						FETCH_SNAPSHOTS_IN_RANGE_BUTTON.doClick();
					}
					else {  
						requestUserConnection();
					}
				}
				catch(Exception exception) {
					requestUserConnection();
				}
			}
		});
	}
		
}
