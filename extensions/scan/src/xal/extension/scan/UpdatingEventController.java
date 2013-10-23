package xal.extension.scan;

import java.util.*;
import java.awt.event.*;

/**
 *  This controller calls actionPerformed method of all registered listeners
 *  upon external requests though the call to the "update" method, but with the
 *  interval not less that value defined for the instance of this class
 *
 *@author    shishlo
 */

public class UpdatingEventController {

	private Object syncObj = new Object();

	//Internal synch object for purpose this class instance only
	private Object syncObjInernal = new Object();

	private volatile boolean updateInProgress = false;

	private volatile boolean threadInProgress = false;

	private Vector<ActionListener> listenersV = new Vector<ActionListener>();

	private ActionEvent updateEvent = null;

	//stack counters
	private volatile int stackSize = 0;
	private volatile int currentIndex = 0;

	//stop - to start or stop updating
	private volatile boolean stop = false;

	//sleep time in milliseconds
	private int sleepTime = 1000;

	private Runnable updateRun = null;


	/**
	 *  Constructor for the UpdatingPlotController object
	 */
	public UpdatingEventController() {
		updateEvent = new ActionEvent(this, 0, "update");

		updateRun =
			new Runnable() {
				public void run() {
					boolean hasToStop = false;
					while (!hasToStop) {
						threadInProgress = true;

						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							updateInProgress = false;
							threadInProgress = false;
							return;
						}

						synchronized (syncObj) {
							currentIndex = stackSize;
							for (int i = 0; i < listenersV.size(); i++) {
								listenersV.get(i).actionPerformed(updateEvent);
							}
						}

						synchronized (syncObjInernal) {
							if (currentIndex >= stackSize) {
								hasToStop = true;
								updateInProgress = false;
								threadInProgress = false;
								stackSize = 0;
							}
						}
					}
				}
			};
	}



	/**
	 *  Sets the minimal update time.
	 *
	 *@param  tm  The new minimal sleep time value in sec
	 */
	public void setUpdateTime(double tm) {
		if (tm <= 0.) {
			sleepTime = 60000;
			return;
		}
		sleepTime = (int) (1000. * tm);
	}


	/**
	 *  Returns the minimal update time.
	 *
	 *@return    The minimal sleep time value in sec
	 */
	public double getUpdateTime() {
		return (sleepTime / 1000.);
	}


	/**
	 *  Returns true if the updating has not been accomplished.
	 *
	 *@return    true of false
	 */
	public boolean inProgress() {
		return updateInProgress;
	}


	/**
	 *  Retirns the stop flag
	 *
	 *@return    The stop value
	 */
	public boolean isStop() {
		return stop;
	}


	/**
	 *  Sets the stop flag
	 *
	 *@param  stop  The new stop value
	 */
	public void setStop(boolean stop) {
		this.stop = stop;
	}


	/**
	 *  Reluctantly calls the actionPerformed method of registered listeners.
	 */
	public void update() {

		if (stop) {
			return;
		}

		synchronized (syncObjInernal) {
			stackSize++;
			if (stackSize > 100000000) {
				stackSize = 0;
			}
		}

		if (updateInProgress) {
			return;
		}

		updateInProgress = true;
		if (!threadInProgress) {
			Thread execThread = new Thread(updateRun);
			execThread.start();
		}
	}


	/**
	 *  Adds the new ActionListener.
	 *
	 *@param  al  The new ActionListener.
	 */
	public void addActionListener(ActionListener al) {
		listenersV.add(al);
	}


	/**
	 *  Removes the ActionListener.
	 *
	 *@param  al  The ActionListener to be removed.
	 */
	public void removeActionListener(ActionListener al) {
		listenersV.remove(al);
	}


	/**
	 *  Returns all ActionListeners as a Vector
	 *
	 *@return    The vector with references to ActionListeners
	 */
	public Vector<ActionListener> getActionListeners() {
		return new Vector<ActionListener>(listenersV);
	}


	/**
	 *  Sets the synchronization object without any others actions
	 *
	 *@param  syncObjNew  The new syncObj value
	 */
	private void setSyncObj(Object syncObjNew) {
		synchronized (syncObj) {
			synchronized (syncObjNew) {
				syncObj = syncObjNew;
			}
		}
	}



	/**
	 *  Gets the syncObj attribute of the UpdatingEventController object
	 *
	 *@return    The syncObj value
	 */
	public Object getSyncObj() {
		return syncObj;
	}


	//------------------------------------
	//MAIN for debugging
	//------------------------------------
	/**
	 *  Description of the Method
	 *
	 *@param  args  Description of the Parameter
	 */
	public static void main(String args[]) {

		UpdatingEventController uc = new UpdatingEventController();

		uc.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Call performed!!! Freq. = 1 Hz! Update call Freq.= 10Hz!");
				}
			});

		uc.setUpdateTime(1.0);

		for (int i = 0; i < 100; i++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
			uc.update();
		}

	}

}

