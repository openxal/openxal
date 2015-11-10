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

import xal.extension.bricks.WindowReference;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.smf.FunctionGraphsXALSynopticAdaptor;
import xal.extension.widgets.smf.XALSynopticPanel;
import xal.extension.widgets.swing.KeyValueFilteredTableModel;
import xal.sim.scenario.ModelInput;
import xal.tools.data.KeyValueAdaptor;
import xal.tools.dispatch.DispatchQueue;
import xal.tools.dispatch.DispatchTimer;

/**
 * @author luxiaohan
 * controller for binding the MachineSimulator model to the user interface
 */
public class MachineSimulatorController implements MachineModelListener {
     /** simulated states table model */
     final private KeyValueFilteredTableModel<MachineSimulationRecord> STATES_TABLE_MODEL;
     /**accelerator node table model*/
     final private KeyValueFilteredTableModel<NodePropertyRecord> SEQUENCE_TABLE_MODEL;
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
     final private List<ModelInput> ALL_MODEL_INPUT;
     /**the modelinputs with non-null test value*/
     final private List<ModelInput> VALID_MODEL_INPUT;
     /**sync period in milliseconds*/
     private long _syncPeriod;
     /** the plotter*/
     private MachineSimulatorTwissPlot _machineSimulatorTwissPlot;
     /**the list of NodePropertyRecord*/
     private List<NodePropertyRecord> nodePropertyRecords;
     /** the position list of elements*/
     private List<Double> _positions;



	/**constructor */
	public  MachineSimulatorController(final MachineSimulatorDocument document,final WindowReference windowReference) {
		
		STATES_TABLE_MODEL = new KeyValueFilteredTableModel<MachineSimulationRecord>();
		SEQUENCE_TABLE_MODEL = new KeyValueFilteredTableModel<NodePropertyRecord>();
		PLOT_DATA = new HashMap<String,List<Double>>();
		KEY_VALUE_ADAPTOR= new KeyValueAdaptor();
		
		VALUE_SYNC_TIME = DispatchTimer.getCoalescingInstance( DispatchQueue.createSerialQueue(""), getLiveValueSynchronizer() );
		// set the default sync period to 1 second
		_syncPeriod = 1000;
		
		ALL_MODEL_INPUT = new ArrayList<ModelInput>();
		VALID_MODEL_INPUT = new ArrayList<ModelInput>();
		
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

        //set the column name of the states table
        STATES_TABLE_MODEL.setColumnName( "elementID", "Element" );
        for(final ScalarParameter scalarParameter:SCALAR_PARAMETERS){
            STATES_TABLE_MODEL.setColumnName(scalarParameter.getKeyPath(), scalarParameter.getSymbol() );
           }
        for(final VectorParameter vectorParameter:VECTOR_PARAMETERS){
        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForX(),vectorParameter.getSymbolForX());
        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForY(),vectorParameter.getSymbolForY());
        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForZ(),vectorParameter.getSymbolForZ());
           }
        
        //set the column name of the sequence table
        SEQUENCE_TABLE_MODEL.setColumnName( "NodeId", "Node" );
        SEQUENCE_TABLE_MODEL.setColumnName( "PropertyName", "Property" );
        SEQUENCE_TABLE_MODEL.setColumnName( "DesignValue", "Design Value" );
        SEQUENCE_TABLE_MODEL.setColumnName( "LiveValue", "Live Value" );
        SEQUENCE_TABLE_MODEL.setColumnName("TestValue", "Test Value");

        //get components
        final JTable statesTable = (JTable)windowReference.getView( "States Table" );
        statesTable.setModel( STATES_TABLE_MODEL );
        
        final JTable sequenceTable = (JTable)windowReference.getView( "Sequence Table" );
        sequenceTable.setModel( SEQUENCE_TABLE_MODEL );

        final JTextField statesTableFilterField = (JTextField)windowReference.getView( "States Table Filter Field" );
        STATES_TABLE_MODEL.setInputFilterComponent( statesTableFilterField );
        STATES_TABLE_MODEL.setMatchingKeyPaths( "elementID" );
        
        //set the filter field for sequence table
        SEQUENCE_TABLE_MODEL.setInputFilterComponent(statesTableFilterField);
        SEQUENCE_TABLE_MODEL.setMatchingKeyPaths( "NodeId" );
        
        //configure the sequence table model
		  SEQUENCE_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, "DesignValue", "LiveValue", "TestValue" );
		  SEQUENCE_TABLE_MODEL.setKeyPaths( "NodeId", "PropertyName", "DesignValue", "LiveValue", "TestValue" );
		  SEQUENCE_TABLE_MODEL.setColumnEditable( "TestValue", true );

        final FunctionGraphsJPanel twissParametersPlot = ( FunctionGraphsJPanel ) windowReference.getView( "States Plot" );
        _machineSimulatorTwissPlot=new MachineSimulatorTwissPlot( twissParametersPlot,SCALAR_PARAMETERS,VECTOR_PARAMETERS );

		//synoptic display of nodes
		final Box synopticBox = ( Box )windowReference.getView( "SynopticContainer" );
		final XALSynopticPanel xalSynopticPanel = FunctionGraphsXALSynopticAdaptor.assignXALSynopticViewTo( twissParametersPlot, MODEL.getSequence() );
		synopticBox.removeAll();
		synopticBox.add( xalSynopticPanel );
		synopticBox.validate();

        // handle the parameter selections of Table view
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

				//turn the keyPath list to string array
				final String[] parameterKeyPaths= new String[parameterKeyPathsList.size()];
				parameterKeyPathsList.toArray(parameterKeyPaths);

				STATES_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, "position");
				STATES_TABLE_MODEL.setColumnClassForKeyPaths( Double.class,parameterKeyPaths );
				

				final String[] parameterKeyPathsForTable = new String[standardParameterKeys.length + parameterKeyPaths.length];
				// add standard parameters at the start
				System.arraycopy( standardParameterKeys, 0, parameterKeyPathsForTable, 0, standardParameterKeys.length );
				// append scalar and vector parameters after standard parameters
				System.arraycopy( parameterKeyPaths, 0, parameterKeyPathsForTable, standardParameterKeys.length, parameterKeyPaths.length );

				STATES_TABLE_MODEL.setKeyPaths( parameterKeyPathsForTable );

				/**************   configure plot view   ****************/

				twissParametersPlot.removeAllGraphData();
				//setup plot panel and show the selected parameters' graph
				if( parameterKeyPaths.length > 0 && MODEL.getSimulation() != null ){
					configureParametersData( MODEL.getSimulation().getSimulationRecords(), parameterKeyPaths );
					for( final String parameterKey:parameterKeyPaths ){
						_machineSimulatorTwissPlot.showTwissPlot( _positions, PLOT_DATA.get(parameterKey), parameterKey );
					}
				}
				xalSynopticPanel.setAcceleratorSequence( MODEL.getSequence() );
			}
		};

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
                	checkTestValues();
                	MODEL.setModelInputs( ALL_MODEL_INPUT, VALID_MODEL_INPUT );
                	final MachineSimulation simulation = MODEL.runSimulation();
                  STATES_TABLE_MODEL.setRecords( simulation.getSimulationRecords() );                  
                  _positions=simulation.getAllPosition();

                  PARAMETER_HANDLER.actionPerformed( null );
                     }
                else JOptionPane.showMessageDialog(windowReference.getWindow(), "You need to select sequence(s) first","Warning!",JOptionPane.PLAIN_MESSAGE);       

            }
        });

		final JCheckBox phaseSlipCheckbox = (JCheckBox)windowReference.getView( "Phase Slip Checkbox" );
		phaseSlipCheckbox.setSelected( MODEL.getSimulator().getUseRFGapPhaseSlipCalculation() );
		phaseSlipCheckbox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				MODEL.getSimulator().setUseRFGapPhaseSlipCalculation( phaseSlipCheckbox.isSelected() );
			}
		});


    }

	
  /** get the selected parameters' data from simulation records
    * @param records the result of simulation
    * @param keyPaths specifies the array of key paths to get the data to plot
    */ 
    private void configureParametersData( final List<MachineSimulationRecord> records,final String[] keyPaths ){
      PLOT_DATA.clear();      
    	for( final String keyPath:keyPaths ){
    		PLOT_DATA.put( keyPath, new ArrayList<Double>( records.size() ) );
    		for( final MachineSimulationRecord record:records ){
    			PLOT_DATA.get(keyPath).add( (Double)KEY_VALUE_ADAPTOR.valueForKeyPath( record,keyPath ) );
    		}
    	}   	
    }
    /**get a runnalbe that syncs the values */
    private Runnable getLiveValueSynchronizer(){
    	return new Runnable() {
			public void run() {
				SEQUENCE_TABLE_MODEL.fireTableDataChanged();
			}
		};
    }
    
    /**check if there are non-null test values and set to scenario before running simulator*/
    private void checkTestValues(){
    	ALL_MODEL_INPUT.clear();
    	VALID_MODEL_INPUT.clear();
    	for( NodePropertyRecord record:nodePropertyRecords ){
    		ALL_MODEL_INPUT.add( record.getModelInput() );
    		if( !Double.isNaN( record.getTestValue() ) ){
    			record.getModelInput().setDoubleValue( record.getTestValue() );
    			VALID_MODEL_INPUT.add( record.getModelInput() );
    		}
    	}
    }


    /**event indicates that the sequence has changed*/
    public void modelSequenceChanged( MachineModel model ) {
    	if( model.getSequence() != null ){
    		nodePropertyRecords = model.getWhatIfConfiguration().getNodePropertyRecords();
    		SEQUENCE_TABLE_MODEL.setRecords( nodePropertyRecords );
   		VALUE_SYNC_TIME.startNowWithInterval( _syncPeriod, 0 );
    	}
    	
    }


	/**event indicates that the scenario has changed*/
	public void modelScenarioChanged( MachineModel model) {
		if( model.getSequence() != null ){
			nodePropertyRecords = model.getWhatIfConfiguration().getNodePropertyRecords();
			SEQUENCE_TABLE_MODEL.setRecords( nodePropertyRecords );
			VALUE_SYNC_TIME.resume();
		}
		
	}
}



