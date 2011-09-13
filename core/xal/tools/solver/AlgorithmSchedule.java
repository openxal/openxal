/*
 *  AlgorithmSchedule.java
 *
 *  Created Wednesday June 9, 2004 2:32 pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.tools.solver;

import xal.tools.messaging.MessageCenter;

import xal.tools.solver.algorithm.*;
import xal.tools.solver.constraint.*;
import xal.tools.solver.market.*;

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
	protected MessageCenter _messageCenter;
	
	/** proxy which forwarding messages to registered listeners */
	protected AlgorithmScheduleListener _eventProxy;
	
	/** determines when to stop the trials */
	volatile protected Stopper _stopper;
	
	/** the problem to solve */
	protected Problem _problem;
	
	/** the market of algorithm runs */
	protected AlgorithmMarket _market;
	
	/** the solver running the schedule */
	protected Solver _solver;
	

	/**
	 * Creates a new instance of Schedule.
	 * @param solver The solver
	 * @param market The market providing runs.
	 * @param stopper The stopper which can terminate the schedule.
	 */
	public AlgorithmSchedule( final Solver solver, final AlgorithmMarket market, final Stopper stopper ) {
		_messageCenter = new MessageCenter( "Algorithm Schedule" );
		_eventProxy = _messageCenter.registerSource( this, AlgorithmScheduleListener.class );
		
		_solver = solver;
		_market = market;
		
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
		_messageCenter.registerTarget( aListener, this, AlgorithmScheduleListener.class );
	}


	/**
	 * Remove an algorithm schedule listener.
	 * @param aListener  The listener to remove.
	 */
	public void removeAlgorithmScheduleListener( AlgorithmScheduleListener aListener ) {
		_messageCenter.removeTarget( aListener, this, AlgorithmScheduleListener.class );
	}
	
	
	/**
	 * Get the algorithm market.
	 * @return the algorithm market
	 */
	public AlgorithmMarket getMarket() {
		return _market;
	}
	
	
	/**
	 * Assign a new problem.
	 * @param problem the new problem
	 */
	public void setProblem( final Problem problem ) {
		_problem = problem;
		_market.setProblem( problem );
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
		return !_stopper.shouldStop( _solver );
	}


	/** Execute the search schedule. */
	public void execute() {
		try {
			if ( shouldExecute() ) {
				// the very first algorithm should be the InitialAlgorithm which generates a trial point from the variables' starting values
				final AlgorithmStrategy initialStrategy = new SingleAlgorithmStrategy( _solver.getAlgorithmPool(), new InitialAlgorithm( _problem ) );
				execute( initialStrategy );			
			}
			
			while ( shouldExecute() ) {
				execute( _market.nextStrategy() );
			}			
		}
		catch ( RunTerminationException exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/**
	 * Execute the specified strategy.
	 * @param strategy the strategy to execute.
	 */
	protected void execute( final AlgorithmStrategy strategy ) {
		if ( strategy != null ) {
			_eventProxy.strategyWillExecute( this, strategy, _solver.getScoreBoard() );
			strategy.execute( this, _solver.getScoreBoard() );
			_eventProxy.strategyExecuted( this, strategy, _solver.getScoreBoard() );			
		}
	}
	
	
	/**
	 * Evaluate the specified trial point or return null if the run has been terminated.
	 * @param trialPoint the trial point to evaluate
	 * @return a scored trial corresponding to the specified trial point
	 * @throws xal.tools.solver.RunTerminationException if the run has been terminated
	 */
	public Trial evaluateTrialPoint( final AlgorithmRun algorithmRun, final TrialPoint trialPoint ) {
		if ( _stopper.shouldStop( _solver ) )  throw new RunTerminationException( "Run terminated by the stopper." );
		else if ( algorithmRun.getCount() < 0 )  throw new RunTerminationException( "Run terminated due to overrun of scheduled evaluations." );
		
		final Trial trial = new Trial( _problem, trialPoint, algorithmRun.getAlgorithm(), algorithmRun.getAlgorithmStrategy() );
		score( trial );
		
		return trial;
	}


	/**
	 * Score the trial.
	 * @param trial  The trial to be scored.
	 */
	protected void score( final Trial trial ) {
		final boolean isSuccessful = _problem.evaluate( trial );
		if ( !isSuccessful )  _eventProxy.trialVetoed( this, trial );
		_solver.judge( trial );
		_eventProxy.trialScored( this, trial );
	}
}

