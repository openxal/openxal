/**
 * SineTransform.java
 * 
 * Author       : Christopher K. Allen
 * Created      : August, 2007
 */
package xal.tools.dsp;

import JSci.maths.matrices.DoubleSquareMatrix;
import JSci.maths.vectors.AbstractDoubleVector;
import JSci.maths.vectors.DoubleVector;


/**
 * <p>
 * This class encapsulates many aspects of the Fourier sine transform.  Although this is a
 * discrete transform, it is not a Fast Fourier Transform (FFT).  Specifically, we do not
 * fully exploit the dyadic nature of the transform kernel.  In fact, we explicitly compute
 * this kernel which, in the discrete version, is a matrix.  Thus, this transform can
 * potentially be quite expensive for large data sets.  What we have here then, is essentially
 * a convenient wrapper around a real, symmetric matrix which makes this class look like a
 * magical transformer.
 * </p>
 * <p>
 * The advantages of the sine transform are that it only involves real numbers (i.e., primitives
 * of type <code>double</code>).  The transform is somewhat generalized in that we assume a base 
 * frequency of <i>&pi;</i> rather than 2<i>&pi;</i> so that we can transform even 
 * functions as well as odd ones.  (The FFT has "positive" and "negative" frequencies.)
 * The only caveat is that the function <i>f</i> being transformed 
 * is assumed to have value zero at the boundaries (as does the sine kernel).  There are two 
 * approaches here, either we pad the given function by zero (adding two data points), or we 
 * construct the transform so that the recovered function (via inverse transform) always has a 
 * zero values at the boundaries.  Here we choose the later, preferring not to change the dimensions 
 * of the data. 
 * </p>
 * <p>
 * The transform performed here is given by
 * <br>
 * <br>&nbsp;&nbsp;  [<b>f^</b>] = [<b>K</b>]&middot;[<b>f</b>]</p>
 * <br>
 * where [<b>f^</b>] is the vector of transformed data, [<b>K</b>] is the real symmetric matrix
 * kernel, and [<b>f</b>] is data vector of input function values.  The elements 
 * <i>K<sub>mn</sub></i> of the matrix kernel are given by
 * <br>
 * <br>&nbsp;&nbsp;    <i>K<sub>mn</sub></i> = (2/(<i>N</i>-1))<sup>&frac12;</sup> sin <i>&pi;mn</i>/(<i>N</i>-1)<br>
 * <br>
 * where <i>N</i> is the size of the data vector [<b>f</b>], and indices <i>m, n</i> range over the 
 * values 0,&hellip;,<i>N</i>.  The factor (2/(<i>N</i>-1))<sup>&frac12;</sup> is a normalization
 * constant; specifically, the value of the <i>L</i><sub>2</sub> norm 
 * ||sin <i>&pi;n</i>/(<i>N</i>-1)||.  It's presence ensures the dual nature 
 * of the transform, specifically [<b>K</b>].[<b>K</b>] = [<b>I</b>] where [<b>I</b>] is 
 * the identity matrix (sans the initial and final 1 entry).
 * </p>
 * <p>
 * From the value of <i>K<sub>mn</sub></i> we see that the stride in [<b>f^</b>] 
 * is 1/2<i>T</i>, where
 * <i>T</i> is the period of the interval over which <i>f</i> is defined.  Thus, the largest
 * frequency we can see is (<i>N</i>-1)/2<i>T</i>.  Also, we see that there is no specific 
 * DC component.  
 * That is, the DC (or zero-frequency) component of the signal has a projection upon all the
 * odd-frequency components in this general Fourier sine transform.  Thus, it cannot be identified
 * separately as in the traditional Fourier transform.
 * </p>
 *   
 * @author Christopher K. Allen
 *
 */
public class FourierSineTransform {

    
    /*
     * Local Attributes
     */
    
    /** the expected data size */
    private int                 szData = 0;
    
    /** the transform kernel */
    private DoubleSquareMatrix  matKer = null;
    
    
    /*
     * Initialization
     */
    
    
    /**
     * Create a new sine transform object for transforming data vectors of 
     * size <var>szData</var>.  During construction the 
     * <var>szData</var>&times;<var>szData</var> transform matrix kernel is
     * built. Upon completion the returned transform object is able to
     * transform any <code>double[]</code> object of the appropriate size. 
     * 
     * @param szData
     * 
     * @see FourierSineTransform#transform(double[])
     */
    public FourierSineTransform(int szData) {
        this.initTransform(szData);
    }
    
    
    /*
     * Attribute Query
     */
    
    /**
     * Return the expected size of the data, which is also the dimensions of the 
     * kernel.  
     * 
     * @return  size of the transform vectors  (that is, the value <i>N</i>)
     */
    public int  getDataSize()   {
        return this.szData;
    }
    
    
    
    /*
     * Operations
     */

    
    
    /**
     * Compute and return the value of the frequency stride for this transform
     * given the total time period over which the data is taken.
     *  
     * @param dblPeriod     total length of the data window
     * 
     * @return              frequency interval (stride) between transformed data points
     */
    public double  compFreqStrideFromPeriod(double dblPeriod)   {
        return 1.0/(2.0*dblPeriod);
    }
    
    /**
     * Compute and return the value of the frequency stride for this transform
     * given the time stride (time interval between data points).
     * 
     * @param dblDelta  time interval between data points
     * 
     * @return          frequency interval (stride) between transformed data points
     */
    public double   compFreqStrideFromInterval(double dblDelta)   {
        int         N = this.getDataSize() - 1;
        double      T = dblDelta*N;
        
        return this.compFreqStrideFromPeriod(T);
    }
    
    /**
     * <p>
     * Compute and return the Fourier sine transform of the given function.  Note that
     * this transform is essential dual to itself, thus, the transform and the inverse 
     * transform are the same operation.  This fact follows from the normalization used
     * so that the transform matrix is essentially a root of unity.
     * </p>
     * <p>
     * The returned values are ordered so that the lowest frequency components come
     * first.  That is, the components are indexed according to their discrete frequency.
     * Note also that the zero-frequency component of a sine transform is identically
     * zero, as is the <i>N<sup>th</sup></i> component.  Thus, the first and last values will
     * always be zero.
     * </p>
     * 
     * @param arrFunc   vector array of function values (zero values on either end)
     * 
     * @return          vector array of transformed value
     * 
     * @throws IllegalArgumentException     invalid function dimension
     */
    public double[] transform(final double[] arrFunc) throws IllegalArgumentException {
        int     szArr = arrFunc.length;
        
        
        // Check the dimensions
        if (szArr != this.getDataSize())
            throw new IllegalArgumentException("FourierSineTransform#transform() - array size #= " + this.getDataSize());
        
        
        // Perform the transform
        AbstractDoubleVector    vecTrans = this.matKer.multiply(new DoubleVector(arrFunc));
        double[]                arrTrans = new double[szArr];
        
        
        // Unpack the results and return them
        for (int index=0; index<szArr; index++) {
            arrTrans[index] = vecTrans.getComponent(index);
        }
        
        return arrTrans;
    }
    
    /**
     * <p>
     * Compute and return the discrete power spectrum for the given function.  The power
     * spectrum is the square of the frequency spectrum and, therefore, is always
     * positive.
     * </p>
     * <p>
     * The returned values are ordered so that the lowest frequency components come
     * first.  That is, the components are indexed according to their discrete frequency.
     * Note also that the zero-frequency component of a sine transform is identically
     * zero, as is the <i>N<sup>th</sup></i> component.  Thus, the first and last values will
     * always be zero.
     * </p>
     * 
     * @param arrFunc   discrete function
     * 
     * @return          discrete power spectrum of given function
     * 
     * @throws IllegalArgumentException     invalid function dimension
     */
    public double[] powerSpectrum(final double[] arrFunc) throws IllegalArgumentException {
        double[]        arrSpec = this.transform(arrFunc);
        
        for (int index=0; index<this.getDataSize(); index++)    {
            double dblVal = arrSpec[index];
            arrSpec[index] = dblVal*dblVal;
        }

        return arrSpec;
    }

    
    
    /*
     * Support Methods
     */
    
    /**
     * Computes and stores the sine transform kernel.
     * 
     * @param   szData  dimensions of the tranform kernel.
     */
    private void initTransform(int szData) {
        int           m,n;      // matrix indices
        double          k;      // current matrix value
        
        final int           N = szData - 1;         // vector/matrix dimensions 
        final double        c = Math.sqrt(2.0/N);   // normalization constant
        
        // Create matrix kernel and compute element values
        DoubleSquareMatrix  K = new DoubleSquareMatrix( N + 1 );
        for (m=0; m<=N; m++)
            for (n=m; n<=N; n++)    {
                k = c*Math.sin(Math.PI*m*n/N);
                
                K.setElement(m, n, k);
                K.setElement(n, m, k);
            }
        
        this.szData = N + 1;
        this.matKer = K;
    }


    
}
