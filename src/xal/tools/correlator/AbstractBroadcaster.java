/*
 * AbstractBroadcaster.java
 *
 * Created on July 25, 2002, 1:26 PM
 */

package xal.tools.correlator;

import xal.tools.messaging.MessageCenter;

import java.util.*;


/**
 * AbstractBroadcaster is an abstract delegate for the Correlator and is responsible for filtering notifications 
 * from the bin agents and rebroadcasting the notifications to the CorrelationNotice listeners.  Concrete subclasses 
 * provide for different broadcasting behaviors.  For example, a broacaster may post every correlation, passively 
 * wait for an event before posting only the most recent best correlation, or maintain a buffer of the best correlations
 * with mutually exclusive channels.
 *
 * @author  tap
 */
abstract class AbstractBroadcaster<RecordType> implements BinListener<RecordType>, StateNotice<RecordType> {
	private final MessageCenter LOCAL_CENTER;
	
	private final MessageCenter BROADCAST_CENTER;
	
    transient protected int fullCount;
	
    protected CorrelationNotice<RecordType> correlationProxy;
	
    protected CorrelationFilter<RecordType> correlationFilter;
    
	
    /** Creates a new instance of Broadcaster */
	@SuppressWarnings( "unchecked" )	// must cast proxy for Generics
    public AbstractBroadcaster( final MessageCenter localCenter ) {
        BROADCAST_CENTER = new MessageCenter( "Correlator Broadcast" );      // external broadcast center  
        LOCAL_CENTER = localCenter;     // internal correlator messaging
        
        fullCount = 0;
		
        // listen for internal bin events from all of the bins registered with the local message center
        LOCAL_CENTER.registerTarget(this, BinListener.class);
        
        // register to broadcast correlations
        correlationProxy = (CorrelationNotice<RecordType>)BROADCAST_CENTER.registerSource( this, CorrelationNotice.class );
    }
    
    
    /** Dispose of this delegate and all of its overhead */
    void dispose() {
        BROADCAST_CENTER.removeSource( this, CorrelationNotice.class );		// stop posting correlation notices
		LOCAL_CENTER.removeTarget( this, BinListener.class );				// stop listening for bin correlations
    }
    
    
    /**
     * Register the listener as a receiver of Correlation notices from this 
     * correlator.
     */
    public void addCorrelationNoticeListener( final CorrelationNotice<RecordType> listener ) {
        BROADCAST_CENTER.registerTarget( listener, this, CorrelationNotice.class );
    }
    
    
    /**
     * Unregister the listener as a receiver of Correlation notices from this 
     * correlator.
     */
    public void removeCorrelationNoticeListener( final CorrelationNotice<RecordType> listener ) {
        BROADCAST_CENTER.removeTarget( listener, this, CorrelationNotice.class );
    }
    
    
    /** 
	 * Set the full count of all channels monitored.
	 * @param newCount The new count of all channels being monitored.
	 */
    synchronized void setFullCount( final int newCount ) {
        fullCount = newCount;
    }
    
    
    /** 
	 * Get the full count of all channels monitored
	 * @return The number of channels being monitored.
	 */
    int fullCount() {
        return fullCount;
    }
    
    
    /** 
	 * Set the correlation filter.
	 * @param newFilter The new filter to use to filter correlations.
	 */
    synchronized void setCorrelationFilter( final CorrelationFilter<RecordType> newFilter ) {
        correlationFilter = newFilter;
    }
    
    
    /** 
	 * Post the correlation.
	 * @param correlation The correlation to post.
	 */
    synchronized protected void postCorrelation( final Correlation<RecordType> correlation ) {
        correlationProxy.newCorrelation( this, correlation );
    }
    
    
    /**
     * Implement BinListener interface and handle receiving a new correlation.
	 * @param sender The bin agent that published the new correlation.
	 * @param correlation The new correlation.
     */
    abstract public void newCorrelation( final BinAgent<RecordType> sender, final Correlation<RecordType> correlation );
    
    
	/**
	 * Implement BinListener interface.  This method does nothing.
	 * @param sender The bin agent who sent this message.
	 */
    public void willReset( final BinAgent<RecordType> sender ) {}
	
	
	/**
	 * Handle the source added event.
	 * @param sender The correlator to which the source has been added.
	 * @param name The name identifying the new source.
	 * @param newCount The new number of sources correlated.
	 */
    public void sourceAdded( final Correlator<?,RecordType,?> sender, final String name, final int newCount ) {
		setFullCount( newCount );
	}
	
	
	/**
	 * Handle the source removed event.
	 * @param sender The correlator from which the source has been removed.
	 * @param name The name identifying the new source.
	 * @param newCount The new number of sources correlated.
	 */
    public void sourceRemoved( final Correlator<?,RecordType,?> sender, final String name, final int newCount ) {
		setFullCount(newCount);
	}
	
	
	/**
	 * Handle the bin timespan changed event.
	 * @param sender The correlator whose timespan bin has changed.
	 * @param newTimespan The new timespan used by the correlator.
	 */
    public void binTimespanChanged( final Correlator<?,RecordType,?> sender, final double newTimespan ) {}
	
	
	/**
	 * Handle the advance notice of the correlator stopping.
	 * @param sender The correlator that will stop.
	 */
    public void willStopMonitoring( final Correlator<?,RecordType,?> sender ) {}
	
	
	/**
	 * Handle the advance notice of the correlator starting.
	 * @param sender The correlator that will start.
	 */
    public void willStartMonitoring( final Correlator<?,RecordType,?> sender ) {}
	
	
	/**
	 * Handle the correlation filter changed event.
	 * @param sender The correlator whose correlation filter has changed.
	 * @param newFilter The new correlation filter to use.
	 */
    public void correlationFilterChanged( final Correlator<?,RecordType,?> sender, final CorrelationFilter<RecordType> newFilter ) {}
}
