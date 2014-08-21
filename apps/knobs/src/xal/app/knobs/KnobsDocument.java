//
//  KnobsDocument.java
//  xal
//
//  Created by Thomas Pelaia on 9/13/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.smf.*;
import xal.tools.data.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.smf.data.XMLDataManager;
import xal.tools.messaging.MessageCenter;

import java.net.URL;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;


/** Knobs document */
public class KnobsDocument extends AcceleratorDocument implements DataListener {
	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "KnobsDocument";
	
	/** The main model of this document */
	final protected KnobsModel _model;
	
	/** horizontal bump maker action */
	protected Action MAKE_HORIZONTAL_BUMP_ACTION;
	
	/** vertical bump maker action */
	protected Action MAKE_VERTICAL_BUMP_ACTION;
	
	
	/** Create a new empty document */
    public KnobsDocument() {
        this( null );
    }
    
    
    /** 
	* Create a new document loaded from the URL file 
	* @param url The URL of the file to load into the new document.
	*/
    public KnobsDocument( final URL url ) {
        setSource( url );
			
		_model = new KnobsModel( getAccelerator(), getSelectedSequence() );
		_model.addKnobsModelListener( new KnobsModelListener() {
			public void groupsChanged( final KnobsModel model ) {
				setHasChanges( true );
			}
			public void modified( final KnobsModel model ) {
				setHasChanges(true);
			}
		});
		
        if ( url != null ) {
            System.out.println( "Opening document: " + url.toString() );
            final DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
            update( documentAdaptor.childAdaptor( dataLabel() ) );
            setHasChanges( false );
        }
    }
    
    
    /** Make a main window by instantiating the my custom window. */
    public void makeMainWindow() {
        mainWindow = new KnobsWindow( this );
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
        catch(Exception exception) {
			exception.printStackTrace();
            displayError( "Save Failed!", "Save failed due to an internal exception!", exception );
        }
    }	
    
    
    /** Handle the accelerator change event. */
    public void acceleratorChanged() {
		if ( _model != null ) {
			_model.setAccelerator( getAccelerator() );
			if ( getKnobsWindow() != null ) {
				getKnobsWindow().postMessage( this, "Accelerator loaded:  " + getAcceleratorFilePath() );
			}
		}
    }
    
    
    /** Handle the sequence change event. */
    public void selectedSequenceChanged() {
		if ( _model != null ) {
			_model.setSequence( getSelectedSequence() );
		}
		if ( MAKE_HORIZONTAL_BUMP_ACTION != null ) {
			MAKE_HORIZONTAL_BUMP_ACTION.setEnabled( _model != null && getSelectedSequence() != null );
		}
		if ( MAKE_VERTICAL_BUMP_ACTION != null ) {
			MAKE_VERTICAL_BUMP_ACTION.setEnabled( _model != null & getSelectedSequence() != null );
		}
		
		if ( getKnobsWindow() != null ) {
			final AcceleratorSeq sequence = getSelectedSequence();
			getKnobsWindow().postMessage( this, "Sequence selected:  " + ( sequence != null ? sequence.getId() : "None" ) );
		}
    }
	
	
	/**
	 * Get this document's main model
	 * @return the main model of this document
	 */
	public KnobsModel getModel() {
		return _model;
	}
	
	
	/**
	 * Get the main window cast as a knobs window.
	 * @return the knobs window associated with this document
	 */
	public KnobsWindow getKnobsWindow() {
		return (KnobsWindow)getMainWindow();
	}
    
    
    /**
	 * Register actions specific to this document instance.  
     * @param commander The commander with which to register the custom commands.
     */
    public void customizeCommands( final Commander commander ) {
        MAKE_HORIZONTAL_BUMP_ACTION = new AbstractAction( "make-horizontal-bumps" ) {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
				makeBumpKnobs( PlaneAdaptor.getHorizontalAdaptor() );
            }
        };
        commander.registerAction( MAKE_HORIZONTAL_BUMP_ACTION );
		MAKE_HORIZONTAL_BUMP_ACTION.setEnabled( _model != null && getSelectedSequence() != null );
		
        MAKE_VERTICAL_BUMP_ACTION = new AbstractAction( "make-vertical-bumps" ) {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
				makeBumpKnobs( PlaneAdaptor.getVerticalAdaptor() );
            }
        };        
        commander.registerAction( MAKE_VERTICAL_BUMP_ACTION );
		MAKE_VERTICAL_BUMP_ACTION.setEnabled( _model != null && getSelectedSequence() != null );
	}
	
	
	/** make the bump knobs */
	protected void makeBumpKnobs( final PlaneAdaptor planeAdaptor ) {
		new BumpGeneratorDialog( getMainWindow(), _model, getKnobsWindow().getController().getSelectedGroup(), planeAdaptor ).setVisible( true );
	}
		
    
    /** 
	* dataLabel() provides the name used to identify the class in an external data source.
	* @return The tag for this data node.
	*/
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /**
	 * Instructs the receiver to update its data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update( final DataAdaptor adaptor ) {
		if ( adaptor.hasAttribute( "acceleratorPath" ) ) {
			final String acceleratorPath = adaptor.stringValue( "acceleratorPath" );
			applySelectedAcceleratorWithDefaultPath( acceleratorPath );
		}
		
        DataAdaptor modelAdaptor = adaptor.childAdaptor( _model.dataLabel() );
        _model.update( modelAdaptor );
    }
    
    
    /**
	 * Instructs the receiver to write its data to the adaptor for external storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write( final DataAdaptor adaptor ) {
        adaptor.setValue( "version", "1.0.0" );
        adaptor.setValue( "date", new java.util.Date().toString() );
		
		if ( getAccelerator() != null ) {
			adaptor.setValue( "acceleratorPath", getAcceleratorFilePath() );
		}
		
        adaptor.writeNode( _model );
    }
}
