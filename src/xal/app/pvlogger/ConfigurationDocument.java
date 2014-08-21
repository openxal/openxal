//
//  ConfigurationDocument.java
//  xal
//
//  Created by Tom Pelaia on 8/28/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.app.pvlogger;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.sql.*;

import xal.extension.application.*;
import xal.extension.application.smf.AcceleratorDocument;
import xal.smf.*;
import xal.ca.Channel;
import xal.extension.bricks.WindowReference;
import xal.service.pvlogger.*;
import xal.tools.data.KeyValueSorting;
import xal.tools.data.KeyValueRecordListener;
import xal.tools.database.*;
import xal.extension.widgets.swing.*;
import xal.extension.widgets.smf.NodeChannelSelector;


/** document for managing the PV Logger configuration */
public class ConfigurationDocument extends AcceleratorDocument {
	/** signals pending addition to the database for the currently selected group */
	final private List<String> PENDING_GROUP_SIGNALS;
	
	/** list of available signals */
	final private List<String> AVAILABLE_SIGNALS;
	
	/** PV Logger */
	final private LoggerConfiguration LOGGER_CONFIGURATION;
	
	/** table of channel groups */
	private JTable GROUP_TABLE;
	
	/** table model for the channel groups */
	private KeyValueTableModel<ChannelGroupRecord> GROUP_TABLE_MODEL;
	
	/** table model for the channels */
	private KeyValueFilteredTableModel<String> CHANNEL_TABLE_MODEL;
	
	/** selector for channels */
	private KeyValueRecordSelector<String> _channelSelector;
	
	/** indicates whether the channel selector needs to be updated before display */
	private boolean _channelSelectorNeedsUpdate;
	
	/** button to revert channel additions */
	private JButton REVERT_ADDITIONS_BUTTON;
	
	/** button to publish channel additions */
	private JButton PUBLISH_ADDITIONS_BUTTON;
	
	/** button to revert group edits */
	private JButton REVERT_GROUP_EDITS_BUTTON;
	
	/** button to publish group edits */
	private JButton PUBLISH_GROUP_EDITS_BUTTON;
	
	/** list of group records which have been modified */
	final private Set<ChannelGroupRecord> MODIFIED_GROUP_RECORDS;
	
	
	/** Constructor */
	public ConfigurationDocument() {
		_channelSelectorNeedsUpdate = true;
		PENDING_GROUP_SIGNALS = new ArrayList<String>();
		AVAILABLE_SIGNALS = new ArrayList<String>();
		LOGGER_CONFIGURATION = new LoggerConfiguration( null );
		MODIFIED_GROUP_RECORDS = new HashSet<ChannelGroupRecord>();
	}
	
    
    /** Make a main window by instantiating the my custom window. */
    public void makeMainWindow() {		
		final WindowReference windowReference = getDefaultWindowReference( "ConfigurationWindow", this );
        mainWindow = (XalWindow)windowReference.getWindow();
		
		GROUP_TABLE = (JTable)windowReference.getView( "Group Table" );
		GROUP_TABLE_MODEL = new KeyValueTableModel<ChannelGroupRecord>( new ArrayList<ChannelGroupRecord>(), "label", "serviceID", "defaultLoggingPeriod", "retention", "description" );
		GROUP_TABLE_MODEL.setColumnName( "defaultLoggingPeriod", "Default Logging Period" );
		GROUP_TABLE_MODEL.setColumnClass( "defaultLoggingPeriod", Double.class );
		GROUP_TABLE_MODEL.setColumnClass( "retention", Double.class );
		GROUP_TABLE_MODEL.setColumnEditable( "serviceID", true );
		GROUP_TABLE_MODEL.setColumnEditable( "defaultLoggingPeriod", true );
		GROUP_TABLE_MODEL.setColumnEditable( "retention", true );
		GROUP_TABLE_MODEL.setColumnEditable( "description", true );
		GROUP_TABLE_MODEL.addKeyValueRecordListener( new GroupEditHandler() );
		
		GROUP_TABLE.setModel( GROUP_TABLE_MODEL );
		GROUP_TABLE.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		GROUP_TABLE.getSelectionModel().addListSelectionListener( new GroupSelectionHandler() );
		
		final JTable channelTable = (JTable)windowReference.getView( "Signal Table" );
		CHANNEL_TABLE_MODEL = new KeyValueFilteredTableModel<String>( new ArrayList<String>(), "toString" );
		CHANNEL_TABLE_MODEL.setMatchingKeyPaths( "toString" );
		CHANNEL_TABLE_MODEL.setColumnName( "toString", "Channel" );
		channelTable.setModel( CHANNEL_TABLE_MODEL );
		final JTextField channelFilterField = (JTextField)windowReference.getView( "ChannelFilterField" );
		channelFilterField.putClientProperty( "JTextField.variant", "search" );
		channelFilterField.putClientProperty( "JTextField.Search.Prompt", "Filter channels" );
		CHANNEL_TABLE_MODEL.setInputFilterComponent( channelFilterField );
		
		final JButton channelFilterClearButton = (JButton)windowReference.getView( "ChannelFilterClearButton" );
		channelFilterClearButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				channelFilterField.setText( null );
			}
		});
		
		final JButton addChannelsButton = (JButton)windowReference.getView( "AddChannelsButton" );
		addChannelsButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				handleChannelSelection();
			}
		});
		
		REVERT_ADDITIONS_BUTTON = (JButton)windowReference.getView( "RevertAdditionsButton" );
		REVERT_ADDITIONS_BUTTON.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				clearPendingSignals();
			}
		});
		
		PUBLISH_ADDITIONS_BUTTON = (JButton)windowReference.getView( "PublishAdditionsButton" );
		PUBLISH_ADDITIONS_BUTTON.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				publishSignals();
			}
		});
		
		REVERT_GROUP_EDITS_BUTTON = (JButton)windowReference.getView( "RevertGroupEditsButton" );
		REVERT_GROUP_EDITS_BUTTON.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				clearGroupEdits();
			}
		});
		
		
		PUBLISH_GROUP_EDITS_BUTTON = (JButton)windowReference.getView( "PublishGroupEditsButton" );
		PUBLISH_GROUP_EDITS_BUTTON.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				publishGroupEdits();
			}
		});
    }
	
	
	/** show this document */
	public void showDocument() {
		super.showDocument();
		
		updateControls();
		if ( requestConnectionIfNeeded() ) {
			fetchChannelGroups();
		}
		else {
			closeDocument();
		}
	}
	
	
	/** handle the event in which the accelerator is changed  */
	public void acceleratorChanged() {
		if ( accelerator != null && selectedSequence == null ) {
			_channelSelectorNeedsUpdate = true;
		}
	}
	
	
	/** handle the event in which the sequence is changed */
	public void selectedSequenceChanged() {
		if ( accelerator != null && selectedSequence != null ) {
			_channelSelectorNeedsUpdate = true;
		}
	}
	
	
	/** request a database connection if needed */
	private boolean requestConnectionIfNeeded() {
		final Connection originalConnection = LOGGER_CONFIGURATION.getConnection();
		if ( originalConnection != null ) {
			try {
				if( originalConnection.isClosed() ) {
					return requestConnection();
				}
				else {
					return true;
				}
			}
			catch ( Exception exception ) {
				return requestConnection();
			}
		}
		else {
			return requestConnection();
		}		
	}
	
	
	/** request a database connection */
	private boolean requestConnection() {
		final ConnectionDictionary dictionary = ConnectionDictionary.getPreferredInstance( "personal" );
		final ConnectionDialog dialog = ConnectionDialog.getInstance( mainWindow, dictionary );
		final Connection connection = dialog.showConnectionDialog( dictionary.getDatabaseAdaptor() );
		LOGGER_CONFIGURATION.setConnection( connection );
			
		if ( connection != null ) {
			try {
				return !connection.isClosed();
			}
			catch ( Exception exception ) {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	
	/** update available signals */
	private void updateAvailableSignalsIfNeeded() {
		if ( _channelSelectorNeedsUpdate ) {
			final AcceleratorSeq sequence = accelerator != null ? selectedSequence != null ? selectedSequence : accelerator : null;
			AVAILABLE_SIGNALS.clear();
			if ( sequence != null ) {
				final List<AcceleratorNode> nodes = sequence.getAllNodes( true );
				final Set<String> signals = new HashSet<String>();
				for ( final AcceleratorNode node : nodes ) {
					final List<String> handles = new ArrayList<String>( node.getHandles() );
					for ( final String handle : handles ) {
						final Channel channel = node.findChannel( handle );
						if ( channel != null ) {
							signals.add( channel.channelName() );
						}
					}
				}
				AVAILABLE_SIGNALS.addAll( signals );
				AVAILABLE_SIGNALS.removeAll( PENDING_GROUP_SIGNALS );
				Collections.sort( AVAILABLE_SIGNALS );
				buildChannelSelector();
			}
			else {
				_channelSelector = null;
			}
			_channelSelectorNeedsUpdate = false;
		}
	}
	
	
	/** construct a channel selector from the specified sequence */
	private void buildChannelSelector() {
		final ChannelGroup group = getSelectedChannelGroup();
		if ( group != null && !AVAILABLE_SIGNALS.isEmpty() ) {
			final List<String> signals = new ArrayList<String>( AVAILABLE_SIGNALS );
			final Collection<Channel> groupChannels = group.getChannels();
			for ( final Channel channel : groupChannels ) {
				signals.remove( channel.channelName() );
			}
			_channelSelector = KeyValueRecordSelector.getInstanceWithFilterPrompt( signals, mainWindow, "Add Selected Channels", "Channel Filter", "toString" );
			_channelSelector.getRecordTableModel().setColumnName( "toString", "Channel" );
		}
		else {
			_channelSelector = null;
		}
	}
	
	
	/** remove any pending signals */
	private void clearPendingSignals() {
		PENDING_GROUP_SIGNALS.clear();
		_channelSelectorNeedsUpdate = true;
		updateGroupChannels();
		updateControls();
	}
	
	
	/** publish the pending signals */
	private void publishSignals() {
		final ChannelGroup group = getSelectedChannelGroup();
		if ( group != null ) {
			try {
				requestConnectionIfNeeded();
				LOGGER_CONFIGURATION.publishChannelsToGroup( PENDING_GROUP_SIGNALS, group.getLabel() );
				fetchChannelGroups();
				clearPendingSignals();
				_channelSelectorNeedsUpdate = true;
			}
			catch ( Exception exception ) {
				exception.printStackTrace();
				displayError( "Publish Error", "Error publishing new signals.", exception );
			}
		}
	}
	
	
	/** clear the pending channel group edits */
	private void clearGroupEdits() {
		// revert the records to the group settings
		for ( final ChannelGroupRecord record : MODIFIED_GROUP_RECORDS ) {
			record.revert();
		}
		MODIFIED_GROUP_RECORDS.clear();
		updateControls();
		
		// refresh the group table without affecting the current selection
		final int recordCount = GROUP_TABLE_MODEL.getRowCount();
		if ( recordCount > 0 ) {
			GROUP_TABLE_MODEL.fireTableRowsUpdated( 0, recordCount - 1 );
		}
	}
	
	
	/** publish the pending group edits */
	private void publishGroupEdits() {
		if ( MODIFIED_GROUP_RECORDS.size() > 0 ) {
			try {
				requestConnectionIfNeeded();
				LOGGER_CONFIGURATION.publishGroupEdits( MODIFIED_GROUP_RECORDS );
				fetchChannelGroups();
				updateControls();
			}
			catch ( Exception exception ) {
				exception.printStackTrace();
				displayError( "Publish Error", "Error publishing group edits.", exception );
			}
		}
	}
	
	
	/** update the group channels to include pending signals */
	private void updateGroupChannels() {
		final ChannelGroup group = getSelectedChannelGroup();
		if ( group != null ) {
			final List<String> channelNames = new ArrayList<String>();
			for ( final Channel channel : group.getChannels() ) {
				channelNames.add( channel.channelName() );
			}
			channelNames.addAll( PENDING_GROUP_SIGNALS );
			Collections.sort( channelNames );
			
			CHANNEL_TABLE_MODEL.setRecords( channelNames );
		}
		else {
			CHANNEL_TABLE_MODEL.setRecords( new ArrayList<String>() );
		}
	}
	
	
	/** update controls */
	private void updateControls() {
		final boolean hasAdditions = PENDING_GROUP_SIGNALS.size() > 0;
		REVERT_ADDITIONS_BUTTON.setEnabled( hasAdditions );
		PUBLISH_ADDITIONS_BUTTON.setEnabled( hasAdditions );
		
		final boolean hasGroupEdits = MODIFIED_GROUP_RECORDS.size() > 0;
		REVERT_GROUP_EDITS_BUTTON.setEnabled( hasGroupEdits );
		PUBLISH_GROUP_EDITS_BUTTON.setEnabled( hasGroupEdits );
	}
	
	
	/** Get the selected channel group */
	private ChannelGroup getSelectedChannelGroup() {
		final int selectedRow = GROUP_TABLE.getSelectedRow();
		return selectedRow >= 0 ? GROUP_TABLE_MODEL.getRecordAtRow( selectedRow ).getGroup() : null;
	}
	
	
	/** fetch the channel groups from the database */
	private void fetchChannelGroups() {
		try {
			MODIFIED_GROUP_RECORDS.clear();
			requestConnectionIfNeeded();
			final List<ChannelGroup> groups = LOGGER_CONFIGURATION.fetchChannelGroups();
			GROUP_TABLE_MODEL.setRecords( ChannelGroupRecord.toRecords( groups ) );
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
			displayError( "Fetch Error", "Error fetching channel groups.", exception );
		}
	}
	
    
    /**
	 * Get a custom menu definition for this document
     * @return The menu definition properties file
     */
    public String getCustomMenuDefinitionResource() {
		return "configuration-menu.properties";
    }

	
	/** implement save command to do nothing */
	public void saveDocumentAs( final java.net.URL theURL ) {}
	
	
	/** handle channel selection */
	public void handleChannelSelection() {
		final int selectedRow = GROUP_TABLE.getSelectedRow();
		final ChannelGroup group = getSelectedChannelGroup();
		if ( group != null ) {
			updateAvailableSignalsIfNeeded();
			if ( _channelSelector != null ) {
				final List<String> channelNames = _channelSelector.showDialog();
				System.out.println( "Channels: " + channelNames );
				if ( channelNames != null ) {
					PENDING_GROUP_SIGNALS.addAll( channelNames );
					Collections.sort( PENDING_GROUP_SIGNALS );
					_channelSelectorNeedsUpdate = true;
					updateGroupChannels();
					updateControls();
				}
			}
			else {
				displayError( "Channel Selection Error", "You must first select an accelerator before you can select channels." );
			}
		}
		else {
			displayError( "Channel Selection Error", "You must first select a channel group before you can select the channels to add to it." );
		}
	}
	
	
	
	/** Handler of group selection events */
	private class GroupSelectionHandler implements ListSelectionListener {
		public void valueChanged( final ListSelectionEvent event ) {
			if ( !event.getValueIsAdjusting() ) {
				PENDING_GROUP_SIGNALS.clear();	// remove any pending signals since they would be associated with the previously selected group
				updateGroupChannels();				
				_channelSelectorNeedsUpdate = true;;
			}
		}
	}
	
	
	
	/** handle chanel group edits */
	private class GroupEditHandler implements KeyValueRecordListener<KeyValueTableModel<ChannelGroupRecord>,ChannelGroupRecord> {
		public void recordModified( final KeyValueTableModel<ChannelGroupRecord> tableModel, final ChannelGroupRecord record, final String keyPath, final Object value ) {
			MODIFIED_GROUP_RECORDS.add( record );
			updateControls();
		}
	}
	
	
	/**
	 * Test whether a connection is good
	 * @param connection the connection to test
	 * @return true if the connection is good and false if not
	 */
	static private boolean testConnection( final Connection connection ) {
		try {
			return !connection.isClosed();
		}
		catch( SQLException exception ) {
			return false;
		}
	}
}

