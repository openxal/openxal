/*
 * SmoothnessObjective.java
 *
 * Created on Wed Oct 13 09:51:48 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;


/**
 * SmoothnessObjective
 * @author  tap
 * @since Oct 13, 2004
 */
public class SmoothnessObjective extends OrbitObjective {
	/** orbit plane index */
	protected final int _planeIndex;

	/** scale of orbit angle used for calculating satisfaction */
	protected double _angleScale;


	/**
	 * Primary Constructor
	 * @param planeIndex  identifier of the x or y plane
	 * @param angleScale  the scale against which to measure angle errors
	 */
	public SmoothnessObjective( final int planeIndex, final double angleScale ) {
		super( generateName( planeIndex ) );

		_planeIndex = planeIndex;
		_angleScale = ( angleScale != 0.0 ) ? angleScale : 1.0;		// make sure the scale is non-zero
	}

	
	/**
	 * Constructor with the default angle scale of 1.0
	 * @param planeIndex  identifier of the x or y plane
	 */
	public SmoothnessObjective( final int planeIndex ) {
		this( planeIndex, 1.0 );
	}


	/** Get the angle scale */
	public double getAngleScale() {
		return _angleScale;
	}


	/** Set the angle scale */
	public void setAngleScale( final double angleScale ) {
		_angleScale = angleScale;
	}


	/**
	 * Determines how satisfied the user is with the specified value for this objective.
	 * @param rmsAngle  The RMS angle displacement
	 * @return the user satisfaction for the specified value
	 */
	public double satisfaction( final double rmsAngle ) {
		final double error = Math.abs( rmsAngle / _angleScale );
		return 1.0 / ( 1.0 + 100.0 * error * error );
	}


	/**
	 * Calculate the RMS angle score for the specified orbit.
	 * @param orbit         the orbit to measure for RMS angle
	 * @param distribution  the corrector distribution used for the orbit
	 * @return              the orbit's RMS angle score
	 */
	public double score( final Orbit orbit, final CorrectorDistribution distribution ) {
		return orbit.getOrbitPlane( _planeIndex ).rmsAngle();
	}


	/**
	 * Generate an objective name for the specified orbit plane.
	 * @param planeIndex  the index of the orbit plane
	 * @return  an objective name for the specified orbit plane
	 */
	protected static String generateName( final int planeIndex ) {
		final String prefix = ( planeIndex == Orbit.X_PLANE ) ? "X " : "Y ";
		return prefix + " RMS Angle";
	}
}

