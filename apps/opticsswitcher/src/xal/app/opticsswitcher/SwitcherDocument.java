/*
 * SwitcherDocument.java
 *
 * Created on November 8, 2011, 3:28 PM
 */

package xal.app.opticsswitcher;

import java.net.*;
import java.io.*;
import java.awt.Color;
import javax.swing.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.logging.*;
import java.util.Vector;

import xal.extension.application.*;
import xal.extension.bricks.WindowReference;
import xal.extension.widgets.plot.*;
import xal.smf.data.OpticsSwitcher;


/**
 * SwitcherDocument is a custom XalDocument for this application.
 * @author  t6p
 */
public class SwitcherDocument extends XalDocument {
    /** Create a new empty document */
    public SwitcherDocument() {
        this( null );
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public SwitcherDocument( final URL url ) {
        setSource( url );
    }
    
    
    /**
     * Make a main window by instantiating the my custom window.  Set the text 
     * pane to use the textDocument variable as its document.
     */
    public void makeMainWindow() {
		final WindowReference windowReference = getDefaultWindowReference( "MainWindow", this );
        mainWindow = (DefaultXalWindow)windowReference.getWindow();

        final OpticsSwitcher opticsSwitcher = OpticsSwitcher.getInstanceForHostedFrame();
		final Box editorContainer = (Box)windowReference.getView( "EditorContainer" );
		editorContainer.add( opticsSwitcher.getView() );
	}
    
    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs( final URL url ) {}
}
