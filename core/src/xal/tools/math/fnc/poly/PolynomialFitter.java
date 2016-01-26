/**
 * PolynomialFitter.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 24, 2015
 */
package xal.tools.math.fnc.poly;

import java.util.List;

import xal.tools.math.Interval;
import xal.tools.math.fnc.RealFunctionSamples;

/**
 * Class representing an uni-variate real polynomial used for the expressed purpose
 * of fitting a function over an interval.  Thus, this class is a polynomial 
 * because it is a polynomial fit to a given function.  It is then possible
 * to specify the domain over which the fit is accurate.  If this domain is
 * specified than any attempt to evaluate the polynomial outside this range
 * results in an exception. 
 *
 *
 * @author Christopher K. Allen
 * @since  Sep 24, 2015
 */
public class PolynomialFitter  {

    
    /*
     * Internal Types
     */
    
    /*
     * Global Methods
     */
    
//    static public UnivariatePolynomial  fit(int nOrder, List<FunctionSample> lstSmps) {
//        int     mRows = lstSmps.size();
//        int     nCols = nOrder;
//        
//        Rmxn    matGram = Rmxn.
//        for (int mrow=0; mrow<mRows; mrow++) {
//            
//        }
//    }
    
    /*
     * Local Attributes
     */
    
    /** The domain of the fitting */
    private Interval        ivlDomain;
    
    
    /*
     * Initialization
     */
    
    /**
     * Initializing constructor for PolynomialFitter.  The given data
     * is used to create a least-squares fit up to the given polynomial order. 
     *
     * @param nDegree   degree of the polynomial used to fit the data
     * @param lstSmps   data of function samples 
     *
     * @since  Sep 25, 2015   by Christopher K. Allen
     */
    public PolynomialFitter(int nDegree, RealFunctionSamples fncSmps) {
        
    }
    
//    /**
//     * Zero argument constructor for PolynomialFitter.
//     *
//     *
//     * @since  Sep 24, 2015   by Christopher K. Allen
//     */
//    public PolynomialFitter() {
//        this.ivlDomain = null;
//    }
//    
//    /**
//     * Create a new polynomial fit defining the polynomial but without
//     * specifying its domain of validity.  This constructor essential creates
//     * a polynomial object.
//     *
//     * @param arrCoef   array of coefficients defining the fitting polynomial
//     *
//     * @since  Sep 24, 2015   by Christopher K. Allen
//     */
//    public PolynomialFitter(double[] arrCoef) {
//        super(arrCoef);
//    }
//
//    /**
//     * Defining constructor for PolynomialFitter.
//     * 
//     * @param ivlDom    domain of accuracy for the fit
//     * @param arrCoef   array of coefficients for the polynomial
//     *
//     * @since  Sep 24, 2015   by Christopher K. Allen
//     */
//    public PolynomialFitter(Interval ivlDom, double[] arrCoef) {
//        super(arrCoef);
//        
//        this.ivlDomain = ivlDom;
//    }
//    
    
    /*
     * ISmoothRealFunction Interface
     */
    
//    /**
//     * Returns the interval representing the domain of validity for the
//     * polynomial fit.
//     * 
//     * @return      real interval where polynomial fit is valid 
//     *
//     * @since  Sep 24, 2015   by Christopher K. Allen
//     */
//    @Override
//    public Interval getDomain() {
//        return this.ivlDomain;
//    }
//
//    /**
//     * @throws IllegalArgumentException     the given value is not in the domain of validity
//     *
//     * @see xal.tools.math.fnc.poly.RealUnivariatePolynomial#evaluateAt(double)
//     *
//     * @since  Sep 24, 2015   by Christopher K. Allen
//     * 
//     */
//    @Override
//    public double evaluateAt(double dblVal) {
//        if (this.ivlDomain != null  && this.ivlDomain.membership(dblVal))
//            return super.evaluateAt(dblVal);
//        
//        throw new IllegalArgumentException("Argument is outside domain of validity");
//    }
//
//    /**
//     * @throws IllegalArgumentException     the given value is not in the domain of validity
//     * 
//     * @see xal.tools.math.fnc.poly.RealUnivariatePolynomial#derivativeAt(double)
//     *
//     * @since  Sep 24, 2015   by Christopher K. Allen
//     */
//    @Override
//    public double derivativeAt(double dblVal) {
//        if (this.ivlDomain != null  && this.ivlDomain.membership(dblVal))
//            return super.derivativeAt(dblVal);
//        
//        throw new IllegalArgumentException("Argument is outside domain of validity");
//    }
    
    
    /*
     * Operations
     */
    
    
    
    
    /*
     * Internal Support
     */
}
