/**
 * 
 */
package xal.tools.dsp;

import xal.tools.collections.LinearBuffer;


/**
 * <p>
 * Implements the fundamental behavior and characteristics of a 
 * digital filter of finite order with real coefficients.  Child classes can 
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
 * <br>&nbsp;&nbsp;  <i>b</i><sub>0</sub>(<i>n</i>)<i>y</i>(<i>n</i>) 
 *                 + <i>b</i><sub>1</sub>(<i>n</i>)<i>y</i>(<i>n</i>-1) 
 *                 + &hellip; 
 *                 + <i>b<sub>N</sub></i>(<i>n</i>)<i>y</i>(<i>n-N</i>)
 *                 = <i>a</i><sub>0</sub>(<i>n</i>)<i>x</i>(<i>n</i>)
 *                 + <i>a</i><sub>1</sub>(<i>n</i>)<i>x</i>(<i>n</i>-1) 
 *                 + &hellip; 
 *                 + <i>a<sub>N</sub></i>(<i>n</i>)<i>x</i>(<i>n-N</i>)
 *                 <br>
 * <br>
 * where <i>n</i> is the current ("time") index, the {<i>x</i>(<i>n</i>)} are the
 * inputs to the filter at time <i>n</i>, the {<i>a<sub>k</sub></i>(<i>n</i>)} 
 * are the input coefficients at time <i>n</i> for delay <i>k</i>, 
 * the {<i>y</i>(<i>n</i>)} are the filter outputs at time <i>n</i>,
 * and the {<i>b<sub>k</sub></i>(<i>n</i>)} are the output coefficients at time <i>n</i>
 * for delay <i>k</i>.  The equation can be
 * rearranged to explicitly demonstrate the current output <i>y</i>(<i>n</i>) in terms
 * of the past <i>N</i> inputs and output
 * <br>
 * <br>&nbsp;&nbsp;  <i>y</i>(<i>n</i>)  
 *                 = (
 *                   <i>a</i><sub>0</sub>(<i>n</i>)<i>x</i>(<i>n</i>)
 *                 + <i>a</i><sub>1</sub>(<i>n</i>)<i>x</i>(<i>n</i>-1) 
 *                 + &hellip; 
 *                 + <i>a<sub>N</sub></i>(<i>n</i>)<i>x</i>(<i>n-N</i>) 
 *                 - <i>b</i><sub>1</sub>(<i>n</i>)<i>y</i>(<i>n</i>-1) 
 *                 - &hellip; 
 *                 - <i>b</i><sub>N</sub>(<i>n</i>)<i>y</i>(<i>n</i>-<i>N</i>) 
 *                 )/<i>b</i><sub>0</sub>(<i>n</i>)
 *                 <br>
 * <br>
 * Note that the coefficient <i>b</i><sub>0</sub> is essentially just an attenuation/amplification 
 * factor.  (In fact, a zeroth-order digital filter is just that.)  
 * </p>
 * 
 * @author Christopher K. Allen
 * 
 * @see AbstractDigitalFilter#getInputCoefficient(int, int)
 * @see AbstractDigitalFilter#getOutputCoefficient(int, int)
 *
 */
public abstract class AbstractDigitalFilter {

    /*
     * Local Attributes
     */
   
    /** filter order */
    private int             intOrder;
    
    /** number of input and output coefficients (order + 1) */
    private int             cntCoefs;

    
    
    /** the current output index */
    private int             intTime;
    
    
    /** input signal buffer */
    private LinearBuffer<Double> bufInput;
    
    /** output signal buffer */
    private LinearBuffer<Double> bufOutput;
    
    

    
    /*
     * Abstract Methods
     */
    
    /**
     * Get the input signal coefficient <i>a<sub>k</sub></i>(<i>n</i>) for
     * the given time and delay.  
     * 
     * @param iTime    current time
     * @param iDelay   delay index of the coefficient
     * 
     * @return  output coefficient at time <var>iTime</var> and delay <var>iDelay</var>
     */
    public abstract double getInputCoefficient(int iTime, int iDelay);
    
    /**
     * Get the output signal coefficient <i>b<sub>k</sub></i>(<i>n</i>) for
     * the given time and delay.  
     * 
     * @param iTime    current time
     * @param iDelay   delay index of the coefficient
     * 
     * @return  output coefficient at time <var>iTime</var> and delay <var>iDelay</var>
     */
    public abstract double getOutputCoefficient(int iTime, int iDelay);
    
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new filter object for processing discrete signals.
     * 
     * @param intOrder  filter order
     */
    public AbstractDigitalFilter(int intOrder)   {
        this.initialize(intOrder);
    }
    
    
    
    /**
     * Attribute Query
     */
    
    /**
     * Returns the order of the digital filter.
     * 
     * @return      filter order
     */
    public int  getOrder()  {
        return this.intOrder;
    }
    
    /**
     * Returns the number of input and output coefficients.  This values is one larger
     * than the filter order.
     * 
     * @return  size of coefficient arrays (i.e., DigitalFilter#getOrder() + 1)
     */
    public int getCoefficientCount()   {
        return this.cntCoefs;
    }
    
    /**
     * <p>
     * Return the value of the current time index.  This value is the number
     * of signal values processed (by a call to {@link AbstractDigitalFilter#response(double)})
     * since the last call to {@link AbstractDigitalFilter#reset()}.
     * After calling <code>reset()</code> the returned index is zero.  Thus, after
     * calling <code>response(double)</code> for the first time the returned value is 1.
     * </p>
     * <p>
     * <strong>IMPORTANT</strong>
     * <br>
     * For child classes implementing the methods 
     * {@link AbstractDigitalFilter#getInputCoefficient(int, int)} and
     * {@link AbstractDigitalFilter#getOutputCoefficient(int, int)}
     * the methods are called <strong>before</strong> this index is updated.
     * For example, upon the first call to {@link AbstractDigitalFilter#response(double)}
     * these methods will be called with the time index as 0. 
     * </p> 
     * 
     * @return  the current signal index ("time" index)
     * 
     * @see AbstractDigitalFilter#reset()
     * @see AbstractDigitalFilter#response(double)
     */
    public int  getTimeIndex()    {
        return this.intTime;
    }

    
    
    /**
     * Operations
     */
    
    
    /**
     * <p>
     * Compute and return the response of the filter to the given
     * input.  The returned response is assumed to be part of a 
     * train of values with depends upon the previous inputs to 
     * this method.  The number of previous input values to which
     * the output depends is given by the order of this filter.
     * </p>
     * <p>
     * To begin processing a new input signal train the method
     * <code>DigitalFilter.{@link #reset()}</code> should be called.
     * </p>
     * <p>
     * <strong>IMPORTANT</strong>
     * <br>
     * For child classes, the methods 
     * {@link AbstractDigitalFilter#getInputCoefficient(int, int)} and
     * {@link AbstractDigitalFilter#getOutputCoefficient(int, int)}
     * are called within this method to retrieve the filter coefficients
     * at the current time index.  The index used in these calls is the
     * pre-updated value, not the value after this function returns.
     * For example, upon the first call to {@link AbstractDigitalFilter#response(double)}
     * these methods will be called with the time index as 0. 
     * </p> 
     * @param dblInput  current input signal
     * 
     * @return          response of this filter to the given signal
     * 
     * @see AbstractDigitalFilter#reset()
     * @see AbstractDigitalFilter#getOrder()
     */
    public double   response(double dblInput) {
        int     iTime;              // current time index
        int     iDelay;             // current coefficient delay index
        double  dblOutput;          // current output value
        
        iTime  = this.getTimeIndex();
        
        // process the inputs
        iDelay = 0;
        dblOutput = this.getInputCoefficient(iTime, iDelay++)*dblInput;
        for (Double dblInpDel : this.bufInput) {
            double  dblCoef = this.getInputCoefficient(iTime, iDelay++);
            
            dblOutput += dblCoef*dblInpDel;
        }
        
        // process the outputs
        iDelay = 1;
        for (Double dblOutDel : this.bufOutput) {
            double  dblCoef = this.getOutputCoefficient(iTime, iDelay++);
            
            dblOutput -= dblCoef*dblOutDel;
        }
        dblOutput = dblOutput/this.getOutputCoefficient(iTime, 0);
        
        
        // Load buffers, update the index, and return
        this.bufInput.add(dblInput);
        this.bufOutput.add(dblOutput);
        this.intTime++;
        
        return dblOutput;
    }
    
    /**
     * Convenience function for computing the response of this filter to
     * an input signal train.  This method simply calls 
     * {@link AbstractDigitalFilter#response(double)} sequentially by increasing index
     * for each element of the argument <var>arrTrain</var>.  Thus, the 
     * returned response depends upon the initial state of the filter when 
     * this method is called.
     * 
     * @param arrTrain  array of input signal 
     * 
     * @return  output signal response of the this filter to given input
     * 
     * @see AbstractDigitalFilter#response(double)
     */
    public double[] response(final double[] arrTrain) {
        int     N = arrTrain.length;
        
        double[]    arrResp = new double[N];
        for (int n=0; n<N; n++) 
            arrResp[n] = this.response(arrTrain[n]);
        
        return arrResp;
    }
    

    /**
     * Clears the input and output buffers, resetting filter for 
     * a new signal.
     */
    public void reset() {
        this.intTime = 0;
        this.bufInput.clear();
        this.bufOutput.clear();
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
        String  strBuffer = "";
        
        strBuffer += "filter order  = " + this.getOrder() + "\n";
        strBuffer += "current index = " + this.getTimeIndex() + "\n";
        strBuffer += "input buffer\n";
        strBuffer += this.bufInput.toString() + "\n";
        strBuffer += "output buffer\n";
        strBuffer += this.bufOutput + "\n";
        
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
        this.intTime   = 0;
        this.intOrder = intOrder;
        this.cntCoefs  = intOrder + 1;
        
        this.bufInput   = new LinearBuffer<Double>(intOrder);
        this.bufOutput  = new LinearBuffer<Double>(intOrder);
    }
    
    /**
     * Check the given coefficient delay index for bounds
     * errors.
     * 
     * @param index     coefficient delay index
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
