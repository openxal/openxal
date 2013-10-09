//
//  OpticsObjectiveListener.java
//  xal
//
//  Created by Thomas Pelaia on 6/9/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;


/** Interface for reporting changes to an optics objective's settings */
public interface OpticsObjectiveListener {
	/**
	 * Handler that indicates that the enable state of the specified objective has changed.
	 * @param objective the objective whose state has changed.
	 * @param isEnabled the new enable state of the objective.
	 */
	public void objectiveEnableChanged( OpticsObjective objective, boolean isEnabled );
	
	
	/**
	 * Handler indicating that the specified objective's settings have changed.
	 * @param objective the objective whose settings have changed.
	 */
	public void objectiveSettingsChanged( OpticsObjective objective );
}
