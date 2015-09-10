//
//  ParameterStoreListener.java
//  xal
//
//  Created by Thomas Pelaia on 6/20/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;


/** Interface of parameter store events */
public interface ParameterStoreListener {
	/**
	 * Event indicating that the parameter store's parameters have been cleared.
	 * @param store the source of the event.
	 */
	public void parametersCleared( final ParameterStore store );
	
	
	/**
	 * Event indicating that a live parameter has been added.
	 * @param store the source of the event.
	 * @param parameter the parameter which has been added.
	 */
	public void liveParameterAdded( final ParameterStore store, final LiveParameter parameter );
	
	
	/**
	 * Event indicating that a core parameter has been added.
	 * @param store the source of the event.
	 * @param parameter the parameter which has been added.
	 */
	public void coreParameterAdded( final ParameterStore store, final CoreParameter parameter );
	
	
	/**
	 * Event indicating that a live parameter has been modified.
	 * @param store the source of the event.
	 * @param parameter the parameter which has changed.
	 */
	public void liveParameterModified( final ParameterStore store, final LiveParameter parameter );
}
