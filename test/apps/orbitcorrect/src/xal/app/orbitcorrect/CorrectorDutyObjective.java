/*
 *  CorrectorDutyObjective.java
 *
 *  Created on Fri Oct 01 09:04:31 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;


/**
 * CorrectorDutyObjective
 *
 * @author   tap
 * @since    Oct 01, 2004
 */
public class CorrectorDutyObjective extends OrbitObjective {
	/** Constructor  */
	public CorrectorDutyObjective() {
		super( "Corrector Duty" );
	}


	/**
	 * Determines how satisfied the user is with the specified value for this objective.
	 * @param dutyScore  The duty score
	 * @return      the user satisfaction for the specified value
	 */
	public double satisfaction( final double dutyScore ) {
		return 1 - dutyScore;
	}


	/**
	 * Calculate the mean square of the fractional corrector strengths with respect to
	 * the associated corrector limits.
	 *
	 * @param orbit         the orbit to measure for RMS displacement
	 * @param distribution  the corrector distribution used for the orbit
	 * @return              the mean square corrector duty
	 */
	public double score( final Orbit orbit, final CorrectorDistribution distribution ) {
		return distribution.worstBiasedLoad();
	}
}

