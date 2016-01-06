/**
 * IRealSmoothFunction.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 24, 2015
 */
package xal.tools.math.fnc;

/**
 * Interface exposing the characteristics of a real function of a real variable which
 * has derivatives.
 *
 *
 * @author Christopher K. Allen
 * @since  Sep 24, 2015
 */
public interface ISmoothRealFunction extends IRealFunction {
    
    /**
     * <p>
     * Return the first-order derivative (<i>n</i> = 1) of the function.
     * Thus, this method is the equivalent of calling 
     * <code>derivativeAt(1, dblLoc)</code>.
     * </p>
     * <p>
     * A smooth, real-valued function has at least one derivative.  Thus, the derivative
     * <i>f</i>'(<i>x</i>) should always exist for any class implementing this interface.
     * </p>
     * 
     * @param dblLoc        the location <i>x</i> at which to evaluate the derivative
     *  
     * @return              the derivative <i>f</i>'(<i>x</i>) of the funciton <i>f</i>
     *
     * @since  Sep 25, 2015   by Christopher K. Allen
     */
    public double   derivativeAt(double dblLoc);
    
    /**
     * <p>
     * Compute and return the <i>n<sup>th</sup></i> derivative at the given location <i>x</i>
     * within the function domain.  The order argument <i>n</i>
     * must be 0 or greater where the 0<sup><i>th</i></sup>
     * derivative is simply the value of the function itself.
     * </p>
     * <p>
     * It is possible that the derivatives of a function are all zero for <i>n</i> greater
     * than a certain value. Consider a polynomial for example, when <i>n</i> is greater than
     * the degree of that polynomial.
     * </p>
     * 
     * @param nOrder        the order <i>n</i> of the derivative
     * @param dblLoc        the location <i>x</i> at which to evaluate the derivative 
     * 
     * @return              the derivative <i>f</i><sup>(<i>n</i>)</sup>(<i>x</i>) of the function
     * 
     * @throws IllegalArgumentException the derivative order must be positive.
     *
     * @since  Sep 24, 2015   by Christopher K. Allen
     */
    public double   derivativeAt(int nOrder, double dblLoc) throws IllegalArgumentException;

}
