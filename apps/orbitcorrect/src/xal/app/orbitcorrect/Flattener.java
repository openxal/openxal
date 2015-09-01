/*
 *  Flattener.java
 *
 *  Created on Tue Sep 07 12:57:34 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.tools.messaging.*;
import xal.extension.solver.*;
import xal.extension.solver.solutionjudge.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.hint.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.ca.*;
import xal.tools.data.*;

import java.util.*;
import java.util.logging.*;


/**
 * Flattener
 * @author   tap
 * @since    Sep 07, 2004
 */
public class Flattener implements ScoreBoardListener, DataListener, OrbitModelListener, PutListener {
	/** data label */
	static public String DATA_LABEL = "Flattener";
	
	/** simulators that have been loaded and keyed by simulator index */
	protected Map<String, MachineSimulator> _loadedSimulators;
	
	/** machine simulator */
	protected MachineSimulator _simulator;
	
	/** the orbit model */
	protected OrbitModel _orbitModel;

	/** the solver to use in finding the flattened orbit */
	protected Solver _solver;

	/** the scheduled time in seconds for finding the flattened orbit */
	protected double _solvingTime;

	/** message center */
	protected final MessageCenter _messageCenter;

	/** proxy which forwards events to registered listeners */
	protected final FlattenListener _eventProxy;

	/** optimal corrector distribution */
	protected CorrectorDistribution _optimalCorrectorDistribution;
	
	/** initial corrector distribution */
	protected CorrectorDistribution _initialCorrectorDistribution;
	
	/** objective for X distortion */
	protected DisplacementObjective _xDistortionObjective;
	
	/** objective for Y distortion */
	protected DisplacementObjective _yDistortionObjective;

	/** objective for the corrector duty (attempt to reduce corrector strengths) */
	protected CorrectorDutyObjective _correctorDutyObjective;

	/** objective to reduce the RMS horizontal orbit angles */
	protected SmoothnessObjective _xSmoothnessObjective;

	/** objective to reduce the RMS vertical orbit angles */
	protected SmoothnessObjective _ySmoothnessObjective;

	/** objectives */
	protected List<OrbitObjective> _objectives;
	
	
	

	/**
	 * Primary Constructor
	 * @param orbitModel the orbit model
	 * @param simulator  the machine simulator
	 * @param solvingTime  the amount of time in seconds to search for a better corrector distribution
	 */
	public Flattener( final OrbitModel orbitModel, final MachineSimulator simulator, final double solvingTime ) {
		_messageCenter = new MessageCenter( "Flattener" );
		_eventProxy = _messageCenter.registerSource( this, FlattenListener.class );
		
		_orbitModel = orbitModel;
		_orbitModel.addOrbitModelListener( this );
		_loadedSimulators = new HashMap<String, MachineSimulator>(2);

		setSimulator( simulator );
		setSolvingTime( solvingTime );
		setupObjectives();		
	}
	
	
	/**
	 * Constructor
	 * @param simulator  the machine simulator
	 */
	public Flattener( final OrbitModel orbitModel, final MachineSimulator simulator ) {
		this( orbitModel, simulator, 60.0 );    // default solving time of 60 seconds
	}
	

	/** Constructor  */
	public Flattener( final OrbitModel orbitModel ) {
		this( orbitModel, new OnlineModelSimulator( orbitModel ) );
	}
	
	
	/** setup the Objectives */
	protected void setupObjectives() {
		_objectives = new ArrayList<OrbitObjective>();
		
		_xDistortionObjective = new DisplacementObjective( Orbit.X_PLANE );
		_objectives.add( _xDistortionObjective );
	
		_yDistortionObjective = new DisplacementObjective( Orbit.Y_PLANE );
		_objectives.add( _yDistortionObjective );

		_correctorDutyObjective = new CorrectorDutyObjective();
		_correctorDutyObjective.setEnabled( false );	// disable this objective unless the user enables it manually
		_objectives.add( _correctorDutyObjective );

		_xSmoothnessObjective = new SmoothnessObjective( Orbit.X_PLANE );
		_xSmoothnessObjective.setEnabled( false );		// disable this objective unless the user enables it manually
		_objectives.add( _xSmoothnessObjective );

		_ySmoothnessObjective = new SmoothnessObjective( Orbit.Y_PLANE );
		_ySmoothnessObjective.setEnabled( false );		// disable this objective unless the user enables it manually
		_objectives.add( _ySmoothnessObjective );
	}


	/**
	 * Add the specified listener as a receiver of FlattenListener events from this instance.
	 * @param listener  The feature to be added to the FlattenListener attribute
	 */
	public void addFlattenListener( final FlattenListener listener ) {
		_messageCenter.registerTarget( listener, this, FlattenListener.class );
		listener.simulatorChanged( this, _simulator );
		listener.solvingTimeChanged( this, _solvingTime );
	}


	/**
	 * Remove the specified listener from receiving FlattenListener events from this instance.
	 * @param listener  Description of the Parameter
	 */
	public void removeFlattenListener( final FlattenListener listener ) {
		_messageCenter.removeTarget( listener, this, FlattenListener.class );
	}


	/**
	 * Get the machine simulator.
	 * @return   The machine simulator
	 */
	public MachineSimulator getSimulator() {
		return _simulator;
	}


	/**
	 * Set the machine simulator.
	 * @param simulator  The new machine simulator
	 */
	public void setSimulator( final MachineSimulator simulator ) {
		if ( simulator != _simulator ) {
			_simulator = simulator;
			_loadedSimulators.put( simulator.getSimulatorType(), simulator );
			_eventProxy.simulatorChanged( this, simulator );
			_orbitModel.getModificationStore().postModification( this );
		}
	}
	
	
	/**
	 * Load the simulator of the specified type.
	 * @param type the simulator type
	 * @return the simulator corresponding to the specified type
	 */
	public MachineSimulator loadSimulator( final String type ) {
		MachineSimulator simulator = null;
		if ( !_loadedSimulators.containsKey( type ) ) {
			if ( type.equals( EmpiricalSimulator.getType() ) ) {
				simulator =  new EmpiricalSimulator( _orbitModel );
			}
			else if ( type.equals( OnlineModelSimulator.getType() ) ) {
				simulator = new OnlineModelSimulator( _orbitModel );
			}
			else {
				return null;
			}
		}
		else {
			simulator = _loadedSimulators.get( type );
		}
		
		setSimulator( simulator );
		return simulator;
	}
    
    
    /** clear any caches (good idea if the flatten failed) to start fresh next time. */
    public void clear() {
        if ( _simulator != null )  _simulator.clear();
    }


	/**
	 * Set the sequence over which to flatten the orbit.
	 * @param sequence    The new sequence
	 * @param bpms        The BPMs
	 * @param supplies  The corrector supplies
	 */
	public void setSequence( final AcceleratorSeq sequence, final List<BpmAgent> bpms, final List<CorrectorSupply> supplies ) {
		_simulator.setSequence( sequence, bpms, supplies );
		_eventProxy.sequenceChanged( this, sequence );
	}


	/**
	 * Get the sequence over which to flatten the orbit.
	 * @return   The sequence
	 */
	public AcceleratorSeq getSequence() {
		return _simulator.getSequence();
	}


	/**
	 * Get the BPMs that define the orbit to flatten.
	 * @return   The BPMs
	 */
	public List<BpmAgent> getBPMAgents() {
		return _simulator.getBPMAgents();
	}


	/**
	 * Set the BPM agents to those specified.
	 * @param bpmAgents  The new BPM agents
	 */
	public void setBPMAgents( final List<BpmAgent> bpmAgents ) {
		_simulator.setBPMAgents( bpmAgents );
		_eventProxy.bpmsChanged( this, bpmAgents );
	}
	
	
	/**
	 * Get the enabled correctors.
	 * @return the enabled correctors
	 */
	public List<CorrectorSupply> getEnabledCorrectorSupplies() {
		return _simulator.getEnabledCorrectorSupplies();
	}


	/**
	 * Get the correctors with which to flatten the orbit.
	 * @return   The correctors with which to flatten the orbit
	 */
	public List<CorrectorSupply> getCorrectorSupplies() {
		return _simulator.getCorrectorSupplies();
	}


	/**
	 * Set the corrector supplies to those specified.
	 * @param supplies  The new corrector supplies
	 */
	public void setCorrectorSupplies( final List<CorrectorSupply> supplies ) {
		_simulator.setCorrectorSupplies( supplies );
		_eventProxy.correctorSuppliesChanged( this, supplies );
	}
	
	
	/**
	 * Get the objectives.
	 * @return the objectives
	 */
	public List<OrbitObjective> getObjectives() {
		return _objectives;
	}


	/**
	 * Flatten the orbit from the specified orbit source.
	 * @param orbitSource    the orbit source whose orbit is the target of flattening
	 * @return               true if successful and false if not
	 * @exception Exception  Description of the Exception
	 */
	public boolean flatten( final OrbitSource orbitSource ) throws Exception {
		return flatten( orbitSource.getOrbit() );
	}


	/**
	 * Flatten the specified orbit.
	 * @param orbit       the orbit to flatten
	 * @return            true if successful and false if not
	 * @throws Exception  Some Exception
	 */
	public boolean flatten( final Orbit orbit ) throws Exception {
		_eventProxy.progressUpdated( this, 0, "Initializing..." );
        
        if ( getSequence() == null ) {
            throw new RuntimeException( "An accelerator sequence must be selected..." );
        }
		
		final List<CorrectorSupply> correctorSupplies = getCorrectorSuppliesToVary();
		
		_simulator.setCorrectorSuppliesToSimulate( correctorSupplies );
		_simulator.reset();		
		if ( !_simulator.prepare() ) {
			_eventProxy.progressUpdated( this, 0, "Initialization interrupted" );
			return false;
		}

		final Problem problem = new Problem();
		final Map<CorrectorSupply,Variable> variables = new HashMap<CorrectorSupply,Variable>();

		final double xRmsDisplacement = orbit.getOrbitPlane( Orbit.X_PLANE ).rmsDisplacement();
		final double yRmsDisplacement = orbit.getOrbitPlane( Orbit.Y_PLANE ).rmsDisplacement();
		final double xRmsAngle = orbit.getOrbitPlane( Orbit.X_PLANE ).rmsAngle();
		final double yRmsAngle = orbit.getOrbitPlane( Orbit.Y_PLANE ).rmsAngle();

		_initialCorrectorDistribution = presentCorrectorDistribution();
		
		System.out.println( "Corrector supplies to vary: " + correctorSupplies );
		System.out.println( "Initial corrector supply fields:" );
		for ( CorrectorSupply correctorSupply : correctorSupplies  ) {			
			// fetch the lower and upper field limits and use them only if they are reasonable
			final double liveLowerLimit = correctorSupply.getLowerFieldLimit();
			final double liveUpperLimit = correctorSupply.getUpperFieldLimit();

			final double lowerLimit = ( !Double.isNaN( liveLowerLimit ) && liveLowerLimit > -10 ) ? liveLowerLimit : -0.01;
			final double upperLimit = ( !Double.isNaN( liveUpperLimit ) && liveUpperLimit < 10 ) ? liveUpperLimit : 0.01;
				
			final double field = _initialCorrectorDistribution.getField( correctorSupply );
			System.out.println( correctorSupply.getID() + ", field: " + field + ", lower limit: " + lowerLimit + ", upper limit: " + upperLimit );
			final Variable variable = new Variable( correctorSupply.getID(), field, lowerLimit, upperLimit );
			problem.addVariable( variable );
			variables.put( correctorSupply, variable );
		}
		
		problem.addHint( InitialDomain.getFractionalDomainHint( 0.05 ) );

		final FlattenEvaluator evaluator = new FlattenEvaluator( orbit, _initialCorrectorDistribution, _simulator, variables );
		problem.setEvaluator( evaluator );
		
		_xDistortionObjective.setOrbitScale( xRmsDisplacement );
		_yDistortionObjective.setOrbitScale( yRmsDisplacement );

		_xSmoothnessObjective.setAngleScale( xRmsAngle );
		_ySmoothnessObjective.setAngleScale( yRmsAngle );

		for ( OrbitObjective objective : _objectives ) {
			if ( objective.isEnabled() ) {
				problem.addObjective( objective );
			}
		}
		
		if ( problem.getObjectives().size() < 1 ) {
			throw new RuntimeException( "At least 1 objective must be enabled..." );
		}

		final double initialCorrectorDuty = _correctorDutyObjective.score( orbit, _initialCorrectorDistribution );

		// Setup the solver
		if ( _solver != null ) {
			_solver.getScoreBoard().removeScoreBoardListener( this );
		}
		//_solver = new Solver( new RandomShrinkSearch(), generateSolvingStopper() );
		_solver = new Solver( generateSolvingStopper() );
		_solver.setSolutionJudge( new WorstObjectiveBiasedJudge() );
		_solver.getScoreBoard().addScoreBoardListener( this );

		// solve the problem
		_eventProxy.progressUpdated( this, 0, "Searching for optimal correctors..." );
        try {
            _solver.solve( problem );
        }
        catch( Exception exception ) {
            clear();
            throw exception;
        }
		System.out.println( _solver.getScoreBoard() );

		final Trial solution = _solver.getScoreBoard().getBestSolution();
		final MutableCorrectorDistribution bestDistribution = new MutableCorrectorDistribution();
		evaluator.updateCorrectorDistribution( bestDistribution, solution.getTrialPoint() );
		_optimalCorrectorDistribution = bestDistribution.getDistribution();
		final Orbit predictedOrbit = _simulator.predictOrbit( orbit, _initialCorrectorDistribution, bestDistribution );

		final double finalCorrectorDuty = _correctorDutyObjective.score( predictedOrbit, _optimalCorrectorDistribution );

		System.out.println( "Initial Corrector Duty: " + initialCorrectorDuty );
		System.out.println( "Final Corrector Duty: " + finalCorrectorDuty );

		System.out.println( "Initial X RMS: " + xRmsDisplacement );
		System.out.println( "Predicted X RMS: " + predictedOrbit.getOrbitPlane( Orbit.X_PLANE ).rmsDisplacement() );
		System.out.println( "Initial X-Angle RMS: " + xRmsAngle );
		System.out.println( "Predicted X-Angle RMS: " + predictedOrbit.getOrbitPlane( Orbit.X_PLANE ).rmsAngle() );
		System.out.println( "Initial Y RMS: " + yRmsDisplacement );
		System.out.println( "Predicted Y RMS: " + predictedOrbit.getOrbitPlane( Orbit.Y_PLANE ).rmsDisplacement() );
		System.out.println( "Initial Y-Angle RMS: " + yRmsAngle );
		System.out.println( "Predicted Y-Angle RMS: " + predictedOrbit.getOrbitPlane( Orbit.Y_PLANE ).rmsAngle() );

		_eventProxy.progressUpdated( this, 1.0, "Corrector search completed" );

		return true;
	}


	/** Stop flattening the orbit.  */
	public void stopFlattening() {
		_simulator.stopPreparing();

		if ( _solver != null ) {
			_solver.stopSolving();
		}
	}


	/** Remove the solver.  */
	public void clearSolver() {
		_solver = null;
	}
	
	
	/** Apply the optimal corrections */
	public void applyCorrections( final double fractionFromInitial) {
		applyCorrectorDistribution( _optimalCorrectorDistribution, fractionFromInitial );
	}
	
	
	/** Revert to the initial corrector distribution */
	public void revertCorrections() {
		applyCorrectorDistribution( _initialCorrectorDistribution, 1.0 );
	}
	
	
	/** apply the optimal corrections  */
	public void applyCorrectorDistribution( final CorrectorDistribution distribution, final double fractionFromInitial ) {
		final List<CorrectorSupply> supplies = getCorrectorSuppliesToVary();
		final double fractionFromBest = 1.0 - fractionFromInitial;
		try {
			for ( CorrectorSupply correctorSupply : supplies ) {
				final double initialField = _initialCorrectorDistribution.getField( correctorSupply );
				final double targetField = distribution.getField( correctorSupply );
				final double field = targetField + fractionFromBest * ( initialField - targetField );
				correctorSupply.requestFieldSetting( field );
				System.out.println( "Set " + correctorSupply.getID() + " field to " + field );
			}
			Channel.flushIO();
		}
		catch ( Exception exception ) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log( Level.SEVERE, "Exception applying corrector strengths.", exception );
			exception.printStackTrace();
			throw new RuntimeException( exception.getMessage(), exception );
		}
	}


	/**
	 * Generate a solve stopper for the selected solving time.
	 * @return   Description of the Return Value
	 */
	private Stopper generateSolvingStopper() {
		return SolveStopperFactory.maxElapsedTimeStopper( _solvingTime );
	}


	/**
	 * Set the solving time.
	 * @param solvingTime  The new solvingTime value
	 */
	public void setSolvingTime( final double solvingTime ) {
		_solvingTime = solvingTime;
		if ( _solver != null ) {
			_solver.setStopper( generateSolvingStopper() );
		}

		_eventProxy.solvingTimeChanged( this, solvingTime );
	}


	/**
	 * Get the scheduled solving time in seconds.
	 * @return   The solvingTime value
	 */
	public double getSolvingTime() {
		return _solvingTime;
	}


	/**
	 * Get the time in seconds elapsed during the solve.
	 * @return   The elapsedTime value
	 */
	public double getElapsedTime() {
		return ( _solver != null ) ? _solver.getScoreBoard().getElapsedTime() : 0;
	}


	/**
	 * Get the fraction complete.
	 * @return   The fractionComplete value
	 */
	public double getFractionComplete() {
		return ( _solver == null ) ? _simulator.fractionPrepared() : getElapsedTime() / getSolvingTime();
	}
	
	
	/**
	 * Determine which corrector supplies should be varied and return them.
	 * @return the corrector supplies which should be varied
	 */
	protected List<CorrectorSupply> getCorrectorSuppliesToVary() {
		final List<CorrectorSupply> availableSupplies = new ArrayList<CorrectorSupply>();
		if ( _xDistortionObjective.isEnabled() ) {
			availableSupplies.addAll( _orbitModel.getHorizontalCorrectorSupplies() );
		}
		if ( _yDistortionObjective.isEnabled() ) {
			availableSupplies.addAll( _orbitModel.getVerticalCorrectorSupplies() );
		}
		
		final List<CorrectorSupply> supplies = new ArrayList<CorrectorSupply>();
		final List<CorrectorSupply> enabledSupplies = _simulator.getEnabledCorrectorSupplies();
		for ( CorrectorSupply supply : availableSupplies ) {
			if ( enabledSupplies.contains( supply ) ) {
				supplies.add( supply );
			}
		}
		
		return supplies;
	}


	/**
	 * Read the current corrector distribution.
	 * @return  the current corrector distribution
	 * @throws Exception  any exception encountered
	 */
	protected CorrectorDistribution presentCorrectorDistribution() throws Exception {		
		final Date timestamp = new Date();
		final MutableCorrectorDistribution distribution = new MutableCorrectorDistribution();
		
		final List<CorrectorSupply> supplies = getCorrectorSuppliesToVary();
		for ( CorrectorSupply supply : supplies ) {
			final double field = supply.getLatestField();
			distribution.addRecord( new CorrectorRecord( supply, timestamp, field ) );
		}

		return distribution.getDistribution();
	}
	
	
	/**
	 * Get the initial corrector distribution.
	 * @return the initial corrector distribution
	 */
	public CorrectorDistribution getInitialCorrectorDistribution() {
		return _initialCorrectorDistribution;
	}
	
	
	/**
	 * Get the proposed corrector distribution.
	 * @return the proposed corrector distribution
	 */
	public CorrectorDistribution getProposedCorrectorDistribution() {
		return _optimalCorrectorDistribution;
	}


	/**
	 * Indicates that a trial was scored
	 * @param scoreboard  Description of the Parameter
	 * @param trial       Description of the Parameter
	 */
	public void trialScored( final ScoreBoard scoreboard, final Trial trial ) { }


	/**
	 * Indicates that a trial was vetoed
	 * @param scoreboard  Description of the Parameter
	 * @param trial       Description of the Parameter
	 */
	public void trialVetoed( final ScoreBoard scoreboard, final Trial trial ) { }


	/**
	 * A new optimal solution has been found
	 * @param scoreboard  Description of the Parameter
	 * @param solution    Description of the Parameter
	 */
	public void newOptimalSolution( final ScoreBoard scoreboard, final Trial solution ) {
		final FlattenEvaluator evaluator = (FlattenEvaluator)_solver.getProblem().getEvaluator();
		final Orbit predictedOrbit = evaluator.getTrialOrbit();
		_eventProxy.newOptimalOrbit( this, predictedOrbit );
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
        for (final DataAdaptor simulatorAdaptor : adaptor.childAdaptors(MappedSimulator.DATA_LABEL)){
			final String type = simulatorAdaptor.stringValue( "type" );
			final MachineSimulator simulator = loadSimulator( type );
			if ( simulator != null ) {
				simulator.update( simulatorAdaptor );
			}
		}
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		final List<MachineSimulator> simulators = new ArrayList<MachineSimulator>( _loadedSimulators.values() );
		for ( MachineSimulator simulator : simulators ) {
			adaptor.writeNode( simulator );
		}
	}
	
	
	/**
	 * Notification that the sequence has changed.
	 * @param  model        the model sending the notification
	 * @param  newSequence  the new accelerator sequence
	 */
	public void sequenceChanged( final OrbitModel model, final AcceleratorSeq newSequence ) {
		_simulator.setSequence( model.getSequence(), model.getBPMAgents(), model.getCorrectorSupplies() );
	}
	
	/**
	 * Notification that the enabled BPMs have changed.
	 * @param  model      model sending this notification
	 * @param  bpmAgents  new enabled bpms
	 */
	public void enabledBPMsChanged( final OrbitModel model, final List<BpmAgent> bpmAgents ) {
		setBPMAgents( model.getBPMAgents() );
	}
	
	
	/**
	 * Notification that the orbit model has added a new orbit source.
	 * @param  model           the model sending the notification
	 * @param  newOrbitSource  the newly added orbit source
	 */
	public void orbitSourceAdded( final OrbitModel model, final OrbitSource newOrbitSource ) {}
	
	
	/**
	 * Notification that the orbit model has removed an orbit source.
	 * @param  model        the model sending the notification
	 * @param  orbitSource  the orbit source that was removed
	 */
	public void orbitSourceRemoved( final OrbitModel model, final OrbitSource orbitSource ) {}
	
	
	/** Handle CA put events */
	public void putCompleted( final Channel channel ) {
	}
}

