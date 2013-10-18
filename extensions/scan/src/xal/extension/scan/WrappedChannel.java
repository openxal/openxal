package xal.extension.scan;

import xal.ca.*;

import java.util.*;
import java.awt.event.*;

/**
 *  The wrapper around ca channel class. This wrapper can be used as container
 *  for connection, set and get value listeners for the ca channel class. It
 *  also try to connect each 60 second (after startMonitor() call) if connection
 *  never been set or has been lost. Important: You have to call stopMonitor()
 *  if you want the instance will be collected by Garbage Collector! Otherwise
 *  it will be a memory leak!!!
 *
 * @author     shishlo
 * @author     tap
 *created    September 18, 2006
 */
public class WrappedChannel extends MonitoredPV {
	/**
	 *  Constructor for the WrappedChannel object
	 */
	public WrappedChannel() {
		this( null );
	}


	/**
	 * Constructor for the WrappedChannel object
	 * @param  channelName  The Parameter
	 */
	public WrappedChannel( final String channelName ) {
		super();
		setChannelName( channelName );
	}


	/**
	 * Sets the value of the channel of the WrappedChannel object
	 * @param  value  The new value
	 */
	public void setValue( final double value ) {
		final ScanChannelMonitor monitor = _monitor;
		final Channel channel = monitor != null ? monitor.getChannel() : null;
		if( channel != null && channel.isConnected() ) {
			try {
				channel.putVal( value );
				_latestEventSuccessful = true;
			}
			catch( ConnectionException exception ) {
				_latestEventSuccessful = false;
				final ActionEvent stateChangedAction = makeEvent( null, channel );
				STATE_EVENT_DISPATCH.actionPerformed( stateChangedAction );
			}
			catch( PutException exception ) {
				_latestEventSuccessful = false;
				final ActionEvent stateChangedAction = makeEvent( null, channel );
				STATE_EVENT_DISPATCH.actionPerformed( stateChangedAction );
			}
		}
		else {
			_currentValue = value;
		}
	}


	/** Make an event for the given record and channel */
	ActionEvent makeEvent( final ChannelTimeRecord record, final Channel channel ) {
		return new PV_Event( this, record, channel );
	}


	//====================================================================
	//==============Inner Class===========================================
	//====================================================================

	/**
	 *  The PV_Event is a subclass of ActionEvent class. It keeps references to the
	 *  channel and the record in the case of changes of the channel state.
	 *
	 *@author     shishlo
	 *created    September 18, 2006
	 */
	public class PV_Event extends ActionEvent {
		/** serialization ID recommended for Serializable classes */
		private static final long serialVersionUID = 0L;

		private ChannelRecord record = null;
		private Channel chan = null;

		/**
		 *  Constructor for the PV_Event object
		 *
		 *@param  recordIn  The channel record
		 *@param  chanIn    The channel
		 *@param  wch       The Parameter
		 */
		public PV_Event(WrappedChannel wch, ChannelRecord recordIn, Channel chanIn) {
			super(wch, 0, "changed");
			record = recordIn;
			chan = chanIn;
		}

		/**
		 *  Returns the channelRecord of the PV_Event object
		 *
		 *@return    The channel's record
		 */
		public ChannelRecord getChannelRecord() {
			return record;
		}

		/**
		 *  Returns the channel of the PV_Event object
		 *
		 *@return    The channel
		 */
		public Channel getChannel() {
			return chan;
		}
	}
	
}

