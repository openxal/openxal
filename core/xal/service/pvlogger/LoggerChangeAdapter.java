/*
 * LoggerChangeAdapter.java
 *
 * Created on Fri Dec 12 10:17:13 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger;


/**
 * LoggerChangeAdapter is the empty implementation of the LoggerChangeListener.
 *
 * @author  tap
 */
public class LoggerChangeAdapter implements LoggerChangeListener {
	/**
	 * Notification that the logger state has changed.
	 * @param logger The logger whose state has changed.
	 * @param type The type of change
	 */
	public void stateChanged(LoggerSession logger, int type) {}
	
	
	/**
	 * Notification that a machine snapshot has been taken.
	 * @param logger The logger which took the snapshot.
	 * @param snapshot The machine snapshot taken.
	 */
	public void snapshotTaken(LoggerSession logger, MachineSnapshot snapshot) {}
	
	
	/**
	 * Notification that a machine snapshot has been published.
	 * @param logger The logger which publshed the snapshot.
	 * @param snapshot The machine snapshot published.
	 */
	public void snapshotPublished(LoggerSession logger, MachineSnapshot snapshot) {}
}

