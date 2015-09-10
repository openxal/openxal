//
//  OpticsObjective.java
//  xal
//
//  Created by Thomas Pelaia on 6/7/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.tools.data.*;
import xal.tools.messaging.MessageCenter;
import xal.extension.solver.*;
import xal.extension.solver.algorithm.*;
import xal.model.probe.*;
import xal.model.probe.traj.*;
import xal.tools.beam.*;

import java.util.*;


/** base class of objectives */
abstract public class OpticsObjective extends Objective implements DataListener {
	/** data label */
	static final public String DATA_LABEL = "OpticsObjective";
	
	/** Message center for dispatching events to registered listeners. */
	private final MessageCenter _messageCenter;
	
	/** Proxy which forwards events to registered listeners. */
	protected final OpticsObjectiveListener _eventProxy;
	
	/** indicates whether this objective is enabled. */
	protected boolean _isEnabled;
	
	/** 90% satisfaction level */
	protected double _tolerance;
	
	
	/**
	 * Constructor
	 * @param name the name of the objective
	 */
	public OpticsObjective( final String name ) {
		super( name );
		
		_messageCenter = new MessageCenter( "Optics Objective" );
		_eventProxy = _messageCenter.registerSource( this, OpticsObjectiveListener.class );
		
		_isEnabled = true;
	}
	
	
	/**
	 * Add the specified listener to receive optics objective event notifications from this object.
	 * @param listener the listener to receive the event notifications.
	 */
	public void addOpticsObjectiveListener( final OpticsObjectiveListener listener ) {
		_messageCenter.registerTarget( listener, this, OpticsObjectiveListener.class );
		listener.objectiveEnableChanged( this, isEnabled() );
		listener.objectiveSettingsChanged( this );
	}
	
	
	/**
	 * Remove the specified listener from receiving optics objective event notifications from this object.
	 * @param listener the listener to remove from receiving event notifications.
	 */
	public void removeOpticsObjectiveListener( final OpticsObjectiveListener listener ) {
		_messageCenter.removeTarget( listener, this, OpticsObjectiveListener.class );
	}
	
	
	/**
	 * Get the label for display.
	 * @return the label to display.
	 */
	public String getLabel() {
		return getName();
	}
	
	
	/**
	 * Determine if this objective is enabled.
	 * @return true if this objective is enabled and false if not.
	 */
	public boolean isEnabled() {
		return _isEnabled;
	}
	
	
	/**
	 * Set whether this objective is enabled.
	 * @param enable if true then enable the objective otherwise disable it
	 */
	public void setEnable( final boolean enable ) {
		if ( enable != _isEnabled ) {
			_isEnabled = enable;
			_eventProxy.objectiveEnableChanged( this, enable );			
		}
	}
	
	
	/**
	 * Get the energy tolerance.
	 * @return the energy tolerance
	 */
	public double getTolerance() {
		return _tolerance;
	}
	
	
	/**
	 * Set the error tolerance.
	 * @param tolerance the new error tolerance.
	 */
	public void setTolerance( final double tolerance ) {
		if ( tolerance != _tolerance ) {
			_tolerance = tolerance;
			_eventProxy.objectiveSettingsChanged( this );			
		}
	}
	
	
	/**
	 * Get the display value for the specified scored value.
	 * @param value the scored value
	 * @return the display value based on the objective's scored value.
	 */
	public double getDisplayValue( final double value ) {
		return value;
	}
	
	
	/**
	 * Generate this objective's value given the specified simulation result and trial values.
	 * @param trialValues table of trial values keyed by variable
	 * @param simulation the simulation result
	 * @param designSimulation the design simulation
	 * @return this objective's value for the specified simulation
	 */
	abstract public double getValue( final Map<Variable, Number> trialValues, final Simulation simulation, final Simulation designSimulation );
	
	
	/**
	 * Determines how satisfied the user is with the specified value for this objective.  By default we
	 * simply return the value.  Individual objectives may override this to provide a better measure of satisfaction.
	 *
	 * @param value  The value associated with this objective for a particular trial
	 * @return       the user satisfaction for the specified value
	 */
	public double satisfaction( double value ) {
		return Double.isNaN( value ) ? 0.0 : value;
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
		setEnable( adaptor.booleanValue( "enabled" ) );
		setTolerance( adaptor.doubleValue( "tolerance" ) );
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "name", getName() );
		adaptor.setValue( "enabled", isEnabled() );
		adaptor.setValue( "tolerance", getTolerance() );
	}
}
