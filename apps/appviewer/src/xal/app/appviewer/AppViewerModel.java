/*
 * AppViewerModel.java
 *
 * Created on Fri Oct 10 17:08:44 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.appviewer;

import xal.tools.services.*;
import xal.application.ApplicationStatus;
import xal.tools.data.*;
import xal.tools.messaging.MessageCenter;

import java.util.*;
import java.util.logging.*;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


/**
 * AppViewerModel is the main model for the appviewer application.  It monitors the status of XAL applications which are running on the local network.
 * @author  tap
 */
public class AppViewerModel implements DataKeys {
	/** attributes which define the properties of the remote application records*/
	static final protected Collection<DataAttribute> DATA_ATTRIBUTES;
	
	/** sort ordering for ordering remote application records */
	static final protected SortOrdering SORT_ORDERING;
	
	/** message center for dispatching events to registered listeners */
	final protected MessageCenter MESSAGE_CENTER;
	
	/** proxy which forwards events to registered listeners */
	final protected AppViewerListener POST_PROXY;
	
	/** timer for refreshing the status of the remote applications */
	final protected Timer STATUS_REFRESH_TIMER;
	
	/** table of application records each of which contains information about the corresponding remote record */
	final protected DataTable DATA_TABLE;
	
	/** table of application handlers keyed by ID */
	final protected Map<String,AppHandler> SERVICE_TABLE;
	
	
	// static initializer
	static {
		DATA_ATTRIBUTES = makeDataAttributes();
		SORT_ORDERING = new SortOrdering( ID_KEY );
	}
	
	
	/** Constructor */
	public AppViewerModel() {
		MESSAGE_CENTER = new MessageCenter( "Model" );
		POST_PROXY = MESSAGE_CENTER.registerSource( this, AppViewerListener.class );
		
		SERVICE_TABLE = new Hashtable<String,AppHandler>();
		DATA_TABLE = new DataTable( "AppStatus", DATA_ATTRIBUTES );
		
		STATUS_REFRESH_TIMER = newStatusRefreshTimer();
		monitorApplications();
	}
	
	
	/**
	 * Initialize a timer which periodically checks for the status of the known applications.
	 * @return new timer
	 */
	protected Timer newStatusRefreshTimer() {
		final Timer timer = new Timer( 30000, new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				updateApplicationStatus();
			}
		});
		
		timer.setInitialDelay( 2000 );
		timer.start();
		
		return timer;
	}
	
	
	/** Post the current records. */
	protected void postCurrentRecords() {
		final List<GenericRecord> presentRecords;
		synchronized ( DATA_TABLE ) {
			presentRecords = new ArrayList<GenericRecord>( DATA_TABLE.getRecords( SORT_ORDERING ) );
		}
		POST_PROXY.applicationsChanged( this, presentRecords );
	}
	
	
	/** For each remote service, poll the service for fresh information */
	protected void updateApplicationStatus() {
		// copy set to avoid a possible concurrent modification exception				
		final Set<String> services = new HashSet<String>( SERVICE_TABLE.keySet() );
		
		// if there are services, update the MPS table for each service
		for ( final String id : services ) {
			final AppHandler handler = getHandler( id );
			final GenericRecord record = getRecord( id );
			
			if ( handler == null )  continue;
			
			new Thread( new Runnable() {
				public void run() {
					try {
						boolean updated;
						synchronized ( DATA_TABLE ) {
							 updated = handler.update( record );
						}
						if (updated) {
							POST_PROXY.applicationUpdated( AppViewerModel.this, record );
						}
					}
					catch( RemoteMessageException exception ) {
						handleRemoteException( handler, exception );
					}
				}
			}).start();
		}
	}
	
	
	/** Refresh the data table of services.  This is usually done in response to a service being added or removed or during initialization of a view. */
	public void updateServiceList() {
		// copy set to avoid a possible concurrent modification exception				
		final Set<String> services = new HashSet<String>( SERVICE_TABLE.keySet() );
		
		// if there are no services, be sure to clear the MPS table
		if ( services.isEmpty() ) {
			POST_PROXY.applicationsChanged( this, Collections.EMPTY_LIST );
			return;
		}
		
		// post what we presently have and then post changes
		postCurrentRecords();
		
		for ( final String id : services ) {
			final AppHandler handler = getHandler( id );
			GenericRecord existingRecord = getRecord( id );
			final boolean isNewRecord = existingRecord == null;
			final GenericRecord record = isNewRecord ? new GenericRecord( DATA_TABLE ) : existingRecord;
			
			if ( handler == null )  continue;
			
			new Thread( new Runnable() {
				public void run() {
					try {
						if ( handler.update( record ) ) {
							synchronized( DATA_TABLE ) {
								if ( isNewRecord ) {
									final GenericRecord existingRecord = getRecord( id );
									if ( existingRecord == null ) {
										DATA_TABLE.add( record );
										postCurrentRecords();
									}
								}
							}
						}
					}
					catch( RemoteMessageException exception ) {
						handleRemoteException( handler, exception );
					}
				}
			}).start();
		}
	}
	
	
	/**
	 * Handle the remote exception thrown during an update.  Remove the handler so we never try to update it again.  Notify the user of the event.
	 * @param handler The handler that had an exception during a remote update
	 * @param exception The exception thrown
	 */
	protected void handleRemoteException( final AppHandler handler, final RemoteMessageException exception ) {
		// don't bother trying to contact the remote service again
		final String id = handler.getID();
		final String message = "Exception trying to update application with id: " + id;
		System.err.println( message );
		Logger.getLogger("global").log( Level.SEVERE, message, exception );
		SERVICE_TABLE.remove( id );
		GenericRecord record = getRecord( id );
		if ( record != null ) {
			DATA_TABLE.remove( record );
		}
		updateServiceList();
		POST_PROXY.remoteException( this, handler, exception );
	}
	
	
	/**
	 * Get the update period for the timer.
	 * @return The update period in seconds.
	 */
	public int getUpdatePeriod() {
		return STATUS_REFRESH_TIMER.getDelay() / 1000;
	}
	
	
	/**
	 * Set the update period for the timer.
	 * @param period The update period in seconds.
	 */
	public void setUpdatePeriod( final int period ) {
		STATUS_REFRESH_TIMER.setDelay( period * 1000 );
		STATUS_REFRESH_TIMER.restart();
	}
	
	
	/**
	 * Get the handler corresponding to the unique application identifier.
	 * @param id The unique application identifier.
	 * @return The handler for the specified application.
	 */
	protected AppHandler getHandler( final String id ) {
		synchronized( SERVICE_TABLE ) {
			return (AppHandler)SERVICE_TABLE.get( id );
		}
	}
	
	
	/**
	 * Get the record corresponding to the unique application identifier.
	 * @param id The unique application identifier.
	 * @return The data record for the specified application.
	 */
	protected GenericRecord getRecord( final String id ) {
		synchronized ( DATA_TABLE ) {
			final GenericRecord record = DATA_TABLE.record( ID_KEY, id );
			return record;
			//return ( record != null ) ? record : new GenericRecord( DATA_TABLE );
		}
	}
	
	
	/**
	 * Add a listener of events from this model.
	 * @param listener The listener to register to receiver events from this model.
	 */
	public void addAppViewerListener( final AppViewerListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, AppViewerListener.class );
	}
	
	
	/**
	 * Remove a listener of events from this model.
	 * @param listener The listener to remove from receiving events from this model.
	 */
	public void removeAppViewerListener( final AppViewerListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, AppViewerListener.class );
	}
	
	
	/** Monitor the addition and removal of XAL applications. */
	public void monitorApplications() throws ServiceException {
		ServiceDirectory.defaultDirectory().addServiceListener( ApplicationStatus.class, new ServiceListener() {
			public void serviceAdded( final ServiceDirectory directory, final ServiceRef serviceRef ) {
				ApplicationStatus proxy = (ApplicationStatus)directory.getProxy( ApplicationStatus.class, serviceRef );
				synchronized( SERVICE_TABLE ) {
					final String id = serviceRef.getRawName();
					final AppHandler handler = new AppHandler( id, proxy );
					SERVICE_TABLE.put( id, handler );
				}
				updateServiceList();
				STATUS_REFRESH_TIMER.restart();
			}
			
			public void serviceRemoved( final ServiceDirectory directory, final String type, final String name ) {
				synchronized( SERVICE_TABLE ) {
					SERVICE_TABLE.remove( name );
					synchronized( DATA_TABLE ) {
						GenericRecord record = DATA_TABLE.record( ID_KEY, name );
						if ( record != null ) {
							DATA_TABLE.remove( record );
						}
					}
				}
				updateServiceList();
				STATUS_REFRESH_TIMER.restart();
			}
		});
	}
	
	
	/**
	 * Make the attributes which are the keys to the data table.  The data table holds all of the records of data about the applications on this network.  
	 * Each record corresponds to a single application.  Each attribute corresponds to a single piece of information about an application.
	 * @return The collection of attributes of the data table.
	 */
	static protected Collection<DataAttribute> makeDataAttributes() {
		List<DataAttribute> attributes = new ArrayList<DataAttribute>();
		attributes.add( new DataAttribute( ID_KEY, String.class, true ) );
		attributes.add( new DataAttribute( APPLICATION_KEY, String.class, false ) );
		attributes.add( new DataAttribute( LAUNCH_TIME_KEY, String.class, false ) );
		attributes.add( new DataAttribute( HOST_KEY, String.class, false ) );
		attributes.add( new DataAttribute( TOTAL_MEMORY_KEY, Double.class, false ) );
		attributes.add( new DataAttribute( FREE_MEMORY_KEY, Double.class, false ) );
		attributes.add( new DataAttribute( SERVICE_OKAY_KEY, Boolean.class, false ) );
		
		return attributes;
	}
	
	
	/**
	 * Run the garbage collector on the application corresponding to the given record.
	 * @param record The record indicating the application on which to run the garbage collector.
	 */
	void collectGarbageOnApplication( final GenericRecord record ) {
		String id = record.stringValueForKey(ID_KEY);
		final AppHandler handler = SERVICE_TABLE.get( id );
		
		new Thread(new Runnable() {
			public void run() {
				handler.collectGarbage();
			}
		}).start();
	}
	
	
	/**
	 * Reveal the specified application.
	 * @param record The record indicating the application to reveal.
	 */
	void revealApplication( final GenericRecord record ) {
		final String id = record.stringValueForKey( ID_KEY );
		final AppHandler handler = SERVICE_TABLE.get( id );
		
		new Thread(new Runnable() {
			public void run() {
				handler.showAllWindows();
			}
		}).start();
	}
	
	
	/**
	 * Quit the application corresponding to the given record.
	 * @param record The record indicating the application to quit.
	 */
	void quitApplication( final GenericRecord record ) {
		String id = record.stringValueForKey(ID_KEY);
		final AppHandler handler = SERVICE_TABLE.get( id );
		
		new Thread(new Runnable() {
			public void run() {
				handler.quitApplication();
			}
		}).start();
	}
	
	
	/**
	 * Force the application corresponding to the given record to quit.
	 * @param record The record indicating the application to force quit.
	 */
	void forceQuitApplication( final GenericRecord record ) {
		String id = record.stringValueForKey(ID_KEY);
		final AppHandler handler = SERVICE_TABLE.get( id );
		
		new Thread(new Runnable() {
			public void run() {
				handler.forceQuitApplication();
			}
		}).start();
	}
}




