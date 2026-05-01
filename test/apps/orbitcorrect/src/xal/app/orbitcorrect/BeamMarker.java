//
//  BeamMarker.java
//  xal
//
//  Created by Tom Pelaia on 1/4/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;


import xal.smf.*;
import xal.ca.*;
import xal.ca.correlator.*;
import xal.tools.correlator.*;
import xal.tools.messaging.MessageCenter;

import java.util.*;


/** represents an item at which we wish to have beam displacement data */
public class BeamMarker<NodeType extends AcceleratorNode> {
	/** node for which this marks */
	final protected NodeType NODE;
	
	
	/**
	 * Primary constructor
	 * @param node	the accelerator node
	 */
	public BeamMarker( final NodeType node ) {
		NODE = node;
	}
	
	
	/**
	 * Get the node's ID.
	 * @return the unique node ID
	 */
	public String getID() {
		return NODE.getId();
	}


	/**
	 * Get the node managed by this marker
	 * @return   the node managed by this marker
	 */
	public NodeType  getNode() {
		return NODE;
	}
	

	/**
	 * Get the position of the node relative to the start of the specified sequence.
	 * @param sequence  The sequence relative to which the node's position is measured
	 * @return          the position of the node relative to the sequence in meters
	 */
	public double getPositionIn( final AcceleratorSeq sequence ) {
		return sequence.getPosition( NODE );
	}


	/**
	 * Get the string representation of the BPM.
	 * @return   the BPM's string representation
	 */
	public String toString() {
		return NODE.toString();
	}
	
	
	/**
	 * Get a comparator that compares beam markers by position relative to the specified sequence
	 * @param sequence relative to which positions are measured
	 * @return comparator based on relative position within a sequence
	 */
	static public Comparator<BeamMarker<?>> getPositionComparator( final AcceleratorSeq sequence ) {
		return new Comparator<BeamMarker<?>>() {
			/**
			 * Compare two markers based upon the position of each marker within the selected sequence.
			 * @param  marker1  the first marker in the comparison
			 * @param  marker2  the second marker in the comparison
			 * @return 0 for equal positions, -1 if the first position is greater than the second and 1 otherwise.
			 */
			public int compare( final BeamMarker<?> marker1, final BeamMarker<?> marker2 ) {
				double position1 = marker1.getPositionIn( sequence );
				double position2 = marker2.getPositionIn( sequence );
				
				return ( position1 == position2 ) ? 0 : ( ( position1 > position2 ) ? 1 : -1 );
			}
			
			
			/**
			 * Test whether the specified comparator is equal to this instance.
			 * @param  comparator  the comparator to compare for equality with this instance
			 * @return true if the comparator is equal to this instance and false if not
			 */
			public boolean equals( final Object comparator ) {
				return comparator == this;
			}					
		};
	}
}
