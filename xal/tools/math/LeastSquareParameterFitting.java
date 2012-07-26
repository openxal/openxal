//
// LeastSquareParameterFitting.java
// 
//
// Created by Tom Pelaia on 12/13/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.math;

import xal.tools.math.differential.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


/** Least Squares parameter fitting tool which takes a differentiable operation as a model. */
public class LeastSquareParameterFitting implements Runnable {
    /** independent (sampled) variable (typically "x") */
    final public static DifferentiableVariable INDEPENDENT_VARIABLE;

    /** dependent (measured) variable (typically "y") */
    final private static DifferentiableVariable DEPENDENT_VARIABLE;
    
    /** sigma variable representing the statistical measurement error */
    final private static DifferentiableVariable SIGMA_VARIABLE;
    
    /** parameters to fit */
    final private BoundedDifferentiableVariable[] PARAMETERS;
    
    /** model for which to fit the data */
    final private DifferentiableOperation MODEL;
    
    /** data samples with which to fit the model */
    final private List<DataSample> DATA_SAMPLES;
    
    /** maximum number of evaluations to perform for each run */
    private int _maxEvaluations;
    
    /** minimizer */
    private DifferentiableOperationMinimizer _minimizer;
    
    
    // static initialization
    static {
        INDEPENDENT_VARIABLE = new DifferentiableVariable( "x", 0.0 );
        DEPENDENT_VARIABLE = new DifferentiableVariable( "y", 0.0 );
        SIGMA_VARIABLE = new DifferentiableVariable( "\u03C3", 0.0 );
    }
    
    
	/** Constructor */
    public LeastSquareParameterFitting( final DifferentiableOperation model, final BoundedDifferentiableVariable ... parameters ) {
        MODEL = model;
        PARAMETERS = parameters;
        DATA_SAMPLES = new ArrayList<DataSample>();
        
        _maxEvaluations = 5;
    }
    
    
    /** 
     * Add a data sample including statistical error (either all or none must specify error)
     * @param x independent variable value
     * @param y dependent (measured) variable value
     * @param sigma statistical measurement error
     */
    public void addSample( final double x, final double y, final double sigma ) {
        DATA_SAMPLES.add( new DataSample( x, y, sigma ) );
        _minimizer = null;
    }
    
    
    /** 
     * Add a data sample without specifying statistical error (either all or none must specify error)
     * @param x independent variable value
     * @param y dependent (measured) variable value
     */
    public void addSample( final double x, final double y ) {
        addSample( x, y, 1.0 );
    }
    
    
    /** clear samples */
    public void clear() {
        DATA_SAMPLES.clear();
        _minimizer = null;
    }
    
    
    /** get the maximum number of model evaluations for a run */
    public int getMaxEvaluations() {
        return _maxEvaluations;
    }
    
    
    /** set the maximum number of model evaluations for a run */
    public void setMaxEvaluations( final int evaluations ) {
        _maxEvaluations = evaluations;
        if ( _minimizer != null ) {
            _minimizer.setMaxEvaluations( evaluations );
        }
    }
    
    
    /** run the minimizer to find the best parameter fit for the model to the samples */
    public void run() throws java.lang.IllegalStateException {
        if ( DATA_SAMPLES.size() < PARAMETERS.length ) {
            throw new IllegalStateException( "At least " + PARAMETERS.length + " samples are needed, but only " + DATA_SAMPLES.size() + " were supplied." );
        }
        
        if ( _minimizer == null ) {
            _minimizer = createMinimizer();
        }
        
        _minimizer.run();
    }
    
    
    /** set the maximum evaluations and run */
    public void runFor( final int evaluations ) {
        setMaxEvaluations( evaluations );
        run();
    }
    
    
    /** get the penalty for the best fit found */
    public double getFitPenalty() {
        if ( _minimizer != null ) {
            return _minimizer.getBestPenalty();
        }
        else {
            throw new IllegalStateException( "Can't get fit penalty since the fitting has either been cleared or not run." );
        }
    }
    
    
    /** get the value of the parameter for the best fit found */
    public double getFitValueForParameter( final BoundedDifferentiableVariable parameter ) {
        if ( _minimizer != null ) {
            return _minimizer.getBestVariableValue( parameter );
        }
        else {
            throw new IllegalStateException( "Can't get fit parameter values since the fitting has either been cleared or not run." );
        }
    }
    
    
    /** Construct the minimizer */
    private DifferentiableOperationMinimizer createMinimizer() {
        final List<BoundedDifferentiableVariable> parameters = new ArrayList<BoundedDifferentiableVariable>( PARAMETERS.length );
        for ( final BoundedDifferentiableVariable parameter : PARAMETERS ) {
            parameters.add( parameter );
        }
        
        final DifferentiableOperation penaltyOperation = createPenaltyOperation();
        final DifferentiableOperationMinimizer minimizer = new DifferentiableOperationMinimizer( penaltyOperation, parameters );
        minimizer.setMaxEvaluations( _maxEvaluations );
        return minimizer;
    }
    
    
    /** Construct the penalty operation */
    private DifferentiableOperation createPenaltyOperation() {
        // ( ( y - f(x, param) ) / sigma )^2
        final DifferentiableOperation statisticalError = DEPENDENT_VARIABLE.minus( MODEL ).over( SIGMA_VARIABLE ).pow( 2 );
        
        // ( y - f(x, param) )^2
        final DifferentiableOperation basicError = DEPENDENT_VARIABLE.minus( MODEL ).pow( 2 );
        
        DifferentiableOperation penaltyOperation = DifferentiableOperation.getConstant( 0.0 );   // initialize penalty to zero
        for ( final DataSample sample : DATA_SAMPLES ) {
            final Map<DifferentiableVariable,DifferentiableOperation> substitution = new HashMap<DifferentiableVariable,DifferentiableOperation>( DATA_SAMPLES.size() );
            substitution.put( INDEPENDENT_VARIABLE, DifferentiableOperation.getConstant( sample.X ) );
            substitution.put( DEPENDENT_VARIABLE, DifferentiableOperation.getConstant( sample.Y ) );
            substitution.put( SIGMA_VARIABLE, DifferentiableOperation.getConstant( sample.SIGMA ) );
            final DifferentiableOperation errorOperation = sample.SIGMA == 1.0 ? basicError : statisticalError;
            penaltyOperation = penaltyOperation.plus( errorOperation.copyWithSubstitutions( substitution ) );
        }
                
        return penaltyOperation;
    }
}



/** data sample */
class DataSample {
    /** value of the independent variable */
    final public double X;
    
    /** (measured) value of the dependent variable */
    final public double Y;
    
    /** statistical measurement error */
    final public double SIGMA;
    
    
    /** Primary Constructor */
    public DataSample( final double x, final double y, final double sigma ) {
        X = x;
        Y = y;
        SIGMA = sigma;
    }
    
    
    /** Constructor with non-zero constant stastical error when it is not known for any sample */
    public DataSample( final double x, final double y ) {
        this( x, y, 1.0 );
    }
}