/*
 *  BpmRecord.java
 *
 *  Created on Fri Jul 09 16:05:09 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import java.util.*;

import xal.smf.*;
import xal.smf.impl.BPM;
import xal.tools.data.*;


/**
 * BpmRecord
 * @author   tap
 * @since    Jul 09, 2004
 */
public class BpmRecord extends BeamMarkerRecord<BPM> implements DataListener {
	/** label for data storage */
	final static public String DATA_LABEL = "BPMRecord";
	
	
	/**
	 * Primary constructor
	 * @param bpmAgent        the BPM agent for which the record is made
	 * @param timestamp  the time of the BPM data
	 * @param xAvg       the average x position
	 * @param yAvg       the average y position
	 * @param ampAvg     the average signal amplitude
	 */
	public BpmRecord( final BpmAgent bpmAgent, final Date timestamp, final double xAvg, final double yAvg, final double ampAvg ) {
		super( bpmAgent, timestamp, xAvg, yAvg, ampAvg );
	}
	
	
	/**
	 * Constructor
	 * @param bpmAgent        the BPM agent for which the record is made
	 * @param timestamp  the time of the BPM data
	 * @param xAvg       the average x position
	 * @param yAvg       the average y position
	 */
	public BpmRecord( final BpmAgent bpmAgent, final Date timestamp, final double xAvg, final double yAvg ) {
		this( bpmAgent, timestamp, xAvg, yAvg, Double.NaN );
	}
	
	
	/**
	 * Constructor
	 * @param record     another BPM record
	 * @param timestamp  the time of the BPM data
	 * @param xAvg       the average x position
	 * @param yAvg       the average y position
	 */
	public BpmRecord( final BpmRecord record, final Date timestamp, final double xAvg, final double yAvg ) {
		this( record.getBpmAgent(), timestamp, xAvg, yAvg, Double.NaN );
	}
	
	
	/**
	 * Constructor using the current timestamp and zero for BPM data
	 * @param bpmAgent  the BPM agent for which the record is made
	 */
	public BpmRecord( final BpmAgent bpmAgent ) {
		this( bpmAgent, new Date(), 0.0, 0.0 );
	}
	
	
	/** produce a BpmRecord from a BeamMarkerRecord assuming that the beam marker record has a BpmAgent as the beam marker */
	static protected BpmRecord getInstance( final BeamMarkerRecord<BPM> record ) {
		return new BpmRecord( (BpmAgent)record.getBeamMarker(), record.getTimestamp(), record.getXAvg(), record.getYAvg(), record.getAmpAvg() );
	}
	
	
	/**
	 * Calculate the difference between a primary BPM record and a reference BPM record.
	 * @param primaryRecord    The primary record
	 * @param referenceRecord  The reference record
	 * @return                 The difference between the primary record and reference record
	 */
	static public BpmRecord calcDifference( final BpmRecord primaryRecord, final BpmRecord referenceRecord ) {
		return getInstance( BeamMarkerRecord.calcDifference( primaryRecord, referenceRecord ) );
	}
	
	
	/**
	 * Get the BPM agent for which this record was generated
	 * @return   this record's BPM agent
	 */
	public BpmAgent getBpmAgent() {
		return (BpmAgent)getBeamMarker();
	}
	
	
	/**
	 * Get the unique identifier for the enclosed BPM
	 * @return   the ID of the record's BPM
	 */
	public String getBpmID() {
		return getNodeID();
	}
	
	
    /** 
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    public String dataLabel() {
		return DATA_LABEL;
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		TIME_STAMP = new Date( adaptor.longValue( "timestamp" ) );
		X_AVG = adaptor.doubleValue( "XAVG" );
		Y_AVG = adaptor.doubleValue( "YAVG" );
		AMP_AVG = adaptor.doubleValue( "AMPAVG" );
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "BPM", BEAM_MARKER.getID() );
		adaptor.setValue( "timestamp", TIME_STAMP.getTime() ); 
		adaptor.setValue( "XAVG", X_AVG );
		adaptor.setValue( "YAVG", Y_AVG );
		adaptor.setValue( "AMPAVG", AMP_AVG );
	}
}

