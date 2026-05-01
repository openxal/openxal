/*
 * SnapshotDetailTableModel.java
 *
 * Created on Fri Apr 30 12:01:10 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogbrowser.apputils;

import xal.service.pvlogger.ChannelGroup;
import xal.tools.text.FormattedNumber;
import xal.service.pvlogger.ChannelSnapshot;
import xal.service.pvlogger.MachineSnapshot;

import java.util.*;
import javax.swing.table.*;


/**
 * SnapshotDetailTableModel is the table model for displaying the channel snapshots associated with the selected machine snapshot.
 * @author  tap
 */
public class SnapshotDetailTableModel  extends AbstractTableModel implements BrowserControllerListener {
	final static protected int SIGNAL_COLUMN = 0;
	final static protected int TIMESTAMP_COLUMN = 1;
	final static protected int VALUE_COLUMN = 2;
	final static protected int COUNT_COLUMN = 3;
	final static protected int STATUS_COLUMN = 4;
	final static protected int SEVERITY_COLUMN = 5;
	
	protected BrowserController _controller;
	
	protected MachineSnapshot _snapshot;
	protected ChannelSnapshot[] _channelSnapshots;
	protected ChannelSnapshot[] _filteredChannelSnapshots;
	
	
	/**
	 * Primary constructor
	 * @param controller the selection controller
	 * @param snapshot the machine snapshot whose filtered channel snapshots are to be displayed
	 */
	public SnapshotDetailTableModel( final BrowserController controller, final MachineSnapshot snapshot ) {
		_controller = controller;
		setMachineSnapshot( snapshot );
	}
	
	
	/**
	 * Constructor
	 * @param controller the selection controller
	 */
	public SnapshotDetailTableModel( final BrowserController controller ) {
		this(controller, null);
	}
	
	
	/**
	 * Set the machine snapshot whose filtered channel snapshots are to be displayed
	 * @param snapshot the machine snapshot
	 */
	public void setMachineSnapshot( final MachineSnapshot snapshot ) {
		_snapshot = snapshot;
		_channelSnapshots = (snapshot != null) ? snapshot.getChannelSnapshots() : new ChannelSnapshot[0];
		_filteredChannelSnapshots = _controller.filterSnapshots( _channelSnapshots );
		fireTableDataChanged();
	}
	
	
	/**
	 * Get the number of table rows to display
	 * @return the number of rows
	 */
	public int getRowCount() {
		return _filteredChannelSnapshots.length;
	}
	
	
	/**
	 * Get the number of table columns to display
	 * @return the number of columns
	 */
	public int getColumnCount() {
		return 6;
	}
	
	
	/** get the snapshot at the specified row */
	public ChannelSnapshot getChannelSnapshot( final int row ) {
		return _filteredChannelSnapshots[ row ];
	}
	
	
	/**
	 * Get the value of the item to display in the specified table cell
	 * @param row the index of the cell's row
	 * @param column the index of the cell's column
	 * @return the value to display in the cell
	 */
	public Object getValueAt( final int row, final int column ) {
		final ChannelSnapshot channelSnapshot = getChannelSnapshot( row );
		
		switch( column ) {
			case SIGNAL_COLUMN:
				return channelSnapshot.getPV();
			case TIMESTAMP_COLUMN:
				return channelSnapshot.getTimestamp();
			case COUNT_COLUMN:
				return channelSnapshot.getValue().length;
			case VALUE_COLUMN:
				final double[] arrayValue = channelSnapshot.getValue();
				return new FormattedNumber( arrayValue.length > 0 ? arrayValue[0] : Double.NaN );
			case STATUS_COLUMN:
				return  Integer.valueOf( channelSnapshot.getStatus() );
			case SEVERITY_COLUMN:
				return  Integer.valueOf( channelSnapshot.getSeverity() );
			default:
				return "";
		}
	}
	
	
	/**
	 * Get the name to display in the column's header
	 * @param column the index of the column
	 * @return the name to display in the column's header
	 */
	public String getColumnName( final int column ) {
		switch( column ) {
			case SIGNAL_COLUMN:
				return "Signal";
			case TIMESTAMP_COLUMN:
				return "Timestamp";
			case VALUE_COLUMN:
				return "Value";
			case COUNT_COLUMN:
				return "Count";
			case STATUS_COLUMN:
				return "Status";
			case SEVERITY_COLUMN:
				return "Severity";
			default:
				return "";
		}
	}
	
	
	/** get the class associated with the specified column */
	public Class getColumnClass( final int column ) {
		switch ( column ) {
			case VALUE_COLUMN: case COUNT_COLUMN: case STATUS_COLUMN: case SEVERITY_COLUMN:
				return Number.class;
			default:
				return super.getColumnClass( column );
		}
	}
	
	
	/** 
	 * event indicating that a snapshot has been selected
	 * @param controller The controller managing selection state
	 * @param snapshot The snapshot that has been selected
	 */
	public void snapshotSelected( final BrowserController controller, final MachineSnapshot snapshot ) {
		setMachineSnapshot( snapshot );
	}
	
	
	/**
	 * event indicating that the selected channel group changed
	 * @param source the browser controller sending this notice
	 * @param newGroup the newly selected channel group
	 */
	public void selectedChannelGroupChanged( final BrowserController source, final ChannelGroup newGroup ) {}
	
	
	/**
	 * Event indicating that the selected signals have changed
	 * @param source the controller sending the event
	 * @param selectedSignals the new collection of selected signals
	 */
	public void selectedSignalsChanged( final BrowserController source, final Collection selectedSignals ) {
		_filteredChannelSnapshots = source.filterSnapshots( _channelSnapshots );
		fireTableDataChanged();
	}
}

