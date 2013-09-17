//
//  KnobGroupListener.java
//  xal
//
//  Created by Thomas Pelaia on 10/31/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import java.util.*;


/** Listener of knob group events */
public interface KnobGroupListener {
	/**
	 * Notice that knobs have been added to the specified group
	 * @param group The group to which new knobs were added
	 * @param addedKnobs The knobs which have been added to the group
	 */
	public void knobsAdded( KnobGroup group, Collection<Knob> addedKnobs );
	
	
	/**
	 * Notice that knobs have been removed from the specified group
	 * @param group The group from which knobs were removed
	 * @param removedKnobs The knobs which have been removed from the group
	 */
	public void knobsRemoved( KnobGroup group, Collection<Knob> removedKnobs );
	
	
	/**
	 * The group's label has been changed
	 * @param group the group whose label has been changed
	 */
	public void labelChanged( KnobGroup group );
	
	
	/**
	 * Indicates that a knob has been modified.
	 * @param group the group whose knob has been modified
	 * @param knob the knob which has been modified
	 */
	public void knobModified( KnobGroup group, Knob knob );
}
