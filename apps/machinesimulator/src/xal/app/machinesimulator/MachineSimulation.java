//
// MachineSimulator.java
// 
//
// Created by Tom Pelaia on 9/19/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machinesimulator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xal.model.probe.Probe;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.tools.beam.calc.SimpleSimResultsAdaptor;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;


/** MachineSimulation is the simulation result for a simulation on an accelerator sequence  */
public class MachineSimulation implements DataListener {
	 /** the data adaptor label used for reading and writing this document */
	 static public final String DATA_LABEL = "MachineSimulation"; 
    /** states for every element */
    final List<MachineSimulationRecord> SIMULATION_RECORDS;
    /**the trajectory*/
    private Trajectory<?> trajectory;
    
	/** Constructor */
    public MachineSimulation( final Probe<?> probe ) {
        trajectory = probe.getTrajectory();
		final SimpleSimResultsAdaptor resultsAdaptor = new SimpleSimResultsAdaptor( trajectory );
		
        SIMULATION_RECORDS = new ArrayList<MachineSimulationRecord>( trajectory.numStates() );
        final Iterator<? extends ProbeState<?>> stateIter = trajectory.stateIterator();
        while ( stateIter.hasNext() ) {
            final ProbeState<?> state = stateIter.next();
			final MachineSimulationRecord simulationRecord = new MachineSimulationRecord( resultsAdaptor, state );
            SIMULATION_RECORDS.add( simulationRecord );
        }
    }
    
    /**Constructor with adaptor*/
    public MachineSimulation ( final DataAdaptor adaptor ) {    	
        trajectory = Trajectory.loadFrom( adaptor );
		final SimpleSimResultsAdaptor resultsAdaptor = new SimpleSimResultsAdaptor( trajectory );
		
        SIMULATION_RECORDS = new ArrayList<MachineSimulationRecord>( trajectory.numStates() );
        final Iterator<? extends ProbeState<?>> stateIter = trajectory.stateIterator();
        while ( stateIter.hasNext() ) {
            final ProbeState<?> state = stateIter.next();
			final MachineSimulationRecord simulationRecord = new MachineSimulationRecord( resultsAdaptor, state );
            SIMULATION_RECORDS.add( simulationRecord );
        }
    }
    
    /** Get the simulation records */
    public List<MachineSimulationRecord> getSimulationRecords() {
        return SIMULATION_RECORDS;
    }
 
    /** get all the element's position in selected sequence ----xiaohan add*/
    public List<Double> getAllPositions(){
      List<Double> allPositions = new ArrayList<Double>(SIMULATION_RECORDS.size());
    	for(final MachineSimulationRecord record:SIMULATION_RECORDS){
    		allPositions.add(record.getPosition());
    	}
    	return allPositions;
    }
    
	/** provides the name used to identify the class in an external data source. */
	public String dataLabel() {
		return DATA_LABEL;
	}

	/** Instructs the receiver to update its data based on the given adaptor. */
	public void update(DataAdaptor adaptor) {
	}

	/** Instructs the receiver to write its data to the adaptor for external storage. */
	public void write( DataAdaptor adaptor ) {
		trajectory.save( adaptor );;
	}
}
