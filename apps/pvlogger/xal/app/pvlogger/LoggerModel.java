/*
 * LoggerModel.java
 *
 * Created on Thu Jan 15 09:52:39 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogger;

import xal.service.pvlogger.*;
import xal.tools.services.*;
import xal.tools.data.*;
import xal.tools.messaging.MessageCenter;

import java.util.*;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


/**
 * LoggerModel is the main model for the pvlogger client.
 *
 * @author  tap
 */
public class LoggerModel implements DataKeys {
	// static constants
	static final Collection<DataAttribute> DATA_ATTRIBUTES;
	static final SortOrdering SORT_ORDERING;
	
	// messaging
	private final MessageCenter MESSAGE_CENTER;
	private final LoggerModelListener POST_PROXY;
	
	// state variables
	protected Timer timer;
	protected Map<String,LoggerHandler> _serviceTable;
	final protected DataTable dataTable;
	
	
	/**
	 * static initialization
	 */
	static {
		DATA_ATTRIBUTES = makeDataAttributes();
		SORT_ORDERING = new SortOrdering( ID_KEY );
	}
	
	
	/**
	 * LoggerModel Constructor
	 */
	public LoggerModel() {
		MESSAGE_CENTER = new MessageCenter("LoggerModel");
		POST_PROXY = MESSAGE_CENTER.registerSource(this, LoggerModelListener.class);
		
		_serviceTable = new Hashtable<String,LoggerHandler>();
		dataTable = new DataTable( "LoggerStatus", DATA_ATTRIBUTES );
		
		initTimer();
		monitorLoggers();
		System.out.println("monitoring loggers...");
	}
	
	
	/**
	 * Dispose of the logger model.  Stop the timer and dispose of the service directory.
	 */
	protected void dispose() {
		timer.stop();
		ServiceDirectory.defaultDirectory().dispose();
		System.out.println("disposed of model...");
	}
	
	
	/**
	 * Initialize the timer which periodically checks for the status of the known applications.
	 */
	protected void initTimer() {
		timer = new Timer(5000, new ActionListener() {
			public void actionPerformed(ActionEvent event) {				
				// copy set to avoid a possible concurrent modification exception				
				final Set<String> services = new HashSet<String>( _serviceTable.keySet() );
				
				// if there are services, update the logger table for each service
				for ( final String serviceID : services ) {
					final LoggerHandler handler = getHandler( serviceID );
					final GenericRecord record = getRecord( serviceID );
					
					if ( handler == null )  continue;			
					
					new Thread( new Runnable() {
						public void run() {
							if ( handler.update(record) ) {
								synchronized(dataTable) {
									POST_PROXY.newLoggerStatus(LoggerModel.this, record);
								}
							}
						}
					}).start();
				}
			}
		});
		
		timer.setInitialDelay(1000);
		timer.start();
	}
	
	
	/**
	 * Refresh the data table of services.  This is usually done in response to a service
	 * being added or removed or during initialization of a view.
	 */
	public void updateServiceList() {
		// copy set to avoid a possible concurrent modification exception				
		final Set<String> services = new HashSet<String>( _serviceTable.keySet() );
		
		// if there are no services, be sure to clear the MPS table
		if ( services.isEmpty() ) {
			POST_PROXY.loggersChanged( this, Collections.<GenericRecord>emptyList() );
			return;
		}

		for ( final String serviceID : services ) {
			final LoggerHandler handler = getHandler( serviceID );
			final GenericRecord record = getRecord( serviceID );
			
			if ( handler == null )  continue;			
			
			new Thread( new Runnable() {
				public void run() {
					if ( handler.update( record ) ) {
						synchronized(dataTable) {
							if ( record != getRecord( serviceID ) ) {
								dataTable.add( record );
							}
							POST_PROXY.loggersChanged( LoggerModel.this, dataTable.getRecords( SORT_ORDERING ) );
						}
					}
				}
			}).start();
		}
	}
	
	
	/**
	 * Get the update period for the timer.
	 * @return The update period in seconds.
	 */
	public int getUpdatePeriod() {
		return timer.getDelay() / 1000;
	}
	
	
	/**
	 * Set the update period for the timer.
	 * @param period The update period in seconds.
	 */
	public void setUpdatePeriod(int period) {
		timer.setDelay(period*1000);
		timer.restart();
	}
	
	
	/**
	 * Get the handler corresponding to the unique logger identifier.
	 * @param serviceID The unique logger identifier.
	 * @return The handler for the specified application.
	 */
	protected LoggerHandler getHandler(String serviceID) {
		synchronized(_serviceTable) {
			return _serviceTable.get(serviceID);
		}
	}
	
	
	/**
	 * Get the record corresponding to the unique application identifier.
	 * @param serviceID The unique application identifier.
	 * @return The data record for the specified application.
	 */
	protected GenericRecord getRecord(String serviceID) {
		GenericRecord record = dataTable.record(ID_KEY, serviceID);
		if ( record == null ) {
			record = new GenericRecord(dataTable);
		}
		return record;
	}
	
	
	/**
	 * Add a listener of events from this model.
	 * @param listener The listener to register to receiver events from this model.
	 */
	public void addLoggerModelListener(LoggerModelListener listener) {
		MESSAGE_CENTER.registerTarget(listener, this, LoggerModelListener.class);
	}
	
	
	/**
	 * Remove a listener of events from this model.
	 * @param listener The listener to remove from receiving events from this model.
	 */
	public void removeLoggerModelListener(LoggerModelListener listener) {
		MESSAGE_CENTER.removeTarget(listener, this, LoggerModelListener.class);
	}
	
	
	
	/** get an instance of a service listener for the remote logging */
	private ServiceListener getServiceListenerInstance() {
		return new ServiceListener() {
			/**
			 * Handle a new service being added
			 * @param directory The service directory.
			 * @param serviceRef A reference to the new service.
			 */
			public void serviceAdded( final ServiceDirectory directory, final ServiceRef serviceRef ) {
				RemoteLogging proxy = directory.getProxy( RemoteLogging.class, serviceRef );
				synchronized(_serviceTable) {
					final String serviceID = serviceRef.getRawName();
					final LoggerHandler handler = new LoggerHandler( serviceID, proxy );
					_serviceTable.put( serviceID, handler );
				}
				updateServiceList();
				timer.restart();
			}
			
			/**
			 * Handle a service being removed
			 * @param directory The service directory.
			 * @param name The unique name of the service.
			 */
			public void serviceRemoved(ServiceDirectory directory, String type, String name) {
				synchronized(_serviceTable) {
					_serviceTable.remove(name);
					synchronized(dataTable) {
						// check if we have the record and if so then remove it
						GenericRecord record = dataTable.record(ID_KEY, name);
						if ( record != null ) {
							dataTable.remove( dataTable.record(ID_KEY, name) );
						}
					}
				}
				updateServiceList();
				timer.restart();
			}
		};
	}

	
	/**
	 * Monitor the addition and removal of XAL applications.
	 */
	public void monitorLoggers() {
		ServiceDirectory.defaultDirectory().addServiceListener( RemoteLogging.class, getServiceListenerInstance() );
	}
	
	
	/**
	 * Make the attributes which are the keys to the data table.  The data table holds all of the records
	 * of data about the applications on this network.  Each record corresponds to a single application.
	 * Each attribute corresponds to a single piece of information about an application.
	 * @return The collection of attributes of the data table.
	 */
	static protected Collection<DataAttribute> makeDataAttributes() {
		final List<DataAttribute> attributes = new ArrayList<DataAttribute>();
		attributes.add( new DataAttribute(ID_KEY, String.class, true) );
		attributes.add( new DataAttribute(LAUNCH_TIME_KEY, String.class, false) );
		attributes.add( new DataAttribute(HOST_KEY, String.class, false) );
		attributes.add( new DataAttribute(SERVICE_OKAY_KEY, Boolean.class, false) );
		
		return attributes;
	}
	
	
	/** 
	 * publish snapshots 
	 * @param record corresponding to the PV Logger for which to publish snapshots
	 */
	void publishSnapshots( final GenericRecord record ) {
		String serviceID = record.stringValueForKey( ID_KEY );
		final LoggerHandler handler = _serviceTable.get( serviceID );
		
		new Thread(new Runnable() {
			public void run() {
				handler.publishSnapshots();
			}
		}).start();
	}
	
	
	/**
	 * Restart the logger corresponding to the given record.
	 * @param record The record indicating the logger to restart.
	 */
	void restartLogger(final GenericRecord record) {
		String serviceID = record.stringValueForKey(ID_KEY);
		final LoggerHandler handler = _serviceTable.get(serviceID);
		
		new Thread(new Runnable() {
			public void run() {
				handler.restartLogger();
			}
		}).start();
	}
	
	
	/**
	 * Shutdown the logger corresponding to the given record.
	 * @param record The record indicating the logger to shutdown.
	 */
	void shutdownLogger(final GenericRecord record) {
		String serviceID = record.stringValueForKey(ID_KEY);
		final LoggerHandler handler = _serviceTable.get(serviceID);
		
		new Thread(new Runnable() {
			public void run() {
				handler.shutdown();
			}
		}).start();
	}
	
	
	/**
	 * For the logger corresponding to the record, start it logging.
	 * @param record The record indicating the logger to start logging.
	 */
	void resumeLogging(final GenericRecord record) {
		String serviceID = record.stringValueForKey(ID_KEY);
		final LoggerHandler handler = _serviceTable.get(serviceID);
		
		new Thread(new Runnable() {
			public void run() {
				handler.resumeLogging();
			}
		}).start();
	}
	
	
	/**
	 * For the logger corresponding to the record, stop it from logging.
	 * @param record The record indicating the logger to stop logging.
	 */
	void stopLogging(final GenericRecord record) {
		String serviceID = record.stringValueForKey(ID_KEY);
		final LoggerHandler handler = _serviceTable.get(serviceID);
		
		new Thread(new Runnable() {
			public void run() {
				handler.stopLogging();
			}
		}).start();
	}
}

