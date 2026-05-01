/*
 * AlgorithmScheduleListener.java
 *
 * Created Wednesday June 23, 2004 12:00pm
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
 
package xal.extension.solver;

import xal.extension.solver.market.*;

import xal.extension.solver.algorithm.*;

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
	 * @param algorithm the algorithm which will execute
	 * @param scoreBoard the scoreboard
	 */
	public void algorithmRunWillExecute( AlgorithmSchedule schedule, SearchAlgorithm algorithm, ScoreBoard scoreBoard );
	 
	 
	/**
	 * Handle an event where a new algorithm run stack has completed.
	 * @param schedule the schedule posting the event
	 * @param algorithm the algorithm that has executed
	 * @param scoreBoard the scoreboard
	 */
	public void algorithmRunExecuted( AlgorithmSchedule schedule, SearchAlgorithm algorithm, ScoreBoard scoreBoard );
 }  
