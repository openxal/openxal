/*
 * MyDocument.java
 *
 * Created on March 19, 2003, 1:32 PM
 */

package xal.app.demo;

import java.net.*;
import java.io.*;
import java.awt.Color;
import javax.swing.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.logging.*;

import xal.extension.application.*;

/**
 * MyDocument is a custom XalDocument for my application.  Each document instance 
 * manages a single plain text document.  The document manages the data that is 
 * displayed in the window.
 *
 * @author  t6p
 */
public class MyDocument extends XalDocument {
    /**
     * The document for the text pane in the main window.
     */
    protected PlainDocument textDocument;
    
    /** Create a new empty document */
    public MyDocument() {
        this(null);
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public MyDocument(java.net.URL url) {
        setSource(url);
        makeTextDocument();
        
        if ( url == null )  return;
        
        try {
            final int charBufferSize = 1000;
            InputStream inputStream = url.openStream();
            BufferedReader reader = new BufferedReader( new InputStreamReader(inputStream) );
            
            StringBuffer stringBuffer = new StringBuffer();
            char[] charBuffer = new char[charBufferSize];
            int numRead = 0;
            while ( (numRead = reader.read(charBuffer, 0, charBufferSize)) != -1 ) {
                stringBuffer.append(charBuffer, 0, numRead);
            }
            
            textDocument.insertString(0, stringBuffer.toString(), null);
            setHasChanges(false);
        }
        catch(java.io.IOException exception) {
            throw new RuntimeException( exception.getMessage() );
        }
        catch(BadLocationException exception) {
            throw new RuntimeException( exception.getMessage() );
        }
    }
    
    
    /**
     * Make a main window by instantiating the my custom window.  Set the text 
     * pane to use the textDocument variable as its document.
     */
    public void makeMainWindow() {
        mainWindow = new MyWindow(this);
        myWindow().getTextView().setDocument(textDocument);        
    }

    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(URL url) {
        try {
            int length = textDocument.getLength();
            String text = textDocument.getText( 0, length );
            
            File file = new File( url.toURI() );
            if ( !file.exists() ) {
                file.createNewFile();
            }
            
            FileWriter writer = new FileWriter( file );
            writer.write( text, 0, text.length() );
            writer.flush();
            setHasChanges( false );
        }
        catch(BadLocationException exception) {
			System.err.println(exception);
			Logger.getLogger("global").log( Level.WARNING, "Save Failed", exception );
			displayWarning("Save Failed!", "Save Failed due to an internal exception!", exception);
        }
        catch(IOException exception) {
			System.err.println(exception);
			Logger.getLogger("global").log( Level.WARNING, "Save Failed", exception );
			displayWarning("Save Failed!", "Save Failed due to an internal exception!", exception);
        }
		catch( java.net.URISyntaxException exception ) {
			System.err.println( exception );
			Logger.getLogger("global").log( Level.WARNING, "Save Failed", exception );
			displayWarning( "Save Failed!", "Save Failed due to an internal exception!", exception );
        }
	}
    
    
    /**
     * Convenience method for getting the main window cast to the proper subclass of XalWindow.
     * This allows me to avoid casting the window every time I reference it.
     * @return The main window cast to its dynamic runtime class
     */
    private MyWindow myWindow() {
        return (MyWindow)mainWindow;
    }
        
    
    /**
     * Register custom actions for the document.
     * @param commander The commander with which to register the custom commands.
     */
    public void customizeCommands( final Commander commander ) {
        // define the "Export" demo action
        final Action exportAction = new AbstractAction( "export-data" ) {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                System.out.println( "Exporting data..." );
				Logger.getLogger("global").log( Level.INFO, "Exporting data." );
                displayConfirmDialog( "Demo Export", "Just simulating the export of data for demo. No data actually exported..." );
            }
        };
        commander.registerAction( exportAction );
    }

	
    /** 
     * Instantiate a new PlainDocument that servers as the document for the text pane.
     * Create a handler of text actions so we can determine if the document has 
     * changes that should be saved.
     */
    private void makeTextDocument() {
        textDocument = new PlainDocument();
        textDocument.addDocumentListener(new DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent evt) {
                setHasChanges(true);
            }
            public void removeUpdate(DocumentEvent evt) {
                setHasChanges(true);
            }
            public void insertUpdate(DocumentEvent evt) {
                setHasChanges(true);
            }
        });
    }
    
    
    /**
     * Edit preferences for the document.  Here we simply change the background color of the 
     * text pane.
     */
    void editPreferences() {
        Object[] colors = {Color.white, Color.gray, Color.red, Color.green, Color.blue};
        Color selection = (Color)JOptionPane.showInputDialog(myWindow(), "Choose a background color:", "Background Color", JOptionPane.INFORMATION_MESSAGE,
        null, colors, myWindow().getTextView().getBackground());
        if ( selection != null ) {
            myWindow().getTextView().setBackground(selection);
        }
    }
}
