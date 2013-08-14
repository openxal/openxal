//
//  KnobsController.java
//  xal
//
//  Created by Thomas Pelaia on 10/31/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.tools.messaging.MessageCenter;

import java.util.*;


/** Manages knob and group selections */
public class KnobsController {
	/** The message center for posting events from this instance */
	final protected MessageCenter MESSAGE_CENTER;
	
	/** The proxy used to post KnobsController events */
	final protected KnobsControllerListener EVENT_PROXY;
	
	/** The group which is presently selected */
	protected KnobGroup _selectedGroup;
	
	/** The application which is presently selected */
	protected List<Knob> _selectedKnobs;
	
	
	/** Constructor */
	public KnobsController() {
		MESSAGE_CENTER = new MessageCenter( "KnobsController" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, KnobsControllerListener.class );
		
		_selectedKnobs = new ArrayList<Knob>();
	}
	
	
	/**
	 * Add a listener of KnobsController events from this instance
	 * @param listener The listener to add
	 */
	public void addKnobsControllerListener( final KnobsControllerListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, KnobsControllerListener.class );
	}
	
	
	/**
	 * Remove a listener of KnobsController events from this instance
	 * @param listener The listener to remove
	 */
	public void removeKnobsControllerListener( final KnobsControllerListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, KnobsControllerListener.class );
	}
	
	
	/**
	 * Get the presently selected group.
	 * @return the presently selected group.
	 */
	public KnobGroup getSelectedGroup() {
		return _selectedGroup;
	}
	
	
	/**
	 * Set the newly selected group
	 * @param group The newly selected group
	 */
	public void setSelectedGroup( final KnobGroup group ) {
		_selectedGroup = group;
		EVENT_PROXY.selectedGroupChanged( this, group );
		setSelectedKnobs( null );
	}
	
	
	/**
	 * Get the presently selected knob
	 * @return the presently selected knob
	 */
	public Knob getSelectedKnob() {
		return _selectedKnobs.isEmpty() ? null : _selectedKnobs.get( 0 );
	}
	
	
	/**
	 * Get the presently selected knobs
	 * @return the presently selected knobs
	 */
	public List<Knob> getSelectedKnobs() {
		return _selectedKnobs;
	}
	
	
	/**
	 * Set the newly selected knob
	 * @param knob The newly selected knob
	 */
	public void setSelectedKnob( final Knob knob ) {
		setSelectedKnobs( knob == null ? null : Collections.singletonList( knob ) );
	}	
	
	
	/**
	 * Set the list of newly selected knobs
	 * @param knobs The list of newly selected knobs
	 */
	public void setSelectedKnobs( final List<Knob> knobs ) {
		_selectedKnobs.clear();
		if ( knobs != null ) {
			_selectedKnobs.addAll( knobs );
		}
		EVENT_PROXY.selectedKnobsChanged( this, _selectedKnobs );
	}	
}
