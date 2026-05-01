/*
 *  SearchAlgorithm.java
 *
 *  Created Wednesday June 9, 2004 2:35 pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver.algorithm;

import xal.tools.messaging.MessageCenter;

import xal.extension.solver.*;
import xal.extension.solver.solutionjudge.*;
import xal.extension.solver.market.*;

import java.util.*;

/**
 * Abstract super class for an optimization search algorithm.
 *
 * @author   ky6
 */
public abstract class SearchAlgorithm implements AlgorithmScheduleListener, SolutionJudgeListener {
    
    /** comparator for sorting based on efficiency */
    public final static Comparator<SearchAlgorithm> EFFICIENCY_COMPARATOR = makeEfficiencyComparator();
    
    /** the message center for dispatching messages */
    final private MessageCenter MESSAGE_CENTER;
    
    /** the proxy for forwarding messages to registered listeners */
    final private SearchAlgorithmListener EVENT_PROXY;
    
    /** the minimum evaluations that an algorithm can choose to exicute */
    private int _proposedEvaluations = 1;
    
    /** measure of this algorithm's running efficiency in solving the problem */
    private double _efficiency = 1.0;
    
    /** the number of evaluations remaining to be run */
    private int _evaluationsLeft; // this is initialized with getRunCount at the beginning of the algorithm evaluations
    
    /** the schedule for the current algorithm */
    private AlgorithmSchedule _schedule;
    
	/** the problem to solve */
	protected Problem _problem;
    


	/** Empty constructor. */
	public SearchAlgorithm() {
		MESSAGE_CENTER = new MessageCenter( "Search Algorithm" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, SearchAlgorithmListener.class );
	}
	
	
	/** Assign a new problem. */
	public void setProblem( final Problem problem ) {
		if ( _problem != problem ) {
			_problem = problem;
		}
	}
	
	
	/** Reset this algorithm. */
	public void reset() {
	}
    
    
    /**
     * Execute an algorithm run (typically many evaluations up to those proposed)
     * @param schedule the schedule requesting the algorithm execution
     * @param scoreboard the scoreboard of solver status
     */
    final public void executeRun( final AlgorithmSchedule schedule, final ScoreBoard scoreboard ) {
        final double initialSatisfaction = ( scoreboard.getBestSolution() != null ) ? scoreboard.getBestSolution().getSatisfaction() : 0.0;
        
        _schedule = schedule;
        _evaluationsLeft = getNearestIntegerInRange( _proposedEvaluations, getMinEvaluationsPerRun(), getMaxEvaluationsPerRun() );
        int initialCount = _evaluationsLeft;
        
        performRun(schedule); // starts the algorithm
        
        int evaluations = initialCount - _evaluationsLeft; // this is the amount of evaluations that were executed
        
        updateEfficiency( initialSatisfaction, scoreboard.getBestSolution().getSatisfaction(), evaluations );
    }
    

    /**
     * Get the nearest integer to the specified target that is within the specified range.
     * @param target the target that we are trying to meet
     * @param lower the minimum value of the allowed range
     * @param upper the maximum value of the allowed range
     * @return the nearest integer to the target, but within the specified range
     */
    static private int getNearestIntegerInRange( final int target, final int lower, final int upper ) {
        return ( target > upper ) ? upper : ( ( target < lower ) ? lower : target );
    }
    
    
    /**
     * Compute and update the efficiency of this algorithm's run.
     * @param initialSatisfaction the satisfaction at the start of the run
     * @param endSatisfaction the satisfaction at the end of the run
     * @param evaluations the number of evaluations run
     */
    private void updateEfficiency( final double initialSatisfaction, final double endSatisfaction, final int evaluations ) {
        if ( evaluations > 0 && initialSatisfaction < 1.0 ) {
            // rescale the satisfaction based on what can be achieved i.e. at most the satisfaction can be 1.0 and charge for evaluations
            final double efficiency = ( endSatisfaction - initialSatisfaction ) / ( ( 1.0 - initialSatisfaction ) * evaluations );
            _efficiency = 0.75 * Math.max( efficiency, 0.0 ) + 0.25 * _efficiency;		// weight the new efficiency against the original efficiency
            
            //System.out.printf("algorithm: %40s, isat: %7.3f, fsat: %7.3f, evaluations: %7d, efficiency: %7.3f\n", getLabel(), initialSatisfaction, endSatisfaction, evaluations, _efficiency);
        }
    }
    
    /**
     * Generate a new efficiency comparator which reverse sorts the algorithms according to efficiency and falls back to algorithm label for deterministic behavior should the efficiencies be degenerate (e.g. before any evaluations).
     * @return a new efficiency comparator
     */
    private static Comparator<SearchAlgorithm> makeEfficiencyComparator() {
        return new Comparator<SearchAlgorithm>() {
            public int compare( final SearchAlgorithm algorithmA, final SearchAlgorithm algorithmB ) {
                final double efficiencyA = algorithmA.getEfficiency();
                final double efficiencyB = algorithmB.getEfficiency();
                return efficiencyA < efficiencyB ? 1 : ( efficiencyA > efficiencyB ? -1 : algorithmA.getLabel().compareTo( algorithmB.getLabel() ) );
            }
        };
    }
    
    
    /**
     * Evaluate the given trial point.
     * @param trialPoint the trial point to evaluate
     * @return the scored trial
     */
    public Trial evaluateTrialPoint( final TrialPoint trialPoint ) {
        --_evaluationsLeft;
        //System.out.println("evaluating trial point for count: " + _evaluationsLeft + " using " + _algorithm.getLabel());
        return _schedule.evaluateTrialPoint( this, trialPoint );
    }


	/**
	 * Get the label for this search algorithm.
	 * @return   The label for this algorithm
	 */
	public abstract String getLabel();


	/**
	 * Calculate the next few trial points.
	 */
	public abstract void performRun(AlgorithmSchedule algorithmSchedule);


    /** get the amount of evaluations left to run for this algorithm */
    public int getEvaluationsLeft(){
        return _evaluationsLeft;
    }
    
	/**
	 * Get the minimum number of evaluations per run.  Subclasses may want to override this method.
	 * @return the minimum number of evaluation per run.
	 */
	public int getMinEvaluationsPerRun() {
		return 1;
	}
	
	
	/**
	 * Get the maximum number of evaluations per run.  Subclasses may want to override this method.
	 * @return the maximum number of evaluation per run.
	 */
	public int getMaxEvaluationsPerRun() {
		return Integer.MAX_VALUE;
	}

    /**
     * Sets that proposed minimum evaluations for an algorithm
     */
    public void setProposedEvaluations(int proposedEvaluations){
        _proposedEvaluations = proposedEvaluations;
    }

    
    /**
     * Get this algorithm's efficiency for improving satisfaction.
     * @return the efficiency
     */
    final public double getEfficiency() {
        return _efficiency;
    }
	

	/**
	 * Get the rating for this algorithm which in an integer between 0 and 10 and indicates how well this algorithm
	 * performs on global searches.
	 * @return   The global search rating for this algorithm.
	 */
	abstract int globalRating();


	/**
	 * Get the rating for this algorithm which in an integer between 0 and 10 and indicates how well this algorithm
	 * performs on local searches.
	 * @return   The local search rating for this algorithm.
	 */
	abstract int localRating();

    /**
     * Add a search algorithm listener.
     * @param listener  The listener to add.
     */
    public void addSearchAlgorithmListener( final SearchAlgorithmListener listener ) {
        MESSAGE_CENTER.registerTarget( listener, this, SearchAlgorithmListener.class );

		// immediately post whether this algorithm is available
        if ( getMinEvaluationsPerRun() > 0 ) {
            listener.algorithmAvailable( this );
        }
        else {
            listener.algorithmUnavailable( this );
        }
    }
    
    
    /**
     * Remove a search algorithm listener.
     * @param listener  The listener to remove.
     */
    public void removeSearchAlgorithmListener( final SearchAlgorithmListener listener ) {
        MESSAGE_CENTER.removeTarget( listener, this, SearchAlgorithmListener.class );
    }

	/**
	 * Handle a message that a trial has been scored.
	 * @param schedule              Description of the Parameter
	 * @param trial                 Description of the Parameter
	 */
	public void trialScored( final AlgorithmSchedule schedule, Trial trial ) { }


	/**
	 * Handle a message that a trial has been vetoed.
	 * @param schedule              Description of the Parameter
	 * @param trial                 Description of the Parameter
	 */
	public void trialVetoed( final AlgorithmSchedule schedule, final Trial trial ) { }
	
	
	/**
	 * Handle an event where a new algorithm run stack will start.
	 * @param schedule the schedule posting the event
	 * @param algorithm the algorithm which will execute
	 * @param scoreBoard the scoreboard
	 */
	public void algorithmRunWillExecute( final AlgorithmSchedule schedule, final SearchAlgorithm algorithm, final ScoreBoard scoreBoard ) {}
	
	
	/**
	 * Handle an event where a new algorithm run stack has completed.
	 * @param schedule the schedule posting the event
	 * @param algorithm the algorithm that has executed
	 * @param scoreBoard the scoreboard
	 */
	public void algorithmRunExecuted( final AlgorithmSchedule schedule, final SearchAlgorithm algorithm, final ScoreBoard scoreBoard ) {}
	

	/**
	 * Send a message that a new optimal solution has been found.
	 * @param source     The source of the new optimal solution.
	 * @param solutions  The list of solutions.
	 * @param solution   The new optimal solution.
	 */
	public void foundNewOptimalSolution( final SolutionJudge source, final List<Trial> solutions, final Trial solution ) { }
}

