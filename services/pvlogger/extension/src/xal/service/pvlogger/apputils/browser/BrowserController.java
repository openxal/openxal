/*
 * BrowserController.java
 *
 * Created on Thu Mar 25 09:00:11 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger.apputils.browser;

import xal.service.pvlogger.*;
import xal.tools.messaging.*;
import xal.tools.data.KeyValueRecordListener;
import xal.extension.widgets.swing.KeyValueTableModel;
import xal.extension.widgets.swing.KeyValueFilteredTableModel;

import java.util.*;


/**
 * BrowserController manages the selection state of the browser window.
 *
 * @author  tap
 */
public class BrowserController implements BrowserModelListener, KeyValueRecordListener<KeyValueFilteredTableModel<PVRecord>,PVRecord> {
	/** browser model */
	protected BrowserModel _model;
	
	/** selected Machine snapshot **/
	protected MachineSnapshot _selectedSnapshot;

	/** The message center for dispatching messages */
	private final MessageCenter MESSAGE_CENTER;
	
	/** Proxy for forwarding messages to registered listeners */
	private final BrowserControllerListener EVENT_PROXY;

	/** table model of PVs */
	private final KeyValueFilteredTableModel<PVRecord> PV_TABLE_MODEL;

	/** map of PV Records keyed by signal */
	private final Map<String,PVRecord> SIGNAL_RECORDS;

	/** table model for displaying the machine snapshots */
	private final KeyValueTableModel<MachineSnapshot> MACHINE_SNAPSHOT_TABLE_MODEL;

	/** table model for displaying the channel snapshots */
	private final KeyValueTableModel<ChannelSnapshot> CHANNEL_SNAPSHOT_TABLE_MODEL;

	
	/**
	 * Constructor
	 */
	public BrowserController( final BrowserModel model ) {
		_model = model;
		model.addBrowserModelListener(this);

		PV_TABLE_MODEL = new KeyValueFilteredTableModel<PVRecord>();
		PV_TABLE_MODEL.setKeyPaths( "enabled", "signal" );
		PV_TABLE_MODEL.setMatchingKeyPaths( "signal" );
		PV_TABLE_MODEL.setColumnName( "enabled", "Use" );
		PV_TABLE_MODEL.setColumnEditable( "enabled", true );
		PV_TABLE_MODEL.setColumnClass( "enabled", Boolean.class );
		PV_TABLE_MODEL.addKeyValueRecordListener( this );

		MACHINE_SNAPSHOT_TABLE_MODEL = new KeyValueTableModel<MachineSnapshot>();
		MACHINE_SNAPSHOT_TABLE_MODEL.setKeyPaths( "id", "timestamp" );

		CHANNEL_SNAPSHOT_TABLE_MODEL = new KeyValueTableModel<ChannelSnapshot>();
		CHANNEL_SNAPSHOT_TABLE_MODEL.setKeyPaths( "PV", "timestamp", "valueCount", "scalarValue", "status", "severity" );
		
		SIGNAL_RECORDS = new Hashtable<String,PVRecord>();

		MESSAGE_CENTER = new MessageCenter("Browser Controller");
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, BrowserControllerListener.class );
		
		updatePVTableModel();
	}
	
	
	/**
	 * Add a listener of controller events from this controller
	 * @param listener the listener to add
	 */
	public void addBrowserControllerListener( final BrowserControllerListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, BrowserControllerListener.class );
	}
	
	
	/**
	 * Remove the listener from receiving controller events from this controller
	 * @param listener the listener to remove
	 */
	public void removeBrowserControllerListener( final BrowserControllerListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, BrowserControllerListener.class );
	}


	/** get the table model of PVs */
	public KeyValueFilteredTableModel<PVRecord> getPVTableModel() {
		return PV_TABLE_MODEL;
	}


	/** get the table model of machine snapshots */
	public KeyValueTableModel<MachineSnapshot> getMachineSnapshotTableModel() {
		return MACHINE_SNAPSHOT_TABLE_MODEL;
	}


	/** get the table model of channel snapshots */
	public KeyValueTableModel<ChannelSnapshot> getChannelSnapshotTableModel() {
		return CHANNEL_SNAPSHOT_TABLE_MODEL;
	}
	
	
	/**
	 * Convert the array of channel wrappers to an array of signals.
	 * @param wrappers the array of channel wrappers
	 * @return the corresponding array of signals
	 */
	static protected String[] convertToPVs( final ChannelWrapper[] wrappers ) {
		String[] signals = new String[wrappers.length];
		for ( int index = 0 ; index < wrappers.length ; index++ ) {
			signals[index] = wrappers[index].getPV();
		}
		return signals;
	}

	
	/**
	 * Select or deselect the collection of signals without affecting the selection status of other signals.
	 * @param select true to select signals and false to deselect signals
	 */
	public void selectSignals( final boolean select ) {
		final List<PVRecord> signalRecords = PV_TABLE_MODEL.getRowRecords();

		for ( final PVRecord record : signalRecords ) {
			record.setEnabled( select );
		}

		PV_TABLE_MODEL.fireTableDataChanged();
		EVENT_PROXY.selectedSignalsChanged( this, getSelectedSignals() );
	}


	/** get the list of selected signals */
	public List<String> getSelectedSignals() {
		final List<String> selectedSignals = new ArrayList<String>();
		final List<PVRecord> signalRecords = PV_TABLE_MODEL.getRowRecords();

		for ( final PVRecord record : signalRecords ) {
			if ( record.getEnabled() ) {
				selectedSignals.add( record.getSignal() );
			}
		}
		return selectedSignals;
	}


	/** determine whether the signal is selected */
	private boolean isSignalSelected( final String signal ) {
		final PVRecord record = SIGNAL_RECORDS.get( signal );
		return record != null && record.getEnabled();
	}
	
	
	/**
	 * Filter each channel snapshot based on whether its signal is selected
	 * @param snapshots The snapshots to filter
	 * @return the array of filtered snapshots corresponding to selected signals
	 */
	public ChannelSnapshot[] filterSnapshots( final ChannelSnapshot[] snapshots ) {
		final List<ChannelSnapshot> filteredSnapshots = new ArrayList<ChannelSnapshot>( snapshots.length );
		
		for ( int index = 0 ; index < snapshots.length ; index++ ) {
			final ChannelSnapshot snapshot = snapshots[index];
			if ( isSignalSelected( snapshot.getPV() ) )  filteredSnapshots.add( snapshot );
		}
		ChannelSnapshot[] result = new ChannelSnapshot[filteredSnapshots.size()];
		filteredSnapshots.toArray( result );
		
		return result;
	}

	
	/**
	 * Get the main model
	 * @return the main model
	 */
	public BrowserModel getModel() {
		return _model;
	}


	/** update the PV table model */
	private void updatePVTableModel() {
		SIGNAL_RECORDS.clear();

		final ChannelGroup group = _model.getSelectedGroup();
		final List<PVRecord> signalRecords = new ArrayList<PVRecord>();
		if ( group != null ) {
			final ChannelWrapper[] wrappers = group.getChannelWrappers();
			for ( final ChannelWrapper wrapper : wrappers ) {
				final String signal = wrapper.getPV();
				final PVRecord record = new PVRecord( signal );
				signalRecords.add( record );
				SIGNAL_RECORDS.put( signal, record );
			}
		}

		PV_TABLE_MODEL.setRecords( signalRecords );
	}


	/** update the detail for the selected machine snapshot */
	private void updateSelectedMachineSnapshotDetail() {
		final MachineSnapshot snapshot = _selectedSnapshot;
		
		final ChannelSnapshot[] channelSnapshots = ( snapshot != null ) ? filterSnapshots( snapshot.getChannelSnapshots() ) : null;
		final List<ChannelSnapshot> channelSnapshotRecords = new ArrayList<ChannelSnapshot>();
		if ( channelSnapshots != null ) {
			for ( final ChannelSnapshot channelSnapshot : channelSnapshots ) {
				channelSnapshotRecords.add( channelSnapshot );
			}
		}
		CHANNEL_SNAPSHOT_TABLE_MODEL.setRecords( channelSnapshotRecords );
	}

	
	/**
	 * Set the snapshot which is selected by the user
	 * @param snapshot the machine snapshot to select
	 */
	public void setSelectedSnapshot( final MachineSnapshot snapshot ) {
		if ( snapshot != null ) {
			try {
				_model.populateSnapshot( snapshot );
			}
			catch( Exception exception ) {
				throw new RuntimeException( exception );
			}
		}
		_selectedSnapshot = snapshot;
		updateSelectedMachineSnapshotDetail();

		EVENT_PROXY.snapshotSelected( this, snapshot );
	}
	
	
	/**
	 * The model's connection has changed
	 * @param model The model whose connection changed
	 */
	public void connectionChanged( final BrowserModel model ) {}
	
	
	/**
	 * Update the channel wrappers for the newly selected channel group and 
	 * forward this event to the browser controller listeners.
	 * @param model the source of the event
	 * @param newGroup the newly selected channel group
	 */
	public void selectedChannelGroupChanged( final BrowserModel model, final ChannelGroup newGroup ) {
		updatePVTableModel();
		EVENT_PROXY.selectedChannelGroupChanged( this, newGroup );
	}
	
	
	/**
	 * Handle the "machine snapshot fetched" event.  Does nothing.
	 * @param model the model providing the event
	 * @param snapshots the new snapshots that have been fetched
	 */
	public void machineSnapshotsFetched( final BrowserModel model, final MachineSnapshot[] snapshots) {
		final List<MachineSnapshot> machineSnapshots = new ArrayList<MachineSnapshot>();
		for ( final MachineSnapshot snapshot : snapshots ) {
			machineSnapshots.add( snapshot );
		}
		MACHINE_SNAPSHOT_TABLE_MODEL.setRecords( machineSnapshots );
	}


	/** forward message that table record changed */
	public void recordModified( final KeyValueFilteredTableModel<PVRecord> tableModel, final PVRecord record, final String keyPath, final Object value ) {
		updateSelectedMachineSnapshotDetail();
		EVENT_PROXY.selectedSignalsChanged( this, getSelectedSignals() );
	}
}

