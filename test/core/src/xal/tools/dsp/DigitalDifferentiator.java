/**
 * DigitalDifferentiator.java
 * 
 * Created      : September, 2007
 * Author       : Christopher K. Allen
 */
package xal.tools.dsp;


/**
 * <p>
 * Convenience class implementing a simple 1<sup>st</sup> order digital differentiator.
 * The response <i>y<sub>n</sub></i> of this filter to an input <i>x<sub>n</sub></i>
 * is given by 
 * <br>
 * <br>&nbsp;&nbsp;  <i>y<sub>n</sub></i> = <i>x<sub>n</sub></i> - <i>x<sub>n</i>-1</sub><br>
 * <br>
 * Thus, the transfer function <i>H</i>(<i>z</i>) is given by
 * <br>
 * <br>&nbsp;&nbsp;  <i>H</i>(<i>z</i>) = 1 - <i>z</i><sup>-1</sup><br>
 * <br>
 * where <i>z</i> is the Z-transform variable.
 * </p>
 * <p>
 * The differentiator is initialized so that the initial input <i>x</i><sub>-1</sub>
 * is zero.  Thus, the first output from this filter is the first input.  This value 
 * may be changed with a call to 
 * {@link DigitalIntegrator#setInputCoefficient(int, double)}.
 * </p>
 * 
 * @author Christopher K. Allen
 *
 * @see xal.tools.dsp.LtiDigitalFilter
 */
public class DigitalDifferentiator extends LtiDigitalFilter {

    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>DigitalDifferentiator</code> object
     * with zero initial response.
     * 
     * @see DigitalDifferentiator#setInitialResponse(double)
     */
    public DigitalDifferentiator()  {
        this(0.0);
    }
    
    /**
     * Create a new <code>DigitalDifferentiator</code> object
     * with the given initial response.
     * 
     * @param dblVal    initial response of the differentiator
     */
    public DigitalDifferentiator(double dblVal) {
        super(1);
        super.setInputCoefficient(0, 1.0);
        super.setInputCoefficient(1, -1.0);
        super.setOutputCoefficient(1, 0.0);
        this.setInitialResponse(dblVal);
    }
    
    /**
     * Set the initial response of the differentiator to the
     * given value.  Note also the the filter is reset.
     * 
     * @param dblVal    initial response of the differentiator
     * 
     * @see LtiDigitalFilter#reset()
     */
    public void setInitialResponse(double dblVal)  {
        this.reset();
        this.response(dblVal);
    }

}
