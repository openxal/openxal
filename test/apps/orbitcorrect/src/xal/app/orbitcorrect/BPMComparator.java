/*
 * BPMComparator.java
 *
 * Created on Mon Oct 04 12:56:36 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import xal.smf.*;
import xal.smf.impl.*;

import java.util.*;

/**
 * BPMComparator
 *
 * @author  tap
 * @since Oct 04, 2004
 */
public class BPMComparator implements Comparator<BpmAgent> {
	/** accelerator sequence used to determine the BPM positions */
	final protected AcceleratorSeq _sequence;
	
	
	/**
	 * Constructor
	 */
	public BPMComparator( final AcceleratorSeq sequence ) {
		_sequence = sequence;
	}
	
	
	/**
	 * Compare two BPM based upon the position of each BPM
	 * within the selected sequence.
	 *
	 * @param  bpmAgent1  the first BPM agent in the comparison
	 * @param  bpmAgent2  the second BPM agent in the comparison
	 * @return       0 for equal positions, -1 if the first position is greater than the second and 1 otherwise.
	 */
	public int compare( final BpmAgent bpmAgent1, final BpmAgent bpmAgent2 ) {
		double position1 = bpmAgent1.getPositionIn( _sequence );
		double position2 = bpmAgent2.getPositionIn( _sequence );

		return ( position1 == position2 ) ? 0 : ( ( position1 > position2 ) ? 1 : -1 );
	}


	/**
	 * Test whether the specified comparator is equal to this instance.
	 *
	 * @param  comparator  the comparator to compare for equality with this instance
	 * @return             true if the comparator is equal to this instance and false if not
	 */
	public boolean equals( final Object comparator ) {
		return comparator == this;
	}


	/** Override hashCode as required for consistency with equals() */
	public int hashCode() {
		return super.hashCode();
	}
}

