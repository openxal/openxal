/*
 * PVLogSnapshotChooser.java
 *
 * Created on February 2, 2005, 4:44 PM
 *
 * Copyright (c) 2001-2005 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 */

package xal.service.pvlogger.apputils.browser;

import xal.service.pvlogger.*;
import xal.tools.database.*;
import xal.extension.widgets.swing.KeyValueTableModel;

import java.sql.*;
import java.util.Date;
import java.util.Collection;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.event.*;
import java.awt.*;
import javax.swing.event.*;

/**
 * This class provides a UI component (a JDialog) for selecting PV Logger ID.  It is modified from the 
 * pvlogbrowser application.
 *
 * @author  Paul Chu
 */
public class PVLogSnapshotChooser {
    
    JDialog pvLogDialog;
        
    /** browser model */
    protected BrowserModel _model = new BrowserModel();
    
    /** controller of the selection state */
    protected BrowserController _controller;
    
    private JTextField pvLogIdField = new JTextField(8);
    
    private long pvLogId = 0;
    
    private String groupName = "default";
	
	
    /** Creates a new instance of PVLogSnapshotChooser */
    public PVLogSnapshotChooser() {
        this( null );
    }
    
	
	/** Constructor */
    public PVLogSnapshotChooser( final Frame owner ) {
		this( owner, false );
    }
    
	
	/** Primary constructor */
    public PVLogSnapshotChooser( final Frame owner, final boolean modal ) {
        if ( owner != null ) {
            pvLogDialog = new JDialog( owner );
            pvLogDialog.setLocationRelativeTo( owner );
		}
		else {
            pvLogDialog = new JDialog();
		}
		pvLogDialog.setTitle( "PV Logger Snapshot Chooser" );
		pvLogDialog.setModal( modal );
        _controller = new BrowserController( _model );
    }
    
	
    public void setGroup(String name) {
    	groupName = name;
    }
    
    public JDialog choosePVLogId() {
        requestUserConnection();
        
		JSplitPane mainView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildSnapshotListView(), buildSnapshotDetailView());
		
		pvLogDialog.setLayout(new BorderLayout());
		pvLogDialog.add(buildQueryView(), BorderLayout.NORTH);
		pvLogDialog.add(mainView, BorderLayout.CENTER);
		pvLogDialog.add(buildResultView(), BorderLayout.SOUTH);
		
		pvLogDialog.pack();
		
        pvLogDialog.setVisible( true );
		
        return pvLogDialog;
    }
    
    protected void requestUserConnection() {    
        // initialize PVLogger
        ConnectionDictionary dict = PVLogger.newBrowsingConnectionDictionary();
        
        Connection conn = dict.getDatabaseAdaptor().getConnection(dict);
        _model.setDatabaseConnection(conn, dict);
        _model.connect();
    }

    /**
     * Build the view for querying the database for the machine snapshots.
     * @return the query view
     */
    protected Container buildQueryView() {
        Box queryView = new Box(BoxLayout.X_AXIS);
        queryView.setBorder( BorderFactory.createEtchedBorder() );
        final int BUTTON_GAP = 20;

        queryView.add( Box.createHorizontalStrut(BUTTON_GAP) );
        queryView.add( new JLabel("From:") );
        final SpinnerDateModel fromDateModel = new SpinnerDateModel();
        JSpinner fromSpinner = new JSpinner(fromDateModel);
        fromSpinner.setEditor( new JSpinner.DateEditor(fromSpinner, "MMM dd, yyyy HH:mm:ss") );
        fromSpinner.setMaximumSize( new Dimension(200, 25) );
        queryView.add(fromSpinner);
        
		try {
			_model.selectGroup(groupName);
		}
		catch( Exception exception ) {
			throw new RuntimeException( exception );
		}

        queryView.add( Box.createHorizontalStrut(10) );
        queryView.add( new JLabel("To:") );
        final SpinnerDateModel toDateModel = new SpinnerDateModel();
        JSpinner toSpinner = new JSpinner(toDateModel);
        toSpinner.setEditor( new JSpinner.DateEditor(toSpinner, "MMM dd, yyyy HH:mm:ss") );
        toSpinner.setMaximumSize( new Dimension(200, 25) );
        queryView.add(toSpinner);

        queryView.add( Box.createHorizontalStrut(BUTTON_GAP) );
        JButton fetchButton = new JButton("Fetch");
        queryView.add(fetchButton);
        fetchButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					try {
                        Date startDate = fromDateModel.getDate();
                        Date endDate = toDateModel.getDate();
                        _model.fetchMachineSnapshots(startDate, endDate);
					}
					catch( Exception exception )  {
						throw new RuntimeException( exception );
					}
                }
        });		

        queryView.add( Box.createHorizontalGlue() );

        return queryView;
    }
	
    /**
     * Build the view that displays the list of fetched machine snapshots
     * @return the snapshot list view
     */
    protected Container buildSnapshotListView() {
        Box listView = new Box(BoxLayout.Y_AXIS);

        listView.add( new JLabel("Machine Snapshots:") );
        Box tableView = new Box(BoxLayout.Y_AXIS);
        listView.add(tableView);
		final KeyValueTableModel<MachineSnapshot> machineSnapshotTableModel = _controller.getMachineSnapshotTableModel();
        final JTable snapshotTable = new JTable( machineSnapshotTableModel );
        snapshotTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableView.add( snapshotTable.getTableHeader() );
        tableView.add( new JScrollPane(snapshotTable) );

        snapshotTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				if ( !event.getValueIsAdjusting() ) {
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

        return listView;
    }

	/**
	 * Build the snapshot detail view which displays detailed information about the snapshot including
	 * the comment and the list of channel snapshots associated with the selected signals.
	 * @return the snapshot detail view
	 */
    protected Container buildSnapshotDetailView() {				
        Box detailView = new Box(BoxLayout.Y_AXIS);
        JLabel titleLabel = new JLabel("Selected Snapshot:");
        detailView.add(titleLabel);
        detailView.add( new JLabel("Comment:") );
        final JTextArea commentTextView = new JTextArea();
        commentTextView.setEditable(false);

        Box tableBox = new Box(BoxLayout.Y_AXIS);		
		final KeyValueTableModel<ChannelSnapshot> detailTableModel = _controller.getChannelSnapshotTableModel();
        final JTable dataTable = new JTable( detailTableModel );
        tableBox.add( dataTable.getTableHeader() );
        tableBox.add( new JScrollPane( dataTable ) );

        _controller.addBrowserControllerListener( new BrowserControllerListener() {
                /** 
                 * event indicating that a snapshot has been selected
                 * @param controller The controller managing selection state
                 * @param snapshot The snapshot that has been selected
                 */
                public void snapshotSelected(BrowserController controller, MachineSnapshot snapshot) {
                        if ( snapshot != null ) {
                                commentTextView.setText( snapshot.getComment() );
                                pvLogIdField.setText((new Long(snapshot.getId())).toString());
                        }
                        else {
                                commentTextView.setText("");				
                        }
                }


                /**
                 * event indicating that the selected channel group changed
                 * @param source the browser controller sending this notice
                 * @param newGroup the newly selected channel group
                 */
                public void selectedChannelGroupChanged(BrowserController source, ChannelGroup newGroup) {}


                /**
                 * Event indicating that the selected signals have changed
                 * @param source the controller sending the event
                 * @param selectedSignals the new collection of selected signals
                 */
                public void selectedSignalsChanged(BrowserController source, Collection<String> selectedSignals) {}
        });
        JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, commentTextView, tableBox);

        detailView.add(mainPane);

        return detailView;
    }
    
    protected Container buildResultView() {
        Box result = new Box(BoxLayout.X_AXIS);
        JLabel titleLabel = new JLabel("Selected Snapshot:");
        result.add(titleLabel);
        pvLogIdField.setMaximumSize(new Dimension(100, 25));
        result.add(pvLogIdField);
        
        result.add( Box.createHorizontalStrut(20) );
        JButton done = new JButton("Select");
        done.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                pvLogId = (new Long(pvLogIdField.getText())).longValue();
                pvLogDialog.setVisible(false);
                System.out.println("pvLogId = " + pvLogId);
                // for testing purpose
                // System.exit(0);
            }
        });
        result.add(done);
        
        return result;
    }
    
    public long getPVLogId() {
        return pvLogId;
    }
	        
// for testing purpose
//    public static void main(String[] args) {
//        PVLogSnapshotChooser psc = new PVLogSnapshotChooser();
//        psc.choosePVLogId();
//    }
}
