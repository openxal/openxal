/*
 *  DisplacementObjective.java
 *
 *  Created on Wed Sep 29 10:05:16 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.extension.solver.*;

import java.util.*;


/**
 * DisplacementObjective
 * @author   tap
 * @since    Sep 29, 2004
 */
public class DisplacementObjective extends OrbitObjective {
	/** orbit plane index */
	protected final int _planeIndex;

	/** scale of orbit displacement used for calculating satisfaction */
	protected double _orbitScale;

	
	/**
	 * Primary Constructor
	 * @param planeIndex  the index identifying either the x or y axis
	 * @param orbitScale  the scale of the current oribit error
	 */
	public DisplacementObjective( final int planeIndex, final double orbitScale ) {
		super( generateName( planeIndex ) );

		_planeIndex = planeIndex;
		_orbitScale = ( orbitScale != 0.0 ) ? orbitScale : 1.0;		// make sure the scale is non-zero
	}
	
	
	/**
	 * Constructor
	 * @param planeIndex  the index identifying either the x or y axis
	 */
	public DisplacementObjective( final int planeIndex ) {
		this( planeIndex, 1.0 );
	}
	
	
	/**
	 * Set the orbit scale.
	 * @param orbitScale the orbit RMS scale in mm against which to base the objective
	 */
	public void setOrbitScale( final double orbitScale ) {
		_orbitScale = ( orbitScale != 0.0 ) ? orbitScale : 1.0;		// make sure the scale is non-zero
	}
	
	
	/**
	 * Determines how satisfied the user is with the specified value for this objective.
	 * @param score  The orbit displacement score
	 * @return the user satisfaction for the specified value
	 */
	public double satisfaction( final double score ) {
		final double error = score / ( _orbitScale * _orbitScale );
        
        if ( Double.isNaN( error ) ) {
            throw new RuntimeException( "The calculated Satisfaction is Not a Number.\nCheck magnet fields." );
        }
        
		return 1.0 / ( 1.0 + error );
	}
	
	
	/**
	 * Calculate the displacement score for the specified orbit.
	 * @param orbit         the orbit for which to measure the displacement score
	 * @param distribution  the corrector distribution used for the orbit
	 * @return              the orbit's displacement score
	 */
	public double score( final Orbit orbit, final CorrectorDistribution distribution ) {
		return orbit.getOrbitPlane( _planeIndex ).rmsDisplacement();
	}
	
	
	/**
	 * Generate an objective name for the specified orbit plane.
	 * @param planeIndex  the index of the orbit plane
	 * @return            an objective name for the specified orbit plane
	 */
	protected static String generateName( final int planeIndex ) {
		final String prefix = ( planeIndex == Orbit.X_PLANE ) ? "X" : "Y";
		return prefix + " Distortion";
	}
}

