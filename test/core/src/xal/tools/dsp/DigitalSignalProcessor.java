/**
 * DigitalSignalProcessor.java
 * 
 * Created      : September, 2007
 * Author       : Christopher K. Allen
 */
package xal.tools.dsp;



/**
 * <p>
 * Convenience class for packaging common digital processing operations.
 * This class relies heavily upon most of the other classes on the 
 * <code>xal.tools.dsp</code> package.
 * </p>
 * <p>
 * Rather than making this a utility class, it has been designed to be
 * instantiated.  In this manner it can be tuned by the users.  Although
 * this capability is not available now, this class may evolve.
 * </p>
 * 
 * @author Christopher K. Allen
 *
 */
public class DigitalSignalProcessor {
    
    
    
    /*
     * Local Attributes
     */
    
    /** the size of signal to be processed */
    private int     szSignal = 0;
    

    /*
     * Processing Objects
     */
    
    /** the differentiator object - instantiated on demand */
    private DigitalDifferentiator   dfoDiffer = null;
    
    /** the integrator object - instantiated on demand */
    private DigitalIntegrator       dfoIntegr = null;
    
    /** the averager object - instantiated on demand */
    private DigitalAverager         dfoAverag = null;
    
    
    /** frequency spectrum analyzer - instantiated on demand */
    private FourierExpTransform     dftExp = null;
    
    /** frequency spectrum analyzer - instantiated on demand */
    private FourierSineTransform     dftSin = null;
    
    
    /** the filter object - instantiated on demand */
    private ExpFilter               efoFilter = null;
    
    

    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>DigitalSignalProcessor</code> object for processing
     * signals of length <var>szSignal</var>.
     * 
     * @param   szSignal    size of signal to be processed
     */
    public DigitalSignalProcessor(int szSignal) {
        this.szSignal = szSignal;
    }
    
    
    /*
     * Attribute Query
     */
    
    /**
     * Return the signal size that this instance can process.
     * 
     * @return  digital signal size expected for processing
     */
    public int  getSignalSize() {
        return this.szSignal;
    }
    
    
    /*
     * Functional Operations
     */
    
    /**
     * Compute and return the differential of the given signal.
     * 
     * @param arrSignal signal to differentiate
     * 
     * @return      differentiated signal
     * 
     * @throws  IllegalArgumentException    this should not occur (serious internal error)
     */
    public double[] differentiate(final double[] arrSignal) throws IllegalArgumentException {
        return this.getDifferentiator().response(arrSignal);
    }
    
    /**
     * Compute and return the integral of the given signal (zero constant of integration).
     * 
     * @param arrSignal signal to integrate
     * 
     * @return      integrated signal
     * 
     * @throws  IllegalArgumentException    this should not occur (serious internal error)
     */
    public double[] integrate(final double[] arrSignal) throws IllegalArgumentException {
        return this.getIntegrator().response(arrSignal);
    }
    
    /**
     * Compute and return the (running) average of the given signal.
     * 
     * @param arrSignal signal to average 
     * 
     * @return      averaged signal
     * 
     * @throws  IllegalArgumentException    this should not occur (serious internal error)
     */
    public double[] average(final double[] arrSignal) throws IllegalArgumentException {
        return this.getAverager().response(arrSignal);
    }
    
    /**
     * Compute and return the total variation of the given
     * signal.  The total variation <i>TV</i>(<i>f</i>) of a
     * signal <i>f</i>(&middot;) is defined as
     * <br>
     * <br>&nbsp;&nbsp;  <i>TV</i>[<i>f</i>](<i>t</i>) 
     *                  &equiv; 
     *                  &int;<sup><i>t</i></sup>|<i>df</i>(<i>&tau;</i>)/<i>dt</i>|<i>d&tau;</i>
     * 
     * @param arrSignal   target signal
     * 
     * @return  total variation of the argument
     */
    public double[]  totalVariation(double[] arrSignal) {

        double[]    arrDif = this.getDifferentiator().response(arrSignal);
        double[]    arrAbs = DigitalFunctionUtility.abs(arrDif);
        double[]    arrTv  = this.getDifferentiator().response(arrAbs);
        
        return arrTv;
    }
    
    
    /*
     * Statistical
     */


    /**
     * <p>
     * Compute and return the signal presence indicator function for the given 
     * signal.  The indicator function is computed by subtracting the
     * (running) average from the signal then computing the total variation.
     * In this manner we hope to observe variation over the noise floor.
     * </p>
     * <p>
     * The returned function is normalized so that its maximum value is unity.
     * It has the form of a cumulative distribution function so that increasing
     * value is indicative of signal likelihood.
     * </p>  
     * 
     * @param arrSignal target signal 
     * 
     * @return          signal presence indicator function
     * 
     * @throws  IllegalArgumentException    this should not occur (serious internal error)
     * 
     * @see DigitalFunctionUtility#totalVariation(double[])
     */
    public double[] signalIndicator(final double[] arrSignal) throws IllegalArgumentException {
        int         N       = arrSignal.length;
        
        double[]    arrAve = this.average(arrSignal);
        double[]    arrDif = DigitalFunctionUtility.subtract(arrSignal, arrAve);
        double[]    arrTv  = this.totalVariation(arrDif);
        
        double      dblMax = arrTv[N-1];
        DigitalFunctionUtility.scaleFunction(1.0/dblMax, arrTv);
        
        return arrTv;
    }
    
    /**
     * <p>
     * Compute and return the auto-correlation function for the given
     * discrete signal.
     * </p>
     * <p>
     * In the spirit of the Z transform we assume that the function is periodic
     * so that <i>f</i>[<i>n</i> + <i>N</i>] = <i>f</i>[<i>n</i>].  In this manner
     * the auto-correlation <i>R<sub>xx</sub></i>[<i>k</i>] function is also periodic,
     * specifically, <i>R<sub>xx</sub></i>[<i>-k</i>] = <i>R<sub>xx</sub></i>[<i>N - k</i>].  
     * </p>
     * <p>
     * Assuming periodicity gives us the follow fact: it's value
     * will never fall to zero if the data contains a noise process with non-zero
     * mean. Indeed, by the property of the auto-correlation 
     * <i>R<sub>xx</sub></i>[<i>-k</i>] = <i>R<sub>xx</sub></i>[<i>k</i>] and, therefore,
     * <i>R<sub>xx</sub></i>[<i>k</i>] = <i>R<sub>xx</sub></i>[<i>N - k</i>] by periodicity.
     * If the underlying signal has support small enough the minimum value
     * of the auto-correlation (at <i>k = N</i>/2) will depend only upon the noise process.  
     * If we assumed
     * the original data non-periodic, this condition would not occur.
     * </p>
     * 
     * @param arrSignal   function to auto-correlate
     * 
     * @return      (one-sided) auto-correction function
     * 
     * @throws  IllegalArgumentException  this should not occur (serious internal error)
     */
    public double[] autoCorrelation(double[] arrSignal) throws IllegalArgumentException {
        return this.crossCorrelation(arrSignal, arrSignal);
    }
    
    /**
     * <p>
     * Compute and return the cross-correlation function for the given
     * signals.  Note that the second argument is the signal that is
     * shifted in the calculation.
     * </p>
     * <p>
     * In the spirit of the Z transform we assume that each function is periodic
     * so that <i>f</i>[<i>n</i> + <i>N</i>] = <i>f</i>[<i>n</i>].  In this manner
     * the cross-correlation <i>R<sub>xy</sub></i>[<i>k</i>] function is also periodic.  
     * More importantly, it's value
     * will never fall to zero if the data contains a noise process with non-zero
     * mean.  If the underlying signal has support small enough the minimum value
     * of the cross-correlation will depend only upon the noise process.  If we assumed
     * the original function non-periodic, this condition would not occur.
     * </p>
     * 
     * @param arrStat  stationary function
     * @param arrShft  shifted function
     * 
     * @return      (one-sided) cross-correction function
     * 
     * @throws IllegalArgumentException the arguments are of different sizes
     */
    public double[] crossCorrelation(double[] arrStat, double[] arrShft)
        throws IllegalArgumentException
    {
        
        if (arrStat.length != arrShft.length)
            throw new IllegalArgumentException(
                            "DigitalSignalProcessor#crossCorrelation(): "
                          + "arguments are different sizes"
                            );
        int         N = arrStat.length;
        
        double[]    arrRxy = new double[N];
        for (int d=0; d<N; d++) {
            double  dblSum = 0.0;
            
            for (int m=0; m<N; m++) {
                int n = (d + m) % N;
                
                dblSum += arrStat[m]*arrShft[n];
            }
            
            arrRxy[d] = dblSum/N;
        }
        
        return arrRxy;
    }
    

    
    /*
     * DFT
     */
    
    
    /**
     * Compute and return the power spectrum of the given signal.  The spectral
     * components are returned in the usual DFT arrangement.  Namely, the first
     * <i>N</i>/2 components are the positive frequency components with zero frequency
     * first in ascending order.  The next
     * <i>N</i>/2 components are the negative components with the highest negative
     * frequency (-<i>N</i>/2) first then in descending order.  This arrangement
     * is due to the topology of the unit circle in the complex plane. 
     * 
     * @param arrSignal signal to analyze
     * 
     * @return      frequency spectrum of given signal
     * 
     * @throws  IllegalArgumentException    this should not occur (serious internal error)
     * 
     * @see FourierExpTransform#powerSpectrum(double[])
     */
    public double[] powerSpectrum(final double[]    arrSignal) throws IllegalArgumentException {
        double[]    arrSpec = this.getTransformer().powerSpectrum(arrSignal);
        
        return arrSpec;
    }
    
    /**
     * 
     */
    
    
    
    /*
     * Internal Support
     */

    /**
     * Check the given discrete function for the proper dimensions.
     * 
     * @param arrSignal   discrete function to check
     * 
     * @throws IllegalArgumentException     function did not have proper dimensions
     */
    private void checkSignal(double[] arrSignal) throws IllegalArgumentException {
        if (arrSignal.length != this.getSignalSize())
            throw new IllegalArgumentException(
                            "DigitalSignalProcessor#checkSignal() - given signal has size = " 
                          + Integer.toString(arrSignal.length)
                          + ", expected size = "
                          + Integer.toString(this.getSignalSize())
                            );
    }
    

    
    /**
     * Return the spectrum analyzer (Fourier transformer) object.  If the 
     * transform has not already been created then we instantiate it and
     * return it.
     * 
     *  @return     the spectrum analyzer object for this instance
     */
    private FourierExpTransform    getTransformer()    {
        if (this.dftExp == null)
            this.dftExp = new FourierExpTransform(this.getSignalSize());

        return this.dftExp;
    }
    
    /**
     * Return the spectrum analyzer (Fourier transformer) object.  If the 
     * transform has not already been created then we instantiate it and
     * return it.
     * 
     *  @return     the spectrum analyzer object for this instance
     */
    private FourierSineTransform    getSinTransformer()    {
        if (this.dftSin == null)
            this.dftSin = new FourierSineTransform(this.getSignalSize());

        return this.dftSin;
    }
    
    /**
     * Return the filter object used by this instance.  If the filter has 
     * not already been created then instantiate and return it.
     * 
     * @return  the filter object for this instance
     */
    private ExpFilter  getFilter() {
        if (this.efoFilter == null)
            this.efoFilter = new ExpFilter(this.getSignalSize());
        
        return this.efoFilter;
    }
    
    /**
     * Return the differentiator object used by this instance.  If the 
     * differentiator has not already been created then instantiate 
     * and return it.
     * 
     * @return  the differentiator object for this instance
     */
    private DigitalDifferentiator   getDifferentiator() {
        if (this.dfoDiffer == null)
            this.dfoDiffer = new DigitalDifferentiator();
        
        this.dfoDiffer.reset();
        return this.dfoDiffer;
    }
    
    /**
     * Return the integrator object used by this instance.  If the integrator 
     * has not already been created then instantiate and return it.
     * 
     * @return  the integrator object for this instance
     */
    private DigitalIntegrator   getIntegrator() {
        if (this.dfoIntegr == null)
            this.dfoIntegr = new DigitalIntegrator();
        
        this.dfoIntegr.reset();
        return this.dfoIntegr;
    }
    
    /**
     * Return the averager object used by this instance.  If the averager
     * has not already been created then instantiate and return it.
     * 
     * @return  the averager object for this instance
     */
    private DigitalAverager getAverager() {
        if (this.dfoAverag == null)
            this.dfoAverag = new DigitalAverager();
     
        this.dfoAverag.reset();
        return this.dfoAverag;
    }

    
    
}
