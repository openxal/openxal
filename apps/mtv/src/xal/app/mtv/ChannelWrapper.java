/**
 * A Wrapper class around the Channel object to provide functionality for 
 * callback value storage. Only works for double value channels now.
 * @version   1.1
 * provisions are in (but commented out) to make this a callback get 
 * archetecture, rather than a monitored system.l
 * @author    J. Galambos
 */
package xal.app.mtv;

import xal.ca.*;

import java.util.*;
import java.awt.event.*;

public class ChannelWrapper implements IEventSinkValue, ConnectionListener {
	
	/** The channel object */
	private Channel channel;
	private ActionListener valueChangeListener = null;
	private ActionEvent valueChangeEvent = null;
	
	/** the value that this channel has */
	private volatile double value = Double.NaN;
	
	/** the monitor for this channel */
	private Monitor monitor;
	
	/** the constructor */
	public ChannelWrapper(Channel chan) { 
		channel = chan;
		channel.addConnectionListener(this);
		channel.requestConnection();	
		valueChangeEvent = new ActionEvent(this,0,"change");
	}

	/** the constructor */
	public ChannelWrapper(String name) { 
		channel = ChannelFactory.defaultFactory().getChannel(name);
		channel.addConnectionListener(this);
		channel.requestConnection();
		valueChangeEvent = new ActionEvent(this,0,"change");
	}
	
	/** sets an ActionListener for a value change*/
	public void setValueChangeListener(ActionListener valueChangeListener){
		this.valueChangeListener = valueChangeListener;
	}
	
	/** whether this channel is connected */
	protected boolean isConnected() { 
		return channel.isConnected();
	}
	
	/** return the channel */
	protected Channel getChannel() { return channel;}
	
	/** returns the latest value from this Channel */
	protected double getValDbl() { return value;}
	
	/** the name of the Channel */
	protected String getId() { return channel.getId();}
	
	/** make a monitor connection to a channel */
	private void makeMonitor() {
		try {
		    monitor = channel.addMonitorValue(this, Monitor.VALUE);	
		    //System.out.println("Connected PV callback on channel " + channel.channelName());
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
		System.out.println("Channel dropped " + aChannel.channelName() );
		value = Double.NaN;
	}


	/** interface method for IEventSinkVal */
	public void eventValue(ChannelRecord newRecord, Channel chan) {
		value = newRecord.doubleValue();
		if(valueChangeListener != null){
			valueChangeListener.actionPerformed(valueChangeEvent);
		}
	}
}

