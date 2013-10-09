/*
 * PassiveBroadcaster.java
 *
 * Created on Fri Sep 05 10:52:04 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.correlator;

import xal.tools.messaging.MessageCenter;


/**
 * PassiveBroadcaster maintains the most recent best correlation (as determined by count) and broadcasts the correlation (or "noCorrelationCaught()") only when instigated. 
 * @author  tap
 */
class PassiveBroadcaster<RecordType> extends AbstractBroadcaster<RecordType> {
    private Correlation<RecordType> bestPartialCorrelation;   // less than full count
    private boolean isFresh;
	
	
    /** Creates a new instance of Broadcaster */
    public PassiveBroadcaster( final MessageCenter aLocalCenter ) {
		super( aLocalCenter );
        isFresh = false;
        bestPartialCorrelation = null;
	}
    
    
    /** 
	 * Get the best partial correlation at this time.
	 * @return the best partial correlation.
	 */
    synchronized Correlation<RecordType> getBestPartialCorrelation() {
        return bestPartialCorrelation;
    }

    
    /**
     * Post the best partial correlation when requested since a full correlation
     * may not be available.
     */
    synchronized void postBestPartialCorrelation() {
        if ( isFresh ) {
            postCorrelation( bestPartialCorrelation );
			isFresh = false;
        }
        else {
            correlationProxy.noCorrelationCaught( this );
        }
    }
	
	
    /**
     * Handle the BinListener event by comparing the number of correlated records and saving it as the best
	 * correlation if it has more than the previous best.  It never posts events, but the best correlation
	 * can be requested at any time.
	 * @param sender The bin agent that published the new correlation.
	 * @param correlation The new correlation.
     */
    synchronized public void newCorrelation( final BinAgent<RecordType> sender, Correlation<RecordType> correlation ) {
        if ( !isFresh || (correlation.numRecords() >= bestPartialCorrelation.numRecords() ) ) {
            bestPartialCorrelation = correlation;
            isFresh = true;
        }
    }
}
