//
//  SatisfactionCurve.java
//  xal
//
//  Created by Thomas Pelaia on 10/17/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.solver;


/** Collection of satisfaction curve functions */
public class SatisfactionCurve {
	/** Constructor */
	protected SatisfactionCurve() {}
	
	
	/**
	 * Generate the exponential satisfaction for a given value and tolerance
	 * @param value the value to test
	 * @param tolerance the tolerance corresponding to 90% satisfaction
	 */
	static public double exponentialSatisfaction( final double value, final double tolerance ) {
		return Math.exp( - 0.1 * Math.abs( value ) / tolerance );		// scale for tolerance yielding 90% satisfaction
	}
	
	
	/**
	 * Generate a satisfaction based on an inverse function a / ( a + |x| )
	 * @param value the value to test
	 * @param tolerance the tolerance corresponding to 90% satisfaction
	 */
	static public double inverseSatisfaction( final double value, final double tolerance ) {
		final double coef = 9.0 * tolerance;		// scale for tolerance yielding 90% satisfaction
		return coef / ( Math.abs( value ) + coef );
	}
	
	
	/**
	 * Generate a satisfaction based on an inverse function 1 - a / ( a + |x| )
	 * @param value the value to test
	 * @param tolerance the tolerance corresponding to 90% satisfaction
	 */
	static public double inverseRisingSatisfaction( final double value, final double tolerance ) {
		return 1.0 - inverseSatisfaction( value, 1.0 - tolerance );
	}
	
	
	/**
	 * Generate a satisfaction based on an inverse square function a / ( a + x^2 )
	 * @param value the value to test
	 * @param tolerance the tolerance corresponding to 90% satisfaction
	 */
	static public double inverseSquareSatisfaction( final double value, final double tolerance ) {
		final double coef = 9.0 * tolerance * tolerance;		// scale for tolerance yielding 90% satisfaction
		return value == 0.0 ? 1.0 : coef / ( value * value + coef );
	}
	
	
	/**
	 * Generate a satisfaction based on an inverse square function 1 - a / ( a + x^2 )
	 * @param value the value to test
	 * @param tolerance the tolerance corresponding to 90% satisfaction
	 */
	static public double inverseSquareRisingSatisfaction( final double value, final double tolerance ) {
		return 1.0 - inverseSquareSatisfaction( value, 1.0 - tolerance );
	}
	
	
	/**
	 * Generate a satisfaction based on an S-Curve that extends from negative to positive infinity
	 * satisfaction = 1/2 + a ( x - x0 ) / ( 1 + 2a|x - x0| )
	 * @param value the value to test
	 * @param center the center value of the satisfaction curve
	 * @param slope the slope of the satisfaction curve at the center
	 */
	static public double sCurveSatisfactionWithCenterAndSlope( final double value, final double center, final double slope ) {
		final double delta = slope * ( value - center );
		return 0.5 + delta / ( 1 + 2 * Math.abs( delta ) );
	}
	
	
	/**
	 * Generate a linear satisfaction curve which has zero at the bottom end and 1 at the top end
	 * @param value the value to test
	 * @param minValue the minimum value
	 * @param maxValue the maximum value
	 */
	static public double linearRisingSatisfaction( final double value, final double minValue, final double maxValue ) {
		return value > minValue ? ( value < maxValue ? ( value - minValue ) / ( maxValue - minValue ) : 1.0 ) : 0.0;
	}
	
	
	/**
	 * Generate a linear satisfaction curve which has zero at the bottom end and 1 at the top end
	 * @param value the value to test
	 * @param minValue the minimum value
	 * @param maxValue the maximum value
	 */
	static public double linearFallingSatisfaction( final double value, final double minValue, final double maxValue ) {
		return 1.0 - linearRisingSatisfaction( value, minValue, maxValue );
	}
	
	
	/**
	 * Generate a satisfaction curve which accelerates and ends with the specified slope
	 * @param value the value to test
	 * @param minValue the minimum value
	 * @param maxValue the maximum value
	 * @param endSlope the slope at the end point
	 */
	static public double acceleratingSatisfaction( final double value, final double minValue, final double maxValue, final double endSlope ) {
		return endSlope >= 0 ? Math.pow( linearRisingSatisfaction( value, minValue, maxValue ), endSlope ) : 1.0 - acceleratingSatisfaction( value, minValue, maxValue, -endSlope );
	}	
	
	
	/**
	 * Generate a satisfaction curve which decelerates and begins with the specified slope
	 * @param value the value to test
	 * @param minValue the minimum value
	 * @param maxValue the maximum value
	 * @param startSlope the slope at the start point
	 */
	static public double deceleratingSatisfaction( final double value, final double minValue, final double maxValue, final double startSlope ) {
		return startSlope >= 0.0 ? 1.0 - Math.pow( 1.0 - linearRisingSatisfaction( value, minValue, maxValue ), startSlope ) : 1.0 - deceleratingSatisfaction( value, minValue, maxValue, -startSlope );
	}	
}
