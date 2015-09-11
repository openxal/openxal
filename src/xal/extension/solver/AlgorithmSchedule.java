/*
 *  AlgorithmSchedule.java
 *
 *  Created Wednesday June 9, 2004 2:32 pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import xal.tools.messaging.MessageCenter;

import xal.extension.solver.algorithm.*;
import xal.extension.solver.constraint.*;
import xal.extension.solver.market.*;

import java.util.*;

/**
 * AlgorithmSchedule keeps track of and executes the next algorithm based on its score.
 * Schedule also sets a trial and a stopper.
 *
 * @author   ky6
 * @author   t6p
 */
public class AlgorithmSchedule {
	/** message center for dispatching messages */
	final private MessageCenter MESSAGE_CENTER;
	
	/** proxy which forwarding messages to registered listeners */
	final private AlgorithmScheduleListener EVENT_PROXY;
	
	/** determines when to stop the trials */
	volatile protected Stopper _stopper;
	
	/** the problem to solve */
	protected Problem _problem;
	
	/** the market of algorithm runs */
	protected AlgorithmMarket _market;
	
	/** the solver running the schedule */
	protected Solver _solver;
    
    /** the maximum proposed Evalautions defined by the largest minimumEvaluations of an algorithm */
    private int _proposedEvaluations;
	

	/**
	 * Creates a new instance of Schedule.
	 * @param solver The solver
	 * @param market The market providing runs.
	 * @param stopper The stopper which can terminate the schedule.
	 */
	public AlgorithmSchedule( final Solver solver, final AlgorithmMarket market, final Stopper stopper ) {
		MESSAGE_CENTER = new MessageCenter( "Algorithm Schedule" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, AlgorithmScheduleListener.class );
		
		_solver = solver;
		_market = market;
        
        _proposedEvaluations = 1;
		
		setStopper( stopper );
	}

	
	/** Reset the algorithm run stack.  */
	public void reset() {		
		_market.reset();
	}

	
	/**
	 * Add an algorithm schedule listener.
	 * @param aListener  The listener to add.
	 */
	public void addAlgorithmScheduleListener( AlgorithmScheduleListener aListener ) {
		MESSAGE_CENTER.registerTarget( aListener, this, AlgorithmScheduleListener.class );
	}


	/**
	 * Remove an algorithm schedule listener.
	 * @param aListener  The listener to remove.
	 */
	public void removeAlgorithmScheduleListener( AlgorithmScheduleListener aListener ) {
		MESSAGE_CENTER.removeTarget( aListener, this, AlgorithmScheduleListener.class );
	}
	
	
	/**
	 * Get the algorithm market.
	 * @return the algorithm market
	 */
	public AlgorithmMarket getMarket() {
		return _market;
	}


	/** get the score board */
	public ScoreBoard getScoreBoard() {
		return _solver.getScoreBoard();
	}
	
	
	/**
	 * Assign a new problem.
	 * @param problem the new problem
	 */
	public void setProblem( final Problem problem ) {
		_problem = problem;
		_market.setProblem( problem );
        computeMinimumEvaluations();
	}
	
	
	/**
	 * Get the stopper.
	 * @return the stopper
	 */
	public Stopper getStopper() {
		return _stopper;
	}
	
	
	/**
	 * Assign a new stopper.
	 * @param stopper the new stopper
	 */
	public void setStopper( final Stopper stopper ) {
		_stopper = stopper;
	}
	
	
	/**
	 * Determine whether to continue executing the schedule.
	 * @return true if the stopper allows us to continue executing the schedule and false if not
	 */
	public boolean shouldExecute() {
		return !shouldStop();
	}


	/**
	 * Allows the algorithms to check when they should stop executing thier code
	 */
	public boolean shouldStop(){
		return _stopper.shouldStop( _solver );
	}


    /**
     * Going to get the largest minimumEvaluations that a program desires
     * This will search through all the current algorithms in teh pool
     */
    private void computeMinimumEvaluations(){
        final AlgorithmPool pool = _solver.getAlgorithmPool();

		int proposedEvaluations = 1;
        for( SearchAlgorithm algorithm: pool.getAlgorithms() ){
            int algorithmMinEvals = algorithm.getMinEvaluationsPerRun();
            if( algorithmMinEvals > proposedEvaluations ){
                proposedEvaluations = algorithmMinEvals;
            }
        }

		_proposedEvaluations = proposedEvaluations;
    }


	/** Execute the search schedule. */
	public void execute() {
		try {
			if ( shouldExecute() ) {
				// the very first algorithm should be the InitialAlgorithm which generates a trial point from the variables' starting values
				executeRun( new InitialAlgorithm( _problem ) );
			}
			
			while ( shouldExecute() ) {
				executeRun( _market.nextAlgorithm() );
			}			
		}
		catch ( RunTerminationException exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/**
	 * Execute a run for the specified algorithm.
	 * @param algorithm the algorithm to execute.
	 */
	private void executeRun( final SearchAlgorithm algorithm ) {
		if ( algorithm != null ) {
            algorithm.setProposedEvaluations( _proposedEvaluations );
            
			EVENT_PROXY.algorithmRunWillExecute( this, algorithm, _solver.getScoreBoard() );
			algorithm.executeRun( this, _solver.getScoreBoard() );
			EVENT_PROXY.algorithmRunExecuted( this, algorithm, _solver.getScoreBoard() );
		}
	}
	
	
	/**
	 * Evaluate the specified trial point or return null if the run has been terminated.
	 * @param trialPoint the trial point to evaluate
	 * @return a scored trial corresponding to the specified trial point
	 * @throws xal.extension.solver.RunTerminationException if the run has been terminated
	 */
	public Trial evaluateTrialPoint( final SearchAlgorithm searchAlgorithm, final TrialPoint trialPoint ) {
		if ( _stopper.shouldStop( _solver ) )  throw new RunTerminationException( "Run terminated by the stopper." );
		else if ( searchAlgorithm.getEvaluationsLeft() < 0 )  throw new RunTerminationException( "Run terminated due to overrun of scheduled evaluations." );
		
		final Trial trial = new Trial( _problem, trialPoint, searchAlgorithm );
		score( trial );
		
		return trial;
	}


	/**
	 * Score the trial.
	 * @param trial  The trial to be scored.
	 */
	private void score( final Trial trial ) {
		final boolean isSuccessful = _problem.evaluate( trial );
		if ( !isSuccessful )  EVENT_PROXY.trialVetoed( this, trial );
		_solver.judge( trial );
		EVENT_PROXY.trialScored( this, trial );
	}
}

