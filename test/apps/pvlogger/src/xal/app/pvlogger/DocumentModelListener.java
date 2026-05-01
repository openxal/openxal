/*
 * DocumentModelListener.java
 *
 * Created on Thu Mar 18 09:30:02 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogger;

import xal.tools.data.GenericRecord;

import java.util.*;


/**
 * DocumentModelListener is the interface for receiving DocumentModel events
 *
 * @author  tap
 */
public interface DocumentModelListener {
	/**
	 * Notification that a new logger has been selected
	 * @param source the document model managing selections
	 * @param handler the latest handler selection or null if none is selected
	 */
	public void handlerSelected(DocumentModel source, RemoteLoggerRecord handler);
	
	
	/**
	 * Notification that a new logger session has been selected
	 * @param source the document model managing selections
	 * @param handler the latest session handler selection or null if none is selected
	 */
	public void sessionHandlerSelected(DocumentModel source, LoggerSessionHandler handler);
	
	
	/**
	 * Notification that the channels of the selected logger have changed
	 * @param model the document model managing selections
	 * @param channelRefs the latest channel refs containing the channel information
	 */
	public void channelsChanged(DocumentModel model, List<ChannelRef> channelRefs);
	
	
	/**
	 * Notification that a new machine snapshot has been published
	 * @param model the document model managing selections
	 * @param timestamp the timestamp of the latest machine snapshot
	 * @param snapshotDump the textual dump of the latest machine snapshot
	 */
	public void snapshotPublished(DocumentModel model, Date timestamp, String snapshotDump);
	
	
	/**
	 * Notification that a logger record has been updated
	 * @param model the document model managing selections
	 * @param record the updated logger record
	 */
	public void recordUpdated(DocumentModel model, GenericRecord record);
	
	
	/**
	 * Notification that a logger session has been updated
	 * @param model the document model managing selections
	 * @param source the updated logger session
	 */
	public void loggerSessionUpdated(DocumentModel model, LoggerSessionHandler source);
}

