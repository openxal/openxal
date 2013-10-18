package xal.extension.scan;

import xal.ca.*;

import java.util.*;
import java.awt.event.*;

/**
 *  The ActionEvent extention to keep info about a channel record in the case of MonitoredPV event.
 *
 *@author     shishlo
 *created    October 31, 2005
 */
public class MonitoredPVEvent extends ActionEvent {
	private static final long serialVersionUID = 0L;

	private ChannelRecord record = null;
	private Channel chan = null;

	/**
	 *  Constructor for the MonitoredPVEvent object
	 *
	 *@param  mpv       The Parameter
	 *@param  recordIn  The Parameter
	 *@param  chanIn    The Parameter
	 */
	public MonitoredPVEvent(MonitoredPV mpv, ChannelRecord recordIn, Channel chanIn) {
		super(mpv, 0, "changed");
		record = recordIn;
		chan = chanIn;
	}

	/**
	 *  Returns the channelRecord of the MonitoredPVEvent object
	 *
	 *@return    The channelRecord
	 */
	public ChannelRecord getChannelRecord() {
		return record;
	}

	/**
	 *  Returns the channel of the MonitoredPVEvent object
	 *
	 *@return    The channel
	 */
	public Channel getChannel() {
		return chan;
	}
}

