//
//  WorstObjectiveBiasedJudge.java
//  xal
//
//  Created by Thomas Pelaia on 6/15/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.solver.solutionjudge;

import xal.tools.messaging.MessageCenter;

import xal.tools.solver.*;

import java.util.*;


/**
 * This judge weights the least satisfied objective most and each subsequent one half less than the prior one.
 * Each objective satisfaction must be based on a range of 0 to 1.
 * @author t6p
 */
public class WorstObjectiveBiasedJudge extends SolutionJudge {
	/** the bias weight */
	final protected double BIAS_WEIGHT;
	
	/** the current best satisfaction */
	protected double _bestSatisfaction;
	
	/** used to normalize the total satisfaction to a scale of 0 to 1 */
	protected double _totalWeight;
	
	/** the current list of the most optimal solutions */
	protected List _optimalSolutions;
	
	
	/** Constructor */
	public WorstObjectiveBiasedJudge() {
		this( 0.25 );
	}
	
	
	/** Constructor */
	public WorstObjectiveBiasedJudge( final double biasWeight ) {
		BIAS_WEIGHT = biasWeight;
		_bestSatisfaction = 0.0;
		_totalWeight = 0.0;
		_optimalSolutions = new ArrayList();
	}
	
	
	/**
	 * Reset the satisfaction sum judge.
	 */
	public void reset() {
		_bestSatisfaction = 0.0;
		_totalWeight = 0.0;
		_optimalSolutions = new ArrayList();
	}
	
	
	/**
	 * Get the optimal solutions.
	 * @return a list of solutions
	 */
	public List getOptimalSolutions() {
		return _optimalSolutions;	 	 
	}
	
	
	/**
	 * Judge the trial.
	 * @param trial The trial with which to update the solution judge.
	 */
	public void judge( final Trial trial ) {
		final List objectives = trial.getProblem().getObjectives();
		final int numObjectives = objectives.size();
		final List satisfactions = new ArrayList( numObjectives );
		
		// collect the list of each satisfaction
		final Iterator objectiveIter = objectives.iterator();		 
		while( objectiveIter.hasNext() ) {
			final Objective objective = (Objective)objectiveIter.next();
			final double satisfaction = trial.getScore( objective ).getSatisfaction();
			satisfactions.add( new Double( satisfaction ) );
		}
		Collections.sort( satisfactions );		// sort satisfactions from worst to best
		
		// weight each satisfaction with most weight for the worst satisfaction and exponentially decreasing from there
		double weightedSum = 0.0;
		double weight = 1.0;
		final Iterator satisfactionIter = satisfactions.iterator();
		while ( satisfactionIter.hasNext() ) {
			final double satisfaction = ((Double)satisfactionIter.next()).doubleValue();
			weightedSum += weight * satisfaction;
			weight *= BIAS_WEIGHT;	// weight the worst satisfactions most
		}
		
		// make sure we do this at least once and then cache it
		if (  _totalWeight == 0.0 ) {
			_totalWeight = ( 1.0 - Math.pow( BIAS_WEIGHT, numObjectives ) ) / ( 1.0 - BIAS_WEIGHT );
		}
		
		// generate the overall satisfaction which is scaled from 0 to 1
		final double totalSatisfaction = weightedSum / _totalWeight;
		trial.setSatisfaction( totalSatisfaction );
		
		if( totalSatisfaction == _bestSatisfaction ) {
			_optimalSolutions.add( trial );
			_eventProxy.foundNewOptimalSolution( this, _optimalSolutions, trial );
		}
		else if( totalSatisfaction > _bestSatisfaction ) {
			_bestSatisfaction = totalSatisfaction;
			_optimalSolutions.clear();
			_optimalSolutions.add( trial );
			_eventProxy.foundNewOptimalSolution( this, _optimalSolutions, trial );
		}
	}
}



