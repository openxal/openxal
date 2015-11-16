//
// MachineModel.java
// 
//
// Created by Tom Pelaia on 9/19/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machinesimulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.text.DateFormat;
import java.util.List;
import java.util.Map;

import xal.model.ModelException;
import xal.smf.AcceleratorSeq;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.tools.messaging.MessageCenter;


/** MachineModel is the main model for the machine */
public class MachineModel implements DataListener {
 	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "MachineModel";
	/** message center used to post events from this instance */
	final private MessageCenter MESSAGE_CENTER;
	/**the proxy to post events */
	final private MachineModelListener EVENT_PROXY;
	/**the simulation history records*/
	final private LinkedList<SimulationHistoryRecord> SIMULATION_HISTORY_RECORDS;
   /** simulator */
   final private MachineSimulator SIMULATOR;
   /** accelerator sequence on which to run the simulations */
   private AcceleratorSeq _sequence;
   /**all the history datum*/
   final private Map<AcceleratorSeq, List<NodePropertyHistoryRecord>> ALL_HISTORY_DATUM;
   /** latest simulation */
   private MachineSimulation _simulation;
   /** whatIfconfiguration*/
   private WhatIfConfiguration _whatIfConfiguration;
   /**the list of NodePropertyRecord*/
   private List<NodePropertyRecord> nodePropertyRecords;
   

    
	/** Constructor */
	public MachineModel() {
		MESSAGE_CENTER = new MessageCenter( DATA_LABEL );
		
		SIMULATOR = new MachineSimulator( null );
		
		SIMULATION_HISTORY_RECORDS = new LinkedList<SimulationHistoryRecord>();
		
		ALL_HISTORY_DATUM = new HashMap<AcceleratorSeq, List<NodePropertyHistoryRecord>>();
		
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, MachineModelListener.class );

	}
    
    
    /** set the accelerator sequence on which to run the simulations */
    public void setSequence( final AcceleratorSeq sequence ) throws ModelException {
        SIMULATOR.setSequence( sequence );
        _sequence = sequence;
        setupWhatIfConfiguration( _sequence );
        EVENT_PROXY.modelSequenceChanged(this);
    }
    
	/** Set the synchronization mode */   
    public void setSynchronizationMode( final String newMode ){
    	SIMULATOR.setSynchronizationMode( newMode );
    }
    
    /** Set whether to use field readback when modeling live machine */   
    public void setUseFieldReadback(  final boolean useFieldReadback ){
    	SIMULATOR.setUseFieldReadback( useFieldReadback );
    }
    
    /**post the event that the scenario has changed*/
    public void modelScenarioChanged(){
    	setupWhatIfConfiguration( _sequence );
    	EVENT_PROXY.modelScenarioChanged(this);
    }

    /** get the accelerator sequence */
    public AcceleratorSeq getSequence() {
        return _sequence;
    }
    /**get the whatIfConfiguration */
    public WhatIfConfiguration getWhatIfConfiguration(){
    	return _whatIfConfiguration;
    }
    /** setup WhatIfConfiguration*/
    private void setupWhatIfConfiguration( final AcceleratorSeq sequence ){
    	if( sequence != null ) _whatIfConfiguration = new WhatIfConfiguration( sequence );
    }
    
    /**configure the ModelInputs from a list of NodePropertyRecord*/
    private void configModelInputs( final List<NodePropertyRecord> nodePropertyRecords ){
    	SIMULATOR.configModelInputs( nodePropertyRecords );
    }
    
    /** Get the most recent simulation */
    public MachineSimulation getSimulation() {
        return _simulation;
    }
    
    /**get the simulation history record*/
    public List<SimulationHistoryRecord> getSimulationHistoryRecords(){
    	return SIMULATION_HISTORY_RECORDS;
    }
    
	
	/** Run the simulation and record the result and the values used for simulation */
	public MachineSimulation runSimulation() {
		
		nodePropertyRecords = this.getWhatIfConfiguration().getNodePropertyRecords();
		configModelInputs( nodePropertyRecords );
		
		_simulation = SIMULATOR.run();
		
		if(_simulation != null ) {
			Date time = new Date();
			SIMULATION_HISTORY_RECORDS.addFirst( new SimulationHistoryRecord( time ) );
			
			if( ALL_HISTORY_DATUM.get( _sequence ) == null ){				
				ALL_HISTORY_DATUM.put( _sequence, createHistoryRecordForNodePropertyValues( nodePropertyRecords ) );
			}

			for( final NodePropertyHistoryRecord historyRecord: ALL_HISTORY_DATUM.get( _sequence ) ){
				historyRecord.addValue( time, SIMULATOR.getPropertyValuesRecord().get( historyRecord.getAcceleratorNode() ).get( historyRecord.getPropertyName() ) );
			}

			
		}
		return _simulation;
	}
	
	/**get the history record of property values for the specified sequence*/
	public List<NodePropertyHistoryRecord> getNodePropertyHistoryRecords(){
		return ALL_HISTORY_DATUM.get( _sequence );
	}
	
	/**create a history record for the sequence*/
	private List<NodePropertyHistoryRecord> createHistoryRecordForNodePropertyValues( final List<NodePropertyRecord> nodePropertyRecords ){
		List<NodePropertyHistoryRecord> nodePropertyHistoryRecords = new ArrayList<NodePropertyHistoryRecord>();
		for( final NodePropertyRecord record:nodePropertyRecords){
			nodePropertyHistoryRecords.add( new NodePropertyHistoryRecord( record.getAcceleratorNode(), record.getPropertyName() ) );
		}
		return nodePropertyHistoryRecords;
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
    
	/**
	 * Add a listener of MachineModel events from this instance
	 * @param listener The listener to add
	 */
	public void addMachineModelListener( final MachineModelListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, MachineModelListener.class );
	}
	
	
	/**
	 * Remove a listener of MachineModel events from this instance
	 * @param listener The listener to remove
	 */
	public void removeMachineModelListener( final MachineModelListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, MachineModelListener.class );
	}

/**to record the simulation history*/
 class SimulationHistoryRecord{
	 
	 /**The time when run simulation*/
	final private Date TIME;
	/**time format*/
	final private DateFormat DATE_FORMAT;
	/**sequence id*/
	final private AcceleratorSeq SEQUENCE;
	/**record name*/
	private String recordName;
	/**select state*/
	private Boolean selectState;

	/**Constructor*/
	public SimulationHistoryRecord( final Date time ){
		TIME = time;
		SEQUENCE = _sequence;
		DATE_FORMAT = DateFormat.getDateTimeInstance();
		recordName = getNodeId()+getDateTime();
		selectState = true;
	}
	
	/**get the select state*/
	public Boolean getSelectState(){
		return selectState;
	}
	
	/**set the select state*/
	public void setSelectState( final Boolean select ){
		for( final NodePropertyHistoryRecord record:ALL_HISTORY_DATUM.get( SEQUENCE ) ){
			if( select ) record.reAddValue( TIME );
			else record.removeValue( TIME );
		}
		EVENT_PROXY.historyRecordSelectStateChanged( ALL_HISTORY_DATUM.get( SEQUENCE ) );
		selectState = select;
	}
	
	/**get the sequence id*/
	public String getNodeId(){
		return SEQUENCE.getId();
	}

	/**get the time*/
	public String getDateTime(){
		return DATE_FORMAT.format(TIME);
	}

	/**get the record name*/
	public String getRecordName(){
		return recordName;
	}

	/**set the record name*/
	public void setRecordName( final String newName ){
		recordName = newName;
	}
		
}

}

