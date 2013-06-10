//
//  ReadbackSetRecord.java
//  xal
//
//  Created by Tom Pelaia on 4/30/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//
package xal.app.virtualaccelerator;

import java.util.*;

import xal.ca.*;
import xal.smf.*;


/** record to cache and synchronize readback and setpoint values */
public class ReadbackSetRecord {
	/** accelerator node */
	final private AcceleratorNode NODE;
	
	/** readback channel */
	final private Channel READ_CHANNEL;
	
	/** setpoint channel */
	final private Channel SET_CHANNEL;
	
	/** latest setpoint */
	private double _lastSetpoint;
	
	/** latest readback */
	private double _lastReadback;
	
	
	/** Constructor */
	public ReadbackSetRecord( final AcceleratorNode node, final Channel readChannel, final Channel setChannel ) {
		NODE = node;
		READ_CHANNEL = readChannel;
		SET_CHANNEL = setChannel;
	}
	
	
	/** get the accelerator node */
	public AcceleratorNode getNode() {
		return NODE;
	}
	
	
	/** get the readback channel */
	public Channel getReadbackChannel() {
		return READ_CHANNEL;
	}
	
	
	/** get the setpoint channel */
	public Channel getSetpointChannel() {
		return SET_CHANNEL;
	}
	
	
	/** get the last setpoint value */
	public double getLastSetpoint() {
		return _lastSetpoint;
	}
	
	
	/** get the last readback value */
	public double getLastReadback() {
		return _lastReadback;
	}
	
	
	/** update the readback from the setpoint, noise and offset */
	public void updateReadback( final double basisValue, final Map<Channel, Double> noiseMap, final Map<Channel, Double> offsetMap, final PutListener listener ) throws Exception {
		if ( READ_CHANNEL != null && SET_CHANNEL != null ) {
			final Double noise = noiseMap.get( READ_CHANNEL );
			final Double offset = offsetMap.get( READ_CHANNEL );
			final double readBack = noise != null && offset != null ? NoiseGenerator.setValForPV( basisValue, noise, offset ) : basisValue;
			_lastSetpoint = basisValue;
			_lastReadback = readBack;
			READ_CHANNEL.putValCallback( readBack, listener );
		}			
	}
	
	
	/** update the readback from the setpoint, noise and offset */
	public void updateReadback( final Map<Channel, Double> noiseMap, final Map<Channel, Double> offsetMap, final PutListener listener ) throws Exception {
		if ( READ_CHANNEL != null && SET_CHANNEL != null ) {
			final double setPoint = SET_CHANNEL.getValDbl();
			updateReadback( setPoint, noiseMap, offsetMap, listener );
		}			
	}	
}
