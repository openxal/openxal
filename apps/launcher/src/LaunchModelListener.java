/*
 * LaunchModelListener.java
 *
 * Created on Fri Mar 05 11:58:55 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.launcher;


/**
 * LaunchModelListener is the interface implemented by listeners of launch model events.
 *
 * @author  tap
 */
public interface LaunchModelListener {
	/**
	 * Handle the event indicating that the launch model has been modified.
	 * @param model The model which has been modified.
	 */
	public void modified( LaunchModel model );
}

