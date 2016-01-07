//
// MachineSimulator.java
// 
//
// Created by Tom Pelaia on 9/19/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machinesimulator;

import xal.tools.data.*;
import xal.model.*;
import xal.model.alg.*;
import xal.model.probe.*;
import xal.model.probe.traj.ProbeState;
import xal.sim.scenario.*;
import xal.sim.sync.SynchronizationException;
import xal.smf.*;
import xal.smf.impl.Electromagnet;

import java.util.*;
import java.util.logging.*;


/** MachineSimulator performs and records simulations */
public class MachineSimulator implements DataListener {
 	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "MachineSimulator";
    
    /** accelerator sequence */
    private AcceleratorSeq _sequence;
	
	/** the probe used in the online model run */
	private Probe<?> _entranceProbe;
	
	/** the scenario used in the online model run */
	private Scenario _scenario;
    
    /** indicates whether to use field readback rather than field setpoint when modeling the live machine */
    private boolean _useFieldReadback;

	/** perform full RF Gap phase slip calculation */
	private boolean _useRFGapPhaseSlipCalculation;
	
	/** indicator of whether the simulation is running */
	private volatile boolean _isRunning;
	
	/**the list of ModelInput*/
	private List<ModelInput> modelInputs;
	/**the actual property values used for simulation*/
	private Map<AcceleratorNode, Map<String, Double>> propertyValuesRecordForNodes;
	/**the number of running times*/
	private int runNumber=0;

    
	/** Constructor */
    public MachineSimulator( final AcceleratorSeq sequence, final Probe<?> entranceProbe ) {
		_isRunning = false;
        _useFieldReadback = false;  // by default use the field setting
		_useRFGapPhaseSlipCalculation = true;	// by default perform the full RF Gap phase slip calculation
		
		modelInputs = new ArrayList<ModelInput>();
        
		try {
            setSequenceProbe( sequence, entranceProbe );
		}
		catch( ModelException exception ) {
			exception.printStackTrace();
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log( Level.SEVERE, "Exception initializing the online model simulator.", exception );
		}
    }
	
	
	/**
	 * Constructor
	 * @param sequence    The sequence over which the simulation is made.
	 */
	public MachineSimulator( final AcceleratorSeq sequence ) {
		this( sequence, null );
	}
    
    
    /** Set the sequence */
    public void setSequenceProbe( final AcceleratorSeq sequence, final Probe<?> entranceProbe ) throws ModelException {
        if ( !_isRunning ) {
			// make sure we clear the entrance probe if the sequence changes
			if ( sequence != _sequence )  _entranceProbe = null;

            _sequence = sequence;
            
            _entranceProbe = entranceProbe != null ? copyProbe( entranceProbe ) : sequence != null ? getDefaultProbe( sequence ) : null;
            
            applyFieldReadbackSource();
            configScenario();
        }
        else {
			throw new RuntimeException( "Can't change sequence while a simulation is in progress!" );
        }
    }
    
    
    /** Create a new scenario */
    private void configScenario() throws ModelException {    	
        if ( _sequence != null ) {
            _scenario = Scenario.newScenarioFor( _sequence );
        }
        else {
            _scenario = null;
        }
    }
    
    /**Get the scenario*/
    public Scenario getScenario() {
    	return _scenario;
    }
    
	/** Set the synchronization mode */    
    public void setSynchronizationMode(final String newMode){   	
    	if(_scenario != null) _scenario.setSynchronizationMode(newMode);
    }
    
    /** Set the accelerator sequence */
    public void setSequence( final AcceleratorSeq sequence ) throws ModelException {
        setSequenceProbe( sequence, null );
    }
    
    
    /** 
     * Construct a new probe from the given probe
     * @param entranceProbe probe to copy
     * @return new probe constructed from the given probe
     */
    static private Probe<? extends ProbeState<?>> copyProbe( final Probe<?> entranceProbe ) {
        final Probe<? extends ProbeState<?>> probe = entranceProbe.copy();		// performs a deep copy of the probe including algorithm
        probe.initialize();
        return probe;
    }
	
	
	/**
	 * Set the entrance probe.
	 * @param entranceProbe the new entrance probe
	 */
	public void setEntranceProbe( final Probe<?> entranceProbe ) {
		if ( !_isRunning ) {
			_entranceProbe = copyProbe( entranceProbe );
		}
		else {
			throw new RuntimeException( "Can't change probe while a simulation is in progress!" );
		}
	}


	/** set whether to use the full RF Gap phase slip calculation */
	public void setUseRFGapPhaseSlipCalculation( final boolean usePhaseSlipCalc ) {
		_useRFGapPhaseSlipCalculation = usePhaseSlipCalc;
		final Probe<?> probe = _entranceProbe;
		if ( probe != null ) {
			probe.getAlgorithm().setRfGapPhaseCalculation( _useRFGapPhaseSlipCalculation );
		}
	}


	/** determine whether the full RF Gap phase slip calculation will be used */
	public boolean getUseRFGapPhaseSlipCalculation() {
		return _useRFGapPhaseSlipCalculation;
	}
	
    
    /** Set whether to use field read_back when modeling live machine */
    public void setUseFieldReadback( final boolean useFieldReadback ) {
        if ( _useFieldReadback != useFieldReadback ) {
            _useFieldReadback = useFieldReadback;
            applyFieldReadbackSource();
        }
    }
    
    
    /** Determine whether the field read_back is used when modeling the live machine */
    public boolean getUseFieldReadback() {
        return _useFieldReadback;
    }
    
    
    /** apply the user's selection for the source of the getField method (readback or set point) */
    private void applyFieldReadbackSource() {
        final boolean useFieldReadback = _useFieldReadback;     // local copy of the field readback flag
        final AcceleratorSeq sequence = _sequence;   // local copy of the sequence
        if ( sequence != null ) {
            final List<Electromagnet> magnets = sequence.getAllNodesOfType( Electromagnet.s_strType );
            for ( final Electromagnet magnet : magnets ) {
                magnet.setUseFieldReadback( useFieldReadback );
            }
        }
    }

	
	/**
	 * Apply the tracker update policy to the current probe (does not stick for future probes)
	 * @param policy one of the Tracker update policies: UPDATE_ALWAYS, UPDATE_ENTRANCE, UPDATE_EXIT, UPDATE_ENTRANCEANDEXIT
	 */
	public void applyTrackerUpdatePolicy( final int policy ) {
		if ( _entranceProbe != null ) {
			final Tracker tracker = (Tracker)_entranceProbe.getAlgorithm();
			tracker.setProbeUpdatePolicy( policy );
		}
	}
	
	
	/**
	 * Get the entrance probe.
	 * @return the probe used in the simulation
	 */
	public Probe<?> getEntranceProbe() {
		return _entranceProbe;
	}
	
	
	/**
	 * Get the default probe for the specified sequence.
	 * @param sequence the sequence for which to get the default probe
	 * @return the default probe for the specified sequence
	 */
	public Probe<?> getDefaultProbe( final AcceleratorSeq sequence ) {
		try {
			final Probe<?> probe = ( sequence instanceof Ring ) ? createRingProbe( sequence ) : createEnvelopeProbe( sequence );
			probe.getAlgorithm().setRfGapPhaseCalculation( _useRFGapPhaseSlipCalculation );
			return probe;
		}
		catch( InstantiationException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception creating the default probe.", exception );
		}
	}


	/** create a new ring probe */
	private TransferMapProbe createRingProbe( final AcceleratorSeq sequence ) throws InstantiationException {
		final TransferMapTracker tracker = AlgorithmFactory.createTransferMapTracker( sequence );
		return ProbeFactory.getTransferMapProbe( sequence, tracker );
	}


	/** create a new envelope probe */
	private EnvelopeProbe createEnvelopeProbe( final AcceleratorSeq sequence ) throws InstantiationException {
		final IAlgorithm tracker = AlgorithmFactory.createEnvTrackerAdapt( sequence );
		return ProbeFactory.getEnvelopeProbe( sequence, tracker );
	}
	
	
	/**
	 * Get the default kinetic energy of the incoming particle.
	 * @param sequence the sequence for which to get the particle's design kinetic energy at the entrance
	 * @return the default kinetic energy of a particle at the entrance to the sequence
	 */
	public double getDefaultEntranceKineticEnergy( final AcceleratorSeq sequence ) {
		return getDefaultProbe( sequence ).getKineticEnergy();
	}
	
	/**
	 * configure the modelInputs with the list of NodePropertyRecord which holds the ModelInput instance
	 * @param nodePropertyRecords The list of NodePropertyRecord
	 * @throws SynchronizationException 
	 */
    public void configModelInputs( final List<NodePropertyRecord> nodePropertyRecords ){
    	List<ModelInput> newModelInputs = new ArrayList<ModelInput>();
    	Map<AcceleratorNode, Map<String, Double>> propertyValueForNode = new HashMap<AcceleratorNode, Map<String,Double>>();
    	for( NodePropertyRecord record:nodePropertyRecords ){
    		if( !Double.isNaN( record.getTestValue() ) ){
    			newModelInputs.add( record.getModelInput() );
    		}
    	}
	
    	changeModelInputs( modelInputs, newModelInputs );	

    	for( NodePropertyRecord record : nodePropertyRecords){
    		try {
				propertyValueForNode.put( record.getAcceleratorNode(), _scenario.propertiesForNode( record.getAcceleratorNode() ) );
			} catch (SynchronizationException e) {
				e.printStackTrace();
			}
    	}
    	// record the values used for simulation
    	propertyValuesRecordForNodes = propertyValueForNode;

    }
    
    /**
     * change the modelInput
     * @param oldInputs The old modelInputs which we set last time
     * @param newInputs The new modelInputs which we are going to set
     */
    private void changeModelInputs( final List<ModelInput> oldInputs, final List<ModelInput> newInputs ){
    	for( final ModelInput oldInput : oldInputs ){
    		_scenario.removeModelInput( oldInput.getAcceleratorNode(), oldInput.getProperty() );
    	}
    	
    	for( final ModelInput newInput : newInputs ){
    		_scenario.setModelInput( newInput.getAcceleratorNode(), newInput.getProperty(), newInput.getDoubleValue() );    		
    	}
    	
    	modelInputs = newInputs;
    }
    
    /**Return the values record used for simulation*/
    public Map<AcceleratorNode, Map<String, Double>> getPropertyValuesRecord(){
    	return propertyValuesRecordForNodes;
    }
    
    /**Return the number of running time*/
    public int getRunNumber(){
    	return runNumber;
    }
    
	/**
	 * Run the simulation.
	 * @return the generated simulation or null if the run failed.
	 */
	public MachineSimulation run() {
		try {
			_isRunning = true;
			final Probe<?> probe = copyProbe( _entranceProbe );	// perform a deep copy of the entrance probe leaving the entrance probe unmodified
            _scenario.setProbe( probe );
			_scenario.resync();
			_scenario.run();
			
			runNumber++;
			
			return new MachineSimulation( probe );
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

    
    /** provides the name used to identify the class in an external data source. */
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /** Instructs the receiver to update its data based on the given adaptor. */
    public void update( final DataAdaptor adaptor ) {
    	boolean useFieldRb = adaptor.hasAttribute( "useFieldReadBack" ) ? adaptor.booleanValue( "useFieldReadBack" ) : false;
    	setUseFieldReadback( useFieldRb );
    	if ( adaptor.hasAttribute( "runNum" ) ) runNumber = adaptor.intValue( "runNum" );
    	if ( _scenario != null && adaptor.hasAttribute( "synchMode" ) ){
    		_scenario.setSynchronizationMode( adaptor.stringValue( "synchMode" ) );
    	}
    	
    	_entranceProbe = Probe.readFrom( adaptor );

    }
    
    
    /** Instructs the receiver to write its data to the adaptor for external storage. */
    public void write( final DataAdaptor adaptor ) {    	
    	adaptor.setValue( "useFieldReadBack", _useFieldReadback );
    	adaptor.setValue( "runNum", runNumber );
    	adaptor.setValue( "synchMode", _scenario.getSynchronizationMode() );
    	_entranceProbe.save( adaptor );
    }
}
