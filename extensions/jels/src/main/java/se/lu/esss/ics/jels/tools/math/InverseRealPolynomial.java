package se.lu.esss.ics.jels.tools.math;

import xal.tools.math.poly.UnivariateRealPolynomial;

/**
 * <p>Represents a model function of the form: 
 * ( 1 + x2(x0/x-1) + x3/2(x0/x-1)^2 + ... + xn/(n-1)!(x0/x-1)^(n-1) ) / x1
 * where x0,x1,...,xn are parameters.</p>
 * 
 * <p>This is an encapsulation of a TTF function as is used in TraceWin.
 * The class is extended from UnivariateRealPolynomial just so it can be 
 * easily switched with it. </p> 
 *
 * @author Ivo List
 *
 */
public class InverseRealPolynomial extends UnivariateRealPolynomial {
    /*
     *  Local Attributes
     */

     /** the vector of coefficients */
     private double[]   m_arrCoef = null;


     /*
      * Initialization
      */


    /**
     * Creates an empty polynomial object, the zero polynomial.
     */
    public InverseRealPolynomial() {
    }

    /**
     * Creates and initializes a polynomial to the specified coefficients.
     *
     * @param iOrder
     * @return
     */
    public InverseRealPolynomial(double[] arrCoef)   {
        this.setCoefArray(arrCoef);
    }

    /**
     * Set the entire coefficient array.  The coefficient
     * array is arranged in order of ascending indeterminate order.
     *
     * @param arrCoef   double array of coefficients.
     */
    public void setCoefArray(double[] arrCoef)   {
        this.m_arrCoef = arrCoef;
    }


    /*
     * Attribute Queries
     */


    /**
     * Return the degree of the polynomial.  That is, the highest
     * indeterminant order for all the nonzero coefficients.
     */
    public int  getDegree() {
        return this.getCoefs().length-1;
    }

    /**
     * Get the specified coefficient value.
     *
     * If the value of <code>iOrder</code> is larger than the size of the
     * coefficient array then the coefficient is assumed to have value zero.
     *
     * @param iOrder    index of coefficient
     * @return          coefficient
     */
    public double getCoef(int iOrder)   {
        if (this.m_arrCoef.length == 0)
            return 0.0;
        if (iOrder >= this.m_arrCoef.length)
            return 0.0;

        return this.m_arrCoef[iOrder];
    }

    /**
     * Return the entire array of coefficients. 
     *
     * @return      the entire coefficient array
     */
    public double[] getCoefs()      {
        return this.m_arrCoef;
    }



    /*
     *  Polynomial operations
     */

    /**
     * Evaluate the model function for the specified value: 
     * (1 + x2(x0/x-1) + x3/2(x0/x-1)^2 + ... + xn/(n-1)!(x0/x-1)^(n-1)) / x1
     * where x0,x1,...,xn are coefficients.
     * 
     * @param   dblVal      indeterminate value to evaluate the model function at
     */
    public double evaluateAt(double dblVal) {
        if (this.m_arrCoef == null || this.m_arrCoef.length == 0)
            return 1.0;

        double x0 = m_arrCoef[0];
        if (x0 == 0.0) return 1.0;
        
        int     N = this.m_arrCoef.length;      // number of coefficients
        double  dblAccum = 0.0;                 // accumulator

        for (int n=N-1; n>=1; n--) {
        	double f = 1.;
        	for (int j = 2; j<n; j++) f*=j;
        	dblAccum += this.getCoef(n) * Math.pow(x0/dblVal-1, n-1) / f;
        }

        return dblAccum / m_arrCoef[1];
    }

    /**
     * Evaluate derivative of the model function for the specified value of the indeterminate.
     * If the coefficient vector has not been specified, it return 0.
     * 
     * (-x2(x0 x^-2) - x3(x0/x-1)(x0 x^-2) - ... - xn/(n-2)!(x0/x-1)^(n-2)(x0 x^-2)) / x1
     * where x0,x1,...,xn are coefficients.
     * 
     * @param   dblVal      indeterminate value to evaluate the model function derivative
     */
    public double evaluateDerivativeAt(double dblVal) {
        if (this.m_arrCoef == null || this.m_arrCoef.length == 0)
            return 0.0;

        double x0 = m_arrCoef[0];
        int     N = this.m_arrCoef.length;      // number of coefficients
        double  dblAccum = 0.0;                 // accumulator

        for (int n=N-1; n>=2; n--) {
        	double f = 1.;
        	for (int j = 2; j<n-1; j++) f*=j;
        	dblAccum += this.getCoef(n) * Math.pow(x0/dblVal-1, n-2) * x0 / dblVal / dblVal / f;
        }

        return -dblAccum / m_arrCoef[1];
    }

    public UnivariateRealPolynomial plus(UnivariateRealPolynomial polyAddend)  {
    	throw new UnsupportedOperationException();
    }

    public UnivariateRealPolynomial times(UnivariateRealPolynomial polyFac) {
    	throw new UnsupportedOperationException();
    }


    /*
     * Testing and Debugging
     */



    /**
     * Construct and return a textual representation of the contents of this
     * model function as a <code>String</code> object.
     *
     * @return a String representation of the model function contents
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        int     N = this.getDegree();

        String  x0 = Double.toString(this.getCoef(0));
        String  strPoly = "("+Double.toString(this.getCoef(1));

        for (int n=2; n<=N; n++)
            strPoly += " + " + this.getCoef(n) + "("+ x0+"/x)^" + (n-1);

        return strPoly + ")/x1";
    }

}
