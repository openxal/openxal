//
//  SolverSession.java
//  xal
//
//  Created by Thomas Pelaia on 6/9/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xal.model.probe.Probe;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
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


/** Stores a solver configuration and the corresponding result. */
public class SolverSession implements OpticsObjectiveListener, DataListener {

    
    /*
     * Constants
     */
    
    /** index of the X coordinate result */
    final static public int INT_INDEX_X = 0;
    
    /** index of the Y coordinate result */
    final static public int INT_INDEX_Y = 1;
    
    /** index of the Z coordinate result */
    final static public int INT_INDEX_Z = 2;
        
    
    /** Message center for dispatching events to registered listeners. */
	private final MessageCenter _messageCenter;
	
	/** Proxy which forwards events to registered listeners. */
	private final SolverSessionListener _eventProxy;
	
	/** simulator used to predict the physical behavior */
	private final OnlineModelSimulator _simulator;
	
	/** core parameters */
	final protected List<CoreParameter> _coreParameters;
	
	/** the accelerator sequence */
	final protected AcceleratorSeq _sequence;
	
	/** the list of all available objectives */
	final protected List<OpticsObjective> _objectives;
	
	/** table of all available objectives keyed by name */
	final protected Map<String, OpticsObjective> _objectivesTable;
	
	/** the problem to solve */
	final protected Problem _problem;
	
	/** the name of the session */
	protected String _name;
	
	/** the fixed custom parameters */
	protected List<CoreParameter> _fixedCustomParameters;
	
	/** the live parameter variables */
	protected List<Variable> _variables;
	
	/** the solver stopper  */
	protected Stopper _stopper;
	
	/** the minimum solving time in seconds */
	protected double _minSolveTime;
	
	/** the maximum solving time in seconds */
	protected double _maxSolveTime;
	
	/** the target overall satisfaction */
	protected double _targetSatisfaction;
	
	
	/**
	 * Constructor
	 */
	public SolverSession( final String name, final AcceleratorSeq sequence, final List<AcceleratorNode> evaluationNodes, final Probe<?> entranceProbe, final List<CoreParameter> coreParameters ) {
		_messageCenter = new MessageCenter( "Solver Session" );
		_eventProxy = _messageCenter.registerSource( this, SolverSessionListener.class );
		
		setName( name );
		_sequence = sequence;
		_coreParameters = coreParameters;
		
		_simulator = new OnlineModelSimulator( _sequence, evaluationNodes, entranceProbe );
		
		_problem = new Problem();
		_problem.addHint( InitialDomain.getFractionalDomainHint( 0.01 ) );	// change variables by 1% initially
		_problem.setEvaluator( new OpticsEvaluator( _simulator ) );
		
		_objectives = new ArrayList<OpticsObjective>();
		_objectivesTable = new HashMap<String, OpticsObjective>();
		makeObjectives();
		
		_minSolveTime = 30.0;
		_maxSolveTime = 1000.0;
		_targetSatisfaction = 0.99;
		
		_stopper = SolveStopperFactory.minMaxTimeSatisfactionStopper( _minSolveTime, _maxSolveTime, _targetSatisfaction );		
	}
	
	
	/** Prepare for a new solve */
	public void prepareForSolving() {
		getDesignSimulation();		// do this first to avoid contamination from custom settings
		_variables = determineVariables();
		_simulator.setFixedCustomParameterValues( _fixedCustomParameters );
		_problem.setVariables( _variables );
		_problem.setObjectives( getEnabledObjectives() );
	}
	
	
	/** Cleanup after a solve */
	public void cleanup() {
		_simulator.cleanup( _variables, _fixedCustomParameters );
	}
	
	
	/**
	 * Add the specified listener to receive solver session event notifications from this object.
	 * @param listener the listener to receive the event notifications.
	 */
	public void addSolverSessionListener( final SolverSessionListener listener ) {
		_messageCenter.registerTarget( listener, this, SolverSessionListener.class );
	}
	
	
	/**
	 * Remove the specified listener from receiving solver session event notifications from this object.
	 * @param listener the listener to remove from receiving event notifications.
	 */
	public void removeSolverSessionListener( final SolverSessionListener listener ) {
		_messageCenter.removeTarget( listener, this, SolverSessionListener.class );
	}
	
	
	/**
	 * Get the design simulation.
	 * @return the design simulation
	 */
	public Simulation getDesignSimulation() {
		return _simulator.getDesignSimulation();
	}
	
	
	/**
	 * Set the new evaluation nodes.
	 * @param evaluationNodes the new list of evaluation nodes
	 */
	public void setEvaluationNodes( final List<AcceleratorNode> evaluationNodes ) {
		_simulator.setEvaluationNodes( evaluationNodes );
	}
	
	
	/**
	 * Set the entrance probe.
	 * @param entranceProbe the new entrance probe
	 */
	public void setEntranceProbe( final Probe<?> entranceProbe ) {
		_simulator.setEntranceProbe( entranceProbe );
	}
	
	
	
	/** Make the objectives. */
	protected void makeObjectives() {
		addObjective( new EnergyObjective( 3.0, 0.1 ) );
		
		addObjective( new BetaMeanErrorObjective( "Mean Beta Error X", INT_INDEX_X, 0.1 ) );
		addObjective( new BetaMeanErrorObjective( "Mean Beta Error Y", INT_INDEX_Y, 0.1 ) );
		addObjective( new BetaMeanErrorObjective( "Mean Beta Error Z", INT_INDEX_Z, 0.1 ) );
		
		addObjective( new BetaWorstErrorObjective( "Worst Beta Error X", INT_INDEX_X, 0.1 ) );
		addObjective( new BetaWorstErrorObjective( "Worst Beta Error Y", INT_INDEX_Y, 0.1 ) );
		addObjective( new BetaWorstErrorObjective( "Worst Beta Error Z", INT_INDEX_Z, 0.1 ) );
		
		addObjective( new BetaMaxObjective( "Maximum Beta X", INT_INDEX_X, 10.0, 2.0 ) );
		addObjective( new BetaMaxObjective( "Maximum Beta Y", INT_INDEX_Y, 10.0, 2.0 ) );
		addObjective( new BetaMaxObjective( "Maximum Beta Z", INT_INDEX_Z, 10.0, 2.0 ) );
		
		addObjective( new BetaMinObjective( "Minimum Beta X", INT_INDEX_X, 5.0, 2.0 ) );
		addObjective( new BetaMinObjective( "Minimum Beta Y", INT_INDEX_Y, 5.0, 2.0 ) );
		addObjective( new BetaMinObjective( "Minimum Beta Z", INT_INDEX_Z, 5.0, 2.0 ) );
		
		addObjective( new EtaMaxObjective( "Maximum Eta X", INT_INDEX_X, 3.0, 1.0 ) );
		addObjective( new EtaMaxObjective( "Maximum Eta Y", INT_INDEX_Y, 3.0, 1.0 ) );
		
		addObjective( new EtaMinObjective( "Minimum Eta X", INT_INDEX_X, 0.0, 1.0 ) );
		addObjective( new EtaMinObjective( "Minimum Eta Y", INT_INDEX_Y, 0.0, 1.0 ) );
	}
	
	
	/**
	 * Add an objective.
	 * @param objective the objective to add.
	 */
	protected void addObjective( final OpticsObjective objective ) {
		_objectives.add( objective );
		_objectivesTable.put( objective.getName(), objective );
		objective.addOpticsObjectiveListener( this );
	}
	
	
	/**
	 * Get an objective by its name.
	 * @param name the name of the objective to get
	 * @return the objective corresponding to the specified name or null if no match exists
	 */
	protected OpticsObjective getObjective( final String name ) {
		return _objectivesTable.get( name );
	}
	
	
	/**
	 * Get the list of enabled objectives.
	 * @return enabled objectives
	 */
	protected List<OpticsObjective> getEnabledObjectives() {
		final List<OpticsObjective> enabledObjectives = new ArrayList<>( _objectives.size() );

		for ( final OpticsObjective objective : _objectives ) {
			if ( objective.isEnabled() ) {
				enabledObjectives.add( objective );
			}
		}
		
		return enabledObjectives;
	}
	
	
	/**
	 * Get the minimum solving time.
	 * @return the minimum solving time in seconds
	 */
	public double getMinSolveTime() {
		return _minSolveTime;
	}
	
	
	/**
	 * Get the maximum solving time.
	 * @return the maximum solving time in seconds
	 */
	public double getMaxSolveTime() {
		return _maxSolveTime;
	}
	
	
	/**
	 * Get the overall target satisfaction.
	 * @return the target satisfaction
	 */
	public double getTargetSatisfaction() {
		return _targetSatisfaction;
	}
	
	
	/**
	 * Set the minimum and maximum solving time and the target satisfaction.
	 * @param minDuration the new minimum solving time in seconds
	 * @param maxDuration the new maximum solving time in seconds
	 * @param targetSatisfaction the new target satisfaction (stops the solve when reached and time is within the min/max duration range)
	 */
	public void setStopperSettings( final double minDuration, final double maxDuration, final double targetSatisfaction ) {
		if ( minDuration != _minSolveTime ) {
			_minSolveTime = minDuration;
		}
		
		if ( maxDuration != _maxSolveTime ) {
			_maxSolveTime = maxDuration;
		}
		
		if ( targetSatisfaction != _targetSatisfaction ) {
			_targetSatisfaction = targetSatisfaction;
		}
		
		_eventProxy.stopperChanged( this );			
	}
	
	
	/**
	 * Update solve duration.
	 * @param duration the new solving duration.
	 */
	public void updateSolveDuration( final double duration ) {
		setStopperSettings( _minSolveTime, duration, _targetSatisfaction );
	}
	
	
	/** Update the problem with based on any changes made. */
	public void updatedProblem() {
		_problem.setObjectives( getEnabledObjectives() );
	}
	
	
	/**
	 * Get the problem.
	 * @return the problem to solve
	 */
	public Problem getProblem() {
		return _problem;
	}
	
	
	/**
	 * Get the fixed custom parameters.
	 * @return the fixed custom parameters
	 */
	public List<CoreParameter> getFixedCustomParameters() {
		return _fixedCustomParameters;
	}
	
	
	/**
	 * Get the variables.
	 * @return the variables
	 */
	public List<Variable> getVariables() {
		return _variables;
	}
	
	
	/**
	 * Get all available objectives.
	 * @return all available objectives.
	 */
	public List<OpticsObjective> getObjectives() {
		return _objectives;
	}
	
	
	/**
	 * Get the solver stopper.
	 * @return the stopper used to stop the solving progress
	 */
	public Stopper getStopper() {
		return SolveStopperFactory.minMaxTimeSatisfactionStopper( _minSolveTime, _maxSolveTime, _targetSatisfaction );
	}
	
	
	/**
	 * Set the name for this session.
	 * @param name the name for this session
	 */
	public void setName( final String name ) {
		_name = name;
	}
	
	
	/** Disable all objectives */
	protected void disableAllObjectives() {
		for ( final OpticsObjective objective : _objectives ) {
			objective.setEnable( false );
		}
	}
	
	
	/**
	 * Determine which parameters are variables.
	 * @return the parameter which are variables
	 */
	protected List<Variable> determineVariables() {
		_fixedCustomParameters = new ArrayList<CoreParameter>();
		
		final List<Variable> variables = new ArrayList<Variable>();
		for ( final CoreParameter parameter : _coreParameters ) {
			if ( parameter.isVariable() ) {
				variables.add( new LiveParameterVariable( parameter ) );
			}
			else if ( parameter.getActiveSource() != CoreParameter.DESIGN_SOURCE ) {
				_fixedCustomParameters.add( parameter );
			}
		}
		
		System.out.println( "fixed parameters: " + _fixedCustomParameters );
		System.out.println( "variables: " + variables );
		
		return variables;
	}
	
	
    /**
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    public String dataLabel() {
		return "SolverSession";
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		setName( adaptor.stringValue( "name" ) );
		final double minDuration = adaptor.doubleValue( "minSolveTime" );
		final double maxDuration = adaptor.doubleValue( "maxSolveTime" );
		final double targetSatisfaction = adaptor.doubleValue( "targetSatisfaction" );
		setStopperSettings( minDuration, maxDuration, targetSatisfaction );
		
		disableAllObjectives();		// only enable the objectives explicitly enabled in the adaptor
		for (final DataAdaptor objectiveAdaptor : adaptor.childAdaptors(OpticsObjective.DATA_LABEL)){
			final String name = objectiveAdaptor.stringValue( "name" );
			final OpticsObjective objective = getObjective( name );
			objective.update( objectiveAdaptor );
		}
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "name", _name );
		adaptor.setValue( "minSolveTime", _minSolveTime );
		adaptor.setValue( "maxSolveTime", _maxSolveTime );
		adaptor.setValue( "targetSatisfaction", _targetSatisfaction );

		for ( final OpticsObjective objective : _objectives ) {
			final DataAdaptor objectiveAdaptor = adaptor.createChild( objective.dataLabel() );
			objective.write( objectiveAdaptor );
		}
	}
	
	
	/**
	 * Handler that indicates that the enable state of the specified objective has changed.
	 * @param objective the objective whose state has changed.
	 * @param isEnabled the new enable state of the objective.
	 */
	public void objectiveEnableChanged( OpticsObjective objective, boolean isEnabled ) {
		_eventProxy.objectiveEnableChanged( this, objective, isEnabled );
	}
	
	
	/**
	 * Handler indicating that the specified objective's settings have changed.
	 * @param objective the objective whose settings have changed.
	 */
	public void objectiveSettingsChanged( OpticsObjective objective ) {
		_eventProxy.objectiveSettingsChanged( this, objective );
	}
}
