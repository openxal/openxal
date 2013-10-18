/**
 * ProfileDataProcessor.java
 * 
 * Created      : August, 2007
 * Author       : Christopher K. Allen
 */
package xal.extension.wirescan.profile;

import java.util.ArrayList;

import JSci.maths.matrices.AbstractDoubleSquareMatrix;
import JSci.maths.matrices.DoubleSquareMatrix;
import JSci.maths.vectors.AbstractDoubleVector;
import JSci.maths.vectors.DoubleVector;

import xal.tools.dsp.DataProcessingException;
import xal.tools.dsp.DigitalFunctionUtility;
import xal.tools.dsp.DigitalSignalProcessor;
import xal.tools.dsp.LtiDigitalFilter;


/**
 * <p>
 * Class for processing profile data contained in <code>ProfileData</code> objects.
 * </p>
 * <p>
 * The objective of this class is to interpolate any missing data (represented by a 
 * value <code>Double.NaN</cod>), remove the noise baseline, decouple the cross-talk
 * between the signals, then gate the noise in the signal.  Thus, it is expected that 
 * the data is processed by the following
 * sequence of method calls:
 * <br>
 * <br>&nbsp;&nbsp; {@link ProfileDataProcessor#interpolateMissingData()}
 * <br>&nbsp;&nbsp; {@link ProfileDataProcessor#removeBaselineNoise(double)}
 * <br>&nbsp;&nbsp; {@link ProfileDataProcessor#decoupleSignals(int, double)}
 * <br>&nbsp;&nbsp; {@link ProfileDataProcessor#thresholdFilter(double)}
 * <br>
 * <br>
 * Once this sequence is completed the processed signals will be approximately zero base-lined,
 * decoupled, and normalized to have unity first integral.  
 * </p>
 * <p>
 * One may apply the above sequence of calls using default values for all the arguments
 * with the single call to <code>ProfileDataProcessor.{@link #processData()}
 * </p>
 * <p>
 * There are three numeric tuning parameters that must be provided in the processing.
 * <br>
 * <br>ProfileDataProcessor.{@link #removeBaselineNoise(double dblSigThreshold)}
 * <br><var>dblSigThreshold</var> = amplitude of indicator function deciding signal presence
 * <br>This value is in the interval [0,1].  Larger will use more signal samples to determine
 * noise properties, but may also include actual signals.  Smaller values are more likely to
 * exclude signals in the noise calculations, but will use few samples to characterize the
 * noise.
 * <br>
 * <br>ProfileDataProcessor.{@link #decoupleSignals(int cntSmpMax, double dblFracSigMin)}
 * <br><var>cntSmpMax</var> = number of samples to average when computing coefficients
 * <br>This value must be an integer greater than 1.  This value determined the number 
 * of samples around the maximum value used when 
 * computing coupling coefficients.  Small numbers will use samples with the most integrity.
 * Larger numbers will include samples more affected by noise, but may provide a better
 * statistical approximation if the data set contains suspect data.
 * <br><var>dblFracSigMin</var> fractional signal strength necessary for inclusion in coupling calculation
 * <br>This value ranges in the interval [0,1].  Large values ensure that only the best samples 
 * are used in computing the coupling coefficients, however, it reduces the available sample
 * size.  Small values increase the sample size but may include samples corrupted by noise.
 * <br>
 * <br>ProfileDataProcessor.{@link #thresholdFilter(double dblFracSigMax)}
 * <br><var>dblFracSigMax</var>	= fraction of maximum signal where threshold is applied 
 * <br>The argument is the fraction of the maximum signal strength where the
 * threshold is activated.  Thus, this value is in the interval (-1,1) where
 * a value of 1 would cause a filter output of zero for all inputs.
 * Although sign is not enforced per se, we are expecting a positive signal.
 * Thus, a value for <var>dblFracSigMax</var> in the interval (0,1) is appropriate.
 * Then if the projection data were strictly non-negative, a value 0 would indicate
 * no action by the filter.
 * </p>
 * <br>
 * Default values are suggested as constants with this class.
 * </p>
 * 
 * 
 * @author Christopher K. Allen
 */
public class ProfileDataProcessor {

    
    /*
     * Global Constants
     */
    
    /** Amplitude of signal indicator used when determining noise floor */
    public final static double    DBL_SIG_INDICATOR_THRESHOLD = 0.03;
    
    /** Maximum number of samples to use when computing coupling coefficients */
    public final static int       INT_CPL_MAX_SAMPLE_COUNT = 3;
    
    /** Minimum fractional signal amplitude necessary to include sample for coupling coefficient calc. */
    public final static double    DBL_CPL_MIN_FRAC_SIGNAL_AMPL = 0.5;
    
    /** Maximum fractional signal amplitude for threshold filtering */
    public final static double	  DBL_THR_MAX_FRAC_SIGNAL_AMPL = 0.03; 
    
    
    
    //
    // Low Pass Filter coefficients
    //
    
    /** Order of the filter */
    public final static int         INT_DSP_LOWPASS_ORDER = 2;
    
    /** Coefficients of the filter input */
    public final static double[]    ARR_DBL_LOWPASS_INP_COEFFS = {
                                     1.0,
                                    -2.0,
                                     1.0
                                    };
    
    /** Coefficients of the filter output */
    public final static double[]    ARR_DBL_LOWPASS_OUT_COEFFS = {
                                     1.0,
                                    -1.9988,
                                     0.9988
                                    };
    
    
    //
    // High Pass Filter coefficients
    //
    
    /** Order of the filter */
    public final static int         INT_DSP_HIGHPASS_ORDER = 2;
    
    /** Coefficients of the filter input */
    public final static double[]    ARR_DBL_HIGHPASS_INP_COEFFS = {
                                     0.5690356,
                                     1.380712,
                                     0.5690356
                                    };
    
    /** Coefficients of the filter output */
    public final static double[]    ARR_DBL_HIGHPASS_OUT_COEFFS = {
                                     1.0,
                                     0.94280905,
                                     0.33333333,
                                    };
    
    
    /*
     * Local Attributes
     */
    
    /** the original data object */
    private ProfileData     dataRaw;
    
    /** processed data object */
    private ProfileData     dataPrc;
    
    
    /** the amplifier gain matrix */
    private DoubleSquareMatrix  matGain;
    
    /** the cross-talk coupling matrix */
    private DoubleSquareMatrix  matXTlk;
    

    
    /*
     * Processing Objects
     */
    
    /** the DSP object */
    private DigitalSignalProcessor  dsp;
    
    /** the low-pass filter object */
    private LtiDigitalFilter        fltLow;
    
    /** the high-pass filter object */
    private LtiDigitalFilter        fltHigh;

    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>ProfileDataProcessor</code> object attached to the given
     * profile data object.
     * 
     * @param   dataOrg    profile data under process
     * 
     * @see	ProfileDataProcessor
     */
    public ProfileDataProcessor(final ProfileData dataOrg) {
        this.dataRaw = dataOrg;
        this.dataPrc = ProfileData.copy(dataOrg);
        this.dsp     = new DigitalSignalProcessor(dataOrg.getDataSize());
        this.fltLow  = this.initLowPassFilter();
        this.fltHigh = this.initHighPassFilter();
    }
    
    
    
    /*
     * Attribute Query and Properties
     */
    
    /**
     * Convenience method for returning the (vector) size of the associated 
     * <code>ProfileData</code> data object.
     * 
     * @return      size of the associated projection data vectors
     * 
     * @see gov.sns.tools.data.profile.ProfileData#getDataSize()
     */
    public int getDataSize() {
        return this.getProcessedData().getDataSize();
    }


    /**
     * Return the raw profile data object.
     * 
     * @return  raw profile data object
     */
    public ProfileData  getRawData()    {
        return this.dataRaw;
    }
    
    /**
     * Return the processed profile data object.
     * 
     * @return  processed profile data object
     */
    public ProfileData  getProcessedData()   {
        return this.dataPrc;
    }
    
    /**
     * Return the processed data for the given profile view.
     * 
     * @param   view    desired profile view
     * 
     * @return  processed profile data
     */
    public double[] getProcessedData(ProfileData.Angle view) {
        return this.getProcessedData().getProjection(view);
    }
    
    
    
    /**
     * Compute and return the signal presence indicator function for the given 
     * projection view.  The indicator function is computed by subtracting the
     * (running) average from the signal then computing the total variation. 
     * 
     * @param view  projection data 
     * 
     * @return      signal presence indicator function
     * 
     * @throws  IllegalArgumentException    this should not occur (serious internal error)
     * 
     * @see DigitalFunctionUtility#totalVariation(double[])
     */
    public double[] indicatorFunction(ProfileData.Angle view) throws IllegalArgumentException {
        return this.dsp.signalIndicator(this.getProcessedData(view));
    }
    
    
    
    /*
     * Data Operations
     */
    
    
    /**
     * <p>
     * Perform the default processing on the associated projection
     * data.  The following operations are performed:
     * <br>
     * <br>&nbsp;&nbsp; {@link ProfileDataProcessor#interpolateMissingData()}
     * <br>&nbsp;&nbsp; {@link ProfileDataProcessor#removeBaselineNoise(double)}
     * <br>&nbsp;&nbsp; {@link ProfileDataProcessor#decoupleSignals(int, double)}
     * <br>&nbsp;&nbsp; {@link ProfileDataProcessor#thresholdFilter(double)}
     * <br>
     * <br>
     * Default values for the arguments are taken from the global constants of this
     * class.
     * </p>
     * 
     * @throws  DataProcessingException     data set exceeds maximum bad data points
     */
    public void processData() throws DataProcessingException	{
    	this.interpolateMissingData();
//    	this.highPassFilter();
//    	this.lowPassFilter();
    	this.removeBaselineNoise(DBL_SIG_INDICATOR_THRESHOLD);
    	this.decoupleSignals(INT_CPL_MAX_SAMPLE_COUNT, DBL_CPL_MIN_FRAC_SIGNAL_AMPL);
    	this.thresholdFilter(DBL_THR_MAX_FRAC_SIGNAL_AMPL);
    }

    /**
     * Interpolate between missing data points.  Missing data is indicated with 
     * a value <code>Double.NaN</code>.  If we encounter more than two
     * contiguous bad data points the operation punts by throwing an
     * exception.
     * 
     * @throws  DataProcessingException     data set exceeds maximum bad data points
     */
    public void interpolateMissingData() throws DataProcessingException {

        // If the data set is complete there is nothing to do
        if (!this.getProcessedData().hasMissingData()) 
            return;
        
        // Process each profile function
        for (ProfileData.Angle view : ProfileData.Angle.values())
            this.linearInterpolate(this.getProcessedData().getProjection(view));
    }
    
    public void lowPassFilter()  {
        
        for (ProfileData.Angle view : ProfileData.Angle.values()) {
            double[]    arrPrj = this.getProcessedData(view);
            double[]    arrPrc = this.getLowPass().response(arrPrj);

            this.getProcessedData().setProjection(view, arrPrc);
        }
    }
    
    public void highPassFilter()  {
        
        for (ProfileData.Angle view : ProfileData.Angle.values()) {
            double[]    arrPrj = this.getProcessedData(view);
            double[]    arrPrc = this.getHighPass().response(arrPrj);

            this.getProcessedData().setProjection(view, arrPrc);
        }
    }
    
    /**
     * <p>
     * Remove the baseline noise signal from the data set.  We assume that
     * each profile signal <i>f<sup>^</sup></i>(<i>n</i>) is given by the 
     * following:
     * <br>
     * <br>&nbsp;&nbsp;  <i>f<sup>^</sup></i>(<i>n</i>) 
     *                          = 
     *                      <i>f</i>(<i>n</i>) + <i>W</i>(<i>n</i>)<br>
     * <br>
     * where <i>f</i>(<i>n</i>) is the true signal at index <i>n</i> and
     * <i>W</i>(<i>n</i>) is a noise process with a (possibly) nonzero mean
     * given by E[<i>W</i>(<i>n</i>)] = <i>w</i> (independent of <i>n</i>).
     * The value of <i>w</i> represents the baseline noise signal.  
     * </p>
     * <p>
     * To remove 
     * this content we compute the signal indicator function for each signal
     * and find the largest index such that all the indicator functions are 
     * less than the given value.  All data before this index is assumed to
     * be noise.  We use that data to compute <i>w</i> for each signal assuming 
     * that the original signal is filtered such that E[(<i>W - w</i>)&sup2;] is small. 
     * The value <i>w</i> is subtracted from each respective signal. 
     * </p>
     * <p>
     * Note, however, 
     * that only the mean component of the noise is removed.  If the noise 
     * process has a non-zero standard deviation &sigma;<sub><i>W</i></sub> = 
     * E[(<i>W</i>(<i>n</i>) - <i>w</i>)<sup>2</sup>]<sup>&frac12;</sup> then
     * spurious noise is still present.  The spurious component can be attenuated
     * by further filtering, but at the cost of degrading the true signal
     * <i>f</i>(<i>n</i>).  We originally assumed that this value was small enough
     * to get an accurate determination from the data before signal presence.
     * </p> 
     * 
     * @param   dblSigThreshold     amplitude of indicator function deciding signal presence
     *  
     * @throws DataProcessingException  currently this should not happen
     */
    public void removeBaselineNoise(double dblSigThreshold) {

        // Compute the indicator functions
        ArrayList<double[]> lstSigInd = new ArrayList<double[]>();
        
        for (ProfileData.Angle view : ProfileData.Angle.values())
            lstSigInd.add(this.indicatorFunction(view));
        
        // Find location of first signal 
        int iSignal = this.getDataSize();
        for (double[] arrIndic : lstSigInd)  {
            int index = DigitalFunctionUtility.argGreaterThan(dblSigThreshold, arrIndic);
            if (index < iSignal)
                iSignal = index;
        }
        iSignal--;
        
        // Remove noise floor
        for (ProfileData.Angle view : ProfileData.Angle.values()) {
            double[]    arrPrj = this.getProcessedData(view);
            double[]    arrAve = this.getDsp().average(arrPrj);
            double      dblFlr = arrAve[iSignal];
            
            DigitalFunctionUtility.subtractFrom(arrPrj, dblFlr);
        }
    }
    
    /**
     * <p>
     * Decouple the project data signals.  That is, remove the cross-talk
     * signals from each projection data set.
     * </p>
     * <p>
     * It is assumed that the profile signals may be represented
     * by the equation
     * <br>
     * <br>&nbsp;&nbsp;  <b>f*</b>(<i>n</i>) = <b>GCf</b>(<i>n</i>)<br>
     * <br>
     * where <b>f</b>*(<i>n</i>) is the vector-valued function of measured
     * profile data (one element for each projection) at time <i>n</i>, 
     * <b>G</b> is the gain matrix, <b>C</b> is the cross-talk matrix, and
     * <b>f</b>(<b>n</b>) is the vector-valued function of profile data.
     * The gain matrix is chosen so that the integral of element in
     * <b>f</b>(<b>n</b>) is unity.  That is, &int;<b>f</b>(<b>n</b>)<i>dn</i>
     * = (1,1,&hellip;,1).  
     * </p>
     * <strong>NOTE:</strong>
     * <p>
     * It is best to use this method after the baseline noise have been 
     * removed from the profile data.
     * </p>
     * @param cntSmpMax     maximum number of samples to average when computing coefficients
     * @param dblFracSigMin minimum fractional signal strength for use in calculation 
     */
    public void decoupleSignals(int cntSmpMax, double dblFracSigMin)   {
        
        // Compute the cross-talk coefficient, the gain matrix, then factor the two
        this.computeXTalk(cntSmpMax, dblFracSigMin);
        this.computeGains();
        this.factorGainAndXTalk();
        
        // Decouple the profile signals by inverting gain and coupling matrices
        AbstractDoubleSquareMatrix  matGC  = this.matGain.multiply(this.matXTlk);
        AbstractDoubleSquareMatrix  matGCi = matGC.inverse();

        int             N = this.getDataSize();
        int             szVec = ProfileData.Angle.getCount();
        DoubleVector    vecMeas = new DoubleVector(szVec);
        
        for (int n=0; n<N; n++) {   // for each signal value
            
            // pack measurement vector
            for (ProfileData.Angle view : ProfileData.Angle.values()) {
                double  dblMeas = this.getProcessedData(view)[n];
                vecMeas.setComponent(view.getIndex(), dblMeas);
            }
            
            // decouple
            AbstractDoubleVector vecDecpl = matGCi.multiply(vecMeas);
            
            // unpack decoupled signal
            for (ProfileData.Angle view : ProfileData.Angle.values()) {
                double  dblDecpl = vecDecpl.getComponent(view.getIndex());
                this.getProcessedData(view)[n] = dblDecpl;
            }
        }
    }
    
    /**
     * <p>
     * Amplitude threshold filter for use as a noise gate.
     * </p>
     * <p>
     * Apply a threshold filter to each projection data signal using the
     * given fractional amplitude.  Specifically, the projection data
     * signal strength must be strictly greater than <var>dblFracSigMax</var>
     * of the maximum signal strength to achieve a nonzero value.  If the value
     * <var>dblFracSigMax</var> is chosen properly the filter will perform as
     * a noise gate where we assume a positive signal in the projection data.
     * </p>
     * <p>
     * NOTE:
     * <br>- This is a zero-order filter (no memory), but it is highly nonlinear.
     * Thus, you need to be careful when using this method, especially for 
     * computations with the resulting data.
     * <br>- The argument is the fraction of the maximum signal strength where the
     * threshold is activated.  Thus, this value is in the interval (-1,1) where
     * a value of 1 would cause a filter output of zero for all inputs.
     * <br>- Although sign is not enforced per se, we are expecting a positive signal.
     * Thus, a value for <var>dblFracSigMax</var> in the interval (0,1) is appropriate.
     * Then if the projection data were strictly non-negative, a value 0 would indicate
     * no action by the filter.
     * </p>
     * 
     * @param dblFracSigMax		fraction of maximum signal where threshold is applied 
     */
    public void thresholdFilter(double dblFracSigMax) {
    	
    	// Process each projection separately
    	for (ProfileData.Angle view : ProfileData.Angle.values()) {
    		double[]	arrPrj = this.getProcessedData(view);
    		double   dblMaxSig = DigitalFunctionUtility.maximumValue(arrPrj);
    		double	 dblThres  = dblFracSigMax*dblMaxSig;
    		
    		// Signal strength must be greater than dblThres to have nonzero value
    		for (int index=0; index<arrPrj.length; index++)	
    			if (arrPrj[index] < dblThres)
    				arrPrj[index] = 0.0;
    	}
    }
    
    /*
     * Debugging
     */
    
    /**
     * Write out contents both original data and processed data to 
     * a string for inspection.  The returned string is a formatted
     * table of values as produced by <code>DigitalFunctionUtility#buildValueTable()</code>.
     * 
     * @return      string of tabulated projection data
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        // Create a string buffer 
        String          strBuf   = "";

        
        // Create the column header and argument array
        int         index    = 0;
        int         cntArgs  = 2*ProfileData.Angle.getCount() + 1; 
        double[][]  arrArgs  = new double[cntArgs][];
        
        strBuf = "Index\t Position\t";
        arrArgs[index++] = this.getProcessedData().getActuatorPositions();
        for (ProfileData.Angle view : ProfileData.Angle.values()) {
            strBuf += view + "-raw\t" + view + "-prc\t";
            arrArgs[index++] = this.getRawData().getProjection(view);
            arrArgs[index++] = this.getProcessedData().getProjection(view);
        }
        
        // Write out to the string formatter
        strBuf += "\n";
        strBuf += DigitalFunctionUtility.buildValueTable(arrArgs);
        
        return strBuf;
    }
    
    
    
    
    /*
     * Internal Support 
     */
    
    
    
    /**
     * Initializes the low-pass filter and returns it.
     * 
     * @return  the initialized low-pass filter object. 
     */
    private LtiDigitalFilter    initLowPassFilter() {
        LtiDigitalFilter    fltLow = new LtiDigitalFilter( this.getDataSize() );
        
        fltLow.setInputCoefficients(ProfileDataProcessor.ARR_DBL_LOWPASS_INP_COEFFS);
        fltLow.setOutputCoefficients(ProfileDataProcessor.ARR_DBL_LOWPASS_OUT_COEFFS);
        
        return fltLow;
    }
    
    /**
     * Initializes the high-pass filter and returns it.
     * 
     * @return  the initialized high-pass filter object. 
     */
    private LtiDigitalFilter    initHighPassFilter() {
        LtiDigitalFilter    fltHigh = new LtiDigitalFilter( this.getDataSize() );
        
        fltHigh.setInputCoefficients(ProfileDataProcessor.ARR_DBL_HIGHPASS_INP_COEFFS);
        fltHigh.setOutputCoefficients(ProfileDataProcessor.ARR_DBL_HIGHPASS_OUT_COEFFS);
        
        return fltHigh;
    }
    
    /**
     * Return the digital signal processor object.
     * 
     * @return  the dsp used by this instance
     */
    private DigitalSignalProcessor  getDsp()    {
        return this.dsp;
    }
    
    /**
     * Return the low-pass digital filter object.
     * 
     * @return  the low-pass filter used by this instance.
     */
    private LtiDigitalFilter    getLowPass()    {
        return this.fltLow;
    }
    
    /**
     * Return the high-pass digital filter object.
     * 
     * @return  the high-pass filter used by this instance.
     */
    private LtiDigitalFilter    getHighPass()    {
        return this.fltHigh;
    }
    
    /**
     * Fills in bad data points with a linear interpolation using its
     * neighbors.  If two contiguous bad points are found (two <code>NaN</code>
     * values) then an exception is thrown.
     * 
     * @param arrFunc       discrete function containing bad data points
     * 
     * @throws DataProcessingException  encountered two contiguous bad data points
     */
    private void    linearInterpolate(double[] arrFunc) throws DataProcessingException  {
        
        int     cntBadData = 0;     // number of bad data points found
        
        for (int index=0; index<arrFunc.length; index++)    {
            
            // If its good data ignore it
            if (arrFunc[index] != Double.NaN)   
                continue;
        
            // Found a bad data points
            cntBadData++;
            
            
            // If there are two contiguous points give up
            if (arrFunc[index+1] == Double.NaN)
                throw new DataProcessingException(
                                "ProfileDataProcessor#interpolator() -"
                              + "encountered contiguous bad data points"
                              );

            
            // If located on boundary just set to zero
            if ( (index == 0) || (index == arrFunc.length+1) ) {
                arrFunc[index] = 0.0;
                continue;
            }

            // Internal isolated bad data point - simple interpolation
            arrFunc[index] = (arrFunc[index-1] + arrFunc[index+1])/2.0;
            
        }
    }
    
    /**
     * Compute the cross-talk coefficients between the projection signals.
     * 
     * @param cntSmpAve     number of samples to average when computing coefficients
     * @param dblFracMaxSig signal strength must be this fraction of max signal to be used in average 
     */
    private void computeXTalk(int cntSmpAve, double dblFracMaxSig) {
        
        // Instantiate the cross talk matrix
        int                 szArray = ProfileData.Angle.getCount();
        DoubleSquareMatrix  matXTlk = new DoubleSquareMatrix(szArray);
        
        // Search for the maximum signal amplitude - compute coefficients there
        for (ProfileData.Angle view1 : ProfileData.Angle.values()) {
            double[]    arrPrj1 = this.getProcessedData(view1);
            int         iCplInd = DigitalFunctionUtility.argMaximum(arrPrj1);
            double      dblMax1 = arrPrj1[iCplInd];
            
            
            // compute cross-talk coefficient for each coupled signal
            iCplInd -= cntSmpAve/2;       // index of signal to start averaging
            if (iCplInd < 0)
                iCplInd = 0;
            
            for (ProfileData.Angle view2 : ProfileData.Angle.values())    {
                double[]    arrPrj2 = this.getProcessedData(view2);

                // average over cntSmpAve coupled samples
                int     cntSamples = 0;
                double  dblCpl = 0.0;
                for (int index=iCplInd; index<iCplInd+cntSmpAve; index++) {
                    double  dblVal1 = arrPrj1[index];
                    double  dblVal2 = arrPrj2[index];
                    
                    if (dblVal1 > dblFracMaxSig*dblMax1)  {
                        dblCpl += dblVal2/dblVal1;
                        cntSamples++;
                    }
                }
                dblCpl = dblCpl/cntSamples;
                matXTlk.setElement(view1.getIndex(), view2.getIndex(), dblCpl);
            }
        }
        
        this.matXTlk = matXTlk;
    }
    
    /**
     * <p>
     * Compute the amplifier gains.  The amplifier gains essentially provide
     * normalization.  Specifically, we compute the gains so that each true
     * projection signal will have an integral of unity.
     * </p>
     * <strong>IMPORTANT:</strong>
     * <p>
     * This method must be called after 
     * <code>ProfileDataProcessor{@link #computeXTalk(int)}</code>
     * but before the gain and cross-talk coefficients are separated with the call to
     * <code>ProfileDataProcessor{@link #factorGainAndXTalk()}</code>.
     * </p>
     */
    private void computeGains() {
        
        // Instantiate the gain matrix
        int                 szArray = ProfileData.Angle.getCount();
        DoubleSquareMatrix  matGain = new DoubleSquareMatrix(szArray);

        // Compute the integral of the signal and remove the contributions from cross-talk
        for (ProfileData.Angle view1 : ProfileData.Angle.values()) {
            double[]    arrPrj = this.getProcessedData(view1);
            
            double dblInt = DigitalFunctionUtility.integral(arrPrj);
            for (ProfileData.Angle view2 : ProfileData.Angle.values())
                if (!view2.equals(view1))
                    dblInt -= this.matXTlk.getElement(view1.getIndex(), view2.getIndex());
            
            matGain.setElement(view1.getIndex(), view1.getIndex(), dblInt);
        }
        
        this.matGain = matGain;
    }
    
    /**
     * <p>
     * Separate the gain from the cross-talk coupling.
     * </p>
     * </p>
     * <strong>IMPORTANT:</strong>
     * <p>
     * This method must be called after 
     * <code>ProfileDataProcessor{@link #computeXTalk(int)}</code>
     * and
     * <code>ProfileDataProcessor{@link #computeGains()}.
     * </p>
     * 
     */
    private void factorGainAndXTalk() {

        // Instantiate the normalized cross-talk matrix
        int                 szArray = ProfileData.Angle.getCount();
        DoubleSquareMatrix  matCpl = new DoubleSquareMatrix(szArray);
        
        // For each non-diagonal cross-talk coefficient, divide by gain
        for (ProfileData.Angle view1 : ProfileData.Angle.values())    {

            double  dblGain = matGain.getElement(view1.getIndex(), view1.getIndex());
            for (ProfileData.Angle view2 : ProfileData.Angle.values()) {
                
                if (view2.equals(view1)) { // this is a diagonal element - unity value
                    matCpl.setElement(view1.getIndex(), view2.getIndex(), 1.0);

                } else {
                    double  dblCoef = this.matXTlk.getElement(view1.getIndex(), view2.getIndex());

                    matCpl.setElement(view1.getIndex(), view2.getIndex(), dblCoef/dblGain);
                }
            }
        }
        
        this.matXTlk = matCpl;
    }
    
    
}



/*
 * Storage
 */

