/**
 * ExpFilter.java
 * 
 * Created      : September, 2007
 * Author       : Christopher K. Allen
 */
package xal.tools.dsp;

import JSci.maths.Complex;
import JSci.maths.vectors.AbstractComplexVector;


/**
 * <p>
 * Discrete filter based upon the <code>FourierExpTransform</code> class.  
 * </p>
 * <p>
 * When the filters are described as "perfect", we are implying that they do not roll 
 * off with frequency (thus, they are of infinite order).  The transfer function of the
 * filter has values of either unity or zero.  This may or may not correspond to a 
 * linear phase filter.
 * </p> 
 * 
 * @author Christopher K. Allen
 *
 * @see xal.tools.dsp.FourierExpTransform
 */
public class ExpFilter {

    
    /*
     * Local Attributes
     */
    
    /** the transformer object */
    private FourierExpTransform    dftXfrm = null;
    
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new filter object for processing discrete functions (i.e.,
     * objects of type <code>double[]</code>) with the given size.
     * 
     * @param szData    size of the functions this filter can process
     */
    public ExpFilter(int szData)   {
        this.dftXfrm = new FourierExpTransform(szData);
    }
    
    /*
     * Attribute Query
     */
    
    /**
     * Return the expected size of the discrete function to be
     * processed.
     * 
     * @return  array size for functions this filter object can process
     */
    public int  getDataSize()   {
        return this.dftXfrm.getDataSize();
    }
    
    /**
     * Return the maximum discrete frequency processed by this filter.  
     *
     * @return      maximum (positive) filter frequency
     */
    public int  getMaximumFrequency()   {
        return this.getDataSize()/2;
    }
    
    
    
    /*
     * Operations
     */
    
    /**
     * Perfect low-pass filter.  Any frequency components <strong>greater</strong>
     * than the given (discrete) cut off frequency are complete removed.  The filtered 
     * function is returned.
     * 
     * @param intFreq   discrete cut off frequency
     * @param arrFunc   discrete function to be filtered
     * 
     * @return          filtered discrete function
     * 
     * @throws IndexOutOfBoundsException    cut off frequency either negative or greater than max frequency
     * @throws IllegalArgumentException     discrete function size is not equal to filter dimensions
     */
    public double[] perfectLowPass(int intFreq, final double[] arrFunc) 
        throws IndexOutOfBoundsException, IllegalArgumentException  
    {
        // Check arguments
        this.checkFunction(arrFunc);
        this.checkFrequency(intFreq);
        
        // Transform function to frequency domain then remove components greater than cutoff
        int                     iFreqMax = this.getMaximumFrequency();
        AbstractComplexVector   vecTrans = this.getTranformer().transform(arrFunc);
        
        for (int index=intFreq+1; index<iFreqMax; index++)
            this.clearFreqComponent(index, vecTrans);

//        // Correct the phasing
//        this.linearPhase(vecTrans);
        
        // Transform back to time domain and return
        double[]    arrRecvrd = this.getTranformer().inverse(vecTrans);
        
        return arrRecvrd;
    }
    
    
    /**
     * Perfect high-pass filter.  Any frequency components <strong>less</strong> than
     * the given (discrete) cut off frequency are complete removed.  The filtered 
     * function is returned.
     * 
     * @param intFreq   discrete cut off frequency
     * @param arrFunc   discrete function to be filtered
     * 
     * @return          filtered discrete function
     * 
     * @throws IndexOutOfBoundsException    cut off frequency either negative or greater than max frequency
     * @throws IllegalArgumentException     discrete function size is not equal to filter dimensions
     */
    public double[] perfectHighPass(int intFreq, final double[] arrFunc) 
        throws IndexOutOfBoundsException, IllegalArgumentException  
    {
        // Check arguments
        this.checkFunction(arrFunc);
        this.checkFrequency(intFreq);
        
        // Transform function to frequency domain then remove components less than cutoff
        AbstractComplexVector   vecTrans = this.getTranformer().transform(arrFunc);
        
        for (int index=0; index<intFreq; index++)    
            this.clearFreqComponent(index, vecTrans);

        // Transform back to time domain and return
        double[]    arrRecvrd = this.getTranformer().inverse(vecTrans);
        
        return arrRecvrd;
    }

    /**
     * Perfect band-pass filter.  Any frequency components <strong>outside</strong>
     * the the given (discrete) cut off frequencies (exclusive) are complete removed.
     * That is, only discrete frequency components inside the interval 
     * [<var>intFreqLow</var>,<var>intFreqHigh</var>] are contained in the
     * returned function.  
     * 
     * @param intFreqLow    discrete low-band cut off frequency
     * @param intFreqHigh   discrete high-band cut off frequency
     * @param arrFunc       discrete function to be filtered
     * 
     * @return          filtered discrete function
     * 
     * @throws IndexOutOfBoundsException    cut off frequency either negative or greater than data size
     * @throws IllegalArgumentException     discrete function size is not equal to filter dimensions
     */
    public double[] perfectBandPass(int intFreqLow, int intFreqHigh, final double[] arrFunc) 
        throws IndexOutOfBoundsException, IllegalArgumentException  
    {
        // Check arguments
        this.checkFunction(arrFunc);
        this.checkFrequency(intFreqLow);
        this.checkFrequency(intFreqHigh);
        
        // Transform function to frequency domain then remove components greater than cutoff
        int                     intFreqMax = this.getMaximumFrequency();
        AbstractComplexVector   vecTrans = this.getTranformer().transform(arrFunc);
        
        for (int index=0; index<intFreqLow; index++)    // remove low frequencies    
            this.clearFreqComponent(index, vecTrans);
        for (int index=intFreqHigh+1; index<intFreqMax; index++)    // remove high frequencies
            this.clearFreqComponent(index, vecTrans);

        // Transform back to time domain and return
        double[]    arrRecvrd = this.getTranformer().inverse(vecTrans);
        
        return arrRecvrd;
    }
    
    /**
     * Perfect notch filter.  Any frequency components <strong>inside</strong> the
     * the given (discrete) cut off frequencies (inclusive) are complete removed.
     * That is, only discrete frequency components outside the interval 
     * [<var>intFreqLow</var>,<var>intFreqHigh</var>] are contained in the
     * returned function.  
     * 
     * @param intFreqLow    discrete low-band cut off frequency
     * @param intFreqHigh   discrete high-band cut off frequency
     * @param arrFunc   discrete function to be filtered
     * 
     * @return          filtered discrete function
     * 
     * @throws IndexOutOfBoundsException    cut off frequency either negative or greater than data size
     * @throws IllegalArgumentException     discrete function size is not equal to filter dimensions
     */
    public double[] perfectNotch(int intFreqLow, int intFreqHigh, final double[] arrFunc) 
        throws IndexOutOfBoundsException, IllegalArgumentException  
    {
        // Check arguments
        this.checkFunction(arrFunc);
        this.checkFrequency(intFreqLow);
        this.checkFrequency(intFreqHigh);
        
        // Transform function to frequency domain then remove components greater than cutoff
        AbstractComplexVector   vecTrans = this.getTranformer().transform(arrFunc);
        
        for (int index=intFreqLow; index<intFreqHigh; index++)    // remove notch frequencies    
            this.clearFreqComponent(index, vecTrans);

        // Transform back to time domain and return
        double[]    arrRecvrd = this.getTranformer().inverse(vecTrans);
        
        return arrRecvrd;
    }
    
    /**
     * Perfect notch filter.  Removes a single
     * (discrete) frequency component from the given function.  
     * 
     * @param intFreq   discrete frequency to be removed
     * @param arrFunc   discrete function to be filtered
     * 
     * @return          filtered discrete function
     * 
     * @throws IndexOutOfBoundsException    cut off frequency either negative or greater than data size
     * @throws IllegalArgumentException     discrete function size is not equal to filter dimensions
     */
    public double[] perfectNotch(int intFreq, final double[] arrFunc)  
        throws IndexOutOfBoundsException, IllegalArgumentException  
    {
        return this.perfectNotch(intFreq, intFreq, arrFunc);
    }
    
    
    
    /*
     * Internal Support
     */
    
    /**
     * Parabolic low-pass filter.  Any frequency components <strong>greater</strong>
     * than the given (discrete) cut off frequency are complete removed.  Frequencies
     * in the pass-band are attenuated by a transfer function with unit high and 
     * parabolic shape with value zero at the cut off frequency.  
     * 
     * @param intFreq   discrete cut off frequency
     * @param arrFunc   discrete function to be filtered
     * 
     * @return          filtered discrete function
     * 
     * @throws IndexOutOfBoundsException    cut off frequency either negative or greater than max frequency
     * @throws IllegalArgumentException     discrete function size is not equal to filter dimensions
     */
    public double[] parabolicLowPass(int intFreq, final double[] arrFunc) 
        throws IndexOutOfBoundsException, IllegalArgumentException  
    {
        // Check arguments
        this.checkFunction(arrFunc);
        this.checkFrequency(intFreq);

        // Transform function to frequency domain then remove components greater than cutoff
        int                     iFreqMax = this.getMaximumFrequency();
        AbstractComplexVector   vecTrans = this.getTranformer().transform(arrFunc);

        for (int index=intFreq; index<iFreqMax; index++)
            this.clearFreqComponent(index, vecTrans);

        // Attenuate the pass-band
        for (int index=1; index<intFreq; index++)   {
            double      dblH = (1.0*index)/intFreq;
            Complex     cpxA = new Complex(1.0 - dblH*dblH, 0.0);

            this.amplifyFreqComponent(index, cpxA, vecTrans);
        }

        //        // Correct the phasing
        //        this.linearPhase(vecTrans);

        // Transform back to time domain and return
        double[]    arrRecvrd = this.getTranformer().inverse(vecTrans);

        return arrRecvrd;
    }

    
    
    /*
     * Internal Support
     */
    
    
    /**
     * Returns the sine transform object for this instance.
     * 
     * @return  the transform object
     */
    private FourierExpTransform    getTranformer() {
        return this.dftXfrm;
    }
    
    /**
     * Set the given frequency component to zero.
     * 
     * @param intFreq       discrete frequency
     * 
     * @param vecTrans      transform function
     */
    private void clearFreqComponent(int intFreq, AbstractComplexVector vecTrans) {
        int     N = this.getDataSize();
        
        vecTrans.setComponent(intFreq, Complex.ZERO);
        
        if (intFreq == 0) return;
        vecTrans.setComponent(N - intFreq, Complex.ZERO);
    }
    
    /**
     * Amplify/Attenuate the given frequency component to by the given factor.
     * 
     * @param intFreq       discrete frequency
     * @param cpxA          amplification/Attenuation factor
     * 
     * @param vecTrans      transform function
     */
    private void amplifyFreqComponent(int intFreq, Complex cpxA, AbstractComplexVector vecTrans) {
        int     N = this.getDataSize();
        
        Complex cpxSpec;
        
        cpxSpec = vecTrans.getComponent(intFreq);
        vecTrans.setComponent(intFreq, cpxSpec.multiply(cpxA));

        if (intFreq == 0) return;
        cpxSpec = vecTrans.getComponent(N - intFreq);
        vecTrans.setComponent(N - intFreq, cpxSpec.multiply(cpxA));
    }
    
    /**
     * Correct the phasing of the frequency response to simulate that of a
     * linear phase filter.  These filters have constant time delay for
     * all frequencies.
     * 
     * @param vecTrans  transformed spectrum to be phased
     */
    private void    linearPhase(AbstractComplexVector vecTrans) {
        int     N = this.getDataSize();
        double  h = 2.0*Math.PI/N;
        
        for (int iFreq=0; iFreq<N; iFreq++) {
            double  cos = Math.cos(iFreq*h);
            double  sin = Math.sin(iFreq*h);

            Complex cpxPhase = new Complex(cos, -sin);
            Complex cpxSpec  = vecTrans.getComponent(iFreq);
            vecTrans.setComponent(iFreq, cpxSpec.multiply(cpxPhase));
        }
    }
    
    /**
     * Check the given discrete function for the proper dimensions.
     * 
     * @param arrFunc   discrete function to check
     * 
     * @throws IllegalArgumentException     function did not have proper dimensions
     */
    private void checkFunction(double[] arrFunc) throws IllegalArgumentException {
        if (arrFunc.length != this.getDataSize())
            throw new IllegalArgumentException(
                            "ExpFilter#checkFunction() - given function has size = " 
                          + Integer.toString(arrFunc.length)
                          + ", expected size = "
                          + Integer.toString(this.getDataSize())
                            );
    }
    
    /**
     * Check the given frequency to see that it lies within the interval of 
     * processing frequencies for this object.
     * 
     * @param intFreq   discrete frequency to inspect
     * 
     * @throws IndexOutOfBoundsException    frequency lies outside acceptable interval
     */
    private void checkFrequency(int intFreq) throws IndexOutOfBoundsException {
        if ( (intFreq < 0) || (intFreq > this.getMaximumFrequency()) )
            throw new IndexOutOfBoundsException(
                            "ExpFilter#checkFrequency() - given frequency = " 
                            + Integer.toString(intFreq)
                            + "outside interval [0,"
                            + Integer.toString(this.getDataSize()/2)
                            + "]"
                            );
    }
}
