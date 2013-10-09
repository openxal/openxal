//
// TestErrorPropagator.java: Source file for 'TestErrorPropagator'
// Project xal
//
// Created by Tom Pelaia II on 9/15/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.math;

import xal.tools.math.differential.*;

import org.junit.*;


/** Test ErrorPropagator */
public class TestErrorPropagator {
    /** maximum error allowed between test and control evaluations */
    final static private double ERROR_TOLERANCE = 1.0e-6;
    
    
    @Test
    public void testSingleVariableErrorPropagation() {
        checkErrorPropagation( 1.0, 0.0 );
        checkErrorPropagation( 1.0, 0.5 );
    }
    
    
    @Test
    public void testTwoVariableErrorPropagation() {
        checkErrorPropagation( 1.0, 0.0, 1.0, 0.0 );
        checkErrorPropagation( 1.0, 0.1, 2.0, 0.5 );
    }
    
    
    /** test error propagation for the specified value and variance */
    static private void checkErrorPropagation( final double xValue, final double xVariance ) {
        final DifferentiableVariable xVariable = DifferentiableOperation.getVariable( "x", xValue );
        
        // variance for 5x is 25 * dx^2
        assertResult( 25 * xVariance, ErrorPropagator.getInstance( xVariable.times( 5 ), xVariable ), xVariance );
        
        // variance for x^2 is (2x)^2 * dx^2
        assertResult( 4 * xValue * xValue * xVariance, ErrorPropagator.getInstance( xVariable.pow( 2 ), xVariable ), xVariance );
    }
    
    
    /** test error propagation for the specified values and variances */
    static private void checkErrorPropagation( final double xValue, final double xVariance, final double yValue, final double yVariance ) {
        final DifferentiableVariable xVariable = DifferentiableOperation.getVariable( "x", xValue );
        final DifferentiableVariable yVariable = DifferentiableOperation.getVariable( "y", yValue );
        
        // variance for x + y is dx^2 + dy^2
        assertResult( xVariance + yVariance, ErrorPropagator.getInstance( xVariable.plus( yVariable ), xVariable, yVariable ), xVariance, yVariance );
        
        // variance for x - y is dx^2 + dy^2
        assertResult( xVariance + yVariance, ErrorPropagator.getInstance( xVariable.minus( yVariable ), xVariable, yVariable ), xVariance, yVariance );
                
        // variance for x * y is y^2 * dx^2 + x^2 * dy^2
        assertResult( xVariance * yValue * yValue  + yVariance * xValue * xValue, ErrorPropagator.getInstance( xVariable.times( yVariable ), xVariable, yVariable ), xVariance, yVariance );
        
        // variance for x / y is dx^2 / y^2 + dy^2 * x^2 / y^4
        assertResult( xVariance / ( yValue * yValue )  + yVariance * xValue * xValue / ( yValue * yValue * yValue * yValue ), ErrorPropagator.getInstance( xVariable.over( yVariable ), xVariable, yVariable ), xVariance, yVariance );
    }
    
    
    /** 
     * Assert true if the test value result matches the control value 
     * @param controlVariance value against which the comparison is made
     */
    static private void assertResult( final double controlVariance, final ErrorPropagator errorPropagator, final double ... sourceVariances ) {
        final double variance = errorPropagator.getVarianceWithSourceVariances( sourceVariances );
        assertResult( variance, controlVariance );
    }
    
    
    /** 
     * Assert true if the test value result matches the control value 
     * @param testValue value to test
     * @param controlValue value against which the comparison is made
     */
    static private void assertResult( final double testValue, final double controlValue ) {
//        System.out.println( "test: " + testValue + ", control: " + controlValue );
        Assert.assertTrue( testValue == controlValue || Math.abs( testValue - controlValue ) < ERROR_TOLERANCE || ( Double.isNaN( testValue ) && Double.isNaN( controlValue ) ) );
    }
}



