//
// MonitorController.java
// Open XAL
//
// Created by Pelaia II, Tom on 8/29/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import xal.extension.application.Application;
import xal.extension.bricks.*;
import xal.extension.widgets.swing.KeyValueFilteredTableModel;
import xal.tools.dispatch.*;
import xal.tools.UpdateListener;


/** MonitorController */
public class MonitorController implements MonitorModelListener, UpdateListener {
	/** The main model of this document */
	final private LaunchModel LAUNCH_MODEL;

	/** model for monitoring remote applications */
	final private MonitorModel MONITOR_MODEL;

	/** table of live applications to monitor */
	final private JTable REMOTE_APPS_TABLE;

	/** table model for displaying the applications */
	final private KeyValueFilteredTableModel<RemoteAppRecord> APP_TABLE_MODEL;

	/** filter field */
	final private JTextField FILTER_FIELD;

	/** timer for refreshing the data */
	final private DispatchTimer REFRESH_TIMER;


	/** Constructor */
	public MonitorController( final LaunchModel launchModel, final WindowReference windowReference ) {
		LAUNCH_MODEL = launchModel;

		MONITOR_MODEL = new MonitorModel();
		MONITOR_MODEL.addMonitorModelListener( this );

		final JButton liveAppsFilterClearButton = (JButton)windowReference.getView( "LiveAppsFilterClearButton" );
		liveAppsFilterClearButton.addActionListener( clearFilterAction() );
		
		final JButton quitAppButton = (JButton)windowReference.getView( "QuitAppButton" );
		quitAppButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				for ( final RemoteAppRecord record : getSelectedRemoteAppRecords() ) {
					try {
						record.quit( 0 );
					}
					catch ( Exception exception ) {
						exception.printStackTrace();
						Application.displayError( "Quit App Failed!", "Failed to quit selected apps due to an internal exception!", exception );
					}
				}
			}
		});

		final JButton forceQuitAppButton = (JButton)windowReference.getView( "ForceQuitAppButton" );
		forceQuitAppButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				for ( final RemoteAppRecord record : getSelectedRemoteAppRecords() ) {
					try {
						record.forceQuit( 0 );
					}
					catch ( Exception exception ) {
						exception.printStackTrace();
						Application.displayError( "Force Quit App Failed!", "Failed to force selected apps to quit due to an internal exception!", exception );
					}
				}
			}
		});

		final JButton revealAppButton = (JButton)windowReference.getView( "RevealAppButton" );
		revealAppButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				for ( final RemoteAppRecord record : getSelectedRemoteAppRecords() ) {
					try {
						record.showAllWindows();
					}
					catch ( Exception exception ) {
						exception.printStackTrace();
						Application.displayError( "Reveal App Failed!", "Failed to reveal selected apps due to an internal exception!", exception );
					}
				}
			}
		});

		FILTER_FIELD = (JTextField)windowReference.getView( "LiveAppsFilterField" );
		FILTER_FIELD.putClientProperty( "JTextField.variant", "search" );
		FILTER_FIELD.putClientProperty( "JTextField.Search.Prompt", "Application Filter" );

		REMOTE_APPS_TABLE = (JTable)windowReference.getView( "LiveAppsTable" );
		REMOTE_APPS_TABLE.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
		REMOTE_APPS_TABLE.setAutoCreateRowSorter( true );
		REMOTE_APPS_TABLE.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		
		APP_TABLE_MODEL = new KeyValueFilteredTableModel<RemoteAppRecord>( new ArrayList<RemoteAppRecord>(), "applicationName", "hostName", "totalMemory", "launchTime", "heartbeat" );
		APP_TABLE_MODEL.setMatchingKeyPaths( "applicationName", "hostName" );
		APP_TABLE_MODEL.setColumnName( "applicationName", "Application" );
		APP_TABLE_MODEL.setInputFilterComponent( FILTER_FIELD );
		REMOTE_APPS_TABLE.setModel( APP_TABLE_MODEL );

		REFRESH_TIMER = new DispatchTimer( DispatchQueue.getMainQueue(), new Runnable() {
			public void run() {
				for ( final RemoteAppRecord record : APP_TABLE_MODEL.getRowRecords() ) {
					record.refresh();
				}
			}
		});
		REFRESH_TIMER.startNowWithInterval( 5000, 0 );	// refresh records every 5 seconds

		final Box monitorView = (Box)windowReference.getView( "MonitorView" );
		final JTabbedPane mainTabPane = (JTabbedPane)windowReference.getView( "MainTabPane" );
		// monitor tab pane selection changes to monitor remote applications only when the monitor pane is selected
		mainTabPane.addChangeListener( new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				if ( mainTabPane.getSelectedComponent() == monitorView ) {
					REFRESH_TIMER.resume();
				}
				else {
					REFRESH_TIMER.suspend();
				}
			}
		});

		// suspend the refresh timer if the monitor is not selected to avoid unnecessary network activity
		if ( mainTabPane.getSelectedComponent() != monitorView ) {		// future proofed in case the monitor view ever becomes the first tab open at launch
			REFRESH_TIMER.suspend();
		}
	}


	/** called when the source posts an update to this observer */
	public void observedUpdate( final Object source ) {
		if ( source instanceof RemoteAppRecord ) {
			final RemoteAppRecord record = (RemoteAppRecord)source;
			DispatchQueue.getMainQueue().dispatchAsync( new Runnable() {
				public void run() {
					final List<RemoteAppRecord> records = APP_TABLE_MODEL.getRowRecords();
					final int row = records.indexOf( record );
					if ( row >= 0 ) {
						APP_TABLE_MODEL.fireTableRowsUpdated( row, row );
					}
				}
			});
		}
	}


	/** event indicating that the list of remote apps has changed */
	public void remoteAppsChanged( final MonitorModel model, final List<RemoteAppRecord> remoteApps ) {
		DispatchQueue.getMainQueue().dispatchAsync( new Runnable() {
			public void run() {
				APP_TABLE_MODEL.setRecords( remoteApps );
				for ( final RemoteAppRecord record : remoteApps ) {
					record.setUpdateListener( MonitorController.this );
				}
			}
		});
	}


	/** Get the remote app records selected by the user */
	private RemoteAppRecord[] getSelectedRemoteAppRecords() {
		final int[] selectedRows = REMOTE_APPS_TABLE.getSelectedRows();
		final RemoteAppRecord[] selectedRecords = new RemoteAppRecord[ selectedRows.length ];

		int recordIndex = 0;
		for ( final int row : selectedRows ) {
			final int modelRow = REMOTE_APPS_TABLE.convertRowIndexToModel( row );
			selectedRecords[ recordIndex++ ] = APP_TABLE_MODEL.getRecordAtRow( modelRow );
		}

		return selectedRecords;
	}
	

	/** action to clear the filter field */
	private AbstractAction clearFilterAction() {
		return new AbstractAction() {
            private static final long serialVersionUID = 1L;

			public void actionPerformed( final ActionEvent event ) {
				FILTER_FIELD.setText( "" );
			}
		};
	}
}
