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
import xal.extension.service.*;
import xal.tools.messaging.MessageCenter;
import xal.tools.dispatch.DispatchQueue;
import xal.tools.dispatch.DispatchTimer;

import java.util.*;
import java.util.concurrent.Callable;
import javax.swing.Timer;


/**
 * MPSModel is the main model for the MPS client.
 *
 * @author  tap
 */
public class MPSModel {
	/** default refresh period in milliseconds */
	final static private long DEFAULT_REMOTE_REFRESH_PERIOD_MILLISECONDS = 10000;

	/** queue for synchronizing access to the remote MPS records */
    final private DispatchQueue REMOTE_MPS_ACCESS_QUEUE;
    
	/** list of remote MPS records corresponding the remote services that have been discovered */
    final private List<RemoteMPSRecord> REMOTE_MPS_RECORDS;

	/** table of remote MPS records keyed by name */
    final private Map<String, RemoteMPSRecord> REMOTE_MPS_RECORDS_TABLE;

	/** message center for dispatching messages from this model */
	final private MessageCenter MESSAGE_CENTER;

	/** proxy for forwarding events from this model to registered listeners */
	final private MPSModelListener EVENT_PROXY;
	
	/** timer for refreshing the remote MPS records with the remote services */
	final private DispatchTimer REMOTE_REFRESH_TIMER;

		
	/**
	 * MPSModel Constructor
	 */
	public MPSModel() {
		MESSAGE_CENTER = new MessageCenter( "MPSModel" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, MPSModelListener.class );
		
        REMOTE_MPS_ACCESS_QUEUE = DispatchQueue.createConcurrentQueue( "Remote MPS Tools" );

        REMOTE_MPS_RECORDS = new ArrayList<RemoteMPSRecord>();
		REMOTE_MPS_RECORDS_TABLE = new HashMap<String, RemoteMPSRecord>();
		
		REMOTE_REFRESH_TIMER = DispatchTimer.getFixedRateInstance( DispatchQueue.createSerialQueue("Remote Refresh" ), new Runnable() {
			public void run() {
				refreshRemoteRecords();
			}
		});
		REMOTE_REFRESH_TIMER.startNowWithInterval( DEFAULT_REMOTE_REFRESH_PERIOD_MILLISECONDS, 0 );

		monitorMPSTools();
		
		System.out.println("monitoring MPS services...");
	}

	
	/**
	 * Dispose of the MPS model.  Stop the timer and dispose of the service directory.
	 */
	protected void dispose() {
		//timer.stop();
		ServiceDirectory.defaultDirectory().dispose();
		System.out.println("disposed of model...");
	}
	

	/** Get the list of records corresponding to remote MPS services */
    public List<RemoteMPSRecord> getRemoteMPSTools() {
        return REMOTE_MPS_ACCESS_QUEUE.dispatchSync( new Callable<List<RemoteMPSRecord>> () {
            public List<RemoteMPSRecord> call() {
                return new ArrayList<RemoteMPSRecord>( REMOTE_MPS_RECORDS );
            }
        });
    }


	/** Add a model listener */
    public void addMPSModelListener( final MPSModelListener listener ) {
        MESSAGE_CENTER.registerTarget( listener, this, MPSModelListener.class );
    }
    
    
	/** Remove a model listener */
    public void removeMPSModelListener( final MPSModelListener listener ) {
        MESSAGE_CENTER.removeTarget( listener, this, MPSModelListener.class );
    }


	/** Monitor for remote services becoming available and going away */
    public void monitorMPSTools() {
        ServiceDirectory.defaultDirectory().addServiceListener( MPSPortal.class, new ServiceListener() {
            
            public void serviceAdded( final ServiceDirectory directory, final ServiceRef serviceRef ) {
                final MPSPortal proxy = directory.getProxy(MPSPortal.class, serviceRef );
                final RemoteMPSRecord remoteRecord = new RemoteMPSRecord( proxy );
                final String serviceID = serviceRef.getRawName();

                final String hostName = proxy.getHostName();
				System.out.println( "Found remote MPS Service with ID: " + serviceID + " on host: " + hostName );

                REMOTE_MPS_ACCESS_QUEUE.dispatchBarrierAsync( new Runnable () {
                    public void run() {
                        REMOTE_MPS_RECORDS_TABLE.put( serviceID, remoteRecord );
                        REMOTE_MPS_RECORDS.add( remoteRecord );
                    }
                });
                EVENT_PROXY.servicesChanged( MPSModel.this, getRemoteMPSTools() );
            }
            
            public void serviceRemoved( final ServiceDirectory directory, final String type, final String name ) {
                System.out.println( "Removing MPS Service with name: " + name );
                
                REMOTE_MPS_ACCESS_QUEUE.dispatchBarrierAsync( new Runnable() {
                    public void run() {
                        final RemoteMPSRecord remoteRecord = REMOTE_MPS_RECORDS_TABLE.get( name );
                        REMOTE_MPS_RECORDS_TABLE.remove( name );
                        REMOTE_MPS_RECORDS.remove( remoteRecord );
                    }
                });
                EVENT_PROXY.servicesChanged( MPSModel.this, getRemoteMPSTools() );
            }
            
        });
    }


	/** refresh the remote records */
	private void refreshRemoteRecords() {
		System.out.println( "Refresh the remote records..." );
		final List<RemoteMPSRecord> remoteRecords = getRemoteMPSTools();
		for ( final RemoteMPSRecord remoteRecord : remoteRecords ) {
			remoteRecord.refresh();
		}
	}

	
	/**
	 * Set the update period for the timer.
	 * @param period The update period in seconds.
	 */
	public void setUpdatePeriod( final double period ) {
		REMOTE_REFRESH_TIMER.startNowWithInterval( (long)(period * 1000), 0 );
	}
}

