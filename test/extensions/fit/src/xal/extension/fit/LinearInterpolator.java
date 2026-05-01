/*
 * LinearInterpolator.java
 *
 * Created on August 11, 2003, 4:10 PM
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.extension.fit;

/**
 * LinearInterpolator calculates the linear interpolated value at at any point
 * within the bounds of an array of values.
 *
 * @author  tap
 */
final public class LinearInterpolator {
    /** epsilonWeight is the fraction resolution of the step. */
    static final private double epsilonWeight = 1.0e-6;
    
    final protected double[] _values;
    final protected double _start;
    final protected double _step;
    
    
    /** 
     * Creates a new instance of Interpolator.
     * @param values is the array of values at fixed intervals.
     * @param start is the start of the domain of points and corresponds to the first array element
     * @param step is the step in the domain for each successive element in the array
     */
    public LinearInterpolator(final double[] values, final double start, double step) {
        _values = values;
        _start = start;
        _step = step;
    }
    
    
    /**
     * Calculate the interpolated value at the specified point in the domain.  The point must
     * reside within the domain of points from <code>start</code> to <code>start + step * values.length</code>.
     * @param point The point in the domain for which we should interpolate the value.
     * @return The interpolated value.
     * @throws java.lang.ArrayIndexOutOfBoundsException if the point does not fall int the accepted domain
     */
    public double calcValueAt(final double point) throws ArrayIndexOutOfBoundsException {
        final double element = (point - _start) / _step;
        final double weight = element - Math.floor(element);
        final int index = (int)element;
        
        return (weight < epsilonWeight) ? _values[index] : (1-weight) * _values[index] + weight * _values[index+1];
    }
}
