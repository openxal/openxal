/*
 * DefaultBroadcaster.java
 *
 * Created on Fri Sep 05 10:50:34 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.correlator;

import xal.tools.messaging.MessageCenter;


/**
 * DefaultBroadcaster immediately broadcasts every full correlation as they arrive from the bin agents. 
 *
 * @author  tap
 */
class DefaultBroadcaster<RecordType> extends AbstractBroadcaster<RecordType> {
    /** Creates a new instance of Broadcaster */
    public DefaultBroadcaster( final MessageCenter aLocalCenter ) {
		super( aLocalCenter );
	}
	
	
    /**
     * Handle the BinListener event by immediately posting a correlation if the correlation has a full count.
	 * @param sender The bin agent that published the new correlation.
	 * @param correlation The new correlation.
     */
    synchronized public void newCorrelation( final BinAgent<RecordType> sender, final Correlation<RecordType> correlation ) {
        if ( correlation.numRecords() == fullCount ) {    // broadcast the correlation
            postCorrelation( correlation );
        }
    }
}

