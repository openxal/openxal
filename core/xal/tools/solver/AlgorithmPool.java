/*
 *  AlgorithmPool.java
 *
 *  Created Thursday July 8, 2004 4:12pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.tools.solver;

import xal.tools.solver.algorithm.*;
import xal.tools.solver.market.*;
import xal.tools.solver.solutionjudge.*;

import xal.tools.messaging.MessageCenter;

import java.util.*;

/**
 * AlgorithmPool keeps track of the available algorithms.
 * @author   ky6
 * @author   t6p
 */
public class AlgorithmPool implements SearchAlgorithmListener, SolutionJudgeListener, AlgorithmScheduleListener {
	/** The list of all algorithms */
	protected Collection<SearchAlgorithm> _algorithms;
	
	/** The list of algorithms available for scheduling */
	protected Collection<SearchAlgorithm> _availableAlgorithms;
	
	/** Message center for dispatching events to registerd listeners */
	protected MessageCenter _messageCenter;
	
	/** Proxy which forwards events to registered listeners */
	protected AlgorithmPoolListener _proxy;


	/**
	 * Creates a new AlgorithmPool instance Constructor that takes a list of algorithms
	 * @param algorithms  the collection of algorithms to populate the pool
	 */
	public AlgorithmPool( final Collection<SearchAlgorithm> algorithms ) {
		_algorithms = new HashSet<SearchAlgorithm>();
		_availableAlgorithms = new HashSet<SearchAlgorithm>();

		_messageCenter = new MessageCenter( "Algorithm Pool" );
		_proxy = _messageCenter.registerSource( this, AlgorithmPoolListener.class );

		addAlgorithms( algorithms );
	}


	/** Empty constructor which populates the pool of all algorithms */
	public AlgorithmPool() {
		this( generateDefaultAlgorithms() );
	}


	/**
	 * Creates a new AlgorithmPool instance Constructor that takes a list of algorithms
	 * @param algorithm  Description of the Parameter
	 */
	public AlgorithmPool( final SearchAlgorithm algorithm ) {
		this( Collections.<SearchAlgorithm>singletonList( algorithm ) );
	}


	/**
	 * Get all the algorithms.
	 * @return The default set of algorithms
	 */
	public static Collection<SearchAlgorithm> generateDefaultAlgorithms() {
		final Collection<SearchAlgorithm> allAlgorithms = new HashSet<SearchAlgorithm>();
		
		allAlgorithms.add( new RandomSearch() );
		allAlgorithms.add( new RandomShrinkSearch() );
		allAlgorithms.add( new SimplexSearchAlgorithm() );
		allAlgorithms.add( new DirectedStep() );
		
		return allAlgorithms;
	}


	/** Reset the algorithm pool by resetting all the algorithms. */
	public void reset() {
        for ( final SearchAlgorithm algorithm : _algorithms ) {
			algorithm.reset();
		}
	}
	
	
	/**
	 * Assign the problem to each algorithm in the pool.
	 * @param problem the problem to solve
	 */
	public void setProblem( final Problem problem ) {
        for ( final SearchAlgorithm algorithm : _algorithms ) {
			algorithm.setProblem( problem );
		}
	}


	/**
	 * Add an algorithm pool listener.
	 * @param listener  The listerner to add.
	 */
	public void addAlgorithmPoolListener( final AlgorithmPoolListener listener ) {
		_messageCenter.registerTarget( listener, this, AlgorithmPoolListener.class );
	}


	/**
	 * Remove a algorithm pool listener.
	 * @param listener  The listener to remove.
	 */
	public void removeAlgorithmPoolListener( final AlgorithmPoolListener listener ) {
		_messageCenter.removeTarget( listener, this, AlgorithmPoolListener.class );
	}
	
	
	/**
	 * Set the algorithm as the sole algorithm in the pool.
	 * @param algorithm the algorithm to set as the only item in the pool
	 */
	public void setAlgorithm( final SearchAlgorithm algorithm ) {
		removeAllAlgorithms();
		addAlgorithm( algorithm );
	}

	
	/**
	 * Add existing algorithms to the algorithm list.
	 * @param algorithms  The feature to be added to the Algorithms attribute
	 */
	public void addAlgorithms( final Collection<SearchAlgorithm> algorithms ) {
		for ( final SearchAlgorithm algorithm : algorithms ) {
			addAlgorithm( algorithm );
		}
	}
	
	
	/**
	 * Add an algorithm to the pool.
	 * @param algorithm     The feature to be added to the Algorithm attribute
	 */
	public void addAlgorithm( final SearchAlgorithm algorithm ) {
		_algorithms.add( algorithm );
		algorithm.addSearchAlgorithmListener( this );
		_proxy.algorithmAdded( this, algorithm );
	}


	/**
	 * Remove an algorithm from the pool.
	 * @param algorithm  the algorithm to remove from the pool
	 */
	public void removeAlgorithm( final SearchAlgorithm algorithm ) {
		algorithm.removeSearchAlgorithmListener( this );
		_algorithms.remove( algorithm );
		_availableAlgorithms.remove( algorithm );
		_proxy.algorithmRemoved( this, algorithm );
	}

	
	/**
	 * Remove the specified algorithms.
	 * @param algorithms  the algorithms to remove
	 */
	public void removeAlgorithms( final Collection<SearchAlgorithm> algorithms ) {
		for ( final SearchAlgorithm algorithm : algorithms ) {
			removeAlgorithm( algorithm );
		}
	}

	
	/** Remove all algorithms. */
	public void removeAllAlgorithms() {
		removeAlgorithms( new HashSet<SearchAlgorithm>(_algorithms) );
	}



	/**
	 * Get the algorithm list.
	 * @return   The list of algorithms.
	 */
	public Collection<SearchAlgorithm> getAlgorithms() {
		return new HashSet<SearchAlgorithm>( _algorithms );
	}


	/**
	 * Get the available algorithms.
	 * @return   The list of available algorithm.
	 */
	public Collection<SearchAlgorithm> getAvailableAlgorithms() {
		return new HashSet<SearchAlgorithm>( _availableAlgorithms );
	}
	
	
	/**
	 * Send a message that a trial has been scored.
	 * @param algorithmSchedule The algorithm schedule that holds the trial scored.
	 * @param trial The trial that was scored.	  
	 */
	public void trialScored( final AlgorithmSchedule algorithmSchedule, final Trial trial ) {
		for ( final SearchAlgorithm algorithm : getAlgorithms() ) {
			algorithm.trialScored( algorithmSchedule, trial );
		}
	}
	
	
	/**
	 * Send a message that a trial has been vetoed.
	 * @param algorithmSchedule The algorithm schedule that holds the trial vetoed.
	 * @param trial The trial that was vetoed.
	 */
	public void trialVetoed( final AlgorithmSchedule algorithmSchedule, final Trial trial ) {
		for ( final SearchAlgorithm algorithm : getAlgorithms() ) {
			algorithm.trialVetoed( algorithmSchedule, trial );
		}
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
	public void strategyExecuted( final AlgorithmSchedule schedule, final AlgorithmStrategy strategy, final ScoreBoard scoreBoard ) {}
	

	/**
	 * Send a message that an algorithm is available. An algorithm is available if it has all the
	 * data it needs to propose a new trial.
	 * @param source  The source of the available algorithm.
	 */
	public void algorithmAvailable( final SearchAlgorithm source ) {
		_availableAlgorithms.add( source );
		_proxy.algorithmAvailable( this, source );
	}


	/**
	 * Send a message that an algorithm is not available.
	 * @param source  The source of the available algorithm.
	 */
	public void algorithmUnavailable( SearchAlgorithm source ) {
		_availableAlgorithms.remove( source );
		_proxy.algorithmUnavailable( this, source );
	}
	
	
	/**
     * Event indicating that a new optimal solution has been found.
	 * @param source     The source of the new optimal solution.
	 * @param solutions  The list of solutions.
	 * @param solution   The new optimal solution.
	 */
	public void foundNewOptimalSolution( final SolutionJudge source, final List<Trial> solutions, final Trial solution ) {
		for ( final SearchAlgorithm algorithm : _algorithms ) {
			algorithm.foundNewOptimalSolution( source, solutions, solution );
		}
	}
}

