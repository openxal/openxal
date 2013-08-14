//
//  SolverSessionListener.java
//  xal
//
//  Created by Thomas Pelaia on 6/9/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;


/** Interface for reporting changes to a solver session. */
public interface SolverSessionListener {
	/**
	 * Handler which indicates that the solver's stopper has changed for the specified session.
	 * @param session the session whose stopper has changed.
	 */
	public void stopperChanged( SolverSession session );
	
	
	/**
	 * Handler that indicates that the enable state of the specified objective has changed.
	 * @param session the session whose objective enable state has changed
	 * @param objective the objective whose enable state has changed.
	 * @param isEnabled the new enable state of the objective.
	 */
	public void objectiveEnableChanged( SolverSession session, OpticsObjective objective, boolean isEnabled );
	
	
	/**
	 * Handler indicating that the specified objective's settings have changed.
	 * @param session the session whose objective has changed
	 * @param objective the objective whose settings have changed.
	 */
	public void objectiveSettingsChanged( SolverSession session, OpticsObjective objective );
}
