/*
 * CorrectorComparator.java
 *
 * Created on Mon Oct 04 10:49:19 EDT 2004
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
 * Compares two correctors by relative position in the selected sequence.
 *
 * @author  tap
 * @since Oct 04, 2004
 */
public class CorrectorComparator implements Comparator<Dipole> {
	/** accelerator sequence used to get corrector position */
	protected AcceleratorSeq _sequence;
	
	/**
	 * Constructor
	 */
	public CorrectorComparator( final AcceleratorSeq sequence ) {
		_sequence = sequence;
	}
	
	
	/**
	 * Compare two correctors based upon the position of each corrector within the selected
	 * sequence.
	 *
	 * @param corrector1  the first corrector in the comparison
	 * @param corrector2  the second corrector in the comparison
	 * @return            0 for equal positions, -1 if the first position is greater than the
	 *      second and 1 otherwise.
	 */
	public int compare( final Dipole corrector1, final Dipole corrector2 ) {
		final double position1 = _sequence.getPosition( corrector1 );
		final double position2 = _sequence.getPosition( corrector2 );

		return ( position1 == position2 ) ? compareOrientation( corrector1, corrector2 ) : ( ( position1 > position2 ) ? 1 : -1 );
	}
	
	
	/**
	 * Compare orientation ordering arithmetically by orientation so that horizontal and
	 * vertical correctors are treated distinctly.
	 */
	public int compareOrientation( final Dipole corrector1, final Dipole corrector2 ) {
		final int orientation1 = corrector1.getOrientation();
		final int orientation2 = corrector2.getOrientation();
		return ( orientation1 == orientation2 ) ? 0 : ( orientation1 > orientation2 ) ? 1 : -1;
	}


	/**
	 * Test whether the specified comparator is equal to this instance.
	 *
	 * @param comparator  the comparator to compare for equality with this instance
	 * @return            true if the comparator is equal to this instance and false if not
	 */
	public boolean equals( final Object comparator ) {
		return comparator == this;
	}


	/** Override hashCode as required for consistency with equals() */
	public int hashCode() {
		return super.hashCode();
	}
}

