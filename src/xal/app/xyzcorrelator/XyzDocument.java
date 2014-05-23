/*
 * MyDocument.java
 *
 * Created on April 19, 2003, 1:32 PM
 */

package xal.app.xyzcorrelator;

import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Color;
import java.util.*;

import xal.extension.application.*;
import xal.extension.bricks.WindowReference;
import xal.extension.widgets.plot.*;
import xal.extension.application.smf.*;
import xal.smf.*;
import xal.smf.data.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.data.*;
import xal.ca.ChannelTimeRecord;
import xal.tools.correlator.Correlation;


/** Document to manage a set of two or three correlated PVs */
public class XyzDocument extends AcceleratorDocument implements DataListener, CorrelationModelListener {
 	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "XyzDocument";
	
	/** Model for managing the correlations */
	final private CorrelationModel MODEL;
	
	/** bricks window reference */
	private WindowReference _windowReference;
	
	/** binds the model to the views */
	private CorrelationController _correlationController;
	
	
	/** Constructor */
    public XyzDocument() {
        this( null );
    }
	
	
	/** Primary Constructor */
	public XyzDocument( final URL url ) {		
		MODEL = new CorrelationModel();
		MODEL.addCorrelationModelListener( this );
		
		final WindowReference windowReference = getDefaultWindowReference( "MainWindow", this );
		_windowReference = windowReference;
		
		_correlationController = new CorrelationController( this, windowReference );

		setSource( url );
		
        if ( url != null ) {
            System.out.println( "Opening document: " + url.toString() );
            final DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
            update( documentAdaptor.childAdaptor( dataLabel() ) );
        }
		setHasChanges( false );
	}
	
	
	/** get the model */
	public CorrelationModel getModel() {
		return MODEL;
	}
	
	
	/** Make a main window by instantiating the my custom window. */
    public void makeMainWindow() {				
        mainWindow = (XalWindow)_windowReference.getWindow();
		setHasChanges( false );
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
	
    
    /** Hook for handling the accelerator change event.  Subclasses should override this method to provide custom handling.  The default handler does nothing. */
    public void acceleratorChanged() {
		selectedSequenceChanged();
		MODEL.setAccelerator( getAccelerator() );
		setHasChanges( true );
    }
    
    
    /* * Hook for handling the selected sequence change event.  Subclasses should override this method to provide custom handling.  The default handler does nothing. */
    public void selectedSequenceChanged() {
		setHasChanges( true );
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
		
		final DataAdaptor modelAdaptor = adaptor.childAdaptor( CorrelationModel.DATA_LABEL );
		if ( modelAdaptor != null )  MODEL.update( modelAdaptor );
		
		final DataAdaptor correlationAdaptor = adaptor.childAdaptor( CorrelationController.DATA_LABEL );
		if ( correlationAdaptor != null )  _correlationController.update( correlationAdaptor );
    }
    
    
    /** Instructs the receiver to write its data to the adaptor for external storage. */
    public void write( final DataAdaptor adaptor ) {
        adaptor.setValue( "version", "1.0.0" );
        adaptor.setValue( "date", new java.util.Date().toString() );
		
		adaptor.writeNode( MODEL );
		adaptor.writeNode( _correlationController );
		
		if ( getAccelerator() != null ) {
			adaptor.setValue( "acceleratorPath", getAcceleratorFilePath() );
			
			final AcceleratorSeq sequence = getSelectedSequence();
			if ( sequence != null ) {
				adaptor.setValue( "sequence", sequence.getId() );
			}
		}		
    }
	
	
	/** the plotting channels have changed */
	public void plottingChannelsChanged( final List<xal.ca.Channel> channels ) {
		setHasChanges( true );
	}
	
	
	/** the monitored channels have changed */
	public void monitoredChannelsChanged( final List<xal.ca.Channel> channels ) {
		setHasChanges( true );
	}
	
	
	/** correlation captured */
	public void correlationCaptured( final Correlation<ChannelTimeRecord> correlation, final List<ChannelTimeRecord> plotRecords ) {}
}
