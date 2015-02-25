/*
 * VADocument.java
 *
 * Created on Thu Feb 19 15:16:57 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.dbbrowser;

import xal.extension.application.*;

import java.net.URL;


/**
 * BrowserDocument
 *
 * @author  t6p
 */
class BrowserDocument extends XalDocument {
	/** browser model */
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
		_model = new BrowserModel();
        setSource(url);
    }
    
    
    /**
     * Subclasses should implement this method to return the array of file 
     * suffixes identifying the files that can be written by the document.
	 * By default this method returns the same types as specified by the 
	 * application adaptor.
     * @return An array of file suffixes corresponding to writable files
     */
    public String[] writableDocumentTypes() {
        return new String[0];
	}
	
	
	/**
	 * Generate and set the title for this document.  By default the title is set to 
	 * the file path of the document or the default emtpy document file path if the document
	 * does not have a file store.
	 */
	public void generateDocumentTitle() {
		if ( _model != null ) {
			String user = _model.getUser();
			String databaseURL = _model.getDatabaseURL();
			String title = (user != null) ? user + " - " + databaseURL : "Disconnected...";
			setTitle(title);
		}
		else {
			super.generateDocumentTitle();
		}
	}
	
	
	/**
	 * Update the document title.
	 */
	public void updateTitle() {
		generateDocumentTitle();
	}
    
    
    /**
     * Make a main window by instantiating the my custom window.
     */
    public void makeMainWindow() {
        mainWindow = new BrowserWindow(this, _model);
    }
    
    
    /** Hook indicating that the window was opened. */
    public void windowOpened() {
        final BrowserWindow window = (BrowserWindow)mainWindow;
        window.showConnectionRequest();
    }

    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(URL url) {
    }
}




