/*
 * PatientBroadcaster.java
 *
 * Created on Wed Sep 10 08:43:47 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.correlator;

import xal.tools.messaging.MessageCenter;


/**
 * PatientBroadcaster posts only the best correlation (most records) for a given correlation time period.  
 * Smaller correlations for a given correlation time period are dropped.  The effect is to post only mutually exclusive correlations.
 * @author  tap
 */
public class PatientBroadcaster<RecordType> extends AbstractBroadcaster<RecordType> {
	// correlator variables
	protected double binTimespan;
	
	// state variables
	protected double lastTime;
	protected Correlation<RecordType> pendingCorrelation;
	
	
	/** Creates a new instance of PatientBroadcaster */
    public PatientBroadcaster( final MessageCenter aLocalCenter ) {
		super( aLocalCenter );
		
		lastTime = Double.NaN;
		pendingCorrelation = null;
	}
    
    
    /**
     * Handle the BinListener event and determine if we should cache it or post it.
	 * @param sender The bin agent that published the new correlation.
	 * @param correlation The new correlation.
     */
    synchronized public void newCorrelation( final BinAgent<RecordType> sender, final Correlation<RecordType> correlation ) {
		final int numRecords = correlation.numRecords();
		final boolean isFullCount = ( numRecords == fullCount );
		final double correlationTime = correlation.meanTimeInSeconds();
		
		if ( pendingCorrelation == null ) {			// test if there are any pending correlations
			if ( isFullCount ) {					// if correlation is a full count, post it immediately
				postCorrelation( correlation );
			}
			else if ( lastTime == Double.NaN || !correlatesWithLast(correlationTime) ) {
				pendingCorrelation = correlation;
			}
		}
		else if ( correlatesWithLast( correlationTime ) ){// this correlation intersects with the last correlation
			if ( isFullCount ) {					// if correlation is a full count, post it immediately
				pendingCorrelation = null;
				postCorrelation( correlation );
			}
			else if ( numRecords > pendingCorrelation.numRecords() ) {	// see if correlation is better than pending
				pendingCorrelation = correlation;	// replace pending correlation with this correlation
			}
		}
		else {										// must be a mutually exclusive correlation to the pending correlation
			postCorrelation( pendingCorrelation );	// post the pending correlation since it was the best for its time
			if ( isFullCount ) {					// if correlation is a full count, post it immediately
				pendingCorrelation = null;
				postCorrelation( correlation );
			}
			else {			// place correlation as pending and wait to see if any better correlations come along for its time
				pendingCorrelation = correlation;
			}
		}
		lastTime = correlationTime;	// always store the time of the last correlation no matter what
    }
	
	
	/**
	 * Determine whether the correlation time intersects with the last time within the bin time span. 
	 * @param correlationTime The time of the correlation with which to test against the last time.
	 * @return true if the correlation correlates with the last correlation and false otherwise.
	 */
	private boolean correlatesWithLast( final double correlationTime ) {
		return Math.abs( correlationTime - lastTime ) < binTimespan;
	}
	
	
	/**
	 * Handle the bin timespan changed event.
	 * @param sender The correlator whose timespan bin has changed.
	 * @param newTimespan The new timespan used by the correlator.
	 */
    public void binTimespanChanged( final Correlator<?,RecordType,?> sender, final double newTimespan ) {
		super.binTimespanChanged( sender, newTimespan );
		binTimespan = newTimespan;
	}
}

