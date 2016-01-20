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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.text.DateFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import xal.model.ModelException;
import xal.smf.AcceleratorNode;
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
	/**the selected history record of node property values to show*/
	final private Map<AcceleratorSeq, List<NodePropertyHistoryRecord>> NODE_VALUES_TO_SHOW;
	/**the selected history records of diagnostic values*/
	final private Map<AcceleratorSeq, List<DiagnosticRecord>> DIAG_RECORDS;
	/**the colum name of node property history values table*/
	final private Map<AcceleratorSeq,Map<Date, String>> COLUMN_NAME;
   /** simulator */
   final private MachineSimulator SIMULATOR;
   /** accelerator sequence on which to run the simulations */
   private AcceleratorSeq _sequence;
   /** latest simulation */
   private MachineSimulation _simulation;
   /** whatIfconfiguration*/
   private WhatIfConfiguration _whatIfConfiguration;
   /**BpmsConfiguration*/
   private DiagnosticConfiguration _diagnosticConfiguration;
   /**the list of NodePropertyRecord*/
   private List<NodePropertyRecord> nodePropertyRecords;
   /**history data snapshot*/
   private List<NodePropertySnapshot> nodePropertySnapshots;
   

    
	/** Constructor */
	public MachineModel() {
		MESSAGE_CENTER = new MessageCenter( DATA_LABEL );
		
		SIMULATOR = new MachineSimulator( null );
		
		SIMULATION_HISTORY_RECORDS = new LinkedList<SimulationHistoryRecord>();
		
		NODE_VALUES_TO_SHOW = new LinkedHashMap<AcceleratorSeq, List<NodePropertyHistoryRecord>>();
		
		DIAG_RECORDS = new LinkedHashMap<AcceleratorSeq, List<DiagnosticRecord>>();
		
		COLUMN_NAME = new LinkedHashMap<AcceleratorSeq, Map<Date, String>>();
		
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, MachineModelListener.class );

	}
    
    
    /** set the accelerator sequence on which to run the simulations */
    public void setSequence( final AcceleratorSeq sequence ) throws ModelException {
        SIMULATOR.setSequence( sequence );
        _sequence = sequence;
        setupWhatIfConfiguration( _sequence );
		  _diagnosticConfiguration = setupDiagConfig( _sequence );
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
    
    /**get the diagnostic records*/
    public List<DiagnosticRecord> getDiagRecords() {
    	return DIAG_RECORDS.get( _sequence );
    }
    
    /**get the DiagnosticConfiguration*/
    public DiagnosticConfiguration getDiagConfig() {
    	return _diagnosticConfiguration;
    }
    
    /**setup DiagnosticConfiguration*/
    private DiagnosticConfiguration setupDiagConfig( final AcceleratorSeq sequence ) {
    	DiagnosticConfiguration diagConfiguration = null ;
    	if( sequence != null ) {
    		diagConfiguration = new DiagnosticConfiguration( sequence );
    		if (DIAG_RECORDS.get( sequence ) == null ) {
    			DIAG_RECORDS.put( sequence, diagConfiguration.createDiagRecords() );
    		}
    	}
    	return diagConfiguration;
    }
    
    /**configure the ModelInputs from a list of NodePropertyRecord*/
    private void configModelInputs( final List<NodePropertyRecord> nodePropertyRecords ){
    	SIMULATOR.configModelInputs( nodePropertyRecords );
    }
    
    /** Get the most recent simulation */
    public MachineSimulation getSimulation() {
        return _simulation;
    }
    
    /**Get the most recent two simulations for specified sequence in the selected history record*/
    public MachineSimulation[] getHistorySimulation( final AcceleratorSeq seq ){
    	MachineSimulation[] machineSimulations = new MachineSimulation[2];
    	int index = 0;
    	if( SIMULATION_HISTORY_RECORDS.size() != 0 ){
        	for( SimulationHistoryRecord record: SIMULATION_HISTORY_RECORDS ){
        		if( record.getCheckState() && record.getSequence().getId().equals( seq.getId() ) ){
        			machineSimulations[index++] = record.getSimulation();
        			if ( index == 2 ) break;
        		}		
        	}
    	}
    	return machineSimulations;
    }
    
    /**get the simulation records, if there is a simulation history record which is selected 
     *return the combination of new and selected history records. 
     */
    public List<MachineSimulationHistoryRecord> getSimulationRecords( final MachineSimulation newSimulation,
    		final MachineSimulation oldSimulation ){
    	List<MachineSimulationHistoryRecord> simulationHistoryRecords = new ArrayList<MachineSimulationHistoryRecord>();
    	if( newSimulation != null ){
        	for ( int index = 0; index<newSimulation.getSimulationRecords().size();index++ ){
        		if ( oldSimulation != null ){
        			simulationHistoryRecords.add( new MachineSimulationHistoryRecord( newSimulation.getSimulationRecords().get( index ),
        					oldSimulation.getSimulationRecords().get( index ) ) );
        		}
        		else {
        			simulationHistoryRecords.add( new MachineSimulationHistoryRecord( newSimulation.getSimulationRecords().get( index ) ) );
        		}
        	}
    	}
    	return simulationHistoryRecords;
    }
    
    /**get the simulation history record*/
    public List<SimulationHistoryRecord> getSimulationHistoryRecords(){
    	return SIMULATION_HISTORY_RECORDS;
    }
    
    /**get the sequence of the first selected history record*/
    public AcceleratorSeq getFirstSeq() {
    	List<AcceleratorSeq> seqs = new ArrayList<AcceleratorSeq>();
    	AcceleratorSeq seq = _sequence;
    	boolean hasSame = false;
    	if( SIMULATION_HISTORY_RECORDS.size() != 0 ) {
    		for ( SimulationHistoryRecord record : SIMULATION_HISTORY_RECORDS ) {   			
    			if ( record.getCheckState() ) {
    				seqs.add( record.getSequence() );
    				if( _sequence.getId().equals( record.getSequence().getId() ) ) hasSame = true;
    			}
    		}
    		
    		if ( seqs.size() != 0 && !hasSame ) seq = seqs.get(0);
    	}
    	return seq;
    }
    
    /**change the diagnostic data records to show*/
    public List<DiagnosticRecord> changeDiagRecords( final AcceleratorSeq seq, final Date time, 
    		final boolean checkState, final List<DiagnosticSnapshot> snapshots) {

    	List<DiagnosticRecord> records = DIAG_RECORDS.get( seq );
    	int recordIndex = 0;
    	for ( int index = 0; index<snapshots.size(); index++ ) {
    		int limit = snapshots.get( index ).getValueNames().length;
    		for ( int chanIndex = 0; chanIndex < limit; chanIndex++ ) {
    			if (snapshots.get( index ).getValueNames()[chanIndex] != null ) {
            		double value = snapshots.get( index ).getValues()[chanIndex];
            		if ( checkState ) {
            			records.get( recordIndex ).addValue(time, value);
            			recordIndex++;
            		}
            		else {
            			records.get( recordIndex ).removeValue( time );
            			recordIndex++;
            		}
    			}

    		}

    	}   	
    	return records;
    }
	
    /**get the selected values history records used for simulation */
    public Map<AcceleratorSeq, List<NodePropertyHistoryRecord>> getNodePropertyHistoryRecords(){
    	return NODE_VALUES_TO_SHOW;
    }
    
    /**get the column names which are going to set in the history data table*/
    public Map<AcceleratorSeq,Map<Date, String>> getColumnNames(){
    	return COLUMN_NAME;
    }
	/** Run the simulation and record the result and the values used for simulation */
	public void runSimulation( final String name ) {
		//configure
		nodePropertyRecords = this.getWhatIfConfiguration().getNodePropertyRecords();
		configModelInputs( nodePropertyRecords );
		//run
		_simulation = SIMULATOR.run();
		//record
		if(_simulation != null ) {
			Date time = new Date();		
			//record the values used for simulation
			nodePropertySnapshots = createValuesSnapshot( nodePropertyRecords, SIMULATOR );
			//record column name for the table
			if ( COLUMN_NAME.get( _sequence ) == null ) COLUMN_NAME.put( _sequence, new TreeMap<Date, String>() );
			//record the simulation history
			SIMULATION_HISTORY_RECORDS.addFirst( new SimulationHistoryRecord( time, name ) );		
			
			if ( NODE_VALUES_TO_SHOW.get( _sequence ) == null ){
				NODE_VALUES_TO_SHOW.put( _sequence, new ArrayList<NodePropertyHistoryRecord>());		
			}
			changeHistoryRecordsToShow( _sequence, time, true, nodePropertySnapshots );
			changeDiagRecords( _sequence, time, true, SIMULATION_HISTORY_RECORDS.getFirst().getDiagSnapshots() );
			
		}	
	}
	
	/**record the values assignment to simulation*/
	private List<NodePropertySnapshot> createValuesSnapshot( final List<NodePropertyRecord> nodePropertyRecords,
			final MachineSimulator simulator ){
		List<NodePropertySnapshot> nodePropertySnapshots = new ArrayList<NodePropertySnapshot>(nodePropertyRecords.size());
		for( final NodePropertyRecord record:nodePropertyRecords ){
			AcceleratorNode node = record.getAcceleratorNode();
			String property = record.getPropertyName();
			double value = simulator.getPropertyValuesRecord().get( node ).get( property );
			nodePropertySnapshots.add( new NodePropertySnapshot( node.getId(), property, value ) );
		}		
		return nodePropertySnapshots;
	}
	
	/**add or delete values of NodePropertyHistoryRecord according to the checked state of simulation history records*/
	private List<NodePropertyHistoryRecord> changeHistoryRecordsToShow( final AcceleratorSeq seq, final Date time,
			final Boolean checkedState, final List<NodePropertySnapshot> snapshots ){
		List<NodePropertyHistoryRecord> records = NODE_VALUES_TO_SHOW.get( seq );
		
		if ( records.size() == 0 ){		
			for ( final NodePropertySnapshot snapshot:snapshots){
				String node = snapshot.getNodeId();
				String propertyName = snapshot.getPropertyName();
				records.add( new NodePropertyHistoryRecord( node, propertyName ) );
			}
		}
		
		for ( int index = 0 ; index<records.size(); index++ ){			
			double value = snapshots.get( index ).getValue();
			if ( checkedState ) records.get( index ).addValue( time, value );
			else records.get( index ).removeValue( time );
		}		
		return records;
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
        
        final List<DataAdaptor> historyRecordAdaptors = adaptor.childAdaptors( SimulationHistoryRecord.DATA_LABEL );
        for ( final DataAdaptor recordAdaptor : historyRecordAdaptors ) {
        	SIMULATION_HISTORY_RECORDS.add( new SimulationHistoryRecord( recordAdaptor ) );
        }
    }
    
    
    /** Instructs the receiver to write its data to the adaptor for external storage. */
    public void write( final DataAdaptor adaptor ) {
    	adaptor.writeNode( SIMULATOR );
    	adaptor.writeNodes( SIMULATION_HISTORY_RECORDS );
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
 class SimulationHistoryRecord implements DataListener {
	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "SimulationHistoryRecord"; 
	/**The time when run simulation*/
	final private Date TIME;
	/**time format*/
	final private DateFormat DATE_FORMAT;
	/**the sequence*/
	final private AcceleratorSeq SEQUENCE;
	/**the record of values assignment to simulation*/
	final private List<NodePropertySnapshot> VALUES_SNAPSHOT;
	/**the records of diagnostic data*/
	final private List<DiagnosticSnapshot> DIAG_SNAPSHOTS;
	/**the simulation result*/
	private MachineSimulation SIMULATION;
	/**record name*/
	private String recordName;
	/**checked state*/
	private Boolean checkState;

	/**Constructor*/
	public SimulationHistoryRecord( final Date time, final String recordName ){
		TIME = time;
		SEQUENCE = _sequence;
		SIMULATION = _simulation;
		VALUES_SNAPSHOT = nodePropertySnapshots;
		DIAG_SNAPSHOTS = _diagnosticConfiguration.snapshotValues();
		DATE_FORMAT = DateFormat.getDateTimeInstance();
		this.recordName = ( recordName != null ) ? recordName : " Run "+SIMULATOR.getRunNumber();
		checkState = true;
		COLUMN_NAME.get(SEQUENCE).put( TIME, this.recordName );
	}
	
	/**Constructor with dataAdaptor*/
	public SimulationHistoryRecord ( final DataAdaptor adaptor ) {
		TIME = adaptor.hasAttribute( "time" ) ? new Date( adaptor.longValue( "time" ) ) : new Date() ;
		DATE_FORMAT = DateFormat.getDateTimeInstance();
		
		SEQUENCE = _sequence.getAccelerator().findSequence( adaptor.stringValue( "sequence" ) );
		recordName = adaptor.hasAttribute( "recordName" ) ? adaptor.stringValue( "recordName" ) : "Run_Old"+SIMULATOR.getRunNumber();
		checkState = false;
		
		SIMULATION = null;
		VALUES_SNAPSHOT = new ArrayList<NodePropertySnapshot>();
		DIAG_SNAPSHOTS = new ArrayList<DiagnosticSnapshot>();

		update( adaptor );
	}
	
	/**get the check state*/
	public Boolean getCheckState(){
		return checkState;
	}
	
	/**set the check state*/
	public void setCheckState( final Boolean check ){
		checkState = check;
		changeHistoryRecordsToShow( SEQUENCE, TIME, check, VALUES_SNAPSHOT );
		changeDiagRecords( SEQUENCE, TIME, check, DIAG_SNAPSHOTS );
		if ( check ) COLUMN_NAME.get( SEQUENCE ).put( TIME, recordName );
		else COLUMN_NAME.get( SEQUENCE).remove( TIME );
		AcceleratorSeq seq = getFirstSeq();			
		EVENT_PROXY.historyRecordCheckStateChanged( NODE_VALUES_TO_SHOW.get( seq ), COLUMN_NAME.get( seq ), DIAG_RECORDS.get(seq), seq );

	}
	
	/**get the sequence*/
	public AcceleratorSeq getSequence(){
		return SEQUENCE;
	}
	
	/**get simulation*/
	public MachineSimulation getSimulation(){
		return SIMULATION;
	}
	
	/**get the values assignment to simulation*/
	public List<NodePropertySnapshot> getValuesSnapshot(){
		return VALUES_SNAPSHOT;
	}
	
	/**get the diag data*/
	public List<DiagnosticSnapshot> getDiagSnapshots() {
		return DIAG_SNAPSHOTS;
	}
	
	/**get the date variable*/
	public Date getDate(){
		return TIME;
	}

	/**get the time*/
	public String getDateTime(){
		return DATE_FORMAT.format( TIME );
	}

	/**get the record name*/
	public String getRecordName(){
		return recordName;
	}

	/**set the record name*/
	public void setRecordName( final String newName ){
		COLUMN_NAME.get( SEQUENCE ).replace(TIME, newName);
		AcceleratorSeq seq = getFirstSeq();	
		EVENT_PROXY.historyRecordCheckStateChanged( NODE_VALUES_TO_SHOW.get( seq ), COLUMN_NAME.get( seq ), DIAG_RECORDS.get(seq), seq );

		recordName = newName;
	}

	/** provides the name used to identify the class in an external data source. */
	public String dataLabel() {
		return DATA_LABEL;
	}

	/** Instructs the receiver to update its data based on the given adaptor. */
	public void update( DataAdaptor adaptor ) {
		VALUES_SNAPSHOT.clear();
		final List<DataAdaptor> nodeProSnapshotsAdaptors = adaptor.childAdaptors( NodePropertySnapshot.DATA_LABEL );
		for ( final DataAdaptor nodeProSnapshotsAdaptor : nodeProSnapshotsAdaptors ) {
			VALUES_SNAPSHOT.add( new NodePropertySnapshot( nodeProSnapshotsAdaptor ) );
		}
		
		DIAG_SNAPSHOTS.clear();
		final List<DataAdaptor> diagSnapshotsAdaptors = adaptor.childAdaptors( DiagnosticSnapshot.DATA_LABEL );
		for ( final DataAdaptor diagSnapshotsAdaptor : diagSnapshotsAdaptors ) {
			DIAG_SNAPSHOTS.add( new DiagnosticSnapshot( diagSnapshotsAdaptor ) );
		}
		
		final DataAdaptor simAdaptor = adaptor.childAdaptor( MachineSimulation.DATA_LABEL );	
		SIMULATION = new MachineSimulation( simAdaptor );
		

		if ( COLUMN_NAME.get( SEQUENCE ) == null ) COLUMN_NAME.put( SEQUENCE, new TreeMap<Date, String>());
		if ( NODE_VALUES_TO_SHOW.get( SEQUENCE ) == null ){
			NODE_VALUES_TO_SHOW.put( SEQUENCE, new ArrayList<NodePropertyHistoryRecord>());		
		}
		
		setupDiagConfig( SEQUENCE );
	}

	/** Instructs the receiver to write its data to the adaptor for external storage. */
	public void write(DataAdaptor adaptor) {
		adaptor.setValue( "time", TIME.getTime() );
		adaptor.setValue( "recordName", recordName );
		adaptor.setValue( "sequence", SEQUENCE.getId() );
		adaptor.writeNodes( VALUES_SNAPSHOT );
		adaptor.writeNodes( DIAG_SNAPSHOTS );
		adaptor.writeNode( SIMULATION );
	}
		
}

}

