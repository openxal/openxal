/*
 *  SolutionJudge.java
 *
 *  Created Tuesday June 29, 2004 12:17pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver.solutionjudge;

import xal.tools.messaging.MessageCenter;

import xal.extension.solver.Trial;

import java.util.*;

/**
 * SolutionJudge decides whether the latest scored solution is an optimal solution. In many
 * casses only one solution can be the optimal solution at any time. In other cases, there may
 * be a surface of optimal solutions at any time.
 *
 * @author   ky6
 * @author t6p
 */
public abstract class SolutionJudge {
	/** message center for dispatching events to registered listeners */
	protected MessageCenter _messageCenter;
	
	/** proxy which forwards events to registed listeners */
	protected SolutionJudgeListener _eventProxy;
	
	
	/** Creates a new instance of SolutionJudge */
	public SolutionJudge() {
		_messageCenter = new MessageCenter( "Solution Judge" );
		_eventProxy = _messageCenter.registerSource( this, SolutionJudgeListener.class );
		reset();
	}


	/**
	 * Get the default solution judge.
	 * @return   the worst objective biased judge
	 */
	public static SolutionJudge getInstance() {
		return new WorstObjectiveBiasedJudge();
	}


	/** Reset the solution judge.  */
	public abstract void reset();


	/**
	 * Add a solution judge listener.
	 * @param aListener  The listener to add.
	 */
	public void addSolutionJudgeListener( SolutionJudgeListener aListener ) {
		_messageCenter.registerTarget( aListener, this, SolutionJudgeListener.class );
	}


	/**
	 * Remove a solution judge listener.
	 * @param aListener  The listener to remove.
	 */
	public void removeSolutionJudgeListener( SolutionJudgeListener aListener ) {
		_messageCenter.removeTarget( aListener, this, SolutionJudgeListener.class );
	}


	/**
	 * Get the optimal solutions.
	 * @return   A list of solutions.
	 */
	public abstract List<Trial> getOptimalSolutions();


	/**
	 * Judge the trial.
	 * @param trial  The trial to update the solution judge with.
	 */
	public abstract void judge( Trial trial );
}

