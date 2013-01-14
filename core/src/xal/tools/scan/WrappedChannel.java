package xal.tools.scan;

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
 *@author     shishlo
 *created    September 18, 2006
 */
public class WrappedChannel {

	private Channel ch = null;

	private Monitor monitor = null;

	private volatile boolean isRunning = false;

	private volatile boolean valChanged = false;

	private volatile double currValue = 0.0;

	private IEventSinkValue callBack = null;

	private volatile boolean isGood = false;

	//sleep time between attempt to connect in sec.
	private double sleepTime = 60.0;

	private Vector<ActionListener> stateListenersV = new Vector<ActionListener>();
	private Vector<ActionListener> valueListenersV = new Vector<ActionListener>();
	private Vector<ActionListener> putListenersV = new Vector<ActionListener>();

	private Thread connectThread = null;

	/**
	 *  Constructor for the WrappedChannel object
	 */
	public WrappedChannel() {
		init();
	}

	/**
	 *  Constructor for the WrappedChannel object
	 *
	 *@param  chanName  The Parameter
	 */
	public WrappedChannel(String chanName) {
		init();
		setChannelName(chanName);
	}


	/**
	 *  It initilizes the listeners.
	 */
	private void init() {
		callBack =
			new IEventSinkValue() {
				public void eventValue(ChannelRecord record, Channel chan) {
					currValue = record.doubleValue();
					valChanged = true;
					int nL = valueListenersV.size();
					if(nL > 0) {
						PV_Event changedAction = new PV_Event(getSelf(), record, chan);
						for(int i = 0; i < nL; i++) {
							valueListenersV.get(i).actionPerformed(changedAction);
						}
					}
				}
			};
	}


	/**
	 *  Returns the self-reference of the WrappedChannel object
	 *
	 *@return    The self value
	 */
	private WrappedChannel getSelf() {
		return this;
	}

	/**
	 *  Sets the channelName attribute of the MonitoredPV object
	 *
	 *@param  chanName  The new channelName value
	 */
	public void setChannelName(String chanName) {
		if(chanName != null) {
			Channel chIn = ChannelFactory.defaultFactory().getChannel(chanName);
			setChannel(chIn);
		} else {
			stopMonitor();
			ch = null;
			currValue = 0.;
		}
	}

	/**
	 *  Sets the channelNameQuietly attribute of the MonitoredPV object
	 *
	 *@param  chanName  The new channelNameQuietly value
	 */
	public void setChannelNameQuietly(String chanName) {
		if(chanName != null) {
			Channel chIn = ChannelFactory.defaultFactory().getChannel(chanName);
			setChannelQuietly(chIn);
		} else {
			stopMonitor();
			ch = null;
		}
	}


	/**
	 *  Returns the channelName attribute of the MonitoredPV object
	 *
	 *@return    The channelName value
	 */
	public String getChannelName() {
		if(ch != null) {
			return ch.channelName();
		}
		return "null";
	}

	/**
	 *  Sets the new channel of the WrappedChannel object
	 *
	 *@param  chIn  The new channel
	 */
	public void setChannel(Channel chIn) {
		if(chIn != null) {
			stopMonitor();
			tryConnect(chIn);
		} else {
			stopMonitor();
			ch = null;
			currValue = 0.;
		}

	}

	/**
	 *  Sets the channel quietly
	 *
	 *@param  chIn  The new channel
	 */
	public void setChannelQuietly(Channel chIn) {
		if(chIn != null) {
			stopMonitor();
			ch = chIn;
		} else {
			stopMonitor();
			ch = null;
		}
	}


	/**
	 *  Returns the channel of the WrappedChannel object
	 *
	 *@return    The channel
	 */
	public Channel getChannel() {
		return ch;
	}

	/**
	 *  Returns the value of the channel
	 *
	 *@return    The value
	 */
	public double getValue() {
		return currValue;
	}

	/**
	 *  Sets the value of the channel of the WrappedChannel object
	 *
	 *@param  value  The new value
	 */
	public void setValue(double value) {
		if(ch != null && ch.isConnected()) {
			try {
				ch.putVal(value);
			} catch(ConnectionException e) {
				ChannelRecord record = null;
				isGood = false;
				int nL = stateListenersV.size();
				if(nL > 0) {
					PV_Event changedAction = new PV_Event(getSelf(), record, ch);
					for(int i = 0; i < nL; i++) {
						stateListenersV.get(i).actionPerformed(changedAction);
					}
				}
			} catch(PutException e) {
				ChannelRecord record = null;
				isGood = false;
				int nL = stateListenersV.size();
				if(nL > 0) {
					PV_Event changedAction = new PV_Event(getSelf(), record, ch);
					for(int i = 0; i < nL; i++) {
						stateListenersV.get(i).actionPerformed(changedAction);
					}
				}
			}
		} else {
			currValue = value;
		}
	}

	/**
	 *  Returns boolean value "true" if the value was changed.
	 *
	 *@return    The boolean value
	 */
	public boolean valueChanged() {
		return valChanged;
	}

	/**
	 *  Sets the boolean parameter about the value change
	 *
	 *@param  valChanged  The new value change flag
	 */
	public void setValueChanged(boolean valChanged) {
		this.valChanged = valChanged;
	}

	/**
	 *  Returns true if the WrappedChannel object is good
	 *
	 *@return    The true if the WrappedChannel object is good
	 */
	public boolean isGood() {
		return isGood;
	}

	/**
	 *  Adds a state change listener to the WrappedChannel object
	 *
	 *@param  actionListener  a state change listener
	 */
	public void addStateListener(ActionListener actionListener) {
		if(actionListener != null) {
			stateListenersV.add(actionListener);
		}
	}

	/**
	 *  Removes one of the state change listeners.
	 *
	 *@param  actionListener  The state change listener to remove
	 */
	public void removeStateListener(ActionListener actionListener) {
		stateListenersV.remove(actionListener);
	}

	/**
	 *  Removes all state change listeners.
	 */
	public void removeStateListeners() {
		stateListenersV.clear();
	}

	/**
	 *  Adds a value change listener to the WrappedChannel object
	 *
	 *@param  actionListener  a value change listener
	 */
	public void addValueListener(ActionListener actionListener) {
		if(actionListener != null) {
			valueListenersV.add(actionListener);
		}
	}

	/**
	 *  Removes one of the value change listeners.
	 *
	 *@param  actionListener  The value change listener to remove
	 */
	public void removeValueListener(ActionListener actionListener) {
		valueListenersV.remove(actionListener);
	}

	/**
	 *  Removes all value change listeners.
	 */
	public void removeValueListeners() {
		valueListenersV.clear();
	}

	/**
	 *  Stops monitoring value changes
	 */
	public void stopMonitor() {
		isRunning = false;
		isGood = false;
		if(connectThread != null && connectThread.isAlive()) {
			connectThread.interrupt();
		}
	}

	/**
	 *  Starts monitoring value changes
	 */
	public void startMonitor() {
		if(isRunning == true) {
			return;
		}
		if(ch != null) {
			tryConnect(ch);
		} else {
			isRunning = false;
			isGood = false;
			if(monitor != null) {
				monitor.clear();
				monitor = null;
			}
		}
	}

	/**
	 *  Sets the sleepTime attribute of the MonitoredPV object
	 *
	 *@param  sleepTime  The new sleep time value
	 */
	public void setSleepTime(double sleepTime) {
		this.sleepTime = sleepTime;
	}

	/**
	 *  Returns the sleep time between attempt to monitor parameter of the
	 *  WrappedChannel object
	 *
	 *@return    The sleep time value
	 */
	public double getSleepTime() {
		return sleepTime;
	}

	/**
	 *  It will try to start monitoring of the value changes
	 *
	 *@param  chIn  The channel
	 */
	private void tryConnect(final Channel chIn) {

		Runnable tryConnectRun =
			new Runnable() {
				public void run() {
					connectThread = Thread.currentThread();
					ch = chIn;
					isRunning = true;
					isGood = false;
					while(isRunning && ch != null) {
						boolean isGoodIni = isGood;
						try {
							if(!isGood) {
								if(monitor != null) {
									monitor.clear();
								}
								if(isRunning && ch != null) {
									monitor = ch.addMonitorValue(callBack, Monitor.VALUE);
								}
							}
							currValue = ch.getValDbl();
							isGood = true;
						} catch(ConnectionException e) {
							isGood = false;
							currValue = 0.0;
						} catch(MonitorException e) {
							isGood = false;
							currValue = 0.0;
						} catch(GetException e) {
							isGood = false;
							currValue = 0.0;
						}

						if(!isRunning) {
							break;
						}

						if(isGoodIni != isGood) {
							if(stateListenersV.size() > 0) {

								ChannelRecord record = null;
								try {
									record = ch.getValueRecord();
								} catch(ConnectionException e) {} catch(GetException e) {}

								PV_Event changedAction = new PV_Event(getSelf(), record, ch);
								for(int i = 0, n = stateListenersV.size(); i < n; i++) {
									stateListenersV.get(i).actionPerformed(changedAction);
								}
							}
						}

						if(!isRunning) {
							break;
						}

						try {
							Thread.sleep((long) (sleepTime * 1000.0));
						} catch(InterruptedException e) {
							isRunning = false;
							currValue = 0.0;
							if(monitor != null) {
								monitor.clear();
								monitor = null;
							}
							return;
						}

						if(!isRunning) {
							break;
						}
					}
					if(monitor != null) {
						monitor.clear();
						monitor = null;
					}
					isRunning = false;
					isGood = false;
				}
			};

		Thread monitorThread = new Thread(tryConnectRun);
		monitorThread.start();
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

