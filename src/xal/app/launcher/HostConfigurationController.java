//
//  HostConfigurationController.java
//  xal
//
//  Created by Thomas Pelaia on 9/11/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import xal.extension.application.*;
import xal.tools.bricks.WindowReference;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;


/** controller of the host configuration */
public class HostConfigurationController {
	/** host configuration dialog */
	final private WindowReference WINDOW_REFERENCE;
	
	/** table model for setting host settings */
	final private HostSettingsTableModel HOST_SETTINGS_TABLE_MODEL;
	
	/** main model */
	final private LaunchModel MODEL;
	
	
	/** Constructor */
	public HostConfigurationController( final LaunchModel model, final WindowReference windowReference ) {
		WINDOW_REFERENCE = windowReference;
		
		MODEL = model;
		
		HOST_SETTINGS_TABLE_MODEL = new HostSettingsTableModel( model );
		final JTable hostTable = (JTable)windowReference.getView( "HostTable" );
		hostTable.setModel( HOST_SETTINGS_TABLE_MODEL );
		hostTable.getColumnModel().getColumn( HostSettingsTableModel.ENABLE_COLUMN ).setMaxWidth( 75 );
		
		final String commandTemplate = model.getLauncher().getHostConfiguration().getCommandTemplate();
		final JTextField hostCommandField = (JTextField)windowReference.getView( "HostCommandField" );
		hostCommandField.setText( commandTemplate );
		
		setupControls( model, windowReference );
	}
	
	
	/** setup the dialog's controls */
	private void setupControls( final LaunchModel model, final WindowReference windowReference ) {
		final HostConfiguration hostConfiguration = model.getLauncher().getHostConfiguration();

		final JTable hostTable = (JTable)windowReference.getView( "HostTable" );
		
		final JTextField hostCommandField = (JTextField)windowReference.getView( "HostCommandField" );
		hostCommandField.addActionListener( commandFieldAction( hostCommandField ) );
		hostCommandField.addFocusListener( commandFieldFocusHandler( hostCommandField ) );

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
	
	
	/** action for handling command field events */
	private ActionListener commandFieldAction( final JTextField textField  ) {
		return new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				handleHostCommandChange( textField );
			}
		};
	}
	
	
	/** action for handling command field events */
	private FocusListener commandFieldFocusHandler( final JTextField textField ) {
		return new FocusListener() {
			private String _originalText = null;
			
			public void focusGained( final FocusEvent event ) {
				_originalText = textField.getText();
			}
			
			public void focusLost( final FocusEvent event ) {
				final String text = textField.getText();
				if ( text != null && _originalText != null ) {
					if ( !text.equals( _originalText ) ) {
						handleHostCommandChange( textField );
					}
				}
				else if ( text != _originalText ) {
					handleHostCommandChange( textField );
				}
				_originalText = null;
			}
		};
	}
	
	
	/** handle the host command change */
	private void handleHostCommandChange( final JTextField textField ) {
		MODEL.getLauncher().getHostConfiguration().setCommandTemplate( textField.getText() );
		MODEL.postModifications();
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



