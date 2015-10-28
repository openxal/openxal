//
// MachineModel.java
// 
//
// Created by Tom Pelaia on 9/19/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machinesimulator;

import javax.swing.JOptionPane;

import xal.model.ModelException;
import xal.smf.AcceleratorSeq;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;


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
    
	/** Set the synchronization mode */   
    public void setSynchronizationMode(final String newMode){
    	SIMULATOR.setSynchronizationMode(newMode);
    }
    
    /** Set whether to use field readback when modeling live machine */   
    public void setUseFieldReadback( final boolean useFieldReadback){
    	SIMULATOR.setUseFieldReadback(useFieldReadback);
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
		if(_sequence != null) _simulation = SIMULATOR.run();
		else JOptionPane.showMessageDialog(null, "You need to select sequence(s) first","Warning!",JOptionPane.PLAIN_MESSAGE);
		
		return _simulation;
	}


	/** Get the simulator */
	public MachineSimulator getSimulator() {
		return SIMULATOR;
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
