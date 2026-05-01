/*
 *  RepRateListener.java
 *
 *  Created on Thu Aug 05 16:26:18 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;


/**
 * RepRateListener
 *
 * @author    tap
 * @since     Aug 05, 2004
 */
public interface RepRateListener {
	/**
	 * Notification that the rep-rate has changed.
	 *
	 * @param monitor  The monitor announcing the new rep-rate.
	 * @param repRate  The new rep-rate.
	 */
	public void repRateChanged( RepRateMonitor monitor, double repRate );
}

