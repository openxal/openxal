/*
 * StateStore.java
 *
 * Created on Fri Dec 05 16:10:57 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.score;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * StateStore is an interface that persistent storages must implement for publishing and recovering
 * score snapshots
 *
 * @author  tap (copied by jdg from pvlogger)
 */
public interface StateStore {
		
	
	/**
	 * Fetch the score snapshot associated with the type and date identifiers
	 * @param type -  The  score snapshot typei dentifier
	 * @param time -  The  score snapshot timestamp 
	 * @return The score snapshop read from the persistent store.
	 */
	public ScoreSnapshot fetchScoreSnapshot(final String type, final Timestamp time);
	
	/**
	 * Fetch the golden snapshot associated with an equipment_id
	 *
	 * @param type   - the score group label
	 * @return      The score snapshop read from the persistent store.
	 *    or null if there is no golden set yet.
	 * @exception StateStoreException  - Description of the Exception
	 * @throws gov.sns.apps.score.StateStoreException  if a SQL exception is thrown
	 */
	public ScoreSnapshot fetchGoldenSnapshot(final String type);

	
	/** 
	 * publish a score snapshot to the database
	 * @param scoreSnapshot  PV data record 
	 * @return success/failure indicator
	 */
	public boolean publish(final ScoreSnapshot scoreSnapshot);	
	
	
	/**
	 * Fetch the score snapshots within the specified time range.  If the type is not null,
	 * then restrict the score snapshots to those of the specified type.  The score snapshots
	 * do not include the PV date.  A complete snapshot can be obtained using the 
	 * fetchScoreSnapshot(dtype, date) method.
	 * @param type The type of score snapshots to fetch or null for no restriction
	 * @param startTime The start time of the time range
	 * @param endTime The end time of the time range
	 * @return An array of score snapshots meeting the specified criteria
	 */
	public List<ScoreSnapshot> fetchScoreSnapshotsInRange(String type, Date startTime, Date endTime);
	
	
	/**
	 * Fetch an array of valid logger types
	 * @return an array of available logger types
	 */
	public String[] fetchTypes();
	
	
	/**
	 * Fetch a channel group for the specified logger type
	 * @param type the logger type
	 * @return a channel group for the logger type which includes the type, description and the pvs to log
	 */
	public ScoreGroup fetchGroup(final String type);
}

