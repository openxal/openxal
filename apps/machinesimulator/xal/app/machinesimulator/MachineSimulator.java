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
import xal.sim.scenario.*;
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
	private Probe _entranceProbe;
	
	/** the scenario used in the online model run */
	private Scenario _scenario;
    
    /** indicates whether to use field readback rather than field setpoint when modeling the live machine */
    private boolean _useFieldReadback;
	
	/** indicator of whether the simulation is running */
	private volatile boolean _isRunning;

    
	/** Constructor */
    public MachineSimulator( final AcceleratorSeq sequence, final Probe entranceProbe ) {
		_isRunning = false;
        _useFieldReadback = false;  // by default use the field setting
        
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
    public void setSequenceProbe( final AcceleratorSeq sequence, final Probe entranceProbe ) throws ModelException {
        if ( !_isRunning ) {
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
            _scenario.setSynchronizationMode( Scenario.SYNC_MODE_DESIGN );
            _scenario.setProbe( _entranceProbe );
        }
        else {
            _scenario = null;
        }
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
    static private Probe copyProbe( final Probe entranceProbe ) {
        final Probe probe = Probe.newProbeInitializedFrom( entranceProbe );
        // todo: need to copy the probe's tracker if we can to avoid editing the shared probe tracker elsewhere
        probe.initialize();
        
        return probe;
    }
	
	
	/**
	 * Set the entrance probe.
	 * @param entranceProbe the new entrance probe
	 */
	public void setEntranceProbe( final Probe entranceProbe ) {
		if ( !_isRunning ) {
			_entranceProbe = copyProbe( entranceProbe );
			if ( _scenario != null ) {
				_scenario.setProbe( _entranceProbe );
			}
		}
		else {
			throw new RuntimeException( "Can't change probe while a simulation is in progress!" );
		}
	}
    
    
    /** Set whether to use field readback when modeling live machine */
    public void setUseFieldReadback( final boolean useFieldReadback ) {
        if ( _useFieldReadback != useFieldReadback ) {
            _useFieldReadback = useFieldReadback;
            applyFieldReadbackSource();
        }
    }
    
    
    /** Determine whether the field readback is used when modeling the live machine */
    public boolean getUseFieldReadback() {
        return _useFieldReadback;
    }
    
    
    /** apply the user's selection for the source of the getField method (readback or set point) */
    private void applyFieldReadbackSource() {
        final boolean useFieldReadback = _useFieldReadback;     // local copy of the field readback flag
        final AcceleratorSeq sequence = _sequence;   // local copy of the sequence
        if ( sequence != null ) {
            final List<AcceleratorNode> magnets = sequence.getAllNodesOfType( Electromagnet.s_strType );
            for ( final AcceleratorNode magnet : magnets ) {
                ((Electromagnet)magnet).setUseFieldReadback( useFieldReadback );
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
	public Probe getEntranceProbe() {
		return _entranceProbe;
	}
	
	
	/**
	 * Get the default probe for the specified sequence.
	 * @param sequence the sequence for which to get the default probe
	 * @return the default probe for the specified sequence
	 */
	static public Probe getDefaultProbe( final AcceleratorSeq sequence ) {
		try {
			final Probe probe = ( sequence instanceof Ring ) ? createRingProbe( sequence ) : createEnvelopeProbe( sequence );
			probe.getAlgorithm().setRfGapPhaseCalculation( true );	// make sure we enable the full RF gap phase slip calculation
			return probe;
		}
		catch( InstantiationException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception creating the default probe.", exception );
		}
	}


	/** create a new ring probe */
	static private Probe createRingProbe( final AcceleratorSeq sequence ) throws InstantiationException {
		final TransferMapTracker tracker = AlgorithmFactory.createTransferMapTracker( sequence );
		return ProbeFactory.getTransferMapProbe( sequence, tracker );
	}


	/** create a new envelope probe */
	static private Probe createEnvelopeProbe( final AcceleratorSeq sequence ) throws InstantiationException {
		final EnvelopeTracker tracker = AlgorithmFactory.createEnvelopeTracker( sequence );
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
	 * Run the simulation.
	 * @return the generated simulation or null if the run failed.
	 */
	public MachineSimulation run() {
		try {
			_isRunning = true;
			_entranceProbe.reset();            
			_scenario.resync();			
			_scenario.run();
			
			return new MachineSimulation( _entranceProbe );
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
    }
    
    
    /** Instructs the receiver to write its data to the adaptor for external storage. */
    public void write( final DataAdaptor adaptor ) {
    }
}
