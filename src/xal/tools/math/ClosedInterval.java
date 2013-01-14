/*
 * Created on Mar 2, 2004
 *
 */
package xal.tools.math;

/**
 * Represents a closed interval of the real line.
 * 
 * @author Christopher K. Allen
 *
 */
public class ClosedInterval extends Interval {

    
    /*
     * Global Constants
     */
    
    /** Serialization version */
    private static final long serialVersionUID = 1L;


    /*
     *  Initialization
     */
    

    /** 
     * Default constructor - creates a new instance of Interval with one point, 
     * the origin.
     */
    public ClosedInterval() {
        super();
    }


    /**
     * Initializing constructor - creates a single point (zero length) interval
     * given by the value of the argument <code>pt</code>.
     * 
     * @param pt    The single point contained in the interval
     * 
     */
    public ClosedInterval(double pt) {
        this.setMin(pt);
        this.setMax(pt);
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
    public ClosedInterval(double min, double max) throws MathException {
        super(min, max);
    }
    
    /**
     * Copy constructor - create a new open interval initialized to the argument.
     *  
     * @param   I       interval to copy
     * 
     * @throws MathException    malformed interval to copy
     */
    public ClosedInterval(Interval I) throws MathException {
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
        return (getMax()>=x) && (getMin()<=x); 
    };

     
    /**
     * Is there a nonzero intersection between this interval
     * and the argument.  
     * 
     * @param  I   interval to be tested
     * @return     true if the intervals intersect
     */
    public boolean intersect(ClosedInterval I)  {   
        return (this.getMax()>=I.getMin()) || (this.getMin()<=I.getMax()); 
    }
    
    /**
     * Is the given interval a subset of this interval.
     *
     * @param I     interval under test
     * 
     * @return      <code>true</code> if <b>I</b>&sub;<code>this</code>,
     *              <code>false</code> otherwise.
     *
     * @author Christopher K. Allen
     * @since  Apr 27, 2011
     */
    public boolean contains(ClosedInterval I)   {
        return (this.getMax()>=I.getMax() && this.getMin()<=I.getMin());
    }
     

    /**
     * Are intervals equal
     *
     * @param  I       interval object to be checked for equality
     *
     * @return         true if both objects are equal as intervals
     */
    public boolean equals(ClosedInterval I)      { 
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
