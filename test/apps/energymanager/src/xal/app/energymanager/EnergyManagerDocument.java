//
//  EnergyManagerDocument.java
//  xal
//
//  Created by Thomas Pelaia on 2/2/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.smf.*;
import xal.model.probe.Probe;
import xal.model.probe.traj.ProbeState;
import xal.smf.data.XMLDataManager;
import xal.tools.data.*;
import xal.tools.xml.XmlDataAdaptor;

import java.net.URL;
import java.util.List;


/** Main document. */
public class EnergyManagerDocument extends AcceleratorDocument implements DataListener, EnergyManagerListener {
	/** this document's model */
	protected EnergyManager _model; 
	
	
	/** Create a new empty document */
    public EnergyManagerDocument() {
        this( null );
    }
    
    
    /** 
	* Create a new document loaded from the URL file 
	* @param url The URL of the file to load into the new document.
	*/
    public EnergyManagerDocument( final java.net.URL url ) {
        setSource( url );
		
		_model = new EnergyManager();
		
        if ( url != null ) {
			System.out.println( "Opening document: " + url );
			final DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
			update( documentAdaptor.childAdaptor( dataLabel() ) );
		}
		
		setHasChanges( false );
    }
    
    
    /** Make a main window by instantiating the my custom window. */
    public void makeMainWindow() {
        mainWindow = new EnergyManagerWindow( this );
		_model.addEnergyManagerListener( this );
		_model.addEnergyManagerListener( (EnergyManagerWindow)mainWindow );
		
		setHasChanges( false );		// need this to avoid document change indication upon loading a new document
    }
	
	
	/**
	 * Convenience method to get this document's corresponding window cast as an Energy Manager window.
	 * @return this document's corresponding window
	 */
	protected EnergyManagerWindow getEnergyManagerWindow() {
		return (EnergyManagerWindow)getMainWindow();
	}
	
    
    /**
	* Save the document to the specified URL.
	* @param url The URL to which the document should be saved.
	*/
    public void saveDocumentAs( final URL url ) {
        try {
            final XmlDataAdaptor documentAdaptor = XmlDataAdaptor.newEmptyDocumentAdaptor();
            documentAdaptor.writeNode( this );
            documentAdaptor.writeToUrl( url );
            setHasChanges( false );
        }
        catch( XmlDataAdaptor.WriteException exception ) {
			if ( exception.getCause() instanceof java.io.FileNotFoundException ) {
				System.err.println( exception );
				displayError( "Save Failed!", "Save failed due to a file access exception!", exception );
			}
			else if ( exception.getCause() instanceof java.io.IOException ) {
				System.err.println( exception );
				displayError( "Save Failed!", "Save failed due to a file IO exception!", exception );
			}
			else {
				exception.printStackTrace();
				displayError( "Save Failed!", "Save failed due to an internal write exception!", exception );
			}
        }
        catch( Exception exception ) {
			exception.printStackTrace();
            displayError( "Save Failed!", "Save failed due to an internal exception!", exception );
        }
    }
	
	
	/** Dispose of this document's resources. */
	public void freeCustomResources() {
		if ( _model != null ) {
			_model.removeEnergyManagerListener( this );
			_model.removeEnergyManagerListener( (EnergyManagerWindow)getMainWindow() );
			_model.dispose();
			_model = null;
		}
	}
	
	
	/**
	 * Get the orbit model
	 * @return the orbit model
	 */
	public EnergyManager getModel() {
		return _model;
	}
	
	
	/**
	 * Override this method to post a message that we are loading the default accelerator.
	 * @return true if the default accelerator was successfully loaded
	 */
	protected boolean loadDefaultAccelerator() {
		System.out.println( "Loading default accelerator..." );
		return super.loadDefaultAccelerator();
	}
		
    
    /**
	 * Override this method to post a message that we are loading a new accelerator.
     * @param newAccelerator The accelerator managed by this document.
     * @param newPath The path to the accelerator managed by this document.
     */
    public void setAccelerator( final Accelerator newAccelerator, final String newPath ) {
		System.out.println( "Loading new accelerator..." );
		super.setAccelerator( newAccelerator, newPath );
    }
	
	
    /** 
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    public String dataLabel() {
		return "EnergyManagerDocument";
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		if ( adaptor.hasAttribute( "acceleratorPath" ) ) {
			final String acceleratorPath = adaptor.stringValue( "acceleratorPath" );
			applySelectedAcceleratorWithDefaultPath( acceleratorPath );
		}
		
		if ( adaptor.hasAttribute( "sequence" ) ) {
			final String sequenceID = adaptor.stringValue( "sequence" );
			setSelectedSequence( getAccelerator().getSequence( sequenceID ) );
		}
		else {
			final DataAdaptor comboAdaptor = adaptor.childAdaptor( "comboseq" );
			if ( comboAdaptor != null ) {
				setSelectedSequence( AcceleratorSeqCombo.getInstance( getAccelerator(), comboAdaptor ) );				
			}
		}
		
		final DataAdaptor modelAdaptor = adaptor.childAdaptor( _model.dataLabel() );
        _model.update( modelAdaptor );
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "version", "1.0.0" );
        adaptor.setValue( "date", new java.util.Date().toString() );
		
		adaptor.setValue( "acceleratorPath", getAcceleratorFilePath() );
		
		final AcceleratorSeq sequence = getSelectedSequence();
		if ( sequence instanceof AcceleratorSeqCombo ) {
			final DataAdaptor comboAdaptor = adaptor.createChild( "comboseq" );
			((AcceleratorSeqCombo)sequence).write( comboAdaptor );
		}
		else {
			adaptor.setValue( "sequence", getSelectedSequence().getId() );
		}
		
        adaptor.writeNode( _model );		
	}
	
	
	/**
	 * Post a message to the main window.
	 * @param message the message to post
	 */
	public void postMessage( final String message ) {
		if ( mainWindow != null ) {
			getEnergyManagerWindow().postMessage( message );
		}
	}
	
    
    /**
	* Hook for handling the accelerator change event.  Subclasses should override
	* this method to provide custom handling.  The default handler does nothing.
	*/
    public void acceleratorChanged() {
		_model.setSequence( null );
		setHasChanges( true );
		
		postMessage( "Accelerator changed to: " + getAcceleratorFilePath() );
    }
    
    
    /**
	 * Override this method to post a message that we are loading a new sequence.
     * @param selection The accelerator sequence to be managed by this document.
     */
    public void setSelectedSequence( final AcceleratorSeq selection ) {
		if ( selection != null ) {
			postMessage( "Loading new sequence...  " + selection.getId() );			
		}
		
		super.setSelectedSequence( selection );
    }
    
    
    /**
	* Hook for handling the selected sequence change event.  Subclasses should override
	* this method to provide custom handling.  The default handler does nothing.
	*/
    public void selectedSequenceChanged() {
		_model.setSequence( this.selectedSequence );
    }
	
	
    /**
	* Hook for handling the accelerator file path change event.  Subclasses should override
	* this method to provide custom handling.  The default handler does nothing.
	*/
    public void acceleratorFilePathChanged() {
		setHasChanges( true );
    }
	
	
	/**
	 * Handle the event indicating that the list of evaluation nodes has changed.
	 * @param model the model posting the event
	 * @param range the new position range of evaluation nodes (first position, last position)
	 * @param nodes the new evaluation nodes
	 */
	public void evaluationNodesChanged( final EnergyManager model, final double[] range, final List<AcceleratorNode> nodes ) {
		setHasChanges( true );
	}
	
	
	/**
	 * Handle the event indicating that the model's entrance probe has changed.
	 * @param model the model posting the event
	 * @param entranceProbe the new entrance probe
	 */
	public void entranceProbeChanged( final EnergyManager model, final Probe<?> entranceProbe ) {
		setHasChanges( true );
	}
	
	
	/** 
	 * Handle the event indicating that the model's sequence has changed. 
	 * @param model the model posting the event
	 * @param sequence the model's new sequence
	 * @param nodeAgents the model's node agents
	 * @param parameters the list of live parameters
	 */
	public void sequenceChanged( final EnergyManager model, final AcceleratorSeq sequence, final List<NodeAgent> nodeAgents, final List<LiveParameter> parameters ) {
		setHasChanges( true );
	}
	
	
	/**
	 * Event indicating that a live parameter has been modified.
	 * @param model the source of the event.
	 * @param parameter the parameter which has changed.
	 */
	public void liveParameterModified( final EnergyManager model, final LiveParameter parameter ) {
		setHasChanges( true );
	}
	
	
	/**
	 * Event indicating that the optimizer settings have changed.
	 * @param model the source of the event.
	 * @param optimizer the optimizer whose settings have changed.
	 */
	public void optimizerSettingsChanged( final EnergyManager model, final OpticsOptimizer optimizer ) {
		setHasChanges( true );
	}
	
	
	/**
	 * Event indicating that the optimizer has found a new optimal solution.
	 * @param model the source of the event.
	 * @param optimizer the optimizer which has found a new optimial solution.
	 */
	public void newOptimalSolutionFound( final EnergyManager model, final OpticsOptimizer optimizer ) {
		setHasChanges( true );
	}
	
	
	/**
	 * Event indicating that the optimizer has started.
	 * @param model the source of the event.
	 * @param optimizer the optimizer which has started.
	 */
	public void optimizerStarted( final EnergyManager model, final OpticsOptimizer optimizer ) {
		setHasChanges( true );
	}
}
