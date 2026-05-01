/*
 *  ScoreBoard.java
 *
 *  Created Wednesday June 9, 2004 2:32pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import xal.tools.messaging.MessageCenter;

import xal.extension.solver.solutionjudge.SolutionJudgeListener;
import xal.extension.solver.solutionjudge.SolutionJudge;
import xal.extension.solver.market.*;
import xal.extension.solver.algorithm.SearchAlgorithm;

import java.util.*;

/**
 * Scoreboard maintains the status of the solver including the clock and the best solution
 * found so far.
 *
 * @author   ky6
 * @author	t6p
 */
final public class ScoreBoard implements AlgorithmScheduleListener, SolutionJudgeListener {
	/** center for broadcasting events */
	final private MessageCenter MESSAGE_CENTER;
	
	/** proxy which forwards events to registerd listeners */
	final private ScoreBoardListener EVENT_PROXY;
	
	/** time when the solver started */
	private Date _startTime;
	
	/** the best solution found */
	private Trial _bestSolution;
	
	/** the solution judge */
	private SolutionJudge _solutionJudge;
	
	/** the number of evaluations performed */
	private int _evaluations;
	
	/** number of algorithm run executions (note that one run can correspond to many evaluations) */
	private int _algorithmRunExecutions;
	
	/** the number of evaluations that have been vetoed */
	private int _vetoes;
	
	/** the number of times an optimal soluition was found */
	private int _optimalSolutionsFound;

    /** HashMap for containing the algorithms and the amount of evalautions that each one completes */
    private Map<String, Integer> _evaluationsLog = new HashMap<String, Integer>();

    /** this is for recording the effiency of the algorithms over a set amount of evaluations */
    private EfficiencyLogger _efficiencyLogger = null;
    
	/**
	 * Constructor
	 * @param solutionJudge  the solution judge
	 */
	public ScoreBoard( final SolutionJudge solutionJudge ) {
		MESSAGE_CENTER = new MessageCenter( "Scoreboard" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, ScoreBoardListener.class );
		
		setSolutionJudge( solutionJudge );
		reset();
	}
	
	
	/**
	 * Add the specified listener as a receiver of ScoreBoard events from this instance. 
	 */
	public void addScoreBoardListener( final ScoreBoardListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, ScoreBoardListener.class );
	}
	
	
	/**
	 * Remove the specified listener from receiving ScoreBoard events from this instance. 
	 */
	public void removeScoreBoardListener( final ScoreBoardListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, ScoreBoardListener.class );
	}


	/** Reset the start time and the number of evaluations.  */
	public void reset() {
		_solutionJudge.reset();
		_startTime = new Date();
		_evaluations = 0;
		_algorithmRunExecutions = 0;
		_vetoes = 0;
		_optimalSolutionsFound = 0;
        _bestSolution = null;
	}


	/**
	 * Set the solution judge.
	 * @param solutionJudge   The new solutionJudge value
	 */
	public void setSolutionJudge( SolutionJudge solutionJudge ) {
		if ( _solutionJudge != null ) {
			_solutionJudge.removeSolutionJudgeListener( this );
		}

		_solutionJudge = solutionJudge;
		if ( solutionJudge != null ) {
			solutionJudge.addSolutionJudgeListener( this );
		}
	}


	/**
	 * Get the solution judge.
	 * @return   The solution judge.
	 */
	public SolutionJudge getSolutionJudge() {
		return _solutionJudge;
	}
    
    /**
     * Get the satisfaction of the best trial point
     */
    public double getSatisfaction(){
        return _bestSolution.getSatisfaction();
    }
	
	/**
	 * Get the number of algorithm executions
	 * @return number of algorithm executions
	 */
	public int getAlgorithmExecutions() {
		return _algorithmRunExecutions;
	}


	/**
	 * Get the number of vetoes.
	 * @return   The number of vetoes made.
	 */
	public int getVetoes() {
		return _vetoes;
	}


	/**
	 * Get the number of optimal solutions found.
	 * @return   The number of optimal solutions found.
	 */
	public int getOptimalSolutionsFound() {
		return _optimalSolutionsFound;
	}


	/**
	 * Get the elapsed time.
	 * @return   elapsed time in seconds.
	 */
	public double getElapsedTime() {
		Date currentTime = new Date();
		long elapsedTime = currentTime.getTime() - _startTime.getTime();
		return ( (double)( elapsedTime ) ) / 1000;
	}
	
	
	/**
	 * Judge the specified trial.
	 * @param trial the trial to judge
	 */
	public void judge( final Trial trial ) {
		_solutionJudge.judge( trial );
	}


	/**
	 * Send a message that a trial has been scored.
	 * @param algorithmSchedule  The algorithm schedule that holds the trial scored.
	 * @param trial              The trial that was scored.
	 */
	public void trialScored( final AlgorithmSchedule algorithmSchedule, final Trial trial ) {
		++_evaluations;
        EVENT_PROXY.trialScored( this, trial );
        
        SearchAlgorithm algorithm = trial.getAlgorithm();
        String label = algorithm.getLabel();
        if(_evaluationsLog.containsKey(label)){
            Integer evaluations = _evaluationsLog.get(label);
            evaluations ++;
            _evaluationsLog.put(label, evaluations);
        }
        else{
            _evaluationsLog.put(label, 1);
        }
        
        if( _efficiencyLogger != null )  _efficiencyLogger.record(trial);
	}


	/**
	 * Send a message that a trial has been vetoed.
	 * @param algorithmSchedule  The algorithm schedule that holds the trial vetoed.
	 * @param trial              The trial that was vetoed.
	 */
	public void trialVetoed( final AlgorithmSchedule algorithmSchedule, final Trial trial ) {
		++_vetoes;
		EVENT_PROXY.trialVetoed( this, trial );
	}
	
	
	/**
	 * Handle an event where a new algorithm run stack will start.
	 * @param schedule the schedule posting the event
	 * @param algorithm the algorithm which will execute
	 * @param scoreBoard the scoreboard
	 */
	public void algorithmRunWillExecute( final AlgorithmSchedule schedule, final SearchAlgorithm algorithm, final ScoreBoard scoreBoard ) {}
	
	
	/**
	 * Handle an event where a new algorithm run stack has completed.
	 * @param schedule the schedule posting the event
	 * @param algorithm the algorithm that has executed
	 * @param scoreBoard the scoreboard
	 */
	public void algorithmRunExecuted( final AlgorithmSchedule schedule, final SearchAlgorithm algorithm, final ScoreBoard scoreBoard ) {
		++_algorithmRunExecutions;
	}
	

	/**
	 * Send a message that a new optimal solution has been found.
	 * @param source     The source of the new optimal solution.
	 * @param solutions  The list of solutions.
	 * @param solution   Description of the Parameter
	 */
	public void foundNewOptimalSolution( final SolutionJudge source, final List<Trial> solutions, final Trial solution ) {
		++_optimalSolutionsFound;
		_bestSolution = solution;
		EVENT_PROXY.newOptimalSolution( this, solution );
	}


	/**
	 * Get the new solution.
	 * @return   The new solution.
	 */
	public Trial getBestSolution() {
		return _bestSolution;
	}


	/**
	 * A string for displaying the ScoreBoard.
	 * @return   The string reprsentation of the ScoreBoard.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "\n\tScoreBoard\n***********************************\n" );
		buffer.append( "Elapsed Time:  " + getElapsedTime() + " seconds\n" );
		buffer.append( "Evaluations:  " + getEvaluations() + "\n" );
		buffer.append( "Vetoes:  " + getVetoes() + "\n" );
		buffer.append( "Optimal Solutions Found:  " + getOptimalSolutionsFound() + "\n" );
		buffer.append( "Number of Existing Optimal Solutions:  " + _solutionJudge.getOptimalSolutions().size() + "\n" );
		buffer.append( "Overall Satisfaction:  " + _bestSolution.getSatisfaction() + "\n" );

		buffer.append( "Optimal Solutions: \n" );
		
		final Iterator<Trial> solutionIter = _solutionJudge.getOptimalSolutions().iterator();
		int count = 0;
		while ( solutionIter.hasNext() && count < 3 ) {
			count++;
			buffer.append( "-------------------------------------\n" );
			final Trial optimalSolution = solutionIter.next();
			
			final Problem problem = optimalSolution.getProblem();
			for ( final Variable variable : problem.getVariables() ) {
				double value = optimalSolution.getTrialPoint().getValue( variable );
				buffer.append( "Variable: " + variable.getName() + " = " + value + "\n" );
			}
			
			for ( final Objective objective : problem.getObjectives() ) {
				Score score = optimalSolution.getScore( objective );
				double value = score.getValue();
				double satisfaction = score.getSatisfaction();
				buffer.append( "Objective: " + objective.getName() );
				buffer.append( " = " + value );
				buffer.append( ", satisfaction = " + satisfaction + "\n" );
			}
		}

		return buffer.toString();
	}
    
    
    /**
     * Get the number of evaluations.
     * @return   The number of evaluations.
     */
    public int getEvaluations() {
        return _evaluations;
    }
    
    /**
     * get a copy of the evaluations for each algorithm executed
     */
    public Map<String, Integer> getEvaluationsLog() {
        return new HashMap<>( _evaluationsLog );
    }
    
    
    
    
    /**
     * class for recording the effieceny for each algorithm averaged over a set amount of time
     * Also records the averaged distrobution of evalutions among algorithms
     *
     * The record method is based on evalutions. Default is to average and record every 1000 evalutions
     */
    private class EfficiencyLogger{
        protected int _evaluationsStep; // average and record information every (_evaluationsStep) number of evaluaitons
        protected Map<String, double[]> _currentData = new HashMap<String, double[]>(); // Algorithm name, dataArray
        //double[] dataArray is [0] = Evaluaitons, [1] initial satisfaction, [2] final satisfaction
        
        protected int _pendingEvaluations; // just keeps tracks of how total evaluations have occured since last print statment
        
        /*
         * Just initializes the parameters
         */
        public EfficiencyLogger() {
            _pendingEvaluations = 0;
            _evaluationsStep = 1000;
            
        }
        
        /*
         * Controlls the stepsize over which to integrate and average
         */
        public void setEvaluationsStep(int evaluationsStep){
            if(evaluationsStep > 0){
                _evaluationsStep = evaluationsStep;
            }
        }
        
        /*
         * Records the evaluation for an algorithm, keeping track of the efficiency. Basically just shows if there is any improvement by that algorithm
         */
        public void record(Trial trial){
            _pendingEvaluations ++;
            
            String thisLabel = trial.getAlgorithm().getLabel();
            if(thisLabel == "Initial Algorithm"){
                System.out.printf("%15s %5s, %6s\n","### Total Evals || ", "Evaluations || ", "Efficiency ###"); // displaying the format of what is later printed
            }
            
            double[] emptyArray = new double[] {0,0,0};
            double[] dataArray = new double[3];
            
            // if there is no data for that particular algorithm, add aglorithm to list and initialize points on dataArray
            if(!_currentData.containsKey(thisLabel) || Arrays.equals(_currentData.get(thisLabel), emptyArray)){
                dataArray[0] = 1;
                dataArray[1] = trial.getSatisfaction();
                dataArray[2] = trial.getSatisfaction();
                _currentData.put(thisLabel, dataArray);

            }
            // update data to existing algorithm in map
            else{
                dataArray = _currentData.get(thisLabel);
                dataArray[0] ++;
                double oldSatisfaction = dataArray[2];
                double newSatisfaction = trial.getSatisfaction();
                if(newSatisfaction > oldSatisfaction){
                    dataArray[2] = trial.getSatisfaction();
                }
                
                _currentData.put(thisLabel, dataArray);
            }
            
            // if evaluation limit reached, save, print, and reset
            if(_pendingEvaluations >= _evaluationsStep){
                
                String[] labels = _currentData.keySet().toArray(new String[0]);
                System.out.printf("%6d ", getEvaluations());
                
                int algorithmIndx = 0; // keeps track of how many algorithms have been printed on one line, limit to only 4
                for(String label: labels){
                    
                    dataArray = _currentData.get(label);
                    double finalSatisfaction = dataArray[2];
                    double initialSatisfaction = dataArray[1];
                    
                    dataArray[2] = finalSatisfaction;
                    _currentData.put(label, dataArray);
                    
                    algorithmIndx ++;
                    if(algorithmIndx > 5){
                        System.out.printf("\n%6s", "");
                        algorithmIndx = 0;
                    }
                    
                    double evaluations = dataArray[0];
                    double efficiency = (finalSatisfaction - initialSatisfaction)/((1.0 - initialSatisfaction)*evaluations);
                    
                    String shortLabel = new String();
                    if(label.length() > 13) {
                        shortLabel = label.substring(0,13);
                    }
                    else{
                        shortLabel = label;
                    }
                    
                    if(evaluations == 0){
                        efficiency = 0;
                    }
                    
                    System.out.printf("%s %5.0f, %8.5f || ", shortLabel, evaluations, efficiency);
                    
                    _currentData.put(label, emptyArray);
                }
                
                System.out.printf("\n");
                
                
                // once data is recorded and printed, reset current data values
                _pendingEvaluations = 0;
            }
        }
    }


    /**
     * Turns efficiencyLogger on with parameters
     */
    public void recordEfficiency( final int evaluationsStep ){
		_efficiencyLogger = new EfficiencyLogger();
        _efficiencyLogger.setEvaluationsStep( evaluationsStep );
    }

}

