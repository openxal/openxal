//
// MachineModel.java
// 
//
// Created by Tom Pelaia on 9/19/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machsim;

import xal.tools.data.*;
import xal.model.*;
import xal.model.alg.*;
import xal.model.probe.*;
import xal.sim.scenario.*;
import xal.smf.*;
import xal.smf.impl.Electromagnet;

import java.util.*;
import java.util.logging.*;


/** MachineModel is the main model for the machine */
public class MachineModel implements DataListener {
 	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "MachineModel";
    
    /** simulator */
    final private MachineSimulator SIMULATOR;
    
    /** accelerator sequence on which to run the simulations */
    private AcceleratorSeq _sequence;
    
    /** latest simulation */
    private MachineSimulation _simulation;

    
	/** Constructor */
	public MachineModel() {        
        SIMULATOR = new MachineSimulator( null );
	}
    
    
    /** set the accelerator sequence on which to run the simulations */
    public void setSequence( final AcceleratorSeq sequence ) throws ModelException {
        SIMULATOR.setSequence( sequence );
        _sequence = sequence;
    }
    
    
    /** get the accelerator sequence */
    public AcceleratorSeq getSequence() {
        return _sequence;
    }
    
    
    /** Get the most recent simulation */
    public MachineSimulation getSimulation() {
        return _simulation;
    }
    
	
	/** Run the simulation. */
	public MachineSimulation runSimulation() {
        _simulation = SIMULATOR.run();
        return _simulation;
	}

    
    /** provides the name used to identify the class in an external data source. */
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /** Instructs the receiver to update its data based on the given adaptor. */
    public void update( final DataAdaptor adaptor ) {
        final DataAdaptor simulatorAdaptor = adaptor.childAdaptor( MachineSimulator.DATA_LABEL );
        if ( simulatorAdaptor != null )  SIMULATOR.update( simulatorAdaptor );
    }
    
    
    /** Instructs the receiver to write its data to the adaptor for external storage. */
    public void write( final DataAdaptor adaptor ) {
        adaptor.writeNode( SIMULATOR );
    }
}
