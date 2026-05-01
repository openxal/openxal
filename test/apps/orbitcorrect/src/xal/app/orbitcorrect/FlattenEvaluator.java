/*
 *  FlattenEvaluator.java
 *
 *  Created on Tue Sep 28 15:33:17 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.extension.solver.*;
import xal.smf.*;
import xal.smf.impl.*;

import java.util.*;


/**
 * FlattenEvaluator is an evaluator module for optimizing the orbit. The evaluator scores the
 * set of trial correctors based upon parameters set by the user. The overall score is a
 * weighted sum of the satisfaction for the predicted orbit error, angle error and corrector
 * duty factor.
 *
 * @author   tap
 * @since    Sep 28, 2004
 */
public class FlattenEvaluator implements Evaluator {
	/** Description of the Field */
	protected final Orbit _initialOrbit;
	/** Description of the Field */
	protected final CorrectorDistribution _initialCorrectorDistribution;
	/** Description of the Field */
	protected final MachineSimulator _simulator;
	/** map of corrector supply variables keyed by corrector supply */
	protected final Map<CorrectorSupply,Variable> _correctorVariables;
	/** trial corrector distribution */
	protected final MutableCorrectorDistribution _trialCorrectorDistribution;
	/** trial orbit */
	protected Orbit _trialOrbit;

	/**
	 * Primary Constructor
	 *
	 * @param orbit                         Description of the Parameter
	 * @param initialCorrectorDistribution  Description of the Parameter
	 * @param simulator                     Description of the Parameter
	 * @param correctorVariables            Description of the Parameter
	 */
	public FlattenEvaluator( final Orbit orbit, final CorrectorDistribution initialCorrectorDistribution, final MachineSimulator simulator, final Map<CorrectorSupply,Variable> correctorVariables ) {
		_initialOrbit = orbit;
		_initialCorrectorDistribution = initialCorrectorDistribution;
		_simulator = simulator;
		_correctorVariables = correctorVariables;

		_trialCorrectorDistribution = new MutableCorrectorDistribution();
	}
	
	
	/**
	 * Get the initial orbit.
	 */
	public Orbit getInitialOrbit() {
		return _initialOrbit;
	}
	
	
	/**
	 * Get the initial corrector distribution.
	 */
	public CorrectorDistribution getInitialCorrectorDistribution() {
		return _initialCorrectorDistribution;
	}
	
	
	/**
	 * Get the latest trial corrector distribution.
	 */
	public CorrectorDistribution getTrialCorrectorDistribution() {
		return _trialCorrectorDistribution;
	}
	
	
	/**
	 * Get the latest trial orbit.
	 */
	public Orbit getTrialOrbit() {
		return _trialOrbit;
	}


	/**
	 * Score the trial.
	 *
	 * @param trial  The trial to evaluate.
	 */
	public void evaluate( final Trial trial ) {
		final TrialPoint trialPoint = trial.getTrialPoint();

		updateCorrectorDistribution( _trialCorrectorDistribution, trialPoint );
		_trialOrbit = _simulator.predictOrbit( _initialOrbit, _initialCorrectorDistribution, _trialCorrectorDistribution );

		final Iterator<Objective> objectiveIter = trial.getProblem().getObjectives().iterator();
		while ( objectiveIter.hasNext() ) {
			OrbitObjective objective = (OrbitObjective)objectiveIter.next();
			double score = objective.score( _trialOrbit, _trialCorrectorDistribution );
			trial.setScore( objective, score );
		}
	}


	/**
	 * Generate a corrector distribution from the specified trial point.
	 *
	 * @param trialPoint  the trial point from which to get the trial corrector strengths
	 */
	public void updateCorrectorDistribution( final MutableCorrectorDistribution trialCorrectorDistribution, final TrialPoint trialPoint ) {
		final Date timestamp = new Date();

		trialCorrectorDistribution.clear();

		final Iterator<Map.Entry<CorrectorSupply, Variable>> correctorEntryIter = _correctorVariables.entrySet().iterator();
		while ( correctorEntryIter.hasNext() ) {
			final Map.Entry<CorrectorSupply, Variable> entry = correctorEntryIter.next();
			final CorrectorSupply supply = entry.getKey();
			final double field = trialPoint.getValue( entry.getValue() );

			trialCorrectorDistribution.addRecord( new CorrectorRecord( supply, timestamp, field ) );
		}
	}
}

