/**
 * WireHarp.java
 *
 * @author Christopher K. Allen
 * @since  Jan 9, 2013
 *
 */

package xal.smf.impl;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.tools.data.DataAdaptor;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.profile.ASignal;
import xal.smf.impl.profile.ASignalAttrs;
import xal.smf.impl.profile.ParameterSet;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.impl.profile.Signal;
import xal.smf.impl.profile.SignalAttrSet;
import xal.smf.impl.profile.SignalSet;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.smf.scada.AScada;
import xal.smf.scada.BadStructException;
import xal.smf.scada.ScadaAnnotationException;
import xal.smf.scada.ScadaFieldMap;
import xal.smf.scada.ScadaRecord;

/**
 * <h3>Wire Harp Hardware</h3>
 * <p>
 * Represents a harp diagnostic device, in particular
 * such a device installed at the SNS facility.  The harp has
 * fixed, physical (carbon) wires used to measure the profile of
 * the particle beam.  This class implements the device API.  
 * </p>
 * <h3>NOTES:</h3>
 * <p>
 * &middot; There is a pretty egregious kluge on the attribute 
 * <code>DaqConfig.cntWires</code>.  The value is hard coded with the constant
 * <code>DaqConfig.CNT_WIRES</code> since there is no way to dynamically
 * acquire this value.
 * </p>  
 * 
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Jan 9, 2013
 */
public class WireHarp extends ProfileDevice {


    /*
     * Enumeration Constants for Common Values
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

        /** Take a new baseline measurement */
        BASELINE(7),

        /** The secret command */
        SECRET(9);


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


    /*
     * Configuration and Status Structures
     */

    /**
     * Contains device status parameters.
     *
     * @author Christopher K. Allen
     * @since  Mar 20, 2014
     */
    public static class DevStatus extends ParameterSet {


        /*
         * Global Attributes
         */

        /** Map of field names to field SCADA descriptors for this structure */
        public final static ScadaFieldMap   FLD_MAP = new ScadaFieldMap(DevStatus.class);


        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param smfHarp    data acquisition device
         * 
         * @return current set of device status parameters
         *  
         * @throws ConnectionException  unable to connect parameter read back channel
         * @throws GetException         general field initialization exception 
         * 
         * @author Christopher K. Allen
         * @since  Mar 20, 2014
         */
        public static DevStatus acquire(WireHarp smfHarp) 
                throws ConnectionException, GetException  
        {
            return new DevStatus(smfHarp);
        }


        //        /*
        //         * DataAdaptor Interface
        //         */
        //
        //        /**
        //         * Returns the name of this class as the data label
        //         *
        //         * @see xal.tools.data.DataListener#dataLabel()
        //         *
        //         * @author Christopher K. Allen
        //         * @since  Mar 20, 2014
        //         */
        //        @Override
        //        public String dataLabel() {
        //            return this.getClass().getCanonicalName();
        //        }
        //        

        /*
         * Status Parameters
         */

        /**
         * Alarm flag for wire signal; it indicates correct operations.
         * <br>
         * <br>
         *  status: 0=OK, 1=Saturated
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatDevStatus"
                )
        public int      statDev;

        /** 
         * Machine Protection System status flag 
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatMpsStatus"
                )
        public int      statMps;

        /**
         * Manual control status indicator
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatCtrlStatus"
                )
        public int      statCtrl;

        /** 
         * Flag indicating harp is inserted
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatHarpInsert"
                )
        public int      hrpIns;

        /**
         * Flag indicating harp is retracted
         */
        @AScada.Field( 
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatHarpRetract"
                )
        public  int     hrpRetr;

        /**
         * Flag indicating harp is stopped
         */
        @AScada.Field( 
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatHarpStop"
                )
        public  int     hrpStop;


        /** 
         * Result code of the latest harp user command.
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatCmdResult"
                )
        public int      cmdResult;



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
         * Create a new <code>DevStatus</code> object containing the status
         * information of the given device.
         *
         * @param smfHarp    wire harp device under query
         * 
         * @throws ConnectionException  unable to connect parameter read back channel
         * @throws GetException         general field initialization exception 
         *
         * @since     Jan 21, 2010
         * @author    Christopher K. Allen
         */
        public DevStatus(WireHarp smfHarp) throws ConnectionException, GetException {
            super(smfHarp);
        }
    }

    /**
     * Class <code>WireHarp.DaqConfig</code>.  A data structure containing fields
     * that indicate the status of the data acquisition capabilities.  In particular,
     * there are status records for the operation of each wire within each 
     * measurement plane (eg., horizontal, vertical, diagonal). 
     * <br>
     * <h3>NOTE</h3>
     * <br>
     * &middot; There is a pretty egregious kluge on the attribute 
     * <code>DaqConfig.cntWires</code>.  The value is hard coded with the constant
     * <code>DaqConfig.CNT_WIRES</code> since there is no way to dynamically
     * acquire this value.
     *
     * @author Christopher K. Allen
     * @since  Apr 11, 2014
     */
    public static class DaqConfig extends ParameterSet implements IProfileDomain {

        /*
         * Global Attributes
         */

        /** Map of field names to field SCADA descriptors for this structure */
        public final static ScadaFieldMap   FLD_MAP = new ScadaFieldMap(DaqConfig.class);


        /**
         * Convenience method for retrieving a new initialized data structure
         * populated from the given device state.
         *
         * @param smfHarp    data acquisition device
         * 
         * @return current set of device status parameters
         *  
         * @throws ConnectionException  unable to connect parameter read back channel
         * @throws GetException         general field initialization exception 
         * 
         * @author Christopher K. Allen
         * @since  Mar 20, 2014
         */
        public static DaqConfig acquire(WireHarp smfHarp) 
                throws ConnectionException, GetException  
                {
            return new DaqConfig(smfHarp);
                }


        /*
         * Status Parameters
         */

        /**   
         * The number of wires within the harp.
         * <br>
         * <br>
         * April, 2014: Currently this value must be provided externally as it is not available
         * via the control system. 
         */
        //        @AScada.Field( 
        //                type = int.class,
        //                ctrl = false,
        //                hndRb = ""
        //                )
        public int      cntWires = CNT_WIRES;

        /** 
         * Array of wire positions on the beam axis for the horizontal projection plane.
         */
        @AScada.Field(
                type = double[].class,
                ctrl = false,
                hndRb = "DatHorRawPositions"
                )
        public double[] arrPosHor;

        /** 
         * Array of wire positions on the beam axis for the vertical projection plane.
         */
        @AScada.Field(
                type = double[].class,
                ctrl = false,
                hndRb = "DatVerRawPositions"
                )
        public double[] arrPosVer;

        /** 
         * Array of wire positions on the beam axis for the diagonal projection plane.
         */
        @AScada.Field(
                type = double[].class,
                ctrl = false,
                hndRb = "DatDiaRawPositions"
                )
        public double[] arrPosDia;

        /**   
         * Bit record indicating operation status for each wire of the
         * horizontal wire set.
         * <br> <br>
         * &nbsp; &nbsp; bolStatus = 2<sup>n</sup> &amp; statWireHor
         * <br> <br>
         * where <i>n</i>=0,1,... is the index of the wire.
         * <br><br>
         * <b>Do Not</b> access this field directly!  
         */
        @AScada.Field(
                type  = int.class,
                ctrl  = false,
                hndRb = "StatRecHorWires"
                )
        public int     recWiresHor;

        /**   
         * Bit record indicating operation status for each wire
         * of the vertical wire set.
         * <br> <br>
         * &nbsp; &nbsp; bolStatus = 2<sup>n</sup> &amp; statWireVer
         * <br> <br>
         * where <i>n</i>=0,1,... is the index of the wire.  
         * <br><br>
         * <b>Do Not</b> access this field directly!  
         */
        @AScada.Field(
                type  = long.class,
                ctrl  = false,
                hndRb = "StatRecVerWires"
                )
        public int     recWiresVer;

        /**   
         * Bit record indicating operation status for each wire
         * of the diagonal wire set.
         * <br> <br>
         * &nbsp; &nbsp; bolStatus = 2<sup>n</sup> &amp; statWireDia
         * <br> <br>
         * where <i>n</i>=0,1,... is the index of the wire.  
         * <br><br>
         * <b>Do Not</b> access this field directly!  
         */
        @AScada.Field(
                type  = int.class,
                ctrl  = false,
                hndRb = "StatRecDiaWires"
                )
        public int     recWiresDia;

        /**
         * Type code of the current fitting profile
         * <br>0 = Super Gaussian
         * <br>1 = Super Gaussian &times; Gaussian
         * <br>2 = Super Gaussian + Gaussian
         * <br>3 = Super Gaussian &times; Parabola
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = false,
                hndRb   = "StatFitType"
                )
        public int      fitTypeCode;


        /*
         * Initialization
         */

        /**
         * Create a new, uninitialized <code>DaqConfig</code> object.
         *
         * @since     Jan 21, 2010
         * @author    Christopher K. Allen
         */
        public DaqConfig() {
            super();
        }

        /**
         * Create a new <code>DaqConfig</code> object containing the status
         * information of the given device.
         *
         * @param smfHarp    wire harp device under query
         * 
         * @throws ConnectionException  unable to connect parameter read back channel
         * @throws GetException         general field initialization exception 
         *
         * @since     Jan 21, 2010
         * @author    Christopher K. Allen
         */
        public DaqConfig(WireHarp smfHarp) throws ConnectionException, GetException {
            super(smfHarp);
        }


        /*
         * Operations
         */

        /**
         * Returns the number of valid harp wires for the given projection
         * plane.
         *  
         * @param ang   projection angle for the harp 
         * 
         * @return      number of wires producing valid data for the given harp
         *
         * @author Christopher K. Allen
         * @since  Jun 5, 2014
         */
        public int  validWireCount(ANGLE ang) {
            boolean[]   arrValWire = this.validWires(ang);

            int     cntValWires = 0;
            for (boolean bolFlag : arrValWire) 
                if (bolFlag)
                    cntValWires++;

            return cntValWires;
        }

        /**
         * Check if the indicated wire is valid, that is, producing valid data.  The given wire
         * index must be in the range [0,<i>N</i>] where <i>N</i> is the number of wires
         * in the harp. 
         * 
         * @param ang       the transverse plane of the profile (i.e., wire set)
         * @param index     the index of the wire within the harp
         * 
         * @return          <code>true</code> if the indicated wire is working correctly,
         *                  <code>false</code> otherwise
         *                  
         * @throws IllegalArgumentException the index provided was not in the correct range
         *
         * @author Christopher K. Allen
         * @since  Apr 14, 2014
         */
        public boolean  validWire(ANGLE ang, int index) throws IllegalArgumentException {

            // Check for proper index values
            if (index < 0)
                throw new IllegalArgumentException("Index must be non-negative");

            if (index >= this.cntWires)
                throw new IllegalArgumentException("Index must be less than number of wires");

            // Create mask for given wire index
            int         intMask = 1 << index;
            boolean     bolValid;   // result of validity check

            switch (ang) {

            case HOR: 
                bolValid = (intMask & this.recWiresHor) > 0;
                break;

            case VER:
                bolValid = (intMask & this.recWiresVer) > 0;
                break;

            case DIA:
                bolValid = (intMask & this.recWiresDia) > 0;
                break;

            default:
                bolValid = false;
                break;
            }

            return bolValid;
        }

        /**
         * Creates an array of status flags for each harp wire for the given transverse
         * plane.  The array size equals the number of wires in the harp (<code>cntWires</code>),
         * the index into the array is the index of the harp wire.
         * 
         * @param ang   transverse plane of the profile
         * 
         * @return      array containing status flags indexed by harp wire 
         * 
         * @author Christopher K. Allen
         * @since  Apr 14, 2014
         */
        public boolean[] validWires(ANGLE ang) {

            // Get the status record for the desired profile plane
            long    lngStatRec; // status record for the desired transverse plane

            switch (ang) {
            case HOR:
                lngStatRec = this.recWiresHor;
                break;
            case VER:
                lngStatRec = this.recWiresVer;
                break;
            case DIA:
                lngStatRec = this.recWiresDia;
                break;
            default:
                lngStatRec = 0;
                break;
            }


            // Allocate the returned array and initialize loop
            boolean[]   arrValid = new boolean[this.cntWires];
            int         intMask  = 1;

            for (int index=0; index<this.cntWires; index++) {
                arrValid[index] = (intMask & lngStatRec) > 0;
                intMask <<= 1;
            }

            return arrValid;
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
            return this.cntWires;
        }

        /**
         * Returns the array of sampling positions for the profile data
         * 
         * @author Christopher K. Allen
         * @since  Apr 24, 2014
         * 
         * @see xal.smf.impl.profile.ProfileDevice.IProfileDomain#getSamplePositions(xal.smf.impl.profile.ProfileDevice.ANGLE)
         */
        @Override
        public double[] getSamplePositions(ANGLE angle) {

            switch (angle) {
            case HOR: return this.arrPosHor;
            case VER: return this.arrPosVer;
            case DIA: return this.arrPosDia;
            default: return null;
            }
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

            double[]    arrPos = this.getSamplePositions(angle);

            return arrPos[0];
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
            int         indMax = this.getSampleCount(angle) - 1;
            double[]    arrPos = this.getSamplePositions(angle);
            double      posMax = arrPos[indMax];
            double      dblLen = posMax - this.getInitialPosition(angle);

            return dblLen;
        }
    }

    /**
     * Data structure containing the configuration parameters for 
     * a <code>WireHarp</code> device. The data structure is active
     * and can be used to set the configuration parameters as well.
     *
     * @author Christopher K. Allen
     * @since  Mar 20, 2014
     */
    public static class DevConfig extends ParameterSet {


        /**
         * Returns the defined set of configuration parameters currently
         * used for the given device.
         * 
         * @param smfHarp   profile device being queried
         * 
         * @return          current configuration for the given device
         * 
         * @throws ConnectionException  unable to connect to a parameter read back channel
         * @throws GetException         general channel access get exception
         *
         * @author Christopher K. Allen
         * @since  Mar 20, 2014
         */
        public static DevConfig acquire(WireHarp smfHarp) throws ConnectionException, GetException 
        {
            return new DevConfig(smfHarp);
        }


        /*
         * DataListener Interface
         */

        /**
         * Returns the class name as the <code>DataAdaptor</code>
         * data label.
         *
         * @see xal.tools.data.DataListener#dataLabel()
         *
         * @author Christopher K. Allen
         * @since  Mar 20, 2014
         */
        @Override
        public String dataLabel() {
            return this.getClass().getCanonicalName();
        }


        /*
         * SCADA Fields
         */

        /** 
         * The (discrete) common amplifier gain for all profile channels.
         * <br>
         * &nbsp; &nbsp; Range = {0, 1, 2}  
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = true,
                hndRb   = "CfgGainCmnRb",
                hndSet  = "CfgGainCmnSet"
                )
        public int      gainCmn;

        //        /** 
        //         * The (discrete) amplifier gain for for the horizontal profile
        //         * channel.
        //         * <br>
        //         * &nbsp; &nbsp; Range = {0, 1, 2}  
        //         */
        //        @AScada.Field(
        //                type    = int.class,
        //                ctrl    = true,
        //                hndRb   = "CfgGainHorRb",
        //                hndSet  = "CfgGainHorSet"
        //                )
        //        public int      gainHor;
        //        
        //        /** 
        //         * The (discrete) amplifier gain for for the vertical profile
        //         * channel.
        //         * <br>
        //         * &nbsp; &nbsp; Range = {0, 1, 2}  
        //         */
        //        @AScada.Field(
        //                type    = int.class,
        //                ctrl    = true,
        //                hndRb   = "CfgGainVerRb",
        //                hndSet  = "CfgGainVerSet"
        //                )
        //        public int      gainVer;
        //        
        //        /** 
        //         * The (discrete) amplifier gain for for the diagonal profile
        //         * channel.
        //         * <br>
        //         * &nbsp; &nbsp; Range = {0, 1, 2}  
        //         */
        //        @AScada.Field(
        //                type    = int.class,
        //                ctrl    = true,
        //                hndRb   = "CfgGainDiaRb",
        //                hndSet  = "CfgGainDiaSet"
        //                )
        //        public int      gainDia;

        /**
         * The triggering delay, I think it is in micro-seconds.
         * This is the delay between the trigger event and the actual
         * data acquisition.   
         */
        @AScada.Field(
                type    = double.class,
                ctrl    = true,
                hndRb   = "CfgTrgDelayRb",
                hndSet  = "CfgTrgDelaySet"
                )
        public double   trgDelay;

        /**   
         * Triggering event type code.
         * 
         * @see TRGEVT
         */
        @AScada.Field(
                type    = int.class,
                ctrl    = true,
                hndRb   = "CfgTrgEventRb",
                hndSet  = "CfgTrgEventSet"
                )
        public int      trgEvent;


        /*
         * Initialization
         */

        /**
         * Create a new, uninitialized <code>DevConfig</code> object.
         *
         * @author Christopher K. Allen
         * @since  Mar 20, 2014
         */
        public DevConfig() {
            super();
        }

        /**
         * Create a new <code>DevConfig</code> object initialized
         * from the given data source. 
         *
         * @param daptSrc       data source containing data structure fields
         *
         * @author Christopher K. Allen
         * @since  Mar 20, 2014
         */
        public DevConfig(DataAdaptor daptSrc) {
            super(daptSrc);
        }

        /**
         * Create a new <code>ActrConfig</code> object initialize
         * with values fetched from the given device.
         *
         * @param smHarp    data acquisition device
         * 
         * @throws ConnectionException  unable to connect to a parameter channel
         * @throws GetException         unable to retrieve values from channel access 
         *
         * @author Christopher K. Allen
         * @since  Mar 20, 2014
         */
        public DevConfig(WireHarp smHarp) throws ConnectionException, GetException {
            super(smHarp);
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
            this.gainCmn = gain.getGainValue();
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
            return GAIN.getGainFromValue(this.gainCmn);
        }

        /**
         * Sets the trigger event code by converting the given
         * <code>TRGEVT</code> enumeration constant to the correct
         * code and assigning the value to the trigger event
         * field in this data structure.
         * 
         * @param evt       enumeration for the desired trigger event code
         *
         * @author Christopher K. Allen
         * @since  Mar 21, 2014
         * 
         * @see DevConfig#trgEvent
         */
        public void setTriggerEvent(TRGEVT evt) {
            this.trgEvent = evt.getEventValue();
        }

        /**
         * Converts the trigger event code to the corresponding
         * <code>TRGEVT</code> enumeration constant and returns it.
         * 
         * @return  the <code>TRGEVT</code> constant for the current 
         *          trigger event code
         *
         * @author Christopher K. Allen
         * @since  Mar 21, 2014
         * 
         * @see DevConfig#trgEvent
         */
        public TRGEVT getTriggerEvent() {
            return TRGEVT.getEventFromValue(trgEvent);
        }
    }


    /*
     * Processed Signal Attributes
     */

    /**
     * Data structure containing the profile signal characteristics.
     * I believe the signal is processed by assuming a Gaussian profile.
     *
     * @author Christopher K. Allen
     * @since  Mar 19, 2014
     */
    @ASignalAttrs.ASet(
            attrHor = @ASignalAttrs( hndAmpRb = "FitAttrHorAmp", hndAreaRb = "FitAttrHorArea", hndMeanRb = "FitAttrHorMean", hndOffsetRb = "FitAttrHorOffset", hndStdevRb = FitAttrSet.X_RMS_HANDLE),
            attrVer = @ASignalAttrs( hndAmpRb = "FitAttrVerAmp", hndAreaRb = "FitAttrVerArea", hndMeanRb = "FitAttrVerMean", hndOffsetRb = "FitAttrVerOffset", hndStdevRb = FitAttrSet.Y_RMS_HANDLE),
            attrDia = @ASignalAttrs( hndAmpRb = "FitAttrDiaAmp", hndAreaRb = "FitAttrDiaArea", hndMeanRb = "FitAttrDiaMean", hndOffsetRb = "FitAttrDiaOffset", hndStdevRb = "FitAttrDiaStd")
            )
    public static class FitAttrSet extends SignalAttrSet {
		/** handle to the X RMS PV */
		public static final String X_RMS_HANDLE = "FitAttrHorStd";

		/** handle to the Y RMS PV */
		public static final String Y_RMS_HANDLE = "FitAttrVerStd";


        /**
         * Convenience method for retrieving a new, initialized data
         * structure populated from the current given device state.
         * 
         * @param smfHarp   data acquisition device
         * 
         * @return          data structure containing the current
         *                  state of the given hardware device
         *                  
         * @throws NoSuchChannelException unable to find channel for given handle 
         * @throws ConnectionException  unable to connect to a field's channel
         * @throws GetException         general CA GET exception while fetch field value
         *
         * @author Christopher K. Allen
         * @since  Mar 19, 2014
         */
        public static FitAttrSet    aquire(WireHarp smfHarp) 
                throws NoSuchChannelException, ConnectionException, GetException
        {
            return new FitAttrSet(smfHarp);
        }




        /*
         * Initialization
         */

        /**
         * Constructor for FitAttrSet, creates a new, uninitialized
         * <code>FitAttrSet</code> abject.
         *
         *
         * @author Christopher K. Allen
         * @since  Mar 19, 2014
         */
        public FitAttrSet() {
            super();
        }

        /**
         * Constructor for FitAttrSet. Create a new <code>FitAttrSet</code>
         * object initialized from the given data source.
         *
         * @param daptSrc   persistent data store containing initializing
         *                  data field values
         *
         * @author Christopher K. Allen
         * @since  Mar 19, 2014
         */
        public FitAttrSet(DataAdaptor daptSrc) {
            this();
            this.update(daptSrc);
        }

        /**
         * Constructor for FitAttrSet.
         *
         * @param wharp
         * @throws ConnectionException
         * @throws GetException
         *
         * @author Christopher K. Allen
         * @since  Mar 19, 2014
         */
        private FitAttrSet(WireHarp wharp) throws ConnectionException, GetException {
            super(wharp);
        }
    }

    /**
     * <p>
     * Structure for maintaining a <code>WireHarp</code> profile data
     * measurement.  Each instance of 
     * this data structure contains a snapshot of the beam profile provided by the
     * wire harp and acquired through the SCADA mechanism of <code>{@link ScadaRecord}</code>.
     * </p>
     * <p>  
     * The data structure is derived from the <code>SignalSet</code>
     * base class which does all the work.  
     * The data from each plane is available through the attributes of type 
     * <code>{@link WireHarp.Signal}</code> in the <code>WireHarp.SignalSet</code> base class.
     * The data acquisition channels are identified with
     * the annotation <code>{@link ASignalSet}</code> which tells the base class how to 
     * connect the <code>{@link WireHarp.Signal}</code> attributes.
     * </p>
     *
     * @author Christopher K. Allen
     * @since  Feb 5, 2013
     * 
     * @see SignalSet
     * @see ScadaRecord
     */
    @ASignal.ASet( 
            sigHor = @ASignal(hndPosRb = "DatHorRawPositions", hndValRb = "DatHorRawSignal" ),
            sigVer = @ASignal(hndPosRb = "DatVerRawPositions", hndValRb = "DatVerRawSignal" ),
            sigDia = @ASignal(hndPosRb = "DatDiaRawPositions", hndValRb = "DatDiaRawSignal" ) 
            )
    static public class DataRaw extends SignalSet {


        /*
         * Global Operations
         */

        /**
         * Convenience method for retrieving a new, initialized measurement set
         * acquired from the given device.
         *
         * @param smfHarp   measurement device
         * 
         * @return          newly acquired data set from the given device
         *  
         * @throws ConnectionException          unable to connect data read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         * @throws ScadaAnnotationException     the <code>ADaqProfile</code> annotations are incomplete
         * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
         *
         * 
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static DataRaw  aquire(WireHarp smfHarp) 
                throws ConnectionException, GetException, ScadaAnnotationException, BadStructException 
        {
            return new DataRaw(smfHarp);
        }


        /*
         * Initialization
         */

        /**
         * Creates a new, empty instance of <code>Measurement</code>.
         *
         * @throws ScadaAnnotationException this class is not annotated with <code>ASignal.ASet</code>,
         *                                  or the annotation is corrupt   
         *
         * @author Christopher K. Allen
         * @since  Feb 5, 2013
         */
        public DataRaw() throws ScadaAnnotationException {
            super();

            WireHarp.klugeTheSampleCount(this);
        }

        /**
         * Creates a new instance of <code>DataRaw</code> initialized with the data provided by the
         * given data source.
         *
         * @param daSrc     measurement data source presenting <code>DataAdaptor</code> interface
         * 
         * @throws IllegalAccessException       if an underlying <code>Signal</code> field is inaccessible.
         * @throws ScadaAnnotationException     the <code>ADaqProfile</code> annotations are incomplete
         * @throws IllegalArgumentException     general field incompatibility exception
         *
         * @author Christopher K. Allen
         * @since  Feb 12, 2013
         */
        public DataRaw(DataAdaptor daSrc) throws IllegalArgumentException, IllegalAccessException {
            super(daSrc);

            WireHarp.klugeTheSampleCount(this);
        }


        /**
         * Creates a new instance of <code>DataRaw</code> and initializes the signal values
         * by acquiring data from the given device.
         *
         * @param smfHarp        data acquisition device
         * 
         * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
         * @throws ScadaAnnotationException     the <code>ADaqProfile</code> annotations are incomplete
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         *
         * @author Christopher K. Allen
         * @since  Feb 12, 2013
         */
        public DataRaw(WireHarp smfHarp) throws ConnectionException, GetException, ScadaAnnotationException, BadStructException  {
            super(smfHarp);

//            DaqConfig   cfgDaq = DaqConfig.acquire(smfHarp);
            WireHarp.klugeTheSampleCount(this);
//            WireHarp.cleanTheDataSet(this, cfgDaq);
        }
    }

    /**
     * <p>
     * Structure for maintaining a <code>WireHarp</code> profile data
     * fits.  The instances of this data structure contains the fits to the
     * current profile data as computed by the harp device controller.  The fit
     * data is acquired in the same manner as the actual measurement data, specifically
     * through the SCADA mechanism of <code>{@link ScadaRecord}</code>.
     * </p>
     * <p>  
     * The data structure is derived from the <code>SignalSet</code>
     * base class which does all the work.  
     * The fit data for each plane is available through the attributes of type 
     * <code>{@link Signal}</code> in the <code>WireHarp.SignalSet</code> base class.
     * The PV channels for the fit data are identified with
     * the annotation <code>{@link ASignalSet}</code> which tells the base class how to 
     * connect the <code>{@link Signal}</code> attributes.
     * </p>
     *
     * @author Christopher K. Allen
     * @since  Feb 13, 2013
     *
     */
    @ASignal.ASet( 
            sigHor = @ASignal(hndPosRb = "DatHorFitPositions", hndValRb = "DatHorFitSignal" ),
            sigVer = @ASignal(hndPosRb = "DatVerFitPositions", hndValRb = "DatVerFitSignal" ),
            sigDia = @ASignal(hndPosRb = "DatDiaFitPositions", hndValRb = "DatDiaFitSignal" ) 
            )
    public static class DataFit extends SignalSet {


        /*
         * Global Operations
         */

        /**
         * Convenience method for retrieving a new, initialized measurement set
         * acquired from the given device.
         *
         * @param smfHarp   measurement device
         * 
         * @return          newly acquired data set from the given device
         *  
         * @throws ConnectionException          unable to connect data read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         * @throws ScadaAnnotationException     the <code>ADaqProfile</code> annotations are incomplete
         * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
         *
         * 
         * @since  Nov 14, 2009
         * @author Christopher K. Allen
         */
        public static DataFit   aquire(WireHarp smfHarp) 
                throws ConnectionException, GetException, ScadaAnnotationException, BadStructException 
        {
            return new DataFit(smfHarp);
        }

        /**
         * Creates a new, empty instance of <code>DataFit</code>.
         *
         * @throws ScadaAnnotationException this class is not annotated with <code>ASignal.ASet</code>,
         *                                  or the annotation is corrupt   
         *
         * @author Christopher K. Allen
         * @since  Feb 12, 2013
         */
        public DataFit() throws ScadaAnnotationException {
            super();

            WireHarp.klugeTheSampleCount(this);
        }

        /**
         * Creates a new instance of <code>DataFit</code> initialized with the data provided by the
         * given data source.
         *
         * @param daSrc     measurement data source presenting <code>DataAdaptor</code> interface
         * 
         * @throws IllegalAccessException       if an underlying <code>Signal</code> field is inaccessible.
         * @throws ScadaAnnotationException     the <code>ADaqProfile</code> annotations are incomplete
         * @throws IllegalArgumentException     general field incompatibility exception
         *
         * @author Christopher K. Allen
         * @since  Feb 12, 2013
         */
        public DataFit(DataAdaptor daSrc) throws IllegalArgumentException,
        IllegalAccessException {
            super(daSrc);

            WireHarp.klugeTheSampleCount(this);
        }

        /**
         * Creates a new instance of <code>DataFit</code> and initializes the signal values
         * by acquiring data from the given device.
         *
         * @param smfHarp        data acquisition device
         * 
         * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
         * @throws ScadaAnnotationException     the <code>ADaqProfile</code> annotations are incomplete
         * @throws ConnectionException          unable to connect to a parameter read back channel
         * @throws GetException                 general CA GET exception while fetch field value
         *
         * @author Christopher K. Allen
         * @since  Feb 12, 2013
         */
        public DataFit(WireHarp smfHarp) throws ScadaAnnotationException,
        ConnectionException, GetException, BadStructException
        {
            super(smfHarp);

//            DaqConfig cfgDaq = DaqConfig.acquire(smfHarp);
            
            WireHarp.klugeTheSampleCount(this);
//            WireHarp.cleanTheDataSet(this, cfgDaq);
        }
    }

    /**************************
     * 
     * WireHarp Class
     * 
     */


    /*
     * Global Constants
     */

    /** The type code for this hardware device */
    public static final String      STR_TYPE_ID = "Harp";

    /** software type for the WireHarp class */
    static final public String      SOFTWARE_TYPE = "Version 2.0.0"; 

    /** Hardware type for the WireHarp class */
    static final public String      HARDWARE_TYPE = "wireharp"; 


    /** <b>Kluge</b> - Number of wires on current SNS harps */
    public final static int             CNT_WIRES = 30;


    /*
     * Global Operations
     */

    /**
     * Sets sample size attribute for each signal in the
     * set to the static value <code>{@link #CNT_WIRES}</code>.  The
     * obvious kluge for the fact that there is no dynamic way to get
     * this number.
     * 
     * @param setSigs   the set of <code>Signal</code> objects to be modified
     *
     * @author Christopher K. Allen
     * @since  Jun 5, 2014
     */
    private static void klugeTheSampleCount(SignalSet setSigs) {

        setSigs.hor.cnt = CNT_WIRES;
        setSigs.ver.cnt = CNT_WIRES;
        setSigs.dia.cnt = CNT_WIRES;
    }

    /**
     * This methods processes the given data set removing samples from 
     * invalid wires.  These wires are identified in the DAQ structure
     * provided in the arguments. The given signal set is modified so that
     * the <code>Signal</code> objects within have new <code>Signal#pos</code>
     * and <code>Signal#val</code> objects.
     * 
     * @param setSigs       the set of signals to be processed and modified
     * @param cfgDaq        data structure containing the valid wire info
     *
     * @author Christopher K. Allen
     * @since  Jun 5, 2014
     * 
     * @deprecated  I don't want to throw this away but I don't want anyone
     *              using it yet either.
     */
    @Deprecated
    private static void cleanTheDataSet(SignalSet setSigs, DaqConfig cfgDaq) {

        for (ANGLE angle : ANGLE.values()) {
            int                 cntWires = cfgDaq.getSampleCount(angle);
            int                 cntValid = cfgDaq.validWireCount(angle);

            double[]            arrPos = new double[cntValid];
            double[]            arrVal = new double[cntValid];
            Signal              sigAng = setSigs.getSignal(angle);
            int                 indVld = 0;
            for (int indSmp=0; indSmp<cntWires; indSmp++) {
                if ( !cfgDaq.validWire(angle, indSmp) )
                    continue;


                arrPos[indVld] = sigAng.pos[indSmp];
                arrVal[indVld] = sigAng.val[indSmp];
                indVld++;
            }

            sigAng.cnt = cntValid;
            sigAng.pos = arrPos;
            sigAng.val = arrVal;
        }
    }


    /*
     * SMF Requirements
     */


    /**
     * Register the hardware device types that this class
     * recognizes.
     */
    static {
		ElementTypeManager.defaultManager().registerTypes( WireHarp.class, STR_TYPE_ID, HARDWARE_TYPE );
    }


    /*
     * Initialization
     */

	/**
	 * Primary Constructor
	 * @param strId unique identifier of this node
	 * @param channelFactory factory for generating channels for this node
	 */
	public WireHarp( final String strId, final ChannelFactory channelFactory ) {
		super( strId, channelFactory );
	}

    /**
     * Creates a new instance of <code>WireHarp</code>.
     *
     * @param strId
     *
     * @author Christopher K. Allen
     * @since  Feb 13, 2013
     */
    public WireHarp( final String strId ) {
        this( strId, null );
    }


    /*
     * Node Properties
     */

    /** 
     * Get the device type
     *   
     * @return  The type identifier of this class
     */
    @Override
    public String getType()  { 
        return STR_TYPE_ID; 
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
        final Channel channel = getAndConnectChannel( CMD.HANDLE_RESULT );

        return channel.getArrInt();
    }


}
