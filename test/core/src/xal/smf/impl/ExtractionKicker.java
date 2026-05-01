//
//  VerticalKicker.java
//  xal
//
//  Created by Tom Pelaia on 1/9/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.smf.impl;

import xal.ca.*;
import xal.smf.*;
import xal.smf.impl.qualify.*;


/** the extraction kicker represents a pulsed magnet for extracting the beam vertically from the ring */
public class ExtractionKicker extends Dipole {
	/** node type */
    public static final String s_strType   = "EKick";
    
    /** voltage readback handle */
    public static final String VOLTAGE_RB_HANDLE = "voltageRB";
    
    /** voltage setting handle */
    public static final String VOLTAGE_SET_HANDLE = "voltageSet";
	
    
	// static initializer
    static {
        registerType();
    }


	/** Primary Constructor */
	public ExtractionKicker( final String strId, final ChannelFactory channelFactory )     {
		super( strId, channelFactory );
	}


	/** Constructor */
    public ExtractionKicker( final String strId )     { 
        this( strId, null );
    }
	
    
    /** Register type for qualification */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( ExtractionKicker.class, s_strType, "kicker", "vertkicker", "extractionkicker" );
    }
	
	
    /** Override to provide type signature */
    public String getType()   { return s_strType; };
	
    
    /**
	 * Get the orientation of the magnet as defined by MagnetType.  The orientation of all vertical correctors is VERTICAL.
     * @return VERTICAL
     */
    public int getOrientation() {
        return VERTICAL;
    }
    
    
    /**
	 * Determine whether this magnet is a corrector.
     * @return false     
	 */
    public boolean isCorrector() {
        return false;
    }
	
    /**
     * Get the voltage readback for this kicker's power supply.
     * @return the power supply voltage readback
     */
    public double getVoltage() throws ConnectionException, GetException {
        final Channel channel = getAndConnectChannel( VOLTAGE_RB_HANDLE );
        return channel.getValDbl();
    }
    
    
    /**
     * Get the voltage setting for this kicker's power supply.
     * @return the power supply voltage setting
     */
    public double getVoltageSetting() throws ConnectionException, GetException {
        final Channel channel = getAndConnectChannel( VOLTAGE_SET_HANDLE );
        return channel.getValDbl();
    }
    
    
    /**
     * Set this kicker's power supply voltage
     * @param voltage is the new voltage to apply to the power supply.
     */
    public void setVoltage( final double voltage ) throws ConnectionException, PutException {
        final Channel channel = getAndConnectChannel( VOLTAGE_SET_HANDLE );
        channel.putVal( voltage );
    }
}
