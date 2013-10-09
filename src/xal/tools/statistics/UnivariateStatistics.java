/*
 *  UnivariateStatistics.java
 *
 *  Created on April 3, 2002, 8:33 AM
 */
package xal.tools.statistics;

/**
 * UnivariateStatistics calculates statistics of a series of measurements. UnivariateStatistics
 * is immutable. Use MutableUnivariateStatistics if you need to update the statistics with
 * measurements. Only simple statistics are generated (mean and standard deviation).
 *
 * @author   tap
 */
public class UnivariateStatistics {
	/** the number of samples */
	protected int _population;
	/** the mean and mean square of the samples */
	protected double _mean, _meanSquare;

	/** Constructor with no samples. */
	public UnivariateStatistics() {
		this( 0, 0, 0 );
	}


	/**
	 * Copy constructor
	 *
	 * @param stats  the statistics to copy
	 */
	public UnivariateStatistics( final UnivariateStatistics stats ) {
		this( stats._population, stats._mean, stats._meanSquare );
	}


	/**
	 * Constructor which scales the samples from an existing set of statistics.
	 *
	 * @param stats  the statistics against which to scale
	 * @param scale  factor which is used to scale the copied statistics
	 */
	public UnivariateStatistics( final UnivariateStatistics stats, final double scale ) {
		this( stats._population, scale * stats._mean, scale * scale * stats._meanSquare );
	}


	/**
	 * Primary Constructor with a starting set of statistics.
	 *
	 * @param size           the number of samples
	 * @param average        the mean
	 * @param averageSquare  the mean square of the samples
	 */
	public UnivariateStatistics( final int size, final double average, final double averageSquare ) {
		_population = size;
		_mean = average;
		_meanSquare = averageSquare;
	}


	/**
	 * Get the population of the samples (i.e. the number of samples)
	 * @return   the number of samples
	 */
	public int population() {
		return _population;
	}


	/**
	 * Get the mean of the samples.
	 * @return   the mean of the samples
	 */
	public double mean() {
		return _mean;
	}
	
	
	/**
	 * Get the mean square of the samples.
	 * @return   the mean of the samples
	 */
	public double meanSquare() {
		return _meanSquare;
	}


	/**
	 * Get the standard deviation of the samples.
	 *
	 * @return   the standard deviation of the samples
	 */
	public double standardDeviation() {
		return Math.sqrt( variance() );
	}


	/**
	 * Get the variance of the samples.
	 *
	 * @return   the variance of the samples
	 */
	public double variance() {
		return _meanSquare - _mean * _mean;
	}


	/**
	 *  Get the sample standard deviation of the measurements (implies a random subset of
	 *  all data).
	 *
	 * @return   the sample standard deviation
	 */
	public double sampleStandardDeviation() {
		return Math.sqrt( sampleVariance() );
	}


	/**
	 *  Get the sample variance of the measurements (implies a random subset of all data).
	 *
	 * @return   the sample variance of the measurements
	 */
	public double sampleVariance() {
		double sampleVariance = 0;
		try {
			final double scale = ( (double)_population ) / ( _population - 1 );
			sampleVariance = scale * variance();
		}
		catch ( ArithmeticException excption ) {
			sampleVariance = Double.POSITIVE_INFINITY;
		}

		return sampleVariance;
	}


	/**
	 * Get the variance of the mean from the actual value.
	 *
	 * @return   the variance of the mean from the actual value
	 */
	public double varianceOfMean() {
		return variance() / _population;
	}


	/**
	 * Get the standard deviation of the mean from the actual value.
	 *
	 * @return   the standard deviation of the mean from the actual value
	 */
	public double standardDeviationOfMean() {
		return Math.sqrt( varianceOfMean() );
	}


	/**
	 *  Get the variance of the mean from the actual value assuming the supporting
	 *  data is a random subset of all the data.
	 *
	 * @return   the sample variance of the mean
	 */
	public double sampleVarianceOfMean() {
		return sampleVariance() / _population;
	}


	/**
	 *  Get the standard deviation of the mean from the actual value assuming the
	 *  supporting data is a random subset of all the data.
	 *
	 * @return   the sample standard deviation of the mean
	 */
	public double sampleStandardDeviationOfMean() {
		return Math.sqrt( sampleVarianceOfMean() );
	}
}

