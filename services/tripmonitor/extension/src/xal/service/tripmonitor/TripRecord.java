//
//  TripRecord.java
//  xal
//
//  Created by Thomas Pelaia on 8/2/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import java.util.Date;
import java.util.Comparator;

import xal.ca.Timestamp;


/** record of a trip */
public class TripRecord {
	/** PV which indicates the trip */
	final protected String PV;
	
	/** the timestamp of the trip */
	final protected Timestamp TIME_STAMP;
	
	/** value */
	final int VALUE;
	
	
	/** Primary Constructor */
	public TripRecord( final String pv, final Timestamp timeStamp, final int value ) {
		PV = pv;
		TIME_STAMP = timeStamp;
		VALUE = value;
	}
	
	
	/** Constructor */
	public TripRecord( final String pv, final java.sql.Timestamp sqlTimestamp ) {
		this( pv, new Timestamp( sqlTimestamp ), -1 );
	}
	
	
	/** get the trip record from the record map */
	static public TripRecord getInstanceFromRecordMap( final java.util.HashMap<String, Object> recordMap ) {
		final String pv = (String)recordMap.get( TripMonitorPortal.PV_KEY );
		final long time = ((Date)recordMap.get( TripMonitorPortal.TIMESTAMP_KEY )).getTime();
		
		return new TripRecord( pv, new java.sql.Timestamp( time ) );
	}
	
	
	/** Get the PV */
	public String getPV() {
		return PV;
	}
	
	
	/** Get the time stamp */
	public Timestamp getTimeStamp() {
		return TIME_STAMP;
	}
	
	
	/** Get the time stamp */
	public Date getDate() {
		return TIME_STAMP.getDate();
	}
	
	
	/** Get the time stamp */
	public java.sql.Timestamp getSQLTimestamp() {
		return TIME_STAMP.getSQLTimestamp();
	}	
	
	
	/** Get the value of the PV */
	public int getValue() {
		return VALUE;
	}
	
	
	/** get a description of this record */
	public String toString() {
		return "PV:  " + PV + ", Timestamp:  " + TIME_STAMP + ", Value:  " + VALUE;
	}
	
	
	/** determine if the specified trip record is equal to this one */
	public boolean equals( final Object tripRecord ) {
		if ( tripRecord != null && tripRecord instanceof TripRecord ) {
			final TripRecord record = (TripRecord)tripRecord;
			return record.TIME_STAMP.equals( TIME_STAMP ) && record.PV.equals( PV );
		}
		else {
			return false;
		}
	}


	/** override hashCode to be consistent with equals() */
	public int hashCode() {
		return TIME_STAMP.hashCode() + PV.hashCode();
	}
	
	
	/** get the timestamp based comparator */
	static public Comparator<TripRecord> timestampComparator() {
		return new Comparator<TripRecord>() {
			public int compare( final TripRecord record1, final TripRecord record2 ) {
				return record1.TIME_STAMP.compareTo( record2.TIME_STAMP );
			}
			
			public boolean equals( final Object comparator ) {
				return this == comparator;
			}
		};
	}
	
	
	/** get the PV based comparator */
	static public Comparator<TripRecord> pvComparator() {
		return new Comparator<TripRecord>() {
			/** compare first by PV and then by time stamp */
			public int compare( final TripRecord record1, final TripRecord record2 ) {
				final int result = record1.PV.compareTo( record2.PV );
				return result != 0 ? result : record1.TIME_STAMP.compareTo( record2.TIME_STAMP );
			}
			
			public boolean equals( final Object comparator ) {
				return this == comparator;
			}
		};
	}
}
