/**
 *
 */
package xal.app.machinesimulator;

import java.awt.Window;
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
import xal.smf.impl.BPM;
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
     final private KeyValueFilteredTableModel<NodePropertyHistoryRecord> PARAM_HISTORY_TABLE_MODEL;
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
     /**diagnostic plot data*/
     private Map<String, List<Double>> diagDataForPlot;
     /**the positions of diagnostic devices*/
     private Map<String, List<Double>> diagPositions;
     /**the sequence*/
     private AcceleratorSeq _sequence;
     /**sync period in milliseconds*/
     private long _syncPeriod;
     /** the plotter*/
     private MachineSimulatorPlot _machineSimulatorPlot;
     /**the list of NodePropertyRecord*/
     private List<NodePropertyRecord> nodePropertyRecords;
     /**the list of diagnostic records*/
     private List<DiagnosticRecord> diagData;
     /**the key paths for diagnostic records*/
     private String[] keyPathsForDiagRecord;
     /** the position list of elements*/
     private List<Double> _positions;
     /**the scan index*/
     private int scanNumber = 0;
     /**the key paths for show difference between two simulation results*/
     private List<String> keyPathsForSimDiff;
     /**the key paths for show difference between two diagnostic records*/
     private List<String> keyPathsForDiagDiff;
     /**refresh action*/
     private ActionListener refresh;
     /**the selected nodeProperty records used for scan in new run parameters table*/
     private List<NodePropertyRecord> nodePropRecordsForScan;


	/**constructor */
	public  MachineSimulatorController(final MachineSimulatorDocument document,final WindowReference windowReference) {
		
		STATES_TABLE_MODEL = new KeyValueFilteredTableModel<MachineSimulationHistoryRecord>();
		NEW_PARAMETERS_TABLE_MODEL = new KeyValueFilteredTableModel<NodePropertyRecord>();
		HISTORY_RECORD_TABLE_MODEL = new KeyValueFilteredTableModel<SimulationHistoryRecord>();
		PARAM_HISTORY_TABLE_MODEL = new KeyValueFilteredTableModel<NodePropertyHistoryRecord>();
		
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
	
	/**run the model*/
	public void runModel( final Window window, final String name ) {
        if( MODEL.getSequence() != null ){
            System.out.println( "running the model..." );
            _sequence = MODEL.getSequence();
         	
            MODEL.runSimulation( name );
         	
           //set records and configure history data table
           historyRecordCheckStateChanged( MODEL.getNodePropertyHistoryRecords().get( _sequence ),
         		  MODEL.getColumnNames().get( _sequence), MODEL.getDiagRecords(), _sequence );
           
           //change records for history record table
           changeSimHistoryRecords( MODEL.getSimulationHistoryRecords() );
              }
         else JOptionPane.showMessageDialog( window,
         		"You need to select sequence(s) first","Warning!",JOptionPane.PLAIN_MESSAGE);  
	}
	
	
    /** configure the main window */
    private void configureMainWindow( final WindowReference windowReference ) {
    	
    	makeHistoryRecordsView( windowReference );
    	makeNewRunParamView( windowReference );
    	makeParamHistoryView( windowReference );
    	makeDiagView( windowReference );
    	makeStatesTableView( windowReference );

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
		String[] types = { BPM.s_strType };
		String[] paramsForBPM = { BPM.X_AVG_HANDLE, BPM.Y_AVG_HANDLE };
		diagPositions = new HashMap<String, List<Double>>( types.length );
		//configure the diagnostic check-boxes action
		final ActionListener DIAG_HANDLER = event -> {
			if ( diagData != null && diagData.size() != 0 && keyPathsForDiagRecord.length>3 ) {				
				int leg = keyPathsForDiagRecord.length;
				LinkedList<String> keyPathsForDiagPlot = new LinkedList<String>();
				
				// take at most two records to plot
				for ( int index = 0; index < leg-3; index++ ){
					keyPathsForDiagPlot.addFirst( keyPathsForDiagRecord[leg-1-index] );
					if ( index == 1 ) break;
				}
				keyPathsForDiagPlot.addFirst( keyPathsForDiagRecord[2] );
				keyPathsForDiagDiff = keyPathsForDiagPlot.subList(1, keyPathsForDiagPlot.size() );
				//get the data by given key paths from diagData
				diagDataForPlot = configureParametersData( diagData, keyPathsForDiagPlot );
				//filter the data by type and parameter name
				diagPositions.put( types[0], filDiagData( types[0], paramsForBPM[0], diagDataForPlot.get( keyPathsForDiagPlot.get(0) ) ) );				
				int upLimit = keyPathsForDiagPlot.size();
				//plot the data
				for ( int index = 1; index < upLimit; index++ ) {
					List<Double> values = diagDataForPlot.get( keyPathsForDiagPlot.get( index ) );
					if ( xSelectionCheckbox.isSelected() ) {						
						List<Double> data = filDiagData( types[0], paramsForBPM[0], values );
						List<List<Double>> validData = filValidData( diagPositions.get( types[0] ), data );
						_machineSimulatorPlot.showPlot( validData.get(0), validData.get(1),
								(upLimit-1-index)+types[0]+paramsForBPM[0], LEGEND_NAME[upLimit-1-index] );
					}
					if ( ySelectionCheckbox.isSelected() ) {
						List<Double> data = filDiagData( types[0], paramsForBPM[1], values );
						List<List<Double>> validData = filValidData( diagPositions.get( types[0] ), data );					
						_machineSimulatorPlot.showPlot( validData.get(0), validData.get(1),
								(upLimit-1-index)+types[0]+paramsForBPM[1], LEGEND_NAME[upLimit-1-index] );
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
//						for( int index = 0; index<keyPathsForDiagDiff.size(); index++ ){				
							final String legName = LEGEND_NAME[0]+" - "+LEGEND_NAME[1];
							List<Double> oldValues = diagDataForPlot.get( keyPathsForDiagDiff.get( 0 ) );
							List<Double> newValues = diagDataForPlot.get( keyPathsForDiagDiff.get( 1 ) );
							if ( xSelectionCheckbox.isSelected() ) {
								List<List<Double>> newFilData = filValidData( diagPositions.get( types[0] ), filDiagData( types[0], paramsForBPM[0], newValues ) ) ;
								List<List<Double>> oldFilData= filValidData( diagPositions.get( types[0] ), filDiagData(types[0], paramsForBPM[0], oldValues ) );
								List<List<Double>> diffData = calculateDiff( newFilData, oldFilData );
								_machineSimulatorPlot.showPlot( diffData.get( 0 ), diffData.get( 1 ), types[0]+paramsForBPM[0], legName );
							}
							if ( ySelectionCheckbox.isSelected() ) {
								List<List<Double>> newFilData = filValidData( diagPositions.get( types[0] ), filDiagData(types[0], paramsForBPM[1], newValues ) ) ;
								List<List<Double>> oldFilData = filValidData( diagPositions.get( types[0] ), filDiagData(types[0], paramsForBPM[1], oldValues ) );
								List<List<Double>> diffData = calculateDiff( newFilData, oldFilData );
								_machineSimulatorPlot.showPlot( diffData.get( 0 ), diffData.get( 1 ), types[0]+paramsForBPM[1], legName );
							}
//						}
					}
					//show difference between two simulation results
					for( int index = 0; index<keyPathsForSimDiff.size(); index++ ){
						final String legName = LEGEND_NAME[0]+" - "+LEGEND_NAME[1];
						String keySimOld = keyPathsForSimDiff.get( index );
						String keySim = keyPathsForSimDiff.get( index+1 );
						List<List<Double>> dataOld = filValidData( _positions, simDataForPlot.get( keySimOld ) );
						List<List<Double>> dataNew = filValidData( _positions, simDataForPlot.get( keySim ) );
						List<List<Double>> diffData = calculateDiff( dataNew, dataOld ); 
						_machineSimulatorPlot.showPlot( diffData.get( 0 ), diffData.get( 1 ) , keySim, legName );
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
				if ( bpmCheckbox.isSelected() ) {
					DIAG_RECORD_TABLE_MODEL.setRecords( diagData );
					DIAG_HANDLER.actionPerformed( null );
				}
				else DIAG_RECORD_TABLE_MODEL.setRecords( null );
				
				if ( _sequence != null ) twissParametersPlot.setName( _sequence.getId() );
				if( parameterKeyPathsForTable.length > 0 && simulations[0] != null ){
					simDataForPlot = configureParametersData(  MODEL.getSimulationRecords( simulations[0], simulations[1] ), parameterKeyPathsForTableList );
					_positions = simulations[0].getAllPositions();
					for( final String parameterKey:parameterKeyPathsForTable ){
						final String legName = parameterKey.contains("old") ? LEGEND_NAME[1] : LEGEND_NAME[0];
						List<List<Double>> filData = filValidData( _positions, simDataForPlot.get( parameterKey ) );
						_machineSimulatorPlot.showPlot( filData.get( 0 ), filData.get( 1 ), parameterKey, legName );					
					}
				}
				if ( showDifference.isSelected() ) SHOW_DIFFERENCE_HANDELER.actionPerformed(null);
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
	
	/**make the table of history records*/
	private void makeHistoryRecordsView( final WindowReference windowReference ) {
		
        final JTextField filterField = (JTextField)windowReference.getView( "History Table Filter Field" );
        final JButton clearButton = (JButton)windowReference.getView( "History Filter Clear Button" );
        clearButton.addActionListener( event -> {
        	filterField.setText( "" );
        });
  
        final JTable historyRecordTable = (JTable)windowReference.getView( "History Record Table" );
        historyRecordTable.setModel( HISTORY_RECORD_TABLE_MODEL );
        
        HISTORY_RECORD_TABLE_MODEL.setInputFilterComponent( filterField );
        HISTORY_RECORD_TABLE_MODEL.setMatchingKeyPaths( "sequence.id", "recordName" );
        HISTORY_RECORD_TABLE_MODEL.setColumnName( "checkState", "Compare" );
        HISTORY_RECORD_TABLE_MODEL.setColumnName( "sequence.id", "Sequence" );
        HISTORY_RECORD_TABLE_MODEL.setColumnName( "dateTime", "Time" );
        
        HISTORY_RECORD_TABLE_MODEL.setColumnClassForKeyPaths( Boolean.class, "checkState" );
        HISTORY_RECORD_TABLE_MODEL.setKeyPaths( "checkState", "sequence.id", "dateTime", "recordName");
        HISTORY_RECORD_TABLE_MODEL.setColumnEditable( "recordName", true );
        HISTORY_RECORD_TABLE_MODEL.setColumnEditable( "checkState", true );
        
		//configure the remove button of the history record view
		final JButton removeButton = (JButton)windowReference.getView( "Remove Button" );
		removeButton.addActionListener( event -> {		
			List<SimulationHistoryRecord> records = MODEL.getSimulationHistoryRecords();
			int removed = 0;
			int[] selRows = historyRecordTable.getSelectedRows();
			for( int index = 0; index < selRows.length; index++ ){
				records.get( selRows[index]-removed ).setCheckState( false );
				records.remove( selRows[index]-removed );
				removed++;
			}
			if ( records.size() == 0 ) PARAM_HISTORY_TABLE_MODEL.setRecords( null );
			HISTORY_RECORD_TABLE_MODEL.setRecords( records );
		});
		
		//configure the select all button
		final JButton selectAllButton = (JButton)windowReference.getView( "Check All" );
		selectAllButton.addActionListener( event -> {
			final int rowCount = HISTORY_RECORD_TABLE_MODEL.getRowCount();
			for ( int index = 0; index < rowCount; index++){
				HISTORY_RECORD_TABLE_MODEL.getRecordAtRow( index ).setCheckState( true );
			}
			HISTORY_RECORD_TABLE_MODEL.fireTableRowsUpdated(0, rowCount );
		});
		
		//configure the unselect all button
		final JButton unselectAllButton = (JButton)windowReference.getView( "Uncheck All" );
		unselectAllButton.addActionListener( event -> {
			final int rowCount = HISTORY_RECORD_TABLE_MODEL.getRowCount();
			for ( int index = 0; index < rowCount; index++){
				HISTORY_RECORD_TABLE_MODEL.getRecordAtRow( index ).setCheckState( false );
			}
			HISTORY_RECORD_TABLE_MODEL.fireTableRowsUpdated(0, rowCount );
		});
	}
	
	/**make the table of new run parameters*/
	private void makeNewRunParamView( final WindowReference windowReference ) {
		
		nodePropRecordsForScan = new ArrayList<NodePropertyRecord>();
        final JTextField filterField = (JTextField)windowReference.getView( "New Run Param Filter Field" );
        final JButton clearButton = (JButton)windowReference.getView( "New Run Param Filter Clear" );
        clearButton.addActionListener( event -> {
        	filterField.setText( "" );
        });
        
		//configure the clear test button
		final JButton clearTestButton = (JButton)windowReference.getView( "Clear Test" );
		clearTestButton.addActionListener( event -> {
			List<NodePropertyRecord> records = NEW_PARAMETERS_TABLE_MODEL.getRowRecords();
			if ( records != null ) {
				for ( final NodePropertyRecord record : records ) {
					record.setTestValue( Double.NaN );
				}				
				NEW_PARAMETERS_TABLE_MODEL.fireTableRowsUpdated( 0, records.size()-1 );
			}

		});
		
		
		//configure the scan button
		final JButton scanButton = (JButton)windowReference.getView( "Scan Button" );
		 scanButton.addActionListener( event -> {
			 nodePropRecordsForScan.clear();
				for ( final NodePropertyRecord record : nodePropertyRecords ) {
					if ( record.getCheckState() ) nodePropRecordsForScan.add( record );
				}	
				if ( nodePropertyRecords != null && nodePropRecordsForScan.size() != 0 ) {				
					List<Double[]> scanSource = extractScanSource( nodePropRecordsForScan );
					if ( scanSource.size() != 0 ) {
						ScanAlgorithm<Double> algorithm = new ScanAlgorithm<Double>( scanSource, Double.class );
						boolean scanState = algorithm.generateScanSpots();
						if ( scanState ) {
							startScan( windowReference.getWindow(), algorithm );
						}
						else JOptionPane.showMessageDialog( windowReference.getWindow(),
								"Exceed the limit : " + ScanAlgorithm.MAXIMUM_CAPACITY, "Warning!", JOptionPane.PLAIN_MESSAGE );
					}
					else JOptionPane.showMessageDialog( windowReference.getWindow(),
							"Scan failed !\n Please check the settings!", "Warning!", JOptionPane.ERROR_MESSAGE );
					
				}
				else JOptionPane.showMessageDialog( windowReference.getWindow(), 
						"You need to select a node first!", "Warning!", JOptionPane.PLAIN_MESSAGE);
		 });
	
		//configure the uncheck button
		final JButton uncheckButton = (JButton)windowReference.getView( "Uncheck Scan Button" );
		uncheckButton.addActionListener( event -> {
			List<NodePropertyRecord> records = NEW_PARAMETERS_TABLE_MODEL.getRowRecords();
			if ( records != null ) {
				for ( final NodePropertyRecord record : records ) {
					record.setCheckState( false );
				}				
				NEW_PARAMETERS_TABLE_MODEL.fireTableRowsUpdated( 0, records.size()-1 );
			}
		});
		
		
		
	     final JTable newRunParamTable = (JTable)windowReference.getView( "New Run Parameters Table" );
	     newRunParamTable.setModel( NEW_PARAMETERS_TABLE_MODEL );
	     
			//configure the clear setting button
			final JButton clearSetting = (JButton)windowReference.getView( "Clear Setting" );
			clearSetting.addActionListener( event -> {
				int[] selRows = newRunParamTable.getSelectedRows();
				for ( int index = 0; index < selRows.length; index++ ) {
					NodePropertyRecord record = NEW_PARAMETERS_TABLE_MODEL.getRecordAtRow( selRows[index] );
					record.setCheckState( false );
					record.setScanStartValue( Double.NaN );
					record.setScanEndValue( Double.NaN );
					record.setSteps( 0 );
				}
			});
	        
	     //set the column name of the sequence table
	     NEW_PARAMETERS_TABLE_MODEL.setColumnName( "acceleratorNode.id", "Node" );
	     NEW_PARAMETERS_TABLE_MODEL.setColumnName( "propertyName", "Property" );
	     NEW_PARAMETERS_TABLE_MODEL.setColumnName( "checkState", "Scan" );
	        
	     //set the filter field for sequence table
	     NEW_PARAMETERS_TABLE_MODEL.setInputFilterComponent( filterField );
	     NEW_PARAMETERS_TABLE_MODEL.setMatchingKeyPaths( "acceleratorNode.id" );
	         
	     //configure the sequence table model
		  NEW_PARAMETERS_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, 
				  "designValue", "liveValue", "testValue", "scanStartValue", "scanEndValue" );
		  NEW_PARAMETERS_TABLE_MODEL.setColumnClass( "checkState", Boolean.class );
		  NEW_PARAMETERS_TABLE_MODEL.setColumnClass( "steps", Integer.class );
		  NEW_PARAMETERS_TABLE_MODEL.setKeyPaths( "acceleratorNode.id", 
				  "propertyName", "designValue", "liveValue", "testValue", "checkState", "scanStartValue", "scanEndValue", "steps" );
		  NEW_PARAMETERS_TABLE_MODEL.setColumnEditable( "testValue", true );
		  NEW_PARAMETERS_TABLE_MODEL.setColumnEditable( "checkState", true );
		  NEW_PARAMETERS_TABLE_MODEL.setColumnEditKeyPath( "scanStartValue", "checkState" );
		  NEW_PARAMETERS_TABLE_MODEL.setColumnEditKeyPath( "scanEndValue", "checkState" );
		  NEW_PARAMETERS_TABLE_MODEL.setColumnEditKeyPath( "steps", "checkState" );
	}
	
	/**make the table of parameter history records*/
	private void makeParamHistoryView( final WindowReference windowReference ) {
		
		  final JTextField filterField = (JTextField)windowReference.getView( "Param History Filter Field" );
	        final JButton clearButton = (JButton)windowReference.getView( "Param History Filter Clear" );
	        clearButton.addActionListener( event -> {
	        	filterField.setText( "" );
	        });
		  
        final JTable paramHistoryDataTable = (JTable)windowReference.getView( "Param History Data Table" );
        paramHistoryDataTable.setModel( PARAM_HISTORY_TABLE_MODEL );
        
        PARAM_HISTORY_TABLE_MODEL.setColumnName( "nodeId" , "Node" );
        PARAM_HISTORY_TABLE_MODEL.setColumnName( "propertyName", "Property" );
        
        PARAM_HISTORY_TABLE_MODEL.setInputFilterComponent( filterField );
        PARAM_HISTORY_TABLE_MODEL.setMatchingKeyPaths( "nodeId" );
	}
	
	/**make the tables of diagnostic live values and history records */
	private void makeDiagView( final WindowReference windowReference ) {
		
        final JTextField filterField = (JTextField)windowReference.getView( "Diagnostics History Filter Field" );
        final JButton clearButton = (JButton)windowReference.getView( "Diagnostics History Filter Clear" );
        clearButton.addActionListener( event -> {
        	filterField.setText( "" );
        });
        
        final JTable diagLiveTable = (JTable)windowReference.getView( "Diag Live Table" );
        diagLiveTable.setModel( DIAG_LIVE_TABLE_MODEL );
        
        final JButton disableButton = (JButton)windowReference.getView( "Diagnostic Disable Button" );
        disableButton.addActionListener( event -> {
        	final int rowCount = DIAG_LIVE_TABLE_MODEL.getRowCount();
        	for ( int row = 0; row < rowCount; row++ ) {
        		DIAG_LIVE_TABLE_MODEL.getRecordAtRow( row ).setCheckState( false );
        	}
        	DIAG_LIVE_TABLE_MODEL.fireTableRowsUpdated( 0, rowCount );
        });
        
        final JButton enableButton = (JButton)windowReference.getView( "Diagnostic Enable Button" );
        enableButton.addActionListener( event -> {
        	final int rowCount = DIAG_LIVE_TABLE_MODEL.getRowCount();
        	for ( int row = 0; row < rowCount; row++ ) {
        		DIAG_LIVE_TABLE_MODEL.getRecordAtRow( row ).setCheckState( true );
        	}
        	DIAG_LIVE_TABLE_MODEL.fireTableRowsUpdated( 0, rowCount );
        });
        
        DIAG_LIVE_TABLE_MODEL.setInputFilterComponent( filterField );
        DIAG_LIVE_TABLE_MODEL.setMatchingKeyPaths( "node.id" );
        
        DIAG_LIVE_TABLE_MODEL.setColumnName( "checkState", "use" );
        DIAG_LIVE_TABLE_MODEL.setColumnName( "node.id",  "BPM" );
        DIAG_LIVE_TABLE_MODEL.setColumnName( "valueX", "xAvg" );
        DIAG_LIVE_TABLE_MODEL.setColumnName( "valueY", "yAvg" );
        
		  final String[] keyPathsForDiagLive = {"checkState", "node.id", "position", "valueX", "valueY"};
        
        DIAG_LIVE_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, "position", "valueX", "valueY");
        DIAG_LIVE_TABLE_MODEL.setColumnClass( "checkState", Boolean.class );
        DIAG_LIVE_TABLE_MODEL.setColumnEditable( "checkState", true );
        DIAG_LIVE_TABLE_MODEL.setKeyPaths(keyPathsForDiagLive);
        
        //configure the diagnostic record table
        final JTable diagRecordTable = (JTable)windowReference.getView( "Diag Record Table");
        diagRecordTable.setModel( DIAG_RECORD_TABLE_MODEL );
        
        DIAG_RECORD_TABLE_MODEL.setInputFilterComponent( filterField );
        DIAG_RECORD_TABLE_MODEL.setMatchingKeyPaths( "node.id" );
        DIAG_RECORD_TABLE_MODEL.setColumnName( "node.id", "BPM" );
        
        DIAG_RECORD_TABLE_MODEL.setColumnClass( "position", Double.class );
		
	}
	
	/**make the table of simulation results*/
	private void makeStatesTableView( final WindowReference windowReference ) {
		
        final JTextField filterField = (JTextField)windowReference.getView( "States Table Filter Field" );
        final JButton clearButton = (JButton)windowReference.getView( "States Table Filter Clear" );
        clearButton.addActionListener( event -> {
        	filterField.setText( "" );
        });

	     final JTable statesTable = (JTable)windowReference.getView( "States Table" );
	     statesTable.setModel( STATES_TABLE_MODEL );
	     
       STATES_TABLE_MODEL.setColumnName( "elementID", "Element" );

       STATES_TABLE_MODEL.setInputFilterComponent( filterField );
       STATES_TABLE_MODEL.setMatchingKeyPaths( "elementID" );
       
		  STATES_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, "position");
	}
	
	
	/**change simulation history records*/
	public void changeSimHistoryRecords ( final List<SimulationHistoryRecord> simHistoryRecords ) {
		HISTORY_RECORD_TABLE_MODEL.setRecords( simHistoryRecords );
	}
	
  /** get the selected parameters' data from the specified records
    * @param records the specified records which hold the data
    * @param keyPaths specifies the array of key paths to get the data to plot
    */ 
    private <T> Map<String, List<Double>> configureParametersData( final List<T> records, final List<String> keyPaths ){
      final Map<String, List<Double>> data = new HashMap<String, List<Double>>();     
    	for( final String keyPath:keyPaths ){
    		data.put( keyPath, new ArrayList<Double>( records.size() ) );
    		for( final T record:records ){
    			data.get(keyPath).add( (Double)KEY_VALUE_ADAPTOR.valueForKeyPath( record,keyPath ) );
    		}
    	}   	
    	return data;
    }
    
    /**
     * calculate the difference of two records' data
     * @param data1 the first data, include positions and values
     * @param data2 the second data, include positions and values
     * @return the difference data
     */
    private List<List<Double>> calculateDiff( final List<List<Double>> data1, final List<List<Double>> data2 ) {
    	List<List<Double>> diffData = new ArrayList<List<Double>>();
    	List<Double> critPos, othPos ;
    	if ( data1.get(0).size() <= data2.get(0).size() ) {
    		critPos = data1.get( 0 );
    		othPos = data2.get( 0 );
    	}
    	else {
    		critPos = data2.get( 0 );
    		othPos = data1.get( 0 );
    	}
    	List<Double> calculValues = new ArrayList<>( critPos.size() );
    	int diffNumber = 0;
    	for ( int index = 0; index< critPos.size(); ) {
    		if ( critPos.get( index ) == othPos.get( index + diffNumber ) ) {
    			calculValues.add(data1.get(1).get( index ) - data2.get(1).get( index ) );
    			index++;
    		}
    		else diffNumber++;
    	}
    	diffData.add( critPos );
    	diffData.add(calculValues);
    	return diffData;
    }
    
    /**
     * remove the invalid data and it's corresponding position
     * @param pos the positions
     * @param dataForPlot dataForPlot for filtering
     * @return the filtered data
     */
    private List<List<Double>> filValidData ( final List<Double> pos, final List<Double> dataForPlot ) {
    	List<List<Double>> filteredData = new ArrayList<List<Double>>();
    	List<Double> filPos = new ArrayList<Double>( pos );
    	List<Double> filData = new ArrayList<Double>( dataForPlot );
    	int removed = 0;
    	if ( pos.size() == dataForPlot.size() ) {
    		for ( int dataIndex = 0; dataIndex < dataForPlot.size(); dataIndex++ ){
    			if ( Double.isNaN( dataForPlot.get( dataIndex ) ) ) {
    				//TODO:maybe we can add other filter conditions in future
    				filPos.remove( dataIndex - removed );
    				filData.remove( dataIndex - removed );
    				removed ++;
    			}
    		}
    		filteredData.add( filPos );
    		filteredData.add( filData );
    	}

    	return filteredData;
    }
    
    /**
     * filter the diagnostic data by the node type, parameter
     * @param type the type of one node
     * @param param one parameter of one node
     * @param dataForFil the data source
     * @return the filtered values
     */
    private List<Double> filDiagData( final String type, final String param, final List<Double> dataForFil ) {
    	List<Double> filData =  new ArrayList<Double>();
    	for ( int index = 0; index< diagData.size(); index++ ) {
    		DiagnosticRecord record = diagData.get( index );
    		if ( record.getNode().getType().equals( type ) && record.getValueName().equals(param) ) {
    			filData.add( dataForFil.get( index ) );
    		}
    	}
    	return filData;
    }
    
    /**
     * start the scanning
     * @param window the parent window of confirm dialog
     * @param algorithm the algorithm to generate all the scan spots
     */
    private void startScan( final Window window, final ScanAlgorithm<Double> algorithm ) {
		StringBuilder confirmation = new StringBuilder();
		List<Double> testValues = new ArrayList<>( nodePropRecordsForScan.size() );
		for( final NodePropertyRecord record : nodePropRecordsForScan ) {
			testValues.add( record.getTestValue() );
			String nodeinform = record.getAcceleratorNode().getId() + " . " + record.getPropertyName() + " : "
					+ " Range :( " + record.getScanStartValue() + ")-(" + record.getScanEndValue() + ") Steps : " + record.getSteps() + "\n";
			confirmation.append( nodeinform );
		}
		confirmation.append( "Get results : " + algorithm.getScanSteps() );
		int confirm = JOptionPane.showConfirmDialog( window,
				confirmation, "Scan Confirmation", JOptionPane.YES_NO_OPTION );
		if ( confirm == JOptionPane.YES_OPTION ) {
			scanNumber++;
			List<Double[]> scanSpots = algorithm.getScanSpots();
			List<int[]> scanCmbntnIndexes = algorithm.getScanSpotsIndex();
			for ( int spotIndex = 0; spotIndex < scanSpots.size(); spotIndex++ ) {
				Double[] scanSpot = scanSpots.get( spotIndex );
				int[] cmbntnIndex = scanCmbntnIndexes.get( spotIndex );
				for ( int index = 0; index < scanSpot.length; index++ ) {
					nodePropRecordsForScan.get( index ).setTestValue( scanSpot[ index ] );
				}
				
				//build the record name
				StringBuilder nameBuilder = new StringBuilder();
				nameBuilder.append(  "Scan " + scanNumber + ":[" );
				for ( int i : cmbntnIndex ) {
					nameBuilder.append( i + "." );
				}
				nameBuilder.replace( nameBuilder.length()-1, nameBuilder.length(), "]" );
				runModel( window, nameBuilder.toString() );;
			}
			//restore the test values
			for ( int nodeIndex = 0; nodeIndex < nodePropRecordsForScan.size(); nodeIndex++ ) {
				nodePropRecordsForScan.get( nodeIndex ).setTestValue( testValues.get( nodeIndex ) );
			}
		}
		
	}
    
    /**
     * extract the scan source from the specified node records
     * @param records the specified node property records
     * @return the scan source
     */
    private List<Double[]> extractScanSource ( final List<NodePropertyRecord> records ) {
    	List<Double[]> scanSource = new ArrayList<Double[]>();
    	
    	for ( int nodeIndex = 0; nodeIndex < records.size(); nodeIndex++ ) {
    		Double startValue = records.get( nodeIndex ).getScanStartValue();
    		Double endValue = records.get( nodeIndex ).getScanEndValue();
    		int steps = ( startValue == endValue ) ? 1 : records.get( nodeIndex ).getSteps();
    		if ( Double.isNaN( startValue ) || Double.isNaN( endValue ) || steps <= 0 ) {
    			scanSource.clear();
    			return scanSource;
    		}
    		Double[] nodeData = new Double[steps];
    		int dataSize = nodeData.length;
    		for ( int index = 0; index < dataSize   ; index++ ) {
    			nodeData[index] = ( steps == 1 ) ? startValue : startValue + index*( endValue - startValue )/(steps-1);
    		}
    		scanSource.add( nodeData );
    	}
    	
    	
    	return scanSource;
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
    		
    		DIAG_LIVE_TABLE_MODEL.setRecords( model.getDiagConfig().getDiagnosticAgents() );
   		VALUE_SYNC_TIME.startNowWithInterval( _syncPeriod, 0 );
    	}
    	
    	if ( model.getSimulationHistoryRecords().size() == 0 ) refresh.actionPerformed( null );
    	//unselect all the history records when changing the sequence 
		for ( int index = 0; index < model.getSimulationHistoryRecords().size();index++){
			model.getSimulationHistoryRecords().get( index ).setCheckState( false );
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

	/**event indicates that the history records check state changed*/
	public void historyRecordCheckStateChanged( final List<NodePropertyHistoryRecord> nodePropertyHistoryRecords,
			final Map<Date, String> columnName, final List<DiagnosticRecord> dRecords, final AcceleratorSeq seq ) {
		
		int columnNumber = (columnName != null ) ? columnName.size() : 0;
		String[] historyDataKeyPaths = new String[columnNumber+2];
        historyDataKeyPaths[0] = "nodeId";
        historyDataKeyPaths[1] = "propertyName";
		keyPathsForDiagRecord = new String[columnNumber+3];
		   keyPathsForDiagRecord[0] = "node.id";
		   keyPathsForDiagRecord[1] = "valueName";
		   keyPathsForDiagRecord[2] = "position";		
		if ( nodePropertyHistoryRecords != null && columnName != null ) {
			String[] names = new String[columnNumber];
			columnName.values().toArray( names );
			for ( int index = 0;index<columnNumber; index++ ){
				historyDataKeyPaths[index+2] = "values."+index;
				keyPathsForDiagRecord[index+3] = "values."+index;
				PARAM_HISTORY_TABLE_MODEL.setColumnClass( "values."+index, Double.class );
				PARAM_HISTORY_TABLE_MODEL.setColumnName( "values."+index, names[index]);
				
				DIAG_RECORD_TABLE_MODEL.setColumnClass( "values."+index, Double.class );
				DIAG_RECORD_TABLE_MODEL.setColumnName( "values."+index, names[index] );
			}
			
			PARAM_HISTORY_TABLE_MODEL.setKeyPaths( historyDataKeyPaths );
			PARAM_HISTORY_TABLE_MODEL.setRecords( nodePropertyHistoryRecords );
			
			DIAG_RECORD_TABLE_MODEL.setKeyPaths( keyPathsForDiagRecord );
		}
		else {
			PARAM_HISTORY_TABLE_MODEL.setRecords( null );
		}

		_sequence = seq;
		
		diagData = dRecords;

		refresh.actionPerformed(null);
		
	}
	
	
}



