/*
 * LaunchBoardListener.java
 *
 * Created on Mon Apr 05 09:05:29 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.launcher;


/**
 * LaunchBoardListener is a listener of messages posted to the launch board.
 *
 * @author  tap
 */
public interface LaunchBoardListener {
	/**
	 * Post an application wide message from the source
	 * @param source The source of the message
	 * @param message The message posted
	 */
	public void postMessage(Object source, String message);
	
	
	/**
	 * Post an application wide error message from the source
	 * @param source The source of the message
	 * @param message The message posted
	 */
	public void postErrorMessage(Object source, String message);
}

