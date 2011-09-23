/*
 * LaunchDocument.java
 *
 * Created on Fri March 5 9:15:32 EDT 2004
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.launcher;

import xal.application.*;
import xal.tools.data.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.IconLib;
import xal.tools.bricks.WindowReference;
import xal.tools.messaging.MessageCenter;

import java.net.URL;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.text.*;


/**
 * LaunchDocument is the document for the launcher application.
 *
 * @author t6p
 */
class LaunchDocument extends XalDocument implements DataListener, LaunchBoardListener {
	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "LaunchDocument";
	
	/** The main model of this document */
	final protected LaunchModel MODEL;
	
	/** main window reference */
	final WindowReference WINDOW_REFERENCE;
	
	
	/** Create a new empty document */
    public LaunchDocument() {
        this( null );
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public LaunchDocument( final java.net.URL url ) {
		MessageCenter.defaultCenter().registerTarget( this, LaunchBoardListener.class );
		
        setSource( url );
		
		WINDOW_REFERENCE = getDefaultWindowReference( "MainWindow", this );

		MODEL = new LaunchModel();
		MODEL.addLaunchModelListener( newLaunchModelHandler() );
						
        if ( url != null ) {
            System.out.println( "Opening document: " + url.toString() );
            DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
            update( documentAdaptor.childAdaptor( dataLabel() ) );
			setHasChanges( getSource() == null );
        }
		else {
			MODEL.preConfigure();
		}
		
		new RunController( MODEL, WINDOW_REFERENCE );
		new FileWatcherController( MODEL, WINDOW_REFERENCE );
		new RulesController( MODEL, WINDOW_REFERENCE );
		new HostConfigurationController( MODEL, WINDOW_REFERENCE );
    }
    
    
    /** Make a main window by instantiating the my custom window. */
    public void makeMainWindow() {
        mainWindow = (XalWindow)WINDOW_REFERENCE.getWindow();
		setHasChanges( getSource() == null );
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
			
			// if there is no default document then ask whether to set the current one to the default one
			if ( url != null && PreferenceController.getDefaultDocumentURL() == null ) {
				final int selection = JOptionPane.showConfirmDialog( mainWindow, "Make this document the default launcher document?", "Default Launcher",  JOptionPane.YES_NO_OPTION );
				if ( selection == JOptionPane.YES_OPTION ) {
					PreferenceController.setDefaultDocumentURLSpec( url.toString() );
				}
			}
        }
        catch( XmlDataAdaptor.WriteException exception ) {
			if ( exception.getCause() instanceof java.io.FileNotFoundException ) {
				System.err.println(exception);
				displayError("Save Failed!", "Save failed due to a file access exception!", exception);
			}
			else if ( exception.getCause() instanceof java.io.IOException ) {
				System.err.println(exception);
				displayError("Save Failed!", "Save failed due to a file IO exception!", exception);
			}
			else {
				exception.printStackTrace();
				displayError("Save Failed!", "Save failed due to an internal write exception!", exception);
			}
        }
        catch( Exception exception ) {
			exception.printStackTrace();
            displayError("Save Failed!", "Save failed due to an internal exception!", exception);
        }
    }
	
	
	/**
	 * Get the launch model which represents the main model of this document
	 * @return the main model of this document
	 */
	public LaunchModel getModel() {
		return MODEL;
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
        DataAdaptor modelAdaptor = adaptor.childAdaptor( MODEL.dataLabel() );
        MODEL.update( modelAdaptor );
    }
    
    
    /**
     * Instructs the receiver to write its data to the adaptor for external storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write( final DataAdaptor adaptor ) {
        adaptor.setValue( "version", "2.0.0" );
        adaptor.setValue( "date", new java.util.Date().toString() );
        adaptor.writeNode( MODEL );
    }
	
	
	/** get a launch model handler */
	private LaunchModelListener newLaunchModelHandler() {
		return new LaunchModelListener() {
			/** handle the event indicating that the model has been modified */
			public void modified( final LaunchModel model ) {
				setHasChanges( true );
			}			
		};
	}
	
	
	/** Post of a standard message */
	public void postMessage( final Object source,  final String message ) {
		final JTextComponent messageField = (JTextComponent)WINDOW_REFERENCE.getView( "MessageField" );
		messageField.setText( message );
	}
	
	
	
	/**
	 * Post an error message
	 * @param source The source of the message
	 * @param message The message posted
	 */
	public void postErrorMessage(Object source, String message) {
		postMessage( source, "<html><span style=\"color: red;\">" + message + "</span></html>" );
	}	
}




