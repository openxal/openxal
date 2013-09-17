//
//  OrbitObjectiveListener.java
//  xal
//
//  Created by Thomas Pelaia on 11/29/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

/** Orbit Objective Event notification */
public interface OrbitObjectiveListener {
	/**
	 * Handle the event indicating that the sender's enable state has changed.
	 * @param sender the objective whose enable state has changed.
	 * @param isEnabled true if the objective is now enabled and false if not
	 */
	public void enableChanged( final OrbitObjective sender, final boolean isEnabled );
}
