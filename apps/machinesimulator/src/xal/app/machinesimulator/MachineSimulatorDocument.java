/*
 * MachineSimulatorDocument.java
 *
 * Created by Tom Pelaia on 9/19/11
 */

package xal.app.machinesimulator;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.smf.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.data.*;
import xal.extension.bricks.WindowReference;


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

   /** controller*/
   final MachineSimulatorController _machineSimulatorController;
    /**the scalar parameters*/
   final List<ScalarParameter> SCALAR_PARAMETERS;
   /**the vector parameters*/
   final List<VectorParameter> VECTOR_PARAMETERS;
	
	
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
		
		SCALAR_PARAMETERS=new ArrayList<ScalarParameter>();
		SCALAR_PARAMETERS.add(new ScalarParameter("Kinetic Energy", "probeState.kineticEnergy"));
		
		VECTOR_PARAMETERS=new ArrayList<VectorParameter>();
		VECTOR_PARAMETERS.add(new VectorParameter("Beta","beta","twissParameters", "beta"));
		VECTOR_PARAMETERS.add(new VectorParameter("Alpha","alpha","twissParameters", "alpha"));
		VECTOR_PARAMETERS.add(new VectorParameter("Gamma","gamma","twissParameters", "gamma"));
		VECTOR_PARAMETERS.add(new VectorParameter("Emittance","epsilon","twissParameters", "emittance"));
		VECTOR_PARAMETERS.add(new VectorParameter("EnvelopeRadius","sigma","twissParameters", "envelopeRadius"));
		VECTOR_PARAMETERS.add(new VectorParameter("BetatronPhase","phi","betatronPhase"));
        
        // initialize the model here
        MODEL = new MachineModel();
		_machineSimulatorController= new MachineSimulatorController(this,WINDOW_REFERENCE);

		if ( url != null ) {
            System.out.println( "Opening document: " + url.toString() );
            final DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
            update( documentAdaptor.childAdaptor( dataLabel() ) );
        }
		
		setHasChanges( false );
    }
    
    
    /** Make and configure the main window. */
    public void makeMainWindow() {
        mainWindow = (XalWindow)WINDOW_REFERENCE.getWindow();
		setHasChanges( false );
    }
    
    
	/** get the model */
	public MachineModel getModel() {
		return MODEL;
	}
	/**get the list of scalar parameters*/
	public List<ScalarParameter> getScarlarParameter() {
		return SCALAR_PARAMETERS;		
	}
	/**get the list of vector parameters*/
	public List<VectorParameter> getVectorParameter() {
		return VECTOR_PARAMETERS;
	}
    
	
    
/**    // Generate the twiss parameter key from the base twiss parameter name and the plane
    static private String toTwissParameterKey( final String twissParameterName, final int plane ) {
        return "twiss." + plane + "." + twissParameterName;
    } */

    
    
 /*    * Save the document to the specified URL.
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
