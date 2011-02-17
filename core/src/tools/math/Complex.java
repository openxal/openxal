//
//  Complex.java
//  xal
//
//  Created by Tom Pelaia on 1/2/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

package gov.sns.tools.math;


/** representation of a complex number */
public class Complex {
	final private double REAL;
	final private double IMAGINARY;
	
		
	/** Primary constructor */
	public Complex( final double realPart, final double imaginaryPart ) {
		REAL = realPart;
		IMAGINARY = imaginaryPart;
	}
	
	
	/** Constructor with pure real number */
	public Complex( final double realPart ) {
		this( realPart, 0.0 );
	}
	
	
	/** Get a string representation of this complex number */
	public String toString() {
		final String operator = IMAGINARY >= 0 ? " + i" : " - i";
		return REAL + operator + Math.abs( IMAGINARY );
	}
	
	
	/** get the real part */
	final public double real() {
		return REAL;
	}
	
	
	/** get the imaginary part */
	final public double imaginary() {
		return IMAGINARY;
	}
	
	
	/** calculate the negative of this complex number */
	final public Complex negate() {
		return new Complex( -REAL, -IMAGINARY );
	}
	
	
	/** modulus of this complex number */
	final public double modulus() {
		return Math.sqrt( REAL * REAL + IMAGINARY * IMAGINARY );
	}
	
	
	/** get the phase */
	final public double phase() {
		return Math.atan2( IMAGINARY, REAL );
	}
	
	
	/** modulus squared of this complex number */
	final public double modulusSquared() {
		return REAL * REAL + IMAGINARY * IMAGINARY;
	}
	
	
	/** complex conjugate */
	final public Complex conjugate() {
		return new Complex( REAL, -IMAGINARY );
	}
	
	
	/** calculate the reciprocal of this complex number */
	final public Complex reciprocal() {
		final double denominator = modulusSquared();
		return new Complex( REAL / denominator, -IMAGINARY / denominator );
	}
	
	
	/** complex multiplaction */
	final public Complex times( final Complex multiplier ) {
		return new Complex( REAL * multiplier.REAL - IMAGINARY * multiplier.IMAGINARY, REAL * multiplier.IMAGINARY + IMAGINARY * multiplier.REAL );
	}
	
	
	/** complex multiplaction */
	final public Complex times( final double multiplier ) {
		return new Complex( REAL * multiplier, IMAGINARY * multiplier );
	}
	
	
	/** complex division */
	final public Complex divide( final Complex divisor ) {
		final double denominator = divisor.modulusSquared();
		final double real = ( REAL * divisor.REAL + IMAGINARY * divisor.IMAGINARY ) / denominator;
		final double imaginary = ( IMAGINARY * divisor.REAL - REAL * divisor.IMAGINARY ) / denominator;
		return new Complex( real, imaginary );
	}
	
	
	/** complex division */
	final public Complex divide( final double divisor ) {
		return new Complex( REAL / divisor, IMAGINARY / divisor );
	}
	
	
	/** complex addition */
	final public Complex plus( final Complex addend ) {
		return new Complex( REAL + addend.REAL, IMAGINARY + addend.IMAGINARY );
	}
	
	
	/** complex addition */
	final public Complex plus( final double addend ) {
		return new Complex( REAL + addend, IMAGINARY );
	}
	
	
	/** complex subtraction */
	final public Complex minus( final Complex subtrahend ) {
		return new Complex( REAL - subtrahend.REAL, IMAGINARY - subtrahend.IMAGINARY );
	}
	
	
	/** complex subtraction */
	final public Complex minus( final double subtrahend ) {
		return new Complex( REAL - subtrahend, IMAGINARY );
	}
}
