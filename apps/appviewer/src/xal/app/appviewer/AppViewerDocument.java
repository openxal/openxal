/*
 * AppViewerDocument.java
 *
 * Created on Fri Oct 10 14:08:21 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.appviewer;

import xal.application.*;

import java.net.URL;


/**
 * AppViewerDocument
 *
 * @author  tap
 */
class AppViewerDocument extends XalDocument {
	protected AppTableModel appTableModel;
	protected AppViewerModel model;
	
	
	/** Create a new empty document */
    public AppViewerDocument() {
        this(null);
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public AppViewerDocument(java.net.URL url) {
        setSource(url);
		try {
			model = new AppViewerModel();
			appTableModel = new AppTableModel(model);
		}
		catch(Exception exception) {
			Application.displayError("Exception registering services", "Exception registering services.  Will exit!", exception);
			System.exit(1);
		}
    }
    
    
    /**
     * Make a main window by instantiating the my custom window.  Set the text 
     * pane to use the textDocument variable as its document.
     */
    public void makeMainWindow() {
        mainWindow = new AppViewerWindow(this, appTableModel);
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
    private AppViewerWindow appWindow() {
        return (AppViewerWindow)mainWindow;
    }
	
	
	/**
	 * Get the main model.
	 * @return the main model.
	 */
	public AppViewerModel getModel() {
		return model;
	}
}




