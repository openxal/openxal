/*
 * InitialAlgorithm.java
 *
 * Created on Mon Sep 20 09:11:53 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.extension.solver.algorithm;

import xal.extension.solver.*;

import java.util.*;

/**
 * InitialAlgorithm
 *
 * @author  tap
 * @since Sep 20, 2004
 */
public class InitialAlgorithm extends SearchAlgorithm {
	 /** Constructor */
	 public InitialAlgorithm( final Problem problem ) {
		 super();
		 
		 setProblem( problem );
	 }
	 
	 
	 /**
	  * Return the label for a search algorithm.
	  * @return a label for the algorithm
	  */
	 public String getLabel() {
		 return "Initial Algorithm";
	 }
	
	
	/**
	 * Calculate the next few trial points.
	 * @param schedule the schedule of runs
	 */
	public void performRun( final AlgorithmSchedule schedule ) {
		evaluateTrialPoint( nextTrialPoint() );
	}
	
	 
	 /**
	  * Return the next trial point.
	  * @return a new trial point
	  */
	 public TrialPoint nextTrialPoint() {
		 return _problem.generateInitialTrialPoint();
	 }
	
	
	/**
	 * Get the minimum number of evaluations per run.  Subclasses may want to override this method.
	 * @return the minimum number of evaluation per run.
	 */
	public int getMinEvaluationsPerRun() {
		return 0;
	}
	
	
	/**
	 * Get the maximum number of evaluations per run.  Subclasses may want to override this method.
	 * @return the maximum number of evaluation per run.
	 */
	public int getMaxEvaluationsPerRun() {
		return 1;
	}
	
	 
	 /**
	  * Returns the global rating which in an integer
	  * between 0 and 10.
	  */
	 int globalRating() {
		 return 0;
	 }
	 
	 
	 /**
	  * Returns the local rating which is an integer 
	  * between 0 and 10. 
	  */
	 int localRating() {
		 return 0;
	 }
}

