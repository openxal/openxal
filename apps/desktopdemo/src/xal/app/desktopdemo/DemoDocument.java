//
//  DemoDocument.java
//  xal
//
//  Created by Thomas Pelaia on 3/31/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.desktopdemo;

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


/** demonstration document */
public class DemoDocument extends XalInternalDocument {
    /** The document for the text pane in the main window. */
    protected PlainDocument _textDocument;
    
	
    /** Constructor */
    public DemoDocument() {
        this( null );
    }
    
    
    /** 
	* Primary constructor which builds a document from the URL content.
	* @param url The URL of the file to load into the new document.
	*/
    public DemoDocument( final URL url ) {
        setSource(url);
        makeTextDocument();
        
        if ( url == null )  return;
        
        try {
            final int charBufferSize = 1000;
            InputStream inputStream = url.openStream();
            BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );
            
            StringBuffer stringBuffer = new StringBuffer();
            char[] charBuffer = new char[charBufferSize];
            int numRead = 0;
            while ( ( numRead = reader.read(charBuffer, 0, charBufferSize) ) != -1 ) {
                stringBuffer.append( charBuffer, 0, numRead );
            }
            
            _textDocument.insertString( 0, stringBuffer.toString(), null );
            setHasChanges( false );
        }
        catch( java.io.IOException exception ) {
            throw new RuntimeException( exception.getMessage() );
        }
        catch( BadLocationException exception ) {
            throw new RuntimeException( exception.getMessage() );
        }
    }
    
    
    /**
	 * Make a main window by instantiating the my custom window.  Set the text 
     * pane to use the textDocument variable as its document.
     */
    public void makeMainWindow() {
        _mainWindow = new DemoWindow( this );
        myWindow().getTextView().setDocument( _textDocument );
    }
	
    
    /**
	 * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs( final URL url ) {
        try {
            int length = _textDocument.getLength();
            String text = _textDocument.getText( 0, length );
            
            File file = new File( url.toURI() );
            if ( !file.exists() ) {
                file.createNewFile();
            }
            
            FileWriter writer = new FileWriter( file );
            writer.write( text, 0, text.length() );
            writer.flush();
            setHasChanges( false );
        }
        catch( BadLocationException exception ) {
			System.err.println( exception );
			Logger.getLogger("global").log( Level.WARNING, "Save Failed", exception );
			displayWarning( "Save Failed!", "Save Failed due to an internal exception!", exception );
        }
        catch( IOException exception ) {
			System.err.println( exception );
			Logger.getLogger("global").log( Level.WARNING, "Save Failed", exception );
			displayWarning( "Save Failed!", "Save Failed due to an internal exception!", exception );
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
    private DemoWindow myWindow() {
        return (DemoWindow)_mainWindow;
    }
    
	
    /** 
	* Instantiate a new PlainDocument that servers as the document for the text pane.
	* Create a handler of text actions so we can determine if the document has 
	* changes that should be saved.
	*/
    private void makeTextDocument() {
        _textDocument = new PlainDocument();
        _textDocument.addDocumentListener( new DocumentListener() {
            public void changedUpdate( final DocumentEvent event ) {
                setHasChanges( true );
            }
            public void removeUpdate( final DocumentEvent event ) {
                setHasChanges( true );
            }
            public void insertUpdate( final DocumentEvent event ) {
                setHasChanges( true );
            }
        });
    }	
    
    
    /**
	 * Provides the optional document menu bar.  If this method is not supplied, then the document will not have its own menu bar.
     * @return The menu definition properties file path in classpath notation
	 * @see ApplicationAdaptor#getPathToResource
     */
	protected String getCustomInternalMenuDefinitionResource() {
		return "docmenu.properties";
	}


    /**
	 * Register actions specific to this document instance. This code demonstrates how to define custom actions for
	 * menus for a particular document instance.  This method is optional.  
     * @param commander The commander with which to register the custom commands.
     */
    public void customizeCommands( final Commander commander ) {
        final Action t1Action = new AbstractAction( "do-t1" ) {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
                System.out.println( "Doing T1 for document:  " + getTitle() );
				Logger.getLogger("global").log( Level.INFO, "Doing T1..." );
            }
        };        
        commander.registerAction( t1Action );
		
        final Action t2Action = new AbstractAction( "do-t2" ) {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
                System.out.println( "Doing T2 for document:  " + getTitle() );
				Logger.getLogger("global").log( Level.INFO, "Doing T2..." );
            }
        };        
        commander.registerAction( t2Action );
		
        final Action t3Action = new AbstractAction( "do-t3" ) {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
                System.out.println( "Doing T3 for document:  " + getTitle() );
				Logger.getLogger("global").log( Level.INFO, "Doing T3..." );
            }
        };        
        commander.registerAction( t3Action );
	}
    
    
    /**
	 * Register document specific actions for the desktop menu.  You do so by registering 
	 * actions with the commander.  Those action instances should have a reference to this document so the action is 
	 * executed on the document when the action is activated.  The default implementation of this method does nothing.
     * @param commander The commander that manages commands.
     * @see Commander#registerAction(Action)
     */
    protected void customizeDesktopCommands( final Commander commander ) {
        final Action printAction = new AbstractAction( "print-document-name" ) {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
                System.out.println( "The document name: " + getTitle() );
				Logger.getLogger("global").log( Level.INFO, "Printing the document name: " + getTitle() );
            }
        };        
        commander.registerAction( printAction );
    }
}
