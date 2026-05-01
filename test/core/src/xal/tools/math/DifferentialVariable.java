/*
* DifferentialVariable.java
*
* Created by t6p on 11/8/2010
*
* Copyright (c) 2010 Spallation Neutron Source
* Oak Ridge National Laboratory
* Oak Ridge, TN 37830
*/

package xal.tools.math;


/**
* Represents an immutable value and its differentials relative to independent variables and performs math operations on them in support of error propagation.
* @author  t6p
*/
public class DifferentialVariable {
	/** representation of the constant zero */
	static final public DifferentialVariable ZERO = newConstant( 0.0 );

	/** representation of the constant one */
	static final public DifferentialVariable ONE = newConstant( 1.0 );

    /** offset of first independent variable for this differential relative to the array of all independent variables */
    final private int OFFSET;
    
    /** differentials */
    final private double[] DERIVATIVES;
    
    /** value of the variable */
    final private double VALUE;
    
    
    /** 
     * Primary Constructor 
     * @param value the value of the variable
     * @param offset  offset of this differential's first independent variable relative to all independent variables
     * @param derivatives  array of derivatives beginning with this differential's first independent variable
     */
    public DifferentialVariable( final double value, final int offset, final double ... derivatives ) {
        OFFSET = offset;
        VALUE = value; 
        DERIVATIVES = derivatives;
    }
    
    
    /** 
     * Constructor with zero offset 
     * @param value the value of the variable
     * @param derivatives  array of derivatives beginning with the first independent variable
     */
    static public DifferentialVariable getInstance( final double value, final double ... derivatives ) {
        return new DifferentialVariable( value, 0, derivatives );
    }
	
	
	/** get a constant value */
	static public DifferentialVariable newConstant( final double value ) {
		return new DifferentialVariable( value, 0, 0.0 );
	}
    
    
    /** get the value */
    public double getValue() {
        return VALUE;
    }
    
    
    /** get the derivative for the independent variable at the specified index */
    public double getDerivative( final int index ) {
        final int localIndex = index - OFFSET;
        return localIndex < 0 ? 0.0 : localIndex < DERIVATIVES.length ? DERIVATIVES[ localIndex ] : 0.0;
    }
    
    
    /** calculate the variance for this variable given a common variance for each independent variable */
    public double varianceWithSignalVariance( final double commonVariance ) {
        double sumSquareDerivatives = 0.0;
        for ( final double derivative : DERIVATIVES ) {
            sumSquareDerivatives += derivative * derivative;
        }
        
        return commonVariance * sumSquareDerivatives;
    }
    
    
    /** calculate the variance for this variable given variances for all independent variables */
    public double varianceWithSignalVariances( final double ... variances ) {
        return varianceWithSignalVariances( 0, variances );
    }
    
    
    /** calculate the variance for this variable given the independent variables starting at the specified offset among all independent variables */
    public double varianceWithSignalVariances( final int offset, final double ... variances ) {
        double variance = 0.0;
        int vindex = OFFSET - offset;
        for ( int index = OFFSET ; index < DERIVATIVES.length ; index++ ) {
            final double derivative = getDerivative( index );
            variance += derivative * derivative * variances[ vindex ];
            ++vindex;
        }
        
        return variance;
    }
    
    
    /** perform the addition operation between a variable and a scalar value */
    static public DifferentialVariable add( final DifferentialVariable variable, final double value ) {
        return new DifferentialVariable( variable.VALUE + value, variable.OFFSET, variable.DERIVATIVES );
    }
    
    
    /** perform the addition operation between a variable and a scalar value */
    static public DifferentialVariable add( final double value, final DifferentialVariable variable ) {
        return DifferentialVariable.add( variable, value );
    }
    
    
    /** perform the addition operation between two variables */
    static public DifferentialVariable add( final DifferentialVariable ... addends ) {
        double value = 0.0;
        int offset = Integer.MAX_VALUE;
        int maxIndex = 0;
        for ( final DifferentialVariable addend : addends ) {
            value += addend.VALUE;
            if ( addend.OFFSET < offset )  offset = addend.OFFSET;
            final int topAddendIndex = addend.OFFSET + addend.DERIVATIVES.length - 1;   // index of the addend's last independent variable
            if ( topAddendIndex > maxIndex )  maxIndex = topAddendIndex;
        }
        
        final double[] derivatives = new double[ maxIndex - offset + 1 ];
        
        for ( int index = offset ; index <= maxIndex ; index++ ) {
            double derivative = 0.0;
            for ( final DifferentialVariable addend : addends ) {
                derivative += addend.getDerivative( index );
            }
            derivatives[ index - offset ] = derivative;
        }
        
        return new DifferentialVariable( value, offset, derivatives );
    }

    
    /** subtract the scalar value from the variable */
    static public DifferentialVariable subtract( final DifferentialVariable variable, final double subtrahend ) {
        return DifferentialVariable.add( variable, - subtrahend );
    }

    
    /** subtract the variable from the scalar value */
    static public DifferentialVariable subtract( final double value, final DifferentialVariable variable ) {
        return DifferentialVariable.add( variable.negate(), value );
    }
    
    
    /** subtract the subtrahend variable from the minuend variable */
    static public DifferentialVariable subtract( final DifferentialVariable minuend, final DifferentialVariable subtrahend ) {
        return DifferentialVariable.add( minuend, subtrahend.negate() );
    }
    
    
    /** perform the multiplication operation between a variable and a scalar value */
    static public DifferentialVariable multiply( final DifferentialVariable variable, final double value ) {
        final double[] derivatives = new double[ variable.DERIVATIVES.length ];
        for ( int index = 0 ; index < derivatives.length ; index++ ) {
            derivatives[ index ] = value * variable.DERIVATIVES[ index ];
        }
        return new DifferentialVariable( variable.VALUE * value, variable.OFFSET, derivatives );
    }
    
    
    /** perform the multiplication operation between a variable and a scalar value */
    static public DifferentialVariable multiply( final double value, final DifferentialVariable variable ) {
        return DifferentialVariable.multiply( variable, value );
    }
    
    
    /** multiply the variables */
    static public DifferentialVariable multiply( final DifferentialVariable ... multiplicands ) {
        double value = 1.0;
        int offset = Integer.MAX_VALUE;
        int maxIndex = 0;
        final double[] sensitivities = new double[ multiplicands.length ];
        for ( int termIndex = 0 ; termIndex < multiplicands.length ; termIndex++ ) {
            final DifferentialVariable multiplicand = multiplicands[ termIndex ];
            value *= multiplicand.VALUE;
            if ( multiplicand.OFFSET < offset )  offset = multiplicand.OFFSET;
            final int topMultiplicandIndex = multiplicand.OFFSET + multiplicand.DERIVATIVES.length - 1;   // index of the multiplicand's last independent variable
            if ( topMultiplicandIndex > maxIndex )  maxIndex = topMultiplicandIndex;
            
            // special care must be taken if the value is zero since the differential is multiplied by the product of all other values
            if ( multiplicand.VALUE == 0.0 ) {
                double sensitivity = 1.0;
                for ( int otherIndex = 0 ; otherIndex < multiplicands.length ; otherIndex++ ) {
                    if ( otherIndex != termIndex )  sensitivity *= multiplicands[ otherIndex ].VALUE;
                }
                sensitivities[ termIndex ] = sensitivity;
            }
        }
        
        final double[] derivatives = new double[ maxIndex - offset + 1 ];
        
        for ( int index = offset ; index <= maxIndex ; index++ ) {
            double derivative = 0.0;
            for ( int termIndex = 0 ; termIndex < multiplicands.length ; termIndex++ ) {
                final DifferentialVariable multiplicand = multiplicands[ termIndex ];
                if ( multiplicand.VALUE != 0.0 ) {
                    derivative += value * multiplicand.getDerivative( index ) / multiplicand.VALUE;
                }
                else {
                    derivative += sensitivities[ termIndex ] * multiplicand.getDerivative( index );
                }
            }
            derivatives[ index - offset ] = derivative;
        }
        
        return new DifferentialVariable( value, offset, derivatives );
    }
    
    
    /** divide the variable by the scalar value */
    static public DifferentialVariable divide( final DifferentialVariable variable, final double value ) {
        return DifferentialVariable.multiply( variable, 1 / value );
    }
    
    
    /** divide the scalar value by the variable */
    static public DifferentialVariable divide( final double value, final DifferentialVariable variable ) {
        return DifferentialVariable.multiply( variable.reciprocal(), value );
    }
    
    
    /** divide the dividend by the divisor */
    static public DifferentialVariable divide( final DifferentialVariable dividend, final DifferentialVariable divisor ) {
        return DifferentialVariable.multiply( dividend, divisor.reciprocal() );
    }
    
    
    /** negate the variable */
    public DifferentialVariable negate() {
        final double[] derivatives = new double[ DERIVATIVES.length ];
        for ( int index = 0 ; index < derivatives.length ; index++ ) {
            derivatives[ index ] = - DERIVATIVES[ index ];
        }
        return new DifferentialVariable( - this.VALUE, this.OFFSET, derivatives );
    }
    
    
    /** calculate and return the reciprocol of this variable */
    public DifferentialVariable reciprocal() {
        final double value = 1.0 / VALUE;
        final double inverseFactor = - value * value;   // f = 1/u -> df = - du / u^2
        final double[] derivatives = new double[ DERIVATIVES.length ];
        for ( int index = 0 ; index < derivatives.length ; index++ ) {
            derivatives[ index ] = inverseFactor * DERIVATIVES[ index ];
        }
        return new DifferentialVariable( value, this.OFFSET, derivatives );
    }
	
	
	/** add this variable to the specified addend */
	public DifferentialVariable plus( final double addend ) {
		return DifferentialVariable.add( this, addend );
	}
	
	
	/** add this variable to the specified addend */
	public DifferentialVariable plus( final DifferentialVariable addend ) {
		return DifferentialVariable.add( this, addend );
	}
	
	
	/** subtract the specified subtrahend from this variable */
	public DifferentialVariable minus( final double subtrahend ) {
		return DifferentialVariable.subtract( this, subtrahend );
	}
	
	
	/** subtract the specified subtrahend from this variable */
	public DifferentialVariable minus( final DifferentialVariable subtrahend ) {
		return DifferentialVariable.subtract( this, subtrahend );
	}
	
	
	/** multiply this variable by the specified multiplicand */
	public DifferentialVariable times( final double multiplicand ) {
		return DifferentialVariable.multiply( this, multiplicand );
	}
	
	
	/** multiply this variable by the specified multiplicand */
	public DifferentialVariable times( final DifferentialVariable multiplicand ) {
		return DifferentialVariable.multiply( this, multiplicand );
	}
	
	
	/** divide this variable by the specified divisor */
	public DifferentialVariable over( final double divisor ) {
		return DifferentialVariable.divide( this, divisor );
	}
	
	
	/** divide this variable by the specified divisor */
	public DifferentialVariable over( final DifferentialVariable divisor ) {
		return DifferentialVariable.divide( this, divisor );
	}
	
	
	/** Perform a unary math operation with the resulting value and the differential factor from the chain rule */
	final private DifferentialVariable unaryOperation( final double value, final double differentialFactor ) {
		final double[] derivatives = new double[ DERIVATIVES.length ];
        for ( int index = 0 ; index < derivatives.length ; index++ ) {
            derivatives[ index ] = differentialFactor * DERIVATIVES[ index ];
        }
        return new DifferentialVariable( value, this.OFFSET, derivatives );
	}
	
	
	/** raise this variable to the specified power */
	final public DifferentialVariable pow( final double power ) {
		final double value = Math.pow( VALUE, power );
		final double differentialFactor = power * Math.pow( VALUE, power - 1.0 );
		return unaryOperation( value, differentialFactor );
	}
	
	
	/** get the square root of this variable */
	final public DifferentialVariable sqrt() {
		return this.pow( 0.5 );
	}
	
	
	/** get the absolute value of this variable */
	final public DifferentialVariable abs() {
		final double value = Math.abs( VALUE );
		final double differentialFactor = VALUE / value;
		return unaryOperation( value, differentialFactor );
	}
	
	
	/** get the natural logarithm (base e) of this variable */
	final public DifferentialVariable log() {
		final double value = Math.log( VALUE );
		final double differentialFactor = 1.0 / VALUE;
		return unaryOperation( value, differentialFactor );
	}
	
	
	/** get the exponential (base e) of this variable */
	final public DifferentialVariable exp() {
		final double value = Math.exp( VALUE );
		return unaryOperation( value, value );
	}
	
	
	/** get the sine of this variable */
	final public DifferentialVariable sin() {
		final double value = Math.sin( VALUE );
		final double differentialFactor = Math.cos( VALUE );
		return unaryOperation( value, differentialFactor );
	}
	
	
	/** get the cosine of this variable */
	final public DifferentialVariable cos() {
		final double value = Math.cos( VALUE );
		final double differentialFactor = - Math.sin( VALUE );
		return unaryOperation( value, differentialFactor );
	}
	
	
	/** get the tangent of this variable */
	final public DifferentialVariable tan() {
		final double value = Math.tan( VALUE );
		final double secant = 1.0 / Math.cos( VALUE );
		final double differentialFactor = secant * secant;
		return unaryOperation( value, differentialFactor );
	}
	
	
	/** get the arc sine of this variable */
	final public DifferentialVariable asin() {
		final double value = Math.asin( VALUE );
		final double differentialFactor = 1.0 / Math.sqrt( 1.0 - VALUE * VALUE );
		return unaryOperation( value, differentialFactor );
	}
	
	
	/** get the arc cosine of this variable */
	final public DifferentialVariable acos() {
		final double value = Math.acos( VALUE );
		final double differentialFactor = - 1.0 / Math.sqrt( 1.0 - VALUE * VALUE );
		return unaryOperation( value, differentialFactor );
	}
	
	
	/** get the arc tangent of this variable */
	final public DifferentialVariable atan() {
		final double value = Math.atan( VALUE );
		final double differentialFactor = 1.0 / ( 1.0 + VALUE * VALUE );
		return unaryOperation( value, differentialFactor );
	}
	
	
	/** get the hyperbolic sine of this variable */
	final public DifferentialVariable sinh() {
		final double value = Math.sinh( VALUE );
		final double differentialFactor = Math.cosh( VALUE );
		return unaryOperation( value, differentialFactor );
	}
	
	
	/** get the hyperbolic cosine of this variable */
	final public DifferentialVariable cosh() {
		final double value = Math.cosh( VALUE );
		final double differentialFactor = Math.sinh( VALUE );
		return unaryOperation( value, differentialFactor );
	}
	
	
	/** get the hyperbolic tangent of this variable */
	final public DifferentialVariable tanh() {
		final double value = Math.tanh( VALUE );
		final double sech = 1.0 / Math.cosh( VALUE );
		final double differentialFactor = sech * sech;
		return unaryOperation( value, differentialFactor );
	}
}    
