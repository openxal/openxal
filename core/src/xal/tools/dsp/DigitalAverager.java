/**
 *  DigitalAverager.java
 *  
 *  Created     : September, 2007
 *  Author      : Christopher K. Allen
 */
package xal.tools.dsp;


/**
 * Performs a running average on an input signal.  This object is a 
 * linear digital filter which is not time-invariant.
 * 
 * @author Christopher K. Allen
 *
 */
public class DigitalAverager extends AbstractDigitalFilter {

    /**
     * Create a new <code>DigitalAverager</code> object.
     */
    public DigitalAverager() {
        super(1);
    }

    /**
     * Returns the input coefficient for the given time index.  The only non-zero
     * input coefficient is <i>a</i><sub>0</sub> for no delay.  Its value is given by
     * <br>
     * <br>&nbsp;&nbsp;  <i>a</i><sub>0</sub>(<i>n</i>) = 1/(<i>n</i> + 1) <br>
     * <br>
     * 
     * @param   iTime   current time index
     * @param   iDelay  delay index of coefficient
     * 
     * @return  input coefficient for given delay index and current time
     * 
     * @see xal.tools.dsp.AbstractDigitalFilter#getInputCoefficient(int, int)
     */
    @Override
    public double getInputCoefficient(int iTime, int iDelay) {
        if (iDelay == 0)
            return 1.0/(iTime + 1.0);
        else
            return 0.0;
    }

    /**
     * Returns the output coefficient for the given time index.  The values of 
     * the coefficients <i>b</i><sub>0</sub> and <i>b</i><sub>1</sub> are given
     * by
     * <br>
     * <br>&nbsp;&nbsp;  <i>b</i><sub>0</sub>(<i>n</i>) = 1 <br>
     * <br>&nbsp;&nbsp;  <i>b</i><sub>-1</sub>(<i>n</i>) = <i>n</i>/(<i>n</i> + 1) <br>
     * <br>
     * 
     * @param   iTime   current time index
     * @param   iDelay  delay index of coefficient
     * 
     * @return  output coefficient for given delay index and current time
     * 
     * @see xal.tools.dsp.AbstractDigitalFilter#getOutputCoefficient(int, int)
     */
    @Override
    public double getOutputCoefficient(int iTime, int iDelay) {
        switch (iDelay) {
        case 0:
            return 1.0;
            
        case 1:
            return -1.0*iTime/(iTime + 1.0);
            
        default:
            return 0.0;
        }
    }


}
