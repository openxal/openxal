/*
 * MainScan2D.java
 *
 * Created on July 31, 2003, 10:25 AM
 */

package xal.app.scan2d;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.extension.application.*;
import xal.extension.application.smf.AcceleratorApplication;

/**
 * MainScan2D is a concrete subclass of ApplicationAdaptor 
 * for two dimensional scan. 
 *
 * @author  shishlo
 */

public class MainScan2D extends ApplicationAdaptor {

    /** Constructor */
    public MainScan2D(){ }   

    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[] {"scan2d"};
    }
    
    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] {"scan2d"};
    }
    
    
    /**
     * Returns an instance of the one dimensional scan document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new ScanDocument2D();
    }
    
    /**
     * Returns an instance of the one dimensional scan document
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of the one dimensional scan document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new ScanDocument2D(url);
    }
    
    /**
     * Specifies the name of the one dimensional scan application.
     * @return Name of the application.
     */
    public String applicationName() {
        return "Scan2D";
    }
    
    
    /**
     * Activates the preference panel for the one dimensional scan.
     * @param document The document whose preferences are being changed.
     */
    public void editPreferences(XalDocument document){
        ((ScanDocument2D)document).editPreferences();
    }
    
    
    /**
     * Specifies whether the one dimensional scan will send 
     * standard output and error to the console.
     * @return Name of my application.
     */
    public boolean usesConsole() {
        String usesConsoleProperty = System.getProperty("usesConsole");
        if ( usesConsoleProperty != null ) {
            return Boolean.valueOf(usesConsoleProperty).booleanValue();
        }
        else {
            return true;
        }
    }

    /** The main method of the application. */
    static public void main(String[] args) {
		final MainScan2D appAdaptor = new MainScan2D();
		URL[] predefConfURLArr = null;


		if ( args.length == 0 ) {
			predefConfURLArr = new URL[0];
		}
		else {
			predefConfURLArr = new URL[args.length];
			for ( int index = 0; index < args.length; index++ ) {
				predefConfURLArr[index] = appAdaptor.getResourceURL( "config/" + args[index] );
			}
		}

		try {
			AcceleratorApplication.launch( appAdaptor, predefConfURLArr );
		}
		catch (Exception exception) {
			System.err.println(exception.getMessage());
			exception.printStackTrace();
			JOptionPane.showMessageDialog( null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE );
		}
    }
}
