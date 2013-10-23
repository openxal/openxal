/*
 *  BeamAtFoilMain.java
 *
 *  Created on July 18, 2008
 */
package xal.app.beamatfoil;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.extension.application.*;

/**
 *  BeamAtFoilMain is a concrete subclass of ApplicationAdaptor for the
 *  Quad shaker application.
 *
 *@author     shishlo
 */

public class BeamAtFoilMain extends ApplicationAdaptor {

	/**
	 *  Constructor
	 */
	public BeamAtFoilMain() { }


	/**
	 *  Returns the text file suffixes of files this application can open.
	 *
	 *@return    Suffixes of readable files
	 */
	public String[] readableDocumentTypes() {
		return new String[]{"baf"};
	}


	/**
	 *  Returns the text file suffixes of files this application can write.
	 *
	 *@return    Suffixes of writable files
	 */
	public String[] writableDocumentTypes() {
		return new String[]{"baf"};
	}


	/**
	 *  Returns an instance of the BeamAtFoil application document.
	 *
	 *@return    An instance of BeamAtFoil application document.
	 */
	public XalDocument newEmptyDocument() {
		return new BeamAtFoilDocument();
	}


	/**
	 *  Returns an instance of the BeamAtFoil application document corresponding
	 *  to the specified URL.
	 *
	 *@param  url  The URL of the file to open.
	 *@return      An instance of an BeamAtFoil application document.
	 */
	public XalDocument newDocument(java.net.URL url) {
		return new BeamAtFoilDocument(url);
	}


	/**
	 *  Specifies the name of the BeamAtFoil application.
	 *
	 *@return    Name of the application.
	 */
	public String applicationName() {
		return "Beam At Foil";
	}


	/**
	 *  Activates the preference panel for the BeamAtFoil application.
	 *
	 *@param  document  The document whose preferences are being changed.
	 */
	public void editPreferences(XalDocument document) {
		((BeamAtFoilDocument) document).editPreferences();
	}


	/**
	 *  Specifies whether the BeamAtFoil application will send standard output
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

		if(args.length == 0) {

			BeamAtFoilMain doc = new BeamAtFoilMain();

			try {
				Application.launch(doc);
			} catch(Exception exception) {
				System.err.println(exception.getMessage());
				exception.printStackTrace();
				JOptionPane.showMessageDialog(null, exception.getMessage(),
						exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
			}
			return;
		}

		BeamAtFoilMain doc = new BeamAtFoilMain();

		URL[] predefConfURLArr = new URL[args.length];

		for(int i = 0; i < args.length; i++) {
			predefConfURLArr[i] = doc.getClass().getResource("config/" + args[i]);
		}

		try {
			Application.launch(doc, predefConfURLArr);
		} catch(Exception exception) {
			System.err.println(exception.getMessage());
			exception.printStackTrace();
			JOptionPane.showMessageDialog(null, exception.getMessage(),
					exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
		}
	}
}

