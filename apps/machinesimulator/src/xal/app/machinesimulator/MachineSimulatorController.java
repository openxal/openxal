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
     final MachineModel MODEL;
     /**records of simulation result*/
     public List<MachineSimulationRecord> _allRecords=null;
     /** the document for the Machine Simulator application*/
     final private MachineSimulatorDocument _Document;
     /** the plotter*/
     public MachineSimulatorTwissPlot _machineSimulatorTwissPlot;
 	  /** key value adaptor to get the twiss value from a record for the specified key path */
     final KeyValueAdaptor KEY_VALUE_ADAPTOR;
     /**a map array from parameter's key to plot data list*/
     final HashMap<String, List<Double>> PLOT_DATA;
     /** the position list of elements*/
     public List<Double> _position;


	/**constructor */
	public  MachineSimulatorController(final MachineSimulatorDocument document,final WindowReference windowReference) {
		_Document=document;
		WINDOW_REFERENCE=windowReference;
		STATES_TABLE_MODEL = new KeyValueFilteredTableModel<MachineSimulationRecord>();
		PLOT_DATA = new HashMap<String,List<Double>>();
		KEY_VALUE_ADAPTOR= new KeyValueAdaptor();
        // initialize the model here
        MODEL = _Document.getModel();

      configureMainWindow(WINDOW_REFERENCE);
	}


    /** configure the main window */
    private void configureMainWindow( final WindowReference windowReference ) {
        STATES_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, "position", "probeState.kineticEnergy" );

        STATES_TABLE_MODEL.setColumnName( "elementID", "Element" );
        STATES_TABLE_MODEL.setColumnName( "probeState.kineticEnergy", "Kinetic Energy" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.0.beta", "<html>&beta;<sub>x</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.0.alpha", "<html>&alpha;<sub>x</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.0.gamma", "<html>&gamma;<sub>x</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.0.emittance", "<html>&epsilon;<sub>x</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.0.envelopeRadius", "<html>&sigma;<sub>x</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "betatronPhase.toArray.0", "<html>&phi;<sub>x</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.1.beta", "<html>&beta;<sub>y</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.1.alpha", "<html>&alpha;<sub>y</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.1.gamma", "<html>&gamma;<sub>y</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.1.emittance", "<html>&epsilon;<sub>y</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.1.envelopeRadius", "<html>&sigma;<sub>y</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "betatronPhase.toArray.1", "<html>&phi;<sub>y</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.2.beta", "<html>&beta;<sub>z</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.2.alpha", "<html>&alpha;<sub>z</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.2.gamma", "<html>&gamma;<sub>z</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.2.emittance", "<html>&epsilon;<sub>z</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "twissParameters.2.envelopeRadius", "<html>&sigma;<sub>z</sub></html>" );
        STATES_TABLE_MODEL.setColumnName( "betatronPhase.toArray.2", "<html>&phi;<sub>z</sub></html>" );

        final JTable statesTable = (JTable)windowReference.getView( "States Table" );
        statesTable.setModel( STATES_TABLE_MODEL );

        final JTextField statesTableFilterField = (JTextField)windowReference.getView( "States Table Filter Field" );
        STATES_TABLE_MODEL.setInputFilterComponent( statesTableFilterField );
        STATES_TABLE_MODEL.setMatchingKeyPaths( "elementID" );

        final FunctionGraphsJPanel twissParametersPlot = (FunctionGraphsJPanel) windowReference.getView("States Plot");
        _machineSimulatorTwissPlot=new MachineSimulatorTwissPlot(twissParametersPlot);

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
                // array of optional scalar parameters to display
                final List<String> scalarParameterNames = new ArrayList<String>();
                if ( kineticEnergyCheckbox.isSelected() )  scalarParameterNames.add( "probeState.kineticEnergy" );
                final String[] scalarParameterKeys = new String[ scalarParameterNames.size() ];
                int scalarParameterIndex = 0;
                for ( final String scalarParameterName : scalarParameterNames ) {
                    scalarParameterKeys[ scalarParameterIndex++ ] = scalarParameterName;
                }
                STATES_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, scalarParameterKeys );

                // Add each selected plan to the list of planes to display and associate each plane with its corresponding twiss array index
                final List<String> planes = new ArrayList<String>(3);
                if ( xSelectionCheckbox.isSelected() )  planes.add( "0" );
                if ( ySelectionCheckbox.isSelected() )  planes.add( "1" );
                if ( zSelectionCheckbox.isSelected() )  planes.add( "2" );

                // Add each selected twiss parameter name to the list of parameters to display
                final List<String> twissParameterNames = new ArrayList<String>();
                if ( betaCheckbox.isSelected() )  twissParameterNames.add( "beta" );
                if ( alphaCheckbox.isSelected() )  twissParameterNames.add( "alpha" );
                if ( gammaCheckbox.isSelected() )  twissParameterNames.add( "gamma" );
                if ( emittanceCheckbox.isSelected() )  twissParameterNames.add( "emittance" );
                if ( beamSizeCheckbox.isSelected() )  twissParameterNames.add( "envelopeRadius" );

                int vectorParameterBaseCount = twissParameterNames.size();
                if ( betatronPhaseCheckbox.isSelected() )  vectorParameterBaseCount++;

                // construct the full vector parameter keys from each pair of selected planes and vector parameter names
                final String[] vectorParameterKeys = new String[ planes.size() * vectorParameterBaseCount ];
                int vectorParameterIndex = 0;
                for ( final String plane : planes ) {
                    for ( final String twissParameter : twissParameterNames ) {
                        vectorParameterKeys[ vectorParameterIndex++ ] = "twissParameters." + plane + "." + twissParameter;
                    }

                    if ( betatronPhaseCheckbox.isSelected() ) {
                        vectorParameterKeys[ vectorParameterIndex++ ] = "betatronPhase.toArray." + plane;
                    }
                }
                STATES_TABLE_MODEL.setColumnClassForKeyPaths( Double.class, vectorParameterKeys );

                final String[] parameterKeys = new String[standardParameterKeys.length + scalarParameterKeys.length + vectorParameterKeys.length];
                // add standard parameters at the start
                System.arraycopy( standardParameterKeys, 0, parameterKeys, 0, standardParameterKeys.length );
                // append optional scalar parameters after standard parameters
                System.arraycopy( scalarParameterKeys, 0, parameterKeys, standardParameterKeys.length, scalarParameterKeys.length );
                // append vector parameters after scalar parameters
                System.arraycopy( vectorParameterKeys, 0, parameterKeys, scalarParameterKeys.length + standardParameterKeys.length, vectorParameterKeys.length );
                STATES_TABLE_MODEL.setKeyPaths( parameterKeys );

/**************   configure plot view   ****************/
               final String[] parameterKeysForPlot=new String[parameterKeys.length-2];
                //copy the parameters' key without elementID and position
                System.arraycopy(parameterKeys, 2, parameterKeysForPlot, 0, parameterKeys.length-2);

                twissParametersPlot.removeAllGraphData();
                //setup plot panel and show the selected parameters' graph
                if(parameterKeysForPlot.length!=0&_allRecords!=null){
                  getParametersData(_allRecords, parameterKeysForPlot);
                	_machineSimulatorTwissPlot.setupPlot(twissParametersPlot);

                   for(final String parameterKey:parameterKeysForPlot){
                        _machineSimulatorTwissPlot.showTwissPlot(_position, PLOT_DATA.get(parameterKey), parameterKey);
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
                _allRecords=simulation.getSimulationRecords();
                _position=simulation.getAllPosition();
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



