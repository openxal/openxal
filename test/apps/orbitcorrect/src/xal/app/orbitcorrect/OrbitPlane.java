//
//  OrbitPlane.java
//  xal
//
//  Created by Tom Pelaia on 1/5/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import java.util.*;


/** orbit excursion data for a particular plane */
public class OrbitPlane {
	/** label for the plane */
	final protected String _label;
	
	/** excursions for this plane */
	final protected double[] _avgDisplacements;
	
	/** positions of elements */
	final protected double[] _positions;
	
	
	/** Constructor */
	public OrbitPlane( final String label, final double[] positions, final double[] avgDisplacements ) {
		_label = label;
		_positions = positions;
		_avgDisplacements = avgDisplacements;
	}
	
	
	/**
	 * Get the label.
	 * @return the label for this orbit plane
	 */
	public String getLabel() {
		return _label;
	}
	
	
	/**
	 * Get the array of average orbit values for each BPM.
	 * @return the list of average orbit displacements
	 */
	public double[] getAvgDisplacements() {
		return _avgDisplacements;
	}
	
	
	/**
	 * Get the list of average displacements.
	 * @return the list of average displacements
	 */
	public List<Double> getAvgDisplacementList() {
		final List<Double> displacements = new ArrayList<Double>( _avgDisplacements.length );
		for ( double value : _avgDisplacements ) {
			displacements.add( value );
		}
		return displacements;
	}
	
	
	/**
	 * Get the RMS of the orbit in this plane.
	 * @return the RMS orbit displacement in millimeters
	 */
	public double rmsDisplacement() {
		double squareSum = 0;
		int count = 0;
		for ( int index = 0 ; index < _avgDisplacements.length ; index++ ) {
			final double displacement = _avgDisplacements[index];
			// NaN could be from the raw value or from a channel marked invalid
			if ( !Double.isNaN( displacement ) ) {
				count += 1;
				squareSum += displacement * displacement;
			}
		}
		
		return count > 0 ? Math.sqrt( squareSum / count ) : 0.0;
	}
	
	
	/**
	 * Get the RMS angle of the orbit.
	 * @return the RMS angle of the orbit in milliradians
	 */
	public double rmsAngle() {
		if ( _avgDisplacements.length < 2 )  return 0.0;
		
		double lastPosition = 0.0;					// initial value doesn't matter
		double lastDisplacement = Double.NaN;		// NaN indicates that it hasn't yet been set
		double sumSquareAngle = 0;
		int count = 0;
		for ( int index = 0 ; index < _avgDisplacements.length ; index++ ) {
			final double position = _positions[index];
			final double displacement = _avgDisplacements[index];

			// NaN could be from the raw value or from a channel marked invalid
			if ( !Double.isNaN( displacement ) ) {
				// need a previous point for angle calculation
				if ( !Double.isNaN( lastDisplacement ) ) {
					count += 1;
					final double angle = ( displacement - lastDisplacement ) / ( position - lastPosition );
					sumSquareAngle += angle * angle;
				}
				lastPosition = position;
				lastDisplacement = displacement;
			}			
		}
		
		return count > 0 ? Math.sqrt( sumSquareAngle / count ) : 0.0;
	}
}
