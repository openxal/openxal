/**
 * 
 */
package xal.app.machinesimulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextField;

import xal.extension.bricks.WindowReference;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.swing.KeyValueFilteredTableModel;

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
      
     /**result of MachineSimulatoion */
     public MachineSimulation _MachineSimulation;
      /** the document for the Machine Simulator application*/
     final private MachineSimulatorDocument _Document;
     /** the plotter*/
     public MachineSimulatorPlotter _MachineSimulatorPlotter;
       /** initialize the data list of parameters*/
     public List<Double> _position=new ArrayList<Double>();
     public List<Double> kineticenery=new ArrayList<Double>();     
     public List<Double> betax=new ArrayList<Double>();
     public List<Double> betay=new ArrayList<Double>();
     public List<Double> betaz=new ArrayList<Double>();
     public List<Double> alphax=new ArrayList<Double>();
     public List<Double> alphay=new ArrayList<Double>();
     public List<Double> alphaz=new ArrayList<Double>();
     public List<Double> gammax=new ArrayList<Double>();
     public List<Double> gammay=new ArrayList<Double>();
     public List<Double> gammaz=new ArrayList<Double>();
     public List<Double> emittancex=new ArrayList<Double>();
     public List<Double> emittancey=new ArrayList<Double>();
     public List<Double> emittancez=new ArrayList<Double>();
     public List<Double> beamsizex=new ArrayList<Double>();
     public List<Double> beamsizey=new ArrayList<Double>();
     public List<Double> beamsizez=new ArrayList<Double>();
     public List<Double> betatronphasex=new ArrayList<Double>();
     public List<Double> betatronphasey=new ArrayList<Double>();
     public List<Double> betatronphasez=new ArrayList<Double>();

        
	/**constructor */
	public  MachineSimulatorController(final MachineSimulatorDocument document,final WindowReference windowReference) {
		// TODO Auto-generated constructor stub
		_Document=document;
		WINDOW_REFERENCE=windowReference;
		STATES_TABLE_MODEL = new KeyValueFilteredTableModel<MachineSimulationRecord>();
        // initialize the model here
        MODEL = _Document.getModel();

//        RECORDS=_MachineSimulation.getSimulationRecords();
         configureTableWindow(WINDOW_REFERENCE);
         configurePlotWindow(WINDOW_REFERENCE);
	}
	
	
    /** configure the Table window */
    private void configureTableWindow( final WindowReference windowReference ) {
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
  //                   System.out.println(vectorParameterBaseCount);
                
                // construct the full vector parameter keys from each pair of selected planes and vector parameter names
                final String[] vectorParameterKeys = new String[ planes.size() * vectorParameterBaseCount ];
                int vectorParameterIndex = 0;
                for ( final String plane : planes ) {
                    for ( final String twissParameter : twissParameterNames ) {
//                        vectorParameterKeys[ vectorParameterIndex++ ] = "twiss." + plane + "." + twissParameter;
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
        
        
        // configure the run button
        final JButton runButton = (JButton)windowReference.getView( "Run Button" );
        runButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                System.out.println( "running the model..." );
                final MachineSimulation simulation = MODEL.runSimulation();
                getParametersData(simulation);
                STATES_TABLE_MODEL.setRecords( simulation.getSimulationRecords() );
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
    
    /**configure the plot window*/ 
    private void configurePlotWindow(final WindowReference windowReference){
        final FunctionGraphsJPanel twissParametersplot = (FunctionGraphsJPanel) WINDOW_REFERENCE.getView("States Plot");
        _MachineSimulatorPlotter=new MachineSimulatorPlotter(twissParametersplot);

        // handle the parameter selections of plot view
        final JCheckBox kineticEnergyCheckbox = (JCheckBox)windowReference.getView( "Kinetic Energy Checkbox1" );
        final JCheckBox xSelectionCheckbox = (JCheckBox)windowReference.getView( "X Selection Checkbox1" );
        final JCheckBox ySelectionCheckbox = (JCheckBox)windowReference.getView( "Y Selection Checkbox1" );
        final JCheckBox zSelectionCheckbox = (JCheckBox)windowReference.getView( "Z Selection Checkbox1" );
        final JCheckBox betaCheckbox = (JCheckBox)windowReference.getView( "Beta Checkbox1" );
        final JCheckBox alphaCheckbox = (JCheckBox)windowReference.getView( "Alpha Checkbox1" );
        final JCheckBox gammaCheckbox = (JCheckBox)windowReference.getView( "Gamma Checkbox1" );
        final JCheckBox emittanceCheckbox = (JCheckBox)windowReference.getView( "Emittance Checkbox1" );
        final JCheckBox beamSizeCheckbox = (JCheckBox)windowReference.getView( "Beam Size Checkbox1" );
        final JCheckBox betatronPhaseCheckbox = (JCheckBox)windowReference.getView( "Betatron Phase Checkbox1" );

        final ActionListener PARAMETER_HANDLER = new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
           	 twissParametersplot.removeAllGraphData();
            	   _MachineSimulatorPlotter.setupPlot(twissParametersplot);
              final int[] planes = new int[3]; 
            	if(xSelectionCheckbox.isSelected())
            		planes[0]=1;
            	else planes[0]=0;
            	if(ySelectionCheckbox.isSelected())
            		planes[1]=1;
            	else planes[1]=0;
            	if(zSelectionCheckbox.isSelected())
            		planes[2]=1;
            	else planes[2]=0;
            	    if(kineticEnergyCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, kineticenery, "kineticenery");
            	if(planes[0]==1){
            		if(betaCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, betax, "betax");
            		if(alphaCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, alphax, "alphax");
            		if(gammaCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, gammax, "gammax");
            		if(emittanceCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, emittancex, "emittancex");
            		if(beamSizeCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, beamsizex, "beamsizex");
            		if(betatronPhaseCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, betatronphasex, "betatronphasex");
            	}
            	if(planes[1]==1){
            		if(betaCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, betay, "betay");
            		if(alphaCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, alphay, "alphay");
            		if(gammaCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, gammay, "gammay");
            		if(emittanceCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, emittancey, "emittancey");
            		if(beamSizeCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, beamsizey, "beamsizey");
            		if(betatronPhaseCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, betatronphasey, "betatronphasey");
            	}
            	if(planes[2]==1){
            		if(betaCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, betaz, "betaz");
            		if(alphaCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, alphaz, "alphaz");
            		if(gammaCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, gammaz, "gammaz");
            		if(emittanceCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, emittancez, "emittancez");
            		if(beamSizeCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, beamsizez, "beamsizez");
            		if(betatronPhaseCheckbox.isSelected())_MachineSimulatorPlotter.showtwissplot(_position, betatronphasez, "betatronphasez");
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
        ClearButton.addActionListener( new ActionListener() {
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
        });
        
    }
  /** get the parameter data from Machinesimulation*/  
    public void getParametersData(MachineSimulation simulation){
    	_MachineSimulation=simulation;
      _position=_MachineSimulation.getAllPosition();
        kineticenery.removeAll(kineticenery);
        
        betax.removeAll(betax);
        betay.removeAll(betay);
        betaz.removeAll(betaz);
        
        alphax.removeAll(alphax);
        alphay.removeAll(alphay);
        alphaz.removeAll(alphaz);
        
        gammax.removeAll(gammax);
        gammay.removeAll(gammay);
        gammaz.removeAll(gammaz);
        
        emittancex.removeAll(emittancex);
        emittancey.removeAll(emittancey);
        emittancez.removeAll(emittancez);
        
        beamsizex.removeAll(beamsizex);
        beamsizey.removeAll(beamsizey);
        beamsizez.removeAll(beamsizez);
        
        betatronphasex.removeAll(betatronphasex);
        betatronphasey.removeAll(betatronphasey);
        betatronphasez.removeAll(betatronphasez);
      for(int i=0;i<_MachineSimulation.getSimulationRecords().size();i++){
    	  kineticenery.add(_MachineSimulation.getSimulationRecords().get(i).getProbeState().getKineticEnergy());
    	  
        betax.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[0].getBeta());  
    	  betay.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[1].getBeta());
    	  betaz.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[2].getBeta());
    	  
        alphax.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[0].getAlpha());  
        alphay.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[1].getAlpha());
        alphaz.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[2].getAlpha());
        
        gammax.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[0].getGamma());  
    	  gammay.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[1].getGamma());
    	  gammaz.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[2].getGamma());
    	  
        emittancex.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[0].getEmittance());  
        emittancey.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[1].getEmittance());
        emittancez.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[2].getEmittance());
        
        beamsizex.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[0].getEnvelopeRadius());  
        beamsizey.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[1].getEnvelopeRadius());
        beamsizez.add(_MachineSimulation.getSimulationRecords().get(i).getTwissParameters()[2].getEnvelopeRadius());
        
        betatronphasex.add(_MachineSimulation.getSimulationRecords().get(i).getBetatronPhase().getx());  
        betatronphasey.add(_MachineSimulation.getSimulationRecords().get(i).getBetatronPhase().gety());
        betatronphasez.add(_MachineSimulation.getSimulationRecords().get(i).getBetatronPhase().getz());
      }
      
          
    }
}


 
