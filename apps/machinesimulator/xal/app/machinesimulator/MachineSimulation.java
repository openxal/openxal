//
// MachineSimulator.java
// 
//
// Created by Tom Pelaia on 9/19/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machinesimulator;

import xal.smf.*;
import xal.model.probe.*;
import xal.model.probe.traj.*;

import java.util.*;


/** MachineSimulation is the simulation result for a simulation on an accelerator sequence  */
public class MachineSimulation {
    /** states for every element */
    final List<IPhaseState> ELEMENT_STATES;
    
    
	/** Constructor */
    public MachineSimulation( final Probe probe ) {
        final Trajectory trajectory = probe.getTrajectory();
        
        ELEMENT_STATES = new ArrayList<IPhaseState>( trajectory.numStates() );
        
        final Iterator<ProbeState> stateIter = trajectory.stateIterator();
        while ( stateIter.hasNext() ) {
            final IPhaseState state = (IPhaseState)stateIter.next();
            ELEMENT_STATES.add( state );
//            System.out.println( state.getElementId() );
//            System.out.println( "   position: " + state.getPosition() );
//            System.out.println( "" );
        }
    }
    
    
    /** Get the states */
    public List<IPhaseState> getStates() {
        return ELEMENT_STATES;
    }
}
