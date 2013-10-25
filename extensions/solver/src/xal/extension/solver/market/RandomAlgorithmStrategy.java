/*
 *  RandomAlgorithmStrategy.java
 *
 *  Created Tuesday July 13 2004 11:59am
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver.market;

import xal.extension.solver.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.solutionjudge.*;

import java.util.*;


/**
 * RandomAlgorithmStrategy is a subclass of algorithm strategy and selects a random algorithm
 * strategy.
 * @author   ky6
 * @author t6p
 */
public class RandomAlgorithmStrategy extends AlgorithmStrategy {
	/** the pool of algorithms from which to choose */
	final AlgorithmPool _pool;
	
	/** random number generator */
	final protected Random _randomGenerator;
	
	
	/** Constructor */
	public RandomAlgorithmStrategy( final AlgorithmPool pool ) {
		super( pool );
		
		_pool = pool;
		_randomGenerator = new Random( 0 );
	}


	/**
	 * Return the label for the algorithm strategy.
	 * @return   The label.
	 */
	public String getLabel() {
		return "Label: RandomAlgorithmStrategy";
	}


	/**
	 * Pick a random algorithm from among the available algorithms and generate a new run stack.
	 * @return      An algorithm run stack representing the proposed runs.
	 */
	protected AlgorithmRunStack proposeRuns() {
		final SearchAlgorithm algorithm = randomAlgorithm( new ArrayList<SearchAlgorithm>( _pool.getAlgorithms() ) );
		return new AlgorithmRunStack( new AlgorithmRun( algorithm, this, getRunCount( 25, algorithm ) ) );
	}


	/**
	 * Generate a random integer between 0 and the size of the algorithm pool. NextInt calls the
	 * next pseudorandom integer between 0 (inclusive) and size (exclusive). This covers all the algorithms in the pool.
	 * @param algorithms  The list of algorithms from which to pick a random algorithm
	 * @return            Description of the Return Value
	 */
	private SearchAlgorithm randomAlgorithm( final List<SearchAlgorithm> algorithms ) {
		final int index = _randomGenerator.nextInt( algorithms.size() );
		return algorithms.get( index );
	}
}

