/*
 *  InjDumpWizardMain.java
 *
 *   Created on October 10, 2007
 */
package xal.app.injdumpwizard;


import javax.swing.JOptionPane;

import xal.extension.application.Application;
import xal.extension.application.ApplicationAdaptor;
import xal.extension.application.XalDocument;


/**
 *  InjDumpWizardMain is a concrete subclass of ApplicationAdaptor for the
 *  Quad shaker application.
 *
 *@author     shishlo
 */

public class InjDumpWizardMain extends ApplicationAdaptor {

	/**
	 *  Constructor
	 */
	public InjDumpWizardMain() { }


	/**
	 *  Returns the text file suffixes of files this application can open.
	 *
	 *@return    Suffixes of readable files
	 */
	public String[] readableDocumentTypes() {
		return new String[]{"idw"};
	}


	/**
	 *  Returns the text file suffixes of files this application can write.
	 *
	 *@return    Suffixes of writable files
	 */
	public String[] writableDocumentTypes() {
		return new String[]{"idw"};
	}

    
    /** Indicates whether the welcome dialog should be displayed at launch. */
    public boolean showsWelcomeDialogAtLaunch() {
        return false;
    }


	/**
	 *  Returns an instance of the InjDumpWizard application document.
	 *
	 *@return    An instance of InjDumpWizard application document.
	 */
	public XalDocument newEmptyDocument() {
		return new InjDumpWizardDocument();
	}


	/**
	 *  Returns an instance of the InjDumpWizard application document corresponding
	 *  to the specified URL.
	 *
	 *@param  url  The URL of the file to open.
	 *@return      An instance of an InjDumpWizard application document.
	 */
	public XalDocument newDocument(java.net.URL url) {
		return new InjDumpWizardDocument(url);
	}


	/**
	 *  Specifies the name of the InjDumpWizard application.
	 *
	 *@return    Name of the application.
	 */
	public String applicationName() {
		return "Injection Dump Wizard";
	}


	/**
	 *  Activates the preference panel for the InjDumpWizard application.
	 *
	 *@param  document  The document whose preferences are being changed.
	 */
	public void editPreferences(XalDocument document) {
		((InjDumpWizardDocument) document).editPreferences();
	}


	/**
	 *  Specifies whether the InjDumpWizard application will send standard output
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
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		final InjDumpWizardMain appAdaptor = new InjDumpWizardMain();

		try {
			Application.launch( appAdaptor );
		}
		catch(Exception exception) {
			System.err.println(exception.getMessage());
			exception.printStackTrace();
			JOptionPane.showMessageDialog( null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE );
		}
	}
}

