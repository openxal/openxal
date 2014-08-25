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


/** MachineSimulation is the simulation result for a simulation on an accelerator sequence  */
public class MachineSimulation {
    /** states for every element */
    final List<MachineSimulationRecord> SIMULATION_RECORDS;;
    
    
	/** Constructor */
    public MachineSimulation( final Probe<?> probe ) {
        final Trajectory<?> trajectory = probe.getTrajectory();
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
}
