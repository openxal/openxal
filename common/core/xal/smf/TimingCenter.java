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
	public TimingCenter() {
		_channelSuite = new ChannelSuite();
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
    public void update(IDataAdaptor adaptor) {		
        // read the channel suites
        IDataAdaptor suiteAdaptor = adaptor.childAdaptor(ChannelSuite.DATA_LABEL);
        if ( suiteAdaptor != null ) {
            _channelSuite.update(suiteAdaptor);
        }
	}
    
    
    /**
     * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write(IDataAdaptor adaptor) {
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

