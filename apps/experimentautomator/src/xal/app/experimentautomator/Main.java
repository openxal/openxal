package xal.app.experimentautomator;

import java.util.logging.Level;
import java.util.logging.Logger;

import xal.app.experimentautomator.core.EADocument;
import xal.extension.application.Application;
import xal.extension.application.ApplicationAdaptor;
import xal.extension.application.XalDocument;
import xal.extension.application.smf.AcceleratorApplication;

/**
 * This is the main class for the experiment automator application.
 * 
 * @version   0.1  30 Nov 2014
 * @author robinnewhouse
 */
public class Main extends ApplicationAdaptor {

	// --------- Document management -------------------------------------------

	/**
	 * Returns the text file suffixes of files this application can open.
	 * 
	 * @return Suffixes of readable files
	 */
	public String[] readableDocumentTypes() {
		return new String[] { "txt", "text" };
	}

	/**
	 * Returns the text file suffixes of files this application can write.
	 * 
	 * @return Suffixes of writable files
	 */
	public String[] writableDocumentTypes() {
		return new String[] { "txt", "text" };
	}

	/**
	 * Implement this method to return an instance of my custom document.
	 * 
	 * @return An instance of my custom document.
	 */
	public XalDocument newEmptyDocument() {
		return new EADocument();
	}

	/**
	 * Implement this method to return an instance of my custom document
	 * corresponding to the specified URL.
	 * 
	 * @param url
	 *            The URL of the file to open.
	 * @return An instance of my custom document.
	 */
	public XalDocument newDocument(java.net.URL url) {
		return new EADocument(url);
	}

	// --------- Global application management ---------------------------------

	/**
	 * Specifies the name of my application.
	 * 
	 * @return Name of my application.
	 */
	public String applicationName() {
		return "Experiment Automator";
	}

	/**
	 * Constructor
	 */
	public Main() {

	}

	/**
	 * Activates the preference panel for the one dimensional scan.
	 * 
	 * @param document
	 *            The document whose preferences are being changed.
	 */
	public void editPreferences(XalDocument document) {
		((EADocument) document).editPreferences();
	}

	/** The main method of the application. */
	static public void main(String[] args) {
		try {
			Logger.getLogger("global").log(Level.INFO,
					"Starting application...");
			setOptions(args);
			AcceleratorApplication.launch(new Main());
		} catch (Exception exception) {
			Logger.getLogger("global").log(Level.SEVERE,
					"Error starting application.", exception);
			System.err.println(exception.getMessage());
			exception.printStackTrace();
			Application.displayApplicationError("Launch Exception",
					"Launch Exception", exception);
			System.exit(-1);
		}
	}
}
