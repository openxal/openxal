/*
 * Wire.java
 */

package xal.app.wirescan;

import javax.swing.JOptionPane;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.smf.impl.*;

/**
 * This is the main class for the wirescanner application.
 *
 * @author	S. Bunch
 * @version	1.0
 */
public class Wire extends ApplicationAdaptor {
	/**
	 * Returns the text file suffixes of files this application can open.
	 * @return Suffixes of readable files
	 */
	public String[] readableDocumentTypes() {
		return new String[] {"wss"};
	}

	/**
	 * Returns the text file suffixes of files this application can write.
	 * @return Suffixes of writable files
	 */
	public String[] writableDocumentTypes() {
		return new String[] {"wss"};
	}

	/**
	 * Returns an instance of a wire document.
	 * @return An instance of a wire document.
	 */
	public XalDocument newEmptyDocument() {
		return new WireDoc();
	}

	/**
	 * Returns an instance of a wire document 
	 * corresponding to the specified URL.
	 * @param url The URL of the file to open.
	 * @return An instance of a wire document.
	 */
	public XalDocument newDocument(java.net.URL url) {
		return new WireDoc(url);
	}

	/**
	 * Specifies the name of the wirescanner application.
	 * @return Name of the wirescanner application.
	 */
	public String applicationName() {
		return "Wirescanner Application";
	}

	/**
	 * Specifies whether I want to send standard output and error to the console.
	 * 
	 * @return true or false.
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

	/** Capture the application launched event and print it */
	public void applicationFinishedLaunching() {
		System.out.println("Application finished launching...");
	}

	/** The main method of the application. */
	static public void main(String[] args) {
		try {
			System.out.println("Launching application...");
			AcceleratorApplication.launch( new Wire() );
		}
		catch(Exception exception) {
			System.err.println( exception.getMessage() );
			exception.printStackTrace();
			JOptionPane.showMessageDialog(null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
		}
	}
}
