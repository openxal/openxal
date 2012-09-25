/*
 * MachineSnapshot.java
 *
 * Created on Thu Dec 04 13:27:17 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger;

import java.util.*;
import java.text.*;


/**
 * MachineSnapshot is a representation of the data for a snapshot of the machine state at some point in time.
 *
 * @author  tap
 */
public class MachineSnapshot {
	static final protected DateFormat TIME_FORMAT;
	
	protected long _id;
	protected Date _timestamp;
	protected ChannelSnapshot[] _channelSnapshots;
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
	 * @param id The unique identifier of this instance in the persistent storage.
	 * @param type Identifies the type of machine snapshot
	 * @param timestamp The timestamp when the snapshot was taken.
	 * @param comment A comment about this snapshot.
	 * @param channelSnapshots The channel snapshots associated with this machine snapshot.
	 */
	public MachineSnapshot(long id, String type, Date timestamp, String comment, ChannelSnapshot[] channelSnapshots) {
		_id = id;
		_type = type;
		_timestamp = timestamp;
		_comment = comment;
		_channelSnapshots = channelSnapshots;
	}
	
	
	/**
	 * Primary constructor.
	 * @param id The unique identifier of this instance in the persistent storage.
	 * @param timestamp The timestamp when the snapshot was taken.
	 * @param comment A comment about this snapshot.
	 * @param channelSnapshots The channel snapshots associated with this machine snapshot.
	 */
	public MachineSnapshot(long id, Date timestamp, String comment, ChannelSnapshot[] channelSnapshots) {
		this(id, null, timestamp, comment, channelSnapshots);
	}
	
	
	/**
	 * Constructor.
	 * @param timestamp The timestamp when the snapshot was taken.
	 * @param comment A comment about this snapshot.
	 * @param channelSnapshots The channel snapshots associated with this machine snapshot.
	 */
	public MachineSnapshot(Date timestamp, String comment, ChannelSnapshot[] channelSnapshots) {
		this(0, timestamp, comment, channelSnapshots);
	}
	
	
	/**
	 * Constructor of a MachineSnapshot with no data.  Placeholders for channel snapshots are constructed.
	 * @param channelCount The number of channel placeholders to make.
	 */
	public MachineSnapshot(int channelCount) {
		this( new Date(), "", new ChannelSnapshot[channelCount]);
	}
	
	
	/**
	 * Set the channel snapshot for the specified index.
	 * @param index The index identifying the channel snapshot placeholder
	 * @param channelSnapshot The channel snapshot to associate with this machine snapshot.
	 */
	public void setChannelSnapshot(int index, ChannelSnapshot channelSnapshot) {
		_channelSnapshots[index] = channelSnapshot;
	}
	
	
	/**
	 * Set the channel snapshots for the machine snapshot
	 * @param channelSnapshots The array of channel snapshots to associate with the machine snapshot
	 */
	void setChannelSnapshots(final ChannelSnapshot[] channelSnapshots) {
		_channelSnapshots = channelSnapshots;
	}
	
	
	/**
	 * Get the channel snapshots.
	 * @return The array of channel snapshots.
	 */
	public ChannelSnapshot[] getChannelSnapshots() {
		return _channelSnapshots;
	}
	
	
	/**
	 * Get the number of channel snapshot placeholders.
	 * @return the number of channel snapshot placeholders.
	 */
	public int getChannelCount() {
		return _channelSnapshots.length;
	}
	
	
	/**
	 * Get the unique identifier of this machine snapshot.
	 * @return The unique identifier of this machine snapshot.
	 */
	public long getId() {
		return _id;
	}
	
	
	/**
	 * Set the unique identifier of this machine snapshot.
	 * @param id The unique identifier to use for this machine snapshot
	 */
	public void setId(long id) {
		_id = id;
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
	public Date getTimestamp() {
		return _timestamp;
	}
	
	
	/**
	 * Override toString() to get a textual description of the machine snapshot.
	 * @return a textual description of the machine snapshot.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("id: " + _id + "\n");
		buffer.append("type:  " + _type + "\n");
		buffer.append("Timestamp:  " + TIME_FORMAT.format(_timestamp) + "\n");
		buffer.append("Comment:  " + _comment + "\n");
		for ( int index = 0 ; index < _channelSnapshots.length ; index++ ) {
			ChannelSnapshot channelSnapshot = _channelSnapshots[index];
			if ( channelSnapshot != null ) {
				buffer.append( channelSnapshot + "\n" );
			}
		}
		
		return buffer.toString();
	}
}

