/**
 *  DigitalIntegrator.java
 *  
 *  Created     : September, 2007
 *  Author      : Christopher K. Allen
 */
package xal.tools.dsp;


/**
 * <p>
 * Convenience class implementing a simple 1<sup>st</sup> order digital integrator.
 * The response <i>y<sub>n</sub></i> of this filter to an input <i>x<sub>n</sub></i>
 * is given by 
 * <br>
 * <br>&nbsp;&nbsp;  <i>y<sub>n</sub></i> = <i>y<sub>n</i>-1</sub> + <i>x<sub>n</sub></i><br>
 * <br>
 * Thus, the transfer function <i>H</i>(<i>z</i>) is given by
 * <br>
 * <br>&nbsp;&nbsp;  <i>H</i>(<i>z</i>) = 1/(1 - <i>z</i><sup>-1</sup>)<br>
 * <br>
 * where <i>z</i> is the Z-transform variable.  Note that the integrator is unstable
 * for zero frequency corresponding to <i>z</i> = 1, which is expected for integration.
 * </p>
 * <p>
 * The integrator is initialized so that the constant of integration (<i>y</i><sub>-1</sub>)
 * is zero.  This value may be changed with a call to 
 * {@link DigitalIntegrator#setConstantOfIntegration(double)}.
 * </p>
 * 
 * @author Christopher K. Allen
 *
 * @see xal.tools.dsp.LtiDigitalFilter
 */
public class DigitalIntegrator extends LtiDigitalFilter {

    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>DigitalIntegrator</code> object with
     * zero constant of integration.
     */
    public DigitalIntegrator()  {
        this(0.0);
    }
    
    /**
     * Create a new <code>DigitalIntegrator</code> object with the
     * given constant of integration.
     * 
     * @param dblConst  constant of integration
     * 
     * @see DigitalIntegrator#setConstantOfIntegration(double)
     */
    public DigitalIntegrator(double dblConst)  {
        super(1);
        super.setInputCoefficient(0, 1.0);
        super.setInputCoefficient(1, 0.0);
        super.setOutputCoefficient(1, -1.0);
        
        this.setConstantOfIntegration(dblConst);
    }
    
    
    /**
     * Sets the constant of integration equal to the given
     * value.  Note also that the filter is reset.
     * 
     * @param dblConst  constant of integration
     * 
     * @see LtiDigitalFilter#reset()
     */
    public void setConstantOfIntegration(double dblConst) {
        this.reset();
        this.response(dblConst);
    }
}
