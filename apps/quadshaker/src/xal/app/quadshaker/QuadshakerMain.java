/*
 *  QuadshakerMain.java
 *
 *  Created on May 24, 2005, 10:25 AM
 */
package xal.app.quadshaker;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.extension.application.*;

/**
 *  QuadshakerMain is a concrete subclass of ApplicationAdaptor for the
 *  Quad shaker application.
 *
 *@author     shishlo
 */

public class QuadshakerMain extends ApplicationAdaptor {

	/**
	 *  Constructor
	 */
	public QuadshakerMain() { }


	/**
	 *  Returns the text file suffixes of files this application can open.
	 *
	 *@return    Suffixes of readable files
	 */
	public String[] readableDocumentTypes() {
		return new String[]{"qsh"};
	}


	/**
	 *  Returns the text file suffixes of files this application can write.
	 *
	 *@return    Suffixes of writable files
	 */
	public String[] writableDocumentTypes() {
		return new String[]{"qsh"};
	}


	/**
	 *  Returns an instance of the Quadshaker application document.
	 *
	 *@return    An instance of Quadshaker application document.
	 */
	public XalDocument newEmptyDocument() {
		return new QuadshakerDocument();
	}


	/**
	 *  Returns an instance of the Quadshaker application document corresponding
	 *  to the specified URL.
	 *
	 *@param  url  The URL of the file to open.
	 *@return      An instance of an Quadshaker application document.
	 */
	public XalDocument newDocument(java.net.URL url) {
		return new QuadshakerDocument(url);
	}


	/**
	 *  Specifies the name of the Quadshaker application.
	 *
	 *@return    Name of the application.
	 */
	public String applicationName() {
		return "Quad Shaker";
	}


	/**
	 *  Activates the preference panel for the Quadshaker application.
	 *
	 *@param  document  The document whose preferences are being changed.
	 */
	public void editPreferences(XalDocument document) {
		((QuadshakerDocument) document).editPreferences();
	}


	/**
	 *  Specifies whether the Quadshaker application will send standard output
	 *  and error to the console.
	 *
	 *@return    true or false.
	 */
	public boolean usesConsole() {
		String usesConsoleProperty = System.getProperty("usesConsole");
		if(usesConsoleProperty != null) {
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
		final QuadshakerMain appAdaptor = new QuadshakerMain();
		URL[] predefConfURLArr = null;

		if(args.length == 0) {
			predefConfURLArr = new URL[0];
		}
		else {
			predefConfURLArr = new URL[args.length];
			for( int index = 0; index < args.length; index++ ) {
				predefConfURLArr[index] = appAdaptor.getResourceURL( "config/" + args[index] );
			}
		}

		try {
			Application.launch( appAdaptor, predefConfURLArr );
		}
		catch(Exception exception) {
			System.err.println(exception.getMessage());
			exception.printStackTrace();
			JOptionPane.showMessageDialog( null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE );
		}
	}
}

