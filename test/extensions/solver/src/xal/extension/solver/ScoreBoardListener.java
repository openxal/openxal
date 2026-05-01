/*
 * ScoreBoardListener.java
 *
 * Created on Wed Oct 06 14:06:35 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.extension.solver;


/**
 * ScoreBoardListener
 *
 * @author  tap
 * @since Oct 06, 2004
 */
public interface ScoreBoardListener {
	/** Indicates that a trial was scored */
	public void trialScored( ScoreBoard scoreboard, Trial trial );
	
	/** Indicates that a trial was vetoed */
	public void trialVetoed( ScoreBoard scoreboard, Trial trial );
	
	/** A new optimal solution has been found */
	public void newOptimalSolution( ScoreBoard scoreboard, Trial trial );
}

