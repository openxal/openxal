//
//  BetaMaxObjective.java
//  xal
//
//  Created by Thomas Pelaia on 6/10/05.
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


/** Objective to limit the maximum Beta function */
public class BetaMaxObjective extends OpticsObjective {
	protected final int _axis;
	protected double _maxBeta;
	
	
	/**
	 * Constructor
	 */
	public BetaMaxObjective( final String name, final int axis, final double maxBeta, final double tolerance ) {
		super( name );
		
		_axis = axis;
		setMaxBeta( maxBeta );
		setTolerance( tolerance );
	}
	
	
	/**
	 * Get the maximum beta desired.
	 * @return the maximum beta desired.
	 */
	public double getMaxBeta() {
		return _maxBeta;
	}
	
	
	/**
	 * Set the maximum beta desired.
	 * @param maxBeta the maximum beta desired
	 */
	public void setMaxBeta( final double maxBeta ) {
		if ( maxBeta != _maxBeta ) {
			_maxBeta = maxBeta;
			_eventProxy.objectiveSettingsChanged( this );			
		}
	}
	
	
	/**
	 * Initialize the objective with the design simulation.
	 * @param simulation the design simulation
	 * @return the design target
	 */
	public double getDesignTarget( final Simulation simulation ) {
		return simulation.getBetaMax()[ _axis ];
	}
	
	
	/**
	 * Generate this objective's value given the specified simulation result and trial values.
	 * @param trialValues table of trial values keyed by variable
	 * @param simulation the simulation result
	 * @param designSimulation the design simulation
	 * @return this objective's value for the specified simulation
	 */
	public double getValue( final Map<Variable, Number> trialValues, final Simulation simulation, final Simulation designSimulation ) {
		final List<Double> worstBetas = new ArrayList<Double>();
		final double[] betas = simulation.getBeta()[ _axis ];
		for ( double beta : betas ) {
			if ( Double.isNaN( beta ) ) {
				return Double.NaN;
			}
			else if ( beta >= _maxBeta ) {
				worstBetas.add( beta );
			}
		}
		if ( worstBetas.isEmpty() ) {
			return simulation.getBetaMax()[ _axis ];
		}
		else {
			Collections.sort( worstBetas );
			Collections.reverse( worstBetas );
			double value = 0.0;
			double weight = 1.0;
			for ( Double beta : worstBetas ) {
				value += weight * beta.doubleValue();
				weight *= 0.25;
			}
			return value;
		}
	}
	
	
	/**
	 * Determines how satisfied the user is with the specified value for this objective.
	 * @param value  The value associated with this objective for a particular trial
	 * @return       the user satisfaction for the specified value
	 */
	public double satisfaction( double value ) {
		return Double.isNaN( value ) ? 0.0 : ( value < _maxBeta ) ? 1.0 : SatisfactionCurve.inverseSquareSatisfaction( value - _maxBeta, _tolerance );
	}	
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		super.update( adaptor );
		
		setMaxBeta( adaptor.doubleValue( "maxBeta" ) );
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		super.write( adaptor );
		
		adaptor.setValue( "maxBeta", _maxBeta );
	}
}
