/*
 * TimingCenter.java
 *
 * Created on Thu Feb 26 09:36:10 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.smf;

import xal.tools.*;
import xal.tools.data.*;
import xal.smf.data.*;
import xal.ca.*;

import java.util.*;


/**
 * TimingCenter holds the timing channels for the accelerator.
 * @author  tap
 */
public class TimingCenter implements DataListener {
	
    /**
     * Enumeration of the possible values of the Beam Model process variable.
     * These are string values, or indexed integers, so providing an enumeration
     * set helps avoid the brittleness of hard-coding the values.
     *
     * @author Christopher K. Allen
     * @since   Nov 4, 2011
     */
    public enum BEAM_MODE {

        /** Error!  The beam mode is in a condition of unknown state. */
        UNKNOWN("Unknown!"),
        
        /** The beam is off (no beam mode) */
        OFF("Off"),
        
        /** We are testing the Machine Protection System */
        MPSTEST("MPS Test"),
        
        /** The largest possible beam macro-pulse is 10 microseconds long */
        MCRSEC_10("10 uSec"),

        /** The largest possible beam macro-pulse is 50 microseconds long */
        MCRSEC_50("50 uSec"),
        
        /** The largest possible beam macro-pulse is 100 microseconds long */
        MCRSEC_100("100 uSec"),
        
        /** The largest possible beam macro-pulse is 1 milliseconds long */
        MILSEC_1("1 mSec"),
        
        /** We are at full throttle - the beam can be all long as other systems allow */
        FULLPWR("Full Power");
        
        /**
         * Returns the string value of the enumeration constant.
         * 
         * @return  value of the PV which this enumeration constant represents
         *
         * @author Christopher K. Allen
         * @since  Nov 4, 2011
         */
        public String   getPvValue() {
            return this.strVal;
        }
        
        /**
         * Check whether or not the given string is lexically equivalent 
         * to the PV value represented by this enumeration constant
         *
         * @param strPvVal
         * 
         * @return  <code>true</code> if the given string is this beam mode value,
         *          <code>false</code> otherwise
         *
         * @author Christopher K. Allen
         * @since  Nov 4, 2011
         */
        public boolean isPvValue(String strPvVal) {
            int intCmp = this.strVal.compareTo(strPvVal);
            
            return intCmp == 0;
        }

        /** the string value of the PV which this constant represents */
        private String      strVal;
        
        /** 
         * Create a new, initialized <code>BEAM_MODE</code> enumeration
         * constant.
         * 
         * @param strVal    value of the PV which this constant represents
         *
         * @author  Christopher K. Allen
         * @since   Nov 4, 2011
         */
        private BEAM_MODE(String strVal) {
            this.strVal = strVal;
        }
        
    }
	
	static final public String DATA_LABEL = "timing";
	
    /** channel suite associated with this node */
    protected ChannelSuite _channelSuite;
	
    // ------------------- handles ------------------------------------------------
	/** beam trigger PV: 0=Trigger, 1=Counting */
    public static final String TRIGGER_HANDLE = "trigger";
	
	/** beam trigger mode PV: 0=Continuous, 1=Single-shot */
    public static final String MODE_HANDLE = "mode";
	
	/** specify how many beam pulse(s) */
    public static final String COUNTDOWN_HANDLE = "countDown";
	
	/** readback while triggered beam pulses are counting down */
    public static final String COUNT_HANDLE = "count";
	
	/** readback of overall rep rate (Hz) */
    public static final String REP_RATE_HANDLE = "repRate";
	
	/** beam on event */
    public static final String BEAM_ON_EVENT_HANDLE = "beamOnEvent";
	
	/** beam on event counter */
    public static final String BEAM_ON_EVENT_COUNT_HANDLE = "beamOnEventCount";
	
	/** diagnostic demand event */
    public static final String DIAGNOSTIC_DEMAND_EVENT_HANDLE = "diagnosticDemandEvent";
	
	/** diagnostic demand event counter */
    public static final String DIAGNOSTIC_DEMAND_EVENT_COUNT_HANDLE = "diagnosticDemandEventCount";
	
	/** slow (1 Hz) diagnostic event */
    public static final String SLOW_DIAGNOSTIC_EVENT_HANDLE = "slowDiagnosticEvent";
	
	/** slow (1 Hz) diagnostic event counter */
    public static final String SLOW_DIAGNOSTIC_EVENT_COUNT_HANDLE = "slowDiagnosticEventCount";
	
	/** fast (6 Hz) diagnostic event */
    public static final String FAST_DIAGNOSTIC_EVENT_HANDLE = "fastDiagnosticEvent";
	
	/** fast (6 Hz) diagnostic event counter */
    public static final String FAST_DIAGNOSTIC_EVENT_COUNT_HANDLE = "fastDiagnosticEventCount";
	
	/** readback of the ring frequency in MHz */
	public static final String RING_FREQUENCY_HANDLE = "ringFrequency";
	
	/** number of stored turns in the ring */
	public static final String RING_STORED_TURNS_HANDLE = "ringStoredTurns";
	
	/** Machine Mode */
	public static final String MACHINE_MODE_HANDLE = "machineMode";

	/** Ring Energy */
	public static final String RING_ENERGY = "ringEnergy";
	
    /**
     * <p> 
     * CKA 11/04/11: Beam mode specifying maximum pulse length.
     * </p>
     * <p>
     * String values: 
     * <br>    "Off", 
     * <br>    "Standby", 
     * <br>    "MPS Test", 
     * <br>    "10 uSec", 
     * <br>    "50 uSec", 
     * <br>    "100 uSec",
     * <br>    "1 mSec", 
     * <br>    "Full Power", 
     * <br>    "Unknown!"
     * </p>
     */
	public static final String BEAM_MODE_HANDLE ="beamMode";
	
	/** Active Flavor */
	public static final String ACTIVE_FLAVOR_HANDLE = "activeFlavor";
	
	/** beam reference gate width (Turns) */
	public static final String BEAM_REFERENCE_GATE_WIDTH = "beamReferenceGateWidth";
	
	/** actual chopper delay (Turns) */
	public static final String CHOPPER_DELAY = "chopperDelay";
	
	/** actual chopper beam on (Turns) */
	public static final String CHOPPER_BEAM_ON = "chopperBeamOn";
	
	
	/**
	 * Create an empty TimingCenter
	 */
	public TimingCenter( final ChannelFactory channelFactory ) {
		_channelSuite = channelFactory != null ? new ChannelSuite( channelFactory ) : new ChannelSuite();
	}


	/**
	 * Create an empty TimingCenter
	 */
	public TimingCenter() {
		this( null );
	}

	
	/**
	 * Get the default TimingCenter corresponding to the user's default main optics source
	 * @return the default TimingCenter or null if no default has been specified
	 * @throws xal.tools.ExceptionWrapper if an exception occurs while parsing the data source
	 */
	static public TimingCenter getDefaultTimingCenter() throws ExceptionWrapper {
		XMLDataManager dataManager = XMLDataManager.getDefaultInstance();
		return (dataManager != null) ? dataManager.getTimingCenter() : null;
	}
	
	
    /** 
     * dataLabel() provides the name used to identify the class in an 
     * external data source.
     * @return a tag that identifies the receiver's type
     */
    public String dataLabel() {
		return DATA_LABEL;
	}
    
    
    /**
     * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update(DataAdaptor adaptor) {		
        // read the channel suites
        DataAdaptor suiteAdaptor = adaptor.childAdaptor(ChannelSuite.DATA_LABEL);
        if ( suiteAdaptor != null ) {
            _channelSuite.update(suiteAdaptor);
        }
	}
    
    
    /**
     * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write(DataAdaptor adaptor) {
        adaptor.writeNode(_channelSuite);
	}
	
	
	/**
	 * Get this timing center's channel suite
	 * @return this timing center's channel suite
	 */
	public ChannelSuite getChannelSuite() {
		return _channelSuite;
	}
    
    
    /** accessor to channel suite handles */
    public Collection<String> getHandles() {
        return _channelSuite.getHandles();
    }
	
    
    /**
     * Find the channel for the specified handle.
     * @param handle The handle for the channel to get.
     * @return The channel for the specified handle or null if there is no match.
     */
    public Channel findChannel( final String handle ) {
		return _channelSuite.getChannel( handle );
    }
	
	
    /** 
	 * Get the Channel of this timing center, for the specified channel handle.
     * @param handle The handle to the desired channel stored in the channel suite
     */
    public Channel getChannel( final String handle ) throws NoSuchChannelException {
        final Channel channel = findChannel( handle );
        
        if ( channel == null ) {
            throw new NoSuchChannelException( this, handle );
        }
        
        return channel;
    }
    
    
    /**
     * Get the channel corresponding to the specified handle and connect it. 
     * @param handle The handle for the channel to get.
     * @return The channel associated with this node and the specified handle or null if there is no match.
     * @throws xal.smf.NoSuchChannelException if no such channel as specified by the handle is associated with this node.
     * @throws xal.ca.ConnectionException if the channel cannot be connected
     */
    public Channel getAndConnectChannel(String handle) throws NoSuchChannelException, ConnectionException {
        Channel channel = getChannel(handle);
        channel.connectAndWait();
        
        return channel;
    }
}

