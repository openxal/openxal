/*
 *  Trial.java
 *
 *  Created Wednesday June 9, 2004 2:15pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import xal.extension.solver.algorithm.*;
import xal.extension.solver.constraint.*;
import xal.extension.solver.market.*;

import java.util.*;


/**
 * Trial keeps track of trial points.
 *
 * @author ky6
 * @author t6p
 */
public class Trial {
	/** trial point of variable values */
	protected final TrialPoint _trialPoint;
	
	/** the problem being solved */
	protected final Problem _problem;
	
	/** the algorithm that generated this trial */
	protected final SearchAlgorithm _searchAlgorithm;
	
	/** a veto if any */
	protected TrialVeto _veto;
	
	/** table of objective scores */
	protected final Map<Objective,Score> OBJECTIVE_SCORES;
	
	/** overall satisfaction provided by some solution judges */
	protected double _satisfaction;
	
	/** optional, custom information that an objective or evaluator may choose to store here for convenience */
	protected Object _customInfo;

	
	/**
	 * Primary Constructor.
	 * @param problem              the problem being solved
	 * @param trialPoint           the trial point of variable values
	 * @param algorithm            the algorithm that generated this trial
	 */
	public Trial( final Problem problem, final TrialPoint trialPoint, final SearchAlgorithm algorithm ) {
		_problem = problem;
		_trialPoint = trialPoint;
		_searchAlgorithm = algorithm;
		OBJECTIVE_SCORES = new HashMap<Objective,Score>();
		_veto = null;
	}
	
	
	/**
	 * Constructor.
	 * @param problem              the problem being solved
	 * @param trialPoint           the trial point of variable values
	 */
	public Trial( final Problem problem, final TrialPoint trialPoint ) {
		this( problem, trialPoint, null );
	}
	

	/**
	 * Veto the trial.
	 * @param veto the veto
	 */
	public void vetoTrial( final TrialVeto veto ) {
		_veto = veto;
	}
	
	
	/**
	 * Get the trial veto if any
	 * @return the trial veto or null if there is none
	 */
	public TrialVeto getVeto() {
		return _veto;
	}
	
	
	/**
	 * Determine if this trial has been vetoed
	 * @return true if the trial has been vetoed and false if not
	 */
	public boolean isVetoed() {
		return _veto != null;
	}
	

	/**
	 * Set the scores of a trial point.
	 * @param score  The new score value
	 */
	public void setScore( final Score score ) {
		Objective objective = score.getObjective();
		OBJECTIVE_SCORES.put( objective, score );
	}


	/**
	 * Set the scores of a trial point.
	 * @param objective  The new score value
	 * @param value      The new score value
	 */
	public void setScore( final Objective objective, final double value ) {
		setScore( new Score( objective, value ) );
	}


	/**
	 * Get the score corresponding to the specified objective.
	 * @param objective  Description of the Parameter
	 * @return             The score of the specified objective.
	 */
	public Score getScore( final Objective objective ) {
		return OBJECTIVE_SCORES.get( objective );
	}


	/**
	 * Get the satisfaction for a specific objective.
	 * @param objective  The objective to get.
	 * @return             The satisfaction.
	 */
	public double getSatisfaction( final Objective objective ) {
		final Score score = OBJECTIVE_SCORES.get( objective );
		final double satisfaction = score.getSatisfaction();

		if ( !validateSatisfaction( satisfaction ) ) {
			throw new RuntimeException( "Objective \"" + objective.getName() + "\" has satisfaction of " + satisfaction + " which is outside the accepted range of 0 to 1." );
		}

		return satisfaction;
	}
	
	
	/**
	 * Specify the overall satisfaction of this solution.
	 * @param satisfaction the overall satisfaction of this solution
	 */
	public void setSatisfaction( final double satisfaction ) {
		if ( !validateSatisfaction( satisfaction ) ) {
			throw new IllegalArgumentException( "Attempting to set trial satisfaction to " + satisfaction + " which is outside the accepted range of 0 to 1." );
		}

		_satisfaction = satisfaction;
	}


	/** Validate that the satisfaciton is within the accepted bounds of 0 to 1. */
	static private boolean validateSatisfaction( final double satisfaction ) {
		return satisfaction >= 0 && satisfaction <= 1.0;
	}
	
	
	/**
	 * Get the overall satisfaction which many solution judges provide.
	 * @return the overall satisfaction of this solution
	 */
	public double getSatisfaction()  {
		return _satisfaction;
	}
	

	/**
	 * Get the problem.
	 * @return   The problem.
	 */
	public Problem getProblem() {
		return _problem;
	}


	/**
	 * Get the trial point.
	 * @return   The trial point.
	 */
	public TrialPoint getTrialPoint() {
		return _trialPoint;
	}


	/**
	 * Get the search algorithm that generated this trial.
	 * @return   The search algorithm.
	 */
	public SearchAlgorithm getAlgorithm() {
		return _searchAlgorithm;
	}


	/**
	 * Get the scores keyed by objective
	 * @return   Table of scores keyed by objective.
	 */
	public Map<Objective,Score> getScores() {
		return OBJECTIVE_SCORES;
	}
	
	
	/**
	 * Get optional, custom information (if any) that was provided for convenience.
	 * @return optional, custom information
	 */
	public Object getCustomInfo() {
		return _customInfo;
	}
	
	
	/**
	 * Provide optional, custom information for convenience
	 * @param customInfo the custom information to provide
	 */
	public void setCustomInfo( final Object customInfo ) {
		_customInfo = customInfo;
	}


	/**
	 * A string for displaying a trial. The string consist of a trial point and a score.
	 * @return   The string representation of a trial.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "\nTrial Point: " + _trialPoint + "\n " );
		buffer.append( "Satisfaction: " + _satisfaction + "\n" );
		buffer.append( "Scores: " + OBJECTIVE_SCORES + "\n" );

		return buffer.toString();
	}
}

