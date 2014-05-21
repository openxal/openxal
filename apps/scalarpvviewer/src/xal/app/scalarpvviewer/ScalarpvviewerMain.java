/*
 *  ScalarpvviewerMain.java
 * 
 *  Created on May 24, 2005, 10:25 AM
 */
package xal.app.scalarpvviewer;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.extension.application.*;

/**
 *  ScalarpvviewerMain is a concrete subclass of ApplicationAdaptor for the Scalarpvviewer
 *  application.
 *
 *@author     shishlo
 */

public class ScalarpvviewerMain extends ApplicationAdaptor {

    /**
     *  Constructor
     */
    public ScalarpvviewerMain() { }


    /**
     *  Returns the text file suffixes of files this application can open.
     *
     *@return    Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[]{"spv"};
    }


    /**
     *  Returns the text file suffixes of files this application can write.
     *
     *@return    Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[]{"spv"};
    }


    /**
     *  Returns an instance of the Scalarpvviewer application document.
     *
     *@return    An instance of Scalarpvviewer application document.
     */
    public XalDocument newEmptyDocument() {
        return new ScalarpvviewerDocument();
    }


    /**
     *  Returns an instance of the Scalarpvviewer application document corresponding
     *  to the specified URL.
     *
     *@param  url  The URL of the file to open.
     *@return      An instance of an Scalarpvviewer application document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new ScalarpvviewerDocument(url);
    }


    /**
     *  Specifies the name of the Scalarpvviewer application.
     *
     *@return    Name of the application.
     */
    public String applicationName() {
        return "FINGERPRINT";
    }


    /**
     *  Activates the preference panel for the Scalarpvviewer application.
     *
     *@param  document  The document whose preferences are being changed.
     */
    public void editPreferences(XalDocument document) {
        ((ScalarpvviewerDocument) document).editPreferences();
    }


    /**
     *  Specifies whether the Scalarpvviewer application will send standard output
     *  and error to the console.
     *
     *@return    true or false.
     */
    public boolean usesConsole() {
        String usesConsoleProperty = System.getProperty("usesConsole");
        if (usesConsoleProperty != null) {
            return Boolean.valueOf(usesConsoleProperty).booleanValue();
        } else {
            return true;
        }
    }


    /**
     *  The main method of the application.
     *
     *@param  args  The command line arguments
     */
    public static void main(String[] args) {
		final ScalarpvviewerMain appAdaptor = new ScalarpvviewerMain();
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
			Application.launch( appAdaptor, predefConfURLArr );
		}
		catch (Exception exception) {
			System.err.println(exception.getMessage());
			exception.printStackTrace();
			JOptionPane.showMessageDialog( null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE );
		}
    }
}

