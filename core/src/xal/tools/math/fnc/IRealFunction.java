/**
 * IRealUnivariateFunction.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 24, 2015
 */
package xal.tools.math.fnc;

import xal.tools.math.Interval;

/**
 * Interface describing the characteristics of a real-valued function of
 * a real variable.  It can be evaluated at a given point within its domain
 * of definition, which here is an interval.  If a class chooses not to 
 * implement the <code>{@link #getDomain()}</code> method the default implementation
 * returns the value <code>Interval.REAL_LINE</code>.
 *
 * @author Christopher K. Allen
 * @since  Sep 24, 2015
 */
public interface IRealFunction {
    
    
    /**
     * Returns the domain of the function.  The default implementation is to 
     * return the entire real line &reals;.  That is, the function is assumed
     * to be defined on the whole real line.
     * 
     * @return  the interval of definition for the function
     * 
     * @since  Sep 24, 2015   by Christopher K. Allen
     */
    public default Interval getDomain() { 
        return Interval.REAL_LINE; 
    }
    
    /**
     * Evaluate the function at the given location within its domain.
     * 
     * @param dblLoc    a valid point within the function domain
     * 
     * @return  the value of the function at the given location
     * 
     * @throws IllegalArgumentException     may be thrown if location is outside function domain
     *
     * @since  Sep 24, 2015   by Christopher K. Allen
     */
    public double   evaluateAt(double dblLoc) throws IllegalArgumentException;

}
