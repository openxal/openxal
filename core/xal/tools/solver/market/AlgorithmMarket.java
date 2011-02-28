/*
 *  AlgorithmMarket.java
 *
 *  Created Tuesday July 13 2004 1:14pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.tools.solver.market;

import xal.tools.messaging.*;
import xal.tools.solver.*;
import xal.tools.solver.algorithm.*;
import xal.tools.solver.solutionjudge.*;

import java.util.*;


/**
 * AlgorithmMarket keeps track of algorithm strategies.
 * @author   ky6
 * @author   t6p
 */
public class AlgorithmMarket implements AlgorithmScheduleListener, SolutionJudgeListener {
	/** the probability ratio for picking strategies sorted by efficiency */
	final static private double PROBABILITY_RATIO = 0.25;
	
	/** natural logarithm of the probability ratio  */
	final static private double PROBABILITY_RATIO_LOG = Math.log( PROBABILITY_RATIO );
	
	/** the random number generator */
	final private Random RANDOM_GENERATOR;
	
	/** the random number generator seed for reproducibility */
	static final private long RANDOM_SEED = 12345678901234L;
	
	/** the list of strategies in the market sorted by efficiency so the most efficient strategies appear first */
	protected List _strategies;
	
	/** the pool of algorithms from which to pick an algorithm */
	protected AlgorithmPool _algorithmPool;
	
	/** message center which dispatches events to registered listeners */
	protected MessageCenter _messageCenter;
	
	/** proxy which forwards events to registered listeners */
	protected AlgorithmMarketListener _eventProxy;

	
	/**
	 * Primary Constructor
	 * @param pool        the pool of algorithms
	 * @param strategies  the list of strategies
	 */
	public AlgorithmMarket( final AlgorithmPool pool, final List strategies ) {
		RANDOM_GENERATOR = new Random( RANDOM_SEED );
		
		_strategies = new ArrayList();
		
		_messageCenter = new MessageCenter("Algorithm Market");
		_eventProxy = (AlgorithmMarketListener)_messageCenter.registerSource( this, AlgorithmMarketListener.class );
		
		setAlgorithmPool( pool );
		setAlgorithmStrategies( strategies );
	}
	
	
	/**
	 * Constructor
	 * @param pool the pool of algorithms
	 */
	public AlgorithmMarket( final AlgorithmPool pool ) {
		this( pool, Collections.EMPTY_LIST );
		
		final Iterator algorithmIter = pool.getAlgorithms().iterator();
		while ( algorithmIter.hasNext() ) {
			final SearchAlgorithm algorithm = (SearchAlgorithm)algorithmIter.next();
			addAlgorithmStrategy( new SingleAlgorithmStrategy( pool, algorithm ) );
		}
	}
	

	/**
	 * Constructor
	 * @param pool      the pool of algorithms
	 * @param strategy  the only strategy to use in the market
	 */
	public AlgorithmMarket( final AlgorithmPool pool, final AlgorithmStrategy strategy ) {
		this( pool, Collections.singletonList( strategy ) );
	}


	/**
	 * Constructor
	 * @param algorithm  the only algorithm to use which also implies the single algorithm strategy
	 */
	public AlgorithmMarket( final SearchAlgorithm algorithm ) {
		this( new AlgorithmPool( algorithm ), Collections.EMPTY_LIST );
		setAlgorithmStrategy( new SingleAlgorithmStrategy( _algorithmPool, algorithm ) );
	}


	/**
	 * Constructor
	 * @param strategies  the list of strategies
	 */
	public AlgorithmMarket( final List strategies ) {
		this( new AlgorithmPool(), strategies );
	}


	/** Constructor using the default algorithm pool and the default strategies. */
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
		_messageCenter.registerTarget( listener, this, AlgorithmMarketListener.class );
	}
	
	
	/**
	 * Remove the listener from receiving AlgorithmMarket events.
	 * @param listener the listener to remove from receiving algorithm market events
	 */
	public void removeAlgorithmMarketListener( final AlgorithmMarketListener listener ) {
		_messageCenter.removeTarget( listener, this, AlgorithmMarketListener.class );
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
	 * Get the algorithm strategies.
	 * @return   The list of strategies.
	 */
	public List getStrategies() {
		return _strategies;
	}


	/**
	 * Set the list of strategies.
	 * @param strategies  The list of strategies.
	 */
	public void setAlgorithmStrategies( final List strategies ) {
		_strategies.clear();
		_strategies.addAll( strategies );
	}


	/**
	 * Set the strategy.
	 * @param strategy  The algorithm strategy to set the list with.
	 */
	public void setAlgorithmStrategy( final AlgorithmStrategy strategy ) {
		setAlgorithmStrategies( Collections.singletonList( strategy ) );
	}


	/**
	 * Add an algorithm strategy to the list of strategies.
	 * @param strategy  The algorithm strategy to add.
	 */
	public void addAlgorithmStrategy( final AlgorithmStrategy strategy ) {
		_strategies.add( strategy );
	}


	/**
	 * Set the algorithm pool.
	 * @param pool  The algorithm pool used to set the local algorithm pool.
	 */
	public void setAlgorithmPool( final AlgorithmPool pool ) {
		final AlgorithmPool oldPool = _algorithmPool;
		_algorithmPool = pool;
		_eventProxy.poolChanged( this, oldPool, pool );
	}
	
	
	/**
	 * Get the next strategy to execute by sorting strategies by efficiency and then picking a strategy randomly but weighted by
	 * the probability ratio for each successive strategy.
	 * @return the next strategy
	 */
	public AlgorithmStrategy nextStrategy() {
		Collections.sort( _strategies, AlgorithmStrategy.EFFICIENCY_COMPARATOR );
		final int count = _strategies.size();
		final int selectedIndex = (int)( Math.log( 1.0 - RANDOM_GENERATOR.nextDouble() * ( 1.0 - Math.pow( PROBABILITY_RATIO, count ) ) ) / PROBABILITY_RATIO_LOG );
		return (AlgorithmStrategy)_strategies.get( Math.min( selectedIndex, count - 1 ) );
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
	public void foundNewOptimalSolution( final SolutionJudge source, final List solutions, final Trial solution ) { 
		_algorithmPool.foundNewOptimalSolution( source, solutions, solution );
	}
}

