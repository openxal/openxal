/*
 * ChannelAgent.java
 *
 * Created on June 27, 2002, 8:46 AM
 */

package xal.tools.correlator;

import xal.tools.messaging.MessageCenter;

import java.util.*;

/**
 * Generator manages a single channel.  It performs any setup, monitors the 
 * channel and it manages a circular buffer of bin agents that gather 
 * correlated events.
 *
 * @author  tap
 */
abstract public class SourceAgent implements StateNotice {
	/** number of bins to store events for correlation comparison */
    final private int BIN_POOL_SIZE = 10;
	
    private final MessageCenter MESSAGE_CENTER;
    
    protected String _name;
    private LinkedList<BinAgent> binAgents;        // bins sorted by timestamp
    protected BinUpdate binUpdateProxy;
    private CorrelationTester correlationTester;

    
    /** Creates new ChannelAgent */
    public SourceAgent( final MessageCenter newLocalCenter, final String name, final RecordFilter recordFilter, final CorrelationTester tester ) {
        _name = name;
        correlationTester = tester;
        MESSAGE_CENTER = newLocalCenter;
        
        setupEventHandler(recordFilter);
        
        createBins();
        registerEvents();
    }
    
    
    private void registerEvents() {        
        binUpdateProxy = MESSAGE_CENTER.registerSource( this, BinUpdate.class );
        MESSAGE_CENTER.registerTarget( this, StateNotice.class );
    }
    
    
    private void unregisterEvents() {
        MESSAGE_CENTER.removeSource( this, BinUpdate.class );
        MESSAGE_CENTER.removeTarget( this, StateNotice.class );
    }
    
    
    /**
     * Subclasses implement this method to handle the monitoring of its sources 
     * in a way specific to the particular SourceAgent subclass.  When an event 
     * is captured and it passes the filter test, this method should call postEvent().
     * @see #postEvent
     */
    abstract protected void setupEventHandler( final RecordFilter recordFilter );
    
    
    /** clear memory of all events */
    public void reset() {
        for ( final BinAgent binAgent : binAgents ) {
            binAgent.reset();
        }
    }
    

    /** set the timespan to each bin */
    public void setBinTimespan(double timespan) {
        for ( final BinAgent binAgent : binAgents ) {
            binAgent.setTimespan( timespan );
        }
    }
    
    
    /** Create a pool of bins that form a circular buffer */
    private void createBins() {
        binAgents = new LinkedList<BinAgent>();
        
        for ( int index = 0 ; index < BIN_POOL_SIZE ; index++ ) {
            createNewBin();
        }
    }
    
    
    /** Create a new bin.  Register each bin for events. */
    private void createNewBin() {
        BinAgent binAgent = new BinAgent( MESSAGE_CENTER, correlationTester );
        
        MESSAGE_CENTER.registerTarget( binAgent, BinUpdate.class );
        MESSAGE_CENTER.registerTarget( binAgent, StateNotice.class );
        binAgents.add( binAgent );
    }
    
    
    /** deallocate the bins when they are no longer needed */
    private void removeBins() {
        synchronized( binAgents ) {
            for ( final BinAgent binAgent : binAgents ) {
                removeBin( binAgent );
            }

            binAgents.clear();
        }
    }
    
    
    /** Remove a bin */
    private void removeBin( final BinAgent binAgent ) {
        MESSAGE_CENTER.removeTarget( binAgent, BinUpdate.class );
        MESSAGE_CENTER.removeTarget( binAgent, StateNotice.class );
        
        binAgent.shutdown();
    }
    
    
    /** Used when recycling bins.  Cycle bins in a circular buffer. */
    private BinAgent nextBin() {
        BinAgent nextBin;
        
        synchronized( binAgents ) {
            nextBin = (BinAgent)binAgents.removeFirst();
            binAgents.addLast( nextBin );
        }
        
        return nextBin;
    }
    
    
    /**
     * This method is used to advertise a new event record received by the event handler 
     * of the SourceAgent subclass.  When an event record has passed the the filter 
     * test it should be posted via this method so that other stakeholders 
     * (i.e. the bin agents) can handle the event properly.
     */
    final protected void postEvent(Object record, double timestamp) {
        nextBin().resetWithRecord(name(), record, timestamp);

        // now notify bins everywhere of the new record
        binUpdateProxy.newEvent(name(), record, timestamp);
    }

    
    /** Name of the managed source */
    public String name() {
        return _name;
    }
    
    
    /** Start monitoring the channel */
    abstract public boolean startMonitor();
    
    
    /** Stop monitoring the channel */
    abstract public void stopMonitor();
    
    
    /** shutdown this channel agent and remove itself */
    synchronized protected void shutdown() {
        stopMonitor();
        unregisterEvents();
        removeBins();
    }
    
    
    // implement StateNotice interface
    public void sourceAdded(Correlator sender, String name, int newCount) {
    }
    
    
    // implement StateNotice interface
    public void sourceRemoved(Correlator sender, String name, int newCount) {
    }
    
    
    // implement StateNotice interface
    public void binTimespanChanged(Correlator sender, double newTimespan) {
        setBinTimespan(newTimespan);
    }
    
    
    // implement StateNotice interface
    public void willStopMonitoring(Correlator sender) {
        stopMonitor();
    }

    
    // implement StateNotice interface
    public void willStartMonitoring(Correlator sender) {
        reset();
        startMonitor();
    }
    
    
    /** Implement StateNotice interface to listen for change of state */
    public void correlationFilterChanged(Correlator sender, CorrelationFilter newFilter) {
    }
}
