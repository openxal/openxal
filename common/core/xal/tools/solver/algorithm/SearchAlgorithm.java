/*
 *  SearchAlgorithm.java
 *
 *  Created Wednesday June 9, 2004 2:35 pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.tools.solver.algorithm;

import xal.tools.messaging.MessageCenter;

import xal.tools.solver.*;
import xal.tools.solver.solutionjudge.*;
import xal.tools.solver.market.*;

import java.util.*;

/**
 * Abstract super class for an optimization search algorithm.
 *
 * @author   ky6
 */
public abstract class SearchAlgorithm implements AlgorithmScheduleListener, SolutionJudgeListener {
	/** the problem to solve */
	protected Problem _problem;
	
	/** the message center for dispatching messages */
	protected MessageCenter _messageCenter;
	
	/** the proxy for forwarding messages to registered listeners */
	protected SearchAlgorithmListener _eventProxy;


	/** Empty constructor. */
	public SearchAlgorithm() {
		_messageCenter = new MessageCenter( "Search Algorithm" );
		_eventProxy = (SearchAlgorithmListener)_messageCenter.registerSource( this, SearchAlgorithmListener.class );
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
	 * Add a search algorithm listener.
	 * @param listener  The listener to add.
	 */
	public void addSearchAlgorithmListener( final SearchAlgorithmListener listener ) {
		_messageCenter.registerTarget( listener, this, SearchAlgorithmListener.class );
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
		_messageCenter.removeTarget( listener, this, SearchAlgorithmListener.class );
	}


	/**
	 * Get the label for this search algorithm.
	 * @return   The label for this algorithm
	 */
	public abstract String getLabel();


	/**
	 * Calculate the next few trial points.
	 * @param algorithmRun the algorithm run to perform the evaluation
	 */
	public abstract void performRun( AlgorithmRun algorithmRun );
	
	
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
	 * @param strategy the strategy which will execute
	 * @param scoreBoard the scoreboard
	 */
	public void strategyWillExecute( final AlgorithmSchedule schedule, final AlgorithmStrategy strategy, final ScoreBoard scoreBoard ) {}
	
	
	/**
	 * Handle an event where a new algorithm run stack has completed.
	 * @param schedule the schedule posting the event
	 * @param strategy the strategy that has executed
	 * @param scoreBoard the scoreboard
	 */
	public void strategyExecuted( final AlgorithmSchedule schedule, final AlgorithmStrategy strategy, final ScoreBoard scoreBoard ) {}
	

	/**
	 * Send a message that a new optimal solution has been found.
	 * @param source     The source of the new optimal solution.
	 * @param solutions  The list of solutions.
	 * @param solution   The new optimal solution.
	 */
	public void foundNewOptimalSolution( final SolutionJudge source, final List solutions, final Trial solution ) { }
}

