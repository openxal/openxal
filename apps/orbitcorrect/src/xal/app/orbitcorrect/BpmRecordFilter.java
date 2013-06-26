/*
 *  BpmRecordFilter.java
 *
 *  Created on Wed Sep 08 15:25:40 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;


/**
 * BpmRecordFilter is an interface for filtering BPM records to determine if they should be
 * accepted or rejected.
 *
 * @author   tap
 * @since    Sep 08, 2004
 */
public interface BpmRecordFilter {
	/**
	 * Determine if the BPM record should be accepted.
	 *
	 * @param record  the BPM record to filter
	 * @return        true to accept the record and false to reject it
	 */
	public boolean accept( final BpmRecord record );
}

