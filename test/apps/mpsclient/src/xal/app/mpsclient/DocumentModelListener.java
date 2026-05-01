/*
 * DocumentModelListener.java
 *
 * Created on Mon Mar 15 12:57:12 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.mpsclient;

import java.util.*;


/**
 * DocumentModelListener is implemented by receivers which wish to receive document model events.
 *
 * @author  tap
 */
public interface DocumentModelListener extends RemoteMPSRecordListener {
	/**
	 * This event indicates that a new handler has been selected
	 * @param model The model sending the event
	 * @param handler The new selected handler or null if none is selected
	 */
	public void handlerSelected(DocumentModel model, RemoteMPSRecord handler);
	
	
	/**
	 * This event is sent to indicate that a new MPS type has been selected.
	 * @param model The model sending the event
	 * @param index The index of the MPS type selected or -1 if none is selected
	 */
	public void mpsTypeSelected(DocumentModel model, int index);
}

