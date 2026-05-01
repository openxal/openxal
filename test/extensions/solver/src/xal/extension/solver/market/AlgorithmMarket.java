/*
 *  AlgorithmMarket.java
 *
 *  Created Tuesday July 13 2004 1:14pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver.market;

import xal.tools.messaging.*;
import xal.extension.solver.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.solutionjudge.*;

import java.util.*;


/**
 * AlgorithmMarket keeps track of algorithms.
 * @author   ky6
 * @author   t6p
 */
public class AlgorithmMarket implements AlgorithmScheduleListener, SolutionJudgeListener {
	/** the probability ratio for picking algorithms sorted by efficiency */
	final static private double PROBABILITY_RATIO = 0.25;
	
	/** natural logarithm of the probability ratio  */
	final static private double PROBABILITY_RATIO_LOG = Math.log( PROBABILITY_RATIO );
	
	/** the random number generator */
	final private Random RANDOM_GENERATOR;
	
	/** the random number generator seed for reproducibility */
	static final private long RANDOM_SEED = 12345678901234L;
	
	/** the list of algorithms in the market sorted by efficiency so the most efficient algorithms appear first */
	private List<SearchAlgorithm> _algorithmsByEfficiency;
	
	/** the pool of algorithms from which to pick an algorithm */
	private AlgorithmPool _algorithmPool;
	
	/** message center which dispatches events to registered listeners */
	final private MessageCenter MESSAGE_CENTER;
	
	/** proxy which forwards events to registered listeners */
	final private AlgorithmMarketListener EVENT_PROXY;

	
	/**
	 * Primary Constructor
	 * @param pool        the pool of algorithms
	 * @param algorithms  the list of algorithms
	 */
	public AlgorithmMarket( final AlgorithmPool pool ) {
		RANDOM_GENERATOR = new Random( RANDOM_SEED );		

		MESSAGE_CENTER = new MessageCenter("Algorithm Market");
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, AlgorithmMarketListener.class );
		
		_algorithmsByEfficiency = new ArrayList<SearchAlgorithm>();
		setAlgorithmPool( pool );
	}


	/**
	 * Constructor
	 * @param algorithm  the only algorithm to use which also implies the single algorithm
	 */
	public AlgorithmMarket( final SearchAlgorithm algorithm ) {
		this( new AlgorithmPool( algorithm ) );
	}


	/** Constructor using the default algorithm pool and the default algorithms List. */
	public AlgorithmMarket() {
		this( new AlgorithmPool() );
	}
	
	
	/** reset the market */
	public void reset() {
		_algorithmPool.reset();
		RANDOM_GENERATOR.setSeed( RANDOM_SEED );
	}
	
	
	/**
	 * Add a listener to receive AlgorithmMarket events.
	 * @param listener the listener to add for receiving algorithm market events
	 */
	public void addAlgorithmMarketListener( final AlgorithmMarketListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, AlgorithmMarketListener.class );
	}
	
	
	/**
	 * Remove the listener from receiving AlgorithmMarket events.
	 * @param listener the listener to remove from receiving algorithm market events
	 */
	public void removeAlgorithmMarketListener( final AlgorithmMarketListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, AlgorithmMarketListener.class );
	}
	
	
	/**
	 * Assign a new problem.
	 * @param problem the new problem
	 */
	public void setProblem( final Problem problem ) {
		_algorithmPool.setProblem( problem );
	}
	


	/**
	 * Get the algorithm pool.
	 * @return   The algorithm pool.
	 */
	public AlgorithmPool getAlgorithmPool() {
		return _algorithmPool;
	}


	/**
	 * Get the algorithm List.
	 * @return   The list of algorithms.
	 */
	public List<SearchAlgorithm> getAlgorithms() {
		return _algorithmsByEfficiency;
	}


	/**
	 * Set the list of algorithms.
	 * @param algorithmsList  The list of algorithms.
	 */
	private void setAlgorithms( final List<SearchAlgorithm> algorithms ) {
		_algorithmsByEfficiency.clear();
		_algorithmsByEfficiency.addAll( algorithms );
	}


	/**
	 * Set the algorithm pool.
	 * @param pool  The algorithm pool used to set the local algorithm pool.
	 */
	public void setAlgorithmPool( final AlgorithmPool pool ) {
		final AlgorithmPool oldPool = _algorithmPool;
		_algorithmPool = pool;

		// add algorithms
		_algorithmsByEfficiency.clear();
		_algorithmsByEfficiency.addAll( pool.getAlgorithms() );

		EVENT_PROXY.poolChanged( this, oldPool, pool );
	}
	
	
	/**
	 * Get the next algorithm to execute by sorting algorithms by efficiency and then picking a algorithm randomly but weighted by
	 * the probability ratio for each successive algorithm.
	 * @return the next algorithm
	 */
	public SearchAlgorithm nextAlgorithm() {
		Collections.sort( _algorithmsByEfficiency, SearchAlgorithm.EFFICIENCY_COMPARATOR );
		final int count = _algorithmsByEfficiency.size();
		final int selectedIndex = (int)( Math.log( 1.0 - RANDOM_GENERATOR.nextDouble() * ( 1.0 - Math.pow( PROBABILITY_RATIO, count ) ) ) / PROBABILITY_RATIO_LOG );
		return _algorithmsByEfficiency.get( Math.min( selectedIndex, count - 1 ) );
	}
	
	
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
	 * Handle a message that a trial has been scored.
	 * @param algorithmSchedule  The algorithm schedule that holds the trial scored.
	 * @param trial              The trial that was scored.
	 */
	public void trialScored( final AlgorithmSchedule algorithmSchedule, final Trial trial ) {
		_algorithmPool.trialScored( algorithmSchedule, trial );
	}


	/**
	 * Handle a message that a trial has been vetoed.
	 * @param algorithmSchedule  The algorithm schedule that holds the trial vetoed.
	 * @param trial              The trial that was vetoed.
	 */
	public void trialVetoed( final AlgorithmSchedule algorithmSchedule, final Trial trial ) { 
		_algorithmPool.trialVetoed( algorithmSchedule, trial );
	}


	/**
	 * Event indicating that a new optimal solution has been found.
	 * @param source     The source of the new optimal solution.
	 * @param solutions  The list of solutions.
	 * @param solution   The new optimal solution.
	 */
	public void foundNewOptimalSolution( final SolutionJudge source, final List<Trial> solutions, final Trial solution ) { 
		_algorithmPool.foundNewOptimalSolution( source, solutions, solution );
	}
}

