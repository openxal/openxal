/*
 * MachineSnapshotTableModel.java
 *
 * Created on Thu Apr 29 16:30:52 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogbrowser.apputils;

import xal.service.pvlogger.MachineSnapshot;
import xal.service.pvlogger.ChannelGroup;
import javax.swing.table.*;


/**
 * MachineSnapshotTableModel is the table model for displaying the list of fetched machine snapshots.
 * @author  tap
 */
public class MachineSnapshotTableModel extends AbstractTableModel implements BrowserModelListener {
	final static protected int TIMESTAMP_COLUMN = 0;
	final static protected int ID_COLUMN = 1;
	
	protected MachineSnapshot[] _snapshots;
	
	
	/**
	 * Primary constructor
	 * @param snapshots the array of machine snapshots to display
	 */
	public MachineSnapshotTableModel(MachineSnapshot[] snapshots) {
		setMachineSnapshots(snapshots);
	}
	
	
	/**
	 * Constructor
	 */
	public MachineSnapshotTableModel() {
		this(new MachineSnapshot[0]);
	}
	
	
	/**
	 * Set the array of machine snapshots to display
	 * @param snapshots the array of machine snapshots to display
	 */
	public void setMachineSnapshots(MachineSnapshot[] snapshots) {
		_snapshots = snapshots;
		fireTableDataChanged();
	}
	
	
	/**
	 * Get the machine snapshot displayed at the specified table row.
	 * @param row the index of the table row for which to get the associated machine snapshot
	 */
	public MachineSnapshot getMachineSnapshotAt(final int row) {
		return _snapshots[row];
	}
	
	
	/**
	 * Get the number of rows in the table
	 * @return the number of rows in the table
	 */
	public int getRowCount() {
		return _snapshots.length;
	}
	
	
	/**
	 * Get the number of table columns
	 * @return the number of table columns
	 */
	public int getColumnCount() {
		return 2;
	}
	
	
	/** get the class of the data for the specified column */
	public Class getColumnClass( final int column ) {
		switch( column ) {
			case ID_COLUMN:
				return Number.class;
			default:
				return super.getColumnClass( column );
		}
	}
	
	
	/**
	 * Get the value of the item associated with the cell at the specified row and column
	 * @param row index of the cell's row
	 * @param column index of the cell's column
	 * @return the value to display in the cell
	 */
	public Object getValueAt( final int row, final int column ) {
		final MachineSnapshot snapshot = _snapshots[row];
		
		switch( column ) {
			case TIMESTAMP_COLUMN:
				return snapshot.getTimestamp();
			case ID_COLUMN:
				return  Long.valueOf( snapshot.getId() );
			default:
				return "";
		}
	}
	
	
	/**
	 * Get the name to display in the column's header
	 * @param column index of the column
	 * @return the name to display in the column's header
	 */
	public String getColumnName( final int column ) {
		switch( column ) {
			case TIMESTAMP_COLUMN:
				return "Timestamp";
			case ID_COLUMN:
				return "ID";
			default:
				return "";
		}
	}	
	
	
	/**
	 * The browser model's connection has changed
	 * @param model The model whose connection changed
	 */
	public void connectionChanged(BrowserModel model) {}
	
	
	/**
	 * event indicating that the selected channel group changed
	 * @param model the source sending this notice
	 * @param newGroup the newly selected channel group
	 */
	public void selectedChannelGroupChanged(BrowserModel model, ChannelGroup newGroup) {}
	
	
	/**
	 * event indicating that machine snapshots have been fetched.
	 * @param model the source of this event
	 * @param snapshots the fetched snapshots
	 */
	public void machineSnapshotsFetched(BrowserModel model, MachineSnapshot[] snapshots) {
		setMachineSnapshots(snapshots);
	}
}

