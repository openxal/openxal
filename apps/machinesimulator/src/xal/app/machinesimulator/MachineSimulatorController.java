/**
 *
 */
package xal.app.machinesimulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextField;

import xal.extension.bricks.WindowReference;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.swing.KeyValueFilteredTableModel;
import xal.tools.data.KeyValueAdaptor;

/**
 * @author luxiaohan
 * controller for binding the MachineSimulator model to the user interface
 */
public class MachineSimulatorController {
	  /** main window reference */
	  final private WindowReference WINDOW_REFERENCE;
     /** simulated states table model */
     final KeyValueFilteredTableModel<MachineSimulationRecord> STATES_TABLE_MODEL;
     /** main model */
     final private MachineModel MODEL;
     /** the plotter*/
     public MachineSimulatorTwissPlot _machineSimulatorTwissPlot;
 	  /** key value adaptor to get the twiss value from a record for the specified key path */
     final private KeyValueAdaptor KEY_VALUE_ADAPTOR;
     /**a map array from parameter's key to plot data list*/
     final private HashMap<String, List<Double>> PLOT_DATA;
     /**the scalar parameters*/
     final private List<ScalarParameter> SCALAR_PARAMETERS;
     /**the vector parameters*/
     final private List<VectorParameter> VECTOR_PARAMETERS;
 	  /**list of parameters*/
 	  final private List<Parameter> PARAMETERS;
     


	/**constructor */
	public  MachineSimulatorController(final MachineSimulatorDocument document,final WindowReference windowReference) {
		WINDOW_REFERENCE=windowReference;
		
		STATES_TABLE_MODEL = new KeyValueFilteredTableModel<MachineSimulationRecord>();
		PLOT_DATA = new HashMap<String,List<Double>>();
		KEY_VALUE_ADAPTOR= new KeyValueAdaptor();
      // initialize the model here
      MODEL = document.getModel();
		
		SCALAR_PARAMETERS=new ArrayList<ScalarParameter>();
		SCALAR_PARAMETERS.add(new ScalarParameter("Kinetic Energy", "probeState.kineticEnergy"));
		
		VECTOR_PARAMETERS=new ArrayList<VectorParameter>();
		VECTOR_PARAMETERS.add(new VectorParameter("Beta","beta","twissParameters", "beta"));
		VECTOR_PARAMETERS.add(new VectorParameter("Alpha","alpha","twissParameters", "alpha"));
		VECTOR_PARAMETERS.add(new VectorParameter("Gamma","gamma","twissParameters", "gamma"));
		VECTOR_PARAMETERS.add(new VectorParameter("Emittance","epsilon","twissParameters", "emittance"));
		VECTOR_PARAMETERS.add(new VectorParameter("EnvelopeRadius","sigma","twissParameters", "envelopeRadius"));
		VECTOR_PARAMETERS.add(new VectorParameter("BetatronPhase","phi","betatronPhase"));
		//put scalar and vector parameter together
		PARAMETERS = new ArrayList<Parameter>(SCALAR_PARAMETERS.size()+VECTOR_PARAMETERS.size());
		PARAMETERS.addAll(SCALAR_PARAMETERS);
		PARAMETERS.addAll(VECTOR_PARAMETERS);
        

      configureMainWindow(WINDOW_REFERENCE);
	}


    /** configure the main window */
    private void configureMainWindow( final WindowReference windowReference ) {

        //set the column name of the table
        STATES_TABLE_MODEL.setColumnName( "elementID", "Element" );
        for(final ScalarParameter scalarParameter:SCALAR_PARAMETERS){
            STATES_TABLE_MODEL.setColumnName(scalarParameter.getKeyPath(), scalarParameter.getSymbol() );
           }
        for(final VectorParameter vectorParameter:VECTOR_PARAMETERS){        	
        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForX(),vectorParameter.getSymbolForX());
        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForY(),vectorParameter.getSymbolForY());
        	   STATES_TABLE_MODEL.setColumnName(vectorParameter.getKeyPathForZ(),vectorParameter.getSymbolForZ());
           }

        //get components
        final JTable statesTable = (JTable)windowReference.getView( "States Table" );
        statesTable.setModel( STATES_TABLE_MODEL );

        final JTextField statesTableFilterField = (JTextField)windowReference.getView( "States Table Filter Field" );
        STATES_TABLE_MODEL.setInputFilterComponent( statesTableFilterField );
        STATES_TABLE_MODEL.setMatchingKeyPaths( "elementID" );

        final FunctionGraphsJPanel twissParametersPlot = (FunctionGraphsJPanel) windowReference.getView("States Plot");
        _machineSimulatorTwissPlot=new MachineSimulatorTwissPlot(twissParametersPlot,PARAMETERS);

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
                    if ( betaCheckbox.isSelected() ) parameterKeyPathsList.add(VECTOR_PARAMETERS.get(0).getKeyPathToArray().get(plane));                    
                    if ( alphaCheckbox.isSelected() ) parameterKeyPathsList.add(VECTOR_PARAMETERS.get(1).getKeyPathToArray().get(plane));
                    if ( gammaCheckbox.isSelected() )  parameterKeyPathsList.add(VECTOR_PARAMETERS.get(2).getKeyPathToArray().get(plane) );
                    if ( emittanceCheckbox.isSelected() ) parameterKeyPathsList.add(VECTOR_PARAMETERS.get(3).getKeyPathToArray().get(plane) );
                    if ( beamSizeCheckbox.isSelected() )  parameterKeyPathsList.add(VECTOR_PARAMETERS.get(4).getKeyPathToArray().get(plane) );
                    if ( betatronPhaseCheckbox.isSelected() ) parameterKeyPathsList.add(VECTOR_PARAMETERS.get(5).getKeyPathToArray().get(plane) );
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
                if(parameterKeyPaths.length!=0&MODEL.getSimulation()!=null){
                  getParametersData(MODEL.getSimulation().getSimulationRecords(), parameterKeyPaths);
                	_machineSimulatorTwissPlot.setupPlot(twissParametersPlot);

                   for(final String parameterKey:parameterKeyPaths){
                        _machineSimulatorTwissPlot.showTwissPlot(MODEL.getSimulation().getAllPosition(), PLOT_DATA.get(parameterKey), parameterKey);
                         }
                     }

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
                final MachineSimulation simulation = MODEL.runSimulation();
                STATES_TABLE_MODEL.setRecords( simulation.getSimulationRecords() );

                PARAMETER_HANDLER.actionPerformed(null);
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
    public void getParametersData(final List<MachineSimulationRecord> records,final String[] keyPaths){
    	PLOT_DATA.clear();
    	for(final String keyPath:keyPaths){
    		PLOT_DATA.put(keyPath, new ArrayList<Double>(records.size()));
    		for(final MachineSimulationRecord record:records){
    			PLOT_DATA.get(keyPath).add((Double)KEY_VALUE_ADAPTOR.valueForKeyPath(record,keyPath));
    		}
    	}
    }

}



