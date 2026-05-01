//
//  HostConfigurationController.java
//  xal
//
//  Created by Thomas Pelaia on 9/11/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import xal.extension.application.*;
import xal.extension.bricks.WindowReference;
import xal.extension.widgets.swing.KeyValueTableModel;
import xal.tools.data.KeyValueRecordListener;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.*;


/** controller of the host configuration */
public class HostConfigurationController {
	/** host configuration dialog */
	final private WindowReference WINDOW_REFERENCE;
	
	/** table model for setting host settings */
	final private HostSettingsTableModel HOST_SETTINGS_TABLE_MODEL;
	
	/** main model */
	final private LaunchModel MODEL;

	/** host configuration */
	final private HostConfiguration HOST_CONFIGURATION;

	/** table displaying the list of commands for hosts */
	final private JTable HOST_COMMAND_TABLE;

	/** table model for displaying the list of commands for hosts */
	final private KeyValueTableModel<StringArgument> HOST_COMMAND_TABLE_MODEL;

	/** list of host commands */
	private List<StringArgument> _hostCommands;

	
	/** Constructor */
	public HostConfigurationController( final LaunchModel model, final WindowReference windowReference ) {
		WINDOW_REFERENCE = windowReference;
		
		MODEL = model;
		
		HOST_SETTINGS_TABLE_MODEL = new HostSettingsTableModel( model );
		final JTable hostTable = (JTable)windowReference.getView( "HostTable" );
		hostTable.setModel( HOST_SETTINGS_TABLE_MODEL );
		hostTable.getColumnModel().getColumn( HostSettingsTableModel.ENABLE_COLUMN ).setMaxWidth( 75 );

		HOST_CONFIGURATION = model.getLauncher().getHostConfiguration();

		HOST_COMMAND_TABLE = (JTable)windowReference.getView( "HostCommandTable" );

		HOST_COMMAND_TABLE_MODEL = new KeyValueTableModel<>( _hostCommands, "value" );
		HOST_COMMAND_TABLE_MODEL.setColumnName( "value", "Command" );
		HOST_COMMAND_TABLE_MODEL.setColumnEditable( "value", true );
		HOST_COMMAND_TABLE_MODEL.addKeyValueRecordListener( new KeyValueRecordListener<KeyValueTableModel<StringArgument>,StringArgument>() {
			public void recordModified( final KeyValueTableModel<StringArgument> tableModel, final StringArgument argument, final String keyPath, final Object value ) {
				pushHostCommandsToModel();
			}
		});
		HOST_COMMAND_TABLE.setModel( HOST_COMMAND_TABLE_MODEL );

		final JButton deleteHostCommandButton = (JButton)windowReference.getView( "DeleteHostCommandButton" );
		deleteHostCommandButton.addActionListener( new ActionListener() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed( final ActionEvent event ) {
				final int[] selectedRows = HOST_COMMAND_TABLE.getSelectedRows();
				final Set<StringArgument> argumentsToRemove = new HashSet<StringArgument>( selectedRows.length );
				for ( final int selectedRow : selectedRows ) {
					final int commandRow = HOST_COMMAND_TABLE.convertRowIndexToModel( selectedRow );
					argumentsToRemove.add( HOST_COMMAND_TABLE_MODEL.getRecordAtRow( commandRow ) );
				}
				_hostCommands.removeAll( argumentsToRemove );
				pushHostCommandsToModel();
				HOST_COMMAND_TABLE_MODEL.fireTableDataChanged();
			}
		});

		final JButton addHostCommandButton = (JButton)windowReference.getView( "AddHostCommandButton" );
		addHostCommandButton.addActionListener( new ActionListener() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed( final ActionEvent event ) {
				final int selectedRow = HOST_COMMAND_TABLE.getSelectedRow();
				if ( selectedRow >= 0 ) {
					final int commandRow = HOST_COMMAND_TABLE.convertRowIndexToModel( selectedRow );
					_hostCommands.add( commandRow, new StringArgument( "" ) );
					pushHostCommandsToModel();
					HOST_COMMAND_TABLE_MODEL.fireTableDataChanged();
				}
			}
		});
		
		setupControls( model, windowReference );

		refreshView();
	}


	/** push the host commands to the model */
	private void pushHostCommandsToModel() {
		final List<String> commands = StringArgument.toStrings( _hostCommands );
		HOST_CONFIGURATION.setCommands( commands );
		MODEL.postModifications();
	}


	/** refresh the host command view */
	private void refreshHostCommandsView() {
		_hostCommands = StringArgument.toArguments( HOST_CONFIGURATION.getCommands() );
		HOST_COMMAND_TABLE_MODEL.setRecords( _hostCommands );
	}


	/** refresh the view with the model data */
	private void refreshView() {
		refreshHostCommandsView();
	}

	
	/** setup the dialog's controls */
	private void setupControls( final LaunchModel model, final WindowReference windowReference ) {
		final HostConfiguration hostConfiguration = model.getLauncher().getHostConfiguration();

		final JTable hostTable = (JTable)windowReference.getView( "HostTable" );

		final JButton deleteButton = (JButton)windowReference.getView( "DeleteHostButton" );
		deleteButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final int[] selectedRows = hostTable.getSelectedRows();
				if ( selectedRows.length > 0 ) {
					HOST_SETTINGS_TABLE_MODEL.deleteRows( selectedRows );
					hostConfiguration.refreshEnabledHosts();
					model.postModifications();
				}
			}
		});
		
		final JButton addButton = (JButton)windowReference.getView( "AddHostButton" );
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				HOST_SETTINGS_TABLE_MODEL.addHostSetting();
				hostConfiguration.refreshEnabledHosts();
				model.postModifications();
			}
		});
	}
}



/** table model for editing the host settings */
class HostSettingsTableModel extends AbstractTableModel {
    /** required serialization version ID */
    private static final long serialVersionUID = 1L;
    
	/** colunm for enabling the host */
	static final public int ENABLE_COLUMN = 0;
	
	/** column for the host */
	static final public int HOST_COLUMN = ENABLE_COLUMN + 1;
	
	/** host settings */
	private List<HostSetting> _hostSettings;
	
	/** host configuration */
	final private HostConfiguration HOST_CONFIGURATION;
	
	/** model */
	final private LaunchModel MODEL;
	
	
	/** Constructor */
	public HostSettingsTableModel( final LaunchModel model ) {
		MODEL = model;
		HOST_CONFIGURATION = model.getLauncher().getHostConfiguration();
		setHostSettings( HOST_CONFIGURATION.getHostSettings() );
	}
	
	
	/** get the host settings */
	public List<HostSetting> getHostSettings() {
		return _hostSettings;
	}
	
	
	/** set the host settings */
	public void setHostSettings( final List<HostSetting> hostSettings ) {
		_hostSettings = hostSettings;		
		fireTableDataChanged();
	}
	
	
	/** add a new host setting */
	public void addHostSetting() {
		_hostSettings.add( new HostSetting() );
		fireTableDataChanged();
	}
	
	
	/** delete the host settings corresponding to the specified indices */
	public void deleteRows( final int[] rows ) {
		final List<HostSetting> settingsToDelete = new ArrayList<HostSetting>( rows.length );
		for ( int row : rows ) {
			settingsToDelete.add( _hostSettings.get( row ) );
		}
		_hostSettings.removeAll( settingsToDelete );
		fireTableDataChanged();
	}
	
	
	/** get the column count */
	public int getColumnCount() {
		return 2;
	}
	
	
	/** get the column name */
	public String getColumnName( final int column  ) {
		switch( column ) {
			case ENABLE_COLUMN:
				return "Enabled";
			case HOST_COLUMN:
				return "Host Address";
			default:
				return "";
		}
	}
	
	
	/** get the column class */
	public Class<?> getColumnClass( final int column  ) {
		switch( column ) {
			case ENABLE_COLUMN:
				return Boolean.class;
			case HOST_COLUMN:
				return String.class;
			default:
				return String.class;
		}
	}
	
	
	/** get the row count */
	public int getRowCount() {
		return _hostSettings.size();
	}
	
	
	/** determine if the specified cell is editable */
	public boolean isCellEditable( final int row, final int column ) {
		return true;
	}
	
	
	/** get the value for the specified cell */
	public Object getValueAt( final int row, final int column ) {
		final HostSetting setting = _hostSettings.get( row );
		if ( setting == null )  return null;
		
		switch( column ) {
			case ENABLE_COLUMN:
				return setting.isEnabled();
			case HOST_COLUMN:
				return setting.getHost();
			default:
				return null;
		}
	}
	
	
	/** set the value of the specified cell */
	public void setValueAt( final Object value, final int row, final int column ) {
		final HostSetting setting = _hostSettings.get( row );
		if ( setting == null )  return;
		
		switch( column ) {
			case ENABLE_COLUMN:
				setting.setEnabled( value != null ? ((Boolean)value).booleanValue() : false );
				HOST_CONFIGURATION.refreshEnabledHosts();
				break;
			case HOST_COLUMN:
				setting.setHost( value != null ? value.toString() : "" );
				break;
			default:
				break;
		}
		
		MODEL.postModifications();
	}
}



