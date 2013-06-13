/*
 * PVUpdater.java
 */

package xal.app.wirescan;

import xal.ca.*;

/**
 * This class is the base class for all PVUpdater classes.
 * A PVUpdater will take a Channel and first add a ConnectionListener,
 * then check for a current connection and if one does not exist,
 * it will make the connection, after a connection is established a
 * monitor is made for the connected Channel.  Because different
 * Channels return different data types, it was required to produce
 * this base class and allow other classes to inherit it and customize
 * the eventValue method to manipulate different data types as the
 * user needs.
 *
 * @author	S. Bunch
 * @version	1.0
 * @see PVUpdaterDbl
 * @see PVUpdaterDblArry
 * @see PVUpdaterByteArry
 */
public class PVUpdater implements IEventSinkValue, ConnectionListener
{
	/** The channel to monitor */
	protected Channel theChannel;
        
        protected Monitor mon;

	/** A blank constructor */
	public PVUpdater(){}

	/**
	 * Constructor used to add a listener, connect, and add a monitor
	 * to a Channel
	 *
	 * @param channel	The channel to be used
	 * @see Channel
	 */
	public PVUpdater(Channel channel){
		theChannel = channel;
		theChannel.addConnectionListener(this);
		if(!theChannel.isConnected()) {
			theChannel.connectAndWait();
		}
		else makeMonitor(theChannel);
	}

	/**
	 * A conveinance method to return the name of the channel.
	 *
	 * @return	String value of the channel's ID.
	 */
	 protected String PVName() {return theChannel.getId();}

	/**
	 * Interface method inherited from IEventSinkValue which should be
	 * overloaded by any class that inherits this class.
	 *
	 * @param newRecord		The value that is checked for changes.
	 * @param chan		The channel that is being monitored.
	 * @see IEventSinkValue
	 * @see ChannelRecord
	 * @see Channel
	 */
	public void eventValue(ChannelRecord newRecord, Channel chan){
            
        }

	/**
	 * Returns an empty value.
	 * This is needed to satisfy the xml parsing,
	 * but is not needed for the functionality of this app
	 *
	 * @param s		String used by xml parsing
	 * @return		A new blank PVUpdater
	 */
	static public PVUpdater valueOf(String s) {
		return new PVUpdater();
	}

	/**
	 * ConnectionListener interface for making monitors.
	 *
	 * @param aChannel		Channel to add a monitor to
	 * @see Channel
	 */
	public void connectionMade(Channel aChannel) {
		makeMonitor(aChannel);
	}

	/** ConnectionListener interface for dropped connections.
	 *
	 * @param aChannel		Channel connection was dropped from
	 * @see Channel
	 */
	public void connectionDropped(Channel aChannel) {
		System.out.println("Channel dropped " + aChannel.channelName() );
	}

	/** ConnectionListener interface for making monitors.
	 *
	 * @param aChannel		Channel to add monitor to
	 * @see Channel
	 */
	protected void makeMonitor(Channel aChannel) {
		try {
			mon = aChannel.addMonitorValue(this, Monitor.VALUE);
		}
		catch(ConnectionException exc) {
			System.err.println( exc.getMessage() );
			exc.printStackTrace();
		}
		catch(MonitorException exc) {
			System.err.println( exc.getMessage() );
			exc.printStackTrace();
		}
	}
        
        protected void removeMonitor(Channel aChannel) {
                // remove connection listener
                aChannel.removeConnectionListener(this);
                
                // remove channel monitor
                mon.clear();
        }
}
