/*
 *  SignalSource.java
 *
 *  Created on Thu Mar 04 16:07:08 EST 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.service.mpstool;

import xal.tools.database.*;

import java.util.*;


/**
 * SignalSource is the interface to implement for a source of MPS signals.
 *
 * @author   tap
 */
public interface SignalSource {
	/**
	 * Fetch MPS signals for the specified MPS latch type.
	 * @param type                The MPS signal latch type (e.g. "FPL" or "FPAR").
	 * @return                    The array of MPS PVs
	 * @throws DatabaseException  if the fetch fails
	 */
	public String[] fetchMPSSignals( final String type ) throws DatabaseException;


	/**
	 * Fetch a map of input monitors keyed by their associated MPS signal.
	 * @param type                The MPS signal latch type (e.g. "FPL" or "FPAR").
	 * @return                    The input monitor map keyed by MPS signal.
	 * @throws DatabaseException  if the fetch fails
	 */
	public Map<String,InputMonitor> fetchInputMonitors( final String type ) throws DatabaseException;


	/**
	 * Publish the MPS daily statistics to the database.
	 * @param day                    The day for which the statistics were gathered
	 * @param statistics             The daily trip statistics
	 * @exception DatabaseException  if the publish attempt fails
	 */
	public void publishDailyStatistics( final java.util.Date day, final Collection<TripStatistics> statistics ) throws DatabaseException;
}

