/*
 * DocumentModel.java
 *
 * Created on Thu Feb 05 08:46:34 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogger;

import xal.tools.messaging.MessageCenter;
import xal.tools.data.GenericRecord;

import java.util.*;


/**
 * DocumentModel is a model for one document.  It manages the state of logger selections.
 *
 * @author  tap
 */
public class DocumentModel implements LoggerSessionListener {
	/** message center */
	final protected MessageCenter _messageCenter;
	
	/** proxy for posting messages */
	final protected DocumentModelListener _proxy;
	
	/** main logger model */
	protected LoggerModel _mainModel;
	
	/** selected handler to monitor */
	protected RemoteLoggerRecord _selectedHandler;
	
	/** selected session handler */
	protected LoggerSessionHandler _selectedSessionHandler;
	
	
	/**
	 * DocumentModel constructor
	 */
	public DocumentModel(LoggerModel mainModel) {
		_mainModel = mainModel;
		_selectedHandler = null;
		_selectedSessionHandler = null;
		
		_messageCenter = new MessageCenter("Document Model");
		_proxy = _messageCenter.registerSource( this, DocumentModelListener.class );
	}
	
	
	/**
	 * Dispose of the resources managed by this model.
	 */
	public void dispose() {}
	
	
	/**
	 * Add a listener of document model events from this document model
	 * @param listener the listener to add
	 */
	public void addDocumentModelListener(DocumentModelListener listener) {
		_messageCenter.registerTarget(listener, this, DocumentModelListener.class);
	}
	
	
	/**
	 * Remove a listener of document model events from this document model
	 * @param listener the listener to remove
	 */
	public void removeDocumentModelListener(DocumentModelListener listener) {
		_messageCenter.removeTarget(listener, this, DocumentModelListener.class);
	}
	
	
	/**
	 * Set the selected logger handler
	 * @param handler the selected logger handler
	 */
	public void setSelectedHandler( final RemoteLoggerRecord handler ) {
		_selectedHandler = handler;
		setSelectedSessionHandler( null );
		_proxy.handlerSelected( this, handler );
	}
	
	
	/**
	 * Get the logger handler for the selected logger tool
	 * @return the logger handler for the selected logger tool
	 */
	public RemoteLoggerRecord getSelectedHandler() {
		return _selectedHandler;
	}
	
	
	/**
	 * Set the selected session handler
	 * @param handler the selected session handler
	 */
	public void setSelectedSessionHandler( final LoggerSessionHandler handler ) {
		if ( _selectedSessionHandler != null )  _selectedSessionHandler.removeLoggerSessionListener( this );
		_selectedSessionHandler = handler;
		_proxy.sessionHandlerSelected( this, handler );
		if ( _selectedSessionHandler != null )  _selectedSessionHandler.addLoggerSessionListener( this );
	}
	
	
	/**
	 * Get the selected session handler within the selected logger handler
	 * @return the selected session handler
	 */
	public LoggerSessionHandler getSelectedSessionHandler() {
		return _selectedSessionHandler;
	}
	
	
	/**
	 * Get the main (application wide) logger model
	 * @return the main logger model
	 */
	public LoggerModel getMainModel() {
		return _mainModel;
	}
	
	
	/**
	 * Notification that the specified logger session handler's channels have changed.
	 * @param source the logger session handler whose channels changed
	 * @param channelRefs the new channel references
	 */
	public void channelsChanged( final LoggerSessionHandler source, final List<ChannelRef> channelRefs ) {
		_proxy.channelsChanged( this, channelRefs );
	}
	
	
	/**
	 * Notification that the specified logger session has published a new machine snapshot
	 * @param source the logger session for which the snapshot has been published
	 * @param timestamp the timestamp of the machine snapshot
	 * @param snapshotDump a textual dump of the machine snapshot
	 */
	public void snapshotPublished( final LoggerSessionHandler source, final Date timestamp, final String snapshotDump ) {
		_proxy.snapshotPublished( this, timestamp, snapshotDump );
	}
	
	
	/**
	 * Notification that the logger session handler has been updated
	 * @param source the logger session handler which has been updated
	 */
	public void loggerSessionUpdated( final LoggerSessionHandler source ) {
		_proxy.loggerSessionUpdated( this, source );
	}
}

