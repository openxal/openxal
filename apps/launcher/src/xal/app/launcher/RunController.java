//
//  AppController.java
//  xal
//
//  Created by Tom Pelaia on 5/5/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import xal.tools.bricks.WindowReference;
import xal.tools.swing.KeyValueFilteredTableModel;

import java.awt.Point;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;


/** Controller for synchronizing the application view to the model */
public class RunController {
	/** The main model of this document */
	final private LaunchModel MODEL;
	
	/** file watcher */
	final private FileWatcher FILE_WATCHER;
	
	/** table of applications that can be run */
	final private JTable APP_TABLE;
	
	/** table model for displaying the applications */
	final private KeyValueFilteredTableModel<App> APP_TABLE_MODEL;
	
	/** filter field */
	final private JTextField FILTER_FIELD;
	
	
	/** Constructor */
	public RunController( final LaunchModel model, final WindowReference windowReference ) {
		MODEL = model;
		FILE_WATCHER = model.getFileWatcher();
		
		final JButton appFilterClearButton = (JButton)windowReference.getView( "AppFilterClearButton" );
		appFilterClearButton.addActionListener( clearFilterAction() );
		
		final JButton refreshAppsButton = (JButton)windowReference.getView( "AppsRefreshButton" );
		refreshAppsButton.addActionListener( refreshAppsAction() );
		
		final JButton appLaunchButton = (JButton)windowReference.getView( "AppLaunchButton" );
		appLaunchButton.addActionListener( runAction() );
		
		FILTER_FIELD = (JTextField)windowReference.getView( "AppFilterField" );
		FILTER_FIELD.putClientProperty( "JTextField.variant", "search" );
		FILTER_FIELD.putClientProperty( "JTextField.Search.Prompt", "Application Filter" );

		APP_TABLE = (JTable)windowReference.getView( "AppTable" );
		APP_TABLE.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
		APP_TABLE.setAutoCreateRowSorter( true );
		APP_TABLE.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		APP_TABLE_MODEL = new KeyValueFilteredTableModel<App>( new ArrayList<App>(), "label", "rule.kind", "notes" );
		APP_TABLE_MODEL.setMatchingKeyPaths( "label", "rule.kind", "file.name", "notes" );
		APP_TABLE_MODEL.setColumnName( "rule.kind", "Kind" );
		APP_TABLE_MODEL.setInputFilterComponent( FILTER_FIELD );
		APP_TABLE.setModel( APP_TABLE_MODEL );
		APP_TABLE.addMouseListener( appTableClickHandler() );
		APP_TABLE.addMouseMotionListener( appTableMouseMonitor() );
		
		APP_TABLE.getColumnModel().getColumn( 2 ).setPreferredWidth( 300 );		// allot space for the notes
		
		refreshView();
	}
	
	
	/** run the selected application */
	private void runSelectedApplication() {
		final int selectedRow = APP_TABLE.getSelectedRow();
		if ( selectedRow >= 0 ) {
			final int modelRow = APP_TABLE.convertRowIndexToModel( selectedRow );
			final App application = APP_TABLE_MODEL.getRecordAtRow( modelRow );
			System.out.println( "Run: " + application.getPath() );
			try {
				MODEL.launchApplication( application );
			}
			catch( Exception exception ) {
				exception.printStackTrace();
				JOptionPane.showMessageDialog( APP_TABLE, exception.getMessage(), "Error Running Application", JOptionPane.WARNING_MESSAGE );
			}
		}		
	}
	
	
	/** refresh the view with the model data */
	private void refreshView() {
		final List<App> applications = MODEL.getApplications();
		APP_TABLE_MODEL.setRecords( applications );
	}
	
	
	/** action to refresh the list of applications */
	private AbstractAction refreshAppsAction() {
		return new AbstractAction() {
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				MODEL.refreshApplications();
				refreshView();
			}
		};
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
	
	
	/** action to run the selected application */
	private AbstractAction runAction() {
		return new AbstractAction() {
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				runSelectedApplication();
			}
		};
	}
	
	
	/** handler of application table selection events */
	private MouseListener appTableClickHandler() {
		return new MouseAdapter() {
			public void mouseClicked( final MouseEvent event ) {
				switch ( event.getClickCount()  ) {
					case 2:
						runSelectedApplication();
						break;
					default:
						break;
				}
			}
		};
	}
	
	
	/** mouse motion listener to set the tool tip on the application under the mouse */
	private MouseMotionListener appTableMouseMonitor() {
		return new MouseMotionAdapter() {
			public void mouseMoved( final MouseEvent event ) {
				final Point eventPoint = event.getPoint();
				final int row = APP_TABLE.rowAtPoint( eventPoint );
				final int column = APP_TABLE.columnAtPoint( eventPoint );
				if ( row >= 0 ) {
					final Object cellValue = APP_TABLE_MODEL.getValueAt( row, column );
					final String tip = cellValue != null ? cellValue.toString() : null;
					APP_TABLE.setToolTipText( tip != null && tip.length() > 20 ? tip : null );
				}
				else {
					APP_TABLE.setToolTipText( null );
				}
			}
		};
	}
}
