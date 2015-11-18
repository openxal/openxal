/**
 *
 */
package xal.app.machinesimulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;

import xal.app.machinesimulator.MachineModel.SimulationHistoryRecord;
import xal.extension.bricks.WindowReference;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.smf.FunctionGraphsXALSynopticAdaptor;
import xal.extension.widgets.smf.XALSynopticPanel;
import xal.extension.widgets.swing.KeyValueFilteredTableModel;
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
     final private KeyValueFilteredTableModel<NodePropertyRecord> SEQUENCE_TABLE_MODEL;
     /**history record table model*/
     final private KeyValueFilteredTableModel<SimulationHistoryRecord> HISTORY_RECORD_TABLE_MODEL;
     /**history data table model*/
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
     /**all the modelinputs variables*/
     /**sync period in milliseconds*/
     private long _syncPeriod;
     /** the plotter*/
     private MachineSimulatorTwissPlot _machineSimulatorTwissPlot;
     /**the list of NodePropertyRecord*/
     private List<NodePropertyRecord> nodePropertyRecords;
     /** the position list of elements*/
     private List<Double> _positions;
     /**the keyPaths for history data*/
     private String[] historyDataKeyPaths = new String[2];



	/**constructor */
	public  MachineSimulatorController(final MachineSimulatorDocument document,final WindowReference windowReference) {
		
		STATES_TABLE_MODEL = new KeyValueFilteredTableModel<MachineSimulationHistoryRecord>();
		
		SEQUENCE_TABLE_MODEL = new KeyValueFilteredTableModel<NodePropertyRecord>();
		HISTORY_RECORD_TABLE_MODEL = new KeyValueFilteredTableModel<SimulationHistoryRecord>();
		HISTORY_DATA_TABLE_MODEL = new KeyValueFilteredTableModel<NodePropertyHistoryRecord>();
		
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
        
/***************configure the history record view*****************************************/
        //configure history record table
        final JTable historyRecordTable = (JTable)windowReference.getView( "History Record Table" );
        historyRecordTable.setModel(HISTORY_RECORD_TABLE_MODEL);
        
        HISTORY_RECORD_TABLE_MODEL.setColumnName("SelectState", "select");
        HISTORY_RECORD_TABLE_MODEL.setColumnName("Sequence.Id", "Sequence" );
        HISTORY_RECORD_TABLE_MODEL.setColumnName("DateTime", "Time" );
        HISTORY_RECORD_TABLE_MODEL.setColumnName("RecordName", "Recordname" );
        
        HISTORY_RECORD_TABLE_MODEL.setColumnClassForKeyPaths(Boolean.class, "SelectState");
        HISTORY_RECORD_TABLE_MODEL.setKeyPaths( "SelectState", "Sequence.Id", "DateTime", "RecordName");
        HISTORY_RECORD_TABLE_MODEL.setColumnEditable( "RecordName", true );
        HISTORY_RECORD_TABLE_MODEL.setColumnEditable( "SelectState", true );
        
        //configure history data table
        final JTable historyDataTable = (JTable)windowReference.getView( "History Data Table" );
        historyDataTable.setModel( HISTORY_DATA_TABLE_MODEL );
        
        HISTORY_DATA_TABLE_MODEL.setColumnName( "AcceleratorNode.Id" , "Node" );
        HISTORY_DATA_TABLE_MODEL.setColumnName( "PropertyName", "Property" );
        historyDataKeyPaths[0] = "AcceleratorNode.Id";
        historyDataKeyPaths[1] =  "PropertyName";
        
        
/******************configure the sequence table view*************************************/
        
        final JTable sequenceTable = (JTable)windowReference.getView( "Sequence Table" );
        sequenceTable.setModel( SEQUENCE_TABLE_MODEL ); 
        
        //set the column name of the sequence table
        SEQUENCE_TABLE_MODEL.setColumnName( "AcceleratorNode.Id", "Node" );
        SEQUENCE_TABLE_MODEL.setColumnName( "PropertyName", "Property" );
        SEQUENCE_TABLE_MODEL.setColumnName( "DesignValue", "Design Value" );
        SEQUENCE_TABLE_MODEL.setColumnName( "LiveValue", "Live Value" );
        SEQUENCE_TABLE_MODEL.setColumnName("TestValue", "Test Value");
        
        //set the filter field for sequence table
        SEQUENCE_TABLE_MODEL.setInputFilterComponent(statesTableFilterField);
        SEQUENCE_TABLE_MODEL.setMatchingKeyPaths( "AcceleratorNode.Id" );
         
        //configure the sequence table model
		  SEQUENCE_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, "DesignValue", "LiveValue", "TestValue" );
		  SEQUENCE_TABLE_MODEL.setKeyPaths( "AcceleratorNode.Id", "PropertyName", "DesignValue", "LiveValue", "TestValue" );
		  SEQUENCE_TABLE_MODEL.setColumnEditable( "TestValue", true );

/**********************configure the states table view***********************************/
	     //get components
	     final JTable statesTable = (JTable)windowReference.getView( "States Table" );
	     statesTable.setModel( STATES_TABLE_MODEL );
	     
        //set the column name of the states table
        STATES_TABLE_MODEL.setColumnName( "elementID", "Element" );
        for(final ScalarParameter scalarParameter:SCALAR_PARAMETERS){
            STATES_TABLE_MODEL.setColumnName(scalarParameter.getKeyPath(), scalarParameter.getSymbol() );
            
            STATES_TABLE_MODEL.setColumnName("old."+scalarParameter.getKeyPath(), "Old-"+scalarParameter.getSymbol() );
           }
        for(final VectorParameter vectorParameter:VECTOR_PARAMETERS){
        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForX(),vectorParameter.getSymbolForX());
        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForY(),vectorParameter.getSymbolForY());
        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForZ(),vectorParameter.getSymbolForZ());
        	   
        	   STATES_TABLE_MODEL.setColumnName("old."+vectorParameter.getKeyPathForX(),"<html>Old-"+vectorParameter.getSymbolForX().substring(6) );
        	   STATES_TABLE_MODEL.setColumnName("old."+vectorParameter.getKeyPathForY(),"<html>Old-"+vectorParameter.getSymbolForY().substring(6) );
        	   STATES_TABLE_MODEL.setColumnName("old."+vectorParameter.getKeyPathForZ(),"<html>Old-"+vectorParameter.getSymbolForZ().substring(6) );
           }
        //set the filter field for states table
        STATES_TABLE_MODEL.setInputFilterComponent( statesTableFilterField );
        STATES_TABLE_MODEL.setMatchingKeyPaths( "elementID" );
        
		  STATES_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, "position");
       
/**************************configure the plot view**************************************/
        final FunctionGraphsJPanel twissParametersPlot = ( FunctionGraphsJPanel ) windowReference.getView( "States Plot" );
        _machineSimulatorTwissPlot=new MachineSimulatorTwissPlot( twissParametersPlot,SCALAR_PARAMETERS,VECTOR_PARAMETERS );

		//synoptic display of nodes
		final Box synopticBox = ( Box )windowReference.getView( "SynopticContainer" );
		final XALSynopticPanel xalSynopticPanel = FunctionGraphsXALSynopticAdaptor.assignXALSynopticViewTo( twissParametersPlot, MODEL.getSequence() );
		synopticBox.removeAll();
		synopticBox.add( xalSynopticPanel );
		synopticBox.validate();

/***************************Check boxes action**************************************/
		
		final ActionListener PARAMETER_HANDLER = new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
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
				
				//create the combination keyPath used for comparing new and old simulation results
				final List<String> combParameterKeyPathsList = new ArrayList<String>(2*parameterKeyPathsList.size());
				for( final String path:parameterKeyPathsList){
					combParameterKeyPathsList.add( path );
					combParameterKeyPathsList.add( "old."+path );
				}
				
				//determine the final keyPaths by history simulation record select state
				final List<String> parameterKeyPathsForTableList;
				
				if( MODEL.getHistorySimulation( MODEL.getSequence() )[1] != null ){
					parameterKeyPathsForTableList = combParameterKeyPathsList;
				}
				else {
					parameterKeyPathsForTableList = parameterKeyPathsList;
				}

				
				//turn the keyPath list to string array
				final String[] parameterKeyPathsForTable= new String[parameterKeyPathsForTableList.size()];
				parameterKeyPathsForTableList.toArray( parameterKeyPathsForTable );


				STATES_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, parameterKeyPathsForTable );
				

				final String[] allKeyPathsForTable = new String[standardParameterKeys.length + parameterKeyPathsForTable.length];
				// add standard parameters at the start
				System.arraycopy( standardParameterKeys, 0, allKeyPathsForTable, 0, standardParameterKeys.length );
				// append scalar and vector parameters after standard parameters
				System.arraycopy( parameterKeyPathsForTable, 0, allKeyPathsForTable, standardParameterKeys.length, parameterKeyPathsForTable.length );

				STATES_TABLE_MODEL.setKeyPaths( allKeyPathsForTable );
//				STATES_TABLE_MODEL.setRecords( MODEL.getSimulationRecords( MODEL.getHistorySimulation( MODEL.getSequence() )[0], MODEL.getHistorySimulation( MODEL.getSequence() )[1] ) );

				/**************   configure plot view   ****************/

				twissParametersPlot.removeAllGraphData();
				//setup plot panel and show the selected parameters' graph
				if( parameterKeyPathsForTable.length > 0 && MODEL.getSimulation() != null ){
					configureParametersData(  MODEL.getSimulationRecords( MODEL.getSimulation(), MODEL.getHistorySimulation( MODEL.getSequence() )[1] ), parameterKeyPathsForTable );
					for( final String parameterKey:parameterKeyPathsForTable ){
						_machineSimulatorTwissPlot.showTwissPlot( _positions, PLOT_DATA.get(parameterKey), parameterKey );
					}
				}
				xalSynopticPanel.setAcceleratorSequence( MODEL.getSequence() );
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
                System.out.println( "running the model..." );                
                if( MODEL.getSequence() != null ){
                	final MachineSimulation simulation = MODEL.runSimulation();
                	
                	STATES_TABLE_MODEL.setRecords( MODEL.getSimulationRecords(simulation, MODEL.getHistorySimulation( MODEL.getSequence() )[1] ) );
                
                	_positions=simulation.getAllPosition();
                  
                  HISTORY_RECORD_TABLE_MODEL.setRecords(MODEL.getSimulationHistoryRecords());
                  historyRecordSelectStateChanged( MODEL.getNodePropertyHistoryRecords() );
                 
                  PARAMETER_HANDLER.actionPerformed( null );
                     }
                else JOptionPane.showMessageDialog(windowReference.getWindow(), "You need to select sequence(s) first","Warning!",JOptionPane.PLAIN_MESSAGE);       

            }
        });

      //configure the phase slip check box
		final JCheckBox phaseSlipCheckbox = (JCheckBox)windowReference.getView( "Phase Slip Checkbox" );
		phaseSlipCheckbox.setSelected( MODEL.getSimulator().getUseRFGapPhaseSlipCalculation() );
		phaseSlipCheckbox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				MODEL.getSimulator().setUseRFGapPhaseSlipCalculation( phaseSlipCheckbox.isSelected() );
			}
		});
		
		//configure the remove button of the history record view
		final JButton removeButton = (JButton)windowReference.getView( "Remove Button" );
		removeButton.addActionListener( event -> {		
			List<SimulationHistoryRecord> records = MODEL.getSimulationHistoryRecords();			
			int recordNumber = records.size();
			int removed = 0;
			for( int index = 0; index < recordNumber; index++ ){			
				if( records.get( index-removed ).getSelectState() ) {					
					records.get( index-removed ).setSelectState( false );
					records.remove( index-removed );
					removed++;
				}
			}

			HISTORY_RECORD_TABLE_MODEL.setRecords( records );
		});
		
		//configure the select all button
		final JButton selectAllButton = (JButton)windowReference.getView( "Select All" );
		selectAllButton.addActionListener( event -> {
			List<SimulationHistoryRecord> records = MODEL.getSimulationHistoryRecords();
			for ( int index = 0; index < records.size();index++){
				records.get( index ).setSelectState( true );
			}
			HISTORY_RECORD_TABLE_MODEL.fireTableRowsUpdated(0, records.size()-1 );;
		});
		
		//configure the unselect all button
		final JButton unselectAllButton = (JButton)windowReference.getView( "Unselect All" );
		unselectAllButton.addActionListener( event -> {
			List<SimulationHistoryRecord> records = MODEL.getSimulationHistoryRecords();
			for ( int index = 0; index < records.size();index++){
				records.get( index ).setSelectState( false );
			}
			
			HISTORY_RECORD_TABLE_MODEL.fireTableRowsUpdated( 0, records.size()-1 );
		});

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
    	return () -> SEQUENCE_TABLE_MODEL.fireTableRowsUpdated( 0, SEQUENCE_TABLE_MODEL.getRowCount() - 1  ); 
    }

    /**event indicates that the sequence has changed*/
    public void modelSequenceChanged( final MachineModel model ) {
    	if( model.getSequence() != null ){
    		nodePropertyRecords = model.getWhatIfConfiguration().getNodePropertyRecords();
    		SEQUENCE_TABLE_MODEL.setRecords( nodePropertyRecords );
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
		if( model.getSequence() != null ){
			nodePropertyRecords = model.getWhatIfConfiguration().getNodePropertyRecords();
			SEQUENCE_TABLE_MODEL.setRecords( nodePropertyRecords );
			VALUE_SYNC_TIME.resume();
		}
		
	}

	/**event indicates that the history record select state changed*/
	public void historyRecordSelectStateChanged( final List<NodePropertyHistoryRecord> nodePropertyHistoryRecords ) {
		int valueNumber = nodePropertyHistoryRecords.get(0).getValues().length;
		String[] keyPathsForValues = new String[valueNumber];
		String[] allKeyPaths = new String[historyDataKeyPaths.length+keyPathsForValues.length];
		for( int index = 0; index<valueNumber;index++){
			HISTORY_DATA_TABLE_MODEL.setColumnName( "Values."+index , "Value Record--"+index );
			keyPathsForValues[index] = "Values."+index;
		}
		HISTORY_DATA_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, keyPathsForValues);
		System.arraycopy( historyDataKeyPaths, 0, allKeyPaths, 0, historyDataKeyPaths.length );
		System.arraycopy( keyPathsForValues, 0, allKeyPaths, historyDataKeyPaths.length, keyPathsForValues.length );
		
		HISTORY_DATA_TABLE_MODEL.setKeyPaths( allKeyPaths );
		
		HISTORY_DATA_TABLE_MODEL.setRecords( nodePropertyHistoryRecords );

	}
	
}



