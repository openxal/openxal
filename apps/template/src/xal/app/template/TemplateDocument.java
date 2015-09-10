/*
 * TemplateDocument.java
 *
 * Created on Fri Oct 10 14:08:21 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.template;

import xal.extension.application.*;

import java.net.URL;


/**
 * TemplateDocument
 *
 * @author  somebody
 */
class TemplateDocument extends XalDocument {
	/** Create a new empty document */
    public TemplateDocument() {
        this(null);
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public TemplateDocument(java.net.URL url) {
        setSource(url);
    }
    
    
    /**
     * Make a main window by instantiating the my custom window.
     */
    public void makeMainWindow() {
        mainWindow = new TemplateWindow(this);
    }

    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(URL url) {
    }
}




