//
//  BPMCorrelatedSource.java
//  Created by Thomas Pelaia on 5/18/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import xal.tools.correlator.*;
import xal.tools.messaging.MessageCenter;

/** Source agent to correlate for BPM signals */
public class BPMCorrelatedSource extends SourceAgent<BpmRecord> {
    final private BpmAgent _bpmAgent;
	private volatile boolean _enabled;
    private volatile boolean _activeFlag;
	private BPMEventHandler _eventHandler;
	
    
    /** Constructor */
    public BPMCorrelatedSource( final MessageCenter newLocalCenter, final BpmAgent bpmAgent, final String newName, final RecordFilter<BpmRecord> recordFilter, final CorrelationTester<BpmRecord> tester ) {
        super( newLocalCenter, newName, recordFilter, tester );
        _bpmAgent = bpmAgent;
    }
    
    
    /**
	 * Setup the event handler to use the specified record filter to filter events for this channel.
     * @param recordFilter The filter to use for this source.
     */
    protected void setupEventHandler( final RecordFilter<BpmRecord> recordFilter ) {
        _activeFlag = false;
		
        if ( recordFilter == null ) {
            _eventHandler = new BPMEventHandler();
        }
        else {
            _eventHandler = new FilteredBPMEventHandler( recordFilter );
        }        
    }
    
    
    /** 
	* Determine if the BPM is enabled for correlations.
	* @return true if the BPM is enabled for correlations.
	*/
    public boolean isEnabled() {
        return _enabled;
    }
    
    
    /** 
	* Determine if the channel is actively being monitored.
	* @return true if the channel is being monitored and false otherwise.
	*/
    public boolean isActive() {
        return _activeFlag;
    }
    
    
    /** 
	* Start monitoring the BPM events.
	* @return true if the BPM is successfully being monitored and false otherwise.
	*/
    public boolean startMonitor() {
		if ( !_enabled ) {
			_enabled = true;
			_bpmAgent.addBpmEventListener( _eventHandler );
		}		
		
		_activeFlag = _bpmAgent.isOnline();
		
        return _activeFlag;
    }
    
    
    /** Stop monitoring the BPM */
    public void stopMonitor() {
		if ( _enabled ) {
			_enabled = false;
			_bpmAgent.removeBpmEventListener( _eventHandler );			
		}
		
        _activeFlag = false;
    }
	
	
	/** Handle BPM events for the BPM agent */
	private class BPMEventHandler implements BpmEventListener {    
		/**
		 * The BPM's monitored state has changed.
		 * @param agent   The BPM agent with the channel whose value has changed
		 * @param record  The record of the new BPM state
		 */
		public void stateChanged( final BpmAgent agent, final BpmRecord record ) {
            if ( !_activeFlag ) return;
			
            final double timestamp = ( (double)record.getTimestamp().getTime() ) / 1000.0;
            postEvent( record, timestamp );
		}
		
		
		/**
		 * The channel's connection has changed. Either it has established a new connection or the existing connection has dropped.
		 * @param agent      The BPM agent with the channel whose connection has changed
		 * @param handle     The handle of the BPM channel whose connection has changed.
		 * @param connected  The channel's new connection state
		 */
		public void connectionChanged( final BpmAgent agent, final String handle, final boolean connected ) {
			_activeFlag = _enabled && agent.isOnline();
		}
	}
    
    
    /** Handle the BPM events and filter the events according to the supplied <code>RecordFilter</code>. */
    protected class FilteredBPMEventHandler extends BPMEventHandler {
        final RecordFilter<BpmRecord> filter;
        
		/** Constructor */
        public FilteredBPMEventHandler( final RecordFilter<BpmRecord> newFilter ) {
            filter = newFilter;
        }
        
		
		/**
		 * The BPM's monitored state has changed.
		 * @param agent   The BPM agent with the channel whose value has changed
		 * @param record  The record of the new BPM state
		 */
		public void stateChanged( final BpmAgent agent, final BpmRecord record ) {
            /** Handle only those events accepted by the filter */
            if ( filter.accept( record ) ) {
                super.stateChanged( agent, record );
            }
		}
	}	
}
