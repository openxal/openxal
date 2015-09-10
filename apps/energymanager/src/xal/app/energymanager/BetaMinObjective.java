//
//  BetaMinObjective.java
//  xal
//
//  Created by Thomas Pelaia on 6/13/05.
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


/** Objective to limit the minimum Beta function */
public class BetaMinObjective extends OpticsObjective {
	protected final int _axis;
	protected double _minBeta;
	
	
	/**
	 * Constructor
	 */
	public BetaMinObjective( final String name, final int axis, final double minBeta, final double tolerance ) {
		super( name );
		
		_axis = axis;
		setMinBeta( minBeta );
		setTolerance( tolerance );
	}
	
	
	/**
	 * Get the minimum beta desired.
	 * @return the minimum beta desired.
	 */
	public double getMinBeta() {
		return _minBeta;
	}
	
	
	/**
	 * Set the minimum beta desired.
	 * @param minBeta the minimum beta desired
	 */
	public void setMinBeta( final double minBeta ) {
		if ( minBeta != _minBeta ) {
			_minBeta = minBeta;
			_eventProxy.objectiveSettingsChanged( this );			
		}
	}
	
	
	/**
	 * Initialize the objective with the design simulation.
	 * @param simulation the design simulation
	 * @return the design target
	 */
	public double getDesignTarget( final Simulation simulation ) {
		return simulation.getBetaMin()[ _axis ];
	}
	
	
	/**
	 * Generate this objective's value given the specified simulation result and trial values.
	 * @param trialValues table of trial values keyed by variable
	 * @param simulation the simulation result
	 * @param designSimulation the design simulation
	 * @return this objective's value for the specified simulation
	 */
	public double getValue( final Map<Variable, Number> trialValues, final Simulation simulation, final Simulation designSimulation ) {
		return simulation.getBetaMin()[ _axis ];
	}
	
	
	/**
	 * Determines how satisfied the user is with the specified value for this objective.
	 * @param value  The value associated with this objective for a particular trial
	 * @return       the user satisfaction for the specified value
	 */
	public double satisfaction( double value ) {
		return Double.isNaN( value ) ? 0.0 : ( value > _minBeta ) ? 1.0 : SatisfactionCurve.inverseSquareSatisfaction( value - _minBeta, _tolerance );
	}	
	
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		super.update( adaptor );
		
		setMinBeta( adaptor.doubleValue( "minBeta" ) );
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		super.write( adaptor );
		
		adaptor.setValue( "minBeta", _minBeta );
	}
}
