/*
 *  ScoreBoard.java
 *
 *  Created Wednesday June 9, 2004 2:32pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.tools.solver;

import xal.tools.messaging.MessageCenter;

import xal.tools.solver.solutionjudge.SolutionJudgeListener;
import xal.tools.solver.solutionjudge.SolutionJudge;
import xal.tools.solver.market.*;

import java.util.*;

/**
 * Scoreboard maintains the status of the solver including the clock and the best solution
 * found so far.
 *
 * @author   ky6
 * @author	t6p
 */
public class ScoreBoard implements AlgorithmScheduleListener, SolutionJudgeListener {
	/** center for broadcasting events */
	final protected MessageCenter _messageCenter;
	
	/** proxy which forwards events to registerd listeners */
	final protected ScoreBoardListener _eventProxy;
	
	/** time when the solver started */
	protected Date _startTime;
	
	/** the best solution found */
	protected Trial _bestSolution;
	
	/** the solution judge */
	protected SolutionJudge _solutionJudge;
	
	/** the number of evaluations performed */
	protected int _evaluations;
	
	/** number of strategy executions */
	private int _strategyExecutions;
	
	/** the number of evaluations that have been vetoed */
	protected int _vetoes;
	
	/** the number of times an optimal soluition was found */
	protected int _optimalSolutionsFound;


	/**
	 * Constructor
	 * @param solutionJudge  the solution judge
	 */
	public ScoreBoard( final SolutionJudge solutionJudge ) {
		_messageCenter = new MessageCenter( "Scoreboard" );
		_eventProxy = (ScoreBoardListener)_messageCenter.registerSource( this, ScoreBoardListener.class );
		
		setSolutionJudge( solutionJudge );
		reset();
	}
	
	
	/**
	 * Add the specified listener as a receiver of ScoreBoard events from this instance. 
	 */
	public void addScoreBoardListener( final ScoreBoardListener listener ) {
		_messageCenter.registerTarget( listener, this, ScoreBoardListener.class );
	}
	
	
	/**
	 * Remove the specified listener from receiving ScoreBoard events from this instance. 
	 */
	public void removeScoreBoardListener( final ScoreBoardListener listener ) {
		_messageCenter.removeTarget( listener, this, ScoreBoardListener.class );
	}


	/** Reset the start time and the number of evaluations.  */
	public void reset() {
		_solutionJudge.reset();
		_startTime = new Date();
		_evaluations = 0;
		_strategyExecutions = 0;
		_vetoes = 0;
		_optimalSolutionsFound = 0;
		_bestSolution = null;
	}


	/**
	 * Set the solution judge.
	 * @param solutionJudge   The new solutionJudge value
	 */
	public void setSolutionJudge( SolutionJudge solutionJudge ) {
		if ( _solutionJudge != null ) {
			_solutionJudge.removeSolutionJudgeListener( this );
		}

		_solutionJudge = solutionJudge;
		if ( solutionJudge != null ) {
			solutionJudge.addSolutionJudgeListener( this );
		}
	}


	/**
	 * Get the solution judge.
	 * @return   The solution judge.
	 */
	public SolutionJudge getSolutionJudge() {
		return _solutionJudge;
	}


	/**
	 * Get the number of evaluations.
	 * @return   The number of evaluations.
	 */
	public int getEvaluations() {
		return _evaluations;
	}
	
	
	/**
	 * Get the number of strategy executions
	 * @return number of strategy executions
	 */
	public int getStrategyExecutions() {
		return _strategyExecutions;
	}


	/**
	 * Get the number of vetoes.
	 * @return   The number of vetoes made.
	 */
	public int getVetoes() {
		return _vetoes;
	}


	/**
	 * Get the number of optimal solutions found.
	 * @return   The number of optimal solutions found.
	 */
	public int getOptimalSolutionsFound() {
		return _optimalSolutionsFound;
	}


	/**
	 * Get the elapsed time.
	 * @return   elapsed time in seconds.
	 */
	public double getElapsedTime() {
		Date currentTime = new Date();
		long elapsedTime = currentTime.getTime() - _startTime.getTime();
		return ( (double)( elapsedTime ) ) / 1000;
	}
	
	
	/**
	 * Judge the specified trial.
	 * @param trial the trial to judge
	 */
	public void judge( final Trial trial ) {
		_solutionJudge.judge( trial );
	}


	/**
	 * Send a message that a trial has been scored.
	 * @param algorithmSchedule  The algorithm schedule that holds the trial scored.
	 * @param trial              The trial that was scored.
	 */
	public void trialScored( final AlgorithmSchedule algorithmSchedule, final Trial trial ) {
		++_evaluations;
		_eventProxy.trialScored( this, trial );		
	}


	/**
	 * Send a message that a trial has been vetoed.
	 * @param algorithmSchedule  The algorithm schedule that holds the trial vetoed.
	 * @param trial              The trial that was vetoed.
	 */
	public void trialVetoed( final AlgorithmSchedule algorithmSchedule, final Trial trial ) {
		++_vetoes;
		_eventProxy.trialVetoed( this, trial );
	}
	
	
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
	public void strategyExecuted( final AlgorithmSchedule schedule, final AlgorithmStrategy strategy, final ScoreBoard scoreBoard ) {
		++_strategyExecutions;
	}
	

	/**
	 * Send a message that a new optimal solution has been found.
	 * @param source     The source of the new optimal solution.
	 * @param solutions  The list of solutions.
	 * @param solution   Description of the Parameter
	 */
	public void foundNewOptimalSolution( final SolutionJudge source, final List solutions, final Trial solution ) {
		++_optimalSolutionsFound;
		_bestSolution = solution;
		_eventProxy.newOptimalSolution( this, solution );
	}


	/**
	 * Get the new solution.
	 * @return   The new solution.
	 */
	public Trial getBestSolution() {
		return _bestSolution;
	}


	/**
	 * A string for displaying the ScoreBoard.
	 * @return   The string reprsentation of the ScoreBoard.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "\n\tScoreBoard\n***********************************\n" );
		buffer.append( "Elapsed Time:  " + getElapsedTime() + " seconds\n" );
		buffer.append( "Evaluations:  " + getEvaluations() + "\n" );
		buffer.append( "Vetoes:  " + getVetoes() + "\n" );
		buffer.append( "Optimal Solutions Found:  " + getOptimalSolutionsFound() + "\n" );
		buffer.append( "Number of Existing Optimal Solutions:  " + _solutionJudge.getOptimalSolutions().size() + "\n" );
		buffer.append( "Overall Satisfaction:  " + _bestSolution.getSatisfaction() + "\n" );

		buffer.append( "Optimal Solutions: \n" );
		Iterator solutionIter = _solutionJudge.getOptimalSolutions().iterator();
		int count = 0;
		while ( solutionIter.hasNext() && count < 3 ) {
			count++;
			buffer.append( "-------------------------------------\n" );
			Trial optimalSolution = (Trial)solutionIter.next();
			Iterator variableIter = optimalSolution.getProblem().getVariables().iterator();
			Iterator objectiveIter = optimalSolution.getProblem().getObjectives().iterator();
			while ( variableIter.hasNext() ) {
				Variable variable = (Variable)variableIter.next();
				double value = optimalSolution.getTrialPoint().getValue( variable );
				buffer.append( "Variable: " + variable.getName() + " = " + value + "\n" );
			}
			while ( objectiveIter.hasNext() ) {
				Objective objective = (Objective)objectiveIter.next();
				Score score = optimalSolution.getScore( objective );
				double value = score.getValue();
				double satisfaction = score.getSatisfaction();
				buffer.append( "Objective: " + objective.getName() );
				buffer.append( " = " + value );
				buffer.append( ", satisfaction = " + satisfaction + "\n" );
			}
		}

		return buffer.toString();
	}
}

