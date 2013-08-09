/*
 * MPSModel.java
 *
 * Created on Tue Feb 17 16:30:56 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.mpsclient;

import xal.service.mpstool.MPSPortal;
import xal.tools.services.*;
import xal.tools.data.*;
import xal.tools.messaging.MessageCenter;
import xal.tools.dispatch.DispatchQueue;

import java.util.*;
import java.util.concurrent.Callable;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


/**
 * MPSModel is the main model for the MPS client.
 *
 * @author  tap
 */
public class MPSModel {
	// static constants
	//static final Collection<DataAttribute> dataAttributes;
    
    final private DispatchQueue REMOTE_MPS_ACCESS_QUEUE;
    
    final private List<RemoteMPSRecord> REMOTE_MPS_RECORDS;
    
    final private Map<String, RemoteMPSRecord> REMOTE_MPS_RECORDS_TABLE;
    
    
	// messaging
	protected MessageCenter messageCenter;
	protected MPSModelListener postProxy;
	
	// state variables
//	protected Timer timer;
//	protected Map<String, RequestHandler> serviceTable;
//	final protected DataTable dataTable;
	
	
	/**
	 * static initialization
	 */
//	static {
//		dataAttributes = makeDataAttributes();
//		sortOrdering = new SortOrdering(ID_KEY);
//	}
//	
	
	/**
	 * MPSModel Constructor
	 */
	public MPSModel() {
		messageCenter = new MessageCenter("MPSModel");
		postProxy = messageCenter.registerSource(this, MPSModelListener.class);
		
        REMOTE_MPS_ACCESS_QUEUE = DispatchQueue.createConcurrentQueue( "Remote MPS Tools" );

        REMOTE_MPS_RECORDS = new ArrayList<RemoteMPSRecord>();
		REMOTE_MPS_RECORDS_TABLE = new HashMap<String, RemoteMPSRecord>();
		
		//initTimer();
		monitorMPSTools();
		System.out.println("monitoring MPS tools...");
	}
	
	/**
	 * Dispose of the MPS model.  Stop the timer and dispose of the service directory.
	 */
	protected void dispose() {
		//timer.stop();
		ServiceDirectory.defaultDirectory().dispose();
		System.out.println("disposed of model...");
	}
	
	
    public List<RemoteMPSRecord> getRemoteMPSTools() {
        return REMOTE_MPS_ACCESS_QUEUE.dispatchSync( new Callable<List<RemoteMPSRecord>> () {
            public List<RemoteMPSRecord> call() {
                return new ArrayList<RemoteMPSRecord>( REMOTE_MPS_RECORDS );
            }
        });
    }
    
    public void addMPSModelListener( final MPSModelListener listener ) {
        messageCenter.registerTarget( listener, this, MPSModelListener.class );
    }
    
    
    public void removeMPSModelListener( final MPSModelListener listener ) {
        messageCenter.removeTarget( listener, this, MPSModelListener.class );
    }

    public void monitorMPSTools() {
        System.out.println( "Being monitoring for remote MPS Tools..." );
        ServiceDirectory.defaultDirectory().addServiceListener( MPSPortal.class, new ServiceListener() {
            
            public void serviceAdded( final ServiceDirectory directory, final ServiceRef serviceRef ) {
                final MPSPortal proxy = directory.getProxy(MPSPortal.class, serviceRef );
                final RemoteMPSRecord remoteRecord = new RemoteMPSRecord( proxy );
                final String serviceID = serviceRef.getRawName();
                
                System.out.println( "Found remote MPS Tool with ID: " + serviceID );
                
                final String hostName = proxy.getHostName();
                System.out.println( "Got host name: " + hostName );
                
                REMOTE_MPS_ACCESS_QUEUE.dispatchBarrierAsync( new Runnable () {
                    public void run() {
                        REMOTE_MPS_RECORDS_TABLE.put( serviceID, remoteRecord );
                        REMOTE_MPS_RECORDS.add( remoteRecord );
                    }
                });
                postProxy.servicesChanged( MPSModel.this, getRemoteMPSTools() );
            }
            
            public void serviceRemoved( final ServiceDirectory directory, final String type, final String name ) {
                System.out.println( "Removing MPS Tool with name: " + name );
                
                REMOTE_MPS_ACCESS_QUEUE.dispatchBarrierAsync( new Runnable() {
                    public void run() {
                        final RemoteMPSRecord remoteRecord = REMOTE_MPS_RECORDS_TABLE.get ( name );
                        REMOTE_MPS_RECORDS_TABLE.remove( name );
                        REMOTE_MPS_RECORDS.remove( remoteRecord );
                    }
                });
                postProxy.servicesChanged( MPSModel.this, getRemoteMPSTools() );
            }
            
        });
    }

//
//	
//	/**
//	 * For each remote service, poll the service for fresh information
//	 */
//	protected void updateMPSServiceStatus() {
//		// copy set to avoid a possible concurrent modification exception				
//		Set<String> services = new HashSet<>( serviceTable.keySet() );
//		
//		// if there are services, update the MPS table for each service
//		Iterator<String> serviceIter = services.iterator();
//		while ( serviceIter.hasNext() ) {
//			final String id = serviceIter.next();
//			final RequestHandler handler = getHandler(id);
//			final GenericRecord record = getRecord(id);
//			
//			if ( handler == null )  continue;			
//			
//			new Thread( new Runnable() {
//				public void run() {
//					handler.update(record);
//				}
//			}).start();
//		}
//	}
//	
//	
//	/**
//	 * Refresh the data table of services.  This is usually done in response to a service
////	 * being added or removed or during initialization of a view.
//	 */
//	public void updateServiceList() {
//		// copy set to avoid a possible concurrent modification exception				
//		Set<String> services = new HashSet<>( serviceTable.keySet() );
//		
//		// if there are no services, be sure to clear the MPS table
//		if ( services.isEmpty() ) {
//			postProxy.servicesChanged(this, Collections.<GenericRecord>emptyList() );
//			return;
//		}
//		
//		Iterator<String> serviceIter = services.iterator();
//		while ( serviceIter.hasNext() ) {
//			final String id = serviceIter.next();
//			final RequestHandler handler = getHandler(id);
//			final GenericRecord record = getRecord(id);
//			
//			if ( handler == null )  continue;			
//			
//			new Thread( new Runnable() {
//				public void run() {
//					if ( handler.update(record) ) {
//						synchronized(dataTable) {
//							if (record != getRecord(id)) {
//								dataTable.add(record);
//							}
//							postProxy.servicesChanged(MPSModel.this, dataTable.getRecords(sortOrdering));
//						}
//					}
//				}
//			}).start();
//		}
//	}
	
//
	/**
	 * Get the update period for the timer.
	 * @return The update period in seconds.
	 */
//	public int getUpdatePeriod() {
//		return timer.getDelay() / 1000;
//	}
//	
//	
//	/**
//	 * Set the update period for the timer.
//	 * @param period The update period in seconds.
//	 */
//	public void setUpdatePeriod(int period) {
//		timer.setDelay(period*1000);
//		timer.restart();
//	}
//
//	
//	/**
//	 * Get the handler corresponding to the unique MPS identifier.
//	 * @param id The unique MPS identifier.
//	 * @return The handler for the specified application.
////	 */
//	protected RequestHandler getHandler(String id) {
//		synchronized(serviceTable) {
//			return serviceTable.get(id);
//		}
//	}
//
//	
//	/**
//	 * Get the record corresponding to the unique application identifier.
//	 * @param id The unique application identifier.
//	 * @return The data record for the specified application.
//	 */
//	protected GenericRecord getRecord(String id) {
//		GenericRecord record = dataTable.record(ID_KEY, id);
//		if ( record == null ) {
//			record = new GenericRecord(dataTable);
//		}
//		return record;
//	}
//	
//	
//	/**
//	 * Add a listener of events from this model.
//	 * @param listener The listener to register to receiver events from this model.
//	 */
//	public void addMPSModelListener(MPSModelListener listener) {
//		messageCenter.registerTarget(listener, this, MPSModelListener.class);
//	}
//	
//	
//	/**
//	 * Remove a listener of events from this model.
//	 * @param listener The listener to remove from receiving events from this model.
//	 */
//	public void removeMPSModelListener(MPSModelListener listener) {
//		messageCenter.removeTarget(listener, this, MPSModelListener.class);
//	}
//	
//	
//	/**
//	 * Setup service discovery so we can monitor MPS services on the local network that are
//	 * either new or have quit.
//	 */
//	public void monitorMPSTools() {
//		ServiceDirectory.defaultDirectory().addServiceListener(MPSPortal.class, new ServiceListener() {
//			/**
//			 * Handle a new service being added
//			 * @param directory The service directory.
//			 * @param serviceRef A reference to the new service.
//			 */
//			public void serviceAdded(ServiceDirectory directory, ServiceRef serviceRef) {
//				MPSPortal proxy = directory.getProxy(MPSPortal.class, serviceRef);
//				String id = serviceRef.getRawName();
//				RequestHandler handler = new RequestHandler(id, proxy);;
//				synchronized(serviceTable) {
//					serviceTable.put(id, handler);
//				}
//				updateServiceList();
//				handler.addRequestHandlerListener(MPSModel.this);
//				timer.restart();
//			}
//			
//			/**
//			 * Handle a service being removed
//			 * @param directory The service directory.
//			 * @param name The unique name of the service.
//			 */
//			public void serviceRemoved(ServiceDirectory directory, String type, String name) {
//				synchronized(serviceTable) {
//					RequestHandler handler = getHandler(name);
//					if ( handler != null ) {
//						handler.removeRequestHandlerListener(MPSModel.this);
//					}
//					serviceTable.remove(name);
//					synchronized(dataTable) {
//						// check if we have the record and if so then remove it
//						GenericRecord record = dataTable.record(ID_KEY, name);
//						if ( record != null ) {
//							dataTable.remove( dataTable.record(ID_KEY, name) );
//						}
//					}
//				}
//				updateServiceList();
//				timer.restart();
//			}
//		});
//	}
//	
//	
//	/**
//	 * Make the attributes which are the keys to the data table.  The data table holds all of the records
//	 * of data about the applications on this network.  Each record corresponds to a single application.
//	 * Each attribute corresponds to a single piece of information about an application.
//	 * @return The collection of attributes of the data table.
//	 */
//	static protected Collection<DataAttribute> makeDataAttributes() {
//		List<DataAttribute> attributes = new ArrayList<>();
//		attributes.add( new DataAttribute(ID_KEY, String.class, true) );
//		attributes.add( new DataAttribute(LAUNCH_TIME_KEY, String.class, false) );
//		attributes.add( new DataAttribute(HOST_KEY, String.class, false) );
//		attributes.add( new DataAttribute(SERVICE_OKAY_KEY, Boolean.class, false) );
//		
//		return attributes;
//	}
//	
//	
//	/**
//	 * Indicates that channels have been updated.
//	 * @param handler The handler sending the event
//	 * @param mpsTypeIndex index of the MPS type for which the event applies
//	 * @param channelRefs The list of the new ChannelRef instances
//	 */
//	public void mpsChannelsUpdated(RequestHandler handler, int mpsTypeIndex, List<ChannelRef> channelRefs) {}
//		
//	
//	/**
//	 * Indicates that Input channels have been updated.
//	 * @param handler The handler sending the event
//	 * @param mpsTypeIndex index of the MPS type for which the event applies
//	 * @param channelRefs The list of the new ChannelRef instances
//	 */
//	 public void inputChannelsUpdated(RequestHandler handler, int mpsTypeIndex, List<ChannelRef> channelRefs) {}
//
//	
//	/**
//	 * Indicates that an MPS event has happened.
//	 * @param handler The handler sending the event
//	 * @param mpsTypeIndex index of the MPS type for which the event applies
//	 */
//	public void mpsEventsUpdated(RequestHandler handler, int mpsTypeIndex) {}
//	
//	
//	/**
//	 * Indicates that the handler has checked for new status from the MPS service.
//	 * @param handler The handler sending the event.
//	 * @param timestamp The timestamp of the latest status check
//	 */
//	public void lastCheck(RequestHandler handler, Date timestamp) {
//		String id = handler.getID();
//		GenericRecord record = getRecord(id);
//		if ( record != null ) {
//			postProxy.lastCheck(record, timestamp);
//		}
//	}
}

