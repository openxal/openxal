/*
 *  SolveStopperFactory.java
 *
 *  Created Monday June 28 2004 11:32am
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import java.util.*;


/**
 * SolverStopperFactory is an interface which generates a stopper. Stoppers stop the solver
 * after the specified number of evaluations have been performed.
 *
 * @author   ky6
 * @author t6p
 */
public class SolveStopperFactory {
	/**
	 * Stop the solver immediately.
	 * @return     The stopper implementation.
	 */
	public static Stopper immediateStopper() {
		return new Stopper() {
				public boolean shouldStop( Solver solver ) {
					return true;
				}
			};
	}

	
	/**
	 * Stop the solver after the solver reaches max evaluations (or algorithm executions to avoid a possible hang).
	 * @param maxEvaluations  The maximum evaluations or algorithm executions to run the solver.
	 * @return The stopper implementation.
	 */
	public static Stopper maxEvaluationsStopper( final int maxEvaluations ) {
		return new Stopper() {
				public boolean shouldStop( final Solver solver ) {
					final ScoreBoard scoreboard = solver.getScoreBoard();
					return scoreboard.getEvaluations() >= maxEvaluations || scoreboard.getAlgorithmExecutions() >= maxEvaluations;
				}
			};
	}


	/**
	 * Get a stopper after a certain number of seconds.
	 * @param maxSeconds  The maximum number of seconds before getting the stopper.
	 * @return            A stopper.
	 */
	public static Stopper maxElapsedTimeStopper( final double maxSeconds ) {
		return new Stopper() {
				public boolean shouldStop( Solver solver ) {
					return solver.getScoreBoard().getElapsedTime() >= maxSeconds;
				}
			};
	}
	
	
	
	/**
	 * Get a stopper that stops after the minimum satisfaction is achieved. This stopper should not be used alone since it may never stop if the satisfaction is unreachable.
	 * @param satisfactionTarget The satisfaction that must be reached by all objectives before stopping.
	 * @return              A stopper.
	 */
	public static Stopper minSatisfactionStopper( final double satisfactionTarget ) {
		return new Stopper() {
			public boolean shouldStop( Solver solver ) {
				return meetsSatisfaction( solver, satisfactionTarget );
			}
		};
	}
	
	
	/**
	 * Stop the solver after the solver reaches max evaluations (or algorithm executions to avoid a possible hang).
	 * @param maxEvaluations  The maximum evaluations or algorithm executions to run the solver.
	 * @param satisfactionTarget The satisfaction that must be reached by all objectives before stopping.
	 * @return The stopper implementation.
	 */
	public static Stopper maxEvaluationsSatisfactionStopper( final int maxEvaluations, final double satisfactionTarget ) {
		return new Stopper() {
			public boolean shouldStop( final Solver solver ) {
				final ScoreBoard scoreboard = solver.getScoreBoard();
				if ( scoreboard.getEvaluations() >= maxEvaluations || scoreboard.getAlgorithmExecutions() >= maxEvaluations )  return true;
				
				return meetsSatisfaction( solver, satisfactionTarget );
			}
		};
	}
	

	/**
	 * Get a stopper that runs between a minimum and maximum time and has a minimum satisfaction
	 * that all objectives must reach in order to stop short of the maximum time.
	 * @param minSeconds    The mininum number of seconds before getting stopper.
	 * @param maxSeconds    The maximum number of seconds before getting stopper.
	 * @param satisfactionTarget The satisfaction that must be reached by all objectives before stopping.
	 * @return              A stopper.
	 */
	public static Stopper minMaxTimeSatisfactionStopper( final double minSeconds, final double maxSeconds, final double satisfactionTarget ) {
		return new Stopper() {
				public boolean shouldStop( Solver solver ) {
					final double elapsedTime = solver.getScoreBoard().getElapsedTime();
					
					// check if the maximum elapsed time has been exceeded
					if ( elapsedTime >= maxSeconds )  return true;
					
					// check if the minimum elapsed time has yet to be reached
					if ( elapsedTime < minSeconds )  return false;
					
					return meetsSatisfaction( solver, satisfactionTarget );
				}
			};
	}
	
	
	/** 
	 * Utility method to test whether the satisfaction target is met.
	 * @param satisfactionTarget The satisfaction that must be reached by all objectives before stopping.
	 */
	static private boolean meetsSatisfaction( final Solver solver, final double satisfactionTarget ) {
		// get the best solution found so far
		final Trial bestSolution = solver.getScoreBoard().getBestSolution();
		if ( bestSolution == null )  return false;
		
		// check if any objective satisfaction is below the minimum satisfaction					
		for ( final Objective objective : solver.getProblem().getObjectives() ) {
			final double satisfaction = bestSolution.getSatisfaction( objective );
			if ( Double.isNaN( satisfaction ) || satisfaction < satisfactionTarget ) {
				return false;
			}
		}
		
		return true;	// if we made it this far then all conditions are satisfied to stop		
	}
	
	
	/** 
	 * Get a stopper that stops after the specified number of repeat solutions is found.
	 * @param minRepeatSolutions the number of repeat solutions to find before stopping
	 */
	public static Stopper flatOptimizationStopper( final int minRepeatSolutions ) {
		return new Stopper() {
			public boolean shouldStop( Solver solver ) {
				return solver.getScoreBoard().getSolutionJudge().getOptimalSolutions().size() >= minRepeatSolutions;
			}
		};
	}


	/**
	 * Get a stopper after the max number of optimal solutions is reached.
	 * @param minOptimalSolutions  The minimum number of optimal solutions
	 * @return                     The maxOptimalSolutionStopper value
	 */
	public static Stopper maxOptimalSolutionStopper( final int minOptimalSolutions ) {
		return new Stopper() {
				public boolean shouldStop( Solver solver ) {
					return solver.getScoreBoard().getOptimalSolutionsFound() >= minOptimalSolutions;
				}
			};
	}


	/**
	 * Compound stopper which stops the solver if either stopper1 or stopper2 would stop it.
	 * @param stopper1  The first stopper to check
	 * @param stopper2  The second stopper to check
	 * @return          A compound stopper
	 */
	public static Stopper orStopper( final Stopper stopper1, final Stopper stopper2 ) {
		return SolveStopperFactory.orStoppers( stopper1, stopper2 );
	}
	
	
	/**
	 * Compound stopper which stops the solver if any of the stoppers stop it.
	 * @param stoppers  The stoppers to check
	 * @return          A compound stopper
	 */
	public static Stopper orStoppers( final Stopper ... stoppers ) {
		return new Stopper() {
			public boolean shouldStop( final Solver solver ) {
				for ( final Stopper stopper : stoppers ) {
					if ( stopper.shouldStop( solver ) ) return true;
				}
				return false;
			}
		};
	}
	

	/**
	 * Compound stopper which stops the solver if both stopper1 and stopper2 would stop it.
	 * @param stopper1  The first stopper to check
	 * @param stopper2  The second stopper to check
	 * @return          A compound stopper
	 */
	public static Stopper andStopper( final Stopper stopper1, final Stopper stopper2 ) {
		return new Stopper() {
				public boolean shouldStop( final Solver solver ) {
					return stopper1.shouldStop( solver ) && stopper2.shouldStop( solver );
				}
			};
	}
}

