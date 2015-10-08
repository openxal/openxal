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

import xal.ca.BadChannelException;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.IEventSinkValue;
import xal.ca.Monitor;
import xal.ca.MonitorException;
import xal.ca.PutException;
import xal.tools.data.DataAdaptor;
import xal.smf.Accelerator;
import xal.smf.NoSuchChannelException;
import xal.smf.TimingCenter;
import xal.smf.impl.profile.ASignal;
import xal.smf.impl.profile.ASignalAttrs;
import xal.smf.impl.profile.ParameterSet;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.impl.profile.SignalAttrSet;
import xal.smf.impl.profile.SignalSet;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.smf.scada.AScada;
import xal.smf.scada.BadStructException;
import xal.smf.scada.BatchConnectionTest;
import xal.smf.scada.XalPvDescriptor;
import xal.smf.scada.ScadaAnnotationException;
import xal.smf.scada.ScadaFieldDescriptor;
import xal.smf.scada.ScadaFieldMap;

import java.util.Collection;
import java.util.MissingResourceException;
import java.util.concurrent.RejectedExecutionException;


/**
 * <h3>Wire Scanner Hardware</h3>
 * <p>
 * Represents a wire scanner diagnostic device, in particular
 * those devices installed at the SNS facility.  This class
 * implements the new profile device API at SNS.  The previous
 * device API interfaced with the <tt>SMF</tt> class
 * <code>{@link ProfileMonitor}</code>.
 * </p>
 * <h3>NOTES:</h3>
 * <p>
 * &middot; After a command is issued to a wire scanner device the command
 * buffer must be cleared back to zero.  This is due to a "quirk"
 * in the current EPICS installation where command signal echos
 * occur in the network.  To prevent the command from being issued
 * multiple times it is necessary to clear the command buffer after
 * issuing a command.
 * <br>
 * &middot; The time between the issuing of a command and the clearing
 * of the command buffer is call the <i>command latency</i>.
 * <br>
 * &middot; Currently the command buffer is not reset - the new controller
 * software appears to correct the EPICS quirk. 
 * <br>
 * &middot; This is a refactoring of the original <code>WireScanner</code>
 * class that generalizes to the <code>WireHarp</code>
 * </p>  
 * 
 * <p>
 * <b>Ported from XAL on Jul 21, 2014. (Over-wrote old WireScanner 
 * implementation.)</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 *
 * @since  Mar 26, 2009 Version 1.0
 * @version Mar, 2010 Version 2.1
 * @version Oct, 2011 Version 2.2
 * @version Feb, 2013 Version 3.0
 * @version Mar, 2014 Version 3.1
 * 
 * @author Tom Pelaia
 * @author Christopher K. Allen
 * 
 * @see xal.smf.impl.ProfileMonitor
 */
public class WireScanner extends ProfileDevice {


    
    /*
     * Wire Scanner Command Classes
     */

    /**
     * <h3>Wire Scanner Commands</h3>
     * <p>
     * The commands below represent the possible values for the first argument 
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

        /**  Start the scan using the scan parameters specified by the user */
        XPRT_SCAN(3),

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
        UPDATE(20),

        /** Performs a (simple) scan using predefined scan parameters within the device controller */
        EZ_SCAN(21);


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
        public static final String HANDLE_CMD = "Command"; //$NON-NLS-1$

        /** command result handle */
        public static final String HANDLE_RESULT = "CommandResult"; //$NON-NLS-1$


        /**
         * Returns the integer-value device command associated with the command enumeration
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
         * <br>
         * <br>
         *  &nbsp; &nbsp; <code>{@link #getArgument(int)}</code>
         * <br>
         * <br>
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
                throw new IllegalArgumentException("Index is larger than argument count"); //$NON-NLS-1$

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

            bufText.append("CMD=" + this.getCommand()); //$NON-NLS-1$
            bufText.append("Args=("); //$NON-NLS-1$
            for (int i=0; i<this.getArgumentCount(); i++)
                bufText.append( "," + this.getArgument(i) ); //$NON-NLS-1$
            bufText.append(")"); //$NON-NLS-1$

            return bufText.toString();
        }
    }

    
    /*
     * Global Constants
     */
    
    /** device type */
    public static final String s_strType = "WS"; //$NON-NLS-1$
    
    /** software type for the Wire Scanner class */
	static final public String SOFTWARE_TYPE = "Version 2.0.0"; //$NON-NLS-1$
	
	/** Hardware type for the WireScanner class */
	static final public String HARDWARE_TYPE = "wirescanner"; //$NON-NLS-1$

	
    
    /** handle for the horizontal sigma Gauss channel */
    static final public String HORIZONTAL_SIGMA_GAUSS_HANDLE = "SigHorGaussStd"; //$NON-NLS-1$
    
    /** handle for the vertical sigma Gauss channel */
    static final public String VERTICAL_SIGMA_GAUSS_HANDLE = "SigVerGaussStd"; //$NON-NLS-1$

    
    /** The data processing window offset caused by (analog) filtering   */
    private static final double DBL_FILTER_OFFSET = 3.0e-6;

    
    
    
    /*
     * SMF Requirements
     */
    

    /**
     * Register the hardware device types that this class
     * recognizes.
     */
    static {
		ElementTypeManager.defaultManager().registerTypes( WireScanner.class, s_strType, HARDWARE_TYPE );
    }


    
    /*
     * Wire Scanner Configuration Classes (Data Structures)
     */
    
    /**
     * Data structure containing the sampling parameters
     * for data acquisition. 
     *
     * @since  Dec 16, 2009
     * @author Christopher K. Allen
     */
    public static class SmplConfig extends ParameterSet {

       

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
        public static SmplConfig acquire(ProfileDevice ws) 
            throws ConnectionException, GetException  
        {
            return new SmplConfig(ws);
        }


        
        /*
         * SCADA Fields 
         */

        /** The digitizer sampling rate (in Hz) */
        @AScada.Field(
                      type   = int.class, 
                      ctrl   = true, 
                      hndRb  = "DaqCfgScanRateRb", 
                      hndSet = "DaqCfgScanRateSet"
                      )
        public int              sampleRate;

        
        /** The sampling window [0.1,2] msec */
        @AScada.Field(
                type    = double.class,
                ctrl    = true,
                hndRb   = "DaqCfgWindowRb",
                hndSet  = "DaqCfgWindowSet" 
                )
        public double           sampleWindow;

        
        /** The amplifier signal gain {0, 1, 2} */
        @AScada.Field(
                type   = int.class,
                ctrl   = true,
                hndRb  = "DaqCfgGainRb",
                hndSet = "DaqCfgGainSet"
                )
        public int              signalGain;

        
        /** The timeout period for a no-beam exception [20,300] sec */
        @AScada.Field(
                type   = int.class,
                ctrl     = true,
                hndRb  = "DaqCfgTimeoutRb",
                hndSet = "DaqCfgTimeoutSet"
                )
        public int              signalTimeout;


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
        public SmplConfig() {
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
        public SmplConfig(DataAdaptor daptSrc) {
            super(daptSrc);
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
        public SmplConfig(ProfileDevice ws) throws ConnectionException, GetException {
            super(ws);
        }
        
        /*
         * Operations
         */
        
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
    }


    /**
     * This class is a data structure for managing scan actuator
     * configuration data parameters.  
     *
     * @since  Nov 9, 2009
     * @author Christopher K. Allen
     * 
     */
    public static class ActrConfig extends ParameterSet {

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
        public static ActrConfig aquire(ProfileDevice ws) throws ConnectionException, GetException 
        {
            return new ActrConfig(ws);
        }


        /** 
         * The initial (actuator) speed of the actuator in mm/sec.  
         * This is the maximum speed obtained by the actuator
         * en route to the location where the data acquisition
         * begins. 
         */
        @AScada.Field(
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
        @AScada.Field(
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
        @AScada.Field(
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
        @AScada.Field(
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
        @AScada.Field(
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
        @AScada.Field(
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
        @AScada.Field(
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
        @AScada.Field(
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
        @AScada.Field(
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
        @AScada.Field(
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
            super(daptSrc);
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
        public ActrConfig(ProfileDevice ws) throws ConnectionException, GetException {
            super(ws);
        }
    }

    /**
     * This class is a data structure for managing scan 
     * configuration parameters.  
     *
     * @since  Nov 9, 2009
     * @author Christopher K. Allen
     * 
     */
    public static class ScanConfig extends ParameterSet implements IProfileDomain {

        /* 
         * Global Constants 
         */
        
        /** the numeric value 1/2<sup>-1/2</sup> */
        public static final double  DBL_INV_SQRT_2 = 1./Math.sqrt(2.0);
        
        
        /*
         * Global Attributes
         */
        
        /** The map between structure field names and there corresponding descriptors */
        public static final ScadaFieldMap   FLD_MAP = new ScadaFieldMap(ScanConfig.class);
        

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
        public static ScanConfig acquire(ProfileDevice ws) 
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
        @AScada.Field(
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
        @AScada.Field(
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
        @AScada.Field(
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
        @AScada.Field(
                type    = int.class,
                ctrl    = true,
                hndRb   = "ScanCfgStepPulsesRb",
                hndSet  = "ScanCfgStepPulsesSet"
        )
        public int         pulseCount;

        /** 
         * The maximum position of the scan considering
         * the current values of <code>posInit</code>, <code>stepLength</code>,
         * and <code>stepCount</code>
         */
        @AScada.Field(
                type    = double.class,
                ctrl    = false,
                hndRb   = "ScanCfgScanLngRb"
        )
        public double     lngScan;
        
        /**
         * The stroke length of the device actuator.
         * Thus, this is the maximum possible distance the
         * wire can travel while taking data.  It is a mechanical
         * limitation. 
         * <br> <br>
         * The product of <code>stepLength</code> &times; 
         * <code>stepCount</code> must be less than this value. 
         * This is a read-only quantity.
         */
        @AScada.Field(
                type   = double.class,
                ctrl   = false,
                hndRb  = "ScanCfgStrokeLngRb"
        )
        public double      lngStroke;

        /**
         * Error flag indicating that the scan parameters are inconsistent,
         * that is, they do not describe a viable scan.
         */
        @AScada.Field( 
                type    = int.class,
                ctrl    = false,
                hndRb   = "ScanCfgScanOutOfRngRb"
        )
        public int      errScanRng;
        
        
        /*
         * Operations
         */


        /*
         * Initialization
         */

        /**
         * Create a new, uninitialized <code>ScanConfig</code> object.
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
            this.lngScan    = 0.0;
            this.lngStroke  = 0.0;
            this.errScanRng = 0;
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
            super(daptSrc);
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
        public ScanConfig(ProfileDevice ws) throws ConnectionException, GetException {
            super(ws);
        }

        /*
         * Convenience Methods
         */
        
        /**
         * Returns the stroke length of the actuator arm for a scan.  This is the
         * total physical distances traveled by the actuator arm during a scan
         * and is <b>not</b> necessarily the abscissa of the measured data.  
         * This value, <i>L</i>, is typically given by the formula
         * <br/>
         * <br/>
         * &nbsp; &nbsp; <i>L</i> = <i>N</i><sub>steps</sub> &Delta;<i>L</i>
         * <br/>
         * <br/>
         * where <i>N</i><sub>steps</sub> is the number
         * of scan steps and &Delta;<i>L</i> is the step length.  
         * 
         * @return      movement distance <i>L</i> of scan actuator
         *
         * @author Christopher K. Allen
         * @since  Oct 8, 2015
         */
        public double getScanlLength() {

            // Get the total stroke length of the actuator arm
            double  dblLen = this.stepCount * this.stepLength;

            return dblLen;
        }
        
        
        /*
         * IProfileDomain Interface
         */
        
        /**
         *
         * @see xal.smf.impl.profile.ProfileDevice.IProfileDomain#getSampleCount(xal.smf.impl.profile.ProfileDevice.ANGLE)
         *
         * @author Christopher K. Allen
         * @since  Apr 24, 2014
         */
        @Override
        public int getSampleCount(ANGLE angle) {
            return stepCount;
        }
        
        /**
         * @see xal.smf.impl.profile.ProfileDevice.IProfileDomain#getSamplePositions(xal.smf.impl.profile.ProfileDevice.ANGLE)
         *
         * @author Christopher K. Allen
         * @since  Apr 24, 2014
         */
        @Override
        public double[] getSamplePositions(ANGLE angle) {
            int         cntSmp = this.getSampleCount(angle);
            double      dblDel = this.getIntervalLength(angle)/(cntSmp - 1);
            
            double      dblPos = this.getInitialPosition(angle);
            double[]    arrPos = new double[cntSmp];
            for (int i=0; i<cntSmp; i++) {
                arrPos[i] = dblPos;
                
                dblPos += dblDel;
            }
            
            return arrPos;
        }

        /**
         *
         * @see xal.smf.impl.profile.ProfileDevice.IProfileDomain#getInitialPosition(xal.smf.impl.profile.ProfileDevice.ANGLE)
         *
         * @author Christopher K. Allen
         * @since  Apr 24, 2014
         */
        @Override
        public double getInitialPosition(ANGLE angle) {
            
            // Get the initial position of the scanner arm
            double  dblPosInit = this.posInit;
            
            // scale it in horizontal and vertical planes if necessary
//            switch (angle) {
//            case HOR: dblPosInit *= DBL_INV_SQRT_2; break;
//            case VER: dblPosInit *= DBL_INV_SQRT_2; break;
//            default: break;
//            }
            
            return dblPosInit;
        }

        /**
         *
         * @see xal.smf.impl.profile.ProfileDevice.IProfileDomain#getIntervalLength(xal.smf.impl.profile.ProfileDevice.ANGLE)
         *
         * @author Christopher K. Allen
         * @since  Apr 24, 2014
         */
        @Override
        public double getIntervalLength(ANGLE angle) {
            
            // Get the total stroke length of the actuator arm
            double  dblLen = this.stepCount * this.stepLength;
            
            switch (angle) {
            case HOR: dblLen *= DBL_INV_SQRT_2; break;
            case VER: dblLen *= DBL_INV_SQRT_2; break;
            default: break;
            }
            
            return dblLen;
        }
    }


    /**
     * Data structure containing the configuration parameters
     * for the on-board data processing and analysis. 
     *
     * @since  Dec 16, 2009
     * @author Christopher K. Allen
     */
    public static class PrcgConfig extends ParameterSet {

        /*
         * Global Attributes
         */
        /** Map of field names to SCADA field Descriptors */
        final public static ScadaFieldMap  FLD_MAP = new ScadaFieldMap(WireScanner.PrcgConfig.class);
        
        
        /*
         * Global Methods
         */
        

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
        public static PrcgConfig acquire(ProfileDevice ws) 
            throws ConnectionException, GetException    {
            return new PrcgConfig(ws);
        }


        /*
         * Data structure fields
         */
        
        /** Inversion of acquired profile signal {-1,+1} */
        @AScada.Field( 
                type    = int.class,
                ctrl    = true,
                hndRb   = "PrcgCfgInvertRb",
                hndSet  = "PrcgCfgInvertSet"
        )
        public int                     sigInv;

        
        /** Time position to start signal averaging */
        @AScada.Field(
                type    = double.class,
                ctrl    = true,
                hndRb   = "PrcgCfgAvgBeginRb",
                hndSet  = "PrcgCfgAvgBeginSet"
        )
        public double                   avgBgn;

        
        /** The size (in time) of the time averaging window */
        @AScada.Field(
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
            super(daptSrc);
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
        public PrcgConfig(ProfileDevice ws) throws ConnectionException, GetException {
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
    public static class TrgConfig extends ParameterSet {

        /*
         * Global Attributes
         */
        
        /** Map of field names to SCADA field Descriptors */
        public final static ScadaFieldMap  FLD_MAP = new ScadaFieldMap(WireScanner.TrgConfig.class);
        
        
        

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
        public static TrgConfig acquire(ProfileDevice ws) 
            throws ConnectionException, GetException  
        {
            return new TrgConfig(ws);
        }


        /*
         * Data Fields
         */

        
        /** Time delay between beam-on event and actual DAQ start. 
         * NOTE: this value may be negative indicating DAQ before the beam-on event 
         * (thus, sampling only noise) */
        @AScada.Field( 
                type   = double.class,
                ctrl   = true,
                hndRb  = "TrgCfgDelayRb",
                hndSet = "TrgCfgDelaySet"
        )
        public double           delay;

        
        /** Event code to trigger data acquisition - values 0,...,256 */
        @AScada.Field(
                type   = int.class,
                ctrl   = true,
                hndRb  = "TrgCfgTrigEventRb",
                hndSet = "TrgCfgTrigEventSet"
        )
        public int              event;



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
            super(daptSrc);
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
        public TrgConfig(ProfileDevice ws) throws ConnectionException, GetException {
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

        
        /*
         * Global Attributes
         */
        
        /** Map of field names to field SCADA descriptors for this structure */
        public final static ScadaFieldMap   FLD_MAP = new ScadaFieldMap(WireScanner.DevStatus.class);
        
        
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
        public static DevStatus acquire(ProfileDevice ws) 
            throws ConnectionException, GetException  
        {
            return new DevStatus(ws);
        }


        /*
         * Status Parameters
         */

        /**
         * Alarm flag for wire signal; it indicates a
         * saturation condition.
         * <br>
         * <br>
         *  status: 0=OK, 1=Saturated
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatAlarmSgnlRb"
        )
        public int      almSgnl;
        
        /** 
         * forward limit switch activated 
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatFwdLimitRb"
        )
        public int      limFwd;
        
        /** 
         * reverse limit switch activated 
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatRevLimitRb"
        )
        public int      limRev;
        
        /**
         * Scan out of range error
         */
        @AScada.Field( 
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatScanOutOfRngRb"
        )
        public  int     errScanRng;

        /** 
         * Horizontal wire damage
         * <br>
         * <br>
         *  status: 0=OK, 1=MPS trip
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatHorWireDmgRb"
        )
        public int      dmgHor;

        /** 
         * Vertical wire damage 
         * <br>
         * <br>
         *  status: 0=OK, 1=MPS trip
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatVerWireDmgRb"
        )
        public int      dmgVer;

        /** 
         * Diagonal wire damage
         * <br>
         * <br>
         *  status: 0=OK, 1=Damage 
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatDiaWireDmgRb"
        )
        public int      dmgDia;

        /**
         * Timing error flag.  General error in the device controller
         * timing hardware.
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatAlarmTmgRb"
        )
        public  int     almTmg;

        /** 
         * MPS 0 trip 
         * <br>
         * <br>
         *  status: 0=OK, 1=MPS trip
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatMps0Rb"
        )
        public int      errMps0;

        /** 
         * MPS 1 trip 
         * <br>
         * <br>
         *  status: 0=OK, 1=MPS trip
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatMps1Rb"
        )
        public int      errMps1;

        /** 
         * General power supply error 
         * <br>
         * <br>
         *  status: 0=OK, 1=Error condition
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatPowerSupplyRb"
        )
        public int      errPs;
        
        /** 
         * General error during scan 
         * <br>
         * <br>
         *  status: 0=OK, 1=Error condition
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatScanErrorRb"
        )
        public int      errScan;

        /** 
         * Actuator collision error. Motion was disabled 
         * to prevent collision with another device. 
         * <br>
         * <br>
         *  status: 0=OK, 1=collision detected
         */
        @AScada.Field( 
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
        @AScada.Field( 
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatScanSeqIdRb"
        )
        public int      idScan;

        /** 
         * Movement state of the wire.
         * <br>
         * <br>
         * Values: 0=stationary, 1=moving, 2=failure
         */
        @AScada.Field( 
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatMotionRb"
        )
        public int      mvtStatus;
        
        /** 
         * Maximum actuator excursion during a scan 
         */
        @AScada.Field(
                type    = double.class,
                ctrl    = false,
                hndRb   = "StatScanStrokeRb"
        )
        public double   mvtMax;

        /** 
         * Current position of the wire 
         */
        @AScada.Field(
                type    = double.class,
                ctrl    = false,
                hndRb   = "StatWirePosRb"
        )
        public double   wirePos;

        /** 
         * Current speed of the wire 
         */
        @AScada.Field(
                type    = double.class,
                ctrl    = false,
                hndRb   = "StatWireSpeedRb"
        )
        public double   wireVel;
        
        /** 
         * The maximum stroke length of the actuator 
         */
        @AScada.Field(
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
        public DevStatus(ProfileDevice ws) throws ConnectionException, GetException {
            super(ws);
        }
    }

    
    /*
     * Processed Signal Attributes
     */

    /**
     * Data structure containing the profile data characteristics
     * when modeled as a Gaussian signal.
     *
     * @since  Feb 23, 2010
     * @author Christopher K. Allen
     */
    @ASignalAttrs.ASet(
            attrHor = @ASignalAttrs( hndAmpRb = "SigHorGaussAmp", hndAreaRb = "SigHorGaussArea", hndMeanRb = "SigHorGaussMean", hndOffsetRb = "SigHorGaussOffset", hndStdevRb = "SigHorGaussStd"), 
            attrVer = @ASignalAttrs( hndAmpRb = "SigVerGaussAmp", hndAreaRb = "SigVerGaussArea", hndMeanRb = "SigVerGaussMean", hndOffsetRb = "SigVerGaussOffset", hndStdevRb = "SigVerGaussStd"),
            attrDia = @ASignalAttrs( hndAmpRb = "SigDiaGaussAmp", hndAreaRb = "SigDiaGaussArea", hndMeanRb = "SigDiaGaussMean", hndOffsetRb = "SigDiaGaussOffset", hndStdevRb = "SigDiaGaussStd")
            )
    public static class GaussFitAttrSet extends SignalAttrSet {

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
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static GaussFitAttrSet acquire(ProfileDevice ws) 
            throws NoSuchChannelException, ConnectionException, GetException {
            return new GaussFitAttrSet(ws);
        }


        /*
         * Initialization
         */

        /**
         * Create a new <code>GaussFitAttrSet</code> object.
         *
         * @since     Feb 25, 2010
         * @author    Christopher K. Allen
         */
        public GaussFitAttrSet() {
            super();
        }

        /**
         * Create a new <code>GaussFitAttrSet</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public GaussFitAttrSet(DataAdaptor daptSrc) {
            super();
            this.update(daptSrc);
        }

        /**
         * Create a new <code>Gaussian</code> object.
         *
         * @param ws        device providing initializing data
         *
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         *  
         * @since     Feb 25, 2010
         * @author    Christopher K. Allen
         */
        private GaussFitAttrSet(ProfileDevice ws) throws ConnectionException, GetException {
            super(ws);
        }
    }

    /**
     * Data structure containing the profile data characteristics
     * when modelled as a Double Gaussian signal.
     *
     * @since  Feb 23, 2010
     * @author Christopher K. Allen
     */
    @ASignalAttrs.ASet(
            attrHor = @ASignalAttrs( hndAmpRb = "SigHorDblGaussAmp", hndAreaRb = "SigHorDblGaussArea", hndMeanRb = "SigHorDblGaussMean", hndOffsetRb = "SigHorDblGaussOffset", hndStdevRb = "SigHorDblGaussStd"), 
            attrVer = @ASignalAttrs(hndAmpRb = "SigVerDblGaussAmp", hndAreaRb = "SigVerDblGaussArea", hndMeanRb = "SigVerDblGaussMean", hndOffsetRb = "SigVerDblGaussOffset", hndStdevRb = "SigVerDblGaussStd"),
            attrDia = @ASignalAttrs(hndAmpRb = "SigDiaDblGaussAmp", hndAreaRb = "SigDiaDblGaussArea", hndMeanRb = "SigDiaDblGaussMean", hndOffsetRb = "SigDiaDblGaussOffset", hndStdevRb = "SigDiaDblGaussStd")
            )
    public static class DblGaussFitAttrSet extends SignalAttrSet {

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
         * @throws IllegalAccessException   a needed field in the annotation is not publicly accessible   
         * @throws IllegalArgumentException this class is not annotated with <code>ASgnlAttrSet</code>
         *
         * 
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static DblGaussFitAttrSet acquire(ProfileDevice ws) throws ConnectionException, GetException { 
            return new DblGaussFitAttrSet(ws);
        }


        /*
         * Initialization
         */

        /**
         * Create a new <code>DblGaussianAttrSet</code> object.
         *
         * @since     Feb 25, 2010
         * @author    Christopher K. Allen
         */
        public DblGaussFitAttrSet()  {
            super();
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
            super();
            this.update(daptSrc);
        }

        /**
         * Create a new, initialized <code>DblGaussianAttrSet</code> object.
         *
         * @param ws        device providing initializing data
         *
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         *  
         * @since     Feb 25, 2010
         * @author    Christopher K. Allen
         */
        private DblGaussFitAttrSet(ProfileDevice ws) throws ConnectionException, GetException {
            super(ws);
        }
    }

    /**
     * Data structure containing the profile data characteristics
     * when modelled as a Double Gaussian signal.
     *
     * @since  Feb 23, 2010
     * @author Christopher K. Allen
     */
    @ASignalAttrs.ASet(
            attrHor = @ASignalAttrs(hndAmpRb = "SigHorStatAmp", hndAreaRb = "SigHorStatArea", hndMeanRb = "SigHorStatMean", hndOffsetRb = "SigHorStatOffset", hndStdevRb = "SigHorStatStd"),
            attrVer = @ASignalAttrs(hndAmpRb = "SigVerStatAmp", hndAreaRb = "SigVerStatArea", hndMeanRb = "SigVerStatMean", hndOffsetRb = "SigVerStatOffset", hndStdevRb = "SigVerStatStd"),
            attrDia = @ASignalAttrs(hndAmpRb = "SigDiaStatAmp", hndAreaRb = "SigDiaStatArea", hndMeanRb = "SigDiaStatMean", hndOffsetRb = "SigDiaStatOffset", hndStdevRb = "SigDiaStatStd")
            )
    public static class StatisticalAttrSet extends SignalAttrSet {

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
        public static StatisticalAttrSet acquire(ProfileDevice ws) 
            throws NoSuchChannelException, ConnectionException, GetException 
        {
            return new StatisticalAttrSet(ws);
        }


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
            super();
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
            super();
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
        private StatisticalAttrSet(ProfileDevice ws) 
            throws ConnectionException, GetException, IllegalArgumentException 
        {
            super(ws);
        }
    }



    /*
     * Profile Data Structures
     */


    /**
     * Data structure contain the profile data available
     * during data acquisition in a point-by-point fashion 
     * (i.e., as the scan progress).
     *
     * @since   Nov 13, 2009
     * @version Mar 05, 2013
     * @author  Christopher K. Allen
     */
    @ASignal.ASet( 
            sigHor = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatHorLivePtPositions", hndValRb = "DatHorLivePtSignal", hndNseAvgRb = "DatHorNoiseAvg", hndNseVarRb = "DatHorNoiseStd"),
            sigVer = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatVerLivePtPositions", hndValRb = "DatVerLivePtSignal", hndNseAvgRb = "DatVerNoiseAvg", hndNseVarRb = "DatVerNoiseStd"),
            sigDia = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatDiaLivePtPositions", hndValRb = "DatDiaLivePtSignal", hndNseAvgRb = "DatDiaNoiseAvg", hndNseVarRb = "DatDiaNoiseStd") 
            )
    public static class DataLivePt extends SignalSet {

        
//        /** The static list of field descriptors for this class */
//        private static final    List<ScadaFieldDescriptor>  LST_FLD_DESCRS = new ScadaFieldList(DataLivePt.class);
//        
//        
//        /**
//         * Returns the set of field descriptors for all the (field, channel)
//         * pairs used by this class.
//         *
//         * @return  set of field descriptors used by this class.
//         *
//         * @throws ScadaAnnotationException the <code>ADaqProfile</code> annotations are incomplete
//         * 
//         * @author Christopher K. Allen
//         * @since  Mar 16, 2011
//         */
//        public static List<ScadaFieldDescriptor>  getFieldDescriptorList() 
//            throws ScadaAnnotationException
//        {
//                return LST_FLD_DESCRS;
//        }

        /*
         * Global Operations
         */
        
        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws ScadaAnnotationException     the <code>ASignal.ASet</code> annotations are incomplete
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
         * 
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static DataLivePt  acquire(WireScanner ws) 
            throws NoSuchChannelException, ConnectionException, GetException, ScadaAnnotationException, BadStructException 
        {
            return new DataLivePt(ws);
        }


//        /**
//         * Extracts the field descriptor for the position channel
//         * from the annotations of the given <code>Data</code>-derived class type.
//         * 
//         * @param clsData   the <code>Data</code>-derived child class with DAQ annotations
//         * 
//         * @return      position channel field descriptor for this plane
//         *  
//         * @throws ScadaAnnotationException the <code>ADaqProfile</code> annotations are incomplete
//         * @throws NoSuchFieldException     a required field of the <code>ASet</code> annotation was empty or not found
//         * @throws IllegalAccessException   a required field of the <code>ASet</code> annotation is not accessible
//         * @throws IllegalArgumentException the argument <var>clsData</var> is not annotated with <code>ASet</code> 
//         *
//         * @author Christopher K. Allen
//         * @since  Sep 27, 2011
//         */
//        public static ScadaFieldDescriptor getSignalPosFd(ANGLE ang) throws ScadaAnnotationException  {
//
//            if (  !DataLivePt.class.isAnnotationPresent(ASet.class) )
//                throw new ScadaAnnotationException("No signal information present this class " + DataLivePt.class);
//
//            ASet        annSigSet = DataLivePt.class.getAnnotation(ASet.class);
//            ASignal     annSig;
//
//            switch (ang) {
//
//            case HOR:   // I am the horizontal profile   
//                annSig = annSigSet.sigHor();
//                break;
//
//            case VER:   // I am the vertical profile
//                annSig = annSigSet.sigVer();
//                break;
//
//            case DIA:   // I am the diagonal profile   
//                annSig = annSigSet.sigDia();
//                break;
//
//                // I am the walrus
//            default:
//                return null;
//            }
//
//            ScadaFieldDescriptor sfdPosArr = Signal.FIELD.POS.createDescriptor(annSig);
//
//            //            String      strHndPos = annSig.hndPosRb();
//            //            Class<?>    typHndPos = annSig.typePos();
//            //
//            //            ScadaFieldDescriptor    sfdPosArr = new ScadaFieldDescriptor(
//            //                    Signal.PROP.SGNL_POS.getFieldName(), 
//            //                    typHndPos, 
//            //                    strHndPos
//            //            );
//
//            return sfdPosArr;
//        }
//
//        /**
//         * Extracts the field descriptor for the signal value channel
//         * from the set of acquisition descriptors.
//         * 
//         * @param clsData   the <code>Data</code>-derived child class with DAQ annotations
//         * 
//         * @return      signal value channel field descriptor for this plane
//         *  
//         * @throws ScadaAnnotationException the <code>ADaqProfile</code> annotations are incomplete
//         * @throws NoSuchFieldException     a required field of the <code>ASet</code> annotation was empty or not found
//         * @throws IllegalAccessException   a required field of the <code>ASet</code> annotation is not accessible
//         * @throws IllegalArgumentException the argument <var>clsData</var> is not annotated with <code>ASet</code> 
//         *
//         * @author Christopher K. Allen
//         * @since  Feb 22, 2011
//         */
//        public static ScadaFieldDescriptor getSignalValFd(ProfileDevice.ANGLE ang) throws ScadaAnnotationException {
//
//            Class<? extends SignalSet>      clsData = DataLivePt.class;
//            
//            if (  ! clsData.isAnnotationPresent(ASignalAttrs.class) ) 
//                throw new ScadaAnnotationException("No signal information present for class " + clsData);
//
//            ASet        annSigSet = clsData.getAnnotation(ASet.class);
//            ASignal     annSig;
//
//            switch (ang) {
//
//            case HOR:   // I am the horizontal profile
//                annSig = annSigSet.sigHor();
//                break;
//
//            case VER:   // I am the vertical profile
//                annSig = annSigSet.sigVer();
//                break;
//
//
//            case DIA:   // I am the diagonal profile
//                annSig = annSigSet.sigDia();
//                break;
//
//            default:
//                return null;
//            }
//
//            ScadaFieldDescriptor    sfdValArr = Signal.FIELD.VAL.createDescriptor(annSig);
//
//            return sfdValArr;
//        }
//
        
        /*
         * Initialization
         */

        /**
         * Create a new empty <code>DataLive</code> object.
         *
         * @throws ScadaAnnotationException the channel handle annotations are not setup correctly 
         *
         * @since     Feb 16, 2010
         * @author    Christopher K. Allen
         */
        public DataLivePt() throws ScadaAnnotationException {
            super();
        }

        /**
         * Create a new <code>DataLive</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @throws ScadaAnnotationException the Data AcQ annotations are not setup correctly
         *
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public DataLivePt(DataAdaptor daptSrc) throws ScadaAnnotationException {
            super();
            this.update(daptSrc);
        }

        /**
         * Create a new <code>DataLive</code> object initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws ScadaAnnotationException     the <code>ASignal.ASet</code> annotations are incomplete
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
         *
         * @since     Nov 14, 2009
         * @author    Christopher K. Allen
         */
        public DataLivePt(WireScanner ws) 
            throws ConnectionException, GetException, ScadaAnnotationException, BadStructException 
        {
            super(ws);
        }

    }

    /**
     * <p>
     * Data structure contain the profile data available
     * during data acquisition in a point-by-point fashion 
     * (i.e., as the scan progress).
     * </p>
     * <p>
     * This appears to be then entire profile available after every
     * measurement sample.  This is different than the addition point
     * value after each sample.  The connected Process Variables are different,
     * so it is likely that this should work.
     * </p>
     * <p>
     * However, it is <b>never used</b> as of yet.
     * </p>
     * 
     * @since  Nov 13, 2009
     * @author Christopher K. Allen
     */
    @ASignal.ASet( 
            sigHor = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatHorLiveArrPositions", hndValRb = "DatHorLiveArrSignal", hndNseAvgRb = "DatHorNoiseAvg", hndNseVarRb = "DatHorNoiseStd"),
            sigVer = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatVerLiveArrPositions", hndValRb = "DatVerLiveArrSignal", hndNseAvgRb = "DatVerNoiseAvg", hndNseVarRb = "DatVerNoiseStd"),
            sigDia = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatDiaLiveArrPositions", hndValRb = "DatDiaLiveArrSignal", hndNseAvgRb = "DatDiaNoiseAvg", hndNseVarRb = "DatDiaNoiseStd") 
            )
    public static class DataLiveArr extends SignalSet {

        
//        /** The static list of field descriptors for this class */
//        private static final    List<ScadaFieldDescriptor>  LST_FLD_DESCRS = new ScadaFieldList(DataLiveArr.class);
//        
//
//        /**
//         * Returns the set of field descriptors for all the (field, channel)
//         * pairs used by this class.
//         *
//         * @return  set of field descriptors used by this class.
//         *
//         * @throws ScadaAnnotationException the <code>ADaqProfile</code> annotations are incomplete
//         * 
//         * @author Christopher K. Allen
//         * @since  Mar 16, 2011
//         */
//        public static List<ScadaFieldDescriptor>  getFieldDescriptorList() 
//            throws ScadaAnnotationException
//        {
//
//            return LST_FLD_DESCRS;
//        }

        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws ScadaAnnotationException     the <code>ASignal.ASet</code> annotations are incomplete
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
         *
         *
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static DataLiveArr  acquire(WireScanner ws) 
        throws ConnectionException, GetException, ScadaAnnotationException, BadStructException 
        {
            return new DataLiveArr(ws);
        }



        /*
         * Initialization
         */

        /**
         * Create a new empty <code>DataLiveArr</code> object.
         *
         * @throws ScadaAnnotationException the channel handle annotations are not setup correctly 
         *
         * @since     Feb 16, 2010
         * @author    Christopher K. Allen
         */
        public DataLiveArr() throws ScadaAnnotationException {
            super();
        }

        /**
         * Create a new <code>DataLiveArr</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @throws ScadaAnnotationException the Data AcQ annotations are not setup correctly
         * @throws MissingResourceException a data field was missing from the data source
         * @throws BadStructException      data structure fields are ill-defined/incompatible
         *  
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public DataLiveArr(DataAdaptor daptSrc) throws ScadaAnnotationException {
            super();
            this.update(daptSrc);
        }

        /**
         * Create a new <code>DataLiveArr</code> object initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws ScadaAnnotationException     the <code>ASignal.ASet</code> annotations are incomplete
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
         *
         * @since     Nov 14, 2009
         * @author    Christopher K. Allen
         */
        public DataLiveArr(WireScanner ws) 
                throws ConnectionException, GetException, ScadaAnnotationException, BadStructException 
        {
            super(ws);
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
    @ASignal.ASet( 
            sigHor = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatHorRawPositions", hndValRb = "DatHorRawSignal", hndNseAvgRb = "DatHorNoiseAvg", hndNseVarRb = "DatHorNoiseStd"),
            sigVer = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatVerRawPositions", hndValRb = "DatVerRawSignal", hndNseAvgRb = "DatVerNoiseAvg", hndNseVarRb = "DatVerNoiseStd"),
            sigDia = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatDiaRawPositions", hndValRb = "DatDiaRawSignal", hndNseAvgRb = "DatDiaNoiseAvg", hndNseVarRb = "DatDiaNoiseStd") 
            )
    public static class DataRaw extends SignalSet {

        
//        /** The static list of field descriptors for this class */
//        private static final    List<ScadaFieldDescriptor>  LST_FLD_DESCRS = new ScadaFieldList(DataRaw.class);
//        
//        
//        /**
//         * Returns the set of field descriptors for all the (field, channel)
//         * pairs used by this class.
//         *
//         * @return  set of field descriptors used by this class.
//         *
//         * @throws ScadaAnnotationException the <code>ADaqProfile</code> annotations are incomplete
//         * 
//         * @author Christopher K. Allen
//         * @since  Mar 16, 2011
//         */
//        public static final List<ScadaFieldDescriptor>    getFieldDescriptorList()
//            throws ScadaAnnotationException
//        {
//            
//            return LST_FLD_DESCRS;
//        }
        
        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws ScadaAnnotationException     the <code>ASignal.ASet</code> annotations are incomplete
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
         *
         * 
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static DataRaw acquire(WireScanner ws) 
            throws ConnectionException, GetException, ScadaAnnotationException, BadStructException 
        {
            return new DataRaw(ws);
        }



        /**
         * Create a new, empty <code>DataRaw</code> object.
         *
         * @throws ScadaAnnotationException this class is not annotated with <code>ASignal.ASet</code>,
         *                                  or the annotation is corrupt   
         *                                  
         * @since     Feb 26, 2010
         * @author    Christopher K. Allen
         */
        public DataRaw() throws ScadaAnnotationException {
            super();
        }

        /**
         * Create a new <code>DataRaw</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @throws  MissingResourceException        a data field was missing from the data source
         * @throws BadStructException  data structure fields are ill-defined/incompatible
         *  
         *                                  
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public DataRaw(DataAdaptor daptSrc) throws MissingResourceException, BadStructException {
            super();
            this.update(daptSrc);
        }

        /**
         * Create a new <code>DataRaw</code> object and initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws ScadaAnnotationException     the <code>ASignal.ASet</code> annotations are incomplete
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
         *
         * @since     Nov 14, 2009
         * @author    Christopher K. Allen
         */
        public DataRaw(WireScanner ws) throws ConnectionException, GetException, ScadaAnnotationException, BadStructException {
            super(ws);
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
    @ASignal.ASet( 
            sigHor = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatHorFitPositions", hndValRb = "DatHorFitSignal"),
            sigVer = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatVerFitPositions", hndValRb = "DatVerFitSignal"),
            sigDia = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatDiaFitPositions", hndValRb = "DatDiaFitSignal") 
            )
    public static class DataFit extends SignalSet {


//        /** The static list of field descriptors for this class */
//        private static final    List<ScadaFieldDescriptor>  LST_FLD_DESCRS = new ScadaFieldList(DataFit.class);
//        
//        /**
//         * Returns the set of field descriptors for all the (field, channel)
//         * pairs used by this class.
//         *
//         * @return  set of field descriptors used by this class.
//         *
//         * @throws ScadaAnnotationException the <code>ADaqProfile</code> annotations are incomplete
//         * 
//         * @author Christopher K. Allen
//         * @since  Mar 16, 2011
//         */
//        public static final List<ScadaFieldDescriptor>    getFieldDescriptorList() 
//            throws ScadaAnnotationException
//        {
//            return LST_FLD_DESCRS;
//        }
        
        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws ScadaAnnotationException     the <code>ASignal.ASet</code> annotations are incomplete
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
         * 
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static DataFit acquire(WireScanner ws) 
            throws ConnectionException, GetException, ScadaAnnotationException, BadStructException 
        {
            return new DataFit(ws);
        }

        
        /*
         * Initialization
         */
        
        /**
         * Creates a new, empty <code>DataFit</code> object.
         *
         * @throws ScadaAnnotationException this class is not annotated with <code>ASignal.ASet</code>,
         *                                  or the annotation is corrupt   
         *                                  
         * @since     Feb 26, 2010
         * @author    Christopher K. Allen
         */
        public DataFit() throws ScadaAnnotationException {
            super();
        }

        /**
         * Create a new <code>DataFit</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @throws ScadaAnnotationException this class is not annotated with <code>ASignal.ASet</code>,
         *                                  or the annotation is corrupt   
         *                                  
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public DataFit(DataAdaptor daptSrc) throws  ScadaAnnotationException {
            super();
            this.update(daptSrc);
        }

        /**
         * Create a new <code>DataFit</code> object initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws ScadaAnnotationException     the <code>ASignal.ASet</code> annotations are incomplete
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
         *
         * @since     Nov 14, 2009
         * @author    Christopher K. Allen
         */
        public DataFit(WireScanner ws) 
            throws ConnectionException, GetException, ScadaAnnotationException, BadStructException 
        {
            super(ws);
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
    @ASignal.ASet( 
            sigHor = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatHorTracePositions", hndValRb = "DatHorTraceSignal"),
            sigVer = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatVerTracePositions", hndValRb = "DatVerTraceSignal"),
            sigDia = @ASignal(hndCntRb = "ScanCfgStepCntRb", hndPosRb = "DatDiaTracePositions", hndValRb = "DatDiaTraceSignal") 
            )
    public static class Trace extends SignalSet {

        
//        /**
//         * Returns the set of field descriptors for all the (field, channel)
//         * pairs used by this class.
//         *
//         * @return  set of field descriptors used by this class.
//         *
//         * @author Christopher K. Allen
//         * @since  Mar 16, 2011
//         */
//        public static final List<ScadaFieldDescriptor>    getFieldDescriptorList() {
//
//            return Data.getFieldDescriptorList(Trace.class);
//            
//        }
        
        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param ws    data acquisition device
         * 
         * @return New data populated structure
         *  
         * @throws ScadaAnnotationException     the <code>ASignal.ASet</code> annotations are incomplete
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
         * 
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static Trace acquire(WireScanner ws) 
            throws ConnectionException, GetException, ScadaAnnotationException, BadStructException 
        {
            return new Trace(ws);
        }



        
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
         * @see xal.smf.impl.WireScanner2.Data#update(xal.tools.data.DataAdaptor)
         */
        @Override
        public void update(DataAdaptor adaptor) {
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
         * @see xal.smf.impl.WireScanner2.Data#write(xal.tools.data.DataAdaptor)
         */
        @Override
        public void write(DataAdaptor adaptor) {
            super.write(adaptor);
        }

        
        /*
         * Initialization
         */

        /**
         * Creates a new <code>DataTrace</code> object.
         *
         * @throws ScadaAnnotationException     the <code>ASignal.ASet</code> annotations are incomplete
         *
         * @since     Feb 26, 2010
         * @author    Christopher K. Allen
         */
        public Trace() throws ScadaAnnotationException {
            super();
        }

        /**
         * Create a new <code>DataTrace</code> object initialized
         * from the given data source.
         *
         * @param daptSrc       persistent data store containing
         *                      data structure field values
         *
         * @throws ScadaAnnotationException     the <code>ASignal.ASet</code> annotations are incomplete
         *                                  
         * @since     Mar 17, 2010
         * @author    Christopher K. Allen
         */
        public Trace(DataAdaptor daptSrc) throws ScadaAnnotationException {
            super();
            this.update(daptSrc);
        }

        /**
         * Create a new <code>DataLive</code> object initialize
         * with values fetched from the given device.
         *
         * @param ws    data acquisition device
         * 
         * @throws ScadaAnnotationException     the <code>ASignal.ASet</code> annotations are incomplete
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
         *
         * @since     Nov 14, 2009
         * @author    Christopher K. Allen
         */
        private Trace(WireScanner ws) throws ScadaAnnotationException, ConnectionException, GetException, BadStructException {
            super();

            this.dia.loadHardwareValues(ws);
            this.ver.loadHardwareValues(ws);
            this.hor.loadHardwareValues(ws);
        }
    }


    
    /*
     * Local Attributes
     */
    
    /** The SCADA connection tester */
    private BatchConnectionTest       tstConnect = null;



    /*
     * Initialization
     */

	/**
	 * Primary Constructor.
	 * @param nodeID unique identifier for this node
	 * @param channelFactory factory for generating channels for this node
	 */
	public WireScanner( final String nodeID, final ChannelFactory channelFactory )   {
		super( nodeID, channelFactory );
	}


    /**
     * Create a new <code>WireScanner</code> object.
     *
     * @param nodeID
     *
     * @since     Jun 18, 2009
     * @author    Christopher K. Allen
     */
    public WireScanner( final String nodeID )   { 
        this( nodeID, null );
    }   


    /*
     * AcceleratorNode Properties
     */

    /** 
     * Get the device type
     *   
     * @return  The type identifier of this class
     */
    @Override
    public String getType()  { 
        return s_strType; 
    }


    /** 
     * Derived class may furnish a unique software type 
     * 
     * @return     the "software type" or the driver version
     *              for the hardware.
     */
    @Override
    public String getSoftType() {
        return SOFTWARE_TYPE; 
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
    public synchronized void runCommand(final CMD cmd) throws  ConnectionException, PutException, InterruptedException 
    {
        // Issue command
        Channel channel = getAndConnectChannel( CMD.HANDLE_CMD );
        channel.putVal( cmd.getCode() );

//        // Reset the on-board command buffer to avoid the IOC echo (a kluge)
//        Thread.sleep(INT_CMD_LATCY);
//        channel.putVal( CMD.NOOP.getCode() );
    }

    /**
     * Issue a wire scanner command with or without arguments arguments.  
     * The command and argument list is packed in the argument of type
     * <code>{@link WireScanner2.CmdPck}</code>
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
    public synchronized void runCommand(final CmdPck cmdPck) 
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

//        // Reset the on-board command buffer to avoid the IOC echo (a kluge)
//        Thread.sleep(INT_CMD_LATCY);
//        channel.putVal( CMD.NOOP.getCode() );
    }
    
    


    /** 
     * Returns the command result code(s).  These are integer valued
     * codes for the result of a previous issued device command.  See
     * the wire scanner documentation to interpret the particular
     * resultant code.
     * 
     * @return command result   array of integer codes for a command result.
     * 
     * @throws ConnectionException Unable to connect to result readback channel
     * @throws GetException        Unable to read result from readback channel
     */
    public int[] getCommandResult() throws ConnectionException, GetException {
        //        final Channel channel = getAndConnectChannel( COMMAND_RESULT_HANDLE );
        final Channel channel = getAndConnectChannel( CMD.HANDLE_RESULT );

        return channel.getArrInt();
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
    public synchronized boolean  testConnection(Collection<ScadaFieldDescriptor> setFds, double dblTmOut) 
        throws BadChannelException
    {
        boolean bolResult = this.tstConnect.testConnection(setFds, dblTmOut);
        
        return bolResult;
    }

//    /**
//     * Test the connections in all the channels of the given parameter set for this
//     * accelerator device.  The test will wait up to the given length
//     * of time before declaring failure.
//     *
//     * @param clsScada      type of a SCADA data structure
//     * @param dblTmOut      time out before test fails (in seconds)
//     * 
//     * @return              <code>true</code> if all connections were successful,
//     *                      <code>false</code> if not all connection were made within given time
//     *                      
//     * @throws BadStructException  the given class is not a SCADA data structre 
//     * @throws BadChannelException  An unbound channel handle within a field descriptor 
//     *
//     * @author Christopher K. Allen
//     * @throws BadChannelException 
//     * @since  Feb 4, 2011
//     */
//    public synchronized boolean testConnection(Class<?> clsScada, double dblTmOut) 
//        throws BadStructException, BadChannelException 
//    {
//        
//        Collection<ScadaFieldDescriptor>    setFds = new ScadaFieldList(clsScada);
//        
//        if (setFds.size() == 0)
//            throw new BadStructException("Class is not a SCADA data structure"); //$NON-NLS-1$
//        
//        return this.testConnection(setFds, dblTmOut);
//    }

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
    public synchronized void configureHardware(ParameterSet datPvFlds) 
        throws PutException, ConnectionException 
    {
        datPvFlds.setHardwareValues(this);
    }

    /**
     * Changes the data processing parameters (see <code>{@link PrcgConfig}</code>)
     * so that the on-board controller selects the beam micro-bunch section for computing 
     * measurement results.
     *
     * @param enmBmSct      the part of the  beam on which to focus the analysis
     * @param dblBmFrac     proportion of the current beam to be used in analysis
     *                      (a value 1 is the entire macro-pulse)
     * 
     * @throws RejectedExecutionException (runtime exception) the machine is not in 50 &mu;sec mode, parameter will not be changed
     * @throws NoSuchChannelException   error in XAL channel handle/EPICS PV binding
     * @throws ConnectionException      could not connect to data processing PVs 
     * @throws GetException             could not retrieve some data processing parameters
     * @throws PutException             unable to set the new analysis configuration parameters
     *
     * @author Christopher K. Allen
     * @since  Nov 3, 2011
     */
    public void analysisParametersSelect(MACROPULSE enmBmSct, double dblBmFrac) 
        throws ConnectionException, NoSuchChannelException, GetException, PutException, RejectedExecutionException 
    {
        Accelerator     accl = this.getAccelerator();
        TimingCenter    tmg  = accl.getTimingCenter();
        
        // Check that we are in 50 micro-second mode
        Channel         chnBmMode = tmg.getAndConnectChannel(TimingCenter.BEAM_MODE_HANDLE);
        String          strBmMode = chnBmMode.getValString();
        
        if ( !TimingCenter.BEAM_MODE.MCRSEC_50.isPvValue(strBmMode) )
            throw new RejectedExecutionException("Parameters rejected - the machine is not in 50 microsecond mode. Mode is " + strBmMode);
        
        // Compute the number of pulses we are going to use
        Channel         chnPlsCnt = tmg.getAndConnectChannel(TimingCenter.CHOPPER_BEAM_ON);
        int             cntTtlPls = chnPlsCnt.getValInt();
        double          dblPulses = dblBmFrac*cntTtlPls;
        
        // Convert from beam pulse count to window size (in seconds)
        double          dblWndSz  = 1.0E-6 * dblPulses;

        PrcgConfig      cfgPrcg   = PrcgConfig.acquire(this);
        
        // Set the new processing parameters according to the section of beam we are selecting
        double  dblDlyPls = 0.0;      // number of pulses to delay
        double  dblDlyTm  = 0.0;       // corresponding delay time (in seconds)
        
        switch (enmBmSct) {

        case HEAD:
            dblDlyPls = 0.0;
            dblDlyTm  = 0.0;
            break;
            
        case BODY:
            // Compute the time delay
            dblDlyPls = (1.0 - dblBmFrac/2.0)*cntTtlPls;
            dblDlyTm  = 1.0E-6 * dblDlyPls;

            // Add the window offset caused by the analogue filter
            dblDlyTm += DBL_FILTER_OFFSET;
            
            break;
            
        case TAIL:
            // Compute the time delay
            dblDlyPls = (1.0 - dblBmFrac)*cntTtlPls;
            dblDlyTm  = 1.0E-6 * dblDlyPls;
            
            // Add the window offset caused by the analogue filter
            dblDlyTm += DBL_FILTER_OFFSET;
            
            break;
            
        case ALL:
            // The time delaly is zero (and the window is 50 micro-seconds)
            dblDlyTm  = 0.0;
            dblWndSz  = 1.0E-6 * cntTtlPls;
            break;
        }

        // Send the new processing parameters to the hardware
        cfgPrcg.avgBgn = dblDlyTm;
        cfgPrcg.avgLng = dblWndSz;

        this.configureHardware(cfgPrcg);
    }
    
    
}
