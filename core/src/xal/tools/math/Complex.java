//
//  Complex.java
//  xal
//
//  Created by Tom Pelaia on 1/2/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

package xal.tools.math;


/** 
 * <p>
 * Representation of a complex number
 * </p>
 * <p>
 * <h4>CKA NOTES:</h4>
 * &middot;  I added a few methods to this class as I need additional operations for complex numbers.  These
 * methods are clearly identified in case there are issues.
 * <br/>
 * &middot; I changed class attribute names from <code>REAL</code> and <code>IMAGINARY</code> to
 * <code>dblReal</code> and <code>dblImag</code> because Eclipse kept yelling at me for having
 * capitalized variable names (a violation of "convention").  There are no other compelling reasons
 * to keep the current names and should be switched back if seen fit.  
 * </p> 
 * 
 * @author tp6
 * @version Sep 30, 2015, Christopher Allen
 */
public class Complex {
    

    
    /*
     * Global Constants
     */
    
    /** The complex value zero */
    public static final Complex         ZERO = new Complex(0.0, 0.0);
    
    /** the real unit */
    public static final Complex         ONE = new Complex(1.0, 0.0);
    
    /** the imaginary unit */
    public static final Complex         IUNIT = new Complex(0.0, 1.0);

    
    
    /*
     * Global Operations
     */
    
    /**
     * Computes the complex square root of this complex number <i>s</i>.
     * 
     * @param   s   complex number on which to operate
     * 
     * @return value of &radic;<i>s</i> 
     *
     * @since  Sep 30, 2015,   Christopher K. Allen
     */
    public static Complex sqrt(final Complex s) {
        Complex csqrt;
        double dX, dY, dW, dR;
        
        final double dblReal = s.real();
        final double dblImag = s.imaginary();

        if((dblReal == 0) && (dblImag == 0.0)) {
            csqrt = new Complex(0.0, 0.0);
            return (csqrt);
        } 

        dX = Math.abs(dblReal);
        dY = Math.abs(dblImag);

        if( dX >= dY ) {
            dR = dY/dX;
            dW = Math.sqrt(dX)*Math.sqrt(0.5*(1.0 + Math.sqrt(1+dR*dR)));
        } else {
            dR = dX/dY;
            dW = Math.sqrt(dY)*Math.sqrt(0.5*(dR + Math.sqrt(1+dR*dR)));
        }

        if(dblReal >= 0.0) {
            csqrt = new Complex(dW, dblImag/(2.0*dW));
        } else {
            double dblIm = (dblImag > 0.0) ? dW : -dW;
            double dblRe = dblImag/(2.0*dblIm);
            csqrt = new Complex( dblRe, dblIm);
        }

        return (csqrt);
    }

    /**
     * <p>
     * Compute and return the natural logarithm of the given complex number <i>s</i>.
     * The value is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; log(<i>s</i>) = ln(|<i>s</i>|) + <i>i</i> arg(<i>s</i>)
     * <br/>
     * <br/>
     * where ln is the real-valued natural logarithm function and arg is the angle
     * of <i>s</i> in the complex plane.
     * </p>
     * 
     * @param   s   complex number on which to operate
     * 
     * @return     the value of ln(<i>s</i>) &in; &Copf;
     *
     * @since  Sep 30, 2015,   Christopher K. Allen
     */
    public static Complex log(final Complex s) {
        double dblMag = Math.log( s.modulus() );
        double dblAng = s.phase();
        
        Complex    cpxLog = new Complex(dblMag, dblAng);
        
        return cpxLog;
    }
    
    /**
     * <p>
     * Compute and return the exponential of the given complex number <i>s</i>. The
     * value is given by the formula
     * <br/>
     * <br/>
     * &nbsp; &nbsp; exp(<i>s</i>) = exp(&sigma;)[cos(&omega;) + <i>i</i> sin(&omega;)]
     * <br/>
     * <br/>
     * where <i>s</i> = &sigma; + <i>i</i>&omega; and &sigma;, &omega; &in; &Ropf;.
     * </p>
     * 
     * @param   s   complex number on which to operate
     * 
     * @return this result of exponentiating this complex number
     *
     * @since  Sep 30, 2015,   Christopher K. Allen
     */
    public static Complex exp(final Complex s) {
        double  dblMag = Math.exp( s.real() );
        Complex cpxPhs = new Complex( Math.cos(s.imaginary()), Math.sin(s.imaginary())); 
        
        return cpxPhs.times(dblMag);
    }
    
    /**
     * Computes and returns the complex number <i>z</i> on the unit circle corresponding
     * to the mapping of the given real number by the Euler formula.  The returned
     * values is given by 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>z</i> = cos &theta; + <i>i</i> sin &theta; ,
     * <br/>
     * <br/>
     * where &theta; is the real number given in the argument.
     * 
     * @param ang       the real number &theta; (in radians)
     * 
     * @return          the value of <i>z</i> described above
     *
     * @since  Oct 7, 2015,   Christopher K. Allen
     */
    public static Complex   euler(final double ang) {
        Complex cpxAng = new Complex(Math.cos(ang), Math.sin(ang));
        
        return cpxAng;
    }
    
    /**
     * <p>
     * Compute and return the trigonometric sine function of the given complex number <i>s</i>.
     * The formula for the returned value is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; sin(s) = sin(&sigma;)cosh(&omega;) - <i>i</i> cos(&sigma;)sinh(&omega;)
     * <br/>
     * <br/>
     * where 
     * where <i>s</i> = &sigma; + <i>i</i>&omega; and &sigma;, &omega; &in; &Ropf;.
     * </p>
     * 
     * @param   s   complex number on which to operate
     * 
     * @return  the complex sine of the argument
     *
     * @since  Sep 30, 2015,   Christopher K. Allen
     */
    public static Complex sin(final Complex s) {
        double dblRe = +Math.sin(s.real()) * Math.cosh(s.imaginary());
        double dblIm = -Math.cos(s.real()) * Math.sinh(s.imaginary());
        
        Complex cpxSin = new Complex(dblRe, dblIm);
        
        return cpxSin;
    }

    /**
     * <p>
     * Compute and return the hyperbolic sine function of the given complex number <i>s</i>.
     * The formula for the returned value is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; sinh(s) = sinh(&sigma;)cos(&omega;) + <i>i</i> cosh(&sigma;)sin(&omega;)
     * <br/>
     * <br/>
     * where 
     * where <i>s</i> = &sigma; + <i>i</i>&omega; and &sigma;, &omega; &in; &Ropf;.
     * </p>
     * 
     * @param   s   complex number on which to operate
     * 
     * @return  the complex hyberbolic sine of the argument
     *
     * @since  Sep 30, 2015,   Christopher K. Allen
     */
    public static Complex sinh(final Complex s) {
        double dblRe = +Math.sinh(s.real()) * Math.cos(s.imaginary());
        double dblIm = +Math.cosh(s.real()) * Math.sin(s.imaginary());
        
        Complex cpxSinh = new Complex(dblRe, dblIm);
        
        return cpxSinh;
    }

    /**
     * <p>
     * Compute and return the trigonometric cossine function of the given complex number <i>s</i>.
     * The formula for the returned value is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; cos(s) = cos(&sigma;)cosh(&omega;) - <i>i</i> sin(&sigma;)sinh(&omega;)
     * <br/>
     * <br/>
     * where 
     * where <i>s</i> = &sigma; + <i>i</i>&omega; and &sigma;, &omega; &in; &Ropf;.
     * </p>
     * 
     * @param   s   complex number on which to operate
     * 
     * @return  the complex cosine of the argument
     *
     * @since  Sep 30, 2015,   Christopher K. Allen
     */
    public static Complex cos(final Complex s) {
        double dblRe = +Math.cos(s.real()) * Math.cosh(s.imaginary());
        double dblIm = -Math.sin(s.real()) * Math.sinh(s.imaginary());
        
        Complex cpxSin = new Complex(dblRe, dblIm);
        
        return cpxSin;
    }

    /**
     * <p>
     * Compute and return the hyperbolic cosine function of the given complex number <i>s</i>.
     * The formula for the returned value is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; cosh(s) = cosh(&sigma;)cos(&omega;) + <i>i</i> sinh(&sigma;)sin(&omega;)
     * <br/>
     * <br/>
     * where 
     * where <i>s</i> = &sigma; + <i>i</i>&omega; and &sigma;, &omega; &in; &Ropf;.
     * </p>
     * 
     * @param   s   complex number on which to operate
     * 
     * @return  the complex hyberbolic sine of the argument
     *
     * @since  Sep 30, 2015,   Christopher K. Allen
     */
    public static Complex cosh(final Complex s) {
        double dblRe = +Math.cosh(s.real()) * Math.cos(s.imaginary());
        double dblIm = +Math.sinh(s.real()) * Math.sin(s.imaginary());
        
        Complex cpxSinh = new Complex(dblRe, dblIm);
        
        return cpxSinh;
    }




    
    /*
     * Local Attributes
     */
    
    /** real part of the complex number */
	final private double dblReal;
	
	/** imaginary part of the complex number */
	final private double dblImag;
	
		
	/*
	 * Initialization
	 */
		
	/** Primary constructor */
	public Complex( final double realPart, final double imaginaryPart ) {
		dblReal = realPart;
		dblImag = imaginaryPart;
	}
	
	
	/** Constructor with pure real number */
	public Complex( final double realPart ) {
		this( realPart, 0.0 );
	}
	
	
	/*
	 * Object Overrides
	 */
	
	/** Get a string representation of this complex number */
	@Override
	public String toString() {
		final String operator = dblImag >= 0 ? " + i" : " - i";
		return dblReal + operator + Math.abs( dblImag );
	}
	
	
	/*
	 * Complex Field
	 */
	
	/** get the real part */
	final public double real() {
		return dblReal;
	}
	
	
	/** get the imaginary part */
	final public double imaginary() {
		return dblImag;
	}
	
	
	/** modulus of this complex number */
	final public double modulus() {
		return Math.sqrt( dblReal * dblReal + dblImag * dblImag );
	}
	
	
	/** get the phase */
	final public double phase() {
		return Math.atan2( dblImag, dblReal );
	}
	
	
	/** modulus squared of this complex number */
	final public double modulusSquared() {
		return dblReal * dblReal + dblImag * dblImag;
	}
	
	
	/** complex conjugate */
	final public Complex conjugate() {
		return new Complex( dblReal, -dblImag );
	}
	
	
	/*
	 * Algebraic Operations
	 */
	
	/** calculate the reciprocal of this complex number */
	final public Complex reciprocal() {
		final double denominator = modulusSquared();
		return new Complex( dblReal / denominator, -dblImag / denominator );
	}
	
    /** complex multiplication */
	final public Complex times( final Complex multiplier ) {
		return new Complex( dblReal * multiplier.dblReal - dblImag * multiplier.dblImag, dblReal * multiplier.dblImag + dblImag * multiplier.dblReal );
	}
	
	
	/** complex multiplication */
	final public Complex times( final double multiplier ) {
		return new Complex( dblReal * multiplier, dblImag * multiplier );
	}
	
	
	/** complex division */
	final public Complex divide( final Complex divisor ) {
		final double denominator = divisor.modulusSquared();
		final double real = ( dblReal * divisor.dblReal + dblImag * divisor.dblImag ) / denominator;
		final double imaginary = ( dblImag * divisor.dblReal - dblReal * divisor.dblImag ) / denominator;
		return new Complex( real, imaginary );
	}
	
	/** complex division */
	final public Complex divide( final double divisor ) {
		return new Complex( dblReal / divisor, dblImag / divisor );
	}
	
	
	/** complex addition */
	final public Complex plus( final Complex addend ) {
		return new Complex( dblReal + addend.dblReal, dblImag + addend.dblImag );
	}
	
	/** complex addition */
	final public Complex plus( final double addend ) {
		return new Complex( dblReal + addend, dblImag );
	}
	
    /** calculate the negative of this complex number */
    final public Complex negate() {
        return new Complex( -dblReal, -dblImag );
    }

	/** complex subtraction */
	final public Complex minus( final Complex subtrahend ) {
		return new Complex( dblReal - subtrahend.dblReal, dblImag - subtrahend.dblImag );
	}
	
	
	/** complex subtraction */
	final public Complex minus( final double subtrahend ) {
		return new Complex( dblReal - subtrahend, dblImag );
	}
	
	
}
