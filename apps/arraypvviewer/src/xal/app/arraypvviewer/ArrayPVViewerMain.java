/*
 *  ArrayPVViewerMain.java
 *
 *  Created on July 31, 2003, 10:25 AM
 */
package xal.app.arraypvviewer;

import xal.extension.application.Application;
import xal.extension.application.ApplicationAdaptor;
import xal.extension.application.XalDocument;
import xal.extension.application.smf.AcceleratorApplication;

import java.net.URL;

import javax.swing.JOptionPane;

/**
 *  ArrayPVViewerMain is a concrete subclass of ApplicationAdaptor for the arrayPVViewer
 *  application.
 *
 *@author     shishlo
 *@version    July 12, 2004
 */

public class ArrayPVViewerMain extends ApplicationAdaptor {

    /**
     *  Constructor
     */
    public ArrayPVViewerMain() { }


    /**
     *  Returns the text file suffixes of files this application can open.
     *
     *@return    Suffixes of readable files
     */
    @Override
    public String[] readableDocumentTypes() {
        return new String[]{"apv"};
    }


    /**
     *  Returns the text file suffixes of files this application can write.
     *
     *@return    Suffixes of writable files
     */
    @Override
    public String[] writableDocumentTypes() {
        return new String[]{"apv"};
    }


    /**
     *  Returns an instance of the arrayPVViewer application document.
     *
     *@return    An instance of arrayPVViewer application document.
     */
    @Override
    public XalDocument newEmptyDocument() {
        return new ArrayPVViewerDocument();
    }


    /**
     *  Returns an instance of the arrayPVViewer application document corresponding
     *  to the specified URL.
     *
     *@param  url  The URL of the file to open.
     *@return      An instance of an arrayPVViewer application document.
     */
    @Override
    public XalDocument newDocument(java.net.URL url) {
        return new ArrayPVViewerDocument(url);
    }


    /**
     *  Specifies the name of the arrayPVViewer application.
     *
     *@return    Name of the application.
     */
    @Override
    public String applicationName() {
        return "Array_PV_Viewer";
    }


    /**
     *  Activates the preference panel for the arrayPVViewer application.
     *
     *@param  document  The document whose preferences are being changed.
     */
    @Override
    public void editPreferences(XalDocument document) {
        ((ArrayPVViewerDocument) document).editPreferences();
    }


    /**
     *  Specifies whether the arrayPVViewer application will send standard output
     *  and error to the console.
     *
     *@return    true or false.
     */
    @Override
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
		final ArrayPVViewerMain appAdaptor = new ArrayPVViewerMain();
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

