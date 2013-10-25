/*
 *  SingleAlgorithmStrategy.java
 *
 *  Created Thursday August 10, 2004 9:12am
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
 * SingleAlgorithmStrategy is a subclass of algorithm strategy and generates algorithm runs
 * using a single algorithm.
 *
 * @author   ky6
 * @author t6p
 */
public class SingleAlgorithmStrategy extends AlgorithmStrategy {
	/** the single algorithm to schedule */
	final protected SearchAlgorithm _algorithm;


	/**
	 * Constructor
	 * @param pool the pool of algorithms
	 * @param algorithm  the algorithm to schedule
	 */
	public SingleAlgorithmStrategy( final AlgorithmPool pool, final SearchAlgorithm algorithm ) {
		super( pool );
		
		_algorithm = algorithm;
	}


	/**
	 * Return the label for the algorithm strategy. It is also used for deterministic sorting (see makeEfficiencyComparator method).
	 * @return   The label.
	 */
	public String getLabel() {
		return "SingleAlgorithmStrategy (" + _algorithm.getLabel() + ")";
	}


	/**
	 * Get the search algorithm.
	 * @return   The searchAlgorithm value
	 */
	public SearchAlgorithm getSearchAlgorithm() {
		return _algorithm;
	}


	/**
	 * The default method for generating an algorithm run stack. Returns a stack populated with a single algorithm.
	 * @return  An algorithm run stack representing the proposed runs.
	 */
	protected AlgorithmRunStack proposeRuns() {
		return new AlgorithmRunStack( new AlgorithmRun( _algorithm, this, getRunCount( 25, _algorithm ) ) );
	}
}

