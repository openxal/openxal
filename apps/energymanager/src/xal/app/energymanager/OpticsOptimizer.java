//
//  OpticsOptimizer.java
//  xal
//
//  Created by Thomas Pelaia on 2/18/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.tools.data.*;
import xal.tools.messaging.MessageCenter;
import xal.extension.solver.*;
import xal.extension.solver.hint.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.market.*;
import xal.extension.solver.solutionjudge.*;
import xal.model.probe.Probe;
import xal.model.probe.traj.ProbeState;
import xal.smf.*;
import xal.smf.impl.*;

import java.util.*;


/** Class for optimizing an optics. */
public class OpticsOptimizer implements ScoreBoardListener, SolverSessionListener, Runnable, DataListener {
	/** data label */
	public final static String DATA_LABEL = "Optimizer";
	
	/** Message center for dispatching events to registered listeners. */
	private final MessageCenter _messageCenter;
	
	/** Proxy which forwards events to registered listeners. */
	private final OpticsOptimizerListener _eventProxy;
	
	/** the solver session */
	protected SolverSession _activeSolverSession;
	
	/** the solver */
	protected Solver _solver;
	
	/** indicates whether this optimizer is running */
	protected volatile boolean _isRunning;
	
	/** the current best solution */
	protected volatile Trial _bestSolution;
	
	
	/**
	 * Primary Constructor
	 */
	public OpticsOptimizer( final SolverSession solverSession ) {
		_messageCenter = new MessageCenter( "Optics Optimizer" );
		_eventProxy = _messageCenter.registerSource( this, OpticsOptimizerListener.class );
		
		_activeSolverSession = solverSession;
		solverSession.addSolverSessionListener( this );
		
		_bestSolution = null;
		_isRunning = false;
		
		_solver = new Solver( solverSession.getStopper() );
		_solver.setSolutionJudge( new WorstObjectiveBiasedJudge() );
		_solver.getScoreBoard().addScoreBoardListener( this );
	}
	
	
	/**
	 * Constructor
	 */
	public OpticsOptimizer( final AcceleratorSeq sequence, final List<AcceleratorNode> evaluationNodes, final Probe<?> entranceProbe, final ParameterStore parameterStore ) {
		this( new SolverSession( sequence.getId(), sequence, evaluationNodes, entranceProbe, parameterStore.getCoreParameters() ) );
	}
	
	
	/**
	 * Add the specified listener to receive optimization event notifications from this object.
	 * @param listener the listener to receive the event notifications.
	 */
	public void addOpticsOptimizerListener( final OpticsOptimizerListener listener ) {
		_messageCenter.registerTarget( listener, this, OpticsOptimizerListener.class );
		if ( _bestSolution != null ) {
			listener.newOptimalSolution( this, _bestSolution );
		}
	}
	
	
	/**
	 * Remove the specified listener from receiving optimization event notifications from this object.
	 * @param listener the listener to remove from receiving event notifications.
	 */
	public void removeOpticsOptimizerListener( final OpticsOptimizerListener listener ) {
		_messageCenter.removeTarget( listener, this, OpticsOptimizerListener.class );
	}
	
	
	/**
	 * Get the solver session.
	 * @return the active solver session
	 */
	public SolverSession getActiveSolverSession() {
		return _activeSolverSession;
	}
	
	
	/**
	 * Get a table of variable values corresponding to the best solution.
	 * @return a table of variable values corresponding to the best solution.
	 */
	public Map<String, Double> getBestVariableValues() {
		final Trial solution = _bestSolution;
		
		if ( solution == null )  return Collections.emptyMap();
		
		final Map<String, Double> valueMap = new HashMap<>();
		final Iterator<Variable> variableIter = _activeSolverSession.getVariables().iterator();
		while ( variableIter.hasNext() ) {
			final LiveParameterVariable variable = (LiveParameterVariable)variableIter.next();
			valueMap.put( variable.getName(), new Double( solution.getTrialPoint().getValue( variable ) ) );
		}
		
		return valueMap;
	}
	
	
	/**
	 * Get the best solution.
	 * @return the current best solution
	 */
	public Trial getBestSolution() {
		return _bestSolution;
	}
	
	
	/**
	 * Get the best solution.
	 * @return the current best solution
	 */
	public Simulation getBestSimulation() {
		return _bestSolution != null ? (Simulation)_bestSolution.getCustomInfo() : null;
	}
	
	
	/**
	 * Get the elapsed time.
	 * @return the elapsed time in seconds.
	 */
	public double getElapsedTime() {
		return _solver.getScoreBoard().getElapsedTime();
	}
	
	
	/**
	 * Set the new evaluation nodes.
	 * @param evaluationNodes the new list of evaluation nodes
	 */
	public void setEvaluationNodes( final List<AcceleratorNode> evaluationNodes ) {
		if ( !isRunning() ) {
			_activeSolverSession.setEvaluationNodes( new ArrayList<AcceleratorNode>( evaluationNodes ) );
		}
		else {
			throw new RuntimeException( "Can't change the evaluation nodes while an optimization run is in progress!" );
		}
	}
	
	
	/**
	 * Set the entrance probe.
	 * @param entranceProbe the new entrance probe
	 */
	public void setEntranceProbe( final Probe<?> entranceProbe ) {
		if ( !isRunning() ) {
			_activeSolverSession.setEntranceProbe( entranceProbe );			
		}
		else {
			throw new RuntimeException( "Can't change the entrance kinetic energy while an optimization run is in progress!" );
		}
	}
	
	
	/**
	 * Determine if an optimization is running.
	 * @return true if an optimization is currently running and false if not
	 */
	public boolean isRunning() {
		return _isRunning;
	}
	
	
	/** Perform just an evaluation */
	public void evaluateInitialPoint() {
		try {
			_isRunning = true;
			
			_activeSolverSession.prepareForSolving();
			
			_eventProxy.optimizationStarted( this );	// now everything has been initialized
			final Trial trial = _activeSolverSession.getProblem().evaluateInitialPoint();
			_bestSolution = trial;						// allows subsequent operations to work on this trial
			_activeSolverSession.cleanup();
			_eventProxy.newOptimalSolution( this, trial );
		}
		catch ( Exception exception ) {
			_eventProxy.optimizationFailed( this, exception );
		}
		finally {
			_isRunning = false;
			_eventProxy.optimizationStopped( this );			
		}
	}
	
	
	/** Optimize the optics with the given conditions. */
	public void run() {
		try {
			_isRunning = true;			
			_bestSolution = null;
			
			_solver.setStopper( _activeSolverSession.getStopper() );
			_activeSolverSession.prepareForSolving();
			
			_eventProxy.optimizationStarted( this );	// now everything has been initialized for solving
			_solver.solve( _activeSolverSession.getProblem() );
			_activeSolverSession.cleanup();
			
			System.out.println( _solver.getScoreBoard() );			
		}
		catch ( Exception exception ) {
			_eventProxy.optimizationFailed( this, exception );
		}
		finally {
			_isRunning = false;
			_eventProxy.optimizationStopped( this );			
		}
	}
	
	
	/**
	 * Spawn a new optimization run in a new thread.
	 * @return the thread in which the optimization run is being performed.
	 */
	public Thread spawnRun() {
		final Thread thread = new Thread( this );
		thread.start();
		
		return thread;
	}
	
	
	/** 
	 * Set the solve duration.
	 * @param duration the new duration in seconds
	 */
	public void setSolvingDuration( final double duration ) {
		_activeSolverSession.updateSolveDuration( duration );
		_solver.setStopper( _activeSolverSession.getStopper() );
	}
	
	
	/** Stop solving */
	public void stopSolving() {
		_solver.stopSolving();
	}
	
	
	/** Copy the best solution to the custom settings */
	public void copyOptimalToCustomValues() {
		if ( _bestSolution != null ) {
			final Trial solution = _bestSolution;
            
			final Iterator<Variable> variableIter = _activeSolverSession.getVariables().iterator();
			while ( variableIter.hasNext() ) {
				final LiveParameterVariable variable = (LiveParameterVariable)variableIter.next();
				final double value = solution.getTrialPoint().getValue( variable );
				variable.getParameter().setCustomValue( value );
			}			
		}
	}
	
	
	/** Indicates that a trial was scored */
	public void trialScored( final ScoreBoard scoreboard, final Trial trial ) {
		_eventProxy.trialScored( this, trial );
	}
	
	
	/** Indicates that a trial was vetoed */
	public void trialVetoed( final ScoreBoard scoreboard, final Trial trial ) {}
	
	
	/** A new optimal solution has been found */
	public void newOptimalSolution( final ScoreBoard scoreboard, final Trial trial ) {
		_bestSolution = trial;
		_eventProxy.newOptimalSolution( this, trial );
	}
	
	
	/**
	 * Handler which indicates that the solver's stopper has changed for the specified session.
	 * @param session the session whose stopper has changed.
	 */
	public void stopperChanged( SolverSession session ) {
		_eventProxy.optimizerSettingsChanged( this );
	}
	
	
	/**
	 * Handler that indicates that the enable state of the specified objective has changed.
	 * @param session the session whose objective enable state has changed
	 * @param objective the objective whose enable state has changed.
	 * @param isEnabled the new enable state of the objective.
	 */
	public void objectiveEnableChanged( SolverSession session, OpticsObjective objective, boolean isEnabled ) {
		_eventProxy.optimizerSettingsChanged( this );
	}
	
	
	/**
	 * Handler indicating that the specified objective's settings have changed.
	 * @param session the session whose objective has changed
	 * @param objective the objective whose settings have changed.
	 */
	public void objectiveSettingsChanged( SolverSession session, OpticsObjective objective ) {
		_eventProxy.optimizerSettingsChanged( this );
	}
	
	
	/** determine whether objective results can be exported */
	public boolean canExportObjectiveResults() {
		return _bestSolution != null;
	}
	
	
	/**
	 * Write the objectives out to the specified writer.
	 * @param writer the writer to which the objectives should be written
	 */
	public void exportObjectiveResults( final java.io.Writer writer ) throws java.io.IOException {
		final Trial solution = _bestSolution;
		
		if ( solution == null )   throw new RuntimeException( "Exception exporting results due to no evaluation having been run." );
		
		final Simulation simulation = (Simulation)solution.getCustomInfo();
		final double inputEnergy = simulation.getTrajectory().initialState().getKineticEnergy() / 1e6;		// input energy in MeV
		writer.write( "\nInput Energy (MeV):  " + inputEnergy + "\n" );
				
		writer.write( "\n########## \n" );
		writer.write( "# Objective Results \n" );
		writer.write( "# Objective  \tValue  \t% Satisfaction \n" );
		
		for ( final OpticsObjective objective : _activeSolverSession.getEnabledObjectives() ) {
			writer.write( objective.getLabel() );
			writer.write( "  \t" + objective.getDisplayValue( solution.getScore( objective ).getValue() ) );
			writer.write( "  \t" + 100 * solution.getSatisfaction( objective ) );
			writer.write( "\n" );
		}
	}
	
	
    /**
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    public String dataLabel() {
		return DATA_LABEL;
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		final DataAdaptor sessionAdaptor = adaptor.childAdaptor( _activeSolverSession.dataLabel() );
		_activeSolverSession.update( sessionAdaptor );
		
		final DataAdaptor solutionAdaptor = adaptor.childAdaptor( "solution" );
		if ( solutionAdaptor != null ) {
			readSolution( solutionAdaptor );			
		}
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		final DataAdaptor sessionAdaptor = adaptor.createChild( _activeSolverSession.dataLabel() );
		_activeSolverSession.write( sessionAdaptor );
		
		if ( _bestSolution != null ) {
			writeSolution( adaptor );
		}
	}
	
	
	/**
	 * Read the solution from the adaptor.
	 * @param solutionAdaptor the adaptor from which to read the solution
	 */
	protected void readSolution( final DataAdaptor solutionAdaptor ) {
		final Map<String, Double> valueTable = new HashMap<>();
        
        for (final DataAdaptor variableValueAdaptor : solutionAdaptor.childAdaptors("variableValue" )){
			final String name = variableValueAdaptor.stringValue( "name" );
			final double value = variableValueAdaptor.doubleValue( "value" );
			valueTable.put( name, new Double( value ) );
		}
		
		_activeSolverSession.prepareForSolving();
		final Map<Variable, Number> trialTable = new HashMap<>();
		final Iterator<Variable> variableIter = _activeSolverSession.getVariables().iterator();
		while( variableIter.hasNext() ) {
			final LiveParameterVariable variable = (LiveParameterVariable)variableIter.next();
			final Number value = valueTable.get( variable.getName() );
			trialTable.put( variable, value );
		}
		
		final TrialPoint trialPoint = new TrialPoint( trialTable );
		_bestSolution = new Trial( _activeSolverSession.getProblem(), trialPoint );
		
		report();	// report the best solution
		_activeSolverSession.cleanup();
	}
	
	
	/** Report on the specified trial as if we ran a single evaluation of the solver. Must be called within a prepared solver session.*/
	private void report() {
		try {
			_isRunning = true;
			_eventProxy.optimizationStarted( this );
			
			_activeSolverSession.getProblem().getEvaluator().evaluate( _bestSolution );
		}
		catch ( Exception exception ) {
			_eventProxy.optimizationFailed( this, exception );
		}
		finally {
			_isRunning = false;
			_eventProxy.newOptimalSolution( this, _bestSolution );
			_eventProxy.optimizationStopped( this );			
		}
	}
	
	
	/**
	 * Write the best solution to the data adaptor.
	 * @param adaptor the adaptor to which to write the solution
	 */
	protected void writeSolution( final DataAdaptor adaptor ) {
		final DataAdaptor solutionAdaptor = adaptor.createChild( "solution" );
		final Trial solution = _bestSolution;
		final Iterator<Variable> variableIter = _activeSolverSession.getVariables().iterator();
		while ( variableIter.hasNext() ) {
			final LiveParameterVariable variable = (LiveParameterVariable)variableIter.next();
			final DataAdaptor variableAdaptor = solutionAdaptor.createChild( "variableValue" );
			variableAdaptor.setValue( "name", variable.getName() );
			variableAdaptor.setValue( "value", solution.getTrialPoint().getValue( variable ) );
		}
	}
}





