/*
 *  Solver.java
 *
 *  Created Wednesday June 9, 2004 2:15pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import xal.tools.messaging.MessageCenter;

import xal.extension.solver.algorithm.*;
import xal.extension.solver.market.*;
import xal.extension.solver.solutionjudge.*;

import java.util.*;

/**
 * Solver is the primary class for setting up and running an optimization.
 * @author   ky6
 * @author   t6p
 */
public class Solver implements AlgorithmPoolListener, AlgorithmMarketListener {
	/** The problem to solve */
	protected Problem _problem;
	
	/** The score board for keeping track of the solver status */
	protected ScoreBoard _scoreboard;
	
	/** The schedule of algorithms to run */
	protected AlgorithmSchedule _schedule;


	/**
	 * Primary Constructor
	 * @param stopper        Determines when the solving is finished
	 * @param solutionJudge  Decides if a solution is optimal.
	 * @param market         The market of algorithms to use
	 */
	public Solver( final AlgorithmMarket market, final Stopper stopper, final SolutionJudge solutionJudge ) {
		_scoreboard = new ScoreBoard( solutionJudge );

		_schedule = new AlgorithmSchedule( this, market, stopper );

		market.addAlgorithmMarketListener( this );
		market.getAlgorithmPool().addAlgorithmPoolListener( this );
		solutionJudge.addSolutionJudgeListener( market );

		_schedule.addAlgorithmScheduleListener( market );
		_schedule.addAlgorithmScheduleListener( _scoreboard );
	}


	/**
	 * Constructor using the specified stopper, solution judge and only the specified algorithm.
	 * @param stopper        Determines when the solving is finished
	 * @param solutionJudge  Decides if a solution is optimal.
	 * @param algorithm      The algorithm to use.
	 */
	public Solver( final SearchAlgorithm algorithm, final Stopper stopper, final SolutionJudge solutionJudge ) {
		this( new AlgorithmMarket( algorithm ), stopper, solutionJudge );
	}


	/**
	 * Constructor using the specified stopper and only the specified algorithm.
	 * @param stopper        Determines when the solving is finished
	 * @param algorithm      The algorithm to use.
	 */
	public Solver( final SearchAlgorithm algorithm, final Stopper stopper ) {
		this( algorithm, stopper, SolutionJudge.getInstance() );
	}


	/**
	 * Constructor using the specified stopper and solution judge.
	 * @param stopper        Determines when the solving is finished
	 * @param solutionJudge  Decides if a solution is optimal
	 */
	public Solver( final Stopper stopper, final SolutionJudge solutionJudge ) {
		this( new AlgorithmMarket(), stopper, solutionJudge );
	}


	/**
	 * Constructor using the default solution judge and the specified stopper.
	 * @param stopper        Determines when the solving is finished
	 */
	public Solver( final Stopper stopper ) {
		this( stopper, SolutionJudge.getInstance() );
	}


	/** Reset the solver. */
	public void reset() {
		_scoreboard.reset();
		_schedule.reset();
	}


	/**
	 * Solve the problem.
	 * @param problem  Description of the Parameter
	 * @throws xal.extension.solver.InvalidConfigurationException if the problem is ill defined
	 */
	public void solve( final Problem problem ) throws InvalidConfigurationException {
		if ( problem.getVariables().size() < 1 )  throw new InvalidConfigurationException( "At least one variable must be specified." );
		if ( problem.getObjectives().size() < 1 )  throw new InvalidConfigurationException( "At least one objective must be specified." );
		
		setProblem( problem );
		reset();
		_schedule.execute();
	}


	/**
	 * Set the problem.
	 * @param problem  The new problem value
	 */
	public void setProblem( final Problem problem ) {
		_problem = problem;
		_schedule.setProblem( problem );
	}


	/**
	 * Get the problem.
	 * @return   The problem.
	 */
	public Problem getProblem() {
		return _problem;
	}
	
	
	/** Stop the solver immediately. */
	public void stopSolving() {
		setStopper( SolveStopperFactory.immediateStopper() );
	}


	/**
	 * Set the stopper.
	 * @param stopper  The new stopper value
	 */
	public void setStopper( final Stopper stopper ) {
		_schedule.setStopper( stopper );
	}


	/**
	 * Get the solution judge.
	 * @return   The solutionJudge value
	 */
	public SolutionJudge getSolutionJudge() {
		return _scoreboard.getSolutionJudge();
	}


	/**
	 * Set the solution judge.
	 * @param solutionJudge  The new solutionJudge value
	 */
	public void setSolutionJudge( SolutionJudge solutionJudge ) {
		SolutionJudge oldJudge = getSolutionJudge();
		if ( oldJudge != null ) {
			oldJudge.removeSolutionJudgeListener( getAlgorithmMarket() );
		}

		solutionJudge.addSolutionJudgeListener( getAlgorithmMarket() );
		_scoreboard.setSolutionJudge( solutionJudge );
	}


	/**
	 * Get the scoreboard that shows the present state of solving. It shows the best solution
	 * found so far and the time elapsed since solving started.
	 * @return   The scoreboard that shows the present state of solving.
	 */
	public ScoreBoard getScoreBoard() {
		return _scoreboard;
	}
	
	
	/**
	 * Judge the specified trial.
	 * @param trial the trial to judge
	 */
	protected void judge( final Trial trial ) {
		_scoreboard.judge( trial );
	}


	/**
	 * Get the algorithm schedule.
	 * @return   The algorithm schedule.
	 */
	public AlgorithmSchedule getAlgorithmSchedule() {
		return _schedule;
	}


	/**
	 * Set the algorithm pool.
	 * @param anAlgorithmPool  The pool used to set the pool.
	 */
	public void setAlgorithmPool( AlgorithmPool anAlgorithmPool ) {
		getAlgorithmMarket().setAlgorithmPool( anAlgorithmPool );
	}


	/**
	 * Get the algorithm pool.
	 * @return   The algorithm pool.
	 */
	public AlgorithmPool getAlgorithmPool() {
		return getAlgorithmMarket().getAlgorithmPool();
	}


	/**
	 * Get the algorithm market.
	 * @return   The algorithm market.
	 */
	public AlgorithmMarket getAlgorithmMarket() {
		return _schedule.getMarket();
	}


	/**
	 * Send a message that an algorithm. was added to the pool.
	 * @param source     The source of the added algorithm.
	 * @param algorithm  Description of the Parameter
	 */
	public void algorithmAdded( final AlgorithmPool source, final SearchAlgorithm algorithm ) {
		getSolutionJudge().addSolutionJudgeListener( algorithm );
		_schedule.addAlgorithmScheduleListener( algorithm );
	}


	/**
	 * Send a message that an algorithm was removed from the pool.
	 * @param source     The source of the removed algorithm.
	 * @param algorithm  Description of the Parameter
	 */
	public void algorithmRemoved( final AlgorithmPool source, final SearchAlgorithm algorithm ) {
		getSolutionJudge().removeSolutionJudgeListener( algorithm );
		_schedule.removeAlgorithmScheduleListener( algorithm );
	}


	/**
	 * Send a message that an algorithm is available.
	 * @param source     The source of the available algorithm.
	 * @param algorithm  Description of the Parameter
	 */
	public void algorithmAvailable( final AlgorithmPool source, final SearchAlgorithm algorithm ) { }


	/**
	 * Send a message that an algorithm is unvavailable.
	 * @param source     The source of the unavailable algorithm.
	 * @param algorithm  Description of the Parameter
	 */
	public void algorithmUnavailable( final AlgorithmPool source, final SearchAlgorithm algorithm ) { }


	/**
	 * Event indicating that the algorithm pool changed.
	 * @param market   The market whose pool has changed.
	 * @param oldPool  Description of the Parameter
	 * @param newPool  Description of the Parameter
	 */
	public void poolChanged( final AlgorithmMarket market, final AlgorithmPool oldPool, final AlgorithmPool newPool ) {
		if ( oldPool != null ) {
			oldPool.removeAlgorithmPoolListener( this );
			for ( final SearchAlgorithm algorithm : oldPool.getAlgorithms() ) {
				algorithmRemoved( oldPool, algorithm );
			}
		}
		
		if ( newPool != null ) {
			newPool.addAlgorithmPoolListener( this );
			for ( final SearchAlgorithm algorithm : newPool.getAlgorithms() ) {
				algorithmAdded( newPool, algorithm );
			}
		}
	}
    
    
    // Same process above but defining the number of evaluations over which to average
    public void recordEfficiency( final int evaluations ){
        _scoreboard.recordEfficiency( evaluations );
    }
    
    
}

