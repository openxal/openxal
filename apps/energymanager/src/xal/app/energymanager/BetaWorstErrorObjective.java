//
//  BetaWorstErrorObjective.java
//  xal
//
//  Created by Thomas Pelaia on 6/15/05.
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


/** Objective to minimize the worst Beta error. */
public class BetaWorstErrorObjective extends OpticsObjective {
	/** indicates the x, y or z axis */
	protected final int _axis;
	
	
	/**
	 * Constructor
	 */
	public BetaWorstErrorObjective( final String name, final int axis, final double tolerance ) {
		super( name );
		
		_axis = axis;
		setTolerance( tolerance );
	}
	
	
	/**
	 * Get the label for display.
	 * @return the label to display.
	 */
	public String getLabel() {
		return getName() + " (%)";
	}
	
	
	/**
	 * Get the display value for the specified scored value.  Display the value as a percent.
	 * @param value the scored value
	 * @return the display value based on the objective's scored value as a percent
	 */
	public double getDisplayValue( final double value ) {
		return 100 * value;
	}
	
	
	/**
	 * Generate this objective's value given the specified simulation result and trial values.
	 * @param trialValues table of trial values keyed by variable
	 * @param simulation the simulation result
	 * @param designSimulation the design simulation
	 * @return this objective's value for the specified simulation
	 */
	public double getValue( final Map<Variable, Number> trialValues, final Simulation simulation, final Simulation designSimulation ) {
		return simulation.getWorstBetaError( designSimulation )[ _axis ];
	}
	
	
	/**
	 * Determines how satisfied the user is with the specified value for this objective.
	 * @param value  The value associated with this objective for a particular trial
	 * @return       the user satisfaction for the specified value
	 */
	public double satisfaction( double value ) {
		return Double.isNaN( value ) ? 0.0 : SatisfactionCurve.inverseSquareSatisfaction( value, _tolerance );
	}	
}
