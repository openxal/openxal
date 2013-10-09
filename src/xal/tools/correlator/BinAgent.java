/*
 * BinAgent.java
 *
 * Created on June 27, 2002, 8:27 AM
 */

package xal.tools.correlator;

import xal.tools.statistics.MutableUnivariateStatistics;
import xal.tools.messaging.MessageCenter;

import java.util.*;

/**
 * BinAgent is the class that gathers correlated events.  It is assigned a 
 * timestamp.  The agent listens for events and any event that is within the 
 * timespan of timestamp, is accepted.  When the number of accepted channels
 * equals the number of all channels monitored by the correlator, the bin agent
 * wraps all the gathered event records into a correlation object and notifies the
 * correlator world that it has found a correlation.  The bin then resets itself.
 *
 * @author  tap
 */
public class BinAgent<RecordType> implements BinUpdate<RecordType>, StateNotice<RecordType> {
    private double earliestTimestamp, latestTimestamp;      // time spread for current correlation
    private MutableUnivariateStatistics timeStatistics;     // time statistics for current correlation
    private double _timespan;                               // time window restriction
    private Map<String,RecordType> recordTable;                    // table of correlated records
    private BinListener<RecordType> binProxy;               // proxy for posting bin events
    private MessageCenter _localCenter;         // internal message center for the correlator
    private CorrelationTester<RecordType> correlationTester;
	private boolean enabled;		// true if this agent is prepared to receive events and false if not.
	
	
    /** Creates new BinAgent */
    public BinAgent( final MessageCenter localCenter, final CorrelationTester<RecordType> tester ) {
		enabled = false;
        correlationTester = tester;
        _localCenter = localCenter;
        timeStatistics = new MutableUnivariateStatistics();
        recordTable = new HashMap<String,RecordType>();
        
        registerEvents();
    }
    
    
    /** Register events for this bin agent */
	@SuppressWarnings( "unchecked" )	// need cast to get the proxy using Generics
    synchronized public void registerEvents() {
        /** Register this bin agent as a poster of bin events */
        binProxy = (BinListener<RecordType>)_localCenter.registerSource( this, BinListener.class ); 
    }
    
    
    /** Prepare itself for disposal. */
    synchronized void shutdown() {
        /** Unregister this bin agent as a poster of correlation notices */
        _localCenter.removeSource( this, BinListener.class );
    }
    
    
    /** Forget all events */
    synchronized public void reset() {
		enabled = false;
        binProxy.willReset(this);
        recordTable.clear();
        timeStatistics = new MutableUnivariateStatistics();
    }
	
	
    /**
     * Forget all events and set the timestamp to the supplied one.
     * This is used when a bin is recycled.
     */
    synchronized public void resetWithRecord( final String name, final RecordType record, final double timestamp ) {
        reset();
        timeStatistics.addSample(timestamp);
        earliestTimestamp = timestamp;
        latestTimestamp = timestamp;
		enabled = true;
		newEvent( name, record, timestamp );
    }
    
    
    /**
     * Set the maximum time span allowed among the records collected.
     */
    public void setTimespan(double timespan) {
        _timespan = timespan;
    }
    
    
    /** record the event record and handle any complete correlation sets found */
    synchronized private void addRecord( final String name, final RecordType record, final double timestamp ) {
        recordTable.put( name, record );
        timeStatistics.addSample( timestamp );
        final Correlation<RecordType> correlation = new Correlation<RecordType>( recordTable, timeStatistics );
        if ( correlationTester.accept( correlation ) ) {
            binProxy.newCorrelation( this, correlation );
        }
    }
    
    
    /**  
     * Remove the record corresponding to the specified name.
     * This method is used when a channel is removed and thus the associated 
     * record is no longer relevant.
     */
    private void removeRecord(String name) {
        recordTable.remove(name);
    }
    
    
    /** 
     * Implement BinUpdate interface
     */
    synchronized public void newEvent( final String name, final RecordType record, final double timestamp ) {
        if ( !enabled || recordTable.containsKey(name) )  return;
        
        double earlyRange = Math.abs(earliestTimestamp - timestamp);
        double lateRange = Math.abs(latestTimestamp - timestamp);
        double range = Math.max(earlyRange, lateRange);
        
        if ( range < _timespan ) {
            addRecord( name, record, timestamp );
            earliestTimestamp = Math.min(timestamp, earliestTimestamp);
            latestTimestamp = Math.max(timestamp, latestTimestamp);
        }
    }
	
    
    /** Implement StateNotice interface to listen for change of state */
    synchronized public void sourceAdded( final Correlator<?,RecordType,?> sender, final String name, final int newCount ) {}
    
    
    /** Implement StateNotice interface to listen for change of state */
    synchronized public void sourceRemoved( final Correlator<?,RecordType,?> sender, final String name, final int newCount ) {
        removeRecord(name);
    }
	
    
    /** Implement StateNotice interface to listen for change of state */
    synchronized public void binTimespanChanged( final Correlator<?,RecordType,?> sender, final double newTimespan ) {
        setTimespan(newTimespan);
        double range = Math.abs(latestTimestamp - earliestTimestamp);
        
        // check if we are in violation of the new time span
        if ( range > newTimespan ) {
            reset();    // throw everything away
        }
    }
    
    
    /** Implement StateNotice interface to listen for change of state */
    public void willStopMonitoring( final Correlator<?,RecordType,?> sender ) {}
    
    
    /** Implement StateNotice interface to listen for change of state */
    public void willStartMonitoring( final Correlator<?,RecordType,?> sender ) {}
    
    
    /** Implement StateNotice interface to listen for change of state */
    public void correlationFilterChanged( Correlator<?,RecordType,?> sender, CorrelationFilter<RecordType> newFilter ) {}
}
