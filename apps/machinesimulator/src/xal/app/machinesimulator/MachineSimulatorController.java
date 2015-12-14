/**
 *
 */
package xal.app.machinesimulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import xal.app.machinesimulator.MachineModel.SimulationHistoryRecord;
import xal.extension.bricks.WindowReference;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.smf.FunctionGraphsXALSynopticAdaptor;
import xal.extension.widgets.smf.XALSynopticPanel;
import xal.extension.widgets.swing.KeyValueFilteredTableModel;
import xal.smf.AcceleratorSeq;
import xal.tools.data.KeyValueAdaptor;
import xal.tools.dispatch.DispatchQueue;
import xal.tools.dispatch.DispatchTimer;

/**
 * @author luxiaohan
 * controller for binding the MachineSimulator model to the user interface
 */
public class MachineSimulatorController implements MachineModelListener {
     /** simulated states table model */
     final private KeyValueFilteredTableModel<MachineSimulationHistoryRecord> STATES_TABLE_MODEL;
     /**accelerator node table model*/
     final private KeyValueFilteredTableModel<NodePropertyRecord> NEW_PARAMETERS_TABLE_MODEL;
     /**history record table model*/
     final private KeyValueFilteredTableModel<SimulationHistoryRecord> HISTORY_RECORD_TABLE_MODEL;
     /**parameter history table model*/
     final private KeyValueFilteredTableModel<NodePropertyHistoryRecord> HISTORY_DATA_TABLE_MODEL;
     /**bpm live table model*/
     final private KeyValueFilteredTableModel<DiagnosticAgent> DIAG_LIVE_TABLE_MODEL;
     /**bpm record table model*/
     final private KeyValueFilteredTableModel<DiagnosticRecord> DIAG_RECORD_TABLE_MODEL;
     /** main model */
     final private MachineModel MODEL;
     /** key value adaptor to get the twiss value from a record for the specified key path */
     final private KeyValueAdaptor KEY_VALUE_ADAPTOR;
     /**the scalar parameters*/
     final private List<ScalarParameter> SCALAR_PARAMETERS;
     /**the model vector parameters*/
     final private List<ModelVectorParameter> MODEL_VECTOR_PARAMETERS;
     /**the machine vector parameters*/
     final private List<MachineVectorParameter> MACHINE_VECTOR_PARAMETERS;
     /**timer to sync the live value*/
     final private DispatchTimer VALUE_SYNC_TIME;
     /**the legend name*/
     final private String[] LEGEND_NAME;
     /**a map array from parameter's key to plot data list*/
     private Map<String, List<Double>> simDataForPlot;
     /**diagnostic plot datum*/
     private Map<String, List<Double>> diagDataForPlot;
     /**the positions of diagnostic device*/
     private List<Double> diagPosition;
     /**the sequence*/
     private AcceleratorSeq _sequence;
     /**sync period in milliseconds*/
     private long _syncPeriod;
     /** the plotter*/
     private MachineSimulatorPlot _machineSimulatorPlot;
     /**the list of NodePropertyRecord*/
     private List<NodePropertyRecord> nodePropertyRecords;
     /**the list of diagnostic records*/
     private List<DiagnosticRecord> diagDatum;
     /**the key paths for diagnostic records*/
     private String[] keyPathsForDiagRecord;
     /** the position list of elements*/
     private List<Double> _positions;
     /**the key paths for show difference between two simulation results*/
     private List<String> keyPathsForSimDiff;
     /**the key paths for show difference between two diagnostic records*/
     private List<String> keyPathsForDiagDiff;
     /**refresh action*/
     private ActionListener refresh;



	/**constructor */
	public  MachineSimulatorController(final MachineSimulatorDocument document,final WindowReference windowReference) {
		
		STATES_TABLE_MODEL = new KeyValueFilteredTableModel<MachineSimulationHistoryRecord>();
		NEW_PARAMETERS_TABLE_MODEL = new KeyValueFilteredTableModel<NodePropertyRecord>();
		HISTORY_RECORD_TABLE_MODEL = new KeyValueFilteredTableModel<SimulationHistoryRecord>();
		HISTORY_DATA_TABLE_MODEL = new KeyValueFilteredTableModel<NodePropertyHistoryRecord>();
		
		DIAG_LIVE_TABLE_MODEL = new KeyValueFilteredTableModel<DiagnosticAgent>();
		DIAG_RECORD_TABLE_MODEL = new KeyValueFilteredTableModel<DiagnosticRecord>();
		
		
		LEGEND_NAME = new String[2];
		
		KEY_VALUE_ADAPTOR= new KeyValueAdaptor();
		
		VALUE_SYNC_TIME = DispatchTimer.getCoalescingInstance( DispatchQueue.getMainQueue(), getLiveValueSynchronizer() );
		// set the default sync period to 1 second
		_syncPeriod = 1000;
		
      // initialize the model here
      MODEL = document.getModel();
      MODEL.addMachineModelListener(this);
		
		SCALAR_PARAMETERS = new ArrayList<ScalarParameter>();
		SCALAR_PARAMETERS.add(new ScalarParameter("Kinetic Energy", "probeState.kineticEnergy"));
		
		MACHINE_VECTOR_PARAMETERS = new ArrayList<MachineVectorParameter>();
		MACHINE_VECTOR_PARAMETERS.add( new MachineVectorParameter( "Bpm", "BPM" ) );
		
		MODEL_VECTOR_PARAMETERS = new ArrayList<ModelVectorParameter>();
		MODEL_VECTOR_PARAMETERS.add(new ModelVectorParameter( "Orbit", "", "posCoordinates" ) );
		MODEL_VECTOR_PARAMETERS.add(new ModelVectorParameter( "Beta","beta","twissParameters", "beta" ) );
		MODEL_VECTOR_PARAMETERS.add(new ModelVectorParameter( "Alpha","alpha","twissParameters", "alpha" ) );
		MODEL_VECTOR_PARAMETERS.add(new ModelVectorParameter( "Gamma","gamma","twissParameters", "gamma" ) );
		MODEL_VECTOR_PARAMETERS.add(new ModelVectorParameter( "Emittance","epsilon","twissParameters", "emittance" ) );
		MODEL_VECTOR_PARAMETERS.add(new ModelVectorParameter( "EnvelopeRadius","sigma","twissParameters", "envelopeRadius" ) );
		MODEL_VECTOR_PARAMETERS.add(new ModelVectorParameter( "BetatronPhase","phi","betatronPhase" ) );
        
      configureMainWindow(windowReference);
	}


    /** configure the main window */
    private void configureMainWindow( final WindowReference windowReference ) {
    	  //get the filter
        final JTextField statesTableFilterField = (JTextField)windowReference.getView( "States Table Filter Field" );
        // handle the parameter selections
        final JCheckBox kineticEnergyCheckbox = (JCheckBox)windowReference.getView( "Kinetic Energy Checkbox" );

        final JCheckBox xSelectionCheckbox = (JCheckBox)windowReference.getView( "X Selection Checkbox" );
        final JCheckBox ySelectionCheckbox = (JCheckBox)windowReference.getView( "Y Selection Checkbox" );
        final JCheckBox zSelectionCheckbox = (JCheckBox)windowReference.getView( "Z Selection Checkbox" );
        
        final JCheckBox bpmCheckbox = (JCheckBox)windowReference.getView( "BPM" );
        
        final JCheckBox orbitCheckbox = (JCheckBox)windowReference.getView( "Orbit Checkbox" );
        final JCheckBox betaCheckbox = (JCheckBox)windowReference.getView( "Beta Checkbox" );
        final JCheckBox alphaCheckbox = (JCheckBox)windowReference.getView( "Alpha Checkbox" );
        final JCheckBox gammaCheckbox = (JCheckBox)windowReference.getView( "Gamma Checkbox" );
        final JCheckBox emittanceCheckbox = (JCheckBox)windowReference.getView( "Emittance Checkbox" );
        final JCheckBox beamSizeCheckbox = (JCheckBox)windowReference.getView( "Beam Size Checkbox" );
        final JCheckBox betatronPhaseCheckbox = (JCheckBox)windowReference.getView( "Betatron Phase Checkbox" );
       
        //the show difference checkbox in plot view
		  final JCheckBox showDifference = (JCheckBox)windowReference.getView( "Show Difference" );        
/******************configure the new parameters table view*************************************/
	        
	     final JTable sequenceTable = (JTable)windowReference.getView( "New Run Parameters" );
	     sequenceTable.setModel( NEW_PARAMETERS_TABLE_MODEL ); 
	        
	     //set the column name of the sequence table
	     NEW_PARAMETERS_TABLE_MODEL.setColumnName( "acceleratorNode.id", "Node" );
	     NEW_PARAMETERS_TABLE_MODEL.setColumnName( "propertyName", "Property" );
	        
	     //set the filter field for sequence table
	     NEW_PARAMETERS_TABLE_MODEL.setInputFilterComponent(statesTableFilterField);
	     NEW_PARAMETERS_TABLE_MODEL.setMatchingKeyPaths( "acceleratorNode.id" );
	         
	     //configure the sequence table model
		  NEW_PARAMETERS_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, "designValue", "liveValue", "testValue" );
		  NEW_PARAMETERS_TABLE_MODEL.setKeyPaths( "acceleratorNode.id", "propertyName", "designValue", "liveValue", "testValue" );
		  NEW_PARAMETERS_TABLE_MODEL.setColumnEditable( "testValue", true );
        
/***************configure the history record view*****************************************/
        //configure history record table
        final JTable historyRecordTable = (JTable)windowReference.getView( "History Record Table" );
        historyRecordTable.setModel(HISTORY_RECORD_TABLE_MODEL);
        
        HISTORY_RECORD_TABLE_MODEL.setColumnName("selectState", "Compare");
        HISTORY_RECORD_TABLE_MODEL.setColumnName("sequence.id", "Sequence" );
        HISTORY_RECORD_TABLE_MODEL.setColumnName("dateTime", "Time" );
        
        HISTORY_RECORD_TABLE_MODEL.setColumnClassForKeyPaths(Boolean.class, "selectState");
        HISTORY_RECORD_TABLE_MODEL.setKeyPaths( "selectState", "sequence.id", "dateTime", "recordName");
        HISTORY_RECORD_TABLE_MODEL.setColumnEditable( "recordName", true );
        HISTORY_RECORD_TABLE_MODEL.setColumnEditable( "selectState", true );
        
        //configure history data table
        final JTable historyDataTable = (JTable)windowReference.getView( "History Data Table" );
        historyDataTable.setModel( HISTORY_DATA_TABLE_MODEL );
        
        HISTORY_DATA_TABLE_MODEL.setColumnName( "acceleratorNode.id" , "Node" );
        HISTORY_DATA_TABLE_MODEL.setColumnName( "propertyName", "Property" );
        
        HISTORY_DATA_TABLE_MODEL.setInputFilterComponent( statesTableFilterField );
        HISTORY_DATA_TABLE_MODEL.setMatchingKeyPaths( "acceleratorNode.id" );
        
/**********************configure the diagnostics history view***********************************/
        final JTable diagLiveTable = (JTable)windowReference.getView( "Diag Live Table" );
        diagLiveTable.setModel( DIAG_LIVE_TABLE_MODEL );
        
        DIAG_LIVE_TABLE_MODEL.setInputFilterComponent( statesTableFilterField );
        DIAG_LIVE_TABLE_MODEL.setMatchingKeyPaths( "node.id" );
        
        DIAG_LIVE_TABLE_MODEL.setColumnName( "checkState", "use" );
        DIAG_LIVE_TABLE_MODEL.setColumnName( "node.id",  "BPM" );
        DIAG_LIVE_TABLE_MODEL.setColumnName( "values.0", "xAvg" );
        DIAG_LIVE_TABLE_MODEL.setColumnName( "values.1", "yAvg" );
        
		  final String[] keyPathsForDiagLive = {"checkState", "node.id", "position", "values.0", "values.1"};
        
        DIAG_LIVE_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, "position", "values.0", "values.1");
        DIAG_LIVE_TABLE_MODEL.setColumnClass( "checkState", Boolean.class );
        DIAG_LIVE_TABLE_MODEL.setColumnEditable( "checkState", true );
        DIAG_LIVE_TABLE_MODEL.setKeyPaths(keyPathsForDiagLive);
        
        //configure the diagnostic record table
        final JTable diagRecordTable = (JTable)windowReference.getView( "Diag Record Table");
        diagRecordTable.setModel( DIAG_RECORD_TABLE_MODEL );
        
        DIAG_RECORD_TABLE_MODEL.setInputFilterComponent( statesTableFilterField );
        DIAG_RECORD_TABLE_MODEL.setMatchingKeyPaths( "node.id" );
        DIAG_RECORD_TABLE_MODEL.setColumnName( "node.id", "BPM" );

/**********************configure the states table view***********************************/
	     //get components
	     final JTable statesTable = (JTable)windowReference.getView( "States Table" );
	     statesTable.setModel( STATES_TABLE_MODEL );
	     
        //set the column name of the states table
        STATES_TABLE_MODEL.setColumnName( "elementID", "Element" );

        //set the filter field for states table
        STATES_TABLE_MODEL.setInputFilterComponent( statesTableFilterField );
        STATES_TABLE_MODEL.setMatchingKeyPaths( "elementID" );
        
		  STATES_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, "position");
       
/**************************configure the plot view**************************************/
		  final List<Parameter> parameters = new ArrayList<Parameter>();
		  parameters.addAll( SCALAR_PARAMETERS );
		  parameters.addAll( MACHINE_VECTOR_PARAMETERS );
		  parameters.addAll( MODEL_VECTOR_PARAMETERS );
        final FunctionGraphsJPanel twissParametersPlot = ( FunctionGraphsJPanel ) windowReference.getView( "States Plot" );
        _machineSimulatorPlot = new MachineSimulatorPlot( twissParametersPlot, parameters );

		//synoptic display of nodes
		final Box synopticBox = ( Box )windowReference.getView( "SynopticContainer" );
		final XALSynopticPanel xalSynopticPanel = FunctionGraphsXALSynopticAdaptor.assignXALSynopticViewTo( twissParametersPlot, _sequence );
		synopticBox.removeAll();
		synopticBox.add( xalSynopticPanel );
		synopticBox.validate();

/***************************Check boxes action**************************************/

		//configure the bpm checkbox action
		final ActionListener BPM_HANDLER = event -> {
			if ( diagDatum != null && keyPathsForDiagRecord.length>3 ) {
				int leg = keyPathsForDiagRecord.length;
				LinkedList<String> keyPathsForDiagPlot = new LinkedList<String>();
				for ( int index = 0; index < leg-3; index++ ){
					keyPathsForDiagPlot.addFirst( keyPathsForDiagRecord[leg-1-index] );
					if ( index == 1 ) break;
				}				
				keyPathsForDiagPlot.addFirst( keyPathsForDiagRecord[2] );				
				keyPathsForDiagDiff = keyPathsForDiagPlot.subList( 1, keyPathsForDiagPlot.size() );
				diagDataForPlot = configureParametersData( diagDatum, keyPathsForDiagPlot );
				List<Double> pos = diagDataForPlot.get( keyPathsForDiagRecord[2] );			
				diagPosition = pos.subList(0, pos.size()/2);
				int upLimit = keyPathsForDiagPlot.size()-1;
				for ( int index = 0; index < upLimit; index++ ) {						
					if ( xSelectionCheckbox.isSelected() ) {
						_machineSimulatorPlot.showPlot( diagPosition, 
								diagDataForPlot.get( keyPathsForDiagPlot.get( index+1 ) ).subList(0, pos.size()/2),
								(upLimit-1-index)+".BPM.X.", LEGEND_NAME[upLimit-1-index] );
					}
					if ( ySelectionCheckbox.isSelected() ) {
						_machineSimulatorPlot.showPlot( diagPosition,
								diagDataForPlot.get( keyPathsForDiagPlot.get( index+1 ) ).subList(pos.size()/2, pos.size() ),
								(upLimit-1-index)+".BPM.Y.", LEGEND_NAME[upLimit-1-index] );
					}
				}
			}
			
		};
		
		//configure the show difference check box of plot view
		final ActionListener SHOW_DIFFERENCE_HANDELER =  event -> {
			MachineSimulation[] simulations = MODEL.getHistorySimulation(_sequence);
			if ( showDifference.isSelected() && simulations[0] != null ){
				if ( simulations[1] != null ){
					twissParametersPlot.removeAllGraphData();
					//show difference between two diagnostic records
					if ( bpmCheckbox.isSelected() ) {
						Map<String, List<Double>> diagDiff = calculateDiff( keyPathsForDiagDiff, diagDataForPlot );
						for( int index = 0; index<keyPathsForDiagDiff.size(); index++ ){
							final String legName = LEGEND_NAME[0]+" - "+LEGEND_NAME[1];
							String keyDiag = keyPathsForDiagDiff.get( index+1 );
							if ( xSelectionCheckbox.isSelected() ) {
								_machineSimulatorPlot.showPlot( diagPosition, 
										diagDiff.get( keyDiag ).subList( 0, diagPosition.size() ),
										"BPM.X", legName );
							}
							if ( ySelectionCheckbox.isSelected() ) {
								_machineSimulatorPlot.showPlot( diagPosition,
										diagDiff.get( keyDiag ).subList( diagPosition.size(), 2*diagPosition.size() ),
										"BPM.Y", legName );
							}
							index++;
						}
					}
					//show difference between two simulation results
					Map<String, List<Double>> simDiff = calculateDiff(keyPathsForSimDiff, simDataForPlot);
					for( int index = 0; index<keyPathsForSimDiff.size(); index++ ){
						final String legName = LEGEND_NAME[0]+" - "+LEGEND_NAME[1];
						String keySim = keyPathsForSimDiff.get( index+1 );
						_machineSimulatorPlot.showPlot( _positions, simDiff.get( keySim ) , keySim, legName );
						index++;
					}

				}
			}
			else {
				showDifference.setSelected(false);
				refresh.actionPerformed(null);
			}
		};
		
		showDifference.addActionListener(SHOW_DIFFERENCE_HANDELER);

		
		final ActionListener PARAMETER_HANDLER = new ActionListener() {	
			public void actionPerformed( final ActionEvent event ) {
				//get the first two simulations which are selected 
				MachineSimulation[] simulations = MODEL.getHistorySimulation( _sequence );
				//configure the column name of states table
		        for(final ScalarParameter scalarParameter:SCALAR_PARAMETERS){
		            STATES_TABLE_MODEL.setColumnName(scalarParameter.getKeyPath(), scalarParameter.getSymbol()+" : "+LEGEND_NAME[0] );
		            STATES_TABLE_MODEL.setColumnName("old."+scalarParameter.getKeyPath(), scalarParameter.getSymbol()+" : "+LEGEND_NAME[1] );
		           }
		        for(final ModelVectorParameter vectorParameter:MODEL_VECTOR_PARAMETERS){
		        	   String symbX = vectorParameter.getSymbolForX();
		        	   String symbY = vectorParameter.getSymbolForY();
		        	   String symbZ = vectorParameter.getSymbolForZ();
		        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForX(),
		        			   symbX.substring(0, symbX.length()-6)+" : "+LEGEND_NAME[0]+"<html>" );
		        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForY(), 
		        			   symbY.substring(0, symbY.length()-6)+" : "+LEGEND_NAME[0]+"<html>" );
		        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForZ(), 
		        			   symbZ.substring(0, symbZ.length()-6)+" : "+LEGEND_NAME[0]+"<html>" );
		        	   
		        	   STATES_TABLE_MODEL.setColumnName("old."+vectorParameter.getKeyPathForX(), 
		        			   symbX.substring(0, symbX.length()-6)+" : "+LEGEND_NAME[1]+"<html>" );
		        	   STATES_TABLE_MODEL.setColumnName("old."+vectorParameter.getKeyPathForY(), 
		        			   symbY.substring(0, symbY.length()-6)+" : "+LEGEND_NAME[1]+"<html>" );
		        	   STATES_TABLE_MODEL.setColumnName("old."+vectorParameter.getKeyPathForZ(), 
		        			   symbZ.substring(0, symbZ.length()-6)+" : "+LEGEND_NAME[1]+"<html>" );
		           }
				
				// array of standard parameters to display
				final String[] standardParameterKeys = new String[] { "elementID", "position" };

				// array of optional scalar and vector parameters to display
				final List<String> parameterKeyPathsList = new ArrayList<String>();
				if ( kineticEnergyCheckbox.isSelected() )  parameterKeyPathsList.add(SCALAR_PARAMETERS.get(0).getKeyPath());

				// Add each selected plan to the list of planes to display
				final List<String> planes = new ArrayList<String>(3);
				if ( xSelectionCheckbox.isSelected() )  planes.add( "X" );
				if ( ySelectionCheckbox.isSelected() )  planes.add( "Y" );
				if ( zSelectionCheckbox.isSelected() )  planes.add( "Z" );
				// Add each selected twiss parameter key path to the list of parameters to display
				for ( final String plane : planes ){
					if ( orbitCheckbox.isSelected() ) parameterKeyPathsList.add( MODEL_VECTOR_PARAMETERS.get(0).getKeyPathToArray().get(plane) );
					if ( betaCheckbox.isSelected() ) parameterKeyPathsList.add( MODEL_VECTOR_PARAMETERS.get(1).getKeyPathToArray().get(plane) );
					if ( alphaCheckbox.isSelected() ) parameterKeyPathsList.add( MODEL_VECTOR_PARAMETERS.get(2).getKeyPathToArray().get(plane) );
					if ( gammaCheckbox.isSelected() )  parameterKeyPathsList.add( MODEL_VECTOR_PARAMETERS.get(3).getKeyPathToArray().get(plane) );
					if ( emittanceCheckbox.isSelected() ) parameterKeyPathsList.add( MODEL_VECTOR_PARAMETERS.get(4).getKeyPathToArray().get(plane) );
					if ( beamSizeCheckbox.isSelected() )  parameterKeyPathsList.add( MODEL_VECTOR_PARAMETERS.get(5).getKeyPathToArray().get(plane) );
					if ( betatronPhaseCheckbox.isSelected() ) parameterKeyPathsList.add( MODEL_VECTOR_PARAMETERS.get(6).getKeyPathToArray().get(plane) );
				}
				
				//create the combination keyPaths used for comparing new and old simulation results
				final List<String> combParameterKeyPathsList = new ArrayList<String>(2*parameterKeyPathsList.size());
				for( final String path:parameterKeyPathsList){
					combParameterKeyPathsList.add( "old."+path );
					combParameterKeyPathsList.add( path );
				}
				
				keyPathsForSimDiff = combParameterKeyPathsList;
				
				//determine the final keyPaths by history simulation record select state
				final List<String> parameterKeyPathsForTableList;
				
				if( simulations[1] != null ){
					parameterKeyPathsForTableList = combParameterKeyPathsList;
				}
				else {
					parameterKeyPathsForTableList = parameterKeyPathsList;
				}

				//turn the keyPaths list to string array
				final String[] parameterKeyPathsForTable= new String[parameterKeyPathsForTableList.size()];
				parameterKeyPathsForTableList.toArray( parameterKeyPathsForTable );

				STATES_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, parameterKeyPathsForTable );
				
				final String[] allKeyPathsForTable = new String[standardParameterKeys.length + parameterKeyPathsForTable.length];
				// add standard parameters at the start
				System.arraycopy( standardParameterKeys, 0, allKeyPathsForTable, 0, standardParameterKeys.length );
				// append scalar and vector parameters after standard parameters
				System.arraycopy( parameterKeyPathsForTable, 0, allKeyPathsForTable, standardParameterKeys.length, parameterKeyPathsForTable.length );

				STATES_TABLE_MODEL.setKeyPaths( allKeyPathsForTable );
				STATES_TABLE_MODEL.setRecords( MODEL.getSimulationRecords( simulations[0], simulations[1] ));

				/**************   configure plot view   ****************/

				//setup plot panel and show the selected parameters' graph
				twissParametersPlot.removeAllGraphData();
				if ( bpmCheckbox.isSelected() ) BPM_HANDLER.actionPerformed( null );
				
				if ( _sequence != null ) twissParametersPlot.setName( _sequence.getId() );
				if( parameterKeyPathsForTable.length > 0 && simulations[0] != null ){
					simDataForPlot = configureParametersData(  MODEL.getSimulationRecords( simulations[0], simulations[1] ), parameterKeyPathsForTableList );
					_positions = simulations[0].getAllPositions();
					for( final String parameterKey:parameterKeyPathsForTable ){
						final String legName = parameterKey.contains("old") ? LEGEND_NAME[1] : LEGEND_NAME[0]; 
						_machineSimulatorPlot.showPlot( _positions, simDataForPlot.get(parameterKey), parameterKey, legName );					
					}
					
					if ( showDifference.isSelected() ) SHOW_DIFFERENCE_HANDELER.actionPerformed(null);
				}
				xalSynopticPanel.setAcceleratorSequence( _sequence );
			}
		};

/*************************activate check boxes***************************************/
		
        kineticEnergyCheckbox.addActionListener( PARAMETER_HANDLER );

        xSelectionCheckbox.addActionListener( PARAMETER_HANDLER );
        ySelectionCheckbox.addActionListener( PARAMETER_HANDLER );
        zSelectionCheckbox.addActionListener( PARAMETER_HANDLER );
        
		bpmCheckbox.addActionListener( PARAMETER_HANDLER );
        
        orbitCheckbox.addActionListener( PARAMETER_HANDLER );
        betaCheckbox.addActionListener( PARAMETER_HANDLER );
        alphaCheckbox.addActionListener( PARAMETER_HANDLER );
        gammaCheckbox.addActionListener( PARAMETER_HANDLER );
        emittanceCheckbox.addActionListener( PARAMETER_HANDLER );
        beamSizeCheckbox.addActionListener( PARAMETER_HANDLER );

        betatronPhaseCheckbox.addActionListener( PARAMETER_HANDLER );

        // perform the initial parameter display configuration
        PARAMETER_HANDLER.actionPerformed( null );

        // configure the Clear All button
        final JButton ClearButton = (JButton)windowReference.getView( "Clear All" );
        final ActionListener CLEAR_BUTTON=new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
            	kineticEnergyCheckbox.setSelected(false);
            	bpmCheckbox.setSelected(false);
            	orbitCheckbox.setSelected(false);
                xSelectionCheckbox.setSelected(false);
                ySelectionCheckbox.setSelected(false);
                zSelectionCheckbox.setSelected(false);
                betaCheckbox.setSelected(false);
                alphaCheckbox.setSelected(false);
                gammaCheckbox.setSelected(false);
                emittanceCheckbox.setSelected(false);
                beamSizeCheckbox.setSelected(false);
                betatronPhaseCheckbox.setSelected(false);

                PARAMETER_HANDLER.actionPerformed( null );
            }
        };
      ClearButton.addActionListener(CLEAR_BUTTON);


        // configure the run button
        final JButton runButton = (JButton)windowReference.getView( "Run Button" );
        runButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {                
                if( MODEL.getSequence() != null ){
                   System.out.println( "running the model..." );
                   _sequence = MODEL.getSequence();
                	final MachineSimulation simulation = MODEL.runSimulation();
                	
                  //set records and configure history data table
                  historyRecordSelectStateChanged( MODEL.getNodePropertyHistoryRecords().get(_sequence),
                		  MODEL.getColumnNames().get(_sequence), MODEL.getDiagRecords(), _sequence );
                  
                	//set records of states table
                	STATES_TABLE_MODEL.setRecords( MODEL.getSimulationRecords(simulation, MODEL.getHistorySimulation( _sequence )[1] ) );
                	//set records for history record table
                  HISTORY_RECORD_TABLE_MODEL.setRecords(MODEL.getSimulationHistoryRecords());
                 
//                  refresh.actionPerformed( null );
                     }
                else JOptionPane.showMessageDialog(windowReference.getWindow(),
                		"You need to select sequence(s) first","Warning!",JOptionPane.PLAIN_MESSAGE);       

            }
        });

		
		//configure the remove button of the history record view
		final JButton removeButton = (JButton)windowReference.getView( "Remove Button" );
		removeButton.addActionListener( event -> {		
			List<SimulationHistoryRecord> records = MODEL.getSimulationHistoryRecords();
			int removed = 0;
			int[] selRows = historyRecordTable.getSelectedRows();
			for( int index = 0; index < selRows.length; index++ ){
				records.get( selRows[index]-removed ).setSelectState( false );
				records.remove( selRows[index]-removed );
				removed++;
			}
			if ( records.size() == 0 ) HISTORY_DATA_TABLE_MODEL.setRecords( null );
			HISTORY_RECORD_TABLE_MODEL.setRecords( records );
		});
		
		//configure the select all button
		final JButton selectAllButton = (JButton)windowReference.getView( "Check All" );
		selectAllButton.addActionListener( event -> {
			List<SimulationHistoryRecord> records = MODEL.getSimulationHistoryRecords();
			for ( int index = 0; index < records.size();index++){
				records.get( index ).setSelectState( true );
			}
			HISTORY_RECORD_TABLE_MODEL.fireTableRowsUpdated(0, records.size()-1 );;
		});
		
		//configure the unselect all button
		final JButton unselectAllButton = (JButton)windowReference.getView( "Uncheck All" );
		unselectAllButton.addActionListener( event -> {
			List<SimulationHistoryRecord> records = MODEL.getSimulationHistoryRecords();
			for ( int index = 0; index < records.size();index++){
				records.get( index ).setSelectState( false );
			}
			HISTORY_DATA_TABLE_MODEL.setRecords(null);
			HISTORY_RECORD_TABLE_MODEL.fireTableRowsUpdated( 0, records.size()-1 );
		});

		
		//configure the refresh action
		refresh = event -> {		
			if ( MODEL.getColumnNames().get(_sequence ) != null ){
				final Map<Date, String> recordName = MODEL.getColumnNames().get(_sequence);
				final String[] name = new String[recordName.size()];
				recordName.values().toArray( name );
				for ( int index = 0 ; index< name.length; index++ ){
					LEGEND_NAME[index] = name[name.length-1-index];
					if ( index == 1 ) break;
				}
			}

			PARAMETER_HANDLER.actionPerformed( null );
			
			if ( showDifference.isSelected() ) SHOW_DIFFERENCE_HANDELER.actionPerformed( null );
		};

    }
	
  /** get the selected parameters' data from simulation records
    * @param records the result of simulation
    * @param keyPaths specifies the array of key paths to get the data to plot
    */ 
    private <T> Map<String, List<Double>> configureParametersData( final List<T> records,final List<String> keyPaths ){
      final Map<String, List<Double>> datum = new HashMap<String, List<Double>>();     
    	for( final String keyPath:keyPaths ){
    		datum.put( keyPath, new ArrayList<Double>( records.size() ) );
    		for( final T record:records ){
    			datum.get(keyPath).add( (Double)KEY_VALUE_ADAPTOR.valueForKeyPath( record,keyPath ) );
    		}
    	}   	
    	return datum;
    }
    
    /**
     * calculate the difference of two records' datum
     * @param keyPaths the key paths 
     * @param datum the datum array include two records' datum
     * @return the difference datum
     */
    private Map<String,List<Double>> calculateDiff( final List<String> keyPaths, final Map<String, List<Double>> datum ) {
    	Map<String, List<Double>> diffDatum = new HashMap<String, List<Double>>();
    	for ( int index = 0; index< keyPaths.size(); index++) {
    		List<Double> newValues = datum.get( keyPaths.get( index+1 ) );
    		List<Double> oldValues = datum.get( keyPaths.get( index ) );
    		List<Double> diff = new ArrayList<Double>();
			for( int valueIndex = 0; valueIndex<newValues.size(); valueIndex++ ){							
				diff.add( newValues.get( valueIndex ) - oldValues.get( valueIndex ) );
			}
			diffDatum.put( keyPaths.get( index+1 ), diff );
			index++;
    	}
    	return diffDatum;
    }
    
    /**get a runnalbe that syncs the values */
    private Runnable getLiveValueSynchronizer(){
    	return () -> {
    		NEW_PARAMETERS_TABLE_MODEL.fireTableRowsUpdated( 0, NEW_PARAMETERS_TABLE_MODEL.getRowCount() - 1  );
    		DIAG_LIVE_TABLE_MODEL.fireTableRowsUpdated(0, DIAG_LIVE_TABLE_MODEL.getRowCount() - 1 );;
    	};
    }

    /**event indicates that the sequence has changed*/
    public void modelSequenceChanged( final MachineModel model ) {
    	if( model.getSequence() != null ){
        	_sequence = model.getSequence();
    		nodePropertyRecords = model.getWhatIfConfiguration().getNodePropertyRecords();
    		NEW_PARAMETERS_TABLE_MODEL.setRecords( nodePropertyRecords );
    		
    		DIAG_LIVE_TABLE_MODEL.setRecords( model.getDiagConfig().getBpms() );
   		VALUE_SYNC_TIME.startNowWithInterval( _syncPeriod, 0 );
    	}
    	
    	//unselect all the history records when changing the sequence 
		for ( int index = 0; index < model.getSimulationHistoryRecords().size();index++){
			model.getSimulationHistoryRecords().get( index ).setSelectState( false );
		}
		
		HISTORY_RECORD_TABLE_MODEL.fireTableRowsUpdated(0, model.getSimulationHistoryRecords().size()-1 );
    	
    }


	/**event indicates that the scenario has changed*/
	public void modelScenarioChanged( final MachineModel model) {
		if( _sequence != null ){
			nodePropertyRecords = model.getWhatIfConfiguration().getNodePropertyRecords();
			NEW_PARAMETERS_TABLE_MODEL.setRecords( nodePropertyRecords );
			VALUE_SYNC_TIME.resume();
		}
		
	}

	/**event indicates that the history record select state changed*/
	public void historyRecordSelectStateChanged( final List<NodePropertyHistoryRecord> nodePropertyHistoryRecords,
			final Map<Date, String> columnName, final List<DiagnosticRecord> dRecords, final AcceleratorSeq seq ) {
		
		if ( nodePropertyHistoryRecords != null && columnName != null ) {
			int columnNumber = columnName.size();		
			String[] names = new String[columnNumber];
			columnName.values().toArray( names );
			String[] historyDataKeyPaths = new String[columnNumber+2];
	        historyDataKeyPaths[0] = "acceleratorNode.id";
	        historyDataKeyPaths[1] = "propertyName";
			keyPathsForDiagRecord = new String[columnNumber+3];
			   keyPathsForDiagRecord[0] = "node.id";
			   keyPathsForDiagRecord[1] = "valueName";
			   keyPathsForDiagRecord[2] = "position";
			for ( int index = 0;index<columnNumber; index++ ){
				historyDataKeyPaths[index+2] = "values."+index;
				keyPathsForDiagRecord[index+3] = "values."+index;
				HISTORY_DATA_TABLE_MODEL.setColumnClass( "values."+index, Double.class );
				HISTORY_DATA_TABLE_MODEL.setColumnName( "values."+index, names[index]);
				
				DIAG_RECORD_TABLE_MODEL.setColumnClass( "values."+index, Double.class );
				DIAG_RECORD_TABLE_MODEL.setColumnName( "values."+index, names[index] );
			}
			
			HISTORY_DATA_TABLE_MODEL.setKeyPaths( historyDataKeyPaths );
			HISTORY_DATA_TABLE_MODEL.setRecords( nodePropertyHistoryRecords );
			
			DIAG_RECORD_TABLE_MODEL.setKeyPaths( keyPathsForDiagRecord );
			DIAG_RECORD_TABLE_MODEL.setRecords( dRecords );
		}
		else {
			HISTORY_DATA_TABLE_MODEL.setRecords( null );
			DIAG_RECORD_TABLE_MODEL.setRecords( null );
		}

		_sequence = seq;
		
		diagDatum = dRecords;

		refresh.actionPerformed(null);
		
	}
	
	
}



