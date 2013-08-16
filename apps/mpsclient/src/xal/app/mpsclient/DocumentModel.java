/*
 * DocumentModel.java
 *
 * Created on Thu Feb 05 08:46:34 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.mpsclient;

import xal.tools.messaging.MessageCenter;
 
import java.util.*;


/**
 * DocumentModel is a model for one document and maintains and broadcasts selection state status.
 *
 * @author  tap
 */
public class DocumentModel implements RemoteMPSRecordListener {
	/** main model */
	protected MPSModel _mainModel;
	
	/** selected remote MPS record */
    protected RemoteMPSRecord _selectedHandler;
	
	/** The list index of the selected MPS Type */
	protected int _selectedMPSTypeIndex;
	
	/** Message center for dispatching events */
	protected MessageCenter _messageCenter;
	
	/** Proxy for posting DocumentModel events */
	protected DocumentModelListener _proxy;
	
	
	/**
	 * DocumentModel constructor
	 * @param mainModel The application's main model
	 */
	public DocumentModel(MPSModel mainModel) {
		_messageCenter = new MessageCenter("Document Model");
		_proxy = _messageCenter.registerSource( this, DocumentModelListener.class );
		
		_mainModel = mainModel;
		_selectedHandler = null;
		_selectedMPSTypeIndex = -1;
	}
	
	
	/**
	 * Dispose of the resources managed by this model.
	 */
	public void dispose() {
	}
	
	
	/**
	 * Add the specified listener as a receiver of this model's events.
	 * @param listener The listener to add as a receiver of this model's events.
	 */
	public void addDocumentModelListener(DocumentModelListener listener) {
		_messageCenter.registerTarget( listener, this, DocumentModelListener.class );
	}
	
	
	/**
	 * Remove the specified listener from being a receiver of this model's events.
	 * @param listener The listener to remove.
	 */
	public void removeDocumentModelListener(DocumentModelListener listener) {
		_messageCenter.removeTarget( listener, this, DocumentModelListener.class );
	}
	
	
	/**
	 * Set the selected request handler
	 * @param handler the selected request handler
	 */
	public void setSelectedHandler( final RemoteMPSRecord handler ) {
        if ( _selectedHandler != null )  _selectedHandler.removeRemoteMPSRecordListener( this );
		_selectedHandler = handler;
        if ( handler != null )  handler.addRemoteMPSRecordListener( this );
		setSelectedMPSTypeIndex(-1);
		_proxy.handlerSelected( this, handler );
	}
	
	
	/**
	 * Get the request handler for the selected MPS tool
	 * @return the request handler for the selected MPS tool
	 */
	public RemoteMPSRecord getSelectedHandler() {
		return _selectedHandler;
	}
	
	
	/**
	 * Set the index of the selected MPS latch type.
	 * @param index The new index of the selected MPS latch type or -1 if none is selected.
	 */
	public void setSelectedMPSTypeIndex(int index) {
		_selectedMPSTypeIndex = index;
		_proxy.mpsTypeSelected( this, index );
	}
	
	
	/**
	 * Get the index of the selected MPS latch type.
	 * @return the index of the selected MPS latch type or -1 if none is selected
	 */
	public int getSelectedMPSTypeIndex() {
		return _selectedMPSTypeIndex;
	}
	
	
	/**
	 * Get the main (application wide) MPS model
	 * @return the main MPS model
	 */
	public MPSModel getMainModel() {
		return _mainModel;
	}
	
	
	/**
	 * Indicates that MPS channels have been updated.  Rebroadcast
	 * this event to document model listeners.
	 * @param handler The handler sending the event
	 * @param mpsTypeIndex index of the MPS type for which the event applies
	 * @param channelRefs The list of the new ChannelRef instances
	 */
	public void mpsChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, List<ChannelRef> channelRefs) {
		if ( mpsTypeIndex == _selectedMPSTypeIndex ) {
			_proxy.mpsChannelsUpdated( handler, mpsTypeIndex, channelRefs );
		}
	}
	
	
	/**
	 * Indicates that Input channels have been updated.
	 * @param handler The handler sending the event
	 * @param mpsTypeIndex index of the MPS type for which the event applies
	 * @param channelRefs The list of the new ChannelRef instances
	 */
	 public void inputChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, List<ChannelRef> channelRefs) {
		if ( mpsTypeIndex == _selectedMPSTypeIndex ) {
			_proxy.inputChannelsUpdated( handler, mpsTypeIndex, channelRefs );
		}
	 }
	
	
	/**
	 * Indicates that an MPS event has happened.  Rebroadcast
	 * this event to document model listeners.
	 * @param handler The handler sending the event
	 * @param mpsTypeIndex index of the MPS type for which the event applies
	 */
	public void mpsEventsUpdated(RemoteMPSRecord handler, int mpsTypeIndex) {
		if ( mpsTypeIndex == _selectedMPSTypeIndex ) {
			_proxy.mpsEventsUpdated( handler, mpsTypeIndex );
		}
	}
	
	
	/**
	 * Indicates that the handler has checked for new status from the MPS service.  Rebroadcast
	 * this event to document model listeners.
	 * @param handler The handler sending the event.
	 * @param timestamp The timestamp of the latest status check
	 */
	public void lastCheck(RemoteMPSRecord handler, Date timestamp) {
		_proxy.lastCheck( handler, timestamp );
	}
}

