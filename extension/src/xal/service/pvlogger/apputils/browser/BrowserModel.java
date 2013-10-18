/*
 * BrowserModel.java
 *
 * Created on Thu Mar 25 08:56:54 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger.apputils.browser;

import xal.tools.ArrayTool;
import xal.service.pvlogger.*;
import xal.tools.database.*;
import xal.tools.messaging.MessageCenter;

import java.sql.*;
import java.util.*;


/**
 * BrowserModel is the main document model.
 *
 * @author  tap
 */
public class BrowserModel {
	final protected MessageCenter MESSAGE_CENTER;
	final protected BrowserModelListener EVENT_PROXY;
	
	protected boolean _hasConnected = false;
	
	protected PVLogger _pvLogger;
	protected String[] _loggerTypes;
	
	protected MachineSnapshot[] _snapshots;
	protected ChannelGroup _group;
	
	
	/**
	 * Constructor
	 */
	public BrowserModel() {
		MESSAGE_CENTER = new MessageCenter( "Browser Model" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, BrowserModelListener.class );
		
		_snapshots = new MachineSnapshot[0];
		_group = null;
	}
	
	
	/**
	 * Add a listener of model events from this model.
	 * @param listener the listener to add for receiving model events.
	 */
	public void addBrowserModelListener( final BrowserModelListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, BrowserModelListener.class );
	}
	
	
	/**
	 * Remove the listener from receiving model events from this model.
	 * @param listener the listener to remove from receiving model events.
	 */
	public void removeBrowserModelListener( final BrowserModelListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, BrowserModelListener.class );
	}
	
	
	/**
	 * Set the database connection to the one specified.
	 * @param connection the new database connection
	 */
	 public void setDatabaseConnection( final Connection connection, final ConnectionDictionary dictionary ) {
		_hasConnected = false;
		
		_group = null;
		_snapshots = new MachineSnapshot[0];
		_loggerTypes = null;
		_pvLogger = new PVLogger( dictionary );
		_hasConnected = true;
		EVENT_PROXY.connectionChanged( this );
	 }
	
	
	/**
	 * Connect to the database with the default connection dictionary
	 * @throws DatabaseException if the connection or schema fetch fails
	 */
	public void connect() throws DatabaseException {
		connect( PVLogger.newBrowsingConnectionDictionary() );
	}
	
	
	/**
	 * Connect to the database with the specified connection dictionary
	 * @param dictionary The connection dictionary
	 * @throws DatabaseException if the connection or schema fetch fails
	 */
	public void connect( final ConnectionDictionary dictionary ) throws DatabaseException {
		final Connection connection = dictionary.getDatabaseAdaptor().getConnection( dictionary );
		setDatabaseConnection( connection, dictionary );
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
	protected String[] fetchLoggerTypes() throws SQLException {
		_loggerTypes = _pvLogger.fetchTypes();
		return _loggerTypes;
	}
	
	
	/**
	 * Get the array of available logger types.
	 * @return the array of available logger types.
	 */
	public String[] getLoggerTypes() throws SQLException {
		return ( _hasConnected && _loggerTypes == null ) ? fetchLoggerTypes() : _loggerTypes;
	}
	
	
	/**
	 * Select the specified channel group corresponding to the logger type.
	 * @param type the logger type identifying the channel group
	 * @return the channel group
	 */
	public ChannelGroup selectGroup( final String type ) throws SQLException {
		if ( type == null ) {
			_group = null;
			EVENT_PROXY.selectedChannelGroupChanged( this, _group );
		}
		else if ( _group == null || !_group.getLabel().equals( type ) ) {
			_group = _pvLogger.getChannelGroup( type );
			EVENT_PROXY.selectedChannelGroupChanged( this, _group );
		}
		return _group;
	}
	
	
	/**
	 * Get the selected channel group
	 * @return the selected channel group
	 */
	public ChannelGroup getSelectedGroup() {
		return _group;
	}
	
	
	/**
	 * Get the array of machine snapshots that had been fetched.
	 * @return the array of machine snapshots
	 */
	public MachineSnapshot[] getSnapshots() {
		return _snapshots;
	}
	
	
	/**
	 * Fetch the machine snapshots that were taken between the selected times.  Only identifier
	 * data is fetched into the machine snapshot.
	 * @param startTime the start time of the range
	 * @param endTime the end time of the range
	 */
	public void fetchMachineSnapshots( final java.util.Date startTime, final java.util.Date endTime ) throws SQLException {
		_snapshots = _pvLogger.fetchMachineSnapshotsInRange( _group.getLabel(), startTime, endTime );
		System.out.println( "Found " + _snapshots.length + " snapshots..." );
		EVENT_PROXY.machineSnapshotsFetched( this, _snapshots );
	}
	
	
	/**
	 * Populate all fetched machine snapshots with all of their data.
	 */
	public void populateSnapshots() throws SQLException {
		for ( int index = 0 ; index < _snapshots.length ; index++ ) {
			populateSnapshot( _snapshots[index] );
		}
	}
	
	
	/**
	 * Populate the machine snapshot with all of its data.
	 * @param snapshot the machine snapshot to populate
	 * @return the machine snapshot that was populated (same object as the parameter)
	 */
	public MachineSnapshot populateSnapshot( final MachineSnapshot snapshot ) throws SQLException {
		return snapshot.getChannelCount() == 0 ? _pvLogger.loadChannelSnapshotsInto( snapshot ) : snapshot;
	}
}

