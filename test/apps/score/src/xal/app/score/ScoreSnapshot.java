/*
 * ScoreSnapshot.java
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.score;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import xal.tools.data.GenericRecord;

/**
 * ScoreSnapshot is a representation of the data for a snapshot of the machine state at some point in time.
 *
 * @author  tap
 */
public class ScoreSnapshot {
	static final protected DateFormat TIME_FORMAT;
	/** date/time of the snapshot */
	protected Timestamp _timestamp;
	/** the dataTable containing the default PV, system + type information*/
 	protected PVData _data;
	protected String _type;
	protected String _comment;
	
	/**
	 * Static initializer 
	 */
	static {
		TIME_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
	}
	
	
	/**
	 * Primary constructor.
	 * @param type Identifies the type of machine snapshot
	 * @param timestamp The timestamp when the snapshot was taken.
	 * @param comment A comment about this snapshot.
	 * @param data - the PVData table with the snapshot data
	 */
	public ScoreSnapshot(String type, Timestamp timestamp, String comment, PVData data) {
		_type = type;
		_timestamp = timestamp;
		_comment = comment;
		_data = data;
	}
	
	/**
	 * Constructor to create a snapshot with no underlying data.
	 * useful for collecting lists of potentially interesting snaphots,
	 * over a prescribed time interval.
	 * @param type Identifies the type of machine snapshot
	 * @param timestamp The timestamp when the snapshot was taken.
	 * @param comment A comment about this snapshot.
	 */
	public ScoreSnapshot(String type, Timestamp timestamp, String comment) {
		_type = type;
		_timestamp = timestamp;
		_comment = comment;
		_data = null;
	}

	
	/**
	 * Primary constructor.
	 * @param type Identifies the type of machine snapshot
	 * @param timestamp The timestamp when the snapshot was taken.
	 * @param comment A comment about this snapshot.
	 * @param rows - array of scoreRow data
	 */	
	public ScoreSnapshot(String type, Timestamp timestamp, String comment, ScoreRow[] rows) {
		_type = type;
		_timestamp = timestamp;
		_comment = comment;
		_data = makeData(type, rows);		
	}
	
	/** construct a snapshot from a scoregroup. 
	 * the "saved data, timestamp and comment will be empty"
	 * @param group PV data record schema 
	 */
	public ScoreSnapshot( ScoreGroup group) {
		_type = group.getLabel();
		_timestamp = null;
		_comment = "new data set";
		_data = group.createPVData();
	}
	
	/** an empty snapshot */
	public ScoreSnapshot() {
		_timestamp = null;
		_comment = "";
	}
	
	/**
	 * Set the PVData for the machine snapshot
	 * @param data - the PVData structure
	 */
	void setData(final PVData data) {
		_data = data;
	}
	
	
	/**
	 * Get the channel snapshots.
	 * @return The array of channel snapshots.
	 */
	public PVData getData() {
		return _data;
	}
	
	
	/**
	 * Get the group id which identifies the type of machine snapshot
	 * @return the group id identifying the type of snapshot
	 */
	public String getType() {
		return _type;
	}
	
	
	/**
	 * Set the group id identifying the type of snapshot
	 * @param type type of snapshot
	 */
	public void setType(final String type) {
		_type = type;
	}
	
	
	/**
	 * Get the comment.
	 * @return the comment assigned to this machine snapshot.
	 */
	public String getComment() {
		return _comment;
	}
	
	
	/**
	 * Set the comment.
	 * @param comment The comment to assign to this machine snapshot.
	 */
	public void setComment(String comment) {
		_comment = comment;
	}
	
	
	/**
	 * Get the timestamp.
	 * @return The time when this machine snapshot was taken.
	 */
	public Timestamp getTimestamp() {
		return _timestamp;
	}
	
	/**
	 * Set the timestamp.
	 * @param ts time when this machine snapshot was taken.
	 */
	public void setTimestamp(Timestamp ts) {
		_timestamp = ts;
	}
	
	/** get the number of rows in the dataTable of this snapshot */
	protected int getRowCount() {
		if(_data == null) return 0;
		return (_data.getDataTable().records()).size();
		
	}
	
	/** method to cast the PVData structure into an array of ScoreRows,
	* which is more convenient for publishing to the database */
	protected ScoreRow[] getScoreRows() {
		String rbName, spName, sys, subSys, useRB;
		ScoreRecord record;
		ScoreRow aRow;
		final List<ScoreRow> rows = new ArrayList<ScoreRow>();
		Collection<GenericRecord> records = _data.getDataTable().records();
		Iterator<GenericRecord> itr = records.iterator();
		while (itr.hasNext()) {
		    record = (ScoreRecord) itr.next();
		    sys = record.stringValueForKey(PVData.systemKey);
		    subSys = record.stringValueForKey(PVData.typeKey);
			final DataTypeAdaptor dataTypeAdaptor = record.getDataTypeAdaptor();
		    rbName = record.stringValueForKey(PVData.rbNameKey);
		    spName = record.stringValueForKey(PVData.spNameKey);
		    final String rbValStr =  record.stringValueForKey( PVData.rbSavedValKey );
		    final String spValStr =  record.stringValueForKey( PVData.spSavedValKey );
		    spName = (spName.equals("null")) ? null : spName;
		    rbName = (rbName.equals("null")) ? null : rbName;		    
		    if(record.booleanValueForKey(PVData.restoreRBValKey))
			    useRB = "Y";
		    else
			    useRB = "N";
		    aRow = ScoreRow.getInstanceWithStringRepValues( sys, subSys, dataTypeAdaptor, rbName, rbValStr, spName, spValStr, useRB );
		    rows.add( aRow );
		}
		return rows.toArray( new ScoreRow[rows.size()] );
	}
	
	/** create the PVData table from an array of ScoreRows
	* this is used when constructing a ScoreSnapshot from the database */
	
	protected PVData makeData(String type, ScoreRow [] rows) {
		PVData data = new PVData(type);
		String spName, rbName;	
		for (int index = 0; index < rows.length; index++) {
			final DataTypeAdaptor dataTypeAdaptor = rows[index].getDataTypeAdaptor();
			spName = (rows[index].getSPName() == null) ? "null" : rows[index].getSPName();
			rbName = (rows[index].getRBName() == null) ? "null" : rows[index].getRBName();
			final Object rbVal = rows[index].getRBValue();
			final String rbValStr = dataTypeAdaptor.asString( rbVal );
			final Object spVal = rows[index].getSPValue();
			final String spValStr = dataTypeAdaptor.asString( spVal );
			data.addRecord( rows[index].getSystem(), rows[index].getSubSystem(), dataTypeAdaptor, spName, spValStr, rbName, rbValStr, rows[index].useRB() );
		}
		return data;
	}
	
	/**
	 * Override toString() to get a textual description of the machine snapshot.
	 * @return a textual description of the machine snapshot.
	 */
	@Override
    public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("type:  " + _type + "\n");
		buffer.append("Timestamp:  " + TIME_FORMAT.format(_timestamp) + "\n");
		buffer.append("Comment:  " + _comment + "\n");	
		return buffer.toString();
	}
}

