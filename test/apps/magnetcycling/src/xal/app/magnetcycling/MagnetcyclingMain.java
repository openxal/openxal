/*
 *  MagnetcyclingMain.java
 *
 *  Created on May 24, 2005, 10:25 AM
 */
package xal.app.magnetcycling;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.extension.application.*;

/**
 *  MagnetcyclingMain is a concrete subclass of ApplicationAdaptor for the
 *  Magnet Cycling application.
 *
 *@author     shishlo
 */

public class MagnetcyclingMain extends ApplicationAdaptor {

	/**
	 *  Constructor
	 */
	public MagnetcyclingMain() { }


	/**
	 *  Returns the text file suffixes of files this application can open.
	 *
	 *@return    Suffixes of readable files
	 */
	public String[] readableDocumentTypes() {
		return new String[]{"cyc"};
	}


	/**
	 *  Returns the text file suffixes of files this application can write.
	 *
	 *@return    Suffixes of writable files
	 */
	public String[] writableDocumentTypes() {
		return new String[]{"cyc"};
	}


	/**
	 *  Returns an instance of the Magnetcycling application document.
	 *
	 *@return    An instance of Magnetcycling application document.
	 */
	public XalDocument newEmptyDocument() {
		return new MagnetcyclingDocument();
	}


	/**
	 *  Returns an instance of the Magnetcycling application document corresponding
	 *  to the specified URL.
	 *
	 *@param  url  The URL of the file to open.
	 *@return      An instance of an Magnetcycling application document.
	 */
	public XalDocument newDocument(java.net.URL url) {
		return new MagnetcyclingDocument(url);
	}


	/**
	 *  Specifies the name of the Magnetcycling application.
	 *
	 *@return    Name of the application.
	 */
	public String applicationName() {
		return "Magnets Cycling";
	}


	/**
	 *  Activates the preference panel for the Magnetcycling application.
	 *
	 *@param  document  The document whose preferences are being changed.
	 */
	public void editPreferences(XalDocument document) {
		((MagnetcyclingDocument) document).editPreferences();
	}


	/**
	 *  Specifies whether the Magnetcycling application will send standard output
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
	public static void main( final String[] args ) {

		final MagnetcyclingMain appAdaptor = new MagnetcyclingMain();
		URL[] predefConfURLArr;

		// if no args are supplied, load the HEBT/Ring configuration
		if( args.length == 0 ) {
			predefConfURLArr = new URL[1];
			predefConfURLArr[0] = appAdaptor.getResourceURL( "config/HEBT_Ring_ini.cyc" );
		}
		else {		// load the specified configurations
			predefConfURLArr = new URL[args.length];
			for( int index = 0; index < args.length; index++ ) {
				predefConfURLArr[index] = appAdaptor.getResourceURL( "config/" + args[index] );
			}
		}

		try {
			Application.launch( appAdaptor, predefConfURLArr );
		}
		catch(Exception exception) {
			System.err.println( exception.getMessage() );
			exception.printStackTrace();
			JOptionPane.showMessageDialog( null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE );
		}
	}
}

