/*
 * VerboseBroadcaster.java
 *
 * Created on Fri Sep 05 10:51:32 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.correlator;

import xal.tools.messaging.MessageCenter;


/**
 * VerboseBroadcaster immediately broadcasts every correlation from the bin agents as they arrive.
 *
 * @author  tap
 */
class VerboseBroadcaster<RecordType> extends AbstractBroadcaster<RecordType> {
    /** Creates a new instance of Broadcaster */
    public VerboseBroadcaster( final MessageCenter aLocalCenter ) {
		super( aLocalCenter );
	}
	
	
    /**
     * Handle the BinListener event by immediately posting every new correlation.
	 * @param sender The bin agent that published the new correlation.
	 * @param correlation The new correlation.
     */
    synchronized public void newCorrelation( final BinAgent<RecordType> sender, final Correlation<RecordType> correlation ) {
		postCorrelation( correlation );
    }
}

