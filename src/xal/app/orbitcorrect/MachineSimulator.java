/*
 *  MachineSimulator.java
 *
 *  Created on Tue Sep 07 13:45:36 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import java.util.ArrayList;
import java.util.List;

import xal.smf.AcceleratorSeq;
import xal.tools.data.DataListener;


/**
 * MachineSimulator
 * @author   tap
 * @since    Sep 07, 2004
 */
public abstract class MachineSimulator implements DataListener {
	/** store of modifications */
	protected ModificationStore _modificationStore;
	
	/** the accelerator sequence to flatten */
	protected AcceleratorSeq _sequence;
	
	/** Available BPMs */
	protected List<BpmAgent> _bpmAgents;
	
	/** Available horizontal and vertical correctors */
	protected List<CorrectorSupply> _correctorSupplies;
	
	/** list of corrector agents to simulate */
	protected List<CorrectorSupply> _correctorSuppliesToSimulate;
	
	/** indicates that this simulator should stop preparations */
	protected volatile boolean _shouldStopPreparing;
	
	/** fraction prepared */
	protected volatile double _fractionPrepared;
	
	
	/**
	 * Constructor
	 * @param modificationStore the store of modifications
	 * @param sequence    The sequence over which the orbit is measured.
	 * @param bpmAgents        The BPM agents used to measure the orbit.
	 * @param supplies  The corrector supplies used to correct the orbit.
	 */
	public MachineSimulator( final ModificationStore modificationStore, final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents, final List<CorrectorSupply> supplies ) {
		_modificationStore = modificationStore;
		_bpmAgents = new ArrayList<BpmAgent>();
		_correctorSupplies = new ArrayList<CorrectorSupply>();
		_correctorSuppliesToSimulate = new ArrayList<CorrectorSupply>();
		
		setup( sequence, bpmAgents, supplies );
		setSequence( sequence, bpmAgents, supplies );
	}
	
	
	/**
	 * Get the type of simulator by getting a unique string for this instance's class.
	 * @return the simulator type
	 */
	public String getSimulatorType() {
		return getClass().toString();
	}
	
	
	/**
	 * Initialize the simulator. This default implementation does nothing. Subclasses may wish to
	 * override this method to provide custom initialization.
	 * @param sequence    The sequence over which the orbit is measured.
	 * @param bpmAgents        The BPM agents used to measure the orbit.
	 * @param supplies  The corrector supplies used to correct the orbit.
	 */
	protected void setup( AcceleratorSeq sequence, List<BpmAgent> bpmAgents, List<CorrectorSupply> supplies ) {}
	
	
	/** Reset in preparation for the next simulation. */ 
	abstract public void reset();
	
	
	/** Clear cached data */
	abstract public void clear();
	
	
	/**
	 * Set the sequence, BPMs and correctors to the specified values.
	 * @param sequence    The sequence over which the orbit is measured.
	 * @param bpmAgents   The BPM agents used to measure the orbit.
	 * @param supplies  The corrector supplies used to correct the orbit.
	 */
	public void setSequence( AcceleratorSeq sequence, List<BpmAgent> bpmAgents, List<CorrectorSupply> supplies ) {
		_sequence = sequence;
		setBPMAgents( bpmAgents );
		setCorrectorSupplies( supplies );
	}
	
	
	/**
	 * Get the sequence.
	 * @return   This simulator's sequence.
	 */
	public AcceleratorSeq getSequence() {
		return _sequence;
	}
	
	
	/**
	 * Get the list of BPM agents.
	 * @return   This simulator's BPM agents.
	 */
	public List<BpmAgent> getBPMAgents() {
		return _bpmAgents;
	}
	
	
	/**
	 * Get the BPM agents that are enabled in this simulator.
	 * @return the BPM agents that are enabled in this simulator
	 */
	public List<BpmAgent> getEnabledBPMAgents() {
		final List<BpmAgent> enabledBpmAgents = new ArrayList<BpmAgent>();
		for ( final BpmAgent bpmAgent : _bpmAgents ) {
			if ( bpmAgent.getFlattenEnabled() )  enabledBpmAgents.add( bpmAgent );
		}
		return enabledBpmAgents;
	}
	
	
	/**
	 * Set the BPM agents to the specified values.
	 * @param bpmAgents  The new list of BPM agents to use.
	 */
	public void setBPMAgents( final List<BpmAgent> bpmAgents ) {
		_bpmAgents.clear();
		_bpmAgents.addAll( bpmAgents );
	}
	
	
	/**
	 * Event indicating that the bpm enable has changed.
	 * @param bpmAgent  the BPM agent whose enable status changed
	 */
	public void bpmFlattenEnableChanged( final BpmAgent bpmAgent ) {}
	
	
	/**
	 * Set the list of corrector agents to simulate.
	 */
	public void setCorrectorSuppliesToSimulate( final List<CorrectorSupply> supplies ) {
		_correctorSuppliesToSimulate = supplies;
	}
	
	
	/**
	 * Determine if the specified BPM agent is enabled in this simulator.
	 * @return true if the BPM agent is enabled and false if not
	 */
	public boolean isBPMEnabled( final BpmAgent bpmAgent ) {
		return bpmAgent.getFlattenEnabled();
	}
	
	
	/**
	 * Get the list of correctors used in the simulation.
	 * @return   This simulator's correctors.
	 */
	public List<CorrectorSupply> getCorrectorSupplies() {
		return _correctorSupplies;
	}
	
	
	/**
	 * Get the corrector supplies that are enabled in this simulator.
	 * @return the list of enabled corrector supplies
	 */
	public List<CorrectorSupply> getEnabledCorrectorSupplies() {
		final ArrayList<CorrectorSupply> enabledSupplies = new ArrayList<CorrectorSupply>( _correctorSupplies.size() );
		
		for ( CorrectorSupply supply : _correctorSupplies ) {
			if ( supply.isEnabled() )  enabledSupplies.add( supply );
		}
		
		return enabledSupplies;
	}
	
	
	/**
	 * Set the correctors to use in the simulation.
	 * @param supplies  The new list of corrector supplies to use in the simulation.
	 */
	public void setCorrectorSupplies( final List<CorrectorSupply> supplies ) {
		_correctorSupplies.clear();
		
		if ( supplies == null )  return;
		
		_correctorSupplies.addAll( supplies );		
	}
	
	
	/**
	 * Add the corrector supply to the list of supplies to use in the simulation.
	 * @param supply  The corrector supply to add to the simulation.
	 */
	public void setCorrectorSupplyEnable( final CorrectorSupply supply, final boolean enable ) {
		supply.setEnabled( enable );
	}
	
	
	/**
	 * Prepare for the simulation runs.
	 * 
	 * @return true if successful and false if not.
	 */
	abstract public boolean prepare();
	
	
	/**
	 * Stop preparation prematurely.
	 */
	public void stopPreparing() {
		_shouldStopPreparing = true;
	}
	
	
	/**
	 * Percent prepared for simulation.
	 * 
	 * @return the percent this simulator is prepared for running a simulation.
	 */
	public double fractionPrepared() {
		return _fractionPrepared;
	}
	
	
	/**
	 * Calculate and get the predicted orbit given the initial orbit, initial corrector strengths
	 * and the final corrector strengths.
	 *
	 * @param initialOrbit                  The initial orbit
	 * @param initialCorrectorDistribution  The initial corrector strengths
	 * @param finalCorrectorDistribution    The final corrector strengths
	 * @return                              The predicted Orbit
	 */
	public abstract Orbit predictOrbit( final Orbit initialOrbit, final CorrectorDistribution initialCorrectorDistribution, final CorrectorDistribution finalCorrectorDistribution );
}

