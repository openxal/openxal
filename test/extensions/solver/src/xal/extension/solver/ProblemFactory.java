//
//  ProblemFactory.java
//  xal
//
//  Created by Thomas Pelaia on 6/27/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.solver;

import java.util.*;


/** Generate problems for some special cases */
public class ProblemFactory {
	/** hide the constructor */
	protected ProblemFactory() {}
	
	
	/**
	 * Generate a problem which seeks to minimize the score which is in a range of zero to infinity
	 * @param variables the variables
	 * @param scorer the scorer
	 * @param tolerance the score corresponding to 90% satisfaction
	 */
	static public Problem getInverseSquareMinimizerProblem( final List<Variable> variables, final Scorer scorer, final double tolerance ) {
		final Objective objective = getInverseSquareObjectiveWithTolerance( tolerance );
		return getProblem( variables, scorer, objective );
	}
	
	
	/**
	 * Generate a problem which seeks to maximize the score which is in a range of zero to infinity
	 * @param variables the variables
	 * @param scorer the scorer
	 * @param tolerance the score corresponding to 90% satisfaction
	 */
	static public Problem getInverseSquareMaximizerProblem( final List<Variable> variables, final Scorer scorer, final double tolerance ) {
		final Objective objective = getInverseSquareRisingObjectiveWithTolerance( tolerance );
		return getProblem( variables, scorer, objective );
	}
	
	
	/**
	 * Generate a problem which uses an S-curve satisfaction curve over the range of negative infinity to positive infinity with center at zero
	 * @param variables the variables
	 * @param scorer the scorer
	 * @param slope the slope of the satisfaction curve at the center
	 */
	static public Problem getSCurveProblem( final List<Variable> variables, final Scorer scorer, final double slope ) {
		return getSCurveProblem( variables, scorer, 0.0, slope );
	}
	
	
	/**
	 * Generate a problem which uses an S-curve satisfaction curve over the range of negative infinity to positive infinity
	 * @param variables the variables
	 * @param scorer the scorer
	 * @param center the center value of the satisfaction curve
	 * @param slope the slope of the satisfaction curve at the center
	 */
	static public Problem getSCurveProblem( final List<Variable> variables, final Scorer scorer, final double center, final double slope ) {
		final Objective objective = getSCurveObjectiveWithCenterAndSlope( center, slope );
		return getProblem( variables, scorer, objective );
	}
	
	
	/**
	 * Generate a problem which uses a linear rising satisfaction curve over the specified score range
	 * @param variables the variables
	 * @param scorer the scorer
	 * @param minScore the minimum score
	 * @param maxScore the maximum score
	 */
	static public Problem getLinearMaximizerProblem( final List<Variable> variables, final Scorer scorer, final double minScore, final double maxScore ) {
		final Objective objective = getLinearRisingObjective( minScore, maxScore );
		return getProblem( variables, scorer, objective );
	}
	
	
	/**
	 * Generate a problem which uses a linear falling satisfaction curve over the specified score range
	 * @param variables the variables
	 * @param scorer the scorer
	 * @param minScore the minimum score
	 * @param maxScore the maximum score
	 */
	static public Problem getLinearMinimizerProblem( final List<Variable> variables, final Scorer scorer, final double minScore, final double maxScore ) {
		final Objective objective = getLinearFallingObjective( minScore, maxScore );
		return getProblem( variables, scorer, objective );
	}
	
	
	/**
	 * Generate a problem which uses an accelerating satisfaction curve over the specified score range
	 * @param variables the variables
	 * @param scorer the scorer
	 * @param minScore the minimum score
	 * @param maxScore the maximum score
	 * @param endSlope the slope of the satisfaction curve at the maximum score
	 */
	static public Problem getAcceleratingProblem( final List<Variable> variables, final Scorer scorer, final double minScore, final double maxScore, final double endSlope ) {
		final Objective objective = getAcceleratingObjective( minScore, maxScore, endSlope );
		return getProblem( variables, scorer, objective );
	}
	
	
	/**
	 * Generate a problem which uses a decelerating satisfaction curve over the specified score range
	 * @param variables the variables
	 * @param scorer the scorer
	 * @param minScore the minimum score
	 * @param maxScore the maximum score
	 * @param startSlope the slope of the satisfaction curve at the minimum score
	 */
	static public Problem getDeceleratingProblem( final List<Variable> variables, final Scorer scorer, final double minScore, final double maxScore, final double startSlope ) {
		final Objective objective = getDeceleratingObjective( minScore, maxScore, startSlope );
		return getProblem( variables, scorer, objective );
	}
	
	
	/**
	 * Generate a problem from variables, a scorer and a single objective
	 * @param variables the variables
	 * @param scorer the scorer
	 * @param objective the objective
	 * @return a new problem
	 */
	static private Problem getProblem( final List<Variable> variables, final Scorer scorer, final Objective objective ) {
		final List<Objective> objectives = new ArrayList<Objective>( 1 );
		objectives.add( objective );
		return new Problem( objectives, variables, getEvaluator( scorer, variables, objective ) );
	}
	
	
	/**
	 * Generate an evaluator which takes the score from the scorer and feeds it to a single objective.
	 * @param scorer the scorer
	 * @param objective the objective to use
	 * @return an evaluator
	 */
	static private Evaluator getEvaluator( final Scorer scorer, final List<Variable> variables, final Objective objective ) {
		return new Evaluator() {
			public void evaluate( final Trial trial ) {
				final double score = scorer.score( trial, variables );
				trial.setScore( objective, score );
			}
		};
	}
	
	
	/**
	 * Generate an objective which uses a linear rising satisfaction curve over the specified score range 
	 * @param minScore the minimum score
	 * @param maxScore the maximum score
	 */
	static private Objective getLinearRisingObjective( final double minScore, final double maxScore ) {
		return new Objective( "Rising Linear" ) {			
			/**
			 * Determines how satisfied the user is with the specified value for this objective.
			 * @param value  The value associated with this objective for a particular trial
			 * @return       the user satisfaction for the specified value
			 */
			public double satisfaction( final double score ) {
				return SatisfactionCurve.linearRisingSatisfaction( score, minScore, maxScore );
			}
		};
	}
	
	
	/**
	 * Generate an objective which uses a linear falling satisfaction curve over the specified score range 
	 * @param minScore the minimum score
	 * @param maxScore the maximum score
	 */
	static private Objective getLinearFallingObjective( final double minScore, final double maxScore ) {
		return new Objective( "Falling Linear" ) {			
			/**
			 * Determines how satisfied the user is with the specified value for this objective.
			 * @param value  The value associated with this objective for a particular trial
			 * @return       the user satisfaction for the specified value
			 */
			public double satisfaction( final double score ) {
				return SatisfactionCurve.linearFallingSatisfaction( score, minScore, maxScore );
			}
		};
	}
	
	
	/**
	 * Generate an objective which uses an accelerating satisfaction curve over the specified score range 
	 * @param minScore the minimum score
	 * @param maxScore the maximum score
	 * @param endSlope the slope of the satisfaction curve at the maximum score
	 */
	static private Objective getAcceleratingObjective( final double minScore, final double maxScore, final double endSlope ) {
		return new Objective( "Accelerating" ) {			
			/**
			 * Determines how satisfied the user is with the specified value for this objective.
			 * @param value  The value associated with this objective for a particular trial
			 * @return       the user satisfaction for the specified value
			 */
			public double satisfaction( final double score ) {
				return SatisfactionCurve.acceleratingSatisfaction( score, minScore, maxScore, endSlope );
			}
		};
	}
	
	
	/**
	 * Generate an objective which uses a decelerating satisfaction curve over the specified score range 
	 * @param minScore the minimum score
	 * @param maxScore the maximum score
	 * @param startSlope the slope of the satisfaction curve at the minimum score
	 */
	static private Objective getDeceleratingObjective( final double minScore, final double maxScore, final double startSlope ) {
		return new Objective( "Decelerating" ) {			
			/**
			 * Determines how satisfied the user is with the specified value for this objective.
			 * @param value  The value associated with this objective for a particular trial
			 * @return       the user satisfaction for the specified value
			 */
			public double satisfaction( final double score ) {
				return SatisfactionCurve.deceleratingSatisfaction( score, minScore, maxScore, startSlope );
			}
		};
	}
	
	
	/**
	 * Generate an objective which uses an S-Curve satisfaction curve with the specified center and slope
	 * @param center the center value of the satisfaction curve
	 * @param slope the slope of the satisfaction curve at the center
	 */
	static private Objective getSCurveObjectiveWithCenterAndSlope( final double center, final double slope ) {
		return new Objective( "S-Curve" ) {			
			/**
			 * Determines how satisfied the user is with the specified value for this objective.
			 * @param value  The value associated with this objective for a particular trial
			 * @return       the user satisfaction for the specified value
			 */
			public double satisfaction( final double score ) {
				return SatisfactionCurve.sCurveSatisfactionWithCenterAndSlope( score, center, slope );
			}
		};
	}
	
	
	/**
	 * Generate an objective which uses an inverse square satisfaction curve with the specified tolerance
	 * @param tolerance the score corresponding to 90% satisfaction
	 */
	static private Objective getInverseSquareObjectiveWithTolerance( final double tolerance ) {
		return new Objective( "Inverse Square" ) {			
			/**
			 * Determines how satisfied the user is with the specified value for this objective.
			 * @param value  The value associated with this objective for a particular trial
			 * @return       the user satisfaction for the specified value
			 */
			public double satisfaction( final double score ) {
				return SatisfactionCurve.inverseSquareSatisfaction( score, tolerance );
			}
		};
	}
	
	
	/**
	 * Generate an objective which uses an inverse square rising satisfaction curve with the specified tolerance
	 * @param tolerance the score corresponding to 90% satisfaction
	 */
	static private Objective getInverseSquareRisingObjectiveWithTolerance( final double tolerance ) {
		return new Objective( "Inverse Square Rising" ) {			
			/**
			 * Determines how satisfied the user is with the specified value for this objective.
			 * @param value  The value associated with this objective for a particular trial
			 * @return       the user satisfaction for the specified value
			 */
			public double satisfaction( final double score ) {
				return SatisfactionCurve.inverseSquareRisingSatisfaction( score, tolerance );
			}
		};
	}
}
