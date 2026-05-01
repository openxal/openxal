package xal.extension.scan;

import xal.ca.*;
import xal.tools.messaging.MessageCenter;

import java.util.*;
import java.awt.event.*;


/**
 * Manage the monitor events for a Process Variable
 * @author     shishlo
 * @author     tap
 * created    October 31, 2005
 */
public class MonitoredPV {
	/** default for the current value (0.0 rather than NaN for backward compatibility) */
	final private static double DEFAULT_CURRENT_VALUE = 0.0;

	/** hash of monitored PVs keyed by alias */
	final private static Map<String,MonitoredPV> ALIAS_PV_MAP;

	/** local message center for state events */
	final private MessageCenter STATE_MESSAGE_CENTER;

	/** dispatches state events to registered listeners */
	final ActionListener STATE_EVENT_DISPATCH;

	/** local message center for value events */
	final private MessageCenter VALUE_MESSAGE_CENTER;

	/** dispatches value events to registered listeners */
	final ActionListener VALUE_EVENT_DISPATCH;

	/** handler of the delegate callbacks */
	final private MonitorDelegateHandler DELEGATE_HANDLER;

	private String _alias = null;

	/** monitor of channel to monitor */
	protected ScanChannelMonitor _monitor;

	/** market used to indicate whether the value has changed since the last reset */
	volatile private boolean _valueChanged;

	/** the current value (either monitored or set) */
	volatile double _currentValue;

	/** indicates whether the latest event was successful */
	volatile boolean _latestEventSuccessful;


	// static initializer
	static {
		ALIAS_PV_MAP = new HashMap<String,MonitoredPV>();
	}


	/** Constructor for the MonitoredPV */
	MonitoredPV() {
		STATE_MESSAGE_CENTER = new MessageCenter( "MonitoredPV State" );
		STATE_EVENT_DISPATCH = STATE_MESSAGE_CENTER.registerSource( this, ActionListener.class );

		VALUE_MESSAGE_CENTER = new MessageCenter( "MonitoredPV Value" );
		VALUE_EVENT_DISPATCH = VALUE_MESSAGE_CENTER.registerSource( this, ActionListener.class );

		DELEGATE_HANDLER = new MonitorDelegateHandler();

		_latestEventSuccessful = false;
		_valueChanged = false;
		_currentValue = DEFAULT_CURRENT_VALUE;
	}


	/**
	 *  Returns the monitoredPV attribute of the MonitoredPV class
	 *
	 *@param  alias  The Parameter
	 *@return        The monitoredPV value
	 */
	public static MonitoredPV getMonitoredPV( final String alias ) {
		if(alias == null) {
			return null;
		}
		if( ALIAS_PV_MAP.containsKey( alias ) ) {
			return ALIAS_PV_MAP.get( alias );
		}
		else {
			MonitoredPV mpv = new MonitoredPV();
			mpv.setAlias( alias );
			ALIAS_PV_MAP.put( alias, mpv );
			return mpv;
		}
	}


	/**
	 *  Sets the alias attribute of the MonitoredPV object
	 *  @param  alias  The new alias value
	 */
	private void setAlias( final String alias ) {
		_alias = alias;
	}


	/**
	 * Determine if a monitored PV is associated with the specified alias
	 * @param  alias  The alias to lookup
	 * @return true if a monitored PV has been assigned to the alias
	 */
	static boolean hasAlias( final String alias ) {
		return ALIAS_PV_MAP.containsKey( alias );
	}


	/**
	 * Determine whether the value was changed since the last reset (i.e. setValueChanged(false)).
	 * @return  true if the value was changed since the last time it was reset; false otherwise
	 */
	public boolean valueChanged() {
		return _valueChanged;
	}


	/**
	 * Sets the boolean marker to the specified change state
	 * @param  valueChanged  The new value change marker state
	 */
	public void setValueChanged( final boolean valueChanged ) {
		_valueChanged = valueChanged;
	}


	/**
	 * Remove the monitored PV associated with the alias
	 * @param  alias  The Parameter
	 */
	public static void removeMonitoredPV( final String alias ) {
		if( alias == null ) {
			return;
		}
		if( ALIAS_PV_MAP.containsKey( alias ) ) {
			MonitoredPV mpv = ALIAS_PV_MAP.get( alias );
			mpv.stopMonitor();
			ALIAS_PV_MAP.remove( alias );
		}
	}


	/**
	 * Dispose of the monitored PV and remove its alias
	 * @param  mpv  the monitored PV to remove
	 */
	public static void removeMonitoredPV( MonitoredPV mpv ) {
		if( mpv == null ) {
			return;
		}
		mpv.stopMonitor();
		removeMonitoredPV( mpv.getAlias() );
	}


	/**
	 *  Returns the aliases attribute of the MonitoredPV class
	 *
	 *@return    The aliases value
	 */
	public static Object[] getAliases() {
		return ALIAS_PV_MAP.keySet().toArray();
	}

	/**
	 *  Returns the alias attribute of the MonitoredPV object
	 *
	 *@return    The alias value
	 */
	public String getAlias() {
		return _alias;
	}


	/**
	 * Get the name of the channel to be monitored
	 * @return    The channelName value
	 */
	public String getChannelName() {
		final Channel channel = getChannel();
		return channel != null ? channel.channelName() : null;
	}


	/**
	 * Get the channel to be monitored
	 * @return    The channel value
	 */
	public Channel getChannel() {
		final ScanChannelMonitor monitor = _monitor;
		return monitor != null ? monitor.getChannel() : null;
	}


	/**
	 * Sets the channel to monitor
	 * @param channel the new channel to monitor
	 * @param delegate to handle monitor callbacks
	 * @param requestEvents request channel events
	 */
	private void setChannel( final Channel channel, final boolean requestEvents ) {
		final ScanChannelMonitor oldMonitor = _monitor;
		if ( oldMonitor != null ) {
			oldMonitor.dispose();
		}

		_currentValue = DEFAULT_CURRENT_VALUE;
		if ( channel != null ) {
			_monitor = new ScanChannelMonitor( channel, DELEGATE_HANDLER, requestEvents );
		}
	}


	/**
	 * Sets the channel to monitor
	 * @param  channel the new channel to monitor
	 */
	public void setChannel( final Channel channel ) {
		setChannel( channel, true );
	}


	/**
	 * Sets the channelQuietly attribute of the MonitoredPV object
	 * @param  channel  The new channelQuietly value
	 */
	public void setChannelQuietly( final Channel channel ) {
		setChannel( channel, false );
	}


	/**
	 * Set the name of the channel to monitor
	 * @param  channelName  The new channelName value
	 */
	public void setChannelName( final String channelName ) {
		if ( channelName != null && !channelName.isEmpty() ) {
			final Channel channel = ChannelFactory.defaultFactory().getChannel( channelName );
			setChannel( channel );
		}
		else {
			setChannel( null );
		}
	}


	/**
	 * Sets the name of the channel to monitor without posting events
	 * @param  chanName  The new channelNameQuietly value
	 */
	public void setChannelNameQuietly( final String chanName ) {
		final Channel channel = ChannelFactory.defaultFactory().getChannel( chanName );
		setChannelQuietly( channel );
	}


	/**
	 * Get the latest value of the monitored channel
	 * @return    The value value
	 */
	public double getValue() {
		return _currentValue;
	}


	/**
	 * Determine whether the channel is good meaning that it is connected, has posted a monitored value and the latest event was successful
	 * @return  channel status
	 */
	public boolean isGood() {
		final ScanChannelMonitor monitor = _monitor;
		return monitor != null && monitor.isValid() && _latestEventSuccessful;
	}


	/**
	 * Add a listener for state change events from this monitored PV
	 * @param  actionListener  listener of state change events
	 */
	public void addStateListener( final ActionListener actionListener ) {
		STATE_MESSAGE_CENTER.registerTarget( actionListener, this, ActionListener.class );
	}


	/**
	 * Remove the listener from getting state change events from this monitored PV
	 * @param  actionListener  the listener to remove
	 */
	public void removeStateListener( final ActionListener actionListener ) {
		STATE_MESSAGE_CENTER.removeTarget( actionListener, this, ActionListener.class );
	}


	/**
	 * Add a listener for value change events from this monitored PV
	 * @param  actionListener  The listener to add for value change events
	 */
	public void addValueListener( final ActionListener actionListener ) {
		VALUE_MESSAGE_CENTER.registerTarget( actionListener, this, ActionListener.class );
	}

	/**
	 * Remove the listener from getting value change events from this monitored PV
	 * @param  actionListener  the listener to remove
	 */
	public void removeValueListener( final ActionListener actionListener ) {
		VALUE_MESSAGE_CENTER.removeTarget( actionListener, this, ActionListener.class );
	}


	/** Make an event for the given record and channel */
	ActionEvent makeEvent( final ChannelTimeRecord record, final Channel channel ) {
		return new MonitoredPVEvent( this, record, channel );
	}


	/** Stop the monitor */
	public void stopMonitor() {
		final ScanChannelMonitor monitor = _monitor;
		if ( monitor != null ) {
			monitor.stop();
		}
	}


	/** Start the monitor */
	public void startMonitor() {
		final ScanChannelMonitor monitor = _monitor;
		if ( monitor != null ) {
			monitor.start();
		}
	}


	/** Handle the delegate callbacks */
	private class MonitorDelegateHandler implements ScanChannelMonitorDelegate {
		/** Callback for channel state events */
		public void channelStateChanged( final ScanChannelMonitor monitor, final boolean valid ) {
			final ActionEvent stateChangedAction = makeEvent( monitor.getLatestRecord(), monitor.getChannel() );
			STATE_EVENT_DISPATCH.actionPerformed( stateChangedAction );
		}


		/** Callback for channel monitor events */
		public void channelRecordUpdate( final ScanChannelMonitor monitor, final ChannelTimeRecord record ) {
			final double value = record.doubleValue();
			MonitoredPV.this._currentValue = value;
			MonitoredPV.this._valueChanged = true;
			MonitoredPV.this._latestEventSuccessful = true;
			final ActionEvent valueChangedAction = makeEvent( record, monitor.getChannel() );
			VALUE_EVENT_DISPATCH.actionPerformed( valueChangedAction );
		}
	}
}