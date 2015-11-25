/*
 * LinearInterpolator.java
 *
 * Created on August 11, 2003, 4:10 PM
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.math.fnc.interp;

import xal.tools.math.Interval;
import xal.tools.math.fnc.IRealFunction;

/**
 * <p>
 * LinearInterpolator calculates the linear interpolated value at at any point
 * within the bounds of an array of values.  The function samples are on an
 * equally spaced grid.
 * </p>
 * <p>
 * <h4>NOTES: CKA</h4>
 * This class is copied from the original <code>xal.tools.LinearInterpolator</code> 
 * implemented by Thomas Pelaia.  Although it would be more attractive to simply
 * move the original class to the current package, it is difficult to make 
 * dramatic changes in the core once established.  The class has been modified
 * slightly to support the <code>IRealFunction</code> interface.
 * </p>
 *
 * @author  tap
 * @author  Christopher K. Allen
 * 
 * @since Aug 11, 2003
 * @version Sep 25, 2015
 */
final public class GridInterpolation implements IRealFunction {
    
    
    
    /* 
     * Global Constants
     */
    
    /** epsilonWeight is the fraction resolution of the step. */
    static final private double DBL_WGT_EPS = 1.0e-6;
    
    
    /*
     * Local Attributes
     */
    
    /** Array of function values across grid */
    final private double[] arrValues;
    
    /** Grid domain for the function samples */
    final private Interval  ivlDomain;
    
    /** Stride of the interpolation grid */
    final private double dblStep;

    
//  /** The left-hand side of the domain */
//  final protected double dblDomStart;
//  

    
    /** 
     * Creates a new instance of Interpolator initialized with the given value array
     * and grid description.
     * 
     * @param values is the array of values at fixed intervals.
     * @param start is the start of the domain of points and corresponds to the first array element
     * @param step is the step in the domain for each successive element in the array
     */
    public GridInterpolation( final double[] values, final double start, double step ) {
        this.arrValues = values;
        this.dblStep = step;
//      dblDomStart = start;
        
        double  dblMin = start;
        double  dblMax = (this.arrValues.length - 1) * this.dblStep;
        
        this.ivlDomain = new Interval(dblMin, dblMax);
    }
    
    
    
    /*
     * IRealFunction Interface
     */
    
    /**
     *
     * @see xal.tools.math.fnc.IRealFunction#getDomain()
     *
     * @since  Sep 25, 2015   by Christopher K. Allen
     */
    @Override
    public Interval getDomain() {
        return this.ivlDomain;
    }
    
    /**
     * Calculate the interpolated value at the specified point in the domain.  The point must
     * reside within the domain of points from <code>start</code> to 
     * <code>start + step * values.length</code>.
     * 
     * @param point The point in the domain for which we should interpolate the value.
     * 
     * @return The interpolated value.
     * 
     * @throws java.lang.ArrayIndexOutOfBoundsException if the point does not fall into the accepted domain
     */
    @Override
    public double evaluateAt( final double point ) throws IllegalArgumentException {
        final double dblElem = (point - this.ivlDomain.getMin()) / this.dblStep;
        final double dblWgt  = dblElem - Math.floor(dblElem);
        final int    index   = (int)dblElem;
        
        if (index >= this.arrValues.length - 1)
            throw new IllegalArgumentException("Evaluation point is outside function domain");
        
        double      dblF1 = this.arrValues[index];
        double      dblF2 = this.arrValues[index+1];
        return (dblWgt < DBL_WGT_EPS) ? dblF1 : (1-dblWgt) * dblF1 + dblWgt * dblF2;
    }
}
