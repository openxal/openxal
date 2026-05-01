/*
 * LoggerSessionListener.java
 *
 * Created on Tue Jun 01 16:21:57 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogger;

import java.util.*;


/**
 * LoggerSessionListener
 *
 * @author  tap
 * @since Jun 01, 2004
 */
public interface LoggerSessionListener {
	/**
	 * Notification that the specified logger session handler's channels have changed.
	 * @param source the logger session handler whose channels changed
	 * @param channelRefs the new channel references
	 */
	public void channelsChanged(LoggerSessionHandler source, List<ChannelRef> channelRefs);
	
	
	/**
	 * Notification that the specified logger session has published a new machine snapshot
	 * @param source the logger session for which the snapshot has been published
	 * @param timestamp the timestamp of the machine snapshot
	 * @param snapshotDump a textual dump of the machine snapshot
	 */
	public void snapshotPublished(LoggerSessionHandler source, Date timestamp, String snapshotDump);
	
	
	/**
	 * Notification that the logger session handler has been updated
	 * @param source the logger session handler which has been updated
	 */
	public void loggerSessionUpdated(LoggerSessionHandler source);
}

