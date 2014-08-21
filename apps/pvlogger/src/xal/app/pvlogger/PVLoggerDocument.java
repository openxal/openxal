/*
 * PVLoggerDocument.java
 *
 * Created on Wed Dec 3 15:00:00 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogger;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.service.pvlogger.*;
import xal.tools.database.*;
import xal.tools.apputils.PathPreferenceSelector;

import java.net.URL;


/**
 * PVLoggerDocument
 *
 * @author  tap
 */
class PVLoggerDocument extends AcceleratorDocument {	
	/** model for this document */
	protected DocumentModel _model;
	
	
	/** Create a new empty document */
    public PVLoggerDocument(LoggerModel mainModel) {
        this(null, mainModel);
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public PVLoggerDocument( final java.net.URL url, final LoggerModel mainModel ) {
        setSource(url);
		_model = new DocumentModel( mainModel );
    }
    
    
    /**
     * Make a main window by instantiating the my custom window.  Set the text 
     * pane to use the textDocument variable as its document.
     */
    public void makeMainWindow() {
        mainWindow = new PVLoggerWindow( this );
    }
	
    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(URL url) {
    }
    
	
	/**
	 * Override this method to prevent the accelerator from being loaded.
	 * @param filePath file path to the accelerator for which to first attempt to load
	 */
	public xal.smf.Accelerator applySelectedAcceleratorWithDefaultPath( final String filePath ) {
		return null;
	}
	
	
    /**
     * Convenience method for getting the main window cast to the proper subclass of XalWindow.
     * This allows me to avoid casting the window every time I reference it.
     * @return The main window cast to its dynamic runtime class
     */
    private PVLoggerWindow getWindow() {
        return (PVLoggerWindow)mainWindow;
    }
	
	
	/**
	 * Get the logger model
	 * @return The logger model
	 */
	public DocumentModel getModel() {
		return _model;
	}
	
	
	/**
	 * Override the inherited method to dispose of the model before the document is closed.
	 */
	public void willClose() {
		_model.dispose();
	}
}




