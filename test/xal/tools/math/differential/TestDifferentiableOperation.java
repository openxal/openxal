//
// DifferentiableOperation.java: Source file for 'DifferentiableOperation'
// Project xal
//
// Created by Tom Pelaia II on 4/29/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.math.differential;

import org.junit.*;


/** Test DifferentiableOperation */
public class TestDifferentiableOperation {
    /** maximum error allowed between test and control evaluations */
    final static private double ERROR_TOLERANCE = 1.0e-6;
    
    
    @Test
    public void testOperationEvaluation() {
        checkOperationEvaluation( -7.2 );
        checkOperationEvaluation( -0.43 );
        checkOperationEvaluation( 0.0 );
        checkOperationEvaluation( 0.27 );
        checkOperationEvaluation( 1.0 );
        checkOperationEvaluation( 3.5 );
    }
    
    
    @Test
    public void testArithmeticEvaluation() {
        checkArithmeticEvaluation( 0.0, 1.0 );
        checkArithmeticEvaluation( 0.0, 2.5 );
        checkArithmeticEvaluation( 3.1, 0.0 );
        checkArithmeticEvaluation( 1.0, 4.2 );
        checkArithmeticEvaluation( 1.0, -6.3 );
        checkArithmeticEvaluation( 8.3, 1.0 );
        checkArithmeticEvaluation( 3.5, -7.2 );
        checkArithmeticEvaluation( -3.5, 7.2 );
    }
    
    
    @Test
    public void testDerivativeEvaluation() {
        checkDerivativeEvaluation( 0.0 );
        checkDerivativeEvaluation( 1.0 );
        checkDerivativeEvaluation( 2.3 );
        checkDerivativeEvaluation( -7.8 );
        checkDerivativeEvaluation( 8.6 );
    }
    
    
    @Test
    public void testPartialDerivativeEvaluation() {
        checkPartialDerivativeEvaluation( 0.0, 0.0, 0.0 );
        checkPartialDerivativeEvaluation( 1.0, 1.0, 1.0 );
        checkPartialDerivativeEvaluation( 2.3, -3.4, 7.9 );
        checkPartialDerivativeEvaluation( 7.4, 6.4, 4.0 );
        checkPartialDerivativeEvaluation( -4.4, 3.2, -7.0 );
    }
    
    
    /** test arithmetic evaluation for the specified variable values */
    static private void checkArithmeticEvaluation( final double xValue, final double yValue ) {
        final DifferentiableVariable xVar = DifferentiableOperation.getVariable( "x", xValue );
        final DifferentiableVariable yVar = DifferentiableOperation.getVariable( "y", yValue );
        
        assertResult( xVar.plus( yVar ).evaluate(), xValue + yValue );
        assertResult( xVar.minus( yVar ).evaluate(), xValue - yValue );
        assertResult( xVar.times( yVar ).evaluate(), xValue * yValue );
        assertResult( xVar.over( yVar ).evaluate(), xValue / yValue );
    }
    
    
    /** test operation evaluation for the specified variable value */
    static private void checkOperationEvaluation( final double xValue ) {
        final DifferentiableVariable xVar = DifferentiableOperation.getVariable( "x", xValue );
        
        assertResult( xVar.evaluate(), xValue );
        assertResult( xVar.abs().evaluate(), Math.abs( xValue ) );
        assertResult( xVar.negate().evaluate(), - xValue );
        assertResult( xVar.reciprocal().evaluate(), 1.0 / xValue );
        assertResult( xVar.pow( 2 ).evaluate(), xValue * xValue );
        assertResult( xVar.pow( 5.3 ).evaluate(), Math.pow( xValue, 5.3 ) );
        assertResult( xVar.exp().evaluate(), Math.exp( xValue ) );
        assertResult( xVar.log().evaluate(), Math.log( xValue ) );
        assertResult( xVar.sqrt().evaluate(), Math.sqrt( xValue ) );
        assertResult( xVar.sin().evaluate(), Math.sin( xValue ) );
        assertResult( xVar.cos().evaluate(), Math.cos( xValue ) );
        assertResult( xVar.tan().evaluate(), Math.tan( xValue ) );
        assertResult( xVar.asin().evaluate(), Math.asin( xValue ) );
        assertResult( xVar.acos().evaluate(), Math.acos( xValue ) );
        assertResult( xVar.atan().evaluate(), Math.atan( xValue ) );
        assertResult( xVar.sinh().evaluate(), Math.sinh( xValue ) );
        assertResult( xVar.cosh().evaluate(), Math.cosh( xValue ) );
        assertResult( xVar.tanh().evaluate(), Math.tanh( xValue ) );
    }
    
    
    /** test single derivative evaluation for the specified variable value */
    static private void checkDerivativeEvaluation( final double xValue ) {
        final DifferentiableVariable xVar = DifferentiableOperation.getVariable( "x", xValue );
        
        // test polynomials
        assertResult( xVar.plus( 3.2 ).getDerivative( xVar ).evaluate(), 1.0 );
        assertResult( xVar.pow( 2 ).plus( xVar.times( 3 ) ).plus( 7 ).getDerivative( xVar ).evaluate(), 2 * xValue + 3 );
        
        // test transcendentals
        assertResult( xVar.plus( 1.0 ).exp().getDerivative( xVar ).evaluate(), Math.exp( xValue + 1.0 ) );
        assertResult( xVar.sin().getDerivative( xVar ).evaluate(), Math.cos( xValue ) );
        assertResult( xVar.cos().getDerivative( xVar ).evaluate(), - Math.sin( xValue ) );
        assertResult( xVar.log().getDerivative( xVar ).evaluate(), 1.0 / xValue );
        
        // test chain rule
        assertResult( xVar.plus( 1 ).pow( 2 ).exp().getDerivative( xVar ).evaluate(), 2 * ( xValue + 1 ) * Math.exp( ( xValue + 1 ) * ( xValue + 1 ) ) );
    }
    
    
    /** test partial derivative evaluation for the specified variable values */
    static private void checkPartialDerivativeEvaluation( final double xValue, final double yValue, final double zValue ) {
        final DifferentiableVariable xVar = DifferentiableOperation.getVariable( "x", xValue );
        final DifferentiableVariable yVar = DifferentiableOperation.getVariable( "y", yValue );
        final DifferentiableVariable zVar = DifferentiableOperation.getVariable( "z", zValue );
        
        // test single order partial differentials
        assertResult( xVar.plus( yVar, zVar ).getDerivative( xVar ).evaluate(), 1.0 );
        assertResult( xVar.times( yVar, zVar ).getDerivative( yVar ).evaluate(), xValue * zValue );
        assertResult( xVar.plus( yVar ).exp().getDerivative( yVar ).evaluate(), Math.exp( xValue + yValue ) );
        
        // test mixed differentials
        assertResult( xVar.times( yVar ).times( zVar ).getDerivative( xVar ).getDerivative( yVar ).evaluate(), zValue );
        assertResult( xVar.times( yVar ).times( zVar ).getDerivative( xVar ).getDerivative( yVar ).getDerivative( zVar ).evaluate(), 1.0 );
        assertResult( xVar.times( yVar.plus( zVar ) ).sin().getDerivative( xVar ).getDerivative( yVar ).evaluate(), Math.cos( xValue * ( yValue + zValue ) ) - xValue * ( yValue + zValue ) * Math.sin( xValue * ( yValue + zValue ) ) );
    }
    
    
    /** 
     * Assert true if the test value result matches the control value 
     * @param testValue value to test
     * @param controlValue value against which the comparison is made
     */
    static private void assertResult( final double testValue, final double controlValue ) {
        Assert.assertTrue( testValue == controlValue || Math.abs( testValue - controlValue ) < ERROR_TOLERANCE || ( Double.isNaN( testValue ) && Double.isNaN( controlValue ) ) );
    }
}



