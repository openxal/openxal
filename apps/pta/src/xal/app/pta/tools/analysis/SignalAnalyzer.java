/**
 * DataAnalyzer.java
 *
 * @author  Christopher K. Allen
 * @since	Dec 9, 2011
 */
package xal.app.pta.tools.analysis;

import xal.tools.math.ElementaryFunction;
import xal.smf.impl.profile.Signal;
import xal.smf.impl.profile.SignalAttrs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>
 * This class computes several properties of a signal produced by
 * a profile device.  These properties include the mean and variance
 * of the signal (i.e., the beam position and size) along with the 
 * variance caused by the measurement error.
 * </p>
 * <p>
 * A measurement is contains a set of <i>N</i> measurements 
 * {<i>m<sub>k</sub></i>}<sub><i>k</i>=1</sub><sup><i>N</i></sup> and <i>N</i>
 * measurement locations {<i>x<sub>k</sub></i>}<sub><i>k</i>=1</sub><sup><i>N</i></sup>.  A
 * measurement process <i>m<sub>k</sub></i> is composed of the signal itself <i>f<sub>k</sub></i>
 * plus a noise process <i>W<sub>k</sub></i>.  That is,
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <i>m<sub>k</sub></i> = <i>f<sub>k</sub></i> + <i>W<sub>k</sub></i> ,
 * <br/>
 * <br/>
 * where <i>W<sub>k</sub></i> is assumed a stationary, white-noise process characterized by 
 * E[<i>W<sub>k</sub></i>] = &Omega and Var[<i>W<sub>k</sub></i>] = <i>V</i>.
 * </p>
 * <p>
 * A signal object contains the vectors {<i>x<sub>k</sub></i>} and {<i>f</i>(<i>x<sub>k</sub></i>)},
 * along with the quantities &Omega; &equiv; E[<i>W<sub>k</sub></i>] and
 * <i>V</i> &equiv; Var[<i>W<sub>k</sub></i>] where <i>W<sub>k</sub></i> is a white noise
 * process found at every sample point <i>k</i>.
 * </p>
 * <p>
 * We encounter the discrete version of the moment 
 * &lt;(<i>x - x<sub>c</sub></i>)<i><sup>n</sup></i>&gt; often
 * in the signal analysis. To expedite matters we introduce the quantity
 * <i>S<sub>N</sub><sup>n</sup></i>(<i>x<sub>c</sub></i>).  This function is defined
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <i>S<sub>N</sub><sup>n</sup></i>(<i>x<sub>c</sub></i>) &equiv; 
 *           &Sigma;<sub><i>k</i>=1</sub><sup><i>N</i></sup> 
 *           &nbsp; (<i>x<sub>k</sub></i> - <i>x<sub>c</sub></i>)<sup><i>n</i></sup> 
 *           <i>f</i>(<i>x<sub>k</sub></i>)
 * <br/>
 * <br/>
 * where <i>N</i> is the number of sample points, <i>x<sub>k</sub></i> is the <i>k</i><sup>th</sup>
 * sample position, <i>x<sub>c</sub></i> is the center of the moment,
 * and <i>f</i>(<i>x<sub>k</sub></i>) is the <i>k</i><sup>th</sup> (unknown) signal value.
 * </p> 
 * <p>
 * The actual measurement sets have the form 
 * ({<i>x<sub>k</sub></i>},{<i>m<sub>k</sub></i>}, &Omega;, <i>V</i>} where
 * (as before) {<i>x<sub>k</sub></i>} is the set of measurement locations,
 * {<i>m<sub>k</sub></i>} is the set of measurement values, &Omega; is the noise
 * mean, and <i>V</i> is the noise variance.  When doing computations with these
 * sets we replace <i>f</i>(<i>x<sub>k</sub></i>) with the known quantity 
 * (<i>m<sub>k</sub></i> - &Omega;) which contains both the signal value and the
 * measurement noise with baseline removed.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Dec 9, 2011
 */
public class SignalAnalyzer {
    
    
    /*
     * Global Constants
     */
    
    /** Approximation of the square-root of 2 */
    public static final double      DBL_SQRT_2 = 1.41421356237;
    
    /** Approximation of the square-root of 3 */
    public static final double      DBL_SQRT_3 = 1.73205080757; 
    
    /** Approximation of the square-root of 5 */
    public static final double      DBL_SQRT_5 = 2.23606797750;
    
    /** Approximation of the square-root of 2&pi; */
    public static final double      DBL_SQRT_2PI = 2.50662827463;
    
    
    /*
     * Inner Classes
     */
    
    /**
     * Enumeration of all the computed signal properties.  These
     * are included to expedite GUI displays.
     *
     * @author Christopher K. Allen
     * @since   Dec 13, 2011
     */
    public enum PROP { 
    
        /** Mean of the noise process within the profile measurement */
        NOISE_AVG("Noise mean", "getNoiseMean"),
        
        /** Variance of the noise process within the profile measurement */
        NOISE_STD("Noise var", "getNoiseVariance"),
        
        /** The maximum signal value above the noise floor */
        SIG_MAX("Signal maximum", "getSignalMaximum" ),
        
        /** The average signal value above the noise floor */
        SIG_AVG("Signal average", "getSignalAverage" ),
        
        /** The signal-to-noise ratio defined as max(f)/&radic;2<i>V</i> */
        SNR("Signal to noise", "getSignalToNoiseRatio" ),
        
        /** The total beam charge */
        BM_CHG("Beam charge", "getBeamCharge" ),
        
        /** The beam position */
        BM_POS("Beam position", "getBeamPosition" ),
        
        /** The RMS beam size */
        BM_RMS("Beam RMS size", "getBeamRmsSize" ),
        
        /** Variance in the beam charge calculation (from noise) */
        VAR_CHG("Charge variance", "getBeamChargeVariance" ),
        
        /** Variance in the beam position calculation (from noise) */
        VAR_POS("Position variance", "getBeamPositionVariance" ),
        
        /** Variance in the RMS beam size position (from noise) */
        VAR_RMS("RMS size variance", "getBeamRmsSizeVariance");
        
        /**
         * Returns the label of the property in the data structure
         * which corresponds to this enumeration constant.
         *
         * @return  property label
         * 
         * @since  Nov 13, 2009
         * @author Christopher K. Allen
         */
        public String       getPropertyLabel() {
            return this.strFldLbl;
        }

        /**
         * Using reflection, we return the value of the field that this
         * enumeration constant represents, within the given data structure.
         *
         * @param data      data structure having field corresponding to this constant
         * 
         * @return          value of the given data structure's field 
         * 
         * @since  Apr 22, 2010
         * @author Christopher K. Allen
         */
        public double       getPropertyValue(SignalAnalyzer data) {

            try {
                Method      mthFldGtr = this.getFieldGetter();
                double      dblFldVal = (Double) mthFldGtr.invoke(data);

                return dblFldVal;

            } catch (SecurityException e) {
                System.err.println("SERIOUS ERROR: SignalAnalyzer$PROP#getPropertyValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (IllegalArgumentException e) {
                System.err.println("SERIOUS ERROR: SignalAnalyzer$PROP#getPropertyValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (IllegalAccessException e) {
                System.err.println("SERIOUS ERROR: SignalAnalyzer$PROP#getPropertyValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (InvocationTargetException e) {
                System.err.println("SERIOUS ERROR: SignalAnalyzer$PROP#getPropertyValue()"); //$NON-NLS-1$
                e.printStackTrace();

            }

            return 0.0;
        }

        
        
        /** The property label */
        private final String        strFldLbl;
        
        /** name of the field in the data structure */
        private Method              mthFldGtr;

        
        /** 
         * Create the property enumeration constant with given label.
         * 
         * @param strLabel     label for the signal property 
         * @param strFldGtr    the name of the getter method for the field corresponding to this enumeration constant
         */
        private PROP(String strLabel, String strFldGtr) {
            this.strFldLbl = strLabel;
            this.mthFldGtr = null;
            
            try {
                this.mthFldGtr = SignalAnalyzer.class.getMethod( strFldGtr );
                
            } catch (SecurityException e) {
                System.err.println("SERIOUS ERROR: SignalAnalyzer$PROP#PROP() - getter inaccessible: " + strFldGtr); //$NON-NLS-1$
                e.printStackTrace();

            } catch (NoSuchMethodException e) {
                System.err.println("SERIOUS ERROR: SignalAnalyzer$PROP#PROP() no getter method " + strFldGtr); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
        
        /**
         * Returns the field getter method for the signal property that this enumeration
         * constant represents.
         *
         * @return  the signal property getter method for this constant
         * 
         * @since  Dec 13, 2011
         * @author Christopher K. Allen
         */
        private Method  getFieldGetter() {
            return this.mthFldGtr;
        }
    }
    
    
    /**
     * Enumeration of all the alarm conditions on the 
     * computed beam quantities.  This alarm goes true if a beam quantity's variance
     * exceeds a given tolerance.  These constants are included to expedite GUI displays.
     *
     * @author Christopher K. Allen
     * @since   Dec 13, 2011
     */
    public enum ALARM { 
    
        /** The total beam charge */
        BM_CHG(PROP.BM_CHG, "checkBeamChargeTolerance" ),
        
        /** The beam position */
        BM_POS(PROP.BM_POS, "checkBeamPositionTolerance" ),
        
        /** The RMS beam size */
        BM_RMS(PROP.BM_RMS, "checkBeamRmsSizeTolerance" );
        
        
        /**
         * Returns the <code>SignalAnalyzer$PROP</code> enumeration constant
         * corresponding to the beam property that this constant check for
         * tolerance compliance.
         *
         * @return  beam property that this constant considers
         *
         * @author Christopher K. Allen
         * @since  Dec 15, 2011
         */
        public PROP getBeamPropery() {
            return this.enmBmPrp;
        }
        
        /**
         * Using reflection, we check the tolerance of the beam quantity
         * corresponding to this enumeration constant.  T(he checking
         * method of the class is invoked using reflecting.)
         *
         * @param data      data structure with computed beam quantities
         * @param dblErrTol the error tolerance we allow in the computed beam quantities 
         * 
         * @return          <code>true</code> if the beam quantity corresponding to this 
         *                  constant is within tolerance, <code>false</code> otherwise 
         * 
         * @since  Apr 22, 2010
         * @author Christopher K. Allen
         */
        public boolean  checkTolerance(SignalAnalyzer data, double dblErrTol) {

            try {
                Method      mthPrpChk = this.getPropTolChecker();
                boolean     bolPrpTol = (Boolean) mthPrpChk.invoke(data, dblErrTol);

                return bolPrpTol;

            } catch (SecurityException e) {
                System.err.println("SERIOUS ERROR: SignalAnalyzer$PROP#getFieldValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (IllegalArgumentException e) {
                System.err.println("SERIOUS ERROR: SignalAnalyzer$PROP#getFieldValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (IllegalAccessException e) {
                System.err.println("SERIOUS ERROR: SignalAnalyzer$PROP#getFieldValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (InvocationTargetException e) {
                System.err.println("SERIOUS ERROR: SignalAnalyzer$PROP#getFieldValue()"); //$NON-NLS-1$
                e.printStackTrace();

            }

            return false;
        }

        
        
        /** The property label */
        private final PROP         enmBmPrp;
        
        /** name of the field in the data structure */
        private Method              mthPrpChk;

        
        /** 
         * Create the property enumeration constant with given label.
         * 
         * @param enmBmPrp     the beam <code>PROP</code> constant corresponding to the tolerance we are checking
         * @param strPrpChk    the name of the tolerance checking method for this beam property
         */
        private ALARM(PROP   enmBmPrp, String strPrpChk) {
            this.enmBmPrp  = enmBmPrp;
            this.mthPrpChk = null;
            
            try {
                this.mthPrpChk = SignalAnalyzer.class.getMethod( strPrpChk, double.class );
                
            } catch (SecurityException e) {
                System.err.println("SERIOUS ERROR: SignalAnalyzer$PROP#PROP() - method inaccessible: " + strPrpChk); //$NON-NLS-1$
                e.printStackTrace();

            } catch (NoSuchMethodException e) {
                System.err.println("SERIOUS ERROR: SignalAnalyzer$PROP#PROP() no method " + strPrpChk); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
        
        /**
         * Returns the property tolerance checker method for the beam quantity that this enumeration
         * constant represents.
         *
         * @return  the beam property tolerance checking method for this constant
         * 
         * @since  Dec 13, 2011
         * @author Christopher K. Allen
         */
        private Method  getPropTolChecker() {
            return this.mthPrpChk;
        }
    }
    
    
    /*
     * Global Methods
     */
    
    
    /**
     * <p>
     * Computes the discrete moment component 
     * <i>S<sub>N</sub><sup>n</sup></i>(<i>x<sub>c</sub></i>) used repeatedly in the
     * signal analysis.  This function is defined
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>S<sub>N</sub><sup>n</sup></i>(<i>x<sub>c</sub></i>) &equiv; 
     *           &Sigma;<sub><i>k</i>=1</sub><sup><i>N</i></sup> 
     *           &nbsp; (<i>x<sub>k</sub></i> - <i>x<sub>c</sub></i>)<sup><i>n</i></sup> 
     *           (<i>m</i><sub>k</sub></i> - &Omega;) <i>h<sub>k</sub></i>
     * <br/>
     * <br/>
     * where <i>N</i> is the number of sample points, <i>x<sub>n</sub></i> is the <i>n</i><sup>th</sup>
     * sample position, &Omega; is the noise floor, <i>m<sub>k</sub></i> is the <i>k</i><sup>th</sup> 
     * measurement value (signal value), and <i>h<sub>k</sub></i> &equiv; 
     * <i>x<sub>k</sub></i> - <i>x<sub>k-1</i></sub> is the step size between measurement
     * points <i>k</i>-1 and <i>k</i>.  
     * </p> 
     * <p>
     * This function is a primary component of the moment &lt;(<i>x - x<sub>c</sub></i>)<sup><i>n</i></sup>&gt; 
     * where <i>x<sub>c</sub></i> is the <em>center</em> of the moment and <i>n</i> is the <em>order</em>
     * of the moment.
     * </p>
     *
     * @param intOrder  order of the moment <i>n</i>
     * @param dblCtr    center of the moment <i>x<sub>c</sub></i>
     * @param sigMsmt    measurement signal ({<i>x<sub>k</sub></i>},{<i>m<sub>k</sub></i>}, &Omega;, <i>V</i>}
     * 
     * @return  the value of <i>S<sub>N</sub><sup>n</sup></i>(<i>x<sub>c</sub></i>)
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2011
     */
    public static double    momentSummation(int intOrder, double dblCtr, Signal sigMsmt) {

        // Perform weighted running summation
        int     N = sigMsmt.cnt;
        double  dblSum = 0.0;
        
        for (int n=1; n<N; n++) {  // skip the first measurement to avoid boundary

            // Get the step size, the measurement position, the measurement value, and compute the weight
            double  dblPos = sigMsmt.pos[n];
            double dblVal  = sigMsmt.val[n];

            double  dblStp = sigMsmt.pos[n] - sigMsmt.pos[n-1];
            
            double  dblMsmt = dblVal - sigMsmt.navg; 
            double  dblWgt  = ElementaryFunction.pow(dblPos - dblCtr, intOrder);
            
            dblSum += dblMsmt * dblWgt * dblStp;
        }
        
        return dblSum;
    }
    
    /**
     * Computes the value of the variance function <i>Z<sub>N</sub><sup>n</sup></i>(<i>x<sub>c</sub></i>)
     * which is ubiquitous in noise computations involving the summation 
     * <i>S<sub>N</sub><sup>n</sup></i>(<i>x<sub>c</sub></i>).  The function is defined
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>Z<sub>N</sub><sup>n</sup></i>(<i>x<sub>c</sub></i>) &equiv; 
     *               [
     *               &Sigma;<sub><i>k</i>=1</sub><sup><i>N</i></sup> 
     *               &nbsp; (<i>x<sub>k</sub></i> - <i>x<sub>c</sub></i>)<sup>2<i>n</i></sup> 
     *               ]<sup>1/2</sup>
     * <br/>
     * <br/>
     * where <i>N</i> is the number of sample points, <i>x<sub>k</sub></i> is the <i>k</i><sup>th</sup>
     * sample position, and <i>x<sub>c</sub></i> is the center of the corresponding moment quantity.
     *
     * @param cntSmp    the number of samples of <var>arrPos</var> to use
     * @param intOrder  the exponential order <i>n</i>
     * @param dblCtr    the central position <i>x<sub>c</sub></i>
     * @param arrPos    the vector of sample positions {<i>x<sub>n</sub></i>}
     * 
     * @return  value of the function <i>Z<sub>N</sub><sup>n</sup></i>(<i>x<sub>c</sub></i>)
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2011
     */
    public static double varianceSummation(int cntSmp, int intOrder, double dblCtr, double[] arrPos) {
        
        // Perform running summation
        double  dblSum = 0.0;
        int     intExp = 2 * intOrder;
        
        for (int k=0; k<cntSmp; k++) {
            double dblPos = arrPos[k];
            
            dblSum += ElementaryFunction.pow(dblPos - dblCtr, intExp);
        }
        
        // Take square root and return
        double  dblRoot = Math.sqrt(dblSum);
        
        return dblRoot;
    }
    
    
    
    /*
     * Local Attributes
     */
    
    /** The signal object */
    private Signal          sigTarget;
    
    
    /** maximum signal value */
    private double          dblSigMax;
    
    /** average signal value */
    private double          dblSigAvg;
    
    /** signal to noise ratio */
    private double          dblSnr;
    
    
    /** beam charge */
    private double          dblBmChg;
    
    /** beam position */
    private double          dblBmPos;
    
    /** beam size */
    private double          dblBmRms;

    
    /** beam charge variance */
    private double          dblBmChgVar;
    
    /** variance in beam position */
    private double          dblBmPosVar;
    
    /** Variance in beam size */
    private double          dblBmRmsVar;
    

    
    /*
     * Initialization
     */
    
    /**
     * Creates a new, uninitialized <code>SignalAnalyzer</code> object.
     * Use <code>{@link #setSignal(xal.smf.impl.Signal)}</code>
     * to compute all the signal and beam properties.
     *
     * @author  Christopher K. Allen
     * @since   Dec 12, 2011
     */
    public SignalAnalyzer() {
        this.clearAll();
    }
    
    /**
     * Creates a new, initialized <code>SignalAnalyzer</code> object.  All the
     * signal and beam properties are computed during construction and are 
     * immediately available.
     * 
     * @param sigTarget     the measurement signal to be analyzed
     *
     * @author  Christopher K. Allen
     * @since   Dec 12, 2011
     */
    public SignalAnalyzer(Signal sigTarget) {
        this.setSignal(sigTarget);
    }
    
    /**
     * Sets the target <code>Signal</code> object and computes
     * all the signal characteristics.
     *
     * @param sigTarget
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2011
     */
    public void setSignal(Signal sigTarget) {
        this.sigTarget = sigTarget;
        
        if (sigTarget == null) {
            this.clearAll();
            return;
        }

        this.compSignalMax();
        this.compSignalAvg();
        this.compSignalToNoise();
        
        this.compBeamProperties();
        this.compPropVariances();
    }
    
    
    
    /*
     * Operations - Properties
     */
    
    /**
     * Returns the RMS properties of the current signal in a 
     * <code>SignalAttrs</code> data structure.  That is, the signal properties
     * are computed directly from the raw data using RMS techniques, no preprocessing
     * is performed.
     *  
     * @return  RMS signal attributes
     *
     * @author Christopher K. Allen
     * @since  Apr 23, 2014
     */
    public SignalAttrs  getRmsSignalAttrs() {
        SignalAttrs attSignal = new SignalAttrs();
        
        attSignal.offset = this.getNoiseMean();
        attSignal.amp    = this.getSignalMaximum();
        attSignal.area   = this.getBeamCharge();
        attSignal.mean   = this.getBeamPosition();
        attSignal.stdev  = this.getBeamRmsSize();
        
        return attSignal;
    }
    
    /**
     * Returns the value &Omega; of the noise mean.  This value is provided by the wire 
     * scanner controller through the attached <code>Signal</code> object.
     *
     * @return  the expectation &Omega; &equiv; E[<i>W</i>] where <i>W</i> is the noise 
     *          process within the profile measurement {<i>m</i><sub><i>k</i></sub>}
     *
     * @author Christopher K. Allen
     * @since  Feb 17, 2012
     */
    public double getNoiseMean() {
        return this.sigTarget.navg;
    }
    
    /**
     * Returns the value <i>V</i> of the noise variance.  This value is provided by the wire 
     * scanner controller through the attached <code>Signal</code> object.
     *
     * @return  the standard deviation <i>V</i> &equiv; E[(<i>W</i> - &Omega;)<sup>2</sup>]<sup>1/2</sup>
     *          where <i>W</i> is the noise process within the profile {<i>m<sub>k</sub></i>}
     *
     * @author Christopher K. Allen
     * @since  Feb 17, 2012
     */
    public double getNoiseVariance() {
        return this.sigTarget.nvar;
    }
    
    /**
     * Returns the maximum value max {<i>f<sub>k</sub></i>} of the measurement 
     * signal <i>f</i>. Specifically,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; max <i>f</i> = max<sub>k</sub> (<i>m<sub>k</sub></i> - &Omega;)
     * <br/>
     * <br/>
     * where {<i>m<sub>k</sub></i>} is the set of measurements, &Omega; is the noise floor, and <i>N</i>
     * is the number of sample values.
     *
     * @return  maximum value of the measurement signal <i>f</i>
     *
     * @author Christopher K. Allen
     * @since  Dec 12, 2011
     */
    public double getSignalMaximum() {
        return this.dblSigMax;
    }

    /**
     * Returns the computed average value Ave[<i>f</i>] of the measurement 
     * signal {<i>f<sub>k</sub></i>}. Specifically,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; Ave[<i>f</i>] = (1/<i>N</i>) &sum;<sub><i>k</i>=1</sub><sup><i>N</i></sup> 
     *                               (<i>m<sub>k</sub></i> - &Omega;)
     * <br/>
     * <br/>
     * where {<i>m<sub>k</sub></i>} is the set of measurements, &Omega; is the noise floor, and <i>N</i>
     * is the number of sample values.
     *
     * @return  average value of the measurement signal <i>f</i>
     *
     * @author Christopher K. Allen
     * @since  Dec 12, 2011
     */
    public double getSignalAverage() {
        return this.dblSigAvg;
    }

    /**
     * Returns the signal-to-noise ratio (SNR) of the signal {<i>f<sub>k</sub></i>} 
     * within the of the measurement set {<i>m<sub>k</sub></i>}.  We define the SNR
     * as
     * <br/>
     * <br/>
     * &nbsp; &nbsp; SNR = max<i>f</i> / (&radic;2 <i>V</i>)
     * <br/>
     * <br/>
     * where <i>V</i> is the noise variance.
     *
     * @return  SNR of the measurement signal <i>f</i>
     *
     * @author Christopher K. Allen
     * @since  Dec 12, 2011
     */
    public double getSignalToNoiseRatio() {
        return this.dblSnr;
    }

    /**
     * Returns the beam charge <i>Q</i> as computed from the signal data.
     * The analytic expression is <i>Q</i> = &int;<i>f</i>(<i>x</i>)<i>dx</i> while
     * what is returned here is the discrete approximation
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>Q</i> &approx; &sum; <sub><i>k</i>=1</sub><sup><i>N</i></sup> 
     *                                 (<i>m<sub>k</sub></i> - &Omega;) <i>h<sub>k</sub></i> 
     *                        = <i>S<sub>N</sub></i><sup>0</sup>(0),
     * <br/>
     * <br/>
     * where <i>h<sub>k</sub></i> &equiv; (<i>x<sub>k</sub></i> - <i>x</i><sub><i>k</i>-1</sub>).
     *                                
     * @return  the discrete approximation to the total beam charge in the signal
     *
     * @author Christopher K. Allen
     * @since  Dec 12, 2011
     */
    public double getBeamCharge() {
        return this.dblBmChg;
    }

    /**
     * Returns the beam position &mu; as computed from the signal data.
     * The analytic expression is &mu; = (1/<i>Q</i>) &int;<i>x</i><i>f</i>(<i>x</i>)<i>dx</i> while
     * what is returned here is the discrete approximation
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &mu; &approx; (1/<i>Q</i>) &sum; 
     *                                 <i>x<sub>k</i> (<i>m<sub>k</sub></i> - &Omega;) <i>h<sub>k</sub></i> ,
     *                        = (1/<i>Q</i>) <i>S<sub>N</sub></i><sup>1</sup>(0),
     * <br/>
     * <br/>
     * where <i>h<sub>k</sub></i> &equiv; (<i>x<sub>k</sub></i> - <i>x</i><sub><i>k</i>-1</sub>).
     *                                
     * @return  the discrete approximation to the beam position
     *
     * @author Christopher K. Allen
     * @since  Dec 12, 2011
     */
    public double getBeamPosition() {
        return this.dblBmPos;
    }

    /**
     * Returns the RMS beam size &sigma; as computed from the signal data.
     * The analytic expression is &sigma; = 
     * [(1/<i>Q</i>) &int;(<i>x</i>-&mu;)<sup>2</sup><i>f</i>(<i>x</i>)<i>dx</i>]<sup>1/2</sup> 
     * while what is returned here is the discrete approximation
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &sigma; &approx; [(1/<i>Q</i>) &sum; 
     *                                 (<i>x<sub>k</i>-&mu;)<sup>2</sup> (<i>m<sub>k</sub></i> - &Omega;) <i>h<sub>k</sub></i>]<sup>1/2</sup> ,
     *<br/>                                 
     * &nbsp; &nbsp; &nbsp; &nbsp; = [(1/<i>Q</i>) <i>S<sub>N</sub></i><sup>2</sup>(&mu;) ]<sup>1/2</sup>,
     * <br/>
     * <br/>
     * where <i>h<sub>k</sub></i> &equiv; (<i>x<sub>k</sub></i> - <i>x</i><sub><i>k</i>-1</sub>).
     *                                
     * @return  the discrete approximation to the RMS beam size
     *
     * @author Christopher K. Allen
     * @since  Dec 12, 2011
     */
    public double getBeamRmsSize() {
        return this.dblBmRms;
    }

    /**
     * <p>
     * Returns the variance Var[<i>Q</i>] of the computed beam charge.  This variance arises from
     * the noise in the measurement process {<i>m<sub>k</sub></i>} of the signal data.  The value
     * returned is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; Var[<i>Q</i>] = <i>N</i><sup>1/2</sup><i>V</i> , 
     * <br/>
     * <br/>
     * where <i>N</i> is the number of data samples and <i>V</i> is the variance of the
     * noise process within the measurements.  (i.e., <i>V</i> = Var[<i>W<sub>k</sub></i>].)
     * </p>
     * <p>
     * <h3>NOTE:</h3>
     * For further information see
     * C.K. Allen, W. Blokland, S.M. Cousineau, and J.D. Galambos, 
     * "Extracting Information from Noisy, Sampled, Profile Data of Charged Particle Beams", 
     * <i>in preparation</i>.
     * </p>
     *                                
     * @return  the error bar for the beam charge calculation from measurement data
     *
     * @author Christopher K. Allen
     * @since  Dec 12, 2011
     * 
     */
    public double getBeamChargeVariance() {
        return this.dblBmChgVar;
    }

    /**
     * <p>
     * Returns the variance Var[&mu;] of the computed beam position.  This variance arises from
     * the noise in the measurement process {<i>m<sub>k</sub></i>} of the signal data.  The value
     * returned is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; Var[&mu;] &approx; (2/&radic;3<i>Q</i>) <i>N</i><sup>3/2</sup><i>V</i> , 
     * <br/>
     * <br/>
     * where <i>N</i> is the number of data samples and <i>V</i> is the variance of the
     * noise process within the measurements.  (i.e., <i>V</i> = Var[<i>W<sub>k</sub></i>].)
     * </p>
     * <p>
     * <h3>NOTE:</h3>
     * For further information see
     * C.K. Allen, W. Blokland, S.M. Cousineau, and J.D. Galambos, 
     * "Extracting Information from Noisy, Sampled, Profile Data of Charged Particle Beams", 
     * <i>in preparation</i>.
     * </p>
     *                                
     * @return  the error bar for the beam position calculation from measurement data
     *
     * @author Christopher K. Allen
     * @since  Dec 12, 2011
     */
    public double getBeamPositionVariance() {
        return this.dblBmPosVar;
    }

    /**
     * <p>
     * Returns the variance Var[&sigma;] of the computed RMS beam size.  This variance arises from
     * the noise in the measurement process {<i>m<sub>k</sub></i>} of the signal data.  The value
     * returned is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; Var[&sigma;] &approx; (1/&radic;5<i>Q</i>) <i>N</i><sup>5/2</sup><i>V</i> , 
     * <br/>
     * <br/>
     * where <i>N</i> is the number of data samples and <i>V</i> is the variance of the
     * noise process within the measurements.  (i.e., <i>V</i> = Var[<i>W<sub>k</sub></i>].)
     * </p>
     * <p>
     * <h3>NOTE:</h3>
     * For further information see
     * C.K. Allen, W. Blokland, S.M. Cousineau, and J.D. Galambos, 
     * "Extracting Information from Noisy, Sampled, Profile Data of Charged Particle Beams", 
     * <i>in preparation</i>.
     * </p>
     *                                
     * @return  the error bar for the beam RMS size calculation from measurement data
     *
     * @author Christopher K. Allen
     * @since  Dec 12, 2011
     */
    public double getBeamRmsSizeVariance() {
        return this.dblBmRmsVar;
    }
    
    /**
     * Returns the value of a Gaussian distribution with the computed beam size
     * and position (see <code>{@link #getBeamRmsSize()}</code> and 
     * <code>{@link #getBeamPosition()}</code>).
     *
     * @param dblPos    argument of the Gaussian function, i.e., the axial position
     * 
     * @return          value of the Gaussian function evaluated at the argument
     *
     * @author Christopher K. Allen
     * @since  Apr 18, 2012
     */
    public double   evalGaussian(double dblPos) {
        double  t1 = (dblPos - this.dblBmPos)/this.dblBmRms;
        double  t2 = - t1*t1/2.0;
        double  t3 = this.dblBmChg/(DBL_SQRT_2PI*this.dblBmRms);
        
        return t3 * Math.exp(t2);
    }
    
    /**
     * Creates a <code>Signal</code> object having a Gaussian profile
     * with position and standard deviation as calculated from the signal under
     * analysis, that is, the "equivalent Gaussian" as the base signal.
     * 
     * @return  the Gaussian having the same RMS statistics as the given signal,
     *          or an empty <code>Signal</code> object if the target signal could not be cloned
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2012
     */
    public Signal   equivGaussian() {

        try {
            Signal sigGauss = (Signal) this.sigTarget.clone();

            for (int i=0; i<this.sigTarget.cnt; i++) {
                double      dblPos = this.sigTarget.pos[i];
                
                sigGauss.val[i] = this.evalGaussian(dblPos);
            }
            
            return sigGauss;
            
        } catch (CloneNotSupportedException e) {
            String  strErrMsg = "Unable to clone this Signal object";
            
//            MainApplication.getEventLogger().logException(getClass(), e, strErrMsg);
            System.err.println(strErrMsg);
            
            return Signal.createBlankSignal();
        }
        
    }
    
    /**
     * Returns the same signal as <code>{@link #equivGaussian()}</code> only with the noise
     * baseline added to the Gaussian signal.  This is a convenience function used primarily for
     * graphing against the actual signal.
     * 
     * @return  the Gaussian having the same RMS statistics as the given signal,
     *          or an empty <code>Signal</code> object if the target signal could not be cloned
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2012
     */
    public Signal   equivGaussianWithBaseline() {
        Signal sigBase = this.equivGaussian();
        
        for (int i=0; i<this.sigTarget.cnt; i++) {
            sigBase.val[i] += this.sigTarget.navg;
        }
        
        return sigBase;
    }
    
    
    /**
     * <p>
     * Check whether or not the beam charge is within the given error
     * tolerance.  The variance is used to determine the tolerance comparison.
     * </p>
     * <p>
     * <h3>NOTE:</h3>
     * Due to the nature of the variance calculation, this is most important tolerance 
     * as far as beam quantity tolerances are concerned.  If it is violated, then all 
     * other computed beam quantities are out of tolerance.
     * </p>
     * 
     * @param dblErrTol the error tolerance = ratio of variance to value
     * 
     * @return          <code>true</code> if the beam charge error is within tolerance,
     *                  <code>false</code> otherwise
     *
     *
     * @author Christopher K. Allen
     * @since  Dec 15, 2011
     */
    public boolean checkBeamChargeTolerance(double dblErrTol) {
        double  dblErr = this.dblBmChgVar / this.dblBmChg;
        
        if (dblErr > dblErrTol)
            return false;
        
        return true;
    }

    /**
     * <p>
     * Check whether or not the beam charge is within the given error
     * tolerance.  The variance is used to determine the tolerance comparison.
     * </p>
     * 
     * @param dblErrTol the error tolerance = ratio of variance to value
     * 
     * @return          <code>true</code> if the beam position error is within tolerance,
     *                  <code>false</code> otherwise
     *
     *
     * @author Christopher K. Allen
     * @since  Dec 15, 2011
     */
    public boolean checkBeamPositionTolerance(double dblErrTol) {
        double  dblDomMin = this.sigTarget.pos[0];
        double  dblDomMax = this.sigTarget.pos[ this.sigTarget.cnt - 1 ];
        double  dblDomain = dblDomMax - dblDomMin;
        
        double  dblErr = this.dblBmPosVar / dblDomain;
        
        if (dblErr > dblErrTol)
            return false;
        
        return true;
    }
    
    /**
     * <p>
     * Check whether or not the beam RMS size is within the given error
     * tolerance.  The variance is used to determine the tolerance comparison.
     * </p>
     *
     * @param dblErrTol the error tolerance = ratio of variance to value
     * 
     * @return          <code>true</code> if all the RMS size is within tolerance,
     *                  <code>false</code> otherwise
     *
     *
     * @author Christopher K. Allen
     * @since  Dec 15, 2011
     */
    public boolean checkBeamRmsSizeTolerance(double dblErrTol) {
        double  dblErr = this.dblBmRmsVar / this.dblBmRms;
        
        if (dblErr > dblErrTol)
            return false;
        
        return true;
    }

    /**
     * <p>
     * Check whether all of the computed beam quantities are within the given error
     * tolerance.  The variances of the beam quantities are used to determine the
     * quantity errors.
     * </p>
     * <p>
     * <h3>NOTE:</h3>
     * Due to the nature of the variance calculation, the most important quantity is
     * the tolerance of the beam charge.  If it is violated, then all quantities
     * are out of tolerance.
     * </p>
     * 
     * @param dblErrTol the error tolerance = ratio of variance to value
     * 
     * @return          <code>true</code> if all beam quantities are within tolerance,
     *                  <code>false</code> if at least one quantity violates tolerance
     *
     * @author Christopher K. Allen
     * @since  Dec 15, 2011
     */
    public boolean  checkBeamQuantityTolerances(double dblErrTol) {
        if ( !this.checkBeamChargeTolerance(dblErrTol) )
            return false;
        
        if ( !this.checkBeamPositionTolerance(dblErrTol) )
            return false;
        
        if ( !this.checkBeamRmsSizeTolerance(dblErrTol) )
            return false;
        
        return true;
        
    }


    /*
     * Support Methods
     */
    
    /**
     * Clears (zeros) all the signal characteristics.
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2011
     */
    private void clearAll() {
        
        this.sigTarget = null;
        
        this.dblSigMax = 0.0;
        this.dblSigAvg = 0.0;
        this.dblSnr    = 0.0;
        
        this.dblBmChg = 0.0;
        this.dblBmPos = 0.0;
        this.dblBmRms = 0.0;

        this.dblBmChgVar = 0.0;
        this.dblBmPosVar = 0.0;
        this.dblBmRmsVar = 0.0;
    }
    
    /**
     * Determines the maximum signal value.
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2011
     */
    private void compSignalMax() {
        int     cntSigSmp = this.sigTarget.cnt;
        double  dblNseAvg = this.sigTarget.navg;
        double  dblSigMax = this.sigTarget.val[0]; 
        
        for (int k=1; k<cntSigSmp; k++) {
            double  dblVal = this.sigTarget.val[k];
            
            if (dblVal > dblSigMax)
                dblSigMax = dblVal;
        }

        this.dblSigMax = dblSigMax - dblNseAvg; 
    }
    
    /**
     * Computes the average value of the signal.
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2011
     */
    private void compSignalAvg() {
        double  dblCnt = this.sigTarget.cnt;
        double  dblSum = 0.0;
        
        for (double val : this.sigTarget.val)
            dblSum += val;
        
        this.dblSigAvg = dblSum/dblCnt - this.sigTarget.navg;
    }
    
    /**
     * Computes the signal to noise ratio
     *
     * @author Christopher K. Allen
     * @since  Dec 12, 2011
     */
    private void compSignalToNoise() {
        this.dblSnr = this.dblSigMax/(DBL_SQRT_2 * this.sigTarget.nvar);
    }
    
    /**
     * Compute the beam charge, position, and size from the measurement
     * signal. 
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2011
     */
    private void compBeamProperties() {
        double  dblMmt0 = momentSummation(0, 0.0, this.sigTarget);
        double  dblMmt1 = momentSummation(1, 0.0, this.sigTarget);
        double  dblMmt2 = momentSummation(2, dblMmt1/dblMmt0, this.sigTarget);
        
        this.dblBmChg = dblMmt0;
        this.dblBmPos = dblMmt1/dblMmt0;
        this.dblBmRms = Math.sqrt(dblMmt2/dblMmt0);
    }
    
    /**
     * Computes the variances (i.e., "error bars") of the beam
     * properties.
     *
     * @author Christopher K. Allen
     * @since  Dec 12, 2011
     */
    private void compPropVariances() {
        double  dblCntMsmt = this.sigTarget.cnt;
        double  dblNseVar  = this.sigTarget.nvar;
        double  dblSqrtCnt = Math.sqrt(dblCntMsmt);
        double  dbl3_2rCnt = dblSqrtCnt * dblCntMsmt;
        double  dbl5_2rCnt = dbl3_2rCnt * dblCntMsmt;
        
        this.dblBmChgVar = dblSqrtCnt * dblNseVar;
        this.dblBmPosVar = dbl3_2rCnt * dblNseVar * 2.0/(DBL_SQRT_3 * this.dblBmChg);
        this.dblBmRmsVar = dbl5_2rCnt * dblNseVar * 1.0/(DBL_SQRT_5 * this.dblBmChg);
    }
    
}
