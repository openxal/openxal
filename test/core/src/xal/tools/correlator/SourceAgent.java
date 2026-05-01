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
abstract public class SourceAgent<RecordType> implements StateNotice<RecordType> {
	/** number of bins to store events for correlation comparison */
    final private int BIN_POOL_SIZE = 10;
	
	/** Shared Message center */
    final private MessageCenter MESSAGE_CENTER;

	/** tester for correlations */
    final private CorrelationTester<RecordType> CORRELATION_TESTER;

	/** unique name of this source agent */
    protected String _name;
	
	/** bins sorted by timestamp */
    private LinkedList<BinAgent<RecordType>> _binAgents;
	
	/** proxy to forward bin update events to registered listeners */
    protected BinUpdate<RecordType> _binUpdateProxy;

    
    /** Creates new ChannelAgent */
    public SourceAgent( final MessageCenter messageCenter, final String name, final RecordFilter<RecordType> recordFilter, final CorrelationTester<RecordType> tester ) {
        _name = name;
        CORRELATION_TESTER = tester;
        MESSAGE_CENTER = messageCenter;
        
        setupEventHandler( recordFilter );
        
        createBins();
        registerEvents();
    }
    
    
	@SuppressWarnings( "unchecked" )	// need cast to get the proxy using Generics 
    private void registerEvents() {        
        _binUpdateProxy = (BinUpdate<RecordType>)MESSAGE_CENTER.registerSource( this, BinUpdate.class );
        MESSAGE_CENTER.registerTarget( this, StateNotice.class );
    }
    
    
    private void unregisterEvents() {
        MESSAGE_CENTER.removeSource( this, BinUpdate.class );
        MESSAGE_CENTER.removeTarget( this, StateNotice.class );
    }
    
    
    /**
     * Subclasses implement this method to handle the monitoring of its sources in a way specific to the particular SourceAgent subclass.
	 * When an event is captured and it passes the filter test, this method should call postEvent().
	 * @param recordFilter filter for records to accept or reject
     * @see #postEvent
     */
    abstract protected void setupEventHandler(RecordFilter<RecordType> recordFilter);
    
    
    /** clear memory of all events */
    public void reset() {
		for ( final BinAgent<RecordType> binAgent : _binAgents ) {
            binAgent.reset();
        }
    }
    

    /** 
	 * Set the timespan to each bin 
	 * @param timespan for each bin
	 */
    public void setBinTimespan( final double timespan ) {
		final List<BinAgent<RecordType>> binAgents = new ArrayList<BinAgent<RecordType>>();
		synchronized( _binAgents ) {		// need to synchronize since nextBin() modifies _binAgents
			binAgents.addAll( _binAgents );
		}

		for ( final BinAgent<RecordType> binAgent : binAgents ) {
			binAgent.setTimespan( timespan );
		}
    }
    
    
    /** Create a pool of bins that form a circular buffer */
    private void createBins() {
        _binAgents = new LinkedList<BinAgent<RecordType>>();
        
        for ( int index = 0 ; index < BIN_POOL_SIZE ; index++ ) {
            createNewBin();
        }
    }
    
    
    /** Create a new bin.  Register each bin for events. */
    private void createNewBin() {
        final BinAgent<RecordType> binAgent = new BinAgent<RecordType>( MESSAGE_CENTER, CORRELATION_TESTER );
        
        MESSAGE_CENTER.registerTarget( binAgent, BinUpdate.class );
        MESSAGE_CENTER.registerTarget( binAgent, StateNotice.class );
		
        _binAgents.add( binAgent );
    }
    
    
    /** deallocate the bins when they are no longer needed */
    private void removeBins() {
        synchronized( _binAgents ) {
			for ( final BinAgent<RecordType> binAgent : _binAgents ) {
                removeBin( binAgent );
            }

            _binAgents.clear();
        }
    }
    
    
    /** Remove a bin */
    private void removeBin( final BinAgent<RecordType> binAgent ) {
        MESSAGE_CENTER.removeTarget( binAgent, BinUpdate.class );
        MESSAGE_CENTER.removeTarget( binAgent, StateNotice.class );
        
        binAgent.shutdown();
    }
    
    
    /** Used when recycling bins.  Cycle bins in a circular buffer. */
    private BinAgent<RecordType> nextBin() {
        BinAgent<RecordType> nextBin;
        
        synchronized(_binAgents) {
            nextBin = _binAgents.removeFirst();
            _binAgents.addLast( nextBin );
        }
        
        return nextBin;
    }
    
    
    /**
     * This method is used to advertise a new event record received by the event handler of the SourceAgent subclass.  When an event record has passed the the filter
     * test it should be posted via this method so that other stakeholders (i.e. the bin agents) can handle the event properly.
	 * @param record for which the event was posted
	 * @param timestamp for which the event was posted
     */
    final protected void postEvent( final RecordType record, final double timestamp ) {
        nextBin().resetWithRecord( name(), record, timestamp );

        // now notify bins everywhere of the new record
        _binUpdateProxy.newEvent( name(), record, timestamp );
    }

    
    /** 
	 * Name of the managed source 
	 * @return name of the managed source
	 */
    public String name() {
        return _name;
    }
    
    
    /** 
	 * Start monitoring the channel 
	 * @return true upon success and false upon failure
	 */
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
    public void sourceAdded( final Correlator<?,RecordType,?> sender, final String name, final int newCount ) {
    }
    
    
    // implement StateNotice interface
    public void sourceRemoved( final Correlator<?,RecordType,?> sender, final String name, final int newCount ) {
    }
    
    
    // implement StateNotice interface
    public void binTimespanChanged( final Correlator<?,RecordType,?> sender, final double newTimespan ) {
        setBinTimespan(newTimespan);
    }
    
    
    // implement StateNotice interface
    public void willStopMonitoring( final Correlator<?,RecordType,?> sender ) {
        stopMonitor();
    }

    
    // implement StateNotice interface
    public void willStartMonitoring( final Correlator<?,RecordType,?> sender ) {
        reset();
        startMonitor();
    }
    
    
    /** Implement StateNotice interface to listen for change of state */
    public void correlationFilterChanged( final Correlator<?,RecordType,?> sender, final CorrelationFilter<RecordType> newFilter ) {
    }
}
