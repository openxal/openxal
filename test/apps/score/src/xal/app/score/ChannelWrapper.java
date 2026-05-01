/**
 * A Wrapper class around the Channel object to provide functionality for 
 * callback value storage. Only works for double value channels now.
 * @version   1.1
 * provisions are in (but commented out) to make this a callback get 
 * archetecture, rather than a monitored system.l
 * @author    J. Galambos
 */

package xal.app.score;

import xal.ca.*;

import java.util.*;


public class ChannelWrapper implements IEventSinkValue, ConnectionListener {
	/** The channel object */
	private Channel channel;
	
	/** the monitor for this channel */
	private Monitor monitor;
	
	/** latest record */
	private ChannelRecord _latestRecord;
	
	/** has the channel delivered a value since the last call to getReady ?  - to be used if the monitor does not work out and we switch to a callback get */
	protected boolean gotValue = false;
	
	
	/** primary constructor */
	public ChannelWrapper( final Channel chan ) { 
		_latestRecord = null;
		channel = chan;
		channel.addConnectionListener( this );
		channel.requestConnection();
	}
	
	
	/** the constructor */
	public ChannelWrapper( final String name ) { 
		this( ChannelFactory.defaultFactory().getChannel( name ) );
	}
	
	
	/** whether this channel is connected */
	protected boolean isConnected() { 
		return channel.isConnected();
	}
	
	
	/** return the channel */
	protected Channel getChannel() { return channel; }
	
	
	/** get the value as a double */
	public double doubleValue() {
		return _latestRecord != null ? _latestRecord.doubleValue() : Double.NaN;
	}
	
	
	/** get the value as string */
	public String stringValue() {
		return _latestRecord != null ? _latestRecord.stringValue() : "";
	}
	
	
	/** the name of the Channel */
	protected String getId() { return channel.getId(); }
	
	
	/** make a monitor connection to a channel */
	private void makeMonitor() {
		try {
		    monitor = channel.addMonitorValue( this, Monitor.VALUE );	
		}
		catch(ConnectionException exc) {}
		catch(MonitorException exc) {}
	}
	
	
	/** The Connection Listener interface */
	public void connectionMade(Channel chan) {
		if (monitor == null) makeMonitor();
	}
	
	
	/** fire a callback fetch of the value */
	protected void fireGetCallback() {
		try {
			channel.getValueCallback( this );
		}
		catch (Exception ex) {
		}		
	}
	
	
	/** ConnectionListener interface */
	public void connectionDropped(Channel aChannel) {
		System.out.println("Channel dropped " + aChannel.channelName() );
		_latestRecord = null;
	}


	/** interface method for IEventSinkVal */
	public void eventValue(ChannelRecord newRecord, Channel chan) {
		_latestRecord = newRecord;
	}
}

