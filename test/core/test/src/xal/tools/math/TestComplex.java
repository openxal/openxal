//
//  TestComplex.java
//  xal
//
//  Created by Tom Pelaia on 2/17/2011.
//  Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.math;

import xal.tools.math.Complex;
import org.junit.*;


/** test the complex number class */
public class TestComplex {
    /** real part of the sample */
    static final private double SAMPLE_REAL = 3.0;
    
    /** imaginary part of the sample */
    static final private double SAMPLE_IMAGINARY = 4.0;
    
    /** sample against which to test results */
    final private Complex SAMPLE;
    
    
    /** Constructor */
    public TestComplex() {
        SAMPLE = new Complex( SAMPLE_REAL, SAMPLE_IMAGINARY );
    }
    
    
    @Test
    public void testNegation() {
        final Complex negative = SAMPLE.negate();
        Assert.assertTrue( negative.real() == -SAMPLE_REAL && negative.imaginary() == -SAMPLE_IMAGINARY );
    }
    
    
    @Test
    public void testModulus() {
        final double modulus = 5.0;
        Assert.assertTrue( SAMPLE.modulus() == modulus );
    }
    
    
    @Test
    public void testConjugation() {
        final Complex conjugate = SAMPLE.conjugate();
        Assert.assertTrue( conjugate.real() == SAMPLE_REAL && conjugate.imaginary() == -SAMPLE_IMAGINARY );
    }
}