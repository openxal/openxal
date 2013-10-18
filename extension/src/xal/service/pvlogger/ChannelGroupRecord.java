//
// ChannelGroupRecord.java: Source file for 'ChannelGroupRecord'
// Project xal
//
// Created by t6p on 1/20/11
//

package xal.service.pvlogger;

import java.util.ArrayList;
import java.util.List;



/**
 * Represents the properties of a channel group which can be edited.
 * @author  tap
 */
public class ChannelGroupRecord {
	/** represented channel group */
	final private ChannelGroup CHANNEL_GROUP;
	
	/** default logging period (seconds) for the group */
	private double _defaultLoggingPeriod;
	
	/** rentention time in days (or zero for permanent retention) for snapshots associated with this group */
	private double _retention;
	
	/** service ID */
	private String _serviceID;
	
	/** description of the channel group */
	private String _description;
	
	
	/** Constructor */
	public ChannelGroupRecord( final ChannelGroup group ) {
		CHANNEL_GROUP = group;
		revert();
	}
	
	
	/** convert the list of groups to a list of records */
	static public List<ChannelGroupRecord> toRecords( final List<ChannelGroup> groups ) {
		final List<ChannelGroupRecord> records = new ArrayList<ChannelGroupRecord>( groups.size() );
		for ( final ChannelGroup group : groups ) {
			records.add( new ChannelGroupRecord( group ) );
		}
		
		return records;
	}
	
	
	/** revert to the group settings */
	public void revert() {
		_defaultLoggingPeriod = CHANNEL_GROUP.getDefaultLoggingPeriod();
		_retention = CHANNEL_GROUP.getRetention();
		_serviceID = CHANNEL_GROUP.getServiceID();
		_description = CHANNEL_GROUP.getDescription();
	}
	
	
	/** get the represented channel group */
	public ChannelGroup getGroup() {
		return CHANNEL_GROUP;
	}
	
	
	/** get the service ID */
	public String getServiceID() {
		return _serviceID;
	}
	
	
	/** set the service ID */
	public void setServiceID( final String serviceID ) {
		_serviceID = serviceID;
	}
	
	
	/** get the description */
	public String getDescription() {
		return _description;
	}
	
	
	/** set the description */
	public void setDescription( final String description ) {
		_description = description;
	}
	
	
	/** get the label */
	public String getLabel() {
		return CHANNEL_GROUP.getLabel();
	}
	
	
	/** get the default logging period */
	public double getDefaultLoggingPeriod() {
		return _defaultLoggingPeriod;
	}
	
	
	/** set the default logging period */
	public void setDefaultLoggingPeriod( final double period ) {
		_defaultLoggingPeriod = period;
	}
	
	
	/** get the retention */
	public double getRetention() {
		return _retention;
	}
	
	
	/** set the retention */
	public void setRetention( final double retention ) {
		_retention = retention;
	}
}	
