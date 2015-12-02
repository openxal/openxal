/**
 *
 */
package xal.app.machinesimulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
     /** main model */
     final private MachineModel MODEL;
     /** key value adaptor to get the twiss value from a record for the specified key path */
     final private KeyValueAdaptor KEY_VALUE_ADAPTOR;
     /**a map array from parameter's key to plot data list*/
     final private HashMap<String, List<Double>> PLOT_DATA;
     /**the scalar parameters*/
     final private List<ScalarParameter> SCALAR_PARAMETERS;
     /**the vector parameters*/
     final private List<VectorParameter> VECTOR_PARAMETERS;
     /**timer to sync the live value*/
     final private DispatchTimer VALUE_SYNC_TIME;
     /**the legend name*/
     final private String[] LEGEND_NAME;
     /**the sequence*/
     private AcceleratorSeq _sequence;
     /**sync period in milliseconds*/
     private long _syncPeriod;
     /** the plotter*/
     private MachineSimulatorTwissPlot _machineSimulatorTwissPlot;
     /**the list of NodePropertyRecord*/
     private List<NodePropertyRecord> nodePropertyRecords;
     /** the position list of elements*/
     private List<Double> _positions;
     /**the key paths for history data table model*/
     private String[] historyDataKeyPaths;
     /**the key paths for show difference between two simulation results*/
     private List<String> keyPathsForDifference;
     /**refresh action*/
     private ActionListener refresh;



	/**constructor */
	public  MachineSimulatorController(final MachineSimulatorDocument document,final WindowReference windowReference) {
		
		STATES_TABLE_MODEL = new KeyValueFilteredTableModel<MachineSimulationHistoryRecord>();
		
		NEW_PARAMETERS_TABLE_MODEL = new KeyValueFilteredTableModel<NodePropertyRecord>();
		HISTORY_RECORD_TABLE_MODEL = new KeyValueFilteredTableModel<SimulationHistoryRecord>();
		HISTORY_DATA_TABLE_MODEL = new KeyValueFilteredTableModel<NodePropertyHistoryRecord>();
		
		LEGEND_NAME = new String[2];
		
		PLOT_DATA = new HashMap<String,List<Double>>();
		KEY_VALUE_ADAPTOR= new KeyValueAdaptor();
		
		VALUE_SYNC_TIME = DispatchTimer.getCoalescingInstance( DispatchQueue.getMainQueue(), getLiveValueSynchronizer() );
		// set the default sync period to 1 second
		_syncPeriod = 1000;
		
      // initialize the model here
      MODEL = document.getModel();
      MODEL.addMachineModelListener(this);
		
		SCALAR_PARAMETERS=new ArrayList<ScalarParameter>();
		SCALAR_PARAMETERS.add(new ScalarParameter("Kinetic Energy", "probeState.kineticEnergy"));
		
		VECTOR_PARAMETERS=new ArrayList<VectorParameter>();
		VECTOR_PARAMETERS.add(new VectorParameter("Beta","beta","twissParameters", "beta"));
		VECTOR_PARAMETERS.add(new VectorParameter("Alpha","alpha","twissParameters", "alpha"));
		VECTOR_PARAMETERS.add(new VectorParameter("Gamma","gamma","twissParameters", "gamma"));
		VECTOR_PARAMETERS.add(new VectorParameter("Emittance","epsilon","twissParameters", "emittance"));
		VECTOR_PARAMETERS.add(new VectorParameter("EnvelopeRadius","sigma","twissParameters", "envelopeRadius"));
		VECTOR_PARAMETERS.add(new VectorParameter("BetatronPhase","phi","betatronPhase"));
        
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

        final JCheckBox betaCheckbox = (JCheckBox)windowReference.getView( "Beta Checkbox" );
        final JCheckBox alphaCheckbox = (JCheckBox)windowReference.getView( "Alpha Checkbox" );
        final JCheckBox gammaCheckbox = (JCheckBox)windowReference.getView( "Gamma Checkbox" );
        final JCheckBox emittanceCheckbox = (JCheckBox)windowReference.getView( "Emittance Checkbox" );
        final JCheckBox beamSizeCheckbox = (JCheckBox)windowReference.getView( "Beam Size Checkbox" );
        final JCheckBox betatronPhaseCheckbox = (JCheckBox)windowReference.getView( "Betatron Phase Checkbox" );
       
        //the show difference checkbox in plot view
		  final JCheckBox showDifference = (JCheckBox)windowReference.getView( "Show Difference" );
        
/***************configure the history record view*****************************************/
        //configure history record table
        final JTable historyRecordTable = (JTable)windowReference.getView( "History Record Table" );
        historyRecordTable.setModel(HISTORY_RECORD_TABLE_MODEL);
        
        HISTORY_RECORD_TABLE_MODEL.setColumnName("selectState", "Compare");
        HISTORY_RECORD_TABLE_MODEL.setColumnName("sequence.id", "Sequence" );
        HISTORY_RECORD_TABLE_MODEL.setColumnName("dateTime", "Time" );
        HISTORY_RECORD_TABLE_MODEL.setColumnName("recordName", "Recordname" );
        
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
        
         historyDataKeyPaths = new String[2];
        historyDataKeyPaths[0] = "acceleratorNode.id";
        historyDataKeyPaths[1] = "propertyName";
        
/******************configure the sequence table view*************************************/
        
        final JTable sequenceTable = (JTable)windowReference.getView( "New Run Parameters" );
        sequenceTable.setModel( NEW_PARAMETERS_TABLE_MODEL ); 
        
        //set the column name of the sequence table
        NEW_PARAMETERS_TABLE_MODEL.setColumnName( "acceleratorNode.id", "Node" );
        NEW_PARAMETERS_TABLE_MODEL.setColumnName( "propertyName", "Property" );
        NEW_PARAMETERS_TABLE_MODEL.setColumnName( "designValue", "Design Value" );
        NEW_PARAMETERS_TABLE_MODEL.setColumnName( "liveValue", "Live Value" );
        NEW_PARAMETERS_TABLE_MODEL.setColumnName("testValue", "Test Value");
        
        //set the filter field for sequence table
        NEW_PARAMETERS_TABLE_MODEL.setInputFilterComponent(statesTableFilterField);
        NEW_PARAMETERS_TABLE_MODEL.setMatchingKeyPaths( "acceleratorNode.id" );
         
        //configure the sequence table model
		  NEW_PARAMETERS_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, "designValue", "liveValue", "testValue" );
		  NEW_PARAMETERS_TABLE_MODEL.setKeyPaths( "acceleratorNode.id", "propertyName", "designValue", "liveValue", "testValue" );
		  NEW_PARAMETERS_TABLE_MODEL.setColumnEditable( "testValue", true );

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
        final FunctionGraphsJPanel twissParametersPlot = ( FunctionGraphsJPanel ) windowReference.getView( "States Plot" );
        _machineSimulatorTwissPlot=new MachineSimulatorTwissPlot( twissParametersPlot,SCALAR_PARAMETERS,VECTOR_PARAMETERS );

		//synoptic display of nodes
		final Box synopticBox = ( Box )windowReference.getView( "SynopticContainer" );
		final XALSynopticPanel xalSynopticPanel = FunctionGraphsXALSynopticAdaptor.assignXALSynopticViewTo( twissParametersPlot, _sequence );
		synopticBox.removeAll();
		synopticBox.add( xalSynopticPanel );
		synopticBox.validate();

/***************************Check boxes action**************************************/
		
		final ActionListener PARAMETER_HANDLER = new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				//get the first two simulations which are selected 
				MachineSimulation[] simulations = MODEL.getHistorySimulation( _sequence );
				//configure the column name of states table
		        for(final ScalarParameter scalarParameter:SCALAR_PARAMETERS){
		            STATES_TABLE_MODEL.setColumnName(scalarParameter.getKeyPath(), scalarParameter.getSymbol()+" - "+LEGEND_NAME[0] );
		            
		            STATES_TABLE_MODEL.setColumnName("old."+scalarParameter.getKeyPath(), scalarParameter.getSymbol()+" - "+LEGEND_NAME[1] );
		           }
		        for(final VectorParameter vectorParameter:VECTOR_PARAMETERS){
		        	   String symbX = vectorParameter.getSymbolForX();
		        	   String symbY = vectorParameter.getSymbolForY();
		        	   String symbZ = vectorParameter.getSymbolForZ();
		        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForX(),
		        			   symbX.substring(0, symbX.length()-6)+" - "+LEGEND_NAME[0]+"<html>" );
		        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForY(), 
		        			   symbY.substring(0, symbY.length()-6)+" - "+LEGEND_NAME[0]+"<html>" );
		        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForZ(), 
		        			   symbZ.substring(0, symbZ.length()-6)+" - "+LEGEND_NAME[0]+"<html>" );
		        	   
		        	   STATES_TABLE_MODEL.setColumnName("old."+vectorParameter.getKeyPathForX(), 
		        			   symbX.substring(0, symbX.length()-6)+" - "+LEGEND_NAME[1]+"<html>" );
		        	   STATES_TABLE_MODEL.setColumnName("old."+vectorParameter.getKeyPathForY(), 
		        			   symbY.substring(0, symbY.length()-6)+" - "+LEGEND_NAME[1]+"<html>" );
		        	   STATES_TABLE_MODEL.setColumnName("old."+vectorParameter.getKeyPathForZ(), 
		        			   symbZ.substring(0, symbZ.length()-6)+" - "+LEGEND_NAME[1]+"<html>" );
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
					if ( betaCheckbox.isSelected() ) parameterKeyPathsList.add( VECTOR_PARAMETERS.get(0).getKeyPathToArray().get(plane) );
					if ( alphaCheckbox.isSelected() ) parameterKeyPathsList.add( VECTOR_PARAMETERS.get(1).getKeyPathToArray().get(plane) );
					if ( gammaCheckbox.isSelected() )  parameterKeyPathsList.add( VECTOR_PARAMETERS.get(2).getKeyPathToArray().get(plane) );
					if ( emittanceCheckbox.isSelected() ) parameterKeyPathsList.add( VECTOR_PARAMETERS.get(3).getKeyPathToArray().get(plane) );
					if ( beamSizeCheckbox.isSelected() )  parameterKeyPathsList.add( VECTOR_PARAMETERS.get(4).getKeyPathToArray().get(plane) );
					if ( betatronPhaseCheckbox.isSelected() ) parameterKeyPathsList.add( VECTOR_PARAMETERS.get(5).getKeyPathToArray().get(plane) );
				}
				
				//create the combination keyPaths used for comparing new and old simulation results
				final List<String> combParameterKeyPathsList = new ArrayList<String>(2*parameterKeyPathsList.size());
				for( final String path:parameterKeyPathsList){
					combParameterKeyPathsList.add( path );
					combParameterKeyPathsList.add( "old."+path );
				}
				
				keyPathsForDifference = combParameterKeyPathsList;
				
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
				showDifference.setSelected(false);
				if( parameterKeyPathsForTable.length > 0 && simulations[0] != null ){
					configureParametersData(  MODEL.getSimulationRecords( simulations[0], simulations[1] ), parameterKeyPathsForTable );
					_positions = simulations[0].getAllPositions();
					for( final String parameterKey:parameterKeyPathsForTable ){
						final String legName = parameterKey.contains("old") ? LEGEND_NAME[1] : LEGEND_NAME[0]; 
						_machineSimulatorTwissPlot.showTwissPlot( _positions, PLOT_DATA.get(parameterKey), parameterKey, legName );
					}
				}
				xalSynopticPanel.setAcceleratorSequence( _sequence );
			}
		};

/*************************activate check boxes***************************************/
		
        kineticEnergyCheckbox.addActionListener( PARAMETER_HANDLER );

        xSelectionCheckbox.addActionListener( PARAMETER_HANDLER );
        ySelectionCheckbox.addActionListener( PARAMETER_HANDLER );
        zSelectionCheckbox.addActionListener( PARAMETER_HANDLER );

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
                	final MachineSimulation simulation = MODEL.runSimulation();
                  //set records and configure history data table
                  historyRecordSelectStateChanged( MODEL.getNodePropertyHistoryRecords().get(_sequence),
                		  MODEL.getColumnNames().get(_sequence), _sequence );
                	//set records of states table
                	STATES_TABLE_MODEL.setRecords( MODEL.getSimulationRecords(simulation, MODEL.getHistorySimulation( _sequence )[1] ) );
                	//set records for history record table
                  HISTORY_RECORD_TABLE_MODEL.setRecords(MODEL.getSimulationHistoryRecords());
                 
                  PARAMETER_HANDLER.actionPerformed( null );
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
		
		//configure the show difference check box of plot view
		final ActionListener SHOW_DIFFERENCE_HANDELER =  event -> {
			MachineSimulation[] simulations = MODEL.getHistorySimulation(_sequence);
			if ( showDifference.isSelected() && simulations[0] != null ){
				if ( simulations[1] != null ){
					twissParametersPlot.removeAllGraphData();	
					//Todo:caluate the difference and plot
					for( int index = 0; index<keyPathsForDifference.size(); index++ ){					
						final List<Double> newValues = PLOT_DATA.get( keyPathsForDifference.get( index ) );
						final List<Double> oldValues = PLOT_DATA.get( keyPathsForDifference.get( index+1 ) );
						final List<Double> differences = new ArrayList<Double>( newValues.size() );
						for( int valueIndex = 0;valueIndex<newValues.size();valueIndex++ ){							
							differences.add( newValues.get( valueIndex ) - oldValues.get( valueIndex ) );
						}
						final String legName = LEGEND_NAME[0]+" - "+LEGEND_NAME[1];
						_machineSimulatorTwissPlot.showTwissPlot( _positions, differences, keyPathsForDifference.get( index ), legName );
						index++;
					}
				}
				else {
					JOptionPane.showMessageDialog(windowReference.getWindow(),
					"You need to select another record to compare","Warning!",JOptionPane.PLAIN_MESSAGE);
					showDifference.setSelected(false);
				}
			}
			else {
				showDifference.setSelected(false);
				PARAMETER_HANDLER.actionPerformed(null);
			}
		};
		
		showDifference.addActionListener(SHOW_DIFFERENCE_HANDELER);
		
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
		};

    }
	
  /** get the selected parameters' data from simulation records
    * @param records the result of simulation
    * @param keyPaths specifies the array of key paths to get the data to plot
    */ 
    private <T> void configureParametersData( final List<T> records,final String[] keyPaths ){
      PLOT_DATA.clear();      
    	for( final String keyPath:keyPaths ){
    		PLOT_DATA.put( keyPath, new ArrayList<Double>( records.size() ) );
    		for( final T record:records ){
    			PLOT_DATA.get(keyPath).add( (Double)KEY_VALUE_ADAPTOR.valueForKeyPath( record,keyPath ) );
    		}
    	}   	
    }
    
    /**get a runnalbe that syncs the values */
    private Runnable getLiveValueSynchronizer(){
    	return () -> NEW_PARAMETERS_TABLE_MODEL.fireTableRowsUpdated( 0, NEW_PARAMETERS_TABLE_MODEL.getRowCount() - 1  ); 
    }

    /**event indicates that the sequence has changed*/
    public void modelSequenceChanged( final MachineModel model ) {
    	if( model.getSequence() != null ){
        	_sequence = model.getSequence();
    		nodePropertyRecords = model.getWhatIfConfiguration().getNodePropertyRecords();
    		NEW_PARAMETERS_TABLE_MODEL.setRecords( nodePropertyRecords );
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
			final Map<Date, String> columnName, final AcceleratorSeq seq ) {
		
		int columnNumber = columnName.size();		
		String[] name = new String[columnNumber];
		columnName.values().toArray( name );
		String[] valuePathList = new String[columnNumber];
		for ( int index = 0;index<columnNumber; index++ ){
			valuePathList[index] = "values."+index;
			HISTORY_DATA_TABLE_MODEL.setColumnName( "values."+index, name[index]);
		}
		
		String[] keyPaths = new String[historyDataKeyPaths.length+valuePathList.length];
		System.arraycopy(historyDataKeyPaths, 0, keyPaths, 0, historyDataKeyPaths.length);
		System.arraycopy(valuePathList, 0, keyPaths, historyDataKeyPaths.length, valuePathList.length);
		HISTORY_DATA_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, valuePathList );
		HISTORY_DATA_TABLE_MODEL.setKeyPaths(keyPaths);
		HISTORY_DATA_TABLE_MODEL.setRecords( nodePropertyHistoryRecords );
		
		_sequence = seq;

		refresh.actionPerformed(null);
		
	}
	
	
}



