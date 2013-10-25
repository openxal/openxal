/*
 * AlgorithmRun.java
 *
 * Created Thursday June 17 2004 3:35pm
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
   
 package xal.extension.solver; 

 import xal.tools.messaging.MessageCenter;

 import xal.extension.solver.algorithm.*;
 import xal.extension.solver.market.*;
 
 import java.util.*;
 
 /**
  * AlgorithmRun is a keeps track of the number of evaluations to run for a specified algorithm.
  * @author  ky6
  * @author t6p
 */
 public class AlgorithmRun {
	 /** the algorithm to run */
	 protected SearchAlgorithm _algorithm;
	 
	 /** the strategy which generated this run */
	 protected AlgorithmStrategy _algorithmStrategy;
	 
	 /** the schedule which schedules this run */
	 protected AlgorithmSchedule _schedule;
	 
	 /** the number of evaluations remaining to be run */
	 protected int _count;
	 
	 /** the number of evaluations requested to run */
	 final protected int _initialCount;
	 
	 
	 /**
	 * Creates a new instance of AlgorithmRun.
	 * @param algorithm The algorithm.
	 * @param strategy The algorithm strategy
	 * @param count The number of runs to perform.
	 * the generated this algorithm run.
	 */
	 public AlgorithmRun( final SearchAlgorithm algorithm, final AlgorithmStrategy strategy, final int count ) {
		 _initialCount = count;
		 _count = count;
		 _algorithm = algorithm;
		 _algorithmStrategy = strategy;
	 }	
		
		
	 /**
	 * The next algorithm to be run.
	 * @return The algorithm.
	 */
	 public SearchAlgorithm getAlgorithm() {
		 return _algorithm;
	 }
	 
	 
	 /**
	 * Get the algorithm strategy that generated this algorithm
	 * run.
	 * @return The algorithm strategy.
	 */
	 public AlgorithmStrategy getAlgorithmStrategy() {
		 return _algorithmStrategy;
	 }	 
	 
	 
	 /**
	 * Get the number of runs to perform.
	 * @return The number of runs to perform.
	 */
	 public int getCount() {
		 return _count;
	 }
	 
	 
	 /**
	  * Get the initial number of evaluations to run.
	  * @return the initial number of evaluations to run
	  */
	 public int getInitialCount() {
		 return _initialCount;
	 }
	 
	 
	 /**
	  * Convenience method to get the number of evaluations completed.
	  * @return the number of evaluations completed
	  */
	 public int getEvaluationsCompleted() {
		 return _initialCount - _count;
	 }
	
	
	 /** 
	 * Signal if there is another algorithm run.
	 * @return true if there is another algorithm to be run.
	 */
	 public boolean hasNext() {
		 return _count > 0;
	 }
	 
	 
	 /**
	  * Perform the run.
	  * @param schedule the schedule requesting the run
	  */
	 public void performRun( final AlgorithmSchedule schedule ) {
		 _schedule = schedule;
		 _algorithm.performRun( this );
	 }
	 
	 
	 /**
	  * Evaluate the given trial point.
	  * @param trialPoint the trial point to evaluate
	  * @return the scored trial
	  */
	 public Trial evaluateTrialPoint( final TrialPoint trialPoint ) {
		 --_count;
		 return _schedule.evaluateTrialPoint( this, trialPoint );
	 }
 }




