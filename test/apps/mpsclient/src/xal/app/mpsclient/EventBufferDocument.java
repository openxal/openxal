/*
 * EventBufferDocument.java
 *
 * Created on Thu Apr 15 08:44:03 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.mpsclient;

import xal.extension.application.*;

import java.net.URL;
import java.io.*;
import javax.swing.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.logging.*;


/**
 * EventBufferDocument
 *
 * @author  tap
 */
public class EventBufferDocument extends XalDocument {
	final protected RemoteMPSRecord _requestHandler;
	final protected int _mpsType;
	
	
	/** Create a new empty document */
    public EventBufferDocument() {
        this(null);
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public EventBufferDocument(java.net.URL url) {
		this(null, 0);
    }
	
	
	/**
	 * Primary Constructor
	 */
	public EventBufferDocument(RemoteMPSRecord requestHandler, int mpsType) {
		super();
		_requestHandler = requestHandler;
		_mpsType = mpsType;
		generateDocumentTitle();
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
		//return new String[] {"html", "txt"};
	}
    
    
    /**
     * Make a main window by instantiating the custom window.
     */
    public void makeMainWindow() {
        mainWindow = new EventBufferWindow(this, _requestHandler, _mpsType);
    }
	
	
	/**
	 * Generate and set the title for this document.  By default the title is set to 
	 * the file path of the document or the default empty document file path if the document
	 * does not have a file store.
	 */
	public void generateDocumentTitle() {
		if ( _requestHandler == null ) {
			setTitle("Buffer");
		}
		else {
			setTitle( _requestHandler.getMPSTypes().get(_mpsType).toString() + " Buffer");
			//setTitle( _requestHandler.getMPSTypes().get(_mpsType).toString() + " Buffer - " + super.getDisplayFilePath() );
		}
	}
    
    
    /**
     * Subclasses should override this method if this document should use a menu definition
	 * other than the default specified in application adaptor.  The document menu inherits the
	 * application menu definition.  This custom path allows the document to modify the
	 * application wide definitions for this document.  By default this method returns null.
     * @return The menu definition properties file path in classpath notation
     */
    public String getCustomMenuDefinitionResource() {
		return "buffer_menu.properties";
    }
	
	
	/**
	 * Convenience method for getting the query window cast to the QueryWindow class
	 * @return this document's query window
	 */
	protected EventBufferWindow bufferWindow() {
		return (EventBufferWindow)mainWindow;
	}

    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(URL url) {
        try {
            File file = new File(url.getPath());
            if ( !file.exists() ) {
                file.createNewFile();
            }
			
			if ( url.getPath().toUpperCase().endsWith("HTML") ) {
				bufferWindow().writeHTMLTo(file);
			}
			else {
				bufferWindow().writeTextTo(file);
			}
			
            setHasChanges(false);
        }
        catch(IOException exception) {
            System.err.println(exception);
			Logger.getLogger("global").log( Level.WARNING, "Save Failed", exception );
            displayWarning("Save Failed!", "Save Failed due to an internal exception!", exception);
        }
	}	
}

