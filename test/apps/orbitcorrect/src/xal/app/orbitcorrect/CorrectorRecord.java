/*
 *  CorrectorRecord.java
 *
 *  Created on Tue Sep 28 13:30:28 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.smf.impl.*;
import xal.smf.*;

import java.util.*;


/**
 * CorrectorRecord
 * @author   tap
 * @since    Sep 28, 2004
 */
public class CorrectorRecord {
	/** the corrector supply for which the record holds data */
	final protected CorrectorSupply _correctorSupply;
	
	/** the time when the data was taken */
	final protected Date _timestamp;
	
	/** the recorded field of the corrector */
	protected double _field;


	/**
	 * Primary constructor
	 * @param supply  the corrector supply for which the record is taken
	 * @param timestamp  the time when the data was taken
	 * @param field   the recorded field of the corrector
	 */
	public CorrectorRecord( final CorrectorSupply supply, final Date timestamp, final double field ) {
		_correctorSupply = supply;
		_timestamp = timestamp;
		_field = field;
	}


	/**
	 * Get the corrector node.
	 * @return   the corrector node.
	 */
	public final CorrectorSupply getCorrectorSupply() {
		return _correctorSupply;
	}


	/**
	 * Get the recorded corrector field.
	 * @return   the recorded corrector field
	 */
	public final double getField() {
		return _field;
	}


	/**
	 * Set the corrector's recorded field.
	 * @param field  the field to record
	 */
	public final void setField( final double field ) {
		_field = field;
	}


	/**
	 * Get the position of the corrector relative to the start of the specified sequence.
	 * @param sequence  The sequence relative to which the corrector's position is measured
	 * @return          the position of the corrector relative to the sequence in meters
	 */
//	public double getPositionIn( final AcceleratorSeq sequence ) {
//		return sequence.getPosition( _corrector );
//	}


	/**
	 * Determine whether the corrector is horizontal or vertical.
	 * @return   MagnetType.HORIZONTAL for a horizontal corrector and MagnetType.VERTICAL for a vertical one
	 */
	public int getOrientation() {
		return _correctorSupply.getOrientation();
	}


	/**
	 * Generate a string representation of this record
	 * @return   a string representation of this record
	 */
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append( _correctorSupply.getID() );
		buffer.append( ", timestamp: " + _timestamp );
		buffer.append( ", field: " + _field );

		return buffer.toString();
	}
}

