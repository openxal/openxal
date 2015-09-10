//
//  OpticsEvaluator.java
//  xal
//
//  Created by Thomas Pelaia on 2/18/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.extension.solver.*;
import xal.extension.solver.algorithm.*;

import java.util.*;


/** Class for evaluating an optics. */
public class OpticsEvaluator implements Evaluator {
	/** The simulator used to run the online model with trial points */
	final OnlineModelSimulator _simulator;
	
	
	/**
	 * Constructor
	 */
	public OpticsEvaluator( final OnlineModelSimulator simulator ) {
		_simulator = simulator;
	}
	
	
	/**
	 * Get the simulator.
	 */
	public OnlineModelSimulator getSimulator() {
		return _simulator;
	}
	
	
	/**
	 * Score the trial.
	 * @param trial  The trial to evaluate.
	 */
	public void evaluate( final Trial trial ) {
		final Map<Variable, Number> trialValues = trial.getTrialPoint().getValueMap();
		final Simulation designSimulation = _simulator.getDesignSimulation();
		final Simulation simulation = _simulator.runWithVariables( trialValues );

		if ( simulation != null ) {
			for ( final Objective rawObjective : trial.getProblem().getObjectives() ) {
				final OpticsObjective objective = (OpticsObjective)rawObjective;
				final double score = objective.getValue( trialValues, simulation, designSimulation );
				trial.setScore( objective, score );
			}

			trial.setCustomInfo( simulation );
		}
		else {
			trial.vetoTrial( new TrialVeto( trial, null, "Model Exception" ) );
		}
	}
}
