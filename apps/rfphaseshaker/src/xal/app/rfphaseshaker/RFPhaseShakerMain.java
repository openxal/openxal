/*
 *  RFPhaseShakerMain.java
 *
 *  Created on Feb. 25 2009
 */
package xal.app.rfphaseshaker;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.extension.application.*;
import xal.extension.application.smf.AcceleratorApplication;

/**
 *  RFPhaseShakerMain is a concrete subclass of ApplicationAdaptor for the
 *  Quad shaker application.
 *
 *@author     shishlo
 */

public class RFPhaseShakerMain extends ApplicationAdaptor {

	/**
	 *  Constructor
	 */
	public RFPhaseShakerMain() { }


	/**
	 *  Returns the text file suffixes of files this application can open.
	 *
	 *@return    Suffixes of readable files
	 */
	public String[] readableDocumentTypes() {
		return new String[]{"rfsh"};
	}


	/**
	 *  Returns the text file suffixes of files this application can write.
	 *
	 *@return    Suffixes of writable files
	 */
	public String[] writableDocumentTypes() {
		return new String[]{"rfsh"};
	}


	/**
	 *  Returns an instance of the RFPhaseShaker application document.
	 *
	 *@return    An instance of RFPhaseShaker application document.
	 */
	public XalDocument newEmptyDocument() {
		return new RFPhaseShakerDocument();
	}


	/**
	 *  Returns an instance of the RFPhaseShaker application document corresponding
	 *  to the specified URL.
	 *
	 *@param  url  The URL of the file to open.
	 *@return      An instance of an RFPhaseShaker application document.
	 */
	public XalDocument newDocument(java.net.URL url) {
		return new RFPhaseShakerDocument(url);
	}


	/**
	 *  Specifies the name of the RFPhaseShaker application.
	 *
	 *@return    Name of the application.
	 */
	public String applicationName() {
		return "RF Phase Shaker";
	}


	/**
	 *  Activates the preference panel for the RFPhaseShaker application.
	 *
	 *@param  document  The document whose preferences are being changed.
	 */
	public void editPreferences(XalDocument document) {
		((RFPhaseShakerDocument) document).editPreferences();
	}


	/**
	 *  Specifies whether the RFPhaseShaker application will send standard output
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
		final RFPhaseShakerMain appAdaptor = new RFPhaseShakerMain();
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

