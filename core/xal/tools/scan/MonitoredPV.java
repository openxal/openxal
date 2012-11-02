package xal.tools.scan;

import xal.ca.*;

import java.util.*;
import java.awt.event.*;

/**
 *  Description of the Class
 *
 *@author     shishlo
 *created    October 31, 2005
 */
public class MonitoredPV {

	private String alias = null;

	private Channel ch = null;

	private String chName = null;

	private Monitor monitor = null;

	private volatile boolean isRunning = false;
	
	private volatile int thread_counter = 0;

	private volatile double currValue = 0.0;

	private IEventSinkValue callBack = null;

	private volatile boolean isGood = false;

	//sleep time between attempt to connect in sec.
	private double sleepTime = 30.0;

	private Vector<ActionListener> stateListenersV = new Vector<ActionListener>();

	private Vector<ActionListener> valueListenersV = new Vector<ActionListener>();

	private ActionEvent stateChangedAction = null;

	//synchronizing lock
	private Object lockObj = new Object();

	private Thread connectThread = null;

	private static Map<String,MonitoredPV> hashPVs = new HashMap<String,MonitoredPV>();

	/**
	 *  Constructor for the MonitoredPV object
	 */
	private MonitoredPV() {

		final MonitoredPV mpvThis = this;

		callBack =
			new IEventSinkValue() {
				public void eventValue(ChannelRecord record, Channel chan) {
					currValue = record.doubleValue();
					int nL = valueListenersV.size();
					if(nL > 0) {
						stateChangedAction = new MonitoredPVEvent(mpvThis, record, chan);
						for(int i = 0; i < nL; i++) {
							valueListenersV.get(i).actionPerformed(stateChangedAction);
						}
					}
				}
			};
	}

	/**
	 *  Sets the alias attribute of the MonitoredPV object
	 *
	 *@param  alias  The new alias value
	 */
	private void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 *  Returns the monitoredPV attribute of the MonitoredPV class
	 *
	 *@param  alias  The Parameter
	 *@return        The monitoredPV value
	 */
	public static MonitoredPV getMonitoredPV(String alias) {
		if(alias == null) {
			return null;
		}
		if(hashPVs.containsKey(alias)) {
			return hashPVs.get(alias);
		} else {
			MonitoredPV mpv = new MonitoredPV();
			mpv.setAlias(alias);
			hashPVs.put(alias, mpv);
			return mpv;
		}
	}

	/**
	 *  Description of the Method
	 *
	 *@param  alias  The Parameter
	 *@return        The Return Value
	 */
	static boolean hasAlias(String alias) {
		return hashPVs.containsKey(alias);
	}

	/**
	 *  Description of the Method
	 *
	 *@param  alias  The Parameter
	 */
	public static void removeMonitoredPV(String alias) {
		if(alias == null) {
			return;
		}
		if(hashPVs.containsKey(alias)) {
			MonitoredPV mpv = hashPVs.get(alias);
			mpv.stopMonitor();
			hashPVs.remove(alias);
		}
	}

	/**
	 *  Description of the Method
	 *
	 *@param  mpv  The Parameter
	 */
	public static void removeMonitoredPV(MonitoredPV mpv) {
		if(mpv == null) {
			return;
		}
		mpv.stopMonitor();
		removeMonitoredPV(mpv.getAlias());
	}

	/**
	 *  Returns the aliases attribute of the MonitoredPV class
	 *
	 *@return    The aliases value
	 */
	public static Object[] getAliases() {
		return hashPVs.keySet().toArray();
	}

	/**
	 *  Returns the alias attribute of the MonitoredPV object
	 *
	 *@return    The alias value
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 *  Sets the channelName attribute of the MonitoredPV object
	 *
	 *@param  chanName  The new channelName value
	 */
	public void setChannelName(String chanName) {
		stopMonitor();
		chName = chanName;
		if(chanName != null) {
			Channel chIn = ChannelFactory.defaultFactory().getChannel(chanName);
			ch = chIn;
			tryConnect(chIn);
		} else {
			ch = null;
			tryConnect(null);
		}
	}

	/**
	 *  Sets the channelNameQuietly attribute of the MonitoredPV object
	 *
	 *@param  chanName  The new channelNameQuietly value
	 */
	public void setChannelNameQuietly(String chanName) {
		stopMonitor();
		chName = chanName;
		if(chanName != null) {
			Channel chIn = ChannelFactory.defaultFactory().getChannel(chanName);
			ch = chIn;
		}
	}


	/**
	 *  Returns the channelName attribute of the MonitoredPV object
	 *
	 *@return    The channelName value
	 */
	public String getChannelName() {
		return chName;
	}

	/**
	 *  Sets the channel attribute of the MonitoredPV object
	 *
	 *@param  chIn  The new channel value
	 */
	public void setChannel(Channel chIn) {
		stopMonitor();
		if(chIn != null) {
			chName = chIn.channelName();
		} else {
			chName = null;
		}
		ch = chIn;
		tryConnect(chIn);
	}

	/**
	 *  Sets the channelQuietly attribute of the MonitoredPV object
	 *
	 *@param  chIn  The new channelQuietly value
	 */
	public void setChannelQuietly(Channel chIn) {
		stopMonitor();
		if(chIn != null) {
			chName = chIn.channelName();
			ch = chIn;
		} else {
			chName = null;
			ch = null;
		}
	}


	/**
	 *  Returns the channel attribute of the MonitoredPV object
	 *
	 *@return    The channel value
	 */
	public Channel getChannel() {
		return ch;
	}

	/**
	 *  Returns the value attribute of the MonitoredPV object
	 *
	 *@return    The value value
	 */
	public double getValue() {
		return currValue;
	}

	/**
	 *  Returns the good attribute of the MonitoredPV object
	 *
	 *@return    The good value
	 */
	public boolean isGood() {
		return isGood;
	}

	/**
	 *  Adds a feature to the StateListener attribute of the MonitoredPV object
	 *
	 *@param  actionListener  The feature to be added to the StateListener
	 *      attribute
	 */
	public void addStateListener(ActionListener actionListener) {
		if(actionListener != null) {
			stateListenersV.add(actionListener);
		}
	}

	/**
	 *  Description of the Method
	 *
	 *@param  actionListener  The Parameter
	 */
	public void removeStateListener(ActionListener actionListener) {
		stateListenersV.remove(actionListener);
	}

	/**
	 *  Description of the Method
	 */
	public void removeStateListeners() {
		stateListenersV.clear();
	}

	/**
	 *  Adds a feature to the ValueListener attribute of the MonitoredPV object
	 *
	 *@param  actionListener  The feature to be added to the ValueListener
	 *      attribute
	 */
	public void addValueListener(ActionListener actionListener) {
		if(actionListener != null) {
			valueListenersV.add(actionListener);
		}
	}

	/**
	 *  Description of the Method
	 *
	 *@param  actionListener  The Parameter
	 */
	public void removeValueListener(ActionListener actionListener) {
		valueListenersV.remove(actionListener);
	}

	/**
	 *  Description of the Method
	 */
	public void removeValueListeners() {
		valueListenersV.clear();
	}

	/**
	 *  Description of the Method
	 */
	public void stopMonitor() {
		isRunning = false;
		isGood = false;
		while(connectThread != null && connectThread.isAlive()) {
			connectThread.interrupt();
			Thread.yield();
		}
		
		final MonitoredPV mpvThis = this;

		callBack =
			new IEventSinkValue() {
				public void eventValue(ChannelRecord record, Channel chan) {
					currValue = record.doubleValue();
					int nL = valueListenersV.size();
					if(nL > 0) {
						stateChangedAction = new MonitoredPVEvent(mpvThis, record, chan);
						for(int i = 0; i < nL; i++) {
							valueListenersV.get(i).actionPerformed(stateChangedAction);
						}
					}
				}
			};
	}

	/**
	 *  Description of the Method
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
	 *@param  sleepTime  The new sleepTime value
	 */
	public void setSleepTime(double sleepTime) {
		this.sleepTime = sleepTime;
	}

	/**
	 *  Description of the Method
	 *
	 *@param  chIn  The Parameter
	 */
	private void tryConnect(final Channel chIn) {

		final MonitoredPV mpvThis = this;
		
		thread_counter = thread_counter + 1;
		
		Runnable tryConnectRun =
			new Runnable() {
				public void run() {
					synchronized(lockObj) {
						connectThread = Thread.currentThread();
						ch = chIn;
						isRunning = true;
						isGood = false;
						int inner_thread_counter = thread_counter;
						while(isRunning && ch != null) {
							//System.out.println("debug thread="+inner_thread_counter);
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

									stateChangedAction = new MonitoredPVEvent(mpvThis, record, ch);
									for(int i = 0, n = stateListenersV.size(); i < n; i++) {
										stateListenersV.get(i).actionPerformed(stateChangedAction);
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
				}
			};

		Thread monitorThread = new Thread(tryConnectRun);
		monitorThread.start();
	}

}

