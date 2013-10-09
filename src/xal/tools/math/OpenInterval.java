/*
 * Created on Mar 2, 2004
 *
 */
package xal.tools.math;

/**
 * Represents an open interval of the real line.
 * 
 * @author Christopher K. Allen
 *
 */
public class OpenInterval extends Interval {

    /** Serialization version */
    private static final long serialVersionUID = 1L;


    /*
     *  Initialization
     */
    
    /** 
     * Default constructor - creates the empty interval.
     */
    public OpenInterval() {
        super();
    }
    
    /**
     * Initializing constructor - create a new open interval with specified 
     * endpoints.
     *
     *  @param  min     left endpoint
     *  @param  max     right endpoint
     *  
     * @throws MathException <var>max</var> is smaller than <var>min</var> 
     */
    public OpenInterval(double min, double max) throws MathException {
        super(min, max);
    }
    
    /**
     * Copy constructor - create a new open interval initialized to the argument.
     *  
     * @param   I       interval to copy
     * 
     * @throws MathException    malformed interval object
     */
    public OpenInterval(Interval I) throws MathException {
        super(I);    
    }



    /*
     * Set Operations
     */

    /**
     * Is point a member of the open interval
     *
     * @param  x       point to test for membership
     *
     * @return         true if x is in interval
     */
    public boolean membership(double x)    { 
        return (getMax()>x) && (getMin()<x); 
    };

     
    /**
     * Is there a nonzero intersection between this interval
     * and the argument.  
     * 
     * @param  I   interval to be tested
     * @return     true if the intervals intersect
     */
    public boolean intersect(Interval I)  {   
        return (this.getMax()>I.getMin()) || (this.getMin()<I.getMax()); 
    }
     

    /**
     * Are intervals equal
     *
     * @param  I       interval object to be checked for equality
     *
     * @return         true if both objects are equal as intervals
     */
    public boolean equals(OpenInterval I)      { 
        return (this.getMin()==I.getMin()) && (this.getMax()==I.getMax()); 
    };


    /*
     * Debugging
     */
     
     
    /**
     * Return the contents of the interval as a <code>String</code>.
     * 
     * return      string representation of interval
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "(" + getMin() + "," + getMax() + ")";
    }
     
}
