/*
 * ChannelAgent.java
 *
 * Created on June 27, 2002, 8:46 AM
 */

package xal.ca.correlator;

import xal.tools.correlator.*;
import xal.tools.messaging.MessageCenter;
import xal.ca.*;

import java.util.*;

/**
 * ChannelAgent manages a single channel.  It performs any setup, monitors the 
 * channel and it manages a circular buffer of bin agents that gather 
 * correlated events.
 *
 * @author  tap
 */
public class ChannelAgent extends SourceAgent<ChannelTimeRecord> {
    private String _name;
    private Channel _channel;
    private Monitor _monitor;
	private volatile boolean _enabled;
    private volatile boolean _activeFlag;
    private EventHandler _eventHandler;
	private ConnectionListener _connectionHandler;

    
    /** 
	 * Creates new ChannelAgent 
	 * @param localCenter local shared message center
	 * @param newChannel channel to monitor
	 * @param newName name
	 * @param recordFilter filter for records
	 * @param tester correlation tester
	 */
    public ChannelAgent( final MessageCenter localCenter, final Channel newChannel, final String newName, final RecordFilter<ChannelTimeRecord> recordFilter, final CorrelationTester<ChannelTimeRecord> tester ) {
        super( localCenter, newName, recordFilter, tester );
        _channel = newChannel;
		_monitor = null;
    }
    
    
    /**
     * Setup the event handler to use the specified record filter to filter
     * monitor events for this channel.
     * @param recordFilter The filter to use for this channel.
     */
    protected void setupEventHandler( final RecordFilter<ChannelTimeRecord> recordFilter ) {
        _activeFlag = false;

        if ( recordFilter == null ) {
            _eventHandler = new EventHandler();
        }
        else {
            _eventHandler = new FilteredEventHandler( recordFilter );
        }        
    }
    
    
    /** 
     * Determine if the channel is enabled for correlations.
     * @return true if the channel is enabled for correlations.
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
     * Start monitoring the channel.
     * @return true if the channel is successfully being monitored and false otherwise.
     */
    public boolean startMonitor() {
		_enabled = true;
		if ( _connectionHandler == null ) {
			_connectionHandler = new ConnectionHandler();
			_channel.addConnectionListener( _connectionHandler );
		}
		
		// try to connect the channel
		_activeFlag = false;
		if ( !_channel.isConnected() ) {
			_channel.requestConnection();
			Channel.flushIO();
		}
		else {
			makeMonitor();			
		}
        
        return _activeFlag;
    }
    
    
    /** 
     * Stop monitoring the channel 
     */
    public void stopMonitor() {
		_enabled = false;
		if ( _connectionHandler != null ) {
			_channel.removeConnectionListener( _connectionHandler );
			_connectionHandler = null;
		}
        if ( _monitor != null ) {
            _monitor.clear();
			_monitor = null;
        }
        _activeFlag = false;
    }
	
	
	/**
	* Create a monitor to listen for new channel records.
	*/
	synchronized protected void makeMonitor() {
		try {
			if ( _enabled && _channel.isConnected() ) {
				if ( _monitor == null ) {
					_monitor = _channel.addMonitorValTime( _eventHandler, Monitor.VALUE );
				}
				_activeFlag = true;
			}
		}
		catch( ConnectionException exception ) {
            System.err.println(exception);
            _activeFlag = false;
		}
		catch( MonitorException exception ) {
            System.err.println( exception );
            _activeFlag = false;
		}
	}
	
	
	/**
	 * Handle connection changes for the channel
	 */
	private class ConnectionHandler implements ConnectionListener {    
		/**
		 * Make a monitor when the channel is connected.
		 * @param channel The channel which has been connected.
		 */
		public void connectionMade( final Channel channel ) {
			makeMonitor();
		}
		
		
		/**
		 * Indicates that a connection to the specified channel has been dropped.
		 * @param channel The channel which has been disconnected.
		 */
		public void connectionDropped( final Channel channel ) {
			_activeFlag = false;
		}
	}
    
    
    /** Handle the monitor events */
    protected class EventHandler implements IEventSinkValTime {
        /**
         * Implement IEventSinkValTime interface
         *
         * Handle the monitor events for this channel.
         * When the monitor fires, recycle the oldest bin.  Clear all memory of events 
         * and assign the timestamp of the record to be the timestamp for the bin.
         * Broadcast the event within the correlation world so that bins of all 
         * channel agents (not just this one) are notified of the event.
         */
        synchronized public void eventValue( final ChannelTimeRecord record, final Channel channel ) {
            if ( !_activeFlag ) return;

            double timestamp = record.getTimestamp().getSeconds();
            postEvent( record, timestamp );
        } 
    }
    
    
    /** 
     * Handle the Monitor events and filter the events according to the supplied
     * <code>ChannelRecordFilter</code>.
     */
    protected class FilteredEventHandler extends EventHandler {
		RecordFilter<ChannelTimeRecord> filter;
        
        public FilteredEventHandler( final RecordFilter<ChannelTimeRecord> newFilter ) {
            filter = newFilter;
        }
        
        synchronized public void eventValue( final ChannelTimeRecord record, final Channel channel ) {
            /** Handle only those events accepted by the filter */
            if ( filter.accept( record ) ) {
                super.eventValue( record, channel );
            }
        }
   }
}
