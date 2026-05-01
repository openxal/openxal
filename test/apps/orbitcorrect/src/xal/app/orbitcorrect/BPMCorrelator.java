//
//  BPMCorrelator.java
//  Created by Thomas Pelaia on 5/18/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import xal.ca.Channel;
import xal.ca.correlator.ChannelAgent;
import xal.tools.correlator.*;
import xal.tools.messaging.MessageCenter;
import xal.ca.ChannelTimeRecord;

import java.util.*;


/** Correlator for BPM signals */
/**
 * Warnings were suppressed because the correlator correlates two different 
 * types: BPMAgents and Channels
 *
 * The other option is creating a wrapper for both types at the cost of overhead
 * and further complexity
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class BPMCorrelator extends Correlator {
	/** the ID to assign to the beam event */
	final static private String BEAM_EVENT_ID = "Beam Event";
	
	
    /** Constructor */

    public BPMCorrelator( final double aBinTimespan ) {
        this( aBinTimespan, defaultFilter() );
    }
    
    
    /** 
	* Primary Constructor.
	* @param aBinTimespan The time resolution for accepting two events as correlated.
	* @param aFilter A filter to apply to the correlation.
	*/
    public BPMCorrelator( final double aBinTimespan, final CorrelationFilter aFilter ) {
        super( aBinTimespan, aFilter );
    }
    
    
    /** accept correlations with at least 1 BPM record */
    static private CorrelationFilter defaultFilter() {
        return new CorrelationFilter() {
            public boolean accept( final Correlation correlation, final int fullCount ) {
                return correlation.numRecords() > 0;
            }
        };
    }
    
    
    /** accept correlations with at least 1 BPM record and the beam event */
    static private CorrelationFilter beamEventFilter() {
        return new CorrelationFilter() {
            public boolean accept( final Correlation correlation, final int fullCount ) {
                return correlation.numRecords() >= 2 && correlation.isCorrelated( BEAM_EVENT_ID );
            }
        };
    }
	
    
    /**
	 * Overrides the parent method to create and return a BPMCorrelatedSource as a source agent for this correlator.
     * @param source The new source to monitor and correlate.
     * @param sourceName The name to be associated with the source.
     * @param recordFilter The filter to apply to the source's records.
     */
    protected SourceAgent newSourceAgent( final Object source, final String sourceName, final RecordFilter recordFilter ) {
		if ( sourceName.equals( BEAM_EVENT_ID ) ) {
			final Channel beamEventChannel = (Channel)source;
			return new ChannelAgent( localCenter, beamEventChannel, sourceName, recordFilter, correlationTester );
		}
		else {
			final BpmAgent bpmAgent = (BpmAgent)source;
			return new BPMCorrelatedSource( localCenter, bpmAgent, sourceName, recordFilter, correlationTester );
		}
    }
    
    
    /** 
	* Get the number of actively monitored BPMs.
	* @return The number of actively monitored BPMs.
	*/
    synchronized public int numActiveBPMs() {
        return numSources() - numInactiveBPMs();
    }
    
    
    /** 
	* Get the number of BPMs that are inactive due to connection or monitor failure or simply not monitored.
	* @return The number of BPMs that are inactive.
	*/
    synchronized public int numInactiveBPMs() {
        int numFailed = 0;
		
        final Iterator sourceIter = getSourceAgents().iterator();
        while ( sourceIter.hasNext() ) {
            final BPMCorrelatedSource correlatedSource = (BPMCorrelatedSource)sourceIter.next();
            numFailed += ( correlatedSource.isActive() ) ? 0 : 1;
        }
        
        return numFailed;
    }
    
    
    /** 
	* Get the names of BPMs that are not being monitored due to connection or monitor failure or simply not monitoried.
	* @return The collection of names of channels that are not active.
	*/
    synchronized public Collection<String> inactiveBPMsByName() {
        final Collection<String> failedBPMNames = new HashSet<String>();
		
        final Iterator sourceIter = getSourceAgents().iterator();
        while ( sourceIter.hasNext() ) {
            final BPMCorrelatedSource correlatedSource = (BPMCorrelatedSource)sourceIter.next();
            if ( !correlatedSource.isActive() ) {
                failedBPMNames.add( correlatedSource.name() );
            }
        }
        
        return failedBPMNames;
    }
	
	
	/**
	 * Add the beam event channel to monitor and correlate.
	 * @param beamEventChannel the channel to correlate
	 */
	final public void addBeamEvent( final Channel beamEventChannel ) {
		removeBeamEvent();	// remove the exiting beam event monitor if one already exists
		addSource( beamEventChannel, BEAM_EVENT_ID, null );
		if ( beamEventChannel != null ) {
			setCorrelationFilter( beamEventFilter() );
		}
	}
	
	
	/** Remove the beam event */
	final public void removeBeamEvent() {
		if ( hasSource( BEAM_EVENT_ID ) ) {
			removeSource( BEAM_EVENT_ID );
			setCorrelationFilter( defaultFilter() );
		}
	}
	
	
    
    /** 
	* Add a BPM to monitor.  If we already monitor a BPM, do nothing. 
	* @param bpmAgent The BPM agent to monitor for correlations.
	*/
    final public void addBPM( final BpmAgent bpmAgent ) {
        addBPM( bpmAgent, (RecordFilter)null );
    }
    
    
    /** 
	* Add a BPM to monitor.  If we already monitor a BPM, do nothing. 
	* @param bpmAgent The BPM agent to monitor for correlations.
	* @param recordFilter The filter to apply to the channel's records.
	*/
    final public void addBPM( final BpmAgent bpmAgent, final RecordFilter<Object> recordFilter ) {
        addSource( bpmAgent, bpmAgent.getID(), recordFilter );
    }
    
	
    /** 
	* Stop monitoring the specified BPM. 
	* @param bpmAgent The BPM agent we are requesting to stop monitoring and correlating.
	*/
    public void removeBPM( final BpmAgent bpmAgent ) {
		removeSource( bpmAgent.getID() );
    }
}
