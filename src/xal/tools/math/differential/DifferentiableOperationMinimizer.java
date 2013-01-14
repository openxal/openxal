//
// DifferentiableOperationMinimizer.java
// 
//
// Created by Tom Pelaia on 11/14/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.math.differential;

import xal.tools.ArrayTool;

import java.util.*;



/** Perform minimization on a differentiable operation */
public class DifferentiableOperationMinimizer implements Runnable {
    /** generator of random numbers used in search criteria */
    final private Random RANDOM_GENERATOR;
    
    /** operation to minimize */
    final private DifferentiableOperation PENALTY_OPERATION;
    
    /** variables used to minimize the function */
    final private List<BoundedDifferentiableVariable> VARIABLES;
    
    /** gradient operation for the penalty operation */
    final private Gradient GRADIENT_OPERATION;
    
    /** hessian operation for the penalty operation */
    final private Hessian HESSIAN_OPERATION;
    
    /** maximum number of evaluations to evaluate the operation */
    volatile private int _maxEvaluations;
    
    /** best solution found */
    private Trial _bestSolution;
    
    
	/** Primary Constructor */
    public DifferentiableOperationMinimizer( final DifferentiableOperation operation, final List<BoundedDifferentiableVariable> variables, final int maxEvaluations ) {
        RANDOM_GENERATOR = new Random( 0 );
        PENALTY_OPERATION = operation;
        VARIABLES = new ArrayList<BoundedDifferentiableVariable>( variables );
        GRADIENT_OPERATION = new Gradient( PENALTY_OPERATION, VARIABLES );
        HESSIAN_OPERATION = new Hessian( GRADIENT_OPERATION );
        
        _maxEvaluations = maxEvaluations;
        _bestSolution = null;
    }
    
    
	/** Constructor with max evaluations of one */
    public DifferentiableOperationMinimizer( final DifferentiableOperation operation, final List<BoundedDifferentiableVariable> variables ) {
        this( operation, variables, 1 );
    }
   
    
    /** Construct a minimizer from the operation and the variables to explore */
    static public DifferentiableOperationMinimizer getInstance( final DifferentiableOperation operation, final BoundedDifferentiableVariable ... variables ) {
        final List<BoundedDifferentiableVariable> variableList = new ArrayList<BoundedDifferentiableVariable>( variables.length );
        for ( final BoundedDifferentiableVariable variable : variables ) {
            variableList.add( variable );
        }
        return new DifferentiableOperationMinimizer( operation, variableList );
    }
    
    
    /** reset this minimizer */
    public void reset() {
        RANDOM_GENERATOR.setSeed( 0 );
        _bestSolution = null;
    }
    
    
    /** set the maximum number of evaluations of the penalty operation */
    public void setMaxEvaluations( final int evaluations ) {
        _maxEvaluations = evaluations;
    }
    
    
    /** Get the maximum number of iterations to evaluate the operation */
    public int getMaxEvaluations() {
        return _maxEvaluations;
    }
    
    
    /** run the optimization */
    public void run() {
        // if a best solution exists, start with it, otherwise use the default variable values
        if ( _bestSolution == null ) {
            final TrialPoint defaultPoint = new TrialPoint();
            for ( final BoundedDifferentiableVariable variable : VARIABLES ) {
                defaultPoint.setValue( variable, variable.getDefaultValue() );
            }
            _bestSolution = createTrial( defaultPoint );
        }
        
        findNextTrial( _bestSolution, 0 );
    }
    
    
    /** set the maximum number of evaluations and run the optimization */
    public void run( final int maxEvaluations ) {
        setMaxEvaluations( maxEvaluations );
        run();
    }
    
    
    /** Get the penalty for the best solution */
    public double getBestPenalty() {
        return _bestSolution.getPenalty();
    }
    
    
    /** Get the best solution value for the specified variable */
    public double getBestVariableValue( final BoundedDifferentiableVariable variable ) {
        return _bestSolution.getPoint().getValue( variable );
    }
    
    
    /** Get the best solution variable value map */
    public DifferentiableVariableValues getBestVariableValueMap() {
        return _bestSolution.getPoint().getPointMap();
    }
    
    
    /** create a new trial for the point */
    private Trial createTrial( final TrialPoint trialPoint ) {
        return new Trial( trialPoint, getPenalty( trialPoint ) );
    }
    
    
    /** evaluate the operation to minimize at the specified trial point */
    public double getPenalty( final TrialPoint point ) {
        return PENALTY_OPERATION.evaluate( point.getPointMap() );
    }
    
    
    /** Determine the step to take from the initial value to minimize the resulting value given the slope */
    static private double getStepToMinimizeValue( final BoundedDifferentiableVariable variable, final double initialValue, final double slope ) {
        if ( slope != 0.0 ) {
            // to minimize, we need to move opposite the slope
            final double limit = slope > 0.0 ? variable.getLowerLimit() : variable.getUpperLimit();
            return ( initialValue - limit ) / slope;
        }
        else {
            return Double.POSITIVE_INFINITY;
        }
    }
    
    
    /** get the maximum search step along the gradient */
    public double getMaxSearchStep( final TrialPoint initialPoint, final double[] gradient ) {
        double maxStep = Double.POSITIVE_INFINITY;
        final int dimension = gradient.length;
        for ( int index = 0 ; index < dimension ; index++ ) {
            final BoundedDifferentiableVariable variable = VARIABLES.get( index );
            final double initialValue = initialPoint.getValue( variable );
            final double localMaxStep = getStepToMinimizeValue( variable, initialValue, gradient[index] );
            if ( localMaxStep < maxStep )  maxStep = localMaxStep;
        }
        return maxStep;
    }
    
    
    /** determine the next trial to evaluate */
    public Trial findNextTrial( final Trial initialTrial, final int evaluations ) {
        if ( evaluations >= _maxEvaluations )  return initialTrial;
                
        final TrialPoint initialPoint = initialTrial.getPoint();
        final double[] gradient = GRADIENT_OPERATION.evaluate( initialPoint );
        
        final double[][] hessian = HESSIAN_OPERATION.evaluate( initialPoint );
        
        final double gradSquare = Gradient.squareLength( gradient );
        if ( gradSquare == 0.0 || Double.isInfinite( gradSquare ) || Double.isNaN( gradSquare ) ) {
            return findNextRandomTrialWithPowerDistribution( initialTrial, evaluations );
        }
        
        final double curvature = Hessian.getCurvature( gradient, hessian );
        
        double searchStep = 0.0;
        if ( curvature <= 0.0 || Double.isInfinite( curvature ) || Double.isNaN( curvature ) ) {
            searchStep = getMaxSearchStep( initialPoint, gradient );
        }
        else {
            searchStep = gradSquare / curvature;
        }
        
        // search along the gradient
        return findNextTrialAlongGradient( initialTrial, evaluations, searchStep, gradient );
    }
    
    
    /** find the next trial taking the specified step along the gradient */
    public Trial findNextTrialAlongGradient( final Trial initialTrial, final int evaluations, final double step, final double[] gradient ) {
        if ( evaluations >= _maxEvaluations )  return initialTrial;
                
        final TrialPoint initialPoint = initialTrial.getPoint();
        
        final TrialPoint nextPoint = new TrialPoint();
        final int dimension = VARIABLES.size();
        for ( int index = 0 ; index < dimension ; index++ ) {
            final BoundedDifferentiableVariable variable = VARIABLES.get( index );
            final double initialValue = initialPoint.getValue( variable );
            final double value = variable.getNearestBoundedValue( initialValue - step * gradient[index] );
            nextPoint.setValue( variable, value );
        }
        
        final Trial nextTrial = createTrial( nextPoint );        
        
        if ( nextTrial.isBetterThan( initialTrial ) ) {
            _bestSolution = nextTrial;
            return findNextTrial( nextTrial, evaluations + 1 );
        }
        else {
            // move against the gradient with half the step
            return findNextTrialAlongGradient( initialTrial, evaluations + 1, step / 2.0, gradient );
        }
    }
    
    
    /** find the next random trial with the default power distribution */
    public Trial findNextRandomTrialWithPowerDistribution( final Trial initialTrial, final int evaluations ) {
        return findNextRandomTrialWithPowerDistribution( initialTrial, evaluations, 0.5 );
    }
    
    
    /** find the next random trial with the specified power distribution */
    public Trial findNextRandomTrialWithPowerDistribution( final Trial initialTrial, final int evaluations, final double power ) {
        if ( evaluations >= _maxEvaluations )  return initialTrial;
        
        final TrialPoint initialPoint = initialTrial.getPoint();
        final TrialPoint nextPoint = new TrialPoint();
        final int dimension = VARIABLES.size();
        for ( int index = 0 ; index < dimension ; index++ ) {
            final BoundedDifferentiableVariable variable = VARIABLES.get( index );
            final double initialValue = initialPoint.getValue( variable );
            final double value = getRandomValueWithPowerDistribution( initialValue, variable, power );
            nextPoint.setValue( variable, value );
        }
        
        final Trial nextTrial = createTrial( nextPoint );
        
        if ( nextTrial.isBetterThan( initialTrial ) ) {
            _bestSolution = nextTrial;
            return findNextTrial( nextTrial, evaluations + 1 );
        }
        else {
            return findNextRandomTrialWithPowerDistribution( initialTrial, evaluations + 1, power / 2.0 );
        }
    }
    
    
    /** get a random value with the power distribution */
    private double getRandomValueWithPowerDistribution( final double initialValue, final BoundedDifferentiableVariable variable, final double power ) {
        final double lowerRange = initialValue - variable.getLowerLimit();
        final double upperRange = variable.getUpperLimit() - initialValue;
        final double randomLocation = randomValue( 0.0, lowerRange + upperRange );
        double limit = 0.0;
        double rangeFraction = 0.0;
        if ( randomLocation < lowerRange ) {
            limit = variable.getLowerLimit();
            rangeFraction = randomLocation / lowerRange;
        }
        else {
            limit = variable.getUpperLimit();
            rangeFraction = ( randomLocation - lowerRange ) / upperRange;
        }
        
        return initialValue + ( limit - initialValue ) * Math.pow( rangeFraction, 1.0 / power );
    }
    
    
    /** get a random value from a uniform distribution over the range specified */
    private double randomValue( final double lowerLimit, final double upperLimit ) {
        return lowerLimit + ( upperLimit - lowerLimit ) * RANDOM_GENERATOR.nextDouble();
    }
}



/** represents and calculates the gradient operation of a specified operation */
class Gradient {
    /** array of variables corresponding to the gradient components */
    final private List<BoundedDifferentiableVariable> VARIABLES;
    
    /** array of gradient component operations */
    final private DifferentiableOperation[] OPERATION_VECTOR;
    
    
    /** constructor */
    public Gradient( final DifferentiableOperation operation, final List<BoundedDifferentiableVariable> variables ) {
        VARIABLES = variables;
        OPERATION_VECTOR = new DifferentiableOperation[ variables.size() ];
        final int dimension = getDimension();
        for ( int index = 0 ; index < dimension; index++ ) {
            OPERATION_VECTOR[index] = operation.getDerivative( variables.get( index ) );
        }
    }
    
    
    /** get the operation vector */
    public DifferentiableOperation[] getOperationVector() {
        return OPERATION_VECTOR;
    }
    
    
    /** Get the variables */
    public List<BoundedDifferentiableVariable> getVariables() {
        return VARIABLES;
    }
    
    
    /** get the dimension of the gradient */
    public int getDimension() {
        return OPERATION_VECTOR.length;
    }
    
    
    /** square of the metric length of the vector */
    static public double squareLength( final double[] vector ) {
        double squareLength = 0.0;
        for ( int index = 0 ; index < vector.length ; index++ ) {
            final double value = vector[index];
            squareLength += value * value;
        }
        return squareLength;
    }
    
    
    /** evaluate the gradient (returned as array) at the specified point */
    public double[] evaluate( final TrialPoint point ) {
        final DifferentiableVariableValues pointMap = point.getPointMap();
        final int dimension = getDimension();
        final double[] vector = new double[ dimension ];
        for ( int index = 0 ; index < dimension ; index++ ) {
            vector[index] = OPERATION_VECTOR[index].evaluate( pointMap );
        }
        
        return vector;
    }
    
    
    /** Get a string representation of this gradient operation */
    public String toString() {
        return ArrayTool.asString( OPERATION_VECTOR );
    }
}



/** represents and calculates the hessian matrix */
class Hessian {
    /** array of variables corresponding to the gradient components */
    final private List<BoundedDifferentiableVariable> VARIABLES;
    
    /** matrix of hessian component operations */
    final private DifferentiableOperation[][] OPERATION_MATRIX;
    
    
    /** construct the hessian matrix from the gradient */
    public Hessian( final Gradient gradient ) {
        VARIABLES = gradient.getVariables();
        final DifferentiableOperation[] gradientVector = gradient.getOperationVector();
        final int dimension = getDimension();
        OPERATION_MATRIX = new DifferentiableOperation[ dimension ][ dimension ];
        for ( int row = 0 ; row < dimension ; row++ ) {
            final BoundedDifferentiableVariable variable = VARIABLES.get( row );
            OPERATION_MATRIX[row][row] = gradientVector[row].getDerivative( variable );
            for ( int column = 0 ; column < row ; column++ ) {
                OPERATION_MATRIX[column][row] = gradientVector[column].getDerivative( variable );
                OPERATION_MATRIX[row][column] = OPERATION_MATRIX[column][row];
            }
        }
    }
    
    
    /** get the matrix dimension */
    public int getDimension() {
        return VARIABLES.size();
    }
    
    
    /** get the curvature along the gradient: gradient * Hessian * gradient */
    static public double getCurvature( final double[] grad, final double[][] matrix ) {
        double curvature = 0.0;
        final int dimension = grad.length;
        for ( int column = 0 ; column < dimension ; column++ ) {
            curvature += matrix[column][column] * grad[column] * grad[column];  // contribution from the diagonal
            // loop over the upper triangular region and use symmetry (contribution doubles)
            for ( int row = 0 ; row < column ; row++ ) {
                curvature += 2 * matrix[row][column] * grad[row] * grad[column];
            }
        }
        
        return curvature;
    }
    
    
    /** evaluate the hessian matrix at the point and return matrix as an array of arrays */
    public double[][] evaluate( final TrialPoint point ) {
        final DifferentiableVariableValues pointMap = point.getPointMap();
        final int dimension = getDimension();
        final double[][] matrix = new double[dimension][dimension];
        for ( int row = 0 ; row < dimension ; row++ ) {
            matrix[row][row] = OPERATION_MATRIX[row][row].evaluate( pointMap );
            for ( int column = 0 ; column < row ; column++ ) {
                matrix[row][column] = OPERATION_MATRIX[row][column].evaluate( pointMap );
                matrix[column][row] = matrix[row][column];
            }
        }
        
        return matrix;
    }
}



/** Trial representing the evaluated point and the corresponding penalty */
class Trial {
    /** trial point keyed by variable */
    final private TrialPoint POINT;
    
    /** penalty evaluated at the trial point */
    final private double PENALTY;
    
    
    /** Constructor */
    public Trial( final TrialPoint point, final double penalty ) {
        POINT = point;
        PENALTY = penalty;
    }
    
    
    /** get the trial point */
    public TrialPoint getPoint() {
        return POINT;
    }
    
    
    /** Get the penalty */
    public double getPenalty() {
        return PENALTY;
    }
    
    
    /** Determine whether this trial has a lower penalty than the specified point */
    public boolean isBetterThan( final Trial otherTrial ) {
        return PENALTY < otherTrial.PENALTY;
    }
}



/** trial point */
class TrialPoint {
    /** map of assigned variable values */
    final private DifferentiableVariableValues POINT_MAP;
    
    
    /** Constructor */
    public TrialPoint() {
        POINT_MAP = DifferentiableVariableValues.getInstance();
    }
    
    
    /** set the value corresponding to the specified variable */
    public void setValue( final BoundedDifferentiableVariable variable, final double value ) {
        POINT_MAP.assignValue( variable, value );
    }
    
    
    /** get the value for the specified variable */
    public double getValue( final BoundedDifferentiableVariable variable ) {
        return POINT_MAP.getValue( variable );
    }
    
    
    /** get the map of values keyed by variable */
    public DifferentiableVariableValues getPointMap() {
        return POINT_MAP;
    }
    
    
    /** dimension of the point */
    public int dimension() {
        return POINT_MAP.assignmentCount();
    }
}


