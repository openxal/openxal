/*
 * ElementaryFunctions.java
 *
 * Created on October 22, 2002, 10:10 AM
 */

package xal.tools.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *  <p>
 *  Utility case for defining elementary mathematical functions that are not, but should be,
 *  included in the <code>java.lang.Math</code> class.  Several of the functions in this
 *  class are implemented using the methods of <code>java.lang.Math</code>.
 *  </p>
 *
 * @author  Christopher K. Allen
 * 
 * @see java.lang.Math
 * @see java.lang.StrictMath
 */
public final class ElementaryFunction {
    
    /*
     *  Global Attributes
     */
    
    
    /** number of Units in the Last Place (ULPs) used for bracketing approximately equal values */
    public static final int     ULPS_DEFLT_BRACKET = 100;
    
    /** conversion between significant digits in decimal to significant digits in binary log(10)/log(2) */
    public static final double  DBL_DEC_TO_BINARY = 3.32192809488;
    
    
    /** the value PI/2 */
    public static final double PI_BY_2 = Math.PI/2;
    
    /** small tolerance value */
    public static final double EPS = 1000.0*Double.MIN_VALUE;
    
   
    


    /*
     * Elementary Math
     */
    
    /**
     * Test if two <code>double</code> precision numbers are approximately equal.
     * This condition is checked using the the default number of Units in the
     * Last Place (ULPs) bracketing the two numbers.  The default number of
     * ULPs is given in the class constant <code>{@link #ULPS_DEFLT_BRACKET}</code>
     * and current has the value <code>{@value #ULPS_DEFLT_BRACKET}</code>.
     * 
     * @param   x    double precision number
     * @param   y    double precision number
     * 
     * @return  true of <i>y</i> ~ <i>x</i>, false otherwise
     * 
     * @see #approxEq(double, double, int)
     * @see #ULPS_DEFLT_BRACKET
     */
    public static boolean approxEq(double x, double y)  {
        return ElementaryFunction.approxEq(x, y, ElementaryFunction.ULPS_DEFLT_BRACKET);
    }
    
    /**
     * <p>
     * Test if two <code>double</code> precision numbers are approximately equal.
     * This condition is defined with respect to the <b>U</b>nits in <b>L</b>ast
     * <b>P</b>lace (ULPs) bracketing procedure.
     * </p>
     * <p>
     * The ULP values <i>ulp<sub>x</sub></i> and <i>ulp<sub>y</sub></i> are computed
     * for each argument <i>x</i> and <i>y</i>.  
     * These values are the distances between the
     * arguments and the nearest double precision number that can be represented by the
     * IEEE 754 standard.  
     * The bracketing distances &delta;<i>x</i> and &delta;<i>y</i> for <i>x</i> and <i>y</i> 
     * are computed as
     * <pre>
     *      &delta;<i>x</i> &trie; <i>N</i> &times; <i>ulp<sub>x</sub></i> ,
     *      &delta;<i>y</i> &trie; <i>N</i> &times; <i>ulp<sub>y</sub></i> ,
     * </pre>
     * where <i>N</i> is the number of ULPs specified in the arguments.
     * Two intervals are defined
     * <pre>
     *      <i>I<sub>x</sub></i> &trie; [<i>x</i> &minus; &delta;<i>x</i>,<i>x</i> &plus; &delta;<i>x</i>],
     *      <i>I<sub>y</sub></i> &trie; [<i>y</i> &minus; &delta;<i>y</i>,<i>y</i> &plus; &delta;<i>y</i>].
     * </pre>
     * If the intersection <i>I<sub>x</sub></i> &cap; <i>I<sub>y</sub></i> is finite then
     * <i>x</i> and <i>y</i> are considered approximately equal.  
     * </p>
     * 
     * @param   x           double precision number
     * @param   y           double precision number
     * @param   cntUlps     number <i>N</i> of ULPs used to bracket the numbers
     * 
     * @return  <code>true</code> of <i>y</i> ~ <i>x</i> within <i>N</i> ULPs, </code>false</code> otherwise
     */
    public static boolean approxEq(double x, double y, int cntUlps)  {

        if ( x == y ) return true;
        
        double  dx = cntUlps*Math.ulp(x);
        double  dy = cntUlps*Math.ulp(y);
        
        if ( x < y )    {
            if ( x+dx >= y-dy ) 
                return true;
            else                  
                return false;
        
        } else {
            if (y+dy >= x-dx) 
                return true;
            else
                return false;
        }
    }
    
    /**
     * Checks if two double precision numbers are equal up to the given number
     * of significant digits.
     * 
     * @param x         double precision number
     * @param y         double precision number
     * @param cntDgts   number <i>N</i> of significant digits to compare
     * 
     * @return          <code>true</code> if the first <i>N</i> digits of <i>x</i> and <i>y</i> agree,
     *                  <code>false</code> otherwise
     *
     * @since  Dec 31, 2015,   Christopher K. Allen
     */
    public static boolean significantDigitsEqs(double x, double y, int cntDgts) {
        
        BigDecimal      bdRndX = new BigDecimal(x);
        BigDecimal      bdRndY = new BigDecimal(y);
        
        bdRndX = bdRndX.setScale(cntDgts, RoundingMode.HALF_UP);
        bdRndY = bdRndY.setScale(cntDgts, RoundingMode.HALF_UP);

        boolean bolEq = bdRndX.equals(bdRndY);
        
        return bolEq;
    }
    
    /**
     * <p>
     * Test if two <code>double</code> precision numbers are in the same ball
     * of radius <i>r</i>.
     * </p>
     * <p>
     * <h4>NOTES CKA</h4>
     * &middot; This is really a distance function and not a topological one.
     * </p>
     * 
     * 
     * @param   x   double precision number
     * @param   y   double precision number
     * @param   r   radius defining the size of the neighborhood
     * 
     * @return  true of |<i>y</i> - <i>x</i>| <= <i>r</i>, false otherwise
     */
    public static boolean neighbors(double x, double y, double r)  {
        double      difference = x - y;
       
        return Math.abs(difference) <= r;
    }
    
    
    
    /*
     * Trigonometric Functions
     */
    
    /**
     * Inverse tangent function.  
     * 
     * This version of the arctan function is similar to
     * the <code>Math.atan2()</code> function, however it
     * returns values in the interval [-pi/2,+pi/2].  It
     * takes the same arguments as <code>Math.atan2()</code>.
     * 
     * @param   y   argument numerator of atan(y/x)
     * @param   x   argument denominator
     */
    
    
    /*
     * Algebraic Functions
     */
    
    /**
     * Computes the factorial of the given integer.  The factorial
     * <i>n</i>! of the number <i>n</i> is defined
     * <br>
     * <br>
     * &nbsp; &nbsp;  <i>n</i>! &equiv; 1 &middot; 2 &middot; &hellip; &middot; (<i>n</i> - 1) &middot; <i>n</i> 
     *
     * @param n     integer to be "factorialized"
     * 
     * @return      <i>n</i>! = factorial of argument
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2011
     */
    public static final int factorial(int n) {
        if (n < 0)
            return 0;
        
        int     intFac = 1;
        
        for (int i=n; i>0; i--)  
            intFac *= i;
            
        return intFac;
    }
    
    /**
     * <p>
     * Returns the value of the first argument raised to the power of the second argument
     * <i>dblBase</i><sup><i>dblExpon</i></sup>. Special cases:
     * <br>
     * <br>&middot; If the second argument is positive or negative zero, then the result is 1.0.
     * <br>&middot; If the second argument is 1.0, then the result is the same as the first argument.
     * <br>&middot; If the second argument is NaN, then the result is NaN.
     * <br>&middot; If the first argument is NaN and the second argument is nonzero, then the result is NaN.
     * <p>
     * <p>
     * This method should be used over that of <code>{@link Math#pow(double, double)}</code> whenever the
     * exponent is an integer.  Since the later must consider the case of non-integer exponents the 
     * algorithm used there is more expensive than the simple multiplication used here.
     * </p>
     * 
     * @param dblBase   the base of the exponential
     * @param intExpon  the exponent
     * 
     * @return          the value <var>dblBase<sup>dblExpon</var></sup>
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2011
     */
    public static final double pow(double dblBase, int intExpon) {
        double  dblFac = 1.0;
        
        if (intExpon > 0)
            for (int i=0; i<intExpon; i++) 
                dblFac *= dblBase;
        else
            for (int i=0; i<Math.abs(intExpon); i++) 
                dblFac /= dblBase;
        
        return dblFac;
    }
    
    /**
     * <p>
     * Returns the value of the first argument raised to the power of the second argument
     * <i>intBase</i><sup><i>intExpon</i></sup> where the base is an integer. Special cases:
     * <br>
     * <br>&middot; If the second argument is positive or negative zero, then the result is 1.
     * <br>&middot; If the second argument is 1, then the result is the same as the first argument.
     * <br>&middot; If the second argument is NaN, then the result is NaN.
     * <br>&middot; If the first argument is NaN and the second argument is nonzero, then the result is NaN.
     * <p>
     * <p>
     * This method should be used over that of <code>{@link Math#pow(double, double)}</code> whenever both the
     * base and the exponent are integers.  Since the later must consider the case of non-integer exponents the 
     * algorithm used here is less expensive.
     * </p>
     * 
     * @param intBase   the base of the exponential
     * @param intExpon  the exponent
     * 
     * @return          the value <var>intBase<sup>intExpon</var></sup>
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2011
     */
    public static final long pow(int intBase, int intExpon) {
        long    lngFac = 1;
        
        if (intExpon > 0)
            for (int i=0; i<intExpon; i++) 
                lngFac *= intBase;
        else
            for (int i=0; i<Math.abs(intExpon); i++) 
                lngFac /= intBase;
        
        return lngFac;
    }

    
    /*
     * Engineering Functions
     */
    
    /**
     *  <p>
     *  Implementation of the sinc function where
     *  <br>
     *  <br>
     *  &nbsp; sinc(<i>x</i>) &equiv; sin(<i>x</i>)/<i>x</i>.
     *  </p>
     *  <p>
     *  For small values of <i>x</i> we Taylor expand the sinc 
     *  function to sixth order, 
     *  <br>
     *  <br>
     *  &nbsp;  sinc(x) &asymp; 1 - <i>x</i><sup>2</sup>/6 + 
     *                               <i>x</i><sup>4</sup>/120 - 
     *                               <i>x</i><sup>6</sup>/5040 + 
     *                               <i>O</i>(<i>x</i><sup>8</sup>).
     *  <br>
     *  <br>
     *  otherwise we return sin(<i>x</i>)/<i>x</i>.
     *  </p>
     * 
     * @param   x   any real number
     * 
     * @return  sinc(<var>x</var>) &equiv; sin(<var>x</var>)/<var>x</var>
     */
    public static double sinc(double x) {
        
        if (Math.abs(x) < 0.1) {    // avoid singularity at zero
            double      x_2 = x*x;
            double      x_4 = x_2*x_2;
            
            return 1.0 - x_2/6.0 + x_4/120.0 - x_2*x_4/5040.0;
        }
        
        return Math.sin(x)/x;
    }
    
    /**
     *  <p>
     *  Implementation of the sinch function where 
     *  <br>
     *  <br>
     *  &nbsp; sinch(<i>x</i>) &equiv; sinh(<i>x</i>)/<i>x</i>
     *  <br>
     *  <br>
     *  </p>
     *  <p>
     *  For small values of <i>x</i> we Taylor expand the hyperbolic 
     *  sine function to sixth order,
     *  <br>
     *  <br>
     *
     *  &nbsp; sinch(<i>x</i>) &asymp; 1 + <i>x</i><sup>2</sup>/6 + 
     *                                     <i>x</i><sup>4</sup>/120 + 
     *                                     <i>x</i><sup>6</sup>/5040 + 
     *                                     <i>O</i>(<i>x</i><sup>8</sup>).
     *  <br>
     *  <br>
     *  Otherwise we return sinh(<i>x</i>)/<i>x</i>.
     * 
     * @param   x   any real number
     * 
     * @return sinh(<i>x</i>)/<i>x</i>.
     */
    public static double sinch(double x) {
        
        if (Math.abs(x) > ElementaryFunction.EPS) {
            return sinh(x)/x;
        } else {
        	// System.out.println("sinch, x = "+x);
        }
        
        double      x_2 = x*x;
        
        return 1.0 + x_2/6.0 + x_2*x_2/120.0 + x_2*x_2*x_2/5040.0;
    }

    /**
     * Returns the sinch(<i>x</i><sup>2</sup>).  I am not sure
     * why this needs a special implementation, but it's here.
     * There is not special computation, the result is computed
     * directly as sinch(<i>x</i><sup>2</sup>).
     * 
     * @param   x   any real number
     * 
     * @return  sinch(<i>x</i><sup>2</sup>)
     * 
     * @see ElementaryFunction#sinch(double)
     */
    public static double sinchm(double x) {
        
        if (Math.abs(x) > ElementaryFunction.EPS) {
            return Math.sinh(x)/x;
        } else {
        	//System.out.println("sinchm, x = "+x);
        }
        
        double      x_2 = x*x;
        
        return 1.0 + x_2/6.0 + x_2*x_2/120.0 + x_2*x_2*x_2/5040.0;
    }


    /*
     * Hyperbolic Functions
     */
    
    /**
     * Hyperbolic sine function.  This should really be included in Java.
     * 
     * @param   x   any real number
     * 
     * @return &frac12;(<i>e</i><sup>+<i>x</i></sup> - <i>e</i><sup>-<i>x</i></sup>) 
     */
    public final static double sinh(double x) {
        return 0.5*(Math.exp(x)-Math.exp(-x));
    };
    
    /**
     * Hyperbolic cosine function.  This too.
     * 
     * @param   x   any real number
     * 
     * @return &frac12;(<i>e</i><sup>+<i>x</i></sup> + <i>e</i><sup>-<i>x</i></sup>) 
     */
    public final static double cosh(double x) {
        return 0.5*(Math.exp(x)+Math.exp(-x));
    };
    
    /**
     * Hyperbolic tangent function.
     *  
     * @author Christopher Allen
     * 
     * @return sinh(<i>x</i>)/cosh(<i>x</i>)
     */
    public final static double tanh(double x)   {
        return sinh(x)/cosh(x);
    }


    /*
     * Inverse Hyperbolic Functions
     */    
     
    /**
     * Inverse hyperbolic sine function.
     * 
     * @param   x   any real number
     * 
     * @author Christopher K. Allen
     * 
     * @return log[<i>x</i> + (<i>x</i><sup>2</sup> + 1)<sup>1/2</sup>]
     */
    public final static double  asinh(double x) {
        return Math.log(x + Math.sqrt(x*x + 1.0));
    }
    
    /**
     * Inverse hyperbolic cosine function.  Note that due to 
     * the nature of the hyperbolic cosine function the argument
     * <b>must</b> be greater than 1.0.
     * 
     * @param   x   a real number in the interval [1,+&infin;)
     * 
     * @return log[<i>x</i> + (<i>x</i><sup>2</sup> - 1)<sup>1/2</sup>]
     * 
     * @author Christopher K. Allen
     * 
     * @exception   IllegalArgumentException    argument value is outside the domain of definition
     */
    public final static double  acosh(double x)
        throws IllegalArgumentException 
    {
        if (x < 1.0)
            throw new IllegalArgumentException("argument x=" + x + " outside interval [1,+inf)");
        
        return Math.log(x + Math.sqrt(x*x - 1.0));
    }
    
    /**
     * Inverse hyperbolic tangent function.  Note that do to the 
     * nature of the inverse hyperbolic tangent function overflow
     * may occur for arguments close to the values -1 and 1.
     * 
     * @param   x   a real number in the open interval (-1,1)
     * 
     * @return &frac12;log[(<i>x</i> + 1)/(<i>x</i> - 1)]

     * @exception   IllegalArgumentException    argument value is outside the domain of definition
     * 
     * @author Christopher K. Allen
     */
    public final static double atanh(double x)
        throws IllegalArgumentException  
    {
        if (x>=1.0 || x<=-1.0)
            throw new IllegalArgumentException("argument x=" + x + " outside interval (-1,+1)");
        
        return 0.5*Math.log((1.0+x)/(1.0-x));
    }
};
