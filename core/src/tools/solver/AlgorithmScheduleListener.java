/*
 * AlgorithmScheduleListener.java
 *
 * Created Wednesday June 23, 2004 12:00pm
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
 
package xal.tools.solver;

import xal.tools.solver.market.*;

import java.util.*;


/**
 * The interface implemented by listeners of algorithm schedule events.
 * @author ky6  
 * @author t6p
 */
public interface AlgorithmScheduleListener {
	/**
	 * Handle an event where a trial has been scored.
	 * @param algorithmSchedule The algorithm schedule that holds the trial scored.
	 * @param trial The trial that was scored.	  
	 */
	public void trialScored( AlgorithmSchedule algorithmSchedule, Trial trial );
	  
	  
	/**
	 * Handle an event where a trial has been vetoed.
	 * @param algorithmSchedule The algorithm schedule that holds the trial vetoed.
	 * @param trial The trial that was vetoed.
	 */
	public void trialVetoed( AlgorithmSchedule algorithmSchedule, Trial trial );
	 
	 
	/**
	 * Handle an event where a new algorithm run stack will start.
	 * @param schedule the schedule posting the event
	 * @param strategy the strategy which will execute
	 * @param scoreBoard the scoreboard
	 */
	public void strategyWillExecute( AlgorithmSchedule schedule, AlgorithmStrategy strategy, ScoreBoard scoreBoard );
	 
	 
	/**
	 * Handle an event where a new algorithm run stack has completed.
	 * @param schedule the schedule posting the event
	 * @param strategy the strategy that has executed
	 * @param scoreBoard the scoreboard
	 */
	public void strategyExecuted( AlgorithmSchedule schedule, AlgorithmStrategy strategy, ScoreBoard scoreBoard );
 }  
