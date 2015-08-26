/**
 * 
 */
package xal.tools.dsp;

import JSci.maths.Complex;


/**
 * <p>
 * Implements the fundamental behavior and characteristics of a 
 * Linear, Time-Invariant (LTI) digital 
 * filter of finite order with real coefficients.  Child classes can 
 * implement methods for 
 * determining the filter coefficients for desired bandwidth and other
 * transfer characteristics (i.e., Butterworth, Chebychev, etc.).  Once the
 * filter coefficients are determined this class contains most of the 
 * common behavior of a general digital filter.
 * </p>
 * <p>
 * The transfer characteristics for an <i>N</i><sup>th</sup> order digital
 * filter are given by the following:
 * <br>
 * <br>&nbsp;&nbsp;  <i>b</i><sub>0</sub>y<sub>n</sub></i> 
 *                 + <i>b</i><sub>1</sub><i>y<sub>n</i>-1</sub> 
 *                 + &hellip; 
 *                 + <i>b<sub>N</sub></i><i>y<sub>n-N</i></sub>
 *                 = <i>a</i><sub>0</sub>x<sub>n</sub></i>
 *                 + <i>a</i><sub>1</sub><i>x<sub>n</i>-1</sub>
 *                 + &hellip; 
 *                 + <i>a<sub>N</sub></i><i>x<sub>n-N</i></sub> 
 *                 <br>
 * <br>
 * where <i>n</i> is the current ("time") index, the {<i>x<sub>n</sub></i>} are the
 * inputs to the filter at time <i>n</i>, the {<i>a<sub>k</sub></i>} are the input
 * coefficients for delay <i>k</i>, the {<i>y<sub>n</sub></i>} are the 
 * filter outputs at time <i>n</i>, and the {<i>b<sub>k</sub></i>} are the output 
 * coefficients for delay <i>k</i>.  The equation can be
 * rearranged to explicitly demonstrate the current output <i>y<sub>n</sub></i> in terms
 * of the past <i>N</i> inputs and outputs
 * <br>
 * <br>&nbsp;&nbsp;  <i>y<sub>n</sub></i>  
 *                 = (
 *                 <i>a</i><sub>0</sub>x<sub>n</sub></i>
 *                 + <i>a</i><sub>1</sub><i>x<sub>n</i>-1</sub>
 *                 + &hellip; 
 *                 + <i>a<sub>N</sub></i><i>x<sub>n-N</i></sub> 
 *                 - <i>b</i><sub>1</sub><i>y<sub>n</i>-1</sub> 
 *                 - &hellip; 
 *                 - <i>b<sub>N</sub></i><i>y<sub>n-N</i></sub>
 *                 )/<i>b</i><sub>0</sub>
 *                 <br>
 * <br>
 * Note that the coefficient <i>b</i><sub>0</sub> is essentially just an attenuation/amplification 
 * factor.  (In fact, a zeroth-order digital filter is just that.)  The current class initializes 
 * itself with the value <i>b</i><sub>0</sub> = 1.0.  This case
 * is the only nonzero initial value for the input and output coefficients and is done simply to
 * avoid a pathological filter.  
 * </p>
 * <p>
 * Taking the <i>Z</i> transform of the above equations yields the transfer function 
 * <i>H</i>(<i>z</i>)
 * where <i>z</i> is the transform variable (whose domain is the unit circle in the complex
 * plane).  The transfer function has the general form
 * <br>&nbsp;&nbsp; 
 *                  <table>
 *                    <tr>
 *                      <td/>
 *                      <td>
 *                          <i>a</i><sub>0</sub> 
 *                        + <i>a</i><sub>1</sub><i>z</i><sup>-1</sup> 
 *                        + &hellip; 
 *                        + <i>a<sub>N</sub></i><i>z</i><sup>-N</sup>
 *                      </td>
 *                    </tr>
 *                    <tr>
 *                      <td> <i>H</i>(<i>z</i>) = </td>
 *                      <td>&mdash;&mdash;&mdash;&mdash;&mdash;&mdash;&mdash;&mdash;&mdash;&mdash;&mdash;</td>
 *                    </tr>
 *                    <tr>
 *                      <td/>
 *                      <td>
 *                          <i>b</i><sub>0</sub> 
 *                        + <i>b</i><sub>1</sub><i>z</i><sup>-1</sup>
 *                        + &hellip;
 *                        + <i>b<sub>N</sub></i><i>z</i><sup>-N</sup>
 *                      </td>
 *                    </tr>
 *                  </table>
 * Clearly then the filter is linear.  Note that for the Discrete Fourier Transform (DFT)
 * and the frequencies <i>&nu;</i> = 1,&hellip;,<i>&Nu;</i>-1 the transform variable is equal
 * to <i>z<sub>&nu;</sub> = <i>e</i><sup><i>i</i>2<i>&pi;&nu;</i>/<i>&Nu;</sup>.
 * </p>
 * 
 * @author Christopher K. Allen
 *
 * @see xal.tools.dsp.AbstractDigitalFilter
 */
public class LtiDigitalFilter extends AbstractDigitalFilter {

    
    /*
     * Local Attributes
     */
   
    /** input signal coefficients */
    private double[]        arrCoefInp;
    
    /** output signal coefficients */
    private double[]        arrCoefOut;
    

    
    /*
     * Abstract Methods
     */
    
    /**
     * Return the indicated input coefficient determined by the call to 
     * {@link LtiDigitalFilter#setInputCoefficient(int, double)}.  Note that
     * this value is time-independent, that is, the argument <var>iTime</var> is
     * ignored.
     * 
     * @param   iTime   current time index (ignored)
     * @param   iDelay  delay index of the filter coefficient
     * 
     * @return  the input coefficient for the given delay index
     * 
     * @see xal.tools.dsp.LtiDigitalFilter#setInputCoefficient(int, double)
     * @see xal.tools.dsp.AbstractDigitalFilter#getInputCoefficient(int, int)
     */
    @Override
    public double getInputCoefficient(int iTime, int iDelay) {
        return this.arrCoefInp[iDelay];
    }


    /**
     * Return the indicated output coefficient determined by the call to 
     * {@link LtiDigitalFilter#setOutputCoefficient(int, double)}.  Note that
     * this value is time-independent, that is, the argument <var>iTime</var> is
     * ignored.
     * 
     * @param   iTime   current time index (ignored)
     * @param   iDelay  delay index of the filter coefficient
     * 
     * @return  the output coefficient for the given delay index
     * 
     * @see xal.tools.dsp.LtiDigitalFilter#setOutputCoefficient(int, double)
     * @see xal.tools.dsp.AbstractDigitalFilter#getOutputCoefficient(int, int)
     */
    @Override
    public double getOutputCoefficient(int iTime, int iDelay) {
        return this.arrCoefOut[iDelay];
    }
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new filter object for processing discrete signal trains.
     * 
     * @param intOrder  filter order
     */
    public LtiDigitalFilter(int intOrder)   {
        super(intOrder);
        this.initialize(intOrder);
    }
    
    
    /**
     * Sets an input signal coefficient.  
     * 
     * @param iDelay    delay index of the coefficient
     * @param dblVal    coefficient value
     * 
     * @throws IllegalArgumentException index outside interval [0,Order]
     */
    public void setInputCoefficient(int iDelay, double dblVal) throws IllegalArgumentException {
        this.checkIndex(iDelay);
        this.arrCoefInp[iDelay] = dblVal;
    }
    
    /**
     * Sets all the input signal coefficients.  The elements of the argument 
     * array should be indexed by delay; that is, the 0<sup><i>th</i></sup> 
     * element corresponds to no delay, the 1<sup><i>st</i></sup> element to
     * the unit delay, etc.
     * 
     * @param   arrCoeffs   array of input coefficients
     * 
     * @throws IllegalArgumentException     argument has wrong array size
     */
    public void setInputCoefficients(double[] arrCoeffs) throws IllegalArgumentException {
        this.checkIndex(arrCoeffs.length);
        
        for (int iDelay=0; iDelay<arrCoeffs.length; iDelay++)
            this.setInputCoefficient(iDelay, arrCoeffs[iDelay]);
    }
    
    /**
     * Sets an output signal coefficient.  Note that since an index
     * of zero represents the current output it is an inverse scaling
     * value.
     * 
     * @param   iDelay  delay index of the coefficient
     * @param   dblVal  coefficient value
     * 
     * @throws IndexOutOfBoundsException     index outside interval [0,Order]
     */
    public void setOutputCoefficient(int iDelay, double dblVal) throws IndexOutOfBoundsException {
        this.checkIndex(iDelay);
        this.arrCoefOut[iDelay] = dblVal;
    }
    
    /**
     * Sets all the output signal coefficients.  The elements of the argument 
     * array should be indexed by delay; that is, the 0<sup><i>th</i></sup> 
     * element corresponds to no delay, the 1<sup><i>st</i></sup> element to
     * the unit delay, etc.
     * 
     * @param   arrCoeffs   array of output coefficients
     * 
     * @throws IllegalArgumentException     argument has wrong array size
     */
    public void setOutputCoefficients(double[] arrCoeffs) throws IllegalArgumentException {
        this.checkIndex(arrCoeffs.length);
        
        for (int iDelay=0; iDelay<arrCoeffs.length; iDelay++)
            this.setOutputCoefficient(iDelay, arrCoeffs[iDelay]);
    }
    

    /**
     * Attribute Query
     */
    
    /**
     * Return the input coefficient at the given delay index.
     * 
     * @param iDelay    delay index of coefficient
     * 
     * @return      input coefficient at delay given delay index.
     * 
     * @throws IndexOutOfBoundsException    delay index larger than filter order
     * 
     * @see LtiDigitalFilter#setInputCoefficient(int, double)
     */
    public double   getInputCoefficient(int iDelay) throws IndexOutOfBoundsException {
        this.checkIndex(iDelay);
        return this.arrCoefInp[iDelay];
    }
    
    /**
     * Return the output coefficient at the given delay index.
     * 
     * @param iDelay    delay index of coefficient
     * 
     * @return      output coefficient at delay given delay index.
     * 
     * @throws IndexOutOfBoundsException    delay index larger than filter order
     * 
     * @see LtiDigitalFilter#setOutputCoefficient(int, double)
     */
    public double   getOutputCoefficient(int iDelay) throws IndexOutOfBoundsException {
        this.checkIndex(iDelay);
        return this.arrCoefOut[iDelay];
    }
    
    
    /**
     * Operations
     */
    
    
    /**
     * Compute and return the value of the discrete transfer function
     * for the given value of z, the Z-transform variable.  
     * 
     * @param   z   Z-transform variable (lies on the unit circle)
     * 
     * @return      value of this filter's transfer function at z
     */
    public Complex  transferFunction(Complex z) {
        Complex cpxZpwr  = Complex.ONE;     // power of z
        Complex cpxDenom = Complex.ZERO;   // transfer function denominator
        Complex cpxNumer = Complex.ZERO;   // transfer function numerator
        
        for (int iDelay=0; iDelay<this.getCoefficientCount(); iDelay++)    {
            double  a = this.arrCoefInp[iDelay];
            double  b = this.arrCoefOut[iDelay];
            
            cpxNumer = cpxNumer.add( cpxZpwr.multiply(a) );
            cpxDenom = cpxDenom.add( cpxZpwr.multiply(b) );
            
            cpxZpwr  = cpxZpwr.divide(z);
        }
        
        return cpxNumer.divide(cpxDenom);
    }

    
    /**
     * Write out the configuration and state of this filter 
     * as a string for inspection.
     * 
     * @return      configuration and state of this filter in text form
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String  strBuffer = super.toString();
        
        strBuffer += "input coefficients :  " + this.arrCoefInp.toString() + "\n";
        strBuffer += "output coefficients: " + this.arrCoefOut.toString() + "\n";
        
        return strBuffer;
    }



    /*
     * Internal Support
     */
    
    /**
     * Initialize the filter coefficients and filter buffers.
     * 
     * @param intOrder      filter order
     */
    private void initialize(int intOrder) {
        this.arrCoefInp = new double[this.getCoefficientCount()];
        this.arrCoefOut = new double[this.getCoefficientCount()];

        for (int index=0; index<=intOrder; index++)  {
            this.arrCoefInp[index] = 0.0;
            this.arrCoefOut[index] = 0.0;
        }
        
        this.arrCoefOut[0] = 1.0;
    }
    
    /**
     * Check the given coefficient array index for bounds
     * errors.
     * 
     * @param index     coefficient array index
     * 
     * @throws IndexOutOfBoundsException    index outside bounds
     */
    private void checkIndex(int index) throws IndexOutOfBoundsException {
        if ((index<0) || (index>this.getOrder()))
            throw new IllegalArgumentException(
                            "DigitalFiler#checkIndex(): "
                          + "index outside domain [0,"
                          + this.getOrder()
                          + "]"
                            );
        
    }


    
}
