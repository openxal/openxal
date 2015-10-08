/**
 * ProfileDevice.java
 *
 * Author  : Christopher K. Allen
 * Since   : Mar 21, 2014
 */
package xal.smf.impl.profile;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import xal.ca.BadChannelException;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.IEventSinkValue;
import xal.ca.Monitor;
import xal.ca.MonitorException;
import xal.ca.PutException;
import xal.tools.data.DataListener;
import xal.smf.AcceleratorNode;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireScanner.DataLivePt;
import xal.smf.impl.profile.ASignal.ASet;
import xal.smf.scada.BadStructException;
import xal.smf.scada.BatchConnectionTest;
import xal.smf.scada.XalPvDescriptor;
import xal.smf.scada.ScadaAnnotationException;
import xal.smf.scada.ScadaFieldDescriptor;
import xal.smf.scada.ScadaFieldList;

/**
 * <p>
 * This class presents the common behavior of profile data acquisition devices.  It also
 * provides some tools for managing the device and its connections.  For example, testing
 * batch acquisition of data and configuration channels, along with some common enumerations.
 * </p>
 * <p>
 * Child classes can define their own specific behavior using the component-based classes
 * <code>ParameterSet</code> and <code>SignalSet</code>.  They allow the profile device to
 * define configuration parameters and measurement data acquisition channels.
 * </p>
 * <p>
 * Derived classes must define the <code>AcceleratorNode{@link #getType()}</code> method,
 * since this class has no device type of its own.  It simply represents the common behavior
 * of profile data acquisition devices.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 *
 * @author Christopher K. Allen
 * @since  Mar 21, 2014
 */
public abstract class ProfileDevice extends AcceleratorNode {

    /*
     * Internal Classes
     */
    
    /**
     * Interface for the generic description of the domain of profile data.  The
     * domain of definition is an interval on the real line.  This interval is
     * described by its position (the left-most endpoint) and its length.
     *
     * @author Christopher K. Allen
     * @since  Apr 24, 2014
     */
    public interface IProfileDomain {

        /**
         * Returns the number of signal samples in the profile data, that is,
         * the number of data points in the domain.
         * 
         * @param   angle   the projection angle of the data set
         * 
         * @return              number of data samples of the given angle
         *
         * @author Christopher K. Allen
         * @since  Apr 24, 2014
         */
        public int      getSampleCount(ANGLE angle);
        
        /**
         * Returns the locations within the domain interval where the profile data
         * samples are taken.
         * 
         * @param angle         the projection angle of the data set
         * 
         * @return              sample axis positions of the projection data
         *
         * @author Christopher K. Allen
         * @since  Apr 24, 2014
         */
        public double[] getSamplePositions(ANGLE angle);
        
        /**
         * Return the left-most position of the projection interval, that is, 
         * the minimum valued endpoint.
         *  
         * @param   angle   the projection angle of the data set
         * 
         * @return              left endpoint of the projection interval
         *
         * @author Christopher K. Allen
         * @since  Apr 24, 2014
         */
        public double   getInitialPosition(ANGLE angle);
        
        /**
         * Return the length of the real interval containing the projection data.
         * That is, return the length of the smallest interval containing the
         * projection data. Note that this value may be different than the
         * "scan length" as it may depend upon the measurement plane.
         * The value, <i>L</i>, is typically given by the formula
         * <br/>
         * <br/>
         * &nbsp; &nbsp; <i>L</i> = &alpha; <i>N</i><sub>steps</sub> &Delta;<i>L</i>
         * <br/>
         * <br/>
         * where &alpha; is the correction factor, <i>N</i><sub>steps</sub> is the number
         * of scan steps, and &Delta;<i>L</i> is the step length.  For example, if the
         * scan actuator arm is physically at a 45 &deg; angle to the given measurement
         * plane then &alpha; = 1/&radic;2. 
         *  
         * @param angle     the projection angle of the data set
         * 
         * @return          length of the interval of definition for the given data set
         *
         * @author Christopher K. Allen
         * @since  Apr 24, 2014
         */
        public double   getIntervalLength(ANGLE angle);
    }
    
    /**
     * Interface exposed by data structures containing formatted data acquired from profile 
     * diagnostic devices.  The interface presents that data which is common to all
     * profile devices.
     *
     * @author Christopher K. Allen
     * @since  Apr 22, 2014
     */
    public interface IProfileData extends DataListener {

        /**
         * Returns the identifier string of the device that produced the current
         * data.
         *  
         * @return      string identifier of the data acquisition device
         * 
         * @author Christopher K. Allen
         * @since  Apr 22, 2014
         */
        public String           getDeviceId();
        
        /**
         * Returns the device type identifier string of the device that produced
         * the current data.
         * 
         * @return      string identifier of the acquisition device (i.e., "ws", "harp", etc.)
         *
         * @author Christopher K. Allen
         * @since  Sep 22, 2014
         */
        public String           getDeviceTypeId();
        
        /**
         * Returns the length of each signal in the data set, that is, the number of
         * samples in the signals.
         * 
         * @return  number of samples in each profile signal
         *
         * @author Christopher K. Allen
         * @since  Apr 22, 2014
         */
        public int              getDataSize();
        
        /**
         * Returns the raw signal data from the profile device data set.
         * 
         * @return  profile signal data acquire directly without any processing
         *
         * @author Christopher K. Allen
         * @since  Apr 22, 2014
         */
        public SignalSet        getRawData();
        
        /**
         * Returns a fitted signal set taken from the current profile data
         * set.  The type of fit and its corresponding processing should be
         * defined by the exposing method or clear from the context.
         * 
         * @return  fitted signal data from the profile data set
         *
         * @author Christopher K. Allen
         * @since  Apr 22, 2014
         */
        public SignalSet        getFitData();
        
//        /**
//         * Signal attributes (properties) computed from the profile device
//         * data set.  This include signal offset, signal strength, standard
//         * deviation, etc.  These values are computed directed from raw data
//         * or via some processing on the raw data, not fitted data.  
//         * How these values are computed must be should be
//         * defined in in the exposed method or clear from the context.
//         * 
//         * @return  signal properties of the current profile device data set
//         *
//         * @author Christopher K. Allen
//         * @since  Apr 22, 2014
//         */
//        public SignalAttrSet    getRawAttrs();
    //    
        /**
         * Signal attributes (properties) computed from the profile device
         * data set.  This include signal offset, signal strength, standard
         * deviation, etc.  These values are computed from some pre-processed,
         * or fitted, data.  How these values are computed must be should be
         * defined in in the exposed method or clear from the context.
         * 
         * @return  signal properties of the current profile device data set
         *
         * @author Christopher K. Allen
         * @since  Apr 22, 2014
         */
        public SignalAttrSet    getDataAttrs();
        
        /**
         * <p>
         * Indicates whether or not the given wire is operating correctly and
         * producing valid data.
         * The argument is the index for the wire, the same index that would
         * be used for the arrays of data produced by the profile device.
         * </p>
         * <p>
         * If the profile device has no method of validating wire operation
         * then the method will return <code>true</code> regardless of 
         * the argument, rather than a blanket <code>false</code> for all
         * wires.
         * </p>
         * 
         * @param angle     projection angle of the harp to be validated
         * @param iWire     index of the wire to be validated
         * 
         * @return          <code>true</code> if the the wire is operating correctly
         *                  or the device has no method of validation,
         *                  <code>false</code> if the wire is faulty.
         *
         * @author Christopher K. Allen
         * @since  Jul 2, 2014
         */
        public boolean          isValidWire(ANGLE angle, int iWire);
    }

    
    /*
     * Enumeration Constants for Common Values
     */

    /**
     * Enumeration of the projection angles used to produce the profile data. 
     *
     * @since  Jul 16, 2009
     * @author Christopher K. Allen
     */
    public enum ANGLE {

        /** the horizontal plane */
        HOR(0, "Horizontal"), //$NON-NLS-1$

        /** the vertical plane */
        VER(1, "Vertical"), //$NON-NLS-1$

        /** the diagonal plane */
        DIA(2, "Diagonal"); //$NON-NLS-1$


        /*
         * Operations
         */

        /**
         * The the array index for this plot enumeration constant.
         *
         * @return      plot array index
         * 
         * @since  Jul 16, 2009
         * @author Christopher K. Allen
         */
        public int getIndex()       {
            return this.index;
        }

        /**
         * Returns the plot label for this enumeration constant.
         *
         * @return      plot label
         * 
         * @since  Jul 16, 2009
         * @author Christopher K. Allen
         */
        public String getLabel() {
            return this.strLabel;
        }
        
        /**
         * Extracts the proper signal attributes w.r.t. to this plane from the given set of
         * signal attributes.
         * 
         * @param setAttrs      set of signal attributes, one for each phase plane
         * 
         * @return              signal attributes corresponding to this phase plane
         *
         * @author Christopher K. Allen
         * @since  Mar 26, 2014
         */
        public SignalAttrs getSignalAttrs(SignalAttrSet setAttrs) {
            switch (this) {
            case HOR:
                return setAttrs.hor;
            case VER:
                return setAttrs.ver;
            case DIA:
                return setAttrs.dia;
            default:
                return null;        // (this cannot happen)
            }
        }

        /**
         * Extracts the field descriptor for the position channel
         * from the annotations of the given <code>SignalSet</code>-derived class type.
         * 
         * @param clsData   the <code>Data</code>-derived child class with DAQ annotations
         * 
         * @return      position channel field descriptor for this plane
         *  
         * @throws ScadaAnnotationException   a required field of the <code>ASet</code> annotation was empty 
         *                                    or annotation not found
         *
         * @author Christopher K. Allen
         * @since  Sep 27, 2011
         */
        public ScadaFieldDescriptor getSignalPosFd(Class<? extends SignalSet> clsData) throws ScadaAnnotationException  {

            if (  !clsData.isAnnotationPresent(ASet.class) )
                throw new ScadaAnnotationException("No signal information present for class " + DataLivePt.class);

            ASet        annSigSet = clsData.getAnnotation(ASet.class);
            ASignal     annSig;

            switch (this) {

            case HOR:   // I am the horizontal profile   
                annSig = annSigSet.sigHor();
                break;

            case VER:   // I am the vertical profile
                annSig = annSigSet.sigVer();
                break;

            case DIA:   // I am the diagonal profile   
                annSig = annSigSet.sigDia();
                break;

                // I am the walrus
            default:
                return null;
            }

            ScadaFieldDescriptor sfdPosArr = Signal.FIELD.POS.createDescriptor(annSig);
            if (sfdPosArr == null)
                throw new ScadaAnnotationException("Missing field VAL from annotation " + annSig);

            return sfdPosArr;
        }

        /**
         * Extracts the field descriptor for the signal value channel
         * from the set of acquisition descriptors.
         * 
         * @param clsData   the <code>SignalSet</code>-derived child class with DAQ annotations
         * 
         * @return      signal value channel field descriptor for this plane
         *  
         * @throws ScadaAnnotationException   a required field of the <code>ASet</code> annotation was empty 
         *                                    or annotation not found
         *
         * @author Christopher K. Allen
         * @since  Feb 22, 2011
         */
        public ScadaFieldDescriptor getSignalValFd(Class<? extends SignalSet> clsData) throws ScadaAnnotationException {

            if (  ! clsData.isAnnotationPresent(ASignal.ASet.class) ) 
                throw new ScadaAnnotationException("No signal information present for class " + clsData);

            ASet        annSigSet = clsData.getAnnotation(ASet.class);
            ASignal     annSig;

            switch (this) {

            case HOR:   // I am the horizontal profile
                annSig = annSigSet.sigHor();
                break;

            case VER:   // I am the vertical profile
                annSig = annSigSet.sigVer();
                break;


            case DIA:   // I am the diagonal profile
                annSig = annSigSet.sigDia();
                break;

            default:
                return null;
            }

            ScadaFieldDescriptor    sfdValArr = Signal.FIELD.VAL.createDescriptor(annSig);
            if (sfdValArr == null)
                throw new ScadaAnnotationException("Missing field VAL from annotation " + annSig);

            return sfdValArr;
        }



        /*
         * Private
         */

        /** Plot array index */
        private final int     index;

        /** Plot label */
        private final String  strLabel;

        /** 
         * Construct the plot enumeration
         * 
         * @param index     plot array index
         * @param strLabel  plot label 
         */
        private ANGLE(int index, String strLabel) {
            this.index    = index;
            this.strLabel = strLabel;
        }
    }


    /**
     * Enumeration of the available triggering events
     * for the data acquisition.
     *
     * @since  Dec 22, 2009
     * @author Christopher K. Allen
     */
    public enum TRGEVT {
        /** Undefined Code - Exception condition */ UNKNOWN(0),
        /** Trigger event code */ CYCLE_START(1),
        /** Trigger event code */ MPS_RESET(3),
        /** Trigger event code */ MPS_LATCH(4),
        /** Trigger event code */ SPARE0(10),
        /** Trigger event code */ SPARE1(11),
        /** Trigger event code */ SPARE2(12),
        /** Trigger event code */ SOURCE_ON(27),
        /** Trigger event code */ WARM_LINAC_HPRF(28),
        /** Trigger event code */ WARM_LINAC_LLRF(29),
        /** Trigger event code */ COLD_LINAC_HPRF(30),
        /** Trigger event code */ COLD_LINAC_LLRF(31),
        /** Trigger event code */ BEAM_ON(36),
        /** Trigger event code */ BEAM_REFERENCE(37),
        /** Trigger event code */ END_INJECT(38),
        /** Trigger event code */ EXTRACT_EVENT(39),
        /** Trigger event code */ KICKER_CHARGE(40),
        /** Trigger event code */ DIAG_LASER_TRIG(41),
        /** Trigger event code */ RTDL_XMIT(43),
        /** Trigger event code */ RTDL_VALID(44),
        /** Trigger event code */ DIAG_DEMAND(45),
        /** Trigger event code */ DIAG_SLOW(46),
        /** Trigger event code */ DIAG_FAST(47),
        /** Trigger event code */ DIAG_NO_BEAM(48),
        /** Trigger event code */ DIAG_LASER(49),
        /** Trigger event code */ RF_SAMPLE(50),
        /** Trigger event code */ CLK_60HZ(52),
        /** Trigger event code */ CLK_30HZ(53),
        /** Trigger event code */ CLK_20HZ(54),
        /** Trigger event code */ CLK_10HZ(55),
        /** Trigger event code */ CLK_05HZ(56),
        /** Trigger event code */ CLK_02HZ(57),
        /** Trigger event code */ CLK_01HZ(58),
        /** Trigger event code */ DIAG_RTBT_SLOW(59),
        /** Trigger event code */ DIAG_RTBT_FAST(60),
        /** Trigger event code */ DIAG_RTBT(61),
        /** Trigger event code */ PRE_PULSE(63),
        /** Trigger event code */ FLA_0(240),
        /** Trigger event code */ FLA_1(241),
        /** Trigger event code */ FLA_2(242),
        /** Trigger event code */ FLA_3(243),
        /** Trigger event code */ FLA_4(244),
        /** Trigger event code */ FLA_5(245),
        /** Trigger event code */ FLA_6(246),
        /** Trigger event code */ FLA_7(247),
        /** Trigger event code */ TEST_NETWORK_MARKER(249),
        /** Trigger event code */ DIAG_MPS_SNAPSHOT(250),
        /** Trigger event code */ COMPUTE_REP_RATE(251),
        /** Trigger event code */ NEW_REP_RATE_EVENT(252),
        /** Trigger event code */ MPS_ERROR_RESET(253),
        /** Trigger event code */ UTIL_ERROR_RESET(254),
        /** Trigger event code */ SUPERCYCLE_START(255);


        /**
         * Returns the trigger event enumeration constant
         * for the given trigger event code.
         *
         * @param iEvtVal      raw event code value
         * 
         * @return      the trigger event corresponding to the given event code,
         *              or <code>UNKNOWN</code> if there was no match.
         * 
         * @since  Dec 22, 2009
         * @author Christopher K. Allen
         */
        public static TRGEVT getEventFromValue(int iEvtVal) {
            for (TRGEVT evt : TRGEVT.values()) 
                if (evt.getEventValue() == iEvtVal)
                    return evt;

            return TRGEVT.UNKNOWN;
        }

        /**
         * Returns the raw (numeric) event code for the
         * current triggering event. 
         *
         * @return      numeric event code
         * 
         * @since  Dec 22, 2009
         * @author Christopher K. Allen
         */
        public int getEventValue() {
            return this.iEvtCode;
        }

        /*
         * Private
         */

        /** The trigger event code */
        private final int       iEvtCode;


        /** 
         * Create a new enumeration constant
         *  
         * @param iEvtCode  numerical value represented by this enumeration 
         */
        private TRGEVT(int iEvtCode) {
            this.iEvtCode = iEvtCode;
        }
    }

    /**
     * Enumeration of the gain constants for the
     * <code>ProfileDevice</code> amplifier gain values.
     * 
     * @see     WireScanner.SmplConfig#signalGain
     * @see     WireHarp.DevConfig#gainCmn
     *
     * @since  Dec 22, 2009
     * @author Christopher K. Allen
     */
    public enum GAIN {

        /** Unknown gain value */
        UNKNOWN(-1),

        /** Low gain circuit */
        LOW(0),

        /** Medium gain circuit */
        MED(1),

        /** High gain circuit */
        HIGH(2);

        /**
         * Returns the gain enumeration constant for the
         * given gain value.
         *
         * @param intGain       value of the desired gain constant
         * 
         * @return      corresponding gain enumeration constant for given value,
         *              or <code>UNKNOWN</code> if value has no constant
         * 
         * @since  Dec 22, 2009
         * @author Christopher K. Allen
         */
        public static GAIN     getGainFromValue(int intGain) {
            for (GAIN gain : GAIN.values())
                if (gain.getGainValue() == intGain)
                    return gain;
            return UNKNOWN;
        }

        /**
         * Return the value of the gain enumeration.
         *
         * @return      gain circuit value
         * 
         * @since  Dec 22, 2009
         * @author Christopher K. Allen
         */
        public int      getGainValue() {
            return this.intGain;
        }


        /*
         * Private
         */

        /** The gain circuit value */
        private final int       intGain;

        /** 
         * Create a new GAIN enumeration constant with the given value
         * 
         * @param intGain   the numerical gain represented by this enumeration 
         */
        private GAIN(int intGain) {
            this.intGain = intGain;
        }
    }


    /**
     * Enumeration of the various motion states.  That is, the
     * values of <code>{@link DevStatus#mvtStatus}</code>.
     *
     * @since  Jan 22, 2010
     * @author Christopher K. Allen
     */
    public enum MVTVAL {

        /** Unknown or undefined motion code, usually indicates error condition */
        UNDEFINED(-1),

        /**  Scan actuator is stationary */
        STATIONARY(0),

        /**  Scan actuator is moving */
        MOVING(1),

        /**  General motion failure */
        FAILURE(2);


        /**
         * Return the value of the movement state
         * that this enumeration constant represents.
         *
         * @return  motion state value for this enumeration value
         * 
         * @since  Jan 22, 2010
         * @author Christopher K. Allen
         */
        public int  getMotionCode() {
            return this.intVal;
        }

        /**
         * Returns the movement enumeration constant for the
         * given movement code value.
         *
         * @param intMvt       code of the movement indicator
         * 
         * @return      corresponding movement enumeration constant for given code,
         *              or <code>UNDEFINED</code> if value has no constant
         * 
         * @since  Dec 22, 2009
         * @author Christopher K. Allen
         */
        public static MVTVAL getMvtFromCode(int intMvt) {
            for (MVTVAL mvt : MVTVAL.values())
                if (mvt.getMotionCode() == intMvt)
                    return mvt;
            return UNDEFINED;
        }

        /*
         * Private 
         */

        /** The value of the movement state */
        private final int           intVal;

        /** 
         * Create the enumeration constant
         *  
         * @param intVal numerical value represented by this enumeration constant
         */
        private MVTVAL(int intVal) {
            this.intVal = intVal;
        }

    }


    /**
     * Sections of the beam used by the analysis parameter selection
     * methods for setting which part of the acquired beam is used for
     * computing the sample values.
     *
     * @author Christopher K. Allen
     * @since   Nov 16, 2011
     */
    public enum MACROPULSE {

        /** The head of the beam */
        HEAD,

        /** The body of the beam */
        BODY,

        /** The tail of the beam */
        TAIL,

        /** All of the beam pulse */
        ALL;
    }


    /*
     * Local Attributes
     */

    /** The SCADA connection tester */
    private BatchConnectionTest tstConnect = null;


    /*
     * Initialization
     */

	/**
	 * Primary Constructor for ProfileDevice.
	 *
	 * @param strId
	 * @param channelFactory factory for generating channels
	 *
	 * @author Christopher K. Allen
	 * @since  Mar 21, 2014
	 */
	public ProfileDevice( final String strId, final ChannelFactory channelFactory ) {
		super( strId, channelFactory );

		this.tstConnect = new BatchConnectionTest(this);
	}


    /**
     * Constructor for ProfileDevice.
     *
     * @param strId
     *
     * @author Christopher K. Allen
     * @since  Mar 21, 2014
     */
    public ProfileDevice( final String strId ) {
		this( strId, null );
    }




    /*
     * Data Acquisition
     */

    /**
     * Retrieves a given set of configuration parameter values.  The values are returned in a
     * new instance of the data structure type provided in the argument.  (Note that all configuration
     * parameters are contained in data structures derived from the base class <code>ParameterSet</code>.)
     *
     * @param <T>               type parameter of the parameter set requested
     * @param clsType           class type of the parameter set requested
     * 
     * @return                  set of configuration parameters for this device
     * 
     * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible or could not be constructed
     * @throws ConnectionException          unable to connect to a parameter readback channel
     * @throws GetException                 general CA GET exception while reading a parameter value
     *
     * @author Christopher K. Allen
     * @since  Nov 15, 2011
     * 
     * @see ParameterSet
     */
    public <T extends ParameterSet> T acquireConfig(Class<T> clsType)
            throws ConnectionException, GetException, BadStructException {

        try {
            Constructor<T>  ctorCfg = clsType.getConstructor(clsType);
            T               cfgAcq  = ctorCfg.newInstance(this);

            return cfgAcq;

        } catch (SecurityException e) {
            throw new BadStructException("Could not access constructor for " + clsType.getName(), e);

        } catch (NoSuchMethodException e) {
            throw new BadStructException("Could not access constructor for " + clsType.getName(), e);

        } catch (IllegalArgumentException e) {
            throw new BadStructException("Bad constructor argument for " + clsType.getName(), e);

        } catch (InstantiationException e) {
            throw new BadStructException("Could not access instantiate instance of " + clsType.getName(), e);

        } catch (IllegalAccessException e) {
            throw new BadStructException("Could not access constructor for " + clsType.getName(), e);

        } catch (InvocationTargetException e) {
            throw new BadStructException("An exception was thrown by constructor for " + clsType.getName(), e);

        }
    }

    /**
     * Acquires the current measurement data from the wire scanner data buffers.  The
     * type of data set is determined by the class type parameter <code>T</code>
     * given to this method.  (Note that all data sets are data structured derived from the
     * base class <code>Data</code>.)
     *  
     * @param <T>           specific type of data set being requested
     * @param clsType       class type of data being requested
     * 
     * @return              measurement data set currently available on this wire scanner data buffers
     * 
     * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible or could not be constructed
     * @throws ConnectionException          unable to connect to a data buffer channel
     * @throws GetException                 general CA GET exception while reading data buffers
     *
     * @author Christopher K. Allen
     * @since  Nov 15, 2011
     * 
     * @see Data
     */
    public <T extends SignalSet> T acquireData(Class<T> clsType)
            throws ConnectionException, GetException, BadStructException {

        try {
            Constructor<T>  ctorData = clsType.getConstructor(clsType);
            T               dataAcq  = ctorData.newInstance(this);

            return dataAcq;

        } catch (SecurityException e) {
            throw new BadStructException("Could not access constructor for " + clsType.getName(), e);

        } catch (NoSuchMethodException e) {
            throw new BadStructException("Could not access constructor for " + clsType.getName(), e);

        } catch (IllegalArgumentException e) {
            throw new BadStructException("Bad constructor argument for " + clsType.getName(), e);

        } catch (InstantiationException e) {
            throw new BadStructException("Could not access instantiate instance of " + clsType.getName(), e);

        } catch (IllegalAccessException e) {
            throw new BadStructException("Could not access constructor for " + clsType.getName(), e);

        } catch (InvocationTargetException e) {
            throw new BadStructException("An exception was thrown by constructor for " + clsType.getName(), e);

        }
    }




    /**
     * <p>
     * Setup a value monitor on the given process variable (i.e.,
     * using its handle).  The monitor events are
     * sent to the <code>IEventSink</code> object provided.
     * </p>
     * <p>
     * One can specify the event type which fires the monitor using
     * the argument intEvtType.  Any combination of the following
     * event types can be specified with a logical OR operation:
     * <br>
     * <br> &nbsp; <code>Monitor.VALUE</code> - fire upon PV value change
     * <br> &nbsp; <code>Monitor.LOG  </code> - 
     * <br> &nbsp; <code>Monitor.ALARM</code> - fire upon PV alarm value
     * <br>
     * The default value (i.e., no argument) is <code>Monitor.VALUE</code>.
     * </p> 
     *   
     *  @param  pvdFld        handle enumeration for process variable
     *  @param  snkEvents    interface to data sink
     *  @param  mskEvtType   code specifying when the monitor is fired 
     *                       (or'ed combination of {Monitor.VALUE, Monitor.LOG, Monitor.ALARM})
     *                       
     *  @return                 A new monitor on the given process variable
     *  
     *  @throws xal.smf.NoSuchChannelException  
     *                                             if the handle does not identify any 
     *                                             process variable of this accelerator node
     *  @throws xal.ca.ConnectionException     channel is not connected
     *  @throws xal.ca.MonitorException        general monitor failure
     */
    public Monitor createMonitor(XalPvDescriptor.IPvDescriptor pvdFld, IEventSinkValue snkEvents, int ...mskEvtType)
            throws ConnectionException, MonitorException, NoSuchChannelException 
    {
        // Check if there is an event type mask given in the argument list
        int             intEvtType;
        if (mskEvtType.length == 1) 
            intEvtType = mskEvtType[0];
        else
            intEvtType = Monitor.VALUE;

        // Retrieve the channel and create the monitor
        final String  strHnd  = pvdFld.getPvDescriptor().getRbHandle();
        final Channel channel = getAndConnectChannel( strHnd );
        final Monitor monitor = channel.addMonitorValue(snkEvents, intEvtType);

        return monitor;
    }




    /**
     * Set all the hardware parameter values associated
     * with the given data set.
     *
     * @param datPvFlds       set of new hardware values
     * 
     * @throws ConnectionException  unable to connect PV channel
     * @throws PutException         general exception setting values
     * 
     * @since  Dec 17, 2009
     * @author Christopher K. Allen
     */
    public void configureHardware(ParameterSet datPvFlds) throws PutException, ConnectionException 
    {
        datPvFlds.setHardwareValues(this);
    }




    /**
     * Test the connections in all the channels of the given parameter set for this
     * accelerator device.  The test will wait up to the given length
     * of time before declaring failure.
     *
     * @param clsScada      type of a SCADA data structure
     * @param dblTmOut      time out before test fails (in seconds)
     * 
     * @return              <code>true</code> if all connections were successful,
     *                      <code>false</code> if not all connection were made within given time
     *                      
     * @throws BadStructException  the given class is not a SCADA data structure 
     * @throws BadChannelException  An unbound channel handle within a field descriptor 
     *
     * @author Christopher K. Allen
     * @throws BadChannelException 
     * @since  Feb 4, 2011
     */
    public synchronized boolean testConnection(Class<?> clsScada, double dblTmOut)
            throws BadStructException, BadChannelException 
    {
    
        Collection<ScadaFieldDescriptor>    setFds = new ScadaFieldList(clsScada);
    
        if (setFds.size() == 0)
            throw new BadStructException("Class is not a SCADA data structure"); //$NON-NLS-1$
    
        return this.testConnection(setFds, dblTmOut);
    }




    /**
     * Test the connections in all the channels of the given parameter set for this
     * accelerator device.  The test will wait up to the given length
     * of time before declaring failure.
     *
     * @param setFds        set of field descriptors containing channel handles
     * @param dblTmOut      time out before test fails (in seconds)
     * 
     * @return              <code>true</code> if all connections were successful,
     *                      <code>false</code> if not all connection were made within given time
     *                      
     * @throws BadChannelException  An unbound channel handle within a field descriptor 
     *
     * @author Christopher K. Allen
     * @since  Feb 4, 2011
     */
    public synchronized boolean testConnection(Collection<ScadaFieldDescriptor> setFds, double dblTmOut)
            throws BadChannelException {
        boolean bolResult = this.tstConnect.testConnection(setFds, dblTmOut);

        return bolResult;
    }

}
