/*
 * MyDocument.java
 *
 * Created on March 19, 2003, 1:32 PM
 */

package xal.app.acceleratordemo;

import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

import xal.extension.application.*;
import xal.extension.smf.application.*;
import xal.smf.*;
import xal.tools.data.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.smf.data.XMLDataManager;


/**
 * DemoDocument is a custom AcceleratorDocument for the demo application.  Each document instance manages a single plain text document.  The document manages the data that is 
 * displayed in the window.  In this example we display information about the accelerator or the selected sequence as appropriate based on the most recent user action.
 * @author  t6p
 */
public class DemoDocument extends AcceleratorDocument implements DataListener {
	/** line separator */
	final private String LINE_SEPARATOR = System.getProperty( "line.separator" );
	
    /** The document for the text pane in the main window. */
    final protected PlainDocument TEXT_DOCUMENT;
    
	
    /** Create a new empty document */
    public DemoDocument() {
        this( null );
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public DemoDocument(java.net.URL url) {
        setSource( url );

        TEXT_DOCUMENT = makeTextDocument();

		if ( url != null ) {
			final DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
			update( documentAdaptor.childAdaptor( dataLabel() ) );
		}
    }
    
    
    /** Make and configure the main window. */
    public void makeMainWindow() {
        mainWindow = new DemoWindow(this);
        myWindow().getTextView().setDocument( TEXT_DOCUMENT);
 		updateDisplay();
   }    


    /**
     * Convenience method for getting the main window cast to the proper subclass of XalWindow.
     * This allows me to avoid casting the window every time I reference it.
     * @return The main window cast to its dynamic runtime class
     */
    private DemoWindow myWindow() {
        return (DemoWindow)mainWindow;
    }

    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(URL url) {
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



    /**
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    public String dataLabel() {
		return "AcceleratorDemo";
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
	}


    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "version", "1.0.0" );
        adaptor.setValue( "date", new java.util.Date().toString() );

		adaptor.setValue( "acceleratorPath", getAcceleratorFilePath() );
	}

    
    /** 
     * Instantiate a new PlainDocument that servers as the document for the text pane. Create a handler of text actions so we can determine if the document has changes that should be saved.
	 * @return the main text document
     */
    private PlainDocument makeTextDocument() {
        final PlainDocument textDocument = new PlainDocument();
        textDocument.addDocumentListener(new DocumentListener() {
            public void changedUpdate( final DocumentEvent event ) {
                setHasChanges( true );
            }
            public void removeUpdate( final DocumentEvent event ) {
                setHasChanges( true );
            }
            public void insertUpdate( final DocumentEvent event ) {
                setHasChanges( true );
            }
        });
		
		return textDocument;
    }
    
    
    /** Handle the accelerator changed event by displaying the elements of the accelerator in the main window. */
    public void acceleratorChanged() {
		updateDisplay();
    }
    
    
    /** Handle the selected sequence changed event by displaying the elements of the selected sequence in the main window. */
    public void selectedSequenceChanged() {
		updateDisplay();
    }
	
	
	/** Update the display to display the currently selected accelerator or sequence */
	protected void updateDisplay() {
		if ( selectedSequence != null ) {
			displaySelectedSequence();
		}
		else if ( accelerator != null ) {
			displayAccelerator();
		}
		else if ( mainWindow != null ) {
			myWindow().getTextView().setText( "No accelerator has been selected." );
		}
	}
	
	
	/** Display the accelerator in the main window */
	protected void displayAccelerator() {
        if ( accelerator != null && mainWindow != null ) {
            final StringBuffer description = new StringBuffer( "Selected Accelerator: " + accelerator.getId() + LINE_SEPARATOR );
            description.append( "Sequences: " + LINE_SEPARATOR );
            for ( final AcceleratorSeq sequence : accelerator.getSequences() ) {
                description.append( '\t' + sequence.getId() + LINE_SEPARATOR );
            }
            
            myWindow().getTextView().setText( description.toString() );
			
			final String message = "Accelerator changed to: \"" + accelerator.getId() + "\" with path: \"" + acceleratorFilePath + "\"";
			System.out.println( message );
			Logger.getLogger("global").log( Level.INFO, message );
        }
	}
	
	
	/** Display the selected sequence in the main window */
	protected void displaySelectedSequence() {
		if ( selectedSequence != null && mainWindow != null ) {
            StringBuffer description = new StringBuffer( "Selected Sequence: " + selectedSequence.getId() + LINE_SEPARATOR );
            description.append("\tNodes:  \tPositions: " + LINE_SEPARATOR );
            for ( final AcceleratorNode node : selectedSequence.getNodes() ) {
                description.append( '\t' + node.getId() + '\t' + selectedSequence.getPosition(node) + LINE_SEPARATOR );
            }
            
            myWindow().getTextView().setText( description.toString() );
        }		
	}
}
