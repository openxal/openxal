/*
 *  AlgorithmRunStack.java
 *
 *  Created Thursday July 8, 2004 10:38 am
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.tools.solver;

import xal.tools.messaging.MessageCenter;

import xal.tools.solver.algorithm.*;
import xal.tools.solver.market.*;

import java.util.*;


/**
 * AlgorithmRunStack is a container of algorithm runs and represents a single starategy's run requests.
 * @author   ky6
 * @author t6p
 */
public class AlgorithmRunStack {
	/** the algorithm strategy that populated the run stack */
	protected AlgorithmStrategy _strategy;
	
	/** the list of runs */
	protected LinkedList<AlgorithmRun> _runs;
		
	
	/** Primary Constructor */
	public AlgorithmRunStack( final AlgorithmStrategy strategy ) {
		_strategy = strategy;
		_runs = new LinkedList<AlgorithmRun>();
	}
	
	
	/** Constructor */
	public AlgorithmRunStack() {
		this( (AlgorithmStrategy)null );
	}
	
	
	/** 
	 * Constructor
	 * @param run the run with which to initialize the run stack
	 */
	public AlgorithmRunStack( final AlgorithmRun run ) {
		this( run.getAlgorithmStrategy() );
		appendRun( run );
	}
	
	
	/** Reset the algorithm schedule.  */
	public void reset() {
		_runs.clear();
	}
	
	
	/**
	 * Determine if there are any more algorithm runs in the algorithm run stack.
	 * @return   True if there are more algorithm runs.
	 */
	public boolean hasNext() {
		return _runs.iterator().hasNext();
	}
	
	
	/**
	 * Add another algorithm run stack to the stack.
	 * @param anAlgorithmRunStack  The algorithm to add to the stack.
	 */
	public void appendStack( final AlgorithmRunStack anAlgorithmRunStack ) {
		for ( final AlgorithmRun algorithmRun : anAlgorithmRunStack.getAlgorithmRuns() ) {
			appendRun( algorithmRun );
		}
	}
	
	
	/**
	 * Add another another algorithm run to the stack.
	 * @param anAlgorithmRun  The algorithm to add to the stack.
	 */
	public void appendRun( final AlgorithmRun anAlgorithmRun ) {
		_runs.addFirst( anAlgorithmRun );
	}
	
	
	/**
	 * Get the algorithm runs.
	 * @return   The list of algorithm runs.
	 */
	public List<AlgorithmRun> getAlgorithmRuns() {
		return _runs;
	}
	
	
	/**
	 * Get the algorithm strategy that populated this algorithm run stack.
	 * @return the algorithm strategy that populated this instance
	 */
	public AlgorithmStrategy getAlgorithmStrategy() {
		return _strategy;
	}
	
	
	/**
	 * Get the next algorithm to be run.
	 * @return   A search algorithm.
	 */
	public AlgorithmRun popAlgorithmRun() {
		return _runs.removeLast();
	}
}

