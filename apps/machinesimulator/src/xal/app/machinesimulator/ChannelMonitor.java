/**
 * 
 */
package xal.app.machinesimulator;

import xal.ca.Channel;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.ConnectionListener;
import xal.ca.IEventSinkValue;
import xal.ca.Monitor;
import xal.ca.MonitorException;

/**
 * @author luxiaohan
 *The channel monitor to monitor the value changes of the channel
 */
public class ChannelMonitor implements IEventSinkValue, ConnectionListener {
	
	/** The channel object */
	private Channel channel;
	/** the live value */
	private volatile double value = Double.NaN;
	/** the monitor for this channel */
	private Monitor monitor;
	
	/** the constructor */
	public ChannelMonitor(Channel channel) { 
		this.channel = channel;
		this.channel.addConnectionListener(this);
		this.channel.requestConnection();
	}
	
	/** returns the latest value from this Channel */
	protected double getValDbl() { 
		return value;
	}
	
	/**returns the latest value list*/
	protected double[] getValueList() {
		double[] values = {value}; 
		return values;
	}
	
	/** make a monitor connection to a channel */
	private void makeMonitor() {
		try {
		    monitor = channel.addMonitorValue(this, Monitor.VALUE);	
		}
		catch(ConnectionException exc) {}
		catch(MonitorException exc) {}
	}

	/** The Connection Listener interface */
	public void connectionMade(Channel chan) {		
		if (monitor == null) makeMonitor();
	}
	
	/** ConnectionListener interface */
	public void connectionDropped(Channel aChannel) {
		value = Double.NaN;
	}


	/** interface method for IEventSinkVal */
	public void eventValue(ChannelRecord newRecord, Channel chan) {
		value = newRecord.doubleValue();
	}
}
