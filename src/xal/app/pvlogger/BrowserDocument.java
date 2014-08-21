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

import xal.service.pvlogger.*;
import xal.extension.application.smf.*;
import xal.extension.application.*;
import xal.tools.database.*;
import xal.tools.apputils.PathPreferenceSelector;
import xal.service.pvlogger.apputils.browser.*;

import java.net.URL;


/**
 * PVLoggerDocument
 *
 * @author  tap
 */
class BrowserDocument extends AcceleratorDocument {
	/** main model for this document */
	protected BrowserModel _model;
	
	
	/** Create a new empty document */
    public BrowserDocument() {
        this(null);
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public BrowserDocument(java.net.URL url) {
        setSource(url);
		_model = new BrowserModel();
    }
    
    
    /**
     * Make a main window by instantiating the my custom window.  Set the text 
     * pane to use the textDocument variable as its document.
     */
    public void makeMainWindow() {
        mainWindow = new BrowserWindow(this, _model);
    }
	
    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(URL url) {
    }
    
    
    /**
     * Convenience method for getting the main window cast to the proper subclass of XalWindow.
     * This allows me to avoid casting the window every time I reference it.
     * @return The main window cast to its dynamic runtime class
     */
    private BrowserWindow getWindow() {
        return (BrowserWindow)mainWindow;
    }


	/**
	 * Override this method to prevent the accelerator from being loaded.
	 * @param filePath file path to the accelerator for which to first attempt to load
	 */
	public xal.smf.Accelerator applySelectedAcceleratorWithDefaultPath( final String filePath ) {
		return null;
	}

	
	/**
	 * Get the logger model
	 * @return The logger model
	 */
	public BrowserModel getModel() {
		return _model;
	}
	
	
	/**
	 * Override the inherited method to dispose of the model before the document is closed.
	 */
	public void willClose() {
	}
}




