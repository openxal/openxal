//
//  EnergyObjective.java
//  xal
//
//  Created by Thomas Pelaia on 6/7/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.tools.data.*;
import xal.extension.solver.*;
import xal.extension.solver.algorithm.*;
import xal.model.probe.*;
import xal.model.probe.traj.*;
import xal.tools.beam.*;

import java.util.*;


/** Objective for achieving the target energy */
public class EnergyObjective extends OpticsObjective {
	protected double _targetEnergy;
	
	
	/**
	 * Constructor
	 */
	public EnergyObjective( final double targetEnergy, final double tolerance ) {
		super( "Output Energy" );
		
		_targetEnergy = targetEnergy;
		setTolerance( tolerance );
	}
	
	
	/**
	 * Get the target energy.
	 * @return the target energy.
	 */
	public double getTargetEnergy() {
		return _targetEnergy;
	}
	
	
	/**
	 * Set the target energy.
	 * @param target the target energy
	 */
	public void setTargetEnergy( final double target ) {
		if ( target != _targetEnergy ) {
			_targetEnergy = target;
			_eventProxy.objectiveSettingsChanged( this );			
		}
	}
	
	
	/**
	 * Generate this objective's value given the specified simulation result and trial values.
	 * @param trialValues table of trial values keyed by variable
	 * @param simulation the simulation result
	 * @param designSimulation the design simulation
	 * @return this objective's value for the specified simulation
	 */
	public double getValue( final Map<Variable, Number> trialValues, final Simulation simulation, final Simulation designSimulation ) {
		return simulation.getOutputKineticEnergy();
	}
	
	
	
	/**
	 * Determines how satisfied the user is with the specified value for this objective.
	 *
	 * @param value  The value associated with this objective for a particular trial
	 * @return       the user satisfaction for the specified value
	 */
	public double satisfaction( double value ) {
		return Double.isNaN( value ) ? 0.0 : SatisfactionCurve.inverseSquareSatisfaction( value - _targetEnergy, _tolerance );
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		super.update( adaptor );
		
		setTargetEnergy( adaptor.doubleValue( "targetEnergy" ) );
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		super.write( adaptor );
		
		adaptor.setValue( "targetEnergy", _targetEnergy );
	}
}
