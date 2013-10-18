/*
 * MyDocument.java
 *
 * Created on March 19, 2003, 1:32 PM
 */

package xal.app.injectionpainting;

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
import xal.tools.plot.*;


/**
 * WaveformDocument is a custom XalDocument for this application.
 * @author  cp3
 */
public class WaveformDocument extends XalDocument {
    /** control center for waveform development */
    protected WaveformFace _waveformFace;
    
    /** Create a new empty document */
    public WaveformDocument() {
        this( null );
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public WaveformDocument( final URL url ) {
        setSource( url );
    }
    
    
    /**
     * Make a main window by instantiating the my custom window.  Set the text 
     * pane to use the textDocument variable as its document.
     */
    public void makeMainWindow() {
	
	final WindowReference windowReference = getDefaultWindowReference( "MainWindow", this );
        mainWindow = (XalWindow)windowReference.getWindow();
	_waveformFace = new WaveformFace(windowReference );

	}
    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs( final URL url ) {
    }
}
