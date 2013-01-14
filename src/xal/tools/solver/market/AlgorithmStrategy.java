/*
 * AlgorithmStrategy.java
 *
 * Created Monday July 12 2004 4:39pm
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
  
 package xal.tools.solver.market; 
 
 import xal.tools.solver.*;
 import xal.tools.solver.algorithm.*;
 import xal.tools.solver.solutionjudge.*;

 import java.util.*;

 /**
 * AlgorithmStrategy keeps track of algorithm strategies.
 * @author  ky6
 * @author t6p
 */
 abstract public class AlgorithmStrategy implements AlgorithmScheduleListener, AlgorithmPoolListener, SolutionJudgeListener {
	 /** comparator for sorting based on efficiency */
	 final static Comparator<AlgorithmStrategy> EFFICIENCY_COMPARATOR;
	 
	 /** measure of this strategy's running efficiency in solving the problem */
	 protected double _efficiency;
	 
	 
	 /** static constructor */
	 static {
		 EFFICIENCY_COMPARATOR = makeEfficiencyComparator();
	 }
	 
	 
	 /** 
	  * Constructor
	  *	@param pool the pool of algorithms from which to choose
	  */
	 public AlgorithmStrategy( final AlgorithmPool pool ) {
		 _efficiency = 1.0;		// all strategies start with 100% efficiency
	 }
	 
	 
	 /**
	  * Propose some algorithms that should be run.
	  * @return An algorithm run stack representing the proposed runs.
	  */
	 abstract protected AlgorithmRunStack proposeRuns();
	 
	 
	 /**
	  * Return the label for the algorithm strategy. It is also used for deterministic sorting (see makeEfficiencyComparator method).
	  * @return The label.
	  */
	 abstract public String getLabel();
	 
	 
	 /**
	  * Get this strategy's efficiency for improving satisfaction.
	  * @return the efficiency
	  */
	 final public double getEfficiency() {
		 return _efficiency;
	 }
	 
	 
	 /**
	  * Get a string representation of this strategy.
	  * @return a string representation of this strategy
	  */
	 public String toString() {
		 return getLabel() + ", efficiency: " + getEfficiency();
	 }
	 
	 
	 /**
	  * Get the nearest integer to the specified target that is within the specified range.
	  * @param target the target that we are trying to meet
	  * @param lower the minimum value of the allowed range
	  * @param upper the maximum value of the allowed range
	  * @return the nearest integer to the target, but within the specified range
	  */
	 static protected int getNearestIntegerInRange( final int target, final int lower, final int upper ) {
		 return ( target > upper ) ? upper : ( ( target < lower ) ? lower : target );
	 }
	 
	 
	 /**
	  * Get the number of runs that is nearest the target count but withing the algorithm's accepted range.
	  * @param targetCount the target that we are trying to meet
	  * @param algorithm the algorithm that specifies the valid range
	  * @return the suggested run count that is nearest the target count but within the specified algorithm's valid range
	  */
	 static protected int getRunCount( final int targetCount, final SearchAlgorithm algorithm ) {
		 return getNearestIntegerInRange( targetCount, algorithm.getMinEvaluationsPerRun(), algorithm.getMaxEvaluationsPerRun() );
	 }
	 
	 
	 /**
	  * Execute the proposed runs.
	  * @param schedule the schedule requesting the strategy execution
	  * @param scoreboard the scoreboard of solver status
	  */
	 final public void execute( final AlgorithmSchedule schedule, final ScoreBoard scoreboard ) {
		 final double initialSatisfaction = ( scoreboard.getBestSolution() != null ) ? scoreboard.getBestSolution().getSatisfaction() : 0.0;
		 
		 int evaluations = 0;
		 final AlgorithmRunStack runStack = proposeRuns();
		 while( runStack.hasNext() ) {
			 final AlgorithmRun algorithmRun = runStack.popAlgorithmRun();
			 algorithmRun.performRun( schedule );
			 evaluations += algorithmRun.getEvaluationsCompleted();
		 }
		 
		 updateEfficiency( initialSatisfaction, scoreboard.getBestSolution().getSatisfaction(), evaluations );
	 }
	 
	 
	 /**
	  * Compute and update the efficiency of this strategy's run.
	  * @param initialSatisfaction the satisfaction at the start of the run
	  * @param endSatisfaction the satisfaction at the end of the run
	  * @param evaluations the number of evaluations run
	  */
	 private void updateEfficiency( final double initialSatisfaction, final double endSatisfaction, final int evaluations ) {
		 if ( evaluations > 0 && initialSatisfaction < 1.0 ) {
			 // rescale the satisfaction based on what can be achieved i.e. at most the satisfaction can be 1.0 and charge for evaluations
			 final double efficiency = ( endSatisfaction - initialSatisfaction ) / ( ( 1.0 - initialSatisfaction ) * evaluations );
			 _efficiency = 0.75 * Math.max( efficiency, 0.0 ) + 0.25 * _efficiency;		// weight the new efficiency against the original efficiency
		 }
	 }
	 
	 
	 /**
	 * Send a message that a trial has been scored.
	 * @param algorithmSchedule The algorithm schedule that 
	 * holds the trial scored.
	 * @param trial The trial that was scored.	  
	 */
	 public void trialScored( final AlgorithmSchedule algorithmSchedule, final Trial trial ) {}
	  
	  
	 /**
	 * Send a message that a trial has been vetoed.
	 * @param algorithmSchedule The algorithm schedule 
	 * that holds the trial vetoed.
	 * @param trial The trial that was vetoed.
	 */
	 public void trialVetoed( final AlgorithmSchedule algorithmSchedule, final Trial trial ) {}
	 
	 
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
	 * Send a message that an algorithm. was added to the pool.
	 * @param source The source of the added algorithm.
	 * @param algorithm The algorithm which was added.
	 */
	 public void algorithmAdded( final AlgorithmPool source, final SearchAlgorithm algorithm ) {}
	 
	 
	 /**
	 * Send a message that an algorithm was removed from the
	 * pool.
	 * @param source The source of the removed algorithm.
	 * @param algorithm The algorithm which was removed.
	 */
	 public void algorithmRemoved( final AlgorithmPool source, final SearchAlgorithm algorithm ) {}
	 
	 
	 /**
	 * Send a message that an algorithm is available.
	 * @param source The source of the available algorithm.
	 * @param algorithm The algorithm which has become available.
	 */
	 public void algorithmAvailable( final AlgorithmPool source, final SearchAlgorithm algorithm ) {}
	 
	 
	 /**
	 * Send a message that an algorithm is unvavailable.
	 * @param source The source of the unavailable algorithm.
	 * @param algorithm The algorithm which has become unavailable.
	 */
	 public void algorithmUnavailable( final AlgorithmPool source, final SearchAlgorithm algorithm ) {}
	 
	 
	 /**
	 * Send a message that a new optimal solution has been found.
	 * @param source The source of the new optimal solution.
	 * @param solutions The list of solutions.
	 * @param solution The new optimal solution.
	 */
	 public void foundNewOptimalSolution( final SolutionJudge source, final List<Trial> solutions, final Trial solution ) {}
	 
	 
	 /**
	  * Generate a new efficiency comparator which reverse sorts the strategies according to efficiency and falls back to strategy label for deterministic behavior should the efficiencies be degenerate (e.g. before any evaluations).
	  * @return a new efficiency comparator
	  */
	 private static Comparator<AlgorithmStrategy> makeEfficiencyComparator() {
		 return new Comparator<AlgorithmStrategy>() {
			 public int compare( final AlgorithmStrategy strategyA, final AlgorithmStrategy strategyB ) {
				 final double efficiencyA = strategyA.getEfficiency();
				 final double efficiencyB = strategyB.getEfficiency();
				 return efficiencyA < efficiencyB ? 1 : ( efficiencyA > efficiencyB ? -1 : strategyA.getLabel().compareTo( strategyB.getLabel() ) );
			 }


			 /** determine if this comparator equals the specified object */
			 public boolean equals( final Object anObject ) {
				 return this == anObject;
			 }
		 };
	 }
 }
	 


