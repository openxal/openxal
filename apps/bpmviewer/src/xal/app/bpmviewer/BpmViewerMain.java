/*
 *  BpmViewerMain.java
 *
 *  Created on July 31, 2003, 10:25 AM
 */
package xal.app.bpmviewer;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.extension.application.*;
import xal.extension.application.smf.AcceleratorApplication;

/**
 *  BpmViewerMain is a concrete subclass of ApplicationAdaptor for the bpmViewer
 *  application.
 *
 *@author     shishlo
 *@version    July 12, 2004
 */

public class BpmViewerMain extends ApplicationAdaptor {

    /**
     *  Constructor
     */
    public BpmViewerMain() { }


    /**
     *  Returns the text file suffixes of files this application can open.
     *
     *@return    Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[]{"bpm"};
    }


    /**
     *  Returns the text file suffixes of files this application can write.
     *
     *@return    Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[]{"bpm"};
    }


    /**
     *  Returns an instance of the bpmViewer application document.
     *
     *@return    An instance of bpmViewer application document.
     */
    public XalDocument newEmptyDocument() {
        return new BpmViewerDocument();
    }


    /**
     *  Returns an instance of the bpmViewer application document corresponding
     *  to the specified URL.
     *
     *@param  url  The URL of the file to open.
     *@return      An instance of an bpmViewer application document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new BpmViewerDocument(url);
    }


    /**
     *  Specifies the name of the bpmViewer application.
     *
     *@return    Name of the application.
     */
    public String applicationName() {
        return "BPM_Viewer";
    }


    /**
     *  Activates the preference panel for the bpmViewer application.
     *
     *@param  document  The document whose preferences are being changed.
     */
    public void editPreferences(XalDocument document) {
        ((BpmViewerDocument) document).editPreferences();
    }


    /**
     *  Specifies whether the bpmViewer application will send standard output
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
		final BpmViewerMain appAdaptor = new BpmViewerMain();
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

