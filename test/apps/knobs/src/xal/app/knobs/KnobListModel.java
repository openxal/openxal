//
//  KnobListModel.java
//  xal
//
//  Created by Thomas Pelaia on 11/1/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import javax.swing.*;
import java.util.*;


/** Manage the displayed list of knobs */
public class KnobListModel extends AbstractListModel<String> implements KnobGroupListener, KnobsControllerListener {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
	/** The group which supplies the list of knobs to display */
	protected KnobGroup _group;
	
	
	/** Constructor */
	public KnobListModel() {}
	
	
	/**
	 * Set the group which supplies the list of knobs to display
	 * @param group The group whose knobs are being displayed in the JList
	 */
	public void setGroup( final KnobGroup group ) {
		if ( group != null ) {
			group.removeKnobGroupListener( this );
		}
		
		_group = group;
		if ( group != null ) {
			group.addKnobGroupListener( this );
		}
		
		fireContentsChanged( this, 0, getSize() );
	}
	
	
	/**
	 * Get the group whose knobs are being displayed in the JList using this model
	 * @return the group which supplies the knobs to display
	 */
	public KnobGroup getGroup() {
		return _group;
	}
	
	
	/**
	 * Get the number of knobs to display
	 * @return the number of knobs in the group
	 */
	public int getSize() {
		return ( _group != null ) ? _group.getKnobCount() : 0;
	}
	
	
	/**
	 * Get the string to display for the knob at the specified index in the knobs list.
	 * @param index The index of the knob in the JList
	 * @return The label of the knob
	 * @see #getKnobLabel
	 */
	public String getElementAt( final int index ) {
		return  ( _group != null ) ? getKnobLabel( index ) : "";
	}
	
	
	/**
	 * Get the string to display for the knob.  If the knob has a valid file path to the executable,
	 * the label appears in standard black.  If the path is invalid, we use the warning label instead.
	 * @see #warningLabel
	 */
	protected String getKnobLabel( final int index ) {
		final Knob knob = _group.getKnob( index );
		final String label = knob.getName();
		return label; 
	}
	
	
	/**
	 * Change the color of the label to red.
	 * @param label The plain label of the knob.
	 * @return HTML which renders the label as red text
	 */
	static protected String warningLabel( final String label ) {
		return "<html><body><font COLOR=#ff0000>" + label + "</font></body></html>";
	}
	
	
	/**
	 * Handle the event indicating that knobs have been added to the group
	 * @param group The group to which the knobs have been added
	 * @param addedKnobs The collection of added Knob instances
	 */
	public void knobsAdded( final KnobGroup group, final Collection<Knob> addedKnobs ) {
		fireContentsChanged( this, 0, getSize() );
	}
	
	
	/**
	 * Handle the event indicating that knobs have been removed from the group
	 * @param group The group from which the knobs have been removed
	 * @param removedKnobs The collection of removed Knob instances
	 */
	public void knobsRemoved( final KnobGroup group, final Collection<Knob> removedKnobs ) {
		fireContentsChanged( this, 0, getSize() );
	}
	
	
	/**
	 * The group's label has been changed
	 * @param group the group whose label has been changed
	 */
	public void labelChanged( final KnobGroup group ) {}
	
	
	/**
	 * Indicates that a knob has been modified.
	 * @param group the group whose knob has been modified
	 * @param knob the knob which has been modified
	 */
	public void knobModified( KnobGroup group, Knob knob ) {}
	
	
	/**
	 * Handle the event indicating that the knobs controller has a new selected group.  Change the
	 * group used for managing the Knob list to the new selected group.
	 * @param source The knobs controller
	 * @param newSelectedGroup The new selected group
	 */
	public void selectedGroupChanged( final KnobsController source, final KnobGroup newSelectedGroup ) {
		setGroup( newSelectedGroup );
	}
	
	
	/**
	 * Handle the event indicating that the knobs controller has a new selected knob.  This event is ignored.
	 * @param source The knobs controller
	 * @param newSelectedKnobs the new selected knobs
	 */
	public void selectedKnobsChanged( final KnobsController source, final List<Knob> newSelectedKnobs ) {}	
}
