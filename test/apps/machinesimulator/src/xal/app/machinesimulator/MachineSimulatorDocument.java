/*
 * MachineSimulatorDocument.java
 *
 * Created by Tom Pelaia on 9/19/11
 */

package xal.app.machinesimulator;

import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.smf.*;
import xal.tools.apputils.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.smf.data.XMLDataManager;
import xal.tools.data.*;
import xal.extension.bricks.WindowReference;
import xal.extension.widgets.swing.*;
import xal.model.probe.traj.ProbeState;


/**
 * MachineSimulatorDocument represents the document for the Machine Simulator application.
 * @author  t6p
 */
public class MachineSimulatorDocument extends AcceleratorDocument implements DataListener {
 	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "MachineSimulatorDocument";
	
	/** main window reference */
	final WindowReference WINDOW_REFERENCE;
    
    /** main model */
    final MachineModel MODEL;
    
    /** simulated states table model */
    final KeyValueFilteredTableModel<MachineSimulationRecord> STATES_TABLE_MODEL;
	
	
    /** Empty Constructor */
    public MachineSimulatorDocument() {
        this( null );
    }
    
    
    /** 
     * Primary constructor 
     * @param url The URL of the file to load into the new document.
     */
    public MachineSimulatorDocument( final java.net.URL url ) {
        setSource( url );
		
		WINDOW_REFERENCE = getDefaultWindowReference( "MainWindow", this );
        
//        STATES_TABLE_MODEL = new KeyValueFilteredTableModel<IPhaseState>();
        STATES_TABLE_MODEL = new KeyValueFilteredTableModel<MachineSimulationRecord>();
        
        // initialize the model here
        MODEL = new MachineModel();
		
		if ( url != null ) {
            System.out.println( "Opening document: " + url.toString() );
            final DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
            update( documentAdaptor.childAdaptor( dataLabel() ) );
        }		
		
		configureWindow( WINDOW_REFERENCE );		
		
		setHasChanges( false );
    }
    
    
    /** Make and configure the main window. */
    public void makeMainWindow() {
        mainWindow = (XalWindow)WINDOW_REFERENCE.getWindow();
		setHasChanges( false );
    }
    
    
    /** configure the main window */
    private void configureWindow( final WindowReference windowReference ) {
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
    
    
    // Generate the twiss parameter key from the base twiss parameter name and the plane
    static private String toTwissParameterKey( final String twissParameterName, final int plane ) {
        return "twiss." + plane + "." + twissParameterName;
    }
    
    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs( final URL url ) {
        writeDataTo( this, url );
    }
    
    
    /** Handle the accelerator changed event by displaying the elements of the accelerator in the main window. */
    public void acceleratorChanged() {
        try {
            MODEL.setSequence( null );
            setHasChanges( true );
        }
        catch ( Exception exception ) {
            exception.printStackTrace();
            displayError( "Error Setting Accelerator", "Simulator Configuration Exception", exception );
        }
	}
    
    
    /** Handle the selected sequence changed event by displaying the elements of the selected sequence in the main window. */
    public void selectedSequenceChanged() {
        try {
            MODEL.setSequence( getSelectedSequence() );
            setHasChanges( true );
        }
        catch ( Exception exception ) {
            exception.printStackTrace();
            displayError( "Error Setting Sequence", "Simulator Configuration Exception", exception );
        }
	}
	
    
    /** provides the name used to identify the class in an external data source. */
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /** Instructs the receiver to update its data based on the given adaptor. */
    public void update( final DataAdaptor adaptor ) {
		if ( adaptor.hasAttribute( "acceleratorPath" ) ) {
			final String acceleratorPath = adaptor.stringValue( "acceleratorPath" );
			final Accelerator accelerator = applySelectedAcceleratorWithDefaultPath( acceleratorPath );
			
			if ( accelerator != null && adaptor.hasAttribute( "sequence" ) ) {
				final String sequenceID = adaptor.stringValue( "sequence" );
				setSelectedSequence( getAccelerator().findSequence( sequenceID ) );
			}
		}
		
        final DataAdaptor modelAdaptor = adaptor.childAdaptor( MachineModel.DATA_LABEL );
        if ( modelAdaptor != null )  MODEL.update( modelAdaptor );
    }
    
    
    /** Instructs the receiver to write its data to the adaptor for external storage. */
    public void write( final DataAdaptor adaptor ) {
        adaptor.setValue( "version", "1.0.0" );
        adaptor.setValue( "date", new java.util.Date().toString() );
		
        adaptor.writeNode( MODEL );
		
		if ( getAccelerator() != null ) {
			adaptor.setValue( "acceleratorPath", getAcceleratorFilePath() );
			
			final AcceleratorSeq sequence = getSelectedSequence();
			if ( sequence != null ) {
				adaptor.setValue( "sequence", sequence.getId() );
			}
		}
    }
}
