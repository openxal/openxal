/*
 * ScoreDataModel.java
 *

 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.score;

import xal.tools.database.*;

import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.Timestamp;


/**
 * ScoreDataModel is the model for fetching / inserting score data
*  to/from the Oracle database
 * This layer hides details of database communication (e.g. sql queries)
 * from the score level.
 *
 * @author  jdg
 */
public class ScoreDataModel {    
    /** array of scoresnapshots - only populated with comment, date and type header info */
    private List<ScoreSnapshot> _snapshots;
	
    /** database connection flag */
    protected boolean _hasConnected = false;
    
    /** snapshot data persistence */
    protected StateStore _scoreStore;
    
    /** score types */
    protected String[] _scoreTypes;
    
    /** a single fully populated scoresnapshot */
    protected ScoreSnapshot _fetchedSnapshot;
    
    /** the type of Score snapshot to look for */
    protected String _type;
    
    /** the score group that has been fetched */
    protected ScoreGroup _fetchedGroup;
    
    /** the document this model belongs to */
    ScoreDocument theDoc;
    
	
    /**
     * Constructor
     * @param doc application data
     */
    public ScoreDataModel(ScoreDocument doc) {
		_snapshots = new ArrayList<ScoreSnapshot>();
		theDoc = doc;
    }
    
	
    /**
     * Set the database connection to the one specified.
     * @param connection the new database connection
     * @param dictionary parameters needed to establish database connection
     */
	public void setDatabaseConnection( final Connection connection, final ConnectionDictionary dictionary ) {
		_hasConnected = false;
		_snapshots.clear();
		_scoreTypes = null;
		_scoreStore = new SqlStateStore(dictionary, connection, theDoc);
		_hasConnected = true;
	}
    
    /**
     * Connect to the database with the default connection dictionary
     * @throws DatabaseException if the connection or schema fetch fails
     */
    public void connect() throws DatabaseException {
		connect( newConnectionDictionary() );
    }
    
    /**
     * Connect to the database with the specified connection dictionary
     * @param dictionary The connection dictionary
     * @throws DatabaseException if the connection or schema fetch fails
     */
    public void connect( final ConnectionDictionary dictionary ) throws DatabaseException {
		Connection connection = DatabaseAdaptor.getInstance().getConnection( dictionary );
		setDatabaseConnection(connection, dictionary);
    }
	
	
	/** generate a new connection dictionary for publishing score snapshots if available; otherwise one for reading */
	static public ConnectionDictionary newConnectionDictionary() {
		// use the "score" account if available, otherwise "reports" and default as last resort
		return ConnectionDictionary.getPreferredInstance( "score", "reports" );
	}
	
	
    /**
     * Determine if we have successfully connected to the database.  Note that this does
     * not mean that the database connection is still valid.
     * @return true if we have successfully connected to the database and false if not
     */
    public boolean hasConnected() {
		return _hasConnected;
    }
    
    
    /**
     * Fetch the available logger types from the data store.
     * @return an array of available logger types.
     */
    protected String[] fetchScoreTypes() {
		_scoreTypes = _scoreStore.fetchTypes();
		return _scoreTypes;
    }
    
    /**
     * Get the array of available logger types.
     * @return the array of available logger types.
     */
    public String[] getScoreTypes() {
		return (_hasConnected && _scoreTypes == null) ? fetchScoreTypes() : _scoreTypes;
    }
    
	
    /**
     * Get the array of machine snapshots that had been fetched.
     * @return the array of machine snapshots
     */
    public List<ScoreSnapshot> getSnapshots() {
		return _snapshots;
    }
    
	
    /** return a single snapshot from the array of stashed ones 
     * @param index =  the index of the snapshot array to get (from 0)
     * @return machine state at the time of the snapshot
     */
    public ScoreSnapshot getSnapshot( final int index) {
		return _snapshots.size() > index ? _snapshots.get( index ) : null;
    }
	
	
	/** get the number of snapshots fetched */
	public int getSnapshotCount() {
		return _snapshots.size();
	}
	
    
    /**
     * Get the fully populated machine snapshot that has been fetched.
     * @return machine snapshot
     */
    public ScoreSnapshot getFetchedSnapshot() {
		return _fetchedSnapshot;
    }       
    /**
     * Fetch the machine snapshots that were taken between the selected times.
     * The snapshot PV data is not fetched here- only the header info
     *
     * @param groupName = name of the group type
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     */
    public void fetchScoreSnapshots( String groupName, java.util.Date startTime, java.util.Date endTime) {
		_snapshots = _scoreStore.fetchScoreSnapshotsInRange( groupName, startTime, endTime );
		System.out.println("Found " + _snapshots.size() + " snapshots...");
    }
	
    /**
     * Fetch the score snapshot for a specific type and date
     * the full populated data set is returned.
     * data is fetched into the machine snapshot.
     * @param time time of the snapshot
     * @param groupName - the type of this snapshot group
     */
    public void fetchScoreSnapshot(Timestamp time, String groupName) {
		_fetchedSnapshot = _scoreStore.fetchScoreSnapshot(groupName, time);             
		System.out.println("Found  snapshot...");
    }
	
    /**
     * Fetch the score golden snapshot for a specific type
     * the full populated data set is returned.
     * data is fetched into the machine snapshot. 
     * the machine snapshot is set to null if there is no golden set saved yet.
     * @param groupName - the type of this snapshot group
     */
    public void fetchGoldenSnapshot(String groupName) {
		_fetchedSnapshot = _scoreStore.fetchGoldenSnapshot(groupName);          
		if (_fetchedSnapshot != null) System.out.println("Found golden snapshot...");
    }
    
    /**
     * Select the specified channel group corresponding to the logger type.
     * @param type the logger type identifying the channel group
     */
    public void setType(final String type) {
		_type = type;
    }
    
    /** 
     * get the selected type
     *  
     * @return string type identifier for data record
     */
    public String getType(){ return _type;}
    
    /** 
     * get the fetched score group 
     * @param type string type identifier for data record
     */
    public void fetchScoreGroup(String type) {
		_fetchedGroup = _scoreStore.fetchGroup(type);           
		System.out.println("Found  group...");
    }
	
    /** 
     * get the fetched score group 
     * @return data record for the (internally specified) data group 
     */
    public ScoreGroup getFetchedScoreGroup() {return _fetchedGroup;}
    
    /** 
     * publish a snapshot to the database
     *  
     * @param ss  data snapshot 
     */
    public void saveSnapshot(ScoreSnapshot ss) {
		if(_scoreStore.publish(ss))
			theDoc.postElogSaveEntry((new java.util.Date()).toString());
    }
	
}

