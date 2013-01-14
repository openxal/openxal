//
// ErrorPropagator.java: Source file for 'ErrorPropagator'
// Project xal
//
// Created by Tom Pelaia II on 5/4/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.math;

import xal.tools.math.differential.*;
import java.util.Map;
import java.util.HashMap;


/** Propagates errors from independent source variables to the specified operation using standard error propagation. */
public class ErrorPropagator extends java.lang.Object {
    /** operation for which the error propagation will be performed */
    private final DifferentiableOperation VARIANCE_PROPAGATOR;
    
    /** independent variables from which source errors are to be propagated */
    private final DifferentiableVariable[] SOURCE_VARIABLES;
    
    /** table of variables for the signal variances keyed by source variable */
    private final Map<DifferentiableVariable, DifferentiableVariable> VARIANCE_VARIABLES;
    
    
	/** 
     * Constructor 
     * @param baseOperation the operation for which the error propagation will be performed
     * @param sourceVariables independent variables from which source errors are to be propagated
     */
    public ErrorPropagator( final DifferentiableOperation baseOperation, final DifferentiableVariable ... sourceVariables ) {
        SOURCE_VARIABLES = sourceVariables;
        VARIANCE_VARIABLES = new HashMap<DifferentiableVariable,DifferentiableVariable>( sourceVariables.length );
        
        // sum the square of the sensitivity to each source variable times the variance variable
        DifferentiableOperation varianceOperationSum = DifferentiableOperation.getConstant( 0.0 );
        for ( final DifferentiableVariable variable : sourceVariables ) {
            final DifferentiableVariable varianceVariable = DifferentiableOperation.getVariable( variable.getName() + "_Variance", 0.0 );
            VARIANCE_VARIABLES.put( variable, varianceVariable );   // associate the variance variable with the variable
            final DifferentiableOperation sensitivity = baseOperation.getDerivative( variable );
            varianceOperationSum = varianceOperationSum.plus( sensitivity.pow( 2 ).times( varianceVariable ) );
        }
        VARIANCE_PROPAGATOR = varianceOperationSum;
    }
    
    
	/** 
     * Construct an instance of this class 
     * @param baseOperation the operation for which the error propagation will be performed
     * @param sourceVariables independent variables from which source errors are to be propagated
     */
    static public ErrorPropagator getInstance( final DifferentiableOperation baseOperation, final DifferentiableVariable ... sourceVariables ) {
        return new ErrorPropagator( baseOperation, sourceVariables );
    }
    
    
    /** 
     * Set the default variance for the specified source variable
     * @param sourceVariable variable for which to assign the variance
     * @param variance the variance to assign the variable
     */
    public void setSourceVariance( final DifferentiableVariable sourceVariable, final double variance ) {
        if ( sourceVariable == null )  throw new IllegalArgumentException( "ErrorPropagator: The source variable cannot be null." );
            
        final DifferentiableVariable varianceVariable = getVarianceVariable( sourceVariable );
        if ( varianceVariable != null ) {
            varianceVariable.setDefaultValue( variance );
        }
        else {
            throw new IllegalArgumentException( "ErrorPropagator: Attempt to assign a variance to a source variable >> " + sourceVariable + " << which was not given in the propagator constructor." );
        }
    }
    
    
    /** 
     * Set the default variances for the source variables
     * @param sourceVariances the variances to each source in the order they were specified in the constructor
     */
    public void setSourceVariances( final double ... sourceVariances ) {
        if ( sourceVariances.length != SOURCE_VARIABLES.length )  throw new IllegalArgumentException( "ErrorPropagator: The count of variances: " + sourceVariances.length + " must match the count of source variables: " + SOURCE_VARIABLES.length );
        
        for ( int variableIndex = 0 ; variableIndex < sourceVariances.length ; variableIndex++ ) {
            final DifferentiableVariable sourceVariable = SOURCE_VARIABLES[variableIndex];
            setSourceVariance( sourceVariable, sourceVariances[variableIndex] );
        }
    }
    
    
    /** Set a common variance to use as the default for all source variables */
    public void setCommonSourceVariance( final double variance ) {
        for ( final DifferentiableVariable sourceVariable : SOURCE_VARIABLES ) {
            final DifferentiableVariable varianceVariable = getVarianceVariable( sourceVariable );
            varianceVariable.setDefaultValue( variance );
        }
    }
    
    
    /** Set a common sigma to use as the default for all source variables */
    public void setCommonSourceSigma( final double sigma ) {
        setCommonSourceVariance( sigma * sigma );
    }
    
    
    /** 
     * Set the default standard deviation for the specified source variable
     * @param sourceVariable variable for which to assign the variance
     * @param sigma standard deviation to assign the variable
     */
    public void setSourceSigma( final DifferentiableVariable sourceVariable, final double sigma ) {
        setSourceVariance( sourceVariable, sigma * sigma );
    }
    
    
    /** Calculate the operation variance propagated from the default source variances */
    public double getVariance() {
        return VARIANCE_PROPAGATOR.evaluate();
    }
    
    
    /** Calculate the operation variance propagated from a common source variance. Note that this assignment is only for the current calculation and does change the default source variances. */
    public double getVarianceWithCommonSourceVariance( final double sourceVariance ) {
        final DifferentiableVariableValues valueMap = DifferentiableVariableValues.getInstance();
        for ( final DifferentiableVariable sourceVariable : SOURCE_VARIABLES ) {
            final DifferentiableVariable varianceVariable = getVarianceVariable( sourceVariable );
           valueMap.assignValue( varianceVariable, sourceVariance );
        }
        return VARIANCE_PROPAGATOR.evaluate( valueMap );
    }
    
    
    /** Calculate the operation variance propagated from the source variances. Note that this assignment is only for the current calculation and does change the default source variances. */
    public double getVarianceWithSourceVariances( final double ... sourceVariances ) {
        if ( sourceVariances.length != SOURCE_VARIABLES.length )  throw new IllegalArgumentException( "ErrorPropagator: The count of variances: " + sourceVariances.length + " must match the count of source variables: " + SOURCE_VARIABLES.length );
        
        final DifferentiableVariableValues valueMap = DifferentiableVariableValues.getInstance();
        for ( int variableIndex = 0 ; variableIndex < sourceVariances.length ; variableIndex++ ) {
            final DifferentiableVariable sourceVariable = SOURCE_VARIABLES[variableIndex];
            final DifferentiableVariable varianceVariable = getVarianceVariable( sourceVariable );
            valueMap.assignValue( varianceVariable, sourceVariances[variableIndex] );
        }
        
        return VARIANCE_PROPAGATOR.evaluate( valueMap );
    }
    
    
    /** Calculate the operation standard error propagated from the default source variances */
    public double getSigma() {
        final double variance = getVariance();
        return Math.sqrt( variance );
    }
    
    
    /** Get the variance variable for the specified source variable */
    private DifferentiableVariable getVarianceVariable( final DifferentiableVariable sourceVariable ) {
        return VARIANCE_VARIABLES.get( sourceVariable );
    }
}
