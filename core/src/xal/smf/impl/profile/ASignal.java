/**
 * ASignal.java
 *
 * Author  : Christopher K. Allen
 * Since   : Mar 13, 2014
 */
package xal.smf.impl.profile;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation for describing the data acquisition channels for a single plane of a wire
 * profile measurement device.  The names of the read back channels for both 
 * the position array and the value array are included here.  Also, the
 * types of these arrays can be specified.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Jan 20, 2013
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) 
public @interface ASignal {

    
    /*
     * Inner Annotations
     */
    
    /**
     * <p>
     * Annotation for describing all the data acquisition channels of a wire
     * profile measurement device.  The names of the read back channels for both 
     * the position array and the value array are included for each measurement "plane",
     * <i>horizontal, vertical, </i> and <i>diagonal</i>.  
     * </p>
     * <p>
     * The contents of the array for each plane are the following parameters:
     * <br>
     * <br>
     * &nbsp; &nbsp; {<tt>hndPosRb, hndValRb, (hndNseAvg), (hndNseStd), (hndSigSz)</tt>}
     * <br>
     * <br>
     * where <tt>hndPosRb</tt> is the XAL channel handle of the sample position array read back,
     * <tt>hndValRb</tt> is the XAL channel handle of the signal value array read back,
     * <tt>hndNseAvg</tt> is the (optional) XAL channel handle of the noise average read back,
     * <tt>hndNseStd</tt> is the (optional) XAL channel handle of the noise variance read back, and
     * <tt>hndCnt</tt> is the (optional) XAL channel handle of the signal array size read back.
     * </p>  
     *
     * @author Christopher K. Allen
     * @since  Feb 5, 2013
     *
     */
    @Documented
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE) 
    public static @interface ASet {
        
        /**
         * Provides the connection parameters for horizontal projection data acquisition.
         * 
         * @return  <code>ASignal</code> annotation containing profile device connection parameters  
         *
         * @author Christopher K. Allen
         * @since  Feb 14, 2013
         */
        public ASignal   sigHor();
        
        /**
         * Provides the connection parameters for vertical projection data acquisition.
         * 
         * @return  <code>ASignal</code> annotation containing profile device connection parameters  
         *
         * @author Christopher K. Allen
         * @since  Feb 14, 2013
         */
        public ASignal   sigVer();
        
        /**
         * Provides the connection parameters for diagonal projection data acquisition.
         * 
         * @return  <code>ASignal</code> annotation containing profile device connection parameters  
         *
         * @author Christopher K. Allen
         * @since  Feb 14, 2013
         */
        public ASignal   sigDia();
        
    }

    
    /*
     * ASignal Properties
     */
    
    /**
     * The type of the sample count channel.
     *
     * @return  class type of the sample count, default value is <code>int.class</code>
     *
     * @author Christopher K. Allen
     * @since  Jan 31, 2013
     */
    public Class<?> typeCnt()   default     int.class;
    
    
    /**
     * The XAL channel handle for the number of active signal samples.
     * This differs from the size of the signal array returned by the
     * channel in <code>ADaqWire.Signal.Hor#hndPosRb</code> which is
     * the buffer size of the DAQ controller.
     *
     * @return  channel handle for the number of signal samples, default ""
     *
     * @author Christopher K. Allen
     * @since  Jan 31, 2013
     */
    public String   hndCntRb()  default     "";

    /**
     * The type of the profile position array.
     *
     * @return  the class type of the position array, default value is <code>double[].class</code>
     *
     * @author Christopher K. Allen
     * @since  Jan 31, 2013
     */
    public Class<?> typePos()   default     double[].class;

    /**
     * The name of the profile position array read back channel.
     *
     * @return  the name of the channel serving up the position array, no default value
     *
     * @author Christopher K. Allen
     * @since  Jan 31, 2013
     */
    public String   hndPosRb();

    /**
     * The class type of profile value array.
     *
     * @return  the type of the value array, default value is <code>double[].class</code>
     *
     * @author Christopher K. Allen
     * @since  Jan 31, 2013
     */
    public Class<?> typeVal()   default   double[].class;


    /**
     * The name of the profile value array acquisition channel.
     *
     * @return  value array channel name, no default value
     *
     * @author Christopher K. Allen
     * @since  Jan 31, 2013
     */
    public String   hndValRb();
    
    /**
     * The data type of the noise mean value.  The default
     * value is <code>double.class</code>.
     * 
     * @return  class type of the noise mean parameter, default value is <code>double.class</code>
     *
     * @author Christopher K. Allen
     * @since  Dec 7, 2011
     */
    public Class<?> typeNseAvg()  default double.class;

    /**
     * The name of the channel containing the signal noise mean.
     *
     * @return  noise variance channel name, default value is <code>""</code> (empty string)
     *
     * @author Christopher K. Allen
     * @since  Dec 7, 2011
     */
    public String   hndNseAvgRb()   default  "";

    /**
     * The data type of the noise variance value.  
     * 
     * @return  class type of the noise variance parameter, the default value is <code>double.class</code>.
     *
     * @author Christopher K. Allen
     * @since  Dec 7, 2011
     */
    public Class<?> typeNseVar()   default double.class;

    /**
     * The name of the channel containing the signal noise variance.
     *
     * @return  noise variance channel name, no default value
     *
     * @author Christopher K. Allen
     * @since  Dec 7, 2011
     */
    public String   hndNseVarRb()  default "";    
}
