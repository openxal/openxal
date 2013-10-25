//
//  EtaMaxObjective.java
//  xal
//
//  Created by Tom Pelaia on 1/3/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.tools.data.*;
import xal.extension.solver.*;
import xal.extension.solver.algorithm.*;
import xal.model.probe.*;
import xal.model.probe.traj.*;
import xal.tools.beam.*;

import java.util.*;


/** Objective to constrain the maximum chromatic dispersion */
public class EtaMaxObjective extends OpticsObjective {
	protected final int _axis;
	protected double _maxEta;
	
	
	/** Constructor */
	public EtaMaxObjective( final String name, final int axis, final double maxEta, final double tolerance ) {
		super( name );
		
		_axis = axis;
		setMaxEta( maxEta );
		setTolerance( tolerance );
	}
	
	
	/**
	 * Get the maximum dispersion desired.
	 * @return the maximum dispersion desired.
	 */
	public double getMaxEta() {
		return _maxEta;
	}
	
	
	/**
	 * Set the maximum dispersion desired.
	 * @param maxEta the maximum dispersion desired
	 */
	public void setMaxEta( final double maxEta ) {
		if ( maxEta != _maxEta ) {
			_maxEta = maxEta;
			_eventProxy.objectiveSettingsChanged( this );			
		}
	}
	
	
	/**
	 * Initialize the objective with the design simulation.
	 * @param simulation the design simulation
	 * @return the design target
	 */
	public double getDesignTarget( final Simulation simulation ) {
		return simulation.getEtaMax()[ _axis ];
	}
	
	
	/**
	 * Generate this objective's value given the specified simulation result and trial values.
	 * @param trialValues table of trial values keyed by variable
	 * @param simulation the simulation result
	 * @param designSimulation the design simulation
	 * @return this objective's value for the specified simulation
	 */
	public double getValue( final Map<Variable, Number> trialValues, final Simulation simulation, final Simulation designSimulation ) {
		final List<Double> worstEtas = new ArrayList<Double>();
		final double[] etas = simulation.getEta()[ _axis ];
		for ( double eta : etas ) {
			if ( Double.isNaN( eta ) ) {
				return Double.NaN;
			}
			else if ( eta > _maxEta ) {
				worstEtas.add( eta );
			}
		}
		if ( worstEtas.isEmpty() ) {
			return simulation.getEtaMax()[ _axis ];
		}
		else {
			Collections.sort( worstEtas );
			Collections.reverse( worstEtas );
			double value = 0.0;
			double weight = 1.0;
			for ( Double eta : worstEtas ) {
				value += weight * eta.doubleValue();
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
		return Double.isNaN( value ) ? 0.0 : ( value <= _maxEta ) ? 1.0 : SatisfactionCurve.inverseSquareSatisfaction( value - _maxEta, _tolerance );
	}	
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		super.update( adaptor );
		
		setMaxEta( adaptor.doubleValue( "maxEta" ) );
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		super.write( adaptor );
		
		adaptor.setValue( "maxEta", _maxEta );
	}	
}
