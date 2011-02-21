/*
 *  WireScanner.java
 *  
 * Created by Tom Pelaia on 3/26/2009.
 * Copyright 2009 Oak Ridge National Lab. All rights reserved.
 *
 *      Modifications:
 *       06/2009 - Christopher K. Allen
 */

package xal.smf.impl;

import xal.ca.Channel;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.IEventSinkValue;
import xal.ca.Monitor;
import xal.ca.MonitorException;
import xal.ca.PutException;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.smf.AcceleratorNode;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.smf.scada.BadStructDefinition;
import xal.smf.scada.Scada;
import xal.smf.scada.XalPvDescriptor;
import xal.smf.scada.ScadaPacket;
import xal.smf.scada.ScadaPacket.FieldDescriptor;
import xal.smf.scada.ScadaPacket.IFieldDescriptor;

import java.lang.reflect.Field;


/**
 * <h1>Wire Scanner Hardware</h1>
 * <p>
 * Represents a wire scanner diagnostic device, in particular
 * those devices installed at the SNS facility.  This class
 * implements the new profile device API at SNS.  The previous
 * device API interfaced with the <tt>SMF</tt> class
 * <code>{@link ProfileMonitor}</code>.
 * </p>
 *
 * @since  Mar 26, 2009
 * @version Mar, 2010
 * @author Tom Pelaia
 * @author Christopher K. Allen
 * 
 * @see xal.smf.impl.ProfileMonitor
 */
public class WireScanner extends AcceleratorNode {


    /*
     * Global Constants and Variables
     */

    /** device type */
    public static final String      s_strType = "WS";

    /** latency between command issue and command buffer reset command (msec) */
    private static int             INT_CMD_LATCY = 200;
//    private static int             INT_CMD_LATCY = AppProperties.DEVICE.CMD_LATENCY.getValue().asInteger();


    /*
     * SMF Requirements
     */
    /**
     * Register type for qualification
     */
    private static void registerType() {
        final ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType( WireScanner.class, s_strType );
        typeManager.registerType( WireScanner.class, "wirescanner" );
    }

    /**
     * Register the hardware device types that this class
     * recognizes.
     */
    static {
        WireScanner.registerType();
    }

    
    /*
     * Global Methods
     */
    
    /**
     * <p>
     * Sets the default command latency used by all wire scanner
     * devices.
     * </p>
     * <p>
     * After a command is issued to a wire scanner device the command
     * buffer must be cleared back to zero.  This is due to a "quirk"
     * in the current EPICS installation where command signal echos
     * occur in the network.  To prevent the command from being issued
     * multiple times it is necessary to clear the command buffer after
     * issuing a command. 
     * </p>  
     *
     * @param intCmdLtcy    the latency between command issue and buffer clear (in millisec)
     *
     * @author Christopher K. Allen
     * @since  Feb 21, 2011
     */
    public static void  setCommandLatency(int intCmdLtcy) {
        WireScanner.INT_CMD_LATCY = intCmdLtcy;
    }





    /**
     * This is a base class to used to narrow
     * the type of <code>{@link ScadaPacket}</code>.
     * Specifically, data structures derived from this
     * type can be used directly as parameters for
     * <code>WireScanner</code> devices.
     * 
     * @since  Dec 18, 2009
     * @author Christopher K. Allen
     */
    public static abstract class ParameterSet extends ScadaPacket {


        /*
         * DataListener Interface
         */

        /**
         * Label used for parameter set identification. 
         *
         * @since       Mar 4, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.tools.data.DataListener#dataLabel()
         */
        @Override
        public String dataLabel() {
            return this.getClass().getCanonicalName();
        }


        /*
         * Initialization
         */

        /**
         * Create a new <code>WireScanner.ParameterSet</code> object.
         *
         * @param arrPfdDataSet
         *
         * @since     Dec 18, 2009
         * @author    Christopher K. Allen
         */
        protected ParameterSet() {
            super();
        }

        /**
         * Create a new <code>WireScanner.ParameterSet</code> object.
         *
         * @param arrPfdDataSet
         *
         * @since     Dec 18, 2009
         * @author    Christopher K. Allen
         */
        protected ParameterSet(WireScanner ws) 
            throws ConnectionException, GetException 
        {
            super();
            super.loadHardwareValues(ws);
        }
    }



    /**
     * Data structure containing the configuration parameters
     * for data acquisition. 
     *
     * @since  Dec 16, 2009
     * @author Christopher K. Allen
     */
    @Scada
    public static class DaqConfig extends WireScanner.ParameterSet {

        /**
         * Enumeration of the channels associated
         * with the profile device data acquisition.
         *
         * @since  Aug 21, 2009
         * @author Christopher K. Allen
         */
        public enum PARAM implements ScadaPacket.IFieldDescriptor {

            /** Digitizer sampling rate - [100, 5M] Hz, should be kept at 1 MHz */
            RATE("sampleRate", new XalPvDescriptor(int.class, "DaqCfgScanRateRb", "DaqCfgScanRateSet") ),

            /** acquisition sampling duration - [0.1, 2] msec */
            WND("sampleWindow", new XalPvDescriptor(double.class, "DaqCfgWindowRb", "DaqCfgWindowSet") ),

            /** acquisition amplifier gain - values {0,1,2} */
            GAIN("signalGain", new XalPvDescriptor(double.class, "DaqCfgGainRb", "DaqCfgGainSet") ),

            /** acquisition timeout - [20,300] seconds */
            TMO("signalTimeout", new XalPvDescriptor(int.class, "DaqCfgTimeoutRb", "DaqCfgTimeoutSet") );

            /*
             * Interface IFieldDescriptor
             */

            /**
             * Returns the data structure field name associated
             * with this enumeration constant.
             *
             * @return  data structure field name of enumeration
             * 
             * @since  Dec 16, 2009
             * @author Christopher K. Allen
             */
            @Override
            public String       getFieldName() {
                return this.strFldNm;
            }

            /**
             * Returns the PV descriptor for the enumeration
             * constant.
             * 
             *  @return the PV descriptor associated with this enumeration
             *
             * @since       Nov 7, 2009
             * @author  Christopher K. Allen
             */
            @Override
            public XalPvDescriptor getPvDescriptor() {
                return this.pvdField;
            }

            /*
             * Private
             */

            /** The data structure field name */
            private final String             strFldNm;

            /** The XAL process variable descriptor */
            private final XalPvDescriptor    pvdField;

            /**
             * Create a new <code>CHANNEL_DACQ</code> constant
             * for the given channel.
             *
             * @param pvdField        process variable descriptor
             *
             * @since     Aug 21, 2009
             * @author    Christopher K. Allen
             */
            PARAM(String strFldNm, XalPvDescriptor dscrPv) {
                this.strFldNm = strFldNm;
                this.pvdField = dscrPv;
            }
        }

        /**
         * Enumeration of the gain constants for the
         * <code>WireScanner.DaqConfig.PARAM.GAIN</code> 
         * constant.
         * 
         * @see     WireScanner.DaqConfig.PARAM
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

            /** Create a new GAIN enumeration constant with the given value */
            private GAIN(int intGain) {
                this.intGain = intGain;
            }
        }

        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws ConnectionException  unable to connect parameter read back channel
         * @throws GetException         general field initialization exception 
         * 
         * @since  Dec 16, 2009
         * @author Christopher K. Allen
         */
        public static DaqConfig acquire(WireScanner ws) 
            throws ConnectionException, GetException  
        {
            return new DaqConfig(ws);
        }



        /** The digitizer sampling rate (in Hz) */
        @Scada.Field(
                type    = int.class, 
                ctrl      = true,
                hndRb   = "DaqCfgScanRateRb",
                hndSet  = "DaqCfgScanRateSet"
                    )
        public int              sampleRate;

        
        /** The sampling window [0.1,2] msec */
        @Scada.Field(
                type    = double.class,
                ctrl      = true,
                hndRb   = "DaqCfgWindowRb",
                hndSet  = "DaqCfgWindowSet" 
                )
        public double           sampleWindow;

        
        /** The amplifier signal gain {0, 1, 2} */
        @Scada.Field(
                type   = int.class,
                ctrl     = true,
                hndRb  = "DaqCfgGainRb",
                hndSet = "DaqCfgGainSet"
                )
        public int              signalGain;

        
        /** The timeout period for a no-beam exception [20,300] sec */
        @Scada.Field(
                type   = int.class,
                ctrl     = true,
                hndRb  = "DaqCfgTimeoutRb",
                hndSet = "DaqCfgTimeoutSet"
                )
        public int              signalTimeout;


        /**
         * Set the gain as a <code>GAIN</code>
         * enumeration.
         *
         * @param gain  new gain value
         * 
         * @since  Dec 23, 2009
         * @author Christopher K. Allen
         */
        public void setGain(GAIN gain) {
            this.signalGain = gain.getGainValue();
        }

        /**
         * Return the gain as a <code>GAIN</code>
         * enumeration.
         *
         * @return      current gain value
         * 
         * @since  Dec 23, 2009
         * @author Christopher K. Allen
         */
        public GAIN getGain() {
            return GAIN.getGainFromValue(this.signalGain);
        }


        /*
         * Initialization
         */

        /**
         * Create a new, uninitialized <code>DaqConfig</code> object.
         *
         *
         * @since     Dec 20, 2009
         * @author    Christopher K. Allen
         */
        public DaqConfig() {
            super();
        }

        /**
         * Create a new <code>DaqConfig</code> object initialized
         * from the given data source. 
         *
         * @param daptSrc       data source containing data structure fields
         *
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public DaqConfig(DataAdaptor daptSrc) {
            super();
            this.update(daptSrc);
        }

        /**
         * Create a new <code>DaqConfig</code> object initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws ConnectionException  unable to connect to a parameter read back channel
         * @throws GetException         general field initialization exception 
         *
         * @since     Dec 16, 2009
         * @author    Christopher K. Allen
         */
        private DaqConfig(WireScanner ws) throws ConnectionException, GetException {
            super(ws);
        }
    }


    /**
     * This class is a data structure for managing scan actuator
     * configuration data parameters.  
     *
     * @since  Nov 9, 2009
     * @author Christopher K. Allen
     * 
     */
    public static class ActrConfig extends WireScanner.ParameterSet {

        /**
         * Enumeration of the channel handles used to configure
         * a data acquisition scan.
         *
         * @since  Nov 6, 2009
         * @author Christopher K. Allen
         */
        public enum PARAM  implements ScadaPacket.IFieldDescriptor {

            /** Actuator speed to initial position (i.e., "initial move") [0.1,10] mm/sec */
            INITVEL("velInit", new XalPvDescriptor(Double.class, "ActrCfgInitSpeedRb", "ActrCfgInitSpeedSet") ),

            /** Actuator acceleration to initial position [1,20] mm/sec<sup>2</sup> */
            INITACCEL("accelInit", new XalPvDescriptor(Double.class, "ActrCfgInitAccelRb", "ActrCfgInitAccelSet") ),

            /** Maximum actuator speed between sampling locations [0.1,10] mm/sec */
            STEPVEL("velStep", new XalPvDescriptor(Double.class, "ActrCfgStepSpeedRb", "ActrCfgStepSpeedSet") ),

            /** Maximum actuator acceleration between sampling locations [1,20] mm/sec<sup>2</sup> */
            STEPACCEL("accelStep", new XalPvDescriptor(Double.class, "ActrCfgStepAccelRb", "ActrCfgStepAccelSet") ),

            /** Maximum actuator speed while searching for a limit switch [0.1,10] mm/sec */
            SRCHVEL("velSearch", new XalPvDescriptor(Double.class, "ActrCfgSearchSpeedRb", "ActrCfgSearchSpeedSet") ),

            /** Maximum actuator acceleration while searching for a limit switch [1,20] mm/sec<sup>2</sup> */
            SRCHACCEL("accelSearch", new XalPvDescriptor(Double.class, "ActrCfgSearchAccelRb", "ActrCfgSearchAccelSet")),

            /** Maximum actuator speed while returning to home location [0.1,10] mm/sec */
            RETVEL("velReturn", new XalPvDescriptor(Double.class, "ActrCfgReturnSpeedRb", "ActrCfgReturnSpeedSet")),

            /** Maximum actuator acceleration while returning to home location [1,20] mm/sec<sup>2</sup> */
            RETACCEL("accelReturn", new XalPvDescriptor(Double.class, "ActrCfgReturnAccelRb", "ActrCfgReturnAccelSet")),

            /** Maximum time to find a limit switch [20,500] sec */
            SRCHTMO("tmoSearch", new XalPvDescriptor(Double.class, "ActrCfgSearchTimeoutRb", "ActrCfgSearchTimeoutSet")),

            /** Maximum time to reach next sampling location [20, 500] sec */
            STEPTMO("tmoStep", new XalPvDescriptor(Double.class, "ActrCfgStepTimeoutRb", "ActrCfgStepTimeoutSet"));



            /*
             * Interface IFieldDescriptor
             */

            /**
             * Returns the data structure field name for this
             * enumeration constant.
             * 
             * @return  field name of enumeration constant
             *
             * @since   Dec 17, 2009
             * @author  Christopher K. Allen
             *
             */
            @Override
            public String getFieldName() {
                return this.strFldNm;
            }

            /**
             * Return the descriptor for the XAL PV corresponding
             * to the current enumeration constant.
             *
             * @return      XAL channel handle for read back signal
             *  
             * @since       Nov 6, 2009
             * @author  Christopher K. Allen
             */
            @Override
            public XalPvDescriptor getPvDescriptor() {
                return this.pvdFld;
            };


            /*
             * Private
             */

            /** The data structure field name for the enumeration constant */
            private final String                strFldNm;

            /** XAL channel handle for enumeration constant */
            private final XalPvDescriptor       pvdFld;

            /** Create a new enumeration constant */
            private PARAM(String strFldNm, XalPvDescriptor dscrPv) {
                this.strFldNm = strFldNm;
                this.pvdFld   = dscrPv; 
            }
        }

        /**
         * Return the set of current device configuration parameters.  
         *
         * @param ws    profile device under request
         * 
         * @return      configuration for given profile device
         * 
         * @throws ConnectionException  unable to connect to a parameter read back channel
         * @throws GetException         general channel access get exception
         * 
         * @since  Nov 9, 2009
         * @author Christopher K. Allen
         */
        public static ActrConfig aquire(WireScanner ws) 
        throws ConnectionException, GetException 
        {
            return new ActrConfig(ws);
        }


        /** 
         * The initial (actuator) speed of the actuator in mm/sec.  
         * This is the maximum speed obtained by the actuator
         * en route to the location where the data acquisition
         * begins. 
         */
        @Scada.Field(
                type   = double.class,
                ctrl   = true,
                hndRb  = "ActrCfgInitSpeedRb",
                hndSet = "ActrCfgInitSpeedSet"
                )
        public double      velInit;

        
        /** 
         * The maximum acceleration of the actuator used
         * to move into the initial scan location; that is,
         * the time derivative of <code>velInit</code>.  The
         * units are mm/sec<sup>2</sup>.
         */
        @Scada.Field(
                type   = double.class,
                ctrl   = true,
                hndRb  = "ActrCfgInitAccelRb",
                hndSet = "ActrCfgInitAccelSet"
                )
        public double      accelInit;

        
        /** 
         * The maximum actuator velocity between sampling
         * points, in mm/sec. 
         */
        @Scada.Field(
                type   = double.class,
                ctrl   = true,
                hndRb  = "ActrCfgStepSpeedRb",
                hndSet = "ActrCfgStepSpeedSet"
                )
        public double      velStep;

        
        /** 
         * The maximum acceleration of the actuator used to
         * move between sampling points, in mm/sec<sup>2</sup>.
         * That is, the time derivative of <code>velStep</code>.
         */
        @Scada.Field(
                type   = double.class,
                ctrl   = true,
                hndRb  = "ActrCfgStepAccelRb",
                hndSet = "ActrCfgStepAccelSet"
                )
        public double     accelStep;

        
        /**
         * The maximum velocity used by the actuator when searching
         * for a limit switch, in mm/sec.
         */
        @Scada.Field(
                type   = double.class,
                ctrl   = true,
                hndRb  = "ActrCfgSearchSpeedRb",
                hndSet = "ActrCfgSearchSpeedSet"
                )
        public double     velSearch;

        
        /** 
         * The maximum acceleration used by the actuator when
         * searching for a limit switch, in mm/sec<sup>2</sup>.
         */
        @Scada.Field(
                type   = double.class,
                ctrl   = true,
                hndRb  = "ActrCfgSearchAccelRb",
                hndSet = "ActrCfgSearchAccelSet"
                )
        public double     accelSearch;

        
        /**
         * The maximum actuator speed when returning to the 
         * home location, in mm/sec.
         */
        @Scada.Field(
                type   = double.class,
                ctrl   = true,
                hndRb  = "ActrCfgReturnSpeedRb",
                hndSet = "ActrCfgReturnSpeedSet"
                )
        public double     velReturn;

        
        /**
         * The maximum actuator acceleration when returning to the 
         * home location, in mm/sec.
         */
        @Scada.Field(
                type   = double.class,
                ctrl   = true,
                hndRb  = "ActrCfgReturnAccelRb",
                hndSet = "ActrCfgReturnAccelSet"
                )
        public double     accelReturn;

        
        /** 
         * The maximum time allowed while the actuator is searching 
         * for a limit switch, in seconds.
         */
        @Scada.Field(
                type   = double.class,
                ctrl   = true,
                hndRb  = "ActrCfgSearchTimeoutRb",
                hndSet = "ActrCfgSearchTimeoutSet"
                )
        public double     tmoSearch;

        
        /**
         * The maximum time allowed to move the actuator between 
         * sampling locations, in seconds. 
         */
        @Scada.Field(
                type   = double.class,
                ctrl   = true,
                hndRb  = "ActrCfgStepTimeoutRb",
                hndSet = "ActrCfgStepTimeoutSet"
                )
        public double     tmoStep;



        /*
         * Initialization
         */


        /**
         * Create a new, uninitialized <code>ActrConfig</code> object.
         *
         *
         * @since     Nov 13, 2009
         * @author    Christopher K. Allen
         */
        public ActrConfig() {
            super();
        }

        /**
         * Create a new <code>ActrConfig</code> object initialized
         * from the given data source. 
         *
         * @param daptSrc       data source containing data structure fields
         *
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public ActrConfig(DataAdaptor daptSrc) {
            super();
            this.update(daptSrc);
        }

        /**
         * Create a new <code>ActrConfig</code> object initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws ConnectionException  unable to connect to a parameter channel
         * @throws GetException         unable to retrieve values from channel access 
         *
         *
         * @since     Dec 16, 2009
         * @author    Christopher K. Allen
         */
        private ActrConfig(WireScanner ws) throws ConnectionException, GetException {
            super(ws);
        }
    }

    /**
     * This class is a data structure for managing scan 
     * configuration data.  It is used with the 
     * <code>PARAM</code> enumeration.
     *
     * @since  Nov 9, 2009
     * @author Christopher K. Allen
     * 
     */
    public static class ScanConfig extends WireScanner.ParameterSet {

        /**
         * Enumeration of the channel handles used to configure
         * a data acquisition scan.
         *
         * @since  Nov 6, 2009
         * @author Christopher K. Allen
         */
        public enum PARAM  implements ScadaPacket.IFieldDescriptor {

            /** Maximum scan length for actuator (in mm) */
            STROKELEN("strokeLength", new XalPvDescriptor(Double.class, "ScanCfgStrokeSizeRb")),

            /** Initial actuator position (i.e., "initial move") of a scan in mm */
            POSINIT("posInit", new XalPvDescriptor(Double.class, "ScanCfgInitPosRb", "ScanCfgInitPosSet") ),

            /** Number of scan steps */
            STEPCNT("stepCount", new XalPvDescriptor(Integer.class, "ScanCfgStepCntRb", "ScanCfgStepCntSet") ),

            /** Size of each scan step (in mm) */
            STEPLEN("stepLength", new XalPvDescriptor(Double.class, "ScanCfgStepSizeRb", "ScanCfgStepSizeSet") ),
            
            /** Number of beam pulses per step (>1 indicates averaging) */
            PULSECNT("pulseCount", new XalPvDescriptor(Integer.class, "ScanCfgStepPulsesRb", "ScanCfgStepPulsesSet") );


            /*
             * Interface IFieldDescriptor
             */

            /**
             * Returns the data structure field name for this
             * enumeration constant.
             * 
             * @return  field name of enumeration constant
             *
             * @since   Dec 17, 2009
             * @author  Christopher K. Allen
             *
             * @see xal.smf.scada.ScadaPacket.IFieldDescriptor#getFieldName()
             */
            @Override
            public String getFieldName() {
                return this.strFldNm;
            }

            /**
             * Return the descriptor for the XAL PV corresponding
             * to the current enumeration constant.
             *
             * @return      XAL channel handle for read back signal
             *  
             * @since       Nov 6, 2009
             * @author  Christopher K. Allen
             */
            @Override
            public XalPvDescriptor getPvDescriptor() {
                return this.pvdFld;
            };


            /*
             * Private
             */

            /** The data structure field name for the enumeration constant */
            private final String                strFldNm;
            /** XAL channel handle for enumeration constant */
            private final XalPvDescriptor       pvdFld;

            /** Create a new enumeration constant */
            private PARAM(String strFldNm, XalPvDescriptor dscrPv) {
                this.strFldNm = strFldNm;
                this.pvdFld   = dscrPv; 
            }

        }


        /**
         * Return the set of profile scan configuration parameters.  The
         * parameters are obtained from a call to channel access and
         * returned the the scan parameter data structure.
         *
         * @param ws    profile device under request
         * 
         * @return      scan configuration for given profile device
         * 
         * @throws ConnectionException  unable to connect parameter read back channel
         * @throws GetException         general channel access get exception
         * 
         * @since  Nov 9, 2009
         * @author Christopher K. Allen
         */
        public static ScanConfig acquire(WireScanner ws) 
        throws ConnectionException, GetException 
        {
            return new ScanConfig(ws);
        }



        /** 
         * The initial (actuator) position of the scan in mm.  This
         * is the actuator location where the data acquisition
         * begins. 
         * The actuator initially moves to this location then
         * begins the data scan at the specified scan rate.  The
         * data is taken between this value
         * and that returned by <code>compFinalPosition()</code>. 
         */
        @Scada.Field(
                type   = double.class,
                ctrl   = true,
                hndRb  = "ScanCfgInitPosRb",
                hndSet = "ScanCfgInitPosSet"
        )
        public double      posInit;

        
        /** 
         * The total number of profile data samples
         * in the scan data set.  That is, the number
         * of actuator steps during data acquisition.
         */
        @Scada.Field(
                type   = int.class,
                ctrl   = true,
                hndRb  = "ScanCfgStepCntRb",
                hndSet = "ScanCfgStepCntSet"
        )
        public int         stepCount;

        
        /** 
         * The size of each scan step in mm.  
         * That is, the distance between actuator 
         * positions for each profile data sample. 
         */
        @Scada.Field(
                type   = double.class,
                ctrl   = true,
                hndRb  = "ScanCfgStepLngRb",
                hndSet = "ScanCfgStepLngSet"
        )
        public double      stepLength;

        /** 
         * The number of beam pulses (traces) used to compute 
         * a profile sample value. Note that a value greater
         * than 1 indicates averaging.  Attempting to set
         * the value less than 1 does nothing.
         */
        @Scada.Field(
                type    = int.class,
                ctrl    = true,
                hndRb   = "ScanCfgStepPulsesRb",
                hndSet  = "ScanCfgStepPulsesSet"
        )
        public int         pulseCount;

        /**
         * The stroke length of the device actuator.
         * Thus, this is the maximum possible distance the
         * wire can travel while taking data.
         * The product of 
         * <code>stepLength</code> &times; <code>stepCount</code>
         * must be less than this value. 
         * This is a read-only quantity.
         */
        @Scada.Field(
                type   = double.class,
                ctrl   = false,
                hndRb  = "ScanCfgStrokeLngRb"
        )
        public double      lngStroke;


        
        
        /*
         * Operations
         */

        /**
         * Compute and return the final actuator location
         * of the scan.
         *
         * @return  maximum end point of the scan (mm)
         * 
         * @since  Nov 9, 2009
         * @author Christopher K. Allen
         */
        public double compFinalPosition() {
            double  dblInterval = (this.stepCount - 1)*this.stepLength;

            return this.posInit + dblInterval;
        }



        /*
         * Initialization
         */


        /**
         * Create a new, uninitialized <code>ScanConfig</code> object.
         *
         *
         * @since     Nov 13, 2009
         * @author    Christopher K. Allen
         */
        public ScanConfig() {
            super();

            this.posInit    = 0.0;
            this.stepCount  = 0;
            this.stepLength = 0.0;
            this.pulseCount = 0;
        }

        /**
         * Create a new <code>ScanConfig</code> object initialized
         * from the given data source. 
         *
         * @param daptSrc       data source containing data structure fields
         *
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public ScanConfig(DataAdaptor daptSrc) {
            super();
            this.update(daptSrc);
        }

        /**
         * Create a new <code>ScanConfig</code> object initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws ConnectionException  unable to connect to a parameter read back channel
         * @throws GetException         general field initialization exception 
         *
         *
         * @since     Dec 16, 2009
         * @author    Christopher K. Allen
         */
        private ScanConfig(WireScanner ws) throws ConnectionException, GetException {
            super(ws);
        }
    }


    /**
     * Data structure containing the configuration parameters
     * for the on-board data processing. 
     *
     * @since  Dec 16, 2009
     * @author Christopher K. Allen
     */
    public static class PrcgConfig extends WireScanner.ParameterSet {

        /**
         * <p>
         * Returns the array of PV descriptors, one for each field of the data 
         * structure.
         * </p> 
         *
         * @return  the method returns <code>WireScanner.PARAM#values()</code>
         *
         * @author Christopher K. Allen
         * @since  Feb 9, 2011
         */
        public static IFieldDescriptor[] getFieldDescriptors() throws BadStructDefinition {
            return PARAM.values();
        }
        
        /**
         * Enumeration of the channels associated
         * with the profile device data acquisition.
         *
         * @since  Aug 21, 2009
         * @author Christopher K. Allen
         */
        public enum PARAM implements ScadaPacket.IFieldDescriptor {

            /** Invert the acquired signal - {-1, +1} */
            SIGINV("sigInv", new XalPvDescriptor(Integer.class, "PrcgCfgInvertRb", "PrcgCfgInvertSet") ),

            /** Start time to begin averaging - [0, 0.002] sec */
            AVGBGN("avgBgn", new XalPvDescriptor(Double.class, "PrcgCfgAvgBeginRb", "PrcgCfgAvgBeginSet") ),

            /** Length of averaging window [0, 0.002] sec */
            AVGLNG("avgLng", new XalPvDescriptor(Double.class, "PrcgCfgAvgLengthRb", "PrcgCfgAvgLengthSet") );



            /*
             * Interface IFieldDescriptor
             */

            /**
             * Returns the data structure field name associated
             * with this enumeration constant.
             *
             * @return  data structure field name of enumeration
             * 
             * @since  Dec 16, 2009
             * @author Christopher K. Allen
             */
            @Override
            public String       getFieldName() {
                return this.strFldNm;
            }

            /**
             * Returns the PV descriptor for the enumeration
             * constant.
             * 
             *  @return the PV descriptor associated with this enumeration
             *
             * @since       Nov 7, 2009
             * @author  Christopher K. Allen
             */
            @Override
            public XalPvDescriptor getPvDescriptor() {
                return this.pvdField;
            }



            /*
             * Private
             */

            /** The data structure field name */
            private final String             strFldNm;

            /** The XAL process variable descriptor */
            private final XalPvDescriptor    pvdField;

            /**
             * Create a new <code>CHANNEL_DACQ</code> constant
             * for the given channel.
             *
             * @param pvdField        process variable descriptor
             *
             * @since     Aug 21, 2009
             * @author    Christopher K. Allen
             */
            PARAM(String strFldNm, XalPvDescriptor dscrPv) {
                this.strFldNm = strFldNm;
                this.pvdField = dscrPv;
            }
        }

        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws ConnectionException  unable to connect parameter read back channel
         * @throws GetException         general field initialization exception 
         * 
         * @since  Dec 16, 2009
         * @author Christopher K. Allen
         */
        public static PrcgConfig acquire(WireScanner ws) 
        throws ConnectionException, GetException  
        {
            return new PrcgConfig(ws);
        }


        /** Inversion of acquired profile signal {-1,+1} */
        @Scada.Field( 
                type    = int.class,
                ctrl    = true,
                hndRb   = "PrcgCfgInvertRb",
                hndSet  = "PrcgCfgInvertSet"
        )
        public int                     sigInv;

        
        /** Time position to start signal averaging */
        @Scada.Field(
                type    = double.class,
                ctrl    = true,
                hndRb   = "PrcgCfgAvgBeginRb",
                hndSet  = "PrcgCfgAvgBeginSet"
        )
        public double                   avgBgn;

        
        /** The size (in time) of the time averaging window */
        @Scada.Field(
                type   = double.class,
                ctrl   = true,
                hndRb  = "PrcgCfgAvgLengthRb",
                hndSet = "PrcgCfgAvgLengthSet"
        )
        public double                   avgLng;


        /*
         * Initialization
         */

        /**
         * Create a new, uninitialized <code>DaqConfig</code> object.
         *
         *
         * @since     Dec 20, 2009
         * @author    Christopher K. Allen
         */
        public PrcgConfig() {
            super();
        }

        /**
         * Create a new <code>PrcgConfig</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public PrcgConfig(DataAdaptor daptSrc) {
            super();
        }

        /**
         * Create a new <code>DaqConfig</code> object initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws ConnectionException  unable to connect to a parameter read back channel
         * @throws GetException         general field initialization exception 
         *
         *
         * @since     Dec 16, 2009
         * @author    Christopher K. Allen
         */
        private PrcgConfig(WireScanner ws) throws ConnectionException, GetException {
            super(ws);
        }
    }


    /**
     * Data structure containing configuration parameters
     * for DAQ triggering. 
     *
     * @since  Dec 16, 2009
     * @author Christopher K. Allen
     */
    public static class TrgConfig extends WireScanner.ParameterSet {

        /**
         * <p>
         * Returns the array of PV descriptors, one for each field of the data 
         * structure.
         * </p> 
         *
         * @return  the method returns <code>WireScanner.PARAM#values()</code>
         *
         * @author Christopher K. Allen
         * @since  Feb 9, 2011
         */
        public static IFieldDescriptor[] getFieldDescriptors() throws BadStructDefinition {
            return PARAM.values();
        }
        
        /**
         * Enumeration of the channels associated
         * with the profile device data acquisition.
         *
         * @since  Aug 21, 2009
         * @author Christopher K. Allen
         */
        public enum PARAM implements ScadaPacket.IFieldDescriptor {

            /** Delay time from trigger event to actual trigger [0,15] msec */
            DELAY("delay", new XalPvDescriptor(Double.class, "TrgCfgDelayRb", "TrgCfgDelaySet") ),

            /** Trigger event - see trigger event codes 0,...,256 */
            TRGEVT("event", new XalPvDescriptor(Integer.class, "TrgCfgTrigEventRb", "TrgCfgTrigEventSet") );


            /*
             * Interface IFieldDescriptor
             */

            /**
             * Returns the data structure field name associated
             * with this enumeration constant.
             *
             * @return  data structure field name of enumeration
             * 
             * @since  Dec 16, 2009
             * @author Christopher K. Allen
             */
            @Override
            public String       getFieldName() {
                return this.strFldNm;
            }

            /**
             * Returns the PV descriptor for the enumeration
             * constant.
             * 
             *  @return the PV descriptor associated with this enumeration
             *
             * @since       Nov 7, 2009
             * @author  Christopher K. Allen
             */
            @Override
            public XalPvDescriptor getPvDescriptor() {
                return this.pvdField;
            }



            /*
             * Private
             */

            /** The data structure field name */
            private final String             strFldNm;

            /** The XAL process variable descriptor */
            private final XalPvDescriptor    pvdField;

            /**
             * Create a new <code>CHANNEL_DACQ</code> constant
             * for the given channel.
             *
             * @param pvdField        process variable descriptor
             *
             * @since     Aug 21, 2009
             * @author    Christopher K. Allen
             */
            PARAM(String strFldNm, XalPvDescriptor dscrPv) {
                this.strFldNm = strFldNm;
                this.pvdField = dscrPv;
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


            /** Create a new enumeration constant */
            private TRGEVT(int iEvtCode) {
                this.iEvtCode = iEvtCode;
            }

        }

        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws ConnectionException  unable to connect parameter read back channel
         * @throws GetException         general field initialization exception 
         * 
         * @since  Dec 16, 2009
         * @author Christopher K. Allen
         */
        public static TrgConfig acquire(WireScanner ws) 
        throws ConnectionException, GetException  
        {
            return new TrgConfig(ws);
        }


        /*
         * Data Fields
         */

        
        /** Time delay between trigger event and actual DAQ trigger */
        @Scada.Field( 
                type   = double.class,
                ctrl   = true,
                hndRb  = "TrgCfgDelayRb",
                hndSet = "TrgCfgDelaySet"
        )
        public double                   delay;

        
        /** Event code to trigger data acquisition - values 0,...,256 */
        @Scada.Field(
                type   = int.class,
                ctrl   = true,
                hndRb  = "TrgCfgTrigEventRb",
                hndSet = "TrgCfgTrigEventSet"
        )
        public int                      event;



        /*
         * Operations
         */

        /**
         * Sets the value of the <code>event</code> field
         * using the appropriate <code>TRGEVT</code> enumeration
         * constant.
         *
         * @param evt   trigger event code
         * 
         * @since  Jan 19, 2010
         * @author Christopher K. Allen
         */
        public void setTrigEvent(TRGEVT evt) {
            this.event = evt.getEventValue();
        }

        /**
         * Returns the triggering event code taken from
         * the raw value field <code>event</code> of this 
         * data structure.
         *
         * @return      the trigger event converted from the raw field.
         * 
         * @since  Jan 19, 2010
         * @author Christopher K. Allen
         */
        public TRGEVT   getTrigEvent() {
            TRGEVT      evt = TRGEVT.getEventFromValue(this.event);

            return evt;
        }


        /*
         * Initialization
         */

        /**
         * Create a new, uninitialized <code>DaqConfig</code> object.
         *
         *
         * @since     Dec 20, 2009
         * @author    Christopher K. Allen
         */
        public TrgConfig() {
            super();
        }

        /**
         * Create a new <code>TrgConfig</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public TrgConfig(DataAdaptor daptSrc) {
            super();
        }

        /**
         * Create a new <code>TrgConfig</code> object initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws ConnectionException  unable to connect to a parameter read back channel
         * @throws GetException         general field initialization exception 
         *
         *
         * @since     Dec 16, 2009
         * @author    Christopher K. Allen
         */
        private TrgConfig(WireScanner ws) throws ConnectionException, GetException {
            super(ws);
        }
    }

    /**
     * Contains device status parameters.
     *
     * @since  Jan 21, 2010
     * @author Christopher K. Allen
     */
    public static class DevStatus extends ParameterSet {

        /**
         * <p>
         * Returns the array of PV descriptors, one for each field of the data 
         * structure.
         * </p> 
         *
         * @return  the method returns <code>WireScanner.PARAM#values()</code>
         *
         * @author Christopher K. Allen
         * @since  Feb 9, 2011
         */
        public static IFieldDescriptor[] getFieldDescriptors() throws BadStructDefinition {
            return PARAM.values();
        }
        
        /**
         * Enumeration of the channels associated
         * with the profile device data acquisition.
         *
         * @since  Aug 21, 2009
         * @author Christopher K. Allen
         */
        public enum PARAM implements ScadaPacket.IFieldDescriptor {

            /** Forward limit switch is activated */
            LIM_FWD("limFwd", new XalPvDescriptor(Integer.class, "StatFwdLimitRb") ),

            /** Reverse limit switch is activated */
            LIM_REV("limRev", new XalPvDescriptor(Integer.class, "StatRevLimitRb") ),

            /** Horizontal wire status: 0=OK, 1=Damage */
            DMG_HOR("dmgHor", new XalPvDescriptor(Integer.class,"StatHorWireDmgRb") ),

            /** Vertical wire status: 0=OK, 1=Damage */
            DMG_VER("dmgVer", new XalPvDescriptor(Integer.class, "StatVerWireDmgRb") ),

            /** Diagonal wire status: 0=OK, 1=Damage */
            DMG_DIA("dmgDia", new XalPvDescriptor(Integer.class, "StatDiaWireDmgRb") ),

            /** MPS level 0 activated */
            ERR_MPS0("errMps0", new XalPvDescriptor(Integer.class, "StatMps0Rb") ),

            /** MPS level 1 activated */
            ERR_MPS1("errMps1",  new XalPvDescriptor(Integer.class, "StatMps1Rb") ),

            /** Power supply error */
            ERR_PS("errPs",      new XalPvDescriptor(Integer.class, "StatPowerSupplyRb") ),

            /** Aborted scan */
            ERR_SCAN("errScan",  new XalPvDescriptor(Integer.class, "StatScanErrorRb") ),

            /** Motion disabled to prevent collision with another device */
            ERR_COLL("errCollsn", new XalPvDescriptor(Integer.class, "StatCollisionRb") ),

            /** The scan sequence identifier - incremented on the completion of each scan */
            ID_SCAN("idScan", new XalPvDescriptor(Integer.class, "StatScanSeqIdRb") ),

            /** Motion state of the wire: 0=stationary, 1=moving, 2=failure */
            MVT_STATUS("mvtStatus", new XalPvDescriptor(Integer.class, "StatMotionRb") ),

            /** Current position of the wire in millimeters */
            WIRE_POS("wirePos", new XalPvDescriptor(Double.class, "StatWirePosRb") ),

            /** Current speed of the wire in millimeters/second */
            WIRE_VEL("wireVel", new XalPvDescriptor(Double.class, "StatWireSpeedRb") ),
            
            /** Maximum length of actuator motion during scan */
            WIRE_MAX("mvtMax", new XalPvDescriptor(Double.class, "StatScanStrokeRb") );

            /*
             * Interface IFieldDescriptor
             */

            /**
             * Returns the data structure field name associated
             * with this enumeration constant.
             *
             * @return  data structure field name of enumeration
             * 
             * @since  Dec 16, 2009
             * @author Christopher K. Allen
             */
            @Override
            public String       getFieldName() {
                return this.strFldNm;
            }

            /**
             * Returns the PV descriptor for the enumeration
             * constant.
             * 
             *  @return the PV descriptor associated with this enumeration
             *
             * @since       Nov 7, 2009
             * @author  Christopher K. Allen
             */
            @Override
            public XalPvDescriptor getPvDescriptor() {
                return this.pvdField;
            }


            /*
             * Private
             */

            /** The data structure field name */
            private final String             strFldNm;

            /** The XAL process variable descriptor */
            private final XalPvDescriptor    pvdField;

            /**
             * Create a new <code>PARAM</code> constant
             * for the given channel.
             *
             * @param pvdField        process variable descriptor
             *
             * @since     Aug 21, 2009
             * @author    Christopher K. Allen
             */
            PARAM(String strFldNm, XalPvDescriptor dscrPv) {
                this.strFldNm = strFldNm;
                this.pvdField = dscrPv;
            }
        }


        /**
         * Enumeration of the various movement states.  That is, the
         * values of <code>{@link DevStatus.PARAM#MVT_STATUS}</code>.
         *
         * @since  Jan 22, 2010
         * @author Christopher K. Allen
         */
        public enum MVTVAL {

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
            public int  getMotionValue() {
                return this.intVal;
            }

            /*
             * Private 
             */
            /** The value of the movement state */
            private final int           intVal;

            /** Create the enumeration constant */
            private MVTVAL(int intVal) {
                this.intVal = intVal;
            }

        }


        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return current set of device status parameters
         *  
         * @throws ConnectionException  unable to connect parameter read back channel
         * @throws GetException         general field initialization exception 
         * 
         * @since  Dec 16, 2009
         * @author Christopher K. Allen
         */
        public static DevStatus acquire(WireScanner ws) 
        throws ConnectionException, GetException  
        {
            return new DevStatus(ws);
        }


        /*
         * Status Parameters
         */

        /** forward limit switch activated */
        @Scada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatFwdLimitRb"
        )
        public int      limFor;

        
        /** reverse limit switch activated */
        @Scada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatRevLimitRb"
        )
        public int      limRev;

        
        /** 
         * Horizontal wire damage
         *  status: 0=OK, 1=Damage 
         */
        @Scada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatHorWireDmgRb"
        )
        public int      dmgHor;
        

        /** 
         * Vertical wire damage 
         *  status: 0=OK, 1=Damage 
         */
        @Scada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatVerWireDmgRb"
        )
        public int      dmgVer;

        
        /** 
         * Diagonal wire damage
         *  status: 0=OK, 1=Damage 
         */
        @Scada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatDiaWireDmgRb"
        )
        public int      dmgDia;

        
        /** MPS 0 trip */
        @Scada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatMps0Rb"
        )
        public int      errMps0;

        
        /** MPS 1 trip */
        @Scada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatMps1Rb"
        )
        public int      errMps1;

        
        /** Power supply error */
        @Scada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatPowerSupplyRb"
        )
        public int      errPs;
        

        /** 
         * General error during scan 
         */
        @Scada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatScanErrorRb"
        )
        public int      errScan;

        
        /** 
         * Actuator collision error. Motion was disabled 
         * to prevent collision with another device. 
         */
        @Scada.Field( 
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatCollisionRb"
        )
        public int      errCollsn;

        
        /** 
         * The current scan sequence identifier.
         * The value is incremented on the completion of 
         * each scan         
         */
        @Scada.Field( 
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatScanSeqIdRb"
        )
        public int      idScan;

        
        /** 
         * Movement state of the wire.
         * Values: 0=stationary, 1=moving, 2=failure
         */
        @Scada.Field( 
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatMotionRb"
        )
        public int      mvtStatus;
        
        
        /** 
         * Maximum actuator excursion during a scan 
         */
        @Scada.Field(
                type    = double.class,
                ctrl    = false,
                hndRb   = "StatScanStrokeRb"
        )
        public double   mvtMax;

        
        /** 
         * Current position of the wire 
         */
        @Scada.Field(
                type    = double.class,
                ctrl    = false,
                hndRb   = "StatWirePosRb"
        )
        public double   wirePos;

        
        /** 
         * Current speed of the wire 
         */
        @Scada.Field(
                type    = double.class,
                ctrl    = false,
                hndRb   = "StatWireSpeedRb"
        )
        public double   wireVel;
        
        
        /** 
         * The maximum stroke length of the actuator 
         */
        @Scada.Field(
                type   = double.class,
                ctrl   = false,
                hndRb  = "StatScanStrokeRb"
        )
        public double   wireMax;


        /**
         * Create a new <code>DevStatus</code> object.
         *
         * @since     Jan 21, 2010
         * @author    Christopher K. Allen
         */
        public DevStatus() {
            super();
        }

        /**
         * Create a new <code>DevStatus</code> object.
         *
         * @param ws    wire scanner device
         * 
         * @throws ConnectionException  unable to connect parameter read back channel
         * @throws GetException         general field initialization exception 
         *
         * @since     Jan 21, 2010
         * @author    Christopher K. Allen
         */
        private DevStatus(WireScanner ws) throws ConnectionException, GetException {
            super();
            super.loadHardwareValues(ws);
        }
    }



    /*
     * Data Structures
     */


    /**
     * Contains the profile data for one wire taken from 
     * a wire-scanner measurement. 
     *
     * @since  Mar 12, 2010
     * @author Christopher K. Allen
     */
    public static class Profile extends ScadaPacket {


        /**
         * Returns the label used for identifying this data
         * structure in a backing store.
         * 
         * @return      the string identifier for the <em>class</em> data
         *
         * @since       Mar 12, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.tools.data.DataListener#dataLabel()
         */
        @Override
        public String dataLabel() {
            return this.strLabel;
        }


        /**
         * Enumeration of the signal properties data fields.
         *
         * @since  Feb 23, 2010
         * @author Christopher K. Allen
         */
        public enum FIELD {

            /**  Sample positions */
            POS("pos", 0),

            /**  Sample value */
            VAL("val", 1);

            /**
             * Returns the name of the field in the data structure
             * which corresponds to this enumeration constant.
             *
             * @return  data structure field name
             * 
             * @since  Nov 13, 2009
             * @author Christopher K. Allen
             */
            public String       getFieldName() {
                return this.strFldName;
            }

            /**
             * Returns the index of the field enumeration in the
             * enumeration list.
             *
             * @return  position index of the enumeration (origin 0)
             * 
             * @since  Mar 15, 2010
             * @author Christopher K. Allen
             */
            public int          getFieldIndex() {
                return this.index;
            }


            /** name of the field in the data structure */
            private final String            strFldName;

            /** user index of the field */
            private final int               index;

            /** create the enumeration */
            private FIELD(String strFldName, int index) {
                this.strFldName = strFldName;
                this.index      = index;
            }
        }


        /*
         * Data Fields
         */

        /**  
         * Positions of the sample points 
         */
        public double[]   pos = {0.0};

        /**  
         * Signal value at the sample location
         */
        public double[]  val = {0.0};


        /*
         * Local Attributes
         */

        /** The data label (for the data adaptor node) */
        private final String            strLabel;




        /**
         * Write out the contents of this signal.
         * 
         * @return      a representation of the signal as a string of (pos,val) pairs
         *
         * @since       Mar 12, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.smf.scada.ScadaPacket#toString()
         */
        @Override
        public String toString() {
            if (pos == null)
                return "";

            StringBuffer        bufSig = new StringBuffer();
            int                 index  = 0;

            for (double dblPos : this.pos) {
                double  dblVal = this.val[index];

                bufSig.append("(");
                bufSig.append(dblPos);
                bufSig.append(",");
                bufSig.append(dblVal);
                bufSig.append(") ");
            }

            return bufSig.toString(); 
        }



        /*
         * Initialization
         */

        /**
         * Create a new <code>Profile</code> object.
         *
         * @param strLabel      data adaptor node label
         * @param arrPfdSet     array of PV descriptor for each data field
         *
         * @since     Feb 23, 2010
         * @author    Christopher K. Allen
         */
        Profile(String strLabel, FieldDescriptor[] arrPfdSet) {
            super(arrPfdSet);

            this.strLabel = strLabel;
        }

        /**
         * Create a new, initiailzed <code>Signal</code> object.  Data 
         * field values are taken immediately from the diagnostic devices.
         *
         * @param strLabel      data adaptor node label
         * @param arrPfdSet     field descriptors for this data set
         * @param ws            hardware device to acquire data
         * 
         * @throws IllegalArgumentException     general field incompatibility exception
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         *
         *
         * @since     Feb 25, 2010
         * @author    Christopher K. Allen
         */
        Profile (String strLabel, FieldDescriptor[] arrPfdSet, WireScanner ws) 
            throws ConnectionException, GetException, IllegalArgumentException 
        {
            super(arrPfdSet);
            super.loadHardwareValues(ws);

            this.strLabel = strLabel;
        }
    }

    /**
     * Base class for data structures contains
     * profile data as measured by the wire scanner
     * (ergo the identifier <code>Msmt</code>.  
     * Note that these guys are different
     * from the other data structures in that they
     * all have common structure.  Thus I have used
     * a different (more simple, I hope) implementation
     * to populate them.  Each derived class supplies
     * its out (field name, PV descriptor) pairs 
     * to the constructor just as in the base class.
     *
     * @since  Nov 13, 2009
     * @author Christopher K. Allen
     */
    public abstract static class Data implements DataListener {

        /*
         * DataListener Interface
         */

        /**
         * Enumeration of the projection angles used to
         * produce the profile data. 
         *
         * @since  Jul 16, 2009
         * @author Christopher K. Allen
         */
        public enum ANGLE {

            /** the horizontal plane */
            HOR(0, "Horizontal"),

            /** the vertical plane */
            VER(1, "Vertical"),

            /** the diagonal plane */
            DIA(2, "Diagonal");


            /*
             * Operations
             */

            /**
             * The the array index for this plot
             * enumeration constant.
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
             * Returns the plot label for this 
             * enumeration constant.
             *
             * @return      plot label
             * 
             * @since  Jul 16, 2009
             * @author Christopher K. Allen
             */
            public String getLabel() {
                return this.strLabel;
            }



            /*
             * Private
             */

            /** Plot array index */
            private final int     index;

            /** Plot label */
            private final String  strLabel;

            /** Construct the plot enumeration */
            private ANGLE(int index, String strLabel) {
                this.index    = index;
                this.strLabel = strLabel;
            }
        }




        /*
         * Data Structure Fields
         */

        /** The horizontal measurement signal */
        public Profile           hor;

        /** The vertical measurement signal */
        public Profile           ver;

        /** The diagonal measurement signal */
        public Profile           dia;



        /*
         * DataListener Interface
         */

        /**
         * Label used for parameter set identification. 
         *
         * @since       Mar 4, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.tools.data.DataListener#dataLabel()
         */
        @Override
        public String dataLabel() {
            return this.getClass().getCanonicalName();
        }

        /**
         * Load the contents of this data set
         * from the persistent store behind the 
         * <code>DataListener</code> interface.
         * 
         * @param adaptor       data source
         *
         * @since       Mar 4, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.tools.data.DataListener#update(xal.tools.data.DataAdaptor)
         */
        @Override
        public void update(DataAdaptor adaptor) {
            DataAdaptor daptSig = adaptor.childAdaptor( this.dataLabel() );

            hor.update(daptSig);
            ver.update(daptSig);
            dia.update(daptSig);
        }

        /**
         * Write out the contents of this measurement data 
         * set to the given data store.
         * 
         * @param adaptor       data store exposing <code>DataListener</code> interface
         *
         * @since       Mar 4, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.tools.data.DataListener#write(xal.tools.data.DataAdaptor)
         */
        @Override
        public void write(DataAdaptor adaptor) {
            DataAdaptor daptSig = adaptor.createChild( this.dataLabel() );

            hor.write(daptSig);
            ver.write(daptSig);
            dia.write(daptSig);
        }

        /*
         * Object Overrides
         */

        /**
         * Write out a text description of the data structure field
         * values.
         * 
         * @return  string representation of the data structure values
         *
         * @since   Feb 5, 2010
         * @author  Christopher K. Allen
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuffer    bufStr = new StringBuffer();
            bufStr.append(this.getClass().getName() + " values\n");

            bufStr.append("horPosition = " + this.hor.toString() + "\n");
            bufStr.append("verPosition = " + this.ver.toString() + "\n");
            bufStr.append("diaPosition = " + this.dia.toString() + "\n");

            return bufStr.toString();
        }

        /*
         * Operations
         */

        /**
         * Returns the signal corresponding to the 
         * given projection angle enumeration constant.
         *
         * @param ang   projection angle
         * 
         * @return      signal for the given projection angle
         * 
         * @since  Apr 9, 2010
         * @author Christopher K. Allen
         */
        public Profile   getProfileFor(ANGLE ang) {
            switch (ang) {
            case HOR:
                return hor;

            case VER:
                return ver;

            case DIA:
                return dia;

            default:    // This cannot happen - need it to satisfy compiler
                return null;
            }
        }

        /*
         * Initialization
         */

        /**
         * Create a new, uninitialized <code>Data</code> object.
         * Specifically, all the arrays are length 1, value 0.
         *
         *
         * @since     Nov 14, 2009
         * @author    Christopher K. Allen
         */
        protected Data(FieldDescriptor[] arrPfdHor, 
                       FieldDescriptor[] arrPfdVer, 
                       FieldDescriptor[] arrPfdDia) 
        {
            this.hor = new Profile(Data.ANGLE.HOR.getLabel(), arrPfdHor);
            this.ver = new Profile(Data.ANGLE.VER.getLabel(), arrPfdVer);
            this.dia = new Profile(Data.ANGLE.DIA.getLabel(), arrPfdDia);
        }


        /**
         * Create a new <code>Data</code> object initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws IllegalArgumentException     general field incompatibility exception
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         *
         * @since     Nov 14, 2009
         * @author    Christopher K. Allen
         */
        protected Data(FieldDescriptor[] arrPfdHor, 
                       FieldDescriptor[] arrPfdVer, 
                       FieldDescriptor[] arrPfdDia, 
                        WireScanner ws) 
        throws IllegalArgumentException, ConnectionException, GetException 
        {
            this.hor = new Profile(Data.ANGLE.HOR.getLabel(), arrPfdHor);
            this.ver = new Profile(Data.ANGLE.VER.getLabel(), arrPfdVer);
            this.dia = new Profile(Data.ANGLE.DIA.getLabel(), arrPfdDia);

            this.hor.loadHardwareValues(ws);
            this.ver.loadHardwareValues(ws);
            this.dia.loadHardwareValues(ws);
        }

    }


    /**
     * Data structure contain the profile data available
     * during data acquisition (i.e., as the scan 
     * progress).
     *
     * @since  Nov 13, 2009
     * @author Christopher K. Allen
     */
    public static class DataLive extends Data {

        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws NoSuchChannelException unable to find channel for given handle 
         * @throws ConnectionException  unable to connect to a field's channel
         * @throws GetException         general CA GET exception while fetch field value
         *
         * 
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static DataLive  acquire(WireScanner ws) 
            throws NoSuchChannelException, ConnectionException, GetException 
        {
            return new DataLive(ws);
        }

        /**
         * Returns the pair of PV field descriptors describing the
         * channels for the (pos,val) profile signal pair.
         *
         * @param enmAng        the desired profile
         * 
         * @return              an array of <code>FieldDescriptor</code> objects for the
         *                      PVs contain the profile position samples and value samples,
         *                      respectively
         * 
         * @since  Mar 16, 2010
         * @author Christopher K. Allen
         */
        public static FieldDescriptor[] getPosValPair(Data.ANGLE enmAng) {
            return ARR_PFD[enmAng.getIndex()];
        }

        /**
         * Returns the PV descriptor for the given profile data 
         * set field component.  That is, the process variable
         * corresponding to the data field of this data 
         * structure.
         *
         * @param enmAng the desired signal profile
         * @param enmFld either position or the values of the signal
         * 
         * @return      descriptor for the process variable for field
         * 
         * @since  Feb 4, 2010
         * @author Christopher K. Allen
         */
        public static XalPvDescriptor getPvDescriptorFor(Data.ANGLE enmAng, Profile.FIELD enmFld) {
            FieldDescriptor[]  arrPfd = getPosValPair(enmAng); 
            FieldDescriptor    pfdFld = arrPfd[enmFld.getFieldIndex()];
            XalPvDescriptor      pvdFld  = pfdFld.getPvDescriptor();

            return pvdFld;
        }


        /** 
         * <h2>Array of the (field names,PV descriptor) pair</h2>
         *  
         *  This is where all the data fields are bound to the 
         *  XAL channel handles in the XAL configuration file.
         */
        private static final FieldDescriptor[][] ARR_PFD = { 
            {
                new FieldDescriptor(Profile.FIELD.POS.getFieldName(), new XalPvDescriptor(Double[].class, "DatHorLivePositions")),
                new FieldDescriptor(Profile.FIELD.VAL.getFieldName(), new XalPvDescriptor(Double[].class, "DatHorLiveSignal"))
            },
            {
                new FieldDescriptor(Profile.FIELD.POS.getFieldName(), new XalPvDescriptor(Double[].class, "DatVerLivePositions")),
                new FieldDescriptor(Profile.FIELD.VAL.getFieldName(), new XalPvDescriptor(Double[].class, "DatVerLiveSignal")),
            },
            {
                new FieldDescriptor(Profile.FIELD.POS.getFieldName(), new XalPvDescriptor(Double[].class, "DatDiaLivePositions")),
                new FieldDescriptor(Profile.FIELD.VAL.getFieldName(), new XalPvDescriptor(Double[].class, "DatDiaLiveSignal"))
            }
        };




        /*
         * Initialization
         */

        /**
         * JavaBeans constructor.
         * Create a new empty <code>DataLive</code> object.
         *
         *
         * @since     Feb 16, 2010
         * @author    Christopher K. Allen
         */
        public DataLive() {
            super(getPosValPair(ANGLE.HOR), getPosValPair(ANGLE.VER), getPosValPair(ANGLE.DIA));
        }

        /**
         * Create a new <code>DataLive</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public DataLive(DataAdaptor daptSrc) {
            super(getPosValPair(ANGLE.HOR), getPosValPair(ANGLE.VER), getPosValPair(ANGLE.DIA));
            this.update(daptSrc);
        }

        /**
         * Create a new <code>DataLive</code> object initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws GetException         general CA GET exception while fetch field value
         * @throws ConnectionException  unable to connect to a parameter read back channel
         *
         * @since     Nov 14, 2009
         * @author    Christopher K. Allen
         */
        private DataLive(WireScanner ws) throws ConnectionException, GetException {
            super(getPosValPair(ANGLE.HOR), getPosValPair(ANGLE.VER), getPosValPair(ANGLE.DIA), ws);
        }

    }



    /**
     * Data structure containing the profile data available
     * after data acquisition (i.e., once the scan 
     * is complete).
     *
     * @since  Nov 13, 2009
     * @author Christopher K. Allen
     */
    public static class DataRaw extends Data {

        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws NoSuchChannelException unable to find channel for given handle 
         * @throws ConnectionException  unable to connect to a field's channel
         * @throws GetException         general CA GET exception while fetch field value
         *
         * 
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static DataRaw acquire(WireScanner ws) 
        throws NoSuchChannelException, ConnectionException, GetException 
        {
            return new DataRaw(ws);
        }

        /**
         * Returns the pair of PV field descriptors describing the
         * channels for the (pos,val) profile signal pair.
         *
         * @param enmAng        the desired profile
         * 
         * @return              an array of <code>FieldDescriptor</code> objects for the
         *                      PVs contain the profile position samples and value samples,
         *                      respectively
         * 
         * @since  Mar 16, 2010
         * @author Christopher K. Allen
         */
        public static FieldDescriptor[] getPosValPair(Data.ANGLE enmAng) {
            return ARR_PFD[enmAng.getIndex()];
        }


        /** 
         * <h2>Array of the (field names,PV descriptor) pair </h2>
         *  
         *  This is where all the data fields are bound to the 
         *  XAL channel handles in the XAL configuration file.
         */
        private static final FieldDescriptor[][] ARR_PFD = {
            {
                new FieldDescriptor(Profile.FIELD.POS.getFieldName(), new XalPvDescriptor(Double[].class, "DatHorRawPositions")),
                new FieldDescriptor(Profile.FIELD.VAL.getFieldName(), new XalPvDescriptor(Double[].class, "DatHorRawSignal"))
            },
            {
                new FieldDescriptor(Profile.FIELD.POS.getFieldName(), new XalPvDescriptor(Double[].class, "DatVerRawPositions")),
                new FieldDescriptor(Profile.FIELD.VAL.getFieldName(), new XalPvDescriptor(Double[].class, "DatVerRawSignal")),
            },
            {
                new FieldDescriptor(Profile.FIELD.POS.getFieldName(), new XalPvDescriptor(Double[].class, "DatDiaRawPositions")),
                new FieldDescriptor(Profile.FIELD.VAL.getFieldName(), new XalPvDescriptor(Double[].class, "DatDiaRawSignal"))
            }
        };



        /**
         * JavaBeans constructor.
         * Create a new, empty <code>DataRaw</code> object.
         *
         *
         * @since     Feb 26, 2010
         * @author    Christopher K. Allen
         */
        public DataRaw() {
            super(getPosValPair(ANGLE.HOR), getPosValPair(ANGLE.VER), getPosValPair(ANGLE.DIA));
        }

        /**
         * Create a new <code>DataRaw</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public DataRaw(DataAdaptor daptSrc) {
            super(getPosValPair(ANGLE.HOR), getPosValPair(ANGLE.VER), getPosValPair(ANGLE.DIA));
            this.update(daptSrc);
        }

        /**
         * Create a new <code>DataLive</code> object initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws ConnectionException  unable to connect data read back channel
         * @throws GetException         general CA GET exception while fetch field value
         *
         * @since     Nov 14, 2009
         * @author    Christopher K. Allen
         */
        private DataRaw(WireScanner ws) throws ConnectionException, GetException {
            super(getPosValPair(ANGLE.HOR), getPosValPair(ANGLE.VER), getPosValPair(ANGLE.DIA), ws);
        }
    }



    /**
     * Data structure containing the fitted profile data 
     * available after data acquisition (i.e., the profile
     * fit as computed by the acquisition software).
     *
     * @since  Nov 13, 2009
     * @author Christopher K. Allen
     */
    public static class DataFit extends Data {

        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws ConnectionException  unable to connect data read back channel
         * @throws GetException         general CA GET exception while fetch field value
         *
         * 
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static DataFit aquire(WireScanner ws) throws ConnectionException, GetException {
            return new DataFit(ws);
        }

        /**
         * Returns the pair of PV field descriptors describing the
         * channels for the (pos,val) profile signal pair.
         *
         * @param enmAng        the desired profile
         * 
         * @return              an array of <code>FieldDescriptor</code> objects for the
         *                      PVs contain the profile position samples and value samples,
         *                      respectively
         * 
         * @since  Mar 16, 2010
         * @author Christopher K. Allen
         */
        public static FieldDescriptor[] getPosValPair(Data.ANGLE enmAng) {
            return ARR_PFD[enmAng.getIndex()];
        }

        /** 
         * <h2>Array of the (field names,PV descriptor) pair </h2>
         *  
         *  This is where all the data fields are bound to the 
         *  XAL channel handles in the XAL configuration file.
         */
        private static final FieldDescriptor[][] ARR_PFD = {
            {
                new FieldDescriptor(Profile.FIELD.POS.getFieldName(), new XalPvDescriptor(Double[].class, "DatHorFitPositions")),
                new FieldDescriptor(Profile.FIELD.VAL.getFieldName(), new XalPvDescriptor(Double[].class, "DatHorFitSignal"))
            },
            {
                new FieldDescriptor(Profile.FIELD.POS.getFieldName(), new XalPvDescriptor(Double[].class, "DatVerFitPositions")),
                new FieldDescriptor(Profile.FIELD.VAL.getFieldName(), new XalPvDescriptor(Double[].class, "DatVerFitSignal"))
            },
            {
                new FieldDescriptor(Profile.FIELD.POS.getFieldName(), new XalPvDescriptor(Double[].class, "DatDiaFitPositions")),
                new FieldDescriptor(Profile.FIELD.VAL.getFieldName(), new XalPvDescriptor(Double[].class, "DatDiaFitSignal"))
            }
        };


        /**
         * JavaBeans constructor.
         * Creates a new, empty <code>DataFit</code> object.
         *
         *
         * @since     Feb 26, 2010
         * @author    Christopher K. Allen
         */
        public DataFit() {
            super(getPosValPair(ANGLE.HOR), getPosValPair(ANGLE.VER), getPosValPair(ANGLE.DIA));
        }

        /**
         * Create a new <code>DataFit</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public DataFit(DataAdaptor daptSrc) {
            super(getPosValPair(ANGLE.HOR), getPosValPair(ANGLE.VER), getPosValPair(ANGLE.DIA));
            this.update(daptSrc);
        }

        /**
         * Create a new <code>DataLive</code> object initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws ConnectionException  unable to connect data read back channel
         * @throws GetException         general CA GET exception while fetch field value
         *
         * @since     Nov 14, 2009
         * @author    Christopher K. Allen
         */
        private DataFit(WireScanner ws) throws ConnectionException, GetException {
            super(getPosValPair(ANGLE.HOR), getPosValPair(ANGLE.VER), getPosValPair(ANGLE.DIA), ws);
        }
    }


    /**
     * Data structure containing the last available
     * raw data trace as defined by the sample rate
     * and duration.
     *
     * @since  Nov 13, 2009
     * @author Christopher K. Allen
     */
    public static class Trace extends Data {

        /**
         * Maintains the time step parameter used for 
         * trace measurements.  The same value is used
         * for all projection planes.
         *
         * @since  Jun 9, 2010
         * @author Christopher K. Allen
         */
        public static class TimeStep extends ScadaPacket {

            /** The singular PV we maintain */
            static FieldDescriptor[]  ARR_PFD = {
                            new FieldDescriptor("val", double.class, "DatTraceTimeStep")
                            };
            
            
            /*
             * Local Attributes 
             */
            
            /** The time step between trace samples.  This is the same value for all planes */
            public double           val;
            

            /*
             * DataListener Interface
             */
            
            /**
             * Returns the label used to identify the fields of this
             * class in a <code>{@link DataAdaptor}</code> data store.
             *
             * @since 	Jun 9, 2010
             * @author  Christopher K. Allen
             *
             * @see xal.tools.data.DataListener#dataLabel()
             */
            @Override
            public String dataLabel() {
                return this.getClass().getCanonicalName();
            }

            /*
             * Initialization
             */
            
            /**
             * Create a new <code>TimeStep</code> object.
             *
             * @since     Jun 9, 2010
             * @author    Christopher K. Allen
             */
            protected TimeStep() {
                super(ARR_PFD);
            }
            
            /**
             * Create a new <code>TimeStep</code> object and initialize
             * with values fetched from the given device.
             *
             * @param ws    data acquisition device
             * 
             * @throws IllegalArgumentException     general field incompatibility exception
             * @throws ConnectionException          unable to connect to a parameter read back channel
             * @throws GetException                 general CA GET exception while fetch field value
             *
             * @since     Jun 9, 2010
             * @author    Christopher K. Allen
             */
            protected TimeStep(WireScanner ws) throws ConnectionException, GetException, BadStructDefinition {
                this();
                super.loadHardwareValues(ws);
            }
            
            /**
             * Create a new <code>TimeStep</code> object and initialize it
             * from the give data source.
             *
             * @param daptSrc   data store containing state information
             *
             * @since     Jun 10, 2010
             * @author    Christopher K. Allen
             */
            protected TimeStep(DataAdaptor daptSrc) {
                this();
                this.update(daptSrc);
            }
        }
        
        
        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws NoSuchChannelException unable to find channel for given handle 
         * @throws ConnectionException  unable to connect to a field's channel
         * @throws GetException         general CA GET exception while fetch field value
         *
         * 
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static Trace acquire(WireScanner ws) 
            throws NoSuchChannelException, ConnectionException, GetException 
        {
            return new Trace(ws);
        }


        /**
         * Returns the pair of PV field descriptors describing the
         * channels for the (pos,val) profile signal pair.
         *
         * @param enmAng        the desired profile
         * 
         * @return              an array of <code>FieldDescriptor</code> objects for the
         *                      PVs contain the profile position samples and value samples,
         *                      respectively
         * 
         * @since  Mar 16, 2010
         * @author Christopher K. Allen
         */
        public static FieldDescriptor[] getPosValPair(Data.ANGLE enmAng) {
            return ARR_PFD[enmAng.getIndex()];
        }

        /**
         * Returns the PV descriptor for the given profile data 
         * set field component.  That is, the process variable
         * corresponding to the data field of this data 
         * structure.
         *
         * @param enmAng the desired signal profile
         * @param enmFld either position or the values of the signal
         * 
         * @return      descriptor for the process variable for field
         * 
         * @since  Feb 4, 2010
         * @author Christopher K. Allen
         */
        public static XalPvDescriptor getPvDescriptorFor(Data.ANGLE enmAng, Profile.FIELD enmFld) {
            FieldDescriptor[]  arrPfd = getPosValPair(enmAng); 
            FieldDescriptor    pfdFld = arrPfd[enmFld.getFieldIndex()];
            XalPvDescriptor      pvdFld = pfdFld.getPvDescriptor();

            return pvdFld;
        }


        /** 
         * <h2>Array of the (field names,PV descriptor) pair </h2>
         *  
         *  This is where all the data fields are bound to the 
         *  XAL channel handles in the XAL configuration file.
         */
        private static final FieldDescriptor[][] ARR_PFD = {
            {
                new FieldDescriptor(Profile.FIELD.POS.getFieldName(), new XalPvDescriptor(Double[].class, "DatHorTracePositions")),
                new FieldDescriptor(Profile.FIELD.VAL.getFieldName(), new XalPvDescriptor(Double[].class, "DatHorTraceSignal"))
            },
            {
                new FieldDescriptor(Profile.FIELD.POS.getFieldName(), new XalPvDescriptor(Double[].class, "DatVerTracePositions")),
                new FieldDescriptor(Profile.FIELD.VAL.getFieldName(), new XalPvDescriptor(Double[].class, "DatVerTraceSignal"))
            },
            {
                new FieldDescriptor(Profile.FIELD.POS.getFieldName(), new XalPvDescriptor(Double[].class, "DatDiaTracePositions")),
                new FieldDescriptor(Profile.FIELD.VAL.getFieldName(), new XalPvDescriptor(Double[].class, "DatDiaTraceSignal"))
            }
        };
        
        
        /*
         * Local Attributes
         */
        
        /** The time step between trace samples */
        public TimeStep        dt;
        
        
        
        
        /*
         * DataListener Interface
         */
        
        /**
         * Load the data structure fields from the given
         * data source.
         *
         * @since       Jun 9, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.smf.impl.WireScanner.Data#update(xal.tools.data.DataAdaptor)
         */
        @Override
        public void update(DataAdaptor adaptor) {
            this.dt.update(adaptor);
            super.update(adaptor);
        }

        /**
         * Save the data structure field values to the given 
         * data sink.
         * 
         *
         * @since       Jun 9, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.smf.impl.WireScanner.Data#write(xal.tools.data.DataAdaptor)
         */
        @Override
        public void write(DataAdaptor adaptor) {
            this.dt.write(adaptor);
            super.write(adaptor);
        }

        
        /*
         * Initialization
         */

        /**
         * JavaBeans constructor.
         * Creates a new <code>DataTrace</code> object.
         *
         *
         * @since     Feb 26, 2010
         * @author    Christopher K. Allen
         */
        public Trace() {
            super(getPosValPair(ANGLE.HOR), getPosValPair(ANGLE.VER), getPosValPair(ANGLE.DIA));
            
            this.dt = new TimeStep();
        }

        /**
         * Create a new <code>DataTrace</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public Trace(DataAdaptor daptSrc) {
            super(getPosValPair(ANGLE.HOR), getPosValPair(ANGLE.VER), getPosValPair(ANGLE.DIA));
            
            this.dt = new TimeStep(daptSrc);
            this.update(daptSrc);
        }

        /**
         * Create a new <code>DataLive</code> object initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws ConnectionException  unable to connect data read back channel
         * @throws GetException         general CA GET exception while fetch field value
         *
         * @since     Nov 14, 2009
         * @author    Christopher K. Allen
         */
        private Trace(WireScanner ws) throws ConnectionException, GetException {
            super(getPosValPair(ANGLE.HOR), getPosValPair(ANGLE.VER), getPosValPair(ANGLE.DIA));

            this.dt = new TimeStep(ws);
            
            this.hor.loadHardwareValues(ws);
            this.ver.loadHardwareValues(ws);
            this.dia.loadHardwareValues(ws);
            this.dt.loadHardwareValues(ws);
            
            this.hor.pos = this.compTimeVector(this.hor);
            this.ver.pos = this.compTimeVector(this.ver);
            this.dia.pos = this.compTimeVector(this.dia);
        }
        
        /*
         * Support Methods
         */
        
        /**
         * Computes a time vector for the given profile
         * object.  The returned vector will have the same
         * length as the value vector field of the argument
         * (i.e., <var>prf.val.length</var>).  The values
         * will start at 0 (at index 0) and will be spaced
         * a distance <code>{@link this#dt}</code> apart.
         *
         * @param prf   profile object for which the position vector is being computed
         * 
         * @return      position vector for given profile object
         * 
         * @since  Jun 10, 2010
         * @author Christopher K. Allen
         */
        private double[]        compTimeVector(Profile prf) {
            
            // Allocate a position vector the size of the value vector
            int         nPts    = prf.val.length;
            double[]    arrPos  = new double[nPts];
            
            // Pack the position vector will values dt.val apart
            double      dblPos  = 0.0;
            for (int n=0; n<nPts; n++) {
                arrPos[n] = dblPos;
                dblPos   += this.dt.val;
            }
            
            return arrPos;
        }
    }


    /**
     * Quantitative properties of a signal.
     *
     *
     * @since  Feb 19, 2010
     * @author Christopher K. Allen
     */
    public static class ProfileAttrs extends ScadaPacket {


        /*
         * Global Constants
         */

        /** The attribute tag identifying the wire from which the signal was taken */
        public static final String      STR_TAG_WIRE_ID = "angle";


        /*
         * DataListener Interface
         */

        /**
         * Label used for parameter set identification. 
         *
         * @since       Mar 4, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.tools.data.DataListener#dataLabel()
         */
        @Override
        public String dataLabel() {
            return this.strLabel;
        }


        /**
         * Enumeration of the signal properties data fields.
         *
         * @since  Feb 23, 2010
         * @author Christopher K. Allen
         */
        public enum PROP {

            /**  Signal amplitude */
            AMP("amp"),

            /**  Additive offset of the signal from zero baseline */
            OFFSET("offset"),

            /**  Area under the signal curve; the integral; the total mass */
            AREA("area"),

            /**  The statistical average; the center of mass */
            MEAN("mean"),

            /**  The standard deviation */
            STDEV("stdev");

            /**
             * Returns the name of the field in the data structure
             * which corresponds to this enumeration constant.
             *
             * @return  data structure field name
             * 
             * @since  Nov 13, 2009
             * @author Christopher K. Allen
             */
            public String       getFieldName() {
                return this.strFldName;
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
            public double       getFieldValue(ProfileAttrs data) {

                Class<? extends ScadaPacket> clsData    = data.getClass();
                try {
                    Field       fldDataFld = clsData.getField( getFieldName() );
                    double      dblFldVal  = fldDataFld.getDouble(data);

                    return dblFldVal;

                } catch (SecurityException e) {
                    System.err.println("SERIOUS ERROR: WireScanner$SignalTrait#getFieldValue()");
                    e.printStackTrace();

                } catch (NoSuchFieldException e) {
                    System.err.println("SERIOUS ERROR: WireScanner$SignalTrait#getFieldValue()");
                    e.printStackTrace();

                } catch (IllegalArgumentException e) {
                    System.err.println("SERIOUS ERROR: WireScanner$SignalTrait#getFieldValue()");
                    e.printStackTrace();

                } catch (IllegalAccessException e) {
                    System.err.println("SERIOUS ERROR: WireScanner$SignalTrait#getFieldValue()");
                    e.printStackTrace();

                }

                return 0.0;
            }


            /** name of the field in the data structure */
            private final String            strFldName;

            /** create the enumeration */
            private PROP(String strFldName) {
                this.strFldName  = strFldName;
            }
        }

        /*
         * Data Fields
         */

        /**  Maximum value of the signal over baseline */
        public double   amp;

        /**  Value of the signal baseline, i.e., sensor output at zero input */
        public double   offset;

        /**  Area under the signal curve minus baseline */
        public double   area;

        /**  Axis location of the center of mass */
        public double   mean;

        /**  The statistical standard deviation */
        public double   stdev;


        /*
         * Local Attributes
         */

        /** The data label (for the data adaptor node) */
        private final String            strLabel;


        /*
         * Initialization
         */

        /**
         * Create a new <code>Signal</code> object.
         *
         * @param strLabel      data adaptor node label
         * @param arrPfdSet     array of PV descriptor for each data field
         *
         * @since     Feb 23, 2010
         * @author    Christopher K. Allen
         */
        ProfileAttrs(String strLabel, FieldDescriptor[] arrPfdSet) {
            super(arrPfdSet);

            this.strLabel = strLabel;
        }

        /**
         * Create a new, initiailzed <code>Signal</code> object.  Data 
         * field values are taken immediately from the diagnostic devices.
         *
         * @param strLabel      data adaptor node label
         * @param arrPfdSet     field descriptors for this data set
         * @param ws            hardware device to acquire data
         * 
         * @throws IllegalArgumentException     general field incompatibility exception
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         *
         *
         * @since     Feb 25, 2010
         * @author    Christopher K. Allen
         */
        ProfileAttrs(String strLabel, FieldDescriptor[] arrPfdSet, WireScanner ws) 
        throws ConnectionException, GetException, IllegalArgumentException 
        {
            super(arrPfdSet);
            super.loadHardwareValues(ws);

            this.strLabel = strLabel;
        }
    }

    /**
     * Data structure containing the signal properties of the
     * the profile data sets acquired from a wire scanner.
     *
     * @since  Feb 23, 2010
     * @author Christopher K. Allen
     */
    public abstract static class ProfileAttrSet implements DataListener {


        /*
         * Instance Attributes
         */

        /** Horizontal wire signal properties */
        public ProfileAttrs           hor;

        /** Horizontal wire signal properties */
        public ProfileAttrs           ver;

        /** Horizontal wire signal properties */
        public ProfileAttrs           dia;


        /*
         * Operations
         */

        /**
         * Returns the signal properties data structure
         * (i.e., <code>ProfileAttrs</code> object) corresponding
         * to the given projection angle.
         *
         * @param ang   projection angle
         * 
         * @return      profile signal properties for the given projection angle
         * 
         * @since  Apr 23, 2010
         * @author Christopher K. Allen
         */
        public ProfileAttrs     getSignalTraits(Data.ANGLE ang) {

            switch (ang) {

            case HOR:
                return hor;

            case VER:
                return ver;

            case DIA:
                return dia;
            }

            // This shouldn't happen
            return null;
        }


        /*
         * DataListener Interface
         */

        /**
         * Label used for parameter set identification.
         * 
         *  @return     string label (identifier) for parameter set
         *
         * @since       Mar 4, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.tools.data.DataListener#dataLabel()
         */
        @Override
        public String dataLabel() {
            return this.getClass().getCanonicalName();
        }

        /**
         * Load the contents of this signal traits set
         * from the persistent store behind the 
         * <code>DataListener</code> interface.
         * 
         * @param adaptor       data source
         *
         * @since       Mar 4, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.tools.data.DataListener#update(xal.tools.data.DataAdaptor)
         */
        @Override
        public void update(DataAdaptor adaptor) {
            DataAdaptor daptSig = adaptor.childAdaptor( this.dataLabel() );

            hor.update(daptSig);
            ver.update(daptSig);
            dia.update(daptSig);
        }

        /**
         * Write out the contents of this signal traits 
         * set to the given data store.
         * 
         * @param adaptor       data store exposing <code>DataListener</code> interface
         *
         * @since       Mar 4, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.tools.data.DataListener#write(xal.tools.data.DataAdaptor)
         */
        @Override
        public void write(DataAdaptor adaptor) {
            DataAdaptor daptSig = adaptor.createChild( this.dataLabel() );

            hor.write(daptSig);
            ver.write(daptSig);
            dia.write(daptSig);
        }


        /* 
         * Initialization
         */

        /**
         * Create a new <code>ProfileAttrSet</code> object.
         *
         * @param arrPfdHor        the horizontal wire signal 
         * @param arrPfdVer        the vertical wire signal
         * @param arrPfdDia        the diagonal wire signal
         *
         * @since     Feb 25, 2010
         * @author    Christopher K. Allen
         */
        protected ProfileAttrSet(FieldDescriptor[] arrPfdHor, 
                                 FieldDescriptor[] arrPfdVer, 
                                 FieldDescriptor[] arrPfdDia) 
        {
            this.hor = new ProfileAttrs(Data.ANGLE.HOR.getLabel(), arrPfdHor);
            this.ver = new ProfileAttrs(Data.ANGLE.VER.getLabel(), arrPfdVer);
            this.dia = new ProfileAttrs(Data.ANGLE.DIA.getLabel(), arrPfdDia);
        }

        /**
         * Create a new, initialized <code>ProfileAttrSet</code> object.
         *
         * @param arrPfdHor     set of process variable descriptors for the horizonal signal
         * @param arrPfdVer     set of process variable descriptors for the vertical signal
         * @param arrPfdDia     set of process variable descriptors for the diagonal signal
         * @param ws            hardware device containing initialization data.
         *
         * @throws IllegalArgumentException     general field incompatibility exception
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value

         * @since     Feb 25, 2010
         * @author    Christopher K. Allen
         */
        protected ProfileAttrSet(FieldDescriptor[] arrPfdHor, 
                                 FieldDescriptor[] arrPfdVer, 
                                 FieldDescriptor[] arrPfdDia,
                                 WireScanner       ws) 
            throws ConnectionException, GetException, IllegalArgumentException 
                        
        {
            this(arrPfdHor, arrPfdVer, arrPfdDia);

            this.hor.loadHardwareValues(ws);
            this.ver.loadHardwareValues(ws);
            this.dia.loadHardwareValues(ws);
        }

    }



    /**
     * Data structure containing the profile data characteristics
     * when modelled as a Gaussian signal.
     *
     * @since  Feb 23, 2010
     * @author Christopher K. Allen
     */
    public static class GaussFitAttrSet extends ProfileAttrSet {

        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws NoSuchChannelException unable to find channel for given handle 
         * @throws ConnectionException  unable to connect to a field's channel
         * @throws GetException         general CA GET exception while fetch field value
         *
         * 
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static GaussFitAttrSet acquire(WireScanner ws) 
        throws NoSuchChannelException, ConnectionException, GetException 
        {
            return new GaussFitAttrSet(ws);
        }

        /** 
         * <h2>Array of the (field names,PV descriptor) pair: Horizontal </h2>
         *  
         *  This is where all the data fields are bound to the 
         *  XAL channel handles in the XAL configuration file.
         */
        private static final FieldDescriptor[] ARR_PFD_HOR = {
            new FieldDescriptor(ProfileAttrs.PROP.AMP.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorGaussAmp")),
            new FieldDescriptor(ProfileAttrs.PROP.OFFSET.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorGaussOffset")),
            new FieldDescriptor(ProfileAttrs.PROP.AREA.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorGaussArea")),
            new FieldDescriptor(ProfileAttrs.PROP.MEAN.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorGaussMean")),
            new FieldDescriptor(ProfileAttrs.PROP.STDEV.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorGaussStd")),
        };

        /** 
         * <h2>Array of the (field names,PV descriptor) pair: Vertical </h2>
         *  
         *  This is where all the data fields are bound to the 
         *  XAL channel handles in the XAL configuration file.
         */
        private static final FieldDescriptor[] ARR_PFD_VER = {
            new FieldDescriptor(ProfileAttrs.PROP.AMP.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerGaussAmp")),
            new FieldDescriptor(ProfileAttrs.PROP.OFFSET.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerGaussOffset")),
            new FieldDescriptor(ProfileAttrs.PROP.AREA.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerGaussArea")),
            new FieldDescriptor(ProfileAttrs.PROP.MEAN.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerGaussMean")),
            new FieldDescriptor(ProfileAttrs.PROP.STDEV.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerGaussStd")),
        };

        /** 
         * <h2>Array of the (field names,PV descriptor) pair: Diagonal </h2>
         *  
         *  This is where all the data fields are bound to the 
         *  XAL channel handles in the XAL configuration file.
         */
        private static final FieldDescriptor[] ARR_PFD_DIA = {
            new FieldDescriptor(ProfileAttrs.PROP.AMP.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaGaussAmp")),
            new FieldDescriptor(ProfileAttrs.PROP.OFFSET.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaGaussOffset")),
            new FieldDescriptor(ProfileAttrs.PROP.AREA.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaGaussArea")),
            new FieldDescriptor(ProfileAttrs.PROP.MEAN.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaGaussMean")),
            new FieldDescriptor(ProfileAttrs.PROP.STDEV.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaGaussStd")),
        };


        /*
         * Initialization
         */

        /**
         * Create a new <code>Gaussian</code> object.
         *
         * @since     Feb 25, 2010
         * @author    Christopher K. Allen
         */
        public GaussFitAttrSet() {
            super(ARR_PFD_HOR, ARR_PFD_VER, ARR_PFD_DIA);
        }

        /**
         * Create a new <code>Gaussian</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public GaussFitAttrSet(DataAdaptor daptSrc) {
            super(ARR_PFD_HOR, ARR_PFD_VER, ARR_PFD_DIA);
            this.update(daptSrc);
        }

        /**
         * Create a new <code>Gaussian</code> object.
         *
         * @param ws        device providing initializing data
         *
         * @throws IllegalArgumentException     general field incompatibility exception
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         *  
         * @since     Feb 25, 2010
         * @author    Christopher K. Allen
         */
        private GaussFitAttrSet(WireScanner ws) throws ConnectionException, GetException, IllegalArgumentException {
            super(ARR_PFD_HOR, ARR_PFD_VER, ARR_PFD_DIA, ws);
        }
    }

    /**
     * Data structure containing the profile data characteristics
     * when modelled as a Double Gaussian signal.
     *
     * @since  Feb 23, 2010
     * @author Christopher K. Allen
     */
    public static class DblGaussFitAttrSet extends ProfileAttrSet {

        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws NoSuchChannelException unable to find channel for given handle 
         * @throws ConnectionException  unable to connect to a field's channel
         * @throws GetException         general CA GET exception while fetch field value
         *
         * 
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static DblGaussFitAttrSet acquire(WireScanner ws) 
        throws NoSuchChannelException, ConnectionException, GetException 
        {
            return new DblGaussFitAttrSet(ws);
        }

        /** 
         * <h2>Array of the (field names,PV descriptor) pair: Horizontal </h2>
         *  
         *  This is where all the data fields are bound to the 
         *  XAL channel handles in the XAL configuration file.
         */
        private static final FieldDescriptor[] ARR_PFD_HOR = {
            new FieldDescriptor(ProfileAttrs.PROP.AMP.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorDblGaussAmp")),
            new FieldDescriptor(ProfileAttrs.PROP.OFFSET.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorDblGaussOffset")),
            new FieldDescriptor(ProfileAttrs.PROP.AREA.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorDblGaussArea")),
            new FieldDescriptor(ProfileAttrs.PROP.MEAN.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorDblGaussMean")),
            new FieldDescriptor(ProfileAttrs.PROP.STDEV.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorDblGaussStd")),
        };

        /** 
         * <h2>Array of the (field names,PV descriptor) pair: Vertical </h2>
         *  
         *  This is where all the data fields are bound to the 
         *  XAL channel handles in the XAL configuration file.
         */
        private static final FieldDescriptor[] ARR_PFD_VER = {
            new FieldDescriptor(ProfileAttrs.PROP.AMP.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerDblGaussAmp")),
            new FieldDescriptor(ProfileAttrs.PROP.OFFSET.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerDblGaussOffset")),
            new FieldDescriptor(ProfileAttrs.PROP.AREA.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerDblGaussArea")),
            new FieldDescriptor(ProfileAttrs.PROP.MEAN.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerDblGaussMean")),
            new FieldDescriptor(ProfileAttrs.PROP.STDEV.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerDblGaussStd")),
        };

        /** 
         * <h2>Array of the (field names,PV descriptor) pair: Diagonal </h2>
         *  
         *  This is where all the data fields are bound to the 
         *  XAL channel handles in the XAL configuration file.
         */
        private static final FieldDescriptor[] ARR_PFD_DIA = {
            new FieldDescriptor(ProfileAttrs.PROP.AMP.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaDblGaussAmp")),
            new FieldDescriptor(ProfileAttrs.PROP.OFFSET.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaDblGaussOffset")),
            new FieldDescriptor(ProfileAttrs.PROP.AREA.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaDblGaussArea")),
            new FieldDescriptor(ProfileAttrs.PROP.MEAN.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaDblGaussMean")),
            new FieldDescriptor(ProfileAttrs.PROP.STDEV.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaDblGaussStd")),
        };


        /*
         * Initialization
         */

        /**
         * Create a new <code>DblGaussianAttrSet</code> object.
         *
         * @since     Feb 25, 2010
         * @author    Christopher K. Allen
         */
        public DblGaussFitAttrSet() {
            super(ARR_PFD_HOR, ARR_PFD_VER, ARR_PFD_DIA);
        }

        /**
         * Create a new <code>DblGaussianAttrSet</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public DblGaussFitAttrSet(DataAdaptor daptSrc) {
            super(ARR_PFD_HOR, ARR_PFD_VER, ARR_PFD_DIA);
            this.update(daptSrc);
        }

        /**
         * Create a new, initialized <code>DblGaussianAttrSet</code> object.
         *
         * @param ws        device providing initializing data
         *
         * @throws IllegalArgumentException     general field incompatibility exception
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         *  
         * @since     Feb 25, 2010
         * @author    Christopher K. Allen
         */
        private DblGaussFitAttrSet(WireScanner ws) throws ConnectionException, GetException, IllegalArgumentException {
            super(ARR_PFD_HOR, ARR_PFD_VER, ARR_PFD_DIA, ws);
        }
    }

    /**
     * Data structure containing the profile data characteristics
     * when modelled as a Double Gaussian signal.
     *
     * @since  Feb 23, 2010
     * @author Christopher K. Allen
     */
    public static class StatisticalAttrSet extends ProfileAttrSet {

        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws NoSuchChannelException unable to find channel for given handle 
         * @throws ConnectionException  unable to connect to a field's channel
         * @throws GetException         general CA GET exception while fetch field value
         *
         * 
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static StatisticalAttrSet acquire(WireScanner ws) 
        throws NoSuchChannelException, ConnectionException, GetException 
        {
            return new StatisticalAttrSet(ws);
        }

        /** 
         * <h2>Array of the (field names,PV descriptor) pair: Horizontal </h2>
         *  
         *  This is where all the data fields are bound to the 
         *  XAL channel handles in the XAL configuration file.
         */
        private static final FieldDescriptor[] ARR_PFD_HOR = {
            new FieldDescriptor(ProfileAttrs.PROP.AMP.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorStatAmp")),
            new FieldDescriptor(ProfileAttrs.PROP.OFFSET.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorStatOffset")),
            new FieldDescriptor(ProfileAttrs.PROP.AREA.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorStatArea")),
            new FieldDescriptor(ProfileAttrs.PROP.MEAN.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorStatMean")),
            new FieldDescriptor(ProfileAttrs.PROP.STDEV.getFieldName(), new XalPvDescriptor(Double[].class, "SigHorStatStd")),
        };

        /** 
         * <h2>Array of the (field names,PV descriptor) pair: Vertical </h2>
         *  
         *  This is where all the data fields are bound to the 
         *  XAL channel handles in the XAL configuration file.
         */
        private static final FieldDescriptor[] ARR_PFD_VER = {
            new FieldDescriptor(ProfileAttrs.PROP.AMP.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerStatAmp")),
            new FieldDescriptor(ProfileAttrs.PROP.OFFSET.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerStatOffset")),
            new FieldDescriptor(ProfileAttrs.PROP.AREA.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerStatArea")),
            new FieldDescriptor(ProfileAttrs.PROP.MEAN.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerStatMean")),
            new FieldDescriptor(ProfileAttrs.PROP.STDEV.getFieldName(), new XalPvDescriptor(Double[].class, "SigVerStatStd")),
        };

        /** 
         * <h2>Array of the (field names,PV descriptor) pair: Diagonal </h2>
         *  
         *  This is where all the data fields are bound to the 
         *  XAL channel handles in the XAL configuration file.
         */
        private static final FieldDescriptor[] ARR_PFD_DIA = {
            new FieldDescriptor(ProfileAttrs.PROP.AMP.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaStatAmp")),
            new FieldDescriptor(ProfileAttrs.PROP.OFFSET.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaStatOffset")),
            new FieldDescriptor(ProfileAttrs.PROP.AREA.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaStatArea")),
            new FieldDescriptor(ProfileAttrs.PROP.MEAN.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaStatMean")),
            new FieldDescriptor(ProfileAttrs.PROP.STDEV.getFieldName(), new XalPvDescriptor(Double[].class, "SigDiaStatStd")),
        };


        /*
         * Initialization
         */

        /**
         * Create a new <code>Statistical</code> signal set object.
         *
         * @since     Feb 25, 2010
         * @author    Christopher K. Allen
         */
        public StatisticalAttrSet() {
            super(ARR_PFD_HOR, ARR_PFD_VER, ARR_PFD_DIA);
        }

        /**
         * Create a new <code>Statistical</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public StatisticalAttrSet(DataAdaptor daptSrc) {
            super(ARR_PFD_HOR, ARR_PFD_VER, ARR_PFD_DIA);
            this.update(daptSrc);
        }

        /**
         * Create a new, initialized <code>Statistical</code> signal set object.
         *
         * @param ws        device providing initializing data
         *
         * @throws IllegalArgumentException     general field incompatibility exception
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         *  
         * @since     Feb 25, 2010
         * @author    Christopher K. Allen
         */
        private StatisticalAttrSet(WireScanner ws) 
        throws ConnectionException, GetException, IllegalArgumentException 
        {
            super(ARR_PFD_HOR, ARR_PFD_VER, ARR_PFD_DIA, ws);
        }
    }


    
    
    /*
     * Wire Scanner Commands
     */


    /**
     * <h4>Wire Scanner Commands</h4>
     * <p>
     * The commands below represent the 
     * possible values for the first argument 
     * in the command array.  It provides the
     * command type while additional arguments provide
     * the command parameters.
     * </p>
     *
     * @since  Aug 21, 2009
     * @author Christopher K. Allen
     */
    public enum CMD {

        /**  No operation */
        NOOP(0),

        /**  Readout settings and set hardware */
        RESET(1),

        /**  Abort the scan (retracts the fork) */
        ABORT(2),

        /**  Start the scan */
        SCAN(3),

        /**  Stop the scan (keeps fork inserted) */
        STOP(4),

        /**  Park the fork */
        PARK(5),

        /**  Acquire data */
        ACQUIRE(6),

        /**  Move the fork a relative position (in micrometers) */
        MOVE(7),

        /**  Turn the brake on or off (normally done automatically) */
        BRAKE(8),

        /**  Load the motion parameters */
        SETUPSCAN(9),

        /** Load the DAQ parameters */
        SETUPDAQ(11),

        /**  Move until a limit switch is hit (takes arguments 1: forward, 0: reverse) */
        HIT(13),

        /**  Locally save data */
        SAVE(15),

        /**  
         * Recreate the traces and profile points 
         * and re-fit to the new profiles (needed after 
         * modifying for example the delay parameter). 
         */
        REANALYZE(17),

        /**  Update a particular PV with acquired data or analyzed results. */
        UPDATE(20);

        
        /** A list of commands which may contain arguments */
        public static CMD[]     ARR_CMDARG = {
            MOVE,
            BRAKE,
            SETUPSCAN,
            SETUPDAQ,
            HIT,
            UPDATE
        };

        /** Command-issuing handle */
        public static final String HANDLE_CMD = "Command";

        /** command result handle */
        public static final String HANDLE_RESULT = "CommandResult";


        /**
         * Returns the integer-value device command
         * associated with the command enumeration
         * constant.
         *
         * @return      device command for constant
         * 
         * @since  Aug 21, 2009
         * @author Christopher K. Allen
         */
        public int getCode() {
            return this.intCmd;
        }
        
        /**
         * Indicates whether or not this command can have arguments.
         *
         * @return  <code>true</code> if this command can have arguments,
         *          <code>false</code> false otherwise.
         * 
         * @since  May 21, 2010
         * @author Christopher K. Allen
         */
        public boolean hasArgument() {
            
            // See if I am in the list of commands with argument
            for (CMD cmd : ARR_CMDARG)
                if ( this.getCode() == cmd.getCode() )
                    return true;
            
            return false;
        }


        /*
         * Private Stuff
         */

        /** device API command value */
        private final int       intCmd;

        /**
         * Create a new <code>CMD</code> constant
         * with the given device command.
         *
         * @param intCmd        device command for constant
         *
         * @since     Aug 21, 2009
         * @author    Christopher K. Allen
         */
        private CMD(int intCmd) {
            this.intCmd = intCmd;
        }
    }

    /**
     * Valid arguments to commands accepting  
     * arguments. 
     *
     * @since  Aug 21, 2009
     * @author Christopher K. Allen
     */
    public enum CMDARG {

        /** Brake command argument: turn the brake off */
        OFF(CMD.BRAKE, 0),

        /** Brake command argument: turn the brake on */
        ON(CMD.BRAKE, 1),

        /** Hit command argument: Reverse */
        REV(CMD.HIT, 0),

        /** Hit command argument: Forward */
        FWD(CMD.HIT, 1),

        /** Load Parameters */
        SETUPDAQ(CMD.SETUPDAQ, 0),

        /** Update command argument 1: None */
        NONE(CMD.UPDATE, 0),

        /** Update command argument 1: Point (live data update) */
        PT(CMD.UPDATE, 1),

        /** Update command argument 1: Trace */
        TRACE(CMD.UPDATE, 2),

        /** Update command argument 1: Profile */
        PROF(CMD.UPDATE, 3);


        /*
         * Operations
         */

        /**
         * Returns the integer-value device command
         * associated with the (CMD,ARG) pair.
         *
         * @return      device command for enumeration constant
         * 
         * @since  Aug 21, 2009
         * @author Christopher K. Allen
         */
        public CMD  getCommand() {
            return this.enmCmd;
        }

        /**
         * Returns the integer-value command argument
         * associated with the (CMD,ARG) pair.
         *
         * @return      command argument for enumeration constant
         * 
         * @since  Aug 21, 2009
         * @author Christopher K. Allen
         */
        public int      getArgCode()   {
            return this.intCode;
        }

        /*
         * Private Stuff
         */

        /** device API command value */
        private final CMD           enmCmd;

        /** device command argument value */
        private final int           intCode;

        /**
         * Create a new <code>CMD_ARG</code> enumeration
         * constant for the given (CMD,ARG) pair.
         *
         * @param enmCmd    the command to which this argument applies
         * @param intCode    the argument code
         *
         * @since     Aug 21, 2009
         * @author    Christopher K. Allen
         */
        private CMDARG(CMD enmCmd, int intCode) {
            this.enmCmd = enmCmd;
            this.intCode = intCode;
        }
    }

    
    /**
     * A class-level representation of a wire-scanner command.
     * This class must also be used for commands with arguments.
     * Specifically, the command argument is packaged together
     * with a <code>{@link CMD}</code> instance to form a 
     * complete command. 
     *
     * @since  May 21, 2010
     * @author Christopher K. Allen
     */
    public static class CmdPck {
        
        
        
        /*
         * Local Attributes
         */
        
        /** The device command */
        private final CMD       cmd;
        
        /** The list of command arguments */
        private final Number[]  args;
        
        
        /*
         * Initialization
         */
        
        /**
         * Create a new <code>Cmd</code> object for
         * the given <code>CMD</code> and no
         * arguments.
         *
         * @param cmd   the wire scanner command
         *
         * @since     May 21, 2010
         * @author    Christopher K. Allen
         */
        public CmdPck(CMD cmd) {
            this(cmd, new Number[] {});
        }
        
        /**
         * Create a new <code>Cmd</code> object for
         * the given <code>CMD</code> and given 
         * arguments.
         *
         * @param cmd   the wire scanner command
         * @param args  the arguments of the command (may be empty)
         *
         * @since     May 21, 2010
         * @author    Christopher K. Allen
         */
        public CmdPck(CMD cmd, Number... args) {
            this.cmd  = cmd;
            this.args = args;
        }
        
        /**
         * Create a new <code>Cmd</code> object for the 
         * given <code>{@link CMD}</code> and the given
         * <code>{@link CMDARG}</code> argment and any addition
         * arguments <var>addargs</var>.  
         *
         * @param cmd   the wire scanner command
         * @param arg   pre-defined command argument
         * @param addargs  additional arguments of the command (may be empty)
         *
         * @since     May 24, 2010
         * @author    Christopher K. Allen
         */
        public CmdPck(CMD cmd, CMDARG arg, Number... addargs) {
            this.cmd = cmd;
            
            this.args = new Number[1 + addargs.length];
            this.args[0] = arg.getArgCode();
            int i = 1;
            for (Number num : addargs) {
                this.args[i] = num;
                i++;
            }
        }
        
        /*
         * Operations
         */
        
        /**
         * Return the number of arguments in this command.
         * Currently there is a maximum of three.
         *
         * @return Number of command arguments 
         * 
         * @since  May 21, 2010
         * @author Christopher K. Allen
         */
        public int getArgumentCount() {
            if (this.args == null)
                return 0;
            
            return args.length;
        }
        
        /**
         * Returns the command constant of this
         * command instance, that is, an element of
         * the enumeration <code>CMD</code>.  
         *
         * @return      Command code enumeration for this command      
         * 
         * @since  May 21, 2010
         * @author Christopher K. Allen
         */
        public CMD getCommand() {
            return this.cmd;
        }
        
        /**
         * Returns the argument of this command.  
         * This method is actually a shortcut for the 
         * method call
         * <br/>
         * <br/>
         *  &nbsp; &nbsp; <code>{@link #getArgument(int)}</code>
         * <br/>
         * <br/>
         * for the argument of 0.  Specially, use this method
         * if there is only one argument to the command.
         * 
         * @return      the command argument
         * 
         * @since  May 21, 2010
         * @author Christopher K. Allen
         */
        public Number getArgument() {
            return this.getArgument(0);
        }
        
        /**
         * Return the command argument at the given index.
         * Note that the argument index value must be 
         * strictly less than the argument count. 
         *
         * @param index         the command argument index
         * 
         * @return              the command argument at the given index
         * 
         * @throws IllegalArgumentException     the given index is too large for the
         *                                      number of arguments in this command     
         * 
         * @since  May 21, 2010
         * @author Christopher K. Allen
         */
        public Number getArgument(int index) throws IllegalArgumentException {
            if (index >= this.getArgumentCount())
                throw new IllegalArgumentException("Index is larger than argument count");
            
            return this.args[index];
        }

        /**
         * Returns a string representation of the command with arguments. 
         *
         * @since       May 25, 2010
         * @author  Christopher K. Allen
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuffer        bufText = new StringBuffer();
            
            bufText.append("CMD=" + this.getCommand());
            bufText.append("Args=(");
            for (int i=0; i<this.getArgumentCount(); i++)
                bufText.append( "," + this.getArgument(i) );
            bufText.append(")");
            
            return bufText.toString();
        }
        
    }
    
    





    /*
     * Initialization
     */

    /**
     * Create a new <code>WireScanner</code> object.
     *
     * @param nodeID
     *
     * @since     Jun 18, 2009
     * @author    Christopher K. Allen
     */
    public WireScanner( final String nodeID )   { 
        super( nodeID ); 
    }   



    /*
     * Node Properties
     */

    /** Get the device type  */
    @Override
    public String getType()  { return s_strType; }


    /** Derived class may furnish a unique software type */
    @Override
    public String getSoftType() {
        return "Version 2.0.0";
    }




    /*
     * Operations
     */



    /**
     * Issue a wire scanner command with no arguments.
     * 
     * @param cmd       wire scanner command
     * 
     * @throws ConnectionException      unable to find channel 
     * @throws PutException             unable to set channel value
     * @throws InterruptedException     command buffer reset thread interrupted
     */
    public void runCommand(final CMD cmd) throws  ConnectionException, PutException, InterruptedException 
    {
        // Issue command
        Channel channel = getAndConnectChannel( CMD.HANDLE_CMD );
        channel.putVal( cmd.getCode() );

        // Reset the on-board command buffer to avoid the IOC echo (a kluge)
        Thread.sleep(INT_CMD_LATCY);
        channel.putVal( CMD.NOOP.getCode() );
    }

    /**
     * Issue a wire scanner command with or without arguments arguments.  
     * The command and argument list is packed in the argument of type
     * <code>{@link WireScanner.CmdPck}</code>
     * Here we are packing the command-argument vector as an array of type
     * <code>double</code>.
     * 
     * @param cmdPck       packaged wire scanner command with optional arguments
     * 
     * @throws IllegalArgumentException argument/command inconsistency  
     * @throws ConnectionException      unable to find channel 
     * @throws PutException             unable to set channel value
     * @throws InterruptedException     command buffer reset thread interrupted
     */
    public void runCommand(final CmdPck cmdPck) 
        throws IllegalArgumentException, ConnectionException, PutException, InterruptedException 
    {
        // Check for the zero-argument case
        if (cmdPck.getArgumentCount() == 0)
            this.runCommand( cmdPck.getCommand() );

        // Allocate command code array 
        int             cntArgs  = cmdPck.getArgumentCount();
        double[]        arrCodes = new double[cntArgs + 1];

        // Set the command value (index 0 value)
        arrCodes[0] = cmdPck.getCommand().getCode();

        // Set the command argument values in the command code array
        int     i = 1;  // index of argument
        for (int index=0; index<cntArgs; index++)  {
            Number numArg = cmdPck.getArgument(index);
            
            // Add command argument to command code array
            arrCodes[i++] = numArg.doubleValue();
        }

        // Issue command
        Channel channel = getAndConnectChannel( CMD.HANDLE_CMD );
        channel.putVal( arrCodes );

        // Reset the on-board command buffer to avoid the IOC echo (a kluge)
        Thread.sleep(INT_CMD_LATCY);
        channel.putVal( CMD.NOOP.getCode() );
    }
    
    


    /** 
     * Returns the command result code(s).  These are integer valued
     * codes for the result of a previous issued device command.  See
     * the wire scanner documentation to interpret the particular
     * resultant code.
     * 
     * @return command result   array of integer codes for a command result.
     * @throws ConnectionException 
     * @throws GetException 
     */
    public int[] getCommandResult() throws ConnectionException, GetException {
        //        final Channel channel = getAndConnectChannel( COMMAND_RESULT_HANDLE );
        final Channel channel = getAndConnectChannel( CMD.HANDLE_RESULT );

        return channel.getArrInt();
    }

    /**
     * <p>
     * Setup a value monitor on the given process variable (i.e.,
     * using its handle).  The monitor events are
     * sent to the <code>IEventSink</code> object provided.
     * </p>
     * <p>
     * One can specify the event type which fires the monitor using
     * the argument <arg>intEvtType</arg>.  Any combination of the following
     * event types can be specified with a logical OR operation:
     * <br/>
     * <br/> &nbsp; <code>Monitor.VALUE</code> - fire upon PV value change
     * <br/> &nbsp; <code>Monitor.LOG  </code> - 
     * <br/> &nbsp; <code>Monitor.ALARM</code> - fire upon PV alarm value
     * <br/>
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
    public Monitor      createMonitor(XalPvDescriptor.IPvDescriptor pvdFld, IEventSinkValue snkEvents, int ...mskEvtType)
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
     * Test the connections in all the channels of the given parameter set for this
     * accelerator device.  The test will wait up to the given length
     * of time before declaring failure.
     *
     * @param dblTmOut      time out before test fails (in seconds)
     * 
     * @return              <code>true</code> if all connections were successful,
     *                      <code>false</code> if not all connection were made within given time
     *                      
     * @throws BadStructDefinition      the data structure is not defined properly (bad PV Descriptor)
     *
     * @author Christopher K. Allen
     * @since  Feb 4, 2011
     */
    public boolean  testConnection(Class<? extends ParameterSet> clsScada, double dblTmOut) 
        throws BadStructDefinition
    {
        boolean bolResult = ScadaPacket.testConnection(clsScada, this, dblTmOut);
        
        return bolResult;
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
    public void configureHardware(WireScanner.ParameterSet datPvFlds) 
        throws PutException, ConnectionException 
    {
        datPvFlds.setHardwareValues(this);
    }

}