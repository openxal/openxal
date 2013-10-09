//
//  DiscreteFourierTransform.java
//  xal
//
//  Created by Tom Pelaia on 1/2/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//


package xal.tools.math;


/**
 * Calculates the discrete fourier transform.
 * While not as efficient as a fast fourier transform, it offers more flexibility.
 * transform(f) = 1/2N * sum( f(t) e^(i pi p k / N) ), k = 0...2N-1, t = kT/2N, freq = 2 pi p / T, p = 0...2N-1
 */
public class DiscreteFourierTransform {
	final private static double PI2 = 2.0 * Math.PI;
	final private double[] VALUES;
	final private double PERIOD;
	final private Complex[] SPECTRUM;
	
	
	/**
	 * Constructor 
	 * @param values is an even number of values evenly spaced over time
	 * @param period is the time period
	 * @throws java.lang.RuntimeException if the number of time based elements is not even
	 */
	public DiscreteFourierTransform( final double[] values, final double period ) {
		final int count = 2 * ( values.length / 2 );
		if ( count != values.length ) {
			throw new RuntimeException( "The discrete fourier transform requires an even number of time based values, but you have provided " + values.length + " elements." );
		} 
		
		PERIOD = period;
		VALUES = values;
		SPECTRUM = computeTransform();
	}
	
	
	/** compute the discrete fourier transform */
	private Complex[] computeTransform() {
		final double[] values = VALUES;
		final int count = values.length;
		final double countReciprocal = 1.0 / count;
		
		final Complex[] transform = new Complex[count];
		for ( int p = 0 ; p < count ; p++ ) {
			double realSum = 0.0;
			double iSum = 0.0;
			for ( int k = 0 ; k < count ; k++ ) {
				final double phase = PI2 * p * k * countReciprocal;
				realSum += values[k] * Math.cos( phase );
				iSum += values[k] * Math.sin( phase );
			}
			realSum *= countReciprocal;
			iSum *= countReciprocal;
			transform[p] = new Complex( realSum, iSum );
		}
		
		return transform;
	}
	
	
	/** get the time based array of values */
	public double[] getValues() {
		return VALUES;
	}
	
	
	/** get the time for the specified time index */
	public double getTime( final int index ) {
		return index * PERIOD / VALUES.length;
	}
	
	
	/** get the transform at the specified frequency index */
	public Complex[] getSpectrum() {
		return SPECTRUM;
	}
	
	
	/** get the count of the elements in the transform */
	public int getSpectrumCount() {
		return SPECTRUM.length;
	}
	
	
	/** get the frequency associated with the frequency index */
	public double getFrequency( final int index ) {
		return ((double)index) / PERIOD;
	}
}
