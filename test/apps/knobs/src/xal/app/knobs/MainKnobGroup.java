//
//  MainKnobGroup.java
//  xal
//
//  Created by Thomas Pelaia on 11/1/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.tools.data.*;
import xal.tools.xml.XmlDataAdaptor;

import java.util.*;


/** Main group for holding all knobs */
public class MainKnobGroup extends KnobGroup {
	/** The label for this group */
	static public final String LABEL = "All";
	
	/** The DataAdaptor label for reading and writing the main group */
	static public final String DATA_LABEL = "MainKnobGroup"; 
	
	
	/** Constructor */
	public MainKnobGroup() {
		this( new ArrayList<Knob>() );
	}
	
	
	/**
	 * Primary constructor
	 * @param knobs The list of knobs in this main group.
	 */
	public MainKnobGroup( final List<Knob> knobs ) {
		super( LABEL, knobs );
	}
	
	
	/**
	 * Get the knob with the specified ID
	 * @param ID The ID of the knob to get
	 * @return The knob with the specified ID
	 */
	synchronized public Knob getKnobWithID( final long ID ) {
		return _knobs.get( (int)ID );
	}
    
    
    /** 
	* dataLabel() provides the name used to identify the class in an 
	* external data source.
	* @return The tag for this data node.
	*/
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /**
	 * Instructs the receiver to update its data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update( final DataAdaptor adaptor ) {
    }
    
    
    /**
	 * Instructs the receiver to write its data to the adaptor for external storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.writeNodes( _knobs );
    }
	
	
	/** Sort the knobs alphabetically */
	synchronized protected void sortKnobs() {
		super.sortKnobs();
		
		long index = 0;
		for ( Knob knob : _knobs ) {
			knob.setID( index++ );
		}
	}
	
	
	/**
	 * This method overrides the superclass to do nothing since one cannot edit the main group.
	 * @param label The new label
	 */
	public void setLabel( final String label ) {}
	
	
	/**
	 * Determine if the group allows editing of its label.  The MainGroup does not allow editing of its label.
	 * @return false
	 */
	public boolean allowsLabelEdit() {
		return false;
	}
	
	
	/**
	 * Compare two instances.  The  main group always appears ahead of other groups.
	 * @param group the other group against which to compare
	 * @return a positive number if this comes after the argument, negative if this comes before and 0 if they are equal
	 */
	public int compareTo( final KnobGroup group ) {
		return group instanceof MainKnobGroup ? super.compareTo( group ) : -1;
	}	
}
