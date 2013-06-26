//
//  ModificationStore.java
//  xal
//
//  Created by Thomas Pelaia on 11/17/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import xal.tools.messaging.MessageCenter;

import java.util.Map;


/** Store and distribute modification information. */
public class ModificationStore {
	/** the message center for distributing notifications */
	final MessageCenter MESSAGE_CENTER;
	
	/** event proxy which forwards notifications */
	final ModificationStoreListener EVENT_PROXY;
	
	
	/** Constructor */
	public ModificationStore() {
		MESSAGE_CENTER = new MessageCenter( "Modification Store" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, ModificationStoreListener.class );
	}
	
	
	/**
	 * Add the listener to receive events from this instance.
	 * @param listener the listener to receive events.
	 */
	public void addModificationStoreListener( final ModificationStoreListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, ModificationStoreListener.class );
	}
	
	
	/**
	 * Remove the listener from receiving events from this instance.
	 * @param listener the listener to remove.
	 */
	public void removeModificationStoreListener( final ModificationStoreListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, ModificationStoreListener.class );
	}
	
	
	/**
	 * Post a modification event.
	 * @param source the object which was modified
	 * @param modification optional modification information
	 */
	public void postModification( final Object source, final Map<?, ?> modification ) {
		EVENT_PROXY.modificationMade( this, source, modification );
	}
	
	
	/**
	 * Post a modification event with no modification information.
	 * @param source the object which was modified
	 */
	public void postModification( final Object source ) {
		postModification( source, null );
	}
}
