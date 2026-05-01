/*
 * QueryDocument.java
 *
 * Created on Tue Mar 30 11:12:14 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.dbbrowser;

import xal.extension.application.*;

import java.net.URL;
import java.io.*;
import javax.swing.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.sql.*;


/**
 * QueryDocument
 *
 * @author  tap
 */
public class QueryDocument  extends XalDocument {
    /** The document for the SQL text pane in the main window. */
    protected PlainDocument sqlDocument;
	
	/** default database connection */
	final protected Connection DEFAULT_DATABASE_CONNECTION;
	
	
	/** Create a new empty document */
    public QueryDocument() {
        this( null );
    }
	
	
	/** Create a new empty document */
    public QueryDocument( final Connection databaseConnection ) {
        this( null, databaseConnection );
    }
		
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public QueryDocument( final java.net.URL url, final Connection databaseConnection ) {
		DEFAULT_DATABASE_CONNECTION = databaseConnection;
		
        setSource( url );
        makeSqlDocument();
        
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
            
            sqlDocument.insertString(0, stringBuffer.toString(), null);
            setHasChanges(false);
        }
        catch( java.io.IOException exception ) {
            throw new RuntimeException( exception.getMessage() );
        }
        catch( BadLocationException exception ) {
            throw new RuntimeException( exception.getMessage() );
        }
    }
    
    
    /**
     * Subclasses should implement this method to return the array of file 
     * suffixes identifying the files that can be written by the document.
	 * By default this method returns the same types as specified by the 
	 * application adaptor.
     * @return An array of file suffixes corresponding to writable files
     */
    public String[] writableDocumentTypes() {
		return new String[] {"sql"};
	}
    
    
    /**
     * Make a main window by instantiating the my custom window.
     */
    public void makeMainWindow() {
        mainWindow = new QueryWindow(this);
        queryWindow().getQueryView().setDocument(sqlDocument);        
    }


    /**
	 * Subclasses should override this method if this document should use a menu definition
	 * other than the default specified in application adaptor.  The document menu inherits the
	 * application menu definition.  This custom path allows the document to modify the
	 * application wide definitions for this document.  By default this method returns null.
     * @return The menu definition properties file name
     */
    public String getCustomMenuDefinitionResource() {
		return "query_menu.properties";
    }


	/**
	 * Convenience method for getting the query window cast to the QueryWindow class
	 * @return this document's query window
	 */
	protected QueryWindow queryWindow() {
		return (QueryWindow)mainWindow;
	}
	
	
	/**
	 * Get the default database connection.
	 * @return the default database connection
	 */
	public Connection getDefaultDatabaseConnection() {
		return DEFAULT_DATABASE_CONNECTION;
	}
    
	
    /** 
     * Instantiate a new PlainDocument that servers as the document for the text pane.
     * Create a handler of text actions so we can determine if the document has 
     * changes that should be saved.
     */
    private void makeSqlDocument() {
        sqlDocument = new PlainDocument();
        sqlDocument.addDocumentListener(new DocumentListener() {
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
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs( final URL url ) {
        try {
            int length = sqlDocument.getLength();
            String text = sqlDocument.getText( 0, length );
            
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
            displayWarning( "Save Failed!", "Save Failed due to an internal exception!", exception );
        }
        catch(IOException exception) {
            System.err.println( exception );
            displayWarning( "Save Failed!", "Save Failed due to an internal exception!", exception );
        }
		catch( java.net.URISyntaxException exception ) {
            System.err.println( exception );
            displayWarning( "Save Failed!", "Save Failed due to an internal exception!", exception );
		}
    }
}

