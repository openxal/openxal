//
//  OnlineModelSimulator.java
//  xal
//
//  Created by Thomas Pelaia on 5/27/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.tools.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import xal.tools.beam.*;
import xal.model.*;
import xal.sim.scenario.Scenario;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.model.probe.*;
import xal.extension.solver.*;
import xal.model.alg.*;

import java.util.*;
import java.util.logging.*;


/** Simulate the optics using the online model. */
public class OnlineModelSimulator {
	/** the sequence over which the simulation is performed */
	final protected AcceleratorSeq _sequence;
	
	/** the nodes where the evaluations should be counted */
	protected List<AcceleratorNode> _evaluationNodes;
	
	/** the probe used in the online model run */
	protected Probe _probe;
	
	/** the scenario used in the online model run */
	protected Scenario _scenario;
	
	/** the design simulation */
	protected Simulation _designSimulation;
	
	/** indicator of whether the simulation is running */
	protected volatile boolean _isRunning;

	
	/**
	 * Primary Constructor
	 * @param sequence    The sequence over which the simulation is made.
	 * @param evaluationNodes The nodes at which twiss parameters are evaluated
	 * @param entranceProbe the custom initial probe
	 */
	public OnlineModelSimulator( final AcceleratorSeq sequence, final List<AcceleratorNode> evaluationNodes, final Probe entranceProbe ) {
		_isRunning = false;
		_sequence = sequence;

		_evaluationNodes = evaluationNodes;
		
		try {
			if ( entranceProbe != null ) {
				setEntranceProbe( entranceProbe );
			}
			else {
				_probe = getDefaultProbe( _sequence );
			}

			_scenario = Scenario.newScenarioFor( _sequence );
			_scenario.setSynchronizationMode( Scenario.SYNC_MODE_DESIGN );
			_scenario.setProbe( _probe );
			
			_designSimulation = null;
		}
		catch( xal.model.ModelException exception ) {
			exception.printStackTrace();
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log( Level.SEVERE, "Exception initializing the online model simulator.", exception );
		}
	}


	/**
	 * Constructor
	 * @param sequence    The sequence over which the simulation is made.
	 * @param evaluationNodes The nodes at which twiss parameters are evaluated
	 */
	public OnlineModelSimulator( final AcceleratorSeq sequence, final List<AcceleratorNode> evaluationNodes ) {
		this( sequence, evaluationNodes, null );
	}
	
	
	/**
	 * Constructor
	 * @param sequence    The sequence over which the simulation is made.
	 */
	public OnlineModelSimulator( final AcceleratorSeq sequence ) {
		this( sequence, generateEvaluationNodes( sequence ) );
	}
	
	
	/** 
	 * Populate the nodes that will be used for evaluating the corresponding probe states.
	 * @return a list of evaluation nodes
	 */
	static protected List<AcceleratorNode> generateEvaluationNodes( final AcceleratorSeq sequence ) {
		final List<AcceleratorNode> evaluationNodes = new ArrayList<>();
		
		if ( sequence != null ) {
			final OrTypeQualifier qualifier = new OrTypeQualifier().or( Quadrupole.s_strType ).or( RfCavity.s_strType);
			evaluationNodes.addAll( sequence.getNodesWithQualifier( qualifier ) );
		}
		
		return evaluationNodes;
	}
	
	
	/**
	 * Get the list of evaluation nodes.
	 * @return the list of evaluation nodes
	 */
	public List<AcceleratorNode> getEvaluationNodes() {
		return _evaluationNodes;
	}
	
	
	/**
	 * Set the new evaluation nodes.
	 * @param evaluationNodes the new list of evaluation nodes
	 */
	public void setEvaluationNodes( final List<AcceleratorNode> evaluationNodes ) {
		if ( !_isRunning ) {
			_evaluationNodes = evaluationNodes;
			_designSimulation = null;			
		}
		else {
			throw new RuntimeException( "Can't change evaluation nodes while a simulation is in progress!" );
		}
	}
	
	
	/**
	 * Set the entrance probe.
	 * @param entranceProbe the new entrance probe
	 */
	public void setEntranceProbe( final Probe entranceProbe ) {
		if ( !_isRunning ) {
			final Probe probe = copyProbe( entranceProbe );
			probe.reset();
			_probe = probe;
			
			if ( _scenario != null ) {
				_scenario.setProbe( probe );
			}
		}
		else {
			throw new RuntimeException( "Can't change probe while a simulation is in progress!" );
		}
	}


    /**
     * Construct a new probe from the given probe
     * @param otherProbe probe to copy
     * @return new probe constructed from the given probe
     */
    static private Probe copyProbe( final Probe otherProbe ) {
        final Probe probe = otherProbe.copy();		// performs a deep copy of the probe including algorithm
        probe.initialize();
        return probe;
    }


	/**
	 * Apply the tracker update policy to the current probe (does not stick for future probes)
	 * @param policy one of the Tracker update policies: UPDATE_ALWAYS, UPDATE_ENTRANCE, UPDATE_EXIT, UPDATE_ENTRANCEANDEXIT
	 */
	public void applyTrackerUpdatePolicy( final int policy ) {
		if ( _probe != null ) {
			final Tracker tracker = (Tracker)_probe.getAlgorithm();
			tracker.setProbeUpdatePolicy( policy );
		}
	}
	
	
	/**
	 * Get the probe.
	 * @return the probe used in the simulation
	 */
	public Probe getProbe() {
		return _probe;
	}
	
	
	/**
	 * Get the default probe for the specified sequence.
	 * @param sequence the sequence for which to get the default probe
	 * @return the default probe for the specified sequence
	 */
	static public Probe getDefaultProbe( final AcceleratorSeq sequence ) {
        try {
            final Probe probe = (sequence instanceof Ring) ? createRingProbe( sequence ) : createEnvelopeProbe( sequence );
            probe.getAlgorithm().setRfGapPhaseCalculation( true );	// make sure we enable the full RF gap phase slip calculation
			return probe;
        }
        catch ( InstantiationException exception ) {
            System.err.println( "Instantiation exception creating probe." );
            exception.printStackTrace();
			return null;
        }
	}


	/** create a new ring probe */
	static private Probe createRingProbe( final AcceleratorSeq sequence ) throws InstantiationException {
		final TransferMapTracker tracker = AlgorithmFactory.createTransferMapTracker( sequence );
		return ProbeFactory.getTransferMapProbe( sequence, tracker );
	}


	/** create a new envelope probe */
	static private Probe createEnvelopeProbe( final AcceleratorSeq sequence ) throws InstantiationException {
		final IAlgorithm tracker = AlgorithmFactory.createEnvTrackerAdapt( sequence );
		return ProbeFactory.getEnvelopeProbe( sequence, tracker );
	}
	
	
	/**
	 * Get the default kinetic energy of the incoming particle.
	 * @param sequence the sequence for which to get the particle's design kinetic energy at the entrance
	 * @return the default kinetic energy of a particle at the entrance to the sequence
	 */
	static public double getDefaultEntranceKineticEnergy( final AcceleratorSeq sequence ) {
		return getDefaultProbe( sequence ).getKineticEnergy();
	}
	
	
	/**
	 * Get the design simulation.
	 * @return the design simulation
	 */
	public Simulation getDesignSimulation() {
		if ( _designSimulation == null ) {
			_designSimulation = new OnlineModelSimulator( _sequence, _evaluationNodes ).run();
		}
		
		return _designSimulation;
	}
	
	
	/**
	 * Run the simulation with the specified core parameters and then cleanup the model input parameters.
	 * @param coreParameters the core parameters with which to perform the simulation run
	 * @return the simulation
	 */
	public Simulation runWithParametersAndCleanup( final List<CoreParameter> coreParameters ) {
		final List<CoreParameter> customParameters = filterCustomParameters( coreParameters );
		setFixedCustomParameterValues( customParameters );
		final Simulation simulation = run();
		cleanupFixedCustomParameters( customParameters );
		
		return simulation;
	}
	
	
	/** 
	* Run the model.
	* @param variableValues map of variable values keyed by variable
	* @return the generated simulation or null if the run failed
	*/
	public Simulation runWithVariables( final Map<Variable, Number> variableValues ) {
		applyVariableValues( variableValues );
		return run();
	}
	
	
	/**
	 * Run the simulation.
	 * @return the generated simulation or null if the run failed.
	 */
	public Simulation run() {
		try {
			_isRunning = true;
			_probe.reset();

			_scenario.resync();
			_scenario.run();
			
			return new Simulation( _probe, _evaluationNodes );
		}
		catch( Exception exception ) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log( Level.SEVERE, "Exception running the online model.", exception );
			exception.printStackTrace();
			System.out.println( "online model calculation failed..." );
			return null;
		}
		finally {
			_isRunning = false;
		}
	}
	
	
	/**
	 * Set the initial conditions for the fixed parameters.
	 * @param fixedCustomParameters the custom parameters with fixed values
	 */
	public void setFixedCustomParameterValues( final List<CoreParameter> fixedCustomParameters ) {
		for ( CoreParameter parameter : fixedCustomParameters ) {
			final String accessorField = parameter.getAccessorField();
			final double initialValue = parameter.getInitialValue();
			final List<LiveParameter> liveParameters = parameter.getLiveParameters();
			for ( LiveParameter liveParameter : liveParameters ) {
				_scenario.setModelInput( liveParameter.getNode(), accessorField, liveParameter.toPhysical( initialValue ) );
			}
		}
	}
	
	
	/**
	 * Apply the parameter variables to initialize the scenario's elements.
	 * @param variableValues the parameter variables specifying the values of the element parameters.
	 */
	protected void applyVariableValues( final Map<Variable, Number> variableValues ) {
		final Iterator<Map.Entry<Variable, Number>> variableEntriesIter = variableValues.entrySet().iterator();
		while ( variableEntriesIter.hasNext() ) {
            
			final Map.Entry<Variable, Number> variableEntry = variableEntriesIter.next();
            
			final LiveParameterVariable variable = (LiveParameterVariable)variableEntry.getKey();
            
			final String accessorField = variable.getAccessorField();
            
			final double value = ((Double)variableEntry.getValue()).doubleValue();
            
			final List<LiveParameter> liveParameters = variable.getLiveParameters();
            
			final int nodeCount = liveParameters.size();
            
			for ( int index = 0 ; index < nodeCount ; index++ ) {
				final LiveParameter liveParameter = liveParameters.get( index );
				_scenario.setModelInput( liveParameter.getNode(), accessorField, liveParameter.toPhysical( value ) );
			}
		}
	}
	
	
	/** cleanup model inputs */
	public void cleanup( final List<Variable> variables, final List<CoreParameter> fixedCustomParameters ) {
		cleanupFixedCustomParameters( fixedCustomParameters );
		cleanupVariables( variables );
	}
	
	
	/** cleanup the fixed custom parameter model inputs */
	private void cleanupFixedCustomParameters( final List<CoreParameter> fixedCustomParameters ) {
		final Iterator<CoreParameter> parameterIter = fixedCustomParameters.iterator();
		while ( parameterIter.hasNext() ) {
			final CoreParameter parameter = parameterIter.next();
			final String accessorField = parameter.getAccessorField();
			final List<LiveParameter> liveParameters = parameter.getLiveParameters();
			final int nodeCount = liveParameters.size();
			for ( int index = 0 ; index < nodeCount ; index++ ) {
				final LiveParameter liveParameter = liveParameters.get( index );
				_scenario.removeModelInput( liveParameter.getNode(), accessorField );
			}
		}		
	}
	
	
	/**
	 * Cleanup model inputs for variables.
	 * @param variables the parameter variables to cleanup.
	 */
	protected void cleanupVariables( final List<Variable> variables ) {
		final Iterator<Variable> variableIter = variables.iterator();
		while ( variableIter.hasNext() ) {
			final LiveParameterVariable variable = (LiveParameterVariable)variableIter.next();
			final String accessorField = variable.getAccessorField();
			final List<AcceleratorNode> nodes = variable.getNodes();
			final int nodeCount = nodes.size();
			for ( int index = 0 ; index < nodeCount ; index++ ) {
				final AcceleratorNode node = nodes.get( index );
				_scenario.removeModelInput( node, accessorField );
			}
		}
	}
	
	
	/**
	 * Determine which parameters are custom parameters.
	 * @return the parameters which are custom
	 */
	protected List<CoreParameter> filterCustomParameters( final List<CoreParameter> coreParameters ) {
		final List<CoreParameter> customParameters = new ArrayList<>();
		
		final Iterator<CoreParameter> parameterIter = coreParameters.iterator();
		while ( parameterIter.hasNext() ) {
			final CoreParameter parameter = parameterIter.next();
			if ( parameter.getActiveSource() != CoreParameter.DESIGN_SOURCE ) {
				customParameters.add( parameter );
			}
		}
				
		return customParameters;
	}
}



