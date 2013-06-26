//
//  BeamMarkerRecord.java
//  xal
//
//  Created by Tom Pelaia on 1/4/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import java.util.*;

import xal.tools.text.FormattedNumber;
import xal.smf.*;
import xal.smf.AcceleratorNode;
import xal.tools.data.*;


/** record of beam displacement at a beam marker */
public class BeamMarkerRecord<NodeType extends AcceleratorNode> {
	/** the Beam Marker */
	final protected BeamMarker<NodeType> BEAM_MARKER;
	
	/** the time when the data was taken */
	protected Date TIME_STAMP;
	
	/** the average X value over the beam pulse at the marker */
	protected double X_AVG;
	
	/** the average Y value over the beam pulse at the marker */
	protected double Y_AVG;
	
	/** the average signal amplitude over the beam pulse at the marker */
	protected double AMP_AVG;
	
	
	/**
	 * Primary constructor
	 * @param beamMarker        the node agent for which the record is made
	 * @param timestamp  the time of the node data
	 * @param xAvg       the average x position
	 * @param yAvg       the average y position
	 * @param ampAvg     the average signal amplitude
	 */
	public BeamMarkerRecord( final BeamMarker<NodeType> beamMarker, final Date timestamp, final double xAvg, final double yAvg, final double ampAvg ) {
		BEAM_MARKER = beamMarker;
		TIME_STAMP = timestamp;
		X_AVG = xAvg;
		Y_AVG = yAvg;
		AMP_AVG = ampAvg;
	}
	
	
	/**
	 * Constructor
	 * @param beamMarker        the node agent for which the record is made
	 * @param timestamp  the time of the node data
	 * @param xAvg       the average x position
	 * @param yAvg       the average y position
	 */
	public BeamMarkerRecord( final BeamMarker<NodeType> beamMarker, final Date timestamp, final double xAvg, final double yAvg ) {
		this( beamMarker, timestamp, xAvg, yAvg, Double.NaN );
	}
	
	
	/**
	 * Constructor
	 * @param record  another beam marker record
	 * @param timestamp  the time of the node data
	 * @param xAvg       the average x position
	 * @param yAvg       the average y position
	 */
	public BeamMarkerRecord( final BeamMarkerRecord<NodeType> record, final Date timestamp, final double xAvg, final double yAvg ) {
		this( record.getBeamMarker(), timestamp, xAvg, yAvg, Double.NaN );
	}
	
	
	/**
	 * Constructor using the current timestamp and zero for node data
	 * @param beamMarker  the node agent for which the record is made
	 */
	public BeamMarkerRecord( final BeamMarker<NodeType> beamMarker ) {
		this( beamMarker, new Date(), 0.0, 0.0 );
	}
	
	
	/**
	 * Calculate the difference between a primary node record and a reference node record.
	 * @param primaryRecord    The primary record
	 * @param referenceRecord  The reference record
	 * @return                 The difference between the primary record and reference record
	 */
	static public <ParamNodeType extends AcceleratorNode> BeamMarkerRecord<ParamNodeType> calcDifference( final BeamMarkerRecord<ParamNodeType> primaryRecord, final BeamMarkerRecord<ParamNodeType> referenceRecord ) {
		final BeamMarker<ParamNodeType> beamMarker = primaryRecord.getBeamMarker();
		final Date timestamp = primaryRecord.getTimestamp();
		final double xAvg = primaryRecord.getXAvg() - referenceRecord.getXAvg();
		final double yAvg = primaryRecord.getYAvg() - referenceRecord.getYAvg();
		final double ampAvg = primaryRecord.getAmpAvg() - referenceRecord.getAmpAvg();
		
		return new BeamMarkerRecord<ParamNodeType>( beamMarker, timestamp, xAvg, yAvg, ampAvg );
	}
	
	
	/**
	 * Get the Beam marker for which this record was generated
	 * @return   this record's beam marker
	 */
	public BeamMarker<NodeType> getBeamMarker() {
		return BEAM_MARKER;
	}
	
	
	/**
	 * Get the position of the node relative to the start of the specified sequence.
	 * @param sequence  The sequence relative to which the node's position is measured
	 * @return          the position of the node relative to the sequence in meters
	 */
	public double getPositionIn( final AcceleratorSeq sequence ) {
		return BEAM_MARKER.getPositionIn( sequence );
	}
	
	
	/**
	 * Get the unique identifier for the enclosed node
	 * @return   the ID of the record's node
	 */
	public String getNodeID() {
		return BEAM_MARKER.getID();
	}
	
	
	/**
	 * Get the timestamp of this record
	 * @return   the timestamp of this record
	 */
	public Date getTimestamp() {
		return TIME_STAMP;
	}
	
	
	/**
	 * Get the average value of X
	 * @return   the average value of the X field for the node
	 */
	public double getXAvg() {
		return X_AVG;
	}
	
	
	/** Get X Average as a formatted number */
	public FormattedNumber getFormattedXAvg() {
		return new FormattedNumber( "0.0", getXAvg() );
	}
	
	
	/**
	 * Get the average value of Y
	 * @return   the average value of the Y field for the node
	 */
	public double getYAvg() {
		return Y_AVG;
	}
	
	
	/** Get X Average as a formatted number */
	public FormattedNumber getFormattedYAvg() {
		return new FormattedNumber( "0.0", getYAvg() );
	}
	
	
	/**
	 * Get the average value of the node amplitude
	 * @return   the average value of the amplitude field for the node
	 */
	public double getAmpAvg() {
		return AMP_AVG;
	}
	
	
	/**
	 * Generate a string representation of this record
	 * @return   a string representation of this record
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append( BEAM_MARKER.getID() );
		buffer.append( ", timestamp: " + TIME_STAMP );
		buffer.append( ", xAvg: " + X_AVG );
		buffer.append( ", yAvg: " + Y_AVG );
		buffer.append( ", ampAvg: " + AMP_AVG );
		
		return buffer.toString();
	}	
}
